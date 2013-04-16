package com.frs.alto.nosql.ds.simpledb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchDeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeletableItem;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.nosql.ds.BaseNoSqlDataSource;
import com.frs.alto.nosql.mapper.NoSqlKey;
import com.frs.alto.nosql.mapper.NoSqlObjectMapper;
import com.frs.alto.nosql.mapper.TypeTransformer;

public class SimpleDBDatasource extends BaseNoSqlDataSource implements InitializingBean, TypeTransformer {
	
	private static SimpleDateFormat ISO_TS_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	private static Log log = LogFactory.getLog(SimpleDBDatasource.class);
	
	private String domainNamespace = null;
	private String apiId = null;
	private String secretKey = null;
	private String endpoint = null;
	private boolean consistentReads = true;
	private int maxBatchSize = 25;
	
	
	private AWSCredentials credentials = null;
	private AmazonSimpleDBClient client = null;
	private Collection<String> domainNames = null;
	
	public void afterPropertiesSet() throws Exception {
		
		refreshTableNames();
		ISO_TS_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		
	}

	public AWSCredentials getCredentials() {
		if (credentials == null) {
			credentials = new BasicAWSCredentials(apiId, secretKey);
		}
		return credentials;
	}
	
	public AmazonSimpleDBClient getClient() {
		if (client == null) {
			client = new AmazonSimpleDBClient(getCredentials());
			if (endpoint != null) {
				client.setEndpoint(endpoint);
				
			}
		}
		return client;
	}
	
	public void refreshTableNames() {
		
		log.info("Loading SimpleDB Domains...");
				

		ListDomainsResult result = getClient().listDomains();
		
		domainNames = new ArrayList<String>();
		for (String tableName : result.getDomainNames()) {
			if (tableName.startsWith(domainNamespace)) {
				log.info("Domain: " + tableName);		
				domainNames.add(tableName);
			}
		}
		
		
	}
	
	protected String assembleDomainName(Class clazz, NoSqlObjectMapper mapper) {
		return getDomainNamespace() + "-" + mapper.getTableName(clazz);
	}
	
	@Override
	public BaseDomainObject findByKey(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, NoSqlKey key) {
		
		
		GetAttributesRequest request = new GetAttributesRequest(assembleDomainName(clazz, mapper), key.composeUniqueString());
		request.withConsistentRead(consistentReads);
		GetAttributesResult result = getClient().getAttributes(request);
		if (result.getAttributes().size() > 0) {
			return instantiate(clazz, mapper, result.getAttributes());
		}
		else {
			return null;
		}
		
	}
	
	protected BaseDomainObject instantiate(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, List<Attribute> attributes) {
		
		BaseDomainObject domain = mapper.instantiate(clazz);
		
		Map<String, Object> attributeMap = new HashMap<String, Object>();
		
		for (Attribute attrib : attributes) {
			attributeMap.put(attrib.getName(), attrib.getValue());
		}
		
		mapper.fromAttributes(domain, attributeMap);
		
		return domain;
		
	}

	@Override
	public Collection<BaseDomainObject> findByHashKeys(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, Collection<NoSqlKey> hashKeys) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select * from `");
		sb.append(assembleDomainName(clazz, mapper));
		sb.append("` where itemName() in (");
		boolean firstId = false;
		for (NoSqlKey key : hashKeys) {
			if (firstId) {
				sb.append(",");
			}
			sb.append("'");
			sb.append(key.composeUniqueString());
			sb.append("'");
			firstId = true;
		}
		sb.append(")");
		
		
		return findByQuery(clazz, mapper, sb.toString());
	}

	@Override
	public Collection<BaseDomainObject> findWholeRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select * from `");
		sb.append(assembleDomainName(clazz, mapper));
		sb.append("` where ");
		sb.append(mapper.getHashKeyAttribute(clazz));
		sb.append(" = '");
		sb.append(hashKey);
		sb.append("'");
		
		return findByQuery(clazz, mapper, sb.toString());
		
	}

	@Override
	public Collection<BaseDomainObject> findByRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey, String startRange, String endRange) {
		
		String rangeKey = mapper.getRangeKeyAttribute(clazz);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select * from `");
		sb.append(assembleDomainName(clazz, mapper));
		sb.append("` where ");
		sb.append(mapper.getHashKeyAttribute(clazz));
		sb.append("= '");
		sb.append(hashKey);
		sb.append("' and ");
		sb.append(rangeKey);
		sb.append(" >= '");
		sb.append(startRange);
		sb.append("' and ");
		sb.append(rangeKey);
		sb.append(" <= '");
		sb.append(endRange);
		sb.append("'");
		
		return findByQuery(clazz, mapper, sb.toString());

	}

	@Override
	public Collection<BaseDomainObject> findByUpperRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, String startRange) {
		
		String rangeKey = mapper.getRangeKeyAttribute(clazz);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select * from `");
		sb.append(assembleDomainName(clazz, mapper));
		sb.append("` where ");
		sb.append(mapper.getHashKeyAttribute(clazz));
		sb.append("= '");
		sb.append(hashKey);
		sb.append("' and ");
		sb.append(rangeKey);
		sb.append(" >= '");
		sb.append(startRange);
		sb.append("'");
		
		return findByQuery(clazz, mapper, sb.toString());
	}

	@Override
	public Collection<BaseDomainObject> findByLowerRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, String endRange) {
		
		String rangeKey = mapper.getRangeKeyAttribute(clazz);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select * from `");
		sb.append(assembleDomainName(clazz, mapper));
		sb.append("` where ");
		sb.append(mapper.getHashKeyAttribute(clazz));
		sb.append("= '");
		sb.append(hashKey);
		sb.append("' and ");
		sb.append(rangeKey);
		sb.append(" <= '");
		sb.append(endRange);
		sb.append("'");
		return findByQuery(clazz, mapper, sb.toString());
	}

	@Override
	public Collection<BaseDomainObject> findByRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number startRange, Number endRange) {

		String rangeKey = mapper.getRangeKeyAttribute(clazz);
		Class rangeKeyType = mapper.getRangeKeyType(clazz);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select * from `");
		sb.append(assembleDomainName(clazz, mapper));
		sb.append("` where ");
		sb.append(mapper.getHashKeyAttribute(clazz));
		sb.append("= '");
		sb.append(hashKey);
		sb.append("' and ");
		sb.append(rangeKey);
		sb.append(" >= '");
		sb.append(toAttributeValue(startRange, rangeKeyType));
		sb.append("' and ");
		sb.append(rangeKey);
		sb.append(" <= '");
		sb.append(toAttributeValue(endRange, rangeKeyType));
		sb.append("'");
		
		return findByQuery(clazz, mapper, sb.toString());
		
		
	}

	@Override
	public Collection<BaseDomainObject> findByUpperRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number startRange) {
		String rangeKey = mapper.getRangeKeyAttribute(clazz);
		Class rangeKeyType = mapper.getRangeKeyType(clazz);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select * from `");
		sb.append(assembleDomainName(clazz, mapper));
		sb.append("` where ");
		sb.append(mapper.getHashKeyAttribute(clazz));
		sb.append("= '");
		sb.append(hashKey);
		sb.append("' and ");
		sb.append(rangeKey);
		sb.append(" >= '");
		sb.append(toAttributeValue(startRange, rangeKeyType));
		sb.append("'");
		
		return findByQuery(clazz, mapper, sb.toString());
	}

	@Override
	public Collection<BaseDomainObject> findByLowerRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number endRange) {
		
		String rangeKey = mapper.getRangeKeyAttribute(clazz);
		Class rangeKeyType = mapper.getRangeKeyType(clazz);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select * from `");
		sb.append(assembleDomainName(clazz, mapper));
		sb.append("` where ");
		sb.append(mapper.getHashKeyAttribute(clazz));
		sb.append("= '");
		sb.append(hashKey);
		sb.append("' and ");
		sb.append(rangeKey);
		sb.append(" <= '");
		sb.append(toAttributeValue(endRange, rangeKeyType));
		sb.append("'");
		
		return findByQuery(clazz, mapper, sb.toString());
	}

	@Override
	public void createTable(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper) {
		
		String domainName = assembleDomainName(clazz, mapper);
		
		CreateDomainRequest request = new CreateDomainRequest(domainName);
		
		log.info("Create SimpleDB Domain: " + domainName);
		
		getClient().createDomain(request);
		
	}

	@Override
	public boolean tableExists(Class<? extends BaseDomainObject> clazz,
			NoSqlObjectMapper mapper) {
		String domainName = assembleDomainName(clazz, mapper);
		
		return domainNames.contains(domainName);
		
	}

	@Override
	public void delete(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, NoSqlKey key) {
		
		DeleteAttributesRequest deleteRequest = new DeleteAttributesRequest(assembleDomainName(clazz, mapper), key.composeUniqueString());
		getClient().deleteAttributes(deleteRequest);
				
	}

	@Override
	public void delete(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, Collection<NoSqlKey> keys) {
		BatchDeleteAttributesRequest deleteRequest = new BatchDeleteAttributesRequest();
		deleteRequest.withDomainName(assembleDomainName(clazz, mapper));
		for (NoSqlKey key : keys) {
			deleteRequest.withItems(new DeletableItem().withName(key.composeUniqueString()));
		}
		getClient().batchDeleteAttributes(deleteRequest);
		
	}

	@Override
	public String save(BaseDomainObject domain, NoSqlObjectMapper mapper) {
		
		String domainName = assembleDomainName(domain.getClass(), mapper);
		
		Map<String, Object> map = mapper.toAttributes(domain);
		
		String hashKeyAttribute = mapper.getHashKeyAttribute(domain.getClass());
		String hashKeyProperty = mapper.getHashKeyProperty(domain.getClass());
		String rangeKeyAttribute = mapper.getRangeKeyAttribute(domain.getClass());
		String rangeKeyProperty = mapper.getRangeKeyProperty(domain.getClass());
		
		
		try {
		
			if (map.get(hashKeyAttribute) == null) {
				String id = nextId(domain);
				map.put(hashKeyAttribute, id);
				PropertyUtils.setProperty(domain, hashKeyProperty, id);
			}
			if ( (rangeKeyAttribute != null) && (map.get(rangeKeyAttribute) == null) ) {
				if (String.class.isAssignableFrom(mapper.getRangeKeyType(domain.getClass()))) {
					String id = nextId(domain);
					map.put(rangeKeyAttribute, id);
					PropertyUtils.setProperty(domain, rangeKeyProperty, id);
				}
				else {
					throw new IllegalArgumentException("Range key " + rangeKeyAttribute + " must be explicitly set.");
				}
				
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		NoSqlKey key = mapper.getKey(domain);
		
		log.info("Saving " + domain.getClass().getName() + ":" + domain.getObjectIdentifier());
		
		PutAttributesRequest request = new PutAttributesRequest();
		request.setItemName(key.composeUniqueString());
		request.setDomainName(domainName);
		request.setAttributes(prepareWriteAttributes(map));

		getClient().putAttributes(request);
				
		return mapper.getKey(domain).composeUniqueString();
	}
	
	
	protected Collection<ReplaceableAttribute> prepareWriteAttributes(Map<String, Object> rawAttributes) {
		
		Collection<ReplaceableAttribute> results = new ArrayList<ReplaceableAttribute>();
		for (Entry<String, Object> entry : rawAttributes.entrySet()) {
			if (entry.getValue() != null) {
				if (entry.getValue() instanceof Number) {
					results.add(new ReplaceableAttribute(entry.getKey(), entry.getValue().toString(), true));
							
				}
				else if (entry.getValue() instanceof Collection) {
					results.add(new ReplaceableAttribute(entry.getKey(), StringUtils.join((Collection)entry.getValue(), ","), true));
				}
				else {
					results.add(new ReplaceableAttribute(entry.getKey(), entry.getValue().toString(), true));
				}
			}
		}
		
		return results;
		
	}
	

	@Override
	public Collection<String> save(Collection<BaseDomainObject> domains, NoSqlObjectMapper mapper) {
		
		
		Collection<String> ids = new ArrayList<String>();
		
		try {
			Class clazz = domains.iterator().next().getClass();
			
			String hashKeyAttribute = mapper.getHashKeyAttribute(clazz);
			String hashKeyProperty = mapper.getHashKeyProperty(clazz);
			String rangeKeyAttribute = mapper.getRangeKeyAttribute(clazz);
			String rangeKeyProperty = mapper.getRangeKeyProperty(clazz);
			
			BatchPutAttributesRequest request = new BatchPutAttributesRequest();
			request.withDomainName(assembleDomainName(clazz, mapper));
			
			int batchSize = 0;
			
			for (BaseDomainObject domain : domains) {
				batchSize++;
				Map<String, Object> map = mapper.toAttributes(domain);
				
				if (map.get(hashKeyAttribute) == null) {
					if (rangeKeyProperty != null) {
						throw new IllegalStateException("Cannot automatically generate a hash key for objects that have a range key.");
					}
					String id = nextId(domain);
					map.put(hashKeyAttribute, id);
					PropertyUtils.setProperty(domain, hashKeyProperty, id);
				}
				if ( (rangeKeyAttribute != null) && (map.get(rangeKeyAttribute) == null) ) {
					if (String.class.isAssignableFrom(mapper.getRangeKeyType(domain.getClass()))) {
						String id = nextId(domain);
						map.put(rangeKeyAttribute, id);
						PropertyUtils.setProperty(domain, rangeKeyProperty, id);
					}
					else {
						throw new IllegalArgumentException("Range key " + rangeKeyAttribute + " must be explicitly set.");
					}
					
				}
				
				ids.add(mapper.getKey(domain).composeUniqueString());
				ReplaceableItem item = new ReplaceableItem(mapper.getKey(domain).composeUniqueString());
				item.withAttributes(prepareWriteAttributes(map));
				request.withItems(item);
				
				if (batchSize >= maxBatchSize) {
					log.info("Saving Batch: " + batchSize);
					batchSize = 0;
					getClient().batchPutAttributes(request);
					request = new BatchPutAttributesRequest();
					request.withDomainName(assembleDomainName(clazz, mapper));
				}
				
			}
			
			if (batchSize > 0) {
				log.info("Saving Batch: " + batchSize);
				getClient().batchPutAttributes(request);
			}
		
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return ids;
	}

	@Override
	public Collection<String> findAllIds(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper) {
		
		SelectRequest request = new SelectRequest();
		
		request.withSelectExpression("select itemName() from `" + assembleDomainName(clazz, mapper) + "`");
		request.withConsistentRead(consistentReads);
		
		SelectResult result = getClient().select(request);
		
		Collection<String> ids = new ArrayList<String>();
		if (result.getItems() != null) {
			for (Item item : result.getItems()) {
				ids.add(item.getName());
			}
		}
		
		return ids;
	}

	@Override
	public Collection<BaseDomainObject> findAll(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper) {
		
		return findByQuery(clazz, mapper, "select * from `" + assembleDomainName(clazz, mapper) + "`");

	}

	
	protected Collection<BaseDomainObject> findByQuery(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String query) {
		
		SelectRequest request = new SelectRequest();
		
		request.withSelectExpression(query);
		request.withConsistentRead(consistentReads);
	
		

		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		
		SelectResult result = getClient().select(request);
		
		if (result.getItems() != null) {
			for (Item item : result.getItems()) {
				results.add(instantiate(clazz, mapper, item.getAttributes()));
			}
		}
		
		while (result.getNextToken() != null) {
			request.setNextToken(result.getNextToken());
			result = getClient().select(request);
			if (result.getItems() != null) {
				for (Item item : result.getItems()) {
					results.add(instantiate(clazz, mapper, item.getAttributes()));
				}
			}
		}
			
				
		return results;
		
		
	}
	
	public String getDomainNamespace() {
		return domainNamespace;
	}

	public void setDomainNamespace(String domainNamespace) {
		this.domainNamespace = domainNamespace;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public Object toAttributeValue(Object value, Class domainType) {
		
		if (value == null) {
			return null;
		}
		
		if (Date.class.isAssignableFrom(domainType)) {
			return formatTimeStamp((Date)value);
		}
		else if (Boolean.class.isAssignableFrom(domainType)) {
			return ((Boolean)value).booleanValue()?"Y":"N";
		}

		return value;
	}

	@Override
	public Object toDomainValue(Object value, Class domainType) {
		
		if (value == null) {
			return null;
		}
		
		String valueString = (String)value;
		
		if (Date.class.isAssignableFrom(domainType)) {
			Date result = parseTimeStamp(valueString);
			if (result == null) {
				result = new Date(Long.parseLong(valueString));
			}
			return result;
			
			
		}
		else if (Number.class.isAssignableFrom(domainType)) {
			try {
				Number number = (Number)domainType.getConstructor(String.class).newInstance(valueString);
				return number;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		else if (Collection.class.isAssignableFrom(domainType)) {
			List<String> result = Arrays.asList(StringUtils.split(valueString, ","));
			if (Set.class.isAssignableFrom(domainType)) {
				return new LinkedHashSet<String>(result);
			}
			else if (List.class.isAssignableFrom(domainType)) {
				return result;
			}
		}
		else if (Boolean.class.isAssignableFrom(domainType)) {
			return valueString.equals("Y") || valueString.equals("true");
		}
		else if (domainType.isEnum()) {
			return Enum.valueOf(domainType, valueString.toUpperCase());
		}
		else {
			return valueString;
		}
		
		
		return null;
	}

	public boolean isConsistentReads() {
		return consistentReads;
	}

	public void setConsistentReads(boolean consistentReads) {
		this.consistentReads = consistentReads;
	}

	@Override
	public Date parseTimeStamp(String value) {
		if (value == null) {
			return null;
		}
		
		try {
			return ISO_TS_FORMAT.parse(value);
		}
		catch (ParseException ex) {
			return null;
		}
		catch (NumberFormatException ex) {
			return null;
		}

	}

	@Override
	public String formatTimeStamp(Date date) {
		if (date != null) {
			return ISO_TS_FORMAT.format(date);
		}
		else {
			return null;
		}
	}

	
	
	
	

}
