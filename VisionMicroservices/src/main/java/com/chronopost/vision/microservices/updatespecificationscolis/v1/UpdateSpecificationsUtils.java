package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_BA;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_CL;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_CT;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_DC;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_H;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_I;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_IC;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_RB;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_SC;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_ST;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.PROD_NO_LT_BOX;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_ANNUL;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_EVT_CF_I_RBP;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_EVT_CF_I_RPR;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.updatespecificationscolis.v1.EInfoSupplementaire;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;

/**
 * Classe utilitaire métier contenant les règles métiers utilisées par le microservice UpdateSpecifications
 * Ces règles ne sont employées qu'en interne dans le microservice. Et n'ont donc pas vocation à être exportées
 * c'est pourquoi elles font l'objet d'une classe spécifique dans le même package de même projet, et non dans le projet VisionMicroserviceRules 
 * @author jcbontemps
 */
public class UpdateSpecificationsUtils {

    /**
     * @param evt un évènement
     * @return la spécificité portée par l'événement. Si aucune spécificité pour ce type d'événement, retourne null;
     */
    public ESpecificiteColis extractSpecifsEvt(final @NotNull Evt evt) {
		if (evt.getCodeEvt() == null)
			return null;
		if (evt.getProdNoLt() != null && evt.getProdNoLt().equals(PROD_NO_LT_BOX))
			return ESpecificiteColis.BOX;
        switch (evt.getCodeEvt()) {
        case EVT_CT:
            return ESpecificiteColis.CONSIGNE;
        case EVT_IC:
            return ESpecificiteColis.INTERACTION_CLIENT;
        case EVT_I:
        	final String valeurInfocompI = evt.getInfoscomp().get(EInfoComp.CONSIGNE_EVT_I.getCode());
            if (VALUE_EVT_CF_I_RPR.equals(valeurInfocompI) || VALUE_EVT_CF_I_RBP.equals(valeurInfocompI)) return ESpecificiteColis.CONSIGNE;
            break;
        case EVT_SC:
            if (evt.getInfoscomp().get(EInfoComp.CODE_SAC.getCode()) != null) return ESpecificiteColis.SENSIBLE;
            break;
        case EVT_ST:
        	final String client = evt.getInfoscomp().get(EInfoComp.TAXE_NO_CONTRAT.getCode());
            int montant = 0 ;
            String montantS2;
            try {
            	final String montantS = evt.getInfoscomp().get(EInfoComp.TAXE_VALEUR.getCode());
            	try {
            		montantS2 = montantS.split(" ")[0];
            		if (montantS2.contains("."))
            			montantS2 = montantS2.split("\\.")[0];
            		if (montantS2.contains(","))
            			montantS2 = montantS2.split(",")[0];
            	} catch (NullPointerException e){
            		montantS2=montantS;
            	}
                montant = Integer.parseInt(montantS2);
            } catch(NumberFormatException|PatternSyntaxException e)   {
                return null ;
            }
            if (montant > 0 && client == null) return ESpecificiteColis.TAXE;
            break ;
        case EVT_H: // RG-MSUpdSpecColis-005
			if (ESpecificiteColis.ATTRACTIF.getCode().equalsIgnoreCase(evt.getInfoscomp().get(EInfoComp.COMMENTAIRE60.getCode())))
				return ESpecificiteColis.ATTRACTIF;
            break;
        case EVT_BA:
			if (evt.getInfoscomp().get(EInfoComp.ID_CONTENU.getCode()) != null)
				return ESpecificiteColis.SAC;
            break;
        default :
            return null ;
        }
		return null;
    }

     /**
     * @param evt
     * @return true si l'évènement porte bien une spécificité "Consigne Reçue" 
     */
    public boolean isConsignesRecues(final Evt evt) {
        if (EVT_CL.equals(evt.getCodeEvt()) && !VALUE_ANNUL.equals(evt.getInfoscomp().get(EInfoComp.CONSIGNE_EVT_CL.getCode())) && evt.getInfoscomp().get(EInfoComp.CONSIGNE_EVT_CL.getCode()) != null) return true;
        else return false;
    }

    /**
     * @param evt
     * @return true si l'évènement porte bien une spécificité "Consigne annulée" 
     */
    public boolean isConsignesAnnulees(final Evt evt) {
		if (EVT_CL.equals(evt.getCodeEvt())
				&& VALUE_ANNUL.equals(evt.getInfoscomp().get(EInfoComp.CONSIGNE_EVT_CL.getCode()))
				&& evt.getInfoscomp().get(EInfoComp.CONSIGNE_EVT_CL.getCode()) != null)
			return true;
		else
			return false;
    }

    /**
     * @param evt
     * @return true si l'évènement porte bien une spécificité "Consigne traitée" sur un événement I 
     */
    public boolean isConsignesTraiteesI(final Evt evt) {
		if (EVT_I.equals(evt.getCodeEvt()) && evt.getInfoscomp().get(EInfoComp.CONSIGNE_EVT_I.getCode()) != null)
			return true;
		return false;
    }

    /**
     * @param evt
     * @return true si l'évènement porte bien une spécificité "Consigne traitée" sur un événement CT 
     */
    public boolean isConsignesTraiteesCT(final Evt evt) {
		if (EVT_CT.equals(evt.getCodeEvt()) && (evt.getInfoscomp().get(EInfoComp.ACTION_CONTENANT.getCode()) != null
				|| evt.getInfoscomp().get(EInfoComp.CONSIGNE_EVT_CL.getCode()) != null))
			return true;
        return false;
    }

    /**
     * @param evt : l'événement portant la consigne
     * @return la valeur de la consigne portée par l'evt  
     */
    public String extractConsigne(final Evt evt) {
		if (EVT_CL.equals(evt.getCodeEvt()))
			return evt.getInfoscomp().get(EInfoComp.CONSIGNE_EVT_CL.getCode());
		if (EVT_CT.equals(evt.getCodeEvt()))
			return evt.getInfoscomp().get(EInfoComp.ACTION_CONTENANT.getCode()) == null ? evt.getInfoscomp().get(EInfoComp.CONSIGNE_EVT_CL.getCode()) : evt.getInfoscomp().get(EInfoComp.ACTION_CONTENANT.getCode());
		if (EVT_I.equals(evt.getCodeEvt()))
			return evt.getInfoscomp().get(EInfoComp.CONSIGNE_EVT_I.getCode());
		return null;
    }

    /**
     * @param evt : l'événement portant l'id de consigne
     * @return la valeur de'identifiant consigne porté par l'evt s'il existe, null sinon  
     */
    public String extractIdConsigne(final Evt evt) {
		if (evt.getInfoscomp() != null)
			return evt.getInfoscomp().get(EInfoComp.ID_CONSIGNE.getCode());
		else
			return null;
    }

    /**
     * Si l'evt est ST et possède une infoComp TaxeValeur
     * et pas d'infoComp numéro de contrat alors on renvoi la TaxeValeur
     * @param evt
     * @return La valeur de la taxe si l'evt porte une infoSupp TAXE_VALEUR, null sinon
     */
    public String extractInfoSuppTaxeValeur(final Evt evt)   {
		if (EVT_ST.equals(evt.getCodeEvt()) && evt.getInfoscomp() != null) {
			final String valeurTaxe = evt.getInfoscomp().get(EInfoComp.TAXE_VALEUR.getCode());
			final String contratTaxe = evt.getInfoscomp().get(EInfoComp.TAXE_NO_CONTRAT.getCode());
			if (StringUtils.isNotEmpty(valeurTaxe) && StringUtils.isEmpty(contratTaxe))
				return valeurTaxe;
		}
        return null;
    }

    /**
     * Si evt = DC et infoComp SWAP_NO_LT_RETOUR définie on renvoi le numéro Lt Retour 
     * @param evt
     * @return le numéro de LT retour si l'evt porte une infoSupp NO_LT_RETOUR, null sinon  
     */
    public String extractInfoSuppNoLtRetour(final Evt evt)   {
		if (EVT_DC.equals(evt.getCodeEvt()) && evt.getInfoscomp() != null) {
			final String noLtRetour = evt.getInfoscomp().get(EInfoComp.SWAP_NO_LT_RETOUR.getCode());
			if (StringUtils.isNotEmpty(noLtRetour)) {
				return noLtRetour;
			}
		}
        return null;
    }
    
    /**
     * RG-MSUpdSpecColis-63
     * Si evt RB et infoComp IDENTIFIANT POINT RELAIS definie, alors on renvoi l'id du point relais.
     * @param evt
     * @return L'identifiant POINT_RELAIS si l'evt porte une infoSupp POINT_RELAIS  
     */
    public String extractInfoSuppDepotRetour(final Evt evt)   {
        if (EVT_RB.equals(evt.getCodeEvt()) && evt.getInfoscomp() != null){
        	final String idPtRelais = evt.getInfoscomp().get(EInfoComp.IDENTIFIANT_POINT_RELAIS.getCode());
        	if (StringUtils.isNotEmpty(idPtRelais))
        		return idPtRelais;
        }
        return null;
    }
    
    /**
     * RG-MSUpdSpecColis-64
     * @param evt
     * @return l'événement porte une info supplémentaire REMISE_REGATE  
     */
    public String extractInfoSuppRemiseReGate(final Evt evt)   {
    	if (evt.getInfoscomp() != null){
    		final String codeRegate = evt.getInfoscomp().get(EInfoComp.CODE_REGATE_EMMETEUR.getCode());
    		if (StringUtils.isNotEmpty(codeRegate))
    			return codeRegate;
    	}
    	return null;
    }

    /**
     * RG-MSUpdSpecColis-68
     * @param evt
     * @return l'événement porte une info supplémentaire CRENEAU_HORAIRE  
     */
    public boolean isInfoSuppCreneau(final Evt evt)   {
		return (EVT_DC.equals(evt.getCodeEvt()) && evt.getInfoscomp() != null
				&& (evt.getInfoscomp().get(EInfoComp.IDBCO_CRENEAU_BORNE_MIN.getCode()) != null
						|| evt.getInfoscomp().get(EInfoComp.IDBCO_CRENEAU_BORNE_MAX.getCode()) != null));
    }

    /**
     * 
     * @param evt : l'événement à utiliser
     * @return une map d'InfoSupp à partir de l'événement fourni
     *
     * @author LGY
     */
    public Map<String,String> extractInfoSupp(final Evt evt){
    	final Map<String,String> result = new HashMap<>();
    	String value;
    	
    	/* Valeur de la taxe à collecter */
		if ((value = extractInfoSuppTaxeValeur(evt)) != null)
			result.put(EInfoSupplementaire.TAXE_VALEUR.getCode(), value);

		/* No LT Retour d'un SWAP */
		if ((value = extractInfoSuppNoLtRetour(evt)) != null)
			result.put(EInfoSupplementaire.NO_LT_RETOUR.getCode(), value);

		/* Identifiant point relais */
		if ((value = extractInfoSuppDepotRetour(evt)) != null)
			result.put(EInfoSupplementaire.DEPOT_RELAIS.getCode(), value);

		/* Code regate */
		if ((value = extractInfoSuppRemiseReGate(evt)) != null)
			result.put(EInfoSupplementaire.REMISE_REGATE.getCode(), value);
    	
    	/* Creneau horaire de fin */
		if (isInfoSuppCreneau(evt)) {
			String creneauDebut = evt.getInfoscomp().get(EInfoComp.IDBCO_CRENEAU_BORNE_MIN.getCode());
			String creneauFin = evt.getInfoscomp().get(EInfoComp.IDBCO_CRENEAU_BORNE_MAX.getCode());
			if (creneauDebut != null && StringUtils.isNotBlank(creneauDebut))
				result.put(EInfoSupplementaire.CRENEAU_DEBUT_CONTRACTUEL.getCode(), creneauDebut);
			if (creneauFin != null && StringUtils.isNotBlank(creneauFin))
				result.put(EInfoSupplementaire.CRENEAU_FIN_CONTRACTUEL.getCode(), creneauFin);
		}
		return result;
    }
}
