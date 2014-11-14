package com.frs.alto.amqp;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.frs.alto.core.TenantMetaData;
import com.frs.alto.core.TenantResolver;
import com.frs.alto.util.TenantUtils;

public abstract class MultiTenantMessageConsumer implements MessageListener {
	
	private TenantResolver resolver;

	@Override
	public void onMessage(Message message) {
		
		MultiTenantQueueMessage msg = MultiTenantMessageUtils.decodeMessage(message.getBody());
		
		TenantMetaData md = resolver.byId(msg.getTenantId());
		
		TenantUtils.setThreadHost(md);
		
		onMessage(md, msg.getMessage());
		
	}

	protected abstract void onMessage(TenantMetaData md, String msg);
	
	
}
