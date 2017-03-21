package com.chronopost.vision.microservices.exception;

import static javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.chronopost.vision.exceptions.InvalidParameterException;

@Provider
public class InvalidParameterExceptionProvider implements ExceptionMapper<InvalidParameterException> {

	@Override
	public Response toResponse(InvalidParameterException ex) {
		return Response.status(METHOD_NOT_ALLOWED).entity(ex.getMessage()).type("text/plain").build();
	}
}
