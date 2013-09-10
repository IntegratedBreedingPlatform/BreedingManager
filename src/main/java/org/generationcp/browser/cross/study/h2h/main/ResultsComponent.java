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
import com.vaadin.ui.Table;
import com.vaadin.ui.Window.Notification;

import org.generationcp.browser.application.GermplasmStudyBrowserApplication;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
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

    private Table resultsTable;
    
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
    
    private String[] columnIdData = {NUM_OF_ENV_COLUMN_ID,NUM_SUP_COLUMN_ID,PVAL_COLUMN_ID,MEAN_DIFF_COLUMN_ID};
    private Map<String, String> columnIdDataMsgMap = new HashMap();
    
    public static DecimalFormat decimalFormmatter = new DecimalFormat("#,##0.00");
    
    public ResultsComponent(HeadToHeadCrossStudyMain mainScreen){
        this.currentStandardEntryGID = null;
        this.currentTestEntryGID = null;
        this.mainScreen = mainScreen;
        
        //initialize the data map
        columnIdDataMsgMap.put(NUM_OF_ENV_COLUMN_ID, "NoEnv");
        columnIdDataMsgMap.put(NUM_SUP_COLUMN_ID, "NoSup");
        columnIdDataMsgMap.put(PVAL_COLUMN_ID, "Pval");
        columnIdDataMsgMap.put(MEAN_DIFF_COLUMN_ID, "MeanDiff");
        
        
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("550px");
        setWidth("1000px");
    /*    
    testEntryLabel = new Label("<b>Test Entry:</b>");
    testEntryLabel.setContentMode(Label.CONTENT_XHTML);
    addComponent(testEntryLabel, "top:20px;left:30px");
        
    testEntryNameLabel = new Label();
    addComponent(testEntryNameLabel, "top:20px;left:100px");
        
    standardEntryLabel = new Label("<b>Standard Entry:</b>");
    standardEntryLabel.setContentMode(Label.CONTENT_XHTML);
    addComponent(standardEntryLabel, "top:20px;left:450px");
    
    standardEntryNameLabel = new Label();
    addComponent(standardEntryNameLabel, "top:20px;left:550px");
      */  
        
    resultsTable = new Table();
    resultsTable.setWidth("950px");
    resultsTable.setHeight("400px");
    resultsTable.setImmediate(true);
    resultsTable.setColumnCollapsingAllowed(true);
    resultsTable.setColumnReorderingAllowed(true);
    
    /*
    resultsTable.addContainerProperty(TRAIT_COLUMN_ID, String.class, null);
    resultsTable.addContainerProperty(NUM_OF_ENV_COLUMN_ID, Integer.class, null);
    resultsTable.addContainerProperty(NUM_SUP_COLUMN_ID, Integer.class, null);
    resultsTable.addContainerProperty(MEAN_TEST_COLUMN_ID, Double.class, null);
    resultsTable.addContainerProperty(MEAN_STD_COLUMN_ID, Double.class, null);
    resultsTable.addContainerProperty(MEAN_DIFF_COLUMN_ID, Double.class, null);
    resultsTable.addContainerProperty(PVAL_COLUMN_ID, Double.class, null);
        
    resultsTable.setColumnHeader(TRAIT_COLUMN_ID, "TRAIT");
    resultsTable.setColumnHeader(NUM_OF_ENV_COLUMN_ID, "# OF ENV");
    resultsTable.setColumnHeader(NUM_SUP_COLUMN_ID, "# SUP");
    resultsTable.setColumnHeader(MEAN_TEST_COLUMN_ID, "MEAN TEST");
    resultsTable.setColumnHeader(MEAN_STD_COLUMN_ID, "MEAN STD");
    resultsTable.setColumnHeader(MEAN_DIFF_COLUMN_ID, "MEAN DIFF");
    resultsTable.setColumnHeader(PVAL_COLUMN_ID, "PVAL");
    */    
    
    
    addComponent(resultsTable, "top:70px;left:30px");
    
    exportButton = new Button("Export");
    exportButton.setData(EXPORT_BUTTON_ID);
    exportButton.addListener(new org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener(this));
    exportButton.setEnabled(true);
    addComponent(exportButton, "top:500px;left:900px");

       backButton = new Button("Back");
       backButton.setData(BACK_BUTTON_ID);
       backButton.addListener(new org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener(this));
       addComponent(backButton, "top:500px;left:820px");
    }
    
    private void createEnvironmentsResultTable(List<EnvironmentForComparison> environmentForComparisonList, 
    		Map<String,String> germplasmNameIdMap, List<GermplasmPair> germplasmPairList, Map<String, Observation> observationMap){
    	 
    	this.resultsTable.removeAllItems();
    	this.finalEnvironmentForComparisonList = environmentForComparisonList; 
    	
        List<Object> propertyIds = new ArrayList<Object>();
        for(Object propertyId : resultsTable.getContainerPropertyIds()){
            propertyIds.add(propertyId);
        }
        
        for(Object propertyId : propertyIds){
        	resultsTable.removeContainerProperty(propertyId);
        }
        
        resultsTable.addContainerProperty(TEST_COLUMN_ID, String.class, null);
        resultsTable.addContainerProperty(STANDARD_COLUMN_ID, String.class, null);
        
        //environmentsTable.addContainerProperty(ENV_NUMBER_COLUMN_ID, Integer.class, null);
        //environmentsTable.addContainerProperty(LOCATION_COLUMN_ID, String.class, null);
        //environmentsTable.addContainerProperty(COUNTRY_COLUMN_ID, String.class, null);
        //environmentsTable.addContainerProperty(STUDY_COLUMN_ID, String.class, null);
        
        resultsTable.setColumnHeader(TEST_COLUMN_ID, "Test Entry");
        resultsTable.setColumnHeader(STANDARD_COLUMN_ID, "Standard Entry");
        
        resultsTable.setColumnAlignment(TEST_COLUMN_ID, Table.ALIGN_CENTER);
        resultsTable.setColumnAlignment(STANDARD_COLUMN_ID, Table.ALIGN_CENTER);
        
        //environmentsTable.setColumnHeader(LOCATION_COLUMN_ID, "LOCATION");
        //environmentsTable.setColumnHeader(COUNTRY_COLUMN_ID, "COUNTRY");
        //environmentsTable.setColumnHeader(STUDY_COLUMN_ID, "STUDY");
        
        EnvironmentForComparison envForComparison = environmentForComparisonList.get(0);
    	Set<TraitForComparison> traitsIterator = envForComparison.getTraitAndObservationMap().keySet();
    	
    	
        
        for(TraitForComparison traitForCompare : traitsIterator){        	
            for(String columnKey : columnIdData){
            	String msg = columnIdDataMsgMap.get(columnKey);
            	resultsTable.addContainerProperty(traitForCompare.getTraitInfo().getName()+columnKey, String.class, null);
            	resultsTable.setColumnHeader(traitForCompare.getTraitInfo().getName()+columnKey, traitForCompare.getTraitInfo().getName() + " " + msg);
            	resultsTable.setColumnAlignment(traitForCompare.getTraitInfo().getName()+columnKey, Table.ALIGN_CENTER);
            	
            }
        }
        
        //resultsTable.addStyleName("multirowheaders");
        
        
        for(GermplasmPair germplasmPair : germplasmPairList){
        	String uniquieId = germplasmPair.getGid1() + ":" + germplasmPair.getGid2();
        	String testEntry = germplasmNameIdMap.get(Integer.toString(germplasmPair.getGid1()));
        	String standardEntry =  germplasmNameIdMap.get(Integer.toString(germplasmPair.getGid2()));
        	Map<String,String> traitDataMap = new HashMap();
        	ResultsData resData = new ResultsData(germplasmPair.getGid1(), testEntry, germplasmPair.getGid2(), standardEntry, traitDataMap);
        	
        	
        	
        	Item item = resultsTable.addItem(uniquieId);
        	item.getItemProperty(TEST_COLUMN_ID).setValue(testEntry);
        	item.getItemProperty(STANDARD_COLUMN_ID).setValue(standardEntry);
        	traitsIterator = envForComparison.getTraitAndObservationMap().keySet();
        	for(TraitForComparison traitForCompare : traitsIterator){   
        		//String mainColumnIdId = NUM_OF_ENV_COLUMN_ID;
                //String cellKey = traitForCompare.getTraitInfo().getName()+mainColumnIdId;
                //String cellVal = getColumnValue(  mainColumnIdId,   germplasmPair,  traitForCompare,   observationMap,  environmentForComparisonList);                
        		//traitDataMap.put(cellKey, cellVal);
            	//item.getItemProperty(cellKey).setValue(cellVal);    
            	
            	
            	for(String columnKey : columnIdData){
            		String cellKey = traitForCompare.getTraitInfo().getName()+columnKey;
            		String cellVal = getColumnValue(  columnKey,   germplasmPair,  traitForCompare,   observationMap,  environmentForComparisonList);                
            		traitDataMap.put(cellKey, cellVal);
                	item.getItemProperty(cellKey).setValue(cellVal);                	
                }
            	/*
            	mainColumnIdId = NUM_SUP_COLUMN_ID;
            	cellKey = traitForCompare.getTraitInfo().getName()+mainColumnIdId;
                cellVal = getColumnValue(  mainColumnIdId,   germplasmPair,  traitForCompare,   observationMap,  environmentForComparisonList);
            	traitDataMap.put(cellKey,cellVal);            	
            	item.getItemProperty(cellKey).setValue(cellVal);
            	
            	mainColumnIdId = PVAL_COLUMN_ID;
            	cellKey = traitForCompare.getTraitInfo().getName()+mainColumnIdId;
                cellVal = getColumnValue(  mainColumnIdId,   germplasmPair,  traitForCompare,   observationMap,  environmentForComparisonList);
            	traitDataMap.put(cellKey,cellVal);
            	item.getItemProperty(cellKey).setValue(cellVal);
            	
            	mainColumnIdId = MEAN_DIFF_COLUMN_ID;
            	cellKey = traitForCompare.getTraitInfo().getName()+mainColumnIdId;
                cellVal = getColumnValue(  mainColumnIdId,   germplasmPair,  traitForCompare,   observationMap,  environmentForComparisonList);
            	traitDataMap.put(cellKey,cellVal);
            	item.getItemProperty(cellKey).setValue(cellVal);
            	*/
            }
        	resData.setTraitDataMap(traitDataMap);
    	}
        
    }
    
    private String getColumnValue(String columnId, GermplasmPair germplasmPair, TraitForComparison traitForComparison, Map<String, Observation> observationMap, List<EnvironmentForComparison> environmentForComparisonList){
    	String val = "0";
    	if(NUM_OF_ENV_COLUMN_ID.equalsIgnoreCase(columnId)){
    		//get the total number of environment where the germplasm pair was observer and the observation value is not null and not empty string
    		val = getTotalNumOfEnv(germplasmPair, traitForComparison, observationMap,environmentForComparisonList).toString();
    		
    		
    	}else if(NUM_SUP_COLUMN_ID.equalsIgnoreCase(columnId)){
    		val = getTotalNumOfSup(germplasmPair, traitForComparison, observationMap,environmentForComparisonList).toString();
    	}else if(PVAL_COLUMN_ID.equalsIgnoreCase(columnId)){
    		val = getPval(germplasmPair, traitForComparison, observationMap,environmentForComparisonList);
    		
    	}else if(MEAN_DIFF_COLUMN_ID.equalsIgnoreCase(columnId)){
    		val = getMeanDiff(germplasmPair, traitForComparison, observationMap,environmentForComparisonList);
    	}
    	return val;
    }
    private String getPval(GermplasmPair germplasmPair, TraitForComparison traitForComparison, Map<String, Observation> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	double counter = 0;
    	return decimalFormmatter.format(counter);
    }
    private String getMeanDiff(GermplasmPair germplasmPair, TraitForComparison traitForComparison, Map<String, Observation> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	double counter = 0;
    	//r * ( summation of [ (Tijk-Silk)/Nijl ] )
    	/*
    	 * Nijl = is the number of environment where both tijk and silk is not null and not empty string
    	 * r = 1 if increasing and -1 if decreasing
    	 * 
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
			
			Observation obs1 = observationMap.get(keyToChecked1);
    		Observation obs2 = observationMap.get(keyToChecked2);
    		
    		
    		if(obs1 != null && obs2 != null && obs1.getValue() != null 
    				&& obs2.getValue() != null && !obs1.getValue().equalsIgnoreCase("") &&
    				!obs2.getValue().equalsIgnoreCase("")){
    			numOfValidEnv++;
    			double obs1Val = Double.parseDouble(obs1.getValue());
    			double obs2Val = Double.parseDouble(obs2.getValue());
    			
    			listOfObsVal.add(Double.valueOf(obs1Val - obs2Val));
    			
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
    private Integer getTotalNumOfSup(GermplasmPair germplasmPair, TraitForComparison traitForComparison, Map<String, Observation> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
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
			
			Observation obs1 = observationMap.get(keyToChecked1);
    		Observation obs2 = observationMap.get(keyToChecked2);
    		
    		
    		if(obs1 != null && obs2 != null && obs1.getValue() != null 
    				&& obs2.getValue() != null && !obs1.getValue().equalsIgnoreCase("") &&
    				!obs2.getValue().equalsIgnoreCase("")){
    			
    			double obs1Val = Double.parseDouble(obs1.getValue());
    			double obs2Val = Double.parseDouble(obs2.getValue());
    			
    			if(isIncreasing){
    				if(obs1Val > obs2Val)
    					counter++;
    			}else{
    				if(obs1Val < obs2Val)
    					counter++;
    			}
    			
    			
    		}
			
		}
		
		return Integer.valueOf(counter);
    }
    private Integer getTotalNumOfEnv(GermplasmPair germplasmPair, TraitForComparison traitForComparison, Map<String, Observation> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	int counter = 0;
    		
    		
    		String gid1ForCompare = Integer.toString(germplasmPair.getGid1());
    		String gid2ForCompare = Integer.toString(germplasmPair.getGid2());
    		String traitId = Integer.toString(traitForComparison.getTraitInfo().getId());
    		for(EnvironmentForComparison envForComparison: environmentForComparisonList){
    			
    			String envId = envForComparison.getEnvironmentNumber().toString();
    			String keyToChecked1 = traitId + ":" + envId + ":" +gid1ForCompare;
    			String keyToChecked2 = traitId + ":" + envId + ":" +gid2ForCompare;
    			
    			Observation obs1 = observationMap.get(keyToChecked1);
        		Observation obs2 = observationMap.get(keyToChecked2);
        		
        		
        		if(obs1 != null && obs2 != null && obs1.getValue() != null 
        				&& obs2.getValue() != null && !obs1.getValue().equalsIgnoreCase("") &&
        				!obs2.getValue().equalsIgnoreCase("")){
        			counter++;
        			
        		}
    			
    		}
    	
    		
    	return Integer.valueOf(counter);
    }
    
    public void populateResultsTable(List<EnvironmentForComparison> environmentForComparisonList, Map<String,String> germplasmNameIdMap, List<GermplasmPair> germplasmPair, Map<String, Observation> observationMap){
    	createEnvironmentsResultTable(environmentForComparisonList, germplasmNameIdMap, germplasmPair, observationMap);
    	/*
        if(areCurrentGIDsDifferentFromGiven(testEntryGID, standardEntryGID)){
            this.resultsTable.removeAllItems();
            
            List<Result> results = getResults(testEntryGID, standardEntryGID, traitsForComparisonList);
            for(Result result : results){
                this.resultsTable.addItem(new Object[]{result.getTraitName(), result.getNumberOfEnvironments()
                        , result.getNumberOfSup(), result.getMeanTest(), result.getMeanStd(), result.getMeanDiff()
                        , result.getPval()}, result.getTraitName());
            }
            
            this.resultsTable.setColumnCollapsed(NUM_SUP_COLUMN_ID, true);
            this.resultsTable.setColumnCollapsed(PVAL_COLUMN_ID, true);
            this.resultsTable.requestRepaint();
        }
        */
    }
    
/*
    public void populateResultsTable(Integer testEntryGID, Integer standardEntryGID, List<TraitForComparison> traitsForComparisonList){
        if(areCurrentGIDsDifferentFromGiven(testEntryGID, standardEntryGID)){
            this.resultsTable.removeAllItems();
            
            List<Result> results = getResults(testEntryGID, standardEntryGID, traitsForComparisonList);
            for(Result result : results){
                this.resultsTable.addItem(new Object[]{result.getTraitName(), result.getNumberOfEnvironments()
                        , result.getNumberOfSup(), result.getMeanTest(), result.getMeanStd(), result.getMeanDiff()
                        , result.getPval()}, result.getTraitName());
            }
            
            this.resultsTable.setColumnCollapsed(NUM_SUP_COLUMN_ID, true);
            this.resultsTable.setColumnCollapsed(PVAL_COLUMN_ID, true);
            this.resultsTable.requestRepaint();
        }
    }
    */
    
    @SuppressWarnings("rawtypes")
    private List<Result> getResults(Integer testEntryGID, Integer standardEntryGID, List<TraitForComparison> traitsForComparisonList){
        List<Result> toreturn = new ArrayList<Result>();
        
        try{
            Germplasm testEntry = this.germplasmDataManager.getGermplasmWithPrefName(testEntryGID);
            Germplasm standardEntry = this.germplasmDataManager.getGermplasmWithPrefName(standardEntryGID);
            
            String testEntryPrefName = null;
            if(testEntry.getPreferredName() != null){
                testEntryPrefName = testEntry.getPreferredName().getNval().trim();
            } else{
                MessageNotifier.showWarning(getWindow(), "Warning!", "The germplasm you selected as test entry doesn't have a preferred name, "
                    + "please select a different germplasm.", Notification.POSITION_CENTERED);
                return new ArrayList<Result>();
            }
            
            String standardEntryPrefName = null;
            if(standardEntry.getPreferredName() != null){
                standardEntryPrefName = standardEntry.getPreferredName().getNval().trim();
            } else{
            MessageNotifier.showWarning(getWindow(), "Warning!", "The standard entry germplasm you selected as standard entry doesn't have a preferred name, "
                    + "please select a different germplasm.", Notification.POSITION_CENTERED);
                return new ArrayList<Result>();
            }
            
            GermplasmDataManagerImpl dataManagerImpl = (GermplasmDataManagerImpl) this.germplasmDataManager;
            String queryString = "select trait_name, entry_designation, count(observed_value), AVG(observed_value)"
                + " from h2h_details"
                + " where entry_designation in ('"+ testEntryPrefName + "','" + standardEntryPrefName + "')"
                + " group by trait_name, entry_designation"; 
            Query query = dataManagerImpl.getCurrentSessionForCentral().createSQLQuery(queryString);
            List results = query.list();
            Iterator resultsIterator = results.iterator();
            while(resultsIterator.hasNext()){
                Object resultArray1[] = (Object[]) resultsIterator.next();
                Object resultArray2[] = null;
                
                if(resultsIterator.hasNext()){
                    resultArray2 = (Object[]) resultsIterator.next();
                } else{
                    break;
                }
                
                String traitName = (String) resultArray1[0];
                if(traitName != null){
                    traitName = traitName.trim().toUpperCase();
                }
                Integer numberOfEnvironments = getNumberOfEnvironmentsForTrait(traitName, traitsForComparisonList);
                String entry1 = (String) resultArray1[1];
                Double averageOfEntry1 = (Double) resultArray1[3];
                averageOfEntry1 = Math.round(averageOfEntry1 * 100.0) / 100.0;
                Double averageOfEntry2 = (Double) resultArray2[3];
                averageOfEntry2 = Math.round(averageOfEntry2 * 100.0) / 100.0;
                
                Double meanDiff = null;
                if(testEntryPrefName.equals(entry1)){
                    meanDiff = averageOfEntry1 - averageOfEntry2;
                    meanDiff = Math.round(meanDiff * 100.0) / 100.0;
                    toreturn.add(new Result(traitName, numberOfEnvironments, null, averageOfEntry2, averageOfEntry1, meanDiff, null));
                } else{
                    meanDiff = averageOfEntry2 - averageOfEntry1;
                    meanDiff = Math.round(meanDiff * 100.0) / 100.0;
                    toreturn.add(new Result(traitName, numberOfEnvironments, null, averageOfEntry1, averageOfEntry2, meanDiff, null));
                }
            }
        } catch(MiddlewareQueryException ex){
            ex.printStackTrace();
            LOG.error("Database error!", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Please report to IBP.", Notification.POSITION_CENTERED);
            return new ArrayList<Result>();
        } catch(Exception ex){
            ex.printStackTrace();
            LOG.error("Database error!", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Please report to IBP.", Notification.POSITION_CENTERED);
            return new ArrayList<Result>();
        }
        
        return toreturn;
    }
    
    private Integer getNumberOfEnvironmentsForTrait(String traitName, List<TraitForComparison> traitsForComparisonList){
        Integer toreturn = 0;
        /*
        for(TraitForComparison trait : traitsForComparisonList){
            if(trait.getName().equals(traitName)){
                return trait.getNumberOfEnvironments();
            }
        }
        */
        return toreturn;
    }
    
    private boolean areCurrentGIDsDifferentFromGiven(Integer currentTestEntryGID, Integer currentStandardEntryGID){
        if(this.currentTestEntryGID != null && this.currentStandardEntryGID != null){
            if(this.currentTestEntryGID == currentTestEntryGID && this.currentStandardEntryGID == currentStandardEntryGID){
                return false;
            }
        }
        
        return true;
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
         	
         	                         
             
                 listExporter.exportHeadToHeadDataListExcel(tempFileName, resultsTable, traitsIterator, columnIdData, columnIdDataMsgMap);
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
