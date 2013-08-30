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
import org.generationcp.browser.util.SelectedTabCloseHandler;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Item;
import com.vaadin.event.ShortcutAction.KeyCode;
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

    private final static String VERSION = "1.2.0";

    private final static String NAMES = "Names";
    private final static String GID = "gid";

    public static final String SEARCH_OPTION_GID = "GID";
    public static final String SEARCH_OPTION_NAME = "Names";

    public final static String SEARCH_BUTTON_ID = "GermplasmBrowserMain Search Button";
    public final static String SAVE_GERMPLASMLIST_ID = "Save GermplasmList Button";
    public final static String CLOSE_ALL_GERMPLASMDETAIL_TAB_ID = "Close all GermplasmDetail Tab Button";

    public static final String INSTANCE_OPTION_CENTRAL = "Central";
    public static final String INSTANCE_OPTION_LOCAL = "Local";

    private VerticalLayout mainLayout;
    private HorizontalLayout searchFormLayout;
    private Table resultTable;
    private TabSheet tabSheet = new TabSheet();
    private GermplasmIndexContainer dataResultIndexContainer;

    private GermplasmQueries qQuery;
    private String searchChoice;
    private String searchValue;

    private Button btnSearch;
    private Button btnSaveGermplasmList;
    private Button btnCloseAllGermplamDetailTab;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    @Autowired
    private GermplasmDataManager germplasmDataManager;

    private GermplasmSearchFormComponent searchOption;

    private HorizontalLayout hLayoutForButtons;

    private Window saveGermplasmListDialog;

    private boolean forGermplasmWindow;         //this is true if this component is created for the germplasm browser only window

    public GermplasmBrowserMain(boolean forGermplasmWindow) throws InternationalizableException {
        this.forGermplasmWindow = forGermplasmWindow;
        qQuery = new GermplasmQueries();
        dataResultIndexContainer = new GermplasmIndexContainer(qQuery);
    }

    private void displayGermplasmDetailTab(final int gid,String searchString) throws InternationalizableException {

        hLayoutForButtons.setVisible(true);

        VerticalLayout detailLayout = new VerticalLayout();
        detailLayout.setSpacing(true);

        if (!Util.isTabExist(tabSheet, String.valueOf(gid))) {
            GermplasmDetail germplasmDetail = new GermplasmDetail(gid, qQuery, dataResultIndexContainer, mainLayout, tabSheet, false);
            if (germplasmDetail.getGermplasmDetailModel().getGid() != 0){  // Germplasm found
                detailLayout.addComponent(germplasmDetail);
                Tab tab = tabSheet.addTab(detailLayout, String.valueOf(gid), null);
                tab.setClosable(true);
                tab.setDescription(searchString);
                tabSheet.setSelectedTab(detailLayout);
                tabSheet.setCloseHandler(new SelectedTabCloseHandler());
                mainLayout.addComponent(tabSheet);
            } 
            // If germplasm is not found, no details tab is displayed
        } else {
            Tab tab = Util.getTabAlreadyExist(tabSheet, String.valueOf(gid));
            tabSheet.setSelectedTab(tab.getComponent());
        }
    }

    public void searchButtonClickAction() throws InternationalizableException {

        searchChoice = searchOption.getChoice();
        searchValue = searchOption.getSearchValue();

        boolean withNoError = true;
        if (searchValue.length() > 0) {
            if ("GID".equals(searchChoice)) {
                try {
                    int gid = Integer.parseInt(searchValue);
                    displayGermplasmDetailTab(gid,searchValue);
                } catch (NumberFormatException e) {
                    //LOG.error(e.toString() + "\n" + e.getStackTrace());
                    //e.printStackTrace();
                    withNoError = false;
                    if (getWindow() != null) {
                        MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                                messageSource.getMessage(Message.ERROR_INVALID_INPUT_MUST_BE_NUMERIC));
                    }
                }
            } 
            if (withNoError) {
                LazyQueryContainer dataSourceResultLazy =  dataResultIndexContainer.getGermplasmResultLazyContainer(germplasmDataManager, searchChoice, searchValue);                                        
                resultTable.setCaption("Germplasm Search Result: " + dataSourceResultLazy.size());
                resultTable.setContainerDataSource(dataSourceResultLazy);
                mainLayout.requestRepaintAll();
            }

        } else {
            MessageNotifier.showError(getWindow(), "Error", "Please input search string.");
        }
    }

    // Called by GermplasmItemClickListener
    public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        sourceTable.select(itemId);
        int gid = Integer.valueOf(item.getItemProperty(GID).toString());
        displayGermplasmDetailTab(gid,searchValue);
    }

    @Override
    public void afterPropertiesSet() throws Exception { 

        setSpacing(true);

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(false, true, true, true);

        String title =  "<h1>Germplasm Browser</h1> <h2>: " + VERSION + "</h2>";
        Label applicationTitle = new Label();
        applicationTitle.setStyleName("gcp-window-title");
        applicationTitle.setContentMode(Label.CONTENT_XHTML);
        applicationTitle.setValue(title);
        mainLayout.addComponent(applicationTitle);

        searchFormLayout = new HorizontalLayout();

        searchOption = new GermplasmSearchFormComponent();
        searchFormLayout.addComponent(searchOption);

        btnSearch = new Button();
        btnSearch.setData(SEARCH_BUTTON_ID);
        btnSearch.addStyleName("addTopSpace");
        btnSearch.setClickShortcut(KeyCode.ENTER);

        btnSearch.addListener(new GermplasmButtonClickListener(this));
        searchFormLayout.addComponent(btnSearch);

        mainLayout.addComponent(searchFormLayout);

        resultTable = new GermplasmSearchResultComponent(germplasmDataManager, GID, "0");
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
        messageSource.setCaption(btnSearch, Message.SEARCH_LABEL);
        messageSource.setCaption(btnSaveGermplasmList, Message.SAVE_GERMPLASM_LIST_BUTTON_LABEL);
        messageSource.setCaption(btnCloseAllGermplamDetailTab, Message.CLOSE_ALL_GERMPLASM_DETAIL_TAB_LABEL);
    }

    @SuppressWarnings("deprecation")
    private void openDialogSaveList() {
        saveGermplasmListDialog = new Window(messageSource.getMessage(Message.SAVE_GERMPLASM_LIST_WINDOW_LABEL));
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
        if (tabSheet.getComponentCount() > 0){
            openDialogSaveList();
        } else if (getWindow() != null) {
            MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_GERMPLASM_MUST_BE_SELECTED),"");
        }
    }
}
