package org.generationcp.breeding.manager.listmanager.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingLocationField;
import org.generationcp.breeding.manager.customfields.BreedingLocationFieldSource;
import org.generationcp.breeding.manager.customfields.BreedingMethodField;
import org.generationcp.breeding.manager.customfields.ListDateField;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchBarComponent;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListValueChangeListener;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.constant.DefaultGermplasmStudyBrowserPath;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import javax.annotation.Resource;

@Configurable
public class AddEntryDialog extends BaseSubWindow implements InitializingBean, 
							InternationalizableComponent, BreedingManagerLayout, BreedingLocationFieldSource{

    
    private static final long serialVersionUID = -1627453790001229325L;
    
    private static final Logger LOG = LoggerFactory.getLogger(AddEntryDialog.class);
    
    public static final String OPTION_1_ID = "AddEntryDialog Option 1";
    public static final String OPTION_2_ID = "AddEntryDialog Option 2";
    public static final String OPTION_3_ID = "AddEntryDialog Option 3";
    public static final String NEXT_BUTTON_ID = "AddEntryDialog Next Button";
    public static final String CANCEL_BUTTON_ID = "AddEntryDialog Cancel Button";
    public static final String DONE_BUTTON_ID = "AddEntryDialog Done Button";
    private static final String GID = "gid";
    private static final String DEFAULT_NAME_TYPE_CODE = "LNAME";
    private static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";

	@Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;
    
    private Window parentWindow;
    private VerticalLayout topPart;
    private AbsoluteLayout bottomPart;
    private AddEntryDialogSource source;
    private OptionGroup optionGroup;
    private List<Integer> selectedGids;
    
    private BreedingMethodField breedingMethodField;
    private BreedingLocationField breedingLocationField;
    
    private Button doneButton;
    private Button cancelButton;
    
    private Label topPartHeader;
    private Label step2Label;
    
    private Label germplasmDateLabel;
    private Label nameTypeLabel;
    private Label bottomPartHeader;
    
    private ComboBox nameTypeComboBox;
    
    private ListDateField germplasmDateField;

    private GermplasmSearchBarComponent searchBarComponent;
    
    private GermplasmSearchResultsComponent searchResultsComponent;
    
    @Autowired
	private BreedingManagerService breedingManagerService;
	private String programUniqueId;

    
    public AddEntryDialog(AddEntryDialogSource source, Window parentWindow){
        this.setOverrideFocus(true);
        this.source = source;
        this.parentWindow = parentWindow;
        this.selectedGids = new ArrayList<Integer>();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    	instantiateComponents();
        initializeValues();
        addListeners();
        layoutComponents();
    }
    
	@Override
	public void instantiateComponents() {
		
		try {
			programUniqueId = breedingManagerService.getCurrentProject().getUniqueID();
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
		}
		
		initializeTopPart();	
		initializeBottomPart();
		initializeButtonLayout();
	}

	@Override
	public void initializeValues() {
		populateNameTypeComboBox();
	}

	@Override
	public void addListeners() {
		addSearchResultsListeners();
        
        addListenerToOptionGroup();
        
        cancelButton.addListener(new CloseWindowAction());
        doneButton.addListener(new GermplasmListButtonClickListener(this));
	}

	protected void addListenerToOptionGroup() {
		optionGroup.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                if(optionGroup.getValue().equals(OPTION_1_ID)){
                    setSpecifyDetailsVisible(false);
                    if(selectedGids.isEmpty()){
                        doneButton.setEnabled(false);
                    }
                } else if(optionGroup.getValue().equals(OPTION_2_ID)){
                    setSpecifyDetailsVisible(true);
                    doneButton.setEnabled(!selectedGids.isEmpty());
                } else if(optionGroup.getValue().equals(OPTION_3_ID)){
                    doneButton.setEnabled(true);
                    setSpecifyDetailsVisible(true);
                }
            }
        });
	}

	protected void addSearchResultsListeners() {
		searchResultsComponent.getMatchingGermplasmsTable().addListener(new GermplasmListValueChangeListener(this));
        searchResultsComponent.getMatchingGermplasmsTable().addListener(new GermplasmListItemClickListener(this));
	}

	@Override
	public void layoutComponents() {
		setModal(true);
		setWidth("800px");
        setResizable(false);
        setCaption(messageSource.getMessage(Message.ADD_LIST_ENTRIES));
        center();
        
        topPart = new VerticalLayout();
        topPart.setSpacing(true);
        topPart.setMargin(false);
        topPart.addComponent(topPartHeader);
        topPart.addComponent(searchBarComponent);
        topPart.addComponent(searchResultsComponent);
        topPart.addComponent(step2Label);
        topPart.addComponent(optionGroup);
        
        bottomPart = new AbsoluteLayout();
        bottomPart.setWidth("600px");
        bottomPart.setHeight("230px");
        bottomPart.addComponent(bottomPartHeader, "top:15px;left:0px");
        bottomPart.addComponent(breedingMethodField, "top:50px;left:0px");
        bottomPart.addComponent(germplasmDateLabel, "top:107px;left:0px");
        bottomPart.addComponent(germplasmDateField, "top:102px;left:124px");
        bottomPart.addComponent(breedingLocationField, "top:133px;left:0px");
        bottomPart.addComponent(nameTypeLabel, "top:185px;left:0px");
        bottomPart.addComponent(nameTypeComboBox, "top:185px;left:130px");
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidth("100%");
        buttonLayout.setHeight("50px");
        buttonLayout.setSpacing(true);
        buttonLayout.addComponent(cancelButton);
        buttonLayout.addComponent(doneButton);
        buttonLayout.setComponentAlignment(cancelButton, Alignment.BOTTOM_RIGHT);
        buttonLayout.setComponentAlignment(doneButton, Alignment.BOTTOM_LEFT);
        
        addComponent(topPart);
        addComponent(bottomPart);
        addComponent(buttonLayout);
        
        setSpecifyDetailsVisible(false);
	}
    
    public void resultTableItemClickAction(Table sourceTable) {
    	
    	this.selectedGids = getSelectedItemIds(sourceTable);
    	
    	if(!selectedGids.isEmpty()){
    		this.doneButton.setEnabled(true);
    	} else {
    		this.doneButton.setEnabled(false);    		
    	}
    }
    
    public void resultTableValueChangeAction() {
    	this.selectedGids = getSelectedItemIds(searchResultsComponent.getMatchingGermplasmsTable());
    	if(doneButton!=null){
	    	if(!selectedGids.isEmpty()){
	    		this.doneButton.setEnabled(true);
	    	} else {
	    		this.doneButton.setEnabled(false);    		
	    	}
    	}
    }    
    
    public void resultTableItemDoubleClickAction(Table sourceTable, Object itemId, Item item) {
        sourceTable.select(itemId);
        int gid = Integer.valueOf(item.getItemProperty(GID).getValue().toString());
        
        Tool tool = null;
        try {
            tool = workbenchDataManager.getToolWithName(ToolName.germplasm_browser.toString());
        } catch (MiddlewareQueryException qe) {
            LOG.error(messageSource.getMessage(Message.QUERY_EXCEPTION), qe);
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
        
        Window germplasmWindow = new Window(messageSource.getMessage(Message.GERMPLASM_INFORMATION) + " - " + gid);
        
        VerticalLayout layoutForGermplasm = new VerticalLayout();
        layoutForGermplasm.setMargin(false);
        layoutForGermplasm.setWidth("98%");
        layoutForGermplasm.setHeight("98%");
        
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
        
        this.parentWindow.addWindow(germplasmWindow);
    }
    

    protected void initializeTopPart(){
        topPart = new VerticalLayout();
        topPart.setSpacing(true);
        topPart.setMargin(false);
        
        topPartHeader = new Label(messageSource.getMessage(Message.SELECT_A_GERMPLASM));
        topPartHeader.addStyleName("bold");
        topPartHeader.addStyleName("h3");
        
        searchResultsComponent = new GermplasmSearchResultsComponent(null, false, false);
        searchResultsComponent.getMatchingGermplasmsTable().setHeight("150px");
        searchResultsComponent.getMatchingGermplasmsTableWithSelectAll().setHeight("180px");
        searchResultsComponent.setRightClickActionHandlerEnabled(false);
        
        searchBarComponent = new GermplasmSearchBarComponent(searchResultsComponent);
        
        step2Label = new Label(messageSource.getMessage(Message.HOW_DO_YOU_WANT_TO_ADD_THE_GERMPLASM_TO_THE_LIST));
        step2Label.addStyleName("bold");
        
        optionGroup = new OptionGroup();
        optionGroup.addItem(OPTION_1_ID);
        optionGroup.setItemCaption(OPTION_1_ID, messageSource.getMessage(Message.USE_SELECTED_GERMPLASM_FOR_THE_LIST_ENTRY));
        optionGroup.addItem(OPTION_2_ID);
        optionGroup.setItemCaption(OPTION_2_ID, messageSource.getMessage(Message.CREATE_A_NEW_GERMPLASM_RECORD_FOR_THE_LIST_ENTRY_AND_ASSIGN_THE_SELECTED_GERMPLASM_AS_ITS_SOURCE));
        optionGroup.addItem(OPTION_3_ID);
        optionGroup.setItemCaption(OPTION_3_ID, messageSource.getMessage(Message.CREATE_A_NEW_GERMPLASM_RECORD_FOR_THE_LIST_ENTRY));
        optionGroup.select(OPTION_1_ID);
        optionGroup.setImmediate(true);
    }
    
    protected void initializeBottomPart(){
        bottomPart = new AbsoluteLayout();
        bottomPart.setWidth("600px");
        bottomPart.setHeight("230px");
        
        bottomPartHeader = new Label(messageSource.getMessage(Message.SPECIFY_ADDITIONAL_DETAILS));
        bottomPartHeader.addStyleName("bold");
        bottomPartHeader.addStyleName("h3");
        
        breedingMethodField = new BreedingMethodField(parentWindow);
                
        germplasmDateLabel = new Label("Creation Date: ");
        germplasmDateLabel.addStyleName("bold");
        
        germplasmDateField =  new ListDateField("", false);
        germplasmDateField.getListDtDateField().setValue(new Date());
        
        breedingLocationField = new BreedingLocationField(this, parentWindow);
        
        nameTypeLabel = new Label("Name Type: ");
        nameTypeLabel.addStyleName("bold");
        
        nameTypeComboBox = new ComboBox();
        nameTypeComboBox.setWidth("400px");
    }
    
    
    public void initializeButtonLayout(){
        cancelButton = new Button(messageSource.getMessage(Message.CANCEL));
        cancelButton.setData(CANCEL_BUTTON_ID);
        
        doneButton = new Button(messageSource.getMessage(Message.DONE));
        doneButton.setData(DONE_BUTTON_ID);
        doneButton.setEnabled(false);
        doneButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
    }

    
    public void nextButtonClickAction(ClickEvent event){
    	if (optionGroup.getValue().equals(OPTION_1_ID)) {
			// add the germplasm selected as the list entry
			validateInputForOption1(event);
		} else if (optionGroup.getValue().equals(OPTION_2_ID)) {
			validateInputForOption2(event);
		} else if (optionGroup.getValue().equals(OPTION_3_ID)) {
			validateInputForOption3(event);
		}
    }
    
    private void validateInputForOption1(ClickEvent event) {
    	if(!selectedGids.isEmpty()){
            this.source.finishAddingEntry(selectedGids);
            Window window = event.getButton().getWindow();
            window.getParent().removeWindow(window);
        } else {
            MessageNotifier.showWarning(this, messageSource.getMessage(Message.WARNING), 
                    messageSource.getMessage(Message.YOU_MUST_SELECT_A_GERMPLASM_FROM_THE_SEARCH_RESULTS));
        }
	}
    
    private void validateInputForOption2(ClickEvent event) {
    	if(breedingMethodField.getBreedingMethodComboBox().getValue() == null){
    		MessageNotifier.showRequiredFieldError(this, messageSource.getMessage(Message.YOU_MUST_SELECT_A_METHOD_FOR_THE_GERMPLASM));
    	} else if(breedingLocationField.getBreedingLocationComboBox().getValue() == null){
    		MessageNotifier.showRequiredFieldError(this, messageSource.getMessage(Message.YOU_MUST_SELECT_A_LOCATION_FOR_THE_GERMPLASM));
    	}else if(!selectedGids.isEmpty()){
        	if(doneAction()){
        		Window window = event.getButton().getWindow();
        		window.getParent().removeWindow(window);
        	}
        } else{
            MessageNotifier.showWarning(this, messageSource.getMessage(Message.WARNING), 
                    messageSource.getMessage(Message.YOU_MUST_SELECT_A_GERMPLASM_FROM_THE_SEARCH_RESULTS));
        }
	}
    
    private void validateInputForOption3(ClickEvent event) {
    	String searchValue = searchBarComponent.getSearchField().getValue().toString();
    	if(breedingMethodField.getBreedingMethodComboBox().getValue() == null){
    		MessageNotifier.showRequiredFieldError(this, messageSource.getMessage(Message.YOU_MUST_SELECT_A_METHOD_FOR_THE_GERMPLASM));
    	} else if(breedingLocationField.getBreedingLocationComboBox().getValue() == null){
    		MessageNotifier.showRequiredFieldError(this, messageSource.getMessage(Message.YOU_MUST_SELECT_A_LOCATION_FOR_THE_GERMPLASM));
    	} else if(searchValue != null && searchValue.length() != 0){
        	doneAction();
        	Window window = event.getButton().getWindow();
        	window.getParent().removeWindow(window);
        } else {
        	MessageNotifier.showRequiredFieldError(this, messageSource.getMessage(Message.YOU_MUST_ENTER_A_GERMPLASM_NAME_IN_THE_TEXTBOX));
        }
    }
    
    public Boolean doneAction(){
    	if (!this.optionGroup.getValue().equals(OPTION_2_ID)
				&& !this.optionGroup.getValue().equals(OPTION_3_ID)) {
			return false;
		}
    	if (this.optionGroup.getValue().equals(OPTION_2_ID)) {
			List<Integer> addedGids = new ArrayList<Integer>();
			for (Integer selectedGid : selectedGids) {
				Integer newlyCreatedGid = addGermplasm(selectedGid);
				if(newlyCreatedGid==null) {
					return false;
				}
				addedGids.add(newlyCreatedGid);
			}
			this.source.finishAddingEntry(addedGids);
		} else {
			Integer newlyCreatedGid = addGermplasm(null);
			if(newlyCreatedGid==null) {
				return false;
			}
			this.source.finishAddingEntry(newlyCreatedGid);
		}
		return true;
    }
    
    private Integer addGermplasm(Integer selectedGid) {
    	Integer breedingMethodId = (Integer) this.breedingMethodField.getBreedingMethodComboBox().getValue();
        Integer nameTypeId = (Integer) this.nameTypeComboBox.getValue();
        Integer locationId = (Integer) this.breedingLocationField.getBreedingLocationComboBox().getValue();

		Integer date = getGermplasmDate();
		if (date == null) {
			return null;
		}

		Integer currentUserLocalId = getCurrentUserLocalId();
		Germplasm selectedGermplasm = getSelectedGermplasm(selectedGid);
		String germplasmName = searchBarComponent.getSearchField().getValue()
				.toString();
		if (selectedGermplasm!=null && selectedGermplasm.getPreferredName() != null) {
			germplasmName = selectedGermplasm.getPreferredName().getNval();
		}
		
		Germplasm germplasm = createGermplasm(selectedGermplasm, date,
				locationId, breedingMethodId, currentUserLocalId);
		Name name = createName(germplasmName, locationId, date,
				nameTypeId, currentUserLocalId);
		return saveGermplasmToDatabase(germplasm, name);
	}
    
    private Integer getGermplasmDate() {
    	Date dateOfCreation = (Date) this.germplasmDateField.getValue();
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_AS_NUMBER_FORMAT);
        
        if(dateOfCreation==null){
            LOG.error("Invalid date on add list entries! - " + dateOfCreation);
            MessageNotifier.showRequiredFieldError(getWindow(), messageSource.getMessage(Message.VALIDATION_DATE_FORMAT));
            return null;
        }
        String parsedDate = formatter.format(dateOfCreation);
        if(parsedDate==null){
            LOG.error("Invalid date on add list entries! - " + parsedDate);
            MessageNotifier.showRequiredFieldError(getWindow(), messageSource.getMessage(Message.VALIDATION_DATE_FORMAT));
            return null;
        }
		return Integer.parseInt(parsedDate);
	}
    
    private Integer getCurrentUserLocalId() {
		Integer currentUserLocalId = -1;
		try {
			currentUserLocalId = contextUtil.getCurrentUserLocalId();
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(), e);
		}
		return currentUserLocalId;
	}
    
    private Germplasm getSelectedGermplasm(Integer selectedGid) {
		if (selectedGid == null) {
			return null;
		}
		try{
            return this.germplasmDataManager.getGermplasmWithPrefName(selectedGid);
        } catch(MiddlewareQueryException mex){
            LOG.error("Error with getting germplasm with id: " + selectedGid, mex);
            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_WITH_GETTING_GERMPLASM_WITH_ID)+": " + selectedGid 
                    +". "+messageSource.getMessage(Message.ERROR_REPORT_TO));
        }
		return null;
	}
    
    private Germplasm createGermplasm(Germplasm selectedGermplasm, Integer date,
			Integer locationId, Integer breedingMethodId,
			Integer currentUserLocalId) {

		Germplasm germplasm = new Germplasm();
        germplasm.setGdate(date);
        germplasm.setGnpgs(Integer.valueOf(-1));
        germplasm.setGpid1(Integer.valueOf(0));
        germplasm.setGpid2(Integer.valueOf(0));
        germplasm.setGrplce(Integer.valueOf(0));
        germplasm.setLgid(Integer.valueOf(0));
        germplasm.setLocationId(locationId);
        germplasm.setMethodId(breedingMethodId);
        germplasm.setMgid(Integer.valueOf(0));
        germplasm.setReferenceId(Integer.valueOf(0));
        germplasm.setUserId(currentUserLocalId);
        
        if (selectedGermplasm != null) {
			if (selectedGermplasm.getGnpgs() < 2) {
				germplasm.setGpid1(selectedGermplasm.getGpid1());
			} else {
				germplasm.setGpid1(selectedGermplasm.getGid());
			}
			germplasm.setGpid2(selectedGermplasm.getGid());
		}
		
		return germplasm;
	} 
    
    private Name createName(String germplasmName, Integer locationId,
			Integer date, Integer nameTypeId, Integer currentUserLocalId) {
		Name name = new Name();
		name.setNval(germplasmName);
		name.setLocationId(locationId);
		name.setNdate(date);
		name.setNstat(Integer.valueOf(1));
		name.setReferenceId(Integer.valueOf(0));
		name.setTypeId(nameTypeId);
		name.setUserId(currentUserLocalId);
		return name;
	}
    
    private Integer saveGermplasmToDatabase(Germplasm germplasm, Name name) {
		try {
			this.germplasmDataManager.addGermplasm(germplasm, name);
			return germplasm.getGid();
		} catch (MiddlewareQueryException ex) {
			LOG.error("Error with saving germplasm and name records!", ex);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_WITH_SAVING_GERMPLASM_AND_NAME_RECORDS) + messageSource.getMessage(Message.ERROR_REPORT_TO));
            return null;
		}
	}
	        
    private void populateNameTypeComboBox(){
        try{
            List<UserDefinedField> nameTypes = this.germplasmListManager.getGermplasmNameTypes();
            for(UserDefinedField nameType : nameTypes){
                Integer nameTypeId = nameType.getFldno();
                String nameTypeString = nameType.getFname();
                String nameTypeCode = nameType.getFcode();
                this.nameTypeComboBox.addItem(nameTypeId);
                this.nameTypeComboBox.setItemCaption(nameTypeId, nameTypeString);
                if(nameTypeCode.equals(DEFAULT_NAME_TYPE_CODE)){
                    this.nameTypeComboBox.select(nameTypeId);
                }
            }
        } catch (MiddlewareQueryException ex){
            LOG.error("Error with getting germplasm name types!", ex);
            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_WITH_GETTING_GERMPLASM_NAME_TYPES) + messageSource.getMessage(Message.ERROR_REPORT_TO));
            Integer unknownId = Integer.valueOf(0);
            this.nameTypeComboBox.addItem(unknownId);
            this.nameTypeComboBox.setItemCaption(unknownId, messageSource.getMessage(Message.UNKNOWN));
        }
        this.nameTypeComboBox.setNullSelectionAllowed(false);
    }
    
    @Override
    public void updateLabels() {
    	//do nothing
    }

    /**
     * Iterates through the whole table, gets selected item ID's, make sure it's sorted as seen on the UI
     */
    @SuppressWarnings("unchecked")
    private List<Integer> getSelectedItemIds(Table table){
        List<Integer> itemIds = new ArrayList<Integer>();
        List<Integer> selectedItemIds = new ArrayList<Integer>();
        List<Integer> trueOrderedSelectedItemIds = new ArrayList<Integer>();
        
        selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
        itemIds = getItemIds(table);
            
        for(Integer itemId: itemIds){
            if(selectedItemIds.contains(itemId)){
                trueOrderedSelectedItemIds.add(itemId);
            }
        }
        
        return trueOrderedSelectedItemIds;
    }    

    /**
     * Get item id's of a table, and return it as a list 
     * @param table
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Integer> getItemIds(Table table){
        List<Integer> itemIds = new ArrayList<Integer>();
        itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
        return itemIds;
    }    
    
    
    private void setSpecifyDetailsVisible(Boolean visible){
        int height = 530;
    	if(visible){
    		// add height of bottom part + margin
    	    height += 230 + 10;
    		setHeight(height + "px");
    		bottomPart.setVisible(true);
    		center();
    	} else {
    		setHeight(height + "px");
    		bottomPart.setVisible(false);
    		center();
    	}
    }
    
    public void focusOnSearchField(){
        searchBarComponent.getSearchField().focus();
    }



	@Override
	public void updateAllLocationFields() {
		if(breedingLocationField!=null && breedingLocationField.getBreedingLocationComboBox()!=null &&
				breedingLocationField.getBreedingLocationComboBox().getValue()!=null) {
			Object lastValue = breedingLocationField.getBreedingLocationComboBox().getValue();
			breedingLocationField.populateHarvestLocation(Integer.valueOf(lastValue.toString()),programUniqueId);
		}
	}
	
	public void setOptionGroup(OptionGroup optionGroup){
		this.optionGroup = optionGroup;
	}
	
	public void setSelectedGids(List<Integer> selectedGids){
		this.selectedGids = selectedGids;
	}
	
	public Button getDoneButton(){
		return this.doneButton;
	}
	
	public Button getCancelButton(){
		return this.cancelButton;
	}
	
	public void setMessageSource(SimpleResourceBundleMessageSource msgSource){
		this.messageSource = msgSource;
	}

	public void setBreedingManagerService(BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}
}
