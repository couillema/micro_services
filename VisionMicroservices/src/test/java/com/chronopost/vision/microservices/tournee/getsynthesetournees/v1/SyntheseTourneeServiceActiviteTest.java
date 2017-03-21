package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.assertj.core.util.Maps;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.getsynthesetournees.v1.ColisPoint;
import com.chronopost.vision.model.getsynthesetournees.v1.EAnomalie;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.updatespecificationscolis.v1.EConsigne;
import com.chronopost.vision.model.updatespecificationscolis.v1.EInfoSupplementaire;
import com.chronopost.vision.model.updatespecificationscolis.v1.ESpecificiteColis;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.google.common.collect.Sets;

/**
 * Test des méthodes de SyntheseTourneeServiceImpl retournant l'activité d'une
 * tournée
 * 
 * @author jcbontemps
 */
public class SyntheseTourneeServiceActiviteTest extends SyntheseTourneeTestUtils {

    private final SyntheseTourneeServiceImpl service = SyntheseTourneeServiceImpl.INSTANCE;

	public static final String TOURNEE_ID = "tournee1";
	private static final String EVT_TA = "TA";
	private static final String EVT_RB = "RB";
	private static final String EVT_IP = "IP";
	private static final String EVT_TE = "TE";
	private static final String EVT_D = "D";
	private static final String EVT_P = "P";
	
	private SyntheseTourneeDao mock = mock(SyntheseTourneeDao.class);
	
	final static DateTimeFormatter FORMAT_DATE_COMPLET = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
	final static Date MATIN = FORMAT_DATE_COMPLET.parseDateTime("07/04/2016 06:15:00").toDate();
	final static Date APRES_MIDI = FORMAT_DATE_COMPLET.parseDateTime("07/04/2016 14:31:00").toDate();

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        setTranscos();
		service.setDao(mock);
    }
	
    /**
     * la diffETA est inférieure au depassement_eta_min
     */
    @Test
    public void calculAnomaliesTourneeCas01() {
        ColisPoint colisPoint = new ColisPoint();
        colisPoint.setNo_lt("colis1");
        colisPoint.setCodeEvenement("P");
        colisPoint.setDiffETA(-31);

        Tournee tournee = new Tournee();
        List<PointTournee> points = new ArrayList<PointTournee>();
        PointTournee p = new PointTournee();
        //p.setDiffETA(-31);
        p.setNomDestinataire("diffETA < depassement_eta_min");
        p.setAnomalies(new HashSet<String>());
        points.add(p);

        tournee.setPoints(points);
        tournee.putToColisEvenements("colis1", colisPoint);

        // execution
        colisPoint.setIdentifiantPoint(p.getIdentifiantPoint());
        points = service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(points.get(0).getAnomalies().contains(EAnomalie.HD_ETA.getCode()));
    }

    /**
     * la diffETA est supérieure au depassement_eta_max
     */
    @Test
    public void calculAnomaliesTourneeCas02() {
        ColisPoint colisPoint = new ColisPoint();
        colisPoint.setNo_lt("colis1");
        colisPoint.setCodeEvenement("P");
        colisPoint.setDiffETA("80");

        Tournee tournee = new Tournee();
        List<PointTournee> points = new ArrayList<PointTournee>();
        PointTournee p = new PointTournee();
        //p.setDiffETA(80);
        p.setNomDestinataire("diffETA > depassement_eta_min");
        p.setAnomalies(new HashSet<String>());
        points.add(p);

        colisPoint.setIdentifiantPoint(p.getIdentifiantPoint());

        tournee.setPoints(points);
        tournee.putToColisEvenements("colis1", colisPoint);
        
        // execution
        points = service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(points.get(0).getAnomalies().contains(EAnomalie.HD_ETA.getCode()));
    }

    /**
     * la diffETA est OK
     */
    @Test
    public void calculAnomaliesTourneeCas03() {
        Tournee tournee = new Tournee();
        List<PointTournee> points = new ArrayList<PointTournee>();
        PointTournee p = new PointTournee();
        p.setDiffETA(20);
        p.setNomDestinataire("diffETA OK");
        p.setAnomalies(new HashSet<String>());
        points.add(p);

        tournee.setPoints(points);

        // execution
        points = service.calculAnomaliesTournee(tournee);

        // vérification
        assertFalse(points.get(0).getAnomalies().contains(EAnomalie.HD_ETA.getCode()));
    }

    /**
     * la diffETA est null
     */
    @Test
    public void calculAnomaliesTourneeCas04() {
        Tournee tournee = new Tournee();
        List<PointTournee> points = new ArrayList<PointTournee>();
        PointTournee p = new PointTournee();
        p.setDiffETA(null);
        p.setNomDestinataire("diffETA null");
        p.setAnomalies(new HashSet<String>());
        points.add(p);

        tournee.setPoints(points);

        // execution
        points = service.calculAnomaliesTournee(tournee);

        // vérification
        assertFalse(points.get(0).getAnomalies().contains(EAnomalie.HD_ETA.getCode()));
    }

    /**
     * retour présentation infrucutueuse
     */
    @Test
    public void calculAnomaliesTourneeCas05() {
        Tournee tournee = new Tournee();

        ColisPoint colisPoint = new ColisPoint();
        colisPoint.setNo_lt("colis1");
        colisPoint.setCodeEvenement("P");
        tournee.putToColisEvenements("colis1", colisPoint);

        PointTournee p = new PointTournee();
        p.getColisPrevus().add(colisPoint);
        tournee.addToPoints(p);

        // execution
        List<PointTournee> points = service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(points.get(0).getAnomalies().contains(EAnomalie.RET_PRES_INF.getCode()));
        assertTrue(points.get(0).getColisPrevus().iterator().next().getAnomalies().contains(EAnomalie.RET_PRES_INF.getCode()));
    }

    /**
     * Exclusion BL -> anomalie BL mais pas RETOUR_NON_SAISI
     * complété par le test calculAnomaliesTourneeCas40
     */
    @Test
	public void calculAnomaliesTourneeCas06() {
		Tournee tournee = new Tournee();

		ColisPoint colisPoint = new ColisPoint();
		colisPoint.setNo_lt("colis1");
		colisPoint.setCodeEvenement("TA");
		colisPoint.setDateEvt(new Date());
		tournee.putToColisEvenements("colis1", colisPoint);
		SpecifsColis specifs = new SpecifsColis();
		specifs.addEtape(new Date(), "EXCLUSION|BL||||");
		tournee.putToColisSpecifs("colis1", specifs);

		PointTournee p = new PointTournee();
		p.getColisPrevus().add(colisPoint);
		tournee.addToPoints(p);

		// execution
		List<PointTournee> points = service.calculAnomaliesTournee(tournee);

		// vérification
		assertFalse(points.get(0).getAnomalies().contains(EAnomalie.RET_NON_SAISI.getCode()));
		assertFalse(points.get(0).getColisPrevus().iterator().next().getAnomalies()
				.contains(EAnomalie.RET_NON_SAISI.getCode()));
		assertTrue(points.get(0).getAnomalies().contains(EAnomalie.BL.getCode()));
		assertTrue(points.get(0).getColisPrevus().iterator().next().getAnomalies().contains(EAnomalie.BL.getCode()));
	}

    /**
     * mad avec présentation
     */
    @Test
    public void calculAnomaliesTourneeCas07() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint colisPoint = new ColisPoint();
        colisPoint.setNo_lt("colis1");
        colisPoint.setCodeEvenement("RB");
        colisPoint.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        colisPoint.setIdentifiantPoint("p");
        tournee.putToColisEvenements("colis1", colisPoint);

        PointTournee p = new PointTournee();
        p.setIdentifiantPoint("p");
        p.getColisPrevus().add(colisPoint);
        tournee.addToPoints(p);

        Map<String, SpecifsColis> specifsColis = new HashMap<>();
        specifsColis.put("colis1", new SpecifsColis());
        specifsColis.get("colis1").setConsignesTraitees(new HashMap<Date, String>());
        specifsColis.get("colis1").setSpecifsEvt(new HashMap<Date, String>());
        specifsColis.get("colis1").setConsignesAnnulees(new HashMap<Date, String>());
        specifsColis.get("colis1").setConsignesTraitees(new HashMap<Date, String>());
        specifsColis.get("colis1").setSpecifsEvt(new HashMap<Date, String>());
        specifsColis.get("colis1").getConsignesTraitees().put(formatter.parseDateTime("06/04/2016 21:12:00").toDate(), "|" + EConsigne.REMISE_BUREAU.getCode());
        tournee.setColisSpecifs(specifsColis);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertEquals(Sets.newHashSet(EAnomalie.MAD_SANS_PRES.getCode(), EAnomalie.HD_CONTRAT.getCode()), colisPoint.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.MAD_SANS_PRES.getCode(), EAnomalie.HD_CONTRAT.getCode()), p.getAnomalies());
    }

    /**
     * mad sans présentation
     */
    @Test
    public void calculAnomaliesTourneeCas08() {
        Tournee tournee = new Tournee();

        ColisPoint colisPoint = new ColisPoint();
        colisPoint.setNo_lt("colis1");
        colisPoint.setCodeEvenement("RB");
        colisPoint.setDateEvt(new Date());
        tournee.putToColisEvenements("colis1", colisPoint);

        PointTournee p = new PointTournee();
        p.getColisPrevus().add(colisPoint);
        tournee.addToPoints(p);

        Map<String, SpecifsColis> specifsColis = new HashMap<>();
        specifsColis.put("colis1", new SpecifsColis());
        specifsColis.get("colis1").setSpecifsEvt(new HashMap<Date, String>());
        specifsColis.get("colis1").setConsignesAnnulees(new HashMap<Date, String>());
        specifsColis.get("colis1").setConsignesRecues(new HashMap<Date, String>());
        specifsColis.get("colis1").setConsignesTraitees(new HashMap<Date, String>());
        specifsColis.get("colis1").setSpecifsEvt(new HashMap<Date, String>());
        tournee.setColisSpecifs(specifsColis);

        // execution
        List<PointTournee> points = service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(points.get(0).getAnomalies().contains(EAnomalie.MAD_PRES.getCode()));
        assertTrue(points.get(0).getColisPrevus().iterator().next().getAnomalies().contains(EAnomalie.MAD_PRES.getCode()));
    }

    /**
     * Report info présentation infructueuse sur evt dépot bureau
     */
    @Test
    public void calculAnomaliesTourneeCas09() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setCodeEvenement("TA");

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setCodeEvenement("P");
        evt2.putToInfosSupplementaires("ID_AVIS_PASSAGE", "XYZ123456789AA");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());

        ColisPoint evt3 = new ColisPoint();
        evt3.setNo_lt("colis1");
        evt3.setCodeEvenement("RB");

        PointTournee pt1 = new PointTournee();
        pt1.getColisPrevus().add(evt1);
        PointTournee pt2 = new PointTournee();
        pt2.getColisPresents().add(evt2);
        PointTournee pt3 = new PointTournee();
        pt3.getColisPresents().add(evt3);

        tournee.addToPoints(pt1);
        tournee.addToPoints(pt2);
        tournee.addToPoints(pt3);

        tournee.putToColisEvenements("colis1", evt1);
        tournee.putToColisEvenements("colis1", evt2);
        tournee.putToColisEvenements("colis1", evt3);

        // execution
        List<PointTournee> points = service.calculAnomaliesTournee(tournee);

        // vérification
        ColisPoint evtVerif = points.get(2).getColisPresents().iterator().next();
        assertEquals("XYZ123456789AA", evtVerif.getInfoSupplementaire("ID_AVIS_PASSAGE"));
        assertEquals("P", evtVerif.getPresentationEffectuee(formatter.parseDateTime("07/04/2016 14:31:00").toDate()));
    }

    /**
     * <em>RG-MSGetSyntTournee-510</em> : Vue en retour agence après
     * présentation infructueuse
     */
    @Test
    public void calculAnomaliesTourneeCas10() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 14:00:00").toDate());

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setCodeEvenement("P");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());

        PointTournee pt1 = new PointTournee();
        pt1.getColisPrevus().add(evt1);
        PointTournee pt2 = new PointTournee();
        pt2.getColisPresents().add(evt2);

        tournee.addToPoints(pt1);
        tournee.addToPoints(pt2);

        tournee.putToColisEvenements("colis1", evt1);
        tournee.putToColisEvenements("colis1", evt2);
        tournee.putToColisSpecifs("colis1", new SpecifsColis());
        tournee.getColisSpecifs().get("colis1").getEtapes().put(formatter.parseDateTime("07/04/2016 17:31:00").toDate(), "RETOUR_AGENCE|I");

        // execution
        List<PointTournee> points = service.calculAnomaliesTournee(tournee);

        // vérification
        Map<Date, String> evtRetour = new HashMap<>();
        evtRetour.put(formatter.parseDateTime("07/04/2016 17:31:00").toDate(), "I");
        ColisPoint evtVerif = points.get(1).getColisPresents().iterator().next();
        assertEquals(evtRetour, evtVerif.getVueEnRetour());
    }

    /**
     * <em>RG-MSGetSyntTournee-510</em> : Non Vue en retour agence après
     * présentation infructueuse
     */
    @Test
    public void calculAnomaliesTourneeCas11() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 14:00:00").toDate());

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setCodeEvenement("P");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());

        PointTournee pt1 = new PointTournee();
        pt1.getColisPrevus().add(evt1);
        PointTournee pt2 = new PointTournee();
        pt2.getColisPresents().add(evt2);

        tournee.addToPoints(pt1);
        tournee.addToPoints(pt2);

        tournee.putToColisEvenements("colis1", evt1);
        tournee.putToColisEvenements("colis1", evt2);
        tournee.putToColisSpecifs("colis1", new SpecifsColis());

        // execution
        List<PointTournee> points = service.calculAnomaliesTournee(tournee);

        // vérification
        ColisPoint evtVerif = points.get(1).getColisPresents().iterator().next();
        assertTrue(evtVerif.getVueEnRetour().isEmpty());
    }

    /**
     * <em>RG-MSGetSyntTournee-513</em> : date contractuelle non renseignée
     */
    @Test
    public void calculAnomaliesTourneeCas12() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("RG");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);

        tournee.addToPoints(pt1);
        tournee.addToPoints(pt2);

        tournee.putToColisEvenements("colis1", evt1);
        tournee.putToColisEvenements("colis1", evt2);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(evt1.getAnomalies().isEmpty());
        assertEquals(Sets.newHashSet(EAnomalie.HD_CONTRAT.getCode()), evt2.getAnomalies());
        assertTrue(pt1.getAnomalies().isEmpty());
        assertEquals(Sets.newHashSet(EAnomalie.HD_CONTRAT.getCode()), pt2.getAnomalies());
    }

    /**
     * <em>RG-MSGetSyntTournee-513</em> : non hors date contractuelle
     */
    @Test
    public void calculAnomaliesTourneeCas13() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("RG");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);

        tournee.addToPoints(pt1);
        tournee.addToPoints(pt2);

        tournee.putToColisEvenements("colis1", evt1);
        tournee.putToColisEvenements("colis1", evt2);
        tournee.putToColisSpecifs("colis1", new SpecifsColis());
        tournee.getColisSpecifs().get("colis1").setDatesContractuelles(new HashMap<Date, Date>());
        tournee.getColisSpecifs().get("colis1").getDatesContractuelles()
                .put(formatter.parseDateTime("06/04/2016 15:14:00").toDate(), formatter.parseDateTime("07/04/2016 17:31:00").toDate());

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(evt1.getAnomalies().isEmpty());
        assertEquals(Sets.newHashSet(EAnomalie.HD_CONTRAT.getCode()), evt2.getAnomalies());
        assertTrue(pt1.getAnomalies().isEmpty());
        assertEquals(Sets.newHashSet(EAnomalie.HD_CONTRAT.getCode()), pt2.getAnomalies());
    }

    /**
     * <em>RG-MSGetSyntTournee-513</em> : hors date precocite
     */
    @Test
    public void calculAnomaliesTourneeCas14() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());

        // précocité à 13H pour livraison à 14H -> EAnomalie.HD_CONTRAT
        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("RG");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        evt2.setPrecocite("13H");

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);

        tournee.addToPoints(pt1);
        tournee.addToPoints(pt2);

        tournee.putToColisEvenements("colis1", evt1);
        tournee.putToColisEvenements("colis1", evt2);
        tournee.putToColisSpecifs("colis1", new SpecifsColis());
        tournee.getColisSpecifs().get("colis1").setDatesContractuelles(new HashMap<Date, Date>());
        tournee.getColisSpecifs().get("colis1").getDatesContractuelles()
                .put(formatter.parseDateTime("06/04/2016 15:14:00").toDate(), formatter.parseDateTime("07/04/2016 14:00:00").toDate());

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(evt1.getAnomalies().isEmpty());
        assertTrue(evt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
        assertTrue(pt1.getAnomalies().isEmpty());
        assertTrue(pt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
    }

    /**
     * <em>RG-MSGetSyntTournee-517</em> : MAD non permise
     */
    @Test
    public void calculAnomaliesTourneeCas15() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("RC");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        tournee.putToColisEvenements("colis1", evt2);

        ColisPoint evt3 = new ColisPoint();
        evt3.setNo_lt("colis1");
        evt3.setIdentifiantPoint("pt3");
        evt3.setCodeEvenement("RB");
        evt3.setDateEvt(formatter.parseDateTime("07/04/2016 17:05:00").toDate());
        tournee.putToColisEvenements("colis1", evt3);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);
        tournee.addToPoints(pt2);

        PointTournee pt3 = new PointTournee();
        pt3.setIdentifiantPoint("pt3");
        pt3.getColisPresents().add(evt3);
        tournee.addToPoints(pt3);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertEquals(Sets.newHashSet(), pt1.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.HD_CONTRAT.getCode()), evt2.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.MAD_PRES.getCode(), EAnomalie.MAD_NON_PERMISE.getCode()), pt3.getAnomalies());

        assertEquals(Sets.newHashSet(), evt1.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.HD_CONTRAT.getCode()), pt2.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.MAD_PRES.getCode(), EAnomalie.MAD_NON_PERMISE.getCode()), evt3.getAnomalies());
    }

    /**
     * <em>RG-MSGetSyntTournee-518</em> : Récupération TAXE_VALEUR
     */
    @Test
    public void calculAnomaliesTourneeCas16() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("D");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        tournee.putToColisEvenements("colis1", evt2);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);
        tournee.addToPoints(pt2);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setSpecifsEvt(new HashMap<Date, String>());
        //specifsColis1.setInfoSupp(new HashMap<String, String>());
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), ESpecificiteColis.TAXE.getCode());
        specifsColis1.addInfoSupp(EInfoSupplementaire.TAXE_VALEUR.getCode(), "0123456789");
        tournee.putToColisSpecifs("colis1", specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        Map<String, String> infoSupp = new HashMap<>();
        infoSupp.put(EInfoSupplementaire.TAXE_VALEUR.getCode(), "0123456789");
        assertEquals(Maps.newHashMap(), evt1.getInfosSupplementaires());
        assertEquals(infoSupp, evt2.getInfosSupplementaires());
    }

    /**
     * <em>RG-MSGetSyntTournee-523</em> : SWAP D sans P
     */
    @Test
    public void calculAnomaliesTourneeCas17() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

        String noLtColisAller = "MM912357956FR";
        String noLtColisRetour = "MM912357960FR";
        String identifiantPoint = "ANY74M0405718052016075039";

        Tournee tournee = new Tournee();
        tournee.setIdentifiantTournee("74M0418052016075039");

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt(noLtColisAller);
        evt1.setIdentifiantPoint(identifiantPoint);
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("18/05/2016 06:41:00").toDate());
        tournee.putToColisEvenements(noLtColisAller, evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt(noLtColisAller);
        evt2.setIdentifiantPoint(identifiantPoint);
        evt2.setCodeEvenement("D");
        evt2.setDateEvt(formatter.parseDateTime("18/05/2016 11:03:00").toDate());
        tournee.putToColisEvenements(noLtColisAller, evt2);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint(identifiantPoint);
        pt1.getColisPrevus().add(evt1);
        pt1.getColisPrevus().add(evt2);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setSpecifsEvt(new HashMap<Date, String>());
        //specifsColis1.setInfoSupp(new HashMap<String, String>());
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("17/05/2016 20:12:00").toDate(), ESpecificiteColis.SWAP.getCode());
        specifsColis1.addInfoSupp(EInfoSupplementaire.NO_LT_RETOUR.getCode(), noLtColisRetour);
        tournee.putToColisSpecifs(noLtColisAller, specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertEquals(Sets.newHashSet(), evt1.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.SPEC_SWAP_D_SANS_P.getCode()), evt2.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.SPEC_SWAP_D_SANS_P.getCode()), pt1.getAnomalies());
    }

    /**
     * <em>RG-MSGetSyntTournee-524</em> : SWAP D et P
     */
    @Test
    public void calculAnomaliesTourneeCas18() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

        String noLtColisAller = "MM912357956FR";
        String noLtColisRetour = "MM912357960FR";
        String identifiantPoint = "ANY74M0405718052016075039";

        Tournee tournee = new Tournee();
        tournee.setIdentifiantTournee("74M0418052016075039");

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt(noLtColisAller);
        evt1.setIdentifiantPoint(identifiantPoint);
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("18/05/2016 06:41:00").toDate());
        tournee.putToColisEvenements(noLtColisAller, evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt(noLtColisAller);
        evt2.setIdentifiantPoint(identifiantPoint);
        evt2.setCodeEvenement("D");
        evt2.setDateEvt(formatter.parseDateTime("18/05/2016 11:03:00").toDate());
        tournee.putToColisEvenements(noLtColisAller, evt2);

        ColisPoint evt3 = new ColisPoint();
        evt3.setNo_lt(noLtColisAller);
        evt3.setIdentifiantPoint(identifiantPoint);
        evt3.setCodeEvenement("PE");
        evt3.setDateEvt(formatter.parseDateTime("18/05/2016 11:04:00").toDate());
        tournee.putToColisEvenements(noLtColisAller, evt3);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint(identifiantPoint);
        pt1.getColisPrevus().add(evt1);
        pt1.getColisPrevus().add(evt2);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setSpecifsEvt(new HashMap<Date, String>());
        //specifsColis1.setInfoSupp(new HashMap<String, String>());
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("17/05/2016 20:12:00").toDate(), ESpecificiteColis.SWAP.getCode());
        specifsColis1.addInfoSupp(EInfoSupplementaire.NO_LT_RETOUR.getCode(), noLtColisRetour);
        tournee.putToColisSpecifs(noLtColisAller, specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertEquals(Sets.newHashSet(), evt1.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.SPEC_SWAP_D_ET_P.getCode()), evt2.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.SPEC_SWAP_D_ET_P.getCode()), pt1.getAnomalies());
    }

    /**
     * <em>RG-MSGetSyntTournee-525</em> : SWAP OK
     */
    @Test
    public void calculAnomaliesTourneeCas19() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

        String noLtColisAller = "MM912357956FR";
        String noLtColisRetour = "MM912357960FR";
        String identifiantPoint = "ANY74M0405718052016075039";

        Tournee tournee = new Tournee();
        tournee.setIdentifiantTournee("74M0418052016075039");

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt(noLtColisAller);
        evt1.setIdentifiantPoint(identifiantPoint);
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("18/05/2016 06:41:00").toDate());
        tournee.putToColisEvenements(noLtColisAller, evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt(noLtColisAller);
        evt2.setIdentifiantPoint(identifiantPoint);
        evt2.setCodeEvenement("D");
        evt2.setDateEvt(formatter.parseDateTime("18/05/2016 11:03:00").toDate());
        tournee.putToColisEvenements(noLtColisAller, evt2);

        ColisPoint evt3 = new ColisPoint();
        evt3.setNo_lt(noLtColisRetour);
        evt3.setIdentifiantPoint(identifiantPoint);
        evt3.setCodeEvenement("PE");
        evt3.setDateEvt(formatter.parseDateTime("18/05/2016 11:02:00").toDate());
        tournee.putToColisEvenements(noLtColisRetour, evt3);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint(identifiantPoint);
        pt1.getColisPrevus().add(evt1);
        pt1.getColisPrevus().add(evt2);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setSpecifsEvt(new HashMap<Date, String>());
        //specifsColis1.setInfoSupp(new HashMap<String, String>());
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("17/05/2016 20:12:00").toDate(), ESpecificiteColis.SWAP.getCode());
        specifsColis1.addInfoSupp(EInfoSupplementaire.NO_LT_RETOUR.getCode(), noLtColisRetour);
        tournee.putToColisSpecifs(noLtColisAller, specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertEquals(Sets.newHashSet(), evt1.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.SPEC_SWAP_OK.getCode()), evt2.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.SPEC_SWAP_OK.getCode()), pt1.getAnomalies());
    }

    /**
     * <em>RG-MSGetSyntTournee-519/526</em> : consigne REMISE_BUREAU mais evt
     * presentation domicile
     */
    @Test
    public void calculAnomaliesTourneeCas20() {
    	final String noLt = "colis1";
    	final String idPoint1 = "pt1";
    	final String idPoint2 = "pt2";
    	
        // Initialisation 
        Tournee tournee = new Tournee();
        PointTournee pt1,pt2;
        
        ColisPoint evt1 = newColisPoint(noLt,"TA",idPoint1,MATIN);
        ColisPoint evt2 = newColisPoint(noLt,"D",idPoint2,APRES_MIDI);
        tournee.putToColisEvenements(noLt, evt1);
        tournee.putToColisEvenements(noLt, evt2);

        tournee.addToPoints((pt1=newPointTournee(idPoint1, evt1)));
		tournee.addToPoints((pt2=newPointTournee(idPoint2, evt2)));

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.addConsigneTraitee(MATIN, "12345678|" + EConsigne.REMISE_BUREAU.getCode());
        tournee.putToColisSpecifs(noLt, specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        Map<String, String> infoSupp = new HashMap<>();
        infoSupp.put("CONSIGNE", EConsigne.REMISE_BUREAU.getCode());
        infoSupp.put("ID_CONSIGNE", "12345678");

        assertTrue(evt1.getAnomalies().isEmpty());
        assertEquals(infoSupp, evt1.getInfosSupplementaires());
        assertTrue(EAnomalie.TRAC_CONSIGNE_KO.includeIn(evt2.getAnomalies()));
        assertEquals(infoSupp, evt2.getInfosSupplementaires());
        assertTrue(pt1.getAnomalies().isEmpty());
        assertTrue(EAnomalie.TRAC_CONSIGNE_KO.includeIn(pt2.getAnomalies()));
    }

    /**
     * <em>RG-MSGetSyntTournee-520/526</em> : consigne MISE_A_DISPO_AGENCE mais
     * evt presentation domicile
     */
    @Test
    public void calculAnomaliesTourneeCas21() {
    	final String noLt = "colis1";
    	final String idPoint1 = "pt1";
    	final String idPoint2 = "pt2";
    	
        Tournee tournee = new Tournee();
        PointTournee pt1,pt2;

        ColisPoint evt1 = newColisPoint(noLt,"TA",idPoint1,MATIN);
        ColisPoint evt2 = newColisPoint(noLt,"IP",idPoint2,APRES_MIDI);
        tournee.putToColisEvenements(noLt, evt1);
        tournee.putToColisEvenements(noLt, evt2);

        tournee.addToPoints((pt1=newPointTournee(idPoint1, evt1)));
		tournee.addToPoints((pt2=newPointTournee(idPoint2, evt2)));

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.addConsigneTraitee(MATIN, "12345678|" + EConsigne.MISE_A_DISPO_AGENCE.getCode());
        tournee.putToColisSpecifs(noLt, specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        Map<String, String> infoSupp = new HashMap<>();
        infoSupp.put("CONSIGNE", EConsigne.MISE_A_DISPO_AGENCE.getCode());
        infoSupp.put("ID_CONSIGNE", "12345678");

        assertTrue(evt1.getAnomalies().isEmpty());
        assertEquals(infoSupp, evt1.getInfosSupplementaires());
        assertTrue(EAnomalie.TRAC_CONSIGNE_KO.includeIn(evt2.getAnomalies()));
        assertEquals(infoSupp, evt2.getInfosSupplementaires());

        assertTrue(pt1.getAnomalies().isEmpty());
        assertTrue(EAnomalie.TRAC_CONSIGNE_KO.includeIn(pt2.getAnomalies()));
    }

    /**
     * <em>RG-MSGetSyntTournee-521/526</em> : consigne REMISE_TIERS mais evt mad
     * bureau
     */
    @Test
    public void calculAnomaliesTourneeCas22() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("RB");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        tournee.putToColisEvenements("colis1", evt2);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);
        tournee.addToPoints(pt2);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setConsignesTraitees(new HashMap<Date, String>());
        specifsColis1.getConsignesTraitees().put(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), "12345678|" + EConsigne.REMISE_TIERS.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        Map<String, String> infoSupp = new HashMap<>();
        infoSupp.put("CONSIGNE", EConsigne.REMISE_TIERS.getCode());
        infoSupp.put("ID_CONSIGNE", "12345678");

        assertEquals(Sets.newHashSet(), evt1.getAnomalies());
        assertEquals(infoSupp, evt1.getInfosSupplementaires());
        assertEquals(Sets.newHashSet(EAnomalie.TRAC_CONSIGNE_KO.getCode(), EAnomalie.MAD_PRES.getCode(), EAnomalie.HD_CONTRAT.getCode()), evt2.getAnomalies());
        assertEquals(infoSupp, evt2.getInfosSupplementaires());

        assertEquals(Sets.newHashSet(), pt1.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.TRAC_CONSIGNE_KO.getCode(), EAnomalie.MAD_PRES.getCode(), EAnomalie.HD_CONTRAT.getCode()), pt2.getAnomalies());
    }

    /**
     * <em>RG-MSGetSyntTournee-526</em> : consigne
     */
    @Test
    public void calculAnomaliesTourneeCas23() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("D");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        tournee.putToColisEvenements("colis1", evt2);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);
        tournee.addToPoints(pt2);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setConsignesTraitees(new HashMap<Date, String>());
        specifsColis1.getConsignesTraitees().put(formatter.parseDateTime("07/04/2016 06:15:00").toDate(), "12345678|" + EConsigne.REMISE_TIERS.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        Map<String, String> infoSupp = new HashMap<>();
        infoSupp.put("CONSIGNE", EConsigne.REMISE_TIERS.getCode());
        infoSupp.put("ID_CONSIGNE", "12345678");

        assertEquals(Sets.newHashSet(), evt1.getAnomalies());
        assertEquals(infoSupp, evt1.getInfosSupplementaires());
        assertEquals(Sets.newHashSet(EAnomalie.HD_CONTRAT.getCode()), evt2.getAnomalies());
        assertEquals(infoSupp, evt2.getInfosSupplementaires());

        assertEquals(Sets.newHashSet(), pt1.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.HD_CONTRAT.getCode()), pt2.getAnomalies());
    }

    /**
     * <em>RG-MSGetSyntTournee-522</em> : Recopie ano. EVT_NON_PERMIS sur point
     */
    @Test
    public void calculAnomaliesTourneeCas24() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("R");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        evt2.addToAnomalies(EAnomalie.EVT_NON_PERMIS);
        tournee.putToColisEvenements("colis1", evt2);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);
        tournee.addToPoints(pt2);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertEquals(Sets.newHashSet(), pt1.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.EVT_NON_PERMIS.getCode()), pt2.getAnomalies());
    }

    /**
     * <em>RG-MSGetSyntTournee-527</em>
     * Entrée : Colis avec une spécificité REP et son dernier événement un D
     * Sortie :  PointTournee.anomalies contient la spécifité SPECREP
     * 		     ColisPoint.anomalies contient la spécifité SPECREP
     */
    @Test
    public void calculAnomaliesTourneeCas25() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("D");
        evt1.setDateEvt(formatter.parseDateTime("07/06/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setConsignesTraitees(new HashMap<Date, String>());
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("17/05/2016 20:12:00").toDate(), ESpecificiteColis.REP.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertEquals(Sets.newHashSet(EAnomalie.SPEC_REP.getCode()), pt1.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.SPEC_REP.getCode()), evt1.getAnomalies());
    }

    /**
     * <em>RG-MSGetSyntTournee-528</em>
     * Entrée : Colis avec une spécificité TAXE et son dernier événement un D
     * Sortie :  PointTournee.anomalies contient la spécifité SPECTAXE
     * 		     ColisPoint.anomalies contient la spécifité SPECTAXE
     */
    @Test
    public void calculAnomaliesTourneeCas26() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("D");
        evt1.setDateEvt(formatter.parseDateTime("07/06/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setConsignesTraitees(new HashMap<Date, String>());
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("17/05/2016 20:12:00").toDate(), ESpecificiteColis.TAXE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertEquals(Sets.newHashSet(EAnomalie.SPEC_TAXE.getCode()), pt1.getAnomalies());
        assertEquals(Sets.newHashSet(EAnomalie.SPEC_TAXE.getCode()), evt1.getAnomalies());
    }
    
    
    
    
    /**
     * <em>RG-MSGetSyntTournee-529</em>
     * Entrée : Colis avec une spécificité TAXE et evt IP en métropole
     * Sortie :  PointTournee.anomalies contient la spécifité TRACEVTNONPERMIS
     * 		     ColisPoint.anomalies contient la spécifité TRACEVTNONPERMIS
     */
    @Test
    public void calculAnomaliesTourneeCas27() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("IP");
        evt1.setDateEvt(formatter.parseDateTime("07/06/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setConsignesTraitees(new HashMap<Date, String>());
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("17/05/2016 20:12:00").toDate(), ESpecificiteColis.TAXE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);
        
        // On est en métropole
        tournee.setIdentifiantTournee("75R1220160101091523");

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(pt1.getAnomalies().contains(EAnomalie.EVT_NON_PERMIS.getCode()));
        assertTrue(evt1.getAnomalies().contains(EAnomalie.EVT_NON_PERMIS.getCode()));
    }
    
    
    
    /**
     * <em>RG-MSGetSyntTournee-529</em>
     * Entrée : Colis avec une spécificité SENSIBLE et evt RG en métropole
     * Sortie :  PointTournee.anomalies contient la spécifité TRACEVTNONPERMIS
     * 		     ColisPoint.anomalies contient la spécifité TRACEVTNONPERMIS
     */
    @Test
    public void calculAnomaliesTourneeCas28() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("IP");
        evt1.setDateEvt(formatter.parseDateTime("07/06/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setConsignesTraitees(new HashMap<Date, String>());
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("17/05/2016 20:12:00").toDate(), ESpecificiteColis.SENSIBLE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);
        
        // On est en métropole
        tournee.setIdentifiantTournee("75R1220160101091523");

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(pt1.getAnomalies().contains(EAnomalie.EVT_NON_PERMIS.getCode()));
        assertTrue(evt1.getAnomalies().contains(EAnomalie.EVT_NON_PERMIS.getCode()));
    }
    
    /**
     * <em>RG-MSGetSyntTournee-530</em>
     * Entrée : Colis avec une spécificité SENSIBLE et evt RG en DOM
     * Sortie :  PointTournee.anomalies ne contient pas la spécifité TRACEVTNONPERMIS
     * 		     ColisPoint.anomalies ne contient pas la spécifité TRACEVTNONPERMIS
     */
    @Test
    public void calculAnomaliesTourneeCas29() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("IP");
        evt1.setDateEvt(formatter.parseDateTime("07/06/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setConsignesTraitees(new HashMap<Date, String>());
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("17/05/2016 20:12:00").toDate(), ESpecificiteColis.SENSIBLE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);
        
        // On est en DOM
        tournee.setIdentifiantTournee("97R1220160101091523");

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertFalse(pt1.getAnomalies().contains(EAnomalie.EVT_NON_PERMIS.getCode()));
        assertFalse(evt1.getAnomalies().contains(EAnomalie.EVT_NON_PERMIS.getCode()));
    }
    
    /**
     * <em>RG-MSGetSyntTournee-530</em>
     * Entrée : Colis avec une spécificité TAXE et evt RG en DOM
     * Sortie :  PointTournee.anomalies  contient  la spécifité TRACEVTNONPERMIS
     * 		     ColisPoint.anomalies  contient  la spécifité TRACEVTNONPERMIS
     */
    @Test
    public void calculAnomaliesTourneeCas30() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt1");
        evt1.setCodeEvenement("IP");
        evt1.setDateEvt(formatter.parseDateTime("07/06/2016 06:15:00").toDate());
        tournee.putToColisEvenements("colis1", evt1);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.setConsignesTraitees(new HashMap<Date, String>());
        specifsColis1.getSpecifsEvt().put(formatter.parseDateTime("17/05/2016 20:12:00").toDate(), ESpecificiteColis.TAXE.getCode());
        tournee.putToColisSpecifs("colis1", specifsColis1);
        
        // On est en DOM
        tournee.setIdentifiantTournee("97R1220160101091523");

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertFalse(pt1.getAnomalies().contains(EAnomalie.EVT_NON_PERMIS.getCode()));
        assertFalse(evt1.getAnomalies().contains(EAnomalie.EVT_NON_PERMIS.getCode()));
    }

    /**
     * <em>RG-MSGetSyntTournee-533</em> : precocite RDV, Date creneau contractuel
     */
    @Test
    public void calculAnomaliesTourneeCas31() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 13:15:00").toDate());

        // précocité à 13H pour livraison à 14H -> EAnomalie.HD_CONTRAT
        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("RG");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        evt2.setPrecocite("RDV");
        evt2.getInfosSupplementaires().put(EInfoSupplementaire.CRENEAU_DEBUT_CONTRACTUEL.getCode(), "30/11/2016 11:00");
        evt2.getInfosSupplementaires().put(EInfoSupplementaire.CRENEAU_FIN_CONTRACTUEL.getCode(), "30/11/2016 13:00");

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);

        tournee.addToPoints(pt1);
        tournee.addToPoints(pt2);

        tournee.putToColisEvenements("colis1", evt1);
        tournee.putToColisEvenements("colis1", evt2);
        tournee.putToColisSpecifs("colis1", new SpecifsColis());
        tournee.getColisSpecifs().get("colis1").setDatesContractuelles(new HashMap<Date, Date>());
        tournee.getColisSpecifs().get("colis1").getDatesContractuelles()
                .put(formatter.parseDateTime("06/04/2016 15:14:00").toDate(), formatter.parseDateTime("07/04/2016 14:00:00").toDate());

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(evt1.getAnomalies().isEmpty());
        assertTrue(evt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
        assertTrue(pt1.getAnomalies().isEmpty());
        assertTrue(pt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
    }

    /**
     * <em>RG-MSGetSyntTournee-533</em> : precocite RDV, pas de créneau, utilise code service
     */
    @Test
    public void calculAnomaliesTourneeCas32() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 13:15:00").toDate());

        // précocité à 13H pour livraison à 14H -> EAnomalie.HD_CONTRAT
        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("RG");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        evt2.setPrecocite("RDV");

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);

        tournee.addToPoints(pt1);
        tournee.addToPoints(pt2);

        tournee.putToColisEvenements("colis1", evt1);
        tournee.putToColisEvenements("colis1", evt2);
        tournee.putToColisSpecifs("colis1", new SpecifsColis());
        tournee.getColisSpecifs().get("colis1").setDatesContractuelles(new HashMap<Date, Date>());
        tournee.getColisSpecifs().get("colis1").getDatesContractuelles()
                .put(formatter.parseDateTime("06/04/2016 15:14:00").toDate(), formatter.parseDateTime("07/04/2016 14:00:00").toDate());
        Map<Date, Set<String>> specifsService = new HashMap<>();
        specifsService.put(formatter.parseDateTime("07/04/2016 14:31:00").toDate(), new HashSet<String>(Arrays.asList("CR13001500")));
		tournee.getColisSpecifs().get("colis1").setSpecifsService(specifsService);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(evt1.getAnomalies().isEmpty());
        assertTrue(evt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
        assertTrue(pt1.getAnomalies().isEmpty());
        assertTrue(pt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
    }

    /**
     * <em>RG-MSGetSyntTournee-533</em> : precocite RDV, date limite 13h00 par défaut
     */
    @Test
    public void calculAnomaliesTourneeCas33() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt("colis1");
        evt1.setIdentifiantPoint("pt");
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime("07/04/2016 13:15:00").toDate());

        // précocité à 13H pour livraison à 14H -> EAnomalie.HD_CONTRAT
        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt("colis1");
        evt2.setIdentifiantPoint("pt2");
        evt2.setCodeEvenement("RG");
        evt2.setDateEvt(formatter.parseDateTime("07/04/2016 14:31:00").toDate());
        evt2.setPrecocite("RDV");

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint("pt1");
        pt1.getColisPrevus().add(evt1);

        PointTournee pt2 = new PointTournee();
        pt2.setIdentifiantPoint("pt2");
        pt2.getColisPresents().add(evt2);

        tournee.addToPoints(pt1);
        tournee.addToPoints(pt2);

        tournee.putToColisEvenements("colis1", evt1);
        tournee.putToColisEvenements("colis1", evt2);
        tournee.putToColisSpecifs("colis1", new SpecifsColis());
        tournee.getColisSpecifs().get("colis1").setDatesContractuelles(new HashMap<Date, Date>());
        tournee.getColisSpecifs().get("colis1").getDatesContractuelles()
                .put(formatter.parseDateTime("06/04/2016 15:14:00").toDate(), formatter.parseDateTime("07/04/2016 14:00:00").toDate());

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(evt1.getAnomalies().isEmpty());
        assertTrue(evt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
        assertTrue(pt1.getAnomalies().isEmpty());
        assertTrue(pt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
    }
    
    /**
     * <em>RG-MSGetSyntTournee-513</em>
     * Entrée : Colis avec precocité RDV et creneau défini (TA) + 1 evt D dans le créneau
     * Sortie :  PointTournee.anomalies ne contient pas la spécifité HD_CONTRAT
     * 		     ColisPoint.anomalies ne contient pas la spécifité HD_CONTRAT
     */
    @Test
    public void calculAnomaliesTourneeCas35() {
    	final String noLt = "colis35";
    	final String idPoint = "pt35";
    	DateTimeFormatter jourFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    	final String ceJour = jourFormatter.print(new DateTime());
    	
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt(noLt);
        evt1.setIdentifiantPoint(idPoint);
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime(ceJour+" 06:15:00").toDate());
        evt1.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_DEBUT,"10:30");
        evt1.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_FIN,"12:30");
        evt1.setPrecocite("RDV");
        tournee.putToColisEvenements(noLt, evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt(noLt);
        evt2.setIdentifiantPoint(idPoint);
        evt2.setCodeEvenement("D");
        evt2.setDateEvt(formatter.parseDateTime(ceJour+" 12:15:00").toDate());
        tournee.putToColisEvenements(noLt, evt2);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint(idPoint);
        pt1.getColisPrevus().add(evt1);
        pt1.getColisPresents().add(evt2);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.getDatesContractuelles().put(formatter.parseDateTime(ceJour+" 01:15:00").toDate(), formatter.parseDateTime(ceJour+" 11:15:00").toDate());
        tournee.putToColisSpecifs(noLt, specifsColis1);
        
        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertFalse(pt1.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
        assertFalse(evt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
    }
    
    
    /**
     * <em>RG-MSGetSyntTournee-513</em>
     * Entrée : Colis avec precocité RDV et creneau défini (TA) + 1 evt D hors créneau
     * Sortie :  PointTournee.anomalies  contient  la spécifité HD_CONTRAT
     * 		     ColisPoint.anomalies  contient  la spécifité HD_CONTRAT
     */
    @Test
    public void calculAnomaliesTourneeCas36() {
    	final String noLt = "colis36";
    	final String idPoint = "pt36";
    	DateTimeFormatter jourFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    	final String ceJour = jourFormatter.print(new DateTime());
    	
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt(noLt);
        evt1.setIdentifiantPoint(idPoint);
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime(ceJour+" 06:15:00").toDate());
        evt1.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_DEBUT,"10:30");
        evt1.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_FIN,"12:30");
        evt1.setPrecocite("RDV");
        tournee.putToColisEvenements(noLt, evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt(noLt);
        evt2.setIdentifiantPoint(idPoint);
        evt2.setCodeEvenement("D");
        evt2.setDateEvt(formatter.parseDateTime(ceJour+" 09:15:00").toDate());
        tournee.putToColisEvenements(noLt, evt2);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint(idPoint);
        pt1.getColisPrevus().add(evt1);
        pt1.getColisPresents().add(evt2);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.getDatesContractuelles().put(formatter.parseDateTime(ceJour+" 01:15:00").toDate(), formatter.parseDateTime(ceJour+" 15:15:00").toDate());
        tournee.putToColisSpecifs(noLt, specifsColis1);
        
        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(pt1.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
        assertTrue(evt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
    }
    
    /**
     * <em>RG-MSGetSyntTournee-513</em>
     * Entrée : Colis avec precocité 18H et creneau défini (TA) + 1 evt D hors créneau mais avant 18heure
     * Sortie :  PointTournee.anomalies  ne contient pas la spécifité HD_CONTRAT
     * 		     ColisPoint.anomalies ne contient pas la spécifité HD_CONTRAT
     */
    @Test
    public void calculAnomaliesTourneeCas37() {
    	final String noLt = "colis37";
    	final String idPoint = "pt37";
    	DateTimeFormatter jourFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    	final String ceJour = jourFormatter.print(new DateTime());
    	
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt(noLt);
        evt1.setIdentifiantPoint(idPoint);
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime(ceJour+" 06:15:00").toDate());
        evt1.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_DEBUT,"10:30");
        evt1.addInfoSupplementaire(EInfoSupplementaire.CRENEAU_FIN,"12:30");
        evt1.setPrecocite("18H");
        tournee.putToColisEvenements(noLt, evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt(noLt);
        evt2.setIdentifiantPoint(idPoint);
        evt2.setCodeEvenement("D");
        evt2.setDateEvt(formatter.parseDateTime(ceJour+" 09:15:00").toDate());
        tournee.putToColisEvenements(noLt, evt2);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint(idPoint);
        pt1.getColisPrevus().add(evt1);
        pt1.getColisPresents().add(evt2);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.getDatesContractuelles().put(formatter.parseDateTime(ceJour+" 01:15:00").toDate(), formatter.parseDateTime(ceJour+" 18:00:00").toDate());
        tournee.putToColisSpecifs(noLt, specifsColis1);
        
        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertFalse(pt1.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
        assertFalse(evt2.getAnomalies().contains(EAnomalie.HD_CONTRAT.getCode()));
    }
    
    /**
     * <em>RG-MSGetSyntTournee-535</em>
     * Entrée : Colis avec dernier evt TA + evt Exclusion BLOQUE (bloqué)
     * Sortie :  PointTournee.anomalies contient la spécifité BLOQUE
     * 		     ColisPoint.anomalies contient la spécifité BLOQUE
     */
    @Test
    public void calculAnomaliesTourneeCas38() {
    	final String noLt = "colis38";
    	final String idPoint = "pt38";
    	DateTimeFormatter jourFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    	final String ceJour = jourFormatter.print(new DateTime());
    	
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt(noLt);
        evt1.setIdentifiantPoint(idPoint);
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime(ceJour+" 11:15:00").toDate());
        evt1.setPrecocite("18H");
        tournee.putToColisEvenements(noLt, evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt(noLt);
        evt2.setIdentifiantPoint(idPoint);
        evt2.setCodeEvenement("TE");
        evt2.setDateEvt(formatter.parseDateTime(ceJour+" 12:15:00").toDate());
        evt2.setPrecocite("18H");
        tournee.putToColisEvenements(noLt, evt2);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint(idPoint);
        pt1.getColisPrevus().add(evt1);
        pt1.getColisPresents().add(evt1);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.addEtape(formatter.parseDateTime(ceJour+" 16:15:00").toDate(), "EXCLUSION|BL||||");
        tournee.putToColisSpecifs(noLt, specifsColis1);
        
        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(pt1.getAnomalies().contains(EAnomalie.BL.getCode()));
        assertTrue(evt1.getAnomalies().contains(EAnomalie.BL.getCode()));
    }
    
	/**
	 * <em>RG-MSGetSyntTournee-535</em> Entrée : Colis avec TA + dernier evt P +
	 * evt Exclusion BLOQUE (bloqué) Sortie : PointTournee.anomalies ne contient
	 * pas la spécifité BLOQUE car TA n'est pas le dernier evt
	 * ColisPoint.anomalies contient la spécifité BLOQUE car TA n'est pas le
	 * dernier evt
	 */
    @Test
    public void calculAnomaliesTourneeCas39() {
    	final String noLt = "colis39";
    	final String idPoint = "pt39";
    	DateTimeFormatter jourFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
    	final String ceJour = jourFormatter.print(new DateTime());
    	
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        Tournee tournee = new Tournee();

        ColisPoint evt1 = new ColisPoint();
        evt1.setNo_lt(noLt);
        evt1.setIdentifiantPoint(idPoint);
        evt1.setCodeEvenement("TA");
        evt1.setDateEvt(formatter.parseDateTime(ceJour+" 11:15:00").toDate());
        evt1.setPrecocite("18H");
        tournee.putToColisEvenements(noLt, evt1);

        ColisPoint evt2 = new ColisPoint();
        evt2.setNo_lt(noLt);
        evt2.setIdentifiantPoint(idPoint);
        evt2.setCodeEvenement("P");
        evt2.setDateEvt(formatter.parseDateTime(ceJour+" 15:15:00").toDate());
        evt2.setPrecocite("18H");
        tournee.putToColisEvenements(noLt, evt2);

        PointTournee pt1 = new PointTournee();
        pt1.setIdentifiantPoint(idPoint);
        pt1.getColisPrevus().add(evt1);
        pt1.getColisPresents().add(evt1);
        tournee.addToPoints(pt1);

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.addEtape(formatter.parseDateTime(ceJour+" 14:15:00").toDate(), "EXCLUSION|BL||||");
        tournee.putToColisSpecifs(noLt, specifsColis1);
        
        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        assertFalse(pt1.getAnomalies().contains(EAnomalie.BL.getCode()));
        assertFalse(evt1.getAnomalies().contains(EAnomalie.BL.getCode()));
    }

    /**
     * Exclusion non BL -> anomalie RETOUR_NON_SAISI mais pas BL
     * compléte le test calculAnomaliesTourneeCas06
     */
    @Test
    public void calculAnomaliesTourneeCas40() {
        Tournee tournee = new Tournee();

        ColisPoint colisPoint = new ColisPoint();
        colisPoint.setNo_lt("colis1");
        colisPoint.setCodeEvenement("TA");
        colisPoint.setDateEvt(new Date());
        tournee.putToColisEvenements("colis1", colisPoint);
        SpecifsColis specifs = new SpecifsColis();
        specifs.addEtape(new Date(), "EXCLUSION|||||");
		tournee.putToColisSpecifs("colis1", specifs);
        
        PointTournee p = new PointTournee();
        p.getColisPrevus().add(colisPoint);
        tournee.addToPoints(p);

        // execution
        List<PointTournee> points = service.calculAnomaliesTournee(tournee);

        // vérification
        assertTrue(points.get(0).getAnomalies().contains(EAnomalie.RET_NON_SAISI.getCode()));
        assertTrue(points.get(0).getColisPrevus().iterator().next().getAnomalies().contains(EAnomalie.RET_NON_SAISI.getCode()));
        assertFalse(points.get(0).getAnomalies().contains(EAnomalie.BL.getCode()));
        assertFalse(points.get(0).getColisPrevus().iterator().next().getAnomalies().contains(EAnomalie.BL.getCode()));
    }
    
    /**
     * Test de la méthode getSyntheseTourneeActivite
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @Test
	public void getSyntheseTourneeActiviteCas41() throws InterruptedException, ExecutionException {
		Mockito.when(mock.getPointsTournee(TOURNEE_ID)).thenReturn(getPointsTournee());
		List<PointTournee> points = service.getSyntheseTourneeActivite(TOURNEE_ID);
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
	}

    /**
     * <em>RG-MSGetSyntTournee 526</em> : consigne REMISE_BUREAU mais evt
     * presentation domicile avec un id_consigne null
     * 
     * Entree : colis avec un identifiant de consigne null
     * Sortie : id de la consigne égale à 0
     */
    @Test
    public void calculAnomaliesTourneeCas42() {
    	final String noLt1 = "colis1";
    	final String idPoint1 = "pt1";
    	final String idPoint2 = "pt2";
    	
        // Initialisation 
        Tournee tournee = new Tournee();
        
        ColisPoint evt1 = newColisPoint(noLt1,"TA",idPoint1,MATIN);
        ColisPoint evt2 = newColisPoint(noLt1,"D",idPoint2,APRES_MIDI);
        tournee.putToColisEvenements(noLt1, evt1);
        tournee.putToColisEvenements(noLt1, evt2);

        tournee.addToPoints((newPointTournee(idPoint1, evt1)));

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.addConsigneTraitee(MATIN, "|" + EConsigne.REMISE_BUREAU.getCode());
        tournee.putToColisSpecifs(noLt1, specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        Map<String, String> infoSupp = new HashMap<>();
        infoSupp.put("CONSIGNE", EConsigne.REMISE_BUREAU.getCode());
        infoSupp.put("ID_CONSIGNE", "0");

        assertTrue(evt1.getAnomalies().isEmpty());
        assertEquals(infoSupp, evt1.getInfosSupplementaires());
    }
  
  /**
     * <em>RG-MSGetSyntTournee 526</em> : consigne REMISE_BUREAU mais evt
     * presentation domicile avec un id_consigne null
     * 
     * Entree : colis avec un identifiant de consigne égale à la chaîne de caractère "null"
     * Sortie : id de la consigne égale à 0
     */
    @Test
    public void calculAnomaliesTourneeCas43() {
    	final String noLt1 = "colis1";
    	final String idPoint1 = "pt1";
    	final String idPoint2 = "pt2";
    	
        // Initialisation 
        Tournee tournee = new Tournee();
        
        ColisPoint evt1 = newColisPoint(noLt1,"TA",idPoint1,MATIN);
        ColisPoint evt2 = newColisPoint(noLt1,"D",idPoint2,APRES_MIDI);
        tournee.putToColisEvenements(noLt1, evt1);
        tournee.putToColisEvenements(noLt1, evt2);

        tournee.addToPoints((newPointTournee(idPoint1, evt1)));

        SpecifsColis specifsColis1 = new SpecifsColis();
        specifsColis1.addConsigneTraitee(MATIN, "null|" + EConsigne.REMISE_BUREAU.getCode());
        tournee.putToColisSpecifs(noLt1, specifsColis1);

        // execution
        service.calculAnomaliesTournee(tournee);

        // vérification
        Map<String, String> infoSupp = new HashMap<>();
        infoSupp.put("CONSIGNE", EConsigne.REMISE_BUREAU.getCode());
        infoSupp.put("ID_CONSIGNE", "0");

        assertTrue(evt1.getAnomalies().isEmpty());
        assertEquals(infoSupp, evt1.getInfosSupplementaires());
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
     * Retourne un colisPoint initialisé 
     * @param noLt
     * @param evtCode
     * @param idPoint
     * @param date
     * @return
     *
     * @author LGY
     */
    private ColisPoint newColisPoint(final String noLt, final String evtCode,final String idPoint,final Date date){
    	ColisPoint evt = new ColisPoint();
    	evt.setNo_lt(noLt);
    	evt.setIdentifiantPoint(idPoint);
    	evt.setCodeEvenement(evtCode);
    	evt.setDateEvt(date);
    	return evt; 
    }
    
    /**
     * Retourne un pointTournee initialisé
     * @param idPoint
     * @param evt
     * @return
     *
     * @author LGY
     */
    private PointTournee newPointTournee(final String idPoint, final ColisPoint evt){
        PointTournee pt = new PointTournee();
        pt.setIdentifiantPoint(idPoint);
        if ("TA".equals(evt.getCodeEvenement()))
        		pt.getColisPrevus().add(evt);
        else
        		pt.getColisPresents().add(evt);
        
        return pt;
    }
}
