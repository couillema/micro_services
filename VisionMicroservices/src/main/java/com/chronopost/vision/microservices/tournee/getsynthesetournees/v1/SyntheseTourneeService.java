package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.chronopost.vision.model.getsynthesetournees.v1.InfoTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.SyntheseTourneeQuantite;

/**
 * 
 * Interface utilisée pour récupérer les infos relatives aux tournées
 * 
 * @author jcbontemps
 *
 */
interface SyntheseTourneeService {
    // Codes événement
    static final String EVT_D = "D";
    static final String EVT_TA = "TA";
    static final String EVT_TE = "TE";
    static final String EVT_PC = "PC";
    static final String EVT_PE = "PE";

    // Info supp.
    static final String ID_AVIS_PASSAGE = "ID_AVIS_PASSAGE";
    static final String CONSIGNE = "CONSIGNE";
    static final String ID_CONSIGNE = "ID_CONSIGNE";

    // Transco
    static final String DIFFUSION_VISION = "DiffusionVision";
    static final String PARAMETRE_MICROSERVICES = "parametre_microservices";

    enum ParametresMicroservices {
        DEPASSEMENT_MAX_ETA,
        DEPASSEMENT_MIN_ETA,
        EVT_PRESENTATION_POSITIVE,
        EVT_PRESENTATION_NEGATIVE,
        EVT_PRESENTATION_DOMICILE_POSITIVE,
        EVT_PRESENTATION_DOMICILE_NEGATIVE,
        EVT_PRESENTATION_DOMICILE,
        EVT_MISE_A_DISPOSITION_BUREAU,
        EVT_D_PLUS("evt_Dplus"),
        EVT_ECHEC_LIVRAISON;

        private String parametre;

        private ParametresMicroservices() {
            this.parametre = this.name().toLowerCase();
        }

        private ParametresMicroservices(String parametre) {
            this.parametre = parametre;
        }

        @Override
        public String toString() {
            return this.parametre;
        }
    }

    /**
     * retourne la synthèse de chaque tournée
     * 
     * @param idTournee
     *            la liste des identifiants de tournée
     * @return une map avec pour clé l'id de tournée et valeur des indicateurs
     *         quant à la tournée
     */
    Map<String, SyntheseTourneeQuantite> getSyntheseTourneeQuantite(final List<String> idTournee);

    /**
     * retourne la synthèse de l'activité de chaque point d'une tournée
     * 
     * @param idTournee
     *            l'identifiant de la tournée
     * @return la liste des activités de chaque point de cette tournée
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    List<PointTournee> getSyntheseTourneeActivite(final String idTournee) throws InterruptedException, ExecutionException;

    /**
     * retourne la synthèse de la tournée et de de l'activité de chaque point
     * d'une tournée
     * 
     * @param idTournee
     *            l'identifiant de la tournée
     * @return la liste des activités de chaque point et les indicateurs de
     *         cette tournée
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    InfoTournee getSyntheseTourneeActiviteEtQuantite(final String idTournee) throws InterruptedException, ExecutionException;

    /**
     * Le dépassement maximum autorisé pour un ETA est défini par le paramètre
     * depassement_max_eta Le dépassement minimum autorisé pour un ETA est
     * défini par le paramètre depassement_min_eta Pour chaque PointTournee si
     * diffETA> depassement_max_eta ou diffETA<depassement_min_eta alors ajouter
     * au PointTournee.anoamlie la String “HDETA”
     * 
     * @param tournee
     * @return la liste des points enrichie d'anomalies
     */
    List<PointTournee> calculAnomaliesTournee(final Tournee tournee);

    /**
     * Retourne une map <String, InfoTournee>
     * Appelle la méthode getSyntheseTourneeActiviteEtQuantite pour chaque id donné dans la liste
     * Retourne une InfoTournee null si une erreur apparaît durant la méthode
     * 
     * @param tourneeIds id de tournée dont on veut la synthése
     * @return une map <id de la tournée, InfoTournee>
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
	Map<String, InfoTournee> getSyntheseTourneeActivitesEtQuantites(final List<String> tourneeIds);

	void declareAppelMS();
	void declareFailMS();
}
