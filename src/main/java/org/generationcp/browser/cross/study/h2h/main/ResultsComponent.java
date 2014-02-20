package org.generationcp.browser.cross.study.h2h.main;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.h2h.main.pojos.ObservationList;
import org.generationcp.browser.cross.study.h2h.main.pojos.ResultsData;
import org.generationcp.browser.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.browser.cross.study.h2h.main.util.HeadToHeadDataListExport;
import org.generationcp.browser.cross.study.h2h.main.util.HeadToHeadDataListExportException;
import org.generationcp.browser.cross.study.util.HeadToHeadResultsUtil;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@Configurable
public class ResultsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 2305982279660448571L;
    
    @SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.browser.cross.study.h2h.main.ResultsComponent.class);
    
    private static final String MEAN_TEST_COLUMN_ID = "ResultsComponent Mean Test Column ID";
    private static final String MEAN_STD_COLUMN_ID = "ResultsComponent Mean STD Column ID";
    
    public static final String TEST_COLUMN_ID = "ResultsComponent Test Column ID";
    public static final String STANDARD_COLUMN_ID = "ResultsComponent Standard Column ID";
    public static final String NUM_OF_ENV_COLUMN_ID = "ResultsComponent Num Of Env Column ID";
    public static final String NUM_SUP_COLUMN_ID = "ResultsComponent Num Sup Column ID";
    public static final String PVAL_COLUMN_ID = "ResultsComponent Pval Column ID";
    public static final String MEAN_DIFF_COLUMN_ID = "ResultsComponent Mean Diff Column ID";

    private Table[] resultsTable;
    
    private Label testEntryNameLabel;
    private Label standardEntryNameLabel;
    
    public static final String BACK_BUTTON_ID = "ResultsComponent Back Button ID";
    public static final String EXPORT_BUTTON_ID = "ResultsComponent Export Button ID";

	public static final String USER_HOME = "user.home";

    private Button exportButton;
    private Button backButton;
    
    private HeadToHeadCrossStudyMain mainScreen;
    private List<EnvironmentForComparison> finalEnvironmentForComparisonList;
    
    private String[] columnIdData = {NUM_OF_ENV_COLUMN_ID,NUM_SUP_COLUMN_ID,MEAN_TEST_COLUMN_ID,
    		MEAN_STD_COLUMN_ID, PVAL_COLUMN_ID,MEAN_DIFF_COLUMN_ID};
    private Map<String, String> columnIdDataMsgMap = new HashMap<String, String>();
    
    public static DecimalFormat decimalFormmatter = new DecimalFormat("#,##0.00");
    public List<ResultsData> resultsDataList = new ArrayList<ResultsData>();
    private TabSheet mainTabs;
    
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
    public ResultsComponent(HeadToHeadCrossStudyMain mainScreen){
        this.mainScreen = mainScreen;
        
        //initialize the data map
        columnIdDataMsgMap.put(NUM_OF_ENV_COLUMN_ID, "#Env");
        columnIdDataMsgMap.put(NUM_SUP_COLUMN_ID, "#Sup");
        columnIdDataMsgMap.put(MEAN_TEST_COLUMN_ID, "MeanTest");
        columnIdDataMsgMap.put(MEAN_STD_COLUMN_ID, "MeanStd");
        columnIdDataMsgMap.put(PVAL_COLUMN_ID, "Pval");
        columnIdDataMsgMap.put(MEAN_DIFF_COLUMN_ID, "MeanDiff");
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("550px");
        setWidth("1000px");
   
	    exportButton = new Button("Export");
	    exportButton.setData(EXPORT_BUTTON_ID);
	    exportButton.addListener(new org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener(this));
	    exportButton.setEnabled(true);
	    exportButton.setWidth("80px");
	    exportButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	    
		
		backButton = new Button("Back");
		backButton.setData(BACK_BUTTON_ID);
		backButton.setWidth("80px");
		backButton.addListener(new org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener(this));
		
    }
    
    private void createEnvironmentsResultTable(List<EnvironmentForComparison> environmentForComparisonList, 
    		Map<String,String> germplasmNameIdMap, List<GermplasmPair> germplasmPairList, Map<String, ObservationList> observationMap){
    	    	
    	this.removeAllComponents();
    	addComponent(exportButton, "top:505px;left:900px");
    	addComponent(backButton, "top:505px;left:810px");
    	
    	mainTabs = new TabSheet();    
    	mainTabs.setWidth("957px");   
    	mainTabs.setHeight("475px");
        addComponent(mainTabs, "top:20px;left:20px");
        
    	this.finalEnvironmentForComparisonList = environmentForComparisonList; 
        EnvironmentForComparison envForComparison = environmentForComparisonList.get(0);
        Set<TraitForComparison> traitsIterator = envForComparison.getTraitAndObservationMap().keySet();
        Iterator<TraitForComparison> iter = traitsIterator.iterator();
        TraitForComparison[] traitsIteratorArray = new  TraitForComparison[traitsIterator.size()];
        int x = 0;
        while(iter.hasNext()){
        	
        	traitsIteratorArray[x++] = iter.next();
        }
        		//(TraitForComparison[]) envForComparison.getTraitAndObservationMap().keySet().l();
        int traitSize = envForComparison.getTraitAndObservationMap().keySet().size();
        resultsTable = new Table[traitSize];
        VerticalLayout[] layouts = new VerticalLayout[traitSize];
        resultsDataList = new ArrayList<ResultsData>();
        
        for(int counter = 0 ; counter < traitsIteratorArray.length ; counter++){    
        	TraitForComparison traitForCompare = traitsIteratorArray[counter];
        	if(traitForCompare.isDisplay()){
        		//layouts[counter] = new VerticalLayout();
        		//create multiple table       
        		/*
        		this.resultsTable[counter].removeAllItems();
            	
            	
                List<Object> propertyIds = new ArrayList<Object>();
                for(Object propertyId : resultsTable[counter].getContainerPropertyIds()){
                    propertyIds.add(propertyId);
                }
                
                for(Object propertyId : propertyIds){
                	resultsTable[counter].removeContainerProperty(propertyId);
                }
                */
        		
        		resultsTable[counter] = new Table();
        	    resultsTable[counter].setWidth("912px");
        	    resultsTable[counter].setHeight("400px");
        	    resultsTable[counter].setImmediate(true);
        	    resultsTable[counter].setColumnCollapsingAllowed(true);
        	    resultsTable[counter].setColumnReorderingAllowed(true);
        	    
                resultsTable[counter].addContainerProperty(TEST_COLUMN_ID, String.class, null);
                resultsTable[counter].addContainerProperty(STANDARD_COLUMN_ID, String.class, null);
                
                resultsTable[counter].setColumnHeader(TEST_COLUMN_ID, "Test Entry");
                resultsTable[counter].setColumnHeader(STANDARD_COLUMN_ID, "Standard Entry");
                
                resultsTable[counter].setColumnAlignment(TEST_COLUMN_ID, Table.ALIGN_CENTER);
                resultsTable[counter].setColumnAlignment(STANDARD_COLUMN_ID, Table.ALIGN_CENTER);
                
                for(String columnKey : columnIdData){
	            	String msg = columnIdDataMsgMap.get(columnKey);
	            	resultsTable[counter].addContainerProperty(traitForCompare.getTraitInfo().getName()+columnKey, String.class, null);
	            	resultsTable[counter].setColumnHeader(traitForCompare.getTraitInfo().getName()+columnKey, msg);
	            	resultsTable[counter].setColumnAlignment(traitForCompare.getTraitInfo().getName()+columnKey, Table.ALIGN_CENTER);
	            	
	            }
                layouts[counter] = new VerticalLayout();
                layouts[counter].setMargin(true);
                layouts[counter].setSpacing(true);

        		layouts[counter].addComponent(resultsTable[counter]);
        		mainTabs.addTab(layouts[counter], traitForCompare.getTraitInfo().getName());
        		
        	}
        }
        
        
        //traitsIterator = envForComparison.getTraitAndObservationMap().keySet();
    	
        
        for(GermplasmPair germplasmPair : germplasmPairList){
        	String uniquieId = germplasmPair.getGid1() + ":" + germplasmPair.getGid2();
        	String testEntry = germplasmNameIdMap.get(Integer.toString(germplasmPair.getGid1()));
        	String standardEntry =  germplasmNameIdMap.get(Integer.toString(germplasmPair.getGid2()));
        	Map<String,String> traitDataMap = new HashMap<String, String>();
        	ResultsData resData = new ResultsData(germplasmPair.getGid1(), testEntry, germplasmPair.getGid2(), standardEntry, traitDataMap);
        	
        	
        	for(int i = 0 ; i < resultsTable.length ; i++){
        		Table table = resultsTable[i];
        		Item item = table.addItem(uniquieId);
            	item.getItemProperty(TEST_COLUMN_ID).setValue(testEntry);
            	item.getItemProperty(STANDARD_COLUMN_ID).setValue(standardEntry);
            	TraitForComparison traitForCompare = traitsIteratorArray[i];
            	if(traitForCompare.isDisplay()){
            		Map<String, Object> valuesMap = new HashMap<String, Object>();
	            	for(String columnKey : columnIdData){
	            		String cellKey = traitForCompare.getTraitInfo().getName()+columnKey;
	            		String cellVal = getColumnValue(valuesMap, columnKey,   germplasmPair,  traitForCompare,   observationMap,  environmentForComparisonList);                
	            		traitDataMap.put(cellKey, cellVal);
	                	item.getItemProperty(cellKey).setValue(cellVal);                	
	                }
            	}
        	}
        	
        	
        	resData.setTraitDataMap(traitDataMap);
        	resultsDataList.add(resData);
    	}
        
    }
    
    private String getColumnValue(Map<String,Object> valuesMap, String columnId, GermplasmPair germplasmPair, 
    		TraitForComparison traitForComparison, Map<String, ObservationList> observationMap, List<EnvironmentForComparison> environmentForComparisonList){
    	Object value = 0;
    	if(NUM_OF_ENV_COLUMN_ID.equalsIgnoreCase(columnId)){
    		//get the total number of environment where the germplasm pair was observer and the observation value is not null and not empty string
    		value = HeadToHeadResultsUtil.getTotalNumOfEnv(germplasmPair, traitForComparison, observationMap,environmentForComparisonList);
    		
    	}else if(NUM_SUP_COLUMN_ID.equalsIgnoreCase(columnId)){
    		value = HeadToHeadResultsUtil.getTotalNumOfSup(germplasmPair, traitForComparison, observationMap,environmentForComparisonList);
    		
    	} else if(MEAN_TEST_COLUMN_ID.equalsIgnoreCase(columnId)){
    		value = HeadToHeadResultsUtil.getMeanValue(germplasmPair, 1, traitForComparison, observationMap, environmentForComparisonList);
    		
    	} else if(MEAN_STD_COLUMN_ID.equalsIgnoreCase(columnId)){
    		value = HeadToHeadResultsUtil.getMeanValue(germplasmPair, 2, traitForComparison, observationMap, environmentForComparisonList);
    		
    	} else if(PVAL_COLUMN_ID.equalsIgnoreCase(columnId)){
    		Integer numOfEnvts = (Integer) valuesMap.get(NUM_OF_ENV_COLUMN_ID);
    		Integer numOfSucceses = (Integer) valuesMap.get(NUM_SUP_COLUMN_ID);
    		value = HeadToHeadResultsUtil.getPvalue(numOfEnvts, numOfSucceses);
    		
    	}else if(MEAN_DIFF_COLUMN_ID.equalsIgnoreCase(columnId)){
    		value = HeadToHeadResultsUtil.getMeanDiff(germplasmPair, traitForComparison, observationMap,environmentForComparisonList);
    	}
    	
    	valuesMap.put(columnId, value);
    	if (value instanceof Double){
    		value = decimalFormmatter.format(value);
    	}
    	return value.toString();
    }
    
    public static boolean isValidDoubleValue(String val){
    	if(val != null && !val.equalsIgnoreCase("")){
    		try{
    			Double.parseDouble(val);
    			return true;
    		}catch(NumberFormatException ee){
    			return false;
    		}
    	}
    	return false;
    }
    
    public void populateResultsTable(List<EnvironmentForComparison> environmentForComparisonList, Map<String,String> germplasmNameIdMap, List<GermplasmPair> germplasmPair,
    		Map<String, ObservationList> observationMap){
    	createEnvironmentsResultTable(environmentForComparisonList, germplasmNameIdMap, germplasmPair, observationMap);
    
    }
    
    public void setEntriesLabel(String testEntryLabel, String standardEntryLabel){
        this.testEntryNameLabel.setValue(testEntryLabel);
        this.standardEntryNameLabel.setValue(standardEntryLabel);
    }
    
    @Override
    public void updateLabels() {
        
    }
    
    public void exportButtonClickAction(){
            	
    	EnvironmentForComparison envForComparison = this.finalEnvironmentForComparisonList.get(0);
    	Set<TraitForComparison> traitsIterator = envForComparison.getTraitAndObservationMap().keySet();
    	
    	 // in current export format, if # of traits > 42, will exceed Excel's 255 columns limitation	
    	 if (traitsIterator.size() > 42){
    		 MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.WARNING),
    				 messageSource.getMessage(Message.H2H_NUM_OF_TRAITS_EXCEEDED));

    	 } else {
    		 String tempFileName = System.getProperty( USER_HOME ) + "/HeadToHeadDataList.xls";
    		 HeadToHeadDataListExport listExporter = new HeadToHeadDataListExport();
    		 
    		 try {
    			 
    			 listExporter.exportHeadToHeadDataListExcel(tempFileName, resultsDataList, traitsIterator, columnIdData, columnIdDataMsgMap);
    			 FileDownloadResource fileDownloadResource = new FileDownloadResource(new File(tempFileName), this.getApplication());
    			 fileDownloadResource.setFilename("HeadToHeadDataList.xls");
    			 
    			 this.getWindow().open(fileDownloadResource);
    			 this.mainScreen.selectFirstTab();
    			 //TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
    			 //File tempFile = new File(tempFileName);
    			 //tempFile.delete();
    		 } catch (HeadToHeadDataListExportException e) {
    			 MessageNotifier.showError(this.getApplication().getWindow(GermplasmStudyBrowserApplication.HEAD_TO_HEAD_COMPARISON_WINDOW_NAME) 
    					 , "Error with exporting list."
    					 , e.getMessage(), Notification.POSITION_CENTERED);
    		 }
         
         }
         
    }
        
    public void backButtonClickAction(){
        this.mainScreen.selectThirdTab();
    }
}
