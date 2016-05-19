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

package org.generationcp.breeding.manager.germplasm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class SaveGermplasmListAction.
 */
@Configurable
public class SaveGermplasmListAction implements Serializable, InitializingBean {

	private static final long serialVersionUID = 1L;

	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private ContextUtil contextUtil;

	/**
	 * Instantiates a new SaveGermplasmListAction.
	 */
	public SaveGermplasmListAction() {

	}

	/**
	 * Adds the germplasm list name and data.
	 *
	 * @param listName the list name
	 * @param tabSheet the tab sheet
	 * @param type
	 * @param description
	 * @throws MiddlewareQueryException the query exception
	 */
	public void addGermplasListNameAndData(String listName, String listId, TabSheet tabSheet, String description, String type) {

		try {

			Integer userId = this.contextUtil.getCurrentUserLocalId();
			GermplasmList parent = null;
			int statusListName = 1;
			String gidListString = "";

			if ("null".equals(listId)) {
				GermplasmList listNameData = new GermplasmList(null, listName, DateUtil.getCurrentDateAsLongValue(), type, userId,
						description, parent, statusListName);
				listNameData.setProgramUUID(this.contextUtil.getCurrentProgramUUID());
				int listid = this.germplasmListManager.addGermplasmList(listNameData);

				GermplasmList germList = this.germplasmListManager.getGermplasmListById(listid);

				String groupName = "-";
				String designation = "-";
				int status = 0;
				int localRecordId = 0;
				int entryid = 1;

				for (int i = 0; i < tabSheet.getComponentCount(); i++) {
					Tab currentTab = tabSheet.getTab(i);
					int gid = Integer.valueOf(currentTab.getCaption().toString());

					// save germplasm's preferred name as designation
					designation = this.getDesignationFromTab(currentTab);
					String entryCode = this.getPreferredId(tabSheet, gid);
					String seedSource = "Browse for " + currentTab.getDescription();
					GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, entryid, entryCode, seedSource,
							designation, groupName, status, localRecordId);

					this.germplasmListManager.addGermplasmListData(germplasmListData);
					entryid++;

					gidListString = gidListString + ", " + Integer.toString(gid);

				}
			} else {

				GermplasmList germList = this.germplasmListManager.getGermplasmListById(Integer.valueOf(listId));
				String groupName = "-";
				String designation = "-";
				int status = 0;
				int localRecordId = 0;
				int entryid = (int) this.germplasmListManager.countGermplasmListDataByListId(Integer.valueOf(listId));

				for (int i = 0; i < tabSheet.getComponentCount(); i++) {
					Tab currentTab = tabSheet.getTab(i);
					int gid = Integer.valueOf(currentTab.getCaption().toString());

					String entryCode = this.getPreferredId(tabSheet, gid);
					String seedSource = "Browse for " + currentTab.getDescription();
					// check if there is existing gid in the list
					List<GermplasmListData> germplasmList =
							this.germplasmListManager.getGermplasmListDataByListIdAndGID(Integer.valueOf(listId), gid);

					if (germplasmList.isEmpty()) {
						++entryid;

						// save germplasm's preferred name as designation
						designation = this.getDesignationFromTab(currentTab);

						GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, entryid, entryCode, seedSource,
								designation, groupName, status, localRecordId);

						this.germplasmListManager.addGermplasmListData(germplasmListData);

					}
					gidListString = gidListString + ", " + Integer.toString(gid);
				}

			}

			this.contextUtil.logProgramActivity("Saved a germplasm list.", "Saved list - " + listName + " with type - " + type);

		} catch (MiddlewareQueryException e) {
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_ADDING_GERMPLASM_LIST);
		}
	}

	private String getPreferredId(TabSheet tabSheet, int gid) throws MiddlewareQueryException {
		List<Integer> gids = new ArrayList<Integer>();

		for (int i = 0; i < tabSheet.getComponentCount(); i++) {
			Tab currentTab = tabSheet.getTab(i);
			int g = Integer.valueOf(currentTab.getCaption().toString());
			gids.add(g);
		}

		Map<Integer, String> results = this.germplasmDataManager.getPrefferedIdsByGIDs(gids);

		if (results.get(gid) != null) {
			return results.get(gid);
		} else {
			return "-";
		}
	}

	private String getDesignationFromTab(Tab currentTab) {
		// save germplasm's preferred name as designation
		VerticalLayout tabLayout = (VerticalLayout) currentTab.getComponent();
		for (final Iterator<Component> iterator = tabLayout.getComponentIterator(); iterator.hasNext();) {
			Component component = iterator.next();
			// retrieve preferred name from the germplasm details tab
			if (component instanceof GermplasmDetail) {
				GermplasmDetail germplasmDetail = (GermplasmDetail) component;
				return germplasmDetail.getGermplasmDetailModel().getGermplasmPreferredName();
			}
		}
		return "-";
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// do nothing
	}

}
