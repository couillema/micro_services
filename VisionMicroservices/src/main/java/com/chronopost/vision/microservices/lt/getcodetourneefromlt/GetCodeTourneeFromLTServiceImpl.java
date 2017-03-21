package com.chronopost.vision.microservices.lt.getcodetourneefromlt;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.chronopost.vision.microservices.sdk.GetLtV1;
import com.chronopost.vision.model.GetCodeTourneeFromLTResponse;
import com.chronopost.vision.model.Lt;

public class GetCodeTourneeFromLTServiceImpl implements IGetCodeTourneeFromLTService {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(GetCodeTourneeFromLTServiceImpl.class);

    private IGetCodeTourneeFromLTDAO dao;
    private GetLtV1 getLtV1;

    private GetCodeTourneeFromLTServiceImpl() {
        super();
    }

    /**
     * Singleton
     */
    static class InstanceHolder {

        public static GetCodeTourneeFromLTServiceImpl service = new GetCodeTourneeFromLTServiceImpl();

    }

    /**
     * Singleton
     * 
     * @return
     */
    public static GetCodeTourneeFromLTServiceImpl getInstance() {

        return InstanceHolder.service;
    }

    /**
     * setter du endpoint pour getLtV1.
     * 
     * @return
     */

    @Override
    public GetCodeTourneeFromLTResponse findTourneeBy(String noLT, Date dateHeureSearch)
            throws GetCodeTourneeFromLTException {

        if (noLT == null || dateHeureSearch == null) {
            return null;
        }

        Map<String, Lt> mapLt = null;

        try {
            mapLt = getGetLtV1().getLt(Arrays.asList(noLT));

        } catch (Exception e) {
            // pas beau mais trop d'exceptions checked alors que je peux rien en
            // faire
            logger.error("Erreur lors de l'appel à GET LT", e);
            throw new RuntimeException("Erreur lors de l'appel à GET LT", e);
        }

        String noLTResolved = null;

        if (mapLt != null) {

            Lt lt = mapLt.get(noLT);

            if (lt != null && lt.getNoLt() != null) {
                noLTResolved = lt.getNoLt();
            }

        }

        // shouldn't happen but SDK may return while the service shouldn't
        if (noLTResolved == null) {
            throw new GetCodeTourneeFromLTException(GetCodeTourneeFromLTException.LT_NOT_FOUND);
        }

        GetCodeTourneeFromLTResponse tournee = getDao().findTourneeBy(noLTResolved, dateHeureSearch);

        if (tournee == null || tournee.getCodeTournee() == null) {
            throw new GetCodeTourneeFromLTException(GetCodeTourneeFromLTException.TOURNEE_NOT_FOUND);
        }

        return tournee;
    }

    public GetLtV1 getGetLtV1() {
        return getLtV1;
    }

    public GetCodeTourneeFromLTServiceImpl setGetLtV1(GetLtV1 getLtV1) {
        this.getLtV1 = getLtV1;
        return this;
    }

    public IGetCodeTourneeFromLTDAO getDao() {
        return dao;
    }

    public GetCodeTourneeFromLTServiceImpl setDao(IGetCodeTourneeFromLTDAO dao) {
        this.dao = dao;
        return this;
    }

}
