package org.generationcp.browser.germplasmlist.dialogs;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.browser.germplasm.GermplasmSearchFormComponent;
import org.generationcp.browser.germplasm.GermplasmSearchResultComponent;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasmlist.listeners.GermplasmListButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class AddEntryDialog extends Window implements InitializingBean, InternationalizableComponent {
    
    private static final long serialVersionUID = -1627453790001229325L;
    
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
    private SimpleResourceBundleMessageSource messageSource;
    
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

    public AddEntryDialog(AddEntryDialogSource source){
        this.source = source;
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
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(searchButton, Message.SEARCH_LABEL);
    }

}
