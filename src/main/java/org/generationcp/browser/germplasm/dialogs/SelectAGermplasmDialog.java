package org.generationcp.browser.germplasm.dialogs;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.browser.germplasm.GermplasmSearchFormComponent;
import org.generationcp.browser.germplasm.GermplasmSearchResultComponent;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.browser.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class SelectAGermplasmDialog extends Window implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7651767452229107837L;
    
    private final static Logger LOG = LoggerFactory.getLogger(SelectAGermplasmDialog.class);
    
    private static final String GID = "gid";
    
    public static final String SEARCH_BUTTON_ID = "SelectAGermplasmDialog Search Button ID";
    public static final String CANCEL_BUTTON_ID = "SelectAGermplasmDialog Cancel Button ID";
    public static final String DONE_BUTTON_ID = "SelectAGermplasmDialog Done Button ID";
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private Label germplasmComponent;
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
    
    public SelectAGermplasmDialog(Component source, Window parentWindow, Label germplasmComponent){
        this.parentWindow = parentWindow;
        this.germplasmComponent = germplasmComponent;
        this.gQuery = new GermplasmQueries();
        this.dataResultIndexContainer = new GermplasmIndexContainer(gQuery);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("600px");
        setHeight("500px");
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
        searchButton.addListener(new GermplasmButtonClickListener(this));
        searchButton.setClickShortcut(KeyCode.ENTER);
        searchFormLayout.addComponent(searchButton);
        
        mainLayout.addComponent(searchFormLayout);
        
        resultComponent = new GermplasmSearchResultComponent(germplasmDataManager, GID, "0");
        resultComponent.addListener(new GermplasmItemClickListener(this));
        mainLayout.addComponent(resultComponent);
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        
        cancelButton = new Button("Cancel");
        cancelButton.setData(CANCEL_BUTTON_ID);
        cancelButton.addListener(new CloseWindowAction());
        buttonLayout.addComponent(cancelButton);
        
        doneButton = new Button("Done");
        doneButton.setData(DONE_BUTTON_ID);
        doneButton.addListener(new GermplasmButtonClickListener(this));
        doneButton.addListener(new CloseWindowAction());
        doneButton.setEnabled(false);
        buttonLayout.addComponent(doneButton);
        
        mainLayout.addComponent(buttonLayout);
        
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
                
            if(withNoError){
                LazyQueryContainer dataSourceResultLazy =  dataResultIndexContainer.getGermplasmResultLazyContainer(germplasmDataManager, searchChoice, searchValue);                                        
                resultComponent.setCaption("Germplasm Search Result: " + dataSourceResultLazy.size());
                resultComponent.setContainerDataSource(dataSourceResultLazy);
                mainLayout.requestRepaintAll();
            }
        } else {
            MessageNotifier.showError(getWindow(), "Error", "Please input search string.", Notification.POSITION_CENTERED);
        }
    }
    
    public void doneButtonClickAction(){
        try{
            Germplasm selectedGermplasm = this.germplasmDataManager.getGermplasmWithPrefName(selectedGid);
            this.germplasmComponent.setData(selectedGermplasm.getGid());
            if(selectedGermplasm.getPreferredName() != null){
                String preferredName = selectedGermplasm.getPreferredName().getNval();
                this.germplasmComponent.setValue("" + selectedGermplasm.getGid() + " - " + preferredName);
            } else{
                this.germplasmComponent.setValue(selectedGermplasm.getGid());
                MessageNotifier.showWarning(getWindow(), "Warning!", "The germplasm you selected doesn't have a preferred name, "
                    + "please select a different germplasm.", Notification.POSITION_CENTERED);
            }
            this.germplasmComponent.requestRepaint();
        } catch (MiddlewareQueryException ex){
            LOG.error("Error with getting germplasm with gid: " + this.selectedGid, ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting germplasm with gid: " 
                    + this.selectedGid + ". " + messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
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
            germplasmBrowserLink = new ExternalResource("http://localhost:18080/GermplasmStudyBrowser/main/germplasm-" + gid);
        } else {
            germplasmBrowserLink = new ExternalResource(tool.getPath().replace("germplasm/", "germplasm-") + gid);
        }
        
        Window germplasmWindow = new Window("Germplasm Information - " + gid);
        
        VerticalLayout layoutForGermplasm = new VerticalLayout();
        layoutForGermplasm.setMargin(false);
        //layoutForGermplasm.setWidth("640px");
        //layoutForGermplasm.setHeight("560px");
        layoutForGermplasm.setWidth("98%");
        layoutForGermplasm.setHeight("98%");
        
        Embedded germplasmInfo = new Embedded("", germplasmBrowserLink);
        germplasmInfo.setType(Embedded.TYPE_BROWSER);
        germplasmInfo.setSizeFull();
        layoutForGermplasm.addComponent(germplasmInfo);
        
        germplasmWindow.setContent(layoutForGermplasm);
        //germplasmWindow.setWidth("645px");
        //germplasmWindow.setHeight("600px");
        //germplasmWindow.setWidth("90%");
        //germplasmWindow.setHeight("90%");
        
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
