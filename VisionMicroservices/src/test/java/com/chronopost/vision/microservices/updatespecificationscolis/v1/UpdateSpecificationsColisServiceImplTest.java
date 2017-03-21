package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_CL;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_CT;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_DC;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_H;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_I;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_SC;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.EVT_ST;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_ANNUL;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_DATE_CONTRACTUELLE;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_EVT_CF_I_RBP;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_EVT_CF_I_RPR;
import static com.chronopost.vision.microservices.updatespecificationscolis.v1.UpdateSpecificationsColisConstants.VALUE_NIMPORTE_QUOI;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.model.CodeService;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evenement;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.SpecifsColisRules;
import com.chronopost.vision.model.updatespecificationscolis.v1.EConsigne;
import com.chronopost.vision.model.updatespecificationscolis.v1.EEtapesColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.EInfoSupplementaire;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.EvtEtModifs;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.google.common.collect.Sets;

/**
 * Convention d'écriture pour la compréhension des tests: Un cas illustre une
 * situation bien précise et porte le numéro X.Y Alors l'événement associé a le
 * numéro de LT 20160X0Y000000 Et la date Evt 2016, X, Y, 0, 0, 0
 */
public class UpdateSpecificationsColisServiceImplTest {

	private List<EvtEtModifs> evts;

	private UpdateSpecificationsColisServiceImpl impl;

	private UpdateSpecificationsColisDaoMock dao;

	private IUpdateSpecificationsTranscoder transcoderConsignes;

	/* La map qui simule le cacheEvt */
	private static HashMap<String, Evenement> mapRefEvenements = new HashMap<>();
	static {
		mapRefEvenements.put("I", new Evenement("I", "16", EEtapesColis.RETOUR_AGENCE, 1000, "Envoi differe", ""));
		mapRefEvenements.put("D",
				new Evenement("D", "88", EEtapesColis.LIVRAISON, 1000, "Colis livre a l expediteur", ""));
		mapRefEvenements.put(EVT_H, new Evenement(EVT_H, "113", EEtapesColis.TRANSVERSE, 1000, "Information", ""));
		mapRefEvenements.put(EVT_CL,
				new Evenement(EVT_CL, "132", EEtapesColis.TRANSVERSE, 1000, "Consigne demandee", ""));
		mapRefEvenements.put(EVT_CT,
				new Evenement(EVT_CT, "91", EEtapesColis.TRANSVERSE, 2000, "Consigne traitee", ""));
		mapRefEvenements.put(EVT_ST, new Evenement(EVT_ST, "31", EEtapesColis.TRANSVERSE, 1000, "Dedouane", ""));
		mapRefEvenements.put(EVT_DC,
				new Evenement(EVT_DC, "2", EEtapesColis.TRANSVERSE, 5000, "Envoi pret chez l expediteur", ""));
		mapRefEvenements.put(EVT_SC, new Evenement(EVT_SC, "6", EEtapesColis.CONCENTRATION, 1000,
				"Tri effectue dans l agence de depart", ""));

	}

	/* La map qui simule le cacheCodeService */
	private static HashMap<String, CodeService> mapRefCodesServices = new HashMap<>();
	static {
		mapRefCodesServices.put("912", new CodeService("912", "", null, Sets.newHashSet("13H", "REP"), ""));
		mapRefCodesServices.put("950", new CodeService("950", "", null, Sets.newHashSet("13H", "SWAP"), ""));
		mapRefCodesServices.put("913", new CodeService("913", "", null, Sets.newHashSet("13H", "TAXE"), ""));
		mapRefCodesServices.put("960", new CodeService("960", "", null, Sets.newHashSet("13H", "FROID"), ""));
	}

	/* Les mock des caches */
	CacheManager<Evenement> cacheEvtMock;
	CacheManager<CodeService> cacheCodeServiceMock;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public void beforeClass() {

		dao = new UpdateSpecificationsColisDaoMock();

		transcoderConsignes = Mockito.mock(IUpdateSpecificationsTranscoder.class);
		Mockito.when(transcoderConsignes.transcode(Mockito.anyString())).thenAnswer(new Answer<EConsigne>() {
			public EConsigne answer(InvocationOnMock invocation) throws Throwable {
				if (((String) invocation.getArguments()[0]) == null)
					return null;
				return EConsigne.CONSIGNE_EN_LIGNE;
			}
		});

		/* On mock le cacheEvenement */
		cacheEvtMock = Mockito.mock(CacheManager.class);
		Mockito.when(cacheEvtMock.getCache()).thenReturn(mapRefEvenements);
		for (String evtCode : mapRefEvenements.keySet())
			Mockito.when(cacheEvtMock.getValue(evtCode)).thenReturn(mapRefEvenements.get(evtCode));

		/* On mock le cacheCodeService */
		cacheCodeServiceMock = Mockito.mock(CacheManager.class);
		Mockito.when(cacheCodeServiceMock.getCache()).thenReturn(mapRefCodesServices);
		for (String soCode : mapRefCodesServices.keySet())
			Mockito.when(cacheCodeServiceMock.getValue(soCode)).thenReturn(mapRefCodesServices.get(soCode));

		/* Injection des dépendances */
		impl = UpdateSpecificationsColisServiceImpl.getInstance().setDao(dao)
				.setTranscoderConsignes(transcoderConsignes).setRefentielCodeService(cacheCodeServiceMock)
				.setRefentielEvenement(cacheEvtMock);

	}

	@BeforeMethod
	public void beforeMethod() {
		evts = new ArrayList<>();
	}

	/**
	 * Convention d'écriture pour la compréhension des test: Un cas illustre une
	 * situation bien précise et porte le numéro X.Y Alors l'événement associé a
	 * le numéro de LT 20160X0Y000000 Et la date Evt 2016, X, Y, 0, 0, 0
	 */
	@Test
	// cas 1
	// L'événement ayant pour numéro de LT 20160101000000 porte le code Service
	// 912
	// vérifier que la spécificité extraite à partir du transcodage est bien
	// "REP"
	public void traitementCodeService_1() {
		// initialisation
		Date maintenant = new DateTime(2016, 1, 1, 0, 0, 0).toDate();
		final String noLt = "20160101000000";
		dao.setSpecifsColis(new HashMap<String, SpecifsColis>());

		evts.add(newEvtEtModifs(noLt, maintenant, "D", "912"));

		// Exécution
		impl.traitementSpecificationsColis(evts);
		Map<String, SpecifsColis> specifsColis = dao.getSpecifsColis();

		// vérifications
		// Tous les cas doivent avoir ajouté une spécificité sauf le cas 5
		assertTrue(specifsColis.containsKey(noLt));
		SpecifsColis colis = specifsColis.get(noLt);
		assertTrue(colis.getSpecifsService().get(maintenant).contains("REP"));
		assertEquals("912", colis.getService().get(maintenant));
	}

	@Test
	// cas 2
	// L'événement ayant pour numéro de LT 20160102000000 porte le code Service
	// 950
	// vérifier que la spécificité extraite à partir du transcodage est bien
	// "SWAP"
	public void traitementCodeService_2() {

		// initialisation
		Date maintenant = new DateTime(2016, 1, 2, 0, 0, 0).toDate();
		final String noLt = "20160102000000";
		dao.setSpecifsColis(new HashMap<String, SpecifsColis>());

		evts.add(newEvtEtModifs(noLt, maintenant, "D", "950"));

		// Exécution
		impl.traitementSpecificationsColis(evts);
		Map<String, SpecifsColis> specifsColis = dao.getSpecifsColis();

		// vérifications
		assertTrue(specifsColis.containsKey(noLt));
		SpecifsColis colis = specifsColis.get(noLt);
		assertTrue(colis.getSpecifsService().get(maintenant).contains("SWAP"));
		assertEquals("950", colis.getService().get(maintenant));
	}

	@Test
	// cas 3
	// L'événement ayant pour numéro de LT 20160103000000 porte le code Service
	// 913
	// vérifier que la spécificité extraite à partir du transcodage est bien
	// "TAXE"
	public void traitementCodeService_3() {

		// initialisation
		Date maintenant = new DateTime(2016, 1, 3, 0, 0, 0).toDate();
		final String noLt = "20160103000000";
		dao.setSpecifsColis(new HashMap<String, SpecifsColis>());

		evts.add(newEvtEtModifs(noLt, maintenant, "D", "913"));

		// Exécution
		impl.traitementSpecificationsColis(evts);
		Map<String, SpecifsColis> specifsColis = dao.getSpecifsColis();

		// vérifications
		assertTrue(specifsColis.containsKey(noLt));
		SpecifsColis colis = specifsColis.get(noLt);
		assertTrue(colis.getSpecifsService().get(maintenant).contains("TAXE"));
		assertEquals("913", colis.getService().get(maintenant));
	}

	@Test
	// cas 4
	// L'événement ayant pour numéro de LT 20160104000000 porte le code Service
	// 960
	// vérifier que la spécificité extraite à partir du transcodage est bien
	// "FROID"
	public void traitementCodeService_4() {

		// initialisation
		Date maintenant = new DateTime(2016, 1, 4, 0, 0, 0).toDate();
		final String noLt = "20160104000000";
		dao.setSpecifsColis(new HashMap<String, SpecifsColis>());

		evts.add(newEvtEtModifs(noLt, maintenant, "D", "960"));

		// Exécution
		impl.traitementSpecificationsColis(evts);
		Map<String, SpecifsColis> specifsColis = dao.getSpecifsColis();

		// vérifications
		assertTrue(specifsColis.containsKey(noLt));
		SpecifsColis colis = specifsColis.get(noLt);
		assertTrue(colis.getSpecifsService().get(maintenant).contains("FROID"));
		assertEquals("960", colis.getService().get(maintenant));
	}

	@Test
	// cas 5
	// Entrée: L'événement ayant pour numéro de LT 20160105000000 porte un code
	// service fantaisiste
	// Attendu: Aucune spécificité n'est extraite
	public void traitementCodeService_5() {

		Date maintenant = new DateTime(2016, 1, 5, 0, 0, 0).toDate();
		final String noLt = "20160105000000";
		// initialisation
		dao.setSpecifsColis(new HashMap<String, SpecifsColis>());

		evts.add(newEvtEtModifs(noLt, maintenant, "D", "NIMP"));

		// Exécution
		impl.traitementSpecificationsColis(evts);
		Map<String, SpecifsColis> specifsColis = dao.getSpecifsColis();

		// vérifications
		assertTrue(specifsColis.containsKey(noLt));
		SpecifsColis colis = specifsColis.get(noLt);
		assertTrue(colis.getSpecifsService().isEmpty());
		assertTrue(colis.getConsignesAnnulees().isEmpty());
		assertTrue(colis.getConsignesRecues().isEmpty());
		assertTrue(colis.getConsignesTraitees().isEmpty());
		assertTrue(colis.getDatesContractuelles().isEmpty());
		assertTrue(colis.getInfoSupp().isEmpty());
		assertTrue(colis.getService().isEmpty());
		assertTrue(colis.getEtapes().values().size() == 1);
	}

	/**
	 * 
	 * @param noLt
	 * @param dtEvt
	 * @param codeEvt
	 * @param codeService
	 * @return
	 *
	 * @author LGY
	 */
	private EvtEtModifs newEvtEtModifs(final String noLt, final Date dtEvt, final String codeEvt,
			final String codeService) {
		EvtEtModifs evtmodif = new EvtEtModifs();
		evtmodif.setEvt(newEvt(noLt, dtEvt, codeEvt, codeService));
		return evtmodif;
	}

	/**
	 * Retourne un evt a partir des données fournies
	 * 
	 * @param noLt
	 * @param dtEvt
	 * @param codeEvt
	 * @return
	 *
	 * @author LGY
	 */
	private Evt newEvt(final String noLt, final Date dtEvt, final String codeEvt) {
		Evt evt = new Evt();
		evt.setCodeEvt(codeEvt);
		evt.setDateEvt(dtEvt);
		evt.setNoLt(noLt);
		return evt;
	}

	/**
	 * Retourne un evt a partir des données fournies
	 * 
	 * @param noLt
	 * @param dtEvt
	 * @param codeEvt
	 * @param codeService
	 * @return
	 *
	 * @author LGY
	 */
	private Evt newEvt(final String noLt, final Date dtEvt, final String codeEvt, final String codeService) {
		Evt evt = newEvt(noLt, dtEvt, codeEvt);
		evt.setCodeService(codeService);
		return evt;
	}

	/**
	 * Convention d'écriture pour la compréhension des test: Un cas illustre une
	 * situation bien précise et porte le numéro X.Y Alors l'événement associé a
	 * le numéro de LT 20160X0Y000000 Et la date Evt 2016, X, Y, 0, 0, 0
	 */
	@Test
	public void traitementConsigne() {

		dao.setSpecifsColis(new HashMap<String, SpecifsColis>());

		{
			// cas 1.1
			// événement SC
			// => pas de spécificité CONSIGNE
			Map<String, String> aInfoscomp = new HashMap<>();
			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 1, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160101000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_SC);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 2.1
			// événement CL et infocomp 175 et 176 renseignées
			// => spécificité CONSIGNE avec valeur infocomp 175 + "|" +
			// transco(infocomp 176)
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_CL.getCode(), "Kill Elle Driver");
			aInfoscomp.put(EInfoComp.ID_CONSIGNE.getCode(), "California Moutain Snake");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 2, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160201000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_CL);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 2.2
			// événement CL et infocomp 175 à null
			// => pas de CONSIGNE
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_CL.getCode(), null);
			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 2, 2, 0, 0, 0).toDate());
			evt.setNoLt("20160202000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_CL);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 2.3
			// Evénement CL et valeur spécifique "ANNUL" dans infocomp 175
			// => ajout de infocomp 176 dans Consignes Annulées
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_CL.getCode(), VALUE_ANNUL);
			aInfoscomp.put(EInfoComp.ID_CONSIGNE.getCode(), "California Moutain Snake");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 2, 3, 0, 0, 0).toDate());
			evt.setNoLt("20160203000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_CL);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 3.1
			// sur un événement CT, l'infocomp COMMENTAIRE60, ID_CONSIGNE et
			// ACTION_CONTENANT sont renseignées
			// => insertion de infocomp ID_CONSIGNE + "|" +
			// transco(ACTION_CONTENANT) dans les consignes traitées
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.COMMENTAIRE60.getCode(), "I will kill Bill");
			aInfoscomp.put(EInfoComp.ACTION_CONTENANT.getCode(), "Kill O'Ren Ishii");
			aInfoscomp.put(EInfoComp.ID_CONSIGNE.getCode(), "Cottonmouth");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 3, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160301000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_CT);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 3.2
			// événement CT mais pas d'infocomp 185
			// => pas de mise à jour des consignes
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.ACTION_CONTENANT.getCode(), null);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 3, 2, 0, 0, 0).toDate());
			evt.setNoLt("20160302000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_CT);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 4.1
			// evénement I et infoscomp 56 et 60 renseignées
			// => mise à jour des consignes traitées avec la valeur
			// 0|transco(infocomp 56)
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.ID_CONSIGNE.getCode(), "I will kill Bill");
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_I.getCode(), "Kill Vernita Green");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 4, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160401000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_I);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 4.1
			// evénement I et infoscomp 56 à null
			// => Pas de mise à jour des consignes
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_I.getCode(), null);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 4, 2, 0, 0, 0).toDate());
			evt.setNoLt("20160402000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_I);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		impl.traitementSpecificationsColis(evts);

		Map<String, SpecifsColis> specifsColis = dao.getSpecifsColis();
		for (SpecifsColis colis : specifsColis.values()) {
			if (colis.getNoLt().equals("20160101000000")) { // cas 1.1
				assertTrue(colis.getConsignesAnnulees().isEmpty());
				assertTrue(colis.getConsignesRecues().isEmpty());
				assertTrue(colis.getConsignesTraitees().isEmpty());
			}
			if (colis.getNoLt().equals("20160201000000")) { // cas 2.1
				assertTrue(colis.getConsignesAnnulees().isEmpty());
				assertTrue(colis.getConsignesTraitees().isEmpty());
				assertEquals("California Moutain Snake|" + EConsigne.CONSIGNE_EN_LIGNE.getCode(),
						colis.getConsignesRecues().get(new DateTime(2016, 2, 1, 0, 0, 0).toDate()));
			}
			if (colis.getNoLt().equals("20160202000000")) { // cas 2.2
				assertTrue(colis.getConsignesAnnulees().isEmpty());
				assertTrue(colis.getConsignesTraitees().isEmpty());
				assertTrue(colis.getConsignesRecues().isEmpty());
			}
			if (colis.getNoLt().equals("20160203000000")) { // cas 2.3
				assertTrue(colis.getConsignesTraitees().isEmpty());
				assertTrue(colis.getConsignesRecues().isEmpty());
				assertEquals("California Moutain Snake",
						colis.getConsignesAnnulees().get(new DateTime(2016, 2, 3, 0, 0, 0).toDate()));
			}

			if (colis.getNoLt().equals("20160301000000")) { // cas 3.1
				assertEquals("Cottonmouth|" + EConsigne.CONSIGNE_EN_LIGNE.getCode(),
						colis.getConsignesTraitees().get(new DateTime(2016, 3, 1, 0, 0, 0).toDate()));
				assertTrue(colis.getConsignesAnnulees().isEmpty());
				assertTrue(colis.getConsignesRecues().isEmpty());
			}

			if (colis.getNoLt().equals("20160302000000")) { // cas 3.2
				assertTrue(colis.getConsignesAnnulees().isEmpty());
				assertTrue(colis.getConsignesTraitees().isEmpty());
				assertTrue(colis.getConsignesRecues().isEmpty());
			}

			if (colis.getNoLt().equals("20160401000000")) { // cas 4.1
				assertEquals("0|" + EConsigne.CONSIGNE_EN_LIGNE.getCode(),
						colis.getConsignesTraitees().get(new DateTime(2016, 4, 1, 0, 0, 0).toDate()));
				assertTrue(colis.getConsignesAnnulees().isEmpty());
				assertTrue(colis.getConsignesRecues().isEmpty());
			}

			if (colis.getNoLt().equals("20160402000000")) { // cas 4.2
				assertTrue(colis.getConsignesAnnulees().isEmpty());
				assertTrue(colis.getConsignesTraitees().isEmpty());
				assertTrue(colis.getConsignesRecues().isEmpty());
			}

		}
	}

	/**
	 * Convention d'écriture pour la compréhension des tests: Un cas illustre
	 * une situation bien précise et porte le numéro X.Y Alors l'événement
	 * associé a le numéro de LT 20160X0Y000000 Et la date Evt 2016, X, Y, 0, 0,
	 * 0
	 */
	@Test
	public void traitementEvenement() {

		dao.setSpecifsColis(new HashMap<String, SpecifsColis>());

		{
			// cas 1.1
			// l'événement est de type SC et a un infocomp 106 non null
			// => spécificité SENSIBLE

			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CODE_SAC.getCode(), "black mamba");
			aInfoscomp.put(EInfoComp.CODE_TRANSPORT.getCode(), "CT_IF");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 1, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160101000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt("SD");
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 1.2
			// l'événement est de type SC mais son infocomp 106 est null
			// => pas de spécificité SENSIBLE
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CODE_SAC.getCode(), null);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 1, 2, 0, 0, 0).toDate());
			evt.setNoLt("20160102000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_SC);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 2.1
			// L'événement est de type CL et son infocomp 175 est renseignée
			// => pas d'effet sur la spécificité événement
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_CL.getCode(), "Kill Elle Driver");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 2, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160201000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_CL);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 2.2
			// L'événement est de type CL mais son infocomp 175 n'est pas
			// renseignée
			// => pas d'effet sur la spécificité événement
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_CL.getCode(), null);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 2, 2, 0, 0, 0).toDate());
			evt.setNoLt("20160202000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_CL);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 2.3
			// L'événement est de type CL et son infocomp 175 est renseignée
			// avec la valeur "ANNUL"
			// => pas d'effet sur la spécificité événement (intervient sur une
			// consigne)
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_CL.getCode(), VALUE_ANNUL);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 2, 3, 0, 0, 0).toDate());
			evt.setNoLt("20160203000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_CL);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 3.1
			// L'événement est de type CT et son infocomp 185 est renseignée
			// => spécificité CONSIGNE
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.ACTION_CONTENANT.getCode(), "Kill O'Ren Ishii");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 3, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160301000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_CT);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 3.2
			// L'événement est de type CT et son infocomp 185 est nulle
			// => spécificité CONSIGNE
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.ACTION_CONTENANT.getCode(), null);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 3, 2, 0, 0, 0).toDate());
			evt.setNoLt("20160302000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_CT);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 4.1
			// L'événement est de type I et son infocomp 56 est renseignée
			// => pas de pécificité CONSIGNE car mauvaise valeur
			// ajout de "RETOUR_AGENCE|I" aux Etapes
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_I.getCode(), "Kill Vernita Green");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 4, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160401000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_I);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 4.2
			// L'événement est de type I et son infocomp 56 est nulle
			// => pas de pécificité CONSIGNE car pas de valeur
			// ajout de "RETOUR_AGENCE|I" aux Etapes
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_I.getCode(), null);
			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 4, 2, 0, 0, 0).toDate());
			evt.setNoLt("20160402000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_I);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 4.3
			// L'événement est de type I et son infocomp 56 est renseignée avec
			// la clé "EVT_CF_I_RBP"
			// => spécificité CONSIGNE car bonne valeur
			// ajout de "RETOUR_AGENCE|I" aux Etapes
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_I.getCode(), VALUE_EVT_CF_I_RBP);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 4, 3, 0, 0, 0).toDate());
			evt.setNoLt("20160403000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_I);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 4.4
			// L'événement est de type I et son infocomp 56 est renseignée avec
			// la clé "EVT_CF_I_RPR"
			// => spécificité CONSIGNE car bonne valeur
			// ajout de "RETOUR_AGENCE|I" aux Etapes
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CONSIGNE_EVT_I.getCode(), VALUE_EVT_CF_I_RPR);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 4, 4, 0, 0, 0).toDate());
			evt.setNoLt("20160404000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_I);
			evt.setSsCodeEvt("5DIF");
			evt.setLieuEvt("99999");
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 5.1
			// L'événement est de type ST et son infocomp 231 est renseignée
			// avec un nombre, et 233 avec un nom de client
			// => pas de spécificité TAXE car le nom client est renseigné
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.TAXE_VALEUR.getCode(), "511");
			aInfoscomp.put(EInfoComp.TAXE_NO_CONTRAT.getCode(), "Beatrix Kiddo");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 5, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160501000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_ST);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 5.2
			// L'événement est de type ST et son infocomp 231 est renseignée
			// avec une chaine de caractère, et 233 avec un nom de client
			// => pas de spécificité TAXE car le nom client est renseigné et 231
			// n'est pas un nombre
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CODE_SAC.getCode(), "Ceci n'est pas un numéro");
			aInfoscomp.put(EInfoComp.TAXE_NO_CONTRAT.getCode(), "Beatrix Kiddo");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 5, 2, 0, 0, 0).toDate());
			evt.setNoLt("20160502000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_ST);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 5.3
			// => pas de spécificité TAXE car le nom client est renseigné et 231
			// n'est pas un nombre
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CODE_SAC.getCode(), null);
			aInfoscomp.put(EInfoComp.TAXE_NO_CONTRAT.getCode(), "Beatrix Kiddo");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 5, 3, 0, 0, 0).toDate());
			evt.setNoLt("20160503000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_ST);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 5.4
			// L'événement est de type ST et son infocomp 231 a un montant de 0
			// et 233 un nom de client
			// => pas de spécificité TAXE car le nom client est renseigné et 231
			// est à 0
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CODE_SAC.getCode(), "0");
			aInfoscomp.put(EInfoComp.TAXE_NO_CONTRAT.getCode(), "Beatrix Kiddo");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 5, 4, 0, 0, 0).toDate());
			evt.setNoLt("20160504000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_ST);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 5.5
			// L'événement est de type ST et son infocomp montant de 511e et
			// compte client est vide
			// => spécificité TAXE
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.TAXE_VALEUR.getCode(), "511.00 EUR");
			aInfoscomp.put(EInfoComp.TAXE_NO_CONTRAT.getCode(), null);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 5, 5, 0, 0, 0).toDate());
			evt.setNoLt("20160505000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_ST);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 5.6
			// L'événement est de type ST et son infocomp 231 est renseigné avec
			// une chaine de caractères et 233 est vide
			// => pas de spécificité TAXE car bien que le nom client n'est pas
			// renseigné, 231 ne contient pas un nombre
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CODE_SAC.getCode(), "Ceci n'est pas un numéro");
			aInfoscomp.put(EInfoComp.TAXE_NO_CONTRAT.getCode(), null);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 5, 6, 0, 0, 0).toDate());
			evt.setNoLt("20160506000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_ST);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 5.7
			// L'événement est de type ST et ses infocomp 231 et 233 sont vides
			// => pas de spécificité TAXE car bien que le nom client n'est pas
			// renseigné, 231 ne contient pas un nombre
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CODE_SAC.getCode(), null);
			aInfoscomp.put(EInfoComp.TAXE_NO_CONTRAT.getCode(), null);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 5, 7, 0, 0, 0).toDate());
			evt.setNoLt("20160507000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_ST);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 5.8
			// L'événement est de type ST et son infocomp 231 a un montant de 0
			// et 233 le numéro de client est vide
			// => pas de spécificité TAXE car bien que le nom client n'est pas
			// renseigné, 231 contient 0
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.CODE_SAC.getCode(), "0");
			aInfoscomp.put(EInfoComp.TAXE_NO_CONTRAT.getCode(), null);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 5, 8, 0, 0, 0).toDate());
			evt.setNoLt("20160508000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_ST);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 6.1
			// L'événement est de type H et son infocomp 60 est renseignée
			// => pas de spécificité ATTRACTIF car la valeur ATTRACTIF est
			// attendue
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.ID_CONSIGNE.getCode(), "I will kill Bill");

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 6, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160601000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_H);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 6.2
			// L'événement est de type H et son infocomp 60 est nulle
			// => pas de spécificité ATTRACTIF
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.ID_CONSIGNE.getCode(), null);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 6, 2, 0, 0, 0).toDate());
			evt.setNoLt("20160602000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_H);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 6.3
			// L'événement est de type H et son infocomp 60 a la valeur
			// spécifique ATTRACTIF
			// => Spécificité ATTRACTIF
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.COMMENTAIRE60.getCode(), ESpecificiteColis.ATTRACTIF.getCode());

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 6, 3, 0, 0, 0).toDate());
			evt.setNoLt("20160603000000");
			evt.setInfoscomp(aInfoscomp);
			evt.setCodeEvt(EVT_H);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 7.1
			// Modifications est renseigné avec une date de valeur contractuelle
			// => insertion dans la colonne date_contractuelle
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.ID_CONSIGNE.getCode(), ESpecificiteColis.ATTRACTIF.getCode());

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 7, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160701000000");
			evt.setCodeEvt(EVT_H);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evtmodif.setModifications(new HashMap<String, String>());
			evtmodif.getModifications().put(VALUE_DATE_CONTRACTUELLE, "13/11/2016 12:00");
			evts.add(evtmodif);
		}

		{
			// cas 8.1
			// L'evt contient un événement de type ST et son infocomp 111 est
			// renseignée
			// => insertion dans la colonne info-supp
			Map<String, String> aInfoscomp = new HashMap<>();

			aInfoscomp.put(EInfoComp.TAXE_VALEUR.getCode(), VALUE_NIMPORTE_QUOI);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 8, 1, 0, 0, 0).toDate());
			evt.setNoLt("20160801000000");
			evt.setCodeEvt(EVT_ST);
			evt.setInfoscomp(aInfoscomp);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 8.2
			// L'evt contient un événement de type ST
			// les infocomps 106 et 111 sont renseignées
			// les infocomps 233 et 113 sont à vide
			// => insertion dans la colonne info-supp et on a une consigne
			// => spécificité TAXE car le nom client n'est pas renseigné mais le
			// montant l'est
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.TAXE_VALEUR.getCode(), "12");
			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 8, 2, 0, 0, 0).toDate());
			evt.setNoLt("20160802000000");
			evt.setCodeEvt(EVT_ST);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evt.setInfoscomp(aInfoscomp);
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
		}

		{
			// cas 8.3
			// L'evt contient un événement de type DC
			// l'infocomps 70 est renseignée
			// => insertion dans la colonne info-supp de l'infosupp NO_LT_RETOUR
			Map<String, String> aInfoscomp = new HashMap<>();
			aInfoscomp.put(EInfoComp.SWAP_NO_LT_RETOUR.getCode(), VALUE_NIMPORTE_QUOI);

			Evt evt = new Evt();
			evt.setDateEvt(new DateTime(2016, 8, 3, 0, 0, 0).toDate());
			evt.setNoLt("20160803000000");
			evt.setCodeEvt(EVT_DC);
			EvtEtModifs evtmodif = new EvtEtModifs();
			evtmodif.setEvt(evt);
			evts.add(evtmodif);
			evt.setInfoscomp(aInfoscomp);
		}

		impl.traitementSpecificationsColis(evts);
		Map<String, SpecifsColis> specifsColis = dao.getSpecifsColis();
		for (SpecifsColis colis : specifsColis.values()) {
			if (colis.getNoLt().equals("20160101000000"))
				assertEquals(ESpecificiteColis.SENSIBLE.getCode(),
						colis.getSpecifsEvt().get(new DateTime(2016, 1, 1, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160101000000")) {
				assertEquals(SpecifsColisRules.extractDispersionFromEtape(
						colis.getEtapes().get(new DateTime(2016, 1, 1, 0, 0, 0).toDate())), "CT_IF");
			}
			if (colis.getNoLt().equals("20160102000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160201000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160202000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160203000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160301000000"))
				assertEquals(ESpecificiteColis.CONSIGNE.getCode(),
						colis.getSpecifsEvt().get(new DateTime(2016, 3, 1, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160302000000"))
				assertEquals(ESpecificiteColis.CONSIGNE.getCode(),
						colis.getSpecifsEvt().get(new DateTime(2016, 3, 2, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160401000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160402000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160403000000"))
				assertEquals(ESpecificiteColis.CONSIGNE.getCode(),
						colis.getSpecifsEvt().get(new DateTime(2016, 4, 3, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160404000000"))
				assertEquals(ESpecificiteColis.CONSIGNE.getCode(),
						colis.getSpecifsEvt().get(new DateTime(2016, 4, 4, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160501000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160502000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160503000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160504000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160505000000"))
				assertEquals(ESpecificiteColis.TAXE.getCode(),
						colis.getSpecifsEvt().get(new DateTime(2016, 5, 5, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160506000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160507000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160508000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160601000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160602000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160603000000"))
				assertEquals(ESpecificiteColis.ATTRACTIF.getCode(),
						colis.getSpecifsEvt().get(new DateTime(2016, 6, 3, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160701000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160801000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160802000000")) {
				assertEquals(ESpecificiteColis.TAXE.getCode(),
						colis.getSpecifsEvt().get(new DateTime(2016, 8, 2, 0, 0, 0).toDate()));
			}
			if (colis.getNoLt().equals("20160803000000"))
				assertTrue(colis.getSpecifsEvt().isEmpty());
			if (colis.getNoLt().equals("20160101000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160102000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160201000000"))
				assertTrue(colis.getEtapes().size() == 0);
			if (colis.getNoLt().equals("20160202000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160203000000"))
				assertTrue(colis.getEtapes().size() == 0);
			if (colis.getNoLt().equals("20160301000000"))
				assertTrue(colis.getEtapes().size() == 0);
			if (colis.getNoLt().equals("20160302000000"))
				assertTrue(colis.getEtapes().size() == 0);
			if (colis.getNoLt().equals("20160401000000"))
				assertEquals(EEtapesColis.RETOUR_AGENCE.getCode() + "|" + EVT_I + "||||",
						colis.getEtapes().get(new DateTime(2016, 4, 1, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160402000000"))
				assertEquals(EEtapesColis.RETOUR_AGENCE.getCode() + "|" + EVT_I + "||||",
						colis.getEtapes().get(new DateTime(2016, 4, 2, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160403000000"))
				assertEquals(EEtapesColis.RETOUR_AGENCE.getCode() + "|" + EVT_I + "||||",
						colis.getEtapes().get(new DateTime(2016, 4, 3, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160404000000"))
				assertEquals(EEtapesColis.RETOUR_AGENCE.getCode() + "|" + EVT_I + "|5DIF|||99999",
						colis.getEtapes().get(new DateTime(2016, 4, 4, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160501000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160502000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160503000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160504000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160505000000"))
				assertTrue(colis.getEtapes().size() == 0);
			if (colis.getNoLt().equals("20160506000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160507000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160508000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160601000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160602000000"))
				assertTrue(colis.getEtapes().size() == 1);
			if (colis.getNoLt().equals("20160603000000"))
				assertTrue(colis.getEtapes().size() == 0);
			if (colis.getNoLt().equals("20160701000000"))
				assertTrue(colis.getEtapes().size() == 0);
			if (colis.getNoLt().equals("20160801000000"))
				assertTrue(colis.getEtapes().size() == 0);
			if (colis.getNoLt().equals("20160802000000"))
				assertTrue(colis.getEtapes().size() == 0);
			if (colis.getNoLt().equals("20160803000000"))
				assertTrue(colis.getEtapes().size() == 0);

			if (colis.getNoLt().equals("20160101000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160102000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160201000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160202000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160203000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160301000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160302000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160401000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160402000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160403000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160404000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160501000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160502000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160503000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160504000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160505000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160506000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160507000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160508000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160601000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160602000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160603000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160701000000"))
				assertEquals(new DateTime(2016, 11, 13, 12, 0, 0).toDate(),
						colis.getDatesContractuelles().get(new DateTime(2016, 7, 1, 0, 0, 0).toDate()));
			if (colis.getNoLt().equals("20160801000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160802000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());
			if (colis.getNoLt().equals("20160803000000"))
				assertTrue(colis.getDatesContractuelles().isEmpty());

			if (colis.getNoLt().equals("20160101000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160102000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160201000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160202000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160203000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160301000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160302000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160401000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160402000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160403000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160404000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160501000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160502000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160503000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160504000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160505000000"))
				assertTrue(colis.getInfoSupp().size() > 0 && colis.getInfoSupp().keySet().contains("TAXE_VALEUR"));
			if (colis.getNoLt().equals("20160505000000"))
				assertTrue(colis.getInfoSupp().size() > 0 && colis.getInfoSupp().values().contains("511.00 EUR"));
			if (colis.getNoLt().equals("20160506000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160507000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160508000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160601000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160602000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160603000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160701000000"))
				assertTrue(colis.getInfoSupp().isEmpty());
			if (colis.getNoLt().equals("20160801000000")) {
				assertEquals(VALUE_NIMPORTE_QUOI, colis.getInfoSupp().get(EInfoSupplementaire.TAXE_VALEUR.getCode()));
			}
			if (colis.getNoLt().equals("20160802000000"))
				assertEquals("12", colis.getInfoSupp().get(EInfoSupplementaire.TAXE_VALEUR.getCode()));
			if (colis.getNoLt().equals("20160803000000"))
				assertEquals(VALUE_NIMPORTE_QUOI, colis.getInfoSupp().get(EInfoSupplementaire.NO_LT_RETOUR.getCode()));

		}
	}
}