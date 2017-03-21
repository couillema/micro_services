package com.chronopost.vision.microservices.genereevt;

import java.text.SimpleDateFormat;
import java.util.Map;

import com.chronopost.vision.model.Evt;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.transcoder.Transcoder;

/**
 * Classe de mapping entre les différents objets de la fonctionnalité genererEvt
 * 
 * @author jcbontemps
 */
public class Mapper {

    /**
     * Constante pour le transcoding
     */
    private static final String PROJET_VISION = "DiffusionVision";

    /**
     * Constante pour le transcoding
     */
    private static final String FAMILLE_INFOCOMP = "code_bco_link";

    /**
     * Constante pour le ID_SITE_CLIENT
     */
    private static final String ID_SITE_CLIENT = "10";

    /** 
     * Format yyyyMMdd HH:mm:ss
     */
    private static final SimpleDateFormat FORMAT_DATE_SEULE = new SimpleDateFormat("dd/MM/yyy");

    /** 
     * Format HH:mm:ss
     */
    private static final SimpleDateFormat FORMAT_TIME_SEUL = new SimpleDateFormat("HH:mm:ss");
    
    
    /**
     * Cette méthode rempli un DTO à partir d'un Evt selon certaines règles de
     * correspondances. En particulier une transcodifiation est utilisée pour
     * les InfoComp
     * 
     * @param evt
     *            Evt source fourni
     * @return DTO cible à remplir
     * 
     */
    public GenererEvtDTO evtToDto(Evt evt) {

        GenererEvtDTO dto = new GenererEvtDTO() ;
        
        dto.setCodeSiteClient(evt.getLieuEvt());
        dto.setIdPosteClient(ID_SITE_CLIENT);
        dto.setOperateur(evt.getCreateurEvt());
        dto.setNumeroObjet(evt.getNoLt());
        dto.setTypeEvenement(evt.getCodeEvt());

        dto.setDateEvenement(FORMAT_DATE_SEULE.format(evt.getDateEvt())); 
        dto.setHeureEvenement(FORMAT_TIME_SEUL.format(evt.getDateEvt())) ;

        Map<String, String> infoscomp = evt.getInfoscomp();

        String[] icCodes = new String[15];
        String[] icValues = new String[15];

        int i = 0;
        for (String code : infoscomp.keySet()) {
            String value = infoscomp.get(code);
            TranscoderService instance = TranscoderService.INSTANCE ;
            Transcoder transcoder = instance.getTranscoder(PROJET_VISION) ;
            String transcode = transcoder.transcode(FAMILLE_INFOCOMP, code);
            icCodes[i] = transcode != null && !"".equals(transcode.trim()) ? transcode : code ;
            icValues[i] = value;
            i++;
        }

        dto.setIc1Code(icCodes[0]);
        dto.setIc1Value(icValues[0]);
        dto.setIc2Code(icCodes[1]);
        dto.setIc2Value(icValues[1]);
        dto.setIc3Code(icCodes[2]);
        dto.setIc3Value(icValues[2]);
        dto.setIc4Code(icCodes[3]);
        dto.setIc4Value(icValues[3]);
        dto.setIc5Code(icCodes[4]);
        dto.setIc5Value(icValues[4]);
        dto.setIc6Code(icCodes[5]);
        dto.setIc6Value(icValues[5]);
        dto.setIc7Code(icCodes[6]);
        dto.setIc7Value(icValues[6]);
        dto.setIc8Code(icCodes[7]);
        dto.setIc8Value(icValues[7]);
        dto.setIc9Code(icCodes[8]);
        dto.setIc9Value(icValues[8]);
        dto.setIc10Code(icCodes[9]);
        dto.setIc10Value(icValues[9]);
        dto.setIc11Code(icCodes[10]);
        dto.setIc11Value(icValues[10]);
        dto.setIc12Code(icCodes[11]);
        dto.setIc12Value(icValues[11]);
        dto.setIc13Code(icCodes[12]);
        dto.setIc13Value(icValues[12]);
        dto.setIc14Code(icCodes[13]);
        dto.setIc14Value(icValues[13]);
        dto.setIc15Code(icCodes[14]);
        dto.setIc15Value(icValues[14]);

        return dto;
    }

}
