package org.redishash.fastjson;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@SpringBootTest(classes=org.redishash.App.class)

class JSONDateTimeTest {
	
	
	
	@Autowired
	private SerializeConfig serializeConfig;
	
	
	@Data
	public class Inner{
		private LocalDateTime createTime;
		private String ower = "hellp";
		private String shouldbeNull;
		public Inner() {
			createTime = LocalDateTime.now();
		}
	}

	@Test
	void test() {
		
		
		log.info("format:{}",JSON.DEFFAULT_DATE_FORMAT);
		
		JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		Inner obj = new Inner();
		String value = JSON.toJSONString(obj, serializeConfig);
		//SerializerFeature.WriteMapNullValue,
			//	SerializerFeature.WriteDateUseDateFormat);
		
		log.info("json obj:{}",value);
		assertFalse(value.contains("\"shouldbeNull\":null"));
		
		value = JSON.toJSONString(obj, serializeConfig,SerializerFeature.WriteMapNullValue);
		//SerializerFeature.WriteMapNullValue,
			//	SerializerFeature.WriteDateUseDateFormat);
		
		log.info("WriteMapNullValue json obj:{}",value);
		assertTrue(value.contains("\"shouldbeNull\":null"));
		
	}
	
	

}
