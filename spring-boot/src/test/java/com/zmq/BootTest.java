package com.zmq;

import com.zmq.annotation.EnableAspectJAutoProxy;
import com.zmq.annotation.EnableTransactionManagement;
import com.zmq.boot.SpringApplication;
import com.zmq.boot.SpringbootApplication;
import org.junit.jupiter.api.Test;

/**
 * @author zmq
 */
@SpringbootApplication
@EnableAspectJAutoProxy
@EnableTransactionManagement
public class BootTest {

    @Test
    void testBoot() throws Exception {
        SpringApplication.run(BootTest.class);
    }
}
