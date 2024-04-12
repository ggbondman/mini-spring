package com.zmq;

import com.zmq.annotation.SpringbootApplication;
import com.zmq.utils.YamlUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author zmq
 */
@SpringbootApplication
public class SpringTest {


    @Test
    void testYamlResourceResolver(){
        Map<String, Object> stringObjectMap = YamlUtils.loadYamlAsPlainMap("/application.yml");
        System.out.println(stringObjectMap);
    }
}
