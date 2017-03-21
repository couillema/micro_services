package com.chronopost.vision.microservices.getsyntheseagence.v1.collections;

import java.util.HashMap;
import java.util.Map;

import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.google.common.collect.ImmutableMap;

public class ColisSpecByAgence {
    /**
     * ensemble des colis (no_lt) ayant fait l’objet d’un événement dans cet agence, durant cette période.
     */
    private final Map<String, SpecifsColis> colisSaisis = new HashMap<>();
    private ImmutableMap<String, SpecifsColis> colisSaisisUnmodifiable = ImmutableMap.copyOf(colisSaisis);
    private boolean colisSaisisUpdated = false;

    /**
     * ensemble des colis (no_lt) devant avoir une saisie sur l’agence dans le jour indiqué (jour)
     */
    private final Map<String, SpecifsColis> colisASaisir = new HashMap<>();
    private ImmutableMap<String, SpecifsColis> colisASaisirUnmodifiable = ImmutableMap.copyOf(colisASaisir);
    private boolean colisASaisirUpdated = false;
    private final Map<String, SpecifsColis> colisRestantTg2 = new HashMap<>();
    
    /**
     * Renvoie une map <strong>immutable</strong> de colis saisis.<br>
     * 
     * @return les colis saisis
     */
    public ImmutableMap<String, SpecifsColis> getColisSaisis() {
        if (colisSaisisUpdated) {
        	if (colisSaisis!=null)
        		colisSaisisUnmodifiable = ImmutableMap.copyOf(colisSaisis);
        	else
        		colisSaisisUnmodifiable = null;
            colisSaisisUpdated = false;
        }

        return colisSaisisUnmodifiable;
    }

    /**
     * Renvoie les spécificités d'un colis.
     * 
     * @param noLt
     * @return les spécificités d'un colis
     */
    public SpecifsColis getColisSaisis(String noLt) {
        return colisSaisis.get(noLt);
    }

    /**
     * Ajout un colis à la liste des colis saisis.
     * 
     * @param noLt
     * @param specifColis
     */
    public void putToColisSaisis(String noLt, SpecifsColis specifColis) {
        colisSaisis.put(noLt, specifColis);
        colisSaisisUpdated = true;
    }

    /**
     * Ajout une liste de colis à la liste des colis saisis.
     * 
     * @param m
     *            une liste de colis saisis à ajouter.
     */
    public void putAllToColisSaisis(Map<String, SpecifsColis> m) {
        colisSaisis.putAll(m);
        colisSaisisUpdated = true;
    }

    /**
     * Retire un colis de la liste des colis saisis.
     * 
     * @param noLt
     */
    public void removeFromColisSaisis(String noLt) {
        colisSaisis.remove(noLt);
        colisSaisisUpdated = true;
    }

    /**
     * Vide la liste des colis saisis.
     */
    public void clearColisSaisis() {
        colisSaisis.clear();
        colisSaisisUpdated = true;
    }

    /**
     * Set la map de colis saisis. Si le paramètre est null, la map est juste vidé.
     * 
     * @param m
     */
    public void setColisSaisis(Map<String, SpecifsColis> m) {
        clearColisSaisis();
        if (m != null) {
            putAllToColisSaisis(m);
        }
    }

    /**
     * Renvoie une map <strong>immutable</strong> de colis saisis.<br>
     * 
     * @return les colis à saisir.
     */
    public ImmutableMap<String, SpecifsColis> getColisASaisir() {
        if (colisASaisirUpdated) {
            colisASaisirUnmodifiable = ImmutableMap.copyOf(colisASaisir);
            colisASaisirUpdated = false;
        }

        return colisASaisirUnmodifiable;
    }

    /**
     * Renvoie les spécificités d'un colis.
     * 
     * @param noLt
     * @return les spécificités d'un colis
     */
    public SpecifsColis getColisASaisir(String noLt) {
        return colisSaisis.get(noLt);
    }

    /**
     * Ajout un colis à la liste des colis à saisir.
     * 
     * @param noLt
     * @param specifColis
     */
    public void putToColisASaisir(String noLt, SpecifsColis specifColis) {
        colisSaisis.put(noLt, specifColis);
        colisASaisirUpdated = true;
    }

    /**
     * Ajout une liste de colis à la liste des colis à saisir.
     * 
     * @param m
     *            une liste de colis à saisir à ajouter.
     */
    public void putAllToColisASaisir(Map<String, SpecifsColis> m) {
        colisASaisir.putAll(m);
        colisASaisirUpdated = true;
    }

    
    
    
    /**
     * Retire un colis de la liste des colis à saisir.
     * 
     * @param noLt
     */
    public void removeFromColisASaisir(String noLt) {
        colisSaisis.remove(noLt);
        colisASaisirUpdated = true;
    }

    /**
     * Vide la liste des colis à saisir.
     */
    public void clearColisASaisir() {
        colisASaisir.clear();
        colisASaisirUpdated = true;
    }

    /**
     * Set la map de colis à saisir. Si le paramètre est null, la map est juste vidé.
     * 
     * @param m
     */
    public void setColisASaisir(Map<String, SpecifsColis> m) {
        clearColisASaisir();
        if (m != null) {
            putAllToColisASaisir(m);
        }
    }

	/**
	 * 
	 * @return
	 */
	public Map<String, SpecifsColis> getColisRestantTg2() {
		return colisRestantTg2;
	}

	/**
	 * @param m
	 *            Map <noLT, Date>
	 */
	public void putAllToColisRestantTg2(Map<String, SpecifsColis> m) {
		colisRestantTg2.putAll(m);
	}
}
