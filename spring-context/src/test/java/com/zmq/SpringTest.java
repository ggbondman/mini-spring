package com.zmq;

import com.zmq.utils.YamlUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;


public class SpringTest {


    @Test
    void testYamlResourceResolver(){
        Map<String, Object> stringObjectMap = YamlUtils.loadYamlAsPlainMap("/application.yml");
        System.out.println(stringObjectMap);
    }
}
