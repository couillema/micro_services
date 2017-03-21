package com.chronopost.vision.microservices;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.microservices.supervision.ISupervisionDao;

public class MSResponseFilter implements ContainerResponseFilter {

	private ISupervisionDao dao;

	@Override
	public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
			throws IOException {
		if (FeatureFlips.INSTANCE.getBoolean("TraceAppels_Actif", false)) {
			final String url = requestContext.getUriInfo().getRequestUri().getPath();
			final String[] splitUrl = url.split("/");
			final String microservice = url.startsWith("/") ? splitUrl[1] : splitUrl[0];
			final Date requestDate = (Date) requestContext.getProperty("request.date");
			final Long timeExec = new Long(new Date().getTime() - requestDate.getTime());
			final String userAgent = requestContext.getHeaderString("user_agent") == null ? "unknown"
					: requestContext.getHeaderString("user_agent");
			dao.insertMSAppelsInfos(microservice, requestDate, url, timeExec, userAgent);
		}
	}

	public void setDao(final ISupervisionDao instance) {
		this.dao = instance;
	}
}
