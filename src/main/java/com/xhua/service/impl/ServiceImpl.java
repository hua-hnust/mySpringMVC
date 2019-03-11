package com.xhua.service.impl;

import com.xhua.annotation.MyService;
import com.xhua.service.Service;

@MyService("myService")
public class ServiceImpl implements Service {
    public String query(String name, String age) {
        return "欢迎  name:"+name+"  age:"+age;
    }
}
