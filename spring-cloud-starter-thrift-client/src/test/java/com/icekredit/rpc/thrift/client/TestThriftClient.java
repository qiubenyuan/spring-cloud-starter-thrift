package com.icekredit.rpc.thrift.client;

import com.icekredit.rpc.thrift.client.annotation.ThriftClient;
import com.icekredit.rpc.thrift.client.common.ThriftClientAware;

@ThriftClient(serviceId = "test-thrift", refer = TestService.class)
public interface TestThriftClient extends ThriftClientAware<TestService.Client> {
}
