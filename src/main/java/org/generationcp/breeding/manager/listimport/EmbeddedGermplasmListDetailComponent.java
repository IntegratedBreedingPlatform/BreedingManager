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
package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class EmbeddedGermplasmListDetailComponent extends VerticalLayout
    implements InitializingBean, InternationalizableComponent{
    
    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedGermplasmListDetailComponent.class);
    private static final long serialVersionUID = -8889276342164300525L;
    
    public static final String NEW_IMPORT_BUTTON_ID = "Make New Import Button ID";
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    private Button makeImportButton;
    
    private GermplasmImportMain germplasmImportMain;
    private Integer listId;
    
    public EmbeddedGermplasmListDetailComponent(GermplasmImportMain germplasmImportMain, Integer listId) {
        this.germplasmImportMain = germplasmImportMain;
        this.listId = listId;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setMargin(false);
        setSpacing(true);
        setWidth("800px");
        
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
        
        VerticalLayout layoutForList = new VerticalLayout();
        layoutForList.setMargin(false);
        layoutForList.setSpacing(false);
        
        Embedded listInfoPage = new Embedded("", listBrowserLink);
        listInfoPage.setType(Embedded.TYPE_BROWSER);
        listInfoPage.setSizeFull();
        layoutForList.setHeight("550px");
        layoutForList.addComponent(listInfoPage);
  
        GermplasmImportButtonClickListener listener = new GermplasmImportButtonClickListener(this);
        /*
        exportButton = new Button();
        exportButton.setData(EXPORT_BUTTON_ID);
        exportButton.addListener(listener);
        */
        makeImportButton = new Button();
        makeImportButton.setData(NEW_IMPORT_BUTTON_ID);
        makeImportButton.addListener(listener);
        
        HorizontalLayout buttonArea = new HorizontalLayout();
        buttonArea.setMargin(true);
        buttonArea.setSpacing(true);
        buttonArea.addComponent(makeImportButton);
        
        addComponent(layoutForList);
        addComponent(buttonArea);
        setComponentAlignment(buttonArea, Alignment.BOTTOM_RIGHT);
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(makeImportButton, Message.START_NEW_IMPORT);
    }
    
    @SuppressWarnings("serial")
    public void makeNewImportButtonClickAction(){
        ConfirmDialog.show(this.getWindow(), messageSource.getMessage(Message.MAKE_NEW_IMPORT),
            messageSource.getMessage(Message.CONFIRM_REDIRECT_TO_IMPORT_WIZARD),
            messageSource.getMessage(Message.OK), messageSource.getMessage(Message.CANCEL_LABEL), 
            new ConfirmDialog.Listener() {
                
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        germplasmImportMain.reset();
                    }
                }
            }
        );
    }
    


}
