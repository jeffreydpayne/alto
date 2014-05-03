package com.frs.alto.dao.riak;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;

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
	
	private ObjectMapper jsonMapper = new ObjectMapper();
	
	
	protected String getBucketName() {
		
		if (bucketName == null) {
			bucketName = getDomainClass().getName();
		}
		
		return TenantUtils.getThreadTenantIdentifier() + "-" + bucketName;
		
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
			return bucket.fetch(id, getDomainClass()).execute();
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
	
	

}
