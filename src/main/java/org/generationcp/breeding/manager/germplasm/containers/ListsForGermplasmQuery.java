/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.breeding.manager.germplasm.containers;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author Kevin Manansala
 *
 */
public class ListsForGermplasmQuery implements Query {

	private static final Logger LOG = LoggerFactory.getLogger(ListsForGermplasmQuery.class);

	public static final Object GERMPLASMLIST_ID = "id";
	public static final Object GERMPLASMLIST_NAME = "name";
	public static final Object GERMPLASMLIST_DATE = "date";
	public static final Object GERMPLASMLIST_DESCRIPTION = "description";

	private final GermplasmListManager dataManager;
	private final Integer gid;
	private int size;

	/**
	 * These parameters are passed by the QueryFactory which instantiates objects of this class.
	 * 
	 * @param dataManager
	 * @param representationId
	 * @param columnIds
	 */
	public ListsForGermplasmQuery(GermplasmListManager dataManager, Integer gid) {
		super();
		this.dataManager = dataManager;
		this.gid = gid;
		this.size = -1;
	}

	/**
	 * This method seems to be called for creating blank items on the Table
	 */
	@Override
	public Item constructItem() {
		PropertysetItem item = new PropertysetItem();
		item.addItemProperty(ListsForGermplasmQuery.GERMPLASMLIST_ID, new ObjectProperty<String>(""));
		item.addItemProperty(ListsForGermplasmQuery.GERMPLASMLIST_NAME, new ObjectProperty<String>(""));
		item.addItemProperty(ListsForGermplasmQuery.GERMPLASMLIST_DATE, new ObjectProperty<String>(""));
		item.addItemProperty(ListsForGermplasmQuery.GERMPLASMLIST_DESCRIPTION, new ObjectProperty<String>(""));
		return item;
	}

	@Override
	public boolean deleteAllItems() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Item> loadItems(int start, int numOfRows) {
		List<Item> items = new ArrayList<Item>();

		List<GermplasmList> germplasmLists = new ArrayList<GermplasmList>();
		try {
			germplasmLists.addAll(this.dataManager.getGermplasmListByGID(this.gid, start, numOfRows));
		} catch (MiddlewareQueryException ex) {
			ListsForGermplasmQuery.LOG.error("Error with getting lists for gid = " + this.gid + ": " + ex.getMessage(), ex);
			return new ArrayList<Item>();
		}

		for (GermplasmList list : germplasmLists) {
			PropertysetItem item = new PropertysetItem();
			item.addItemProperty(ListsForGermplasmQuery.GERMPLASMLIST_ID, new ObjectProperty<String>(list.getId().toString()));
			item.addItemProperty(ListsForGermplasmQuery.GERMPLASMLIST_NAME, new ObjectProperty<String>(list.getName()));
			item.addItemProperty(ListsForGermplasmQuery.GERMPLASMLIST_DATE, new ObjectProperty<String>(String.valueOf(list.getDate())));
			item.addItemProperty(ListsForGermplasmQuery.GERMPLASMLIST_DESCRIPTION, new ObjectProperty<String>(list.getDescription()));
			items.add(item);
		}

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
		try {
			if (this.size == -1) {
				this.size = ((Long) this.dataManager.countGermplasmListByGID(this.gid)).intValue();
			}
		} catch (MiddlewareQueryException e) {
			ListsForGermplasmQuery.LOG.error("Error in countGermplasmListDataByGID in size() " + e.getMessage(), e);
		}
		return this.size;
	}

}
