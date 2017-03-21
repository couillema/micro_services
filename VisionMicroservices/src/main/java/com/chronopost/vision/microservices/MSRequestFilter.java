package com.chronopost.vision.microservices;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

public class MSRequestFilter implements ContainerRequestFilter {

	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {
		requestContext.setProperty("request.date", new Date());
	}
}
