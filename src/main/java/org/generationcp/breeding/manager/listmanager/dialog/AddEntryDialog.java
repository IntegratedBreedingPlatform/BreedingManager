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
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
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
import org.generationcp.middleware.pojos.workbench.Project;
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

@Configurable
public class AddEntryDialog extends BaseSubWindow implements InitializingBean, 
							InternationalizableComponent, BreedingManagerLayout, BreedingLocationFieldSource{

    
    private static final long serialVersionUID = -1627453790001229325L;
    
    private final static Logger LOG = LoggerFactory.getLogger(AddEntryDialog.class);
    
    public static final String OPTION_1_ID = "AddEntryDialog Option 1";
    public static final String OPTION_2_ID = "AddEntryDialog Option 2";
    public static final String OPTION_3_ID = "AddEntryDialog Option 3";
    public static final String NEXT_BUTTON_ID = "AddEntryDialog Next Button";
    public static final String CANCEL_BUTTON_ID = "AddEntryDialog Cancel Button";
    public static final String BACK_BUTTON_ID = "AddEntryDialog Back Button";
    public static final String DONE_BUTTON_ID = "AddEntryDialog Done Button";
    private static final String GID = "gid";
    private static final String DEFAULT_NAME_TYPE_CODE = "LNAME";
    private static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";

	public static final String GERMPLASM_BROWSER_LINK = "http://localhost:18080/GermplasmStudyBrowser/main/germplasm-";
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
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
    
    public AddEntryDialog(AddEntryDialogSource source, Window parentWindow){
        this.source = source;
        this.parentWindow = parentWindow;
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
		searchResultsComponent.getMatchingGermplasmsTable().addListener(new GermplasmListValueChangeListener(this));
        searchResultsComponent.getMatchingGermplasmsTable().addListener(new GermplasmListItemClickListener(this));
        
        optionGroup.addListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                if(optionGroup.getValue().equals(OPTION_1_ID)){
                    setSpecifyDetailsVisible(false);
                    if(selectedGids.size()==0){
                        doneButton.setEnabled(false);
                    }
                } else if(optionGroup.getValue().equals(OPTION_2_ID)){
                    setSpecifyDetailsVisible(true);
                    if(selectedGids.size()==0){
                        doneButton.setEnabled(false);
                    } else {
                        doneButton.setEnabled(true);
                    }
                } else if(optionGroup.getValue().equals(OPTION_3_ID)){
                    doneButton.setEnabled(true);
                    setSpecifyDetailsVisible(true);
                }
            }
        });
        
        cancelButton.addListener(new CloseWindowAction());
        doneButton.addListener(new GermplasmListButtonClickListener(this));
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
    
    public void resultTableItemClickAction(Table sourceTable) throws InternationalizableException {
    	
    	this.selectedGids = getSelectedItemIds(sourceTable);
    	
    	if(selectedGids.size()>0){
    		this.doneButton.setEnabled(true);
    	} else {
    		this.doneButton.setEnabled(false);    		
    	}
    }
    
    public void resultTableValueChangeAction() throws InternationalizableException {
    	this.selectedGids = getSelectedItemIds(searchResultsComponent.getMatchingGermplasmsTable());
    	if(doneButton!=null){
	    	if(selectedGids.size()>0){
	    		this.doneButton.setEnabled(true);
	    	} else {
	    		this.doneButton.setEnabled(false);    		
	    	}
    	}
    }    
    
    public void resultTableItemDoubleClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        sourceTable.select(itemId);
        int gid = Integer.valueOf(item.getItemProperty(GID).getValue().toString());
        
        Tool tool = null;
        try {
            tool = workbenchDataManager.getToolWithName(ToolName.germplasm_browser.toString());
        } catch (MiddlewareQueryException qe) {
            LOG.error(messageSource.getMessage(Message.QUERY_EXCEPTION), qe);
        }
        
        String addtlParams = Util.getAdditionalParams(workbenchDataManager);
        
        ExternalResource germplasmBrowserLink = null;
        if (tool == null) {
            germplasmBrowserLink = new ExternalResource(GERMPLASM_BROWSER_LINK + gid+ "?restartApplication"+
            		addtlParams);
        } else {
            germplasmBrowserLink = new ExternalResource(tool.getPath().replace("germplasm/", "germplasm-") + gid+ "?restartApplication"+
            		addtlParams);
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
        //germplasmWindow.setWidth("645px");
        //germplasmWindow.setHeight("600px");
        germplasmWindow.setWidth("90%");
        germplasmWindow.setHeight("90%");
        germplasmWindow.center();
        germplasmWindow.setResizable(false);
        
        germplasmWindow.setModal(true);
        
        this.parentWindow.addWindow(germplasmWindow);
    }
    

    private void initializeTopPart(){
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
    
    private void initializeBottomPart(){
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
        cancelButton.addListener(new CloseWindowAction());
        
        doneButton = new Button(messageSource.getMessage(Message.DONE));
        doneButton.setData(DONE_BUTTON_ID);
        doneButton.addListener(new GermplasmListButtonClickListener(this));
        doneButton.setEnabled(false);
        doneButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
    }

    
    public void nextButtonClickAction(ClickEvent event){
    	
        if(optionGroup.getValue().equals(OPTION_1_ID)){
            // add the germplasm selected as the list entry
            if(this.selectedGids.size()>0){
                this.source.finishAddingEntry(selectedGids);
                Window window = event.getButton().getWindow();
                window.getParent().removeWindow(window);
            } else {
                MessageNotifier.showWarning(this, messageSource.getMessage(Message.WARNING), 
                        messageSource.getMessage(Message.YOU_MUST_SELECT_A_GERMPLASM_FROM_THE_SEARCH_RESULTS));
            }
        } else if(optionGroup.getValue().equals(OPTION_2_ID)){
        	if(breedingMethodField.getBreedingMethodComboBox().getValue() == null){
        		MessageNotifier.showError(this, messageSource.getMessage(Message.ERROR), 
                       messageSource.getMessage(Message.YOU_MUST_SELECT_A_METHOD_FOR_THE_GERMPLASM));
        	} else if(breedingLocationField.getBreedingLocationComboBox().getValue() == null){
            		MessageNotifier.showError(this, messageSource.getMessage(Message.ERROR), 
                            messageSource.getMessage(Message.YOU_MUST_SELECT_A_LOCATION_FOR_THE_GERMPLASM));
        	}else if(this.selectedGids.size()>0){
            	if(doneAction()){
            		Window window = event.getButton().getWindow();
            		window.getParent().removeWindow(window);
            	}
            } else{
                MessageNotifier.showWarning(this, messageSource.getMessage(Message.WARNING), 
                        messageSource.getMessage(Message.YOU_MUST_SELECT_A_GERMPLASM_FROM_THE_SEARCH_RESULTS));
            }
        } else if(optionGroup.getValue().equals(OPTION_3_ID)){
            String searchValue = searchBarComponent.getSearchField().getValue().toString();
            
        	if(breedingMethodField.getBreedingMethodComboBox().getValue() == null){
        		MessageNotifier.showError(this, messageSource.getMessage(Message.ERROR), 
        				messageSource.getMessage(Message.YOU_MUST_SELECT_A_METHOD_FOR_THE_GERMPLASM));
        	} else if(breedingLocationField.getBreedingLocationComboBox().getValue() == null){
            		MessageNotifier.showError(this, messageSource.getMessage(Message.ERROR), 
            				messageSource.getMessage(Message.YOU_MUST_SELECT_A_LOCATION_FOR_THE_GERMPLASM));
        	} else if(searchValue != null && searchValue.length() != 0){
            	doneAction();
            	Window window = event.getButton().getWindow();
            	window.getParent().removeWindow(window);
            } else {
                MessageNotifier.showWarning(this, messageSource.getMessage(Message.WARNING), 
                		messageSource.getMessage(Message.YOU_MUST_ENTER_A_GERMPLASM_NAME_IN_THE_TEXTBOX));
            }
        }
    }
    
    public void backButtonClickAction(){
    }
    
    public Boolean doneAction(){
    	
        if(this.optionGroup.getValue().equals(OPTION_2_ID) || this.optionGroup.getValue().equals(OPTION_3_ID)){
        	
	        Integer breedingMethodId = (Integer) this.breedingMethodField.getBreedingMethodComboBox().getValue();
	        Integer nameTypeId = (Integer) this.nameTypeComboBox.getValue();
	        Integer locationId = (Integer) this.breedingLocationField.getBreedingLocationComboBox().getValue();
	        Date dateOfCreation = (Date) this.germplasmDateField.getValue();
	        SimpleDateFormat formatter = new SimpleDateFormat(DATE_AS_NUMBER_FORMAT);
	        
	        if(dateOfCreation==null){
	            LOG.error("Invalid date on add list entries! - " + dateOfCreation);
	            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR), messageSource.getMessage(Message.VALIDATION_DATE_FORMAT));
	            return false;
	        }
	        String parsedDate = formatter.format(dateOfCreation);
	        if(parsedDate==null){
	            LOG.error("Invalid date on add list entries! - " + parsedDate);
	            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR), messageSource.getMessage(Message.VALIDATION_DATE_FORMAT));
	            return false;
	        }
	        
	        Integer date = Integer.parseInt(parsedDate);
            String germplasmName = searchBarComponent.getSearchField().getValue().toString();
	        
	        if(this.optionGroup.getValue().equals(OPTION_2_ID)){
		        
	        	List<Integer> addedGids = new ArrayList<Integer>();
	        	
		        for(Integer selectedGid : selectedGids){
		        
		        	Germplasm selectedGermplasm = null;
		        	
	                try{
	                    selectedGermplasm = this.germplasmDataManager.getGermplasmWithPrefName(selectedGid);
	                    if(selectedGermplasm.getPreferredName() != null){
	                        germplasmName = selectedGermplasm.getPreferredName().getNval();
	                    }
	                } catch(MiddlewareQueryException mex){
	                    LOG.error("Error with getting germplasm with id: " + selectedGid, mex);
	                    MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_WITH_GETTING_GERMPLASM_WITH_ID)+": " + selectedGid 
	                            +". "+messageSource.getMessage(Message.ERROR_REPORT_TO));
	                }
			        
			        
			        Integer userId = Integer.valueOf(getCurrentUserLocalId());
			                
			        Germplasm germplasm = new Germplasm();
			        germplasm.setGdate(date);
			        germplasm.setGnpgs(Integer.valueOf(-1));
			        germplasm.setGpid1(Integer.valueOf(0));
			        germplasm.setGrplce(Integer.valueOf(0));
			        germplasm.setLgid(Integer.valueOf(0));
			        germplasm.setLocationId(locationId);
			        germplasm.setMethodId(breedingMethodId);
			        germplasm.setMgid(Integer.valueOf(0));
			        germplasm.setReferenceId(Integer.valueOf(0));
			        germplasm.setUserId(userId);
			         
		        	if(selectedGermplasm != null){
		                if(selectedGermplasm.getGnpgs()<2){
		                	germplasm.setGpid1(selectedGermplasm.getGpid1());
		                } else {
		                	germplasm.setGpid1(selectedGermplasm.getGid());                            	
		                }
		        	}
		            germplasm.setGpid2(selectedGid);
			        
			        Name name = new Name();
			        name.setNval(germplasmName);
			        name.setLocationId(locationId);
			        name.setNdate(date);
			        name.setNstat(Integer.valueOf(1));
			        name.setReferenceId(Integer.valueOf(0));
			        name.setTypeId(nameTypeId);
			        name.setUserId(userId);
			        
			        try{
			            @SuppressWarnings("unused")
						Integer gid = this.germplasmDataManager.addGermplasm(germplasm, name);
			            addedGids.add(germplasm.getGid());
			        } catch(MiddlewareQueryException ex){
			            LOG.error("Error with saving germplasm and name records!", ex);
			            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_WITH_SAVING_GERMPLASM_AND_NAME_RECORDS) + messageSource.getMessage(Message.ERROR_REPORT_TO));
			            return false;
			        }
	        	}

		        this.source.finishAddingEntry(addedGids);
		        
		        return true;
		        
	        } else {
		        Integer userId = Integer.valueOf(getCurrentUserLocalId());
                
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
		        germplasm.setUserId(userId);
	        	
		        Name name = new Name();
		        name.setNval(germplasmName);
		        name.setLocationId(locationId);
		        name.setNdate(date);
		        name.setNstat(Integer.valueOf(1));
		        name.setReferenceId(Integer.valueOf(0));
		        name.setTypeId(nameTypeId);
		        name.setUserId(userId);
		        
		        try{
		            Integer gid = this.germplasmDataManager.addGermplasm(germplasm, name);
		            this.source.finishAddingEntry(gid);
		            return true;
		        } catch(MiddlewareQueryException ex){
		            LOG.error("Error with saving germplasm and name records!", ex);
		            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), messageSource.getMessage(Message.ERROR_WITH_SAVING_GERMPLASM_AND_NAME_RECORDS) + messageSource.getMessage(Message.ERROR_REPORT_TO));
		            return false;
		        }
	        }
        
    	}
        
        return false;
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
    
    private int getCurrentUserLocalId(){
        try{
            Integer workbenchUserId = this.workbenchDataManager.getWorkbenchRuntimeData().getUserId();
            Project lastProject = this.workbenchDataManager.getLastOpenedProject(workbenchUserId);
            Integer localIbdbUserId = this.workbenchDataManager.getLocalIbdbUserId(workbenchUserId,lastProject.getProjectId());
            if (localIbdbUserId != null) {
                return localIbdbUserId;
            } else {
                return -1;
            }
       } catch(MiddlewareQueryException ex){
           LOG.error("Error with getting local IBDB user!", ex);
               MessageNotifier.showError(getWindow(), "Database Error!", messageSource.getMessage(Message.ERROR_REPORT_TO));
           return -1;
       }
    }
    
    @Override
    public void updateLabels() {
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
    	    height += 230 + 10; // add height of bottom part + margin
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
		Object lastValue = breedingLocationField.getBreedingLocationComboBox().getValue();
		breedingLocationField.populateHarvestLocation(Integer.valueOf(lastValue.toString()));
	}
    
}
