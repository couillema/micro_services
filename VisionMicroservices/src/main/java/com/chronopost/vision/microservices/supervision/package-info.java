/**
 *
 * Le micro service Supervision permet de suivre l’état des injections Evt et Lt
 * ou de la diffusion en cours par les autres microservices. Dans sa forme
 * première il retourne l’état de plusieurs indicateurs (vitesses d’injections
 * et de diffusion) utile à la supervision des systèmes.
 * 
 * Il est également possible de récupérer un relevé de ces états sur une période
 * de données afin de constituer un graphique de tendances ou d’historique.
 * 
 * @author XRE
 */
package com.chronopost.vision.microservices.supervision;