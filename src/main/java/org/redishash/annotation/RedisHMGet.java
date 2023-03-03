package org.redishash.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Inherited
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD})

public @interface RedisHMGet  {
	 /**
     * Cache名
     */
    String cache();

    /**
     * Hash键名（支持Spring EL表达式）
     */
    String hashKey();
    
    /**
     * 是否为查询操作
     * 如果为写入数据库的操作，该值需置为 false
     */
    boolean read() default true;
    
    /**
     * 保存值是否以Json 格式保存
     */
    boolean isJson() default true;
    
    Class<?> clazz() default Object.class;
    
}
