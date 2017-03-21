package com.chronopost.vision.microservices.healthcheck;

import java.util.Collection;

import com.codahale.metrics.health.HealthCheck;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Session;

public class CassandraHealthCheck extends HealthCheck {

	private final Session session;

    public CassandraHealthCheck(Session cassandraSession) {
        this.session = cassandraSession;
    }

    @Override
    protected Result check() throws Exception {        
        if (this.session.isClosed()) {
            return Result.unhealthy("Cassandra session is closed !");
        }
        Collection<Host> hosts = this.session.getState().getConnectedHosts();
        for(Host host:hosts){
	        if (!host.isUp()) {
	            return Result.unhealthy("Cassandra host " +  host.getAddress().getHostName() + " / " + host.getAddress().getHostAddress() + " in DC " + host.getDatacenter()  + " is down !");
	        }
        }
        return Result.healthy();
    }

}
