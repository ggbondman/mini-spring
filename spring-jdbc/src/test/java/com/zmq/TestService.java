package com.zmq;

import com.zmq.annotation.Autowired;
import com.zmq.annotation.Component;
import com.zmq.annotation.Transactional;

import java.util.List;

/**
 * @author zmq
 */

@Transactional
@Component
public class TestService {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> duplicateInsert(){
        List<Product> products;
        jdbcTemplate.update("delete from products where product_id=?", 55);
        jdbcTemplate.update("insert into products(product_id,new_price) values(?,?)", 55, 2323);
        products = jdbcTemplate.queryForList("select * from products", Product.class);
        System.out.println(products);
        jdbcTemplate.update("insert into products(product_id,new_price) values(?,?)", 55, 2323);
        products = jdbcTemplate.queryForList("select * from products", Product.class);
        return products;
    }


}
