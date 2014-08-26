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

package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.SelectListButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.listeners.SelectListItemClickListener;
import org.generationcp.breeding.manager.crossingmanager.listeners.SelectListTreeExpandListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@Configurable
public class SelectGermplasmListTreeComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -8933173351951948514L;
    
    private final static Logger LOG = LoggerFactory.getLogger(SelectGermplasmListTreeComponent.class);
    private final static int BATCH_SIZE = 50;
    
    public final static String REFRESH_BUTTON_ID = "SelectGermplasmListTreeComponent Refresh Button";

    private Tree germplasmListTree;
    private Button refreshButton;
    
    private SelectGermplasmListInfoComponent listInfoComponent;
    private Database database;
    
    @Autowired
    private GermplasmListManager germplasmListManager;  
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public SelectGermplasmListTreeComponent(Database database, SelectGermplasmListInfoComponent listInfoComponent) {
        this.database = database;
        this.listInfoComponent = listInfoComponent;
    }
    
    @Override
    public void afterPropertiesSet() {
        assemble();        
    }
    
    protected void assemble() {
        initializeComponents();
        initializeValues();
        initializeLayout();
        initializeActions();
    }
    
    protected void initializeComponents() {
        germplasmListTree = createGermplasmListTree(database);

        refreshButton = new Button(); // "Refresh"
        refreshButton.setData(REFRESH_BUTTON_ID);
        
        // add tooltip
        germplasmListTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
            private static final long serialVersionUID = -2669417630841097077L;
            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.GERMPLASM_LIST_DETAILS_LABEL); // "Click to view germplasm list details"
            }
        });
    }
    
    protected void initializeValues() {
        
    }
    
    protected void initializeLayout() {
        setSpacing(true);
        setMargin(true);
        
        if (database == Database.LOCAL) {
            addComponent(refreshButton);
        }
        addComponent(germplasmListTree);
    }
    
    protected void initializeActions() {
        if (database == Database.LOCAL) {
            refreshButton.addListener(new SelectListButtonClickListener(this));
        }
    }

    private Tree createGermplasmListTree(Database database) {
        List<GermplasmList> germplasmListParent = new ArrayList<GermplasmList>();

        try {
            germplasmListParent = this.germplasmListManager.getAllTopLevelListsBatched(BATCH_SIZE, database);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            if (getWindow() != null){
                MessageNotifier.showWarning(getWindow(), 
                        messageSource.getMessage(Message.ERROR_DATABASE),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_FOLDERS));
            }
            germplasmListParent = new ArrayList<GermplasmList>();
        }

        Tree germplasmListTree = new Tree();

        for (GermplasmList parentList : germplasmListParent) {
            germplasmListTree.addItem(parentList.getId());
            germplasmListTree.setItemCaption(parentList.getId(), parentList.getName());
            germplasmListTree.setChildrenAllowed(parentList.getId(), hasChildList(parentList.getId()));
        }

        germplasmListTree.addListener(new SelectListTreeExpandListener(this));
        germplasmListTree.addListener(new SelectListItemClickListener(this));

        return germplasmListTree;
    }
    
    // Called by SelectListButtonClickListener
    public void createTree() {
        this.removeComponent(germplasmListTree);
        germplasmListTree.removeAllItems();
        germplasmListTree = createGermplasmListTree(Database.LOCAL);
        this.addComponent(germplasmListTree);
    }

    // called by SelectListItemClickListener
    public void displayGermplasmListDetails(int germplasmListId) throws InternationalizableException{
        try {
            displayGermplasmListInfo(germplasmListId);
        } catch (NumberFormatException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                    messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
        } catch (MiddlewareQueryException e){
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
        }
        
    }

    public void displayGermplasmListInfo(int germplasmListId) throws MiddlewareQueryException {
        GermplasmList germplasmList;
        
        if (!hasChildList(germplasmListId) && !isEmptyFolder(germplasmListId)) {
            germplasmList = this.germplasmListManager.getGermplasmListById(germplasmListId);
        } else {
            germplasmList = null;
        }
        
        listInfoComponent.displayListInfo(germplasmList);
    }
    
    // called by SelectListTreeExpandListener
    public void addGermplasmListNode(int parentGermplasmListId) throws InternationalizableException{
        List<GermplasmList> germplasmListChildren = new ArrayList<GermplasmList>();
        
        try {
            germplasmListChildren = this.germplasmListManager.getGermplasmListByParentFolderIdBatched(parentGermplasmListId, BATCH_SIZE);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_DATABASE), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
            germplasmListChildren = new ArrayList<GermplasmList>();
        }
        
        for (GermplasmList listChild : germplasmListChildren) {
            germplasmListTree.addItem(listChild.getId());
            germplasmListTree.setItemCaption(listChild.getId(), listChild.getName());
            germplasmListTree.setParent(listChild.getId(), parentGermplasmListId);
            // allow children if list has sub-lists
            germplasmListTree.setChildrenAllowed(listChild.getId(), hasChildList(listChild.getId()));
        }
    }   
    
    private boolean hasChildList(int listId) {
        List<GermplasmList> listChildren = new ArrayList<GermplasmList>();
        try {
            listChildren = this.germplasmListManager.getGermplasmListByParentFolderId(listId, 0, 1);
        } catch (MiddlewareQueryException e) {
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            MessageNotifier.showWarning(getWindow(), 
                    messageSource.getMessage(Message.ERROR_DATABASE), 
                    messageSource.getMessage(Message.ERROR_IN_GETTING_GERMPLASM_LISTS_BY_PARENT_FOLDER_ID));
            listChildren = new ArrayList<GermplasmList>();
        }
        return !listChildren.isEmpty();
    }

    private boolean isEmptyFolder(int listId) throws MiddlewareQueryException{
        boolean isFolder = germplasmListManager.getGermplasmListById(listId).getType().equalsIgnoreCase("FOLDER");
        return isFolder && !hasChildList(listId);
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(refreshButton, Message.REFRESH_LABEL);
    }

}
