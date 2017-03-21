package com.chronopost.vision.microservices.maintienindexevt.v1;

import java.io.IOException;
import java.text.ParseException;

import com.chronopost.vision.microservices.maintienindexevt.v1.model.UpdateDepassementProactifInput;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtInput;
import com.chronopost.vision.model.maintienIndexEvt.v1.MaintienIndexEvtOutput;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class MaintienIndexEvtServiceImpl implements IMaintienIndexEvtService {

    private IMaintienIndexEvtDao dao;

    /**
     * Singleton
     */
    static class InstanceHolder {
        public static final IMaintienIndexEvtService service;
        static {
            service = new MaintienIndexEvtServiceImpl();
        }
    }

    /**
     * Singleton
     * 
     * @return
     */
    public static IMaintienIndexEvtService getInstance() {
        return InstanceHolder.service;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chronopost.vision.microservices.maintienindexevt.v1.
     * IMaintienIndexEvtService
     * #setDao(com.chronopost.vision.microservices.maintienindexevt
     * .v1.IMaintienIndexEvtDao)
     */
    @Override
    public IMaintienIndexEvtService setDao(final IMaintienIndexEvtDao dao) {
        this.dao = dao;
        return this;
    }

    @Override
    public MaintienIndexEvtOutput maintienIndexEvt(final MaintienIndexEvtInput input) throws JsonProcessingException,
            ParseException, Exception {
    	final MaintienIndexEvtInput sanitizedInput = sanitizeInput(input);
        if (sanitizedInput == null) {
            return new MaintienIndexEvtOutput().setSuccess(true);
        }

        // maintien de la table traces_date_proactif
        dao.insertTracesDateProactif(sanitizedInput);

        // maintien de depassement_proactif_par_jour si nécessaire
        maintainDepassementProactifParJour(sanitizedInput);

        return new MaintienIndexEvtOutput().setSuccess(true);
    }

    /**
     * Mise en conformité de l'input 
    * 
     * @param input
     * @return le même objet conforme, ou null si impossible de le conformer.
     */
    private MaintienIndexEvtInput sanitizeInput(final MaintienIndexEvtInput input) {
        if (input.getEvts() == null) {
            return null;
        }

        if (input.getEvts().size() == 0) {
            return null;
        }

        if (input.getLt() == null) {
        	input.setLt(new Lt().setNoLt(input.getEvts().get(0).getNoLt()));
        }

        return input;
    }

    /**
     * Maintien de la table d'index des dépassements proactifs. Cas 1 : le colis
     * n'était pas en dépassement et le devient Cas 2 : le colis était en
     * dépassement mais sur une autre date de livraison contractuelle que celle
     * nouvellement calculée. Cas 3 : le colis était en dépassement et ne l'est
     * plus. Cas 4 : le colis était en dépassement et il est livré ou a été
     * présenté
     * 
     * @param input
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws NumberFormatException
     */
    private boolean maintainDepassementProactifParJour(final MaintienIndexEvtInput input) throws NumberFormatException,
            JsonParseException, JsonMappingException, ParseException, IOException {
        boolean estPresenteOuLivreEvt = false;

        for (final Evt evt : input.getEvts()) {
            if (EvtRules.estPresenteOuLivre(evt)) {
                estPresenteOuLivreEvt = true;
            }
        }

        // On évacue les colis livrés ou présentés
        if (!(estPresenteOuLivreEvt) && calculRetardNonNul(input)) {
            // Cas 1 : le colis n'était pas en dépassement et le devient
            if (!(etaitEnDepassementPrecedemment(input)) // pas de dépassement
                                                         // avant
                    && estEnDepassementActuellement(input)) { // dépassement
                                                              // maintenant

            	final UpdateDepassementProactifInput updateDepassProactifInput = new UpdateDepassementProactifInput()
                        .setDateLivraisonContractuelle(
                                DateRules.toTimestampDateWsCalculRetard(input.getResultatCalculRetard()
                                        .getResultRetard().getDateDeLivraisonPrevue()))
                        .setDateLivraisonPrevue(
                                DateRules.toTimestampDateWsCalculRetard(computeDateDeLivraisonEstimee(input)))
                        .setLt(input.getLt()).setNoLt(input.getLt().getNoLt());
                dao.updateDepassementProactifParJour(updateDepassProactifInput);
            }

            // Cas 2 : le colis était en dépassement mais sur une autre date de
            // livraison contractuelle que celle nouvellement calculée.
            else if (etaitEnDepassementPrecedemment(input) // déjà en
                                                           // dépassement
                    && estEnDepassementActuellement(input) // toujours en
                                                           // dépassement
                    && !DateRules.toDateSortable(
                            DateRules.toTimestampDateWsCalculRetard(input.getResultatCalculRetard().getResultRetard()
                                    .getDateDeLivraisonPrevue())).equals(getDateContractuellePrecedente(input)) // date
                                                                                                                // contractuelle
                                                                                                                // différente
                    && getDateContractuellePrecedente(input).length() == 10) { // et
                                                                               // date_contractuelle
                                                                               // correctement
                                                                               // settée
                                                                               // lors
                                                                               // de
                                                                               // la
                                                                               // précédente
                                                                               // détection

                // désactivation de l'ancienne ligne
            	final UpdateDepassementProactifInput updateDepassProactifSupprInput = new UpdateDepassementProactifInput()
                        .setDateLivraisonContractuelle(
                                DateRules.fromDateSortable(getDateContractuellePrecedente(input)))
                        .setDateLivraisonPrevue(
                                DateRules.toTimestampDateWsCalculRetard(computeDateDeLivraisonEstimee(input)))
                        .setLt(input.getLt()).setNoLt(input.getLt().getNoLt()).setDeleted("deleted");
                dao.updateDepassementProactifParJour(updateDepassProactifSupprInput);

                // Création de la nouvelle ligne
                final UpdateDepassementProactifInput updateDepassProactifAddInput = new UpdateDepassementProactifInput()
                        .setDateLivraisonContractuelle(
                                DateRules.toTimestampDateWsCalculRetard(input.getResultatCalculRetard()
                                        .getResultRetard().getDateDeLivraisonPrevue()))
                        .setDateLivraisonPrevue(
                                DateRules.toTimestampDateWsCalculRetard(computeDateDeLivraisonEstimee(input)))
                        .setLt(input.getLt()).setNoLt(input.getLt().getNoLt());
                dao.updateDepassementProactifParJour(updateDepassProactifAddInput);
            }

            // Cas 3 : le colis était en dépassement et ne l'est plus.
            else if (etaitEnDepassementPrecedemment(input) // déjà en
                                                           // dépassement
                    && nestPasEnDepassementActuellement(input) // plus en
                                                               // dépassement
                    && getDateContractuellePrecedente(input).length() == 10) { // et
                                                                               // date_contractuelle
                                                                               // correctement
                                                                               // settée
                                                                               // lors
                                                                               // de
                                                                               // la
                                                                               // précédente
                                                                               // détection

                // désactivation de l'ancienne ligne
            	final UpdateDepassementProactifInput updateDepassProactifSupprInput = new UpdateDepassementProactifInput()
                        .setDateLivraisonContractuelle(
                                DateRules.fromDateSortable(getDateContractuellePrecedente(input)))
                        .setDateLivraisonPrevue(
                                DateRules.toTimestampDateWsCalculRetard(computeDateDeLivraisonEstimee(input)))
                        .setLt(input.getLt()).setNoLt(input.getLt().getNoLt()).setDeleted("deleted");
                dao.updateDepassementProactifParJour(updateDepassProactifSupprInput);
            }
        }
        // Cas 4 : le colis était en dépassement et il est livré ou a été
        // présenté
        else if (etaitEnDepassementPrecedemment(input)) {
        	final UpdateDepassementProactifInput updateDepassProactifSupprInput = new UpdateDepassementProactifInput()
                    .setDateLivraisonContractuelle(DateRules.fromDateSortable(getDateContractuellePrecedente(input)))
                    .setDateLivraisonPrevue(null).setLt(input.getLt()).setNoLt(input.getLt().getNoLt())
                    .setDeleted("deleted");
            dao.updateDepassementProactifParJour(updateDepassProactifSupprInput);
        }

        return true;
    }

    @Override
    public String computeDateDeLivraisonEstimee(final MaintienIndexEvtInput input) {
        // TODO Auto-generated method stub
        if (input.getResultatCalculRetard().getCalculDateDeLivraisonEstimee() != null
                && input.getResultatCalculRetard().getCalculDateDeLivraisonEstimee().getDateDeLivraisonEstimee() != null
                && input.getResultatCalculRetard().getCalculDateDeLivraisonEstimee().getHeureMaxDeLivraisonEstimee() != null) {
            return input.getResultatCalculRetard().getCalculDateDeLivraisonEstimee().getDateDeLivraisonEstimee() // date
                    + " "
                    + input.getResultatCalculRetard().getCalculDateDeLivraisonEstimee().getHeureMaxDeLivraisonEstimee(); // heure
        }
        return null;
    }

    /**
     * retourne vrai si l'input indique que le colis était en dépassement
     * précédemment.
     * 
     * @param input
     * @return
     */
    private boolean etaitEnDepassementPrecedemment(final MaintienIndexEvtInput input) {
        return (input.getLt().getIdxDepassement() == null ? "" : input.getLt().getIdxDepassement()).endsWith("1");
    }

    /**
     * retourne vrai si l'input indique que le colis est en dépassement
     * actuellement. On se base sur le retour du CalculRetard pour le
     * déterminer.
     * 
     * @param input
     * @return
     */
    private boolean estEnDepassementActuellement(final MaintienIndexEvtInput input) {
        return input.getResultatCalculRetard().getAnalyse().getEnRetardDateEstimeeSupDateContractuelle() == 1;
    }

    /**
     * retourne vrai si l'input indique que le colis n'est pas en dépassement
     * actuellement. On se base sur le retour du CalculRetard pour le
     * déterminer.
     * 
     * @param input
     * @return
     */
    private boolean nestPasEnDepassementActuellement(final MaintienIndexEvtInput input) {
        return input.getResultatCalculRetard().getAnalyse().getEnRetardDateEstimeeSupDateContractuelle() == 0;
    }

    /**
     * Recupère la date de livraison contractuelle précédente depuis
     * idxDepassement.
     * 
     * @param input
     * @return
     */
    private String getDateContractuellePrecedente(final MaintienIndexEvtInput input) {
        if (input.getLt().getIdxDepassement() != null) {
            return input.getLt().getIdxDepassement().split("__")[0];
        }
        return "";
    }

    private Boolean calculRetardNonNul(final MaintienIndexEvtInput input) {
        if (input.getResultatCalculRetard() == null) return false;
        if (input.getResultatCalculRetard().getAnalyse() == null) return false;
        if (input.getResultatCalculRetard().getResultRetard() == null) return false;
        return true;
    }
}
