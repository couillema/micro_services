package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import com.chronopost.vision.model.updatespecificationscolis.v1.EConsigne;

/**
 * Implémentation de l'interface IUpdateSpecificationsTranscoder
 * 
 * @author jcbontemps
 */
public enum UpdateSpecificationsColisTranscoderConsignesImpl implements IUpdateSpecificationsTranscoder {
    INSTANCE;

    @Override
    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.updatespecificationscolis.v1.
     * IUpdateSpecificationsTranscoder#transcode(java.lang.String)
     */
    public EConsigne transcode(final String code) {
        if (code == null)
            return null;
        switch (code) {
        case "CHADD_HZCA":
        case "CHADD_ZCA":
        case "CHDATE":
        case "CHDRDV":
        case "COMPADD":
        case "DIF":
        case "RDJ":
        case "RDL":
            return EConsigne.REMISE_ADRESSE;
        case "MDA_HZCA":
        case "MDA_ZCA":
        case "MDA":
            return EConsigne.MISE_A_DISPO_AGENCE;
        case "MDBP_HZCA":
        case "MDBP_ZCA":
        case "MDBPR_ZCA":
        case "MDR_HZCA":
        case "MDR_ZCA":
        case "PUS":
        case "RBP":
        case "EVT_CF_I_RBP":
            return EConsigne.REMISE_BUREAU;
        case "RPR":
        case "EVT_CF_I_RPR":
            return EConsigne.REMISE_POINT_RELAIS;
        case "RTIERS":
        case "RVOISIN":
            return EConsigne.REMISE_TIERS;
        case "SAV":
        case "SK":
            return EConsigne.CONSIGNE_EN_LIGNE;
        case "ANNUL":
        case "DEST":
        case "REXP":
        case "A LIVRER":
        case "A TRIER":
        case "Arret":
        case "Association":
        case "Configuration":
        case "Demarrage":
        case "DR":
        case "Fermeture":
        case "Interrogation":
        case "Mise en service":
        case "Ouverture":
        case "R":
        case "SAC DIRECT":
        case "EVT_CF_I":
        case "EVT_CF_I_AGI":
        case "EVT_CF_I_FRE":
        case "EVT_CF_I_FRS":
        case "EVT_CF_I_MDD":
        case "EVT_CF_I_PUS":
        case "EVT_CF_I_RDJ":
        case "EVT_CF_I_RDL":
        case "EVT_CF_I_SAV":
        case "EVT_CF_I_STK":
        default:
            return null;
        }
    }

}
