package com.frs.alto.hibernate4;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class Hibernate4RegionFactoryBean implements FactoryBean<RegionFactory>, ApplicationContextAware {
	
	private Log log = LogFactory.getLog(Hibernate4RegionFactoryBean.class);
	
	private String regionFactoryBeanId = null;
	
	private ApplicationContext applicationContext = null;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}

	@Override
	public RegionFactory getObject() throws Exception {
		RegionFactory factory = applicationContext.getBean(getRegionFactoryBeanId(), RegionFactory.class);
		log.info("Using Region Factory: " + getRegionFactoryBeanId() + "/" + factory.getClass().getName());
		return factory;
	}

	@Override
	public Class<?> getObjectType() {
		return RegionFactory.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getRegionFactoryBeanId() {
		return regionFactoryBeanId;
	}

	public void setRegionFactoryBeanId(String regionFactoryBeanId) {
		this.regionFactoryBeanId = regionFactoryBeanId;
	}

	
}
