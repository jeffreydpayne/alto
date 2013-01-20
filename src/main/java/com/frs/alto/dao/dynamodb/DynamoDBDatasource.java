package com.frs.alto.dao.dynamodb;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.ListTablesResult;
import com.frs.alto.cache.AltoCache;
import com.frs.alto.domain.BaseDomainObject;

public class DynamoDBDatasource {
	
	private static Log log = LogFactory.getLog(DynamoDBDatasource.class);
	
	private String tableSpace = null;
	private String apiId = null;
	private String secretKey = null;
	private long readThroughput = 10l;
	private long writeThroughput = 5l;
	private String endpoint = null;
	private IdentifierGenerator identifierGenerator = null;
	private boolean cacheByDefault = false;		
	
	private AltoCache altoCache = null;
	private AWSCredentials credentials = null;
	private AmazonDynamoDBClient client = null;
	private Collection<String> tableNames = null;
	
	public String generateIdentifier(BaseDomainObject object) {
		return identifierGenerator.generateStringIdentifier(object);
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
	
	public Collection<String> getTableNames() {
		
		if (tableNames == null) {
			refreshTableNames();
		}
		return tableNames;
		
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

	public IdentifierGenerator getIdentifierGenerator() {
		return identifierGenerator;
	}

	public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
		this.identifierGenerator = identifierGenerator;
	}

	public AltoCache getAltoCache() {
		return altoCache;
	}

	public void setAltoCache(AltoCache altoCache) {
		this.altoCache = altoCache;
	}

	public boolean isCacheByDefault() {
		return cacheByDefault;
	}

	public void setCacheByDefault(boolean cacheByDefault) {
		this.cacheByDefault = cacheByDefault;
	}
	
	

}
