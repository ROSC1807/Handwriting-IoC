package com.atguigu.anno;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//用在属性上面
@Target({ElementType.FIELD})
//运行时生效
@Retention(RetentionPolicy.RUNTIME)
public @interface Di {
}
