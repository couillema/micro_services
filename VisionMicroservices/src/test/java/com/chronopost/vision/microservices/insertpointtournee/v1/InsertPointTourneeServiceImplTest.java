package com.chronopost.vision.microservices.insertpointtournee.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.cachemanager.CacheManager;
import com.chronopost.vision.microservices.tournee.getdetailtournee.v1.Tournee;
import com.chronopost.vision.model.Agence;
import com.chronopost.vision.model.EInfoComp;
import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.rules.EvtRules;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;

/** @author unknown : JJC port */
public class InsertPointTourneeServiceImplTest {
	
    private IInsertPointTourneeDao mockDao;
	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
    
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        mockDao = Mockito.mock(IInsertPointTourneeDao.class);
        InsertPointTourneeServiceImpl.INSTANCE.setDao(mockDao);

        setTranscos("|TA|D|");
        setCacheAgence();
    }

    @Test
    /**
     * Test 1: on fournit 3 evts D,TA et PC au service
     * 
     *  Attendu : seuls 2 evts (D et TA) doivent générer un appel à la Dao.
     *  
     */
    public void traiteEvenementCas1() throws InterruptedException, ExecutionException {
        /* Initialisation */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt("no_lt", "TA", new Date()));
        evts.add(newEvt("no_lt", "D", new Date()));
        evts.add(newEvt("no_lt", "PC", new Date()));

        Mockito.when(mockDao.addEvtDansPoint(Mockito.anyListOf(Evt.class))).thenReturn(true);
        Mockito.when(mockDao.trouverDernierEvtTA(Mockito.any(Evt.class))).thenReturn(null);
        Mockito.when(mockDao.trouverDerniereTournee(Mockito.any(Evt.class))).thenReturn(null);

        /* Running */
        InsertPointTourneeServiceImpl.INSTANCE.traiteEvenement(evts);

        /* Vérifications */
        Mockito.verify(mockDao, Mockito.times(1)).addEvtDansPoint(Mockito.anyListOf(Evt.class));
    }

    @Test
    /**
     * Test 1: on fournit 3 evts D  au service, dont 2 fictifs
     * 
     *  Attendu : on doit avoir 1 appel à addEvt et 2 appels à MajTournee
     *  
     */
    public void traiteEvenementCas2() throws InterruptedException, ExecutionException {
        /* Initialisation */
        List<Evt> evts = new ArrayList<>();
        evts.add(newEvt("no_lt", "D", new Date()));
        evts.add(newEvt(EvtRules.COLIS_FICTIF_DEBUT_TOURNEE, "D", new Date()));
        evts.add(newEvt(EvtRules.COLIS_FICTIF_FIN_TOURNEE, "D", new Date()));

        Mockito.when(mockDao.addEvtDansPoint(Mockito.anyListOf(Evt.class))).thenReturn(true);
        Mockito.when(mockDao.miseAJourTournee(Mockito.anyListOf(Evt.class))).thenReturn(true);

        /* Running */
        InsertPointTourneeServiceImpl.INSTANCE.traiteEvenement(evts);

        /* Vérifications */
        Mockito.verify(mockDao, Mockito.times(2)).addEvtDansPoint(Mockito.anyListOf(Evt.class));
        Mockito.verify(mockDao, Mockito.times(2)).miseAJourTournee(Mockito.anyListOf(Evt.class));
    }

    @Test
    /**
     * Un Evt (non TA) sans idPointC11, on retrouve une TA avec idPointC11 le même jour pour ce colis et sur le meme code tournee.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public void computeIdPointC11Cas1() {
        /* Initialisation */
        Evt evtD = newEvt("MM912357956FR", "D", formatter.parseDateTime("18/05/2016 11:03:00").toDate(), "75999", "75P14");
        
        Map<String, String> infoCompTA = new HashMap<>();
        infoCompTA.put(EInfoComp.ID_POINT_C11.getCode(), "PCO75P1400118052016090715");
        Evt evtTA = newEvt("MM912357956FR", "TA", formatter.parseDateTime("18/05/2016 06:41:00").toDate(), "75999", "75P14", infoCompTA);

        /* Mock DAO */
        Mockito.when(mockDao.trouverDernierEvtTA(Mockito.any(Evt.class))).thenReturn(evtTA);

        /* Running */
        InsertPointTourneeServiceImpl.INSTANCE.computeIdPointC11(evtD);

        /* Vérifications */
        Map<String, String> infoCompD = new HashMap<>();
        infoCompD.put(EInfoComp.ID_POINT_C11.getCode(), "PCO75P14Sb318052016090715");

        assertEquals(evtD.getInfoscomp(), infoCompD);
    }

    @Test
    /**
     * Un Evt (non TA) sans idPointC11, on retrouve une TA avec idPointC11 le même jour pour ce colis mais sur un autre code tournee.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public void computeIdPointC11Cas8() {
        /* Initialisation */
        Evt evtD = newEvt("MM912357956FR", "D", formatter.parseDateTime("18/05/2016 11:03:00").toDate(), "75999", "75P14");
        evtD.setHeureDebutPoint("1002");
        evtD.setDateTournee("20160616");
        
        Map<String, String> infoCompTA = new HashMap<>();
        infoCompTA.put(EInfoComp.ID_POINT_C11.getCode(), "PCO75P1400118052016090715");
        Evt evtTA = newEvt("MM912357956FR", "TA", formatter.parseDateTime("18/05/2016 06:41:00").toDate(), "75999", "75P22", infoCompTA);

        /* Mock DAO */
        Mockito.when(mockDao.trouverDernierEvtTA(Mockito.any(Evt.class))).thenReturn(evtTA);
        Mockito.when(mockDao.trouverDerniereTournee(Mockito.any(Evt.class))).thenReturn(null);

        /* Running */
        InsertPointTourneeServiceImpl.INSTANCE.computeIdPointC11(evtD);

        /* Vérifications */
        Map<String, String> infoCompD = new HashMap<>();
        infoCompD.put(EInfoComp.ID_POINT_C11.getCode(), "PCO75P14Sa216062016000000");

        assertEquals(evtD.getInfoscomp(), infoCompD);
    }
    /**
     * Un Evt (non TA) sans idPointC11, on ne retrouve pas de TA le même jour
     * pour ce colis, mais on trouve une tournée pour l'agence, le code tournée,
     * et le jour.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void computeIdPointC11Cas2() {
        /* Initialisation */
        Evt evtD = newEvt("MM912357956FR", "D", formatter.parseDateTime("18/05/2016 11:03:00").toDate(), "75999", "75P14");
        Tournee tournee = newTournee("75999", formatter.parseDateTime("18/05/2016 09:07:15").toDate(), "75P1418052016090715");

        /* Mock DAO */
        Mockito.when(mockDao.trouverDernierEvtTA(Mockito.any(Evt.class))).thenReturn(null);
        Mockito.when(mockDao.trouverDerniereTournee(Mockito.any(Evt.class))).thenReturn(tournee);

        /* Running */
        InsertPointTourneeServiceImpl.INSTANCE.computeIdPointC11(evtD);

        /* Vérifications */
        Map<String, String> infoCompD = new HashMap<>();
        infoCompD.put(EInfoComp.ID_POINT_C11.getCode(), "PCO75P14Sb318052016090715");

        assertEquals(evtD.getInfoscomp(), infoCompD);
    }
    
    /**
     * Un Evt (non TA) sans idPointC11, on ne retrouve pas de TA le même jour
     * pour ce colis, mais on trouve une tournée pour l'agence, le code tournée,
     * et le jour. 
     * 
     * Identique au Cas2 mais avec un idC11+
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void computeIdPointC11Cas2_2() {
        /* Initialisation */
        Evt evtD = newEvt("MM912357956FR", "D", formatter.parseDateTime("18/05/2016 11:03:00").toDate(), "75999", "75P14");
        Tournee tournee = newTournee("75999", formatter.parseDateTime("18/05/2016 09:07:15").toDate(), "PCO75P1418052016090715");

        /* Mock DAO */
        Mockito.when(mockDao.trouverDernierEvtTA(Mockito.any(Evt.class))).thenReturn(null);
        Mockito.when(mockDao.trouverDerniereTournee(Mockito.any(Evt.class))).thenReturn(tournee);

        /* Running */
        InsertPointTourneeServiceImpl.INSTANCE.computeIdPointC11(evtD);

        /* Vérifications */
        Map<String, String> infoCompD = new HashMap<>();
        infoCompD.put(EInfoComp.ID_POINT_C11.getCode(), "PCO75P14Sb318052016090715");

        assertEquals(evtD.getInfoscomp(), infoCompD);
    }

    /**
     * Un Evt (non TA) sans idPointC11, on ne retrouve ni TA ni tournée.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void computeIdPointC11Cas3() {
        /* Initialisation */
        Evt evtD = newEvt("MM912357956FR", "D", formatter.parseDateTime("18/05/2016 11:03:00").toDate(), "75999", "75P14");
        evtD.setHeureDebutPoint("1002");
        evtD.setDateTournee("20160616");

        /* Mock DAO */
        Mockito.when(mockDao.trouverDernierEvtTA(Mockito.any(Evt.class))).thenReturn(null);
        Mockito.when(mockDao.trouverDerniereTournee(Mockito.any(Evt.class))).thenReturn(null);

        /* Running */
        InsertPointTourneeServiceImpl.INSTANCE.computeIdPointC11(evtD);

        /* Vérifications */
        Map<String, String> infoCompD = new HashMap<>();
        infoCompD.put(EInfoComp.ID_POINT_C11.getCode(), "PCO75P14Sa216062016000000");

        assertEquals(evtD.getInfoscomp(), infoCompD);
    }

    /**
     * Un Evt TA sans idPointC11, on trouve une tournée pour l'agence, le code
     * tournée, et le jour.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void computeIdPointC11Cas4() {
        /* Initialisation */
        Evt evtD = newEvt("MM912357956FR", "D", formatter.parseDateTime("18/05/2016 11:03:00").toDate(), "75999", "75P14");
        Tournee tournee = newTournee("75999", formatter.parseDateTime("18/05/2016 09:07:15").toDate(), "75P1418052016090715");

        /* Mock DAO */
        Mockito.when(mockDao.trouverDerniereTournee(Mockito.any(Evt.class))).thenReturn(tournee);

        /* Running */
        InsertPointTourneeServiceImpl.INSTANCE.computeIdPointC11(evtD);

        /* Vérifications */
        Map<String, String> infoCompD = new HashMap<>();
        infoCompD.put(EInfoComp.ID_POINT_C11.getCode(), "PCO75P14Sb318052016090715");

        assertEquals(evtD.getInfoscomp(), infoCompD);
    }

    /**
     * Un Evt TA sans idPointC11, on ne retrouve pas de tournée.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void computeIdPointC11Cas5() {
        /* Initialisation */
        Evt evtTA = newEvt("MM912357956FR", "TA", formatter.parseDateTime("18/05/2016 11:03:00").toDate(), "75999", "75P14");

        /* Mock DAO */
        Mockito.when(mockDao.trouverDerniereTournee(Mockito.any(Evt.class))).thenReturn(null);

        /* Running */
        InsertPointTourneeServiceImpl.INSTANCE.computeIdPointC11(evtTA);

        /* Vérifications */
        Map<String, String> infoCompD = new HashMap<>();
        infoCompD.put(EInfoComp.ID_POINT_C11.getCode(), "PCO75P14Sb318052016000000");

        assertEquals(evtTA.getInfoscomp(), infoCompD);
    }

    /**
     * Entree: Un Evt (non TA) sans idPointC11, on retrouve une TA avec idC11 le même
     * jour pour ce colis. L'Evt n'a pas de lieuEvt.
     * 
     * Attendu : 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void computeIdPointC11Cas6() {
        /* Initialisation */
        Evt evtD = newEvt("MM912357956FR", "D", formatter.parseDateTime("18/05/2016 11:03:00").toDate());
        
        Map<String, String> infoCompTA = new HashMap<>();
        infoCompTA.put(EInfoComp.ID_C11.getCode(), "75P1418052016090715");
        Evt evtTA = newEvt("MM912357956FR", "TA", formatter.parseDateTime("18/05/2016 06:41:00").toDate(), "75999", "75P14", infoCompTA);

        /* Mock DAO */
        Mockito.when(mockDao.trouverDernierEvtTA(Mockito.any(Evt.class))).thenReturn(evtTA);

        /* Running */
        boolean result = InsertPointTourneeServiceImpl.INSTANCE.computeIdPointC11(evtD);
        
        assertFalse(result);
        assertTrue(evtD.getInfoscomp().isEmpty());
    }

    /**
     * Un Evt (non TA) sans idPointC11, on retrouve une TA avec idC11 le même
     * jour pour ce colis. L'Evt a un lieuEvt qui n'existe pas dans la table
     * agence.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void computeIdPointC11Cas7() {
        /* Initialisation */
        Evt evtD = newEvt("MM912357956FR", "D", formatter.parseDateTime("18/05/2016 11:03:00").toDate(), "RU001", null);
        
        Map<String, String> infoCompTA = new HashMap<>();
        infoCompTA.put(EInfoComp.ID_C11.getCode(), "75P1418052016090715");
        Evt evtTA = newEvt("MM912357956FR", "TA", formatter.parseDateTime("18/05/2016 06:41:00").toDate(), "75999", "75P14", infoCompTA);

        /* Mock DAO */
        Mockito.when(mockDao.trouverDernierEvtTA(Mockito.any(Evt.class))).thenReturn(evtTA);

        /* Running */
        boolean result = InsertPointTourneeServiceImpl.INSTANCE.computeIdPointC11(evtD);
        
        assertFalse(result);
        assertTrue(evtD.getInfoscomp().isEmpty());
    }

    /**
     * 
     * @param noLt
     * @param codeEvt
     * @param dateEvt
     * @return
     */
    private Evt newEvt(String noLt, String codeEvt, Date dateEvt) {
        return newEvt(noLt, codeEvt, dateEvt, null, null, null);
    }

    private Evt newEvt(String noLt, String codeEvt, Date dateEvt, String posteComptable, String codeTournee) {
        return newEvt(noLt, codeEvt, dateEvt, posteComptable, codeTournee, null);
    }

    private Evt newEvt(String noLt, String codeEvt, Date dateEvt, String posteComptable, String codeTournee, Map<String, String> infosComp) {
        return new Evt()
            .setNoLt(noLt)
            .setCodeEvt(codeEvt)
            .setDateEvt(dateEvt)
            .setLieuEvt(posteComptable)
            .setSsCodeEvt(codeTournee)
            .setInfoscomp(infosComp);
    }

    private Tournee newTournee(String agence, Date dateTournee, String idC11) {
        Tournee tournee = new Tournee();
        tournee.setAgence(agence);
        tournee.setDateTournee(dateTournee);
        tournee.setIdC11(idC11);
        return tournee;
    }

    /**
     * Positionne les valeurs indiquées dans le mock de transco
     * 
     * @param evt_point_tournee
     *            : paramètre des evenements à considérer pour le MS
     *            insertPointTournee
     * @throws Exception
     */
    private void setTranscos(final String evt_point_tournee) throws Exception {
        ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);
    	ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
    	Map<String, Map<String, String>> map = new  HashMap<>();
    	Map<String, String> mapParams = new HashMap<>();
    	mapParams.put("evt_point_tournee", evt_point_tournee);
        map.put("parametre_microservices", mapParams);
        Transcoder transcosDiffVision = new Transcoder();
        transcosDiffVision.setTranscodifications(map);
        transcoders.put("DiffusionVision", transcosDiffVision);
        
        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("DiffusionVision")).thenReturn(map);
        TranscoderService.INSTANCE.setTranscoders(transcoders);
        TranscoderService.INSTANCE.setDao(mockTranscoderDao);
        TranscoderService.INSTANCE.addProjet("DiffusionVision");
        FeatureFlips.INSTANCE.setFlipProjectName("DiffusionVision");
    }
    
    private void setCacheAgence() {
        Agence agence = new Agence();
        agence.setPosteComptable("75999");
        agence.setTrigramme("PCO");

        @SuppressWarnings("unchecked")
        CacheManager<Agence> mockCacheAgence = Mockito.mock(CacheManager.class);
        Mockito.when(mockCacheAgence.getValue("75999")).thenReturn(agence);
        
        InsertPointTourneeServiceImpl.INSTANCE.setRefentielAgence(mockCacheAgence);
    }
}
