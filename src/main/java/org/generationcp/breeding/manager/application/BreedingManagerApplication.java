package org.generationcp.breeding.manager.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.commons.hibernate.DynamicManagerFactoryProviderConcurrency;
import org.generationcp.commons.hibernate.util.HttpRequestAwareUtil;
import org.generationcp.commons.vaadin.actions.UpdateComponentLabelsAction;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;


public class BreedingManagerApplication extends SpringContextApplication implements ApplicationContextAware{
    private static final Logger LOG = LoggerFactory.getLogger(BreedingManagerApplication.class);

    private static final long serialVersionUID = 1L;
    
    public static final String GERMPLASM_IMPORT_WINDOW_NAME = "germplasm-import";
    public static final String GERMPLASM_IMPORT_WINDOW_NAME_POPUP = "germplasm-import-popup";
    public static final String CROSSING_MANAGER_WINDOW_NAME = "crosses";
    public static final String NURSERY_TEMPLATE_WINDOW_NAME = "nursery-template";
    public static final String LIST_MANAGER_WINDOW_NAME = "list-manager";
    public static final String LIST_MANAGER_WITH_OPEN_LIST_WINDOW_NAME = "listmanager-";
    public static final String LIST_MANAGER_SIDEBYSIDE = "list-manager-sidebyside";
    public static final String MANAGE_SETTINGS_CROSSING_MANAGER = "crosses-settings";

    private Window window;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private DynamicManagerFactoryProviderConcurrency managerFactoryProvider;
    
    private UpdateComponentLabelsAction messageSourceListener;

    private ApplicationContext applicationContext;
    
    private ListManagerMain listManagerMain;
    private ManageCrossingSettingsMain manageCrossingSettingsMain;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void initSpringApplication(ConfigurableWebApplicationContext arg0) {
        
        messageSourceListener = new UpdateComponentLabelsAction(this);
        messageSource.addListener(messageSourceListener);
        
        window = instantiateListManagerWindow(LIST_MANAGER_WINDOW_NAME);
        setMainWindow(window);
        setTheme("gcp-default");
        window.setSizeUndefined();
        
        // Override the existing error handler that shows the stack trace
        setErrorHandler(this);
    }

    @Override
    public Window getWindow(String name) {
        // dynamically create other application-level windows which is associated with specific URLs
        // these windows are the jumping on points to parts of the application
        if(super.getWindow(name) == null){
            if(name.equals(GERMPLASM_IMPORT_WINDOW_NAME)){
                Window germplasmImportWindow = new Window(messageSource.getMessage(Message.IMPORT_GERMPLASM_LIST_TAB_LABEL));
                germplasmImportWindow.setName(GERMPLASM_IMPORT_WINDOW_NAME);
                germplasmImportWindow.setSizeUndefined();
                germplasmImportWindow.setContent(new GermplasmImportMain(germplasmImportWindow,false));
                this.addWindow(germplasmImportWindow);
                return germplasmImportWindow;
            
            } else if(name.equals(GERMPLASM_IMPORT_WINDOW_NAME_POPUP)){
                Window germplasmImportWindow = new Window(messageSource.getMessage(Message.IMPORT_GERMPLASM_LIST_TAB_LABEL));
                germplasmImportWindow.setName(GERMPLASM_IMPORT_WINDOW_NAME_POPUP);
                germplasmImportWindow.setSizeUndefined();
                germplasmImportWindow.setContent(new GermplasmImportMain(germplasmImportWindow,false, true));
                this.addWindow(germplasmImportWindow);
                return germplasmImportWindow;

            } else if(name.equals(LIST_MANAGER_WINDOW_NAME)){
            	Window listManagerWindow = instantiateListManagerWindow(name);
                this.addWindow(listManagerWindow);

                return listManagerWindow;
            
            } else if(name.startsWith(LIST_MANAGER_WITH_OPEN_LIST_WINDOW_NAME)){
            	String listIdPart = name.substring(name.indexOf("-") + 1);
            	try{
	            	Integer listId = Integer.parseInt(listIdPart);
	            	Window listManagerWindow = new Window(messageSource.getMessage(Message.LIST_MANAGER_TAB_LABEL));
	                listManagerWindow.setName(name);
	                listManagerWindow.setSizeFull();
	                
	                listManagerMain = new org.generationcp.breeding.manager.listmanager.ListManagerMain(listId);
					
	                listManagerWindow.setContent(listManagerMain);
	                this.addWindow(listManagerWindow);
	                
	                return listManagerWindow;
            	} catch(NumberFormatException ex){
            		Window emptyGermplasmListDetailsWindow = new Window(messageSource.getMessage(Message.LIST_MANAGER_TAB_LABEL));
                    emptyGermplasmListDetailsWindow.setSizeUndefined();
                    emptyGermplasmListDetailsWindow.addComponent(new Label(messageSource.getMessage(Message.INVALID_LIST_ID)));
                    this.addWindow(emptyGermplasmListDetailsWindow);
                    return emptyGermplasmListDetailsWindow;
            	}
            
            } else if(name.equals(CROSSING_MANAGER_WINDOW_NAME)){
                Window manageCrossingSettings = new Window(messageSource.getMessage(Message.MANAGE_CROSSES));
                manageCrossingSettings.setName(CROSSING_MANAGER_WINDOW_NAME);
                manageCrossingSettings.setSizeUndefined();
                
                manageCrossingSettingsMain = new ManageCrossingSettingsMain(manageCrossingSettings);
                
                manageCrossingSettings.setContent(manageCrossingSettingsMain);
                this.addWindow(manageCrossingSettings);
                return manageCrossingSettings;
            } 
        }
        
        return super.getWindow(name);
    }

	private Window instantiateListManagerWindow(String name) {
		Window listManagerWindow = new Window(messageSource.getMessage(Message.LIST_MANAGER_TAB_LABEL));
		listManagerWindow.setName(name);
		listManagerWindow.setSizeFull();

		listManagerMain = new org.generationcp.breeding.manager.listmanager.ListManagerMain();
		
		listManagerWindow.setContent(listManagerMain);
		return listManagerWindow;
	}
    
    /** 
     * Override terminalError() to handle terminal errors, to avoid showing the stack trace in the application 
     */
    @Override
    public void terminalError(Terminal.ErrorEvent event) {
        LOG.error("An unchecked exception occurred: ", event.getThrowable());
        event.getThrowable().printStackTrace();
        // Some custom behaviour.
        if (getMainWindow() != null) {
            MessageNotifier.showError(getMainWindow(), messageSource.getMessage(Message.ERROR_INTERNAL),  // TESTED
                    messageSource.getMessage(Message.ERROR_PLEASE_CONTACT_ADMINISTRATOR)
                            + (event.getThrowable().getLocalizedMessage() == null ? "" : "</br>"
                                    + event.getThrowable().getLocalizedMessage()));
        }
    }

    @Override
    public void close() {
        super.close();
        // implement this when we need to do something on session timeout
        messageSource.removeListener(messageSourceListener);
        LOG.debug("Application closed");
    }
    
    public static BreedingManagerApplication get() {
        return get(BreedingManagerApplication.class);
    }

    @Override
    protected void doOnRequestStart(HttpServletRequest request, HttpServletResponse response) {
        super.doOnRequestStart(request, response);
        LOG.trace("Request started " + request.getRequestURI() + "?" + request.getQueryString());
        synchronized (this) {
            HttpRequestAwareUtil.onRequestStart(applicationContext, request, response);
        }
    }
    
    @Override
    protected void doOnRequestEnd(HttpServletRequest request, HttpServletResponse response) {
		super.doOnRequestEnd(request, response);
		LOG.trace("Request ended " + request.getRequestURI() + "?" + request.getQueryString());

		synchronized (this) {
			HttpRequestAwareUtil.onRequestEnd(applicationContext, request, response);
		}

		try {
			managerFactoryProvider.close();
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
    }

    public ListManagerMain getListManagerMain(){
    	return listManagerMain;
    }
    
    public ManageCrossingSettingsMain getManageCrossingSettingsMain() {
    	return manageCrossingSettingsMain;
    }
    
    public void refreshListManagerTree(){
		if(listManagerMain!=null){
			listManagerMain.getListSelectionComponent().getListTreeComponent().refreshComponent();
		}
    }
    
    public void refreshCrossingManagerTree(){
		ManageCrossingSettingsMain manageCrossSettingsMain = getManageCrossingSettingsMain();
		if(manageCrossSettingsMain!=null){
			manageCrossSettingsMain.getMakeCrossesComponent().getSelectParentsComponent().getListTreeComponent().refreshComponent();
		}
    }
    
    public void updateUIForDeletedList(GermplasmList germplasmList){
    	if(getListManagerMain() != null){
    		getListManagerMain().updateUIForDeletedList(germplasmList);
    	}
    	
    	if(getManageCrossingSettingsMain() != null){
    		getManageCrossingSettingsMain().getMakeCrossesComponent().getParentsComponent().updateUIForDeletedList(germplasmList);
    		getManageCrossingSettingsMain().getMakeCrossesComponent().showNodeOnTree(germplasmList.getId());
    	}
    }
    
    
}
