package org.report.builder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan({"org.report.profile","org.report.controller", "org.report.services"})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class,args);
	}
	
}
