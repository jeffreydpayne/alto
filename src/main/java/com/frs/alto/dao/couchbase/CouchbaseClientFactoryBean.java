package com.frs.alto.dao.couchbase;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;

import com.couchbase.client.CouchbaseClient;

public class CouchbaseClientFactoryBean implements FactoryBean<CouchbaseClient> {
	
	private String bucketName;
	
	private String hosts;
	
	private String bucketPassword = "";
	
	@Override
	public CouchbaseClient getObject() throws Exception {
		
		
		String[] hostNames = StringUtils.split(hosts, ",");
		
		List<URI> hosts = new ArrayList<URI>();
		
		for (String hostName : hostNames) {
			hosts.add(new URI("http://" + hostName + ":8091/pools"));
		}
		
		
		return new CouchbaseClient(hosts, bucketName, bucketPassword);


	}

	@Override
	public Class<?> getObjectType() {
		return CouchbaseClient.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}


	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public String getBucketPassword() {
		return bucketPassword;
	}

	public void setBucketPassword(String bucketPassword) {
		this.bucketPassword = bucketPassword;
	}




}
