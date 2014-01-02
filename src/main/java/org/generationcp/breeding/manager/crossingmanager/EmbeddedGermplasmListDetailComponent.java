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

import java.io.File;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerImportButtonClickListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerExporter;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerExporterException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@Configurable
public class EmbeddedGermplasmListDetailComponent extends VerticalLayout
    implements InitializingBean, InternationalizableComponent{
    
    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedGermplasmListDetailComponent.class);
    private static final long serialVersionUID = -8889276342164300525L;
    
    public static final String EXPORT_BUTTON_ID = "Export Button ID";
    public static final String MAKE_CROSSES_BUTTON_ID = "Make New Crosses Button ID";
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private UserDataManager userDataManager;
    
    private Button exportButton;
    private Button makeCrossesButton;
    
    private CrossingManagerMain crossingManager;
    private Integer listId;
    
    public EmbeddedGermplasmListDetailComponent(CrossingManagerMain crossingManager, Integer listId) {
        this.crossingManager = crossingManager;
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
  
        CrossingManagerImportButtonClickListener listener = new CrossingManagerImportButtonClickListener(this);
    
        exportButton = new Button();
        exportButton.setData(EXPORT_BUTTON_ID);
        exportButton.addListener(listener);
        
        makeCrossesButton = new Button();
        makeCrossesButton.setData(MAKE_CROSSES_BUTTON_ID);
        makeCrossesButton.addListener(listener);
        
        HorizontalLayout buttonArea = new HorizontalLayout();
        buttonArea.setMargin(true);
        buttonArea.setSpacing(true);
        buttonArea.addComponent(exportButton);
        buttonArea.addComponent(makeCrossesButton);
        
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
        messageSource.setCaption(exportButton, Message.EXPORT_CROSSES_MADE);
        messageSource.setCaption(makeCrossesButton, Message.MAKE_NEW_CROSSES);
    }
    
    @SuppressWarnings("serial")
    public void makeNewCrossesButtonClickAction(){
        ConfirmDialog.show(this.getWindow(), messageSource.getMessage(Message.MAKE_NEW_CROSSES), 
            messageSource.getMessage(Message.CONFIRM_REDIRECT_TO_MAKE_CROSSES_WIZARD), 
            messageSource.getMessage(Message.OK), messageSource.getMessage(Message.CANCEL_LABEL), 
            new ConfirmDialog.Listener() {
                
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        crossingManager.reset();
                    }
                }
            }
        );
    }
    
    public void exportToFileButtonClickAction(){
        CrossesMade crossesMade = this.crossingManager.getCrossesMade();

        if(crossesMade != null){
            String tempFileName = System.getProperty( "user.home" ) + "/temp.xls";
            
            GermplasmList crossesList = null;
            try {
                crossesList = germplasmListManager.getGermplasmListById(listId);
            } catch(MiddlewareQueryException ex){
                LOG.error("Error getting list with id: " + listId, ex);
                MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting the list of crosses made.  "+messageSource.getMessage(Message.ERROR_REPORT_TO)
                        ,Notification.POSITION_CENTERED);
                return;
            }
            
            User listCreator = null;
            User listExporter = null;
            try{
                listCreator = userDataManager.getUserById(crossesList.getUserId());
            } catch(MiddlewareQueryException ex){
                LOG.error("Error getting users for list creator.", ex);
                MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting user record for list owner.  "+messageSource.getMessage(Message.ERROR_REPORT_TO)
                        ,Notification.POSITION_CENTERED);
                return;
            }
            
            try{
                WorkbenchRuntimeData data = workbenchDataManager.getWorkbenchRuntimeData();
                Project project = workbenchDataManager.getLastOpenedProject(data.getUserId());
                Integer userId = workbenchDataManager.getLocalIbdbUserId(data.getUserId(), project.getProjectId());
                listExporter = userDataManager.getUserById(userId);
            } catch(MiddlewareQueryException ex){
                LOG.error("Error getting users for list exporter.", ex);
                MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting user record for list exporter.  "+messageSource.getMessage(Message.ERROR_REPORT_TO)
                        ,Notification.POSITION_CENTERED);
                return;
            }
            
            CrossingManagerExporter exporter = new CrossingManagerExporter(crossesList, crossesMade, listCreator, listExporter);
    
            try {
                exporter.exportCrossingManagerExcel(tempFileName);
                FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.getApplication());
                fileDownloadResource.setFilename(crossesMade.getGermplasmList().getName().replace(" ", "_") + ".xls");
    
                this.getWindow().open(fileDownloadResource);
        
            } catch (CrossingManagerExporterException e) {
                MessageNotifier.showError(getWindow(), "Error with exporting nursery file.", e.getMessage(), Notification.POSITION_CENTERED);
            }
        } 
    
    }

}
