package com.frs.alto.dao.couchbase;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Logger;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;

import org.apache.commons.beanutils.PropertyUtils;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frs.alto.dao.BaseCachingDaoImpl;
import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.id.IdentifierGenerator;
import com.frs.alto.id.LocalUUIDGenerator;
import com.frs.alto.util.TenantUtils;

public abstract class CouchbaseDaoSupport<T extends BaseDomainObject> extends BaseCachingDaoImpl<T> implements InitializingBean {
	
	public final static String END_TOKEN = "\\u02ad";
	
	public final static String KEY_LIST_ID = "KEYLIST";
	
	private static DateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	

	private Logger logger = Logger.getLogger(CouchbaseDaoSupport.class.getName());
	
	@Autowired
	private CouchbaseClient client;
	
	private IdentifierGenerator idGenerator = new LocalUUIDGenerator();
	
	private String bucketName = null;
	
	private boolean multiTenant = true;
	
	private boolean enumerable = false;
	
	private EnumerationScheme enumerationScheme = EnumerationScheme.VIEW;
	
	private String keyNamespace = null;
	
	private boolean preserveFetchOrder = false;
	
	private Map<String, TemporalRangeKeyMapping> temporalRangeKeys = null;

	
	private ObjectMapper jsonMapper = new ObjectMapper();
	
	{
		
		TimeZone tz = TimeZone.getTimeZone("UTC");
		ISO_FORMAT.setTimeZone(tz);
		
	}
	
	
	protected String getBucketName() {
		
		return bucketName;
		
	}
	
	protected Collection<T> findBetweenWithView(String viewName, Date startDate, Date endDate) {
		
		return null;
	}
	
	protected Collection<T> findBeforeWithView(String viewName, Date queryDate) {
		
		return null;
		
	}
	
	protected Collection<T> findAfterWithView(String viewName, Date queryDate) {
		
		return null;
		
	}
	

	
	protected Collection<T> findBetweenWithView(String viewName, String hashKey, Date startDate, Date endDate) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey + "#" + ISO_FORMAT.format(startDate));
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey + "#" + ISO_FORMAT.format(endDate) +  "#" +  END_TOKEN);
		}
		else {
			query.setRangeStart(hashKey + "#" + ISO_FORMAT.format(startDate));
			query.setRangeEnd(hashKey + "#" + ISO_FORMAT.format(endDate) +  "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	protected Collection<T> findBeforeWithView(String viewName, String hashKey, Date queryDate) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey);
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey + "#" + ISO_FORMAT.format(queryDate) +  "#" +  END_TOKEN);
		}
		else {
			query.setRangeStart(hashKey);
			query.setRangeEnd(hashKey + "#" + ISO_FORMAT.format(queryDate) +  "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	protected Collection<T> findAfterWithView(String viewName, String hashKey, Date queryDate) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey + "#" + ISO_FORMAT.format(queryDate));
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey + "#" + END_TOKEN);
		}
		else {
			query.setRangeStart(hashKey + "#" + ISO_FORMAT.format(queryDate));
			query.setRangeEnd(hashKey +  "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
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
			T result =  jsonMapper.readValue(json, getDomainClass());
			result.setReadOnly(true);
			afterRead(result);
			return result;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected T fromJSON(CASValue<Object> cas) {
		try {
			if (cas == null) {
				return null;
			}
			
			T result =  fromJSON((String)cas.getValue());
			result.setCas(cas.getCas());
			result.setReadOnly(false);
			return result;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	protected String toStorageKey(T domain) {
		
		return toStorageKey(domain.getObjectIdentifier());
		
	}
	
	protected void writeKeyList(KeyList list) {
		//pretty concurrency hostile implementation - fix this

		
		try {
			if (list.getCas() > Long.MIN_VALUE) {
				if ( client.cas(getKeyListKey(), list.getCas(), jsonMapper.writeValueAsString(list.getKeys())) == CASResponse.EXISTS) {
					throw new IllegalStateException("Trying to save object with stale CAS value.");
				}
			}
			else {
				client.set(getKeyListKey(), jsonMapper.writeValueAsString(list.getKeys()));
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
			
	}
	
	protected KeyList fetchKeyList() {
		
		//could be a pretty concurrency hostile implementation - hopefully using CAS protects us from issues

		CASValue<Object> cas = client.gets(getKeyListKey());
		
		if (cas == null) {
			return new KeyList(new ArrayList<String>(), Long.MIN_VALUE);
		}
		
		try {
			return new KeyList((List<String>)jsonMapper.readValue((String)cas.getValue(), new TypeReference<List<String>>() {}), cas.getCas());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
	}
	
	
	protected String getKeyListKey() {
		
		StringBuilder sb = new StringBuilder();
		
		if (isMultiTenant()) {
			sb.append(TenantUtils.getThreadTenantIdentifier());
			sb.append("#");
		}
		sb.append(getKeyNamespace());
		sb.append("#");
		sb.append(KEY_LIST_ID);	
		
		return sb.toString();
		
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
	
	private void populateRangeKeys(T anObject) {
		
		
		
		
		
	}

	@Override
	public String save(T anObject) {
		
		try {
			
			populateRangeKeys(anObject);
			
			if (anObject.isReadOnly()) {
				throw new RuntimeException("This object is readonly.  Obtain a fresh instance before calling save.");
			}

			if (anObject.getObjectIdentifier() == null) {
				anObject.setObjectIdentifier(getIdGenerator().generateStringIdentifier(anObject));
			}
			//process temporal range keys
			if (temporalRangeKeys != null) {
				for (TemporalRangeKeyMapping mapping : temporalRangeKeys.values()) {
					Date sourceValue = (Date)PropertyUtils.getProperty(anObject, mapping.getSourceProperty());
					String targetValue = null;
					if (sourceValue != null) {
						targetValue = ISO_FORMAT.format(sourceValue);
					}
					PropertyUtils.setProperty(anObject, mapping.getTargetProperty(), targetValue);
				}
			}
			
			beforeWrite(anObject);
			if (anObject.getCas() > Long.MIN_VALUE) {
				if (client.cas(toStorageKey(anObject), anObject.getCas(), toJSON(anObject)) == CASResponse.EXISTS) {
					throw new IllegalStateException("Trying to save object with stale CAS value.");
				}
			}
			else {
				client.set(toStorageKey(anObject), toJSON(anObject));
			}
			
			if (isEnumerable() && (getEnumerationScheme().equals(EnumerationScheme.KEY_LIST))) {
				KeyList keyList = fetchKeyList();
				if (!keyList.getKeys().contains(anObject.getObjectIdentifier())) {
					keyList.getKeys().add(anObject.getObjectIdentifier());
					writeKeyList(keyList);
				}
			}
			
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
		
		if (isEnumerable() && (getEnumerationScheme().equals(EnumerationScheme.KEY_LIST))) {
			KeyList keyList = fetchKeyList();
			keyList.getKeys().remove(id);
			writeKeyList(keyList);
		}

		
	}
	
	protected void beforeWrite(T domain) throws Exception {
		
	}
	
	protected void afterRead(T domain) throws Exception {
		
		
	}

	@Override
	public T findById(String id) {
		CASValue<Object> cas = client.gets(toStorageKey(id));
		
		return fromJSON(cas);
	}

	@Override
	public Collection<String> findAllIds() {
		
		
		if (!isEnumerable()) {
			throw new UnsupportedOperationException();
		}
		
		switch (getEnumerationScheme()) {
			case VIEW:
				return findAllIdsWithView();
			case KEY_LIST:
				return findAllIdsWithKeyList();
		}
		
		return null;
		
		
	}
	
	protected Collection<String> findAllIdsWithKeyList() {
		
		return fetchKeyList().getKeys();
		
	}
	
	
	protected Collection<String> findAllIdsWithView() {
		
		View view = client.getView(getDesignDocumentName(), getViewName());

		Query query = new Query();
		query.setIncludeDocs(false);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier());
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" +  END_TOKEN);
		}
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
	
	
	protected Collection<T> findAllWithKeyList() {
		
		KeyList keyList = fetchKeyList();
		
		Collection<T> results = new ArrayList<T>(keyList.getKeys().size());
		List<String> translatedKeys = new ArrayList<String>();
		for (String id : keyList.getKeys()) {
			translatedKeys.add(toStorageKey(id));
		}
		for (Entry<String, Object> entry : client.getBulk(translatedKeys).entrySet()) {
			results.add(fromJSON((String)entry.getValue()));
		}
		
		return results;
		
	}
	
	protected Collection<T> findAllWithView() {
		
		View view = client.getView(getDesignDocumentName(), getViewName());

		Query query = new Query();
		query.setIncludeDocs(true); // Include the full document body
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier());
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" +  END_TOKEN);
		}
		 
		ViewResponse response = client.query(view, query);
		 
		// 4: Iterate over the Data and print out the full document
		Collection<T> results = new ArrayList<T>((int)response.getTotalRows());
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	@Override
	public Collection<T> findAll() {
		
		
		if (!isEnumerable()) {
			throw new UnsupportedOperationException();
		}
		
		switch (getEnumerationScheme()) {
			case VIEW:
				return findAllWithView();
			case KEY_LIST:
				return findAllWithKeyList();
		}
		
		
		return null;
		
	}
	
	
	
	@Override
	public Collection<T> findByIds(Collection<String> ids) {
		
		Collection<T> results = new ArrayList<T>();
		
		if (ids != null) {
			Collection<String> realKeys = new ArrayList<String>();
			for (String id : ids) {
				realKeys.add(toStorageKey(id));
			}
			
			 Map<String, Object> lookup = client.getBulk(realKeys);
			 
			 
			 if (isPreserveFetchOrder()) {
				 for (String id : ids) {
					 String key = toStorageKey(id);
					 if (lookup.containsKey(key)) {
						 results.add(fromJSON((String)lookup.get(key)));
					 }
				 }
			 }
			 else {
				 for (Object value : lookup.values()) {
					 results.add(fromJSON((String)value));
				 }
			 }
		}
			
		return results;
	}




	@Override
	public void afterPropertiesSet() throws Exception {
		cacheTemporalRangeKeys();
		initializeView();	
	}
	
	protected void cacheTemporalRangeKeys() throws Exception {
		
		
		temporalRangeKeys = new HashMap<String, CouchbaseDaoSupport.TemporalRangeKeyMapping>();
		
		for (Annotation annot : this.getClass().getAnnotations()) {
			if (annot.annotationType().equals(TemporalView.class)) {
				TemporalView view = (TemporalView)annot;
				temporalRangeKeys.put(view.timestampKey(), new TemporalRangeKeyMapping(view.timestampKey(), view.rangeKey()));
			}
			else if (annot.annotationType().equals(TemporalViewWithHashKey.class)) {
				TemporalViewWithHashKey view = (TemporalViewWithHashKey)annot;
				temporalRangeKeys.put(view.timestampKey(), new TemporalRangeKeyMapping(view.timestampKey(), view.rangeKey()));
			}

			
		}
		
	}
	
	
	protected void initializeView() throws Exception {
		if (isEnumerable() && (enumerationScheme.equals(EnumerationScheme.VIEW))) {
			View view = null;
			try {
				view = client.getView(getDesignDocumentName(), getViewName());
			}
			catch (InvalidViewException e) {}
			if (view == null) {
				logger.info("Adding Couchbase View: " + getDesignDocumentName());
				DesignDocument doc = new DesignDocument(getDesignDocumentName());
				ViewDesign design = new ViewDesign(getViewName(), getEnumerationMapFunction(), "");
				doc.setView(design);
				client.createDesignDoc(doc);
			}
		}
		
		for (Annotation annot : this.getClass().getAnnotations()) {
			if (annot.annotationType().equals(TemporalView.class)) {
				initializeTemporalView((TemporalView)annot);
			}
			else if (annot.annotationType().equals(RangeKeyView.class)) {
				initializeRangeKeyView((RangeKeyView)annot);
			}
			else if (annot.annotationType().equals(TemporalViewWithHashKey.class)) {
				initializeTemporalViewWithHashKey((TemporalViewWithHashKey)annot);
			}
			else if (annot.annotationType().equals(HashAndRangeKeyView.class)) {
				initializeHashAndRangeKeyView((HashAndRangeKeyView)annot);
			}
			
		}
		
	}
	
	protected void initializeHashAndRangeKeyView(HashAndRangeKeyView annotation) throws Exception {
		
		View view = null;
		try {
			view = client.getView(getViewName(annotation.name()), getViewName(annotation.name()));
		}
		catch (InvalidViewException e) {}
		if (view == null) {
			logger.info("Adding Couchbase View: " + getViewName(annotation.name()));
			DesignDocument doc = new DesignDocument(getViewName(annotation.name()));
			ViewDesign design = new ViewDesign(getViewName(annotation.name()), getHashAndRangeKeyMapFunction(annotation.hashKey(), annotation.rangeKey()), "");
			doc.setView(design);
			if (!client.createDesignDoc(doc)) {
				throw new RuntimeException("Unable to create view: " + getViewName(design.getName()));
			}
		}
		
		
	}
	
	protected void initializeRangeKeyView(RangeKeyView annotation) throws Exception {
		
		View view = null;
		try {
			view = client.getView(getViewName(annotation.name()), getViewName(annotation.name()));
		}
		catch (InvalidViewException e) {}
		if (view == null) {
			logger.info("Adding Couchbase View: " + getViewName(annotation.name()));
			DesignDocument doc = new DesignDocument(getViewName(annotation.name()));
			ViewDesign design = new ViewDesign(getViewName(annotation.name()), getRangeKeyMapFunction(annotation.rangeKey()), "");
			doc.setView(design);
			if (!client.createDesignDoc(doc)) {
				throw new RuntimeException("Unable to create view: " + getViewName(design.getName()));
			}
		}
		
		
	}
	
	protected void initializeTemporalView(TemporalView annotation) throws Exception {
		
		View view = null;
		try {
			view = client.getView(getViewName(annotation.name()), getViewName(annotation.name()));
		}
		catch (InvalidViewException e) {}
		if (view == null) {
			logger.info("Adding Couchbase View: " + getViewName(annotation.name()));
			DesignDocument doc = new DesignDocument(getViewName(annotation.name()));
			ViewDesign design = new ViewDesign(getViewName(annotation.name()), getRangeKeyMapFunction(annotation.rangeKey()), "");
			doc.setView(design);
			if (!client.createDesignDoc(doc)) {
				throw new RuntimeException("Unable to create view: " + getViewName(design.getName()));
			}
		}
		
		
	}
	
	protected void initializeTemporalViewWithHashKey(TemporalViewWithHashKey annotation) throws Exception {
		
		View view = null;
		try {
			view = client.getView(getViewName(annotation.name()), getViewName(annotation.name()));
		}
		catch (InvalidViewException e) {}
		if (view == null) {
			logger.info("Adding Couchbase View: " + getViewName(annotation.name()));
			DesignDocument doc = new DesignDocument(getViewName(annotation.name()));
			ViewDesign design = new ViewDesign(getViewName(annotation.name()), getHashAndRangeKeyMapFunction(annotation.hashKey(), annotation.rangeKey()), "");
			doc.setView(design);
			if (!client.createDesignDoc(doc)) {
				throw new RuntimeException("Unable to create view: " + getViewName(design.getName()));
			}
		}
		
	}
	
	
	protected String getRangeKeyMapFunction(String rangeKey) throws Exception {
		
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate( "com/frs/alto/dao/couchbase/range-key-map-template.js" );
        VelocityContext context = new VelocityContext();
        context.put("keyNameSpace", getKeyNamespace());
        context.put("rangeKey", rangeKey);
        StringWriter writer = new StringWriter();
        t.merge( context, writer );
		return writer.toString();
	}
	
	protected String getHashAndRangeKeyMapFunction(String hashKey, String rangeKey) throws Exception {
		
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate( "com/frs/alto/dao/couchbase/hash-and-range-key-map-template.js" );
        VelocityContext context = new VelocityContext();
        context.put("keyNameSpace", getKeyNamespace());
        context.put("hashKey", hashKey);
        context.put("rangeKey", rangeKey);
        StringWriter writer = new StringWriter();
        t.merge( context, writer );
		return writer.toString();
	}

	protected String getEnumerationMapFunction() throws Exception {
		
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        Template t = ve.getTemplate( "com/frs/alto/dao/couchbase/enumeration-map-template.js" );
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




	public EnumerationScheme getEnumerationScheme() {
		return enumerationScheme;
	}




	public void setEnumerationScheme(EnumerationScheme enumerationScheme) {
		this.enumerationScheme = enumerationScheme;
	}

	



	public boolean isPreserveFetchOrder() {
		return preserveFetchOrder;
	}




	public void setPreserveFetchOrder(boolean preserveFetchOrder) {
		this.preserveFetchOrder = preserveFetchOrder;
	}


	protected String getViewName(String viewName) {
		
		return getKeyNamespace() + "." + viewName;
		
	}

	protected String getKeyNamespace() {
		
		if (keyNamespace == null) {
			keyNamespace = getDomainClass().getSimpleName();
		}
		return keyNamespace;
		
		
	}
	
    private static class KeyList {
    	
    	private final long cas;
    	private final List<String> keys;
    	
    	public KeyList(List<String> keys, long cas) {
    		this.cas = cas;
    		this.keys = keys;
    	}

		public long getCas() {
			return cas;
		}

		public List<String> getKeys() {
			return keys;
		}
    	
    	
    	
    }
    
    
    private static class TemporalRangeKeyMapping {
    	
    	private String sourceProperty;
    	private String targetProperty;
    	
    	public TemporalRangeKeyMapping(String source, String target) {
    		sourceProperty = source;
    		targetProperty = target;
    	}
    	
    	public String getSourceProperty() {
    		return sourceProperty;
    	}
    	
    	public String getTargetProperty() {
    		return targetProperty;
    	}

    	
    }
}