package org.generationcp.breeding.manager.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.generationcp.breeding.manager.crosses.NurseryTemplateMain;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerMain;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.breeding.manager.listmanager.ListManagerMain;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerSidebysideMain;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.hibernate.DynamicManagerFactoryProvider;
import org.generationcp.commons.hibernate.util.HttpRequestAwareUtil;
import org.generationcp.commons.vaadin.actions.UpdateComponentLabelsAction;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class BreedingManagerApplication extends SpringContextApplication implements ApplicationContextAware{
    private final static Logger LOG = LoggerFactory.getLogger(BreedingManagerApplication.class);

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
    
    private VerticalLayout rootLayoutForImportGermplasmList;
    private VerticalLayout rootLayoutForCrossingManager;
    private VerticalLayout rootLayoutForNurseryTemplate;

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private DynamicManagerFactoryProvider managerFactoryProvider;
    
    @Autowired
    private WorkbenchDataManagerImpl workbenchDataManager;
    
    private UpdateComponentLabelsAction messageSourceListener;

    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void initSpringApplication(ConfigurableWebApplicationContext arg0) {
        
        messageSourceListener = new UpdateComponentLabelsAction(this);
        messageSource.addListener(messageSourceListener);
        
        this.rootLayoutForImportGermplasmList = new VerticalLayout();
        rootLayoutForImportGermplasmList.setSizeFull();

        this.rootLayoutForCrossingManager = new VerticalLayout();
        rootLayoutForCrossingManager.setSizeFull();
        
        this.rootLayoutForNurseryTemplate = new VerticalLayout();
        rootLayoutForNurseryTemplate.setSizeFull();
        
        window = new Window(messageSource.getMessage(Message.MAIN_WINDOW_CAPTION)); // "Breeding Manager"
        setMainWindow(window);
        setTheme("gcp-default");
        window.setSizeUndefined();

        TabSheet tabSheet = new TabSheet();
        
        VerticalLayout layouts[] = new VerticalLayout[3];
        layouts[0] = this.rootLayoutForImportGermplasmList;
        layouts[1] = this.rootLayoutForCrossingManager;
        layouts[2] = this.rootLayoutForNurseryTemplate;
        
        WelcomeTab welcomeTab = new WelcomeTab(tabSheet, layouts);
        
        tabSheet.addTab(welcomeTab, messageSource.getMessage(Message.WELCOME_TAB_LABEL)); // "Welcome"
        tabSheet.addTab(rootLayoutForImportGermplasmList, messageSource.getMessage(Message.IMPORT_GERMPLASM_LIST_TAB_LABEL)); // "Import Germlasm List"
        tabSheet.addTab(rootLayoutForCrossingManager, messageSource.getMessage(Message.CROSSING_MANAGER_LABEL)); // "Crossing Manager"
        tabSheet.addTab(rootLayoutForNurseryTemplate, messageSource.getMessage(Message.NURSERY_TEMPLATE_CAPTION_LABEL)); // "Nursery Template"
        tabSheet.addListener(new MainApplicationSelectedTabChangeListener(this));
        
        window.addComponent(tabSheet);
        
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
                germplasmImportWindow.addComponent(new GermplasmImportMain(germplasmImportWindow,false));
                this.addWindow(germplasmImportWindow);
                return germplasmImportWindow;
            } else if(name.equals(GERMPLASM_IMPORT_WINDOW_NAME_POPUP)){
                Window germplasmImportWindow = new Window(messageSource.getMessage(Message.IMPORT_GERMPLASM_LIST_TAB_LABEL));
                germplasmImportWindow.setName(GERMPLASM_IMPORT_WINDOW_NAME_POPUP);
                germplasmImportWindow.setSizeUndefined();
                germplasmImportWindow.addComponent(new GermplasmImportMain(germplasmImportWindow,false, true));
                this.addWindow(germplasmImportWindow);
                return germplasmImportWindow;
//            } else if(name.equals(CROSSING_MANAGER_WINDOW_NAME)){
//                Window crossingManagerWindow = new Window(messageSource.getMessage(Message.CROSSING_MANAGER_TAB_LABEL));
//                crossingManagerWindow.setName(CROSSING_MANAGER_WINDOW_NAME);
//                crossingManagerWindow.setSizeUndefined();
//                crossingManagerWindow.addComponent(new CrossingManagerMain(crossingManagerWindow));
//                this.addWindow(crossingManagerWindow);
//                return crossingManagerWindow;
            } else if(name.equals(NURSERY_TEMPLATE_WINDOW_NAME)){
                Window nurseryTemplateWindow = new Window(messageSource.getMessage(Message.NURSERY_TEMPLATE_TAB_LABEL));
                nurseryTemplateWindow.setName(NURSERY_TEMPLATE_WINDOW_NAME);
                nurseryTemplateWindow.setSizeUndefined();
                nurseryTemplateWindow.addComponent(new NurseryTemplateMain());
                this.addWindow(nurseryTemplateWindow);
                return nurseryTemplateWindow;
            } else if(name.equals(LIST_MANAGER_WINDOW_NAME)){
                Window listManagerWindow = new Window(messageSource.getMessage(Message.LIST_MANAGER_TAB_LABEL));
                listManagerWindow.setName(LIST_MANAGER_WINDOW_NAME);
                listManagerWindow.setSizeUndefined();
                listManagerWindow.addComponent(new ListManagerMain());
                this.addWindow(listManagerWindow);
                return listManagerWindow;
            } else if(name.startsWith(LIST_MANAGER_WITH_OPEN_LIST_WINDOW_NAME)){
            	String listIdPart = name.substring(name.indexOf("-") + 1);
            	try{
	            	Integer listId = Integer.parseInt(listIdPart);
	            	Window listManagerWindow = new Window(messageSource.getMessage(Message.LIST_MANAGER_TAB_LABEL));
	                listManagerWindow.setName(name);
	                listManagerWindow.setSizeUndefined();
	                listManagerWindow.addComponent(new ListManagerMain(listId));
	                this.addWindow(listManagerWindow);
	                return listManagerWindow;
            	} catch(NumberFormatException ex){
            		Window emptyGermplasmListDetailsWindow = new Window(messageSource.getMessage(Message.LIST_MANAGER_TAB_LABEL));
                    emptyGermplasmListDetailsWindow.setSizeUndefined();
                    emptyGermplasmListDetailsWindow.addComponent(new Label(messageSource.getMessage(Message.INVALID_LIST_ID)));
                    this.addWindow(emptyGermplasmListDetailsWindow);
                    return emptyGermplasmListDetailsWindow;
            	}
            } else if (name.equals(LIST_MANAGER_SIDEBYSIDE)){
                Window listManagerSideBySideWindow = new Window("List Manager (Side-by-side)");
                listManagerSideBySideWindow.setName(LIST_MANAGER_SIDEBYSIDE);
                listManagerSideBySideWindow.setSizeFull();
                listManagerSideBySideWindow.addComponent(new ListManagerSidebysideMain());
                this.setMainWindow(listManagerSideBySideWindow);
                this.getMainWindow().getContent().setHeight("100%");
                return listManagerSideBySideWindow;
            }  else if(name.equals(MANAGE_SETTINGS_CROSSING_MANAGER)){
                Window manageCrossingSettings = new Window(messageSource.getMessage(Message.CROSSING_SETTINGS_TAB_LABEL));
                manageCrossingSettings.setName(MANAGE_SETTINGS_CROSSING_MANAGER);
            }  else if(name.equals(CROSSING_MANAGER_WINDOW_NAME)){
                Window manageCrossingSettings = new Window(messageSource.getMessage(Message.MANAGE_CROSSES));
                manageCrossingSettings.setName(CROSSING_MANAGER_WINDOW_NAME);
                manageCrossingSettings.setSizeUndefined();
                manageCrossingSettings.addComponent(new ManageCrossingSettingsMain(manageCrossingSettings));
                this.addWindow(manageCrossingSettings);
                return manageCrossingSettings;
            } 
        }
        
        return super.getWindow(name);
    }
    
    public void tabSheetSelectedTabChangeAction(TabSheet source) throws InternationalizableException {

        if (source.getSelectedTab() == this.rootLayoutForImportGermplasmList) {
            if (this.rootLayoutForImportGermplasmList.getComponentCount() == 0) {
                rootLayoutForImportGermplasmList.addComponent(new GermplasmImportMain(rootLayoutForImportGermplasmList,true));
                rootLayoutForImportGermplasmList.addStyleName("addSpacing");
            } 
        }
        else if (source.getSelectedTab() == this.rootLayoutForCrossingManager) {
            if (this.rootLayoutForCrossingManager.getComponentCount() == 0) {
                rootLayoutForCrossingManager.addComponent(new CrossingManagerMain(rootLayoutForCrossingManager));
                rootLayoutForCrossingManager.addStyleName("addSpacing");
            }
        }
        else if (source.getSelectedTab() == this.rootLayoutForNurseryTemplate) {
            if (this.rootLayoutForNurseryTemplate.getComponentCount() == 0) {
                rootLayoutForNurseryTemplate.addComponent(new NurseryTemplateMain());
                rootLayoutForNurseryTemplate.addStyleName("addSpacing");
            }
        }
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
        
        	Boolean lastOpenedProjectChanged = true;
        	try {
				lastOpenedProjectChanged = workbenchDataManager.isLastOpenedProjectChanged();
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}
        	
        	if (lastOpenedProjectChanged){	
        		 try{
        	        	managerFactoryProvider.close();
        	        }catch(Exception e){
        		        e.printStackTrace();	
        	        }
				close();
				request.getSession().invalidate();
			}
        
        LOG.trace("Request started " + request.getRequestURI() + "?" + request.getQueryString());
        
        synchronized (this) {
            HttpRequestAwareUtil.onRequestEnd(applicationContext, request, response);
        }
    }
    
    @Override
    protected void doOnRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        super.doOnRequestEnd(request, response);
        
        LOG.trace("Request ended " + request.getRequestURI() + "?" + request.getQueryString());
        
        synchronized (this) {
            HttpRequestAwareUtil.onRequestEnd(applicationContext, request, response);
        }
    }

}
