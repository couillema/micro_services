package com.chronopost.vision.microservices.suivibox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;

/**
 * implémentation du service IInsertSuiviboxService Classe d'insertion des
 * événements GC
 * 
 * @author jcbontemps
 */
public class SuiviboxServiceImpl implements SuiviboxService {

    /**
     * dao du service
     */
    private SuiviBoxDao dao;

    public SuiviboxServiceImpl(SuiviBoxDao dao) {
        this.dao = dao;
    }

    /*
     * Pour chaque evt, on regarde si l'élément est bien un GC et si oui, on
     * commande l'insertion en base en appelant la méthode du DAO dao
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.suivibox.insert.services.
     * IInsertSuiviboxService#insertEvtGCInDatabase(java.util.List)
     */
    public boolean insertEvtGCInDatabase(List<Evt> evts) {

    	/* infocomps */
    	Map<String, String> infoscomp = null;
    	
        // liste des box à inserer réellement dans la base
        List<SuiviBoxAgence> listeBox = new ArrayList<SuiviBoxAgence>();

        // Parcours des evts pour déterminer lesquels sont réellement GC et
        // ajout la la liste des box à insérer
        for (Evt evt : evts) {
            if (Utils.isEvenementGC(evt)) {
            	infoscomp = evt.getInfoscomp();
                listeBox.add
                	( new SuiviBoxAgence()
                	. setIdBox(evt.getNoLt())
                	. setDateDernierEvt(evt.getDateEvt())
                    . setPcAgence(evt.getLieuEvt())
                    . setAction(infoscomp.get(EInfoComp.ACTION_CONTENANT.getCode()))
                    . setEtape(infoscomp.get(EInfoComp.ETAPE_CONTENANT.getCode()))
                    . setCodeTournee(infoscomp.get(EInfoComp.CODE_TOURNEE.getCode()))
                    . setCodeLR(infoscomp.get(EInfoComp.CODE_LIGNE_ROUTIERE.getCode()))
                    );
            }
        }

        // Code pour mettre à jour les tables de suivi des boxes
        if (listeBox.size() > 0) {
            dao.updateAgenceBox(listeBox);
        }

        return true;
    }

}
