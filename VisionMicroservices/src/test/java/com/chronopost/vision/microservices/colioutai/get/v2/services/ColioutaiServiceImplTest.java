package com.chronopost.vision.microservices.colioutai.get.v2.services;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.sdk.GetCodeTourneeFromLtV1;
import com.chronopost.vision.microservices.sdk.GetDetailTourneeV1;
import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.Point;
import com.chronopost.vision.model.Position;
import com.chronopost.vision.model.PositionGps;
import com.chronopost.vision.model.colioutai.v2.ColioutaiInfoLT;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.model.rules.LtRules;

import fr.chronopost.soap.consigne.cxf.ConsigneServiceWS;
import fr.chronopost.soap.consigne.cxf.InformationsColisConsigne;
import fr.chronopost.soap.consigne.cxf.ResultInformationsConsigne;

public class ColioutaiServiceImplTest {

	@Test
	public void testFoundNotFound() throws Exception {

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1,
				mockConsigneServiceWS);

		try {
			service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);
			assertTrue(false);
		} catch (ColioutaiException e) {
			assertEquals(e.getCodeErreur(), ColioutaiException.LT_NOT_FOUND);
		}

		Map<String, Lt> mapLT = new HashMap<String, Lt>();

		Lt lt = new Lt();
		lt.setNoLt("1234");
		lt.setLibelleEvt("libevt");
		lt.setCodeEvt("TA");
		lt.setDateEvt(new Timestamp(123456789L));
		lt.setAdresse1Destinataire("5 place de rungis");
		lt.setAdresse2Destinataire("VTri=35");
		lt.setCodePostalDestinataire("75013");
		lt.setVilleDestinataire("Paris");
		lt.setLatitudePrevue("12.45");
		lt.setLongitudePrevue("50.1");
		lt.setPositionC11("12");
		lt.setEta("12:08");

		Map<String, String> aInfoscomp = new HashMap<>();
		aInfoscomp.put("41", "60");
		aInfoscomp.put("240", "12:08");

		DateTime datedujour = new DateTime();

		Evt evt = new Evt().setPrioriteEvt(146).setDateEvt(datedujour.withTimeAtStartOfDay().plusHours(9).plusMillis(30).toDate())
				.setNoLt("XF000000000FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA").setCreateurEvt("TRI1")
				.setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
				.setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
				.setStatusEvt("Acheminement en cours").setInfoscomp(aInfoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
				.setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
				.setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
				.setStatusEnvoi("status_envoi");
		List<Evt> evtList = new ArrayList<Evt>();
		evtList.add(evt);
		lt.setEvenements(evtList);

		mapLT.put("1234", lt);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("1234"))).thenReturn(mapLT);

		ColioutaiInfoLT infoLT = service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);
		assertNotNull(infoLT);
		assertEquals(infoLT.getNoLt(), "1234");
		assertEquals(infoLT.getLibelleStatus(), "libevt");
		assertEquals(infoLT.getStatus(), "TA");
		assertEquals(infoLT.getDateDernierEvenement().getTime(), 123456789L);
		assertEquals(infoLT.getPositionTournee(), Integer.valueOf(12));
		assertEquals(infoLT.isRealise(), LtRules.isColisRealise("TA"));
		assertEquals(infoLT.getAdresseDestinataire(), "5 place de rungis<br>75013 Paris");
		assertEquals(infoLT.getDestinataire().getLati(), 12.45, 0.0001);
		assertEquals(infoLT.getDestinataire().getLongi(), 50.1, 0.0001);
		assertEquals(infoLT.getEtaInitial(), "12:08");
		assertEquals(infoLT.getCreneau(), "11:38 / 12:38");

	}

	@Test
	public void testMaitre() throws Exception {
		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1,
				mockConsigneServiceWS);

		Map<String, Lt> mapLT = new HashMap<String, Lt>();

		Lt lt = new Lt();

		lt.setNoLt("5678");

		Evt evt = new Evt().setDateEvt(new DateTime().toDate()).setNoLt("5678").setCodeEvt("TA");
		lt.setEvenements(Arrays.asList(evt));
		mapLT.put("1234", lt);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("1234"))).thenReturn(mapLT);

		ColioutaiInfoLT infoLT = service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);

		assertNotNull(infoLT);
		assertEquals(infoLT.getNoLt(), "5678");
		assertTrue(infoLT.getShowsLTMaitre());
	}

	//@Test
	public void testDataTournee() throws Exception {
		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1,
				mockConsigneServiceWS);

		Map<String, Lt> mapLT_COLIS_4 = new HashMap<>();

		Lt colis4 = buildLT(4);

		mapLT_COLIS_4.put("LT_4", colis4);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_4"))).thenReturn(mapLT_COLIS_4);

		GetCodeTourneeFromLTResponse getCodeTournee = new GetCodeTourneeFromLTResponse();
		getCodeTournee.setCodeAgence("AGENCE");
		getCodeTournee.setCodeTournee("TOURNEE");

		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_1"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_2"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_3"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_4"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_5"), Mockito.any(Date.class))).thenReturn(getCodeTournee);

		Lt lt1 = buildLT(1);
		Lt lt2 = buildLT(2);
		Lt lt3 = buildLT(3);
		Lt lt4 = buildLT(4);
		Lt lt5 = buildLT(5);

		Map<String, Lt> mapLT_COLIS_1 = new HashMap<>();
		mapLT_COLIS_1.put("LT_1", lt1);
		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_1"))).thenReturn(mapLT_COLIS_1);
		Map<String, Lt> mapLT_COLIS_2 = new HashMap<>();
		mapLT_COLIS_2.put("LT_2", lt2);
		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_2"))).thenReturn(mapLT_COLIS_2);
		Map<String, Lt> mapLT_COLIS_3 = new HashMap<>();
		mapLT_COLIS_3.put("LT_3", lt3);
		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_3"))).thenReturn(mapLT_COLIS_3);
		Map<String, Lt> mapLT_COLIS_5 = new HashMap<>();
		mapLT_COLIS_5.put("LT_5", lt5);
		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_5"))).thenReturn(mapLT_COLIS_5);

		Point p1 = new Point();
		p1.setNumeroPoint(1);
		p1.ajoutLtAuPoint(lt1);
		p1.ajoutLtAuPoint(lt2);

		Point p2 = new Point();
		p2.setNumeroPoint(2);
		p2.ajoutLtAuPoint(lt3);

		Point p3 = new Point();
		p3.setNumeroPoint(3);
		p3.ajoutListLtsAuPoint(Arrays.asList(lt4, lt5));

		DetailTournee detailTournee = new DetailTournee();

		detailTournee.setDateTournee(new Date(12345678L));
		detailTournee.setCodeAgence("AGENCE");
		detailTournee.setCodeTournee("TOURNEE");
		detailTournee.setEvtsSaisis(null);
		detailTournee.setIdC11("AZERTYUIOP");
		detailTournee.setLtsCollecte(Arrays.asList(lt1, lt2, lt3, lt4, lt5));
		detailTournee.setPointsEnDistribution(Arrays.asList(p2, p3));
		detailTournee.setPointsRealises(Arrays.asList(p1));

		PositionGps pos1 = new PositionGps();
		pos1.setCoordonnees(new Position(50.0d, 50.0d));
		pos1.setDateRelevePosition(new Date(300000L));

		PositionGps pos2 = new PositionGps();
		pos2.setCoordonnees(new Position(51.0d, 51.0d));
		pos2.setDateRelevePosition(new Date(200000L));

		PositionGps pos3 = new PositionGps();
		pos3.setCoordonnees(new Position(52.0d, 52.0d));
		pos3.setDateRelevePosition(new Date(100000L));

		// volontairement à l'envers pour tester le sort
		detailTournee.setRelevesGps(Arrays.asList(pos3, pos2, pos1));

		Mockito.when(mockDetailTourneeV1.getDetailTournee(Mockito.eq("AGENCETOURNEE"), Mockito.any(Date.class))).thenReturn(detailTournee);

		ColioutaiInfoLT infoLT = service.findInfoLT("LT_4", DateRules.toTodayTime("09:00"), null);

		checkData(infoLT);
	}

//	private void prepareDataBak() throws Exception {
//		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);
//		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);
//		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);
//		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
//		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);
//		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);
//		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1,
//				mockConsigneServiceWS);
//
//		Map<String, Lt> mapLT_COLIS_4 = new HashMap<>();
//
//		Lt colis4 = buildLT(4);
//
//		mapLT_COLIS_4.put("LT_4", colis4);
//
//		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_4"))).thenReturn(mapLT_COLIS_4);
//
//		GetCodeTourneeFromLTResponse getCodeTournee = new GetCodeTourneeFromLTResponse();
//		getCodeTournee.setCodeAgence("AGENCE");
//		getCodeTournee.setCodeTournee("TOURNEE");
//
//		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_1"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
//		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_2"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
//		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_3"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
//		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_4"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
//		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_5"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
//
//		Lt lt1 = buildLT(1);
//		Lt lt2 = buildLT(2);
//		Lt lt3 = buildLT(3);
//		Lt lt4 = buildLT(4);
//		Lt lt5 = buildLT(5);
//
//		Map<String, Lt> mapLT_COLIS_1 = new HashMap<>();
//		mapLT_COLIS_1.put("LT_1", lt1);
//		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_1"))).thenReturn(mapLT_COLIS_1);
//		Map<String, Lt> mapLT_COLIS_2 = new HashMap<>();
//		mapLT_COLIS_2.put("LT_2", lt2);
//		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_2"))).thenReturn(mapLT_COLIS_2);
//		Map<String, Lt> mapLT_COLIS_3 = new HashMap<>();
//		mapLT_COLIS_3.put("LT_3", lt3);
//		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_3"))).thenReturn(mapLT_COLIS_3);
//		Map<String, Lt> mapLT_COLIS_5 = new HashMap<>();
//		mapLT_COLIS_5.put("LT_5", lt5);
//		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_5"))).thenReturn(mapLT_COLIS_5);
//
//		Point p1 = new Point();
//		p1.setNumeroPoint(1);
//		p1.ajoutLtAuPoint(lt1);
//		p1.ajoutLtAuPoint(lt2);
//
//		Point p2 = new Point();
//		p2.setNumeroPoint(2);
//		p2.ajoutLtAuPoint(lt3);
//
//		Point p3 = new Point();
//		p3.setNumeroPoint(3);
//		p3.ajoutListLtsAuPoint(Arrays.asList(lt4, lt5));
//
//		DetailTournee detailTournee = new DetailTournee();
//
//		detailTournee.setDateTournee(new Date(12345678L));
//		detailTournee.setCodeAgence("AGENCE");
//		detailTournee.setCodeTournee("TOURNEE");
//		detailTournee.setEvtsSaisis(null);
//		detailTournee.setIdC11("AZERTYUIOP");
//		detailTournee.setLtsCollecte(Arrays.asList(lt1, lt2, lt3, lt4, lt5));
//		detailTournee.setPointsEnDistribution(Arrays.asList(p2, p3));
//		detailTournee.setPointsRealises(Arrays.asList(p1));
//
//		PositionGps pos1 = new PositionGps();
//		pos1.setCoordonnees(new Position(50.0d, 50.0d));
//		pos1.setDateRelevePosition(new Date(300000L));
//
//		PositionGps pos2 = new PositionGps();
//		pos2.setCoordonnees(new Position(51.0d, 51.0d));
//		pos2.setDateRelevePosition(new Date(200000L));
//
//		PositionGps pos3 = new PositionGps();
//		pos3.setCoordonnees(new Position(52.0d, 52.0d));
//		pos3.setDateRelevePosition(new Date(100000L));
//
//		// volontairement à l'envers pour tester le sort
//		detailTournee.setRelevesGps(Arrays.asList(pos3, pos2, pos1));
//
//		Mockito.when(mockDetailTourneeV1.getDetailTournee(Mockito.eq("AGENCETOURNEE"), Mockito.any(Date.class))).thenReturn(detailTournee);
//
//	}

	private Lt buildLT(int number) {

		Lt lt = new Lt();

		lt.setNoLt("LT_" + number);
		lt.setLibelleEvt("EVT_" + number);

		if (number == 1 || number == 2) {
			lt.setCodeEvt("D");
		} else {
			lt.setCodeEvt("TA");
		}

		lt.setDateEvt(new Timestamp(1000000L * number));
		lt.setAdresse1Destinataire("adr " + number);
		lt.setCodePostalDestinataire("75" + number);
		lt.setVilleDestinataire("Paris" + number);
		lt.setLatitudePrevue(Integer.toString(number));
		lt.setLongitudePrevue(Integer.toString(number));
		lt.setPositionC11(Integer.toString(number));
		lt.setEta("10:30");

		return lt;
	}

	private void checkLT(ColioutaiInfoLT infoLT, int number) {

		assertNotNull(infoLT);
		assertEquals(infoLT.getNoLt(), "LT_" + number);
		assertEquals(infoLT.getLibelleStatus(), "EVT_" + number);

		if (number == 1 || number == 2) {
			assertEquals(infoLT.getStatus(), "D");
		} else {
			assertEquals(infoLT.getStatus(), "TA");
		}

		assertEquals(infoLT.getDateDernierEvenement().getTime(), 1000000L * number);
		//assertEquals(infoLT.getPositionTournee(), new Integer(number));

		boolean shouldBeRealise = false;
		if (number == 1 || number == 2) {
			shouldBeRealise = true;
		}

		assertEquals(infoLT.isRealise(), shouldBeRealise);

		Lt lt = buildLT(number);
		GeoAdresse adresse = new GeoAdresse(null, null, lt.getAdresse1Destinataire(), lt.getAdresse2Destinataire(), lt.getCodePostalDestinataire(),
				lt.getVilleDestinataire());
		assertEquals(infoLT.getAdresseDestinataire(), GeoAdresse.parseAddress(adresse));
		assertEquals(infoLT.getDestinataire().getLati(), number, 0.0001);
		assertEquals(infoLT.getDestinataire().getLongi(), number, 0.0001);

		assertEquals(infoLT.getCodeTournee(), "TOURNEE");
	}

	private void checkData(ColioutaiInfoLT infoLT) {

		// info de base commun à tous les colis
		checkLT(infoLT, 4);

		// infos additionnelle fournie uniquement au colis demandée
		checkAdditionalInfoOnColisLookedUp(infoLT);

		assertNotNull(infoLT.getTourneePositionsColis());
		assertEquals(infoLT.getTourneePositionsColis().size(), 5);

		assertNotNull(infoLT.getSetLTDuPoint());
		assertEquals(infoLT.getSetLTDuPoint().size(), 1);
		assertEquals(infoLT.getNoPoint(), 3);
		assertTrue(infoLT.getSetLTDuPoint().contains("LT_5"));

		int counter = 0;

		for (ColioutaiInfoLT infoLTFound : infoLT.getTourneePositionsColis()) {

			checkLT(infoLTFound, ++counter);

			// la LT qui est recherchée est présente aussi dans la liste
			// pour une question de simplicité coté client
			if (counter == 4) {
				// infos additionnelle fournie uniquement au colis demandée
				checkAdditionalInfoOnColisLookedUp(infoLTFound);
			}

			if (counter == 1) {
				assertNotNull(infoLTFound.getSetLTDuPoint());
				assertEquals(infoLTFound.getNoPoint(), 1);
				assertEquals(infoLTFound.getSetLTDuPoint().size(), 1);
				assertTrue(infoLTFound.getSetLTDuPoint().contains("LT_2"));
			} else if (counter == 2) {
				assertNotNull(infoLTFound.getSetLTDuPoint());
				assertEquals(infoLTFound.getNoPoint(), 1);
				assertEquals(infoLTFound.getSetLTDuPoint().size(), 1);
				assertTrue(infoLTFound.getSetLTDuPoint().contains("LT_1"));
			} else if (counter == 3) {
				assertNotNull(infoLTFound.getSetLTDuPoint());
				assertEquals(infoLTFound.getNoPoint(), 2);
				assertEquals(infoLTFound.getSetLTDuPoint().size(), 0);
			} else if (counter == 4) {
				assertNotNull(infoLTFound.getSetLTDuPoint());
				assertEquals(infoLTFound.getNoPoint(), 3);
				assertEquals(infoLTFound.getSetLTDuPoint().size(), 1);
				assertTrue(infoLTFound.getSetLTDuPoint().contains("LT_5"));
			} else if (counter == 5) {
				assertNotNull(infoLTFound.getSetLTDuPoint());
				assertEquals(infoLTFound.getNoPoint(), 3);
				assertEquals(infoLTFound.getSetLTDuPoint().size(), 1);
				assertTrue(infoLTFound.getSetLTDuPoint().contains("LT_4"));
			}
		}

	}

	private void checkAdditionalInfoOnColisLookedUp(ColioutaiInfoLT infoLT) {

		List<PositionGps> positionList = infoLT.getCamionPositionTourneeList();

		assertNotNull(positionList);
		assertEquals(positionList.size(), 3);

		PositionGps positionGPS_1 = positionList.get(0);
		assertNotNull(positionGPS_1);
		assertEquals(positionGPS_1.getDateRelevePosition(), new Date(300000L));
		assertNotNull(positionGPS_1.getCoordonnees());
		assertEquals(positionGPS_1.getCoordonnees().getLati(), 50.0d, 0.0001);
		assertEquals(positionGPS_1.getCoordonnees().getLongi(), 50.0d, 0.0001);

		PositionGps positionGPS_2 = positionList.get(1);
		assertNotNull(positionGPS_2);
		assertEquals(positionGPS_2.getDateRelevePosition(), new Date(200000L));
		assertNotNull(positionGPS_2.getCoordonnees());
		assertEquals(positionGPS_2.getCoordonnees().getLati(), 51.0d, 0.0001);
		assertEquals(positionGPS_2.getCoordonnees().getLongi(), 51.0d, 0.0001);

		PositionGps positionGPS_3 = positionList.get(2);
		assertNotNull(positionGPS_3);
		assertEquals(positionGPS_3.getDateRelevePosition(), new Date(100000L));
		assertNotNull(positionGPS_3.getCoordonnees());
		assertEquals(positionGPS_3.getCoordonnees().getLati(), 52.0d, 0.0001);
		assertEquals(positionGPS_3.getCoordonnees().getLongi(), 52.0d, 0.0001);

	}

	@Test
	public void testRecupCoordonneesFromGeoCoding() throws Exception {

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1,
				mockConsigneServiceWS);

		Map<String, Lt> mapLT = new HashMap<String, Lt>();

		Lt lt = new Lt();
		lt.setNoLt("1234");
		lt.setLibelleEvt("libevt");
		lt.setCodeEvt("TA");
		lt.setDateEvt(new Timestamp(123456789L));
		lt.setEvenements(buildEvenementListForGeoTest());
		lt.setAdresse1Destinataire("80 Avenue d'Estienne d'Orves");
		lt.setCodePostalDestinataire("91260");
		lt.setVilleDestinataire("Juvisy sur orge");
		lt.setPositionC11("12");

		mapLT.put("1234", lt);

		Position positionGoogle = new Position();
		positionGoogle.setLati(48.6922093);
		positionGoogle.setLongi(2.3761499);

		// google renvoie quelquechose mais pas POI
		Mockito.when(mockGoogleHelper.geocodeFrom(new GeoAdresse(null, null, "80 Avenue d'Estienne d'Orves", null, "91260", "Juvisy sur orge")))
				.thenReturn(positionGoogle);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("1234"))).thenReturn(mapLT);

		ColioutaiInfoLT infoLT = service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);

		Position pos = infoLT.getDestinataire();

		assertEquals(pos.getLati(), 48.6922093, 0.00001);
		assertEquals(pos.getLongi(), 2.3761499, 0.00001);

		Position positionPoi = new Position();
		positionPoi.setLati(12.3456789);
		positionPoi.setLongi(98.7654321);

		// poi renvoie quelquechose et google aussi avec priorisation POI
		Mockito.when(mockPoiHelper.geocodeFrom(new GeoAdresse(null, null, "80 Avenue d'Estienne d'Orves", null, "91260", "Juvisy sur orge")))
				.thenReturn(positionPoi);

		infoLT = service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);

		pos = infoLT.getDestinataire();

		assertEquals(pos.getLati(), 12.3456789, 0.00001);
		assertEquals(pos.getLongi(), 98.7654321, 0.00001);
	}

	@Test
	public void testHasCoordAndNoCL() throws Exception {

		// si pas de CL on prend la TA sur la LT

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1,
				mockConsigneServiceWS);

		Map<String, Lt> mapLT = new HashMap<String, Lt>();

		Lt lt = new Lt();
		lt.setNoLt("1234");
		lt.setLibelleEvt("libevt");
		lt.setCodeEvt("TA");
		lt.setDateEvt(new Timestamp(123456789L));
		lt.setEvenements(buildEvenementListForGeoTest());
		lt.setAdresse1Destinataire("80 Avenue d'Estienne d'Orves");
		lt.setCodePostalDestinataire("91260");
		lt.setVilleDestinataire("Juvisy sur orge");
		lt.setPositionC11("12");
		lt.setLatitudePrevue("48.6922093");
		lt.setLongitudePrevue("2.3761499");

		mapLT.put("1234", lt);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("1234"))).thenReturn(mapLT);

		ColioutaiInfoLT infoLT = service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);

		Position pos = infoLT.getDestinataire();

		assertEquals(pos.getLati(), 48.6922093, 0.00001);
		assertEquals(pos.getLongi(), 2.3761499, 0.00001);
	}

	@Test
	public void testHasCL() throws Exception {

		// si pas de CL on prend la TA sur la LT

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1,
				mockConsigneServiceWS);

		Map<String, Lt> mapLT = new HashMap<String, Lt>();

		Lt lt = new Lt();
		lt.setNoLt("1234");
		lt.setLibelleEvt("libevt");
		lt.setCodeEvt("TA");
		lt.setDateEvt(new Timestamp(123456789L));
		lt.setEvenements(buildEvenementListForGeoTest());
		lt.setAdresse1Destinataire("80 Avenue d'Estienne d'Orves");
		lt.setCodePostalDestinataire("91260");
		lt.setVilleDestinataire("Juvisy sur orge");
		lt.setPositionC11("12");
		lt.setLatitudeDistri("48.6922093");
		lt.setLongitudeDistri("2.3761499");

		mapLT.put("1234", lt);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("1234"))).thenReturn(mapLT);

		// ajout d'une CL dans les évenements
		Evt evt = new Evt();
		evt.setCodeEvt("CL");
		evt.setDateEvt(new DateTime().withTimeAtStartOfDay().toDate());
		lt.setEvenements(Arrays.asList(evt));

		// le service de consigne nous renvoie une adresse
		// clochette prend cher au passage.

		InformationsColisConsigne infoColisConsigne = new InformationsColisConsigne();
		infoColisConsigne.setNom1Destinataire("vivien");
		infoColisConsigne.setNom2Destinataire("de saint pern");
		infoColisConsigne.setRue1Destinataire("5 place de rungis");
		infoColisConsigne.setCodePostalDestinataire("75013");
		infoColisConsigne.setVilleDestinataire("Paris");

		ResultInformationsConsigne consigne = new ResultInformationsConsigne();
		consigne.setInformationsColisConsigne(infoColisConsigne);

		Mockito.when(mockConsigneServiceWS.getInformationsColisConsigne("1234", false)).thenReturn(consigne);

		Position positionPoi = new Position();
		positionPoi.setLati(12.3456789);
		positionPoi.setLongi(98.7654321);

		// qui est résolu par POI
		Mockito.when(mockPoiHelper.geocodeFrom(new GeoAdresse("vivien", "de saint pern", "5 place de rungis", null, "75013", "Paris"))).thenReturn(
				positionPoi);

		ColioutaiInfoLT infoLT = service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);

		Position pos = infoLT.getDestinataire();

		assertEquals(pos.getLati(), 12.3456789, 0.00001);
		assertEquals(pos.getLongi(), 98.7654321, 0.00001);
	}

	@Test
	public void testHasCLWithNoAddress() throws Exception {

		// si pas de CL on prend la TA sur la LT

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);	
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);		
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);		
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);		
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);		
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, 
				mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1, mockConsigneServiceWS);

		Map<String, Lt> mapLT = new HashMap<String, Lt>();

		Lt lt = new Lt();
		lt.setNoLt("1234");
		lt.setLibelleEvt("libevt");
		lt.setCodeEvt("TA");
		lt.setDateEvt(new Timestamp(123456789L));
		lt.setEvenements(buildEvenementListForGeoTest());
		lt.setAdresse1Destinataire("80 Avenue d'Estienne d'Orves");
		lt.setCodePostalDestinataire("91260");
		lt.setVilleDestinataire("Juvisy sur orge");
		lt.setPositionC11("12");
		lt.setLatitudeDistri("48.6922093");
		lt.setLongitudeDistri("2.3761499");
		lt.setLatitudePrevue("48.692209");
		lt.setLongitudePrevue("2.376149");

		mapLT.put("1234", lt);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("1234"))).thenReturn(mapLT);

		// ajout d'une CL dans les évenements
		Evt evt = new Evt();

		evt.setCodeEvt("CL");
		evt.setDateEvt(new DateTime().withTimeAtStartOfDay().toDate());
		lt.setEvenements(Arrays.asList(evt));

		// on ne trouve pas d'adresse sur la consigne
		Mockito.when(mockConsigneServiceWS.getInformationsColisConsigne("1234", false)).thenReturn(null);

		ColioutaiInfoLT infoLT = service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);

		Position pos = infoLT.getDestinataire();

		assertEquals(pos.getLati(), 48.692209, 0.00001);
		assertEquals(pos.getLongi(), 2.3761499, 0.00001);
	}

	@Test
	public void testPositionFlashage() throws Exception {

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);	
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);		
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);		
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);		
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);		
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, 
				mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1, mockConsigneServiceWS);

		Map<String, Lt> mapLT = new HashMap<String, Lt>();

		Lt lt = new Lt();
		lt.setNoLt("1234");
		lt.setLibelleEvt("libevt");
		lt.setCodeEvt("D");
		lt.setLatitudeDistri("12.45");
		lt.setLongitudeDistri("50.1");

		mapLT.put("1234", lt);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("1234"))).thenReturn(mapLT);

		ColioutaiInfoLT infoLT = service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);
		assertNotNull(infoLT);
		assertEquals(infoLT.getNoLt(), "1234");
		assertEquals(infoLT.getStatus(), "D");
		assertEquals(infoLT.isRealise(), LtRules.isColisRealise("D"));
		assertEquals(infoLT.getPositionFlashage().getLati(), 12.45);
		assertEquals(infoLT.getPositionFlashage().getLongi(), 50.1);
	}

	@Test(expectedExceptions = { NumberFormatException.class })
	public void testPositionFlashageThrowNumberFormatException() throws Exception {

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);	
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);		
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);		
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);		
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);		
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, 
				mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1, mockConsigneServiceWS);

		Map<String, Lt> mapLT = new HashMap<String, Lt>();

		Lt lt = new Lt();
		lt.setNoLt("1234");
		lt.setLibelleEvt("libevt");
		lt.setCodeEvt("D");
		lt.setLatitudeDistri("12,1");
		lt.setLongitudeDistri("5,1");

		mapLT.put("1234", lt);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("1234"))).thenReturn(mapLT);

		service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);

	}

	@Test(expectedExceptions = { NullPointerException.class })
	public void testPositionFlashageThrowNullPointerException() throws Exception {

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);	
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);		
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);		
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);		
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);		
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, 
				mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1, mockConsigneServiceWS);

		Map<String, Lt> mapLT = new HashMap<String, Lt>();

		Lt lt = new Lt();
		lt.setNoLt("1234");
		lt.setLibelleEvt("libevt");
		lt.setCodeEvt("D");
		mapLT.put("1234", lt);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("1234"))).thenReturn(mapLT);

		ColioutaiInfoLT infoLT = service.findInfoLT("1234", DateRules.toTodayTime("09:00"), null);
		infoLT.getPositionFlashage().getLati();

	}

	@Test
	public void testPositionFlashageMultiColis() throws Exception {

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);	
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);		
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);		
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);		
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);		
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, 
				mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1, mockConsigneServiceWS);

		Map<String, Lt> mapLT = new HashMap<String, Lt>();

		Lt lt1 = buildLT(1);
		lt1.setLatitudeDistri(Integer.toString(1));
		lt1.setLongitudeDistri(Integer.toString(1));
		Lt lt3 = buildLT(3);
		lt3.setLatitudeDistri(Integer.toString(3));
		lt3.setLongitudeDistri(Integer.toString(3));
		Lt lt4 = buildLT(4);
		lt4.setLatitudeDistri(Integer.toString(4));
		lt4.setLongitudeDistri(Integer.toString(4));
		Lt lt5 = buildLT(5);
		lt5.setLatitudeDistri(Integer.toString(5));
		lt5.setLongitudeDistri(Integer.toString(5));

		mapLT.put("LT_1", lt1);
		mapLT.put("LT_3", lt3);
		mapLT.put("LT_4", lt4);
		mapLT.put("LT_5", lt5);

		Point p1 = new Point();
		p1.setNumeroPoint(1);
		p1.ajoutListLtsAuPoint(Arrays.asList(lt1, lt3, lt4, lt5));

		DetailTournee detailTournee = new DetailTournee();

		detailTournee.setDateTournee(new Date(12345678L));
		detailTournee.setCodeAgence("AGENCE");
		detailTournee.setCodeTournee("TOURNEE");
		detailTournee.setEvtsSaisis(null);
		detailTournee.setIdC11("AZERTYUIOP");
		detailTournee.setLtsCollecte(Arrays.asList(lt1, lt3, lt4, lt5));
		detailTournee.setPointsRealises(Arrays.asList(p1));

		GetCodeTourneeFromLTResponse getCodeTournee = new GetCodeTourneeFromLTResponse();
		getCodeTournee.setCodeAgence("AGENCE");
		getCodeTournee.setCodeTournee("TOURNEE");

		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_5"), Mockito.any(Date.class))).thenReturn(getCodeTournee);

		Mockito.when(mockDetailTourneeV1.getDetailTournee(Mockito.eq("AGENCETOURNEE"), Mockito.any(Date.class))).thenReturn(detailTournee);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_5"))).thenReturn(mapLT);

		ColioutaiInfoLT infoLT = service.findInfoLT("LT_5", DateRules.toTodayTime("09:00"), null);

		assertNotNull(infoLT);
		assertEquals(infoLT.getNoLt(), "LT_5");
		assertEquals(infoLT.getStatus(), "TA");
		assertEquals(infoLT.isRealise(), LtRules.isColisRealise("TA"));
		assertEquals(infoLT.getPositionFlashage().getLati(), 1.0);
		assertEquals(infoLT.getPositionFlashage().getLongi(), 1.0);

	}

	//@Test
	public void testHasCLsurLTdansTournee() throws Exception {

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);	
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);		
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);		
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);		
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);		
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, 
				mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1, mockConsigneServiceWS);		
		
		Map<String, Lt> mapLT_COLIS_4 = new HashMap<>();

		Lt colis4 = buildLT(4);

		mapLT_COLIS_4.put("LT_4", colis4);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_4"))).thenReturn(mapLT_COLIS_4);

		GetCodeTourneeFromLTResponse getCodeTournee = new GetCodeTourneeFromLTResponse();
		getCodeTournee.setCodeAgence("AGENCE");
		getCodeTournee.setCodeTournee("TOURNEE");

		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_4"), Mockito.any(Date.class))).thenReturn(getCodeTournee);

		Lt lt1 = buildLT(1);
		Lt lt2 = buildLT(2);

		// ajout d'une CL dans les évenements
		Evt evt = new Evt();
		evt.setCodeEvt("CL");
		evt.setDateEvt(new DateTime().withTimeAtStartOfDay().toDate());
		lt2.setEvenements(Arrays.asList(evt));

		Lt lt3 = buildLT(3);
		Lt lt4 = buildLT(4);
		Lt lt5 = buildLT(5);

		Point p1 = new Point();
		p1.setNumeroPoint(1);
		p1.ajoutLtAuPoint(lt1);
		p1.ajoutLtAuPoint(lt2);

		Point p2 = new Point();
		p2.setNumeroPoint(2);
		p2.ajoutLtAuPoint(lt3);

		Point p3 = new Point();
		p3.setNumeroPoint(3);
		p3.ajoutListLtsAuPoint(Arrays.asList(lt4, lt5));

		DetailTournee detailTournee = new DetailTournee();

		detailTournee.setDateTournee(new Date(12345678L));
		detailTournee.setCodeAgence("AGENCE");
		detailTournee.setCodeTournee("TOURNEE");
		detailTournee.setEvtsSaisis(null);
		detailTournee.setIdC11("AZERTYUIOP");
		detailTournee.setLtsCollecte(Arrays.asList(lt1, lt2, lt3, lt4, lt5));
		detailTournee.setPointsEnDistribution(Arrays.asList(p2, p3));
		detailTournee.setPointsRealises(Arrays.asList(p1));

		PositionGps pos1 = new PositionGps();
		pos1.setCoordonnees(new Position(50.0d, 50.0d));
		pos1.setDateRelevePosition(new Date(300000L));

		PositionGps pos2 = new PositionGps();
		pos2.setCoordonnees(new Position(51.0d, 51.0d));
		pos2.setDateRelevePosition(new Date(200000L));

		PositionGps pos3 = new PositionGps();
		pos3.setCoordonnees(new Position(52.0d, 52.0d));
		pos3.setDateRelevePosition(new Date(100000L));

		// volontairement à l'envers pour tester le sort
		detailTournee.setRelevesGps(Arrays.asList(pos3, pos2, pos1));

		Mockito.when(mockDetailTourneeV1.getDetailTournee(Mockito.eq("AGENCETOURNEE"), Mockito.any(Date.class))).thenReturn(detailTournee);


		// le service de consigne nous renvoie une adresse
		// clochette prend cher au passage.

		InformationsColisConsigne infoColisConsigne = new InformationsColisConsigne();
		infoColisConsigne.setNom1Destinataire("vivien");
		infoColisConsigne.setNom2Destinataire("de saint pern");
		infoColisConsigne.setRue1Destinataire("5 place de rungis");
		infoColisConsigne.setCodePostalDestinataire("75013");
		infoColisConsigne.setVilleDestinataire("Paris");

		ResultInformationsConsigne consigne = new ResultInformationsConsigne();
		consigne.setInformationsColisConsigne(infoColisConsigne);

		Mockito.when(mockConsigneServiceWS.getInformationsColisConsigne("LT_2", false)).thenReturn(consigne);

		Position positionPoi = new Position();
		positionPoi.setLati(12.3456789);
		positionPoi.setLongi(98.7654321);

		// qui est résolu par POI
		Mockito.when(mockPoiHelper.geocodeFrom(new GeoAdresse("vivien", "de saint pern", "5 place de rungis", null, "75013", "Paris"))).thenReturn(
				positionPoi);

		ColioutaiInfoLT infoLT = service.findInfoLT("LT_4", DateRules.toTodayTime("09:00"), null);

		Position pos = infoLT.getDestinataire();

		assertEquals(pos.getLati(), 4.0, 0.00001);
		assertEquals(pos.getLongi(), 4.0, 0.00001);

		ColioutaiInfoLT infoLt2 = infoLT.getTourneePositionsColis().get(1);

		assertEquals(infoLt2.getDestinataire().getLati(), 12.3456789, 0.00001);
		assertEquals(infoLt2.getDestinataire().getLongi(), 98.7654321, 0.00001);
	}

	private List<Evt> buildEvenementListForGeoTest() throws ParseException {

		List<Evt> evtList = new ArrayList<Evt>();

		Map<String, String> infoscomp = new HashMap<String, String>();
		infoscomp.put("190", "4.55555");
		infoscomp.put("191", "5.66666");
		infoscomp.put("240", "12:00");
		infoscomp.put("193", "AJA20A0100208092015065959");

		Evt evt = new Evt().setPrioriteEvt(146).setDateEvt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-03-18 22:26:00"))
				.setNoLt("EEINSEVT001FR").setCabRecu("%0020090NA146848396248899250").setCodeEvt("TA").setCreateurEvt("TRI1")
				.setDateCreationEvt("2015-09-01T22:34:56").setIdAccesClient(0).setIdExtractionEvt("717493191").setIdbcoEvt(88)
				.setLibelleEvt("Envoi en transit").setLieuEvt("93999").setPositionEvt(0).setRefIdAbonnement("EVT_CHR").setSsCodeEvt("AJA0")
				.setStatusEvt("Acheminement en cours").setInfoscomp(infoscomp).setCodePostalEvt("13999").setCabEvtSaisi("cab_evt_saisi")
				.setCodeEvtExt("toto").setCodeRaisonEvt("code_raison_evt").setCodeService("code_service").setIdSsCodeEvt(1)
				.setLibelleLieuEvt("libelle_lieu_evt").setProdCabEvtSaisi(1).setProdNoLt(1).setRefExtraction("ref_extraction")
				.setStatusEnvoi("status_envoi");

		evtList.add(evt);

		return evtList;
	}

//	private void prepareDataWithCLBack() throws Exception {
//		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);	
//		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);		
//		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);		
//		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
//		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);		
//		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);		
//		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, 
//				mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1, mockConsigneServiceWS);
//		
//		Map<String, Lt> mapLT_COLIS_4 = new HashMap<>();
//
//		Lt colis4 = buildLT(4);
//
//		mapLT_COLIS_4.put("LT_4", colis4);
//
//		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_4"))).thenReturn(mapLT_COLIS_4);
//
//		GetCodeTourneeFromLTResponse getCodeTournee = new GetCodeTourneeFromLTResponse();
//		getCodeTournee.setCodeAgence("AGENCE");
//		getCodeTournee.setCodeTournee("TOURNEE");
//
//		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_4"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
//
//		Lt lt1 = buildLT(1);
//		Lt lt2 = buildLT(2);
//
//		// ajout d'une CL dans les évenements
//		Evt evt = new Evt();
//		evt.setCodeEvt("CL");
//		evt.setDateEvt(new DateTime().withTimeAtStartOfDay().toDate());
//		lt2.setEvenements(Arrays.asList(evt));
//
//		Lt lt3 = buildLT(3);
//		Lt lt4 = buildLT(4);
//		Lt lt5 = buildLT(5);
//
//		Point p1 = new Point();
//		p1.setNumeroPoint(1);
//		p1.ajoutLtAuPoint(lt1);
//		p1.ajoutLtAuPoint(lt2);
//
//		Point p2 = new Point();
//		p2.setNumeroPoint(2);
//		p2.ajoutLtAuPoint(lt3);
//
//		Point p3 = new Point();
//		p3.setNumeroPoint(3);
//		p3.ajoutListLtsAuPoint(Arrays.asList(lt4, lt5));
//
//		DetailTournee detailTournee = new DetailTournee();
//
//		detailTournee.setDateTournee(new Date(12345678L));
//		detailTournee.setCodeAgence("AGENCE");
//		detailTournee.setCodeTournee("TOURNEE");
//		detailTournee.setEvtsSaisis(null);
//		detailTournee.setIdC11("AZERTYUIOP");
//		detailTournee.setLtsCollecte(Arrays.asList(lt1, lt2, lt3, lt4, lt5));
//		detailTournee.setPointsEnDistribution(Arrays.asList(p2, p3));
//		detailTournee.setPointsRealises(Arrays.asList(p1));
//
//		PositionGps pos1 = new PositionGps();
//		pos1.setCoordonnees(new Position(50.0d, 50.0d));
//		pos1.setDateRelevePosition(new Date(300000L));
//
//		PositionGps pos2 = new PositionGps();
//		pos2.setCoordonnees(new Position(51.0d, 51.0d));
//		pos2.setDateRelevePosition(new Date(200000L));
//
//		PositionGps pos3 = new PositionGps();
//		pos3.setCoordonnees(new Position(52.0d, 52.0d));
//		pos3.setDateRelevePosition(new Date(100000L));
//
//		// volontairement à l'envers pour tester le sort
//		detailTournee.setRelevesGps(Arrays.asList(pos3, pos2, pos1));
//
//		Mockito.when(mockDetailTourneeV1.getDetailTournee(Mockito.eq("AGENCETOURNEE"), Mockito.any(Date.class))).thenReturn(detailTournee);
//
//	}

	@Test
	public void testEtatPoint() {

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);	
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);		
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);		
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);		
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);		
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, 
				mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1, mockConsigneServiceWS);
		
		ColioutaiInfoLT infoLT = new ColioutaiInfoLT();

		infoLT = service.computeEtatPoint(infoLT, new Date());

		assertNull(infoLT.getEtatPoint());

		// cas standard, tournée commencée colis pas livré
		infoLT = new ColioutaiInfoLT();

		infoLT.setStatus("TA");

		assertEquals(service.computeEtatPoint(infoLT, new Date()).getEtatPoint(), ColioutaiInfoLT.ETAT_POINT_PENDING);

		// cas colis livré
		infoLT = new ColioutaiInfoLT();
		infoLT.setRealise(true);

		assertEquals(service.computeEtatPoint(infoLT, new Date()).getEtatPoint(), ColioutaiInfoLT.ETAT_POINT_DONE);

		// Des CL sur toutes les LT du point
		infoLT = new ColioutaiInfoLT();

		infoLT.setStatus("CL");
		Set<String> setLTDuPoint = new HashSet<>();
		setLTDuPoint.add("LT1");
		setLTDuPoint.add("LT2");
		infoLT.setSetLTDuPoint(setLTDuPoint);

		List<ColioutaiInfoLT> tourneePositionsColis = new ArrayList<>();

		ColioutaiInfoLT infoLT1 = new ColioutaiInfoLT();
		infoLT1.setNoLt("LT1");
		infoLT1.setStatus("CL");
		tourneePositionsColis.add(infoLT1);

		ColioutaiInfoLT infoLT2 = new ColioutaiInfoLT();
		infoLT2.setNoLt("LT2");
		infoLT2.setStatus("CL");
		tourneePositionsColis.add(infoLT2);

		ColioutaiInfoLT infoLT3 = new ColioutaiInfoLT();
		infoLT3.setNoLt("LT3");
		infoLT3.setStatus("TA");
		tourneePositionsColis.add(infoLT3);

		infoLT.setTourneePositionsColis(tourneePositionsColis);

		assertEquals(service.computeEtatPoint(infoLT, new Date()).getEtatPoint(), ColioutaiInfoLT.ETAT_POINT_POSTPONED);

		// des CL mais pas sur tous les colis du point
		// PENDING
		infoLT = new ColioutaiInfoLT();

		infoLT.setStatus("CL");
		setLTDuPoint = new HashSet<>();
		setLTDuPoint.add("LT1");
		setLTDuPoint.add("LT2");
		infoLT.setSetLTDuPoint(setLTDuPoint);

		tourneePositionsColis = new ArrayList<>();

		infoLT1 = new ColioutaiInfoLT();
		infoLT1.setNoLt("LT1");
		infoLT1.setStatus("CL");
		tourneePositionsColis.add(infoLT1);

		infoLT2 = new ColioutaiInfoLT();
		infoLT2.setNoLt("LT2");
		infoLT2.setStatus("TA");
		tourneePositionsColis.add(infoLT2);

		infoLT3 = new ColioutaiInfoLT();
		infoLT3.setNoLt("LT3");
		infoLT3.setStatus("TA");
		tourneePositionsColis.add(infoLT3);

		infoLT.setTourneePositionsColis(tourneePositionsColis);

		assertEquals(service.computeEtatPoint(infoLT, new Date()).getEtatPoint(), ColioutaiInfoLT.ETAT_POINT_PENDING);

		// DELAYED quand on est sorti du créneau annoncé
		infoLT = new ColioutaiInfoLT();

		infoLT.setStatus("TA");
		Date creneauMin = DateRules.now();
		creneauMin.setTime(creneauMin.getTime() - 7200);
		Date creneauMax = DateRules.now();
		creneauMax.setTime(creneauMax.getTime() - 3600);
		infoLT.setCreneauMin(DateRules.toTime(creneauMin));
		infoLT.setCreneauMax(DateRules.toTime(creneauMax));
		infoLT.setEtaMaj("12:00");
		assertEquals(service.computeEtatPoint(infoLT, new Date()).getEtatPoint(), ColioutaiInfoLT.ETAT_POINT_DELAYED);

		// colis en livraison - tournée cloturée
		infoLT = new ColioutaiInfoLT();

		infoLT.setStatus("TA");
		// TODO comment on fait pour dire que la tournée est cloturée

		// assertEquals(service.computeEtatPoint(infoLT).getEtatPoint(),
		// ColioutaiInfoLT.ETAT_POINT_POSTPONED);
	}

	@Test
	public void testNbPointsAndNoPointSuivant() throws Exception {

		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1,
				mockConsigneServiceWS);

		Map<String, Lt> mapLT_COLIS_4 = new HashMap<>();

		Lt colis4 = buildLT(4);

		mapLT_COLIS_4.put("LT_4", colis4);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_4"))).thenReturn(mapLT_COLIS_4);

		GetCodeTourneeFromLTResponse getCodeTournee = new GetCodeTourneeFromLTResponse();
		getCodeTournee.setCodeAgence("AGENCE");
		getCodeTournee.setCodeTournee("TOURNEE");

		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_1"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_2"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_3"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_4"), Mockito.any(Date.class))).thenReturn(getCodeTournee);
		Mockito.when(mockCodeTourneeV1.getCodeTourneeFromLt(Mockito.eq("LT_5"), Mockito.any(Date.class))).thenReturn(getCodeTournee);

		Lt lt1 = buildLT(1);
		Lt lt2 = buildLT(2);
		Lt lt3 = buildLT(3);
		Lt lt4 = buildLT(4);
		Lt lt5 = buildLT(5);

		Map<String, Lt> mapLT_COLIS_1 = new HashMap<>();
		mapLT_COLIS_1.put("LT_1", lt1);
		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_1"))).thenReturn(mapLT_COLIS_1);
		Map<String, Lt> mapLT_COLIS_2 = new HashMap<>();
		mapLT_COLIS_2.put("LT_2", lt2);
		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_2"))).thenReturn(mapLT_COLIS_2);
		Map<String, Lt> mapLT_COLIS_3 = new HashMap<>();
		mapLT_COLIS_3.put("LT_3", lt3);
		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_3"))).thenReturn(mapLT_COLIS_3);
		Map<String, Lt> mapLT_COLIS_5 = new HashMap<>();
		mapLT_COLIS_5.put("LT_5", lt5);
		Mockito.when(mockGetLTV1.getLt(Arrays.asList("LT_5"))).thenReturn(mapLT_COLIS_5);

		Point p1 = new Point();
		p1.setNumeroPoint(1);
		p1.ajoutLtAuPoint(lt1);
		p1.ajoutLtAuPoint(lt2);

		Point p2 = new Point();
		p2.setNumeroPoint(2);
		p2.ajoutLtAuPoint(lt3);

		Point p3 = new Point();
		p3.setNumeroPoint(3);
		p3.ajoutListLtsAuPoint(Arrays.asList(lt4, lt5));

		DetailTournee detailTournee = new DetailTournee();

		detailTournee.setDateTournee(new Date(12345678L));
		detailTournee.setCodeAgence("AGENCE");
		detailTournee.setCodeTournee("TOURNEE");
		detailTournee.setEvtsSaisis(null);
		detailTournee.setIdC11("AZERTYUIOP");
		detailTournee.setLtsCollecte(Arrays.asList(lt1, lt2, lt3, lt4, lt5));
		detailTournee.setPointsEnDistribution(Arrays.asList(p2, p3));
		detailTournee.setPointsRealises(Arrays.asList(p1));

		PositionGps pos1 = new PositionGps();
		pos1.setCoordonnees(new Position(50.0d, 50.0d));
		pos1.setDateRelevePosition(new Date(300000L));

		PositionGps pos2 = new PositionGps();
		pos2.setCoordonnees(new Position(51.0d, 51.0d));
		pos2.setDateRelevePosition(new Date(200000L));

		PositionGps pos3 = new PositionGps();
		pos3.setCoordonnees(new Position(52.0d, 52.0d));
		pos3.setDateRelevePosition(new Date(100000L));

		// volontairement à l'envers pour tester le sort
		detailTournee.setRelevesGps(Arrays.asList(pos3, pos2, pos1));

		Mockito.when(mockDetailTourneeV1.getDetailTournee(Mockito.eq("AGENCETOURNEE"), Mockito.any(Date.class))).thenReturn(detailTournee);

		ColioutaiInfoLT infoLT = service.findInfoLT("LT_2", DateRules.toTodayTime("09:00"), null);

		assertNotNull(infoLT.getNoPointSuivant());
		assertEquals(infoLT.getNoPointSuivant(), new Integer(2));
		assertEquals(infoLT.getNbPointsAvantLivraison(), new Integer(0));

		infoLT = service.findInfoLT("LT_3", DateRules.toTodayTime("09:00"), null);

		assertNotNull(infoLT.getNoPointSuivant());
		assertEquals(infoLT.getNoPointSuivant(), new Integer(2));
		assertEquals(infoLT.getNbPointsAvantLivraison(), new Integer(0));

		infoLT = service.findInfoLT("LT_4", DateRules.toTodayTime("09:00"), null);
		assertNotNull(infoLT.getNoPointSuivant());
		assertEquals(infoLT.getNoPointSuivant(), new Integer(2));
		assertEquals(infoLT.getNbPointsAvantLivraison(), new Integer(1));

		infoLT = service.findInfoLT("LT_5", DateRules.toTodayTime("09:00"), null);
		assertNotNull(infoLT.getNoPointSuivant());
		assertEquals(infoLT.getNoPointSuivant(), new Integer(2));
		assertEquals(infoLT.getNbPointsAvantLivraison(), new Integer(1));

	}

	@Test
	public void testComputeCreneauFromTA() throws Exception {		
		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);	
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);		
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);		
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);		
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);		
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, 
				mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1, mockConsigneServiceWS);
		
		Map<String, String> infoscompTA = new HashMap<>();

		Map<String, String> infoscompSM = new HashMap<>();
		infoscompSM.put("79", "TA");
		infoscompSM.put(EvtRules.CRENEAU_LIVRAISON_idbc, "10H30-11H30");

		List<Evt> evtList = new ArrayList<Evt>();

		DateTime datedujour = new DateTime();

		Evt evt1 = new Evt().setPrioriteEvt(146).setDateEvt(datedujour.withTimeAtStartOfDay().plusHours(9).plusMillis(30).toDate())
				.setNoLt("XF000000000FR").setCodeEvt("TA").setInfoscomp(infoscompTA);

		Evt evt2 = new Evt().setPrioriteEvt(145).setDateEvt(datedujour.withTimeAtStartOfDay().plusHours(8).plusMillis(30).toDate())
				.setNoLt("XF000000000FR").setCodeEvt("SM").setInfoscomp(infoscompSM);

		evtList.add(evt1);
		evtList.add(evt2);

		Lt lt = new Lt();
		lt.setNoLt("XF000000000FR");
		lt.setEta("10:00");
		lt.setEvenements(evtList);

		Map<String, Lt> mapLT_COLIS = new HashMap<>();
		mapLT_COLIS.put("XF000000000FR", lt);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("XF000000000FR"))).thenReturn(mapLT_COLIS);

		ColioutaiInfoLT infoLT = service.findInfoLT("XF000000000FR", DateRules.toTodayTime("09:00"), null);

		assertNotNull(infoLT);
		assertEquals(infoLT.isCreneauCalcule(), true);
		assertEquals(infoLT.getCreneau(), "09:00 / 11:00");
	}

	@Test
	public void testComputeCreneauFromSM() throws Exception {
		GoogleGeocoderHelper mockGoogleHelper = Mockito.mock(GoogleGeocoderHelper.class);	
		PoiGeocoderHelper mockPoiHelper = Mockito.mock(PoiGeocoderHelper.class);		
		GetLtV1 mockGetLTV1 = Mockito.mock(GetLtV1.class);		
		GetDetailTourneeV1 mockDetailTourneeV1 = Mockito.mock(GetDetailTourneeV1.class);
		GetCodeTourneeFromLtV1 mockCodeTourneeV1 = Mockito.mock(GetCodeTourneeFromLtV1.class);		
		ConsigneServiceWS mockConsigneServiceWS = Mockito.mock(ConsigneServiceWS.class);		
		ColioutaiServiceImpl service = new ColioutaiServiceImpl(mockGoogleHelper, mockPoiHelper, 
				mockGetLTV1, mockDetailTourneeV1, mockCodeTourneeV1, mockConsigneServiceWS);
		
		Map<String, String> infoscompTA = new HashMap<>();

		Map<String, String> infoscompSM = new HashMap<>();
		infoscompSM.put("79", "TA");
		infoscompSM.put(EvtRules.CRENEAU_LIVRAISON_idbc, "10H30-11H30");

		List<Evt> evtList = new ArrayList<Evt>();

		DateTime datedujour = new DateTime();

		Evt evt1 = new Evt().setPrioriteEvt(146).setDateEvt(datedujour.withTimeAtStartOfDay().plusHours(9).plusMillis(30).toDate())
				.setNoLt("XF000000000FR").setCodeEvt("TA").setInfoscomp(infoscompTA);

		Evt evt2 = new Evt().setPrioriteEvt(145).setDateEvt(datedujour.withTimeAtStartOfDay().plusHours(10).plusMillis(30).toDate())
				.setNoLt("XF000000000FR").setCodeEvt("SM").setInfoscomp(infoscompSM);

		evtList.add(evt1);
		evtList.add(evt2);

		Lt lt = new Lt();
		lt.setNoLt("XF000000000FR");
		lt.setEta("10:00");
		lt.setEvenements(evtList);

		Map<String, Lt> mapLT_COLIS = new HashMap<>();
		mapLT_COLIS.put("XF000000000FR", lt);

		Mockito.when(mockGetLTV1.getLt(Arrays.asList("XF000000000FR"))).thenReturn(mapLT_COLIS);

		ColioutaiInfoLT infoLT = service.findInfoLT("XF000000000FR", DateRules.toTodayTime("09:00"), null);

		assertNotNull(infoLT);
		assertEquals(infoLT.isCreneauCalcule(), false);
		assertEquals(infoLT.getCreneau(), "10:30 / 11:30");
	}

	/**
	 * evite problemes de port etc... donne du temps aux mocks pour possible
	 * correction des problèmes aléatoires de test
	 */
	public static void sleepForMockReset() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
