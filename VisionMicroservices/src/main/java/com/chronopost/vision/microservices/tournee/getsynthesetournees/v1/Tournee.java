package com.chronopost.vision.microservices.tournee.getsynthesetournees.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.chronopost.vision.model.getsynthesetournees.v1.ColisPoint;
import com.chronopost.vision.model.getsynthesetournees.v1.PointTournee;
import com.chronopost.vision.model.updatespecificationscolis.v1.SpecifsColis;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

public class Tournee {
    /* Identifiant de la tournée */
    private String identifiantTournee;
    
    /*
     * Liste des colis de la tournée avec leurs événements (ordonnée par date
     * d'événements)
     */
    private final SortedSetMultimap<String, ColisPoint> colisEvenements = TreeMultimap.create();

    /*
     * Une vue non modifiable de la liste des colis de la tournée avec leurs
     * événements (ordonnée par date d'événements)
     */
    private final Map<String, SortedSet<ColisPoint>> colisEvenementsUnmodifiableMap = Multimaps.asMap(Multimaps
            .unmodifiableSortedSetMultimap(colisEvenements));

    /* Liste des colis de la tournée avec leurs spécificités */
    private final Map<String, SpecifsColis> colisSpecifs = new HashMap<>();

    /* Liste des points de la tournée */
    private final List<PointTournee> points = new ArrayList<>();

    
    
    public String getIdentifiantTournee() {
        return identifiantTournee;
    }

    public void setIdentifiantTournee(String identifiantTournee) {
        this.identifiantTournee = identifiantTournee;
    }

    /**
     * Liste des colis de la tournée avec leurs événements (ordonnée par date
     * d'événement)<br>
     * <br>
     * <strong>!!! ATTENTION !!!</strong> Utilise une Multimap en interne. La
     * méthode {@link Multimap#asMap()} renvoie une Map ne supportant pas
     * {@link Map.Entry#setValue(V)}, {@link Map#put(K, V)} ou
     * {@link Map#putAll(Map)}.<br>
     * Par sécurité, cette méthode renvoie donc une Map non modifiable dont les
     * méthodes de modification lèvent une exception si elles sont appelées.<br>
     * Pour manipuler le contenu de cette Map il faut passer par les accesseurs
     * de cette Class ({@link #setColisEvenements(Map)},
     * {@link #putToColisEvenements(String, Collection)},
     * {@link #putAllToColisEvenements(Map)},
     * {@link #putAllToColisEvenements(Multimap)},
     * {@link #removeFromColisEvenements(String)}, et
     * {@link #clearColisEvenements()}).
     * 
     * @return la liste NON MODIFIABLE des colis de la tournée avec leurs événements (ordonnée
     *         par date d'événement)
     */
    public Map<String, SortedSet<ColisPoint>> getColisEvenements() {
        return this.colisEvenementsUnmodifiableMap;
    }

    /**
     * 
     * @param noLt
     * @return la liste des événements d'un colis ou une liste vide si le colis
     *         n'existe pas dans la tournée
     */
    public ImmutableSortedSet<ColisPoint> getColisEvenements(final String noLt) {
        SortedSet<ColisPoint> evts = new TreeSet<>();

        if (noLt != null && this.colisEvenements.get(noLt) != null) {
            evts = this.colisEvenements.get(noLt);
        }

        return ImmutableSortedSet.copyOf(evts);
    }

    /**
     * 
     * @param colisEvenements
     *            liste des colis de la tournée avec leurs événements
     */
    public void setColisEvenements(final Map<String, Collection<ColisPoint>> colisEvenements) {
        this.clearColisEvenements();
        this.putAllToColisEvenements(colisEvenements);
    }

    /**
     * 
     * @param colisEvenements
     *            liste des colis de la tournée avec leurs événements
     */
    public void setColisEvenements(Multimap<String, ColisPoint> colisEvenements) {
        this.clearColisEvenements();
        this.putAllToColisEvenements(colisEvenements);
    }

    /**
     * Ajoute un ColisPoint à un colis.
     * 
     * @param noLt
     *            un identifiant colis
     * @param colisPoint
     *            un ColisPoint
     * @return la liste de ColisPoint précédement associée à ce colis
     */
    public ImmutableCollection<ColisPoint> putToColisEvenements(String noLt, ColisPoint colisPoint) {
        ImmutableCollection<ColisPoint> oldValue = ImmutableSet.copyOf(this.colisEvenements.get(noLt));
        this.colisEvenements.put(noLt, colisPoint);
        return oldValue;
    }

    /**
     * Ajoute des ColisPoint à un colis
     * 
     * @param noLt
     *            un identifiant colis
     * @param colisPoints
     *            une liste de ColisPoint
     * @return la liste de ColisPoint précédement associée à ce colis
     */
    public ImmutableCollection<ColisPoint> putToColisEvenements(String noLt, Collection<ColisPoint> colisPoints) {
        ImmutableCollection<ColisPoint> oldValue = ImmutableSortedSet.copyOf(this.colisEvenements.get(noLt));
        this.colisEvenements.putAll(noLt, colisPoints);
        return oldValue;
    }

    /**
     * Ajoute une liste de colis avec leurs événements. Ajoute le colis et ses
     * événements s'il n'est pas encore présent, sinon ajoute les événements à
     * la liste existante.
     * 
     * @param map
     *            une liste de colis de la tournée avec leurs événements
     */
    public void putAllToColisEvenements(Map<String, Collection<ColisPoint>> map) {
        if (map != null) {
            for (Map.Entry<String, Collection<ColisPoint>> entry : map.entrySet()) {
                this.colisEvenements.putAll(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Ajoute une liste de colis avec leurs événements. Ajoute le colis et ses
     * événements s'il n'est pas encore présent, sinon ajoute les événements à
     * la liste existante.
     * 
     * @param multimap
     *            une liste de colis de la tournée avec leurs événements
     */
    public void putAllToColisEvenements(Multimap<String, ColisPoint> multimap) {
        this.colisEvenements.putAll(multimap);
    }

    /**
     * Supprime un colis et ses événements de la liste des colis de la tournée.
     * 
     * @param noLt
     *            un identifiant colis
     * @return la liste des événements précédement stockés pour cet identifiant
     *         de colis
     */
    public Collection<ColisPoint> removeFromColisEvenements(String noLt) {
        return this.colisEvenements.removeAll(noLt);
    }

    /**
     * Vide la liste des colis de la tournée avec leurs événements.
     */
    public void clearColisEvenements() {
        this.colisEvenements.clear();
    }

    /**
     * Liste des colis de la tournée avec leurs événements (ordonnée par date
     * d'événement)<br>
     * <br>
     * <strong>!!! ATTENTION !!!</strong> Par sécurité, cette méthode renvoie
     * une Map non modifiable dont les méthodes de modification lèvent une
     * exception si elles sont appelées.<br>
     * Pour manipuler le contenu de cette Map il faut passer par les accesseurs
     * de cette Class ({@link #setColisSpecifs(Map)},
     * {@link #putToColisSpecifs(String, ColisSpecifs)},
     * {@link #putAllToColisSpecifs(Map)},
     * {@link #removeFromColisSpecifs(String)}, et {@link #clearColisSpecifs()}
     * ).
     * 
     * @return la liste des colis de la tournée avec leurs spécificités
     */
    public ImmutableMap<String, SpecifsColis> getColisSpecifs() {
        return ImmutableMap.copyOf(this.colisSpecifs);
    }

    /**
     * 
     * @param colisSpecifs
     *            liste des colis de la tournée avec leurs événements
     */
    public void setColisSpecifs(Map<String, SpecifsColis> colisSpecifs) {
        this.clearColisSpecifs();
        this.putAllToColisSpecifs(colisSpecifs);
    }

    /**
     * Ajoute un ColisSpecifs à un colis.
     * 
     * @param noLt
     *            un identifiant colis
     * @param specifs
     *            un ColisSpecifs
     * @return la liste de ColisSpecifs précédement associée à ce colis
     */
    public SpecifsColis putToColisSpecifs(String noLt, SpecifsColis specifs) {
        return this.colisSpecifs.put(noLt, specifs);
    }

    /**
     * Ajoute une liste de colis avec leurs spécificités.
     * 
     * @param map
     *            une liste de colis de la tournée avec leurs spécificités
     */
    public void putAllToColisSpecifs(Map<String, SpecifsColis> map) {
        this.colisSpecifs.putAll(map);
    }

    /**
     * Supprime un colis et ses spécificités de la liste des colis de la
     * tournée.
     * 
     * @param noLt
     *            un identifiant colis
     * @return la liste des spécificités précédement stockés pour cet
     *         identifiant de colis
     */
    public SpecifsColis removeFromColisSpecifs(String noLt) {
        return this.colisSpecifs.remove(noLt);
    }

    /**
     * Vide la liste des colis de la tournée avec leurs spécificités.
     */
    public void clearColisSpecifs() {
        this.colisSpecifs.clear();
    }

    /**
     * Liste des points de la tournée<br>
     * <br>
     * <strong>!!! ATTENTION !!!</strong> Par sécurité, cette méthode renvoie
     * une List non modifiable dont les méthodes de modification lèvent une
     * exception si elles sont appelées.<br>
     * Pour manipuler le contenu de cette List il faut passer par les accesseurs
     * de cette Class ({@link #setPoints(List)},
     * {@link #addToPoints(PointTournee)}, {@link #addAllToPoints(List)},
     * {@link #removeFromPoints(PointTournee)}, et {@link #clearPoints()} ).
     * 
     * @return la liste des colis de la tournée avec leurs spécificités
     */
    public ImmutableList<PointTournee> getPoints() {
        return ImmutableList.copyOf(this.points);
    }

    public PointTournee getPoint(final String identifiantPoint) {
        PointTournee point = null;

        Predicate<PointTournee> predicate = new Predicate<PointTournee>() {
            public boolean apply(PointTournee point) {
                return point.getIdentifiantPoint().equals(identifiantPoint);
            }
        };

        SortedSet<PointTournee> filteredSet = Sets.newTreeSet(Iterables.filter(this.points, predicate));

        if (filteredSet.size() == 1) {
            point = filteredSet.first();
        }

        return point;
    }

    /**
     * 
     * @param points
     *            une liste de point dans une tournée
     */
    public void setPoints(List<PointTournee> points) {
        this.clearPoints();
        this.addAllToPoints(points);
    }

    /**
     * Ajoute un point dans la tournée
     * 
     * @param point
     *            un PointTournee
     * @return <code>true</code> si la liste de points de la tournée est
     *         correctement mise à jour
     */
    public boolean addToPoints(PointTournee point) {
        return this.points.add(point);
    }

    /**
     * Ajoute une liste de points dans la tournée
     * 
     * @param points
     *            une liste de PointTournee
     * @return <code>true</code> si la liste de points de la tournée est
     *         correctement mise à jour
     */
    public boolean addAllToPoints(List<PointTournee> points) {
        return this.points.addAll(points);
    }

    /**
     * Supprime un point de la tournée
     * 
     * @param point
     *            un PointTournee
     * @return <code>true</code> si la liste contient le point spécifié
     */
    public boolean removeFromPoints(PointTournee point) {
        return this.points.remove(point);
    }

    /**
     * Vide la liste des points d'une tournée
     */
    public void clearPoints() {
        this.points.clear();
    }
}
