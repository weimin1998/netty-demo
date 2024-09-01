package com.weimin.server.service;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "こにちは，" + name;
    }
}
