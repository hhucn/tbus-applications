/**
 * 
 */
package de.hhu.tbus.applications.nt.graph;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author bialon
 * @author Norbert Goebel
 *
 */
public class TbusRoadGraph {
	private DirectedGraph<String, SumoEdge> graph = null;
	private TbusAlgorithms<String, SumoEdge> algos = null;
	
	/**
	 * Id to graph internal edge mapping
	 */
	private Map<String, SumoEdge> idToEdge = null;
	
	/**
	 * Singleton instance
	 */
	static private TbusRoadGraph instance = new TbusRoadGraph();
	/**
	 * Private constructor for singleton
	 */
	private TbusRoadGraph() {}
	
	/**
	 * Singleton getter
	 * @return TbusRoadGraph singleton instance
	 */
	static public TbusRoadGraph getInstance() {
		return instance;
	}
	
	/**
	 * Initializes internal variables
	 */
	private void init() {
		idToEdge = new HashMap<String, SumoEdge>();
	}
	
	/**
	 * Parses the given document using a DOM structure
	 * @param dom Document to parse
	 */
	private DirectedGraph<String, SumoEdge> parseDocument(Document dom) {
		DirectedGraph<String, SumoEdge> newGraph = new DefaultDirectedWeightedGraph<String, SumoEdge>(SumoEdge.class);
		Element root = dom.getDocumentElement();
		
		NodeList edges = root.getElementsByTagName("edge");
		
		for (int i = 0; i < edges.getLength(); ++i) {
			Element edge = (Element) edges.item(i);
			
			// Skip internal edges
			if (edge.getAttribute("function").equals("internal")) {
				continue;
			}
			
			String id = edge.getAttribute("id");
			
			double weight = 1.0;

			NodeList childs = edge.getElementsByTagName("lane");
			if (childs.getLength() > 0) {
				Element lane = (Element) childs.item(0);
				weight = Double.parseDouble(lane.getAttribute("length"));
			}
			
			
			String from = edge.getAttribute("from");
			String to = edge.getAttribute("to");
			
			newGraph.addVertex(from);
			newGraph.addVertex(to);
			
			SumoEdge sumoEdge = new SumoEdge(id, weight);
			newGraph.addEdge(from, to, sumoEdge);
			idToEdge.put(id, sumoEdge);
		}
		
		return newGraph;
	}
	
	/**
	 * Parses the given file and extracts a SUMO network graph
	 * @param sumoNet The XML file containing a SUMO graph
	 */
	public synchronized void parse(File sumoNet) {
		if (graph != null) {
			// This singleton already parsed a file, error!
			System.err.println("Error, " + toString() + " already parsed a file, this singleton would become invalid! Aborting method and keeping a valid state!");
			return;
		}
		
		init();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(sumoNet);
			
			graph = parseDocument(dom);
			algos = new TbusAlgorithms<String, SumoEdge>(graph);
		} catch (IOException | SAXException | ParserConfigurationException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Return the graph's edge identified by id
	 * @param id Edge id
	 * @return Graph internal edge
	 */
	private SumoEdge getEdge(String id) {
		return idToEdge.get(id);
	}
	
	/**
	 * Return the graph's edge length identified by edgeid
	 * @param id Edge id
	 * @return Graph internal edge
	 */
	public double getEdgeLength(String id) {
		return idToEdge.get(id).getWeight();
	}
	
	/**
	 * Return a set of outgoing edges from the current edges endvertex
	 * @param currentEdge
	 * @return set of edges
	 */
	public synchronized Set<String> getNextEdges(String currentEdge) {
		String currentEdgeEnd = graph.getEdgeTarget(getEdge(currentEdge));
		Set<SumoEdge> edges = graph.outgoingEdgesOf(currentEdgeEnd);
		
		Set<String> edgeIds = new HashSet<String>();
		for (SumoEdge edge: edges) {
			edgeIds.add(edge.getId());
		}
		
		return edgeIds;
	}
	
	/**
	 * Return a set of incoming edges to the current edges endvertex
	 * @param currentEdge
	 * @return set of edges
	 */
	public synchronized Set<String> getIncomingEdges(String currentEdge) {
		String currentEdgeEnd = graph.getEdgeTarget(getEdge(currentEdge));
		Set<SumoEdge> edges = graph.incomingEdgesOf(currentEdgeEnd);
		
		Set<String> edgeIds = new HashSet<String>();
		for (SumoEdge edge: edges) {
			edgeIds.add(edge.getId());
		}
		
		return edgeIds;
	}

	/**
	 * Get the length of the shortest path between two edges
	 * @param from Start edge
	 * @param to End edge
	 * @return Distance length
	 */
	public synchronized double getDistance(String from, String to) {
		return (new DijkstraShortestPath<String, SumoEdge>(graph, from, to)).getPathLength();
	}
	
	/**
	 * Get a list of all routes with maximum distance distance (excluding first and last edge), starting from edge from 
	 * @param from Starting edge
	 * @param distance Maximum distance (excluding first and last edge)
	 * @return A list of routes
	 */
	public synchronized List<List<String> > getRoutesStartingFrom(String from, double distance) {
		return algos.getRoutesWithinDisctance(getEdge(from), distance);
	}
	
	/**
	 * Get a list of all routes with maximum distance distance (excluding first and last edge), ending at edge to
	 * @param to Ending edge
	 * @param distance Maximum distance (excluding first and last edge)
	 * @return A list of routes
	 */
	public synchronized List<List<String> > getRoutesLeadingTo(String to, double distance) {
		return algos.getRevertedRoutesWithinDisctance(getEdge(to), distance);
	}

}
