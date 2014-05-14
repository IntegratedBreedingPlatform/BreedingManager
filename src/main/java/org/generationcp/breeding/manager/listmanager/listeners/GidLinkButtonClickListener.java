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

import java.util.Iterator;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.commons.tomcat.util.TomcatUtil;
import org.generationcp.commons.tomcat.util.WebAppStatusInfo;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GidLinkButtonClickListener implements Button.ClickListener {

    private static final long serialVersionUID = -6751894969990825730L;
    private final static Logger LOG = LoggerFactory.getLogger(GidLinkButtonClickListener.class);
    public static final String GERMPLASM_IMPORT_WINDOW_NAME = "germplasm-import";
	public static final String GERMPLASM_BROWSER_LINK = "http://localhost:18080/GermplasmStudyBrowser/main/germplasm-";
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private TomcatUtil tomcatUtil;
    
    private String gid;
    private Boolean viaToolURL;
    private Boolean showAddToList;
    
    public GidLinkButtonClickListener(String gid, Boolean viaToolURL) {
        this.gid = gid;
        this.viaToolURL = viaToolURL;
        this.showAddToList = false;
    }

    public GidLinkButtonClickListener(String gid, Boolean viaToolURL, Boolean showAddToList) {
        this.gid = gid;
        this.viaToolURL = viaToolURL;
        this.showAddToList = showAddToList;
    }    

    @Override
    public void buttonClick(ClickEvent event) {
        
    	final Window mainWindow;
    	if(viaToolURL)
    		mainWindow = event.getComponent().getWindow();
    	else
    		mainWindow = event.getComponent().getApplication().getWindow(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME);
        
    	launchWebTool();
    	
    	Tool tool = null;
        try {
            tool = workbenchDataManager.getToolWithName(ToolName.germplasm_browser.toString());
        } catch (MiddlewareQueryException qe) {
            LOG.error("QueryException", qe);
            /*MessageNotifier.showError(mainWindow, messageSource.getMessage(Message.DATABASE_ERROR),
                    "<br />" + messageSource.getMessage(Message.CONTACT_ADMIN_ERROR_DESC));*/
        }
        
        ExternalResource germplasmBrowserLink = null;
        if (tool == null) {
            germplasmBrowserLink = new ExternalResource(GERMPLASM_BROWSER_LINK + gid + "?restartApplication");
        } else {
            germplasmBrowserLink = new ExternalResource(tool.getPath().replace("germplasm/", "germplasm-") + gid + "?restartApplication");
        }
        
        String preferredName = null;
        try{
        	Name prefName = germplasmDataManager.getPreferredNameByGID(Integer.valueOf(gid));
        	if(prefName != null){
        		preferredName = prefName.getNval();
        	}
        } catch(MiddlewareQueryException ex){
        	LOG.error("Error with getting preferred name of " + gid, ex);
        }
        
        String windowTitle = "Germplasm Details: " + "(" + gid + ")";
        if(preferredName != null){
        	windowTitle = "Germplasm Details: " + preferredName + " (GID: " + gid + ")";
        }
        final Window germplasmWindow = new Window(windowTitle);
        
        AbsoluteLayout layoutForGermplasm = new AbsoluteLayout();
        layoutForGermplasm.setMargin(false);
        layoutForGermplasm.setWidth("100%");
        layoutForGermplasm.setHeight("100%");
        layoutForGermplasm.addStyleName("no-caption");

        Embedded germplasmInfo = new Embedded("", germplasmBrowserLink);
        germplasmInfo.setType(Embedded.TYPE_BROWSER);
        germplasmInfo.setSizeFull();
        
        if(showAddToList){
	        Button addToListLink = new Button("Add to list");
			//addToListLink.setData(ADD_TO_LIST);
			addToListLink.setImmediate(true);
			addToListLink.setStyleName(Bootstrap.Buttons.INFO.styleName());
			addToListLink.setIcon(AppConstants.Icons.ICON_PLUS);
	        layoutForGermplasm.addComponent(addToListLink, "top:15px; right:15px;");
	        layoutForGermplasm.addComponent(germplasmInfo, "top:40px; left:0;");
	        
	        addToListLink.addListener(new ClickListener(){
				private static final long serialVersionUID = 1L;
				@Override
				public void buttonClick(ClickEvent event) {
					
					Window listManagerWindow = ((BreedingManagerApplication) event.getComponent().getApplication()).getWindow(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME);
					Iterator<Component> i = listManagerWindow.getComponentIterator();
					while (i.hasNext()) {
					    Component c = (Component) i.next();
					    if(c instanceof ListManagerMain){
					    	((ListManagerMain) c).getListBuilderComponent().getBuildNewListDropHandler().addGermplasm(Integer.valueOf(gid));
					    }
					}
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
        
        germplasmWindow.setModal(true);
        germplasmWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        germplasmWindow.addStyleName("graybg");
        
        mainWindow.addWindow(germplasmWindow);
    }
    
    
    private void launchWebTool(){
    	
		try {
			Tool germplasmBrowserTool;
			germplasmBrowserTool = workbenchDataManager.getToolWithName(ToolName.germplasm_browser.name());
			
			String url = germplasmBrowserTool.getPath();
	    	
	        WebAppStatusInfo statusInfo = null;
	        String contextPath = null;
	        String localWarPath = null;
	        try {
	        	
	            statusInfo = tomcatUtil.getWebAppStatus();
	            contextPath = TomcatUtil.getContextPathFromUrl(url);
	            localWarPath = TomcatUtil.getLocalWarPathFromUrl(url);
	            
	        }
	        catch (Exception e1) {
	          e1.printStackTrace();
	        }
	    	        
	    	      
	        try {
	            boolean deployed = statusInfo.isDeployed(contextPath);
	            boolean running = statusInfo.isRunning(contextPath);
	            
	            if (!running) {
	                if (!deployed) {
	                    // deploy the webapp
	                    tomcatUtil.deployLocalWar(contextPath, localWarPath);
	                } else {
	                    // start the webapp
	                    tomcatUtil.startWebApp(contextPath);
	                }
	            }
	        }
	        catch (Exception e) {
	           //e.printStackTrace();
	        }
			
		} catch (MiddlewareQueryException e2) {
			// TODO Auto-generated catch block
			//e2.printStackTrace();
		}
    	
    	        
    }

}
