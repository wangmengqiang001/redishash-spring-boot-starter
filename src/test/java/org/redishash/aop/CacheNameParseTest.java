package org.redishash.aop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.redishash.annotation.RedisHGet;
import org.redishash.annotation.RedisHPut;
import org.redishash.aop.RedisHashAspectTest.TElements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes=org.redishash.App.class)
@ActiveProfiles("test")
@Component
class CacheNameParseTest {

	@Autowired
	InnerCache innerCache;
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	static class TElements{
		private String a;
		private int b;
		private String c;
		List<String> u;
	}
	@Component
	public static class InnerCache{
		@RedisHPut(cache="#l[0].a+#l[0].b+'_'+#l[0].c+'_'+#l[1].a",hashKey="#l[0].a")
		public boolean testMethod(List<TElements> l) {
			
			return true;
		}
		
		@RedisHPut(cache="abc:xx:xx",hashKey="'u'+#xyb",isJson=false)
		public boolean testPutText(String textToSave,String xyb) {
			return true;
		}
		
		@RedisHGet(cache="abc:xx:xx",hashKey="'u'+#xyb",isJson=false)
		public String findValue(String xyb) {
			return "";
		}
		@RedisHGet(cache="#l[0].a+#l[0].b+'_'+#l[0].c+'_'+#l[1].a",hashKey="#l[0].a",
				clazz=TElements.class)
		public List<TElements> testGetArray(List<TElements> l) {
			List<TElements> arys= new ArrayList<TElements>();
			
			return arys;
		}
	}
	@Test
	void test() {
		
	
		List<TElements> le = Lists.newArrayList();
		TElements u1 = TElements.builder().a("eml_1").b(1).c("world").u(Lists.newArrayList("a","b","c","d")).build();
		le.add(u1);
		 u1 = TElements.builder().a("eml_2").b(2).u(Lists.newArrayList("a","b","c","d")).build();
		le.add(u1);
		
		innerCache.testMethod( le);
		
	}
	
	@Test
	void test_2() {
		
	
		List<TElements> le = Lists.newArrayList();
		TElements u1 = TElements.builder().a("eml_1").b(1).c("world").u(Lists.newArrayList("a","b","c","d")).build();
		le.add(u1);
		 u1 = TElements.builder().a("eml_2").b(2).u(Lists.newArrayList("a","b","c","d")).build();
		le.add(u1);
		
		innerCache.testMethod( le);
		
		//to get from cache
		List<TElements> ne = innerCache.testGetArray(le);
		log.info("find {} items in cache, items{},class:{}",ne.size(),ne,ne.getClass());
		assertEquals(le.size(),ne.size());
		
	
		
		assertEquals(le.get(0).getClass(),ne.get(0).getClass());
		ne.addAll(le);
		log.info("now, {} items will be updated to cache",ne.size());
		//to append cache
		innerCache.testMethod(ne);
	}
	
	@Test
	void testText() {

		final String textToSave = "save Text not json object here";
		innerCache.testPutText(textToSave,"xyb");
		
		String value = innerCache.findValue("xyb");
		assertEquals(textToSave,value);
	}
}
