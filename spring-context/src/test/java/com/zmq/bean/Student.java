package com.zmq.bean;

import com.zmq.annotation.Component;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zmq
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class Student {

    private int id;

    private String name;

    @PostConstruct
    public void init(){
        this.id = 111;
        this.name = "initiated";
    }
}
