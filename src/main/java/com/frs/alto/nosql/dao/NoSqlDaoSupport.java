package com.frs.alto.nosql.dao;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.beans.factory.InitializingBean;

import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.nosql.ds.NoSqlDataSource;
import com.frs.alto.nosql.mapper.NoSqlKey;
import com.frs.alto.nosql.mapper.NoSqlObjectMapper;

public abstract class NoSqlDaoSupport<T extends BaseDomainObject, R extends Serializable> implements NoSqlDao<T, R>, InitializingBean {
	
	private static String ISO_TS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	private NoSqlDataSource dataSource;
	
	
	protected abstract Class<T> getDomainClass();
	
	protected abstract NoSqlObjectMapper getObjectMapper();
	
	protected Collection<T> generateSeedData() {
		return null;
	}
	
	protected DateFormat getDateFormat() {
		DateFormat fmt = new SimpleDateFormat(ISO_TS_FORMAT);
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		return fmt;
	}
	
	protected String formatDate(Date dt) {
		return getDateFormat().format(dt);
	}
	
	protected Date parseDate(String dt) {
		try {
			return getDateFormat().parse(dt);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	
	@Override
	public String nextId(BaseDomainObject domain) {
		return getDataSource().nextId(domain);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		if (dataSource.isAutoCreateEnabled() && !dataSource.tableExists(getDomainClass(), getObjectMapper())) {
			dataSource.createTable(getDomainClass(), getObjectMapper());
			Collection<T> results = generateSeedData();
			if (results != null) {
				save(results);
			}
		}
		
	}

	@Override
	public void delete(T anObject) {
		dataSource.delete(getDomainClass(), getObjectMapper(), getObjectMapper().getKey(anObject));
	}

	@Override
	public T findById(String id) {
		NoSqlKey key = getObjectMapper().getKey(getDomainClass(), id);
		return (T)dataSource.findByKey(getDomainClass(), getObjectMapper(), key);
	}
	
	@Override
	public T findById(String hashKey, R rangeKey) {
		
		StringBuilder sb = new StringBuilder(hashKey + "#");
						
		if (Date.class.isAssignableFrom(rangeKey.getClass())) {
			sb.append(formatDate((Date)rangeKey));
		}
		else {
			sb.append((String)rangeKey);
		}
		
		NoSqlKey key = getObjectMapper().getKey(getDomainClass(), sb.toString());
		return (T)dataSource.findByKey(getDomainClass(), getObjectMapper(), key);
		
	}

	@Override
	public Collection<String> findAllIds() {
		return dataSource.findAllIds(getDomainClass(), getObjectMapper());
	}

	@Override
	public Collection<T> findAll() {
		return (Collection<T>) dataSource.findAll(getDomainClass(), getObjectMapper());
	}

	

	@Override
	public T findByHashKey(String hashKey) {
		return (T)dataSource.findByKey(getDomainClass(), getObjectMapper(), getObjectMapper().getKey(getDomainClass(), hashKey));
	}

	@Override
	public Collection<T> findByHashKeys(Collection<String> hashKeys) {
		Collection<NoSqlKey> keys = new ArrayList<NoSqlKey>();
		for (String id : hashKeys) {
			keys.add(getObjectMapper().getKey(getDomainClass(), id));
		}
		return (Collection<T>) dataSource.findByHashKeys(getDomainClass(), getObjectMapper(), keys);
	}

	@Override
	public Collection<T> findWholeRange(String hashKey) {
		return (Collection<T>)dataSource.findWholeRange(getDomainClass(), getObjectMapper(), hashKey);
	}

	@Override
	public Collection<T> findByRange(String hashKey, R startRange, R endRange) {
		
		Class type = getObjectMapper().getRangeKeyType(getDomainClass());
		
		if (Number.class.isAssignableFrom(type)) {
			return (Collection<T>)dataSource.findByRange(getDomainClass(), getObjectMapper(), hashKey, (Number)startRange, (Number)endRange);
		}
		else if (Date.class.isAssignableFrom(type)) {
			return (Collection<T>)dataSource.findByRange(getDomainClass(), getObjectMapper(), hashKey, formatDate((Date)startRange), formatDate((Date)endRange));
		}
		else {
			return (Collection<T>)dataSource.findByRange(getDomainClass(), getObjectMapper(), hashKey, (String)startRange, (String)endRange);
		}
	}

	@Override
	public Collection<T> findUpperRange(String hashKey, R startRange) {
		Class type = getObjectMapper().getRangeKeyType(getDomainClass());
		
		if (Number.class.isAssignableFrom(type)) {
			return (Collection<T>)dataSource.findByUpperRange(getDomainClass(), getObjectMapper(), hashKey, (Number)startRange);
		}
		else if (Date.class.isAssignableFrom(type)) {
			return (Collection<T>)dataSource.findByUpperRange(getDomainClass(), getObjectMapper(), hashKey, formatDate((Date)startRange));
		}
		else {
			return (Collection<T>)dataSource.findByUpperRange(getDomainClass(), getObjectMapper(), hashKey, (String)startRange);
		}
	}

	@Override
	public Collection<T> findLowerRange(String hashKey, R endRange) {
		Class type = getObjectMapper().getRangeKeyType(getDomainClass());
		
		if (Number.class.isAssignableFrom(type)) {
			return (Collection<T>)dataSource.findByLowerRange(getDomainClass(), getObjectMapper(), hashKey, (Number)endRange);
		}
		else if (Date.class.isAssignableFrom(type)) {
			return (Collection<T>)dataSource.findByUpperRange(getDomainClass(), getObjectMapper(), hashKey, formatDate((Date)endRange));
		}
		else {
			return (Collection<T>)dataSource.findByLowerRange(getDomainClass(), getObjectMapper(), hashKey, (String)endRange);
		}
	}

	@Override
	public void delete(String hashKey) {
		dataSource.delete(getDomainClass(), getObjectMapper(), getObjectMapper().getKey(getDomainClass(), hashKey));
	}

	@Override
	public void delete(Collection<String> hashKeys) {
		Collection<NoSqlKey> keys = new ArrayList<NoSqlKey>();
		for (String id : hashKeys) {
			keys.add(getObjectMapper().getKey(getDomainClass(), id));
		}
		dataSource.delete(getDomainClass(), getObjectMapper(), keys);
		
	}

	@Override
	public String save(T domain) {
		return dataSource.save(domain, getObjectMapper());
	}

	@Override
	public Collection<String> save(Collection<T> domains) {
		return dataSource.save((Collection<BaseDomainObject>)domains, getObjectMapper());
	}

	public NoSqlDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(NoSqlDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	
}
