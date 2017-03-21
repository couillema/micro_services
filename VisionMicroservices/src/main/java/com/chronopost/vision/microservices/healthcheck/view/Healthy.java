package com.chronopost.vision.microservices.healthcheck.view;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Healthy {

	private String healthy;
	private Date date;
	private String message;


	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Healthy(){
		this.date = new Date();
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getHealthy() {
		return healthy;
	}

	public void setHealthy(String healthy) {
		this.healthy = healthy;
	}

	public boolean equals(Object other) {
		if (this == other) return true;
		if ( !(other instanceof Healthy) ) return false;

		final Healthy health = (Healthy) other;

		if ( !health.getHealthy().equals( getHealthy() ) ) return false;
		if(getMessage() != null){
			if ( !health.getMessage().equals( getMessage() ) ) return false;
		}

		return true;
	}

	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + getHealthy().hashCode();
		hash = hash * 31 
				+ (getMessage() == null ? 0 : getMessage().hashCode());
		return hash;
	}

}
