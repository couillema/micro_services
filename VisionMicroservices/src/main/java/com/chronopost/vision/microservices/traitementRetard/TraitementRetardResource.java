package com.chronopost.vision.microservices.traitementRetard;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.chronopost.vision.model.TraitementRetardInput;
import com.chronopost.vision.model.TraitementRetardWork;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
*
* Ressource REST utilisée par le MS VisionMicroservices au moyen du Framework Dropwizard
* Ressource de traitement des retard de livraison sur les événements
* 
* @author LGY
*/
@Path("/")
@Api("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class TraitementRetardResource {

    /**
     * Service d'insertion des evenements GC
     */
    private ITraitementRetardService service;

    public TraitementRetardResource() {
    }

    /**
     * Injection de la classe service.
     * @param pService : le service TraitementRetard instancié
     * @return
     */
    public TraitementRetardResource setService(ITraitementRetardService pService) {
        this.service = pService ;
        return this;
    }

    /**
     * Méthode appelée sur appel HTTP distant de l'URL /TraitementRetard en POST 
     * la méthode fait appel au ITraitementRetardService service pour traiter les retard (Calcul DLE).    
     * @param lts : liste des Lts concernées
     * @param pRetards : liste des résultats d'invocation du calcul DLE pour chaque Lt.  
     * @return true et code 200 si le traitement s'est bien déroulé, 404 et false si une erreur est survenue durant le process
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @Path("/TraitementRetard")
    @ApiOperation(
			value = "Traitement des retards de livraison détectés.",
			notes = "Seuls les Retards dont la date de livraison estimée "
					+ "est différente de celle précédement calculée pour "
					+ "la Lt doivent être envoyé."
					+ "Ce micro-service effectue des appels au micro-service GenereEvt.")
    public Response traitementRetard(@ApiParam(required=true, value="[{ \"lt\":{\"no_lt\":\"XX123460X\"},\"resultCR\":{\"analyse\":{\"enRetardDateEstimeeDepassee\":0,\"enRetardDateEstimeeSupDateContractuelle\":1,\"enRetardDateHeureEstimeeDepassee\":0,\"enRetardDateHeureEstimeeSupDateHeureContractuelle\":0},\"calculDateDeLivraisonEstimee\":{\"codeEtape\":null,\"codeRaisonDeNonCalculDateDeLivraisonEstimee\":null,\"dateDeLivraisonEstimee\":\"10/01/2016 17:32\",\"dateDeLivraisonEstimeeCalculee\":false,\"dernierEvenement\":null,\"evenementPourCalculDateDeLivraisonEstimee\":null,\"generationRD\":\"O\",\"heureMaxDeLivraisonEstimee\":null,\"heureMinDeLivraisonEstimee\":null,\"informations\":null,\"ligneParametragePourCalculDateDeLivraisonEstimee\":null,\"raisonDeNonCalculDateDeLivraisonEstimee\":null},\"calculDateDeLivraisonEstimeeDebug\":[],\"resultCode\":0,\"resultRetard\":null}},{\"lt\":{\"no_lt\":\"XX123461X\"},\"resultCR\":{\"analyse\":{\"enRetardDateEstimeeDepassee\":0,\"enRetardDateEstimeeSupDateContractuelle\":1,\"enRetardDateHeureEstimeeDepassee\":0,\"enRetardDateHeureEstimeeSupDateHeureContractuelle\":0},\"calculDateDeLivraisonEstimee\":{\"codeEtape\":null,\"codeRaisonDeNonCalculDateDeLivraisonEstimee\":null,\"dateDeLivraisonEstimee\":\"11/01/2016 17:32\",\"dateDeLivraisonEstimeeCalculee\":false,\"dernierEvenement\":null,\"evenementPourCalculDateDeLivraisonEstimee\":null,\"generationRD\":\"N\",\"heureMaxDeLivraisonEstimee\":null,\"heureMinDeLivraisonEstimee\":null,\"informations\":null,\"ligneParametragePourCalculDateDeLivraisonEstimee\":null,\"raisonDeNonCalculDateDeLivraisonEstimee\":null},\"calculDateDeLivraisonEstimeeDebug\":[],\"resultCode\":0,\"resultRetard\":null}}]") final List<TraitementRetardInput> pRetards) {
		try {
			/* on récupère la DLE max (sans la nouvelle DLE) */
			final List<TraitementRetardWork> retards = this.service.extractMaxDLE(pRetards);
			/* On envoi un éventuel evenement RD si nécessaire */
			this.service.genereRD(retards);
			/* on mémorise la nouvelle DLE */
			this.service.memoriseDLE(retards);
		} catch (final Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e).build();
		}
		return Response.status(Status.OK).entity(Boolean.TRUE).build();
    }
}
