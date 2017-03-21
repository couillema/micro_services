package com.chronopost.vision.microservices.colioutai.get.v2;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.colioutai.get.v2.services.ColioutaiException;
import com.chronopost.vision.microservices.colioutai.get.v2.services.ColioutaiService;
import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.colioutai.v2.ColioutaiInfoLT;
import com.chronopost.vision.model.colioutai.v2.ColioutaiLog;
import com.chronopost.vision.model.rules.DateRules;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Resource Colioutai
 * 
 * @author vdesaintpern
 *
 */
@Path("/v2")
@Api("/")
public class ColioutaiResource {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(ColioutaiResource.class);

	/**
	 * Service colioutai
	 */
	private ColioutaiService service = null;

	public ColioutaiResource setService(ColioutaiService service) {
		this.service = service;
		return this;
	}

	/**
	 * Default constructeur
	 */
	public ColioutaiResource() {
		super();
	}

	/**
	 * Recherche des informations sur la LT
	 * 
	 * @param noLT
	 * @return
	 */
	@GET
	@Path("/colis/{noLT}")
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Recherche de l'état de la tournée ayant en charge la LT dont le numéro est passé en paramètres",
	notes = "Ce service fait appel à GetCodeTourneeFromLt pour trouver la tournée en charge du colis, puis à GetDetailTournee pour"
			+ " en récupérer l'état.<br/>"
			+ "Les colis n'ayant pas de géocodage sont géocodés via POI, puis en cas d'échec via l'API Google Maps.<br/>"
			+ "Si le colis n'a pas d'événement D+, l'ETA prévisionnel est recalculé via PTV d'après l'heure et la position de la dernière livraison.")
	public Response findInfoLT(
			@ApiParam(required = true, value = "Numéro de LT", defaultValue = "EE000000001FR") @PathParam("noLT") String noLT) {

		logger.info("Recherche du LT " + noLT);
		ColioutaiInfoLT infoTournee = null;

		try {
			infoTournee = service.findInfoLT(noLT, new Date(), null);
		} catch (ColioutaiException e) {
			if (e.getCodeErreur() != null && e.getCodeErreur().equals(ColioutaiException.LT_NOT_FOUND)) {
				return Response.status(Status.NOT_FOUND.getStatusCode()).entity("LT non trouvée").build();
			}
		} catch (MalformedURLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
		}

		return Response.status(Status.OK).entity(infoTournee).build();
	}
	
	@GET
	@Path("/C/colis/{noLT}")
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Recherche de l'état de la tournée ayant en charge la LT dont le numéro est passé en paramètres",
	notes = "Ce service fait appel à GetCodeTourneeFromLt pour trouver la tournée en charge du colis, puis à GetDetailTournee pour"
			+ " en récupérer l'état.<br/>"
			+ "Les colis n'ayant pas de géocodage sont géocodés via POI, puis en cas d'échec via l'API Google Maps.<br/>"
			+ "Si le colis n'a pas d'événement D+, l'ETA prévisionnel est recalculé via PTV d'après l'heure et la position de la dernière livraison.")
	public Response findInfoLTForClient(
			@ApiParam(required = true, value = "Numéro de LT", defaultValue = "EE000000001FR") @PathParam("noLT") String noLT) {

		logger.info("Recherche du LT " + noLT);
		ColioutaiInfoLT infoTournee = null;

		try {
			infoTournee = service.findInfoLT(noLT, new Date(), null, "C");
		} catch (ColioutaiException e) {
			if (e.getCodeErreur() != null && e.getCodeErreur().equals(ColioutaiException.LT_NOT_FOUND)) {
				return Response.status(Status.NOT_FOUND.getStatusCode()).entity("LT non trouvée").build();
			}
		} catch (MalformedURLException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
		}

		return Response.status(Status.OK).entity(infoTournee).build();
	}
	
	/**
	 * Recherche des informations sur la LT
	 * 
	 * @param noLT
	 * @return
	 */
	@GET
	@Path("/colis/{noLT}/{heure}")
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Recherche de l'état de la tournée ayant en charge la LT dont le numéro est passé en paramètres",
	notes = "Cette version du service permet d'avoir un état de la tournée à une heure précise, et ainsi de remonter dans son déroulement.<br/>"
			+ "Ce service fait appel à GetCodeTourneeFromLt pour trouver la tournée en charge du colis, puis à GetDetailTournee pour"
			+ " en récupérer l'état.<br/>"
			+ "Les colis n'ayant pas de géocodage sont géocodés via POI, puis en cas d'échec via l'API Google Maps.<br/>"
			+ "Si le colis n'a pas d'événement D+, l'ETA prévisionnel est recalculé via PTV d'après l'heure et la position de la dernière livraison.")
	public Response findInfoLT(
			@ApiParam(required = true, value = "Numéro de LT", defaultValue = "EE000000001FR") @PathParam("noLT") String noLT,
			@ApiParam(required = true, value = "Heure au format HH:mm", defaultValue = "10:00") @PathParam("heure") String heure) {

		logger.info("Recherche du LT " + noLT);
		ColioutaiInfoLT infoTournee = null;

		try {
			infoTournee = service.findInfoLT(noLT, DateRules.toTodayTime(heure), null);
		} catch (ColioutaiException e) {
			if (e.getCodeErreur() != null && e.getCodeErreur().equals(ColioutaiException.LT_NOT_FOUND)) {
				return Response.status(Status.NOT_FOUND.getStatusCode()).entity("LT non trouvée").build();
			}
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
		}

		return Response.status(Status.OK).entity(infoTournee).build();
	}

	/**
	 * Recherche des informations sur la LT
	 * 
	 * @param noLT
	 * @return
	 */
	@GET
	@Path("/colis/{noLT}/{heure}/{mockTempsTrajets}")
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Recherche de l'état de la tournée ayant en charge la LT dont le numéro est passé en paramètres",
	notes = "Cette version du service est utilisée pour les tests d'acceptation et les simulations, et prend en paramètre une string XML décrivant la tournée.<br/>"
			+ "Ce service fait appel à GetCodeTourneeFromLt pour trouver la tournée en charge du colis, puis à GetDetailTournee pour"
			+ " en récupérer l'état.<br/>"
			+ "Les colis n'ayant pas de géocodage sont géocodés via POI, puis en cas d'échec via l'API Google Maps.<br/>"
			+ "Si le colis n'a pas d'événement D+, l'ETA prévisionnel est recalculé via PTV d'après l'heure et la position de la dernière livraison.")
	public Response findInfoLT(
			@ApiParam(required = true, value = "Numéro de LT", defaultValue = "EE000000001FR") @PathParam("noLT") String noLT,
			@ApiParam(required = true, value = "Heure au format HH:mm", defaultValue = "10:00") @PathParam("heure") String heure,
			@ApiParam(required = true, value = "Doc XML décrivant la tournée") @PathParam("mockTempsTrajets") String mockTempsTrajets) {

		logger.info("Recherche du LT " + noLT);
		ColioutaiInfoLT infoTournee = null;

		try {
			infoTournee = service.findInfoLT(noLT, DateRules.toTodayTime(heure), mockTempsTrajets);
		} catch (ColioutaiException e) {
			if (e.getCodeErreur() != null && e.getCodeErreur().equals(ColioutaiException.LT_NOT_FOUND)) {
				return Response.status(Status.NOT_FOUND.getStatusCode()).entity("LT non trouvée").build();
			}
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
		}

		return Response.status(Status.OK).entity(infoTournee).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Timed
	@Path("/InsertLog")
	public Response insertLog(ColioutaiInfoLT colioutaiInfoLT) {
		try {
			if (service.insertLog(colioutaiInfoLT)) {
				return Response.status(HttpStatus.SC_OK).entity(true).build();
			}
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
					.entity(new MSTechnicalException("Insertion impossible...")).build();
		} catch (MSTechnicalException e) {
			logger.error("Erreur Technique lors de l'appel à insert log", e);
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
		} catch (FunctionalException e) {
			logger.error("Erreur fonctionnelle lors de l'appel à insert log", e);
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Timed
	@Path("/ColioutaiLog/{typeLog}/{from}/{to}")
	public Response getColioutaiLog(@PathParam("typeLog") String typeLog, @PathParam("from") String from,
			@PathParam("to") String to) {
		try {
			List<ColioutaiLog> list = service.getColioutaiLog(typeLog, DateRules.fromDateSortable(from),
					DateRules.fromDateSortable(to));
			return Response.status(HttpStatus.SC_OK).entity(list).build();
		} catch (MSTechnicalException e) {
			logger.error("Erreur Technique lors de l'appel à getColioutaiLog", e);
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
		} catch (ParseException e) {
			logger.error("Erreur Technique lors de l'appel à getColioutaiLog", e);
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
		}
	}

	@GET
	@Path("/hash/{noLT}")
	@Timed
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Calcul du hash pour LT dont le numéro est passé en paramètres", notes = "Ce service fait appel à une fonction de hashage pour calculer le hash de la LT. en se basant sur le numéro de contrat le code postal expéditeur et un grain de sel.")
	public Response getHash(
			@ApiParam(required = true, value = "Numéro de LT", defaultValue = "EE000000001FR") @PathParam("noLT") String noLT) {
		logger.info("hash pour la LT " + noLT);
		return Response.status(Status.OK).entity(service.calculLtHash(noLT)).build();
	}
}
