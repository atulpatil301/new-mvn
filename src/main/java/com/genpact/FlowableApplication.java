package com.genpact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.genpact.fda.controller", "com.genpact.fda.swagger", "com.genpact.fda.config","com.genpact.fda.pojo", "com.genpact.fda.service"})
public class FlowableApplication {
	private static final Logger logger = LoggerFactory.getLogger(FlowableApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(FlowableApplication.class, args);
	}

}
