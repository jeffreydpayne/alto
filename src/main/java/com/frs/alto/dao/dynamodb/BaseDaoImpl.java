package com.frs.alto.dao.dynamodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.CreateTableRequest;
import com.amazonaws.services.dynamodb.model.CreateTableResult;
import com.amazonaws.services.dynamodb.model.DeleteItemRequest;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.GetItemRequest;
import com.amazonaws.services.dynamodb.model.GetItemResult;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.KeySchema;
import com.amazonaws.services.dynamodb.model.KeySchemaElement;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.QueryRequest;
import com.amazonaws.services.dynamodb.model.QueryResult;
import com.amazonaws.services.dynamodb.model.ScalarAttributeType;
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;
import com.amazonaws.services.dynamodb.model.TableDescription;
import com.frs.alto.dao.BaseDao;
import com.frs.alto.domain.BaseDomainObject;

public abstract class BaseDaoImpl<T extends BaseDomainObject> implements BaseDao<T>, InitializingBean {
	
	private Log log = LogFactory.getLog(BaseDaoImpl.class);
	
	private DynamoDBDatasource dataSource = null;
	
	public abstract String getTableName();
	public abstract Class<T> getDomainClass();
	
	private Boolean useCache = null; //null will use datasource default
	
	private String qualifiedTableName = null;
	
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initializeTable();
	}
	
	protected AmazonDynamoDBClient getClient() {
		return getDataSource().getClient();
	}

	protected String getHashKeyName() {
		return "id";
	}
	protected String getRangeKeyName() {
		return null;
	}
	protected Class getRangeKeyType() {
		return null;
	}
	

	protected String getPrimaryKey(T domain) {
		return domain.getObjectIdentifier();
	}
	
	protected String fromDate(Date dt) {
		if (dt != null) {
			return String.valueOf(dt.getTime());
		}
		else {
			return null;
		}
	}
	
	protected Date toDate(String dtString) {
		if (dtString != null) {
			return new Date(Long.parseLong(dtString));
		}
		else {
			return null;
		}
	}
	
	protected String fromBoolean(Boolean bln) {
		if (bln != null) {
			return bln ? "Y" : "N";
		}
		else {
			return null;
		}
	}
	
	protected Boolean toBoolean(String value) {
		if (value != null) {
			return value.equals("Y");
		}
		else {
			return null;
		}
	}
	
	protected String populateString(String columnDef, Map<String, AttributeValue> map) {
		
		AttributeValue value = map.get(columnDef);
		
		if (value != null) {
			return value.getS();
		}
		else {
			return null;
		}
		
	}
	
	protected List<String> populateList(String columnDef, Map<String, AttributeValue> map) {
		
		AttributeValue value = map.get(columnDef);
		
		if (value != null) {
			return value.getSS();
		}
		else {
			return null;
		}
		
	}
	
	protected Date populateDate(String columnDef, Map<String, AttributeValue> map) {
		
		AttributeValue value = map.get(columnDef);
		
		if (value != null) {
			return new Date(Long.parseLong(value.getN()));
		}
		else {
			return null;
		}
		
	}
	
	protected Boolean populateBoolean(String columnDef, Map<String, AttributeValue> map) {
		
		AttributeValue value = map.get(columnDef);
		
		if (value != null) {
			return toBoolean(value.getS());
		}
		else {
			return null;
		}
		
	}
	
	protected void populateAttribute(Map<String, AttributeValue> attributes, String columnDef, String value) {
		
		if (value != null) {
			attributes.put(columnDef, new AttributeValue(value));
		}
		
	}
	
	protected void populateAttribute(Map<String, AttributeValue> attributes, String columnDef, List<String> value) {
		
		if (value != null) {
			attributes.put(columnDef, new AttributeValue(value));
		}
		
	}
	
	protected void populateAttribute(Map<String, AttributeValue> attributes, String columnDef, Date value) {
		
		if (value != null) {
			attributes.put(columnDef, new AttributeValue().withN(fromDate(value)));
		}
		
	}
	
	protected void populateAttribute(Map<String, AttributeValue> attributes, String columnDef, Boolean value) {
		
		if (value != null) {
			attributes.put(columnDef, new AttributeValue(fromBoolean(value)));
		}
		
	}
	
	protected ScalarAttributeType fromClass(Class clazz) {
		
		
		return ScalarAttributeType.S;
		
	}
	
	protected void initializeTable() {
		
		if (!getDataSource().getTableNames().contains(assembleTableName())) {
			log.info("Creating table: " + assembleTableName());
			
			
			KeySchemaElement hashKey = new KeySchemaElement().withAttributeName(getHashKeyName()).withAttributeType(ScalarAttributeType.S);
			KeySchema ks = new KeySchema().withHashKeyElement(hashKey);
			
			if (getRangeKeyName() != null) {
				KeySchemaElement rangeKey = new KeySchemaElement().withAttributeName(getRangeKeyName()).withAttributeType(fromClass(getRangeKeyType()));
				ks = ks.withRangeKeyElement(rangeKey);
			}
			
			ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
			  .withReadCapacityUnits(getDataSource().getReadThroughput())
			  .withWriteCapacityUnits(getDataSource().getWriteThroughput());

			CreateTableRequest request = new CreateTableRequest()
			  .withTableName(assembleTableName())
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
				desc = getClient().describeTable(new DescribeTableRequest().withTableName(assembleTableName())).getTable();
			}
			log.info("Table status: " + desc.getTableStatus());
			
			Collection<T> factory = factory();
			if (factory != null) {
				for (T domain : factory) {
					save(domain);
				}
			}
			
			
			
		}
		
		
	}
	
	protected Collection<T> factory() {
		return null;
	}


	protected String assembleTableName() {
		if (qualifiedTableName == null) {
			qualifiedTableName = getDataSource().getTableSpace() + "-" + getTableName();
		}
		return qualifiedTableName;
	}
	
	protected abstract void prepareWriteAttributes(T domain, Map<String, AttributeValue> map);
	protected String getPrimaryKeyValue(T domain) {
		return domain.getObjectIdentifier();
	}
	

	@Override
	public String save(T anObject) {
		
		if (anObject.getObjectIdentifier() == null) {
			anObject.setObjectIdentifier(getDataSource().generateIdentifier(anObject));
		}
		
		log.info("Saving " + anObject.getClass().getName() + ":" + anObject.getObjectIdentifier());
		
		PutItemRequest request = new PutItemRequest();
		request.setTableName(assembleTableName());
		Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
		prepareWriteAttributes(anObject, map);
		request.setItem(map);
		getClient().putItem(request);
				
		return getPrimaryKeyValue(anObject);
		
	}
	
	protected abstract T instantiateClass();
	
	protected abstract void populateDomain(T domain, Map<String, AttributeValue> map);
	
	protected T instantiateObject(Map<String, AttributeValue> map) {
		
		T domain = instantiateClass();
		populateDomain(domain, map);
		return domain;
		
	}

	@Override
	public void delete(T anObject) {
		
		DeleteItemRequest deleteItemRequest = new DeleteItemRequest().withTableName(assembleTableName());
		
		if (getRangeKeyName() != null) {
			
		}
		else {
			deleteItemRequest.setKey(new Key().withHashKeyElement(new AttributeValue(getPrimaryKeyValue(anObject))));
		}
		
		getClient().deleteItem(deleteItemRequest);
		
	}

	@Override
	public T findById(String id) {
		GetItemRequest request = new GetItemRequest().withTableName(assembleTableName());
		
		if (getRangeKeyName() != null) {
			
		}
		else {
			request.setKey(new Key().withHashKeyElement(new AttributeValue(id)));
		}
		

		GetItemResult result = getClient().getItem(request);
		if (result.getItem() != null) {
			Map<String, AttributeValue> map = result.getItem();
			return instantiateObject(map);
		}
		else {
			return null;
		}
	}
	
	public Collection<T> findByHashKey(String hashKey) {
		
		QueryRequest request = new QueryRequest(assembleTableName(), new AttributeValue(hashKey));
		
		QueryResult result = getClient().query(request);
		
		Collection<T> results = new ArrayList<T>();
		
		List<Map<String, AttributeValue>> items = result.getItems();
		
		
		if (items != null) {
			for (Map<String, AttributeValue> map : items) {
				results.add(instantiateObject(map));
			}
		}
		
		return results;
		
		
	}

	@Override
	public Collection<String> findAllIds() {
		
		Collection<String> results = new ArrayList<String>();
		
		Key lastKeyEvaluated = null;
		do {
		    ScanRequest scanRequest = new ScanRequest()
		        .withTableName(assembleTableName())
		        .withAttributesToGet(getHashKeyName())
		        .withLimit(100)
		        .withExclusiveStartKey(lastKeyEvaluated);

		    ScanResult result = getClient().scan(scanRequest);
		    for (Map<String, AttributeValue> item : result.getItems()){
		        results.add(item.get(getHashKeyName()).getS());
		    }
		    lastKeyEvaluated = result.getLastEvaluatedKey();
		} while (lastKeyEvaluated != null);
		
		return results;
	}

	@Override
	public Collection<T> findAll() {
		Collection<T> results = new ArrayList<T>();
		
		Key lastKeyEvaluated = null;
		do {
		    ScanRequest scanRequest = new ScanRequest()
		        .withTableName(assembleTableName())
		        .withLimit(100)
		        .withExclusiveStartKey(lastKeyEvaluated);

		    ScanResult result = getClient().scan(scanRequest);
		    for (Map<String, AttributeValue> item : result.getItems()){
		        results.add(instantiateObject(item));
		    }
		    lastKeyEvaluated = result.getLastEvaluatedKey();
		} while (lastKeyEvaluated != null);
		
		return results;
	}


	public DynamoDBDatasource getDataSource() {
		return dataSource;
	}


	public void setDataSource(DynamoDBDatasource dataSource) {
		this.dataSource = dataSource;
	}
	public Boolean getUseCache() {
		return useCache;
	}
	public void setUseCache(Boolean useCache) {
		this.useCache = useCache;
	}
	
	

}
