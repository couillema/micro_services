package com.chronopost.vision.microservices.maintienindexevt.v1.model;

import java.util.Date;

import com.chronopost.vision.model.Lt;

public class UpdateDepassementProactifInput {
	private String noLt;
	private Lt lt;
	private Date dateLivraisonContractuelle;
	private Date dateLivraisonPrevue;
    private String deleted;
	public UpdateDepassementProactifInput() {			
	}
	public String getNoLt() {
		return noLt;
	}
	public Lt getLt() {
		return lt;
	}
	public Date getDateLivraisonContractuelle() {
		return dateLivraisonContractuelle;
	}
	public Date getDateLivraisonPrevue() {
		return dateLivraisonPrevue;
	}
	public String getDeleted() {
		return deleted;
	}
	public UpdateDepassementProactifInput setNoLt(String noLt) {
		this.noLt = noLt;
		return this;
	}
	public UpdateDepassementProactifInput setLt(Lt lt) {
		this.lt = lt;
		return this;
	}
	public UpdateDepassementProactifInput setDateLivraisonContractuelle(Date dateLivraisonContractuelle) {
		this.dateLivraisonContractuelle = dateLivraisonContractuelle;
		return this;
	}
	public UpdateDepassementProactifInput setDateLivraisonPrevue(Date dateLivraisonPrevue) {
		this.dateLivraisonPrevue = dateLivraisonPrevue;
		return this;
	}
	public UpdateDepassementProactifInput setDeleted(String deleted) {
		this.deleted = deleted;
		return this;
	}	
	
	
    
    
}
