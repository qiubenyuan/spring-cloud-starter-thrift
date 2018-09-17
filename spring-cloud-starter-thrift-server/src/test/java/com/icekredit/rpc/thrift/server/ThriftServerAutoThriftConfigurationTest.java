package com.icekredit.rpc.thrift.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author dw_xiajiqiu1
 * @time 2017/7/28 10:34
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ThriftServerAutoConfiguration.class, ThriftConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:application-test.properties")
public class ThriftServerAutoThriftConfigurationTest {

    @Before
    public void ready() throws Exception {
    }

    @Test
    public void testServerStart() throws InterruptedException {

        Thread.currentThread().join();
    }
}
