package com.chronopost.vision.microservices.updatereferentiel;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.updatereferentiel.UpdateReferentielEvtInput;
import com.chronopost.vision.model.updatereferentiel.UpdateReferentielInfocompInput;
import com.chronopost.vision.model.updatereferentiel.UpdateReferentielInfocompOutput;
import com.chronopost.vision.model.updatereferentiel.contrat.ContratVision;
import com.chronopost.vision.model.updatereferentiel.contrat.ReferenceContrat;
import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.exceptions.DriverException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Resource UpdateReferentielInfosComp Resources web utilisées pour metre à jour
 * les transcodifications dues aux modifications du référentiel
 * 
 * @author jcbontemps
 *
 */
@Path("/")
@Api("/")
@Produces(APPLICATION_JSON + ";charset=utf-8")
public class UpdateReferentielResource {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(UpdateReferentielResource.class);

	private UpdateReferentielService service;

	/**
	 * Constructeur
	 */
	public UpdateReferentielResource() {
		super();
	}

	/**
	 * 
	 * @param service
	 *            une implémentation de UpdateReferentielService
	 * @return Cet objet ressource (pratique pour injecter à la création
	 */
	public UpdateReferentielResource setService(UpdateReferentielService service) {
		this.service = service;
		return this;
	}

	@POST
	@Path("/updatereferentiel/infoscomp")
	@Timed
	@Consumes(APPLICATION_JSON)
	@ApiOperation(value = "Met à jour le référentiel des Infoscomp dans la base Fluks des Transcodifications")
	/**
	 * Met à jour le référentiel des Infoscomp dans la base Fluks des
	 * Transcodifications
	 * 
	 * @param input
	 *            un objet UpdateReferentielInfocompOutput contenant la map des
	 *            infoscomp à modifier
	 * @return une réponse HTTP avec un UpdateReferentielInfocompOutput dans le
	 *         corps si 200
	 */
	public Response updateInfoscomp(
			@ApiParam(required = true, value = "{\"infoscomp\": {\"test1\":\"1\",\"test2\":\"2\"}}") UpdateReferentielInfocompInput input) {

		try {
			if (!service.updateInfoscomp(input.getInfoscomp()).contains(false)) {
				return Response.status(HttpStatus.SC_OK).entity(new UpdateReferentielInfocompOutput().setStatus(true))
						.build();
			}
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
					.entity(new MSTechnicalException("Insertion impossible...")).build();
		} catch (MSTechnicalException e) {
			logger.error(
					"Erreur Technique lors de l'appel à com.chronopost.vision.microservices.updatereferentiel.UpdateReferentielResource.updateInfoscomp",
					e);
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
		} catch (Exception e) {
			logger.error(
					"Erreur inconnue lors de l'appel à com.chronopost.vision.microservices.updatereferentiel.UpdateReferentielResource.updateInfoscomp",
					e);
			return Response.status(HttpStatus.SC_NOT_FOUND).entity(e).build();
		}
	}

	@POST
	@Path("/updatereferentiel/evt")
	@Timed
	@Produces(APPLICATION_JSON)
	@ApiOperation(value = "Met à jour le référentiel des Evts dans la base Fluks")
	/**
	 * Met à jour le référentiel des Evt dans la base Fluks des
	 * Transcodifications
	 * 
	 * @param input
	 *            un objet UpdateReferentielEvtOutput contenant une liste de
	 *            d'éléments d'événements utiles à la MAJ
	 * @return une réponse HTTP avec un UpdateReferentielEvtOutput dans le corps
	 *         si 200
	 */
	public Response updateEvt(@ApiParam(required = true) UpdateReferentielEvtInput input) {

		try {
			if (!service.updateEvt(input.getEvts()).contains(false)) {
				return Response.status(HttpStatus.SC_OK).entity(new UpdateReferentielInfocompOutput().setStatus(true))
						.build();
			}
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
					.entity(new MSTechnicalException("Insertion impossible...")).build();
		} catch (MSTechnicalException e) {
			logger.error(
					"Erreur Technique lors de l'appel à com.chronopost.vision.microservices.updatereferentiel.UpdateReferentielResource.updateEvt",
					e);
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e).build();
		} catch (Exception e) {
			logger.error(
					"Erreur inconnue lors de l'appel à com.chronopost.vision.microservices.updatereferentiel.UpdateReferentielResource.updateEvt",
					e);
			return Response.status(HttpStatus.SC_NOT_FOUND).entity(e).build();
		}
	}

	@POST
	@Path("/updatereferentiel/contrat")
	@Timed
	@Produces(APPLICATION_JSON)
	@ApiOperation(value = "Met à jour le référentiel des contrats dans la base Vision, table ref_contrat")
	/**
	 * Met à jour le référentiel des contrats dans la base Vision, table
	 * ref_contrat
	 * 
	 */
	public Response insertRefContrat(@ApiParam(required = true) final ReferenceContrat refContrat) {
		final ContratVision contratVision = refContrat.getContratVision();
		try {
			service.insertRefContrat(contratVision);
			if (contratVision.isDerMsg()) {
				final boolean complete = service.checkInsertVersionComplete(contratVision.getVersion(),
						contratVision.getNbContrats());
				if (complete) {
					service.updateParametreVersionContrat(contratVision.getVersion());
					return Response.status(HttpStatus.SC_CREATED).build();
				} else {
					return Response.status(HttpStatus.SC_EXPECTATION_FAILED).build();
				}
			} else {
				return Response.status(HttpStatus.SC_OK).build();
			}
		} catch (final DriverException driverException) {
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(driverException).build();
		}
	}
}
