package com.frs.alto.nosql.ds.dynamodb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.BatchGetItemRequest;
import com.amazonaws.services.dynamodb.model.BatchGetItemResult;
import com.amazonaws.services.dynamodb.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.dynamodb.model.CreateTableRequest;
import com.amazonaws.services.dynamodb.model.CreateTableResult;
import com.amazonaws.services.dynamodb.model.DeleteItemRequest;
import com.amazonaws.services.dynamodb.model.DeleteRequest;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.GetItemRequest;
import com.amazonaws.services.dynamodb.model.GetItemResult;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.KeySchema;
import com.amazonaws.services.dynamodb.model.KeySchemaElement;
import com.amazonaws.services.dynamodb.model.KeysAndAttributes;
import com.amazonaws.services.dynamodb.model.ListTablesResult;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutRequest;
import com.amazonaws.services.dynamodb.model.QueryRequest;
import com.amazonaws.services.dynamodb.model.QueryResult;
import com.amazonaws.services.dynamodb.model.ScalarAttributeType;
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;
import com.amazonaws.services.dynamodb.model.TableDescription;
import com.amazonaws.services.dynamodb.model.WriteRequest;
import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.nosql.ds.BaseNoSqlDataSource;
import com.frs.alto.nosql.mapper.NoSqlKey;
import com.frs.alto.nosql.mapper.NoSqlObjectMapper;
import com.frs.alto.nosql.mapper.TypeTransformer;

public class DynamoDBDatasource extends BaseNoSqlDataSource implements InitializingBean, TypeTransformer {
	
	
	private static Log log = LogFactory.getLog(DynamoDBDatasource.class);
	
	private String tableSpace = null;
	private String apiId = null;
	private String secretKey = null;
	private long readThroughput = 2l;
	private long writeThroughput = 1l;
	private String endpoint = null;
	private int maxBatchSize = 25;
	
	private AWSCredentials credentials = null;
	private AmazonDynamoDBClient client = null;
	private Collection<String> tableNames = null;
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		refreshTableNames();
		
	}
	


	public AWSCredentials getCredentials() {
		if (credentials == null) {
			credentials = new BasicAWSCredentials(apiId, secretKey);
		}
		return credentials;
	}
	
	public AmazonDynamoDBClient getClient() {
		if (client == null) {
			client = new AmazonDynamoDBClient(getCredentials());
			if (endpoint != null) {
				client.setEndpoint(endpoint);
				
			}
		}
		return client;
	}
	
	public void refreshTableNames() {
		
		log.info("Loading DynamoDB Tables...");
		
		String tableSpacePrefix = getTableSpace();

		ListTablesResult result = getClient().listTables();
		
		tableNames = new ArrayList<String>();
		for (String tableName : result.getTableNames()) {
			if (tableName.startsWith(tableSpacePrefix)) {
				log.info("Table: " + tableName);
				tableNames.add(tableName);
			}
		}
		
		
	}
	
	
	@Override
	public BaseDomainObject findByKey(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, NoSqlKey key) {
		
		String tableName = assembleTableName(clazz, mapper);
		
		GetItemRequest request = new GetItemRequest().withTableName(tableName);
		
		String rangeKeyAttribute = mapper.getRangeKeyAttribute(clazz);

		Key dynamoKey = new Key();
		dynamoKey.withHashKeyElement(new AttributeValue(key.getHashKey()));
		if (rangeKeyAttribute != null) {
			dynamoKey.withRangeKeyElement(new AttributeValue(key.getRangeKey().toString()));
		}
		
		request.setKey(dynamoKey);
		
		GetItemResult result = getClient().getItem(request);
		if (result.getItem() != null) {
			Map<String, AttributeValue> map = result.getItem();
			return instantiate(clazz, mapper, map);
		}
		else {
			return null;
		}
	}
	@Override
	public Collection<BaseDomainObject> findByHashKeys(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, Collection<NoSqlKey> hashKeys) {
		
		
		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		
		Collection<NoSqlKey> unprocessed = new ArrayList<NoSqlKey>(hashKeys);
		Iterator<NoSqlKey> itr = unprocessed.iterator();
		BatchGetItemRequest request = null;
		KeysAndAttributes itemRequest = null;
		Map<String, KeysAndAttributes> requestMap;
		int currentBatch = 0;
		

		String rangeKeyAttribute = mapper.getRangeKeyAttribute(clazz);
		
		NoSqlKey key = null;
		
		Collection<String> ids = new ArrayList<String>();
		
		String tableName = assembleTableName(clazz, mapper);
		
		while (unprocessed.size() > 0) {
			requestMap = new HashMap<String, KeysAndAttributes>();
			currentBatch = 0;
			while (itr.hasNext() && (currentBatch < maxBatchSize)) {
				key = itr.next();
				itr.remove();
				
				itemRequest = requestMap.get(tableName);
				if (itemRequest == null) {
					itemRequest = new KeysAndAttributes();
					requestMap.put(tableName, itemRequest);
				}
						
				
				
				Key dynamoKey = new Key();
				dynamoKey.withHashKeyElement(new AttributeValue(key.getHashKey()));
				if (rangeKeyAttribute != null) {
					dynamoKey.withRangeKeyElement(new AttributeValue(key.getRangeKey().toString()));
				}
				
				itemRequest = new KeysAndAttributes();
				itemRequest.withKeys(dynamoKey);
				
				
				currentBatch++;
			}
			request = new BatchGetItemRequest();
			
			request.setRequestItems(requestMap);
			log.info("Batching " + currentBatch + " write operations.");
			BatchGetItemResult result = getClient().batchGetItem(request);
			
			for (Map<String, AttributeValue> map : result.getResponses().get(tableName).getItems()) {
				results.add(instantiate(clazz, mapper, map));
			}
		}
		return results;
	}
	@Override
	public Collection<BaseDomainObject> findWholeRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey) {
		
		String tableName = assembleTableName(clazz, mapper);
		
		QueryRequest request = new QueryRequest(tableName, new AttributeValue(hashKey));
		
		QueryResult result = getClient().query(request);
		
		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		
		List<Map<String, AttributeValue>> items = result.getItems();
		
		
		if (items != null) {
			for (Map<String, AttributeValue> map : items) {
				results.add(instantiate(clazz, mapper, map));
			}
		}
		
		return results;
	}
	@Override
	public Collection<BaseDomainObject> findByRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, String startRange, String endRange) {

		
		String tableName = assembleTableName(clazz, mapper);
		
		QueryRequest request = new QueryRequest(tableName, new AttributeValue(hashKey));
		
		Condition condition = new Condition();
		condition.withComparisonOperator(ComparisonOperator.BETWEEN);
		condition.withAttributeValueList(new AttributeValue(startRange), new AttributeValue(endRange));
		
		request.setRangeKeyCondition(condition);
		
		QueryResult result = getClient().query(request);
		
		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		
		List<Map<String, AttributeValue>> items = result.getItems();
		
		
		if (items != null) {
			for (Map<String, AttributeValue> map : items) {
				results.add(instantiate(clazz, mapper, map));
			}
		}
		
		return results;
		
		
	}
	@Override
	public Collection<BaseDomainObject> findByUpperRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, String startRange) {

		String tableName = assembleTableName(clazz, mapper);
		
		QueryRequest request = new QueryRequest(tableName, new AttributeValue(hashKey));
    
	    Condition rangeKeyCondition = new Condition()
	        .withComparisonOperator(ComparisonOperator.GE.toString())
	        .withAttributeValueList(new AttributeValue().withS(startRange));			    
		
        request.withRangeKeyCondition(rangeKeyCondition);
		
		QueryResult result = getClient().query(request);
		
		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		
		List<Map<String, AttributeValue>> items = result.getItems();
		
		
		if (items != null) {
			for (Map<String, AttributeValue> map : items) {
				results.add(instantiate(clazz, mapper, map));
			}
		}
		
		return results;
	}
	@Override
	public Collection<BaseDomainObject> findByLowerRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, String endRange) {
		String tableName = assembleTableName(clazz, mapper);
		
		QueryRequest request = new QueryRequest(tableName, new AttributeValue(hashKey));
		
		Condition condition = new Condition();
		condition.withComparisonOperator(ComparisonOperator.LE);
		condition.withAttributeValueList(new AttributeValue(endRange));
		
		request.setRangeKeyCondition(condition);
		
		QueryResult result = getClient().query(request);
		
		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		
		List<Map<String, AttributeValue>> items = result.getItems();
		
		
		if (items != null) {
			for (Map<String, AttributeValue> map : items) {
				results.add(instantiate(clazz, mapper, map));
			}
		}
		
		return results;
	}
	@Override
	public Collection<BaseDomainObject> findByRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number startRange, Number endRange) {

		String tableName = assembleTableName(clazz, mapper);
		
		QueryRequest request = new QueryRequest(tableName, new AttributeValue(hashKey));
		
		Condition condition = new Condition();
		condition.withComparisonOperator(ComparisonOperator.BETWEEN);
		condition.withAttributeValueList(new AttributeValue().withN(startRange.toString()), new AttributeValue().withN(endRange.toString()));
		
		request.setRangeKeyCondition(condition);
		
		QueryResult result = getClient().query(request);
		
		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		
		List<Map<String, AttributeValue>> items = result.getItems();
		
		
		if (items != null) {
			for (Map<String, AttributeValue> map : items) {
				results.add(instantiate(clazz, mapper, map));
			}
		}
		
		return results;
	}
	@Override
	public Collection<BaseDomainObject> findByUpperRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number startRange) {
		String tableName = assembleTableName(clazz, mapper);
		
		QueryRequest request = new QueryRequest(tableName, new AttributeValue(hashKey));
		
		Condition condition = new Condition();
		condition.withComparisonOperator(ComparisonOperator.GE);
		condition.withAttributeValueList(new AttributeValue().withN(startRange.toString()));
		
		request.setRangeKeyCondition(condition);
		
		QueryResult result = getClient().query(request);
		
		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		
		List<Map<String, AttributeValue>> items = result.getItems();
		
		
		if (items != null) {
			for (Map<String, AttributeValue> map : items) {
				results.add(instantiate(clazz, mapper, map));
			}
		}
		
		return results;
	}
	@Override
	public Collection<BaseDomainObject> findByLowerRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number endRange) {
		String tableName = assembleTableName(clazz, mapper);
		
		QueryRequest request = new QueryRequest(tableName, new AttributeValue(hashKey));
		
		Condition condition = new Condition();
		condition.withComparisonOperator(ComparisonOperator.LE);
		condition.withAttributeValueList(new AttributeValue().withN(endRange.toString()));
		
		request.setRangeKeyCondition(condition);
		
		QueryResult result = getClient().query(request);
		
		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		
		List<Map<String, AttributeValue>> items = result.getItems();
		
		
		if (items != null) {
			for (Map<String, AttributeValue> map : items) {
				results.add(instantiate(clazz, mapper, map));
			}
		}
		
		return results;
	}
	
	protected String assembleTableName(Class clazz, NoSqlObjectMapper mapper) {
		String tableName = mapper.getTableName(clazz);
		if (tableName != null) {
			return getTableSpace() + "-" + mapper.getTableName(clazz);
		}
		else {
			return null;
		}
	}
	
	protected ScalarAttributeType fromClass(Class clazz) {
				
		if (Number.class.isAssignableFrom(clazz)) {
			return ScalarAttributeType.N;
		}
		else {
			return ScalarAttributeType.S;
		}
	}
	
	@Override
	public void createTable(Class<? extends BaseDomainObject> clazz,
			NoSqlObjectMapper mapper) {
		
		String tableName = assembleTableName(clazz, mapper);
		
		if ( (tableName != null) && !tableNames.contains(tableName)) {
			log.info("Creating table: " + tableName);
			
			
			KeySchemaElement hashKey = new KeySchemaElement().withAttributeName(mapper.getHashKeyAttribute(clazz)).withAttributeType(ScalarAttributeType.S);
			KeySchema ks = new KeySchema().withHashKeyElement(hashKey);
			
			if (mapper.getRangeKeyAttribute(clazz) != null) {
				KeySchemaElement rangeKey = new KeySchemaElement().withAttributeName(mapper.getRangeKeyAttribute(clazz)).withAttributeType(fromClass(mapper.getRangeKeyType(clazz)));
				ks = ks.withRangeKeyElement(rangeKey);
			}
			
			ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
			  .withReadCapacityUnits(getReadThroughput())
			  .withWriteCapacityUnits(getWriteThroughput());

			CreateTableRequest request = new CreateTableRequest()
			  .withTableName(tableName)
			  .withKeySchema(ks)
			  .withProvisionedThroughput(provisionedThroughput);

			CreateTableResult result = getClient().createTable(request);
			
			TableDescription desc = result.getTableDescription();
			
			while (!desc.getTableStatus().equals("ACTIVE")) {
				log.info("Table status: " + desc.getTableStatus());
				try {
					Thread.sleep(2000l);
				}
				catch (Exception e) {}
				desc = getClient().describeTable(new DescribeTableRequest().withTableName(tableName)).getTable();
			}
			log.info("Table status: " + desc.getTableStatus());		
			
			
		}
		
		
	}
	@Override
	public boolean tableExists(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper) {
		String tableName = assembleTableName(clazz, mapper);
		return tableNames.contains(tableName);
		
	}
	@Override
	public void delete(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, NoSqlKey key) {
		
		String tableName = assembleTableName(clazz, mapper);
		
		DeleteItemRequest deleteItemRequest = new DeleteItemRequest().withTableName(tableName);
		
		String rangeKeyAttribute = mapper.getRangeKeyAttribute(clazz);
		
		Key dynamoKey = new Key();
		dynamoKey.withHashKeyElement(new AttributeValue(key.getHashKey()));
		if (rangeKeyAttribute != null) {
			dynamoKey.withRangeKeyElement(new AttributeValue(key.getRangeKey().toString()));
		}
			
		deleteItemRequest.setKey(dynamoKey);
				
		getClient().deleteItem(deleteItemRequest);
		
	}
	@Override
	public void delete(Class<? extends BaseDomainObject> clazz,
			NoSqlObjectMapper mapper, Collection<NoSqlKey> keys) {
		
		Collection<NoSqlKey> unprocessed = new ArrayList<NoSqlKey>(keys);
		Iterator<NoSqlKey> itr = unprocessed.iterator();
		BatchWriteItemRequest request = null;
		WriteRequest itemRequest = null;
		List<WriteRequest> tableWrites = null;
		Map<String, List<WriteRequest>> requestMap;
		int currentBatch = 0;
		

		String rangeKeyAttribute = mapper.getRangeKeyAttribute(clazz);
		
		NoSqlKey key = null;
		
		Collection<String> ids = new ArrayList<String>();
		
		while (unprocessed.size() > 0) {
			requestMap = new HashMap<String, List<WriteRequest>>();
			currentBatch = 0;
			while (itr.hasNext() && (currentBatch < maxBatchSize)) {
				key = itr.next();
				itr.remove();
				String tableName = assembleTableName(clazz, mapper);
				tableWrites = requestMap.get(tableName);
				if (tableWrites == null) {
					tableWrites = new ArrayList<WriteRequest>();
					requestMap.put(tableName, tableWrites);
				}
						
				
				
				Key dynamoKey = new Key();
				dynamoKey.withHashKeyElement(new AttributeValue(key.getHashKey()));
				if (rangeKeyAttribute != null) {
					dynamoKey.withRangeKeyElement(new AttributeValue(key.getRangeKey().toString()));
				}
				
				itemRequest = new WriteRequest();
				itemRequest.setDeleteRequest(new DeleteRequest().withKey(dynamoKey));
				tableWrites.add(itemRequest);
				
				currentBatch++;
			}
			request = new BatchWriteItemRequest();
			request.setRequestItems(requestMap);
			log.info("Batching " + currentBatch + " write operations.");
			getClient().batchWriteItem(request);
			
		}
					
		
	}
	@Override
	public String save(BaseDomainObject domain, NoSqlObjectMapper mapper) {
		
		String tableName = assembleTableName(domain.getClass(), mapper);
		
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
		
		log.info("Saving " + domain.getClass().getName() + ":" + domain.getObjectIdentifier());
		
		PutItemRequest request = new PutItemRequest();
		request.setTableName(tableName);
		Map<String, AttributeValue> attribs = prepareWriteAttributes(map);
		request.setItem(attribs);
		getClient().putItem(request);
				
		return mapper.getKey(domain).composeUniqueString();
	}
	
	protected Map<String, AttributeValue> prepareWriteAttributes(Map<String, Object> rawAttributes) {
		
		Map<String, AttributeValue> results = new HashMap<String, AttributeValue>();
		for (Entry<String, Object> entry : rawAttributes.entrySet()) {
			if (entry.getValue() != null) {
				if (entry.getValue() instanceof Number) {
					results.put(entry.getKey(), new AttributeValue().withN(entry.getValue().toString()));
				}
				else if (entry.getValue() instanceof Collection) {
					results.put(entry.getKey(), new AttributeValue().withSS((Collection<String>)entry.getValue()));
				}
				else if (StringUtils.isNotBlank(entry.getValue().toString())) {
					results.put(entry.getKey(), new AttributeValue(entry.getValue().toString()));
				}
			}
		}
		
		return results;
		
	}
	
	@Override
	public Collection<String> save(Collection<BaseDomainObject> domains, NoSqlObjectMapper mapper) {

		Collection<BaseDomainObject> unprocessed = new ArrayList<BaseDomainObject>(domains);
		Iterator<BaseDomainObject> itr = unprocessed.iterator();
		BaseDomainObject domain = null;
		BatchWriteItemRequest request = null;
		WriteRequest itemRequest = null;
		List<WriteRequest> tableWrites = null;
		Map<String, List<WriteRequest>> requestMap;
		int currentBatch = 0;
		
		Collection<String> ids = new ArrayList<String>();
		
		while (unprocessed.size() > 0) {
			requestMap = new HashMap<String, List<WriteRequest>>();
			currentBatch = 0;
			while (itr.hasNext() && (currentBatch < maxBatchSize)) {
				domain = itr.next();
				itr.remove();
				String tableName = assembleTableName(domain.getClass(), mapper);
				tableWrites = requestMap.get(tableName);
				if (tableWrites == null) {
					tableWrites = new ArrayList<WriteRequest>();
					requestMap.put(tableName, tableWrites);
				}
				
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
				
				ids.add(mapper.getKey(domain).composeUniqueString());
				
				
				
				itemRequest = new WriteRequest();
				Map<String, AttributeValue> attribs = prepareWriteAttributes(map);
				itemRequest.setPutRequest(new PutRequest().withItem(attribs));
				tableWrites.add(itemRequest);
				
				currentBatch++;
			}
			request = new BatchWriteItemRequest();
			request.setRequestItems(requestMap);
			log.info("Batching " + currentBatch + " write operations.");
			getClient().batchWriteItem(request);
			
		}
		
		
		
		return ids;
	}
	@Override
	public Collection<String> findAllIds(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper) {
		
		String tableName = assembleTableName(clazz, mapper);
		
		String rangeKeyAttribute = mapper.getRangeKeyAttribute(clazz);
		Class rangeKeyType = mapper.getRangeKeyType(clazz);
		
		Collection<String> results = new ArrayList<String>();
		
		Key lastKeyEvaluated = null;
		do {
		    ScanRequest scanRequest = new ScanRequest()
		        .withTableName(tableName)
		        .withAttributesToGet(mapper.getHashKeyAttribute(clazz))
		        .withLimit(100)
		        .withExclusiveStartKey(lastKeyEvaluated);
		    
		    if (rangeKeyAttribute != null) {
		    	scanRequest.withAttributesToGet(rangeKeyAttribute);
		    }

		    ScanResult result = getClient().scan(scanRequest);
		    for (Map<String, AttributeValue> item : result.getItems()){
		    	if (rangeKeyAttribute != null) {
		    		if (Number.class.isAssignableFrom(rangeKeyType)) {
		    			results.add(item.get(mapper.getHashKeyAttribute(clazz)).getS() + "#" + item.get(mapper.getRangeKeyAttribute(clazz)).getN().toString());
		    		}
		    		else {
		    			results.add(item.get(mapper.getHashKeyAttribute(clazz)).getS() + "#" + item.get(mapper.getRangeKeyAttribute(clazz)).getS());
		    		}
		    	}
		    	else {
		    		results.add(item.get(mapper.getHashKeyAttribute(clazz)).getS());
		    	}
		    }
		    lastKeyEvaluated = result.getLastEvaluatedKey();
		} while (lastKeyEvaluated != null);
		
		return results;
	}
	@Override
	public Collection<BaseDomainObject> findAll(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper) {
		String tableName = assembleTableName(clazz, mapper);
		
		Collection<BaseDomainObject> results = new ArrayList<BaseDomainObject>();
		
		Key lastKeyEvaluated = null;
		do {
		    ScanRequest scanRequest = new ScanRequest()
		        .withTableName(tableName)
		        .withLimit(100)
		        .withExclusiveStartKey(lastKeyEvaluated);

		    ScanResult result = getClient().scan(scanRequest);
		    for (Map<String, AttributeValue> item : result.getItems()){
		        results.add(instantiate(clazz, mapper, item));
		    }
		    lastKeyEvaluated = result.getLastEvaluatedKey();
		} while (lastKeyEvaluated != null);
		
		return results;
	}
	
	protected BaseDomainObject instantiate(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, Map<String, AttributeValue> map) {
		
		BaseDomainObject domain = mapper.instantiate(clazz);
		
		mapper.fromAttributes(domain, (Map)map);
		
		return domain;
		
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
		
		AttributeValue attr = (AttributeValue)value;
		
		if (attr == null) {
			return null; 
		}
		
		if (Date.class.isAssignableFrom(domainType)) {
			Date result = parseTimeStamp(attr.getS());
			if (result == null) {
				result = new Date(Long.parseLong(attr.getN()));
			}
			return result;
		}
		else if (Number.class.isAssignableFrom(domainType)) {
			try {
				Number number = (Number)NumberUtils.createNumber(attr.getN());
				return number;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		else if (Collection.class.isAssignableFrom(domainType)) {
			List<String> result = attr.getSS();
			if (Set.class.isAssignableFrom(domainType)) {
				return new LinkedHashSet<String>(result);
			}
			else if (List.class.isAssignableFrom(domainType)) {
				return result;
			}
		}
		else if (Boolean.class.isAssignableFrom(domainType)) {
			return attr.getS().equals("Y");
		}
		else if (domainType.isEnum()) {
			return Enum.valueOf(domainType, attr.getS());
		}
		else {
			return attr.getS();
		}
		
		
		return null;
	}

	public String getTableSpace() {
		return tableSpace;
	}
	public void setTableSpace(String tableSpace) {
		this.tableSpace = tableSpace;
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
	public long getReadThroughput() {
		return readThroughput;
	}
	public void setReadThroughput(long readThroughput) {
		this.readThroughput = readThroughput;
	}
	public long getWriteThroughput() {
		return writeThroughput;
	}
	public void setWriteThroughput(long writeThroughput) {
		this.writeThroughput = writeThroughput;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(int maxBatchSize) {
		this.maxBatchSize = maxBatchSize;
	}

	

	
	
	
}
