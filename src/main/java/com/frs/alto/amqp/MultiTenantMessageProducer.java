package com.frs.alto.amqp;

import org.springframework.amqp.core.AmqpTemplate;

public abstract class MultiTenantMessageProducer {
	
	private AmqpTemplate amqpTemplate;
	
	
	protected void send(String queueName, String message) {
		amqpTemplate.convertAndSend(queueName, MultiTenantMessageUtils.encodeMessage(message));
	}
	
}
