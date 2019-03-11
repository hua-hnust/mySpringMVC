package com.xhua.servlet;

import com.xhua.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyDispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // 包名称
    List<String> packageNames = new ArrayList<String>();

    // 存放类的实例，相当于IOC容器
    Map<String,Object> beans = new HashMap<String,Object>(16);

    // 建立映射关系，相当于处理器映射器
    Map<String,Object> handerMapping = new HashMap<String,Object>(16);

    @Override
    public void init(){
        //1、扫描包
        scanPackage("com.xhua");
        //2、建立beans放入IOC容器
        instance();
        //3、为contrller注入service实例
        ioc();
        //4、建立URL--method的映射
        handerMap();

    }

    /**
     *  扫描包下所有文件
     * @param packagePath
     */
    private void scanPackage(String packagePath) {
        URL url = this.getClass().getClassLoader().getResource("/"+packagePath.replaceAll("\\.","/"));
        String pathFile = url.getFile();
        File dir = new File(pathFile);
        for (File file:dir.listFiles()){
            //如果是文件夹，继续遍历
            if (file.isDirectory()){
                scanPackage(packagePath + "." + file.getName());
            }else {
                packageNames.add(packagePath +"."+ file.getName());
            }
        }
    }

    //建立contrller和service实例
    private void instance(){
        if (packageNames.size()<=0){
            return;
        }
        for (String className:packageNames){
            try{
                Class<?> cName = Class.forName(className.replace(".class",""));
                if (cName.isAnnotationPresent(MyController.class)){
                    Object instance = cName.newInstance();
                    MyRequestMapping myRequestMapping = cName.getAnnotation(MyRequestMapping.class);
                    String key = myRequestMapping.value();
                    //key值为URL的值
                    beans.put(key,instance);
                }else if (cName.isAnnotationPresent(MyService.class)) {
                    Object instance = cName.newInstance();
                    MyService service = cName.getAnnotation(MyService.class);
                    String key = service.value();
                    beans.put(key, instance);
                } else {
                    continue;
                }

            }catch (Exception e){
                System.out.println("类加载失败！");
            }

        }
    }


    /**
     *  建立映射关系 url --- 方法
     */
    private void handerMap(){
        if (beans.size()<=0){
            return;
        }

        for (Map.Entry<String,Object> entry: beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(MyController.class)){
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                String controllerURI = requestMapping.value();
                Method[] methods = clazz.getMethods();
                for (Method method:methods){
                    if (method.isAnnotationPresent(MyRequestMapping.class)){
                        MyRequestMapping rm = method.getAnnotation(MyRequestMapping.class);
                        String rmValue = rm.value();
                        handerMapping.put(controllerURI + rmValue,method);
                    }else {
                        continue;
                    }
                }
            }else {
                continue;
            }
        }
    }

    /**
     * 注入service
     */
    private void ioc(){
        if (beans.isEmpty()){
            return;
        }
        for (Map.Entry<String,Object> bean: beans.entrySet()){
            Object instance = bean.getValue();
            Class<?> clazz = instance.getClass();

            if (clazz.isAnnotationPresent(MyController.class)){
                Field[] fields = clazz.getDeclaredFields();
                for (Field field :fields){
                    if (field.isAnnotationPresent(MyResource.class)){
                        MyResource myResource = field.getAnnotation(MyResource.class);
                        String key = myResource.value();
                        field.setAccessible(true);
                        try {
                            //依赖注入
                            field.set(instance, beans.get(key));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }else {
                        continue;
                    }
                }
            }else {
                continue;
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (handerMapping.isEmpty()){
            return;
        }

        String requestURL = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestURL.replace(contextPath,"");
        Method method = (Method)handerMapping.get(path);
        // bean名称为一级URL
        Object instance = beans.get("/"+path.split("/")[1]);

        Object[] args = handle(req,resp,method);

        try {
            //通过反射调用方法
            method.invoke(instance,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取执行方法的参数
     */
    private Object[] handle(HttpServletRequest request, HttpServletResponse response, Method method) {
        //获取到当前执行的方法有哪些参数
        Class<?>[] parameterTypes = method.getParameterTypes();
        //根据参数的个数， new 一个数组，将所有的参数赋值到 args
        Object[] args = new Object[parameterTypes.length];
        int args_i = 0;
        int index = 0;
        for (Class<?> paramClazz : parameterTypes) {
            if (ServletRequest.class.isAssignableFrom(paramClazz)) {
                args[args_i++] = request;
            }
            if (ServletResponse.class.isAssignableFrom(paramClazz)) {
                args[args_i++] = response;
            }

            //判断有没有注解 @MyRequestParam，有则解析
            Annotation[] paramAnnotations = method.getParameterAnnotations()[index];
            if (paramAnnotations.length > 0) {
                for (Annotation paramAnnotation : paramAnnotations) {
                    //判断 paramAnnotation.getClass() 是否是 MyRequestParam 的子类或子接口
                    if (MyRequestParam.class.isAssignableFrom(paramAnnotation.getClass())) {
                        MyRequestParam myRequestParam = (MyRequestParam) paramAnnotation;
                        //找到注解里的name和age
                        args[args_i++] = request.getParameter(myRequestParam.value());
                    }
                }
            }
            index++;
        }
        return args;
    }
}
