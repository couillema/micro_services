package com.chronopost.vision.microservices.lt.get;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.chronopost.vision.microservices.enums.ETraitementSynonymes;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;
import com.google.common.collect.Maps;

public class GetLtServiceImplTest {


    @Test
    public void testWithoutGetMaster() {
    	IGetLtDao mockDAO = Mockito.mock(GetLtDaoImpl.class);
    	IGetLtService serviceImpl = GetLtServiceImpl.getInstance().setDao(mockDAO);
    	
        Map<String, Lt> mapLt1234 = new HashMap<>();
        Lt lt1234 = new Lt();
        lt1234.setNoLt("1234");
        lt1234.setSynonymeMaitre("ABCD");
        mapLt1234.put("1234", lt1234);

        Mockito.when(mockDAO.getLtsFromDatabase(Mockito.eq(Arrays.asList("1234")), Mockito.anyBoolean())).thenReturn(
                mapLt1234);

        Map<String, Lt> mapLtABCD = new HashMap<>();
        Lt ltABCD = new Lt();
        ltABCD.setNoLt("ABCD");
        mapLtABCD.put("ABCD", ltABCD);

        Mockito.when(mockDAO.getLtsFromDatabase(Mockito.eq(Arrays.asList("ABCD")), Mockito.anyBoolean())).thenReturn(
                mapLtABCD);

        Map<String, Lt> mapLt12345678 = new HashMap<>();
        Lt lt5678 = new Lt();
        lt5678.setNoLt("5678");
        mapLt12345678.put("1234", lt1234);
        mapLt12345678.put("5678", lt5678);

        Mockito.when(mockDAO.getLtsFromDatabase(Mockito.eq(Arrays.asList("1234", "5678")), Mockito.anyBoolean()))
                .thenReturn(mapLt12345678);

        // return empty
        Map<String, Lt> mapReturned = serviceImpl.getLtsFromDatabase(new ArrayList<String>());
        assertEquals(mapReturned.size(), 0);

        // returns empty
        mapReturned = serviceImpl.getLtsFromDatabase(new ArrayList<String>());
        assertEquals(mapReturned.size(), 0);

        // returns empty
        mapReturned = serviceImpl.getLtsFromDatabase(Arrays.asList("xxxx"));
        assertEquals(mapReturned.size(), 0);

        // returns 1 element
        mapReturned = serviceImpl.getLtsFromDatabase(Arrays.asList("1234"));
        assertEquals(mapReturned.size(), 1);
        Lt lt = mapReturned.get("1234");
        assertEquals(lt.getNoLt(), "1234");

        // returns 2 elements
        mapReturned = serviceImpl.getLtsFromDatabase(Arrays.asList("1234", "5678"));
        assertEquals(mapReturned.size(), 2);
        Lt lt1 = mapReturned.get("1234");
        assertEquals(lt1.getNoLt(), "1234");

        Lt lt2 = mapReturned.get("5678");
        assertEquals(lt2.getNoLt(), "5678");
    }

    @Test
    public void testWithGetMaster() {
    	IGetLtDao mockDAO = Mockito.mock(GetLtDaoImpl.class);
    	IGetLtService serviceImpl = GetLtServiceImpl.getInstance().setDao(mockDAO);
        

        Map<String, Lt> mapLt1234 = new HashMap<>();
        Lt lt1234 = new Lt();
        lt1234.setNoLt("1234");
        lt1234.setSynonymeMaitre("ABCD");
        mapLt1234.put("1234", lt1234);

        Mockito.when(mockDAO.getLtsFromDatabase(Mockito.eq(Arrays.asList("1234")), Mockito.anyBoolean())).thenReturn(
                mapLt1234);

        Map<String, Lt> mapLtABCD = new HashMap<>();
        Lt ltABCD = new Lt();
        ltABCD.setNoLt("ABCD");
        mapLtABCD.put("ABCD", ltABCD);

        Mockito.when(mockDAO.getLtsFromDatabase(Mockito.eq(Arrays.asList("ABCD")), Mockito.anyBoolean())).thenReturn(
                mapLtABCD);

        Map<String, Lt> mapLt12345678 = new HashMap<>();
        Lt lt5678 = new Lt();
        lt5678.setNoLt("5678");
        mapLt12345678.put("1234", lt1234);
        mapLt12345678.put("5678", lt5678);

        Mockito.when(mockDAO.getLtsFromDatabase(Mockito.eq(Arrays.asList("1234", "5678")), Mockito.anyBoolean()))
                .thenReturn(mapLt12345678);

        // return empty
        Map<String, Lt> mapReturned = serviceImpl.getLtsFromDatabase(new ArrayList<String>());
        assertEquals(mapReturned.size(), 0);

        // returns empty
        mapReturned = serviceImpl.getLtsFromDatabase(new ArrayList<String>(),
                ETraitementSynonymes.RESOLUTION_DES_SYNONYMES);
        assertEquals(mapReturned.size(), 0);

        // returns empty
        mapReturned = serviceImpl.getLtsFromDatabase(Arrays.asList("xxxx"),
                ETraitementSynonymes.RESOLUTION_DES_SYNONYMES);
        assertEquals(mapReturned.size(), 0);

        // returns 1 element
        mapReturned = serviceImpl.getLtsFromDatabase(Arrays.asList("1234"),
                ETraitementSynonymes.RESOLUTION_DES_SYNONYMES);
        assertEquals(mapReturned.size(), 1);
        Lt ltMaitre = mapReturned.get("ABCD");
        assertNull(ltMaitre);
        Lt ltEsclave = mapReturned.get("1234");
        assertEquals(ltEsclave.getNoLt(), "ABCD");

        // returns 2 elements
        mapReturned = serviceImpl.getLtsFromDatabase(Arrays.asList("1234", "5678"),
                ETraitementSynonymes.RESOLUTION_DES_SYNONYMES);
        assertEquals(mapReturned.size(), 2);

        Lt lt = mapReturned.get("1234");
        assertEquals(lt.getNoLt(), "ABCD");

        Lt lt1 = mapReturned.get("ABCD");
        assertNull(lt1);

        Lt lt2 = mapReturned.get("5678");
        assertEquals(lt2.getNoLt(), "5678");
    }

    @Test
    public void testGetLtParEmailDesti() {
        
    	IGetLtDao mockDAO = Mockito.mock(GetLtDaoImpl.class);
    	IGetLtService serviceImpl = GetLtServiceImpl.getInstance().setDao(mockDAO);
    	
        DateTime dateDeb = new DateTime().withYear(2015).withMonthOfYear(10).withDayOfMonth(30).withHourOfDay(0)
                .withMinuteOfHour(0);
        DateTime dateFin = new DateTime().withYear(2015).withMonthOfYear(10).withDayOfMonth(30).withHourOfDay(23)
                .withMinuteOfHour(0);

        Lt lt1 = new Lt().setNoLt("EE000000001FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("gmail.com");
        Lt lt2 = new Lt().setNoLt("EE000000002FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("toto.com");
        Lt lt3 = new Lt().setNoLt("EE000000003FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("gmail.com");
        Map<String, Lt> retGetLtFromDatabase = Maps.newHashMap();

        retGetLtFromDatabase.put("EE000000001FR", lt1);
        retGetLtFromDatabase.put("EE000000002FR", lt2);
        retGetLtFromDatabase.put("EE000000002FR", lt3);

        Mockito.when(
                mockDAO.rechercheLt(Mockito.matches("email_1_destinataire"), Mockito.matches("adejanovski"),
                        Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(
                Arrays.asList("EE000000001FR", "EE000000002FR", "EE000000003FR"));
        Mockito.when(mockDAO.getLtsFromDatabase(Mockito.anyListOf(String.class), Mockito.anyBoolean())).thenReturn(
                retGetLtFromDatabase);

        Map<String, Lt> result = serviceImpl.getLtsParEmailDestinataire("adejanovski@gmail.com", dateDeb.toDate(),
                dateFin.toDate());
        assertEquals(result.keySet().size(), 2);

        // Vérification d'appel du DAO avec les bons paramètres
        Mockito.verify(mockDAO, Mockito.times(1)).rechercheLt("email_1_destinataire", "adejanovski", dateDeb.toDate(),
                dateFin.toDate());
        Mockito.verify(mockDAO, Mockito.times(1)).getLtsFromDatabase(
                Arrays.asList("EE000000001FR", "EE000000002FR", "EE000000003FR"), false);

    }

    @Test(expectedExceptions = MSTechnicalException.class)
    public void testGetLtParEmailDestiAdresseIncorrecte() {        
    	IGetLtDao mockDAO = Mockito.mock(GetLtDaoImpl.class);
    	IGetLtService serviceImpl = GetLtServiceImpl.getInstance().setDao(mockDAO);
    	
        DateTime dateDeb = new DateTime().withYear(2015).withMonthOfYear(10).withDayOfMonth(30).withHourOfDay(0)
                .withMinuteOfHour(0);
        DateTime dateFin = new DateTime().withYear(2015).withMonthOfYear(10).withDayOfMonth(30).withHourOfDay(23)
                .withMinuteOfHour(0);

        Lt lt1 = new Lt().setNoLt("EE000000001FR").setEmail1Destinataire("adejanovski")
                .setEmail2Destinataire("gmail.com");
        Map<String, Lt> retGetLtFromDatabase = Maps.newHashMap();

        retGetLtFromDatabase.put("EE000000001FR", lt1);

        Mockito.when(
                mockDAO.rechercheLt(Mockito.matches("email_1_destinataire"), Mockito.matches("adejanovski"),
                        Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(
                Arrays.asList("EE000000001FR", "EE000000002FR", "EE000000003FR"));
        Mockito.when(mockDAO.getLtsFromDatabase(Mockito.anyListOf(String.class), Mockito.anyBoolean())).thenReturn(
                retGetLtFromDatabase);

        serviceImpl.getLtsParEmailDestinataire("adejanovski gmail.com", dateDeb.toDate(), dateFin.toDate());

        // On ne doit jamais arriver ici
        assertTrue(false);

    }

}
