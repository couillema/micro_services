package com.chronopost.vision.microservices.lt.insert;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Ressource REST utilisée par le MS VisionMicroservices au moyen du Framework Dropwizard
 * Ressource d'insertion des LT dans la base Cassandra
 * @author jcbontemps
 *
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Api("/")
public class InsertLtResource {

	private static final Logger logger = LoggerFactory.getLogger(InsertLtResource.class);

    protected IInsertLtService service;

    public InsertLtResource() {
    }

    /**
     * 
     * @param service l'objet IInsertLtService utilisé pour effectuer les opérations d'insertion en base  
     * @return cet objet VisionInsertLtResource. Utile pour injecter la dépendance au moment de la création de l'objet
     */
    public InsertLtResource setService(IInsertLtService service) {
        this.service = service;
        return this;
    }

    /**
     * Insertion/mise à jour d'une liste de Lt
     * Permet de modifier les enregistrements de la table LT.
     * L'objectif principal du service est l'insertion des infos LT issues des annonces.
     * @param lts
     * @return true si l'insertion est OK, une TechnicalException sinon
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Path("/InsertLT/")
    @ApiOperation(value = "Insertion/mise à jour d'une liste de Lt",
	notes = "Permet de modifier les enregistrements de la table LT.<br/>"
			+ "L'objectif principal du service est l'insertion des infos LT issues des annonces.") 
	public Response insertLts(
			@ApiParam(required = true, value = "[{\"no_lt\": \"EE000000001FR\",\"cab_evt_saisi\": \"\",\"cab_recu\": \"EE000000001FR\", \"code_evt\": \"D\", \"code_evt_ext\": \"\", \"code_pays_destinataire\": \"FR\", \"code_postal_evt\": \"\", \"code_raison_evt\": \"\"}}]", defaultValue = "[{\"EE000000001FR\": {\"no_lt\": \"EE000000001FR\",\"cab_evt_saisi\": \"\",\"cab_recu\": \"EE000000001FR\", \"code_evt\": \"D\", \"code_evt_ext\": \"\", \"code_pays_destinataire\": \"FR\", \"code_postal_evt\": \"\", \"code_raison_evt\": \"\"}]") List<Lt> lts) {
		try {
			if (service.insertLtsInDatabase(lts)) {
				return Response.status(HttpStatus.SC_OK).entity(true).build();
			}
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
					.entity(new MSTechnicalException("Insertion impossible...")).build();
		} catch (MSTechnicalException e) {
			logger.error("Erreur Technique lors de l'appel à com.chronopost.vision.microservices.lt.insert.VisionInsertLtResource.insertLts", e);
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
		} catch (FunctionalException e) {
			logger.error("Erreur fonctionnelle lors de l'appel à com.chronopost.vision.microservices.lt.insert.VisionInsertLtResource.insertLts", e);
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
		}
	}

}