package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.mockito.Mockito;

import com.chronopost.vision.microservices.tournee.getsynthesetournees.v1.SyntheseTourneeService.ParametresMicroservices;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.ITranscoderDao;
import com.chronopost.vision.transco.transcoder.Transcoder;

public class SyntheseTourneeTestUtils {
	
    private final ITranscoderDao mockTranscoderDao = Mockito.mock(ITranscoderDao.class);

    private static final String depassementMaxEta = "30";
    private static final String depassementMinEta = "-30";
    private static final String evtPresentationPositive = "|RG|D|B|RB|D1|D2|D3|IP|";
    private static final String evtPresentationNegative = "|P|RC|PR|CO|PA|NA|N1|N2|P1|p2|";
    private static final String evtPresentationDomicilePositive = "|RG|D|B|D1|D2|D3|";
    private static final String evtPresentationDomicileNegative = "|P|RC|PR|CO|PA|NA|P1|P2|";
    private static final String evtPresentationDomicile = "|RG|D|B|D1|D2|D3|P|RC|PR|CO|PA|NA|P1|P2|";
    private static final String evtMiseADispositionBureau = "|RB|IP|";
    private static final String evtDPlus = "|RG|P|D|B|RC|PR|RB|CO|PA|NA|N1|N2|P1|P2|D1|D2|D3|IP|";
    private static final String evtEchecLivraison = "|RC|PR|CO|PA|NA|";
    
    protected void setTranscos() throws Exception {
		Map<String, String> mapParam = new HashMap<>();
		mapParam.put(ParametresMicroservices.DEPASSEMENT_MAX_ETA.toString(), depassementMaxEta);
		mapParam.put(ParametresMicroservices.DEPASSEMENT_MIN_ETA.toString(), depassementMinEta);
		mapParam.put(ParametresMicroservices.EVT_PRESENTATION_POSITIVE.toString(), evtPresentationPositive);
		mapParam.put(ParametresMicroservices.EVT_PRESENTATION_NEGATIVE.toString(), evtPresentationNegative);
		mapParam.put(ParametresMicroservices.EVT_PRESENTATION_DOMICILE_POSITIVE.toString(),
				evtPresentationDomicilePositive);
		mapParam.put(ParametresMicroservices.EVT_PRESENTATION_DOMICILE_NEGATIVE.toString(),
				evtPresentationDomicileNegative);
		mapParam.put(ParametresMicroservices.EVT_PRESENTATION_DOMICILE.toString(), evtPresentationDomicile);
		mapParam.put(ParametresMicroservices.EVT_MISE_A_DISPOSITION_BUREAU.toString(), evtMiseADispositionBureau);
		mapParam.put(ParametresMicroservices.EVT_D_PLUS.toString(), evtDPlus);
		mapParam.put(ParametresMicroservices.EVT_ECHEC_LIVRAISON.toString(), evtEchecLivraison);
        
		ConcurrentMap<String, Transcoder> transcoders = new ConcurrentHashMap<>();
		Transcoder transcoderDiffVision = new Transcoder();
		Map<String, Map<String, String>> map = new HashMap<>();
		map.put(SyntheseTourneeService.PARAMETRE_MICROSERVICES, mapParam);
		transcoderDiffVision.setTranscodifications(map);
		transcoders.put(SyntheseTourneeService.DIFFUSION_VISION, transcoderDiffVision);
		TranscoderService.INSTANCE.setTranscoders(transcoders);

		Mockito.when(mockTranscoderDao.getTranscodificationsFromDatabase(SyntheseTourneeService.DIFFUSION_VISION))
				.thenReturn(map);
		TranscoderService.INSTANCE.setDao(mockTranscoderDao);
		TranscoderService.INSTANCE.addProjet(SyntheseTourneeService.DIFFUSION_VISION);
    }
}
