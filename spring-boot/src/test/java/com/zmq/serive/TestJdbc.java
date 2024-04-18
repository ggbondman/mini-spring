package com.zmq.serive;

import com.zmq.JdbcTemplate;
import com.zmq.annotation.Autowired;
import com.zmq.annotation.Service;
import com.zmq.annotation.Transactional;
import com.zmq.model.Student;

import java.util.List;

/**
 * @author zmq
 */
@Service
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
