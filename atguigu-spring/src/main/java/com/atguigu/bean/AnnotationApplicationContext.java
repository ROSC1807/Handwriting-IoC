package com.atguigu.bean;

import com.atguigu.anno.Bean;
import com.atguigu.anno.Di;
import org.springframework.asm.SpringAsmInfo;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AnnotationApplicationContext implements  ApplicationContext {
    //创建一个map集合，放bean对象
    private Map<Class,Object> beanFactory=new HashMap<>();
    private static String rootPath;
    //返回对象
    @Override
    public Object getBean(Class clazz) {
        return beanFactory.get(clazz);
    }
    //设置包扫描规则，当前包以及子包，哪个类有@bean注解，就把这个类通过反射实例化、
    //创建有参数构造，传递包路径
    public  AnnotationApplicationContext(String basePackage){
        //1.把.替换为\
        String packagePath=basePackage.replaceAll("\\.", "\\\\");
        //2.获取包绝对路径
        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while(urls.hasMoreElements()){
                URL url=urls.nextElement();
                String filePath = URLDecoder.decode(url.getFile(), "utf-8");
                //得到了路径

                rootPath= filePath.substring(0,filePath.length()-packagePath.length());
                //包扫描
                loadBean(new File(filePath));
            }
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        //属性注入
        loadDi();

    }
    //包扫描过程
    private void loadBean(File file) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //1.判断当前内容是否是文件夹
        if(file.isDirectory()){
            //2.如果是，获取文件夹里面所有内容
            File[] childrenFiles = file.listFiles();
            //3.判断文件夹里面为空
            if(childrenFiles==null||childrenFiles.length==0){
                return;
            }
            //4.如果文件夹里面不为空，遍历文件夹里面所有内容
            for(File child:childrenFiles){
                //4.1 遍历得到每个file对象，继续判断，如果还是一个文件夹，递归遍历
                if(child.isDirectory()){
                    loadBean(child);
                }
                //4.2 如果遍历得到的file对象，不是文件夹，是文件，
                else {
                    //4.3 得到包路径+类名称（字符串截取过程）
                    String pathWithClass= child.getAbsolutePath().substring(rootPath.length() - 1);
                    //4.4 当前文件的类型是否是.class
                    if(pathWithClass.contains(".class")){
                        //4.5 如果是.class类型，把路径\替换为.，把.class去掉
                        String allName=pathWithClass.replaceAll("\\\\",".").replace(".class","");
                        //4.6 判断类上面是否有注解@Bean,如果有，实例化过程
                        //4.6.1 获取类的Class
                        Class<?> clazz= Class.forName(allName);
                        //4.6.2 判断不是接口
                        if(!clazz.isInterface()) {
                            //4.6.3 判断上面有没有注解@Bean
                            Bean annotation = clazz.getAnnotation(Bean.class);
                            if (annotation != null) {
                                //4.6.4实例化
                                Object instance = clazz.getConstructor().newInstance();
                                //4.7 把对象实例化之后，放到map集合beanFactory
                                //4.7.1 判断当前类如果有接口，让接口的class作为map的key
                                if(clazz.getInterfaces().length>0){
                                    beanFactory.put(clazz.getInterfaces()[0],instance);
                                }
                                else{
                                    beanFactory.put(clazz,instance);
                                }
                            }
                        }
                    }

                }
            }
        }
    }
    private void loadDi(){
        //实例化对象在beanFactory的map集合里面
        //1遍历beanFactroy的map集合
        Set<Map.Entry<Class, Object>> entries = beanFactory.entrySet();
        for(Map.Entry<Class,Object> entry:entries){
            //2获取map集合每个对象，每个对象属性获取到
            Object obj = entry.getValue();
            //获取对象Class
            Class<?> clazz = obj.getClass();
            //获取每个对象属性，获取到
            Field[] declaredFields = clazz.getDeclaredFields();

            //3 遍历得到每个对象属性数组，得到每个属性
            for(Field field:declaredFields){
                //4 判断属性上面是否有@Di注解
                Di annotation=field.getAnnotation(Di.class);
                if(annotation!=null){
                    //如果是私有的属性，设置可以设置值
                    field.setAccessible(true);
                    //5 如果有@Di注解，把对象进行设置(注入)
                    try {
                        field.set(obj,beanFactory.get(field.getType()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

//    public static void main(String[] args) {
//        ApplicationContext context=new AnnotationApplicationContext("com.atguigu");
//        context.getBean();
//    }
}
