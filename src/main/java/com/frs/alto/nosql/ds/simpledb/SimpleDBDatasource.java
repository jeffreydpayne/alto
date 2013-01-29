package com.frs.alto.nosql.ds.simpledb;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.nosql.ds.BaseNoSqlDataSource;
import com.frs.alto.nosql.ds.dynamodb.DynamoDBDatasource;
import com.frs.alto.nosql.mapper.NoSqlKey;
import com.frs.alto.nosql.mapper.NoSqlObjectMapper;

public class SimpleDBDatasource extends BaseNoSqlDataSource implements InitializingBean {
	
	
	private static Log log = LogFactory.getLog(SimpleDBDatasource.class);
	
	private String domainNamespace = null;
	private String apiId = null;
	private String secretKey = null;
	private String endpoint = null;
	
	private AWSCredentials credentials = null;
	private AmazonSimpleDBClient client = null;
	private Collection<String> domainNames = null;
	
	public void afterPropertiesSet() throws Exception {
		
		refreshTableNames();
		
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
	public BaseDomainObject findByKey(Class<? extends BaseDomainObject> clazz,
			NoSqlObjectMapper mapper, NoSqlKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<BaseDomainObject> findByHashKeys(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			Collection<NoSqlKey> hashKeys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<BaseDomainObject> findWholeRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<BaseDomainObject> findByRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, String startRange, String endRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<BaseDomainObject> findByUpperRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, String startRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<BaseDomainObject> findByLowerRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, String endRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<BaseDomainObject> findByRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number startRange, Number endRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<BaseDomainObject> findByUpperRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number startRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<BaseDomainObject> findByLowerRange(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,
			String hashKey, Number endRange) {
		// TODO Auto-generated method stub
		return null;
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
	public void delete(Class<? extends BaseDomainObject> clazz,
			NoSqlObjectMapper mapper, NoSqlKey key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Class<? extends BaseDomainObject> clazz,
			NoSqlObjectMapper mapper, Collection<NoSqlKey> keys) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String save(BaseDomainObject domain, NoSqlObjectMapper mapper) {
		
		//PutAttributesRequest request = new PutAttributesRequest(domainName, itemName, attributes)
		
		//getClient().putAttributes(putAttributesRequest)
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> save(Collection<BaseDomainObject> domains,
			NoSqlObjectMapper mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> findAllIds(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<BaseDomainObject> findAll(
			Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper) {
		// TODO Auto-generated method stub
		return null;
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
	
	
	
	
	

}
