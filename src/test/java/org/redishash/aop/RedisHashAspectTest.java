package org.redishash.aop;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.google.common.collect.Lists;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j

class RedisHashAspectTest {

	@Data
	@Builder
	static class TElements{
		private String a;
		private int b;
		private String c;
		List<String> u;
	}
	public void testMethod(String m,List<TElements> l) {
		
	}
	/*
	 * @Case 测试SPEL Express中包含List, 及参数组合生成key
	 * 
	 */
	@Test
	void testParseKey() {
		final String hashKey="#l[0].a+#l[0].b+'_'+#l[0].c+'_'+#l[1].a";
		
		Object[] args =  new Object[2];
		args[0] = "Hello";
		List<TElements> le = Lists.newArrayList();
		TElements u1 = TElements.builder().a("eml_1").b(1).c("world").u(Lists.newArrayList("a","b","c","d")).build();
		le.add(u1);
		 u1 = TElements.builder().a("eml_2").b(2).u(Lists.newArrayList("a","b","c","d")).build();
		le.add(u1);
		args[1] = le;
		
		Method[] methods = this.getClass().getMethods();
		for(Method m: methods) {
			if("testMethod".equals(m.getName())) {
				RedisHashAspect ra = new RedisHashAspect();
				String value = ra.parseKey(hashKey, m, args);
				log.info("hashKey:{}, paresed value:{}",hashKey,value);
				assertEquals("eml_11_world_eml_2",value);
			}
		}
		
	}

}
