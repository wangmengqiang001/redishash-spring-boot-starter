package org.redishash.aop;

public interface IHashCache {
	void putObject(String cacheName, String hashKey, Object obj) ;
	Object locateObject(String cacheName, String hashKey);
	void evictObject(String cacheName,String hashKey);

}
