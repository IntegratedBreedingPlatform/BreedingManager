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

package org.generationcp.breeding.manager.listmanager.listeners;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ModeView;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.constant.DefaultGermplasmStudyBrowserPath;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Window;

@Configurable
public class GidLinkButtonClickListener implements Button.ClickListener {

    private static final long serialVersionUID = -6751894969990825730L;
    private final static Logger LOG = LoggerFactory.getLogger(GidLinkButtonClickListener.class);
    public static final String GERMPLASM_IMPORT_WINDOW_NAME = "germplasm-import";

    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;

    private ListManagerMain listManagerMain;
    private String gid;
    private Boolean viaToolURL;
    private Boolean showAddToList;
    
    public GidLinkButtonClickListener(String gid, Boolean viaToolURL) {
        this.gid = gid;
        this.viaToolURL = viaToolURL;
        this.showAddToList = false;
    }

    public GidLinkButtonClickListener(ListManagerMain listManagerMain, String gid, Boolean viaToolURL, Boolean showAddToList) {
    	this.listManagerMain = listManagerMain;
        this.gid = gid;
        this.viaToolURL = viaToolURL;
        this.showAddToList = showAddToList;
    }    

    @Override
    public void buttonClick(ClickEvent event) {
        
    	final Window mainWindow;
    	if(viaToolURL) {
            mainWindow = event.getComponent().getWindow();
        } else {
            mainWindow = event.getComponent().getApplication().getWindow(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME);
        }
    	Tool tool = null;
        try {
            tool = workbenchDataManager.getToolWithName(ToolName.germplasm_browser.toString());
		} catch (MiddlewareQueryException qe) {
            LOG.error("QueryException", qe);
        }

		String addtlParams = Util.getAdditionalParams(workbenchDataManager);
		ExternalResource germplasmBrowserLink;
		if (tool == null) {
			germplasmBrowserLink = new ExternalResource(
					WorkbenchAppPathResolver.getFullWebAddress(
							DefaultGermplasmStudyBrowserPath.GERMPLASM_BROWSER_LINK + gid,
							"?restartApplication" + addtlParams));
		} else {
			germplasmBrowserLink = new ExternalResource(WorkbenchAppPathResolver.getWorkbenchAppPath(tool,String.valueOf(gid),"?restartApplication" + addtlParams));
		}
        
        String preferredName = null;
        try{
        	preferredName = germplasmDataManager.getPreferredNameValueByGID(Integer.valueOf(gid));
        } catch(MiddlewareQueryException ex){
        	LOG.error("Error with getting preferred name of " + gid, ex);
        }
        
        String windowTitle = "Germplasm Details: " + "(" + gid + ")";
        if(preferredName != null){
        	windowTitle = "Germplasm Details: " + preferredName + " (GID: " + gid + ")";
        }
        final Window germplasmWindow = new BaseSubWindow(windowTitle);

        AbsoluteLayout layoutForGermplasm = new AbsoluteLayout();
        layoutForGermplasm.setMargin(false);
        layoutForGermplasm.setWidth("100%");
        layoutForGermplasm.setHeight("100%");
        layoutForGermplasm.addStyleName("no-caption");

        final Embedded germplasmInfo = new Embedded(null, germplasmBrowserLink);

        germplasmInfo.setType(Embedded.TYPE_BROWSER);
        germplasmInfo.setSizeFull();


        if(showAddToList){
	        Button addToListLink = new Button("Add to list");
	        if(listManagerMain.listBuilderIsLocked()){
	        	addToListLink.setEnabled(false);
	        	addToListLink.setDescription("Cannot add entry to locked list in List Builder section.");
	        }

	        if(listManagerMain.getModeView().equals(ModeView.INVENTORY_VIEW)){
	        	addToListLink.setEnabled(false);
	        	addToListLink.setDescription("Please switch to list view first before adding a germplasm entry to the list.");
	        }

			addToListLink.setImmediate(true);
			addToListLink.setStyleName(Bootstrap.Buttons.INFO.styleName());
			addToListLink.setIcon(AppConstants.Icons.ICON_PLUS);
	        layoutForGermplasm.addComponent(addToListLink, "top:15px; right:15px;");
	        layoutForGermplasm.addComponent(germplasmInfo, "top:44px; left:0;");

	        addToListLink.addListener(new ClickListener(){
				private static final long serialVersionUID = 1L;
				@Override
				public void buttonClick(ClickEvent event) {
					listManagerMain.getListBuilderComponent().getBuildNewListDropHandler().addGermplasm(Integer.valueOf(gid));
					mainWindow.removeWindow(germplasmWindow);
				}

	        });
        } else {
        	layoutForGermplasm.addComponent(germplasmInfo, "top:0; left:0;");
        }
        germplasmWindow.setContent(layoutForGermplasm);
        germplasmWindow.setWidth("90%");
        germplasmWindow.setHeight("90%");
        germplasmWindow.center();
        germplasmWindow.setResizable(false);
        germplasmWindow.setCloseShortcut(ShortcutAction.KeyCode.ESCAPE);

        germplasmWindow.setModal(true);
        germplasmWindow.addStyleName("graybg");

        mainWindow.addWindow(germplasmWindow);
    }
}
