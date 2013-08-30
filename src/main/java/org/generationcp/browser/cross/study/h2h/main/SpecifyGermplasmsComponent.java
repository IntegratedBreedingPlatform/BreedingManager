package  org.generationcp.browser.cross.study.h2h.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.generationcp.browser.germplasm.dialogs.SelectAGermplasmDialog;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
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
    
    private static final Action ACTION_SELECT = new Action("Select");
    private static final Action ACTION_SELECT_ALL = new Action("Select All");
    private static final Action ACTION_DELETE = new Action("Delete selected");
    private static final Action[] ACTIONS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT, ACTION_SELECT_ALL, ACTION_DELETE };
   
    private Object tableRowItem = null;
    
    private List<GermplasmList> testGermplasmListAdded = new ArrayList(); //can be a germplasm or a germplasmList
    private List<Germplasm> testGermplasmAdded = new ArrayList(); //can be a germplasm or a germplasmList
    private List<GermplasmList> standardGermplasmListAdded = new ArrayList(); //can be a germplasm or a germplasmList
    private List<Germplasm> standardGermplasmAdded = new ArrayList(); //can be a germplasm or a germplasmList
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
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
            }
            entriesTable.setPageLength(0);
        } else {
           ;// MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_CROSS_MUST_BE_SELECTED), Notification.POSITION_CENTERED);
        }
       
    }
    
    public void setDummyTableData(){
    	for(int i = 0 ; i < 100 ; i++){
    		entriesTable.addItem(new Object[] {"test - "+i, "standard - "+i}, new Integer(i));
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
    	//test entry can be Germplasm or Germplasm Entry
    	
    	testGermplasmListAdded.add(germplasmList);
    	doGermplasmPermutation0(germplasmList, standardGermplasmListAdded);
    	doGermplasmPermutation1(germplasmList, standardGermplasmAdded);
    }
    public void addTestGermplasm(Germplasm germplasm ){
    	//test entry can be Germplasm or Germplasm Entry
    	
    	testGermplasmAdded.add(germplasm);
    	doGermplasmPermutation2(germplasm, standardGermplasmListAdded);
    	doGermplasmPermutation3(germplasm, standardGermplasmAdded);
    	//doGermplasmPermutation(testEntry, standardGermplasmListAdded);
    }
    public void addStandardGermplasmList(GermplasmList germplasmList){
    	//test entry can be Germplasm or Germplasm Entry
    	standardGermplasmListAdded.add(germplasmList);
    	doGermplasmPermutation4(testGermplasmListAdded, germplasmList);
    	doGermplasmPermutation5(testGermplasmAdded, germplasmList);
    	//doGermplasmPermutation(testGermplasmListAdded, testEntry);
    }
    public void addStandardGermplasm(Germplasm germplasm){
    	//test entry can be Germplasm or Germplasm Entry
    	standardGermplasmAdded.add(germplasm);
    	doGermplasmPermutation6(testGermplasmListAdded, germplasm);
    	doGermplasmPermutation7(testGermplasmAdded, germplasm);
    }
    private void doGermplasmPermutation0(GermplasmList testEntries, List<GermplasmList> standardEntries){
    	//testEntries.getl
    }
    private void doGermplasmPermutation1(GermplasmList testEntries, List<Germplasm> standardEntry){
    	
    }
    private void doGermplasmPermutation2(Germplasm testEntry,  List<GermplasmList> standardEntries){
    	
    }
    private void doGermplasmPermutation3(Germplasm testEntry,  List<Germplasm> standardEntry){
    	
    }
    
    private void doGermplasmPermutation4(List<GermplasmList> testEntries, GermplasmList standardEntries){
    	
    }
    private void doGermplasmPermutation5(List<Germplasm> testEntry, GermplasmList standardEntries){
    	
    }
    private void doGermplasmPermutation6(List<GermplasmList> testEntries,  Germplasm standardEntry){
    	
    }
    private void doGermplasmPermutation7(List<Germplasm> testEntry,  Germplasm standardEntry){
    	
    }
    
    
}
