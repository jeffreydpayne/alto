package com.frs.alto.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frs.alto.util.TenantUtils;

public class MultiTenantMessageUtils {
	
	
	private static ObjectMapper jsonMapper = new ObjectMapper();
	
	public static String encodeMessage(String msg) {
		
		MultiTenantQueueMessage wrappedMsg = new MultiTenantQueueMessage();
		wrappedMsg.setMessage(msg);
		wrappedMsg.setTenantId(TenantUtils.getThreadTenantIdentifier());
		
		try {
			return jsonMapper.writeValueAsString(wrappedMsg);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static MultiTenantQueueMessage decodeMessage(byte[] body) {
		
		try {
			return jsonMapper.readValue(body, MultiTenantQueueMessage.class);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	
	public static MultiTenantQueueMessage decodeMessage(String msg) {
		
		try {
			return jsonMapper.readValue(msg, MultiTenantQueueMessage.class);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

}
