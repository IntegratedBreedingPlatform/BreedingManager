package  org.generationcp.browser.cross.study.h2h.main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.main.dialogs.SelectGermplasmEntryDialog;
import org.generationcp.browser.cross.study.h2h.main.dialogs.SelectGermplasmListDialog;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.TablesEntries;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

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
    
    private static final BigInteger MAX_NUM_OF_PAIRS = new BigInteger("1000000"); // 100,000 maximum
    
    private Panel testPanel;
    private Panel standardPanel;
    
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
    
    private Table entriesTable;
    
    private static final String TEST_ENTRY_COLUMN_ID = "SpecifyGermplasmsComponent Test Entry Column Id";
    private static final String STANDARD_ENTRY_COLUMN_ID = "SpecifyGermplasmsComponent Standard Entry Column Id";
    
    private static final Action ACTION_SELECT_ALL = new Action("Select All");
    private static final Action ACTION_DELETE = new Action("Delete selected");
    private static final Action[] ACTIONS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE };
   
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    private Set<String> tableEntriesId = new HashSet<String>();
    private Set<String> singleEntriesSet = new HashSet<String>();
    
    private String TEST_ENTRY = "TEST";
    private String STANDARD_ENTRY = "STANDARD";
    
    private Map<String, String> germplasmIdNameMap;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public SpecifyGermplasmsComponent(HeadToHeadCrossStudyMain mainScreen, TraitsAvailableComponent nextScreen){
        this.mainScreen = mainScreen;
        this.nextScreen = nextScreen;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("600px");
        setWidth("1000px");
        
        testPanel = new Panel("TEST");
        testPanel.setWidth("400px");
        testPanel.setSizeUndefined();
        
        standardPanel = new Panel("STANDARD");
        standardPanel.setWidth("470px");
        testPanel.setSizeUndefined();
        
        AbsoluteLayout absLayout = new AbsoluteLayout();
        absLayout.setWidth("400px");
        absLayout.setHeight("90px");
        
        testSearchGermplasmLabel = new Label(messageSource.getMessage(Message.SPECIFY_SINGLE_TEST_ENTRY));
        testSearchGermplasmLabel.setImmediate(true);
        
        testSearchGermplasmListLabel = new Label(messageSource.getMessage(Message.SPECIFY_TEST_GERMPLASM_LIST_ENTRY));
        testSearchGermplasmListLabel.setImmediate(true);
        
        testSearchGermplasm = new Button(messageSource.getMessage(Message.HEAD_TO_HEAD_SEARCH_GERMPLASM));
        testSearchGermplasm.setData(SELECT_TEST_SEARCH_GERMPLASM_BUTTON_ID);
        testSearchGermplasm.setWidth("150px");
        testSearchGermplasm.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        
        testSearchGermplasmList = new Button(messageSource.getMessage(Message.HEAD_TO_HEAD_BROWSE_LIST));
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
        
        standardSearchGermplasmLabel = new Label(messageSource.getMessage(Message.SPECIFY_SINGLE_STANDARD_ENTRY));
        standardSearchGermplasmLabel.setImmediate(true);
        
        standardSearchGermplasmListLabel = new Label(messageSource.getMessage(Message.SPECIFY_STANDARD_GERMPLASM_LIST_ENTRY));
        standardSearchGermplasmListLabel.setImmediate(true);
        
        standardSearchGermplasm = new Button(messageSource.getMessage(Message.HEAD_TO_HEAD_SEARCH_GERMPLASM));
        standardSearchGermplasm.setData(SELECT_STANDARD_SEARCH_GERMPLASM_BUTTON_ID);
        standardSearchGermplasm.setWidth("150px");
        standardSearchGermplasm.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        
        standardSearchGermplasmList = new Button(messageSource.getMessage(Message.HEAD_TO_HEAD_BROWSE_LIST));
        standardSearchGermplasmList.setData(SELECT_STANDARD_SEARCH_GERMPLASM_LIST_BUTTON_ID);
        standardSearchGermplasmList.setWidth("150px");
        standardSearchGermplasmList.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        
        //absLayout.addComponent(specifyTestEntryLabel, "top:20px;left:30px");
        absLayoutStandard.addComponent(standardSearchGermplasmLabel, "top:13px;left:2px");
        absLayoutStandard.addComponent(standardSearchGermplasmListLabel, "top:53px;left:2px");
        absLayoutStandard.addComponent(standardSearchGermplasm, "top:10px;left:250px");
        absLayoutStandard.addComponent(standardSearchGermplasmList, "top:50px;left:250px");
        standardPanel.addComponent(absLayoutStandard);
        
        headerLabel = new Label(messageSource.getMessage(Message.SELECT_TEST_STANDARD_COMPARE));
        headerLabel.setImmediate(true);
        
        addComponent(headerLabel, "top:10px;left:10px");
        addComponent(testPanel, "top:30px;left:10px");
        addComponent(standardPanel, "top:30px;left:460px");
        
        entriesTable = new Table();
        entriesTable.setWidth("900px");
        entriesTable.setHeight("330px");
        entriesTable.setImmediate(true);
        entriesTable.setPageLength(-1);
        //entriesTable.setCacheRate(cacheRate)
        
        entriesTable.addContainerProperty(TEST_ENTRY_COLUMN_ID, String.class, null);
        entriesTable.addContainerProperty(STANDARD_ENTRY_COLUMN_ID, String.class, null);
        
        entriesTable.setColumnHeader(TEST_ENTRY_COLUMN_ID, "Test Entry");
        entriesTable.setColumnHeader(STANDARD_ENTRY_COLUMN_ID, "Standard Entry");
        
        entriesTable.setSelectable(true);
        entriesTable.setMultiSelect(true);
        entriesTable.setNullSelectionAllowed(false);
        entriesTable.setImmediate(true);
        
        entriesTable.addActionHandler(new Action.Handler() {
            private static final long serialVersionUID = 3972058734324800774L;

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
            }
        });
        
        nextButton = new Button("Next");
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        nextButton.setEnabled(false);
        addComponent(nextButton, "top:550px;left:900px");
        
        addComponent(entriesTable, "top:200px;left:20px");
    }

    private void deleteEntriesAction(){
        final Collection<?> selectedIds = (Collection<?>) entriesTable.getValue();
        if (!selectedIds.isEmpty()){
            for (Object itemId : selectedIds){
            	entriesTable.removeItem(itemId);
            	tableEntriesId.remove(itemId);            	
            }
            entriesTable.setPageLength(0);
        } else {
           ;// MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_CROSS_MUST_BE_SELECTED), Notification.POSITION_CENTERED);
        }
        
        if(isTableEntriesEmpty()){
        	//we set the new set since we already cleared it
			singleEntriesSet = new HashSet<String>();
			germplasmIdNameMap = new HashMap<String, String>();
			//mapTableEntriesId = new HashMap();
        }
        
        if(isEitherTableEntriesEmpty()){
        	nextButton.setEnabled(false);
        }else{
        	nextButton.setEnabled(true);
        }
       
    }
    
    public void selectTestEntryButtonClickAction(){
        Window parentWindow = this.getWindow();
        SelectGermplasmEntryDialog selectAGermplasmDialog = new SelectGermplasmEntryDialog(this, parentWindow, true);
        parentWindow.addWindow(selectAGermplasmDialog);
    }
    
    public void selectStandardEntryButtonClickAction(){
        Window parentWindow = this.getWindow();
        SelectGermplasmEntryDialog selectAGermplasmDialog = new SelectGermplasmEntryDialog(this, parentWindow, false);
        parentWindow.addWindow(selectAGermplasmDialog);
    }
    
    public void selectTestGermplasmListButtonClickAction(){
        Window parentWindow = this.getWindow();
        SelectGermplasmListDialog selectAGermplasmDialog = new SelectGermplasmListDialog(this, parentWindow, true);
        parentWindow.addWindow(selectAGermplasmDialog);
    }
    
    public void selectStandardGermplasmListButtonClickAction(){
        Window parentWindow = this.getWindow();
        SelectGermplasmListDialog selectAGermplasmDialog = new SelectGermplasmListDialog(this, parentWindow, false);
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
    	/*
    	Iterator iter = entriesTable.getItemIds().iterator();
		boolean hasLeftBlank = false;
		boolean hasRightBlank = false;
		boolean isNoEntries = true;
		
		
		
		while(iter.hasNext()){
			isNoEntries = false;
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
		if(isNoEntries){
			MessageNotifier.showWarning(getWindow(), "Warning!", "There should be at least one Test/Standard entry.", Notification.POSITION_CENTERED);
			return;
		}
		if(hasLeftBlank){
			MessageNotifier.showWarning(getWindow(), "Warning!", "There should be at least one Test entry.", Notification.POSITION_CENTERED);
			return;
		}else if(hasRightBlank){
			MessageNotifier.showWarning(getWindow(), "Warning!", "There should be at least one Standard entry.", Notification.POSITION_CENTERED);
			return;
		}
		*/
        if(this.nextScreen != null){
        	this.nextScreen.populateTraitsAvailableTable(getGermplasmPairs(), germplasmIdNameMap);
            this.mainScreen.selectSecondTab();
            
        }
    }
    
    @SuppressWarnings("rawtypes")
	private List<GermplasmPair> getGermplasmPairs(){
    	List<GermplasmPair> pairList = new ArrayList<GermplasmPair>();
    	
    	Iterator iter = entriesTable.getItemIds().iterator();						
		while(iter.hasNext()){
			GermplasmPair germplasmPair = new GermplasmPair();			
			//we iterate and permutate against the list
			String id = (String)iter.next();
			String leftId = "";
			String rightId = "";
			StringTokenizer tokenizer = new StringTokenizer(id,":");
			if(tokenizer.countTokens() == 2){
				leftId = tokenizer.nextToken().trim();
				rightId = tokenizer.nextToken().trim();
			}
			
			germplasmPair.setGid1(Integer.valueOf(leftId));
			germplasmPair.setGid2(Integer.valueOf(rightId));
			pairList.add(germplasmPair);
			
		}
    	
    	return pairList;
    }
    
    /**
    private boolean areCurrentGIDsDifferentFromLast(Integer currentTestEntryGID, Integer currentStandardEntryGID){
        if(this.lastTestEntryGID != null && this.lastStandardEntryGID != null){
            if(this.lastTestEntryGID == currentTestEntryGID && this.lastStandardEntryGID == currentStandardEntryGID){
                return false;
            }
        }
        
        return true;
    }
    **/
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
        
    }
    
    private boolean permutationsWillExceedMax(GermplasmList list){
    	List<GermplasmListData> selectedList = list.getListData();
		if (list != null && selectedList!= null && entriesTable != null){
			int permutationsCount = entriesTable.size() * selectedList.size();
    		return MAX_NUM_OF_PAIRS.intValue() < permutationsCount;
    	}
    	
    	return false;
    }
    
    public boolean addGermplasmList(Integer germplasmListId, boolean isTestEntry){
    	try{
	    	GermplasmList germplasmList = germplasmListManager.getGermplasmListById(germplasmListId);
	    	if(germplasmList != null){
	    		
	    		if (!permutationsWillExceedMax(germplasmList)){
	    			doGermplasmPermutationOnTable(isTestEntry, false, germplasmList, null);
	    			return true;
	    			
	    		} else {
	    			MessageNotifier.showWarning(getWindow(), "Warning", "The list selected will create too " +
	    					"many germplasm pairs and may cause the tool to crash. Please select another list.");
	    		}
	    		
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return false;
    }
    
    public void addTestGermplasm(Germplasm germplasm ){  
    	if(germplasm != null){
	    	//testGermplasmAdded.add(germplasm);
	    	//doGermplasmPermutation2(germplasm, standardGermplasmListAdded);
	    	//doGermplasmPermutation3(germplasm, standardGermplasmAdded);
    		doGermplasmPermutationOnTable(true, true, null, germplasm);
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
    	Map<String, Map<String, String>> map = getBothMapEntries();
    	Map<String, String> testMap = map.get(TEST_ENTRY);
    	Map<String, String> standardMap = map.get(STANDARD_ENTRY);
    	List<TablesEntries> tableEntriesList = new ArrayList<TablesEntries>();
    	
    	if(isTableEntriesEmpty()){
			germplasmIdNameMap = new HashMap<String, String>();
        }
    	
    	// create a germplasm list with the germplasm as the sole list item
    	if(isGermplasm){
    		germplasmList = new GermplasmList();
    		List<GermplasmListData> dataList = new ArrayList<GermplasmListData>();
    		
    		GermplasmListData germplasmData = new GermplasmListData();
    		//GID and Designation are fields that will be checked/used
    		germplasmData.setGid(germplasm.getGid());
    		germplasmData.setDesignation(germplasm.getPreferredName().getNval());
    		
    		dataList.add(germplasmData);
    		germplasmList.setListData(dataList);
    	}
    			
		permutateGermplasmListToPartnerEntries(isTestEntry, testMap, standardMap,
						tableEntriesList, germplasmList);

		addToTable(tableEntriesList);
    	
    	if(isEitherTableEntriesEmpty()){
        	nextButton.setEnabled(false);
        }else{
        	nextButton.setEnabled(true);
        }
    	
    }

	private void permutateGermplasmListToPartnerEntries(
			boolean isTestEntry,
			Map<String, String> testMap, Map<String, String> standardMap,
			List<TablesEntries> tableEntriesList,
			GermplasmList germplasmList) {
		
		List<GermplasmListData> germplasmListData = germplasmList.getListData();
		Map<String, String> ownMap = testMap;
		Map<String, String> partnerMap = standardMap;
		
		if (!isTestEntry){
			ownMap = standardMap;
			partnerMap = testMap;
		} 
		
		if(ownMap.keySet().isEmpty() && !partnerMap.keySet().isEmpty()){
			//we need to remove all
			deleteAllSingleEntriesInTableListData(isTestEntry, germplasmListData, testMap.keySet().size(), standardMap.keySet().size());
		}
		
		if(partnerMap.keySet().isEmpty()){
			//just add on one side  	
			for(GermplasmListData listData : germplasmListData){
				String gid = listData.getGid().toString();
				String germplasmName = listData.getDesignation() != null ? listData.getDesignation() : gid;
				
				String testEntryName = germplasmName;
				String standardEntryName = "";
				String newId = gid + ": ";    
				if (!isTestEntry){
					standardEntryName = germplasmName;
					testEntryName = "";
					newId = " :" + gid;
				}
				
				TablesEntries entry = createTableEntries(testEntryName, standardEntryName, newId);
				tableEntriesList.add(entry);
				singleEntriesSet.add(newId);
				germplasmIdNameMap.put(gid, germplasmName);
				
			}
			
		}else{
			//we iterate			
			for(GermplasmListData listData : germplasmListData){
				Iterator<String> partnerIterator = partnerMap.keySet().iterator();
				String gid = listData.getGid().toString();
				String germplasmName = listData.getDesignation() != null ? listData.getDesignation() : gid;
				
				while(partnerIterator.hasNext()){
					String partnerId = (String) partnerIterator.next();
					String partnerName = (String) partnerMap.get(partnerId);

					String testEntryName = germplasmName;
					String standardEntryName = partnerName;
					String newId = gid + ":" + partnerId;    
					
					if (!isTestEntry){
						testEntryName = partnerName;
						standardEntryName = germplasmName;
						newId =  partnerId + ":" + gid;
					}
					
					if(!gid.equalsIgnoreCase(partnerId)){        				
						TablesEntries entry = createTableEntries(testEntryName, standardEntryName, newId);
			    		tableEntriesList.add(entry);
					}
					germplasmIdNameMap.put(partnerId, partnerName);
				}
				
				germplasmIdNameMap.put(gid, germplasmName);
			}
			
			
		}
	}
	
    /*
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
    */
    private TablesEntries createTableEntries(String testName, String standardName, String id){
    	TablesEntries tableEntry = new TablesEntries();		
		tableEntry.setTestEntryName(testName);
		tableEntry.setStandardEntryName(standardName);
		tableEntry.setTestStandardEntry(id);
		return tableEntry;
    }
     
    private void deleteAllSingleEntriesInTableListData(boolean isTestEntry, List<GermplasmListData> germplasmListData, int leftSize, int rightSize){
    	//we delete the single entrie
		for(String idToDelete : singleEntriesSet){
			for(GermplasmListData listData : germplasmListData){
				
				String tempId = "";
				if(isTestEntry)
					tempId = " :" + listData.getGid().toString();
				else
					tempId = listData.getGid().toString()+": ";
				
				if(idToDelete.equalsIgnoreCase(tempId)){
					if(isTestEntry){
						if(rightSize != 1){
							entriesTable.removeItem(idToDelete);				
							tableEntriesId.remove(idToDelete);
						}
					}else{
						if(leftSize != 1){
							entriesTable.removeItem(idToDelete);				
							tableEntriesId.remove(idToDelete);
						}
					}
				}
				
				if(!idToDelete.equalsIgnoreCase(tempId)){
					entriesTable.removeItem(idToDelete);	
					tableEntriesId.remove(idToDelete);
				}
			}
		}
		//we set the new set since we already cleared it
		if(isTableEntriesEmpty()){
			singleEntriesSet = new HashSet<String>();
		}
    }
    
    @SuppressWarnings("rawtypes")
	private Map<String, Map<String, String>> getBothMapEntries(){
    	Map<String, String> testMap = new HashMap<String, String>();
    	Map<String, String> standardMap = new HashMap<String, String>();
    	Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();
    	Iterator iter = entriesTable.getItemIds().iterator();		
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
			
			if(leftId != null && !leftId.equalsIgnoreCase("")){
				testMap.put(leftId, testEntryName);
			}
			if(rightId != null && !rightId.equalsIgnoreCase("")){
				standardMap.put(rightId, standardEntryName);
			}
		}
		resultMap.put(TEST_ENTRY, testMap);
		resultMap.put(STANDARD_ENTRY, standardMap);
		return resultMap;
    }
    	
    private void addToTable(List<TablesEntries> tableEntryList){
    	for(TablesEntries tableEntry : tableEntryList){
    		String newId = tableEntry.getTestStandardEntry();    		
	    	//if not in map, add it in the table
    		
	    	if(!tableEntriesId.contains(newId)){
	    		entriesTable.addItem(new Object[] {tableEntry.getTestEntryName(), tableEntry.getStandardEntryName()}, tableEntry.getTestStandardEntry());
		    	tableEntriesId.add(newId);
		    }
    	}
    }
    
    private boolean isTableEntriesEmpty(){
    	Map<String, Map<String, String>> map = getBothMapEntries();
    	Map<String, String> testMap = map.get(TEST_ENTRY);
    	Map<String, String> standardMap = map.get(STANDARD_ENTRY);
    	if(testMap.keySet().isEmpty() && standardMap.keySet().isEmpty())
    		return true;
    	return false;
    	//return entriesTable.getItemIds().isEmpty();
    }
    
    private boolean isEitherTableEntriesEmpty(){
    	Map<String, Map<String, String>> map = getBothMapEntries();
    	Map<String, String> testMap = map.get(TEST_ENTRY);
    	Map<String, String> standardMap = map.get(STANDARD_ENTRY);
    	if(testMap.keySet().isEmpty() || standardMap.keySet().isEmpty())
    		return true;
    	return false;
    	//return entriesTable.getItemIds().isEmpty();
    }
}
