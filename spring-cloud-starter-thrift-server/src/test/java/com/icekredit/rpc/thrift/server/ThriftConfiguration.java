package com.icekredit.rpc.thrift.server;

import com.icekredit.rpc.thrift.server.impl.ThriftTestServiceImpl;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class ThriftConfiguration {

    @Bean
    public ThriftTestServiceImpl thriftTestService() {
        return new ThriftTestServiceImpl();
    }

}
