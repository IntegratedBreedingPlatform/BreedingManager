package org.generationcp.browser.cross.study.h2h.main.dialogs;

import java.util.Arrays;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.main.SpecifyGermplasmsComponent;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.browser.germplasm.GermplasmSearchFormComponent;
import org.generationcp.browser.germplasm.GermplasmSearchResultComponent;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Item;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class SelectGermplasmEntryDialog extends Window implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7651767452229107837L;
    
    private final static Logger LOG = LoggerFactory.getLogger(SelectGermplasmEntryDialog.class);
    
    private static final String GID = "gid";    
    public static final String SEARCH_BUTTON_ID = "SelectGermplasmEntryDialog Search Button ID";
    public static final String CLOSE_SCREEN_BUTTON_ID = "SelectGermplasmEntryDialog Close Button ID";
    public static final String ADD_BUTTON_ID = "SelectGermplasmEntryDialog Add Button ID";
	public static final String GERMPLASM_BROWSER_LINK = "http://localhost:18080/GermplasmStudyBrowser/main/germplasm-";
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private CrossStudyDataManager crossStudyDataManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private Component source;
    private Window parentWindow;
    
    private VerticalLayout mainLayout;
    
    private Button searchButton;
    private Button doneButton;
    private Button cancelButton;
    
    private GermplasmSearchFormComponent searchComponent;
    private GermplasmSearchResultComponent resultComponent;
    private GermplasmIndexContainer dataResultIndexContainer;
    private GermplasmQueries gQuery;
    
    private Integer selectedGid;
    private boolean isTestEntry; 
    
    private List<Integer> environmentIds;
    
	public void setEnvironmentIds(List<Integer> environmentIds) {
		this.environmentIds = environmentIds;
	}

	public SelectGermplasmEntryDialog(Component source, Window parentWindow, boolean isTestEntry){
        this.source = source;
        this.parentWindow = parentWindow;
        this.isTestEntry = isTestEntry;
        this.gQuery = new GermplasmQueries();
        this.dataResultIndexContainer = new GermplasmIndexContainer(gQuery);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("600px");
        setHeight("530px");
        setResizable(false);
        setCaption("Select a Germplasm");
        // center window within the browser
        center();
        
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        
        HorizontalLayout searchFormLayout = new HorizontalLayout();
        
        searchComponent = new GermplasmSearchFormComponent();
        searchFormLayout.addComponent(searchComponent);
        
        searchButton = new Button("Search");
        searchButton.setData(SEARCH_BUTTON_ID);
        searchButton.addStyleName("addTopSpace");
        searchButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        searchButton.setClickShortcut(KeyCode.ENTER);
        searchButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        searchFormLayout.addComponent(searchButton);
        
        mainLayout.addComponent(searchFormLayout);
        
        resultComponent = new GermplasmSearchResultComponent(germplasmDataManager, GID, "0");
        resultComponent.addListener(new GermplasmItemClickListener(this));
        resultComponent.setHeight("320px");
        mainLayout.addComponent(resultComponent);
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        
        cancelButton = new Button("Close Screen");
        cancelButton.setData(CLOSE_SCREEN_BUTTON_ID);
        cancelButton.addListener(new CloseWindowAction());
        
        String buttonlabel = "";
        if(isTestEntry)
        	buttonlabel = "Add as Test Entry";
        else
        	buttonlabel = "Add as Standard Entry";
        doneButton = new Button(buttonlabel);
        doneButton.setData(ADD_BUTTON_ID);
        doneButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        doneButton.addListener(new CloseWindowAction());
        doneButton.setEnabled(false);
        doneButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        buttonLayout.addComponent(doneButton);
        buttonLayout.addComponent(cancelButton);
        
        mainLayout.addComponent(buttonLayout);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);
        
        addComponent(mainLayout);
    }

    public void searchButtonClickAction() {
        this.doneButton.setEnabled(false);
        this.selectedGid = null;
        
        String searchChoice = searchComponent.getChoice();
        String searchValue = searchComponent.getSearchValue();

        if (searchValue.length() > 0) {
            boolean withNoError = true;
        
            if ("GID".equals(searchChoice)) {
                try {
                    Integer.parseInt(searchValue);
                } catch (NumberFormatException e) {
                    withNoError = false;
                    if (getWindow() != null) {
                        MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_INVALID_FORMAT), 
                                messageSource.getMessage(Message.ERROR_INVALID_INPUT_MUST_BE_NUMERIC), Notification.POSITION_CENTERED);
                    }
                }
            }
            
            // TODO : perhaps default to full search to prevent NPE
            if(withNoError){
            	LazyQueryContainer dataSourceResultLazy = null;
            	if(isTestEntry || environmentIds == null) {
            		dataSourceResultLazy =  dataResultIndexContainer.getGermplasmResultLazyContainer(germplasmDataManager, searchChoice, searchValue);   
            	} else {
            		environmentIds = Arrays.asList(new Integer[] {5794, 5795, 5796, 5880});
                    dataSourceResultLazy =  dataResultIndexContainer.getGermplasmEnvironmentResultLazyContainer(crossStudyDataManager, searchChoice, searchValue, environmentIds);               		
            	}
                resultComponent.setCaption("Germplasm Search Result: " + dataSourceResultLazy.size());
                resultComponent.setContainerDataSource(dataSourceResultLazy);
                mainLayout.requestRepaintAll();
            }
        } else {
            MessageNotifier.showError(getWindow(), "Error", "Please input search string.", Notification.POSITION_CENTERED);
        }
    }
    
    public void addButtonClickAction(){
        try{
            Germplasm selectedGermplasm = this.germplasmDataManager.getGermplasmWithPrefName(selectedGid);
            if(isTestEntry){
            	((SpecifyGermplasmsComponent)source).addTestGermplasm(selectedGermplasm);
            }else{
            	((SpecifyGermplasmsComponent)source).addStandardGermplasm(selectedGermplasm);
            }
        } catch (MiddlewareQueryException ex){
            LOG.error("Error with getting germplasm with gid: " + this.selectedGid, ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting germplasm with gid: " 
                    + this.selectedGid + ". " +messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
        } catch (Exception ex){
            LOG.error("Error with setting selected germplasm.", ex);
            MessageNotifier.showError(getWindow(), "Application Error!", "Error with setting selected germplasm." 
                    + " " + messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
        }
    }

    public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        sourceTable.select(itemId);
        this.selectedGid = Integer.valueOf(item.getItemProperty(GID).toString());
        this.doneButton.setEnabled(true);
    }
    
    public void resultTableItemDoubleClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        sourceTable.select(itemId);
        int gid = Integer.valueOf(item.getItemProperty(GID).toString());
        
        Tool tool = null;
        try {
            tool = workbenchDataManager.getToolWithName(ToolName.germplasm_browser.toString());
        } catch (MiddlewareQueryException qe) {
            LOG.error("QueryException", qe);
        }
        
        ExternalResource germplasmBrowserLink = null;
        if (tool == null) {
            germplasmBrowserLink = new ExternalResource(GERMPLASM_BROWSER_LINK + gid);
        } else {
            germplasmBrowserLink = new ExternalResource(tool.getPath().replace("germplasm/", "germplasm-") + gid);
        }
        
        Window germplasmWindow = new Window("Germplasm Information - " + gid);
        
        VerticalLayout layoutForGermplasm = new VerticalLayout();
        layoutForGermplasm.setMargin(false);
        layoutForGermplasm.setWidth("98%");
        layoutForGermplasm.setHeight("98%");
        
        Embedded germplasmInfo = new Embedded("", germplasmBrowserLink);
        germplasmInfo.setType(Embedded.TYPE_BROWSER);
        germplasmInfo.setSizeFull();
        layoutForGermplasm.addComponent(germplasmInfo);
        
        germplasmWindow.setContent(layoutForGermplasm);
        
        //Instead of setting by percentage, compute it
        germplasmWindow.setWidth(Integer.valueOf((int) Math.round(parentWindow.getWidth()*.90))+"px");
        germplasmWindow.setHeight(Integer.valueOf((int) Math.round(parentWindow.getHeight()*.90))+"px");
        
        germplasmWindow.center();
        germplasmWindow.setResizable(false);
        
        germplasmWindow.setModal(true);
        
        this.parentWindow.addWindow(germplasmWindow);
    }
    
    @Override
    public void updateLabels() {
        
    }
}
