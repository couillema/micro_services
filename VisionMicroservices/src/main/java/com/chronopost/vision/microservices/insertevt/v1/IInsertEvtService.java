package com.chronopost.vision.microservices.insertevt.v1;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.naming.NamingException;

import com.chronopost.vision.jms.ITibcoEmsSender;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.updatespecificationscolis.v1.EvtEtModifs;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Multimap;

import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;

public interface IInsertEvtService {

    /**
     * Injection du dao.
     * 
     * @param dao
     * @return
     */
    public IInsertEvtService setDao(IInsertEvtDao dao);

    /**
     * Init du endpoint pour l'appel du service CalculRetard.
     * 
     * @param calculRetardEndpoint
     * @return
     * @throws MalformedURLException
     */
    IInsertEvtService setCalculRetardEndpoint(String calculRetardEndpoint) throws MalformedURLException;

    /**
     * Service d'insertion d'une liste d'événements.
     * 
     * @param evts
     * @return
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     * @throws ParseException
     * @throws NamingException 
     * @throws JMSException 
     */
    public boolean insertEvts(List<Evt> evts) throws IOException, InterruptedException, ExecutionException,
            TimeoutException, ParseException, JMSException, NamingException;

    /**
     * Récupération d'une liste de Lt avec noLt, evts (evt csv) et codesEvt
     * uniquement.
     * 
     * @param evts
     * @return
     * @throws ParseException
     * @throws JsonProcessingException
     */
    public List<Lt> getLtsPourMiseAJourDepuisEvt(List<Evt> evts, Map<String, Lt> lts,
            Map<String, ResultCalculerRetardPourNumeroLt> resultatsCalculRetard) throws ParseException,
            JsonProcessingException;

    /**
     * Groupage des événements par numéro LT.
     * 
     * @param evts
     * @return
     */
    public Multimap<String, Evt> getEvtsParNoLt(List<Evt> evts);

    /**
     * Filtre une liste d'événements en retirant ceux des colis fictifs et ceux dont le
     * numero de lt est incohérent.
     * 
     * @param evts
     * @return
     */
    List<Evt> filtreEvtColisAIgnorer(final List<Evt> evts);
    
    public List<Evt> filtreEvtColisAEcarter(final List<Evt> evts);
    
    /**
     * remise à zero du calculRetard pour permettre aux tests de changer d'url.
     */
    public void resetCalculRetard();

    public List<EvtEtModifs> getEvtsEtModifsPourSpecificationsColis(List<Evt> evts, Map<String, Lt> ltsFromDb,
            Map<String, ResultCalculerRetardPourNumeroLt> resultatsCalculRetard, Map<String, String> synonymesFromMaitre);
    
    
    public IInsertEvtService setEmsSender(ITibcoEmsSender emsSender);
    
    public IInsertEvtService setQueueDestination(String queueName) throws JMSException;
    

	public void declareAppelMS();
	public void declareFailMS();

}
