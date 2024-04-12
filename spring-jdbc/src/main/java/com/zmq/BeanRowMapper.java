package com.zmq;

import com.google.common.base.CaseFormat;
import com.zmq.exception.DataAccessException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

/**
 * @author zmq
 */
public class BeanRowMapper<T> implements RowMapper<T> {


    Class<T> clazz;
    Constructor<T> constructor;
    Map<String, Field> fields = new HashMap<>();
    Map<String, Method> methods = new HashMap<>();

    public BeanRowMapper(Class<T> clazz) {
        this.clazz = clazz;
        try {
            this.constructor = clazz.getConstructor();
        } catch (ReflectiveOperationException e) {
            throw new DataAccessException(String.format("No public default constructor found for class %s when build BeanRowMapper.", clazz.getName()), e);
        }
        for (Field f : clazz.getFields()) {
            String name = f.getName();
            this.fields.put(name, f);
            out.println(STR."Add row mapping: \{name} to field \{name}");
        }
        for (Method m : clazz.getMethods()) {
            Parameter[] ps = m.getParameters();
            if (ps.length == 1) {
                String name = m.getName();
                if (name.length() >= 4 && name.startsWith("set")) {
                    String prop = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                    this.methods.put(prop, m);
                    out.println(STR."Add row mapping: \{prop} to \{name}(\{ps[0].getType().getSimpleName()})");
                }
            }
        }
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T bean;
        try {
            bean = this.constructor.newInstance();
            ResultSetMetaData meta = rs.getMetaData();
            int columns = meta.getColumnCount();
            for (int i = 1; i <= columns; i++) {
                String label = meta.getColumnLabel(i);
                Method method = this.methods.get(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,label));
                if (method != null) {
                    method.invoke(bean, rs.getObject(label));
                } else {
                    Field field = this.fields.get(label);
                    if (field != null) {
                        field.set(bean, rs.getObject(label));
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new DataAccessException(STR."Could not map result set to class \{this.clazz.getName()}", e);
        }
        return bean;
    }
}