/*
 * Created on Aug 21, 2003
 * 
 */
package com.frs.alto.service;

import java.util.Collection;

import com.frs.alto.domain.BaseDomainObject;

/**
 * @author Jeffrey Payne
 * 
 * Defines core behavior for all brokers regardless of implementation.
 * For each broker, a subinterface declaring its finders should be defined.
 *
 */
public interface BaseService<T extends BaseDomainObject> {
	
	public String save(T anObject);
	public void delete(T anObject);
	public void delete(String id);
	public T findById(String id);
	public Collection<String> findAllIds();
	public Collection<T> findByIds(Collection<String> ids);
	public Collection<T> findAll();
	public Class<T> getDomainClass();
			
}
