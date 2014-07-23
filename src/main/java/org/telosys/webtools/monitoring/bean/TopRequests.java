/**
 *  Copyright (C) 2008-2014  Telosys project org. ( http://www.telosys.org/ )
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.telosys.webtools.monitoring.bean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Top longest requests with duplication.
 */
public class TopRequests {
	
	/** Array of stored requests */
	private final Request[] requests;
	/** Position and value of the shortest stored request in the requests array */ 
	private ValuePosition minimum;
	/** Indicates if the requests array is completed */
	private boolean completed = false;
	
	/**
	 * Contains position and value of a request in the requests array.
	 */
	private static class ValuePosition {
		public Long value = null;
		public Integer position = null;
	}
	
	/**
	 * Constructor.
	 * @param size Number of requests in the array.
	 */
	public TopRequests(int size) {
		if(size <= 0) {
			throw new IllegalStateException("size of top requests list must be greater than 0");
		}
		// Création du tableau des requêtes
		requests = new Request[size];
		// Le tableau des requêtes est vide : il n'est donc pas entièrement rempli
		completed = false;
		// Cas de départ : la première requête sera stockée dans la position 0 du tableau des requêtes
		minimum = new ValuePosition();
		minimum.position = 0;
	}
	
	/**
	 * Copy constructor.
	 * @param topRequests Original data to copy.
	 * @param size Number of requests in the array.
	 */
	public TopRequests(TopRequests topRequests, int size) {
		this.requests = new Request[size];
		List<Request> requests = topRequests.getAllDescending();
		int pos = 0;
		for(Request request : requests) {
			if(pos >= size) {
				break;
			}
			this.requests[pos] = request;
			pos++;
		}
		if(pos >= size) {
			this.completed = true;
		}
		this.minimum = getMinimum();
	}
	
	/**
	 * Add new request.
	 * @param request Request
	 */
	public synchronized void add(Request request) {
		if(!completed) {
			// Le tableau n'a pas été entièrement rempli : on met la requête dans une des cases libres du tableau
			requests[minimum.position] = request;
			// calcul de la position de la future requête à ajouter
			if(minimum.position < requests.length-1) {
				// Le tableau contient encore des espaces libres, on passe à la position suivante dans le tableau
				minimum.position++;
			} else {
				// Le tableau ne contient plus d'espaces libres
				// on indique que le tableau a été complété
				completed = true;
				// calcul de la position de la requête la plus courte qui sera remplacée par la future requête à ajouter
				minimum = getMinimum();
			}
		}
		else if(minimum.value <= request.getElapsedTime()) {
			// La requête est plus longue que l'une des requêtes déjà présentes dans le tableau
			requests[minimum.position] = request;
			// calcul de la position de la requête la plus courte qui sera remplacée par la future requête à ajouter
			minimum = getMinimum();
		}
	}
	
	/**
	 * Returns value and position of the shortest stored request.
	 * @return Value and position
	 */
	protected ValuePosition getMinimum() {
		// Calcule la position et la durée d'exécution de la requête la plus courte dans le tableau des requêtes
		ValuePosition minimum = new ValuePosition();
		minimum.position = 0;
		for(int pos=0; pos<requests.length; pos++) {
			Request request = requests[pos];
			if(request == null) {
				break;
			}
			if(minimum.value == null || request.getElapsedTime() < minimum.value) {
				minimum.position = pos;
				minimum.value = request.getElapsedTime();
			}
		}
		return minimum;
	}

	/**
	 * Return all stored requests by ascending
	 * @return requests
	 */
	public synchronized List<Request> getAllAscending() {
		List<Request> all = new ArrayList<Request>();
		for(Request request : requests) {
			if(request != null) {
				all.add(request);
			}
		}
		all.sort(new RequestComparator());
		return all;
	}

	/**
	 * Return all stored requests by descending
	 * @return requests
	 */
	public synchronized List<Request> getAllDescending() {
		List<Request> all = new ArrayList<Request>();
		for(Request request : requests) {
			if(request != null) {
				all.add(request);
			}
		}
		all.sort(new RequestComparator(false));
		return all;
	}

}
