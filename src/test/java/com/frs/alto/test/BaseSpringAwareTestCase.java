package com.frs.alto.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;

public class BaseSpringAwareTestCase extends Assert {
	
	private static ApplicationContext context = null;
	
	final protected ApplicationContext initializeApplicationContext() {

		if (context == null) {
			context = new ClassPathXmlApplicationContext(new String[] {getApplicationContextFileName()});
		}
		return context;

	}
	
	protected String getApplicationContextFileName() {
		return "spring-env-test.xml";
	}
	
	protected ApplicationContext getApplicationContext() {
		
		if (context == null) {
			initializeApplicationContext();
		}
		
		return context;
		
	}

}
