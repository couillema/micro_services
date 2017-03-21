package com.chronopost.vision.microservices.diffusionevt.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;

import com.chronopost.vision.jms.ITibcoEmsSender;
import com.chronopost.vision.microservices.diffusionevt.v1.commands.SendEmsCommand;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.rules.LtRules;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public enum DiffusionEvtServiceImpl implements IDiffusionEvtService {
	INSTANCE;

	private Destination topicDestination = null;
	private ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
	private ITibcoEmsSender emsSender;
		
    /**
     * Singleton
     * 
     * @return
     */
    public static IDiffusionEvtService getInstance() {
        return INSTANCE;
    }
    
    public IDiffusionEvtService setEmsSender(ITibcoEmsSender emsSender){
    	this.emsSender = emsSender;
    	return this;
    }
    
    public IDiffusionEvtService setTopicDestination(String topicName) throws JMSException{
    	topicDestination = emsSender.getTopicDestination(topicName);
    	return this;
    }
	
	public Boolean diffusionEvt(List<Lt> lts) throws JsonProcessingException, JMSException, NamingException, InterruptedException, ExecutionException {
		/* Enrichissement... */
		
		
		/* Diffusion Topic JMS */
		List<Future<Boolean>> emsSendFutures = Lists.newArrayList();
		for(Lt lt:lts){
			// On doit splitter par événement, donc on sauve la liste d'evt et on boucle dessus
			List<Evt> evts = new ArrayList<Evt>(lt.getEvenements());
			for(Evt evt:evts){				
				Map<String, Object> properties = getJmsProperties(lt.setEvenements(Arrays.asList(evt)));
				emsSendFutures.add(new SendEmsCommand(mapper.writeValueAsString(lt), properties, topicDestination, emsSender).queue());
			}
		}				
		
		for(Future<Boolean> emsSendFutureResult:emsSendFutures){
			if(!emsSendFutureResult.get()){
				return Boolean.FALSE;
			}
		}
		
		return Boolean.TRUE;
	}

	
	private Map<String, Object> getJmsProperties(Lt lt) {
		Map<String, Object> properties = Maps.newHashMap();
		Evt evt = lt.getEvenements().get(0);
		
		properties.put("codeService", lt.getCodeService()==null?"":lt.getCodeService());
		properties.put("noContrat", lt.getNoContrat()==null?"":lt.getNoContrat());
		properties.put("emailDestinataire", lt.getEmail1Destinataire()==null?"":LtRules.getEmailDestinataire(lt));
		properties.put("emailExpediteur", lt.getEmail1Expediteur()==null?"":LtRules.getEmailExpediteur(lt));
		properties.put("telDestinataire", lt.getTelephoneDestinataire()==null?"":LtRules.formatTelephoneDestinataire(lt));
		properties.put("lieuEvt", evt.getLieuEvt()==null?"":evt.getLieuEvt());
		properties.put("codeEvt", evt.getCodeEvt());
		properties.put("statusEvt", evt.getStatusEvt());
		properties.put("idbcoEvt", evt.getIdbcoEvt());
		properties.put("createurEvt", evt.getCreateurEvt()==null?"":evt.getCreateurEvt());
		
		return properties;
	}

}
