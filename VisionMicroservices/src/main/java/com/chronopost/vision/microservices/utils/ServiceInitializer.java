package com.chronopost.vision.microservices.utils;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.jms.TibcoEmsSender;
import com.chronopost.vision.microservices.VisionMicroserviceApplication;
import com.chronopost.vision.microservices.VisionMicroserviceConfiguration;
import com.chronopost.vision.microservices.diffusionevt.v1.DiffusionEvtServiceImpl;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.microservices.genereevt.GenereEvtServiceImpl;
import com.chronopost.vision.microservices.genereevt.Mapper;
import com.chronopost.vision.microservices.insertevt.v1.InsertEvtServiceImpl;
import com.chronopost.vision.microservices.sdk.ColioutaiInfoV1;
import com.chronopost.vision.microservices.sdk.DiffusionEvtV1;
import com.chronopost.vision.microservices.sdk.GenereEvtV1;
import com.chronopost.vision.microservices.sdk.GetCodeTourneeFromLtV1;
import com.chronopost.vision.microservices.sdk.GetDetailTourneeV1;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.microservices.sdk.InsertAgenceColisV1;
import com.chronopost.vision.microservices.sdk.InsertEvtV1;
import com.chronopost.vision.microservices.sdk.InsertLtV1;
import com.chronopost.vision.microservices.sdk.InsertPointTourneeV1;
import com.chronopost.vision.microservices.sdk.MaintienIndexEvtV1;
import com.chronopost.vision.microservices.sdk.SuiviBoxV1;
import com.chronopost.vision.microservices.sdk.SyntheseTourneeV1;
import com.chronopost.vision.microservices.sdk.TraitementRetardV1;
import com.chronopost.vision.microservices.sdk.UpdateSpecificationsColisV1;
import com.chronopost.vision.microservices.sdk.UpdateTourneeV1;

public class ServiceInitializer {

    private final static Logger logger = LoggerFactory.getLogger(ServiceInitializer.class);

    public static void setEndpointsAndTimeouts(VisionMicroserviceConfiguration configuration)
            throws MalformedURLException, JMSException, NamingException {

        // Affichage des endpoints dans la trace
        for (Entry<String, String> endpoint : configuration.getEndpoints().entrySet()) {
            logger.info("Endpoint " + endpoint.getKey() + " = " + endpoint.getValue());
        }

        // GetLT V1
        GetLtV1.getInstance().setEndpoint(configuration.getEndpoints().get("getLt"))
                .setTimeout(configuration.getSdkTimeouts().get("getLt"));

        // GetCodeTournee V1
        GetCodeTourneeFromLtV1.getInstance().setEndpoint(configuration.getEndpoints().get("getCodeTourneeFromLt"))
                .setTimeout(configuration.getSdkTimeouts().get("getCodeTourneeFromLt"));

        // GetDetailTournee V1
        GetDetailTourneeV1.getInstance().setEndpoint(configuration.getEndpoints().get("getDetailTournee"))
                .setTimeout(configuration.getSdkTimeouts().get("getDetailTournee"));

        // Colioutai V1
        ColioutaiInfoV1.getInstance().setEndpoint(configuration.getEndpoints().get("colioutai"))
                .setTimeout(configuration.getSdkTimeouts().get("colioutai"));

        // SuiviBox V1
        SuiviBoxV1.getInstance().setEndpoint(configuration.getEndpoints().get("suivibox"))
                .setTimeout(configuration.getSdkTimeouts().get("suivibox"));

        // InsertEvt V1
        InsertEvtServiceImpl.getInstance().setCalculRetardEndpoint(configuration.getEndpoints().get("calculRetard"));

        InsertEvtV1.getInstance().setEndpoint(configuration.getEndpoints().get("insertEvt"));

        // TraitementRetards V1
        TraitementRetardV1.getInstance().setEndpoint(configuration.getEndpoints().get("traitementRetard"))
                .setTimeout(configuration.getSdkTimeouts().get("traitementRetard"));

        // Genere V1
        GenereEvtServiceImpl.getInstance().setEndpoint(configuration.getEndpoints().get("sgesws"));
        GenereEvtServiceImpl.getInstance().setMapper(new Mapper());

        GenereEvtV1.getInstance().setEndpoint(configuration.getEndpoints().get("genereEvt"))
                .setTimeout(configuration.getSdkTimeouts().get("genereEvt"));

        // UpdateTournee V1
        UpdateTourneeV1.getInstance().setEndpoint(configuration.getEndpoints().get("updateTournee"));

        // MaintienIndexEvt v1
        MaintienIndexEvtV1.getInstance().setEndpoint(configuration.getEndpoints().get("maintienIndexEvt"));

        // UpdateTournee V1
        SyntheseTourneeV1.getInstance().setEndpoint(configuration.getEndpoints().get("syntheseTournee"));

        // InsertPointTournee V1
        InsertPointTourneeV1.getInstance().setEndpoint(configuration.getEndpoints().get("insertPointTournee"))
                .setTimeout(configuration.getSdkTimeouts().get("insertPointTournee"));

        // InsertAgenceColis V1
        InsertAgenceColisV1.getInstance().setEndpoint(configuration.getEndpoints().get("insertAgenceColis"))
                .setTimeout(configuration.getSdkTimeouts().get("insertAgenceColis"));

        // InsertLt V1
        InsertLtV1.getInstance().setEndpoint(configuration.getEndpoints().get("insertLt"))
                .setTimeout(configuration.getSdkTimeouts().get("insertLt"));

        UpdateSpecificationsColisV1.getInstance().setEndpoint(configuration.getEndpoints().get("updateSpecificationsColis")).setTimeout(configuration.getSdkTimeouts().get("updateSpecificationsColis"));
        
        
        try {
            InsertEvtServiceImpl.getInstance()
                    .setCalculRetardEndpoint(configuration.getEndpoints().get("calculRetard"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        
        // Connexion JMS pour la diffusion des evt
        TibcoEmsSender.getInstance().setConnectionFactory(configuration.getJmsDiffusion().get("connectionFactory"))
        								 .setInitialContextFactory(configuration.getJmsDiffusion().get("initialContextFactory"))
        								 .setProviderUrl(configuration.getJmsDiffusion().get("providerUrl"))
        								 .setUser(configuration.getJmsDiffusion().get("user"))
        								 .setPassword(configuration.getJmsDiffusion().get("password"))
        								 .setTtl(Integer.parseInt(configuration.getJmsDiffusion().get("ttl")))
        								 .connect()
        								 ;
        
        DiffusionEvtServiceImpl.getInstance().setEmsSender(TibcoEmsSender.getInstance())
        									 .setTopicDestination(configuration.getJmsDiffusion().get("topic"));
                       
        
        DiffusionEvtV1.getInstance().setEndpoint(configuration.getEndpoints().get("diffusionEvt"))
        .setTimeout(configuration.getSdkTimeouts().get("diffusionEvt"));
        
        
        InsertEvtServiceImpl.getInstance().setEmsSender(TibcoEmsSender.getInstance())
        								  .setQueueDestination(configuration.getJmsDiffusion().get("queue"));
        
    }

    public static void initSchema(String cqlScript) throws FileNotFoundException, InterruptedException,
            URISyntaxException {

        for (String query : cqlScript.split(";")) {
            if (query.trim().length() > 1 && !query.toUpperCase().contains("CREATE KEYSPACE")) {

                try {
                    VisionMicroserviceApplication.getCassandraSession().execute(query);
                } catch (Exception e) {
                    if (!query.toUpperCase().contains("ALTER")) {
                        logger.error("KO : " + query, e);
                        throw new MSTechnicalException(e);
                    }
                }
                logger.info("OK : {}", query);
            }
        }

    }
}
