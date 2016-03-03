package org.generationcp.breeding.manager.listimport.actions;

/**
 * This interface mark the expected behavior of a Builder which is to create a business object T from a dataSource S.
 *
 * @param <T> target to be built. These is the business object that will be created.
 * @param <S> SourceDataProvider, this interface contains the data needed to buid the T object.
 *           This Source S needs to be marked as a DataProvider just for semantics reasons.
 */
public interface Builder<T,S extends DataProvider> {

	public T build(S dataProvider);
}
