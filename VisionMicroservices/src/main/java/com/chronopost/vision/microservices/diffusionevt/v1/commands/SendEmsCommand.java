package com.chronopost.vision.microservices.diffusionevt.v1.commands;

import java.util.Map;

import javax.jms.Destination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.jms.ITibcoEmsSender;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class SendEmsCommand extends HystrixCommand<Boolean>{
	private final static Logger logger = LoggerFactory.getLogger(SendEmsCommand.class);
	
	private ITibcoEmsSender emsSender; 
	private String message;
	private Map<String, Object> properties;
	private Destination jmsDestination;
	
	public SendEmsCommand(String message, Map<String, Object> properties, Destination jmsDestination, ITibcoEmsSender emsSender) {
		super(HystrixCommandGroupKey.Factory.asKey("SendEmsCommand"));
		
		this.message = message;
		this.properties = properties;
		this.jmsDestination = jmsDestination;
		this.emsSender = emsSender;
	}

	@Override
	protected Boolean run() throws Exception {
		emsSender.sendMessage(message, properties, jmsDestination);
		return Boolean.TRUE;
	}
	
	@Override
	protected Boolean getFallback() {
		logger.error("Erreur envoi JMS", getFailedExecutionException());
		throw new MSTechnicalException(getFailedExecutionException());
	}

}
