package org.redishash.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RedisHashCache implements IHashCache {
	
	
	@Autowired
	RedisTemplate<String, ?> redisTemplate;

	@SuppressWarnings("unchecked")
	@Override
	public void putObject(@NonNull String cacheName, @NonNull String hashKey, @NonNull Object obj) {
		log.debug("putObject: cacheName:{},hashKey:{},obj:{}",cacheName,hashKey,obj);
		redisTemplate.boundHashOps(cacheName).put(hashKey, obj);

	}

	@SuppressWarnings("unchecked")
	@Override
	public Object locateObject(@NonNull String cacheName, @NonNull String hashKey) {
		log.info("locateObject: cacheName:{},hashKey:{}",cacheName,hashKey);
		return redisTemplate.boundHashOps(cacheName).get(hashKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void evictObject(String cacheName, String hashKey) {
		log.debug("evict: cacheName:{},hashKey:{}",cacheName,hashKey);
		redisTemplate.boundHashOps(cacheName).delete(hashKey);

	}

	@Override
	public Object locateList(String cacheName) {
		log.debug("locateList: cacheName:{}",cacheName);
		return redisTemplate.boundHashOps(cacheName).values();
	}

}
