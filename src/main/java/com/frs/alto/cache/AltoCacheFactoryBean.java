package com.frs.alto.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.frs.alto.hibernate4.Hibernate4RegionFactoryBean;

/**
 * Simple factory bean that allows the cache implementation to be specified via bean id.
 * This is intended to make it easier to inject a bean id via JNDI or configuration properties.
 * 
 */

public class AltoCacheFactoryBean implements FactoryBean<AltoCache>, ApplicationContextAware {
	
	private Log log = LogFactory.getLog(AltoCacheFactoryBean.class);
	
	private String altoCacheBeanId = null;
	
	private ApplicationContext applicationContext = null;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}

	@Override
	public AltoCache getObject() throws Exception {
		
		AltoCache cache = applicationContext.getBean(getAltoCacheBeanId(), AltoCache.class);
		log.info("Using Alto Cache Implementation: " + getAltoCacheBeanId() + "/" + cache.getClass().getName());
		
		return cache;
	}

	@Override
	public Class<?> getObjectType() {
		return AltoCache.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getAltoCacheBeanId() {
		return altoCacheBeanId;
	}

	public void setAltoCacheBeanId(String altoCacheBeanId) {
		this.altoCacheBeanId = altoCacheBeanId;
	}
	
}
