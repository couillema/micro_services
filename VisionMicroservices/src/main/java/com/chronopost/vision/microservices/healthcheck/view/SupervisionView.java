package com.chronopost.vision.microservices.healthcheck.view;

import java.util.Map;

import io.dropwizard.views.View;

public class SupervisionView extends View {
	
	private final Map<String, Healthy> supervision;
	
	public SupervisionView(Map<String, Healthy> supervision){
		super("supervision.ftl");
		this.supervision = supervision;
	}
	
	public Map<String, Healthy> getSupervision(){
		return supervision;
	}

}
