package com.frs.alto.dao.couchbase;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.InvalidViewException;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewDesign;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
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
	
	private boolean enumerable = false;

	
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
		View view = client.getView(getDesignDocumentName(), getViewName());

		Query query = new Query();
		query.setIncludeDocs(true);
		ViewResponse response = client.query(view, query);
		 
		Collection<String> results = new ArrayList<String>();
		for (ViewRow row : response) {
		  results.add(row.getValue());
		}
		
		return results;
	}
	
	protected String getDesignDocumentName() {
		
		return getKeyNamespace();
	}
	
	protected String getViewName() {
		
		return getKeyNamespace();
	}
	
	@Override
	public Collection<T> findAll() {
		
		View view = client.getView(getDesignDocumentName(), getViewName());

		Query query = new Query();
		query.setIncludeDocs(true); // Include the full document body
		 
		ViewResponse response = client.query(view, query);
		 
		// 4: Iterate over the Data and print out the full document
		Collection<T> results = new ArrayList<T>();
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
		
	}

	
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initializeView();	
	}
	
	protected void initializeView() throws Exception {
		if (isEnumerable()) {
			View view = null;
			try {
				view = client.getView(getDesignDocumentName(), getViewName());
			}
			catch (InvalidViewException e) {}
			if (view == null) {
				logger.info("Adding Couchbase View: " + getDesignDocumentName());
				DesignDocument doc = new DesignDocument(getDesignDocumentName());
				ViewDesign design = new ViewDesign(getViewName(), getMapFunction(), getReduceFunction());
				doc.setView(design);
				client.createDesignDoc(doc);
			}
		}
		
	}

	protected String getReduceFunction() throws Exception {
		return "";
	}

	protected String getMapFunction() throws Exception {
		
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate( "com/frs/alto/dao/couchbase/map-template.js" );
        VelocityContext context = new VelocityContext();
        context.put("keyNameSpace", getKeyNamespace());
        StringWriter writer = new StringWriter();
        t.merge( context, writer );
		return writer.toString();
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
	
	public boolean isEnumerable() {
		return enumerable;
	}




	public void setEnumerable(boolean enumerable) {
		this.enumerable = enumerable;
	}




	protected abstract String getKeyNamespace();
	

}
