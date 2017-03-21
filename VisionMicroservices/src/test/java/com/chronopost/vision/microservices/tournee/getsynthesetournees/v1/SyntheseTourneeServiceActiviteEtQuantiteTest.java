package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import static jersey.repackaged.com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.getsynthesetournees.v1.ColisPoint;
import com.chronopost.vision.model.getsynthesetournees.v1.EAnomalie;
import com.chronopost.vision.model.getsynthesetournees.v1.InfoTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.getsynthesetournees.v1.SyntheseTourneeQuantite;
import com.chronopost.vision.model.updatespecificationscolis.v1.EConsigne;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;

public class SyntheseTourneeServiceActiviteEtQuantiteTest extends SyntheseTourneeTestUtils {

	private final SyntheseTourneeServiceImpl service = SyntheseTourneeServiceImpl.INSTANCE;

	public static final String TOURNEE_ID = "tournee1";
	private static final String EVT_TA = "TA";
	private static final String EVT_RB = "RB";
	private static final String EVT_IP = "IP";
	private static final String EVT_TE = "TE";
	private static final String EVT_D = "D";
	private static final String EVT_P = "P";
	
	private SyntheseTourneeDao mock = mock(SyntheseTourneeDao.class);

	@BeforeClass
	public void setUpBeforeClass() throws Exception {
		setTranscos();
		service.setDao(mock);
	}

	/**
	 * Test de la méthode getSyntheseTourneeActivitesEtQuantites
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@Test
	public void getSyntheseTourneeActivitesEtQuantites() throws InterruptedException, ExecutionException {
		// GIVEN
		// utilise un vrai mock plutôt que la classe SyntheseTourneeDaoMock
		final List<String> tourneeIds = new ArrayList<>();
		tourneeIds.add("abc");
		tourneeIds.add("def");
		tourneeIds.add("ghi");
		when(mock.getPointsTournee("abc")).thenThrow(new InterruptedException());
		when(mock.getPointsTournee("def")).thenReturn(getPointsTournee());
		when(mock.getPointsTournee("ghi")).thenThrow(new ExecutionException(new Exception()));
		// WHEN
		final Map<String, InfoTournee> synthese = service.getSyntheseTourneeActivitesEtQuantites(tourneeIds);
		// vérifie que des exceptions dans le service font retourner une
		// InfoTournee null
		assertNull(synthese.get("abc"));
		assertNull(synthese.get("ghi"));
		assertSynthese(synthese.get("def").getSynthese());
	}

	private void assertSynthese(SyntheseTourneeQuantite synthese) {
		assertEquals(Integer.valueOf(2), synthese.getNbPtTA());
		assertEquals(Integer.valueOf(4), synthese.getNbColisTA());
		assertEquals(Integer.valueOf(2), synthese.getNbPtVisites());
		assertEquals(Integer.valueOf(1), synthese.getNbPtTANonVisites());
		assertTrue(synthese.getNbColisHorsDateContractuelle() == 1 || synthese.getNbColisHorsDateContractuelle() == 3);
		assertTrue(newHashSet("colis22", "colis31", "colis32").containsAll(synthese.getColisHorsDateContractuelle())
				|| newHashSet("colis22").containsAll(synthese.getColisHorsDateContractuelle()));
		assertEquals(Integer.valueOf(1), synthese.getNbColisAvecConsigne());
		assertEquals(Integer.valueOf(3), synthese.getNbColisRetour());
		assertEquals(Integer.valueOf(1), synthese.getNbColisSecurisesRetour());
		assertEquals(Integer.valueOf(2), synthese.getNbPointsAvecColisRetour());
		assertEquals(Integer.valueOf(0), synthese.getNbColisSpecifiques());
		assertEquals(Integer.valueOf(3), synthese.getNbPtAnomalie());
		assertEquals(Integer.valueOf(4), synthese.getNbColisTraites());
		assertEquals(Integer.valueOf(0), synthese.getNbPointsAnomalieTracabilite());
		assertTrue(synthese.getNbPtMisADispoBureau() == 2 || synthese.getNbPtMisADispoBureau() == 3);
		assertTrue(synthese.getNbHorsDelai() == 2 || synthese.getNbHorsDelai() == 3);
		assertTrue(synthese.getPresentationsInfructueuses().isEmpty());
		assertEquals(Integer.valueOf(4), synthese.getNbColisAvecETA());
		assertEquals(Integer.valueOf(2), synthese.getNbColisHorsETA());
		assertEquals(Integer.valueOf(0), synthese.getNbColisAvecConsigneNonRespectee());
		assertEquals(TOURNEE_ID, synthese.getIdC11());
	}

	private Tournee getPointsTournee() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
		Date maintenant = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(maintenant);
		cal.add(Calendar.HOUR, -1);
		Date ilYAUneHeure = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 16);
		Date ceJour16h = cal.getTime();

		String idPt1 = "pt1";
		String idPt2 = "pt2";
		String idPt3 = "pt3";

		String idColis11 = "colis11";
		String idColis12 = "colis12";
		String idColis21 = "colis21";
		String idColis22 = "colis22";
		String idColis31 = "colis31";
		String idColis32 = "colis32";
		String idColis4 = "colis4";

		// création des points de tournée (1, 2, et 3)
		PointTournee pt1 = new PointTournee();
		pt1.setIdentifiantPoint(idPt1);
		pt1.setColisPresents(new HashSet<ColisPoint>());
		pt1.setColisPrevus(new HashSet<ColisPoint>());
		pt1.setDatePassage(formatter.parseDateTime("07/04/2016 06:55:00").toDate());

		PointTournee pt2 = new PointTournee();
		pt2.setIdentifiantPoint(idPt2);
		pt2.setColisPresents(new HashSet<ColisPoint>());
		pt2.setColisPrevus(new HashSet<ColisPoint>());
		pt2.setDatePassage(formatter.parseDateTime("07/04/2016 09:57:00").toDate());

		PointTournee pt3 = new PointTournee();
		pt3.setIdentifiantPoint(idPt3);
		pt3.setColisPresents(new HashSet<ColisPoint>());
		pt3.setColisPrevus(new HashSet<ColisPoint>());
		pt3.setDatePassage(formatter.parseDateTime("07/04/2016 14:30:00").toDate());

		// création de colis
		// chaque colis a pour nom "colisXY" où X est la tournée et Y un numéro
		// (pour la clarté des tests)
		ColisPoint colis11 = new ColisPoint();
		colis11.setNo_lt(idColis11);
		colis11.setCodeEvenement(EVT_TA);
		colis11.setDateEvt(maintenant);
		colis11.setIdentifiantPoint(idPt1);

		ColisPoint colis12 = new ColisPoint();
		colis12.setNo_lt(idColis12);
		colis12.setCodeEvenement(EVT_TA);
		colis12.setDateEvt(maintenant);
		colis12.setIdentifiantPoint(idPt1);

		ColisPoint colis21 = new ColisPoint();
		colis21.setNo_lt(idColis21);
		colis21.setCodeEvenement(EVT_TA);
		colis21.setDateEvt(maintenant);
		colis21.setIdentifiantPoint(idPt2);

		// le colis aura une anomalie HORD_DELAI car livraison 16H > precocité
		// 8H
		ColisPoint colis22 = new ColisPoint();
		colis22.setNo_lt(idColis22);
		colis22.setCodeEvenement(EVT_D);
		colis22.setDateEvt(ceJour16h);
		colis22.setIdentifiantPoint(idPt2);
		colis22.setDiffETA(15);
		colis22.setPrecocite("8H");

		ColisPoint colis31 = new ColisPoint();
		colis31.setNo_lt(idColis31);
		colis31.setCodeEvenement(EVT_RB);
		colis31.setDateEvt(maintenant);
		colis31.setIdentifiantPoint(idPt3);
		colis31.setDiffETA(40);
		// livraision 14h31 - ok car < heure precocité
		colis31.setPrecocite(ESpecificiteColis.DIX_HUIT_HEURE.getCode());

		ColisPoint colis32 = new ColisPoint();
		colis32.setNo_lt(idColis32);
		colis32.setCodeEvenement(EVT_IP);
		colis32.setDateEvt(maintenant);
		colis32.setIdentifiantPoint(idPt3);
		colis32.setDiffETA(40);
		// livraision 14h31 - ok car < heure precocité
		colis32.setPrecocite(ESpecificiteColis.DIX_HUIT_HEURE.getCode());

		ColisPoint colis41 = new ColisPoint();
		colis41.setNo_lt(idColis4);
		colis41.setCodeEvenement(EVT_TA);
		colis41.setDateEvt(formatter.parseDateTime("07/04/2016 06:57:00").toDate());
		colis41.setIdentifiantPoint(idPt1);

		ColisPoint colis42 = new ColisPoint();
		colis42.setNo_lt(idColis4);
		colis42.setCodeEvenement(EVT_P);
		colis42.setDateEvt(formatter.parseDateTime("07/04/2016 09:57:00").toDate());
		colis42.setIdentifiantPoint(idPt2);
		colis42.putToInfosSupplementaires("ID_AVIS_PASSAGE", "XYZ123456789AA");
		colis42.setDiffETA(15);

		ColisPoint colis43 = new ColisPoint();
		colis43.setNo_lt(idColis4);
		colis43.setCodeEvenement(EVT_TE);
		colis43.setDateEvt(formatter.parseDateTime("07/04/2016 09:59:00").toDate());
		colis43.setIdentifiantPoint(idPt2);

		ColisPoint colis44 = new ColisPoint();
		colis44.setNo_lt(idColis4);
		colis44.setCodeEvenement(EVT_RB);
		colis44.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
		colis44.setIdentifiantPoint(idPt3);
		// livraision 14h31 - ok car < heure precocité
		colis44.setPrecocite(ESpecificiteColis.DIX_HUIT_HEURE.getCode());

		// affectation des colis aux point de tournée
		pt1.getColisPrevus().add(colis11);
		pt1.getColisPrevus().add(colis12);
		pt1.getColisPrevus().add(colis41);
		pt2.getColisPrevus().add(colis21);
		pt2.getColisPresents().add(colis22);
		pt3.getColisPresents().add(colis31);
		pt3.getColisPresents().add(colis32);
		pt2.getColisPresents().add(colis42);
		pt2.getColisPresents().add(colis43);
		pt3.getColisPresents().add(colis44);

		// définition des diffETA des points de tournée
		pt1.setDiffETA(null); // ne génère pas d'anomalie
		pt2.setDiffETA(-20); // ne génère pas d'anomalie
		pt3.setDiffETA(40); // génère une anomalie

		Tournee tournee = new Tournee();
		tournee.setIdentifiantTournee(TOURNEE_ID);

		tournee.addToPoints(pt1);
		tournee.addToPoints(pt2);
		tournee.addToPoints(pt3);

		tournee.putToColisEvenements(idColis11, colis11);
		tournee.putToColisEvenements(idColis12, colis12);
		tournee.putToColisEvenements(idColis21, colis21);
		tournee.putToColisEvenements(idColis22, colis22);
		tournee.putToColisEvenements(idColis31, colis31);
		tournee.putToColisEvenements(idColis32, colis32);
		tournee.putToColisEvenements(idColis4, colis41);
		tournee.putToColisEvenements(idColis4, colis42);
		tournee.putToColisEvenements(idColis4, colis43);
		tournee.putToColisEvenements(idColis4, colis44);

		SpecifsColis specifsColis11 = new SpecifsColis();
		specifsColis11.setNoLt(idColis11);
		specifsColis11.setSpecifsEvt(new TreeMap<Date, String>());
		specifsColis11.getSpecifsEvt().put(ilYAUneHeure, ESpecificiteColis.SENSIBLE.getCode());

		SpecifsColis specifsColis22 = new SpecifsColis();
		specifsColis22.setNoLt(idColis22);

		SpecifsColis specifsColis31 = new SpecifsColis();
		specifsColis31.setNoLt(idColis31);
		specifsColis31.setSpecifsService(new TreeMap<Date, Set<String>>());
		specifsColis31.setSpecifsEvt(new TreeMap<Date, String>());
		specifsColis31.getSpecifsEvt().put(formatter.parseDateTime("07/04/2016 14:31:00").toDate(),
				ESpecificiteColis.CONSIGNE.getCode());
		specifsColis31.setConsignesTraitees(new TreeMap<Date, String>());

		SpecifsColis specifsColis32 = new SpecifsColis();
		specifsColis32.setNoLt(idColis32);
		specifsColis32.setSpecifsService(new TreeMap<Date, Set<String>>());
		specifsColis32.setSpecifsEvt(new TreeMap<Date, String>());
		specifsColis32.setConsignesTraitees(new TreeMap<Date, String>());
		specifsColis32.getConsignesTraitees().put(new Date(new Date().getTime() - 2000),
				"|" + EConsigne.REMISE_BUREAU.getCode());

		SpecifsColis specifsColis4 = new SpecifsColis();
		specifsColis4.setNoLt(idColis4);
		specifsColis4.setSpecifsService(new TreeMap<Date, Set<String>>());
		specifsColis4.setSpecifsEvt(new TreeMap<Date, String>());
		specifsColis4.setConsignesTraitees(new TreeMap<Date, String>());

		tournee.putToColisSpecifs(idColis11, specifsColis11);
		tournee.putToColisSpecifs(idColis22, specifsColis22);
		tournee.putToColisSpecifs(idColis31, specifsColis31);
		tournee.putToColisSpecifs(idColis32, specifsColis32);
		tournee.putToColisSpecifs(idColis4, specifsColis4);

		return tournee;
	}

	/**
	 * Test de la méthode getSyntheseTourneeActiviteEtQuantite
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@Test
	public void getSyntheseTourneeActiviteEtQuantite() throws InterruptedException, ExecutionException {
		Mockito.when(mock.getPointsTournee(TOURNEE_ID)).thenReturn(getPointsTournee());
		InfoTournee infoTournee = service.getSyntheseTourneeActiviteEtQuantite(TOURNEE_ID);

		List<PointTournee> points = infoTournee.getPoints();
		Set<String> anoPt1 = points.get(0).getAnomalies();
		Set<String> anoPt2 = points.get(1).getAnomalies();
		Set<String> anoPt3 = points.get(2).getAnomalies();

		assertTrue(anoPt1.size() == 1 && anoPt2.containsAll(Arrays.asList(EAnomalie.RET_NON_SAISI.getCode())));
		assertTrue(anoPt2.size() == 2 && anoPt2
				.containsAll(Arrays.asList(EAnomalie.HD_CONTRAT.getCode(), EAnomalie.RET_NON_SAISI.getCode())));
		assertTrue((anoPt3.size() == 3 && anoPt3.containsAll(Arrays.asList(EAnomalie.MAD_PRES.getCode(),
				EAnomalie.HD_ETA.getCode(), EAnomalie.MAD_SANS_PRES.getCode())))
				|| (anoPt3.size() == 4
						&& anoPt3.containsAll(Arrays.asList(EAnomalie.MAD_PRES.getCode(), EAnomalie.HD_ETA.getCode(),
								EAnomalie.MAD_SANS_PRES.getCode(), EAnomalie.HD_CONTRAT.getCode()))));

		assertEquals(1, points.get(0).getNumPointDistri().intValue());
		assertEquals(2, points.get(1).getNumPointDistri().intValue());
		assertEquals(3, points.get(2).getNumPointDistri().intValue());

		// vérification de la SyntheseQuantiteTournee
		SyntheseTourneeQuantite synthese = infoTournee.getSynthese();

		assertSynthese(synthese);
	}
}
