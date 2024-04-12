package com.zmq;

import com.zmq.annotation.EnableAspectJAutoProxy;
import com.zmq.annotation.EnableTransactionManagement;
import com.zmq.annotation.SpringbootApplication;
import com.zmq.context.AnnotationConfigApplicationContext;
import com.zmq.context.ApplicationContext;
import com.zmq.property.PropertyResolver;
import com.zmq.util.AopUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author zmq
 */

@SpringbootApplication
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class JdbcTest {
    private final static ApplicationContext context = new AnnotationConfigApplicationContext(JdbcTest.class, new PropertyResolver(new Properties()));


    @Test
    public void testJdbcBean() {
        out.println(Arrays.toString(context.getBeanNames()));
    }

    @Test
    public void testJdbcTemplate() {
        JdbcTemplate jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
        List<Number> list = jdbcTemplate.queryForList("select new_price from products", new NumberRowMapper());
        assertFalse(list.isEmpty());
        int insert = jdbcTemplate.update("insert into products(product_id,new_price) values(?,?)", 12, 1441);
        assertEquals(1,insert);
        int update = jdbcTemplate.update("update products set product_id=?,new_price=? where product_id=?", 12, 1999, 12);
        assertEquals(1,update);
        int delete = jdbcTemplate.update("delete from products where product_id=?", 12);
        assertEquals(1,delete);
    }

    @Test
    public void testTransaction(){
        out.println(Arrays.toString(context.getBeanNames()));
        context.getAllBeanDefinitions().forEach(def-> out.println(def.getName()));
        out.println(AopUtils.getAdvisors());
        TestService testService = context.getBean("testService", TestService.class);
        testService.duplicateInsert();
    }




}
