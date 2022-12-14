package org.redishash.aop;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;

import io.netty.util.internal.StringUtil;
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
            // ??????RedisCache??????
            RedisHGet cache = method.getAnnotation( RedisHGet.class);
            if (cache != null && cache.read()) {
            	
            	Class<?> rtnType = method.getReturnType();
            	log.debug("return type :{}",rtnType);
            	
            	
                // ????????????
                String cacheName = parseCacheName(cache.cache(), method, point.getArgs());
                String hashKey = parseKey(cache.hashKey(), method, point.getArgs());
                Object obj = cacher.locateObject(cacheName, hashKey);
                if (obj == null) {
                	log.debug("?????????????????????????????????: {}",method.getName());
                    obj = point.proceed(point.getArgs());
                    if (obj != null) {
                    	log.debug("????????????:{} ??????, ????????????cacheName:{},hashKey:{},val:{}",
                    			method.getName(),cacheName,hashKey,obj);
                    	if(cache.isJson())
                    		cacher.putObject(cacheName,hashKey,JSON.toJSONString(obj));
                    	else
                    		cacher.putObject(cacheName,hashKey,obj);
                    	
         
                    }
                }else {
                	if(cache.isJson()) {
                		//obj = JSONUtils.deserializeObject(obj.toString(), rtnType);
                		if(!rtnType.isArray() && !List.class.equals(rtnType))
                			obj = JSON.parseObject(obj.toString(), rtnType);
                		else {
                			Class<?> eleType = cache.clazz();
                			log.debug("parseArray for type:{}",eleType);
                			obj = JSON.parseArray(obj.toString(), eleType);
                		}
                		
                	}//else return obj directly
                }
                return obj;
            }
        } catch (Throwable ex) {
            log.error("<====== RedisHashCache ????????????: {} ======>", ex);
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
		String cacheName = parseCacheName(cache.cache(), method, point.getArgs());
				
		
		resultSet.stream().forEach(r ->{
					
			try {
				String hashKey = parseKeyOfResult(cache.hashKey(),method, point.getArgs(),r);
				
				cacher.putObject(cacheName,hashKey,JSON.toJSONString(r));
				//JSONUtils.serializeObject(r));
			} catch (Exception e) {
				
				//e.printStackTrace();
				log.debug("exception:", e);
				log.error("error happens :{}",e.getMessage());
			}
			
		});		
		
		
	}

	private String parseCacheName(String cacheName,Method method, Object[] args) {
		if(cacheName.contains("#")){
			return this.parseKey(cacheName, method, args);
		}else {
			return cacheName;
		}
	}
	@AfterReturning(value="cutpointHPut()", returning = "result")
	public void update(JoinPoint point,boolean result) {
		log.info("cutpointHPut:{}, result is:{}",point,result);
		if(!result) //not ok do nothing
			return;
		Method method = ((MethodSignature) point.getSignature()).getMethod();
		RedisHPut cache = method.getAnnotation(RedisHPut.class);
		String cacheName = parseCacheName(cache.cache(), method, point.getArgs());
		String hashKey = parseKey(cache.hashKey(), method, point.getArgs());
		Object value = parseValue(cache.value(),method,point.getArgs());
		try {
			if(cache.isJson()) {
			    cacher.putObject(cacheName,hashKey,JSON.toJSONString(value));
			    		//JSONUtils.serializeObject(value));
			}else {
				cacher.putObject(cacheName,hashKey,value);
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}

	@AfterReturning(value="cutpointHDel()", returning = "result")
	public void evict(JoinPoint point,boolean result) {
		log.info("cutpointHDel :{},result:{}",point,result);
		if(!result) //not ok do nothing
			return;
		
		Method method = ((MethodSignature) point.getSignature()).getMethod();
		RedisHDel cache = method.getAnnotation(RedisHDel.class);
		String cacheName = parseCacheName(cache.cache(), method, point.getArgs());
		String hashKey = parseKey(cache.hashKey(), method, point.getArgs());
		cacher.evictObject(cacheName,hashKey);
		
	}



	  /**
     * ???????????????key
     * key ???????????????????????????SPEL?????????
     *
     * @param hashKey
     * @param method
     * @param args
     * @return
     */
    protected String parseKey(String hashKey, Method method, Object[] args) {
        // ????????????????????????????????????(??????Spring????????????)
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);
        // ??????SPEL??????key?????????
        ExpressionParser parser = new SpelExpressionParser();
        // SPEL?????????
        StandardEvaluationContext context = new StandardEvaluationContext();
        // ?????????????????????SPEL????????????
        for (int i = 0; i < paraNameArr.length; i++) {
            context.setVariable(paraNameArr[i], args[i]);
        }
        return parser.parseExpression(hashKey).getValue(context, String.class);
    }
    @Deprecated
    private String parseKeyOfResult(String hashKey,  Object arg) {
    	final String prefix = "#"+RESULT_VAL+".";
    	Assert.isTrue(hashKey.startsWith(prefix), "hashKey????????????"+prefix);
    	
    	 ExpressionParser parser = new SpelExpressionParser();
         // SPEL?????????
         StandardEvaluationContext context = new StandardEvaluationContext();
         // ?????????????????????SPEL????????????
        
          context.setVariable(RESULT_VAL, arg);
         
         return parser.parseExpression(hashKey).getValue(context, String.class);
    	
    	
    }
    protected String parseKeyOfResult(String hashKey, Method method, Object[] args, Object r) {
    	// ????????????????????????????????????(??????Spring????????????)
    	LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
    	String[] paraNameArr = u.getParameterNames(method);
    	// ??????SPEL??????key?????????
    	ExpressionParser parser = new SpelExpressionParser();
    	// SPEL?????????d'd
    	StandardEvaluationContext context = new StandardEvaluationContext();


    	final String prefix = "#"+RESULT_VAL+".";
    	Assert.isTrue(hashKey.contains(prefix), "hashKey????????????"+prefix);



    	// ?????????????????????SPEL????????????
    	for (int i = 0; i < paraNameArr.length; i++) {
    		context.setVariable(paraNameArr[i], args[i]);
    	}

    	context.setVariable(RESULT_VAL, r);

    	return parser.parseExpression(hashKey).getValue(context, String.class);
    }
    
    protected Object parseValue(String valExp, Method method, Object[] args) {
    	log.debug("valExp is :{}",valExp);
    	//not set valExp, then use first parameter directly
    	if(StringUtil.isNullOrEmpty(valExp)) 
    		return args[0];
    	
        // ????????????????????????????????????(??????Spring????????????)
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);
        // ??????SPEL??????key?????????
        ExpressionParser parser = new SpelExpressionParser();
        // SPEL?????????
        StandardEvaluationContext context = new StandardEvaluationContext();
        // ?????????????????????SPEL????????????
        for (int i = 0; i < paraNameArr.length; i++) {
            context.setVariable(paraNameArr[i], args[i]);
        }
        return parser.parseExpression(valExp).getValue(context);
    }

}
