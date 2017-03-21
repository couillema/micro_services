package com.chronopost.vision.microservices.supervision;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.EXPECTATION_FAILED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.exceptions.InvalidParameterException;
import com.chronopost.vision.microservices.exception.InvalidParameterExceptionProvider;
import com.chronopost.vision.model.supervision.SnapShotVision;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * Ressource REST utilisée par le MS InsertC11 Ressource d'insert des C11
 * 
 * @author XRE
 */
@Path("supervision")
@Api("/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class SupervisionResource {

	private static final Logger logger = LoggerFactory.getLogger(SupervisionResource.class);

	private ISupervisionService service;
	
	/**
	 * Retourne un objet SnapShotVision contenant les vitesses instantanées des
	 * dix dernières minutes
	 */
	@GET
	@Timed
	@ApiOperation(value = "getRecentSnapShot", notes = "Retourne un objet contenant les vitesses instantanées de la dernière dizaine de minutes écoulées")
	public Response getRecentSnapShot() {
		try {
			SnapShotVision snapShot = service.getSnapShotVisionForLast10Minutes();
			return Response.status(OK).entity(snapShot).build();
		} catch (Exception e) {
			logger.error("Error in getRecentSnapShot", e);
			return Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	/**
	 * Retourne une liste de SnapShotVision contenant les vitesses instantanées
	 * du jour actuel ou du jour passé en paramétre
	 * 
	 * @param jour yyyyMMdd
	 * @throws ParseException 
	 */
	@GET
	@Path("releve")
	@Timed
	@ApiOperation(value = "getSnapShotsForADay", notes = "Retourne une liste d'objet contenant les vitesses instantanées "
			+ "du jour passé en paramétre ou du jour actuel si param jour non passé")
	public Response getSnapShotsForADay(@ApiParam(name = "jour", required = false, value="yyyyMMdd") @QueryParam("jour") String jour) {
		List<SnapShotVision> snapShots = new ArrayList<>();
		try {
			snapShots = service.getSnapShotsVisionForADay(jour);
		} catch (InvalidParameterException e) {
			logger.error("InvalidParameterException in getSnapShotsForADay. " + e.getMessage(), e);
			return new InvalidParameterExceptionProvider().toResponse(e);
		} catch (Exception e) {
			logger.error("Error in getSnapShotsForADay", e);
			return Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		return Response.status(OK).entity(snapShots).build();
	}

	/**
	 * Retourne un objet SnapShotVision contenant les vitesses moyennes du jour indiqué
	 * 
	 * @param jour yyyyMMdd
	 */
	@GET
	@Path("average")
	@Timed
	@ApiOperation(value = "getSnapShotAverage", notes = "Retourne un objet contenant les vitesses moyennes du jour indiqué "
			+ "ou du jour actuel si param jour non passé")
	public Response getSnapShotAverage(
			@ApiParam(name = "jour", required = false, value="yyyyMMdd") @QueryParam("jour") String jour) throws InvalidParameterException {
		
		SnapShotVision snapShotVision;
		try {
			snapShotVision = service.getSnapShotsAverageForADay(jour);
		} catch (InvalidParameterException e) {
			logger.error("InvalidParameterException in getSnapShotAverage. " + e.getMessage(), e);
			return new InvalidParameterExceptionProvider().toResponse(e);
		} catch (Exception e) {
			logger.error("Error in getSnapShotAverage", e);
			return Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		return Response.status(OK).entity(snapShotVision).build();
	}

	public void setService(ISupervisionService service) {
		this.service = service;
	}
	
	@GET
	@Path("msStatus")
	public Response getMSStatus() {
		try {
			final boolean msStatus = service.getMSStatus();
			if (msStatus) {
				return Response.status(OK).entity(msStatus).build();
			} else {
				return Response.status(EXPECTATION_FAILED).entity(msStatus).build();
			}
		} catch (Exception e) {
			return Response.status(INTERNAL_SERVER_ERROR).entity(false).build();
		}
	}
}
