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

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
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
@Configurable
public class ManagementNeighborsQuery implements Query {

	private final static Logger LOG = LoggerFactory.getLogger(ManagementNeighborsQuery.class);

	public static final Object GID = "gid";
	public static final Object PREFERRED_NAME = "preferred name";

	@Autowired
	private PedigreeDataManager pedigreeDataManager;
	private final Integer gid;
	private int size = 0;

	/**
	 * These parameters are passed by the QueryFactory which instantiates objects of this class.
	 * 
	 * @param dataManager
	 * @param gid
	 * @param columnIds
	 */
	public ManagementNeighborsQuery(Integer gid) {
		super();
		this.gid = gid;
	}

	/**
	 * This method seems to be called for creating blank items on the Table
	 */
	@Override
	public Item constructItem() {
		PropertysetItem item = new PropertysetItem();
		item.addItemProperty(ManagementNeighborsQuery.GID, new ObjectProperty<String>(""));
		item.addItemProperty(ManagementNeighborsQuery.PREFERRED_NAME, new ObjectProperty<String>(""));
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

		List<Item> items = new ArrayList<Item>();

		List<Germplasm> germplasms = new ArrayList<Germplasm>();
		try {
			germplasms.addAll(this.pedigreeDataManager.getManagementNeighbors(this.gid, start, numOfRows));
		} catch (MiddlewareQueryException ex) {
			ManagementNeighborsQuery.LOG.error("Error with getting management neighbors for gid = " + this.gid + ": " + ex.getMessage());
			return new ArrayList<Item>();
		}

		for (Germplasm germplasm : germplasms) {
			PropertysetItem item = new PropertysetItem();
			item.addItemProperty(ManagementNeighborsQuery.GID, new ObjectProperty<String>(germplasm.getGid().toString()));
			if (germplasm.getPreferredName() != null) {
				item.addItemProperty(ManagementNeighborsQuery.PREFERRED_NAME, new ObjectProperty<String>(germplasm.getPreferredName()
						.getNval()));
			} else {
				item.addItemProperty(ManagementNeighborsQuery.PREFERRED_NAME, new ObjectProperty<String>("-"));
			}
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
			if (this.size == 0) {
				this.size = ((Long) this.pedigreeDataManager.countManagementNeighbors(this.gid)).intValue();
			}
		} catch (MiddlewareQueryException ex) {
			ManagementNeighborsQuery.LOG.error("Error with getting number of management neighbors for gid: " + this.gid + "\n"
					+ ex.toString());
		}
		return this.size;
	}
}
