package com.chronopost.vision.microservices;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import systems.composable.dropwizard.cassandra.CassandraFactory;

import com.datastax.driver.core.Session;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class VisionMicroserviceConfiguration extends Configuration {

	@NotEmpty    
    public static Session cassandraSession;
	@NotEmpty    
    public static Session cassandraSessionFluks;
    
	// proxy pour le geocoder
	@Valid
    @NotNull
    private String proxyURL;
    
    // proxy pour le geocoder
	@Valid
    @NotNull
    private String proxyPort;
    
    // proxy pour le geocoder
	@Valid
    @NotNull
    private Integer proxyTimeout;
    
    @Valid
    @NotNull
    private CassandraFactory cassandra = new CassandraFactory();
    
    @Valid
    @NotNull
    private CassandraFactory cassandraFluks = new CassandraFactory();
    
    @Valid
    @NotNull
    private HashMap<String, String> endpoints = new HashMap<String, String>();
    
    @Valid
    @NotNull
    private HashMap<String, Integer> sdkTimeouts = new HashMap<String, Integer>();

    
    @Valid
    @NotNull
    private HashMap<String, String> jmsDiffusion = new HashMap<String, String>();
    
    @NotNull
    @JsonProperty
    private Map<String, Object> defaultHystrixConfig;

    public Map<String, Object> getDefaultHystrixConfig() {
        return defaultHystrixConfig;
    }
    
    @JsonProperty("cassandra")
    public CassandraFactory getCassandraFactory() {
        return cassandra;
    }

    @JsonProperty("cassandra")
    public void setCassandraFactory(CassandraFactory cassandra) {
        this.cassandra = cassandra;
    }
    
    @JsonProperty("cassandraFluks")
    public CassandraFactory getCassandraFluksFactory() {
        return cassandraFluks;
    }

    @JsonProperty("cassandraFluks")
    public void setCassandraFluksFactory(CassandraFactory cassandraFluks) {
        this.cassandraFluks = cassandraFluks;
    }

    @JsonProperty("proxyUrl")
	public String getProxyURL() {
		return proxyURL;
	}

    @JsonProperty("proxyUrl")
	public void setProxyURL(String proxyURL) {
		this.proxyURL = proxyURL;
	}

    @JsonProperty("proxyPort")
	public String getProxyPort() {
		return proxyPort;
	}

    @JsonProperty("proxyPort")
	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

    @JsonProperty("proxyTimeout")
	public Integer getProxyTimeout() {
		return proxyTimeout;
	}

    @JsonProperty("proxyTimeout")
	public void setProxyTimeout(Integer proxyTimeout) {
		this.proxyTimeout = proxyTimeout;
	}
    
    @JsonProperty("sdkEndpoints")
    public void setEndpoints(HashMap<String, String> endpoints) {
        this.endpoints = endpoints;        
    }

	public HashMap<String, String> getEndpoints() {
		return endpoints;
	}
	
	@JsonProperty("sdkTimeouts")
    public void setSdkTimeouts(HashMap<String, Integer> timeouts) {
        this.sdkTimeouts = timeouts;        
    }

	public HashMap<String, Integer> getSdkTimeouts() {
		return this.sdkTimeouts;
	}
	
	@JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;
	
	
	
	@JsonProperty("jmsDiffusion")
    public void setJmsDiffusion(HashMap<String, String> jmsDiffusion) {
        this.jmsDiffusion = jmsDiffusion;        
    }
	
	public HashMap<String, String> getJmsDiffusion() {
        return this.jmsDiffusion;        
    }
		
}
