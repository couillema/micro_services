package com.chronopost.vision.microservices.tournee.updatetournee.v1;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.Evt;
import com.google.common.collect.Maps;

public class UpdateTourneeServiceImplTest {
    private IUpdateTourneeService service;
    private IUpdateTourneeDao mockDao = Mockito.mock(IUpdateTourneeDao.class);

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        service = UpdateTourneeServiceImpl.INSTANCE.setDao(mockDao);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateTourneeOkTest() throws ParseException, InterruptedException, ExecutionException {
        HashMap<String, String> infoscomp = Maps.newHashMap();
        infoscomp.put("191", "0.717483333333333");
        infoscomp.put("190", "49.5332866666667");
        infoscomp.put("193", "AJA20A0100208092015065959");
        Mockito.reset(mockDao);
        Mockito.when(mockDao.insertAgenceTournee(Mockito.anyList())).thenReturn(true);
        Mockito.when(mockDao.insertColisTourneeAgence(Mockito.anyList())).thenReturn(true);
        Mockito.when(mockDao.insertInfoTournee(Mockito.anyList())).thenReturn(true);
        Mockito.when(mockDao.insertTourneeC11(Mockito.anyList())).thenReturn(true);
        Mockito.when(mockDao.updateTournee(Mockito.anyList())).thenReturn(true);
        Mockito.when(mockDao.updateTourneeCodeService(Mockito.anyList())).thenReturn(true);

        Evt evt1 = new Evt().setNoLt("NOLTTEST").setPrioriteEvt(1).setDateEvt(new Date()).setCodeEvt("TA")
                .setInfoscomp(infoscomp);
        assertTrue(service.updateTournee(Arrays.asList(evt1)));
        Mockito.verify(mockDao).insertAgenceTournee(Arrays.asList(evt1));
        Mockito.verify(mockDao).insertColisTourneeAgence(Arrays.asList(evt1));
        Mockito.verify(mockDao).insertInfoTournee(Arrays.asList(evt1));
        Mockito.verify(mockDao).insertTourneeC11(Arrays.asList(evt1));
        Mockito.verify(mockDao).updateTournee(Arrays.asList(evt1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateTourneeKoTest() throws ParseException, InterruptedException, ExecutionException {
        HashMap<String, String> infoscomp = Maps.newHashMap();
        infoscomp.put("191", "0.717483333333333");
        infoscomp.put("190", "49.5332866666667");
        infoscomp.put("193", "AJA20A0100208092015065959");
        Mockito.reset(mockDao);
        Mockito.when(mockDao.insertAgenceTournee(Mockito.anyList())).thenReturn(false);
        Mockito.when(mockDao.insertColisTourneeAgence(Mockito.anyList())).thenReturn(false);
        Mockito.when(mockDao.insertInfoTournee(Mockito.anyList())).thenReturn(true);
        Mockito.when(mockDao.insertTourneeC11(Mockito.anyList())).thenReturn(true);
        Mockito.when(mockDao.updateTournee(Mockito.anyList())).thenReturn(true);

        Evt evt1 = new Evt().setNoLt("NOLTTEST").setPrioriteEvt(1).setDateEvt(new Date()).setCodeEvt("TA")
                .setInfoscomp(infoscomp);
        assertFalse(service.updateTournee(Arrays.asList(evt1)));
        Mockito.verify(mockDao).insertAgenceTournee(Arrays.asList(evt1));
        Mockito.verify(mockDao).insertColisTourneeAgence(Arrays.asList(evt1));
        Mockito.verify(mockDao).insertInfoTournee(Arrays.asList(evt1));
        Mockito.verify(mockDao).insertTourneeC11(Arrays.asList(evt1));
        Mockito.verify(mockDao).updateTournee(Arrays.asList(evt1));
    }
}
