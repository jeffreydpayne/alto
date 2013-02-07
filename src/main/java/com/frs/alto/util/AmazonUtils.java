package com.frs.alto.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;

public class AmazonUtils {
	
	private static Log log = LogFactory.getLog(AmazonUtils.class);

	public static boolean hasQueue(AmazonSQS client, String queueName) {

		ListQueuesResult result = client.listQueues();
		if (result != null) {

			for (String url : result.getQueueUrls()) {
				if (url.endsWith(queueName)) {
					return true;
				}
			}

		}

		return false;

	}

	public static String getQueueUrl(AmazonSQS client, String queueName, boolean create) {

		ListQueuesResult result = client.listQueues();
		if (result != null) {

			for (String url : result.getQueueUrls()) {
				if (url.endsWith(queueName)) {
					return url;
				}
			}

		}

		
		if (create) {
			CreateQueueRequest createQueueRequest = new CreateQueueRequest(
					queueName);
			String queueUrl = client.createQueue(createQueueRequest).getQueueUrl();
			log.info("Creating Queue: " + queueUrl);
			return queueUrl;
		}
		else {
			return null;
		}

	}

}
