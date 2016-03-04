package org.generationcp.breeding.manager.listimport.actions;

/**
 * Represents a minimal execution that has meaning by itself.
 *
 * @param <T> Is the context object that contains all the data needed for the execution and in which every executable
 *            in the process adds some value to the next one.
 */
public interface Executable<T> {

	public T execute(T target) throws BMSExecutionException;
}
