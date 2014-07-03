package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.listeners.ParentsTableCheckboxListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class MakeCrossesParentsComponent extends VerticalLayout implements BreedingManagerLayout,
									InitializingBean, InternationalizableComponent {

	private static final Logger LOG = LoggerFactory.getLogger(MakeCrossesParentsComponent.class);
	private static final long serialVersionUID = -4789763601080845176L;
	
	private static final int PARENTS_TABLE_ROW_COUNT = 10;
    
    private static final String TAG_COLUMN_ID = "Tag";
    private static final String ENTRY_NUMBER_COLUMN_ID = "Entry Number Column ID";
    private static final String DESIGNATION_ID = "Designation";
    private static final String AVAIL_INV_COLUMN_ID = "Avail Inv";
    private static final String SEED_RES_COLUMN_ID = "Seed Res";
        
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private InventoryDataManager inventoryDataManager;    
    
    private TabSheet parentTabSheet;
    private Label parentListsLabel;
    private Label instructionForParentLists;
    
    private ParentTabComponent femaleParentTab;
    private ParentTabComponent maleParentTab;
    
    private Table femaleParents;
    private CheckBox femaleParentsTagAll;
    private Table maleParents;
    private CheckBox maleParentsTagAll;
    
    private CrossingManagerMakeCrossesComponent makeCrossesMain;
        
    public MakeCrossesParentsComponent(CrossingManagerMakeCrossesComponent parentComponent){
    	this.makeCrossesMain = parentComponent;
	}

    @Override
    public void attach() {
    	super.attach();
    	updateLabels();
    }
	@Override
	public void updateLabels() {

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
        parentListsLabel = new Label(messageSource.getMessage(Message.PARENTS_LISTS));
        parentListsLabel.setStyleName(Bootstrap.Typography.H4.styleName());
        parentListsLabel.addStyleName(AppConstants.CssStyles.BOLD);
        
        instructionForParentLists = new Label(messageSource.getMessage(Message.INSTRUCTION_FOR_PARENT_LISTS));
        
        femaleParentTab = new ParentTabComponent(makeCrossesMain,this,
				messageSource.getMessage(Message.LABEL_FEMALE_PARENTS),PARENTS_TABLE_ROW_COUNT);
        
        maleParentTab = new ParentTabComponent(makeCrossesMain,this,
				messageSource.getMessage(Message.LABEL_MALE_PARENTS),PARENTS_TABLE_ROW_COUNT);
        
        femaleParents = femaleParentTab.getListDataTable();
        maleParents = maleParentTab.getListDataTable();
	}

	@Override
	public void initializeValues() {

	}

	@Override
	public void addListeners() {
		
	}

	@Override
	public void layoutComponents() {
        setSpacing(true);
        setMargin(false,false,false,true);
        setWidth("450px");
        
        parentTabSheet = new TabSheet();
        parentTabSheet.addTab(femaleParentTab,messageSource.getMessage(Message.LABEL_FEMALE_PARENTS));
        parentTabSheet.addTab(maleParentTab,messageSource.getMessage(Message.LABEL_MALE_PARENTS));
        parentTabSheet.setWidth("420px");
        parentTabSheet.setHeight("465px");
        
        HeaderLabelLayout parentLabelLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_LIST_TYPES,parentListsLabel);
        addComponent(parentLabelLayout);
        addComponent(instructionForParentLists);
        addComponent(parentTabSheet);
	}// end of layoutComponent

	@SuppressWarnings("unchecked")
	public void dropToFemaleOrMaleTable(Table sourceTable, Table targetTable, Integer transferrableItemId){
		List<Integer> selectedListEntries = new ArrayList<Integer>();
    	selectedListEntries.addAll((Collection<Integer>) sourceTable.getValue());
    	
    	if(selectedListEntries.isEmpty() && transferrableItemId != null){
    		selectedListEntries.add(transferrableItemId);
    	}
    	
    	List<Integer> entryIdsInSourceTable = new ArrayList<Integer>();
    	entryIdsInSourceTable.addAll((Collection<Integer>) sourceTable.getItemIds());
    	
    	List<Integer> initialEntryIdsInDestinationTable = new ArrayList<Integer>();
    	initialEntryIdsInDestinationTable.addAll((Collection<Integer>) targetTable.getItemIds());    	
    	
    	for(Integer itemId : entryIdsInSourceTable){
    		if(selectedListEntries.contains(itemId)){
	    		Integer entryId = (Integer) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).getValue();
	    		
	    		Button designationBtn = (Button) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue(); 
	    		String designation = designationBtn.getCaption();
	    		
	    		Button gidBtn = (Button) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.GID.getName()).getValue();
	    		Integer gid = Integer.valueOf(Integer.parseInt(gidBtn.getCaption()));
	    		
	    		String seedSource = getSeedSource(sourceTable,entryId);
	    		
	    		GermplasmListEntry entryObject = new GermplasmListEntry(itemId, gid, entryId, designation, seedSource);
	    		Item item = targetTable.addItem(entryObject);
	    		
	    		if(item != null){
	    			
	    			Button newGidButton = new Button(designation, new GidLinkClickListener(gid.toString(),true));
	    			newGidButton.setStyleName(BaseTheme.BUTTON_LINK);
	    			newGidButton.setDescription("Click to view Germplasm information");
	    			
	    			item.getItemProperty(DESIGNATION_ID).setValue(newGidButton);
		    		if(targetTable.equals(femaleParents)){
		    			entryObject.setFromFemaleTable(true);
		    			femaleParentTab.getSaveActionMenu().setEnabled(true);
		    			femaleParentTab.updateNoOfEntries(femaleParents.size());
		    			femaleParentTab.setHasUnsavedChanges(true);
            			//femaleParentList = null;
		    		} else{
		    			entryObject.setFromFemaleTable(false);
		    			maleParentTab.getSaveActionMenu().setEnabled(true);
		    			maleParentTab.updateNoOfEntries(maleParents.size());
		    			maleParentTab.setHasUnsavedChanges(true);
            			//maleParentList = null;
		    		}
		    		
		    		CheckBox tag = new CheckBox();
		    		if(targetTable.equals(femaleParents)){
		    			tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, femaleParentsTagAll));
		    		} else{
		    			tag.addListener(new ParentsTableCheckboxListener(targetTable, entryObject, maleParentsTagAll));
		    		}
		            tag.setImmediate(true);
		            item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
		            
		            Button sourceAvailInvButton = ((Button) sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.AVAILABLE_INVENTORY.getName()).getValue());
		            Button newAvailInvButton = new Button();
		            
		            newAvailInvButton.setCaption(sourceAvailInvButton.getCaption());
		            newAvailInvButton.addListener((InventoryLinkButtonClickListener) sourceAvailInvButton.getData());
		            newAvailInvButton.setStyleName(BaseTheme.BUTTON_LINK);
		            newAvailInvButton.setDescription("Click to view Inventory Details");
		            
		            String seed_res = "-";
		            if(sourceTable.getItemIds().size() == selectedListEntries.size())
		            	seed_res = sourceTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.SEED_RESERVATION.getName()).getValue().toString();
		            
		            item.getItemProperty(AVAIL_INV_COLUMN_ID).setValue(newAvailInvButton);
		            item.getItemProperty(SEED_RES_COLUMN_ID).setValue(seed_res);
		        }
	    	}

            targetTable.requestRepaint();
        }
    	
    	List<Integer> entryIdsInDestinationTable = new ArrayList<Integer>();
    	entryIdsInDestinationTable.addAll((Collection<Integer>) targetTable.getItemIds());
    	    	
    	if(initialEntryIdsInDestinationTable.size()==0 && entryIdsInSourceTable.size()==entryIdsInDestinationTable.size()){
    		if(targetTable.equals(femaleParents)){
    			GermplasmList femaleGermplasmList = ((SelectParentsListDataComponent) makeCrossesMain.getSelectParentsComponent().getListDetailsTabSheet().getSelectedTab()).getGermplasmList();
    			
    			//Checks the source list is a local list
    			if(femaleGermplasmList.getId() < 0){
    				femaleParentTab.getSaveActionMenu().setEnabled(false);
    				femaleParentTab.setHasUnsavedChanges(false);
        			femaleParentTab.setListNameForCrosses(femaleGermplasmList.getName());
        	    	updateCrossesSeedSource(femaleParentTab, femaleGermplasmList);
    			}
    			else{//if the source list is a central list
    				femaleParentTab.getSaveActionMenu().setEnabled(true);
    				femaleParentTab.setHasUnsavedChanges(true);
    				femaleParentTab.setListNameForCrosses("");
    				femaleParentTab.setGermplasmList(null);
    			}
    			
    		} else{//if male
    			GermplasmList maleGermplasmList = ((SelectParentsListDataComponent) makeCrossesMain.getSelectParentsComponent().getListDetailsTabSheet().getSelectedTab()).getGermplasmList();
    			
    			//Checks the source list is a local list
    			if(maleGermplasmList.getId() < 0){
    				maleParentTab.getSaveActionMenu().setEnabled(false);
    				maleParentTab.setHasUnsavedChanges(false);
    				maleParentTab.setListNameForCrosses(maleGermplasmList.getName());
        	    	updateCrossesSeedSource(maleParentTab, maleGermplasmList);
    			}
    			else{//if the source list is a central list
    				maleParentTab.getSaveActionMenu().setEnabled(true);
    				maleParentTab.setHasUnsavedChanges(true);
    				maleParentTab.setListNameForCrosses("");
    				maleParentTab.setGermplasmList(null);
    			}
    			
    		}
    		
    		//updates the crossesMade.savebutton if both parents are save at least once;
    		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
    	} else {
    		if(targetTable.equals(femaleParents)){
    			femaleParentTab.getSaveActionMenu().setEnabled(true);
    			femaleParentTab.setListNameForCrosses("");
    			femaleParentTab.setGermplasmList(null);
    			femaleParentTab.setHasUnsavedChanges(true);
    			clearSeedReservationValues(femaleParents);
    		} else{
    			maleParentTab.getSaveActionMenu().setEnabled(true);
    			maleParentTab.setListNameForCrosses("");
    			maleParentTab.setGermplasmList(null);
    			maleParentTab.setHasUnsavedChanges(true);
    			clearSeedReservationValues(maleParents);
    		}
    	}
    	
	}

	private void clearSeedReservationValues(Table table){
		for(Object itemId : table.getItemIds()){
			table.getItem(itemId).getItemProperty(SEED_RES_COLUMN_ID).setValue("-");
		}
	}
	
	@SuppressWarnings("unused")
	private boolean checkIfGIDisInTable(Table targetTable, Integer gid){
		for(Object itemId : targetTable.getItemIds()){
			GermplasmListEntry entry = (GermplasmListEntry) itemId;
			if(gid.equals(entry.getGid())){
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public void assignEntryNumber(Table parentsTable){
		
		int entryNumber = 1;
		List<GermplasmListEntry> itemIds = new ArrayList<GermplasmListEntry>();
		itemIds.addAll((Collection<GermplasmListEntry>) parentsTable.getItemIds());
		
		for(GermplasmListEntry entry : itemIds){
			Item item = parentsTable.getItem(entry);
    		item.getItemProperty(ENTRY_NUMBER_COLUMN_ID).setValue(Integer.valueOf(entryNumber));
    		entry.setEntryId(entryNumber);
			entryNumber++;
		}
	}

	public void updateCrossesSeedSource(ParentTabComponent parentTab,
			GermplasmList savedList) {
		if (parentTab.equals(femaleParentTab)){
			femaleParentTab.setGermplasmList(savedList);
			if (femaleParentTab.getListNameForCrosses() != null 
					&& !femaleParentTab.getListNameForCrosses().equals(femaleParentTab.getGermplasmList().getName())){
				femaleParentTab.setListNameForCrosses(femaleParentTab.getGermplasmList().getName());
				makeCrossesMain.updateCrossesSeedSource(femaleParentTab.getListNameForCrosses(), 
						maleParentTab.getListNameForCrosses());
			}
		} else {
			maleParentTab.setGermplasmList(savedList);
			if (maleParentTab.getListNameForCrosses() != null 
					&& !maleParentTab.getListNameForCrosses().equals(maleParentTab.getGermplasmList().getName())){
				maleParentTab.setListNameForCrosses(maleParentTab.getGermplasmList().getName());
				makeCrossesMain.updateCrossesSeedSource(femaleParentTab.getListNameForCrosses(), 
						maleParentTab.getListNameForCrosses());
			}
		}
	}
	
	public void updateUIForSuccessfulSaving(ParentTabComponent parentTab, GermplasmList list) {
		parentTab.getSaveActionMenu().setEnabled(false);
		makeCrossesMain.toggleNextButton();
		
		makeCrossesMain.getSelectParentsComponent().selectListInTree(list.getId());
		makeCrossesMain.getSelectParentsComponent().updateUIForDeletedList(list);
		
		//updates the crossesMade.savebutton if both parents are save at least once;
		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
		
		MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), 
				messageSource.getMessage(parentTab.getSuccessMessage()));
	}
	
	public void updateFemaleListNameForCrosses(){
		String femaleListNameForCrosses = "";
		femaleListNameForCrosses = getFemaleList() != null ? getFemaleList().getName() : "";
		femaleParentTab.setListNameForCrosses(femaleListNameForCrosses);
	}
	
	public void updateMaleListNameForCrosses(){
		String maleListNameForCrosses = "";
		maleListNameForCrosses = getMaleList() != null ? getMaleList().getName() : "";
		maleParentTab.setListNameForCrosses(maleListNameForCrosses);
	}
	
    public boolean isFemaleListSaved(){
    	if(femaleParentTab.getListNameForCrosses() != null){
    		return (femaleParentTab.getListNameForCrosses().length() > 0);
    	}
    	return false;
    }
    
    public boolean isMaleListSaved(){
    	if(maleParentTab.getListNameForCrosses() != null){
    		return (maleParentTab.getListNameForCrosses().length() > 0);
    	}
    	return false;
    }
	
	public String getSeedSource(Table table, Integer entryId){
		String seedSource = "";
		if(table.getParent().getParent() instanceof SelectParentsListDataComponent ){
			SelectParentsListDataComponent parentComponent = (SelectParentsListDataComponent) table.getParent().getParent();
			String listname = parentComponent.getListName();			
			seedSource = listname + ":" + entryId;
		}
		
		return seedSource;
	}

    
    /**
     * Implemented something similar to table.getValue(), because that method returns
     *     a collection of items, but does not follow the sorting done by the 
     *     drag n drop sorting, this one does
     * @param table
     * @return List of selected germplasm list entries
     */
    @SuppressWarnings("unchecked")
	protected List<GermplasmListEntry> getCorrectSortedValue(Table table){
    	List<GermplasmListEntry> allItemIds = new ArrayList<GermplasmListEntry>();
    	List<GermplasmListEntry> selectedItemIds = new ArrayList<GermplasmListEntry>();
    	List<GermplasmListEntry> sortedSelectedValues = new ArrayList<GermplasmListEntry>();
    	
    	allItemIds.addAll((Collection<GermplasmListEntry>) table.getItemIds());
    	selectedItemIds.addAll((Collection<GermplasmListEntry>) table.getValue());

    	for(GermplasmListEntry entry : allItemIds){
			CheckBox tag = (CheckBox) table.getItem(entry).getItemProperty(TAG_COLUMN_ID).getValue();
			Boolean tagValue = (Boolean) tag.getValue();
			if(tagValue.booleanValue()){
				selectedItemIds.add(entry);
			} 
		}
    	
    	for(GermplasmListEntry itemId : allItemIds){
    		for(GermplasmListEntry selectedItemId : selectedItemIds){
    			if(itemId.equals(selectedItemId))
    				sortedSelectedValues.add(selectedItemId);    			
    		}
    	}
    	return sortedSelectedValues;
    }
    	
	@SuppressWarnings("unchecked")
	public void addListToMaleTable(Integer germplasmListId){
		
        try {
        	GermplasmList listFromTree = germplasmListManager.getGermplasmListById(germplasmListId);
        	if(listFromTree!=null){
        		List<GermplasmListData> germplasmListDataFromListFromTree = inventoryDataManager.getLotCountsForList(germplasmListId, 0, Integer.MAX_VALUE);
        		
        		Integer addedCount = 0;
        		
        		for(GermplasmListData listData : germplasmListDataFromListFromTree){
        			if(listData.getStatus()!=9){
        				String maleParentValue = listData.getDesignation();
        				
                        Button gidButton = new Button(maleParentValue, new GidLinkClickListener(listData.getGid().toString(),true));
                        gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                        gidButton.setDescription("Click to view Germplasm information");
                        
        				CheckBox tag = new CheckBox();
                    	
        				GermplasmListEntry entryObject = new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(), listData.getDesignation(), listFromTree.getName()+":"+listData.getEntryId());
        				
    		    		
    		    		tag.addListener(new ParentsTableCheckboxListener(maleParents, entryObject, maleParentsTagAll));
    		    		maleParentTab.setListNameForCrosses(listFromTree.getName());
    		    	    updateCrossesSeedSource(maleParentTab, listFromTree);
    		    		
    		    		
    		            tag.setImmediate(true);
        				
    		            //if the item is already existing in the target table, remove the existing item then add a new entry
    		            maleParents.removeItem(entryObject);
    		            

    		            
    	    			
    	    			//#1 Available Inventory
    	    			String avail_inv = "-"; //default value
    	    			if(listData.getInventoryInfo().getLotCount().intValue() != 0){
    	    				avail_inv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
    	    			}
    	    			
    	    			InventoryLinkButtonClickListener inventoryClickListener = new InventoryLinkButtonClickListener(this,germplasmListId,listData.getId(), listData.getGid());
    	    			Button inventoryButton = new Button(avail_inv, inventoryClickListener);
    	    			inventoryButton.setData(inventoryClickListener);
    	    			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
    	    			inventoryButton.setDescription("Click to view Inventory Details");
    	    			
    	    			if(avail_inv.equals("-")){
    	    				inventoryButton.setEnabled(false);
    	    				inventoryButton.setDescription("No Lot for this Germplasm");
    	    			}
    	    			else{
    	    				inventoryButton.setDescription("Click to view Inventory Details");
    	    			}
    	    			
    	    			// Seed Reserved
    	    	   		String seed_res = "-"; //default value
    	    	   		if(listData.getInventoryInfo().getReservedLotCount().intValue() != 0){
    	    	   			seed_res = listData.getInventoryInfo().getReservedLotCount().toString().trim();
    	    	   		}
    		            
        				Item item = maleParents.addItem(entryObject);
        				item.getItemProperty(DESIGNATION_ID).setValue(gidButton);
        				item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
        				
        				item.getItemProperty(AVAIL_INV_COLUMN_ID).setValue(inventoryButton);
        				item.getItemProperty(SEED_RES_COLUMN_ID).setValue(seed_res);
    		            
        				addedCount++;
        			} 
            	}
        		
        		//After adding, check if the # of items added on the table, is equal to the number of list data of the dragged list, this will enable/disable the save option
        		List<Object> itemsLeftAfterAdding = new ArrayList<Object>();
        		itemsLeftAfterAdding.addAll((Collection<? extends Integer>) maleParents.getItemIds());

        		if(addedCount==itemsLeftAfterAdding.size()){
        			maleParentTab.getSaveActionMenu().setEnabled(false);
        			maleParentTab.setHasUnsavedChanges(false);
        			
        			//updates the crossesMade.savebutton if both parents are save at least once;
            		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
            		
        		} else {
        			maleParentTab.getSaveActionMenu().setEnabled(true);
        			maleParentTab.setHasUnsavedChanges(true);
        			clearSeedReservationValues(maleParents);
        			//maleParentList = null;
        		}
        	}
        	
        	maleParentTab.setGermplasmList(listFromTree);
        } catch(MiddlewareQueryException e) {
        	LOG.error("Error in getting list by GID",e);	
        }
        
        assignEntryNumber(maleParents);
		maleParentTab.updateNoOfEntries(maleParents.size());
		parentTabSheet.setSelectedTab(1);
	}
	
	
	@SuppressWarnings("unchecked")
	public void addListToFemaleTable(Integer germplasmListId){
		
        try {
        	GermplasmList listFromTree = germplasmListManager.getGermplasmListById(germplasmListId);
        	
        	if(listFromTree!=null){
        		
        		List<GermplasmListData> germplasmListDataFromListFromTree = inventoryDataManager.getLotCountsForList(germplasmListId, 0, Integer.MAX_VALUE);
        		
        		Integer addedCount = 0;
        		
        		for(GermplasmListData listData : germplasmListDataFromListFromTree){
        			if(listData.getStatus()!=9){
        				String maleParentValue = listData.getDesignation();
        				
                        Button gidButton = new Button(maleParentValue, new GidLinkClickListener(listData.getGid().toString(),true));
                        gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                        gidButton.setDescription("Click to view Germplasm information");
                        
        				CheckBox tag = new CheckBox();
                    	
        				GermplasmListEntry entryObject = new GermplasmListEntry(listData.getId(), listData.getGid(), listData.getEntryId(), listData.getDesignation(), listFromTree.getName()+":"+listData.getEntryId());
        				
    		    		
    		    		tag.addListener(new ParentsTableCheckboxListener(femaleParents, entryObject, femaleParentsTagAll));
    		    		femaleParentTab.setListNameForCrosses(listFromTree.getName());
    		    	    updateCrossesSeedSource(femaleParentTab, listFromTree);
    		    		
    		    		
    		            tag.setImmediate(true);
        				
    		            //if the item is already existing in the target table, remove the existing item then add a new entry
    		            femaleParents.removeItem(entryObject);
    		            
    		            
    	    			//#1 Available Inventory
    	    			String avail_inv = "-"; //default value
    	    			if(listData.getInventoryInfo().getLotCount().intValue() != 0){
    	    				avail_inv = listData.getInventoryInfo().getActualInventoryLotCount().toString().trim();
    	    			}
    	    			
    	    			InventoryLinkButtonClickListener inventoryClickListener = new InventoryLinkButtonClickListener(this,germplasmListId,listData.getId(), listData.getGid());
    	    			Button inventoryButton = new Button(avail_inv, inventoryClickListener);
    	    			inventoryButton.setData(inventoryClickListener);
    	    			inventoryButton.setStyleName(BaseTheme.BUTTON_LINK);
    	    			inventoryButton.setDescription("Click to view Inventory Details");
    	    			
    	    			if(avail_inv.equals("-")){
    	    				inventoryButton.setEnabled(false);
    	    				inventoryButton.setDescription("No Lot for this Germplasm");
    	    			}
    	    			else{
    	    				inventoryButton.setDescription("Click to view Inventory Details");
    	    			}
    	    			
    	    			// Seed Reserved
    	    	   		String seed_res = "-"; //default value
    	    	   		if(listData.getInventoryInfo().getReservedLotCount().intValue() != 0){
    	    	   			seed_res = listData.getInventoryInfo().getReservedLotCount().toString().trim();
    	    	   		}
    		            
    		            
        				Item item = femaleParents.addItem(entryObject);
        				item.getItemProperty(DESIGNATION_ID).setValue(gidButton);
        				item.getItemProperty(TAG_COLUMN_ID).setValue(tag);
        				
        				item.getItemProperty(AVAIL_INV_COLUMN_ID).setValue(inventoryButton);
        				item.getItemProperty(SEED_RES_COLUMN_ID).setValue(seed_res);
        				
        				addedCount++;
        			} 
            	}
        		
        		//After adding, check if the # of items added on the table, is equal to the number of list data of the dragged list, this will enable/disable the save option
        		List<Object> itemsLeftAfterAdding = new ArrayList<Object>();
        		itemsLeftAfterAdding.addAll((Collection<? extends Integer>) femaleParents.getItemIds());

        		if(addedCount==itemsLeftAfterAdding.size()){
        			femaleParentTab.getSaveActionMenu().setEnabled(false);
        			femaleParentTab.setHasUnsavedChanges(false);
        			
        			//updates the crossesMade.savebutton if both parents are save at least once;
            		makeCrossesMain.getCrossesTableComponent().updateCrossesMadeSaveButton();
            		
        		} else {
        			femaleParentTab.getSaveActionMenu().setEnabled(true);
        			femaleParentTab.setHasUnsavedChanges(true);
        			clearSeedReservationValues(femaleParents);
        			//maleParentList = null;
        		}
        	}
        	
        	femaleParentTab.setGermplasmList(listFromTree);
        } catch(MiddlewareQueryException e) {
        	LOG.error("Error in getting list by GID",e);	
        }
        
        assignEntryNumber(femaleParents);
        femaleParentTab.updateNoOfEntries(femaleParents.size());
        parentTabSheet.setSelectedTab(0);
	}

	//SETTERS AND GETTERS
    public Table getFemaleTable(){
    	return femaleParents;
    }
    
    public Table getMaleTable(){
    	return maleParents;
    }
    
    public GermplasmList getFemaleList(){
    	return femaleParentTab.getGermplasmList();
    }
    
    public GermplasmList getMaleList(){
    	return maleParentTab.getGermplasmList();
    }
    
    public void setFemaleParentList(GermplasmList list){
    	femaleParentTab.setGermplasmList(list);
    }
    
    public void setMaleParentList(GermplasmList list){
    	maleParentTab.setGermplasmList(list);
    }
    
	public String getFemaleListNameForCrosses() {
		return femaleParentTab.getListNameForCrosses();
	}

	public String getMaleListNameForCrosses() {
		return maleParentTab.getListNameForCrosses();
	}

	public TabSheet getParentTabSheet() {
		return parentTabSheet;
	}
	
	public ParentTabComponent getFemaleParentTab() {
		return femaleParentTab;
	}

	public void setFemaleParentTab(ParentTabComponent femaleParentTab) {
		this.femaleParentTab = femaleParentTab;
	}

	public ParentTabComponent getMaleParentTab() {
		return maleParentTab;
	}

	public void setMaleParentTab(ParentTabComponent maleParentTab) {
		this.maleParentTab = maleParentTab;
	}

	public void setHasUnsavedChanges(boolean hasChanges) {
		femaleParentTab.setHasUnsavedChanges(hasChanges);
		maleParentTab.setHasUnsavedChanges(hasChanges);
	}

}
