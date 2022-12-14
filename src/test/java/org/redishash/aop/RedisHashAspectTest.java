package org.redishash.aop;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
	
	public List<TElements> findElements(){
		return null;
	}
	
	@Test
	void testFindElements() {
		
		
		Method[] methods = this.getClass().getMethods();
		for(Method m: methods) {
			if("findElements".equals(m.getName())) {
				Type rtType = m.getReturnType();
				assertNotNull(rtType);
				log.debug("type is :{}",rtType);
				assertEquals(List.class,rtType);
				
				Type genTpe = rtType.getClass().getGenericSuperclass();
				log.debug("generic type is :{}",genTpe);
				
				assertNotNull(genTpe);
				
				List<String> list = new ArrayList<String>() {};
				
		        Type clazz = list.getClass().getGenericSuperclass();
		        assertNotNull(clazz);
		        
		        ParameterizedType pt = (ParameterizedType)clazz;
		        log.debug("ParameterizedType:{}",pt);
		        log.debug("acturalType: {}", pt.getActualTypeArguments()[0]);
		        assertEquals(String.class,pt.getActualTypeArguments()[0]);
		        //.toString()
		        
		        
			}
		}
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
	
	@Test
	void testParseValue() {
		final String valExp="#l[0].u";
		
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
				Object value = ra.parseValue(valExp, m, args);
				log.info("valExp:{}, paresed value:{}",valExp,value);
				assertEquals(u1.u.getClass(),value.getClass());
				assertEquals(le.get(0).u,value);
			}
		}
		
	}
	
	@Test
	void testParseMPutKey() {
		String header = "Hiabc:";
		final String valExp="'"+header+"'"+"+#m+#l[0].a+#resultVal.u[0]"; //"Hiabc"+":"+
		//final String valExp=header+"+#m+#l[0].a+#resultVal.u[0]"; //"Hiabc"+":"+
		log.info("valExp:{}",valExp);
		
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
				Object value = ra.parseKeyOfResult(valExp, m, args,u1);
				log.info("valExp:{}, paresed value:{}",valExp,value);
				//assertEquals(u1.u.getClass(),value.getClass());
				assertEquals("Hiabc:Helloeml_1a",value);
			}
		}
		
	}
	
	

}
