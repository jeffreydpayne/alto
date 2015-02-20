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
import org.apache.commons.lang.StringUtils;
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
import com.frs.alto.core.TenantMetaData;
import com.frs.alto.dao.BaseCachingDaoImpl;
import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.id.IdentifierGenerator;
import com.frs.alto.id.LocalUUIDGenerator;
import com.frs.alto.util.TenantUtils;

public abstract class CouchbaseDaoSupport<T extends BaseDomainObject> extends BaseCachingDaoImpl<T> implements InitializingBean {
	
	public final static String END_TOKEN = "\\u02ad";
	
	public final static String KEY_SINGLETON = "singleton";
	
	public final static String KEY_LIST_ID = "KEYLIST";
	
	private static DateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	

	private Logger logger = Logger.getLogger(CouchbaseDaoSupport.class.getName());
	
	@Autowired
	private CouchbaseClient client;
	
	private Integer ttl = null;
	
	private IdentifierGenerator idGenerator = new LocalUUIDGenerator();
	
	private String bucketName = null;
	
	private boolean multiTenant = true;
	
	private boolean enumerable = false;
	
	private EnumerationScheme enumerationScheme = EnumerationScheme.VIEW;
	
	private String keyNamespace = null;
	
	private boolean preserveFetchOrder = false;
	
	private boolean singleton = false;
	
	private Map<String, TemporalRangeKeyMapping> temporalRangeKeys = null;

	
	private ObjectMapper jsonMapper = new ObjectMapper();
	
	{
		
		TimeZone tz = TimeZone.getTimeZone("UTC");
		ISO_FORMAT.setTimeZone(tz);
		
	}
	
	
	protected String getBucketName() {
		
		return bucketName;
		
	}
	
	protected Collection<T> findBetweenWithView(String viewName, String startRange, String endRange) {

		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + startRange);
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + endRange +  "#" +  END_TOKEN);
		}
		else {
			query.setRangeStart(startRange);
			query.setRangeEnd(endRange +  "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	protected Collection<T> findBeforeWithView(String viewName, String queryRange) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier());
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + queryRange +  "#" +  END_TOKEN);
		}
		else {
			query.setRangeStart("");
			query.setRangeEnd(queryRange +  "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	protected Collection<T> findAfterWithView(String viewName, String queryRange) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + queryRange);
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + END_TOKEN);
		}
		else {
			query.setRangeStart(queryRange);
			query.setRangeEnd(END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	

	
	protected Collection<T> findBetweenWithView(String viewName, String hashKey, String startRange, String endRange) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey + "#" + startRange);
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey + "#" + endRange +  "#" +  END_TOKEN);
		}
		else {
			query.setRangeStart(hashKey + "#" + startRange);
			query.setRangeEnd(hashKey + "#" + endRange +  "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	protected Collection<T> findBeforeWithView(String viewName, String hashKey, String queryRange) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey);
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey + "#" + queryRange +  "#" +  END_TOKEN);
		}
		else {
			query.setRangeStart(hashKey);
			query.setRangeEnd(hashKey + "#" + queryRange +  "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	protected Collection<T> findAfterWithView(String viewName, String hashKey, String queryRange) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey + "#" + queryRange);
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + hashKey + "#" + END_TOKEN);
		}
		else {
			query.setRangeStart(hashKey + "#" + queryRange);
			query.setRangeEnd(hashKey +  "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	
	
	public boolean isSingleton() {
		return singleton;
	}

	protected Collection<T> findBetweenWithView(String viewName, Date startDate, Date endDate) {

		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + ISO_FORMAT.format(startDate));
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + ISO_FORMAT.format(endDate) +  "#" +  END_TOKEN);
		}
		else {
			query.setRangeStart(ISO_FORMAT.format(startDate));
			query.setRangeEnd(ISO_FORMAT.format(endDate) +  "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	protected Collection<T> findBeforeWithView(String viewName, Date queryDate) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier());
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + ISO_FORMAT.format(queryDate) +  "#" +  END_TOKEN);
		}
		else {
			query.setRangeStart("");
			query.setRangeEnd(ISO_FORMAT.format(queryDate) +  "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	protected Collection<T> findAfterWithView(String viewName, Date queryDate) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));
		
		Collection<T> results = new ArrayList<T>();

		Query query = new Query();
		query.setIncludeDocs(true);
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + ISO_FORMAT.format(queryDate));
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + END_TOKEN);
		}
		else {
			query.setRangeStart(ISO_FORMAT.format(queryDate));
			query.setRangeEnd(END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		 
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
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
	
	protected void writeKeyList(KeyList list, TenantMetaData md) {
		//pretty concurrency hostile implementation - fix this

		
		try {
			if (list.getCas() > Long.MIN_VALUE) {
				if ( client.cas(getKeyListKey(md), list.getCas(), jsonMapper.writeValueAsString(list.getKeys())) == CASResponse.EXISTS) {
					throw new IllegalStateException("Trying to save object with stale CAS value.");
				}
			}
			else {
				client.set(getKeyListKey(md), jsonMapper.writeValueAsString(list.getKeys()));
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
			
	}
	
	protected KeyList fetchKeyList(TenantMetaData md) {
		
		//could be a pretty concurrency hostile implementation - hopefully using CAS protects us from issues

		CASValue<Object> cas = client.gets(getKeyListKey(md));
		
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
	
	
	protected String getKeyListKey(TenantMetaData md) {
		
		StringBuilder sb = new StringBuilder();
		
		if (md != null) {
			sb.append(md.getTenantIdentifier());
			sb.append("#");
		}
		sb.append(getKeyNamespace());
		sb.append("#");
		sb.append(KEY_LIST_ID);	
		
		return sb.toString();
		
	}
	
	
	protected String toStorageKey(String baseKey) {
		
		if (isMultiTenant()) {
			return toStorageKey(TenantUtils.getThreadTenant(), baseKey);
		}
		else {
			return toStorageKey(null, baseKey);
		}
	
		
	}
	
	protected String toStorageKey(TenantMetaData tenant, String baseKey) {
		
		StringBuilder builder = new StringBuilder();
		if (tenant != null) {
			builder.append(tenant.getTenantIdentifier());
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
			
			if (isSingleton()) {
				anObject.setObjectIdentifier(KEY_SINGLETON);
			}
			else if (anObject.getObjectIdentifier() == null) {
				anObject.setObjectIdentifier(getIdGenerator().generateStringIdentifier(anObject));
			}
			//process temporal range keys
			if (temporalRangeKeys != null) {
				for (TemporalRangeKeyMapping mapping : temporalRangeKeys.values()) {
					Object sourceValue = PropertyUtils.getProperty(anObject, mapping.getSourceProperty());
					Date sourceDate = null;
					String targetValue = null;
					if (sourceValue instanceof Long) {
						sourceDate = new Date((Long)sourceValue);
					}
					else {
						sourceDate = (Date)sourceValue;
					}
					if (sourceValue != null) {
						targetValue = ISO_FORMAT.format(sourceDate);
					}
					PropertyUtils.setProperty(anObject, mapping.getTargetProperty(), targetValue);
				}
			}
			
			beforeWrite(anObject);
			if (anObject.getCas() > Long.MIN_VALUE &&
                    (client.cas(toStorageKey(anObject), anObject.getCas(), toJSON(anObject)) == CASResponse.EXISTS)) {
                
				throw new IllegalStateException("Trying to save object with stale CAS value.");
			}
			else {
				if (getTtl() != null) {
					client.set(toStorageKey(anObject), getTtl(), toJSON(anObject));
				}
				else {
					client.set(toStorageKey(anObject), toJSON(anObject));
				}
			}
			
			if (isEnumerable() && (getEnumerationScheme().equals(EnumerationScheme.KEY_LIST))) {
				KeyList keyList = fetchKeyList(TenantUtils.getThreadTenant());
				if (!keyList.getKeys().contains(anObject.getObjectIdentifier())) {
					keyList.getKeys().add(anObject.getObjectIdentifier());
					writeKeyList(keyList, TenantUtils.getThreadTenant());
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
			KeyList keyList = fetchKeyList(TenantUtils.getThreadTenant());
			keyList.getKeys().remove(id);
			writeKeyList(keyList, TenantUtils.getThreadTenant());
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
	
	public T findById(TenantMetaData tenant, String id) {
		CASValue<Object> cas = client.gets(toStorageKey(tenant, id));
		
		return fromJSON(cas);
	}
	
	@Override
	public Collection<String> findAllIds() {
		
		if (isMultiTenant()) {
			return findAllIds(TenantUtils.getThreadTenant());
		}
		else {
			return findAllIds(null);
		}
	}

	public Collection<String> findAllIds(TenantMetaData tenant) {
		
		
		if (!isEnumerable()) {
			throw new UnsupportedOperationException();
		}
		
		switch (getEnumerationScheme()) {
			case VIEW:
				return findAllIdsWithView(tenant);
			case KEY_LIST:
				return findAllIdsWithKeyList(tenant);
		}
		
		return null;
		
		
	}
	
	
	
	@Override
	public long findCount() {
		if (isMultiTenant()) {
			return findCount(TenantUtils.getThreadTenant());
		}
		else {
			return findCount(null);
		}
					
	}
	
	public long findCount(TenantMetaData tenant) {
		
		if (!isEnumerable()) {
			throw new UnsupportedOperationException();
		}
		
		switch (getEnumerationScheme()) {
			case VIEW:
				return findCountWithView(tenant);
			case KEY_LIST:
				return findCountWithKeyList(tenant);
		}
		
		return 0;
		
		
	}
	
	protected long findCountWithKeyList(TenantMetaData tenant) {
		
		return fetchKeyList(tenant).getKeys().size();
		
	}

	protected Collection<String> findAllIdsWithKeyList(TenantMetaData tenant) {
		
		return fetchKeyList(tenant).getKeys();
		
	}
	
	protected long findCountWithView(TenantMetaData tenant) {
		
		View view = client.getView(getDesignDocumentName(), getViewName());

		Query query = new Query();
		query.setIncludeDocs(false);
		if (tenant != null) {
			query.setRangeStart(tenant.getTenantIdentifier());
			query.setRangeEnd(tenant.getTenantIdentifier() + "#" +  END_TOKEN);
		}
		ViewResponse response = client.query(view, query);
		
		return response.getTotalRows();
		
	}
	
	
	protected Collection<String> findAllIdsWithView(TenantMetaData tenant) {
		
		View view = client.getView(getDesignDocumentName(), getViewName());

		Query query = new Query();
		query.setIncludeDocs(false);
		if (tenant != null) {
			query.setRangeStart(tenant.getTenantIdentifier());
			query.setRangeEnd(tenant.getTenantIdentifier() + "#" +  END_TOKEN);
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
	
	
	protected Collection<T> findAllWithKeyList(TenantMetaData tenant) {
		
		KeyList keyList = fetchKeyList(tenant);
		
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
	
	protected Collection<String> findAllKeysWithView(String viewName) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));

		Query query = new Query();
		query.setIncludeDocs(false); // Include the full document body
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier());
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" +  END_TOKEN);
		}
		 
		ViewResponse response = client.query(view, query);
		 
		Collection<String> results = new ArrayList<String>((int)response.getTotalRows());
		for (ViewRow row : response) {
		  results.add(row.getValue());
		}
		
		return results;
		
	}
	
	protected Collection<String> findAllKeysWithView(String viewName, String rangeKeyValue) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));

		Query query = new Query();
		query.setIncludeDocs(true); // Include the full document body
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + rangeKeyValue);
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + rangeKeyValue + "#" +  END_TOKEN);
		}
		else {
			query.setRangeStart(rangeKeyValue);
			query.setRangeEnd(rangeKeyValue + "#" +  END_TOKEN);
		}
		 
		ViewResponse response = client.query(view, query);
		 
		Collection<String> results = new ArrayList<String>((int)response.getTotalRows());
		for (ViewRow row : response) {
		  results.add(row.getValue());
		}
		
		return results;
		
	}
	
	protected Collection<T> findAllWithView(String viewName) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));

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
	
	protected Collection<T> findAllWithView(String viewName, String rangeKeyValue) {
		
		View view = client.getView(getViewName(viewName), getViewName(viewName));

		Query query = new Query();
		query.setIncludeDocs(true); // Include the full document body
		if (isMultiTenant()) {
			query.setRangeStart(TenantUtils.getThreadTenantIdentifier() + "#" + rangeKeyValue);
			query.setRangeEnd(TenantUtils.getThreadTenantIdentifier() + "#" + rangeKeyValue + "#" +  END_TOKEN);
		}
		else {
			query.setRangeStart(rangeKeyValue);
			query.setRangeEnd(rangeKeyValue + "#" +  END_TOKEN);
		}
		 
		ViewResponse response = client.query(view, query);
		 
		// 4: Iterate over the Data and print out the full document
		Collection<T> results = new ArrayList<T>((int)response.getTotalRows());
		for (ViewRow row : response) {
		  results.add(fromJSON((String)row.getDocument()));
		}
		
		return results;
		
	}
	
	protected Collection<T> findAllWithView(TenantMetaData tenant) {
		
		View view = client.getView(getDesignDocumentName(), getViewName());

		Query query = new Query();
		query.setIncludeDocs(true); // Include the full document body
		
		if (tenant != null) {
			query.setRangeStart(tenant.getTenantIdentifier());
			query.setRangeEnd(tenant.getTenantIdentifier() + "#" +  END_TOKEN);
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
		
		if (isMultiTenant()) {
			return findAll(TenantUtils.getThreadTenant());
		}
		else {
			return findAll(null);
		}
		
	}
	
	public Collection<T> findAll(TenantMetaData md) {
		
		
		if (!isEnumerable()) {
			throw new UnsupportedOperationException();
		}
		
		switch (getEnumerationScheme()) {
			case VIEW:
				return findAllWithView(md);
			case KEY_LIST:
				return findAllWithKeyList(md);
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
			else if (annot.annotationType().equals(CustomRangeKeyView.class)) {
				initializeCustomRangeKeyView((CustomRangeKeyView)annot);
			}
			else if (annot.annotationType().equals(TemporalViewWithHashKey.class)) {
				initializeTemporalViewWithHashKey((TemporalViewWithHashKey)annot);
			}
			else if (annot.annotationType().equals(HashAndRangeKeyView.class)) {
				initializeHashAndRangeKeyView((HashAndRangeKeyView)annot);
			}
			else if (annot.annotationType().equals(CustomHashAndRangeKeyView.class)) {
				initializeCustomHashAndRangeKeyView((CustomHashAndRangeKeyView)annot);
			}
			
		}
		
	}
	
	protected void initializeCustomHashAndRangeKeyView(CustomHashAndRangeKeyView annotation) throws Exception {
		
		View view = null;
		try {
			view = client.getView(getViewName(annotation.name()), getViewName(annotation.name()));
		}
		catch (InvalidViewException e) {}
		if (view == null) {
			logger.info("Adding Couchbase View: " + getViewName(annotation.name()));
			DesignDocument doc = new DesignDocument(getViewName(annotation.name()));
			ViewDesign design = new ViewDesign(getViewName(annotation.name()), getCustomViewFunction(annotation.mapFunctionPath(), annotation.rangeKey(), annotation.hashKey()), getCustomViewFunction(annotation.reduceFunctionPath(), annotation.rangeKey(), annotation.hashKey()));
			doc.setView(design);
			if (!client.createDesignDoc(doc)) {
				throw new RuntimeException("Unable to create view: " + getViewName(design.getName()));
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
	
	protected void initializeCustomRangeKeyView(CustomRangeKeyView annotation) throws Exception {
		
		View view = null;
		try {
			view = client.getView(getViewName(annotation.name()), getViewName(annotation.name()));
		}
		catch (InvalidViewException e) {}
		if (view == null) {
			logger.info("Adding Couchbase View: " + getViewName(annotation.name()));
			DesignDocument doc = new DesignDocument(getViewName(annotation.name()));
			ViewDesign design = new ViewDesign(getViewName(annotation.name()), getCustomViewFunction(annotation.mapFunctionPath(), annotation.rangeKey(), null ), getCustomViewFunction(annotation.reduceFunctionPath(), annotation.rangeKey(), null));
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
	
	public T findSingleton() {
		
		return findSingleton(TenantUtils.getThreadTenant());
		
	}
	
	public T findSingleton(TenantMetaData tenant) {
		
		T result = findById(tenant, KEY_SINGLETON);
		
		if (result == null) {
			try {
				result = getDomainClass().newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		return result;
		
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
	
	protected String getCustomViewFunction(String functionPath, String rangeKey, String hashKey) throws Exception {
		
		if (StringUtils.isEmpty(functionPath)) {
			return "";
		}
		
		
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        
        String fullPath = this.getClass().getPackage().getName();
        fullPath = StringUtils.replace(fullPath, ".", "/");
        
        Template t = ve.getTemplate( fullPath + "/" + functionPath );
        VelocityContext context = new VelocityContext();
        context.put("keyNameSpace", getKeyNamespace());
        if (rangeKey != null) {
        	 context.put("rangeKey", rangeKey);
        }
        if (hashKey != null) {
        	context.put("hashKey", hashKey);
        }
        StringWriter writer = new StringWriter();
        t.merge( context, writer );
        
        System.out.println(writer.toString());
        
		return writer.toString();
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
	
	
	
    public void setClient(CouchbaseClient client) {
		this.client = client;
	}



	public Integer getTtl() {
		return ttl;
	}

	public void setTtl(Integer ttl) {
		this.ttl = ttl;
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
