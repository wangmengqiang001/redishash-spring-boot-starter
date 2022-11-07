package org.redishash.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RedisHPut {

	 /**
    * Cache名（支持Spring EL表达式）
    */
   String cache();

   /**
    * Hash键名（支持Spring EL表达式）
    */
   String hashKey();

   /**
    * 保存值是否以Json 格式保存
    */
   boolean isJson() default true;

}
