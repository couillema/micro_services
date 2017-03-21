package com.chronopost.vision.microservices.getEvts;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.chronopost.vision.model.Evt;

public enum GetEvtsServiceImpl implements IGetEvtsService {
	INSTANCE;
	
	private IGetEvtsDao getEvtsDao;

	@Override
	public List<Evt> getEvts(final String noLt) throws Exception {
		final List<Evt> evts = getEvtsDao.getLtEvts(noLt);
		Collections.sort(evts, new Comparator<Evt>() {
		    @Override
		    public int compare(final Evt o1, final Evt o2) {
		        return o1.getDateEvt().compareTo(o2.getDateEvt());
		    }
		});
		return evts;
	}

	@Override
	public void setDao(final IGetEvtsDao getEvtsDao) {
		this.getEvtsDao = getEvtsDao;
	}
}
