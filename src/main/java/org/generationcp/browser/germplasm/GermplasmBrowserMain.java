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

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.browser.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
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
@Configurable
public class GermplasmBrowserMain extends VerticalLayout implements InitializingBean, InternationalizableComponent{

    private static final long serialVersionUID = 1L;

    private final static Logger LOG = LoggerFactory.getLogger(GermplasmBrowserMain.class);

    private final static String VERSION = "1.1.0";
    
    private final static String NAMES = "Names";
    private final static String GID = "gid";

    public final static String SEARCH_BUTTON_ID = "GermplasmBrowserMain Search Button";
    public final static String SAVE_GERMPLASMLIST_ID = "Save GermplasmList Button";
    public final static String CLOSE_ALL_GERMPLASMDETAIL_TAB_ID = "Close all GermplasmDetail Tab Button";

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
    
    private boolean forGermplasmWindow;         //this is true if this component is created for the germplasm browser only window

    public GermplasmBrowserMain(boolean forGermplasmWindow) throws InternationalizableException {
        this.forGermplasmWindow = forGermplasmWindow;
        qQuery = new GermplasmQueries();
        dataResultIndexContainer = new GermplasmIndexContainer(qQuery);
    }

    private void displayGermplasmDetailTab(int gid) throws InternationalizableException {

        hLayoutForButtons.setVisible(true);

        VerticalLayout detailLayout = new VerticalLayout();
        detailLayout.setSpacing(true);
        // int screenWidth = 1028;

        if (!Util.isTabExist(tabSheet, String.valueOf(gid))) {
            GermplasmDetail germplasmDetail = new GermplasmDetail(gid, qQuery, dataResultIndexContainer, mainLayout, tabSheet);
            if (germplasmDetail.getGermplasmDetailModel().getGid() != 0){  // Germplasm found
                detailLayout.addComponent(germplasmDetail);
                Tab tab = tabSheet.addTab(detailLayout, String.valueOf(gid), null);
                tab.setClosable(true);
                tabSheet.setSelectedTab(detailLayout);
                mainLayout.addComponent(tabSheet);
            } 
            // If germplasm is not found, no details tab is displayed
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

    public void searchButtonClickAction() throws InternationalizableException {

        searchChoice = searchOption.getChoice();
        searchValue = searchOption.getSearchValue();
        instanceChoice = searchOption.getDatabaseInstance();

        if ("Central".equals(instanceChoice)) {
            instance = Database.CENTRAL;
        } else {
            instance = Database.LOCAL;
        }

        boolean withNoError = true;
        if (searchValue.length() > 0) {
            // Window window;
            if ("GID".equals(searchChoice)) {
                try {
                    int gid = Integer.parseInt(searchValue);
                    displayGermplasmDetailTab(gid);
                } catch (NumberFormatException e) {
                    LOG.error(e.toString() + "\n" + e.getStackTrace());
                    e.printStackTrace();
                    withNoError = false;
                    // mainLayout.showNotification("Invalid Input","Must be numeric");
                    if (getWindow() != null) {
                        MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.error_invalid_format),
                                messageSource.getMessage(Message.error_invalid_input_must_be_numeric));
                    }
                }
            }
            if (withNoError) {
                dataSourceResult = dataResultIndexContainer.getGermplasmResultContainer(searchChoice, searchValue, instance);
                resultTable.setCaption("Germplasm Search Result: " + dataSourceResult.size());
                resultTable.setContainerDataSource(dataSourceResult);
                mainLayout.requestRepaintAll();
            }
               
        }
    }

    // Called by GermplasmItemClickListener
    public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        sourceTable.select(itemId);
        int gid = Integer.valueOf(item.getItemProperty(GID).toString());
        displayGermplasmDetailTab(gid);
    }

    @Override
    public void afterPropertiesSet() throws Exception { 

        setSpacing(true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        Label applicationTitle = new Label("<h1>Germplasm Browser " + VERSION + "</h1>");
        applicationTitle.setContentMode(Label.CONTENT_XHTML);
        mainLayout.addComponent(applicationTitle);
        
        searchFormLayout = new HorizontalLayout();

        searchOption = new GermplasmSearchFormComponent();
        searchFormLayout.addComponent(searchOption);

        btnSearch = new Button();
        btnSearch.setData(SEARCH_BUTTON_ID);
        btnSearch.addStyleName("addTopSpace");

        btnSearch.addListener(new GermplasmButtonClickListener(this));
        searchFormLayout.addComponent(btnSearch);

        mainLayout.addComponent(searchFormLayout);

        // Set the initial search result in Central
        dataSourceResult = dataResultIndexContainer.getGermplasmResultContainer(NAMES, "", Database.CENTRAL);
        resultTable = new SearchResultTable(dataSourceResult).getResultTable();

        mainLayout.addComponent(resultTable);

        resultTable.addListener(new GermplasmItemClickListener(this));

        hLayoutForButtons = new HorizontalLayout();
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
        messageSource.setCaption(btnSaveGermplasmList, Message.save_germplasmlist_button_label);
        messageSource.setCaption(btnCloseAllGermplamDetailTab, Message.close_all_germlasmdetail_tab_label);
    }

    @SuppressWarnings("deprecation")
    private void openDialogSaveList() {
        saveGermplasmListDialog = new Window(messageSource.getMessage(Message.save_germplasm_list_window_label));
        saveGermplasmListDialog.setModal(true);
        saveGermplasmListDialog.setWidth(700);
        saveGermplasmListDialog.setHeight(350);
        if(this.forGermplasmWindow) {
            saveGermplasmListDialog.addComponent(new SaveGermplasmListDialog(this.getApplication().getWindow(GermplasmStudyBrowserApplication.GERMPLASM_WINDOW_NAME)
                    , saveGermplasmListDialog, tabSheet));
            this.getApplication().getWindow(GermplasmStudyBrowserApplication.GERMPLASM_WINDOW_NAME).addWindow(saveGermplasmListDialog);
        } else {
            saveGermplasmListDialog.addComponent(new SaveGermplasmListDialog(this.getApplication().getMainWindow(), saveGermplasmListDialog,
                    tabSheet));
            this.getApplication().getMainWindow().addWindow(saveGermplasmListDialog);
        }
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
