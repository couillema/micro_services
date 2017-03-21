package com.chronopost.vision.microservices.updatespecificationscolis.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;

public class UpdateSpecificationsColisDaoMock implements IUpdateSpecificationsColisDao {

    private Map<String, SpecifsColis> specifsColis = new HashMap<>();

    public Map<String, SpecifsColis> getSpecifsColis() {
        return specifsColis;
    }

    public void setSpecifsColis(HashMap<String, SpecifsColis> specifsColis) {
        this.specifsColis = specifsColis;
    }
    
    @Override
    public boolean updateSpecifsServices(List<SpecifsColis> listeSpecifsColis) {
    	for (SpecifsColis colis : listeSpecifsColis) {
    		specifsColis.put(colis.getNoLt(), colis);
    	}
        return true;
    }

    @Override
	public boolean updateConsignes(List<SpecifsColis> listeSpecifsColis) {
		for (SpecifsColis colis : listeSpecifsColis) {
			final String noLt = colis.getNoLt();
			if (!specifsColis.containsKey(noLt)) {
				specifsColis.put(noLt, colis);
			} else {
				specifsColis.get(noLt).setConsignesAnnulees(colis.getConsignesAnnulees());
				specifsColis.get(noLt).setConsignesRecues(colis.getConsignesRecues());
				specifsColis.get(noLt).setConsignesTraitees(colis.getConsignesTraitees());
			}
		}
		return true;
    }

    @Override
	public boolean updateSpecifsEvenements(List<SpecifsColis> listeSpecifsColis) {
		for (final SpecifsColis colis : listeSpecifsColis) {
			final String noLt = colis.getNoLt();
			if (!specifsColis.containsKey(noLt)) {
				specifsColis.put(noLt, colis);
			} else {
				specifsColis.get(noLt).setSpecifsEvt(colis.getSpecifsEvt());
				specifsColis.get(noLt).addAllEtapes(colis.getEtapes());
				specifsColis.get(noLt).setDatesContractuelles(colis.getDatesContractuelles());
				specifsColis.get(noLt).addAllInfoSupp(colis.getInfoSupp());
			}
		}
		return true;
    }

	@Override
	public void updateCptTrtTrtFailMS(int nbTrt, int nbFail) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateCptHitMS() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateCptFailMS() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void declareErreur(Evt evt, String methode, Exception except) {
		// TODO Auto-generated method stub
		
	}

}
