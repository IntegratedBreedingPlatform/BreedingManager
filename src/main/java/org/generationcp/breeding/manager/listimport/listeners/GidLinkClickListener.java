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

package org.generationcp.breeding.manager.listimport.listeners;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.commons.tomcat.util.TomcatUtil;
import org.generationcp.commons.tomcat.util.WebAppStatusInfo;
import org.generationcp.commons.util.ContextUtil;
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
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GidLinkClickListener implements Button.ClickListener, ItemClickListener {

    private static final long serialVersionUID = -6751894969990825730L;
    private final static Logger LOG = LoggerFactory.getLogger(GidLinkClickListener.class);
    public static final String GERMPLASM_IMPORT_WINDOW_NAME = "germplasm-import";
	public static final String GERMPLASM_BROWSER_LINK = "http://localhost:18080/GermplasmStudyBrowser/main/germplasm-";
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private TomcatUtil tomcatUtil;
    
    private String gid;
    private final Boolean viaToolURL;
    private final Window parentWindow;
    
    public GidLinkClickListener() {
    	this.gid = null;
    	this.viaToolURL = false;
    	this.parentWindow = null;
    }

    public GidLinkClickListener(final String gid, final Boolean viaToolURL) {
        this.gid = gid;
        this.viaToolURL = viaToolURL;
        this.parentWindow = null;
    }

    public GidLinkClickListener(final String gid, final Window parentWindow) {
        this.gid = gid;
        this.viaToolURL = false;
        this.parentWindow = parentWindow;
    }
    
    
    @Override
    public void buttonClick(final ClickEvent event) {
    	openDetailsWindow(event.getComponent());
    }
    
	@Override
	public void itemClick(final ItemClickEvent event) {
		this.gid = ((Integer) event.getItemId()).toString();
		openDetailsWindow(event.getComponent());
		
	}

	private void openDetailsWindow (final Component component) {
		Window mainWindow;
		
		if(parentWindow!=null)
			mainWindow = parentWindow;
		else if(viaToolURL)
    		mainWindow = component.getWindow();
    	else
    		mainWindow = component.getApplication().getWindow(GERMPLASM_IMPORT_WINDOW_NAME);
        
    	launchWebTool();
    	
    	Tool tool = null;
        try {
            tool = workbenchDataManager.getToolWithName(ToolName.germplasm_browser.toString());
        } catch (MiddlewareQueryException qe) {
            LOG.error("QueryException", qe);
            /*MessageNotifier.showError(mainWindow, messageSource.getMessage(Message.DATABASE_ERROR),
                    "<br />" + messageSource.getMessage(Message.CONTACT_ADMIN_ERROR_DESC));*/
        }
        String addtlParams = ContextUtil.getContextParameterString(
        		BreedingManagerApplication.currentRequest());
        
        ExternalResource germplasmBrowserLink = null;
        if (tool == null) {
            germplasmBrowserLink = new ExternalResource(GERMPLASM_BROWSER_LINK + gid + "?restartApplication"+
            		addtlParams);
        } else {
            germplasmBrowserLink = new ExternalResource(tool.getPath().replace("germplasm/", "germplasm-") + gid + "?restartApplication"+
            		addtlParams);
        }
        
        String preferredName = null;
        try{
        	preferredName = germplasmDataManager.getPreferredNameValueByGID(Integer.valueOf(gid));
        } catch(MiddlewareQueryException ex){
        	LOG.error("Error with getting preferred name of " + gid, ex);
        }
        
        String windowTitle = "Germplasm Details: " + "(GID: " + gid + ")";
        if(preferredName != null){
        	windowTitle = "Germplasm Details: " + preferredName + " (GID: " + gid + ")";
        }
        Window germplasmWindow = new Window(windowTitle);
        
        VerticalLayout layoutForGermplasm = new VerticalLayout();
        layoutForGermplasm.setMargin(false);
        layoutForGermplasm.setWidth("100%");
        layoutForGermplasm.setHeight("100%");
        layoutForGermplasm.addStyleName("no-caption");
        
        Embedded germplasmInfo = new Embedded("", germplasmBrowserLink);
        germplasmInfo.setType(Embedded.TYPE_BROWSER);
        germplasmInfo.setSizeFull();
       
        layoutForGermplasm.addComponent(germplasmInfo);
        germplasmWindow.setContent(layoutForGermplasm);
        germplasmWindow.setWidth("90%");
        germplasmWindow.setHeight("90%");
        germplasmWindow.center();
        germplasmWindow.setResizable(false);
        
        germplasmWindow.setModal(true);
        germplasmWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        
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
