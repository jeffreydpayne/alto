package com.frs.alto.dao.couchbase;

import java.util.Collection;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import com.couchbase.client.CouchbaseClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frs.alto.dao.BaseCachingDaoImpl;
import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.id.IdentifierGenerator;
import com.frs.alto.id.LocalUUIDGenerator;
import com.frs.alto.util.TenantUtils;

public abstract class CouchbaseDaoSupport<T extends BaseDomainObject> extends BaseCachingDaoImpl<T> {
	

	private Logger logger = Logger.getLogger(CouchbaseDaoSupport.class.getName());
	
	@Autowired
	private CouchbaseClient client;
	
	private IdentifierGenerator idGenerator = new LocalUUIDGenerator();
	
	private String bucketName = null;
	
	private boolean multiTenant = true;

	
	private ObjectMapper jsonMapper = new ObjectMapper();
	
	
	protected String getBucketName() {
		
		return bucketName;
		
	}
	

	protected String toJSON(T domain)  {
		try {
			return jsonMapper.writeValueAsString(domain);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected T fromJSON(String json) {
		try {
			if (json == null) {
				return null;
			}
			return jsonMapper.readValue(json, getDomainClass());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	protected String toStorageKey(T domain) {
		
		return toStorageKey(domain.getObjectIdentifier());
		
	}
	
	protected String toStorageKey(String baseKey) {
		
		StringBuilder builder = new StringBuilder();
		if (isMultiTenant()) {
			builder.append(TenantUtils.getThreadTenantIdentifier());
			builder.append("#");
		}
		builder.append(getKeyNamespace());
		builder.append("#");
		builder.append(baseKey);
		
		return builder.toString();
		
	}

	@Override
	public String save(T anObject) {
		
		try {

			if (anObject.getObjectIdentifier() == null) {
				anObject.setObjectIdentifier(getIdGenerator().generateStringIdentifier(anObject));
			}
			
			client.set(toStorageKey(anObject), toJSON(anObject));
			
			return anObject.getObjectIdentifier();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void delete(T anObject) {
		
		delete(anObject.getObjectIdentifier());
		
	}

	@Override
	public void delete(String id) {
		
		client.delete(toStorageKey(id));

		
	}

	@Override
	public T findById(String id) {
		String json = (String)client.get(toStorageKey(id));
		
		return fromJSON(json);
	}

	@Override
	public Collection<String> findAllIds() {
		return null;
	}

	@Override
	public Collection<T> findAll() {
		return null;
		
	}

	public IdentifierGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IdentifierGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}


	public boolean isMultiTenant() {
		return multiTenant;
	}


	public void setMultiTenant(boolean multiTenant) {
		this.multiTenant = multiTenant;
	}

	protected abstract String getKeyNamespace();
	

}
