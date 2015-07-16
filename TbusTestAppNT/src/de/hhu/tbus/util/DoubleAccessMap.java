/**
 * 
 */
package de.hhu.tbus.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author bialon
 * @param <K> Key class
 * @param <V> Value class
 *
 */
public class DoubleAccessMap<K, V> implements Map<K, V> {
	private HashMap<K, V> map = new HashMap<K, V>();
	private HashMap<V, Set<K>> reverseMap = new HashMap<V, Set<K>>();

	/**
	 * @see java.util.Map#clear()
	 */
	@Override
	public void clear() {
		map.clear();
		reverseMap.clear();
	}

	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		return reverseMap.containsKey(value);
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	/**
	 * @see java.util.Map#get(java.lang.Object)
	 */
	@Override
	public V get(Object key) {
		return map.get(key);
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	public Set<K> getKeys(V value) {
		return reverseMap.get(value);
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	/**
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put(K key, V value) {
		V oldValue = map.get(key);
		V result = map.put(key, value);
		
		// Remove old reversed value
		Set<K> oldKeys = reverseMap.get(oldValue);
		if (oldKeys != null) {
			oldKeys.remove(key);
			
			if (oldKeys.isEmpty()) {
				reverseMap.remove(oldKeys);
			}
		}
		
		Set<K> keys = reverseMap.get(value);
		if (keys == null) {
			keys = new HashSet<K>();
			reverseMap.put(value, keys);
		}
		
		keys.add(key);
		
		return result;
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (java.util.Map.Entry<? extends K, ? extends V> entry: m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	@Override
	public V remove(Object key) {
		V result = map.remove(key);
		
		if (result != null) {
			Set<K> keys = reverseMap.get(result);
			
			if (keys.size() == 1) {
				reverseMap.remove(result);
			} else {
				keys.remove(key);
			}
		}
		
		return result;
	}

	/**
	 * @see java.util.Map#size()
	 */
	@Override
	public int size() {
		return map.size();
	}

	/**
	 * @see java.util.Map#values()
	 */
	@Override
	public Collection<V> values() {
		return map.values();
	}

}
