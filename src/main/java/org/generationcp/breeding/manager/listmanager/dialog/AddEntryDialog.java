package org.generationcp.breeding.manager.listmanager.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListValueChangeListener;
import org.generationcp.breeding.manager.listmanager.util.germplasm.GermplasmSearchQuery;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
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
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

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
    public static final String DONE_BUTTON_ID = "AddEntryDialog Done Button";
    private static final String GID = "gid";
    private static final String DEFAULT_METHOD_CODE = "UDM";
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
    private VerticalLayout firstTabLayout;
    private AbsoluteLayout secondTabLayout;
    private AddEntryDialogSource source;
    private OptionGroup optionGroup;
    private List<Integer> selectedGids;
    
	private static final String GUIDE = 
	        "You may search for germplasms using GID's, germplasm names (partial/full)" +
	        " <br/><br/><b>Search results would contain</b> <br/>" +
	        "  - Germplasms with matching GID's <br/>" +
	        "  - Germplasms with name containing search query <br/>" +
	        " <br/><br/>The <b>Exact matches only</b> checkbox allows you search using partial names (when unchecked)" +
	        " or to only return results which match the query exactly (when checked).";
	private Label searchLabel;
	private TextField searchField;
	private Button searchButton;
    private CheckBox likeOrEqualCheckBox;
    private PopupView popup;
    
    private TableWithSelectAllLayout resultsTable;
    
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
    

    private Germplasm selectedGermplasm;

    public AddEntryDialog(AddEntryDialogSource source, Window parentWindow){
        this.source = source;
        this.parentWindow = parentWindow;
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
    	String searchQuery = searchField.getValue().toString();
    	if(searchQuery==null || searchQuery.equals("")){
    		MessageNotifier.showError(getWindow(), "Error", "You must type in a search query before doing a search" , Notification.POSITION_CENTERED);
    		return;
    	}
    	try {
    		List<Germplasm> germplasms = germplasmDataManager.searchForGermplasm(searchQuery, (((Boolean) likeOrEqualCheckBox.getValue()).equals(true) ? Operation.EQUAL : Operation.LIKE), false);
    	
    		resultsTable.getTable().removeAllItems();
    		
            for(final Germplasm germplasm : germplasms){
            	
            	Item newItem = resultsTable.getTable().addItem(germplasm.getGid()); 
            	
                CheckBox tagCheckBox = new CheckBox();
                tagCheckBox.setImmediate(true);
                tagCheckBox.addListener(new ClickListener() {
    	 			private static final long serialVersionUID = 1L;
    	 			@Override
    	 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
    	 				CheckBox itemCheckBox = (CheckBox) event.getButton();
    	 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
    	 					resultsTable.getTable().select(germplasm.getGid());
    	 				} else {
    	 					resultsTable.getTable().unselect(germplasm.getGid());
    	 				}
    	 			}
    	 			 
    	 		});
            	
                Button gidButton = new Button(String.format("%s", germplasm.getGid()), new GidLinkButtonClickListener(germplasm.getGid().toString(), true));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                
	            List<Name> names = germplasmDataManager.getNamesByGID(new Integer(germplasm.getGid()), null, null);
	            StringBuffer germplasmNames = new StringBuffer("");
	            int i = 0;
	            for (Name n : names) {
	                if (i < names.size() - 1) {
	                    germplasmNames.append(n.getNval() + ",");
	                } else {
	                    germplasmNames.append(n.getNval());
	                }
	                i++;
	            }
	            
	            String methodName = "-";
	            Method germplasmMethod = germplasmDataManager.getMethodByID(germplasm.getMethodId());
	            if(germplasmMethod!=null && germplasmMethod.getMname()!=null){
	            	methodName = germplasmMethod.getMname();
	            }
	            
	            String locationName = "-";
	            Location germplasmLocation = germplasmDataManager.getLocationByID(germplasm.getLocationId());
	            if(germplasmLocation!=null && germplasmLocation.getLname()!=null){
	            	locationName = germplasmLocation.getLname();
	            }
	            
	            newItem.getItemProperty(ListDataTablePropertyID.TAG.getName()).setValue(tagCheckBox);
                newItem.getItemProperty(GermplasmSearchQuery.GID).setValue(germplasm.getGid());
                newItem.getItemProperty(GermplasmSearchQuery.NAMES).setValue(germplasmNames);
                newItem.getItemProperty(GermplasmSearchQuery.METHOD).setValue(methodName);
                newItem.getItemProperty(GermplasmSearchQuery.LOCATION).setValue(locationName);
            
            }
            
    	} catch (MiddlewareQueryException e) {
    		MessageNotifier.showError(getWindow(), "Database Error", "Error while performing search" , Notification.POSITION_CENTERED);
    		LOG.error("Error while performing search on add entry: q="+searchQuery, e);
    	}
    }
    
    public void resultTableItemClickAction(Table sourceTable) throws InternationalizableException {
    	
    	this.selectedGids = getSelectedItemIds(sourceTable);
    	
    	if(selectedGids.size()>0){
    		this.nextButton.setEnabled(true);
    	} else {
    		this.nextButton.setEnabled(false);    		
    	}
    }
    
    public void resultTableValueChangeAction() throws InternationalizableException {
    	this.selectedGids = getSelectedItemIds(resultsTable.getTable());
    	if(nextButton!=null){
	    	if(selectedGids.size()>0){
	    		this.nextButton.setEnabled(true);
	    	} else {
	    		this.nextButton.setEnabled(false);    		
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
        germplasmWindow.setWidth("90%");
        germplasmWindow.setHeight("90%");
        germplasmWindow.center();
        germplasmWindow.setResizable(false);
        
        germplasmWindow.setModal(true);
        
        this.parentWindow.addWindow(germplasmWindow);
    }
    
    private void assembleFirstTab(){
        firstTabLayout = new VerticalLayout();
        firstTabLayout.setSpacing(true);
        firstTabLayout.setMargin(true);
        
        Label step1Label = new Label("1. Search for Germplasm Record to add as List Entry.  Double click on a row in the result table to view germplasm details.");
        firstTabLayout.addComponent(step1Label);
        
        AbsoluteLayout searchFormLayout = new AbsoluteLayout();
        searchFormLayout.setHeight("45px");
        searchFormLayout.setWidth("100%");
                
        searchLabel = new Label();
        searchLabel.setValue(messageSource.getMessage(Message.SEARCH_FOR)+": ");
        searchLabel.setWidth("200px");
        searchLabel.addStyleName("bold");
        
        searchField = new TextField();
        searchField.setImmediate(true);
        
        searchButton = new Button(messageSource.getMessage(Message.SEARCH));
        searchButton.setHeight("24px");
        searchButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        searchButton.setData(SEARCH_BUTTON_ID);
        searchButton.setClickShortcut(KeyCode.ENTER);
        searchButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				searchButtonClickAction();				
			}
        	
        });

        Label descLbl = new Label(GUIDE, Label.CONTENT_XHTML);
        descLbl.setWidth("300px");
        popup = new PopupView(" ? ",descLbl);
        popup.setStyleName("gcp-popup-view");
        
        likeOrEqualCheckBox = new CheckBox();
        likeOrEqualCheckBox.setCaption(messageSource.getMessage(Message.EXACT_MATCHES_ONLY));
        
        searchFormLayout.addComponent(searchLabel, "top:15px; left:15px");
        searchFormLayout.addComponent(searchField, "top:12px; left:100px");
        searchFormLayout.addComponent(searchButton, "top:12px; left:265px");
        searchFormLayout.addComponent(likeOrEqualCheckBox, "top:15px; left:350px");
        searchFormLayout.addComponent(popup, "top:13px; left:500px");
        
        searchFormLayout.addStyleName("searchBarLayout");

        
        firstTabLayout.addComponent(searchFormLayout);
        
        resultsTable = new TableWithSelectAllLayout(ListDataTablePropertyID.TAG.getName());
        
        resultsTable.getTable().setImmediate(true);
        
        resultsTable.getTable().addListener(new GermplasmListValueChangeListener(this));
        resultsTable.getTable().addListener(new GermplasmListItemClickListener(this));
        
        resultsTable.getTable().setColumnWidth(GermplasmSearchQuery.GID, 100);
        resultsTable.getTable().setWidth("100%");
        resultsTable.getTable().setHeight("200px");
        resultsTable.getTable().setSelectable(true);
        resultsTable.getTable().setMultiSelect(true);
        resultsTable.getTable().setColumnReorderingAllowed(true);
        resultsTable.getTable().setColumnCollapsingAllowed(true);

        resultsTable.getTable().addContainerProperty(ListDataTablePropertyID.TAG.getName(), CheckBox.class, null);
        resultsTable.getTable().addContainerProperty(GermplasmSearchQuery.GID, String.class, null);
        resultsTable.getTable().addContainerProperty(GermplasmSearchQuery.NAMES, String.class, null);
        resultsTable.getTable().addContainerProperty(GermplasmSearchQuery.METHOD, String.class, null);
        resultsTable.getTable().addContainerProperty(GermplasmSearchQuery.LOCATION, String.class, null);
        
        messageSource.setColumnHeader(resultsTable.getTable(), (String) ListDataTablePropertyID.TAG.getName(), Message.CHECK_ICON);
        messageSource.setColumnHeader(resultsTable.getTable(), (String) GermplasmSearchQuery.GID, Message.GID_LABEL);
        messageSource.setColumnHeader(resultsTable.getTable(), (String) GermplasmSearchQuery.NAMES, Message.NAMES_LABEL);
        messageSource.setColumnHeader(resultsTable.getTable(), (String) GermplasmSearchQuery.METHOD, Message.METHOD_LABEL);
        messageSource.setColumnHeader(resultsTable.getTable(), (String) GermplasmSearchQuery.LOCATION, Message.LOCATION_LABEL);
        
        firstTabLayout.addComponent(resultsTable);
        
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
                    if(selectedGids.size()==0){
                        nextButton.setEnabled(false);
                    }
                } else if(optionGroup.getValue().equals(OPTION_2_ID)){
                    nextButton.setCaption("Next");
                    if(selectedGids.size()==0){
                        nextButton.setEnabled(false);
                    } else {
                        nextButton.setEnabled(true);
                    }
                } else if(optionGroup.getValue().equals(OPTION_3_ID)){
                    nextButton.setCaption("Next");
                    nextButton.setEnabled(true);
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
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
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
        doneButton.setData(DONE_BUTTON_ID);
        doneButton.addListener(new GermplasmListButtonClickListener(this));
        doneButton.addListener(new CloseWindowAction());
        doneButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        secondTabLayout.addComponent(doneButton, "top:180px;left:90px");
    }
    
    public void nextButtonClickAction(ClickEvent event){
        if(optionGroup.getValue().equals(OPTION_1_ID)){
            // add the germplasm selected as the list entry
            if(this.selectedGids.size()>0){
                this.source.finishAddingEntry(selectedGids);
                Window window = event.getButton().getWindow();
                window.getParent().removeWindow(window);
            } else {
                MessageNotifier.showWarning(this, "Warning!", 
                        "You must select a germplasm/germplasms from the search results.", Notification.POSITION_CENTERED);
            }
        } else if(optionGroup.getValue().equals(OPTION_2_ID)){
            if(this.selectedGids.size()>0){
                if(this.breedingMethodComboBox.getItemIds().isEmpty()){
                    populateBreedingMethodComboBox();
                }
                if(this.nameTypeComboBox.getItemIds().isEmpty()){
                    populateNameTypeComboBox();
                }
                if(this.locationComboBox.getItemIds().isEmpty()){
                    populateLocationComboBox();
                }
                this.accordion.getTab(secondTabLayout).setEnabled(true);
                this.accordion.setSelectedTab(secondTabLayout);
            } else{
                MessageNotifier.showWarning(this, "Warning!", 
                        "You must select a germplasm from the search results.", Notification.POSITION_CENTERED);
            }
        } else if(optionGroup.getValue().equals(OPTION_3_ID)){
            String searchValue = this.searchField.getValue().toString();
            if(searchValue != null && searchValue.length() != 0){
                if(this.breedingMethodComboBox.getItemIds().isEmpty()){
                    populateBreedingMethodComboBox();
                }
                if(this.nameTypeComboBox.getItemIds().isEmpty()){
                    populateNameTypeComboBox();
                }
                if(this.locationComboBox.getItemIds().isEmpty()){
                    populateLocationComboBox();
                }
                this.accordion.getTab(secondTabLayout).setEnabled(true);
                this.accordion.setSelectedTab(secondTabLayout);
            } else {
                MessageNotifier.showWarning(this, "Warning!", 
                        "You must enter a germplasm name in the textbox.",
                        Notification.POSITION_CENTERED);
            }
        }
    }
    
    public void backButtonClickAction(){
        this.accordion.setSelectedTab(firstTabLayout);
    }
    
    public void doneButtonClickAction(){
    	
        if(this.optionGroup.getValue().equals(OPTION_2_ID) || this.optionGroup.getValue().equals(OPTION_3_ID)){
        	
	        Integer breedingMethodId = (Integer) this.breedingMethodComboBox.getValue();
	        Integer nameTypeId = (Integer) this.nameTypeComboBox.getValue();
	        Integer locationId = (Integer) this.locationComboBox.getValue();
	        Date dateOfCreation = (Date) this.germplasmDateField.getValue();
	        SimpleDateFormat formatter = new SimpleDateFormat(DATE_AS_NUMBER_FORMAT);
	        Integer date = Integer.parseInt(formatter.format(dateOfCreation));
	        String germplasmName = this.searchField.getValue().toString();
	        
	        if(this.optionGroup.getValue().equals(OPTION_2_ID)){
		        
		        for(Integer selectedGid : selectedGids){
		        
		        	Germplasm selectedGermplasm = null;
		        	
	                try{
	                    selectedGermplasm = this.germplasmDataManager.getGermplasmWithPrefName(selectedGid);
	                    if(selectedGermplasm.getPreferredName() != null){
	                        germplasmName = selectedGermplasm.getPreferredName().getNval();
	                    }
	                } catch(MiddlewareQueryException mex){
	                    LOG.error("Error with getting germplasm with id: " + selectedGid, mex);
	                    MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting germplasm with id: " + selectedGid 
	                            +". "+messageSource.getMessage(Message.ERROR_REPORT_TO)
	                            , Notification.POSITION_CENTERED);
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
			            Integer gid = this.germplasmDataManager.addGermplasm(germplasm, name);
			            this.source.finishAddingEntry(gid);
			            return;
			        } catch(MiddlewareQueryException ex){
			            LOG.error("Error with saving germplasm and name records!", ex);
			            MessageNotifier.showError(getWindow(), "Database Error!", "Error with saving germplasm and name records. "+messageSource.getMessage(Message.ERROR_REPORT_TO)
			                    , Notification.POSITION_CENTERED);
			            return;
			        }
	        	}
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
		            return;
		        } catch(MiddlewareQueryException ex){
		            LOG.error("Error with saving germplasm and name records!", ex);
		            MessageNotifier.showError(getWindow(), "Database Error!", "Error with saving germplasm and name records. "+messageSource.getMessage(Message.ERROR_REPORT_TO)
		                    , Notification.POSITION_CENTERED);
		            return;
		        }
	        }
        
    	}
        
    }
    
    private void populateBreedingMethodComboBox(){
        try{
            List<Method> methods = this.germplasmDataManager.getAllMethods();
            for(Method method : methods){
                String methodName = method.getMname();
                String methodCode = method.getMcode();
                Integer methodId = method.getMid();
                this.breedingMethodComboBox.addItem(methodId);
                this.breedingMethodComboBox.setItemCaption(methodId, methodName);
                if(methodCode.equals(DEFAULT_METHOD_CODE)){
                    this.breedingMethodComboBox.select(methodId);
                }
            }
        } catch (MiddlewareQueryException ex){
            LOG.error("Error with getting breeding methods!", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting breeding methods. "+messageSource.getMessage(Message.ERROR_REPORT_TO)
                    , Notification.POSITION_CENTERED);
            Integer unknownId = Integer.valueOf(0);
            this.breedingMethodComboBox.addItem(unknownId);
            this.breedingMethodComboBox.setItemCaption(unknownId, "Unknown");
        }
        this.breedingMethodComboBox.setNullSelectionAllowed(false);
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
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting germplasm name types. "+messageSource.getMessage(Message.ERROR_REPORT_TO)
                    , Notification.POSITION_CENTERED);
            Integer unknownId = Integer.valueOf(0);
            this.nameTypeComboBox.addItem(unknownId);
            this.nameTypeComboBox.setItemCaption(unknownId, "Unknown");
        }
        this.nameTypeComboBox.setNullSelectionAllowed(false);
    }
    
    private void populateLocationComboBox(){
        try{
                List<Location> locations = this.germplasmDataManager.getAllBreedingLocations();
                boolean isFirstLocation=false;
            for(Location location : locations){
                Integer locationId = location.getLocid();
                String locationName = location.getLname();
                this.locationComboBox.addItem(locationId);
                this.locationComboBox.setItemCaption(locationId, locationName);
                if(!isFirstLocation){
                  this.locationComboBox.select(locationId);
                  isFirstLocation=true;
                }
            }
        } catch (MiddlewareQueryException ex){
            LOG.error("Error with getting breeding locations!", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Error with getting breeding locations. " + messageSource.getMessage(Message.ERROR_REPORT_TO)
                    , Notification.POSITION_CENTERED);
            Integer unknownId = Integer.valueOf(0);
            this.locationComboBox.addItem(unknownId);
            this.locationComboBox.setItemCaption(unknownId, "Unknown");
        }
        this.locationComboBox.setNullSelectionAllowed(false);
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
               MessageNotifier.showError(getWindow(), "Database Error!", messageSource.getMessage(Message.ERROR_REPORT_TO)
                       , Notification.POSITION_CENTERED);
           return -1;
       }
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(searchButton, Message.SEARCH_LABEL);
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
    
}
