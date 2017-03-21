package com.chronopost.vision.microservices.transcos;

import com.chronopost.vision.model.transco.GetTranscoOutput;
import com.chronopost.vision.transco.TranscoderService;
import com.chronopost.vision.transco.dao.TranscoderDao;


public class TranscoServiceImpl implements ITranscoService{

	/**
	 * Singleton
	 */
	static class InstanceHolder {

		public static TranscoServiceImpl service = new TranscoServiceImpl();

	}

	/**
	 * Singleton
	 * 
	 * @return
	 */
	public static TranscoServiceImpl getInstance() {

		return InstanceHolder.service;
	}

    @Override
    public boolean updateTransco(String projet, String famille, String nom, String valeur) {
        TranscoderDao.INSTANCE.updateTransco(projet, famille, nom, valeur);
        return false;
    }

    @Override
    public GetTranscoOutput getTransco(String projet, String famille, String nom) {
        GetTranscoOutput output = new GetTranscoOutput() ; 
        output.setValeur(TranscoderService.INSTANCE.getTranscoder(projet).transcode(famille, nom));
        return output ;
    }
}
