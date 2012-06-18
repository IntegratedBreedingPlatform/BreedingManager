/*
 * Copyright 2009 IT Mill Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.generationcp.browser.germplasm.application;

import org.generationcp.browser.germplasm.datasource.helper.DatasourceConfig;
import org.generationcp.browser.germplasm.datasource.helper.GermplasmQueries;
import org.generationcp.browser.germplasm.table.indexcontainer.GermplasmIndexContainer;
import org.generationcp.browser.util.Util;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GermplasmDataManager;

import com.vaadin.Application;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MainApplication extends Application 
{
    private final static Logger log = LoggerFactory.getLogger(MainApplication.class);

    private Window window;
	private VerticalLayout mainLayout;
	private HorizontalLayout searchFormLayout;
	private Table resultTable;
	private IndexedContainer dataSourceResult;
	private TabSheet tabSheet = new TabSheet();
	private ManagerFactory factory;
	private GermplasmDataManager managerGermplasm;
	private GermplasmIndexContainer dataResultIndexContainer;

	private GermplasmQueries qQuery;
	private String searchChoice;
	private String searchValue;
	private String instanceChoice;
	private Database instance;


	@Override
	public void init()
	{	
		try {
			initDataSource();
		} catch (Exception e1) {
			// Log into log file
			log.error(e1.toString() + "\n" + e1.getStackTrace());											
			e1.printStackTrace();
		}
		window = new Window("Germplasm Browser");
		setMainWindow(window);
		setTheme("gcp-default");

		mainLayout   = new VerticalLayout();
		mainLayout.setSpacing(true);

		searchFormLayout= new HorizontalLayout();

		final SearchForm searchOption= new SearchForm();
		final SearchGermplasmByPhenotypic searchPhenotypic= new SearchGermplasmByPhenotypic();
		searchFormLayout.addComponent(searchOption);

		Button btnSearch = new Button("Search");
		btnSearch.addStyleName("addTopSpace");
		btnSearch.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {

				searchChoice=searchOption.getChoice();
				searchValue=searchOption.getSearchValue();
				instanceChoice=searchOption.getDatabaseInstance();

				if(instanceChoice.equals("Central")){
					instance=Database.CENTRAL;
				}else{
					instance=Database.LOCAL;
				}

				try{
					boolean withNoError=true;
					if(searchValue.length() > 0){
						if(searchChoice.equals("GID")){
							try {
								int gid=Integer.parseInt(searchValue);
								displayGermplasmDetailTab(gid);
							} catch (QueryException e) {
								// Log into log file
								log.error(e.toString() + "\n" + e.getStackTrace());								
								withNoError=false;
								e.printStackTrace();
							}catch(NumberFormatException nFE) {
								// Log into log file
								log.error(nFE.toString() + "\n" + nFE.getStackTrace());
								withNoError=false;
								window.showNotification("Invalid Input",
										"Must be numeric");
							}
						}
						if(withNoError){
							dataSourceResult = dataResultIndexContainer.getGermplasResultContainer(searchChoice,searchValue,instance);
							resultTable.setCaption("Germplasm Search Result: "+dataSourceResult.size());
							resultTable.setContainerDataSource(dataSourceResult);
							window.requestRepaintAll();
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});

		searchFormLayout.addComponent(btnSearch);
		mainLayout.addComponent(searchFormLayout);

		try {
			// Set the initial search result in Central
			dataSourceResult = dataResultIndexContainer.getGermplasResultContainer("Names","",Database.CENTRAL);
		} catch (QueryException e1) {
			// Log into log file
			log.error(e1.toString() + "\n" + e1.getStackTrace());								
			e1.printStackTrace();
		}
		resultTable= new SearchResultTable(dataSourceResult).getResultTable();

		mainLayout.addComponent(resultTable);

		resultTable.addListener(new ItemClickListener() {

			public void itemClick(ItemClickEvent event) {
				// TODO Auto-generated method stub

				if (event.isDoubleClick())    {
					((Table) event.getSource()).select(event.getItemId());
					int gid=Integer.valueOf(event.getItem().getItemProperty("gid").toString());
					try {
						displayGermplasmDetailTab(gid);
					} catch (QueryException e) {
						// Log into log file
						log.error(e.toString() + "\n" + e.getStackTrace());								
						e.printStackTrace();
					}
				}

			}
		});

		window.addComponent(mainLayout);

	}

	private void displayGermplasmDetailTab(int gid) throws QueryException{
		
		int screenWidth=getScreenWidth();
		
		VerticalLayout detailLayout = new VerticalLayout();
		detailLayout.setSpacing(true);

		if(!Util.isTabExist(tabSheet,String.valueOf(gid))){
			detailLayout.addComponent(new GermplasmDetail(gid,qQuery,dataResultIndexContainer,mainLayout,tabSheet,screenWidth));
			Tab tab =tabSheet.addTab(detailLayout, String.valueOf(gid),null);
			tab.setClosable(true);
			tabSheet.setSelectedTab(detailLayout);
			mainLayout.addComponent(tabSheet);
		}else{
			Tab tab=Util.getTabAlreadyExist(tabSheet, String.valueOf(gid));
			tabSheet.setSelectedTab(tab.getComponent());
		}
	}



	private void initDataSource() throws Exception{
		factory=new DatasourceConfig().getManagerFactory();
		managerGermplasm = factory.getGermplasmDataManager();
		qQuery= new GermplasmQueries(factory,managerGermplasm);
		dataResultIndexContainer= new GermplasmIndexContainer(qQuery);

	}
	
	private int getScreenWidth(){
		WebApplicationContext context = ((WebApplicationContext)this.getContext());
		WebBrowser wb = context.getBrowser();
		return wb.getScreenWidth();
	}

}
