# mini-spring

## 关于

`mini-spring` 是 spring-boot 的简化版。它实现了：

- 内置 tomcat，一行代码即可启动服务
- 完全用注解开发，无需 xml 配置
- IoC 容器，解决 Setter 依赖注入时可能发生的依赖注入问题
- 支持 AOP 面向切面开发，支持 AspectJ 注解和表达式
- 支持 JDBC 操作和事务
- 支持 WEB MVC  

## 环境要求

- JDK：21 及以上版本
- Maven

## 启动方式

1. 通过 git 克隆仓库`git clone git@github.com:ggbondman/mini-spring.git`，或者下载代码到本地
2. 在项目的根目录运行 maven 命令：`mvn install`将项目安装到本地 maven 仓库
3. 创建自己的项目导入依赖

```xml
<properties>
  <!-- JDK版本必需是21以上 -->
  <maven.compiler.source>21</maven.compiler.source>
  <maven.compiler.target>21</maven.compiler.target>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencies>
  <!-- 必需，内含web模块 -->
  <dependency>
    <groupId>com.zmq</groupId>
    <artifactId>spring-boot</artifactId>
    <version>1.0-SNAPSHOT</version>
  </dependency>
  <!-- 按需导入 -->
  <dependency>
    <groupId>com.zmq</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>1.0-SNAPSHOT</version>
  </dependency>
  <!-- 按需导入 -->
  <dependency>
    <groupId>com.zmq</groupId>
    <artifactId>spring-aop</artifactId>
    <version>1.0-SNAPSHOT</version>
  </dependency>
  <!-- 按需导入 -->
  <dependency>
    <groupId>com.zmq</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>1.0-SNAPSHOT</version>
  </dependency>
</dependencies>

<build>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.12.1</version>
      <configuration>
        <source>21</source>
        <target>21</target>
        <compilerArgs>
          <!-- 用于反射获取参数名 -->
          <arg>-parameters</arg>
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>
```

4. 启动方式跟 springboot 大致一样，区别在于启动时需要设置JVM参数`--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED`，在IDEA中设置如下

![image.png](https://cdn.nlark.com/yuque/0/2024/png/26129220/1713435506583-7637671c-69cb-46ea-b8f3-39cec80e6926.png?x-oss-process=image%2Fformat%2Cwebp)

```java
@SpringbootApplication
public class MyApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MyApplication.class);
    }
}
```

## 使用方式

### IoC容器

- 使用方式基本和spring一样，但是

  - 仅支持注入类型，不支持注入接口。
  - 仅支持单例，不支持多例

- IoC 容器的实现方式：

  主要实现 IoC 容器，实现思路和 spring 有差别，对于 bean 的生命周期，spring 是纵向处理的，即完全创建好一个 bean （经过了实例化、属性赋值和初始化）之后才会创建下一个 bean，除非该 bean 依赖于其他 bean 才会转向创建其他依赖的 bean 。而 mini-spring 是横向处理，先对每个 bean 统一分别进行实例化、属性赋值和初始化。

  ![img](https://cdn.nlark.com/yuque/0/2024/jpeg/26129220/1713364612587-bc809e0f-41f9-43ea-abca-9aca3ebcd4fb.jpeg)

- Bean 的生命周期：

  在 mini-spring 中，创建一个 Bean 经历了从 class 文件到 BeanDefinition 到 Bean 的过程，如下图所示，

  ![img](https://cdn.nlark.com/yuque/0/2024/jpeg/26129220/1713362026472-771bca60-db95-49cc-b107-791ad1a8bceb.jpeg)

  1. **创建BeanDefinition：**从 Class 文件中读取出 Bean 的定义，创建为 BeanDefinition
  2. **BeanDefinition后置处理：**创建完所有 BeanDefinition 后，执行 BeanDefinition 后置处理器，BeanDefinition 后置处理器用于注册切面，使用 mini-spring 进行开发时，在启动类上加上`@EnableAspectJAutoProxy` 和`@EnableTransactionManagement`可以开启 AOP 和 JDBC 事务，并注入两个 `BeanDefinitionPostProcessor`：`AopBeanDefinitionPostProcessor` 和 `DataSourceTransactionManager`。`AopBeanDefinitionPostProcessor`用于注入`@Aspect`切面，`DataSourceTransactionManager`用于注入 JDBC 事务切面。开发者可以通过实现`BeanDefinitionPostProcessor`注册BeanDefinition 后置处理器。
  3. **实例化 Bean：**这一步通过构造器或工厂方法进行实例化，这个阶段的 Bean 还未进行属性赋值和初始化。
  4. **BeanPostProcessor前置处理：**对实例化后，属性赋值和初始化前的 Bean 进行前置处理。开启 Aop 功能后，会自动注入`AspectJBeanPostProcessor`，它会根据需要为实例化的 Bean 创建代理对象，并替代原来的 Bean，原来的 Bean 也会保存在`AspectJBeanPostProcessor.TARGET_MAP`中，用于在对 Bean 进行属性赋值和初始化时获取原始对象。开发者可以通过实现并重写`BeanPostProcessor.postProcessBeforeInitialization`方法注册`BeanPostProcessor`前置处理器。
  5. **Bean属性赋值：**查找 Bean 所属类中带有`@Autowired`和`@Value`注解的字段或方法，对这些字段或方法进行属性赋值。
  6. **初始化 Bean：**查找 Bean 所属类中带有`@PostConstruct`注解的方法，或者工厂方法中`@Bean`注解带有 `initMethod`属性值，先执行带有`@PostConstruct`注解的方法，后执行`initMethod`中方法名指定的方法。
  7. **BeanPostProcessor 后置处理：**对初始化后的 Bean 进行后置处理。目前 mini-spring 不会自动注册某个`BeanPostProcessor` 后置处理器，开发者可以通过实现并重写`BeanPostProcessor.postProcessAfterInitialization`方法注册`BeanPostProcessor`后置处理器。
  8. **销毁 Bean：**当 `ApplicationContext` 关闭前，会自动销毁 Bean。通过查找 Bean 所属类中带有`@PreDestroy`注解的方法，或者工厂方法中`@Bean`注解带有 `destroyMethod`属性值，先执行带有`@PreDestroy`注解的方法，后执行`destroyMethod`中方法名指定的方法。

- 可以自定义的拓展点有：

  - BeanDefinitionPostProcessor：创建完BeanDefinition后，实例化Bean之前触发
  - BeanProcessor.postProcessBeforeInitialization: Bean实例化后，初始化前触发
  - BeanProcessor.postProcessAfterInitialization: Bean初始化后触发

```java
/**
 * 预留的拓展点，可以通过实现该接口在BeanDefinition创建后，实例化Bean之前修改BeanDefinition
 */
public interface BeanDefinitionPostProcessor {
    
    void invokeBeanDefinitionPostProcessor(BeanDefinition def);
}

```

```java
/**
 * 预留的拓展点，可以通过实现该接口在Bean的生命周期内操作Bean
 */
public interface BeanPostProcessor extends Orderd {

    /**
     * Bean实例化后，初始化前触发
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * Bean初始化后触发
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * 用于在生成代理对象后，通过此方法获得被代理的原始对象
     */
    default Object getOriginTarget(Object bean, String beanName){
        return bean;
    }


    /**
     * 定义PostPostProcessor的优先级
     */
    default int getOrder(){
        return 0;
    }
}

```

### AOP

- 使用前需要在maven中导入spring-aop包，并启动类上加上注解`@EnableAspectJAutoProxy`

  ```xml
  <dependency>
      <groupId>com.zmq</groupId>
      <artifactId>spring-aop</artifactId>
      <version>1.0-SNAPSHOT</version>
  </dependency>
  ```

  ```java
  @SpringbootApplication
  @EnableAspectJAutoProxy
  public class MyApplication {
  
      public static void main(String[] args) throws Exception {
          SpringApplication.run(MyApplication.class);
      }
  }
  ```

- 使用时基本和spring aop一样，可以使用AspectJ注解，但是不支持bean表达式。
- Advice方法的参数是固定的，不能多也不能少，具体如下面代码所示

```java
@Aspect
@Component
public class TestAop {

    @Pointcut("execution (* com.zmq.controller.*.*(..))")
    public void test(){

    }

    @Before("test()")
    public void before(JoinPoint joinPoint){
        out.println("进入before：Class Method   : " + 	joinPoint.getSignature().getDeclaringTypeName()+" "+joinPoint.getSignature().getName());
    }

    @Around("test()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        out.println("进入around");
        Object result = proceedingJoinPoint.proceed();
        out.println(result);
        out.println("退出around");
        return result;
    }

    @After("test()")
    public void after(JoinPoint joinPoint) throws Throwable {
        out.println("进入after："+ joinPoint.getSignature().getDeclaringTypeName());
    }

    @AfterReturning("test()")
    public void afterReturning(JoinPoint joinPoint,Object returnValue) throws Throwable {
        out.println("进入afterReturning："+ returnValue);
    }

    @AfterThrowing("test()")
    public void afterThrowing(JoinPoint joinPoint,Throwable ex) throws Throwable {
        out.println("进入afterThrowing："+ ex);
    }

}

```

### Jdbc

- 使用前需要在maven中导入spring-jdbc包，并在`application.yml`中配置数据库参数

  ```xml
  <dependency>
      <groupId>com.zmq</groupId>
      <artifactId>spring-jdbc</artifactId>
      <version>1.0-SNAPSHOT</version>
  </dependency>
  ```

  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/mini_spring?characterEncoding=utf-8&serverTimezone=GMT%2B8
      username: root
      password: xxxx
      driver-class-name: com.mysql.cj.jdbc.Driver
  ```

- 默认固定使用`Hikari`作为数据源，暂时无法修改。

- 默认将`JdbcTemplate`注册为bean，可以直接通过`JdbcTemplate`对数据库进行操作。

- 开启JDBC事务需要在启动类上加上注解`@EnableTransactionManagement`

  ```java
  @SpringbootApplication
  @EnableTransactionManagement
  public class MyApplication {
      public static void main(String[] args) throws Exception {
          SpringApplication.run(MyApplication.class);
      }
  }
  ```

- 默认固定事务传播模式为`REQUIRED`，即如果当前方法已经有了一个事务，那么调用方法将在该事务中执行；如果当前方法没有事务，那么调用方法将开启一个新的事务。

- 在类上或者方法上附加`@Transactional`注解便可以对该类上的所有方法或指定方法使用事务。

  ```java
  @Configuration
  public class JdbcConfiguration {
  
      @Bean
      public DataSource dataSource(
              // properties:
              @Value("${spring.datasource.url}") String url,
              @Value("${spring.datasource.username}") String username,
              @Value("${spring.datasource.password}") String password,
              @Value("${spring.datasource.driver-class-name:}") String driver,
              @Value("${spring.datasource.maximum-pool-size:20}") int maximumPoolSize,
              @Value("${spring.datasource.minimum-pool-size:1}") int minimumPoolSize,
              @Value("${spring.datasource.connection-timeout:30000}") int connTimeout
      ) {
          var config = new HikariConfig();
          config.setAutoCommit(false);
          config.setJdbcUrl(url);
          config.setUsername(username);
          config.setPassword(password);
          if (driver != null) {
              config.setDriverClassName(driver);
          }
          config.setMaximumPoolSize(maximumPoolSize);
          config.setMinimumIdle(minimumPoolSize);
          config.setConnectionTimeout(connTimeout);
          return new HikariDataSource(config);
      }
  
      @Bean
      public JdbcTemplate jdbcTemplate(DataSource dataSource) {
          return new JdbcTemplate(dataSource);
      }
  ```

  ```java
  @Service
  @Transactional  // 可以不在类上加此注解，单独在方法上加此注解也可以生效
  public class TestJdbc {
  
      JdbcTemplate jdbcTemplate;
  
      @Autowired
      public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
          this.jdbcTemplate = jdbcTemplate;
      }
  
      public void testInsert(Student student){
          String sql = "insert into student(id,name) values(?,?)";
          jdbcTemplate.update(sql,student.getId(),student.getName());
      }
  
      public void testUpdate(Student student){
          String sql = "update student set name=? where id=?";
          jdbcTemplate.update(sql,student.getName(),student.getId());
      }
  
      public void testDelete(int id){
          String sql = "delete from student where id=?";
          jdbcTemplate.update(sql,id);
      }
  
      public List<Student> testSelectAll(){
          String sql = "select * from student";
          return jdbcTemplate.queryForList(sql,Student.class);
      }
  
      public List<Student> testSelect(int id){
          String sql = "select * from student where id=?";
          return jdbcTemplate.queryForList(sql,Student.class,id);
      }
  
      @Transactional
      public void testTransaction(int id,String name){
          Student s1 = new Student(id,name);
          Student s2 = new Student(id,name);
          testInsert(s1);
          testInsert(s2);
      }
  }
  ```

### Web MVC

- 导入spring-boot包时会自动导入spring-web包
- 支持`@Controller`和`@RestController`注解，规则大致和spring一样
- 支持`@RequestMapping`注解，但不支持`@PostMapping`、`@GetMaaping`等子注解，指定http请求方式可以设置`@RequestMapping`注解的`method`值。
- 视图文件目录默认设置为`/resource/templates`目录，也可以通过在配置文件中设置`spring.web.freemarker.template-path`的值来自定义视图目录，支持使用freemarker模版对数据进行拼接。
- 静态资源目录默认设置为`/resource/templates`目录，可以通过在配置文件中设置`spring.web.freemarker.static-path`的值来自定义静态资源目录。

```yaml
server:
  # 设置服务端口 
  port: 80

spring:
  web:
    freemarker:
      # 视图文件的默认目录
      template-path: /templates
      # 静态资源的默认目录
      static-path: /static
      # 网站图标的默认位置
      favicon-path: /favicon.ico
```

```java
@Controller
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private TestJdbc testJdbc;

    @RequestMapping(method = RequestMethod.GET,value = "/view")
    public ModelAndView testHtml(){
        return new ModelAndView("/index.html",null);
    }

    @RequestMapping(method = RequestMethod.GET,value = "/transaction")
    public void testTransaction(){
        testJdbc.testTransaction(6,"666");
    }
}

```



##  支持的注解

### Springboot 注解

| **注解**                    | **所属模块** | **参数**                         | **说明**               |
| --------------------------- | :----------- | -------------------------------- | ---------------------- |
| Bean                        | context      | value, initMethod, destroyMethod |                        |
| Value                       | context      | value                            |                        |
| AutoWired                   | context      | required                         |                        |
| Component                   | context      | value                            |                        |
| Import                      | context      | value                            |                        |
| ComponentScan               | context      | value                            |                        |
| ComponentScans              | context      | value                            |                        |
| Configuration               | context      | 无                               |                        |
| Primary                     | context      | 无                               |                        |
| EnableAspectJAutoProxy      | aop          | 无                               | 开启 Aop 所需注解      |
| EnableTransactionManagement | jdbc         | 无                               | 开启 jdbc 事务所需注解 |
| Transactional               | jdbc         | value                            |                        |
| Controller                  | web          | value                            |                        |
| Service                     | web          | value                            |                        |
| RestController              | web          | value                            |                        |
| PathVariable                | web          | value                            |                        |
| RequestBody                 | web          | value                            |                        |
| RequestMapping              | web          | method, value                    |                        |
| RequestParam                | web          | value, defaultValue              |                        |
| ResponseBody                | web          | 无                               |                        |
| SpringbootApplication       | boot         | 无                               |                        |

### AspectJ

**支持的 AspectJ 注解**

- @Aspect
- @Pointcut
- @Before
- @After
- @AfterReturning
- @AfterThrowing
- @Around

**支持的切点表达式类型：**

- execution
- within
- this
- target
- args
- @within
- @target
- @annotation
- @args

## 参考资料

- Springboot
- [summer-framework](https://github.com/michaelliao/summer-framework/tree/master)
