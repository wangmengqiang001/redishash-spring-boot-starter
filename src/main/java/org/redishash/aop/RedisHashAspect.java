package org.redishash.aop;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redishash.annotation.RedisHDel;
import org.redishash.annotation.RedisHGet;
import org.redishash.annotation.RedisHMPut;
import org.redishash.annotation.RedisHPut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.alibaba.nacos.client.utils.JSONUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Aspect
public class RedisHashAspect {
	

	  
	private static final String RESULT_VAL = "resultVal";
	@Resource
	@Qualifier("redisHashCache")
	IHashCache cacher;
	
	@Pointcut("@annotation(org.redishash.annotation.RedisHGet)")
	public void cutpointHGet() {
		
	}
	@Pointcut("@annotation(org.redishash.annotation.RedisHPut)")
	public void cutpointHPut() {
		
	}
	@Pointcut("@annotation(org.redishash.annotation.RedisHDel)")
	public void cutpointHDel() {
		
	}
	
	@Pointcut("@annotation(org.redishash.annotation.RedisHMPut)")
	public void cutpointHMPut() {
		
	}
	
	
	@Around("cutpointHGet()")
	public Object cache(ProceedingJoinPoint point) {
        try {
       
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            // 获取RedisCache注解
            RedisHGet cache = method.getAnnotation( RedisHGet.class);
            if (cache != null && cache.read()) {
            	
            	Class<?> rtnType = method.getReturnType();
            	
                // 查询操作
                String cacheName = cache.cache();
                String hashKey = parseKey(cache.hashKey(), method, point.getArgs());
                Object obj = cacher.locateObject(cacheName, hashKey);
                if (obj == null) {
                	log.debug("未找到该对象，执行方法: {}",method.getName());
                    obj = point.proceed(point.getArgs());
                    if (obj != null) {
                    	log.debug("执行方法:{} 完成, 写入缓存cacheName:{},hashKey:{},val:{}",
                    			method.getName(),cacheName,hashKey,obj);
                    	
                    	cacher.putObject(cacheName,hashKey,JSONUtils.serializeObject(obj));
                    	
         
                    }
                }else {
                	obj = JSONUtils.deserializeObject(obj.toString(), rtnType);
                }
                return obj;
            }
        } catch (Throwable ex) {
            log.error("<====== RedisHashCache 执行异常: {} ======>", ex);
        }
        return null;
    }
	@AfterReturning( value = "cutpointHMPut()", returning = "resultSet")
	public void updateAll(JoinPoint point,List<?> resultSet) {
		log.info("afterReturning, to save list batchly");
		if(resultSet == null)
			return;
		Method method = ((MethodSignature) point.getSignature()).getMethod();
		RedisHMPut cache = method.getAnnotation(RedisHMPut.class);
		String cacheName = cache.cache();
		
		resultSet.stream().forEach(r ->{
					
			try {
				String hashKey = parseKeyOfResult(cache.hashKey(),r);
				
				cacher.putObject(cacheName,hashKey,JSONUtils.serializeObject(r));
			} catch (Exception e) {
				
				//e.printStackTrace();
				log.debug("exception:", e);
				log.error("error happens :{}",e.getMessage());
			}
			
		});		
		
		
	}
	@After("cutpointHPut()")
	public void update(JoinPoint point) {
		Method method = ((MethodSignature) point.getSignature()).getMethod();
		RedisHPut cache = method.getAnnotation(RedisHPut.class);
		String cacheName = cache.cache();
		String hashKey = parseKey(cache.hashKey(), method, point.getArgs());
		try {
			cacher.putObject(cacheName,hashKey,JSONUtils.serializeObject(point.getArgs()[0]));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}

	@After("cutpointHDel()")
	public void evict(JoinPoint point) {
		log.info("cutpointHDel :{}",point);
		
		Method method = ((MethodSignature) point.getSignature()).getMethod();
		RedisHDel cache = method.getAnnotation(RedisHDel.class);
		String cacheName = cache.cache();
		String hashKey = parseKey(cache.hashKey(), method, point.getArgs());
		cacher.evictObject(cacheName,hashKey);
		
	}



	  /**
     * 获取缓存的key
     * key 定义在注解上，支持SPEL表达式
     *
     * @param hashKey
     * @param method
     * @param args
     * @return
     */
    private String parseKey(String hashKey, Method method, Object[] args) {
        // 获取被拦截方法参数名列表(使用Spring支持类库)
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);
        // 使用SPEL进行key的解析
        ExpressionParser parser = new SpelExpressionParser();
        // SPEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 把方法参数放入SPEL上下文中
        for (int i = 0; i < paraNameArr.length; i++) {
            context.setVariable(paraNameArr[i], args[i]);
        }
        return parser.parseExpression(hashKey).getValue(context, String.class);
    }
    private String parseKeyOfResult(String hashKey,  Object arg) {
    	final String prefix = "#"+RESULT_VAL+".";
    	Assert.isTrue(hashKey.startsWith(prefix), "hashKey开头应是"+prefix);
    	
    	 ExpressionParser parser = new SpelExpressionParser();
         // SPEL上下文
         StandardEvaluationContext context = new StandardEvaluationContext();
         // 把方法参数放入SPEL上下文中
        
          context.setVariable(RESULT_VAL, arg);
         
         return parser.parseExpression(hashKey).getValue(context, String.class);
    	
    	
    }

}