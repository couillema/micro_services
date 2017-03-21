package com.chronopost.vision.microservices.getsyntheseagence.v1;

import static com.chronopost.vision.model.rules.SpecifsColisRules.estUnColisDeloc;
import static com.chronopost.vision.model.rules.SpecifsColisRules.extractCodeEvtFromEtape;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.DISPERSION;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.EXCLUSION;
import static com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis.PREPA_DISTRI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.cachemanager.parametre.Parametre;
import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.ColisSpecByAgence;
import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.CollectionIndicateursDispersion;
import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.CollectionIndicateursDispersionPassee;
import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.CollectionIndicateursDistribution;
import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.CollectionsDispersion;
import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.CollectionsDispersionPassee;
import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.CollectionsDistribution;
import com.chronopost.vision.microservices.getsyntheseagence.v1.collections.SyntheseAgenceCollectionsUtil;
import com.chronopost.vision.model.getsyntheseagence.v1.ECodeIndicateur;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseColis;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseColisEtListeValeurs;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseListeValeurs;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDispersionQuantite;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDispersionQuantitePassee;
import com.chronopost.vision.model.getsyntheseagence.v1.SyntheseDistributionQuantite;
import com.chronopost.vision.model.rules.SpecifsColisRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public enum SyntheseAgenceServiceImpl implements ISyntheseAgenceService {
	INSTANCE;

	/*
	 * Liste des evts qui ne portent pas l'étape dispersion pour
	 * SupervisionAgence
	 */
	private static final Set<String> evtNotDispersion = Sets.newHashSet("IA", "IS", "RA", "RT");
	private final static String TYPE_RPTSDTA = "RPTSDTA";
	private final static Set<String> INDICATEURS_ALERTES = Sets.newHashSet(
			ECodeIndicateur.EN_ALERTE_ACTIVE.getCode(), ECodeIndicateur.EN_ALERTE_ACTIVE_DELOC.getCode(),
			ECodeIndicateur.EN_ALERTE_ACTIVE_PASSEE.getCode(),
			ECodeIndicateur.EN_ALERTE_ACTIVE_DELOC_PASSEE.getCode());

	private ISyntheseAgenceDao dao;
	private CacheManager<Parametre> cacheParametre;

	public SyntheseAgenceServiceImpl setRefentielParametre(final CacheManager<Parametre> cacheParametre) {
		this.cacheParametre = cacheParametre;
		return this;
	}

	@Override
	public void setDao(final ISyntheseAgenceDao dao) {
		this.dao = dao;
	}

	@Override
	public SyntheseDispersionQuantite getSyntheseDispersionQuantite(final String posteComptable,
			final String dateAppel) {
		return getSyntheseDispersionQuantite(posteComptable, dateAppel, getDateDebut(0), getDateFin(0));
	}

	@Override
	public SyntheseDispersionQuantitePassee getSyntheseDispersionQuantitePassee(final String posteComptable,
			final Integer nbJours) {
		return getSyntheseDispersionQuantitePasse(posteComptable, getDateDebut(nbJours), getDateFin(nbJours));
	}

	@Override
	public SyntheseDistributionQuantite getSyntheseDistributionQuantite(final String posteComptable,
			final String dateAppel) {
		return getSyntheseDistributionQuantite(posteComptable, dateAppel, getDateDebut(0), getDateFin(0));
	}

	/**
	 * Renvoie la liste des colis, le nombre total des colis et la liste des
	 * valeurs pour un indicateur
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param indicateur
	 *            : le code de l'indicateur
	 * @param limit
	 *            : le nombre de colis attendu
	 * @param nbjours
	 *            : Nombre de jours
	 * @param dateAppel
	 *            : La date et l'heure locale d'appel du MS au format ISO 8601
	 * @return Un objet de type
	 *         <code>SyntheseColisEtListeValeurs</code> qui contient
	 *         la liste des colis, les listes des valeurs et le nombre total des
	 *         colis
	 * 
	 * @author bjbari
	 */
	@Override
	public SyntheseColisEtListeValeurs getSyntheseDetailIndicateur(final String posteComptable,
			final String codeIndicateur, final Integer limit, final String dateAppel, final Integer nbJours) {
		if (nbJours != null && nbJours != 0
				&& CollectionIndicateursDispersionPassee.INDICATEURS_JOURS_PRECEDENTS.contains(codeIndicateur)) {
			return getSyntheseDispersionDetailIndicateurPassee(posteComptable, codeIndicateur, limit,
					getDateDebut(nbJours), getDateFin(nbJours));
		} else if ((nbJours == null || nbJours.equals(0))
				&& CollectionIndicateursDispersion.INDICATEURS_JOUR.contains(codeIndicateur)) {
			return getSyntheseDispersionDetailIndicateur(posteComptable, codeIndicateur, limit, dateAppel,
					getDateDebut(nbJours), getDateFin(nbJours));
		} else if ((nbJours == null || nbJours.equals(0))
				&& CollectionIndicateursDistribution.INDICATEURS_JOUR.contains(codeIndicateur)) {
			return getSyntheseDistributionDetailIndicateur(posteComptable, codeIndicateur, limit, dateAppel,
					getDateDebut(nbJours), getDateFin(nbJours));	
		} else
			return new SyntheseColisEtListeValeurs();
	}

	/**
	 * Renvoie la liste des colis, le nombre total des colis et la liste des
	 * valeurs pour les indicateurs distri du jour
	 * 
	 * @param posteComptable : le poste comptable de l’agence
	 * @param codeIndicateur : le code de l'indicateur
	 * @param limit : le nombre de colis attendu
	 * @param dateAppel : La date et l'heure locale d'appel du MS au format ISO 8601
	 * @param fromDate : La date début
	 * @param toDate : la date de fin
	 * @return Un objet de type
	 *         <code>SyntheseColisEtListeValeurs</code> qui contient
	 *         la liste des colis, les listes des valeurs et le nombre total des
	 *         colis
	 */
	
	private SyntheseColisEtListeValeurs getSyntheseDistributionDetailIndicateur(String posteComptable,
			String codeIndicateur, Integer limit, String dateAppel, Date fromDate, Date toDate) {
		
		/* Récupération de tous les colis et leur specif colis ayant eu
		 * une saisie ou à saisir sur l'agence sur la période
		 */
		final ColisSpecByAgence dispersionAgence = dao.getDistributionByAgencePeriode(posteComptable, fromDate, toDate);
		/* Constitution des collections de distribution */
		final CollectionsDistribution collectionsDistribution = genereCollectionsDistribution(
				dispersionAgence.getColisSaisis(), fromDate, toDate, dateAppel, posteComptable);
		
		// Extraire la liste de colis des collections à partir du code indicateur
		final Set<String> listeIndicateur = SyntheseAgenceCollectionsUtil
				.getCollectionDistriFromIndicateurCode(collectionsDistribution, codeIndicateur);

		return genereDetailColisAndFiltersValuesListsFromCollection(listeIndicateur, dispersionAgence, fromDate, toDate,
				limit, codeIndicateur);
	}
	/**
	 * Renvoie la liste des colis, le nombre total des colis et la liste des
	 * valeurs pour un indicateur des jours précédents
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param codeIndicateur
	 *            : le code de l'indicateur
	 * @param limit
	 *            : le nombre de colis attendu
	 * @param dateAppel
	 *            : La date et l'heure locale d'appel du MS au format ISO 8601
	 * @param fromDate
	 *            : La date début
	 * @param toDate
	 *            : la date de fin
	 * @return Un objet de type
	 *         <code>SyntheseColisEtListeValeurs</code> qui contient
	 *         la liste des colis, les listes des valeurs et le nombre total des
	 *         colis
	 */
	private SyntheseColisEtListeValeurs getSyntheseDispersionDetailIndicateur(final String posteComptable,
			final String codeIndicateur, final Integer limit, final String dateAppel, final Date fromDate,
			final Date toDate) {
		// Récupération de tous les colis et leur specif colis ayant eu une
		// saisie ou à saisir sur l'agence sur la période
		final ColisSpecByAgence dispersionAgence = dao.getDispersionByAgencePeriode(posteComptable, fromDate, toDate);
		// Constitution des collections de dispersion
		final CollectionsDispersion collectionDispersion = genereCollectionsDispersion(
				dispersionAgence.getColisSaisis(), dispersionAgence.getColisASaisir(), fromDate, toDate, dateAppel,
				posteComptable);
		// Extraire la liste de colis des collections à partir du code
		// indicateur
		final Set<String> listeIndicateur = SyntheseAgenceCollectionsUtil
				.getCollectionFromIndicateurCode(collectionDispersion, codeIndicateur, dateAppel);

		return genereDetailColisAndFiltersValuesListsFromCollection(listeIndicateur, dispersionAgence, fromDate, toDate,
				limit, codeIndicateur);
	}

	/**
	 * Renvoie la liste des colis, le nombre total des colis et la liste des
	 * valeurs pour un indicateur des jours précédents
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param codeIndicateur
	 *            : le code de l'indicateur
	 * @param limit
	 *            : le nombre de colis attendu
	 * @param nbJours
	 *            : Nombre de jours
	 * @param fromDate
	 *            : La date début
	 * @param toDate
	 *            : la date de fin
	 * @return Un objet de type
	 *         <code>SyntheseColisEtListeValeurs</code> qui contient
	 *         la liste des colis, les listes des valeurs et le nombre total des
	 *         colis
	 * 
	 * @author bjbari
	 */
	private SyntheseColisEtListeValeurs getSyntheseDispersionDetailIndicateurPassee(
			final String posteComptable, final String codeIndicateur, final Integer limit, final Date fromDate,
			final Date toDate) {
		/*
		 * Récupération de tous les colis en cours sur l'agence sur la période
		 */
		final ColisSpecByAgence dispersionAgence = dao.getDispersionByAgencePeriodePassee(posteComptable, fromDate,
				toDate);
		// Constitution des collections de dispersion
		final CollectionsDispersionPassee collectionDispersion = genereCollectionsDispersionPassee(
				dispersionAgence.getColisRestantTg2(), fromDate, toDate, posteComptable);
		// Extraire la liste de colis des collections à partir du code
		// indicateur
		final Set<String> listeIndicateur = SyntheseAgenceCollectionsUtil
				.getCollectionFromIndicateurCodePassee(collectionDispersion, codeIndicateur);

		return genereDetailColisAndFiltersValuesListsFromCollectionPassee(listeIndicateur, dispersionAgence, fromDate,
				toDate, limit, codeIndicateur);
	}

	@Override
	public SyntheseColisEtListeValeurs getSyntheseDetailIndicateurRaffine(
			final SyntheseListeValeurs criteres, final String posteComptable, final String codeIndicateur,
			final Integer limit, final String dateAppel, final Integer nbJours) {
		final ImmutableList<SyntheseColis> listeColisInilale = getSyntheseDetailIndicateur(
				posteComptable, codeIndicateur, null, dateAppel, nbJours).getColis();

		return criteres.raffine(listeColisInilale, limit);
	}

	/**
	 * Renvoie une map qui regroupe les colis par code dispersion et selon les
	 * précocités
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param codeIndicateur
	 *            : le code de l'indicateur
	 * @param dateAppel
	 *            : La date et l'heure locale d'appel du MS au format ISO 8601
	 * @return
	 * 
	 * @author bjbari
	 */
	@Override
	public Map<String, Map<String, Integer>> getSyntheseDispersionGroupByCodeDispersion(final String posteComptable,
			final String codeIndicateur, final String dateAppel, final Integer nbJours) {
		final ImmutableList<SyntheseColis> listeColis = getSyntheseDetailIndicateur(posteComptable,
				codeIndicateur, null, dateAppel, nbJours).getColis();
		final Map<String, Map<String, Integer>> map = new HashMap<>();
		final Set<String> listePrecocite = SpecifsColisRules.getAllPrecocites();
		final String nonRenseigne = "NONRENSEIGNE";
		// Boucle sur les colis
		for (final SyntheseColis syntheseColis : listeColis) {
			final String codeDispersion = syntheseColis.getCodeDispersion();

			// Calcul précocité
			String precociteLoc = syntheseColis.getPrecocite();
			if (!listePrecocite.contains(precociteLoc) || precociteLoc == null)
				precociteLoc = nonRenseigne;

			if (map.containsKey(codeDispersion)) {
				// Map ( key = Précocité , valeur : Nombre de colis par code
				// dispersion & précocité)
				final Map<String, Integer> precociteNombreColis = map.get(codeDispersion);
				// Nombre de colis par précocité
				final int nbrColisParPrecocite = precociteNombreColis.get(precociteLoc) != null
						? precociteNombreColis.get(precociteLoc) : 0;
				// Incrémenter le nombre de colis pour la précocité courante
				precociteNombreColis.put(precociteLoc, nbrColisParPrecocite + 1);
			} else {
				final String codeDispersionLoc = codeDispersion != null ? codeDispersion : nonRenseigne;
				final Map<String, Integer> precociteNombreColis = new HashMap<>();
				precociteNombreColis.put(precociteLoc, 1);
				map.put(codeDispersionLoc, precociteNombreColis);
			}
		}
		return map;
	}

	/**
	 * Récupérer les listes de colis saisis et de colis a saisir (sous forme
	 * colis / specifsColis) pour la Nieme journées précédentes
	 * (getDispersionByAgencePeriode) <br>
	 * Générer les collections dispersion quantite Calculer la synthèse
	 * dispersion retourner la synthèse dispersion
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param dateAppel:
	 *            La date et l'heure locale d'appel du MS au format ISO 8601
	 * @param nbJours
	 *            : Nombre de jours
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	private SyntheseDispersionQuantite getSyntheseDispersionQuantite(final String posteComptable,
			final String dateAppel, final Date fromDate, final Date toDate) {
		/*
		 * Récupération de tous les colis et leur specif colis ayant eu
		 * unesaisie ou à saisir sur l'agence sur la période
		 */
		final ColisSpecByAgence dispersionAgence = dao.getDispersionByAgencePeriode(posteComptable, fromDate, toDate);
		/* Constitution des collections de dispersion */
		final CollectionsDispersion collectionDispersion = genereCollectionsDispersion(
				dispersionAgence.getColisSaisis(), dispersionAgence.getColisASaisir(), fromDate, toDate, dateAppel,
				posteComptable);
		final CollectionIndicateursDispersion indicateursDispersion = SyntheseAgenceCollectionsUtil
				.calculCollectionsIndicateurDispersion(collectionDispersion, dateAppel);
		/* Calcul de l'objet de synthese à partir des collections */
		return SyntheseAgenceCollectionsUtil.calculSyntheseDispersion(indicateursDispersion);
	}

	/**
	 * Récupérer les listes de colis saisis et de colis a saisir (sous forme
	 * colis / specifsColis) pour la Nieme journées précédentes
	 * (getDispersionByAgencePeriode) <br>
	 * pour les jours précédents Générer les collections dispersion quantite
	 * Calculer la synthèse dispersion retourner la synthèse dispersion
	 * 
	 * @param posteComptable:
	 *            le poste comptable de l’agence
	 * @param fromDate
	 * @param toDate
	 * @return un objet de type <code>SyntheseDispersionQuantitePassee</code>
	 * 
	 * @author bjbari
	 */
	private SyntheseDispersionQuantitePassee getSyntheseDispersionQuantitePasse(final String posteComptable,
			final Date fromDate, final Date toDate) {
		/*
		 * Récupération de tous les colis en cours sur l'agence sur la période
		 */
		final ColisSpecByAgence dispersionAgence = dao.getDispersionByAgencePeriodePassee(posteComptable, fromDate,
				toDate);
		/* Constitution des collections de dispersion */
		final CollectionsDispersionPassee collectionDispersionPassee = genereCollectionsDispersionPassee(
				dispersionAgence.getColisRestantTg2(), fromDate, toDate, posteComptable);
		final CollectionIndicateursDispersionPassee indicateursDispersionPassee = SyntheseAgenceCollectionsUtil
				.calculCollectionsIndicateurDispersionPassee(collectionDispersionPassee);
		/* Calcul de l'objet de synthese à partir des collections */
		return SyntheseAgenceCollectionsUtil.calculSyntheseDispersionPassee(indicateursDispersionPassee);
	}

	/**
	 * Récupérer les listes de colis saisis pour la Nieme journées précédentes
	 * (getDistributionByAgencePeriode) <br>
	 * Générer les collections distribution quantite Calculer la synthèse
	 * distribution retourner la synthèse distribution
	 * 
	 * @param posteComptable
	 *            : le poste comptable de l’agence
	 * @param dateAppel:
	 *            La date et l'heure locale d'appel du MS au format ISO 8601
	 * @param nbJours
	 *            : Nombre de jours
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	private SyntheseDistributionQuantite getSyntheseDistributionQuantite(final String posteComptable,
			final String dateAppel, final Date fromDate, final Date toDate) {
		/*
		 * Récupération de tous les colis et leur specif colis ayant eu
		 * une saisie ou à saisir sur l'agence sur la période
		 */
		final ColisSpecByAgence distributionAgence = dao.getDistributionByAgencePeriode(posteComptable, fromDate, toDate);
		/* Constitution des collections de distribution */
		final CollectionsDistribution collectionsDistribution = genereCollectionsDistribution(
				distributionAgence.getColisSaisis(), fromDate, toDate, dateAppel, posteComptable);
		final CollectionIndicateursDistribution indicateursDistribution = SyntheseAgenceCollectionsUtil
				.calculCollectionsIndicateurDistribution(collectionsDistribution, dateAppel);
		/* Calcul de l'objet de synthese à partir des collections */
		return SyntheseAgenceCollectionsUtil.calculSyntheseDistribution(indicateursDistribution);
	}

	/**
	 * Constitue et retourne une liste de colisDetail et la liste des valeurs à
	 * partir de la liste de noLt fournie pour les indicateurs jours précédents
	 * 
	 * @param collection
	 *            : collection contenant la liste des numéros lt à considérer.
	 * @param dispersionAgence
	 * @param toDate
	 * @param limit
	 * @return
	 */
	private SyntheseColisEtListeValeurs genereDetailColisAndFiltersValuesListsFromCollectionPassee(
			final Set<String> collection, final ColisSpecByAgence dispersionAgence, final Date fromDate,
			final Date toDate, final Integer limit, final String codeIndicateur) {
		final Map<String, SpecifsColis> colisSpecifs = new HashMap<>();
		colisSpecifs.putAll(dispersionAgence.getColisRestantTg2());
		return genereDetailColisAndFiltersValuesListsFromCollection(collection, colisSpecifs, fromDate, toDate, limit,
				codeIndicateur);
	}

	/**
	 * Constitue et retourne une liste de colisDetail et la liste des valeurs à
	 * partir de la liste de noLt fournie pour les indicateurs du jour ( dispersion ou distribution)
	 * 
	 * @param collection
	 * @param colisSpecByAgence
	 * @param toDate
	 * @param limit
	 * @return
	 */
	private SyntheseColisEtListeValeurs genereDetailColisAndFiltersValuesListsFromCollection(
			final Set<String> collection, final ColisSpecByAgence colisSpecByAgence, final Date fromDate,
			final Date toDate, final Integer limit, final String codeIndicateur) {
		final Map<String, SpecifsColis> colisSpecifs = new HashMap<>();
		colisSpecifs.putAll(colisSpecByAgence.getColisASaisir());
		colisSpecifs.putAll(colisSpecByAgence.getColisSaisis());
		return genereDetailColisAndFiltersValuesListsFromCollection(collection, colisSpecifs, fromDate, toDate, limit,
				codeIndicateur);
	}

	/**
	 * Constitue et retourne une liste de colisDetail et la liste des valeurs à
	 * partir de la liste de noLt fournie
	 * 
	 * 
	 * @param collection
	 *            : collection contenant la liste des numéros lt à considérer.
	 * @param colisSpecifs
	 * @param toDate
	 *            : date supérieur de la période regardée
	 * @return une liste de colisDetail à partir de la liste de noLt fournie +
	 *         Listes des valeurs des filtres
	 *
	 * 
	 * @author LGY
	 */
	private SyntheseColisEtListeValeurs genereDetailColisAndFiltersValuesListsFromCollection(
			final Set<String> collection, final Map<String, SpecifsColis> colisSpecifs, final Date fromDate,
			final Date toDate, final Integer limit, final String codeIndicateur) {
		final List<SyntheseColis> listSytheseColis = new ArrayList<>();
		final SyntheseColisEtListeValeurs syntheseColisEtListeValeurs = new SyntheseColisEtListeValeurs();
		final SyntheseListeValeurs listeValeurs = new SyntheseListeValeurs();

		if (collection != null)
			for (final String noLt : collection) {
				final SyntheseColis colisDetail = new SyntheseColis();
				colisDetail.setNoLt(noLt);

				/* Récupération du SpecifColis du colis */
				final SpecifsColis specifColis = colisSpecifs.get(noLt);

				/* On récupère la liste des étapes */
				final SortedMap<Date, String> etapes = specifColis.getEtapes();

				if (!etapes.isEmpty()) {
					/*
					 * On extrait la derniere étape de prépa-distri / livraison
					 * et la derniere etape du colis
					 */
					final Map.Entry<Date, String> dernEtapeDispersion = SpecifsColisRules.getLastEtapeFromType(etapes,
							EEtapesColis.DISPERSION);
					final Map.Entry<Date, String> dernEtapePrepaDistriLivraison = SpecifsColisRules
							.getLastEtapeFromType(etapes, EEtapesColis.PREPA_DISTRI, EEtapesColis.LIVRAISON);
					final Map.Entry<Date, String> dernEtapeColis = SpecifsColisRules.getLastEtape(etapes);

					/* Affectation du code postal RG-MSGetSyntAgence-013 */
					final String codePostal = specifColis.getCodePostalDestinataire();
					colisDetail.setCodePostal(codePostal);
					if (!StringUtils.isBlank(codePostal))
						listeValeurs.addToCodePostal(codePostal);

					/*
					 * Affectation du Numéro de contrat RG-MSGetSyntAgence-015
					 */
					final String noContrat = specifColis.getNoContrat();
					colisDetail.setIdContrat(noContrat);
					if (!StringUtils.isBlank(noContrat))
						listeValeurs.addToNumeroContrat(noContrat);

					/* Affectation du Produit */
					colisDetail.setCodeProduit(specifColis.getCodeProduit());

					/* Affectation du code tournée RG-MSGetSyntAgence-006 */
					if (dernEtapePrepaDistriLivraison != null) {
						final String codeTournee = SpecifsColisRules
								.extractDispersionFromEtape(dernEtapePrepaDistriLivraison.getValue());
						if (!"LAPOSTE".equals(codeTournee) && !StringUtils.isBlank(codeTournee)) {
							colisDetail.setCodeTournee(codeTournee);
							listeValeurs.addToCodeTournee(codeTournee);
						}
					}

					/* Affectation du code dispersion RG-MSGetSyntAgence-005 */
					if (dernEtapeDispersion != null) {
						final String codeDispersion = SpecifsColisRules
								.extractDispersionFromEtape(dernEtapeDispersion.getValue());
						if (!StringUtils.isBlank(codeDispersion)) {
							colisDetail.setCodeDispersion(codeDispersion);
							listeValeurs.addToCodeDispersion(codeDispersion);

							if (codeDispersion.length() > 4)
								listeValeurs.addToCodeGrappe(codeDispersion.substring(2, 4));
						}

					}
					/* Affectation de la précocité RG-MSGetSyntAgence-011 */

					final String precocite = SpecifsColisRules.getPrecocite(specifColis, new Date());
					if (!StringUtils.isBlank(precocite)) {
						colisDetail.setPrecocite(precocite);
						listeValeurs.addToPrecocite(precocite);
					}

					/*
					 * Affectation des caractéristiques RG-MSGetSyntAgence-007
					 */
					final Set<String> caracteristiques = SpecifsColisRules.getCaracteristiqueColis(specifColis, toDate);
					if (caracteristiques != null && !caracteristiques.isEmpty()) {
						colisDetail.setCaracteristiques(caracteristiques);
						listeValeurs.addAllToQualificatif(caracteristiques);
					}

					/*
					 * Affectation de la responsabilité RG-MSGetSyntAgence-008
					 */
					final String responsabilite = SpecifsColisRules.getResponsabiliteColis(specifColis);

					if (!StringUtils.isBlank(responsabilite)) {
						colisDetail.setReponsabilite(responsabilite);
						listeValeurs.addToResponsabilite(responsabilite);
					}

					/* Affectation du lieu d'instance RG-MSGetSyntAgence-009 */

					colisDetail.setLieuDInstance(SpecifsColisRules.getLieuInstanceColis(specifColis));

					if (dernEtapeColis != null) {
						/*
						 * Affectation du dernier evt significatif et de sa date
						 * RG-MSGetSyntAgence-012
						 */
						colisDetail.setDernEvtSignificatif(extractCodeEvtFromEtape(dernEtapeColis.getValue()));
						colisDetail.setDateDernEvtSignificatif(dernEtapeColis.getKey());

						/*
						 * Affection du flag de propagation
						 * RG-MSGetSyntAgence-014
						 */
						if ("P".equals(extractCodeEvtFromEtape(dernEtapeColis.getValue()))) {
							colisDetail.setIsPropage(true);
							listeValeurs.addToPropagation("Oui");
						} else {
							listeValeurs.addToPropagation("Non");
						}
						/*
						 * Affection de l'outil de saisie de propagation
						 * RG-MSGetSyntAgence-016
						 */
						final String posteSaisie = SpecifsColisRules
								.extractOutilSaisieFromEtape(dernEtapeColis.getValue());
						if (!StringUtils.isBlank(posteSaisie)) {
							colisDetail.setOutilSaisie(posteSaisie);
							listeValeurs.addToPosteSaisie(posteSaisie);
						}
						/*
						 * Affection du code service RG-MSGetSyntAgence-026
						 */
						final String codeService = SpecifsColisRules.getService(specifColis, toDate);
						if (!StringUtils.isBlank(codeService)) {
							colisDetail.setCodeService(codeService);
							listeValeurs.addToCodeService(codeService);
						}
					}
				}

				/* Alertes */
				if (INDICATEURS_ALERTES.contains(codeIndicateur)) {
					colisDetail.setDateAlerte(
							SpecifsColisRules.getLastDateAlertesInPeriode(specifColis, fromDate, toDate, TYPE_RPTSDTA));
					colisDetail.setTypeAlerte(TYPE_RPTSDTA);
				}

				listSytheseColis.add(colisDetail);
			}

		syntheseColisEtListeValeurs.setTotalColis(listSytheseColis.size());
		syntheseColisEtListeValeurs
				.setColis(SyntheseAgenceCollectionsUtil.getLimitedList(listSytheseColis, limit));
		syntheseColisEtListeValeurs.setListeValeurs(listeValeurs);

		return syntheseColisEtListeValeurs;
	}

	/**
	 * Générer les collection en fonctions des règles de gestion liées à chaque
	 * collection<br>
	 * Retourner l’objet CollectionDispersion
	 * 
	 * @param colisSaisis:
	 *            la liste de colis saisis
	 * @param colisASaisir
	 *            : la liste de colis à saisir
	 * @param fromDate
	 * @param toDate
	 * @param dateAppel
	 *            : La date locale d'appel du MS au format ISO 8601
	 * @param posteComptable
	 *            : id de l'agence recherchée
	 * @return un objet <code>CollectionDispersion</code>
	 */
	private CollectionsDispersion genereCollectionsDispersion(final Map<String, SpecifsColis> colisSaisis,
			final Map<String, SpecifsColis> colisASaisir, final Date fromDate, final Date toDate, String dateAppel,
			final String posteComptable) {
		final CollectionsDispersion collections = new CollectionsDispersion();
		collections.setColisSaisis(colisSaisis.keySet());
		collections.setColisASaisir(colisASaisir.keySet());

		// Si dateAppel est null, on met la date/heure actuelle
		if (StringUtils.isBlank(dateAppel)) {
			dateAppel = new DateTime().toString();
		}

		for (final SpecifsColis specifColis : colisSaisis.values()) {
			final String noLt = specifColis.getNoLt();

			// On extrait les étapes de la période (on filtre les étapes non
			// supervisionagence) RG-MSGetSyntAgence-019
			final SortedMap<Date, String> etapesInPeriode = filtreEtapesPourSupervisionAgence(
					SpecifsColisRules.getEtapesInPeriode(specifColis, fromDate, toDate));
			// Pour les étapes DISPERSE, PREPA_DISTRI et EXCLUSION, on exclut
			// les evt qui viennent pas de l'agence recherchée
			final SortedMap<Date, String> etapesInPeriodeForAgence = filtreEtapesSurAgence(etapesInPeriode,
					posteComptable);

			final Map.Entry<Date, String> exclusion = SpecifsColisRules.getLastEtapeFromType(etapesInPeriodeForAgence,
					EEtapesColis.EXCLUSION);
			final Map.Entry<Date, String> dispersion = SpecifsColisRules.getLastEtapeFromType(etapesInPeriodeForAgence,
					EEtapesColis.DISPERSION);
			final Map.Entry<Date, String> prepaDistri = SpecifsColisRules.getLastEtapeFromType(etapesInPeriodeForAgence,
					EEtapesColis.PREPA_DISTRI);
			final Map.Entry<Date, String> livraison = SpecifsColisRules.getLastEtapeFromType(etapesInPeriode,
					EEtapesColis.LIVRAISON);
			final Map.Entry<Date, String> acquittement = SpecifsColisRules.getLastEtapeFromType(etapesInPeriode,
					EEtapesColis.ACQUITTEMENT_LIVRAISON);
			final Map.Entry<Date, String> incident = SpecifsColisRules.getLastEtapeFromType(etapesInPeriode,
					EEtapesColis.INCIDENT);
			final Map.Entry<Date, String> perdu = SpecifsColisRules.getLastEtapeFromType(etapesInPeriode,
					EEtapesColis.PERDU);
			final Map.Entry<Date, String> derniereEtape = SpecifsColisRules.getLastEtape(etapesInPeriode);

			// Si étape DISPERSION sur la période
			if (dispersion != null) {
				if (estUnColisDeloc(dispersion.getValue())) {
					collections.addToDispersesDeloc(noLt);
				} else {
					collections.addToDisperses(noLt);
				}
				// Si dernière étape du colis est une étape de dispersion
				if (derniereEtape != null && derniereEtape.getKey().equals(dispersion.getKey())
						&& derniereEtape.getValue().equals(dispersion.getValue())) {
					// Deloc
					if (estUnColisDeloc(dispersion.getValue())) {
						collections.addToEnDispersionDeloc(noLt);
					} else {
						collections.addToEnDispersion(noLt);
					}

					/** Alertes */
					// Date de la dernière alerte
					final Date dateAlerte = SpecifsColisRules.getLastDateAlertesInPeriode(specifColis, fromDate, toDate,
							TYPE_RPTSDTA);
					// RG-MSGetSyntAgence-030
					if (dateAlerte != null && dispersion.getKey().before(dateAlerte)) {
						if (estUnColisDeloc(dispersion.getValue()))
							collections.addToEnAlerteActiveDeloc(noLt);
						else
							collections.addToEnAlerteActive(noLt);
					}
					/** Fin Alertes */
					
					// Et si la dernière étape sur la période est une étape Dispersion Poste
					if (derniereEtape != null && SpecifsColisRules.isDisperseVersLaPoste(derniereEtape.getValue()))
						collections.addToEnDispersionPoste(noLt);
				}

				// et que seuls des étapes LAPOSTE suivent (code transport =
				// LAPOSTE)"
				if (SpecifsColisRules.isDisperseVersLaPoste(dispersion.getValue())
						&& SpecifsColisRules.isDispersionLaPoste(etapesInPeriode, fromDate, toDate)) {
					collections.addToDispersesPoste(noLt);
				}
			}

			// Si étape PREPA_DISTRI sur la période
			if (prepaDistri != null) {
				if (estUnColisDeloc(prepaDistri.getValue())) {
					collections.addToPreparesDeloc(noLt);
				} else {
					collections.addToPrepares(noLt);
				}
				if (SpecifsColisRules.isLaPosteResponsable(prepaDistri.getValue())) {
					collections.addToPreparesPoste(noLt);
				}
			}

			// Si étape LIVRAISON sur la période
			if (livraison != null) {
				collections.addToDistribues(noLt);
				if (SpecifsColisRules.isLaPosteResponsable(livraison.getValue())) {
					collections.addToDistribuesPoste(noLt);
				}
			}

			// Si étape ACKLIV|XX|LAPOSTE sur la période
			if (acquittement != null && SpecifsColisRules.isLaPosteResponsable(acquittement.getValue())) {
				collections.addToAcquittementsPoste(noLt);
			}

			// Si la dernière étape est une étape d'exclusion
			if (derniereEtape != null && exclusion != null && derniereEtape.getKey().equals(exclusion.getKey())
					&& derniereEtape.getValue().equals(exclusion.getValue())) {
				// Récupération du nb de jours d'exclusion pour déterminer le
				// type d'exclusion
				final int nbJoursExclusion = SpecifsColisRules.getNbJoursExclusion(exclusion);

				// Si le colis contient une étape (la dernière de la période)
				// d'exclusion >J ou une étape d’exclu à J et qu’il n’est pas
				// 14h
				if (SpecifsColisRules.estExcluDuJour(nbJoursExclusion, dateAppel)) {
					collections.addToExclusDuJour(noLt);
				}

				// Si étape EXCLUSION sur la période
				if (SpecifsColisRules.estExcluAJ(nbJoursExclusion)) {
					collections.addToExclusAJ(noLt);
				} else if (SpecifsColisRules.estExcluAJ1(nbJoursExclusion)) {
					collections.addToExclusAJ1(noLt);
				} else if (SpecifsColisRules.estExcluAJX(nbJoursExclusion)) {
					collections.addToExclusAJX(noLt);
				}
			}

			// RG-MSGetSyntAgence-018
			if (incident != null && SpecifsColisRules.isIncidentDispersion(incident.getValue())) {
				collections.addToIncidentTG2Jour(noLt);
			}

			// Si la dernière étape est une étape PERDU
			if (derniereEtape != null && perdu != null && derniereEtape.getKey().equals(perdu.getKey())
					&& derniereEtape.getValue().equals(perdu.getValue())) {
				collections.addToPerdus(noLt);
			}
		}

		for (final SpecifsColis specifColis : colisASaisir.values()) {
			// Si parmi les étapes d’avant la période scrutée on ne trouve pas
			// l’une des étape suivante: PREPA_DISTRI et LIVRAISON
			final ImmutableSortedMap<Date, String> etapesAvantPeriode = SpecifsColisRules
					.getEtapesBefore(specifColis.getEtapes(), fromDate);
			final Entry<Date, String> dernEtapeAvantCeJour = SpecifsColisRules.getLastEtape(etapesAvantPeriode);

			/*
			 * Si la derniere étape avant la période est bien une étape
			 * d'exclusion alors on ajoute le colis dans les ARemettre en distri
			 */
			if (dernEtapeAvantCeJour != null
					&& dernEtapeAvantCeJour.getValue().contains(EEtapesColis.EXCLUSION.getCode())) {
				collections.addToARemettreEnDistribution(specifColis.getNoLt());
				/*
				 * Si en plus, cette étape est la derniere étape, on peut
				 * l'ajouter à NonEncoreRemisEnDistri ...
				 */
				final SortedMap<Date, String> etapesInPeriode = SpecifsColisRules.getEtapesInPeriode(specifColis,
						fromDate, toDate);
				if (etapesInPeriode.isEmpty()) {
					collections.addToNonRemisEnDistribution(specifColis.getNoLt());
				}
			}
		}

		if (!FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE)) {
			collections.addAllToDisperses(collections.getDispersesDeloc());
			collections.addAllToEnDispersion(collections.getEnDispersionDeloc());
			collections.addAllToPrepares(collections.getPreparesDeloc());
		}

		return collections;
	}

	/**
	 * Générer les collection en fonctions des règles de gestion liées à chaque
	 * collection<br>
	 * Retourner l’objet CollectionDispersion
	 * 
	 * @param colisSaisis:
	 *            la liste de colis saisis
	 * @param colisASaisir
	 *            : la liste de colis à saisir
	 * @param fromDate
	 * @param toDate
	 * @param dateAppel
	 *            : La date locale d'appel du MS au format ISO 8601
	 * @param posteComptable
	 *            : id de l'agence recherchée
	 * @return un objet <code>CollectionDispersion</code>
	 */
	private CollectionsDistribution genereCollectionsDistribution(final Map<String, SpecifsColis> colisSaisis,
			final Date fromDate, final Date toDate, String dateAppel, final String posteComptable) {
		final CollectionsDistribution collections = new CollectionsDistribution();
		collections.setColisSaisis(colisSaisis.keySet());

		// Si dateAppel est null, on met la date/heure actuelle
		if (StringUtils.isBlank(dateAppel)) {
			dateAppel = new DateTime().toString();
		}

		// codes pour RG-MSGetSyntDistri-001
		final Set<String> codesLivDomicile = cacheParametre.getValue("evt_distri_domicile_positif").getSetValue();
		// codes pour RG-MSGetSyntDistri-002
		final Set<String> codesInstance = cacheParametre.getValue("evt_instance").getSetValue();
		// codes pour RG-MSGetSyntDistri-003
		final Set<String> codesEchecLivraison = cacheParametre.getValue("evt_distri_negatif").getSetValue();

		for (final SpecifsColis specifColis : colisSaisis.values()) {
			final String noLt = specifColis.getNoLt();

			// filtre les étapes sur la période
			final SortedMap<Date, String> etapesInPeriode = filtreEtapesPourSupervisionAgence(
					SpecifsColisRules.getEtapesInPeriode(specifColis, fromDate, toDate));
			final ImmutableSortedMap<Date, String> allLivraisonsInPeriode = SpecifsColisRules
					.getEtapesFromType(etapesInPeriode, EEtapesColis.LIVRAISON);
			final Map.Entry<Date, String> derniereLivraisonInPeriode = SpecifsColisRules
					.getLastEtapeFromType(etapesInPeriode, EEtapesColis.LIVRAISON);
			final Map.Entry<Date, String> derniereEtapeInperiode = SpecifsColisRules.getLastEtape(etapesInPeriode);

			// filtre les étapes sur période et agence
			final SortedMap<Date, String> etapesInPeriodeForAgence = filtreEtapesSurAgence(etapesInPeriode,
					posteComptable);
			final ImmutableSortedMap<Date, String> allLivraisonsInPeriodeForAgence = SpecifsColisRules
					.getEtapesFromType(etapesInPeriodeForAgence, EEtapesColis.LIVRAISON);
			final Map.Entry<Date, String> derniereLivraisonInPeriodeForAgence = SpecifsColisRules
					.getLastEtapeFromType(etapesInPeriodeForAgence, EEtapesColis.LIVRAISON);
			final Map.Entry<Date, String> dernierePrepDistriInPeriodeForAgence = SpecifsColisRules
					.getLastEtapeFromType(etapesInPeriodeForAgence, EEtapesColis.PREPA_DISTRI);
			final Map.Entry<Date, String> derniereEtapeInperiodeForAgence = SpecifsColisRules
					.getLastEtape(etapesInPeriodeForAgence);

			// étape ultime du colis, même après la période
			final Map.Entry<Date, String> derniereEtape = SpecifsColisRules.getLastEtape(specifColis.getEtapes());

			// Si derniére étape du colis est une livraison
			if (null != derniereEtape && null != derniereLivraisonInPeriode
					&& derniereEtape.getValue().contains(EEtapesColis.LIVRAISON.getCode())) {
				final String codeEvt = extractCodeEvtFromEtape(derniereLivraisonInPeriode.getValue());
				// si evt de distri domicile
				if (derniereEtape.getKey() == derniereLivraisonInPeriode.getKey()
						&& codesLivDomicile.contains(codeEvt)) {
					collections.addToLivreDomicileP(noLt);
					final String agenceFromEtape = SpecifsColisRules
							.extractCodeAgenceFromEtape(derniereEtape.getValue());
					// si colis sur l'agence
					if (agenceFromEtape.equals(posteComptable)) {
						collections.addToLivreDomicilePA(noLt);
						// si colis deloc
						if (estUnColisDeloc(derniereEtape.getValue())) {
							collections.addToLivreDomicilePADeloc(noLt);
						}
					}
				}
			}

			// parcourt toutes les etapes de livraison sur la période
			final Iterator<Entry<Date, String>> itLivPeriode = allLivraisonsInPeriode.entrySet().iterator();
			while (itLivPeriode.hasNext()) {
				final Entry<Date, String> next = itLivPeriode.next();
				// si il existe une étape LIVRAISON de mise en instance
				if (codesInstance.contains(extractCodeEvtFromEtape(next.getValue()))) {
					collections.addToInstanceP(noLt);
					// si derniere étape de la periode = LIV de mise en instance
					if (next.getKey().equals(derniereEtapeInperiode.getKey())) {
						collections.addToEnInstanceP(noLt);
					}
				}
			}

			// si étape RETOUR_AGENCE sur la période
			if (null != SpecifsColisRules.getLastEtapeFromType(etapesInPeriode, EEtapesColis.RETOUR_AGENCE)) {
				collections.addToVueEnRetourP(noLt);
			}

			// récup code tournée de derniére étape
			final String derniereEtapeCodeTournee = SpecifsColisRules
					.extractTourneeFromEtape(derniereEtapeInperiode.getValue());
			// dernière étape est une étape Hors Agence Chrono
			if (null != derniereEtapeCodeTournee
					&& (derniereEtapeCodeTournee.equals("PICKUP") || derniereEtapeCodeTournee.equals("LAPOSTE"))) {
				collections.addToSortieReseau(noLt);
			}

			// sur période et agence, si derniére étape = étape LIV
			if (null != derniereLivraisonInPeriodeForAgence && null != derniereEtapeInperiodeForAgence
					&& derniereLivraisonInPeriodeForAgence.getKey().equals(derniereEtapeInperiodeForAgence.getKey())) {
				collections.addToEnLivraisonPA(noLt);
			}

			// sur période et agence, si derniére étape = étape PREPA_DISTRI
			// et code evt = "TA"
			if (null != dernierePrepDistriInPeriodeForAgence && null != derniereEtapeInperiodeForAgence
					&& dernierePrepDistriInPeriodeForAgence.getKey().equals(derniereEtapeInperiodeForAgence.getKey())
					&& "TA".equals(SpecifsColisRules
							.extractCodeEvtFromEtape(dernierePrepDistriInPeriodeForAgence.getValue()))) {
				collections.addToEnPrepaPA(noLt);
				// si colis deloc
				if (estUnColisDeloc(dernierePrepDistriInPeriodeForAgence.getValue())) {
					collections.addToEnPrepaPADeloc(noLt);
				}
			}

			// parcourt toutes les etapes de livraison sur période et agence
			final Iterator<Entry<Date, String>> itLivPeriodeAgence = allLivraisonsInPeriodeForAgence.entrySet()
					.iterator();
			while (itLivPeriodeAgence.hasNext()) {
				final Entry<Date, String> next = itLivPeriodeAgence.next();
				// si étape LIVRAISON d'échec de livraison
				if (codesEchecLivraison.contains(extractCodeEvtFromEtape(next.getValue()))) {
					collections.addToEchecPA(noLt);
					// si colis deloc
					if (estUnColisDeloc(next.getValue())) {
						collections.addToEchecPADeloc(noLt);
					}
				}
				// dernière étape est une LIVRAISON de mise en instance
				if (codesInstance.contains(extractCodeEvtFromEtape(next.getValue()))
						&& next.getKey().equals(derniereEtapeInperiode.getKey())) {
					collections.addToEnInstancePA(noLt);
					// si colis deloc
					if (estUnColisDeloc(next.getValue())) {
						collections.addToEnInstancePADeloc(noLt);
					}
				}
			}
		}

		return collections;
	}

	/**
	 * Générer les collection en fonctions des règles de gestion liées à chaque
	 * collection<br>
	 * Retourner l’objet CollectionDispersionPassee
	 * 
	 * @param colisRestantTg2
	 * @param fromDate
	 * @param toDate
	 * @return un objet <code>CollectionDispersionPassee</code>
	 * 
	 * @author bjbari
	 */
	private CollectionsDispersionPassee genereCollectionsDispersionPassee(
			final Map<String, SpecifsColis> colisRestantTg2, final Date fromDate, final Date toDate,
			final String posteComptable) {

		final CollectionsDispersionPassee collections = new CollectionsDispersionPassee();
		// colis en cours
		collections.setColisEnCours(colisRestantTg2.keySet());

		for (final SpecifsColis specifColis : colisRestantTg2.values()) {
			final String noLt = specifColis.getNoLt();
			final SortedMap<Date, String> etapesInPeriode = SpecifsColisRules.getEtapesInPeriode(specifColis, fromDate,
					toDate);
			final Map.Entry<Date, String> exclusionInPeriode = SpecifsColisRules.getLastEtapeFromType(etapesInPeriode,
					EEtapesColis.EXCLUSION);
			final Map.Entry<Date, String> dispersionInPeriode = SpecifsColisRules.getLastEtapeFromType(etapesInPeriode,
					EEtapesColis.DISPERSION);
			final Map.Entry<Date, String> perduInPeriode = SpecifsColisRules.getLastEtapeFromType(etapesInPeriode,
					EEtapesColis.PERDU);
			final Map.Entry<Date, String> derniereEtape = SpecifsColisRules.getLastEtape(specifColis.getEtapes());
			// Vérifier si l'étape est bien sur l'agence
			final String agenceFromEtape = SpecifsColisRules.extractCodeAgenceFromEtape(derniereEtape.getValue());
			final String typeDerniereEtape = SpecifsColisRules.extractEtapeTypeFromEtape(derniereEtape.getValue());
			// Si étape DISPERSION sur la période
			if (dispersionInPeriode != null) {
				// Si dernière étape du colis est une étape de dispersion sur la
				// période
				if (derniereEtape != null && derniereEtape.getKey().equals(dispersionInPeriode.getKey())
						&& derniereEtape.getValue().equals(dispersionInPeriode.getValue())) {

					// Vérifier si l'étape est bien sur l'agence
					if (null != agenceFromEtape && agenceFromEtape.equals(posteComptable)) {
						if (SpecifsColisRules.estUnColisDeloc(derniereEtape.getValue())) {
							collections.addToEnDispersionDelocPassee(noLt);
						} else {
							collections.addToEnDispersionPassee(noLt);
						}
						
						// Si dernière étape du colis type DISPERSION|xx|RBP
						if (SpecifsColisRules.isDisperseVersLaPoste(derniereEtape.getValue())) {
							collections.addToEnDispersionPostePassee(noLt);
						}

						// Alertes
						// Date de la dernière alerte
						final Date dateAlerte = SpecifsColisRules.getLastDateAlertesInPeriode(specifColis, fromDate,
								toDate, TYPE_RPTSDTA);
						// RG-MSGetSyntAgence-030
						if (dateAlerte != null && dispersionInPeriode.getKey().before(dateAlerte)) {
							if (estUnColisDeloc(dispersionInPeriode.getValue()))
								collections.addToEnAlerteActiveDelocPassee(noLt);
							else
								collections.addToEnAlerteActivePassee(noLt);
						}
					}
				}
			}

			// Si étape PERDU sur la période
			if (perduInPeriode != null) {
				// Si la dernière étape est une étape PERDU
				if (derniereEtape != null && perduInPeriode != null
						&& derniereEtape.getKey().equals(perduInPeriode.getKey())
						&& derniereEtape.getValue().equals(perduInPeriode.getValue())) {
					// Vérifier si l'étape est bien sur l'agence
					if (null != agenceFromEtape && agenceFromEtape.equals(posteComptable))
						collections.addToPerdusPassee(noLt);
				}
			}

			// Si étape EXCLUSION sur la période
			if (exclusionInPeriode != null) {
				// Si la dernière étape est une étape d'exclusion
				if (derniereEtape != null && exclusionInPeriode != null
						&& derniereEtape.getKey().equals(exclusionInPeriode.getKey())
						&& derniereEtape.getValue().equals(exclusionInPeriode.getValue())) {
					if (null != agenceFromEtape && agenceFromEtape.equals(posteComptable)) {
						collections.addToExclusPassee(noLt);
						if (SpecifsColisRules.estExclusOubliesPassee(exclusionInPeriode))
							collections.addToExclusOubliesPassee(noLt);
					}
				}
			}
			// Si la dernière étape est une étape d'exclusion et que la
			// date(EXCLUSION) + durée(EXCLUSION) <= fin_période
			if (typeDerniereEtape != null && typeDerniereEtape.equals(EXCLUSION.getCode())
					&& SpecifsColisRules.estARemettreEnDistributionOubliesPassee(derniereEtape, toDate)) {
				collections.addToARemettreEnDistributionOubliesPassee(noLt);
			}
		}

		if (!FeatureFlips.INSTANCE.getBoolean("SADelocActif", Boolean.FALSE)) {
			collections.addAllToEnDispersionPassee(collections.getEnDispersionDelocPassee());
			collections.addAllToEnAlerteActivePassee(collections.getEnAlerteActiveDelocPassee());
		}

		return collections;
	}

	/**
	 * Cette méthode permet de supprimer les étapes qui pour supervision agence
	 * ne sont pas des étapes adéquates
	 * 
	 * @param etapesInPeriode
	 * @return
	 */
	private SortedMap<Date, String> filtreEtapesPourSupervisionAgence(final ImmutableSortedMap<Date, String> etapes) {
		final ImmutableSortedMap.Builder<Date, String> builder = ImmutableSortedMap.naturalOrder();

		if (etapes != null) {
			final Predicate<Map.Entry<Date, String>> predicate = new Predicate<Map.Entry<Date, String>>() {
				public boolean apply(final Map.Entry<Date, String> input) {
					// RG-MSGetSyntAgence-019
					// Si l'étape est une dispersion RA RT IS IA on ne la
					// considère pas
					if (input.getValue().startsWith(EEtapesColis.DISPERSION.getCode())
							&& evtNotDispersion.contains(extractCodeEvtFromEtape(input.getValue()))) {
						return false;
					}
					return true; // L'input ne correspond à aucune EEtapesColis
				}
			};

			final Set<Map.Entry<Date, String>> filteredEtapes = Sets
					.newHashSet(Iterables.filter(etapes.entrySet(), predicate));
			for (final Map.Entry<Date, String> entry : filteredEtapes) {
				builder.put(entry);
			}
		}
		return builder.build();
	}

	/**
	 * Cette méthode permet de supprimer les étapes qui pour supervision agence
	 * ne sont pas des étapes adéquates Supprime également les étapes n'ayant
	 * pas eu lieu dans l'agence recherchée
	 * 
	 * @param etapesInPeriode
	 * @return
	 */
	private SortedMap<Date, String> filtreEtapesSurAgence(final SortedMap<Date, String> etapes,
			final String posteComptable) {
		final ImmutableSortedMap.Builder<Date, String> builder = ImmutableSortedMap.naturalOrder();

		if (etapes != null) {
			final Predicate<Map.Entry<Date, String>> predicate = new Predicate<Map.Entry<Date, String>>() {
				public boolean apply(final Map.Entry<Date, String> input) {
					final String evt = null == extractCodeEvtFromEtape(input.getValue()) ? ""
							: SpecifsColisRules.extractEtapeTypeFromEtape(input.getValue());
					// RG-MSGetSyntAgence-020-21-23-24-25
					// Pour étapes DISPERSE, PREPA_DISTRI et EXCLUSION
					// vérifier que l'étape est bien sur l'agence recherchée
					if (evt.equals(DISPERSION.getCode()) || evt.equals(PREPA_DISTRI.getCode())
							|| evt.equals(EXCLUSION.getCode())) {
						final String agenceFromEtape = SpecifsColisRules.extractCodeAgenceFromEtape(input.getValue());
						if (null == agenceFromEtape || !agenceFromEtape.equals(posteComptable)) {
							return false;
						}
					}

					return true; // L'input ne correspond à aucune EEtapesColis
				}
			};

			final Set<Map.Entry<Date, String>> filteredEtapes = Sets
					.newHashSet(Iterables.filter(etapes.entrySet(), predicate));
			for (final Map.Entry<Date, String> entry : filteredEtapes) {
				builder.put(entry);
			}
		}
		return builder.build();
	}

	@Override
	public SyntheseDispersionQuantite getSyntheseDispersionQuantiteJoursPrecedents(final String posteComptable,
			final String dateDebut, final String dateFin) {
		// La date d'appel local est remplacée par la date de fin pour le calcul
		// de N jours précédents
		return getSyntheseDispersionQuantite(posteComptable, dateFin, SpecifsColisRules.strISO8601ToDate(dateDebut),
				SpecifsColisRules.strISO8601ToDate(dateFin));
	}

	@Override
	public SyntheseColisEtListeValeurs getSyntheseDispersionDetailIndicateurJoursPrecedents(
			final String posteComptable, final String codeIndicateur, final String dateDebut, final String dateFin) {
		// La date d'appel local est remplacée par la date de fin pour le calcul
		// de N jours précédents
		return getSyntheseDispersionDetailIndicateur(posteComptable, codeIndicateur, null, dateFin,
				SpecifsColisRules.strISO8601ToDate(dateDebut), SpecifsColisRules.strISO8601ToDate(dateFin));
	}

	/**
	 * Renvoie la date de début Pour nbJours==0 ou nbJours==null, renvoie la
	 * date d'aujourd'hui à 00.00.00 Pour nbJours!=null ET nbJours!=0, renvoie
	 * la date "nbJours jours dans le passé" à 00.00.00
	 * 
	 * @param nbJours
	 * @return
	 */
	private Date getDateDebut(final Integer nbJours) {
		final DateTime toDay = DateTime.now().withTimeAtStartOfDay();
		final int jour = (nbJours == null) ? 0 : nbJours.intValue();
		return toDay.minusDays(jour).toDate();
	}

	/**
	 * Renvoie la date de fin </br>
	 * Pour nbJours==0 ou nbJours==null, renvoie la date d'aujourd'hui à
	 * 23:59:59 </br>
	 * Pour nbJours!=null ET nbJours!=0, renvoie la date d'hier à 23:59:59
	 * 
	 * @param nbJours
	 * @return
	 */
	private Date getDateFin(final Integer nbJours) {
		final DateTime toDay = DateTime.now().withTimeAtStartOfDay();
		final int jour = (nbJours == null || nbJours == 0) ? 1 : 0;
		return toDay.plusDays(jour).minusSeconds(1).toDate();
	}
	
	/**
     * Declare un appel au MS
     * @param nbTrt
     * @param NbFail
     */
	public void declareAppelMS() {
		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)) {
			dao.updateCptHitMS();
		}
	}

	public void declareFailMS() {
		if (FeatureFlips.INSTANCE.getBoolean("CounterMS_Actif", false)) {
			dao.updateCptFailMS();
		}
	}
}
