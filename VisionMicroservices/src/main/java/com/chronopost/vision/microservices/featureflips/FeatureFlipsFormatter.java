package com.chronopost.vision.microservices.featureflips;

import java.io.FileNotFoundException;
import java.util.Map.Entry;

import com.chronopost.vision.microservices.sdk.utils.ServiceMockResponses;

public class FeatureFlipsFormatter {
	
	public static String getHtmlTemplate() throws FileNotFoundException{
		
		return ServiceMockResponses.readResponse("assets/flips.html");
	}
	
	public static String formatFlip(Entry<String,String> flip){
		if(flip.getValue().equalsIgnoreCase("true") || flip.getValue().equalsIgnoreCase("false")){
			String checked = flip.getValue().equalsIgnoreCase("true")?"checked=\"checked\"":"";
			return "<tr><td><strong>" + flip.getKey() + "</strong></span></td><td style=\"text-align:right\"><span><input type=\"checkbox\" class=\"switch\" name=\"" + flip.getKey() + "\" " + checked + " /><span style=\"color:red\">&nbsp;<a href=\"#\" style=\"color:red;font-size:small\" onclick=\"deleteFlagStep1('" + flip.getKey() + "');return false;\">Supprimer</a></span></td></tr><tr><td colspan=\"2\"><hr/></td></tr>";
		}
		
		return "";
	}

}
