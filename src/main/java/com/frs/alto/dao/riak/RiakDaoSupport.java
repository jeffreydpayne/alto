package com.frs.alto.dao.riak;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.bucket.Bucket;
import com.basho.riak.client.query.StreamingOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frs.alto.dao.BaseCachingDaoImpl;
import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.id.IdentifierGenerator;
import com.frs.alto.id.LocalUUIDGenerator;
import com.frs.alto.util.TenantUtils;

public abstract class RiakDaoSupport<T extends BaseDomainObject> extends BaseCachingDaoImpl<T> {
	
	@Autowired
	private IRiakClient riak;
	
	private IdentifierGenerator idGenerator = new LocalUUIDGenerator();
	
	private String bucketName = null;
	
	@Value("${alto.riak.bucket.namespace}") 
	private String bucketNamespace = null;
	
	@Value("${alto.riak.bucket.per.tenant:false}") 
	private boolean bucketPerTenant = false;
	
	private ObjectMapper jsonMapper = new ObjectMapper();
	
	
	protected String getBucketName() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(bucketNamespace);
		sb.append(".");
		
		if (bucketPerTenant) {
			sb.append(TenantUtils.getThreadTenantIdentifier());
			sb.append(".");
		}
		
		if (bucketName == null) {
			bucketName = getDomainClass().getName();
		}
		
		sb.append(bucketName);
		
		return sb.toString();
		
		
	}
	
	protected Bucket getBucket() throws Exception {
		
		return riak.fetchBucket(getBucketName()).execute();
		
	}
	
	protected String toJSON(T domain) throws Exception {
		
		return jsonMapper.writeValueAsString(domain);
		
	}

	@Override
	public String save(T anObject) {
		
		try {
			Bucket bucket = getBucket();
			
			if (anObject.getObjectIdentifier() == null) {
				anObject.setObjectIdentifier(idGenerator.generateStringIdentifier(anObject));
			}
			
			bucket.store(anObject.getObjectIdentifier(), anObject).execute();
			
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
		try {
			Bucket bucket = getBucket();
			bucket.delete(id).execute();
			
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public T findById(String id) {
		try {
			Bucket bucket = getBucket();
			if (StringUtils.isNotEmpty(id)) {
				return bucket.fetch(id, getDomainClass()).execute();
			}
			else {
				return null;
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<String> findAllIds() {
		try {
			Bucket bucket = getBucket();
			StreamingOperation<String> keyItr = bucket.keys();
			return keyItr.getAll();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Iterator<String> streamAllIds() {
		try {
			Bucket bucket = getBucket();
			StreamingOperation<String> keyItr = bucket.keys();
			return keyItr.iterator();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<T> findAll() {
		throw new UnsupportedOperationException("Not supported in Riak");
	}

	public IdentifierGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IdentifierGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	public String getBucketNamespace() {
		return bucketNamespace;
	}

	public void setBucketNamespace(String bucketNamespace) {
		this.bucketNamespace = bucketNamespace;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	
	

}
