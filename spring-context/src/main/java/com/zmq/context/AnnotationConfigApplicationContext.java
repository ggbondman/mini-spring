package com.zmq.context;

import com.google.common.base.Strings;
import com.zmq.annotation.*;
import com.zmq.beans.BeanDefinition;
import com.zmq.beans.ClassMetaData;
import com.zmq.beans.DefaultClassMetaData;
import com.zmq.exception.*;
import com.zmq.processor.BeanDefinitionPostProcessor;
import com.zmq.processor.BeanPostProcessor;
import com.zmq.property.PropertyResolver;
import com.zmq.resource.ResourceResolver;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static java.lang.System.out;


/**
 * @author zmq
 */
public class AnnotationConfigApplicationContext implements ConfigurableApplicationContext {

    private final static String SPRING_SCAN_PATH = "com.zmq";

    protected final PropertyResolver propertyResolver;

    @Getter
    protected final Map<String, BeanDefinition> beanDefinitions = new HashMap<>();

    protected final Map<String, Object> beans = new HashMap<>();

    protected final Set<String> creatingBean = new HashSet<>();
    protected final List<String> beanDefinitionNames = new ArrayList<>();

    List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) throws BeanDefinitionException {
        this.propertyResolver = propertyResolver;
        ApplicationContextUtils.setApplicationContext(this);
        Set<String> beanClassNames = scanForClassNames(configClass);
        createBeanDefinitions(beanClassNames);
        applyBeanDefinitionPostProcessors();
        createConfigurationBeans();
        registerBeanPostProcessors();
        createNormalBeans();
        injectBeans();
        initializeBeans();
        this.beanDefinitionNames.addAll(this.beanDefinitions.keySet());
        for (Map.Entry<String, BeanDefinition> entry : this.beanDefinitions.entrySet()) {
            this.beans.put(entry.getKey(), entry.getValue().getInstance());
        }
    }

    private void initializeBeans() {
        this.beanDefinitions.values().forEach(def -> {
            Object object = getOriginTarget(def.getName(), def.getInstance());
            if (def.getInitMethod()!=null){
                try {
                    def.getInitMethod().invoke(object);
                } catch (ReflectiveOperationException e){
                    throw new BeanInitializingException("Error initializing bean with name '" + def.getName() + "' through @PostConstruct method '" + def.getInitMethod().toGenericString() + "': " + e.getMessage(), e);
                }
            }
            if (!Strings.isNullOrEmpty(def.getInitMethodName())) {
                try {
                    Method method = def.getBeanClass().getMethod(def.getInitMethodName());
                    method.invoke(object);
                } catch (ReflectiveOperationException e) {
                    throw new BeanInitializingException("Error initializing bean with name '" + def.getName() + "' through init-method '" + def.getInitMethodName() + "': " + e.getMessage(), e);
                }
            }
        });
    }

    private Object getOriginTarget(String beanName, Object bean) {
        List<BeanPostProcessor> reversed = this.beanPostProcessors.reversed();
        for (BeanPostProcessor beanPostProcessor : reversed) {
            bean = beanPostProcessor.getOriginTarget(bean, beanName);
        }
        return bean;
    }


    void applyBeanDefinitionPostProcessors() {
        List<BeanDefinitionPostProcessor> processors = new ArrayList<>();
        this.beanDefinitions.values().stream().filter(BeanDefinition::isBeanDefinitionPostProcessor).forEach(def -> processors.add((BeanDefinitionPostProcessor) createBeanAsEarlySingleton(def)));
        this.beanDefinitions.values().forEach(def -> {
            if (def.getInstance() == null) {
                for (BeanDefinitionPostProcessor processor : processors) {
                    processor.invokeBeanDefinitionPostProcessor(def);
                }
            }
        });

    }

    /**
     * 实例化配置bean
     */
    void createConfigurationBeans() {
        this.beanDefinitions.values().stream().filter(BeanDefinition::isConfiguration).forEach(this::createBeanAsEarlySingleton);
    }

    /**
     * 创建并注册BeanPostProcessors
     */
    void registerBeanPostProcessors() {
        this.beanDefinitions.values().stream().filter(BeanDefinition::isBeanPostProcessor).forEach(def -> this.beanPostProcessors.add((BeanPostProcessor) createBeanAsEarlySingleton(def)));
        this.beanPostProcessors = this.beanPostProcessors.stream().sorted(Comparator.comparingInt(BeanPostProcessor::getOrder)).toList();
    }

    /**
     * 实例化其他普通Bean
     */
    void createNormalBeans() {
        this.beanDefinitions.values().stream().filter(beanDefinition -> beanDefinition.getInstance() == null).forEach(this::createBeanAsEarlySingleton);
    }

    /**
     * 根据字段和setter方法注入Bean
     */
    void injectBeans() {
        this.beanDefinitions.values().forEach(this::injectBeans);
    }

    // 为Bean内部的字段和setter方法注入依赖
    public void injectBeans(BeanDefinition def) {

        ClassMetaData classMetaData = new DefaultClassMetaData(def.getBeanClass());
        Set<Member> members = new HashSet<>();
        if (classMetaData.hasFieldAnnotation(Autowired.class) || classMetaData.hasFieldAnnotation(Value.class)) {
            Field[] autowiredField = classMetaData.getFieldsByAnnotation(Autowired.class);
            Field[] valuedField = classMetaData.getFieldsByAnnotation(Value.class);
            members.addAll(List.of(autowiredField));
            members.addAll(List.of(valuedField));
        }
        if (classMetaData.hasMethodAnnotation(Autowired.class) || classMetaData.hasMethodAnnotation(Value.class)) {
            Method[] autowiredMethods = classMetaData.getMethodsByAnnotation(Autowired.class);
            Method[] valuedMethods = classMetaData.getMethodsByAnnotation(Value.class);
            members.addAll(List.of(autowiredMethods));
            members.addAll(List.of(valuedMethods));
        }
        try {
            for (Member member : members) {
                doInjectBean(def, member);
            }
        } catch (ReflectiveOperationException e) {
            throw new BeanCreationException(e);
        }
    }

    public void doInjectBean(BeanDefinition def, Member member) throws InvocationTargetException, IllegalAccessException {
        checkInjectedFieldOrMethod(member);
        Object object = getOriginTarget(def.getName(), def.getInstance());
        Autowired autowired = null;
        Value value = null;
        Class<?> requiredType = null;
        String dependencyBeanName = null;
        Field field = null;
        Method method = null;
        if (member instanceof Method m) {
            method = m;
            autowired = method.getAnnotation(Autowired.class);
            value = method.getAnnotation(Value.class);
            Parameter[] parameters = method.getParameters();
            if (parameters.length != 1) {
                throw new BeanDefinitionException("Cannot inject a non-setter method " + method.getName() + " for bean '" + def.getName() + "': " + def.getBeanClass().getName());
            }
            dependencyBeanName = parameters[0].getName();
            requiredType = parameters[0].getType();
        } else {
            if (member instanceof Field f) {
                field = f;
                autowired = field.getAnnotation(Autowired.class);
                value = field.getAnnotation(Value.class);
                requiredType = field.getType();
                field.setAccessible(true);
                dependencyBeanName = field.getName();
            }
        }
        if (autowired != null && value != null) {
            throw new BeanDefinitionException("Cannot specify both @Autowired and @Value when inject " + def.getBeanClass().getSimpleName() + "." + member.getName() + " for bean '" + def.getName() + "': " + def.getBeanClass().getName());
        }
        Object arg = value != null ? this.propertyResolver.getProperty(value.value(), requiredType) : getAutowiredBean(autowired.required(), requiredType, dependencyBeanName, false);
        if (method != null) {
            out.println("Method injection: " + def.getBeanClass().getName() + "." + method.toGenericString());
            method.invoke(object, arg);
        } else {
            out.println("Field injection: " + def.getBeanClass().getName() + "." + field.toGenericString());
            field.set(object, arg);
        }
        // 调用BeanPostProcessor处理Bean
        def.setInstance(applyBeanPostProcessorsAfterInitialization(def.getInstance(), def.getName()));

    }

    void checkInjectedFieldOrMethod(Member m) {
        int mod = m.getModifiers();
        if (m instanceof Method && !Modifier.isPublic(mod)) {
            throw new BeanDefinitionException("Cannot inject non public method: " + m);
        }
        if (Modifier.isStatic(mod)) {
            throw new BeanDefinitionException("Cannot inject static field: " + m);
        }
        if (Modifier.isFinal(mod)) {
            if (m instanceof Field field) {
                throw new BeanDefinitionException("Cannot inject final field: " + field);
            }
            if (m instanceof Method) {
                out.println("Inject final method should be careful because it is not called on target bean when bean is proxied and may cause NullPointerException.");
            }
        }

    }

    /**
     * 实例化一个Bean，对于构造器和工厂方法的参数会进行依赖注入，但不进行其他字段和方法级别的注入。
     * 如果创建的Bean不是Configuration，则在构造方法/工厂方法中注入的依赖Bean会自动创建。
     *
     * @param def Bean的定义
     * @return {@link Object} 实例化完成的Bean，但未进行初始化
     */
    @Override
    public Object createBeanAsEarlySingleton(BeanDefinition def) {
        checkDuplicateBean(def.getName());
        // 创建方式：构造方法或者工厂方法
        Executable createFn = def.getFactoryBeanName() == null ? getSuitableConstructor(def.getBeanClass()) : def.getFactoryMethod();
        // 创建参数
        Parameter[] parameters = createFn.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            // 获取@Value注解
            Annotation annotation = getEligibleBeanAnnotation(def, parameters[i]);
            if (annotation instanceof Value value) {
                // 通过@Value注解注入
                args[i] = this.propertyResolver.getProperty(value.value(), parameters[i].getType());
            } else {
                try {
                    args[i] = getAutowiredBean(annotation == null || ((Autowired) annotation).required(), parameters[i].getType(), parameters[i].getName(), def.getFactoryMethod() != null);
                } catch (UnsatisfiedDependencyException e) {
                    throw new UnsatisfiedDependencyException("Error creating bean with name '" + def.getName() + "': Unsatisfied dependency expressed through '" + createFn.getName() + "' parameter " + i + ": " + e.getMessage());
                }
            }
        }
        // 创建bean实例
        Object instance;
        if (createFn instanceof Constructor<?> constructor) {
            // 用构造方法创建
            try {
                instance = constructor.newInstance(args);
            } catch (Exception e) {
                throw new BeanCreationException("Error creating bean with name '" + def.getName() + "': " + e);
            }
        } else {
            // 用@Bean工厂方法创建
            Method method = (Method) createFn;
            try {
                BeanDefinition factoryBeanDefinition = findBeanDefinition(def.getFactoryBeanName());
                if (factoryBeanDefinition==null){
                    throw new BeanCreationException("Error creating bean with name '" + def.getName() + "': the factory bean '"+def.getFactoryBeanName()+"' not found");
                }
                Object factoryInstance = factoryBeanDefinition.getInstance();
                instance = method.invoke(factoryInstance, args);
            } catch (Exception e) {
                throw new BeanCreationException("Error creating bean with name '" + def.getName() + "': " + e);
            }
        }
        // 调用BeanPostProcessor处理Bean，包括生成代理对象
        instance = applyBeanPostProcessorsBeforeInitialization(instance, def.getName());
        def.setInstance(instance);
        return instance;
    }

    private Object getAutowiredBean(boolean required, Class<?> requiredType, String beanName, boolean isConfiguration) {
        BeanDefinition beanDefinition = findBeanDefinition(beanName,requiredType);
        if (beanDefinition == null) {
            if (required) {
                throw new UnsatisfiedDependencyException("Dependency Bean with type '" + requiredType + "' not found");
            }else {
                return null;
            }
        }
        if (beanDefinition.getInstance() == null && isConfiguration) {
            return createBeanAsEarlySingleton(beanDefinition);
        } else {
            return beanDefinition.getInstance();
        }
    }

    public Object applyBeanPostProcessorsBeforeInitialization(Object instance, String beanName) {
        for (BeanPostProcessor beanPostProcessor : this.beanPostProcessors) {
            Object processed = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            if (processed == null) {
                throw new BeanCreationException("PostBeanProcessor returns null when process bean '" + beanName + "' by " + beanPostProcessor);
            }
            // 如果一个BeanPostProcessor替换了原始Bean，则更新Bean的引用
            if (instance != processed) {
                out.println("Bean '" + beanName + "' was replaced by post processor " + beanPostProcessor + ".");
                instance = processed;
            }
        }
        return instance;
    }

    public Object applyBeanPostProcessorsAfterInitialization(Object instance, String beanName) {
        for (BeanPostProcessor beanPostProcessor : this.beanPostProcessors) {
            Object processed = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            if (processed == null) {
                throw new BeanCreationException("PostBeanProcessor returns null when process bean '" + beanName + "' by " + beanPostProcessor);
            }
            // 如果一个BeanPostProcessor替换了原始Bean，则更新Bean的引用
            if (instance != processed) {
                out.println("Bean '" + beanName + "' was replaced by post processor " + beanPostProcessor + ".");
                instance = processed;
            }
        }
        return instance;
    }



    Annotation getEligibleBeanAnnotation(BeanDefinition def, Parameter parameter) {
        // 获取@Value注解
        Value value = parameter.getAnnotation(Value.class);
        // 获取@Autowired注解
        Autowired autowired = parameter.getAnnotation(Autowired.class);
        if (def.isConfiguration() && autowired != null) {
            throw new BeanCreationException(
                    "Cannot specify @Autowired when create @Configuration bean '" + def.getName() + "': " + def.getBeanClass().getName() + ".");
        }
        // BeanPostProcessor和BeanDefinitionPostProcessor不能依赖其他Bean，不允许使用@Autowired创建
        if ((def.isBeanPostProcessor() || def.isBeanDefinitionPostProcessor()) && autowired != null) {
            throw new BeanCreationException(
                    "Cannot specify @Autowired when create BeanPostProcessor or BeanDefinitionPostProcessor'" + def.getName() + "': " + def.getBeanClassName() + ".");
        }
        // 参数需要@Value或@Autowired两者之一，不能同时拥有
        if (value != null && autowired != null) {
            throw new BeanCreationException("Cannot specify both @Autowired and @Value when create bean '" + def.getName() + "': " + def.getBeanClass().getName());
        }
        if (value != null) {
            return value;
        }
        if (autowired != null) {
            return autowired;
        }
        return null;
    }

    /**
     * 检测重复实例化Bean导致的循环依赖
     *
     * @param beanName Bean的名称
     */
    private void checkDuplicateBean(String beanName) {

        if (!this.creatingBean.add(beanName)) {
            throw new UnsatisfiedDependencyException("Duplicate create bean '" + beanName + "'");
        }
    }

    /**
     * 获取实例化Bean所需要的构造器，默认构造器为无参构造器，如果没有无参构造器，则选择唯一的有参构造器。
     * 如果即没有无参构造器，又有多个有参构造器存在，则抛出异常{@link BeanCreationException}
     *
     * @param beanType Bean所属的类
     * @return {@link Constructor}<{@link ?}> 实例化Bean所需要的构造器
     */
    public Constructor<?> getSuitableConstructor(Class<?> beanType) {
        Constructor<?>[] constructors = beanType.getConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        Constructor<?>[] array = Arrays.stream(constructors).filter(constructor -> constructor.getParameterCount() == 0).toArray(Constructor<?>[]::new);
        if (array.length == 0) {
            throw new BeanCreationException("Failed to instantiate [" + beanType.getName() + "]: No default constructor found");
        }
        return array[0];
    }

    @Override
    public List<BeanDefinition> findBeanDefinitions(Class<?> type) {
        return this.beanDefinitions.values().parallelStream().filter(beanDefinition -> type.isAssignableFrom(beanDefinition.getBeanClass())).toList();
    }

    public BeanDefinition findRequiredBeanDefinition(Class<?> type) {
        List<BeanDefinition> defs = findBeanDefinitions(type);
        if (defs.isEmpty()) {
            return null;
        }
        if (defs.size() == 1) {
            return defs.getFirst();
        }
        // more than 1 beans, require @Primary:
        List<BeanDefinition> primaryDefs = defs.stream().filter(BeanDefinition::isPrimary).toList();
        if (primaryDefs.size() == 1) {
            return primaryDefs.getFirst();
        }
        if (primaryDefs.isEmpty()) {
            throw new NoUniqueBeanDefinitionException("No bean with type '" + type.getName() + "' found, but no @Primary specified.");
        } else {
            throw new NoUniqueBeanDefinitionException("Multiple bean with type '" + type.getName() + "' found, and multiple @Primary specified.");
        }
    }

    @Override
    public BeanDefinition findBeanDefinition(String name, Class<?> requiredType) {
        List<BeanDefinition> candidateDefs = findBeanDefinitions(requiredType);
        if (candidateDefs.isEmpty()) {
            return null;
        }
        if (candidateDefs.size() == 1) {
            // 指定类型的beanDefinition只有一个，就返回这个beanDefinition
            return candidateDefs.getFirst();
        }
        List<BeanDefinition> primaryDefs = new ArrayList<>();
        for (BeanDefinition candidateDef : candidateDefs) {
            if (candidateDef.isPrimary()) {
                primaryDefs.add(candidateDef);
            }
            if (candidateDef.getName().equals(name)) {
                // 找到指定name的beanDefinition则直接返回该beanDefinition
                return candidateDef;
            }
        }
        if (primaryDefs.size() == 1) {
            return primaryDefs.getFirst();
        }
        if (primaryDefs.isEmpty()) {
            throw new NoUniqueBeanDefinitionException("Multiple bean with type '" + requiredType + "' found, but no @Primary specified");
        } else {
            throw new NoUniqueBeanDefinitionException("Multiple bean with type '" + requiredType + "' found, and multiple @Primary specified");

        }
    }

    public BeanDefinition findBeanDefinition(String name) {
        return this.beanDefinitions.get(name);
    }


    // 创建Bean的定义，针对@Component类、@Configuration类以及@Configuration类中的@Bean工厂方法
    private void createBeanDefinitions(Set<String> classNames) throws BeanDefinitionException {
        for (String className : classNames) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            ClassMetaData classMetaData = new DefaultClassMetaData(clazz);
            if (classMetaData.isAnnotation()) {
                continue;
            }
            if (classMetaData.isAnnotation() || classMetaData.isEnum() || classMetaData.isInterface() || classMetaData.isRecord() || !classMetaData.hasAnnotation(Component.class)) {
                continue;
            }
            createBeanDefinition(classMetaData);
        }
    }

    private void createBeanDefinition(ClassMetaData classMetaData) {
        if (classMetaData.hasAnnotation(Component.class)) {
            String beanName = classMetaData.getAnnotation(Component.class).value();
            if (Strings.isNullOrEmpty(beanName)) {
                beanName = StringUtils.uncapitalize(classMetaData.getClassName());
            }
            // 获取该类的修饰符，如果是抽象类或者私有类，则抛出异常
            BeanDefinition def = doCreateBeanDefinition(beanName, classMetaData);
            addBeanDefinition(def);
            if (def.isConfiguration()) {
                scanFactoryMethods(def, classMetaData);
            }
        }
        if (classMetaData.hasAnnotation(Import.class)) {
            List<Import> importList = classMetaData.getAnnotations(Import.class);
            for (Import anImport : importList) {
                Class<?>[] classes = anImport.value();
                for (Class<?> aClass : classes) {
                    importBeanDefinition(aClass);
                }
            }
        }
    }

    private BeanDefinition doCreateBeanDefinition(String beanName, ClassMetaData classMetaData) {
        if (classMetaData.isAbstract()) {
            throw new BeanDefinitionException("@Component class " + classMetaData.getIntrospectedClass().getName() + " must not be abstract.");
        }
        if (classMetaData.isPrivate()) {
            throw new BeanDefinitionException("@Component class " + classMetaData.getIntrospectedClass().getName() + " must not be private.");
        }
        if (classMetaData.hasAnnotation(Configuration.class) && BeanPostProcessor.class.isAssignableFrom(classMetaData.getIntrospectedClass())) {
            throw new BeanDefinitionException("@Configuration class '" + classMetaData.getIntrospectedClass().getName() + "' cannot be BeanPostProcessor.");
        }
        return new BeanDefinition(beanName, classMetaData);
    }


    private void importBeanDefinition(Class<?> clazz) {
        out.println("import bean: " + clazz.getName());
        ClassMetaData classMetaData = new DefaultClassMetaData(clazz);
        BeanDefinition def = doCreateBeanDefinition(StringUtils.uncapitalize(classMetaData.getClassName()), classMetaData);
        addBeanDefinition(def);
        if (def.isConfiguration()) {
            scanFactoryMethods(def, classMetaData);
        }
        if (classMetaData.hasAnnotation(Import.class)) {
            List<Import> imports = classMetaData.getAnnotations(Import.class);
            for (Import anImport : imports) {
                for (Class<?> aClass : anImport.value()) {
                    importBeanDefinition(aClass);
                }
            }
        }
    }


    /**
     * Check and add bean definitions.
     */
    void addBeanDefinition(BeanDefinition def) throws BeanDefinitionException {
        if (beanDefinitions.put(def.getName(), def) != null) {
            throw new BeanDefinitionException("Duplicate bean name: " + def.getName());
        }
    }

    /**
     * Scan factory method that annotated with @Bean:
     * <code>
     * &#64;Configuration
     * public class Hello {
     * &#064;Bean
     * ZoneId createZone() {
     * return ZoneId.of("Z");
     * }
     * }
     * </code>
     */
    void scanFactoryMethods(BeanDefinition def, ClassMetaData classMetaData) throws BeanDefinitionException {
        if (classMetaData.hasAnnotation(Configuration.class) && classMetaData.hasMethodAnnotation(Bean.class)) {
            for (Method method : classMetaData.getMethodsByAnnotation(Bean.class)) {
                int mod = method.getModifiers();
                if (Modifier.isAbstract(mod)) {
                    throw new BeanDefinitionException("@Bean method " + def.getName() + "." + method.getName() + " must not be abstract.");
                }
                if (Modifier.isFinal(mod)) {
                    throw new BeanDefinitionException("@Bean method " + def.getName() + "." + method.getName() + " must not be final.");
                }
                if (Modifier.isPrivate(mod)) {
                    throw new BeanDefinitionException("@Bean method " + def.getName() + "." + method.getName() + " must not be private.");
                }
                Class<?> beanType = getBeanType(def, method);
                ClassMetaData beanMetaData = new DefaultClassMetaData(beanType);
                Bean beanAnnotation = method.getAnnotation(Bean.class);
                String beanName = beanAnnotation.value();
                if (Strings.isNullOrEmpty(beanAnnotation.value())) {
                    beanName = StringUtils.uncapitalize(beanType.getSimpleName());
                }
                BeanDefinition beanDefinition = new BeanDefinition(beanName, beanMetaData, method, def.getName(), beanAnnotation.initMethod(), beanAnnotation.destroyMethod());
                addBeanDefinition(beanDefinition);
            }
        }
    }


    private static Class<?> getBeanType(BeanDefinition def, Method method) throws BeanDefinitionException {
        Class<?> beanClass = method.getReturnType();
        if (beanClass.isPrimitive()) {
            throw new BeanDefinitionException("@Bean method " + def.getName() + "." + method.getName() + " must not return primitive type.");
        }
        if (beanClass == void.class || beanClass == Void.class) {
            throw new BeanDefinitionException("@Bean method " + def.getName() + "." + method.getName() + " must not return void.");
        }
        return beanClass;
    }

    /**
     * 根据配置类的@ComponentScan注解扫描获取所有Bean的Class类型
     *
     * @param configClass 配置类
     * @return {@link Set}<{@link String}> 所有Bean的Class类型
     */
    private Set<String> scanForClassNames(Class<?> configClass) {
        Set<String> scanPackages = new HashSet<>();
        scanPackages.add(SPRING_SCAN_PATH);
        ClassMetaData classMetaData = new DefaultClassMetaData(configClass);
        if (classMetaData.hasAnnotation(ComponentScans.class)) {
            ComponentScans componentScans = classMetaData.getAnnotations(ComponentScans.class).getFirst();
            for (ComponentScan componentScan : componentScans.value()) {
                scanPackages.addAll(Arrays.asList(componentScan.value()));
            }
        } else if (classMetaData.hasAnnotation(ComponentScan.class)) {
            scanPackages.addAll(Arrays.asList(classMetaData.getAnnotations(ComponentScan.class).getFirst().value()));
        } else {
            scanPackages.add(configClass.getPackageName());
        }
        Set<String> classNameSet = new HashSet<>();
        for (String scanPackage : scanPackages) {
            classNameSet.addAll(doScanPackage(scanPackage));
        }
        return classNameSet;
    }

    private static List<String> doScanPackage(String scanPackage) {
        ResourceResolver resolver = new ResourceResolver(scanPackage);
        return resolver.scan(res -> {
            String name = res.name();
            if (name.endsWith(".class")) {
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            return null;
        });
    }


    @Override
    public boolean containsBean(String name) {
        return this.beanDefinitions.containsKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        BeanDefinition def = findBeanDefinition(name);
        if (def == null) {
            throw new NoSuchBeanDefinitionException("No bean defined with name '" + name + "'.");
        }
        return (T) def.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(name);
        if (def == null) {
            throw new NoSuchBeanDefinitionException("No bean defined with name '" + name + "'.");
        }
        Object instance = def.getInstance();
        if (!requiredType.isAssignableFrom(instance.getClass())) {
            throw new NoSuchBeanDefinitionException("No bean defined with name '" + name + "' and type '" + requiredType.getName() + "', but a bean of the same name found");
        }
        return (T) def.getInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        BeanDefinition def = findRequiredBeanDefinition(requiredType);
        if (def == null) {
            throw new NoSuchBeanDefinitionException("No bean defined with type '" + requiredType + "'.");
        }
        return (T) def.getInstance();
    }

    @Override
    public Map<String, Object> getBeans() {
        return this.beans;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getBeans(Class<T> clazz) {
        return (List<T>) this.beans.values().stream().filter(bean -> clazz.isAssignableFrom(bean.getClass())).toList();
    }


    @Override
    public String[] getBeanNames() {
        return this.beanDefinitionNames.toArray(new String[0]);
    }

    @Override
    public List<BeanDefinition> getAllBeanDefinitions() {
        return this.beanDefinitions.values().stream().toList();
    }

    @Override
    public void close() {
        out.println("Closing " + this.getClass().getName() + "...");
        destroyBeans();
        this.beans.clear();
        this.beanDefinitions.clear();
        out.println(this.getClass().getName() + " closed.");
        ApplicationContextUtils.setApplicationContext(null);
    }

    private void destroyBeans() {
        for (BeanDefinition def : this.beanDefinitions.values()) {
            Object object = getOriginTarget(def.getName(), def.getInstance());
            if (def.getDestroyMethod()!=null){
                try {
                    def.getDestroyMethod().invoke(object);
                } catch (ReflectiveOperationException e) {
                    throw new BeanInitializingException("Error destroying bean with name '" + def.getName() + "' through @PreDestroy method '" + def.getDestroyMethod().toGenericString() + "': " + e.getMessage(), e);
                }
            }
            if (!Strings.isNullOrEmpty(def.getDestroyMethodName())) {
                try {
                    Method method = def.getBeanClass().getMethod(def.getDestroyMethodName());
                    method.invoke(def.getInstance());
                } catch (ReflectiveOperationException e) {
                    throw new BeanInitializingException("Error destroying bean with name '" + def.getName() + "' through destroy-method '" + def.getDestroyMethodName() + "': " + e.getMessage(), e);
                }
            }
        }
    }
}
