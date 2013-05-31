package org.generationcp.browser.germplasmlist.dialogs;

import java.util.Date;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.browser.germplasm.GermplasmSearchFormComponent;
import org.generationcp.browser.germplasm.GermplasmSearchResultComponent;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.browser.germplasmlist.listeners.GermplasmListButtonClickListener;
import org.generationcp.browser.germplasmlist.listeners.GermplasmListItemClickListener;
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
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
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
    public static final String BACK_BUTTON_ID = "AddEntryDialog Back Button";
    private static final String GID = "gid";
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private Window parentWindow;
    private VerticalLayout firstTabLayout;
    private AbsoluteLayout secondTabLayout;
    private AddEntryDialogSource source;
    private GermplasmSearchFormComponent searchForm;
    private Button searchButton;
    private GermplasmSearchResultComponent resultComponent;
    private OptionGroup optionGroup;
    private Integer selectedGid;
    
    private Accordion accordion;
    
    private Button nextButton;
    private Button cancelButton;
    private Button doneButton;
    private Button backButton;
    
    private Label breedingMethodLabel;
    private Label germplasmDateLabel;
    private Label locationLabel;
    private Label nameTypeLabel;
    
    private ComboBox breedingMethodComboBox;
    private ComboBox locationComboBox;
    private ComboBox nameTypeComboBox;
    
    private DateField germplasmDateField;
    
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
        setHeight("650px");
        setResizable(false);
        setCaption("Add List Entry");
        // center window within the browser
        center();
        
        accordion = new Accordion();
        accordion.setSizeFull();
        
        assembleFirstTab();
        assembleSecondTab();
        accordion.addTab(firstTabLayout, "Select a Germplasm");
        accordion.addTab(secondTabLayout, "Specify additional details");
        accordion.getTab(secondTabLayout).setEnabled(false);
        addComponent(accordion);
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
                firstTabLayout.requestRepaintAll();
            }
        } else {
            MessageNotifier.showError(getWindow(), "Error", "Please input search string.");
        }
    }
    
    public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        sourceTable.select(itemId);
        int gid = Integer.valueOf(item.getItemProperty(GID).toString());
        this.selectedGid = gid;
        this.nextButton.setEnabled(true);
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
    
    private void assembleFirstTab(){
        firstTabLayout = new VerticalLayout();
        firstTabLayout.setSpacing(true);
        firstTabLayout.setMargin(true);
        
        Label step1Label = new Label("1. Search for Germplasm Record to add as List Entry.  Double click on a row in th result table to view germplasm details.");
        firstTabLayout.addComponent(step1Label);
        
        HorizontalLayout searchFormLayout = new HorizontalLayout();
        
        searchForm = new GermplasmSearchFormComponent();
        searchFormLayout.addComponent(searchForm);
        
        searchButton = new Button("Search");
        searchButton.setData(SEARCH_BUTTON_ID);
        searchButton.addStyleName("addTopSpace");
        searchButton.addListener(new GermplasmListButtonClickListener(this));
        searchButton.setClickShortcut(KeyCode.ENTER);
        searchFormLayout.addComponent(searchButton);
        
        firstTabLayout.addComponent(searchFormLayout);
        
        resultComponent = new GermplasmSearchResultComponent(germplasmDataManager, GID, "0");
        resultComponent.addListener(new GermplasmListItemClickListener(this));
        firstTabLayout.addComponent(resultComponent);
        
        Label step2Label = new Label("2. Select how you want to add the germplasm to the list.");
        firstTabLayout.addComponent(step2Label);
        
        optionGroup = new OptionGroup();
        optionGroup.addItem(OPTION_1_ID);
        optionGroup.setItemCaption(OPTION_1_ID, "Use the selected germplasm for the list entry.");
        optionGroup.addItem(OPTION_2_ID);
        optionGroup.setItemCaption(OPTION_2_ID, "Create a new germplasm record for the list entry and assign the selected germplasm as its source.");
        optionGroup.addItem(OPTION_3_ID);
        optionGroup.setItemCaption(OPTION_3_ID, "Create a new germplasm record for the list entry.");
        optionGroup.select(OPTION_1_ID);
        optionGroup.setImmediate(true);
        optionGroup.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                if(optionGroup.getValue().equals(OPTION_1_ID)){
                    nextButton.setCaption("Done");
                    accordion.getTab(secondTabLayout).setEnabled(false);
                }else{
                    nextButton.setCaption("Next");
                }
            }
        });
        firstTabLayout.addComponent(optionGroup);
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        
        cancelButton = new Button("Cancel");
        cancelButton.setData(CANCEL_BUTTON_ID);
        cancelButton.addListener(new CloseWindowAction());
        buttonLayout.addComponent(cancelButton);
        
        nextButton = new Button("Done");
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new GermplasmListButtonClickListener(this));
        nextButton.setEnabled(false);
        buttonLayout.addComponent(nextButton);
        
        firstTabLayout.addComponent(buttonLayout);
    }
    
    private void assembleSecondTab(){
        secondTabLayout = new AbsoluteLayout();
        secondTabLayout.setWidth("600px");
        secondTabLayout.setHeight("400px");
        
        breedingMethodLabel = new Label("Breeding Method");
        secondTabLayout.addComponent(breedingMethodLabel, "top:30px;left:20px");
        
        breedingMethodComboBox = new ComboBox();
        breedingMethodComboBox.setWidth("400px");
        secondTabLayout.addComponent(breedingMethodComboBox, "top:30px;left:200px");
        
        germplasmDateLabel = new Label("Date of Creation");
        secondTabLayout.addComponent(germplasmDateLabel, "top:60px;left:20px");
        
        germplasmDateField =  new DateField();
        germplasmDateField.setResolution(DateField.RESOLUTION_DAY);
        germplasmDateField.setDateFormat("yyyy-MM-dd");
        germplasmDateField.setValue(new Date());
        secondTabLayout.addComponent(germplasmDateField, "top:60px;left:200px");
        
        locationLabel = new Label("Location");
        secondTabLayout.addComponent(locationLabel, "top:90px;left:20px");
        
        locationComboBox = new ComboBox();
        locationComboBox.setWidth("400px");
        secondTabLayout.addComponent(locationComboBox, "top:90px;left:200px");
        
        nameTypeLabel = new Label("Name Type");
        secondTabLayout.addComponent(nameTypeLabel, "top:120px;left:20px");
        
        nameTypeComboBox = new ComboBox();
        nameTypeComboBox.setWidth("400px");
        secondTabLayout.addComponent(nameTypeComboBox, "top:120px;left:200px");
        
        backButton = new Button("Back");
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(new GermplasmListButtonClickListener(this));
        secondTabLayout.addComponent(backButton, "top:180px;left:20px");
        
        doneButton = new Button("Done");
        secondTabLayout.addComponent(doneButton, "top:180px;left:90px");
    }
    
    public void nextButtonClickAction(ClickEvent event){
        if(optionGroup.getValue().equals(OPTION_1_ID)){
            // add the germplasm selected as the list entry
            if(this.selectedGid != null){
                this.source.finishAddingEntry(this.selectedGid);
                Window window = event.getButton().getWindow();
                window.getParent().removeWindow(window);
            }
        } else {
            this.accordion.getTab(secondTabLayout).setEnabled(true);
            this.accordion.setSelectedTab(secondTabLayout);
        }
    }
    
    public void backButtonClickAction(){
        this.accordion.setSelectedTab(firstTabLayout);
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(searchButton, Message.SEARCH_LABEL);
    }

}
