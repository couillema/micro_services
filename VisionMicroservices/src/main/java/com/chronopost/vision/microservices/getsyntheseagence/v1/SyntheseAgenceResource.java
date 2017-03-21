package com.chronopost.vision.microservices.getsyntheseagence.v1;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseColisEtListeValeurs;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDispersionQuantite;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDispersionQuantitePassee;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDistributionQuantite;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseListeValeurs;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 *
 * Point d'entrée du MS getSyntheseAgence
 * 
 * @author FTE
 */
@Path("/getSyntheseAgence/")
@Api("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class SyntheseAgenceResource {
	
	private static final Logger log = LoggerFactory.getLogger(SyntheseAgenceResource.class);

	private ISyntheseAgenceService service;

	public void setService(ISyntheseAgenceService service) {
		this.service = service;
	}

	@GET
	@Path("Dispersion/Quantite/{posteComptable}/dateAppel/{dateAppel}")
	@Timed
	@ApiOperation(value = "Synthèse dispersion", notes = "Renvoie la synthèse dispersion de l'agence pour la journée courante")
	public Response getDispersionQuantite(
			@ApiParam(required = true, value = "Poste comptable de l’agence", defaultValue = "62999") @PathParam("posteComptable") final String posteComptable,
			@ApiParam(required = true, value = "La date et l'heure locale d'appel du MS au format ISO 8601", defaultValue = "2017-01-10T09:55:00+01:00") @PathParam("dateAppel") final String dateAppel,
			@HeaderParam(value="user_agent") final String userAgent) {
		try {
			service.declareAppelMS();
			final SyntheseDispersionQuantite entity = service.getSyntheseDispersionQuantite(posteComptable, dateAppel);
			return newResponse(entity);
		} catch (Exception exception) {
			log.error("Erreur getSyntheseAgence/Dispersion/Quantite v1 : " + exception.getMessage(), exception);
			service.declareFailMS();
			return newResponse(exception);
		}
	}

	@GET
	@Path("Distribution/Quantite/{posteComptable}/dateAppel/{dateAppel}")
	@Timed
	@ApiOperation(value = "Synthèse ditribution", notes = "Renvoie la synthèse ditribution de l'agence pour la journée courante")
	public Response getDistributionQuantite(
			@ApiParam(required = true, value = "Poste comptable de l’agence", defaultValue = "62999") @PathParam("posteComptable") final String posteComptable,
			@ApiParam(required = true, value = "La date et l'heure locale d'appel du MS au format ISO 8601", defaultValue = "2017-01-10T09:55:00+01:00") @PathParam("dateAppel") final String dateAppel,
			@HeaderParam(value="user_agent") final String userAgent) {
		try {
			final SyntheseDistributionQuantite entity = service.getSyntheseDistributionQuantite(posteComptable,
					dateAppel);
			return newResponse(entity);
		} catch (Exception exception) {
			log.error("Erreur getSyntheseAgence/Distribution/Quantite v1 : " + exception.getMessage(), exception);
			return newResponse(exception);
		}
	}

	@GET
	@Path("Dispersion/Quantite/{posteComptable}/{nbJours}")
	@Timed
	@ApiOperation(value = "Synthèse dispersion passée", notes = "Renvoie la synthèse dispersion de l’agence sur le nombre de jour indiqués avant le jour en cours. (Par exemple les 14 jours précédents)")
	public Response getDispersionQuantitePassee(
			@ApiParam(required = true, value = "Poste comptable de l’agence", defaultValue = "62999") @PathParam("posteComptable") final String posteComptable,
			@ApiParam(required = true, value = "Nombre de jours précédents", defaultValue = "14") @PathParam("nbJours") final Integer nbJours,
			@HeaderParam(value="user_agent") final String userAgent) {
		try {
			service.declareAppelMS();
			final SyntheseDispersionQuantitePassee entity = service.getSyntheseDispersionQuantitePassee(posteComptable,
					nbJours);
			return newResponse(entity);
		} catch (Exception exception) {
			log.error("Erreur getSyntheseAgence/Dispersion/Quantite v1 : " + exception.getMessage(), exception);
			service.declareFailMS();
			return newResponse(exception);
		}
	}

	/**
	 * @param posteComptable
	 * @param indicateur
	 * @param limit
	 * @return n colis par poste comptable et indicateur d'étape
	 */
	@GET
	@Path("Activite/{posteComptable}/{indicateur}/dateAppel/{dateAppel}")
	@Timed
	@ApiOperation(value = "Activité (dispersion ou distribution) d'un indicateur", notes = "Renvoie l'activité (dispersion ou distribution) de l'agence pour la journée courante uniquement pour les colis correspondant à un indicateur")
	public Response getActivite(
			@ApiParam(required = true, value = "Poste comptable de l’agence", defaultValue = "62999") @PathParam("posteComptable") final String posteComptable,
			@ApiParam(required = true, value = "Code de l'indicateur", defaultValue = "nbColisSDSeche") @PathParam("indicateur") final String indicateur,
			@ApiParam(required = true, value = "La date et l'heure locale d'appel du MS au format ISO 8601", defaultValue = "2017-01-10T09:55:00+01:00") @PathParam("dateAppel") final String dateAppel,
			@HeaderParam(value="user_agent") final String userAgent) {
		try {
			service.declareAppelMS();
			final SyntheseColisEtListeValeurs resultat = service
					.getSyntheseDetailIndicateur(posteComptable, indicateur, null, dateAppel, null);
			return newResponse(resultat.getColis());
		} catch (Exception exception) {
			log.error("Erreur getSyntheseAgence/Activite v1 : " + exception.getMessage(), exception);
			service.declareFailMS();
			return newResponse(exception);
		}
	}

	/**
	 * Renvoie la liste des colis, le nombre total des colis et la liste des valeurs
	 * @param posteComptable : le poste comptable de l’agence
	 * @param indicateur : le code de l'indicateur
	 * @param limit  : le nombre de colis attendu
	 * @param dateAppel : date local d'appel du MS 
	 * @return Un objet <code>SyntheseDispersionColisEtListeValeurs</code> 
	 * qui contient la liste des colis, les listes des valeurs et le nombre total des colis
	 * 
	 * @author bjbari
	 */
	@GET
	@Path("/ActiviteEtListeValeur/{posteComptable}/{indicateur}/dateAppel/{dateAppel}/{nbJours}")
	@Timed
	@ApiOperation(value = "Activité d'un indicateur ( dispersion ou distribution)", notes = "Renvoie l'activité (dispersion ou distribution) de l'agence pour la"
			+ " journée courante uniquement pour les colis et la liste des valeurs de filtres correspondant à un indicateur ")
	public Response getActiviteEtListeValeur(
			@ApiParam(required = true, value = "Poste comptable de l’agence", defaultValue = "62999") @PathParam("posteComptable") final String posteComptable,
			@ApiParam(required = true, value = "Code de l'indicateur", defaultValue = "nbColisSDSeche") @PathParam("indicateur") final String indicateur,
			@ApiParam(required = true, value = "La date et l'heure locale d'appel du MS au format ISO 8601", defaultValue = "2017-01-10T09:55:00+01:00") @PathParam("dateAppel") final String dateAppel,
			@ApiParam(required = true, value = "Le nombre de colis attendu", defaultValue = "1500") @QueryParam("limit") final Integer limit,
			@ApiParam(required = true, value = "Nombre de jours précédents", defaultValue = "0") @PathParam("nbJours") final Integer nbJours,
			@HeaderParam(value="user_agent") final String userAgent) {
		try {
			service.declareAppelMS();
			final SyntheseColisEtListeValeurs entity = service.getSyntheseDetailIndicateur(posteComptable,
					indicateur, limit, dateAppel, nbJours);
			return newResponse(entity);
		} catch (Exception exception) {
			log.error(
					"Erreur ActiviteEtListeValeur/{posteComptable}/{indicateur}/dateAppel/{dateAppel} v1 : "
							+ exception.getMessage(),
					exception);
			service.declareFailMS();
			return newResponse(exception);
		}
	}

	/**
	 * @param posteComptable : le poste comptable de l’agence
	 * @param indicateur : le code de l'indicateur
	 * @param limit  : le nombre de colis attendu
	 * @param criteres : Un objet <code> SyntheseDispersionListeValeurs <code> qui correspond aux critères des filtres
	 * @param dateAppel : date local d'appel du MS 
	 * @return Un objet <code>SyntheseColisEtListeValeurs</code> 
	 * qui contient la liste raffinée des colis et le nombre total des colis.
	 * 
	 * @author bjbari
	 */
	@POST
	@Path("Activite/{posteComptable}/{indicateur}/Filtre/dateAppel/{dateAppel}/{nbJours}")
	@Timed
	@ApiOperation(value = "Activité dispersion/distribution d'un indicateur selon les critères", notes = "Renvoie l'activité"
			+ " ( dispersion ou distibution ) de l'agence pour la journée courante uniquement pour les colis correspondant"
			+ " à un indicateur et selon les critères passés dans le body")
	@Consumes(MediaType.APPLICATION_JSON + ";")
	public Response getActiviteRaffine(
			@ApiParam(required = true, value = "Poste comptable de l’agence", defaultValue = "62999") @PathParam("posteComptable") final String posteComptable,
			@ApiParam(required = true, value = "Code de l'indicateur", defaultValue = "nbColisSDSeche") @PathParam("indicateur") final String indicateur,
			@ApiParam(required = true, value = "La date et l'heure locale d'appel du MS au format ISO 8601", defaultValue = "2017-01-10T09:55:00+01:00") @PathParam("dateAppel") final String dateAppel,
			@ApiParam(required = true, value = "Le nombre de colis attendu", defaultValue = "1500") @QueryParam("limit") final Integer limit,
			@ApiParam(required = true, value = "Nombre de jours précédents", defaultValue = "0") @PathParam("nbJours") final Integer nbJours,
			@ApiParam(required = true, value = "Objet contenant les listes de valeurs") final SyntheseListeValeurs criteres,
			@HeaderParam(value="user_agent") final String userAgent) {
		try {
			service.declareAppelMS();
			final SyntheseColisEtListeValeurs entity = service.getSyntheseDetailIndicateurRaffine(
					criteres, posteComptable, indicateur, limit, dateAppel, nbJours);
			return newResponse(entity);
		} catch (Exception exception) {
			log.error("Erreur getSyntheseAgence/Activite/Filtre/dateAppel/{dateAppel} v1 : "
					+ exception.getMessage(), exception);
			service.declareFailMS();
			return newResponse(exception);
		}
	}

	/**
	 * Renvoie une map qui regroupe les colis par code dispersion et selon les précocités
	 * 
	 * @param posteComptable : Le poste comptable de l’agence
	 * @param codeIndicateur :Le code de l'indicateur
	 * @param dateAppel : La date et l'heure locale d'appel du MS au format ISO 8601
	 * @return
	 * 
	 * @author bjbari
	 */
	@GET
	@Path("Dispersion/Activite/{posteComptable}/{indicateur}/GroupByCodeDispersion/dateAppel/{dateAppel}/{nbJours}")
	@Timed
	@ApiOperation(value = "Activité dispersion d'un indicateur regroupé par code dispersion et précocité", notes = "Renvoie une map qui regroupe les colis par code dispersion et selon les précocités")
	@Consumes(MediaType.APPLICATION_JSON + ";")
	public Response getDispersionActiviteGroupByCodeDispersion(
			@ApiParam(required = true, value = "Poste comptable de l’agence", defaultValue = "62999") @PathParam("posteComptable") final String posteComptable,
			@ApiParam(required = true, value = "Code de l'indicateur", defaultValue = "nbColisSDSeche") @PathParam("indicateur") final String codeIndicateur,
			@ApiParam(required = true, value = "La date et l'heure locale d'appel du MS au format ISO 8601", defaultValue = "2017-01-10T09:55:00+01:00") @PathParam("dateAppel") final String dateAppel,
			@ApiParam(required = true, value = "Nombre de jours précédents", defaultValue = "0") @PathParam("nbJours") final Integer nbJours,
			@HeaderParam(value="user_agent") final String userAgent) {
		try {
			service.declareAppelMS();
			final Map<String, Map<String, Integer>> entity = service
					.getSyntheseDispersionGroupByCodeDispersion(posteComptable, codeIndicateur, dateAppel, nbJours);
			return newResponse(entity);
		} catch (Exception exception) {
			log.error(
					"Erreur getSyntheseAgence/Dispersion/Activite/{posteComptable}/{indicateur}/GroupByCodeDispersion/dateAppel/{dateAppel} v1 : "
							+ exception.getMessage(),
					exception);
			service.declareFailMS();
			return newResponse(exception);
		}
	}

	/**
	 * Renvoie la synthèse dispersion de l’agence pour l’intervalle de temps [dateDebut,dateDebut]
	 * @param posteComptable
	 * @param dateDebut
	 * @param dateFin
	 * @return
	 */
	@GET
	@Path("Dispersion/Quantite/JoursPrecedents/{posteComptable}/{dateDebut}/{dateFin}")
	@Timed
	@ApiOperation(value = "Synthèse dispersion d'un jour précédent", notes = "Renvoie la synthèse dispersion de l'agence pour la Nième journées précédentes")
	public Response getDispersionQuantiteJoursPrecedents(
			@ApiParam(required = true, value = "Poste comptable de l’agence", defaultValue = "62999") @PathParam("posteComptable") final String posteComptable,
			@ApiParam(required = true, value = "La date et l'heure de début au format ISO 8601", defaultValue = "2017-01-10T09:55:00+01:00") @PathParam("dateDebut") final String dateDebut,
			@ApiParam(required = true, value = "La date et l'heure de fin au format ISO 8601", defaultValue = "2017-01-11T09:55:00+01:00") @PathParam("dateFin") final String dateFin,
			@HeaderParam(value="user_agent") final String userAgent) {
		try {
			service.declareAppelMS();
			final SyntheseDispersionQuantite entity = service
					.getSyntheseDispersionQuantiteJoursPrecedents(posteComptable, dateDebut, dateFin);
			return newResponse(entity);
		} catch (Exception exception) {
			log.error("Erreur getSyntheseAgence/Dispersion/Quantite v1 : " + exception.getMessage(), exception);
			service.declareFailMS();
			return newResponse(exception);
		}
	}

	@GET
	@Path("Dispersion/Activite/JoursPrecedents/{posteComptable}/{indicateur}/{dateDebut}/{dateFin}")
	@Timed
	@ApiOperation(value = "Synthèse dispersion d'un jour précédent", notes = "Renvoie la synthèse dispersion de l'agence pour la Nième journées précédentes")
	public Response getDispersionActiviteJoursPrecedents(
			@ApiParam(required = true, value = "Poste comptable de l’agence", defaultValue = "62999") @PathParam("posteComptable") final String posteComptable,
			@ApiParam(required = true, value = "Code de l'indicateur (du jour)", defaultValue = "nbColisSDSeche") @PathParam("indicateur") final String indicateur,
			@ApiParam(required = true, value = "La date et l'heure de début au format ISO 8601", defaultValue = "2017-01-10T09:55:00+01:00") @PathParam("dateDebut") final String dateDebut,
			@ApiParam(required = true, value = "La date et l'heure de fin au format ISO 8601", defaultValue = "2017-01-11T09:55:00+01:00") @PathParam("dateFin") final String dateFin,
			@HeaderParam(value="user_agent") final String userAgent) {
		try {
			service.declareAppelMS();
			final SyntheseColisEtListeValeurs entity = service
					.getSyntheseDispersionDetailIndicateurJoursPrecedents(posteComptable, indicateur, dateDebut,
							dateFin);
			return newResponse(entity);
		} catch (Exception exception) {
			log.error("Erreur getSyntheseAgence/Dispersion/Activite v1 : " + exception.getMessage(), exception);
			service.declareFailMS();
			return newResponse(exception);
		}
	}

	/**
	 * 
	 * @param entity
	 *            un objet
	 * @return Si l'objet passé en paramètre s'il est non null et n'est pas une exception, renvoie une {@code Response} OK (HTTP 200) et l'objet passé en paramètre.
	 *         Si l'objet passé en paramètre s'il est non null et est une exception, renvoie une {@code Response} INTERNAL_SERVER_ERROR (HTTP 500) avec la trace de l'exception.
	 *         Sinon renvoie une {@code Response} avec un status NOT_FOUND (HTTP 404)
	 */
	private Response newResponse(final Object entity) {
		if (entity instanceof Exception) {
			return Response.serverError().entity(entity).build();
		} else if (entity != null) {
			return Response.ok(entity).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}
}
