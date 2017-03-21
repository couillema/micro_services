package com.chronopost.vision.microservices.lt.get;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.enums.ETraitementSynonymes;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.getLt.v1.RechercheLtParEmailDestiInput;
import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * 
 * Ressource REST utilisée par le MS VisionMicroservices au moyen du Framework
 * Dropwizard Ressource de recherche de LTS dans la base Cassandra avec ou non
 * résolution de synonymie
 * 
 * @author jcbontemps
 */
@Path("/")
@Api("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GetLtResource {

	private static final Logger logger = LoggerFactory.getLogger(GetLtResource.class);

    private IGetLtService service;

    public GetLtResource() {
        super();
    }

    /**
     * @param service Service de récupération des LTs
     * @return
     */
    public GetLtResource setService(IGetLtService service) {
        this.service = service;
        return this;
    }

    /**
     * Retourne une liste de LTs avec résolution des synonymes
     * @param lt une liste (chaine de caractère) de numéros de LTs (séparés par des virgules) à rechercher 
     * @return La Liste des LTs recherchées avec résolution des synonymes
     */
    @GET
    @Timed
    @Path("/GetLT/{no_lt}")
    @ApiOperation("Recherche d'une LT unique par son numéro, avec résolution des synonymes")
    public Response getLT(@ApiParam(required = true, value = "Numéro de LT", defaultValue="EE000000001FR") @PathParam("no_lt") String lt) {
        return this.getLT(lt, true);
    }

    /**
     * Retourne une liste de LTs
     * @param lt une liste (chaine de caractère) de numéros de LTs (séparés par des virgules) à rechercher 
     * @param resolutionSynonymes doit-on effectuer une résolution des synonymes ?
     * @return La Liste des LTs recherchées
     */
    @GET
    @Timed
    @Path("/GetLT/{no_lt}/{withSynonymes}")
    @ApiOperation("Recherche d'une LT unique par son numéro, avec résolution des synonymes optionnelle")
    public Response getLT(@ApiParam(required = true, value = "Numéro de LT", defaultValue="EE000000001FR") @PathParam("no_lt") String lt,@ApiParam(required = true, defaultValue = "true") @PathParam("withSynonymes") Boolean resolutionSynonymes) {

        if (lt == null || lt.trim().equals("")) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Map<String, Lt> mapLT = service.getLtsFromDatabase(Arrays.asList(lt),
                resolutionSynonymes == false ? ETraitementSynonymes.PAS_DE_RESOLUTION_DES_SYNONYMES
                        : ETraitementSynonymes.RESOLUTION_DES_SYNONYMES);

        if (mapLT == null || !mapLT.containsKey(lt)) {
            return Response.status(Status.NOT_FOUND).entity(new HashMap<String,Lt>()).build();
        	//return Response.status(Status.NO_CONTENT).entity().build();
        }

        return Response.status(Status.OK).entity(mapLT).build();
    }

    /**
     * Retourne une liste de LTs avec résolution des synonymes
     * @param lts une liste de numéros de LTs à récupérer
     * @return La Liste des LTs recherchées avec résolution des synonymes
     */
    @POST
    @Timed
    @Path("/GetLTs")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Recherche d'une liste de LT unique par leur numéro, avec résolution des synonymes")
    public Response getLTs(@ApiParam(required = true, value="[\"EE000000001FR\",\"EE123456789FR\"]") List<String> lts) {
        return this.getLTs(lts, true);
    }

    /**
     * Retourne une liste de LTs
     * @param ltList une liste de numéros de LTs à rechercher
     * @param resolutionSynonymes doit-on effectuer une résolution des synonymes ?
     * @return La Liste des LTs recherchées
     */
    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/GetLTs/{withSynonymes}")
    @ApiOperation(value = "Recherche d'une liste de LT par leur numéro, avec résolution des synonymes optionnelle",
			notes = "lien vers la doc : https://drive.google.com/open?id=1NcEO4B5euABBPx54E7hgFbY9A0GB_zA0jxQ2RS0Rr_0")
    public Response getLTs(@ApiParam(required = true, value="[\"EE000000001FR\",\"EE123456789FR\"]", defaultValue="[\"EE000000001FR\",\"EE123456789FR\"]") List<String> ltList, @ApiParam(required=true,defaultValue="true") @PathParam("withSynonymes") Boolean resolutionSynonymes) {

        if (ltList == null || ltList.size() == 0) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Map<String, Lt> mapLTs = service.getLtsFromDatabase(ltList,
                resolutionSynonymes == false ? ETraitementSynonymes.PAS_DE_RESOLUTION_DES_SYNONYMES
                        : ETraitementSynonymes.RESOLUTION_DES_SYNONYMES);
        return Response.status(Status.OK).entity(mapLTs).build();
    }

    /**
     * Retourne une liste de LTs "light" renseignées uniquement avec les champs NO_LT, SYNONYME_MAITRE, IDX_DEPASSEMENT et CODE_SERVICE
     * @param ltList une liste de numéros de LTs à rechercher
     * @param resolutionSynonymes doit-on effectuer une résolution des synonymes ?
     * @return La Liste des LTs recherchées
     */
    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/GetLTs/small/{withSynonymes}")
    @ApiOperation(value = "Recherche d'une liste de LT par leur numéro, avec résolution des synonymes optionnelle",
	notes = "Cette version du service ne renvoie que quelques champs de la table LT pour limiter la charge sur le cluster C* et accélérer les accès : no_lt, synonyme_maitre, idx_depassement et code_service.")
    public Response getLTsSmall(@ApiParam(required = true, value="[\"EE000000001FR\",\"EE123456789FR\"]", defaultValue="[\"EE000000001FR\",\"EE123456789FR\"]") List<String> ltList,@ApiParam(required=true,defaultValue="true") @PathParam("withSynonymes") Boolean resolutionSynonymes) {

        if (ltList == null || ltList.size() == 0) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Map<String, Lt> mapLTs = service.getLtsFromDatabase(ltList,
                resolutionSynonymes == false ? ETraitementSynonymes.PAS_DE_RESOLUTION_DES_SYNONYMES
                        : ETraitementSynonymes.RESOLUTION_DES_SYNONYMES, true);
        return Response.status(Status.OK).entity(mapLTs).build();
    }

    /**
     * Recherche des LT par adresse email destinataire. Limitée à la tranche de
     * date fournie dans parametresRecherche: 
     * adresseEmailDesti = adresse email destinataire
     * dateDebutRecherche = date de début de recherche
     * dateFinRecherche = date de fin de recherche
     * @param parametresRecherche objet paramètre de recherche
     * @return les LTs correspondant à la recherche
     */
    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/RechercheLtParEmailDesti")
    @ApiOperation(value = "Recherche sur une plage de date des LT pour une adresse mail destinataire donnée (avec résolution de synonymes)",
	notes = "lien vers la doc...")
    public Response rechercheLt(@ApiParam(required=true, value="{\"emailDestinataire\":\"stephanie.perrot29@icloud.com\",\"dateDebutRecherche\":\"2014-08-24T10:00:00.000\", \"dateFinRecherche\":\"2015-12-24T23:00:00.000\"}") 
    							RechercheLtParEmailDestiInput parametresRecherche) {
        try {
            if (parametresRecherche.getEmailDestinataire() == null
                    || parametresRecherche.getDateDebutRecherche() == null
                    || parametresRecherche.getDateFinRecherche() == null) {
                logger.warn("Erreur sur RechercheLtParEmailDesti : Il faut fournir une adresse email desti de recherche, ainsi qu'une tranche de dates. Merci de vérifier vos paramètres d'appel.");
                return Response
                        .status(Status.BAD_REQUEST)
                        .entity("Il faut fournir une adresse email desti de recherche, ainsi qu'une tranche de dates. Merci de vérifier vos paramètres d'appel.")
                        .build();
            }

            Map<String, Lt> lts = service.getLtsParEmailDestinataire(parametresRecherche.getEmailDestinataire(),
                    parametresRecherche.getDateDebutRecherche(), parametresRecherche.getDateFinRecherche());
            return Response.status(Status.OK).entity(lts).build();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Erreur sur RechercheLtParEmailDesti", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }
    }

}