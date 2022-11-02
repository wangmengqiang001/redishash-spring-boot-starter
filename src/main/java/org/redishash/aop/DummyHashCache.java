package org.redishash.aop;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DummyHashCache implements IHashCache {
	
	private Map<String,Map<String, Object>> mapCache = Maps.newConcurrentMap();
	
	public int cacheSize() {
		return mapCache.size();
	}
	public int cacheSize(String cacheName) {
		if(mapCache.containsKey(cacheName)) {
			Map<String, Object> map = mapCache.get(cacheName);
			return map.size();
		}
		return -1; //no cache
	}

	@Override
	public void putObject(String cacheName, String hashKey, Object obj) {
		
		log.info("在进行修改后，更新cache中key={},hashKey={}中的值",cacheName,hashKey);
		if(!mapCache.containsKey(cacheName)) {
			Map<String,Object> hkeys = Maps.newConcurrentMap();
			hkeys.put(hashKey, obj);
			
			mapCache.put(cacheName, hkeys);
		}else {
			Map<String,Object> hkeys = mapCache.get(cacheName);
			hkeys.put(hashKey, obj);
		}
		
		
	}

	@Override
	public Object locateObject(String cacheName, String hashKey) {
		log.info("在执行查询前，查看cache中是否有key={},hashKey={}的对象 ",cacheName,hashKey);
		if(!mapCache.containsKey(cacheName)){
			return null;
		}else {
			Map<String, Object> hkeys =  mapCache.get(cacheName);
			return hkeys.get(hashKey);
		}
		
	}

	@Override
	public void evictObject(String cacheName, String hashKey) {
		log.info("删除 object by key: {}, hashKey: {}",cacheName,hashKey);
		mapCache.computeIfPresent(cacheName, (k,v) -> {
			Map<String,Object> hKeys =  v;
			hKeys.remove(hashKey);
			return hKeys.size() > 0?v:null;});
		
		
	}

}
