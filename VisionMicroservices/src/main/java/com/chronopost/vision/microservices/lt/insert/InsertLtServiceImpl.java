package com.chronopost.vision.microservices.lt.insert;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.exception.FunctionalException;
import com.chronopost.vision.microservices.exception.MSTechnicalException;
import com.chronopost.vision.model.Lt;

/**
 * implémentation du service IInsertLtService
 * Classe d'insertion des LTs
 * @author jcbontemps
 */
public class InsertLtServiceImpl implements IInsertLtService {

	private static final Logger logger = LoggerFactory.getLogger(InsertLtServiceImpl.class);

    private InsertLtDAO dao;

    /*
     * (non-Javadoc)
     * @see com.chronopost.vision.microservices.lt.insert.IInsertLtService#insertLtsInDatabase(java.util.List)
     */
    public boolean insertLtsInDatabase(List<Lt> lts) throws MSTechnicalException, FunctionalException {
    	if (lts.size()>0)
    		logger.debug("InsertLt (echantillon): "+lts.get(0).getNoLt());
        return dao.insertLts(lts);
    }

    /*
     * (non-Javadoc)
     * @see com.chronopost.vision.microservices.lt.insert.IInsertLtService#setDao(com.chronopost.vision.microservices.lt.insert.InsertLtDAO)
     */
    public void setDao(InsertLtDAO dao) {
        this.dao = dao;
    }

}