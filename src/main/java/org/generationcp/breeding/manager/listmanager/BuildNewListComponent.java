package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class BuildNewListComponent extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 5314653969843976836L;

	private static final String GID = "GID";
	private static final String ENTRY_ID = "ENTRY ID";
	private static final String ENTRY_CODE = "ENTRY CODE";
	private static final String SEED_SOURCE = "SEED SOURCE";
	private static final String DESIGNATION = "DESIGNATION";
	private static final String PARENTAGE = "PARENTAGE";
	private static final String STATUS = "STATUS";
	private static final String COL8 = " ";
	private static final String COL9 = "  ";
	
	private Object source;
	
    private String DEFAULT_LIST_TYPE = "LST";

	private Label componentTitle;
	private Label componentDescription;

    private Label listNameLabel;
    private Label descriptionLabel;
    private Label listTypeLabel;
    private Label listDateLabel;
    private Label notesLabel;
    
    private ComboBox listTypeComboBox;
    private DateField listDateField;
    private TextField listNameText;
    private TextField descriptionText;
    private TextArea notesTextArea;
    
	private Table germplasmsTable;
	
	private Button saveButton;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	public BuildNewListComponent(ListManagerMain source){
		this.source = source;
	}
	
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {

		componentTitle = new Label();
		componentTitle.setValue(messageSource.getMessage(Message.BUILD_A_NEW_LIST));
		componentTitle.addStyleName("gcp-content-title");
		
		componentDescription = new Label();
		componentDescription.setValue(messageSource.getMessage(Message.BUILD_YOUR_LIST_BY_DRAGGING_LISTS_OR_GERMPLASM_RECORDS_INTO_THIS_NEW_LIST_WINDOW));

		addComponent(componentTitle, "top:30px; left:0px;");
		addComponent(componentDescription, "top:50px; left:0px;");
		
		
        listNameLabel = new Label();
        listNameLabel.setCaption(messageSource.getMessage(Message.NAME_LABEL)+":*");
        listNameLabel.addStyleName("bold");
        addComponent(listNameLabel, "top:105px;left:0px");
        
        listNameText = new TextField();
        listNameText.setWidth("200px");
        addComponent(listNameText, "top:85px;left:46px");

        
        listTypeLabel = new Label();
        listTypeLabel.setCaption(messageSource.getMessage(Message.TYPE_LABEL)+":*");
        listTypeLabel.addStyleName("bold");
        addComponent(listTypeLabel, "top:105px;left:270px");
        
        listTypeComboBox = new ComboBox();
        listTypeComboBox.setWidth("200px");
        listTypeComboBox.setNullSelectionAllowed(false);
        
        List<UserDefinedField> userDefinedFieldList = germplasmListManager.getGermplasmListTypes();
        String firstId = null;
              boolean hasDefault = false;
        for(UserDefinedField userDefinedField : userDefinedFieldList){
                  //method.getMcode()
            if(firstId == null){
                          firstId = userDefinedField.getFcode();
                      }
            listTypeComboBox.addItem(userDefinedField.getFcode());
            listTypeComboBox.setItemCaption(userDefinedField.getFcode(), userDefinedField.getFname());
                  if(DEFAULT_LIST_TYPE.equalsIgnoreCase(userDefinedField.getFcode())){
                      listTypeComboBox.setValue(userDefinedField.getFcode());
                      hasDefault = true;
                  }
              }
        if(hasDefault == false && firstId != null){
            listTypeComboBox.setValue(firstId);
           }

        listTypeComboBox.setTextInputAllowed(false);
        listTypeComboBox.setImmediate(true);
        addComponent(listTypeComboBox, "top:85px;left:310px");


        listDateLabel = new Label();
        listDateLabel.setCaption(messageSource.getMessage(Message.DATE_LABEL)+":");
        listDateLabel.addStyleName("bold");
        addComponent(listDateLabel, "top:105px;left:540px");
      
        listDateField = new DateField();
        listDateField.setDateFormat("yyyy-MM-dd");
        listDateField.setResolution(DateField.RESOLUTION_DAY);
        addComponent(listDateField, "top:85px;left:580px");
        
        
        descriptionLabel = new Label();
        descriptionLabel.setCaption(messageSource.getMessage(Message.DESCRIPTION_LABEL)+"*");
        descriptionLabel.addStyleName("bold");
        addComponent(descriptionLabel, "top:145px;left:0px");
        
        descriptionText = new TextField();
        descriptionText.setWidth("595px");
        addComponent(descriptionText, "top:125px;left:80px");

		
		
        notesLabel = new Label();
        notesLabel.setCaption(messageSource.getMessage(Message.NOTES)+":");
        notesLabel.addStyleName("bold");
        addComponent(notesLabel, "top:105px; left: 720px;");
		
        notesTextArea = new TextArea();
        notesTextArea.setWidth("400px");
        notesTextArea.setHeight("65px");
        notesTextArea.addStyleName("noResizeTextArea");
        addComponent(notesTextArea, "top:85px; left: 770px;");
		
		
		germplasmsTable = new Table();
		germplasmsTable.addContainerProperty(GID, Button.class, null);
		germplasmsTable.addContainerProperty(ENTRY_ID, Integer.class, null);
		germplasmsTable.addContainerProperty(ENTRY_CODE, Integer.class, null);
		germplasmsTable.addContainerProperty(SEED_SOURCE, String.class, null);
		germplasmsTable.addContainerProperty(DESIGNATION, String.class, null);
		germplasmsTable.addContainerProperty(PARENTAGE, String.class, null);
		germplasmsTable.addContainerProperty(STATUS, String.class, null);
		germplasmsTable.addContainerProperty(COL8, String.class, null);
		germplasmsTable.addContainerProperty(COL9, String.class, null);
		germplasmsTable.setWidth("100%");
		germplasmsTable.setHeight("280px");
		
		addComponent(germplasmsTable, "top:170px; left:0px;");
		
		VerticalLayout buttonRow = new VerticalLayout();
		buttonRow.setWidth("100%");
		buttonRow.setHeight("150px");
		
		saveButton = new Button();
		saveButton.setCaption(messageSource.getMessage(Message.SAVE_LIST));
		saveButton.setStyleName(BaseTheme.BUTTON_LINK);
		saveButton.addStyleName("gcp_button");
		
		buttonRow.addComponent(saveButton);
		buttonRow.setComponentAlignment(saveButton, Alignment.MIDDLE_CENTER);
		
		addComponent(buttonRow, "top:420px; left:0px;");
		
		setWidth("100%");
		setHeight("600px");
		
		setupDragSources();
		setupDropHandlers();
	}

		
	/**
	 * Setup drag sources, this will tell Vaadin which tables can the user drag rows from
	 */
	private void setupDragSources(){
		if(source instanceof ListManagerMain){
			//Browse Lists tab
			
			/**
			 * TODO: enable draggable tables here
			 */
			
			//Search Lists and Germplasms tab
			Table matchingGermplasmsTable = ((ListManagerMain) source).getListManagerSearchListsComponent().getSearchResultsComponent().getMatchingGermplasmsTable();
			Table matchingListsTable = ((ListManagerMain) source).getListManagerSearchListsComponent().getSearchResultsComponent().getMatchingListsTable();
			
			matchingGermplasmsTable.setDragMode(TableDragMode.ROW); 
			matchingListsTable.setDragMode(TableDragMode.ROW); 
		}
	}
	
	
	/**
	 * Setup drop handlers, this will dictate how Vaadin will handle drops (mouse releases) on the germplasm table
	 */
	private void setupDropHandlers(){
		germplasmsTable.setDropHandler(new DropHandler() {
			public void drop(DragAndDropEvent dropEvent) {
				TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();
				
				Table sourceTable = (Table) transferable.getSourceComponent();
			    Table targetTable = (Table) dropEvent.getTargetDetails().getTarget();
			
			    AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
                Object droppedOverItemId = dropData.getItemIdOver();
			    
                //TODO: add handler for source tables from "Browse Lists" tab
                
                if(sourceTable.getData().equals(SearchResultsComponent.MATCHING_GEMRPLASMS_TABLE_DATA)){
                	addGermplasmToGermplasmTable(Integer.valueOf(transferable.getItemId().toString()), droppedOverItemId);
                } else if(sourceTable.getData().equals(SearchResultsComponent.MATCHING_LISTS_TABLE_DATA)){
                	addGermplasmListDataToGermplasmTable(Integer.valueOf(transferable.getItemId().toString()), droppedOverItemId);
                }
			    
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}
		});
	}
	
	

	
	
	/**
	 * Add germplasms from a gemrplasm list to the table
	 */
	private void addGermplasmListDataToGermplasmTable(Integer listId, Object droppedOnItemIdObject){
		
		List<Integer> itemIds = getItemIds(germplasmsTable);
		
		int start = 0;
        int listDataCount;
        
        List<GermplasmListData> listDatas = new ArrayList<GermplasmListData>();
		try {
			listDataCount = (int) germplasmListManager.countGermplasmListDataByListId(listId);
			listDatas = this.germplasmListManager.getGermplasmListDataByListId(listId, start, listDataCount);
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        for (GermplasmListData data : listDatas) {
        
        	if(!itemIds.contains(data.getGid())){
				Item newItem;
				if(droppedOnItemIdObject!=null)
					newItem = germplasmsTable.addItem(data.getGid());
				else
					newItem = germplasmsTable.addItemAfter(droppedOnItemIdObject, data.getGid());

				Button gidButton = new Button(String.format("%s", data.getGid()), new GidLinkButtonClickListener(data.getGid().toString(), true));
	            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
				
	            String crossExpansion = "";
            	try {
            		if(germplasmDataManager!=null)
            			crossExpansion = germplasmDataManager.getCrossExpansion(data.getGid(), 1);
            	} catch(MiddlewareQueryException ex){
                    crossExpansion = "-";
                }

	            newItem.getItemProperty(GID).setValue(gidButton);
				//newItem.getItemProperty(SEED_SOURCE).setValue(data.getSeedSource());
	            newItem.getItemProperty(SEED_SOURCE).setValue("From List Manager");
				newItem.getItemProperty(DESIGNATION).setValue(data.getDesignation());
				newItem.getItemProperty(PARENTAGE).setValue(crossExpansion);
				newItem.getItemProperty(STATUS).setValue("0");
				
        	}
        }		
        assignSerializedEntryCode();
	}
	
	
	/**
	 * Add a germplasm to a table, adds it after/before a certain germplasm given the droppedOn item id
	 * @param gid
	 * @param droppedOn
	 */
	private void addGermplasmToGermplasmTable(Integer gid, Object droppedOnItemIdObject){

		List<Integer> itemIds = getItemIds(germplasmsTable);
		if(!itemIds.contains(gid)){
			
			try {
				
				Germplasm germplasm = germplasmDataManager.getGermplasmByGID(gid);

				Item newItem;
				if(droppedOnItemIdObject!=null)
					newItem = germplasmsTable.addItem(gid);
				else
					newItem = germplasmsTable.addItemAfter(droppedOnItemIdObject, gid);
				
				Button gidButton = new Button(String.format("%s", gid), new GidLinkButtonClickListener(gid.toString(), true));
	            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
				
	            String crossExpansion = "";
	            if(germplasm!=null){
	            	try {
	            		if(germplasmDataManager!=null)
	            			crossExpansion = germplasmDataManager.getCrossExpansion(germplasm.getGid(), 1);
	            	} catch(MiddlewareQueryException ex){
	                    crossExpansion = "-";
	                }
	        	}

	            List<Integer> importedGermplasmGids = new ArrayList<Integer>();
		        importedGermplasmGids.add(gid);
	            Map<Integer, String> preferredNames = germplasmDataManager.getPreferredNamesByGids(importedGermplasmGids);
	            String preferredName = preferredNames.get(gid); 
	            
	            Location location = germplasmDataManager.getLocationByID(germplasm.getLocationId());
	            
	            newItem.getItemProperty(GID).setValue(gidButton);
				//newItem.getItemProperty(SEED_SOURCE).setValue(location.getLname());
				newItem.getItemProperty(SEED_SOURCE).setValue("From List Manager");
				newItem.getItemProperty(DESIGNATION).setValue(preferredName);
				newItem.getItemProperty(PARENTAGE).setValue(crossExpansion);
				newItem.getItemProperty(STATUS).setValue("0");
				
				assignSerializedEntryCode();
				
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}

		}
	}	
		
	
	/**
	 * Iterates through the whole table, and sets the entry code from 1 to n based on the row position
	 */
	private void assignSerializedEntryCode(){
		List<Integer> itemIds = getItemIds(germplasmsTable);
    	    	
    	int id = 1;
    	for(Integer itemId : itemIds){
    		germplasmsTable.getItem(itemId).getItemProperty(ENTRY_ID).setValue(id);
    		germplasmsTable.getItem(itemId).getItemProperty(ENTRY_CODE).setValue(id);
    		id++;
    	}
    }

	
	/**
	 * Get item id's of a table, and return it as a list 
	 * @param table
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getItemIds(Table table){
		List<Integer> itemIds = new ArrayList<Integer>();
    	itemIds.addAll((Collection<? extends Integer>) germplasmsTable.getItemIds());
    	return itemIds;
	}

}
