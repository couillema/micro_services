package com.chronopost.vision.microservices.transco;

import java.util.HashMap;
import java.util.Map;

import com.chronopost.vision.transco.dao.ITranscoderDao;

public class TranscoderDaoMock implements ITranscoderDao {

    private Map<String,Map<String, Map<String, String>>> transcos = new HashMap<>() ; 
    
    @Override
    public Map<String, Map<String, String>> getTranscodificationsFromDatabase(String projet) {
        if (transcos.get(projet) == null) transcos.put(projet, new HashMap<String, Map<String, String>>()) ; 
        return transcos.get(projet) ;
    }

    //@Override
    public void updateTransco(String projet, String famille, String entree, String valeur) {
        // TODO Auto-generated method stub
        if (transcos.get(projet) == null) transcos.put(projet, new HashMap<String, Map<String, String>>()) ;
        if (transcos.get(projet).get(famille) == null) transcos.get(projet).put(famille, new HashMap<String, String>()) ;
        transcos.get(projet).get(famille).put(entree, valeur) ;
    }

    //@Override
    public void deleteTransco(String projet, String famille, String entree) {
        // TODO Auto-generated method stub
    }

}
