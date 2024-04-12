package com.zmq.tx;

import com.zmq.annotation.Autowired;
import com.zmq.annotation.Transactional;
import com.zmq.beans.BeanDefinition;
import com.zmq.context.ApplicationContext;
import com.zmq.context.ApplicationContextUtils;
import com.zmq.exception.TransactionException;
import com.zmq.util.AopUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static java.lang.System.out;

/**
 * @author zmq
 */
public class DataSourceTransactionManager implements PlatformTransactionManager,MethodInterceptor {
    static final ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<>();

    private final DataSource dataSource;

    public DataSourceTransactionManager(@Autowired DataSource dataSource) {
        this.dataSource = dataSource;
        ApplicationContext applicationContext = ApplicationContextUtils.getApplicationContext();
        for (BeanDefinition def : applicationContext.getAllBeanDefinitions()) {
            if (def.getBeanClass().isAnnotationPresent(Transactional.class)){
                AopUtils.registerAdvisor(new TransactionAdvisor(def.getBeanClass(),this));
            }
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        TransactionStatus ts = transactionStatus.get();
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
                    out.println(STR."will rollback transaction for caused exception: \{e.getCause() == null ? "null" : e.getCause().getClass().getName()}");
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
}
