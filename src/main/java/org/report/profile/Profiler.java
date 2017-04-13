package org.report.profile;



import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@Aspect
@Component
public class Profiler {

	private static final Logger log = Logger.getLogger(Profiler.class);
	
	@Around("execution(* org.report.services.*.*(..))")
	public Object profile(ProceedingJoinPoint joinPoint) throws Throwable{
		Monitor monitor = MonitorFactory.start("services.monitor");
		Object result = null;
		try {
			result = joinPoint.proceed();
		} finally {
			monitor.stop();
		}
		log.info(monitor);
		return result;
	}
	
}
