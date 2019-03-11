package com.xhua.controller;

import com.xhua.annotation.MyController;
import com.xhua.annotation.MyRequestMapping;
import com.xhua.annotation.MyRequestParam;
import com.xhua.annotation.MyResource;
import com.xhua.service.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@MyController
@MyRequestMapping("/mvc")
public class Controller {

    @MyResource("myService")
    private Service myService;

    @MyRequestMapping("/hello")
    public void test(HttpServletRequest request, HttpServletResponse response,
                     @MyRequestParam("name") String name, @MyRequestParam("age") String age){
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/html; charset=utf-8");
            PrintWriter out = response.getWriter();
            String user = myService.query(name, age);
            out.write(user);
        } catch (IOException e) {
            System.out.println("获取Writer失败");
        }
    }
}
