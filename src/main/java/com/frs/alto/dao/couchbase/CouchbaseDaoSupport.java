package com.frs.alto.dao.couchbase;

import java.util.Collection;
import java.util.logging.Logger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewDesign;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frs.alto.dao.BaseCachingDaoImpl;
import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.id.IdentifierGenerator;
import com.frs.alto.id.LocalUUIDGenerator;
import com.frs.alto.util.TenantUtils;

public abstract class CouchbaseDaoSupport<T extends BaseDomainObject> extends BaseCachingDaoImpl<T> implements InitializingBean {
	

	private Logger logger = Logger.getLogger(CouchbaseDaoSupport.class.getName());
	
	@Autowired
	private CouchbaseClient client;
	
	private IdentifierGenerator idGenerator = new LocalUUIDGenerator();
	
	private String bucketName = null;
	
	private boolean multiTenant = true;
	
	private boolean hasViews = false;

	
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
	
	protected String getDesignDocumentName() {
		
		return null;
	}
	
	protected String getViewName() {
		
		return null;
	}
	
	@Override
	public Collection<T> findAll() {
		
		
		/*
		client.getSpatialView(designDocumentName, viewName)
		
		
		String designDoc = "users";
		String viewName = "by_firstname";
		View view = client.getView(designDoc, viewName);
		 
		// 2: Create a Query object to customize the Query
		Query query = new Query();
		query.setIncludeDocs(true); // Include the full document body
		 
		// 3: Actually Query the View and return the results
		ViewResponse response = client.query(view, query);
		 
		// 4: Iterate over the Data and print out the full document
		for (ViewRow row : response) {
		  System.out.println(row.getDocument());
		}
		*/
		
		
		return null;
		
		
	}

	
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initializeView();	
	}
	
	protected void initializeView() throws Exception {
		if (hasViews) {
			View view = client.getView(getDesignDocumentName(), getViewName());
			if (view == null) {
				DesignDocument doc = new DesignDocument(getDesignDocumentName());
				ViewDesign design = new ViewDesign(getViewName(), getMapFunction(), getReduceFunction());
				doc.setView(design);
				client.createDesignDoc(doc);
			}
		}
		
	}

	protected String getReduceFunction() throws Exception {
		return null;
	}

	protected String getMapFunction() throws Exception {
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
