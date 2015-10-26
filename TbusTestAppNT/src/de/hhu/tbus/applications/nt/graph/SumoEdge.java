/**
 * 
 */
package de.hhu.tbus.applications.nt.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * @author bialon
 *
 */
public class SumoEdge extends DefaultWeightedEdge {

	private double weight;
	private String id;

	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = -3109313072598177444L;

	
	public SumoEdge(String id, double weight) {
		this.id = id;
		this.weight = weight;
	}
	
	@Override
	protected double getWeight() {
		return weight;
	}
	
	public String getId() {
		return id;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SumoEdge other = (SumoEdge) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (Double.doubleToLongBits(weight) != Double
				.doubleToLongBits(other.weight))
			return false;
		return true;
	}
}
