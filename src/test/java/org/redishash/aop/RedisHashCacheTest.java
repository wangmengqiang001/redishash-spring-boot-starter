package org.redishash.aop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redishash.annotation.RedisHDel;
import org.redishash.annotation.RedisHGet;
import org.redishash.annotation.RedisHMPut;
import org.redishash.annotation.RedisHPut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes=org.redishash.App.class)
@ActiveProfiles("test")
class RedisHashCacheTest {

	@Autowired
	InnerRedis inner;
	
	@Builder
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	
	static class InnderData{
		private String deviceId;
		private String metrics;
		private int value;
		private List<String> props;
	}
	@Component
	public static class InnerRedis{
		
		@RedisHGet(hashKey = "#deviceId", cache = "energy:iot:object_model:info_example")
		public InnderData findByKey(String deviceId) {
			InnderData data = InnderData.builder().deviceId(deviceId)
					.metrics(deviceId+"_" + System.currentTimeMillis()).value(100).build();
			return data;
		}
		
		@RedisHPut(hashKey = "#innerData.deviceId", cache = "energy:iot:object_model:info_example")
		public boolean updateBykey(InnderData innerData) {
			
			innerData.setMetrics(innerData.getDeviceId()+"_" + System.currentTimeMillis());
			return true;
			
		}

		
		@RedisHDel(hashKey = "#innerData.deviceId", cache = "energy:iot:object_model:info_example")
		public boolean delByKey(InnderData innerData) {
			return true;
		}
		@RedisHPut(hashKey = "#innerData.deviceId", cache = "energy:iot:object_model:info_example")
		public boolean updateBykeyFalse(InnderData innerData) {
			
			innerData.setMetrics(innerData.getDeviceId()+"_" + System.currentTimeMillis());
			return false;
			
		}
		
		@RedisHDel(hashKey = "#innerData.deviceId", cache = "energy:iot:object_model:info_example")
		public boolean delByKeyFalse(InnderData innerData) {
			return false;
		}
		@RedisHMPut(hashKey = "#resultVal.deviceId", cache = "energy:iot:object_model:info_example")
		public List<InnderData> queryList(){
			List<InnderData> result = Lists.newArrayList();
			for(int n=1; n<=20; n++) {
				String deviceId = "aI_"+n;
			result.add( InnderData.builder().deviceId(deviceId)
					.metrics(deviceId+"_" + System.currentTimeMillis()).value(100+n).build());
			}
			return result;
		}
		@RedisHMPut(hashKey = "#resultal.deviceId", cache = "energy:iot:object_model:info_example")
		public List<InnderData> queryList2(){
			List<InnderData> result = Lists.newArrayList();
			for(int n=1; n<=20; n++) {
				String deviceId = "aI_"+n;
			result.add( InnderData.builder().deviceId(deviceId)
					.metrics(deviceId+"_" + System.currentTimeMillis()).value(100+n).build());
			}
			return result;
		}
		
	}
	
	@Test
	void testPutObject() {
		
		List<String> pros = Lists.newArrayList("directon","speed","shape");
		InnderData data = InnderData.builder().deviceId("abc")
				.metrics("abcd"+"_" + System.currentTimeMillis()).value(200)
				.props(pros).build();
		
		inner.updateBykey(data);
		
		//read it from cache to compare
        InnderData dat =  inner.findByKey("abc");
		
		assertEquals(data.getMetrics(),dat.getMetrics());
	}
	@Test
	void testPutObjectFalse() {
		
		List<String> pros = Lists.newArrayList("directon","speed","shape");
		InnderData data = InnderData.builder().deviceId("abc")
				.metrics("abcd"+"_" + System.currentTimeMillis()).value(200)
				.props(pros).build();
		
		inner.updateBykeyFalse(data);
		
		//read it from cache to compare
		InnderData dat =  inner.findByKey("abc");
		
		assertFalse(data.getMetrics().equals(dat.getMetrics()),"It should not be equal");
	}

	@Test
	void testLocateObject() throws InterruptedException {
	assertNotNull(inner);
	
		
		InnderData value = inner.findByKey("abc");
		log.info("key: abc, value:{}",value);
		assertNotNull(value);
		log.info("sleeping....");
		Thread.sleep(2000);
		log.info("wakeup");
		
		InnderData val = inner.findByKey("abc");
		assertNotNull(val);
		log.info("key: abc, value:{}",val);
		assertEquals(value,val);
	}

	@Test
	void testEvictObject() {
		//update data first
	
		
		//read it from cache or initialize cache
        InnderData dat =  inner.findByKey("abc");
        
		
		InnderData data = InnderData.builder().deviceId("abc").build();
		inner.delByKey(data);
		
		//find again
		 InnderData datNew =  inner.findByKey("abc");
		 
		 //verify they are different
		 assertFalse(dat.getMetrics().equals(datNew.getMetrics()),"It should not be equal");
		 
	}
	@Test
	void testEvictObjectFalse() {
		//update data first
	
		
		//read it from cache or initialize cache
        InnderData dat =  inner.findByKey("abc");
        
		
		InnderData data = InnderData.builder().deviceId("abc").build();
		inner.delByKeyFalse(data);
		
		//find again
		 InnderData datNew =  inner.findByKey("abc");
		 
		 //verify they are same because deletion not happen
		 assertEquals(dat.getMetrics(),datNew.getMetrics());
		 
	}
	@Test
	void testQueryList() {
		List<InnderData> x = inner.queryList();
		
		assertEquals(20,x.size());
	}
	@Test
	void testQueryList2() {
		List<InnderData> x = inner.queryList2();
		
		assertEquals(20,x.size());
	}

	

}
