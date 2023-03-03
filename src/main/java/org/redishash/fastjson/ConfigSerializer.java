package org.redishash.fastjson;


import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;

@AutoConfiguration
public class ConfigSerializer {
	@Bean
    public SerializeConfig serializeConfig() {
		
		
        SerializeConfig config = new SerializeConfig();
      
        // 对LocalDateTime类型指定自定义的序列化器
        config.put(LocalDateTime.class, objectSerializer());
       
        return config;
    }

    @Bean
    public ObjectSerializer objectSerializer() {
        return new FastjsonSerializer();
    }
}
