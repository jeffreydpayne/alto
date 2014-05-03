package com.frs.alto.dao.riak;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.RiakFactory;
import com.basho.riak.client.raw.http.HTTPClientConfig;
import com.basho.riak.client.raw.http.HTTPClusterConfig;

public class RiakClientFactoryBean implements FactoryBean<IRiakClient> {
	
	private String host = "localhost";
	private String[] hosts;
	private int maxConnections = 50;
	private boolean useProtocolBuffers = false;

	@Override
	public IRiakClient getObject() throws Exception {
		
		
		if (hosts != null) {
			HTTPClusterConfig clusterConfig = new HTTPClusterConfig(maxConnections);
		    HTTPClientConfig clientConfig = HTTPClientConfig.defaults();
	    	clusterConfig.addHosts(clientConfig, StringUtils.join(hosts, ","));
	    	return RiakFactory.newClient(clusterConfig);

	    }
	    else if (useProtocolBuffers){
	    	return RiakFactory.pbcClient(host, 8087);
	    }
	    else {
	    	return RiakFactory.httpClient("http://" + host + ":8098/riak");
	        
	    }
	    	
	     
	     
	     

	}

	@Override
	public Class<?> getObjectType() {
		return IRiakClient.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String[] getHosts() {
		return hosts;
	}

	public void setHosts(String[] hosts) {
		this.hosts = hosts;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public boolean isUseProtocolBuffers() {
		return useProtocolBuffers;
	}

	public void setUseProtocolBuffers(boolean useProtocolBuffers) {
		this.useProtocolBuffers = useProtocolBuffers;
	}


}
