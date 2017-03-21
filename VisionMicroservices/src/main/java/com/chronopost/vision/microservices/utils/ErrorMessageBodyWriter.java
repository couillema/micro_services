package com.chronopost.vision.microservices.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.jersey.validation.ValidationErrorMessage;

@Provider
@Produces(MediaType.TEXT_PLAIN)
public class ErrorMessageBodyWriter implements MessageBodyWriter<ErrorMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorMessageBodyWriter.class);

    public boolean isWriteable(
        Class<?> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType)
    {
        return ValidationErrorMessage.class.isAssignableFrom(type);
    }

    public long getSize(
        ErrorMessage t,
        Class<?> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType)
    {
        return -1;
    }

    public void writeTo(
        ErrorMessage t,
        Class<?> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, Object> httpHeaders,
        OutputStream entityStream) throws IOException, WebApplicationException
    {       
        String message = t.getMessage();        
        entityStream.write(message.getBytes(Charsets.UTF_8));
        LOG.info(message);
    }

}