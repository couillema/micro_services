package com.chronopost.vision.microservices.colioutai.get.services;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.chronopost.vision.model.IndiceConfiance;
import com.chronopost.vision.model.Position;
import com.chronopost.vision.model.colioutai.ColioutaiInfoLT;
import com.chronopost.vision.model.rules.DateRules;

public class CalculETAMajTest {

	@Test
	public void testIndiceConfiance() {

		CalculETAMaj maj = new CalculETAMaj(new PTVHelperMock(), null);

		ColioutaiInfoLT colioutaiInfoLT = new ColioutaiInfoLT();
		colioutaiInfoLT.setNoLt("1234");
		colioutaiInfoLT.setIndiceConfiance(null);
		colioutaiInfoLT.setTourneePositionsColis(Arrays.asList(colioutaiInfoLT));
		colioutaiInfoLT = maj.calculETAMAJ(colioutaiInfoLT, "09:14");

		assertNull(colioutaiInfoLT.getEtaMaj());

		colioutaiInfoLT = new ColioutaiInfoLT();
		colioutaiInfoLT.setNoLt("1234");
		colioutaiInfoLT.setIndiceConfiance(IndiceConfiance.D);
		colioutaiInfoLT.setTourneePositionsColis(Arrays.asList(colioutaiInfoLT));
		colioutaiInfoLT = maj.calculETAMAJ(colioutaiInfoLT, "09:14");

		assertNull(colioutaiInfoLT.getEtaMaj());

		colioutaiInfoLT.setIndiceConfiance(IndiceConfiance.A);
		colioutaiInfoLT.setNoLt("1234");
		colioutaiInfoLT.setEtaInitial("09:00");
		colioutaiInfoLT.setTourneePositionsColis(Arrays.asList(colioutaiInfoLT));
		colioutaiInfoLT = maj.calculETAMAJ(colioutaiInfoLT, "09:14");

		assertEquals(colioutaiInfoLT.getEtaMaj(), "09:14");

		colioutaiInfoLT.setIndiceConfiance(IndiceConfiance.B);
		colioutaiInfoLT.setNoLt("1234");
		colioutaiInfoLT.setEtaInitial("09:00");
		colioutaiInfoLT.setTourneePositionsColis(Arrays.asList(colioutaiInfoLT));
		colioutaiInfoLT = maj.calculETAMAJ(colioutaiInfoLT, "09:14");

		assertNotNull(colioutaiInfoLT.getEtaMaj(), "09:14");

		colioutaiInfoLT.setIndiceConfiance(IndiceConfiance.C);
		colioutaiInfoLT.setNoLt("1234");
		colioutaiInfoLT.setEtaInitial("09:00");
		colioutaiInfoLT.setTourneePositionsColis(Arrays.asList(colioutaiInfoLT));
		colioutaiInfoLT = maj.calculETAMAJ(colioutaiInfoLT, "09:14");

		assertNotNull(colioutaiInfoLT.getEtaMaj(), "09:14");
	}

	@Test
	public void testEtaMajPredomineEtaInitial() {

		CalculETAMaj maj = new CalculETAMaj(new PTVHelperMock(), null);

		ColioutaiInfoLT colioutaiInfoLT = new ColioutaiInfoLT();
		colioutaiInfoLT.setNoLt("1234");
		colioutaiInfoLT.setIndiceConfiance(IndiceConfiance.C);
		colioutaiInfoLT.setEtaInitial("09:00");
		colioutaiInfoLT.setEtaMaj("09:30");
		colioutaiInfoLT.setTourneePositionsColis(Arrays.asList(colioutaiInfoLT));
		colioutaiInfoLT = maj.calculETAMAJ(colioutaiInfoLT, "9:14");

		assertNotNull(colioutaiInfoLT.getEtaMaj(), "09:30");
	}

	@Test
	public void testRetardChauffeur() {

		// la tournée se passe dans l'ordre mais le gars prend du retard
		ColioutaiInfoLT infoLT1 = buildColioutaiInfo(1, "8:00", "8:03");
		ColioutaiInfoLT infoLT2 = buildColioutaiInfo(2, "8:10", "8:14");
		ColioutaiInfoLT infoLT3 = buildColioutaiInfo(3, "8:21", "8:23");
		ColioutaiInfoLT infoLT4 = buildColioutaiInfo(4, "8:33", "8:31");
		ColioutaiInfoLT infoLT5 = buildColioutaiInfo(5, "8:46", "8:55");
		ColioutaiInfoLT infoLT6 = buildColioutaiInfo(6, "9:00", "9:07");
		ColioutaiInfoLT infoLT7 = buildColioutaiInfo(7, "9:15", null);
		ColioutaiInfoLT infoLT8 = buildColioutaiInfo(8, "9:31", null);
		ColioutaiInfoLT infoLT9 = buildColioutaiInfo(9, "9:48", null);
		ColioutaiInfoLT infoLT10 = buildColioutaiInfo(10, "10:11", null);

		infoLT8.setTourneePositionsColis(Arrays.asList(infoLT1, infoLT2, infoLT3, infoLT4, infoLT5, infoLT6, infoLT7,
				infoLT8, infoLT9, infoLT10));
		infoLT8.setIndiceConfiance(IndiceConfiance.A);

		CalculETAMaj maj = new CalculETAMaj(new PTVHelperMock(), null);
		infoLT8 = maj.calculETAMAJ(infoLT8, "9:12");

		assertEquals(infoLT8.getEtaMaj(), "09:38");
		assertEquals(infoLT8.getTourneePositionsColis().get(0).getEtaMaj(), "08:03");
		assertEquals(infoLT8.getTourneePositionsColis().get(1).getEtaMaj(), "08:14");
		assertEquals(infoLT8.getTourneePositionsColis().get(2).getEtaMaj(), "08:23");
		assertEquals(infoLT8.getTourneePositionsColis().get(3).getEtaMaj(), "08:31");
		assertEquals(infoLT8.getTourneePositionsColis().get(4).getEtaMaj(), "08:55");
		assertEquals(infoLT8.getTourneePositionsColis().get(5).getEtaMaj(), "09:07");
		assertEquals(infoLT8.getTourneePositionsColis().get(6).getEtaMaj(), "09:22");
		assertEquals(infoLT8.getTourneePositionsColis().get(7).getEtaMaj(), "09:38");
		assertEquals(infoLT8.getTourneePositionsColis().get(8).getEtaMaj(), "09:55");
		assertEquals(infoLT8.getTourneePositionsColis().get(9).getEtaMaj(), "10:18");

	}

	@Test
	public void testRetardChauffeurImportant() {

		// la tournée se passe dans l'ordre mais le gars prend du retard
		ColioutaiInfoLT infoLT1 = buildColioutaiInfo(1, "8:00", "8:03");
		ColioutaiInfoLT infoLT2 = buildColioutaiInfo(2, "8:10", "8:14");
		ColioutaiInfoLT infoLT3 = buildColioutaiInfo(3, "8:21", "8:23");
		ColioutaiInfoLT infoLT4 = buildColioutaiInfo(4, "8:33", "8:31");
		ColioutaiInfoLT infoLT5 = buildColioutaiInfo(5, "8:46", "8:55");
		ColioutaiInfoLT infoLT6 = buildColioutaiInfo(6, "9:00", "9:07");
		ColioutaiInfoLT infoLT7 = buildColioutaiInfo(7, "9:15", null);
		ColioutaiInfoLT infoLT8 = buildColioutaiInfo(8, "9:31", null);
		ColioutaiInfoLT infoLT9 = buildColioutaiInfo(9, "9:48", null);
		ColioutaiInfoLT infoLT10 = buildColioutaiInfo(10, "10:11", null);

		infoLT8.setTourneePositionsColis(Arrays.asList(infoLT1, infoLT2, infoLT3, infoLT4, infoLT5, infoLT6, infoLT7,
				infoLT8, infoLT9, infoLT10));
		infoLT8.setIndiceConfiance(IndiceConfiance.A);

		CalculETAMaj maj = new CalculETAMaj(new PTVHelperMock(), null);
		infoLT8 = maj.calculETAMAJ(infoLT8, "10:00");

		assertEquals(infoLT8.getEtaMaj(), "10:16");
		assertEquals(infoLT8.getTourneePositionsColis().get(0).getEtaMaj(), "08:03");
		assertEquals(infoLT8.getTourneePositionsColis().get(1).getEtaMaj(), "08:14");
		assertEquals(infoLT8.getTourneePositionsColis().get(2).getEtaMaj(), "08:23");
		assertEquals(infoLT8.getTourneePositionsColis().get(3).getEtaMaj(), "08:31");
		assertEquals(infoLT8.getTourneePositionsColis().get(4).getEtaMaj(), "08:55");
		assertEquals(infoLT8.getTourneePositionsColis().get(5).getEtaMaj(), "09:07");
		assertEquals(infoLT8.getTourneePositionsColis().get(6).getEtaMaj(), "10:00");
		assertEquals(infoLT8.getTourneePositionsColis().get(7).getEtaMaj(), "10:16");
		assertEquals(infoLT8.getTourneePositionsColis().get(8).getEtaMaj(), "10:33");
		assertEquals(infoLT8.getTourneePositionsColis().get(9).getEtaMaj(), "10:56");

	}

	@Test
	public void testTourneeIndiceB() {

		// la tournee n'est pas exactement dans l'ordre (1 break)

		ColioutaiInfoLT infoLT1 = buildColioutaiInfo(1, "8:00", "8:03");
		ColioutaiInfoLT infoLT2 = buildColioutaiInfo(2, "8:10", "8:14");
		ColioutaiInfoLT infoLT3 = buildColioutaiInfo(3, "8:21", "8:23");
		ColioutaiInfoLT infoLT4 = buildColioutaiInfo(4, "8:33", "8:31");
		ColioutaiInfoLT infoLT5 = buildColioutaiInfo(7, "9:15", "8:55");
		ColioutaiInfoLT infoLT6 = buildColioutaiInfo(8, "9:23", "9:07");
		ColioutaiInfoLT infoLT7 = buildColioutaiInfo(9, "9:27", "9:23");
		ColioutaiInfoLT infoLT8 = buildColioutaiInfo(5, "8:46", null);
		ColioutaiInfoLT infoLT9 = buildColioutaiInfo(6, "9:00", null);
		ColioutaiInfoLT infoLT10 = buildColioutaiInfo(10, "10:11", null);
		ColioutaiInfoLT infoLT11 = buildColioutaiInfo(11, "10:12", null);

		// on met les positions pour tester les appels PTV
		infoLT1.setDestinataire(new Position(48.12651, 46.16));
		infoLT2.setDestinataire(new Position(48.186487, 49.31));
		infoLT3.setDestinataire(new Position(48.997814, 48.1531));
		infoLT4.setDestinataire(new Position(49.54315, 49.1351));
		infoLT5.setDestinataire(new Position(50.47884, 51.144));
		infoLT6.setDestinataire(new Position(48.3258, 51.351));
		infoLT7.setDestinataire(new Position(51.1238761, 46.3581));
		infoLT8.setDestinataire(new Position(53.2131, 52.531));
		infoLT9.setDestinataire(new Position(53.8468, 50.278));
		infoLT10.setDestinataire(new Position(48.315, 50.384));

		infoLT8.setTourneePositionsColis(Arrays.asList(infoLT1, infoLT2, infoLT3, infoLT4, infoLT5, infoLT6, infoLT7,
				infoLT8, infoLT9, infoLT10, infoLT11));
		infoLT8.setIndiceConfiance(IndiceConfiance.B);

		CalculETAMaj maj = new CalculETAMaj(new PTVHelperMock(), null);
		infoLT8 = maj.calculETAMAJ(infoLT8, "9:29");

		// TODO attention on emule PTV avec des calculs mockés
		assertEquals(infoLT8.getEtaMaj(), "09:46");
		assertEquals(infoLT8.getTourneePositionsColis().get(0).getEtaMaj(), "08:03");
		assertEquals(infoLT8.getTourneePositionsColis().get(1).getEtaMaj(), "08:14");
		assertEquals(infoLT8.getTourneePositionsColis().get(2).getEtaMaj(), "08:23");
		assertEquals(infoLT8.getTourneePositionsColis().get(3).getEtaMaj(), "08:31");
		assertEquals(infoLT8.getTourneePositionsColis().get(4).getEtaMaj(), "08:55");
		assertEquals(infoLT8.getTourneePositionsColis().get(5).getEtaMaj(), "09:07");
		assertEquals(infoLT8.getTourneePositionsColis().get(6).getEtaMaj(), "09:23");
		assertEquals(infoLT8.getTourneePositionsColis().get(7).getEtaMaj(), "09:46");
		assertEquals(infoLT8.getTourneePositionsColis().get(8).getEtaMaj(), "10:00");
		assertEquals(infoLT8.getTourneePositionsColis().get(9).getEtaMaj(), "10:22");
		assertEquals(infoLT8.getTourneePositionsColis().get(10).getEtaMaj(), "10:23");
	}

	@Test
	public void testTourneeIndiceBEnRetard() {

		// la tournee n'est pas exactement dans l'ordre (1 break)

		ColioutaiInfoLT infoLT1 = buildColioutaiInfo(1, "8:00", "8:03");
		ColioutaiInfoLT infoLT2 = buildColioutaiInfo(2, "8:10", "8:14");
		ColioutaiInfoLT infoLT3 = buildColioutaiInfo(3, "8:21", "8:23");
		ColioutaiInfoLT infoLT4 = buildColioutaiInfo(4, "8:33", "8:31");
		ColioutaiInfoLT infoLT5 = buildColioutaiInfo(7, "9:15", "8:55");
		ColioutaiInfoLT infoLT6 = buildColioutaiInfo(8, "9:23", "9:07");
		ColioutaiInfoLT infoLT7 = buildColioutaiInfo(9, "9:27", "9:23");
		ColioutaiInfoLT infoLT8 = buildColioutaiInfo(5, "8:46", null);
		ColioutaiInfoLT infoLT9 = buildColioutaiInfo(6, "9:00", null);
		ColioutaiInfoLT infoLT10 = buildColioutaiInfo(10, "10:11", null);
		ColioutaiInfoLT infoLT11 = buildColioutaiInfo(11, "10:12", null);

		// on met les positions pour tester les appels PTV
		infoLT1.setDestinataire(new Position(48.12651, 46.16));
		infoLT2.setDestinataire(new Position(48.186487, 49.31));
		infoLT3.setDestinataire(new Position(48.997814, 48.1531));
		infoLT4.setDestinataire(new Position(49.54315, 49.1351));
		infoLT5.setDestinataire(new Position(50.47884, 51.144));
		infoLT6.setDestinataire(new Position(48.3258, 51.351));
		infoLT7.setDestinataire(new Position(51.1238761, 46.3581));
		infoLT8.setDestinataire(new Position(53.2131, 52.531));
		infoLT9.setDestinataire(new Position(53.8468, 50.278));
		infoLT10.setDestinataire(new Position(48.315, 50.384));

		infoLT8.setTourneePositionsColis(Arrays.asList(infoLT1, infoLT2, infoLT3, infoLT4, infoLT5, infoLT6, infoLT7,
				infoLT8, infoLT9, infoLT10, infoLT11));
		infoLT8.setIndiceConfiance(IndiceConfiance.B);

		CalculETAMaj maj = new CalculETAMaj(new PTVHelperMock(), null);
		infoLT8 = maj.calculETAMAJ(infoLT8, "10:29");

		// TODO attention on emule PTV avec des calculs mockés
		assertEquals(infoLT8.getEtaMaj(), "10:29");
		assertEquals(infoLT8.getTourneePositionsColis().get(0).getEtaMaj(), "08:03");
		assertEquals(infoLT8.getTourneePositionsColis().get(1).getEtaMaj(), "08:14");
		assertEquals(infoLT8.getTourneePositionsColis().get(2).getEtaMaj(), "08:23");
		assertEquals(infoLT8.getTourneePositionsColis().get(3).getEtaMaj(), "08:31");
		assertEquals(infoLT8.getTourneePositionsColis().get(4).getEtaMaj(), "08:55");
		assertEquals(infoLT8.getTourneePositionsColis().get(5).getEtaMaj(), "09:07");
		assertEquals(infoLT8.getTourneePositionsColis().get(6).getEtaMaj(), "09:23");
		assertEquals(infoLT8.getTourneePositionsColis().get(7).getEtaMaj(), "10:29");
		assertEquals(infoLT8.getTourneePositionsColis().get(8).getEtaMaj(), "10:43");
		assertEquals(infoLT8.getTourneePositionsColis().get(9).getEtaMaj(), "11:05");
		assertEquals(infoLT8.getTourneePositionsColis().get(10).getEtaMaj(), "11:06");
	}

	@Test
	public void testTourneeIndiceCaranage() {

		// la tournee n'est pas exactement dans l'ordre (1 break)

				ColioutaiInfoLT infoLT1 = buildColioutaiInfo(1, "8:00", "8:30");
				ColioutaiInfoLT infoLT2 = buildColioutaiInfo(2, "8:10", "8:14");
				ColioutaiInfoLT infoLT3 = buildColioutaiInfo(3, "8:21", "8:07");
				ColioutaiInfoLT infoLT4 = buildColioutaiInfo(4, "8:33", "8:08");
				ColioutaiInfoLT infoLT5 = buildColioutaiInfo(5, "8:46", "8:45");
				ColioutaiInfoLT infoLT6 = buildColioutaiInfo(6, "9:00", "9:45");
				ColioutaiInfoLT infoLT7 = buildColioutaiInfo(7, "9:15", "9:23");
				ColioutaiInfoLT infoLT8 = buildColioutaiInfo(8, "9:31", "9:44");
				ColioutaiInfoLT infoLT9 = buildColioutaiInfo(9, "9:48", "9:46");
				ColioutaiInfoLT infoLT10 = buildColioutaiInfo(10, "10:11", "10:30");
				ColioutaiInfoLT infoLT11 = buildColioutaiInfo(11, "10:12", null);

				// on met les positions pour tester les appels PTV
				infoLT1.setDestinataire(new Position(48.12651, 46.16));
				infoLT2.setDestinataire(new Position(48.186487, 49.31));
				infoLT3.setDestinataire(new Position(48.997814, 48.1531));
				infoLT4.setDestinataire(new Position(49.54315, 49.1351));
				infoLT5.setDestinataire(new Position(50.47884, 51.144));
				infoLT6.setDestinataire(new Position(48.3258, 51.351));
				infoLT7.setDestinataire(new Position(51.1238761, 46.3581));
				infoLT8.setDestinataire(new Position(53.2131, 52.531));
				infoLT9.setDestinataire(new Position(53.8468, 50.278));
				infoLT10.setDestinataire(new Position(48.315, 50.384));

				infoLT8.setTourneePositionsColis(Arrays.asList(infoLT1, infoLT2, infoLT3, infoLT4, infoLT5, infoLT6, infoLT7,
						infoLT8, infoLT9, infoLT10, infoLT11));
				infoLT8.setIndiceConfiance(IndiceConfiance.B);

				CalculETAMaj maj = new CalculETAMaj(new PTVHelperMock(), null);
				infoLT8 = maj.calculETAMAJ(infoLT8, "10:30");

				// TODO attention on emule PTV avec des calculs mockés
				assertEquals(infoLT8.getEtaMaj(), "09:44");
				assertEquals(infoLT8.getTourneePositionsColis().get(0).getEtaMaj(), "08:07");
				assertEquals(infoLT8.getTourneePositionsColis().get(1).getEtaMaj(), "08:08");
				assertEquals(infoLT8.getTourneePositionsColis().get(2).getEtaMaj(), "08:14");
				assertEquals(infoLT8.getTourneePositionsColis().get(3).getEtaMaj(), "08:30");
				assertEquals(infoLT8.getTourneePositionsColis().get(4).getEtaMaj(), "08:45");
				assertEquals(infoLT8.getTourneePositionsColis().get(5).getEtaMaj(), "09:23");
				assertEquals(infoLT8.getTourneePositionsColis().get(6).getEtaMaj(), "09:44");
				assertEquals(infoLT8.getTourneePositionsColis().get(7).getEtaMaj(), "09:45");
				assertEquals(infoLT8.getTourneePositionsColis().get(8).getEtaMaj(), "09:46");
				assertEquals(infoLT8.getTourneePositionsColis().get(9).getEtaMaj(), "10:30");
				assertEquals(infoLT8.getTourneePositionsColis().get(10).getEtaMaj(), "10:31");
	}

	@Test
	public void testTourneeNonCommenceeAvantHeureDebutTheorique() {

		// la tournée se passe dans l'ordre mais le gars prend du retard
		ColioutaiInfoLT infoLT1 = buildColioutaiInfo(1, "8:00", null);
		ColioutaiInfoLT infoLT2 = buildColioutaiInfo(2, "8:10", null);
		ColioutaiInfoLT infoLT3 = buildColioutaiInfo(3, "8:21", null);
		ColioutaiInfoLT infoLT4 = buildColioutaiInfo(4, "8:33", null);
		ColioutaiInfoLT infoLT5 = buildColioutaiInfo(5, "8:46", null);
		ColioutaiInfoLT infoLT6 = buildColioutaiInfo(6, "9:00", null);
		ColioutaiInfoLT infoLT7 = buildColioutaiInfo(7, "9:15", null);
		ColioutaiInfoLT infoLT8 = buildColioutaiInfo(8, "9:31", null);
		ColioutaiInfoLT infoLT9 = buildColioutaiInfo(9, "9:48", null);
		ColioutaiInfoLT infoLT10 = buildColioutaiInfo(10, "10:11", null);

		infoLT8.setTourneePositionsColis(Arrays.asList(infoLT1, infoLT2, infoLT3, infoLT4, infoLT5, infoLT6, infoLT7,
				infoLT8, infoLT9, infoLT10));
		infoLT8.setIndiceConfiance(IndiceConfiance.A);

		// avant l'heure, c'est pas l'heure
		CalculETAMaj maj = new CalculETAMaj(new PTVHelperMock(), null);
		infoLT8 = maj.calculETAMAJ(infoLT8, "07:45");

		assertNull(infoLT8.getEtaMaj());
		assertNull(infoLT8.getTourneePositionsColis().get(0).getEtaMaj());
		assertNull(infoLT8.getTourneePositionsColis().get(1).getEtaMaj());
		assertNull(infoLT8.getTourneePositionsColis().get(2).getEtaMaj());
		assertNull(infoLT8.getTourneePositionsColis().get(3).getEtaMaj());
		assertNull(infoLT8.getTourneePositionsColis().get(4).getEtaMaj());
		assertNull(infoLT8.getTourneePositionsColis().get(5).getEtaMaj());
		assertNull(infoLT8.getTourneePositionsColis().get(6).getEtaMaj());
		assertNull(infoLT8.getTourneePositionsColis().get(7).getEtaMaj());
		assertNull(infoLT8.getTourneePositionsColis().get(8).getEtaMaj());
		assertNull(infoLT8.getTourneePositionsColis().get(9).getEtaMaj());
	}

	@Test
	public void testTourneeNonCommenceApresHeureDebutTheorique() {

		// la tournée se passe dans l'ordre mais le gars prend du retard
		ColioutaiInfoLT infoLT1 = buildColioutaiInfo(1, "8:00", null);
		ColioutaiInfoLT infoLT2 = buildColioutaiInfo(2, "8:10", null);
		ColioutaiInfoLT infoLT3 = buildColioutaiInfo(3, "8:21", null);
		ColioutaiInfoLT infoLT4 = buildColioutaiInfo(4, "8:33", null);
		ColioutaiInfoLT infoLT5 = buildColioutaiInfo(5, "8:46", null);
		ColioutaiInfoLT infoLT6 = buildColioutaiInfo(6, "9:00", null);
		ColioutaiInfoLT infoLT7 = buildColioutaiInfo(7, "9:15", null);
		ColioutaiInfoLT infoLT8 = buildColioutaiInfo(8, "9:31", null);
		ColioutaiInfoLT infoLT9 = buildColioutaiInfo(9, "9:48", null);
		ColioutaiInfoLT infoLT10 = buildColioutaiInfo(10, "10:11", null);

		infoLT8.setTourneePositionsColis(Arrays.asList(infoLT1, infoLT2, infoLT3, infoLT4, infoLT5, infoLT6, infoLT7,
				infoLT8, infoLT9, infoLT10));
		infoLT8.setIndiceConfiance(IndiceConfiance.A);

		CalculETAMaj maj = new CalculETAMaj(new PTVHelperMock(), null);
		infoLT8 = maj.calculETAMAJ(infoLT8, "09:01");

		assertEquals(infoLT8.getEtaMaj(), "10:32");
		assertEquals(infoLT8.getTourneePositionsColis().get(0).getEtaMaj(), "09:01");
		assertEquals(infoLT8.getTourneePositionsColis().get(1).getEtaMaj(), "09:11");
		assertEquals(infoLT8.getTourneePositionsColis().get(2).getEtaMaj(), "09:22");
		assertEquals(infoLT8.getTourneePositionsColis().get(3).getEtaMaj(), "09:34");
		assertEquals(infoLT8.getTourneePositionsColis().get(4).getEtaMaj(), "09:47");
		assertEquals(infoLT8.getTourneePositionsColis().get(5).getEtaMaj(), "10:01");
		assertEquals(infoLT8.getTourneePositionsColis().get(6).getEtaMaj(), "10:16");
		assertEquals(infoLT8.getTourneePositionsColis().get(7).getEtaMaj(), "10:32");
		assertEquals(infoLT8.getTourneePositionsColis().get(8).getEtaMaj(), "10:49");
		assertEquals(infoLT8.getTourneePositionsColis().get(9).getEtaMaj(), "11:12");
	}

	private ColioutaiInfoLT buildColioutaiInfo(int i, String etaInitial, String heureReal) {

		ColioutaiInfoLT infoLT = new ColioutaiInfoLT();

		infoLT.setNoLt("LT" + i);
		infoLT.setEtaInitial(etaInitial);
		infoLT.setPositionTournee(i);

		try {
			if (heureReal != null) {
				infoLT.setDateDernierEvenement(DateRules.toTodayTime(heureReal));
				infoLT.setRealise(true);
			}
		} catch (Exception e) {
			throw new RuntimeException("Erreur parsing date ", e);
		}

		return infoLT;
	}
}
