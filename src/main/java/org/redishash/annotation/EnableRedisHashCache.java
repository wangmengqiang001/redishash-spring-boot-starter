package org.redishash.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Documented
@Retention(RUNTIME)
@Target({ TYPE, ANNOTATION_TYPE, TYPE_USE })
@Import({org.redishash.aop.RedisConfig.class,
	org.redishash.fastjson.ConfigSerializer.class})
public @interface EnableRedisHashCache {

}
