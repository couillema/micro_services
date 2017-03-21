package com.chronopost.vision.microservices.featureflips;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import com.chronopost.featureflips.FeatureFlips;
import com.chronopost.vision.model.featureflips.GetFeatureFlipOutput;
import com.chronopost.vision.transco.dao.TranscoderDao;

public class FeatureFlipsServiceImpl implements IFeatureFlipsService {

    /**
     * Singleton
     */
    static class InstanceHolder {

        public static IFeatureFlipsService service = new FeatureFlipsServiceImpl();

    }

    /**
     * Singleton
     * 
     * @return
     */
    public static IFeatureFlipsService getInstance() {

        return InstanceHolder.service;
    }

    public String getFeatureFlipsHtml(String baseUrl) throws IOException {
        BufferedReader flipsHtmlReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(
                "/assets/flips.html")));
        StringBuilder flipsHtml = new StringBuilder();
        String line = null;
        while ((line = flipsHtmlReader.readLine()) != null) {
            flipsHtml.append(line + " ");
        }

        return flipsHtml.toString().replace("%%FLIPS_PLACEHOLDER%%", formatFeatureFlips());
    }

    public String formatFeatureFlips() {
        StringBuilder htmlBuilder = new StringBuilder();
        Map<String, Map<String, String>> projetTransco = TranscoderDao.INSTANCE
                .getTranscodificationsFromDatabase(FeatureFlips.INSTANCE.getFlipProjectName());
        if (projetTransco.containsKey(FeatureFlips.INSTANCE.FLIP_FAMILY_NAMES)) {
            for (Entry<String, String> flip : projetTransco.get(FeatureFlips.INSTANCE.FLIP_FAMILY_NAMES)
                    .entrySet()) {
                htmlBuilder.append(FeatureFlipsFormatter.formatFlip(flip));
            }

            return htmlBuilder.toString();
        }

        return "Aucun flip disponible";

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.chronopost.vision.microservices.featureflips.IFeatureFlipsService
     * #updateFeatureFlip(java.lang.String, java.lang.String)
     */
    public boolean updateFeatureFlip(String nomFlip, String valeurFlip) {
        TranscoderDao.INSTANCE.updateTransco(FeatureFlips.INSTANCE.getFlipProjectName(),
                FeatureFlips.INSTANCE.FLIP_FAMILY_NAMES, nomFlip, valeurFlip);
        return true;
    }

    /**
     * Suppression d'un flip de la base
     * 
     * @param nomFlip
     * @param valeurFlip
     * @return
     */
    public boolean deleteFeatureFlip(String nomFlip) {
        TranscoderDao.INSTANCE.deleteTransco(FeatureFlips.INSTANCE.getFlipProjectName(),
                FeatureFlips.INSTANCE.FLIP_FAMILY_NAMES, nomFlip);
        return true;
    }

    @Override
    public GetFeatureFlipOutput getFeatureFlip(String nomFlip) {
        return new GetFeatureFlipOutput().putFlip(nomFlip, FeatureFlips.INSTANCE.getBoolean(nomFlip, false)
                .toString());
    }
}
