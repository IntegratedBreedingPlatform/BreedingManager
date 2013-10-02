package org.generationcp.browser.cross.study.h2h.main;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.h2h.main.pojos.ObservationList;
import org.generationcp.browser.cross.study.h2h.main.pojos.ResultsData;
import org.generationcp.browser.cross.study.h2h.pojos.Result;
import org.generationcp.browser.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.browser.cross.study.h2h.main.util.HeadToHeadDataListExport;
import org.generationcp.browser.cross.study.h2h.main.util.HeadToHeadDataListExportException;
import org.generationcp.browser.germplasmlist.util.GermplasmListExporter;
import org.generationcp.browser.germplasmlist.util.GermplasmListExporterException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmDataManagerImpl;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

@Configurable
public class ResultsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 2305982279660448571L;
    
    private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.browser.cross.study.h2h.main.ResultsComponent.class);
    
    private static final String TRAIT_COLUMN_ID = "ResultsComponent Trait Column ID";
    
    
    private static final String MEAN_TEST_COLUMN_ID = "ResultsComponent Mean Test Column ID";
    private static final String MEAN_STD_COLUMN_ID = "ResultsComponent Mean STD Column ID";
    
    
    public static final String TEST_COLUMN_ID = "ResultsComponent Test Column ID";
    public static final String STANDARD_COLUMN_ID = "ResultsComponent Standard Column ID";
    public static final String NUM_OF_ENV_COLUMN_ID = "ResultsComponent Num Of Env Column ID";
    public static final String NUM_SUP_COLUMN_ID = "ResultsComponent Num Sup Column ID";
    public static final String PVAL_COLUMN_ID = "ResultsComponent Pval Column ID";
    public static final String MEAN_DIFF_COLUMN_ID = "ResultsComponent Mean Diff Column ID";

    private Table[] resultsTable;
    
    private Label testEntryLabel;
    private Label standardEntryLabel;
    private Label testEntryNameLabel;
    private Label standardEntryNameLabel;
    
    private Integer currentTestEntryGID;
    private Integer currentStandardEntryGID;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;

    public static final String BACK_BUTTON_ID = "ResultsComponent Back Button ID";
    public static final String EXPORT_BUTTON_ID = "ResultsComponent Export Button ID";

    private Button exportButton;
    private Button backButton;
    
    private HeadToHeadCrossStudyMain mainScreen;
    private List<EnvironmentForComparison> finalEnvironmentForComparisonList;
    
    private String[] columnIdData = {NUM_OF_ENV_COLUMN_ID,NUM_SUP_COLUMN_ID,MEAN_TEST_COLUMN_ID,
    		MEAN_STD_COLUMN_ID, PVAL_COLUMN_ID,MEAN_DIFF_COLUMN_ID};
    private Map<String, String> columnIdDataMsgMap = new HashMap();
    
    public static DecimalFormat decimalFormmatter = new DecimalFormat("#,##0.00");
    public List<ResultsData> resultsDataList = new ArrayList();
    private TabSheet mainTabs;
    
    public ResultsComponent(HeadToHeadCrossStudyMain mainScreen){
        this.currentStandardEntryGID = null;
        this.currentTestEntryGID = null;
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
    addComponent(exportButton, "top:515px;left:900px");

       backButton = new Button("Back");
       backButton.setData(BACK_BUTTON_ID);
       backButton.addListener(new org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener(this));
       addComponent(backButton, "top:515px;left:820px");
    }
    
    private void createEnvironmentsResultTable(List<EnvironmentForComparison> environmentForComparisonList, 
    		Map<String,String> germplasmNameIdMap, List<GermplasmPair> germplasmPairList, Map<String, ObservationList> observationMap){
    	 
    	mainTabs = new TabSheet();    
    	mainTabs.setWidth("950px");   
        addComponent(mainTabs, "top:20px;left:10px");
        
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
        resultsDataList = new ArrayList();
        
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
        	    resultsTable[counter].setWidth("900px");
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
        	Map<String,String> traitDataMap = new HashMap();
        	ResultsData resData = new ResultsData(germplasmPair.getGid1(), testEntry, germplasmPair.getGid2(), standardEntry, traitDataMap);
        	
        	
        	for(int i = 0 ; i < resultsTable.length ; i++){
        		Table table = resultsTable[i];
        		Item item = table.addItem(uniquieId);
            	item.getItemProperty(TEST_COLUMN_ID).setValue(testEntry);
            	item.getItemProperty(STANDARD_COLUMN_ID).setValue(standardEntry);
            	TraitForComparison traitForCompare = traitsIteratorArray[i];
            	if(traitForCompare.isDisplay()){
	            	for(String columnKey : columnIdData){
	            		String cellKey = traitForCompare.getTraitInfo().getName()+columnKey;
	            		String cellVal = getColumnValue(  columnKey,   germplasmPair,  traitForCompare,   observationMap,  environmentForComparisonList);                
	            		traitDataMap.put(cellKey, cellVal);
	                	item.getItemProperty(cellKey).setValue(cellVal);                	
	                }
            	}
        	}
        	
        	
        	resData.setTraitDataMap(traitDataMap);
        	resultsDataList.add(resData);
    	}
        
    }
    
    private String getColumnValue(String columnId, GermplasmPair germplasmPair, 
    		TraitForComparison traitForComparison, Map<String, ObservationList> observationMap, List<EnvironmentForComparison> environmentForComparisonList){
    	String val = "0";
    	if(NUM_OF_ENV_COLUMN_ID.equalsIgnoreCase(columnId)){
    		//get the total number of environment where the germplasm pair was observer and the observation value is not null and not empty string
    		val = getTotalNumOfEnv(germplasmPair, traitForComparison, observationMap,environmentForComparisonList).toString();
    		
    		
    	}else if(NUM_SUP_COLUMN_ID.equalsIgnoreCase(columnId)){
    		val = getTotalNumOfSup(germplasmPair, traitForComparison, observationMap,environmentForComparisonList).toString();
    		
    	} else if(MEAN_TEST_COLUMN_ID.equalsIgnoreCase(columnId)){
    		val = getMeanValue(germplasmPair, 1, traitForComparison, observationMap, environmentForComparisonList).toString();
    		
    	} else if(MEAN_STD_COLUMN_ID.equalsIgnoreCase(columnId)){
    		val = getMeanValue(germplasmPair, 2, traitForComparison, observationMap, environmentForComparisonList).toString();
    		
    	} else if(PVAL_COLUMN_ID.equalsIgnoreCase(columnId)){
    		val = getPval(germplasmPair, traitForComparison, observationMap,environmentForComparisonList);
    		
    	}else if(MEAN_DIFF_COLUMN_ID.equalsIgnoreCase(columnId)){
    		val = getMeanDiff(germplasmPair, traitForComparison, observationMap,environmentForComparisonList);
    	}
    	return val;
    }
    
    private String getPval(GermplasmPair germplasmPair, TraitForComparison traitForComparison, 
    		Map<String, ObservationList> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	double counter = 0;
    	return "-";//decimalFormmatter.format(counter);
    }
    private String getMeanDiff(GermplasmPair germplasmPair, TraitForComparison traitForComparison, 
    		Map<String, ObservationList> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	double counter = 0;
    	//r * ( summation of [ Ek (Tijk-Silk)/Nijl ] )
    	/*
    	 * Nijl = is the number of environment where both tijk and silk is not null and not empty string
    	 * r = 1 if increasing and -1 if decreasing
    	 * Ek - environment weight
    	 */
    	
    	boolean isIncreasing = false;
    	int numOfValidEnv = 0;
    	if(traitForComparison.getDirection().intValue() == TraitsAvailableComponent.INCREASING.intValue()){
    		isIncreasing = true;
    	}else if(traitForComparison.getDirection().intValue() == TraitsAvailableComponent.DECREASING.intValue()){
    		isIncreasing = false;
    	}
    	
    	String gid1ForCompare = Integer.toString(germplasmPair.getGid1());
		String gid2ForCompare = Integer.toString(germplasmPair.getGid2());
		String traitId = Integer.toString(traitForComparison.getTraitInfo().getId());
		List<Double> listOfObsVal = new ArrayList();
		for(EnvironmentForComparison envForComparison: environmentForComparisonList){
			
			String envId = envForComparison.getEnvironmentNumber().toString();
			String keyToChecked1 = traitId + ":" + envId + ":" +gid1ForCompare;
			String keyToChecked2 = traitId + ":" + envId + ":" +gid2ForCompare;
			
			ObservationList obs1 = observationMap.get(keyToChecked1);
    		ObservationList obs2 = observationMap.get(keyToChecked2);
    		
    		
    		//if(isValidObsValue(obs1, obs2)){
    		if(obs1 != null && obs2 != null){
	    		if(obs1.isValidObservationList() && obs2.isValidObservationList()){
	    			numOfValidEnv++;
	    			//double obs1Val = Double.parseDouble(obs1.getValue());
	    			//double obs2Val = Double.parseDouble(obs2.getValue());
	    			double obs1Val = obs1.getObservationAverage();
	    			double obs2Val = obs2.getObservationAverage();
	    			double envWeight = envForComparison.getWeight(); 
	    			listOfObsVal.add(Double.valueOf( envWeight * (obs1Val - obs2Val) ));
	    		}
    		}
			
		}
		double summation = 0;
		for(Double obsCalculatedVal : listOfObsVal){
			summation += (obsCalculatedVal.doubleValue() / numOfValidEnv);
		}
		
		if(isIncreasing == false && summation != 0)
			summation = -1 * summation; 
    	
		//summation = 123456789.12345567;
		return decimalFormmatter.format(summation);
    	//return Double.valueOf();
    }
    private Integer getTotalNumOfSup(GermplasmPair germplasmPair, 
    		TraitForComparison traitForComparison, Map<String, ObservationList> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	boolean isIncreasing = false;
    	int counter = 0;
    	if(traitForComparison.getDirection().intValue() == TraitsAvailableComponent.INCREASING.intValue()){
    		isIncreasing = true;
    	}else if(traitForComparison.getDirection().intValue() == TraitsAvailableComponent.DECREASING.intValue()){
    		isIncreasing = false;
    	}
    	
    	String gid1ForCompare = Integer.toString(germplasmPair.getGid1());
		String gid2ForCompare = Integer.toString(germplasmPair.getGid2());
		String traitId = Integer.toString(traitForComparison.getTraitInfo().getId());
		for(EnvironmentForComparison envForComparison: environmentForComparisonList){
			
			String envId = envForComparison.getEnvironmentNumber().toString();
			String keyToChecked1 = traitId + ":" + envId + ":" +gid1ForCompare;
			String keyToChecked2 = traitId + ":" + envId + ":" +gid2ForCompare;
			
			ObservationList obs1 = observationMap.get(keyToChecked1);
    		ObservationList obs2 = observationMap.get(keyToChecked2);
    		
    		
    		//if(isValidObsValue(obs1, obs2)){
    		if(obs1 != null && obs2 != null){
	    		if(obs1.isValidObservationList() && obs2.isValidObservationList()){
	    			
	    			//double obs1Val = Double.parseDouble(obs1.getValue());
	    			//double obs2Val = Double.parseDouble(obs2.getValue());
	    			double obs1Val = obs1.getObservationAverage();
	    			double obs2Val = obs2.getObservationAverage();
	    			
	    			if(isIncreasing){
	    				if(obs1Val > obs2Val)
	    					counter++;
	    			}else{
	    				if(obs1Val < obs2Val)
	    					counter++;
	    			}
	    			
	    			
	    		}
    		}
			
		}
		
		return Integer.valueOf(counter);
    }
    private Integer getTotalNumOfEnv(GermplasmPair germplasmPair, 
    		TraitForComparison traitForComparison, Map<String, ObservationList> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	int counter = 0;
    		
    		
    		String gid1ForCompare = Integer.toString(germplasmPair.getGid1());
    		String gid2ForCompare = Integer.toString(germplasmPair.getGid2());
    		String traitId = Integer.toString(traitForComparison.getTraitInfo().getId());
    		for(EnvironmentForComparison envForComparison: environmentForComparisonList){
    			
    			String envId = envForComparison.getEnvironmentNumber().toString();
    			String keyToChecked1 = traitId + ":" + envId + ":" +gid1ForCompare;
    			String keyToChecked2 = traitId + ":" + envId + ":" +gid2ForCompare;
    			
    			ObservationList obs1 = observationMap.get(keyToChecked1);
        		ObservationList obs2 = observationMap.get(keyToChecked2);
        		if(obs1 != null && obs2 != null){
	        		if(obs1.isValidObservationList() && obs2.isValidObservationList()){
	        		//if(isValidObsValue(obs1, obs2)){
	        			counter++;
	        			
	        		}
        		}
    			
    		}
    	
    		
    	return Integer.valueOf(counter);
    }
    
    /*
     * Gets sum of all observed values for a trait for the a germplasm in the pair.
     * If index = 1, get mean for first germplasm in pair. If index = 2, get mean for second germplasm.
     */
    private String getMeanValue(GermplasmPair germplasmPair, int index, TraitForComparison traitForComparison, 
    		Map<String, ObservationList> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	double counter = 0;
    	int numOfValidEnv = 0;
    	double summation = 0;
    	String gid1ForCompare = Integer.toString(germplasmPair.getGid1());
		String gid2ForCompare = Integer.toString(germplasmPair.getGid2());
		String traitId = Integer.toString(traitForComparison.getTraitInfo().getId());

		for(EnvironmentForComparison envForComparison: environmentForComparisonList){			
			String envId = envForComparison.getEnvironmentNumber().toString();
			String keyToChecked1 = traitId + ":" + envId + ":" +gid1ForCompare;
			String keyToChecked2 = traitId + ":" + envId + ":" +gid2ForCompare;
			
			ObservationList obs1 = observationMap.get(keyToChecked1);
    		ObservationList obs2 = observationMap.get(keyToChecked2);
    		
    		// get only values for envt's where trait has been observed for both germplasms
    		if(obs1 != null && obs2 != null){
	    		if(obs1.isValidObservationList() && obs2.isValidObservationList()){
	    			numOfValidEnv++;
	    			
	    			if (index == 1){
	    				summation += obs1.getObservationAverage();
	    			} else if (index == 2){
	    				summation += obs2.getObservationAverage();
	    			}
	    			
	    		}
    		}
			
		}
		
		double mean = 0;
		if (numOfValidEnv > 0){
			mean = summation / numOfValidEnv;
		}
		
		return decimalFormmatter.format(mean);
    }

    
    private boolean isValidObsValue(Observation obs1, Observation obs2){
    	if(obs1 != null && obs2 != null && obs1.getValue() != null 
				&& obs2.getValue() != null && !obs1.getValue().equalsIgnoreCase("") &&
				!obs2.getValue().equalsIgnoreCase("") && isValidDoubleValue(obs1.getValue()) && isValidDoubleValue(obs2.getValue())){
			return true;
			
		}
    	return false;
    }
    
    public static boolean isValidDoubleValue(String val){
    	if(val != null && !val.equalsIgnoreCase("")){
    		try{
    			double d = Double.parseDouble(val);
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
        // TODO Auto-generated method stub
    }
    
    public void exportButtonClickAction(){
        //this.nextScreen.populateEnvironmentsTable(this.currentTestEntryGID, this.currentStandardEntryGID, this.traitsForComparisonList);
        //this.mainScreen.selectThirdTab();
    	
    	 String tempFileName = System.getProperty( "user.home" ) + "/HeadToHeadDataList.xls";
         
         HeadToHeadDataListExport listExporter = new HeadToHeadDataListExport();

         try {
        	EnvironmentForComparison envForComparison = this.finalEnvironmentForComparisonList.get(0);
         	Set<TraitForComparison> traitsIterator = envForComparison.getTraitAndObservationMap().keySet();
         	
         	                         
             
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
         
    	//MessageNotifier.showWarning(getWindow(), "Warning!", "Do the export now", Notification.POSITION_CENTERED);
        return;
    }
        
    public void backButtonClickAction(){
        this.mainScreen.selectThirdTab();
    }
}
