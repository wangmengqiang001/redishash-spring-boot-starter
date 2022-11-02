package org.redishash.aop;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redishash.aop.DummyHashCache;

class DummyHashCacheTest {
	
	private DummyHashCache hashCache;
	
	@BeforeEach
	void init() {
		hashCache=new DummyHashCache();
	}
	

	@Test
	void testPutObject() {
		
		assertEquals(0,hashCache.cacheSize());
		assertEquals(-1,hashCache.cacheSize("a"));
		hashCache.putObject("a", "abc", "object_value_a_abc");
		
		hashCache.putObject("a", "b", "object_value_a_b");
		assertEquals(2,hashCache.cacheSize("a"));
		assertEquals(1,hashCache.cacheSize());
		hashCache.putObject("b", "a", "object_value_b_a");
		assertEquals(2,hashCache.cacheSize());
		hashCache.putObject("ab","abc","hello_world");
		assertEquals(3,hashCache.cacheSize());
		
	}

	@Test
	void testLocateObject() {
		
		String val = (String) hashCache.locateObject("a", "a");
		assertNull(val);
		
		hashCache.putObject("a", "abc", "object_value_a_abc");
		
		val = (String) hashCache.locateObject("a", "abc");
		assertEquals("object_value_a_abc",val);
	
		
	}

	@Test
	void testEvictObject() {		
		
		hashCache.evictObject("a", "a"); //no empty
		
		hashCache.putObject("a", "abc", "object_value_a_abc");
		
		hashCache.putObject("a", "b", "object_value_a_b");
		assertEquals(2,hashCache.cacheSize("a"));
		assertEquals(1,hashCache.cacheSize());
		hashCache.putObject("b", "a", "object_value_b_a");
		assertEquals(2,hashCache.cacheSize());
		hashCache.putObject("ab","abc","hello_world");
		assertEquals(3,hashCache.cacheSize());
		
		hashCache.evictObject("ab","abc");
		assertEquals(2,hashCache.cacheSize());
		
		hashCache.evictObject("a","b");
		assertEquals(1,hashCache.cacheSize("a"));
		
		hashCache.evictObject("a","abc");
		assertEquals(-1,hashCache.cacheSize("a"));
		
	}

}
