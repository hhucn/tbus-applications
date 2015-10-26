/**
 * 
 */
package de.hhu.tbus.applications.nt.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;

/**
 * @author bialon
 *
 * Tbus graph calculations
 */
public class TbusAlgorithms<V, E extends SumoEdge> {
	private Deque<String> currentRoute = null;
	private List<List<String> > routes = null;
	private Deque<String> currentRevertedRoute = null;
	private List<List<String> > revertedRoutes = null;
	private DirectedGraph<V, E> graph = null;
	
	/**
	 * Create a TbusAlgorithms object on the given graph
	 * @param graph
	 */
	public TbusAlgorithms(DirectedGraph<V, E> graph) {
		this.graph = graph;
	}
	
	/**
	 * Init values
	 */
	private void init() {
		currentRoute = new ArrayDeque<String>();
		routes = new ArrayList<List<String> >();
		
		currentRevertedRoute = new ArrayDeque<String>();
		revertedRoutes = new ArrayList<List<String> >();
	}
	
	/**
	 * Get a list of all possible routes (lists of edges) starting with edge start and having a maximum length of maxDistance, excluding first and last edge,
	 * on the given graph
	 * @param start Starting edge
	 * @param maxDistance Maximum distance of route, excluding first and last edge
	 * @return A list of all possible routes under the given values
	 */
	public synchronized List<List<String> > getRoutesWithinDisctance(E start, double maxDistance) {
		init(); 
		
		currentRoute.add(start.getId());
		createRoutes(start, maxDistance);
		
		return routes;
	}
	
	/**
	 * Create routes starting from edge start with a maximum distance of maxDistance excluding the first and last edge.
	 * @param start Starting edge
	 * @param maxDistance Maximum distance of route excluding the first and last edge
	 */
	private void createRoutes(E start, double maxDistance) {
		Set<E> neighbors = graph.outgoingEdgesOf(graph.getEdgeTarget(start));
		
		for (E edge: neighbors) {			
			currentRoute.addLast(edge.getId());
			
			double newDistance = maxDistance - edge.getWeight();
			if (newDistance < 0) {
				routes.add(new ArrayList<String>(currentRoute));
			} else {
				createRoutes(edge, newDistance);
			}
			
			currentRoute.removeLast();
		}
	}
	
	/**
	 * Get a list of all possible routes (lists of edges) starting with edge start and having a maximum length of maxDistance, excluding first and last edge,
	 * on the given graph
	 * @param start Starting edge
	 * @param maxDistance Maximum distance of route, excluding first and last edge
	 * @return A list of all possible routes under the given values
	 */
	public synchronized List<List<String> > getRevertedRoutesWithinDisctance(E start, double maxDistance) {
		if( start == null) {
			return new ArrayList<List<String>>();
		}
		
		init(); 
		
		currentRevertedRoute.add(start.getId());
		createRevertedRoutes(start, maxDistance);
		
		return revertedRoutes;
	}
	
	/**
	 * Create routes starting from edge start with a maximum distance of maxDistance excluding the first and last edge.
	 * @param start Starting edge
	 * @param maxDistance Maximum distance of route excluding the first and last edge
	 */
	private void createRevertedRoutes(E start, double maxDistance) {
		Set<E> neighbors = graph.incomingEdgesOf(graph.getEdgeSource(start));
		
		for (E edge: neighbors) {			
			currentRevertedRoute.addLast(edge.getId());
			
			double newDistance = maxDistance - edge.getWeight();
			if (newDistance < 0) {
				revertedRoutes.add(new ArrayList<String>(currentRevertedRoute));
			} else {
				createRoutes(edge, newDistance);
			}
			
			currentRevertedRoute.removeLast();
		}
	}
}
