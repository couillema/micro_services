package com.chronopost.vision.microservices.maintienindexevt.v1.model;

import java.util.List;

import com.chronopost.vision.model.Evt;
import com.chronopost.vision.model.Lt;

import fr.chronopost.soap.calculretard.cxf.ResultCalculerRetardPourNumeroLt;

public class MaintienIndexEvtDTO {
	
	private Lt lt;
	private List<Evt> evts;
	private ResultCalculerRetardPourNumeroLt resultCalculRetard;
	
	public MaintienIndexEvtDTO() {
		super();		
	}
	
	public Lt getLt() {
		return lt;
	}
	
	public List<Evt> getEvts() {
		return evts;
	}
	
	public ResultCalculerRetardPourNumeroLt getResultCalculRetard() {
		return resultCalculRetard;
	}
	
	public MaintienIndexEvtDTO setLt(final Lt lt) {
		this.lt = lt;
		return this;
	}
	
	public MaintienIndexEvtDTO setEvts(final List<Evt> evts) {
		this.evts = evts;
		return this;
	}
	
	public MaintienIndexEvtDTO setResultCalculRetard(final ResultCalculerRetardPourNumeroLt resultCalculRetard) {
		this.resultCalculRetard = resultCalculRetard;
		return this;
	}
}
