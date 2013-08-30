package  org.generationcp.browser.cross.study.h2h.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.TablesEntries;
import org.generationcp.browser.germplasm.dialogs.SelectAGermplasmDialog;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class SpecifyGermplasmsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7925696669478799303L;
    
    public static final String NEXT_BUTTON_ID = "SpecifyGermplasmsComponent Next Button ID";
    public static final String SELECT_TEST_ENTRY_BUTTON_ID = "SpecifyGermplasmsComponent Select Test Entry Button ID";
    public static final String SELECT_STANDARD_ENTRY_BUTTON_ID = "SpecifyGermplasmsComponent Select Standard Entry Button ID";
    
    public static final String SELECT_TEST_SEARCH_GERMPLASM_BUTTON_ID = "SpecifyGermplasmsComponent Test Search Germplasm Button ID";
    public static final String SELECT_STANDARD_SEARCH_GERMPLASM_BUTTON_ID = "SpecifyGermplasmsComponent Standard Search Germplasm Button ID";
    public static final String SELECT_TEST_SEARCH_GERMPLASM_LIST_BUTTON_ID = "SpecifyGermplasmsComponent Test Search Germplasm List Button ID";
    public static final String SELECT_STANDARD_SEARCH_GERMPLASM_LIST_BUTTON_ID = "SpecifyGermplasmsComponent Standard Search Germplasm List Button ID";
    
    
    private Panel testPanel;
    private Panel standardPanel;
    
    private Label specifyTestEntryLabel;
    private Label specifyStandardEntryLabel;
    private Label testEntryLabel;
    private Label standardEntryLabel;
    
    private Button selectTestEntryButton;
    private Button selectStandardEntryButton;
    private Button nextButton;
    
    private Button testSearchGermplasm;
    private Button standardSearchGermplasm;
    
    private Button testSearchGermplasmList;
    private Button standardSearchGermplasmList;
    
    private Label testSearchGermplasmLabel;
    private Label standardSearchGermplasmLabel;
    
    private Label testSearchGermplasmListLabel;
    private Label standardSearchGermplasmListLabel;
    
    private Label headerLabel;
    
    private HeadToHeadCrossStudyMain mainScreen;
    private TraitsAvailableComponent nextScreen;
    private ResultsComponent resultsScreen;
    
    private Table entriesTable;
    
    private Integer lastTestEntryGID;
    private Integer lastStandardEntryGID;
    
    private static final String TEST_ENTRY_COLUMN_ID = "SpecifyGermplasmsComponent Test Entry Column Id";
    private static final String STANDARD_ENTRY_COLUMN_ID = "SpecifyGermplasmsComponent Standard Entry Column Id";
    
    private static final Action ACTION_SELECT_ALL = new Action("Select All");
    private static final Action ACTION_DELETE = new Action("Delete selected");
    private static final Action[] ACTIONS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE };
   
    private Object tableRowItem = null;
    
    private List<GermplasmList> testGermplasmListAdded = new ArrayList(); //can be a germplasm or a germplasmList
    private List<Germplasm> testGermplasmAdded = new ArrayList(); //can be a germplasm or a germplasmList
    private List<GermplasmList> standardGermplasmListAdded = new ArrayList(); //can be a germplasm or a germplasmList
    private List<Germplasm> standardGermplasmAdded = new ArrayList(); //can be a germplasm or a germplasmList
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    private Map<String, TablesEntries> mapTableEntriesId = new HashMap();
    private Map<String, TablesEntries> singleEntriesSet = new HashMap();
    
    public SpecifyGermplasmsComponent(HeadToHeadCrossStudyMain mainScreen, TraitsAvailableComponent nextScreen
            , ResultsComponent resultScreen){
        this.mainScreen = mainScreen;
        this.nextScreen = nextScreen;
        this.resultsScreen = resultScreen;
        this.lastTestEntryGID = null;
        this.lastStandardEntryGID = null;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("600px");
        setWidth("1000px");
        
        testPanel = new Panel("Test");
        testPanel.setWidth("400px");
        testPanel.setSizeUndefined();
        
        standardPanel = new Panel("STANDARD");
        standardPanel.setWidth("470px");
        testPanel.setSizeUndefined();
        
        
        AbsoluteLayout absLayout = new AbsoluteLayout();
        absLayout.setWidth("400px");
        absLayout.setHeight("90px");
        
        testSearchGermplasmLabel = new Label("Specify a single test entry");
        testSearchGermplasmLabel.setImmediate(true);
        
        testSearchGermplasmListLabel = new Label("Or, specify a list containing test entries");
        testSearchGermplasmListLabel.setImmediate(true);
        
        testSearchGermplasm = new Button("Search Germplasm");
        testSearchGermplasm.setData(SELECT_TEST_SEARCH_GERMPLASM_BUTTON_ID);
        testSearchGermplasm.setWidth("150px");
        testSearchGermplasm.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        
        testSearchGermplasmList = new Button("Browse List");
        testSearchGermplasmList.setData(SELECT_TEST_SEARCH_GERMPLASM_LIST_BUTTON_ID);
        testSearchGermplasmList.setWidth("150px");
        testSearchGermplasmList.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        
        absLayout.addComponent(testSearchGermplasmLabel, "top:13px;left:2px");
        absLayout.addComponent(testSearchGermplasmListLabel, "top:53px;left:2px");
        absLayout.addComponent(testSearchGermplasm, "top:10px;left:220px");
        absLayout.addComponent(testSearchGermplasmList, "top:50px;left:220px");
        testPanel.addComponent(absLayout);
        
        AbsoluteLayout absLayoutStandard = new AbsoluteLayout();
        absLayoutStandard.setWidth("450px");
        absLayoutStandard.setHeight("90px");
        
        standardSearchGermplasmLabel = new Label("Specify a standard test");
        standardSearchGermplasmLabel.setImmediate(true);
        
        standardSearchGermplasmListLabel = new Label("Or, specify a list containing standard entries");
        standardSearchGermplasmListLabel.setImmediate(true);
        
        standardSearchGermplasm = new Button("Search Germplasm");
        standardSearchGermplasm.setData(SELECT_STANDARD_SEARCH_GERMPLASM_BUTTON_ID);
        standardSearchGermplasm.setWidth("150px");
        standardSearchGermplasm.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        
        standardSearchGermplasmList = new Button("Browse List");
        standardSearchGermplasmList.setData(SELECT_STANDARD_SEARCH_GERMPLASM_LIST_BUTTON_ID);
        standardSearchGermplasmList.setWidth("150px");
        standardSearchGermplasmList.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        
        //absLayout.addComponent(specifyTestEntryLabel, "top:20px;left:30px");
        absLayoutStandard.addComponent(standardSearchGermplasmLabel, "top:13px;left:2px");
        absLayoutStandard.addComponent(standardSearchGermplasmListLabel, "top:53px;left:2px");
        absLayoutStandard.addComponent(standardSearchGermplasm, "top:10px;left:250px");
        absLayoutStandard.addComponent(standardSearchGermplasmList, "top:50px;left:250px");
        standardPanel.addComponent(absLayoutStandard);
        
        
        headerLabel = new Label("Select the test and standard entries to be compared");
        headerLabel.setImmediate(true);
        
        addComponent(headerLabel, "top:10px;left:10px");
        addComponent(testPanel, "top:30px;left:10px");
        addComponent(standardPanel, "top:30px;left:460px");
        
        entriesTable = new Table();
        entriesTable.setWidth("900px");
        entriesTable.setHeight("330px");
        entriesTable.setImmediate(true);
        
        entriesTable.addContainerProperty(TEST_ENTRY_COLUMN_ID, String.class, null);
        entriesTable.addContainerProperty(STANDARD_ENTRY_COLUMN_ID, String.class, null);
        
        entriesTable.setColumnHeader(TEST_ENTRY_COLUMN_ID, "Test Entry");
        entriesTable.setColumnHeader(STANDARD_ENTRY_COLUMN_ID, "Standard Entry");
        
        entriesTable.setSelectable(true);
        entriesTable.setMultiSelect(true);
        entriesTable.setNullSelectionAllowed(false);
        entriesTable.setImmediate(true);
        
        entriesTable.addActionHandler(new Action.Handler() {
            public Action[] getActions(Object target, Object sender) {
                    return ACTIONS_TABLE_CONTEXT_MENU;
            }

            public void handleAction(Action action, Object sender, Object target) {
                if (ACTION_DELETE == action) {
                    deleteEntriesAction();
                }
                else if (ACTION_SELECT_ALL == action) {
                	entriesTable.setValue(entriesTable.getItemIds());
                	entriesTable.setPageLength(0);
                }
                tableRowItem = null;
            }
        });
        
      
        
        setDummyTableData();
        addComponent(entriesTable, "top:200px;left:20px");
        
        /*
        testEntryLabel = new Label();
        testEntryLabel.setWidth("200px");
        testEntryLabel.setImmediate(true);
        addComponent(testEntryLabel, "top:20px;left:150px");
       */ 
        /*
        specifyStandardEntryLabel = new Label("Specify a standard entry:");
        addComponent(specifyStandardEntryLabel, "top:20px;left:450px");
        
        standardEntryLabel = new Label();
        standardEntryLabel.setWidth("200px");
        standardEntryLabel.setImmediate(true);
        addComponent(standardEntryLabel, "top:20px;left:600px");
        
        selectTestEntryButton = new Button("Select test entry");
        selectTestEntryButton.setData(SELECT_TEST_ENTRY_BUTTON_ID);
        selectTestEntryButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        addComponent(selectTestEntryButton, "top:70px;left:170px");
        
        selectStandardEntryButton = new Button("Select standard entry");
        selectStandardEntryButton.setData(SELECT_STANDARD_ENTRY_BUTTON_ID);
        selectStandardEntryButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        addComponent(selectStandardEntryButton, "top:70px;left:610px");
        */
        
        nextButton = new Button("Next");
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        addComponent(nextButton, "top:550px;left:900px");
        
        addTestGermplasmList(germplasmListManager.getGermplasmListById(1));
        addStandardGermplasmList(germplasmListManager.getGermplasmListById(1));
    }

    private void deleteEntriesAction(){
        final Collection<?> selectedIds = (Collection<?>) entriesTable.getValue();
        if (!selectedIds.isEmpty()){
            for (Object itemId : selectedIds){
            	entriesTable.removeItem(itemId);
            	mapTableEntriesId.remove(itemId);
            }
            entriesTable.setPageLength(0);
        } else {
           ;// MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_CROSS_MUST_BE_SELECTED), Notification.POSITION_CENTERED);
        }
        
        if(isTableEntriesEmpty()){
        	//we set the new set since we already cleared it
			singleEntriesSet = new HashMap();
        }
       
    }
    
    public void setDummyTableData(){
    	/*
    	for(int i = 0 ; i < 100 ; i++){
    		entriesTable.addItem(new Object[] {"test - "+i, "standard - "+i}, Integer.toString(i));
    		
    	}
    	*/
    	try{
    		
	    	//addTestGermplasmList(germplasmListManager.getGermplasmListById(-1));
    		addStandardGermplasmList(germplasmListManager.getGermplasmListById(-1));
    		//addTestGermplasmList(germplasmListManager.getGermplasmListById(-2));
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void selectTestEntryButtonClickAction(){
        Window parentWindow = this.getWindow();
        SelectAGermplasmDialog selectAGermplasmDialog = new SelectAGermplasmDialog(this, parentWindow, testEntryLabel);
        parentWindow.addWindow(selectAGermplasmDialog);
    }
    
    public void selectStandardEntryButtonClickAction(){
        Window parentWindow = this.getWindow();
        SelectAGermplasmDialog selectAGermplasmDialog = new SelectAGermplasmDialog(this, parentWindow, standardEntryLabel);
        parentWindow.addWindow(selectAGermplasmDialog);
    }
    
    public void nextButtonClickAction(){
        /*
        if(this.testEntryLabel.getData() == null){
            MessageNotifier.showWarning(getWindow(), "Warning!", "Need to specify a test entry. Please use the Select test entry button.", Notification.POSITION_CENTERED);
            return;
        }
        
        if(this.standardEntryLabel.getData() == null){
            MessageNotifier.showWarning(getWindow(), "Warning!", "Need to specify a standard entry. Please use the Select standard entry button.", Notification.POSITION_CENTERED);
            return;
        }
        
        Integer testEntryGID = (Integer) testEntryLabel.getData();
        Integer standardEntryGID = (Integer) standardEntryLabel.getData();
        
        if(this.nextScreen != null){
            if(areCurrentGIDsDifferentFromLast(testEntryGID, standardEntryGID)){
                this.resultsScreen.setEntriesLabel((String) testEntryLabel.getValue(),(String) standardEntryLabel.getValue());
                this.nextScreen.populateTraitsAvailableTable(testEntryGID, standardEntryGID);
                this.lastTestEntryGID = testEntryGID;
                this.lastStandardEntryGID = standardEntryGID;
            }
            this.mainScreen.selectSecondTab();
        }
        */
    	//we check the chosen rows
    	Iterator iter = entriesTable.getItemIds().iterator();
		boolean hasLeftBlank = false;
		boolean hasRightBlank = false;
		while(iter.hasNext()){
			//we iterate and permutate against the list
			String id = (String)iter.next();
			String leftId = "";
			String rightId = "";
			StringTokenizer tokenizer = new StringTokenizer(id,":");
			if(tokenizer.countTokens() == 2){
				leftId = tokenizer.nextToken().trim();
				rightId = tokenizer.nextToken().trim();
			}
			if(leftId.equalsIgnoreCase("")){
				hasLeftBlank = true;
				break;
			}
			if(rightId.equalsIgnoreCase("")){
				hasRightBlank = true;
				break;
			}
		}
		if(hasLeftBlank){
			MessageNotifier.showWarning(getWindow(), "Warning!", "There should be at least one Test entry.", Notification.POSITION_CENTERED);
			return;
		}else if(hasRightBlank){
			MessageNotifier.showWarning(getWindow(), "Warning!", "There should be at least one Standard entry.", Notification.POSITION_CENTERED);
			return;
		}
        if(this.nextScreen != null){
            this.mainScreen.selectSecondTab();
        }
    }
    
    private boolean areCurrentGIDsDifferentFromLast(Integer currentTestEntryGID, Integer currentStandardEntryGID){
        if(this.lastTestEntryGID != null && this.lastStandardEntryGID != null){
            if(this.lastTestEntryGID == currentTestEntryGID && this.lastStandardEntryGID == currentStandardEntryGID){
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
        
    }
    
    public void addTestGermplasmList(GermplasmList germplasmList){  
    	if(germplasmList != null){
	    	//testGermplasmListAdded.add(germplasmList);
	    	//doGermplasmPermutation0(germplasmList, standardGermplasmListAdded);
	    	//doGermplasmPermutation1(germplasmList, standardGermplasmAdded);
	    	doGermplasmPermutationOnTable(true, false, germplasmList, null);
    	}
    }
    public void addTestGermplasm(Germplasm germplasm ){  
    	if(germplasm != null){
	    	//testGermplasmAdded.add(germplasm);
	    	//doGermplasmPermutation2(germplasm, standardGermplasmListAdded);
	    	//doGermplasmPermutation3(germplasm, standardGermplasmAdded);
    		doGermplasmPermutationOnTable(true, true, null, germplasm);
    	}
    }
    public void addStandardGermplasmList(GermplasmList germplasmList){
    	if(germplasmList != null){
	    	//standardGermplasmListAdded.add(germplasmList);
	    	//doGermplasmPermutation4(testGermplasmListAdded, germplasmList);
	    	//doGermplasmPermutation5(testGermplasmAdded, germplasmList);
    		doGermplasmPermutationOnTable(false, false, germplasmList, null);
    	}
    }
    public void addStandardGermplasm(Germplasm germplasm){
    	if(germplasm != null){
	    	//standardGermplasmAdded.add(germplasm);
	    	//doGermplasmPermutation6(testGermplasmListAdded, germplasm);
	    	//doGermplasmPermutation7(testGermplasmAdded, germplasm);
    		doGermplasmPermutationOnTable(false, true, null, germplasm);
    	}
    }
    
    private void doGermplasmPermutationOnTable(boolean isTestEntry, boolean isGermplasm, GermplasmList germplasmList, Germplasm germplasm ){
    	List<TablesEntries> tableEntriesList = new ArrayList();
    	if(isGermplasm){
    		//meaning we use the variable germplasm
    		if(isTableEntriesEmpty()){
    			TablesEntries tableEntry = new TablesEntries();
	    		String testEntryName = "";
	    		String standardEntryName = "";
	    		String newId = "";
	    		if(isTestEntry){
	    			testEntryName = germplasm.getPreferredName() != null ? germplasm.getPreferredName().getNval() : germplasm.getGid().toString();
	    			standardEntryName = "";
	    			newId = germplasm.getGid().toString() + ": ";
	    		}else{
	    			testEntryName = "";
	    			standardEntryName = germplasm.getPreferredName() != null ? germplasm.getPreferredName().getNval() : germplasm.getGid().toString();
	    			newId = " :"+germplasm.getGid().toString();
	    		}
	    		
	    		tableEntry.setTestEntryName(testEntryName);
	    		tableEntry.setStandardEntryName(standardEntryName);
	    		tableEntry.setTestStandardEntry(newId);
	    		tableEntriesList.add(tableEntry);
				singleEntriesSet.put(newId, tableEntry);
    		}else{
    			Iterator iter = entriesTable.getItemIds().iterator();
    			boolean isBlank = false;
    			while(iter.hasNext()){
    				//we iterate and permutate against the list
    				TablesEntries tableEntry = new TablesEntries();
    				String id = (String)iter.next();
    				String leftId = "";
    				String rightId = "";
    				StringTokenizer tokenizer = new StringTokenizer(id,":");
    				if(tokenizer.countTokens() == 2){
    					leftId = tokenizer.nextToken().trim();
    					rightId = tokenizer.nextToken().trim();
    				}
    				Item item = entriesTable.getItem(id);
    				String testEntryName = (String)item.getItemProperty(TEST_ENTRY_COLUMN_ID).getValue();
    				String standardEntryName = (String)item.getItemProperty(STANDARD_ENTRY_COLUMN_ID).getValue();
    				String newTestEntryName = "";
    				String newStandardEntryName = "";
    	    		String newId = "";
    	    		
    				if(isTestEntry){
    					//We need to permutate against the standard
    					newTestEntryName = germplasm.getPreferredName() != null ? germplasm.getPreferredName().getNval() : germplasm.getGid().toString();
    					newStandardEntryName = standardEntryName;
    	    			//newId = germplasm.getGid().toString() + id.trim(); //since id already has -number
    	    			if(testEntryName.equalsIgnoreCase("")){
    	    				isBlank = true;
    	    				newId = germplasm.getGid().toString() + ": ";
    	    				if(!standardEntryName.equalsIgnoreCase("")){
    	    					newId =  germplasm.getGid().toString() + ":" +(rightId.trim().equalsIgnoreCase("") ? " " : rightId.trim());
    	    				}
    	    			}else{
    	    				newId = germplasm.getGid().toString() + ":" + (rightId.trim().equalsIgnoreCase("") ? " " : rightId.trim());    	    				
    	    			}
    	    		}else{
    	    			//we need to permutate against the test entry
    	    			newTestEntryName = testEntryName;
    	    			newStandardEntryName = germplasm.getPreferredName() != null ? germplasm.getPreferredName().getNval() : germplasm.getGid().toString();
    	    			//newId = id.trim()+germplasm.getGid().toString();
    	    			if(standardEntryName.equalsIgnoreCase("")){
    	    				isBlank = true;
    	    				newId =  " :" + germplasm.getGid().toString();
    	    				if(!testEntryName.equalsIgnoreCase("")){
    	    					newId =  (leftId.trim().equalsIgnoreCase("") ? " " : leftId.trim()) + ":" + germplasm.getGid().toString();
    	    				}
    	    			}else{
    	    				newId =  (leftId.trim().equalsIgnoreCase("") ? " " : leftId.trim()) + ":" + germplasm.getGid().toString();
    	    			}
    	    		}
    				tableEntry.setTestEntryName(newTestEntryName);
    	    		tableEntry.setStandardEntryName(newStandardEntryName);
    	    		tableEntry.setTestStandardEntry(newId);
    	    		tableEntriesList.add(tableEntry);
    			}
    			if(isBlank == true)
    				deleteAllSingleEntriesInTable();
    		}
    	}else{
    		//we use the the variable germplasmlist
    		List<GermplasmListData> germplasmListData = germplasmList.getListData();
    		
    			if(isTableEntriesEmpty()){
    				for(GermplasmListData listData : germplasmListData){
	    				TablesEntries tableEntry = new TablesEntries();
	    	    		String testEntryName = "";
	    	    		String standardEntryName = "";
	    	    		String newId = "";
	    	    		if(isTestEntry){
	    	    			testEntryName = listData.getDesignation() != null ? listData.getDesignation() : listData.getGid().toString();
	    	    			standardEntryName = "";
	    	    			newId = listData.getGid().toString() + ": ";
	    	    		}else{
	    	    			testEntryName = "";
	    	    			standardEntryName = listData.getDesignation() != null ? listData.getDesignation() : listData.getGid().toString();
	    	    			newId = " :"+listData.getGid().toString();
	    	    		}
	    	    		
	    	    		tableEntry.setTestEntryName(testEntryName);
	    	    		tableEntry.setStandardEntryName(standardEntryName);
	    	    		tableEntry.setTestStandardEntry(newId);
	    	    		tableEntriesList.add(tableEntry);
	    	    		singleEntriesSet.put(newId, tableEntry);
    				}
    			}else{
    				
	    			Iterator iter = entriesTable.getItemIds().iterator();
	    			boolean isBlank = false;
	    			while(iter.hasNext()){
	    				String id = (String)iter.next();
	    				StringTokenizer tokenizer = new StringTokenizer(id, ":");
	    				String leftId = "";
	    				String rightId = "";
	    				Item item = entriesTable.getItem(id);
	    				
	    				if(tokenizer.countTokens() == 2){
	    					leftId = tokenizer.nextToken().trim();
	    					rightId = tokenizer.nextToken().trim();
	    				}
	    				String testEntryName = (String)item.getItemProperty(TEST_ENTRY_COLUMN_ID).getValue();
	    				String standardEntryName = (String)item.getItemProperty(STANDARD_ENTRY_COLUMN_ID).getValue();
	    				
	    				for(GermplasmListData listData : germplasmListData){
		    				//we iterate and permutate against the list
		    				TablesEntries tableEntry = new TablesEntries();		    						    						    						    						    			
		    				String newTestEntryName = "";
		    				String newStandardEntryName = "";
		    	    		String newId = "";
		    				if(isTestEntry){
		    					//We need to permutate against the standard
		    					newTestEntryName = listData.getDesignation() != null ? listData.getDesignation() : listData.getGid().toString();
		    					newStandardEntryName = standardEntryName;
		    	    			//newId = listData.getGid().toString() + id.trim(); //since id already has -number
		    	    			if(testEntryName.equalsIgnoreCase("")){
		    	    				isBlank = true;
		    	    				newId =  listData.getGid().toString() + ": ";
		    	    				if(!standardEntryName.equalsIgnoreCase("")){
		    	    					newId =  listData.getGid().toString() + ":" +(rightId.trim().equalsIgnoreCase("") ? " " : rightId.trim());
		    	    				}
		    	    			}else{
		    	    				newId =  listData.getGid().toString() + ":" + (rightId.trim().equalsIgnoreCase("") ? " " : rightId.trim());		    	    				
		    	    			}
		    	    		}else{
		    	    			//we need to permutate against the test entry
		    	    			newTestEntryName = testEntryName;
		    	    			newStandardEntryName = listData.getDesignation() != null ? listData.getDesignation() : listData.getGid().toString();
		    	    			//newId = id.trim()+listData.getGid().toString();
		    	    			if(standardEntryName.equalsIgnoreCase("")){
		    	    				isBlank = true;
		    	    				newId =  " :" + listData.getGid().toString();
		    	    				if(!testEntryName.equalsIgnoreCase("")){
		    	    					newId =  (leftId.trim().equalsIgnoreCase("") ? " " : leftId.trim()) + ":" + listData.getGid().toString();
		    	    				}
		    	    			}else{
		    	    				newId =  (leftId.trim().equalsIgnoreCase("") ? " " : leftId.trim()) + ":" + listData.getGid().toString();
		    	    			}
		    	    		}
		    				tableEntry.setTestEntryName(newTestEntryName);
		    	    		tableEntry.setStandardEntryName(newStandardEntryName);
		    	    		tableEntry.setTestStandardEntry(newId);
		    	    		tableEntriesList.add(tableEntry);
		    			}
	    			}
	    			if(isBlank == true)
	    				deleteAllSingleEntriesInTable();
    			}
    		
    	}
    	addToTable(tableEntriesList);
    	
    }
    
    private void deleteAllSingleEntriesInTable(){
    	//we delete the single entrie
		Iterator singleIter = singleEntriesSet.keySet().iterator();
		while(singleIter.hasNext()){
			String idToDelete = (String)singleIter.next();
			entriesTable.removeItem(idToDelete);	    				
		}
		//we set the new set since we already cleared it
		singleEntriesSet = new HashMap();
    }
    /*
    private void doGermplasmPermutation0(GermplasmList testEntries, List<GermplasmList> standardEntries){
    	//testEntries.getl
    	List<GermplasmListData> listData = testEntries.getListData();
    	List<TablesEntries> tableEntriesList = new ArrayList();
    	for(GermplasmListData germplasmListData : listData){
    		for(GermplasmList germplasmList : standardEntries){
    			
    			List<GermplasmListData> standardListData = germplasmList.getListData();
    			for(GermplasmListData standardGermplasmListData : standardListData){
    				TablesEntries tableEntry = new TablesEntries();
    	    		String testEntryName = germplasmListData.getDesignation() != null ? germplasmListData.getDesignation() : germplasmListData.getGid().toString();
    	    		String standardEntryName = standardGermplasmListData.getDesignation() != null ? standardGermplasmListData.getDesignation() : standardGermplasmListData.getGid().toString();
    	    		tableEntry.setTestEntryName(testEntryName);
    	    		tableEntry.setStandardEntryName(standardEntryName);
    	    		tableEntry.setTestStandardEntry(germplasmListData.getGid().toString() + "-" + standardGermplasmListData.getGid().toString());
    	    		tableEntriesList.add(tableEntry);
    				
    			}
	    		
    		}
    		
    		if(tableEntriesList.isEmpty()){
        		TablesEntries tableEntry = new TablesEntries();
        		String testEntryName = germplasmListData.getDesignation() != null ? germplasmListData.getDesignation() : germplasmListData.getGid().toString();
        		String standardEntryName = "";
        		tableEntry.setTestEntryName(testEntryName);
        		tableEntry.setStandardEntryName(standardEntryName);
        		tableEntry.setTestStandardEntry(germplasmListData.getGid().toString() + "-" + "");
        		tableEntriesList.add(tableEntry);
        	}
    	}
    	
    	addToTable(tableEntriesList);
    }
    private void doGermplasmPermutation1(GermplasmList testEntries, List<Germplasm> standardEntry){
    	List<GermplasmListData> listData = testEntries.getListData();
    	List<TablesEntries> tableEntriesList = new ArrayList();
    	for(GermplasmListData germplasmListData : listData){
    		for(Germplasm germplasm : standardEntry){
	    		TablesEntries tableEntry = new TablesEntries();
	    		String testEntryName = germplasmListData.getDesignation() != null ? germplasmListData.getDesignation() : germplasmListData.getGid().toString();
	    		String standardEntryName = germplasm.getPreferredName() != null ? germplasm.getPreferredName().getNval() : germplasm.getGid().toString();
	    		tableEntry.setTestEntryName(testEntryName);
	    		tableEntry.setStandardEntryName(standardEntryName);
	    		tableEntry.setTestStandardEntry(germplasmListData.getGid().toString() + "-" + germplasm.getGid().toString());
	    		tableEntriesList.add(tableEntry);
    		}
    		
    		if(tableEntriesList.isEmpty()){
        		TablesEntries tableEntry = new TablesEntries();
        		String testEntryName = germplasmListData.getDesignation() != null ? germplasmListData.getDesignation() : germplasmListData.getGid().toString();
        		String standardEntryName = "";
        		tableEntry.setTestEntryName(testEntryName);
        		tableEntry.setStandardEntryName(standardEntryName);
        		tableEntry.setTestStandardEntry(germplasmListData.getGid().toString() + "-" + "");
        		tableEntriesList.add(tableEntry);
        	}
    	}
    	addToTable(tableEntriesList);
    	
    }
    private void doGermplasmPermutation2(Germplasm testEntry,  List<GermplasmList> standardEntries){
    	List<TablesEntries> tableEntriesList = new ArrayList();
    	for(GermplasmList germplasmList : standardEntries){			
			List<GermplasmListData> standardListData = germplasmList.getListData();
			for(GermplasmListData standardGermplasmListData : standardListData){
				TablesEntries tableEntry = new TablesEntries();
	    		String testEntryName = testEntry.getPreferredName() != null ? testEntry.getPreferredName().getNval() : testEntry.getGid().toString();
	    		String standardEntryName = standardGermplasmListData.getDesignation() != null ? standardGermplasmListData.getDesignation() : standardGermplasmListData.getGid().toString();
	    		tableEntry.setTestEntryName(testEntryName);
	    		tableEntry.setStandardEntryName(standardEntryName);
	    		tableEntry.setTestStandardEntry(testEntry.getGid().toString() + "-" + standardGermplasmListData.getGid().toString());
	    		tableEntriesList.add(tableEntry);
				
			}
			
			
    		
		}
    	if(tableEntriesList.isEmpty()){
    		TablesEntries tableEntry = new TablesEntries();
    		String testEntryName = testEntry.getPreferredName() != null ? testEntry.getPreferredName().getNval() : testEntry.getGid().toString();
    		String standardEntryName = "";
    		tableEntry.setTestEntryName(testEntryName);
    		tableEntry.setStandardEntryName(standardEntryName);
    		tableEntry.setTestStandardEntry(testEntry.getGid().toString() + "-" + "");
    		tableEntriesList.add(tableEntry);
    	}
    	addToTable(tableEntriesList);
    }
    private void doGermplasmPermutation3(Germplasm testEntry,  List<Germplasm> standardEntry){
    	List<TablesEntries> tableEntriesList = new ArrayList();
    	for(Germplasm germplasm : standardEntry){
    		TablesEntries tableEntry = new TablesEntries();
    		String testEntryName = testEntry.getPreferredName() != null ? testEntry.getPreferredName().getNval() : testEntry.getGid().toString();
    		String standardEntryName = germplasm.getPreferredName() != null ? germplasm.getPreferredName().getNval() : germplasm.getGid().toString();
    		tableEntry.setTestEntryName(testEntryName);
    		tableEntry.setStandardEntryName(standardEntryName);
    		tableEntry.setTestStandardEntry(testEntry.getGid().toString() + "-" + germplasm.getGid().toString());
    		tableEntriesList.add(tableEntry);
    	}
    	if(tableEntriesList.isEmpty()){
    		TablesEntries tableEntry = new TablesEntries();
    		String testEntryName = testEntry.getPreferredName() != null ? testEntry.getPreferredName().getNval() : testEntry.getGid().toString();
    		String standardEntryName = "";
    		tableEntry.setTestEntryName(testEntryName);
    		tableEntry.setStandardEntryName(standardEntryName);
    		tableEntry.setTestStandardEntry(testEntry.getGid().toString() + "-" + "");
    		tableEntriesList.add(tableEntry);
    	}
    	addToTable(tableEntriesList);
    }
    
    private void doGermplasmPermutation4(List<GermplasmList> testEntries, GermplasmList standardEntries){
    	List<TablesEntries> tableEntriesList = new ArrayList();
    	for(GermplasmList testGermplasmList : testEntries){
    		List<GermplasmListData> testListData = testGermplasmList.getListData();
    		for(GermplasmListData testGermplasmListData : testListData){
    			List<GermplasmListData> standardListDataList = standardEntries.getListData();
    			for(GermplasmListData standardListData : standardListDataList){
    				TablesEntries tableEntry = new TablesEntries();
    	    		String testEntryName = testGermplasmListData.getDesignation() != null ? testGermplasmListData.getDesignation() : testGermplasmListData.getGid().toString();
    	    		String standardEntryName = standardListData.getDesignation() != null ? standardListData.getDesignation() : standardListData.getGid().toString();
    	    		tableEntry.setTestEntryName(testEntryName);
    	    		tableEntry.setStandardEntryName(standardEntryName);
    	    		tableEntry.setTestStandardEntry(testGermplasmListData.getGid().toString() + "-" + standardListData.getGid().toString());
    	    		tableEntriesList.add(tableEntry);
    				
    			}
    		}
    	}
    	if(tableEntriesList.isEmpty()){
    		List<GermplasmListData> standardListDataList = standardEntries.getListData();
			for(GermplasmListData standardListData : standardListDataList){
				TablesEntries tableEntry = new TablesEntries();
	    		String testEntryName = "";
	    		String standardEntryName = standardListData.getDesignation() != null ? standardListData.getDesignation() : standardListData.getGid().toString();
	    		tableEntry.setTestEntryName(testEntryName);
	    		tableEntry.setStandardEntryName(standardEntryName);
	    		tableEntry.setTestStandardEntry("" + "-" + standardListData.getGid().toString());
	    		tableEntriesList.add(tableEntry);
				
			}
    	}
    	addToTable(tableEntriesList);
    }
    private void doGermplasmPermutation5(List<Germplasm> testEntry, GermplasmList standardEntries){
    	List<TablesEntries> tableEntriesList = new ArrayList();
    	for(Germplasm testGermplasm : testEntry){
    		List<GermplasmListData> standardListDataList = standardEntries.getListData();
    		for(GermplasmListData standardGermplasmListData : standardListDataList){
    			TablesEntries tableEntry = new TablesEntries();
	    		String testEntryName = testGermplasm.getPreferredName() != null ? testGermplasm.getPreferredName().getNval() : testGermplasm.getGid().toString();
	    		String standardEntryName = standardGermplasmListData.getDesignation() != null ? standardGermplasmListData.getDesignation() : standardGermplasmListData.getGid().toString();
	    		tableEntry.setTestEntryName(testEntryName);
	    		tableEntry.setStandardEntryName(standardEntryName);
	    		tableEntry.setTestStandardEntry(testGermplasm.getGid().toString() + "-" + standardGermplasmListData.getGid().toString());
	    		tableEntriesList.add(tableEntry);
    		}
    	}
    	if(tableEntriesList.isEmpty()){
    		List<GermplasmListData> standardListDataList = standardEntries.getListData();
			for(GermplasmListData standardListData : standardListDataList){
				TablesEntries tableEntry = new TablesEntries();
	    		String testEntryName = "";
	    		String standardEntryName = standardListData.getDesignation() != null ? standardListData.getDesignation() : standardListData.getGid().toString();
	    		tableEntry.setTestEntryName(testEntryName);
	    		tableEntry.setStandardEntryName(standardEntryName);
	    		tableEntry.setTestStandardEntry("" + "-" + standardListData.getGid().toString());
	    		tableEntriesList.add(tableEntry);
				
			}
    	}
    	addToTable(tableEntriesList);
    }
    private void doGermplasmPermutation6(List<GermplasmList> testEntries,  Germplasm standardEntry){
    	List<TablesEntries> tableEntriesList = new ArrayList();
    	for(GermplasmList testGermplasmList : testEntries){
    		List<GermplasmListData> testListData = testGermplasmList.getListData();
    		for(GermplasmListData testGermplasmListData : testListData){
    			
    				TablesEntries tableEntry = new TablesEntries();
    	    		String testEntryName = testGermplasmListData.getDesignation() != null ? testGermplasmListData.getDesignation() : testGermplasmListData.getGid().toString();
    	    		String standardEntryName = standardEntry.getPreferredName() != null ? standardEntry.getPreferredName().getNval() : standardEntry.getGid().toString();
    	    		tableEntry.setTestEntryName(testEntryName);
    	    		tableEntry.setStandardEntryName(standardEntryName);
    	    		tableEntry.setTestStandardEntry(testGermplasmListData.getGid().toString() + "-" + standardEntry.getGid().toString());
    	    		tableEntriesList.add(tableEntry);
    				
    			
    		}
    	}
    	if(tableEntriesList.isEmpty()){
    		
				TablesEntries tableEntry = new TablesEntries();
	    		String testEntryName = "";
	    		String standardEntryName = standardEntry.getPreferredName() != null ? standardEntry.getPreferredName().getNval() : standardEntry.getGid().toString();
	    		tableEntry.setTestEntryName(testEntryName);
	    		tableEntry.setStandardEntryName(standardEntryName);
	    		tableEntry.setTestStandardEntry("" + "-" + standardEntry.getGid().toString());
	    		tableEntriesList.add(tableEntry);
				
			
    	}
    	
    	addToTable(tableEntriesList);
    }
    private void doGermplasmPermutation7(List<Germplasm> testEntry,  Germplasm standardEntry){
    	List<TablesEntries> tableEntriesList = new ArrayList();
    	for(Germplasm testGermplasm : testEntry){
    		
    			TablesEntries tableEntry = new TablesEntries();
	    		String testEntryName = testGermplasm.getPreferredName() != null ? testGermplasm.getPreferredName().getNval() : testGermplasm.getGid().toString();
	    		String standardEntryName = standardEntry.getPreferredName() != null ? standardEntry.getPreferredName().getNval() : standardEntry.getGid().toString();
	    		tableEntry.setTestEntryName(testEntryName);
	    		tableEntry.setStandardEntryName(standardEntryName);
	    		tableEntry.setTestStandardEntry(testGermplasm.getGid().toString() + "-" + standardEntry.getGid().toString());
	    		tableEntriesList.add(tableEntry);
    		
    	}
    	if(tableEntriesList.isEmpty()){
    		
			TablesEntries tableEntry = new TablesEntries();
    		String testEntryName = "";
    		String standardEntryName = standardEntry.getPreferredName() != null ? standardEntry.getPreferredName().getNval() : standardEntry.getGid().toString();
    		tableEntry.setTestEntryName(testEntryName);
    		tableEntry.setStandardEntryName(standardEntryName);
    		tableEntry.setTestStandardEntry("" + "-" + standardEntry.getGid().toString());
    		tableEntriesList.add(tableEntry);
			
		}
    	addToTable(tableEntriesList);
    }
    */
    private void addToTable(List<TablesEntries> tableEntryList){
    	for(TablesEntries tableEntry : tableEntryList){
    		String newId = tableEntry.getTestStandardEntry();    		
	    	//if not in map, add it in the table
	    	if(!mapTableEntriesId.containsKey(newId)){
		    	
		    		entriesTable.addItem(new Object[] {tableEntry.getTestEntryName(), tableEntry.getStandardEntryName()}, tableEntry.getTestStandardEntry());
		    		mapTableEntriesId.put(newId, tableEntry);
		    	
	    	}
    	}
    }
    private boolean isTableEntriesEmpty(){
    	return entriesTable.getItemIds().isEmpty();
    }
    
    
}
