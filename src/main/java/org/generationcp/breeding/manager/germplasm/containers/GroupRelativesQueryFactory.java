/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 * @author Kevin L. Manansala
 *
 *         This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of
 *         Part F of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 **************************************************************/

package org.generationcp.breeding.manager.germplasm.containers;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

/**
 * An implementation of QueryFactory which is needed for using the LazyQueryContainer.
 *
 * Reference: https://vaadin.com/wiki/-/wiki/Main/Lazy%20Query%20Container/#section
 * -Lazy+Query+Container-HowToImplementCustomQueryAndQueryFactory
 *
 * @author Kevin Manansala
 *
 */
public class GroupRelativesQueryFactory implements QueryFactory {

	private final Integer gid;
	@SuppressWarnings("unused")
	private QueryDefinition definition;

	public GroupRelativesQueryFactory(Integer gid) {
		super();
		this.gid = gid;
	}

	/**
	 * Create the Query object to be used by the LazyQueryContainer. Sorting is not yet supported so the parameters to this method are not
	 * used.
	 */
	@Override
	public Query constructQuery(Object[] sortPropertyIds, boolean[] sortStates) {
		return new GroupRelativesQuery(this.gid);
	}

	@Override
	public void setQueryDefinition(QueryDefinition definition) {
		// not sure how a QueryDefinition is used and how to create one
		// for the current implementation this is not used and I just copied
		// this method declaration
		// from the reference
		this.definition = definition;
	}
}
