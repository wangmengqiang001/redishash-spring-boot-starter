package org.redishash.aop;

import org.redishash.annotation.EnableRedisHashCache;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@AutoConfiguration

public class RedisConfig {
		
	
	   /**
     * 
     * 修改默认序列化方式
     * Key及Value,HashKey,HashValue对象都采用String 存储，Value,及HashValue的数据都
     * 根据方法的返回类型进行反序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
       System.out.print("\ninject RedisTemplate!!\n");
    	RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(jedisConnectionFactory());
        template.setConnectionFactory(connectionFactory);
        // 创建一个json的序列化方式
        //GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
     
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(stringRedisSerializer);

        // hash的key也采用String的序列化方式,不然key会乱码
        template.setHashKeySerializer(stringRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(stringRedisSerializer);
        //jackson2JsonRedisSerializer
        template.afterPropertiesSet();
        return template;
    }
    

	
	@Bean
	@ConditionalOnMissingBean(IHashCache.class)
	public IHashCache redisHashCache() {
		System.out.println("自动生成IHashCache对象到spring bean工厂");
		return new RedisHashCache();
	}

    @Bean
    @ConditionalOnBean(EnableRedisHashCache.class) // TODO to check why it cannot found the App class 
   // @ConditionPost
    @ConditionalOnMissingBean(RedisHashAspect.class)
    public RedisHashAspect redisHashAspect() {
    	System.out.println("now, inject aspect here");
    	return new RedisHashAspect();
    }
}
