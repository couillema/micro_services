package com.chronopost.vision.microservices.diffusionevt.v1;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.jms.JMSException;
import javax.naming.NamingException;

import com.chronopost.vision.jms.ITibcoEmsSender;
import com.chronopost.vision.model.Lt;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IDiffusionEvtService {

	IDiffusionEvtService setEmsSender(ITibcoEmsSender emsSender);
	IDiffusionEvtService setTopicDestination(String topicName) throws JMSException;
	Boolean diffusionEvt(List<Lt> lts) throws JsonProcessingException, JMSException, NamingException, InterruptedException, ExecutionException;

}
