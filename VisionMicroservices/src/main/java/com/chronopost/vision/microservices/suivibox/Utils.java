package com.chronopost.vision.microservices.suivibox;

import com.chronopost.vision.model.Evt;

/**
 * Classe utilitaire contenant diverses constantes 
 * ainsi que diverses méthodes statiques réutilisables
 * @author jcbontemps
 *
 */
public class Utils {

    /**
     * Code d'un événement GC
     */
    public static final String CODE_EVT_GC = "GC";
    
    /**
     * Code d'un Prod No Lt
     */
    public static final int CODE_PROD_NO_LT = 20;

    /**
     * Méthode statique déterminant si un Evenement est ou non un événement GC
     * en fonction de son codeEvt ("GC" pour un GC) et de son prodNoLt (20 pour un GC)
     * @param evt Un événement classique
     * @return L'événement est-il de nature GC
     */
    public static boolean isEvenementGC(Evt evt) {
        boolean isEvtGC = false;
        isEvtGC = CODE_EVT_GC.equalsIgnoreCase(evt.getCodeEvt()) && (evt.getProdNoLt() == CODE_PROD_NO_LT);
        return isEvtGC;
    }

}
