package com.chronopost.vision.microservices.healthcheck.view;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.JerseyClientBuilder;

import com.chronopost.vision.microservices.sdk.exception.TechnicalException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class HealthCheckResource {
	
	public final static int DEFAULT_TIMEOUT = 10000;
	
	private Client client;
	
	private int timeout;
    
    private int port;    
	
	public int getPort() {
		return port;
	}
	
	public HealthCheckResource setPort(int port) {
		this.port = port;
		return this;
	}
	
	public HealthCheckResource(){
		client = JerseyClientBuilder.newBuilder().build();
        timeout = DEFAULT_TIMEOUT;
	}

	@GET
	@Path("/healthcheckHtmlView")
	@Timed
	public SupervisionView check(){
		
        Future<Response> futureResponse = client.target("http://127.0.0.1:"+ port).path("/healthcheck").request("application/json")
                .async().get();
        
        Response response = null;

        try {
            response = futureResponse.get(this.timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            throw new TechnicalException("Erreur à l'appel du healthcheck ", e);
        }
        
        String json = response.readEntity(String.class);
        
        ObjectMapper mapper = new ObjectMapper();
		try {
			 Map<String, Healthy> retMap = mapper.readValue(json, new TypeReference<Map<String, Healthy>>(){});
			return new SupervisionView(retMap);
		} catch (IOException e) {
			throw new RuntimeException("Failed parsing json: ", e);
		}

	}

}
