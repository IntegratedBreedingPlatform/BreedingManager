package org.generationcp.browser.germplasmlist.dialogs;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.browser.germplasm.GermplasmSearchFormComponent;
import org.generationcp.browser.germplasm.GermplasmSearchResultComponent;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.browser.germplasmlist.listeners.GermplasmListButtonClickListener;
import org.generationcp.browser.germplasmlist.listeners.GermplasmListItemClickListener;
import org.generationcp.browser.study.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Item;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class AddEntryDialog extends Window implements InitializingBean, InternationalizableComponent {
    
    private static final long serialVersionUID = -1627453790001229325L;
    
    private final static Logger LOG = LoggerFactory.getLogger(AddEntryDialog.class);
    
    public static final String SEARCH_BUTTON_ID = "AddEntryDialog Search Button";
    public static final String OPTION_1_ID = "AddEntryDialog Option 1";
    public static final String OPTION_2_ID = "AddEntryDialog Option 2";
    public static final String OPTION_3_ID = "AddEntryDialog Option 3";
    public static final String NEXT_BUTTON_ID = "AddEntryDialog Next Button";
    public static final String CANCEL_BUTTON_ID = "AddEntryDialog Cancel Button";
    private static final String GID = "gid";
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private Window parentWindow;
    private VerticalLayout mainLayout;
    private AddEntryDialogSource source;
    private GermplasmSearchFormComponent searchForm;
    private Button searchButton;
    private GermplasmSearchResultComponent resultComponent;
    private OptionGroup optionGroup;
    private Button nextButton;
    private Button cancelButton;
    
    private GermplasmQueries gQuery;
    private GermplasmIndexContainer dataResultIndexContainer;

    public AddEntryDialog(AddEntryDialogSource source, Window parentWindow){
        this.source = source;
        this.parentWindow = parentWindow;
        gQuery = new GermplasmQueries();
        dataResultIndexContainer = new GermplasmIndexContainer(gQuery);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        // set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("800px");
        setHeight("600px");
        setResizable(false);
        setCaption("Add List Entry");
        // center window within the browser
        center();
        
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        
        Label step1Label = new Label("1. Search for Germplasm Record to add as List Entry.");
        mainLayout.addComponent(step1Label);
        
        HorizontalLayout searchFormLayout = new HorizontalLayout();
        
        searchForm = new GermplasmSearchFormComponent();
        searchFormLayout.addComponent(searchForm);
        
        searchButton = new Button("Search");
        searchButton.setData(SEARCH_BUTTON_ID);
        searchButton.addStyleName("addTopSpace");
        searchButton.addListener(new GermplasmListButtonClickListener(this));
        searchFormLayout.addComponent(searchButton);
        
        mainLayout.addComponent(searchFormLayout);
        
        resultComponent = new GermplasmSearchResultComponent(germplasmDataManager, GID, "0");
        resultComponent.addListener(new GermplasmListItemClickListener(this));
        mainLayout.addComponent(resultComponent);
        
        Label step2Label = new Label("2. Select how you want to add the germplasm to the list.");
        mainLayout.addComponent(step2Label);
        
        optionGroup = new OptionGroup();
        optionGroup.addItem(OPTION_1_ID);
        optionGroup.setItemCaption(OPTION_1_ID, "Use the selected germplasm for the list entry.");
        optionGroup.addItem(OPTION_2_ID);
        optionGroup.setItemCaption(OPTION_2_ID, "Create a new germplasm record for the list entry and assign the selected germplasm as its source.");
        optionGroup.addItem(OPTION_3_ID);
        optionGroup.setItemCaption(OPTION_3_ID, "Create a new germplasm record for the list entry.");
        optionGroup.select(OPTION_1_ID);
        optionGroup.setImmediate(true);
        mainLayout.addComponent(optionGroup);
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        
        cancelButton = new Button("Cancel");
        cancelButton.addListener(new CloseWindowAction());
        buttonLayout.addComponent(cancelButton);
        
        nextButton = new Button("Next");
        buttonLayout.addComponent(nextButton);
        
        mainLayout.addComponent(buttonLayout);
        
        addComponent(mainLayout);
    }
    
    public void searchButtonClickAction() throws InternationalizableException {

        String searchChoice = searchForm.getChoice();
        String searchValue = searchForm.getSearchValue();

        if (searchValue.length() > 0) {
            boolean withNoError = true;
        
            if ("GID".equals(searchChoice)) {
                try {
                    int gid = Integer.parseInt(searchValue);
                } catch (NumberFormatException e) {
                    withNoError = false;
                    if (getWindow() != null) {
                        MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_INVALID_FORMAT), 
                                messageSource.getMessage(Message.ERROR_INVALID_INPUT_MUST_BE_NUMERIC));
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
            MessageNotifier.showError(getWindow(), "Error", "Please input search string.");
        }
    }
    
    public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
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
        layoutForGermplasm.setWidth("640px");
        layoutForGermplasm.setHeight("560px");
        
        Embedded germplasmInfo = new Embedded("", germplasmBrowserLink);
        germplasmInfo.setType(Embedded.TYPE_BROWSER);
        germplasmInfo.setSizeFull();
        layoutForGermplasm.addComponent(germplasmInfo);
        
        germplasmWindow.setContent(layoutForGermplasm);
        germplasmWindow.setWidth("645px");
        germplasmWindow.setHeight("600px");
        germplasmWindow.center();
        germplasmWindow.setResizable(false);
        
        germplasmWindow.setModal(true);
        
        this.parentWindow.addWindow(germplasmWindow);
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(searchButton, Message.SEARCH_LABEL);
    }

}
