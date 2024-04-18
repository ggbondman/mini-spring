package com.zmq.tx;

import com.zmq.annotation.Transactional;
import com.zmq.beans.BeanDefinition;
import com.zmq.context.ApplicationContext;
import com.zmq.context.ApplicationContextUtils;
import com.zmq.exception.TransactionException;
import com.zmq.processor.BeanDefinitionPostProcessor;
import com.zmq.util.AopUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static java.lang.System.out;

/**
 * @author zmq
 */
public class DataSourceTransactionManager implements PlatformTransactionManager, MethodInterceptor, BeanDefinitionPostProcessor {
    static final ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<>();

    private DataSource dataSource;



    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        TransactionStatus ts = transactionStatus.get();
        ApplicationContext applicationContext = ApplicationContextUtils.getApplicationContext();
        if (this.dataSource==null) {
            this.dataSource = applicationContext.getBean("dataSource", DataSource.class);
        }
        if (ts == null) {
            // start new transaction:
            try (Connection connection = dataSource.getConnection()) {
                final boolean autoCommit = connection.getAutoCommit();
                if (autoCommit) {
                    connection.setAutoCommit(false);
                }
                try {
                    transactionStatus.set(new TransactionStatus(connection));
                    Object r = invocation.proceed();
                    connection.commit();
                    return r;
                } catch (Exception e) {
                    out.println("will rollback transaction for caused exception: " + (e.getCause() == null ? "null" : e.getCause().getClass().getName()));
                    TransactionException te = new TransactionException(e.getCause());
                    try {
                        connection.rollback();
                    } catch (SQLException sqle) {
                        te.addSuppressed(sqle);
                    }
                    throw te;
                } finally {
                    transactionStatus.remove();
                    if (autoCommit) {
                        connection.setAutoCommit(true);
                    }
                }
            }
        } else {
            // join current transaction:
            return invocation.proceed();
        }
    }

    @Override
    public void invokeBeanDefinitionPostProcessor(BeanDefinition def) {
        if (def.getBeanClass().isAnnotationPresent(Transactional.class)) {
            AopUtils.registerAdvisor(new TransactionAdvisor(def.getBeanClass(), this));
        }else {
            for (Method method : def.getBeanClass().getMethods()) {
                if (method.isAnnotationPresent(Transactional.class)) {
                    AopUtils.registerAdvisor(new TransactionAdvisor(method, this));
                }
            }
        }
    }
}
