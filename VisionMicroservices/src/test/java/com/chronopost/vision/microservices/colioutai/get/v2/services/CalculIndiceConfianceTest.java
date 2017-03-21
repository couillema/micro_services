package com.chronopost.vision.microservices.colioutai.get.v2.services;

import static org.testng.Assert.assertEquals;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.chronopost.vision.model.DetailTournee;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.IndiceConfiance;
import com.chronopost.vision.model.Lt;
import com.chronopost.vision.model.Point;
import com.chronopost.vision.model.rules.DateRules;
import com.chronopost.vision.ut.RandomUts;

/** @author unknown : JJC factors code for generic collection t */
public class CalculIndiceConfianceTest {

	@Test
	public void testIndiceA() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(10,"9:15")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.A);
	}
	

	@Test
	public void testIndiceB_1() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.B);
	}
	
	@Test
	public void testIndiceB_2() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15"),
				new ColisRealise(10,"9:20")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.B);
	}
	
	@Test
	public void testIndiceB_3() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15"),
				new ColisRealise(16,"9:20")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.B);
	}
	
	@Test
	public void testIndiceB_4() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15"),
				new ColisRealise(10,"9:20"),
				new ColisRealise(11,"9:21")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.B);
	}
	
	@Test
	public void testIndiceB_5() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15"),
				new ColisRealise(10,"9:20"),
				new ColisRealise(11,"9:21"),
				new ColisRealise(12,"9:22"),
				new ColisRealise(13,"9:23"),
				new ColisRealise(14,"9:24")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.B);
	}
	
	@Test
	public void testIndiceB_6() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15"),
				new ColisRealise(10,"9:20"),
				new ColisRealise(11,"9:21"),
				new ColisRealise(12,"9:22"),
				new ColisRealise(13,"9:23"),
				new ColisRealise(14,"9:24"),
				new ColisRealise(16,"9:25"),
				new ColisRealise(17,"9:26"),
				new ColisRealise(18,"9:27")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.B);
	}
	
	@Test
	public void testIndiceC_1() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15"),
				new ColisRealise(10,"9:20"),
				new ColisRealise(17,"9:26")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.C);
	}
	
	@Test
	public void testIndiceC_2() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15"),
				new ColisRealise(10,"9:20"),
				new ColisRealise(17,"9:26"),
				new ColisRealise(18,"9:27")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.C);
		
	}
	
	@Test
	public void testIndiceC_3() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15"),
				new ColisRealise(10,"9:20"),
				new ColisRealise(17,"9:26"),
				new ColisRealise(18,"9:27"),
				new ColisRealise(11,"9:28")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.C);
	}
	
	
	@Test
	public void testIndiceD() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(3,"8:10"),
				new ColisRealise(5,"8:20"),
				new ColisRealise(7,"8:40"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15"),
				new ColisRealise(17,"9:26")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.D);
	}
	
	@Test
	public void testIndiceWorstThanD() throws Exception {
		
		DetailTournee detailTournee = prepareData(20, Arrays.asList(new ColisRealise(1,"8:00"), 
				new ColisRealise(2,"8:00"),
				new ColisRealise(4,"8:12"),
				new ColisRealise(6,"8:25"),
				new ColisRealise(8,"8:55"),
				new ColisRealise(9,"9:00"),
				new ColisRealise(15,"9:15"),
				new ColisRealise(10,"9:20"),
				new ColisRealise(17,"9:26")));
		
		assertEquals(CalculIndiceConfiance.calculIndiceConfianceFrom(detailTournee), IndiceConfiance.D);
	}
	
	
	private DetailTournee prepareData(int nbColis, List<ColisRealise> colisRealises) throws Exception {

		List<Lt> listLTCollecte = new ArrayList<>();
		List<Point> listPointsEnDistribution = new ArrayList<>();
		List<Point> listPointRealises = new ArrayList<>();
		
		
		for (int i = 1; i <= nbColis; i++) {
		
			List<Evt> evts = new ArrayList<>();
			
			Lt lt = buildLT(i);
			
			Point p = new Point();
			p.setNumeroPoint(i);
			p.ajoutLtAuPoint(lt);
			
			
			listLTCollecte.add(lt);
			lt.setEvenements(evts);
			
			lt.setEta(String.format("%03d", i));
			
			boolean realise = false;
			
			for(ColisRealise colisRealise : colisRealises) {
			
				if(colisRealise.position == i) {
					evts.addAll(buildEvtReal(lt.getNoLt(), colisRealise.dateHeureRealise));
					realise = true;
					break;
				}
			}
			
			if(realise) {
				listPointRealises.add(p);
			} else {
				listPointsEnDistribution.add(p);
			}
		}

		DetailTournee detailTournee = new DetailTournee();

		detailTournee.setDateTournee(new Date(12345678L));
		detailTournee.setCodeAgence("AGENCE");
		detailTournee.setCodeTournee("TOURNEE");
		detailTournee.setIdC11("AZERTYUIOP");
		detailTournee.setLtsCollecte(listLTCollecte);
		detailTournee.setPointsEnDistribution(listPointsEnDistribution);
		detailTournee.setPointsRealises(listPointRealises);

		return detailTournee;

	}

	
	private List<Evt> buildEvtReal(String noLt, Date dateHeureRealise) {
		
		List<Evt> evtForLt = new ArrayList<>();
		
		Evt evt = new Evt();
		
		evt.setNoLt(noLt);
		
		evt.setCodeEvt(RandomUts.getRandonElement(Evt.S_EVTS_D_PLUS)); 
		// evt.setCode_evt(Evt.EVTS_D_PLUS[(int)(Math.random() * (double) Evt.EVTS_D_PLUS.length)]); // COMPREND PAS ?? should be % MODULEO ( pas testee ? ) : ((int) Math.random()) % Evt.S_EVTS_D_PLUS.size() 
		evt.setDateEvt(dateHeureRealise);
		
		evtForLt.add(evt);
		
		return evtForLt;
	}

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
		lt.setLatitudeDistri(Integer.toString(number));
		lt.setLongitudeDistri(Integer.toString(number));
		lt.setPositionC11(Integer.toString(number));
		
		return lt;
	}
	
	private class ColisRealise {
		
		Integer position;
		
		Date dateHeureRealise;
		
		ColisRealise(Integer position, String heure) {
			super();
			
			this.position = position;
			try {
				this.dateHeureRealise = DateRules.toTodayTime(heure);
			} catch (ParseException e) {
				throw new RuntimeException("Heure de parsing de date de realisation - bug dans le test");
			}
		}
		
	}
}
