package com.chronopost.vision.microservices.updatereferentiel;

import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.chronopost.vision.model.updatereferentiel.DefinitionEvt;
import com.chronopost.vision.transco.dao.ITranscoderDao;

public class UpdateReferentielServiceTest {

    public static ITranscoderDao mockTranscoderDao ;

    public static UpdateReferentielService service ;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        mockTranscoderDao = Mockito.mock(ITranscoderDao.class) ;
        Map<String, Map<String, String>> transcos = new HashMap<String, Map<String, String>>();
        Map<String, String> transcoInfoscomp = new HashMap<String, String>();        
        transcos.put("id_infocomp", transcoInfoscomp);    	
        Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase("DiffusionVision")).thenReturn(transcos);
        service = UpdateReferentielServiceImpl.getInstance().setDao(mockTranscoderDao) ;
    }

    @Test
    public void updateInfoscomp()   {

        // initialisations
        Map<String, String> infoscomp = new HashMap<>();
        infoscomp.put("test1", "1") ;
        infoscomp.put("test2", "2") ;
        
        List<Boolean> updateInfoscomp = service.updateInfoscomp(infoscomp) ;
        assertTrue(!updateInfoscomp.contains(false)) ;

        // Vérification des appels au DAO pour insérer les transcos
        Mockito.verify(mockTranscoderDao, Mockito.times(1)).updateTransco("DiffusionVision", "id_infocomp", "test1", "1");
        Mockito.verify(mockTranscoderDao, Mockito.times(1)).updateTransco("DiffusionVision", "id_infocomp", "test2", "2");
        
    }

    @Test
    public void updateEvt() {

        // initialisations
        List<DefinitionEvt> evts = new ArrayList<>();

        DefinitionEvt def1 = new DefinitionEvt() ;
        def1.setIdEvenement("idEvenement1");
        def1.setCodeProducerInput("codeProducerInput1");
        def1.setCodeEvtInput("codeEvtInput1") ;
        def1.setPriorite("priorite1") ;
        def1.setLibVueCalculRetard("libVueCalculRetard1") ;
        def1.setLivVueChronotrace("livVueChronotrace1") ;
        def1.setLibEvt("libEvt1") ;

        DefinitionEvt def2 = new DefinitionEvt() ;
        def2.setIdEvenement("idEvenement2");
        def2.setCodeProducerInput("codeProducerInput2");
        def2.setCodeEvtInput("codeEvtInput2") ;
        def2.setPriorite("priorite2") ;
        def2.setLibVueCalculRetard("libVueCalculRetard2") ;
        def2.setLivVueChronotrace("livVueChronotrace2") ;
        def2.setLibEvt("libEvt2") ;

        evts.add(def1) ;
        evts.add(def2) ;

        // execution
        List<Boolean> updateEvt = service.updateEvt(evts) ;
        assertTrue(!updateEvt.contains(false)) ;

        
        // Vérification des appels au DAO pour insérer les transcos
        Mockito.verify(mockTranscoderDao, Mockito.times(1)).updateTransco("DiffusionVision", "code_id_evt", "codeProducerInput1|codeEvtInput1", "idEvenement1");
        Mockito.verify(mockTranscoderDao, Mockito.times(1)).updateTransco("DiffusionVision", "code_id_evt", "codeProducerInput2|codeEvtInput2", "idEvenement2");
        
        Mockito.verify(mockTranscoderDao, Mockito.times(1)).updateTransco("DiffusionVision", "evenements", "idEvenement1", "codeEvtInput1|priorite1|libVueCalculRetard1|livVueChronotrace1|libEvt1");
        Mockito.verify(mockTranscoderDao, Mockito.times(1)).updateTransco("DiffusionVision", "evenements", "idEvenement2", "codeEvtInput2|priorite2|libVueCalculRetard2|livVueChronotrace2|libEvt2");

        

    }

}
