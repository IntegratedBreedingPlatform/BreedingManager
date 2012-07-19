/*
 * Copyright 2009 IT Mill Ltd. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.generationcp.browser.germplasm;

import java.util.ArrayList;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.browser.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
@Configurable
public class GermplasmBrowserMain extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private final static Logger LOG = LoggerFactory.getLogger(GermplasmBrowserMain.class);

	private final static String NAMES = "Names";
	private final static String GID = "gid"; 

	public final static String SEARCH_BUTTON_ID = "GermplasmBrowserMain Search Button";
	public final static String SAVE_GERMPLASMLIST_ID="Save GermplasmList Button";
	public final static String CLOSE_ALL_GERMPLASMDETAIL_TAB_ID="Close all GermplasmDetail Tab Button";

	private VerticalLayout mainLayout;
	private HorizontalLayout searchFormLayout;
	private Table resultTable;
	private IndexedContainer dataSourceResult;
	private TabSheet tabSheet = new TabSheet();
	private GermplasmIndexContainer dataResultIndexContainer;

	private GermplasmQueries qQuery;
	private String searchChoice;
	private String searchValue;
	private String instanceChoice;
	private Database instance;

	private Button btnSearch;
	private Button btnSaveGermplasmList;
	private Button btnCloseAllGermplamDetailTab;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private GermplasmSearchFormComponent searchOption;

	private HorizontalLayout hLayoutForButtons;

	private Window saveGermplasmListDialog;





	public GermplasmBrowserMain() {

		try {

			qQuery = new GermplasmQueries();

			dataResultIndexContainer = new GermplasmIndexContainer(qQuery);

		} catch (Exception e1) {
			// Log into log file
			LOG.error(e1.toString() + "\n" + e1.getStackTrace());
			e1.printStackTrace();
		}

	}

	private void displayGermplasmDetailTab(int gid) throws QueryException {

		hLayoutForButtons.setVisible(true);

		VerticalLayout detailLayout = new VerticalLayout();
		detailLayout.setSpacing(true);
		// int screenWidth = 1028;

		if (!Util.isTabExist(tabSheet, String.valueOf(gid))) {
			detailLayout.addComponent(new GermplasmDetail(gid, qQuery, dataResultIndexContainer, mainLayout, tabSheet));
			Tab tab = tabSheet.addTab(detailLayout, String.valueOf(gid), null);
			tab.setClosable(true);
			tabSheet.setSelectedTab(detailLayout);
			mainLayout.addComponent(tabSheet);
		} else {
			Tab tab = Util.getTabAlreadyExist(tabSheet, String.valueOf(gid));
			tabSheet.setSelectedTab(tab.getComponent());
		}
	}

	// private int getScreenWidth(){
	// WebApplicationContext context =
	// ((WebApplicationContext)this.getContext());
	// WebBrowser wb = context.getBrowser();
	// return wb.getScreenWidth();
	// }

	public void searchButtonClickAction() {

		searchChoice = searchOption.getChoice();
		searchValue = searchOption.getSearchValue();
		instanceChoice = searchOption.getDatabaseInstance();

		if ("Central".equals(instanceChoice)) {
			instance = Database.CENTRAL;
		} else {
			instance = Database.LOCAL;
		}

		try {
			boolean withNoError = true;
			if (searchValue.length() > 0) {
				// Window window;
				if ("GID".equals(searchChoice)) {
					try {
						int gid = Integer.parseInt(searchValue);
						displayGermplasmDetailTab(gid);

					} catch (QueryException e) {
						// Log into log file
						LOG.error(e.toString() + "\n" + e.getStackTrace());
						withNoError = false;
						e.printStackTrace();
					} catch (NumberFormatException nFE) {
						// Log into log file
						LOG.error(nFE.toString() + "\n" + nFE.getStackTrace());
						withNoError = false;
						// mainLayout.showNotification("Invalid Input","Must be numeric");
					}
				}
				if (withNoError) {
					dataSourceResult = dataResultIndexContainer.getGermplasmResultContainer(searchChoice, searchValue, instance);
					resultTable.setCaption("Germplasm Search Result: " + dataSourceResult.size());
					resultTable.setContainerDataSource(dataSourceResult);
					mainLayout.requestRepaintAll();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Called by GermplasmItemClickListener
	public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) {
		sourceTable.select(itemId);
		int gid = Integer.valueOf(item.getItemProperty(GID).toString());
		try {
			displayGermplasmDetailTab(gid);
		} catch (QueryException e) {
			// Log into log file
			LOG.error(e.toString() + "\n" + e.getStackTrace());
			e.printStackTrace();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		setSpacing(true);

		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setMargin(true);

		searchFormLayout = new HorizontalLayout();


		searchOption = new GermplasmSearchFormComponent();
		searchFormLayout.addComponent(searchOption);

		btnSearch = new Button();
		btnSearch.setData(SEARCH_BUTTON_ID);
		btnSearch.addStyleName("addTopSpace");

		btnSearch.addListener(new GermplasmButtonClickListener(this));
		searchFormLayout.addComponent(btnSearch);

		mainLayout.addComponent(searchFormLayout);


		try {
			// Set the initial search result in Central
			dataSourceResult = dataResultIndexContainer.getGermplasmResultContainer(NAMES, "", Database.CENTRAL);
		} catch (QueryException e1) {
			// Log into log file
			LOG.error(e1.toString() + "\n" + e1.getStackTrace());
			e1.printStackTrace();
		}
		resultTable = new SearchResultTable(dataSourceResult).getResultTable();

		mainLayout.addComponent(resultTable);

		resultTable.addListener(new GermplasmItemClickListener(this));

		hLayoutForButtons= new HorizontalLayout();
		hLayoutForButtons.setSpacing(true);

		// save germplasmlist button
		btnSaveGermplasmList = new Button();
		btnSaveGermplasmList.setData(SAVE_GERMPLASMLIST_ID);
		btnSaveGermplasmList.addListener(new GermplasmButtonClickListener(this));
		hLayoutForButtons.addComponent(btnSaveGermplasmList);


		// close all GermplasmDetail tab button
		btnCloseAllGermplamDetailTab = new Button();
		btnCloseAllGermplamDetailTab.setData(CLOSE_ALL_GERMPLASMDETAIL_TAB_ID);
		btnCloseAllGermplamDetailTab.addListener(new GermplasmButtonClickListener(this));
		hLayoutForButtons.addComponent(btnCloseAllGermplamDetailTab);

		hLayoutForButtons.setVisible(false);
		mainLayout.addComponent(hLayoutForButtons);

		addComponent(mainLayout);

	}

	@Override
	public void attach() {

		super.attach();

		updateLabels();
	}


	@Override
	public void updateLabels() {

		messageSource.setCaption(btnSearch, Message.search_label);
		messageSource.setCaption(btnSaveGermplasmList,  Message.save_germplasmlist_button_label);
		messageSource.setCaption(btnCloseAllGermplamDetailTab,  Message.close_all_germlasmdetail_tab_label);

	}



	@SuppressWarnings("deprecation")
	private void openDialogSaveList() {
		
		saveGermplasmListDialog=new Window(messageSource.getMessage(Message.save_germplasm_list_window_label));
		saveGermplasmListDialog.setModal(true);
		saveGermplasmListDialog.setWidth(500);
		saveGermplasmListDialog.setHeight(300);
		saveGermplasmListDialog.addComponent(new SaveGermplasmListDialog(this.getApplication().getMainWindow(),saveGermplasmListDialog,tabSheet));
		this.getApplication().getMainWindow().addWindow(saveGermplasmListDialog);
		
	}

	public void closeAllGermplasmDetailTabButtonClickAction() {

		Util.closeAllTab(tabSheet);
		hLayoutForButtons.setVisible(false);
		mainLayout.removeComponent(tabSheet);
		mainLayout.requestRepaintAll();

	}

	public void saveGermplasmListButtonClickAction() {
		openDialogSaveList();
	}
}
