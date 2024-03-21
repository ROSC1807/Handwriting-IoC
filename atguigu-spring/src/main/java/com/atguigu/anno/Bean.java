package com.atguigu.anno;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//用在类或者接口上面
@Target(ElementType.TYPE)
//在运行时生效
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
}
