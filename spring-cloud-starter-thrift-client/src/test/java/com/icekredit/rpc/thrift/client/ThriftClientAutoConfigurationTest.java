package com.icekredit.rpc.thrift.client;

import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ThriftClientAutoConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:application-test.properties")
public class ThriftClientAutoConfigurationTest {

    @Autowired
    private TestService.Iface testService;

    @Test
    public void test() throws TException {

        System.out.println(testService.test(1000));
    }

}
