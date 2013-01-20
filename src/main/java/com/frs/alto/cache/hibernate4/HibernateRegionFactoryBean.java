package com.frs.alto.cache.hibernate4;

import org.hibernate.cache.spi.RegionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/*
 * A simple factory bean designed to allow a RegionFactory to be specified by bean id.
 * The idea here is a spring configuration context that may define multiple Regions
 * could have JNDI injected property determine which region to use.
 * 
 * The use case is that a simple EhCache implementation may suffice for simple self hosted system, but
 * it may be desirable to use a more complicated cache (TerraCotta or Memecached) in multi-tenant systems.
 */

public class HibernateRegionFactoryBean implements FactoryBean<RegionFactory>, ApplicationContextAware {
	
	
	private String regionFactoryBeanId = null;
	
	private ApplicationContext applicationContext;
		
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;		
	}

	public String getRegionFactoryBeanId() {
		return regionFactoryBeanId;
	}

	public void setRegionFactoryBeanId(String regionFactoryBeanId) {
		this.regionFactoryBeanId = regionFactoryBeanId;
	}

	@Override
	public RegionFactory getObject() throws Exception {
		return applicationContext.getBean(getRegionFactoryBeanId(), RegionFactory.class);
	}

	@Override
	public Class<?> getObjectType() {
		return RegionFactory.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	

	

	
	
}
