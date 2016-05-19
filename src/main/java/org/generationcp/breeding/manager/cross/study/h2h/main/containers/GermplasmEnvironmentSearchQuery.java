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

package org.generationcp.breeding.manager.cross.study.h2h.main.containers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.h2h.GermplasmLocationInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.vaadin.addons.lazyquerycontainer.Query;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * An implementation of Query which is needed for using the LazyQueryContainer.
 *
 * Reference: https://vaadin.com/wiki/-/wiki/Main/Lazy%20Query%20Container/#section
 * -Lazy+Query+Container-HowToImplementCustomQueryAndQueryFactory
 *
 * @author Joyce Avestro
 *
 */
public class GermplasmEnvironmentSearchQuery implements Query {

	public static final Object GID = "gid";
	public static final Object NAMES = "names";
	public static final Object LOCATION = "location";

	private final CrossStudyDataManager crossStudyDataManager;
	private final String searchChoice;
	private final String searchValue;
	private final List<Integer> environmentIds;
	private int size;

	/**
	 * These parameters are passed by the QueryFactory which instantiates objects of this class.
	 * 
	 * @param environmentIds
	 * 
	 */
	public GermplasmEnvironmentSearchQuery(CrossStudyDataManager crossStudyDataManager, String searchChoice, String searchValue,
			List<Integer> environmentIds) {
		super();
		this.crossStudyDataManager = crossStudyDataManager;
		this.searchChoice = searchChoice;
		this.searchValue = searchValue;
		this.environmentIds = environmentIds;
		this.size = -1;
	}

	/**
	 * This method seems to be called for creating blank items on the Table
	 */
	@Override
	public Item constructItem() {
		PropertysetItem item = new PropertysetItem();
		item.addItemProperty(GermplasmEnvironmentSearchQuery.GID, new ObjectProperty<String>(""));
		item.addItemProperty(GermplasmEnvironmentSearchQuery.NAMES, new ObjectProperty<String>(""));
		item.addItemProperty(GermplasmEnvironmentSearchQuery.LOCATION, new ObjectProperty<String>(""));
		return item;
	}

	@Override
	public boolean deleteAllItems() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieves the dataset by batches of rows. Used for lazy loading the dataset.
	 */
	@Override
	public List<Item> loadItems(int start, int numOfRows) {

		List<GermplasmLocationInfo> searchCandidates = new ArrayList<GermplasmLocationInfo>();
		try {
			searchCandidates =
					this.crossStudyDataManager.getGermplasmLocationInfoByEnvironmentIds(new HashSet<Integer>(this.environmentIds));
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<Item> items = new ArrayList<Item>();

		for (GermplasmLocationInfo candidate : searchCandidates) {
			if (String.valueOf(candidate.getGid()).contains(this.searchValue)
					|| StringUtils.containsIgnoreCase(candidate.getGermplasmName(), this.searchValue)) {
				PropertysetItem item = new PropertysetItem();
				item.addItemProperty(GermplasmEnvironmentSearchQuery.GID, new ObjectProperty<String>(candidate.getGid().toString()));
				item.addItemProperty(GermplasmEnvironmentSearchQuery.NAMES, new ObjectProperty<String>(candidate.getGermplasmName()));
				item.addItemProperty(GermplasmEnvironmentSearchQuery.LOCATION, new ObjectProperty<String>(candidate.getLocationName()));
				items.add(item);
			}
		}

		this.size = items.size();

		return items;
	}

	@Override
	public void saveItems(List<Item> arg0, List<Item> arg1, List<Item> arg2) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the total number of rows to be displayed on the Table
	 */
	@Override
	public int size() {
		return this.size;
	}

}
