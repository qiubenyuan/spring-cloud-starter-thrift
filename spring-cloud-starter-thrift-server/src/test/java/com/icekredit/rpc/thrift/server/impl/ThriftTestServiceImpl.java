package com.icekredit.rpc.thrift.server.impl;

import com.icekredit.rpc.thrift.server.TestService;
import com.icekredit.rpc.thrift.server.annotation.ThriftService;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@ThriftService("thriftTestServiceImpl")
@Component
public class ThriftTestServiceImpl implements TestService.Iface {

    private static final byte[] KEY = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    private static final byte[] KB_1 = new byte[1024];
    private static final byte[] KB_10 = new byte[10 * 1024];
    private static final byte[] KB_100 = new byte[100 * 1024];

    private static final String KB_1_STR;
    private static final String KB_10_STR;
    private static final String KB_100_STR;

    private static final String DEFAULT_UUID = UUID.randomUUID().toString().replace("-", "");

    static {
        Random random = new Random();
        for (int i = 0; i < KB_1.length; i++) {
            KB_1[i] = KEY[random.nextInt(KEY.length)];
        }

        KB_1_STR = new String(KB_1);

        for (int i = 0; i < KB_10.length; i++) {
            KB_10[i] = KEY[random.nextInt(KEY.length)];
        }
        KB_10_STR = new String(KB_10);

        for (int i = 0; i < KB_100.length; i++) {
            KB_100[i] = KEY[random.nextInt(KEY.length)];
        }
        KB_100_STR = new String(KB_100);

    }

    @Override
    public String test(int length) throws TException {

        String result = KB_1_STR;

        if (length == 100) {
            result = KB_100_STR;
        }
        if (length == 10) {
            result = KB_10_STR;
        }

        if (length == 0) {
            result = DEFAULT_UUID;
        }

        if (length == 1000) {
            result = "MyClient";
        }

        System.out.println("Call RPC Test, data length: " + length + "KB");
        return result;
    }


}
