package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import com.chronopost.vision.model.updatespecificationscolis.v1.EConsigne;

/**
 * Interface Transcoder
 * @author jcbontemps
 */
public interface IUpdateSpecificationsTranscoder {

    /**
     * @param code
     * @return la transcodification du code
     */
    public EConsigne transcode(final String code) ;

}
