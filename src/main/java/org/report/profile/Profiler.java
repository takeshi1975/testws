package org.report.profile;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class Profiler {

	private static final Logger log = Logger.getLogger(Profiler.class);
	
	@Around("execution(* org.report.service.*.*(..))")
	public Object profile(ProceedingJoinPoint joinPoint) throws Throwable{
		log.info("Estamos en el profiler *");
		long t1 = System.currentTimeMillis();
		Object result =  joinPoint.proceed();
		long t2 = System.currentTimeMillis();		
		log.info("Fin del profiler. Time -->"+(t2-t1));
		return result;
	}
	
}
