/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.browser.germplasm;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.containers.ListsForGermplasmQuery;
import org.generationcp.browser.germplasm.containers.ListsForGermplasmQueryFactory;
import org.generationcp.browser.germplasmlist.listeners.GermplasmListItemClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class GermplasmListComponent extends Table implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = LoggerFactory.getLogger(GermplasmListComponent.class);
    
    private GermplasmListManager dataManager;
    private Integer gid;
    private boolean fromUrl;				//this is true if this component is created by accessing the Germplasm Details page directly from the URL
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;

    public GermplasmListComponent(GermplasmListManager dataManager, Integer gid, boolean fromUrl) {
    	this.dataManager = dataManager;
    	this.gid = gid;
    	this.fromUrl = fromUrl;
    }
    
    // Called by GermplasmListItemClickListener
    public void listItemClickAction(ItemClickEvent event, Integer listId) {
    	Window mainWindow = event.getComponent().getWindow();
        
        Tool tool = null;
        try {
            tool = workbenchDataManager.getToolWithName(ToolName.germplasm_list_browser.toString());
        } catch (MiddlewareQueryException qe) {
            LOG.error("QueryException", qe);
        }
        
        ExternalResource listBrowserLink = null;
        if (tool == null) {
            listBrowserLink = new ExternalResource("http://localhost:18080/GermplasmStudyBrowser/main/germplasmlist-" + listId);
        } else {
            listBrowserLink = new ExternalResource(tool.getPath().replace("germplasmlist/", "germplasmlist-") + listId);
        }
        
        Window germplasmListWindow = new Window("Germplasm List Information - " + listId);
        
        VerticalLayout layoutForList = new VerticalLayout();
        layoutForList.setMargin(false);
        layoutForList.setWidth("640px");
        layoutForList.setHeight("560px");
        
        Embedded listInfoPage = new Embedded("", listBrowserLink);
        listInfoPage.setType(Embedded.TYPE_BROWSER);
        listInfoPage.setSizeFull();
        layoutForList.addComponent(listInfoPage);
        
        germplasmListWindow.setContent(layoutForList);
        germplasmListWindow.setWidth("645px");
        germplasmListWindow.setHeight("600px");
        germplasmListWindow.center();
        germplasmListWindow.setResizable(false);
        germplasmListWindow.setModal(true);
        
        mainWindow.addWindow(germplasmListWindow);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ListsForGermplasmQueryFactory factory = new ListsForGermplasmQueryFactory(this.dataManager, this.gid);
        LazyQueryContainer container = new LazyQueryContainer(factory, false, 50);
        
        // add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
        container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_ID, String.class, null);
        container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_NAME, String.class, null);
        container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_DATE, String.class, null);
        container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_DESCRIPTION, String.class, null);
        container.getQueryView().getItem(0); // initialize the first batch of data to be displayed
        setContainerDataSource(container);
        
        messageSource.setColumnHeader(this, (String) ListsForGermplasmQuery.GERMPLASMLIST_ID, Message.ID_HEADER);
        messageSource.setColumnHeader(this, (String) ListsForGermplasmQuery.GERMPLASMLIST_NAME, Message.NAME_HEADER);
        messageSource.setColumnHeader(this, (String) ListsForGermplasmQuery.GERMPLASMLIST_DATE, Message.DATE_HEADER);
        messageSource.setColumnHeader(this, (String) ListsForGermplasmQuery.GERMPLASMLIST_DESCRIPTION, Message.DESCRIPTION_HEADER);
        setVisibleColumns(new String[] { (String) ListsForGermplasmQuery.GERMPLASMLIST_NAME, (String) ListsForGermplasmQuery.GERMPLASMLIST_DATE, (String) ListsForGermplasmQuery.GERMPLASMLIST_DESCRIPTION});
        
        setSelectable(true);
        setMultiSelect(false);
        setSizeFull();
        setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);
        setPageLength(15);
        
        if (!fromUrl) {
        	addListener(new GermplasmListItemClickListener(this));
        }
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        /*messageSource.setColumnHeader(this, NAME, Message.name_header);
        messageSource.setColumnHeader(this, DATE, Message.date_header);
        messageSource.setColumnHeader(this, DESCRIPTION, Message.description_header);*/
    }

}
