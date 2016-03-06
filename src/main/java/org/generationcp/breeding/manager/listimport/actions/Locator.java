package org.generationcp.breeding.manager.listimport.actions;

import java.util.List;

public interface Locator<K,T> {

	public List<T> locate(K key) throws NoElementFoundException;
}
