/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package org.generationcp.browser.study;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.study.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @author Mark Agarrado
 *
 */
@Configurable
public class TableViewerDatasetTable extends Table implements InitializingBean {
	
	private static final long serialVersionUID = 9114757066977945573L;
	private final static Logger LOG = LoggerFactory.getLogger(TableViewerDatasetTable.class);
	private static final String NUMERIC_VARIABLE = "Numeric variable";
	
	private StudyDataManager studyDataManager;
	private Integer studyId;
	private Integer datasetId;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	public TableViewerDatasetTable(StudyDataManager studyDataManager, Integer studyId, Integer datasetId) {
		this.studyDataManager = studyDataManager;
		this.studyId = studyId;
		this.datasetId = datasetId;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		generateDatasetTable();
	}

	private void generateDatasetTable() {
    	// set the column header ids
        List<VariableType> variables = new ArrayList<VariableType>();
        List<String> columnIds = new ArrayList<String>();

        try {
            DataSet dataset = studyDataManager.getDataSet(datasetId);
            variables = dataset.getVariableTypes().getVariableTypes();
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in getting variables of dataset: "
                            + datasetId + "\n" + e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            variables = new ArrayList<VariableType>();
            if (getWindow() != null) {
                MessageNotifier.showWarning(getWindow(), 
                        messageSource.getMessage(Message.ERROR_DATABASE), 
                        messageSource.getMessage(Message.ERROR_IN_GETTING_VARIABLES_OF_DATASET)  + " " + datasetId); 
            }
        }

        for(VariableType variable : variables)
        {
            if(variable.getStandardVariable().getStoredIn().getId() != TermId.STUDY_INFORMATION.getId()){
                String columnId = new StringBuffer().append(variable.getId()).append("-").append(variable.getLocalName()).toString();
                columnIds.add(columnId);
                
                // add the column ids to display for the Table
                if (NUMERIC_VARIABLE.equals(variable.getStandardVariable().getDataType().getName())) {
                	this.addContainerProperty(columnId, BigDecimal.class, null);
                } else {
                	//define column as Button for GID, else define as String
                	if (columnId.contains("GID")) {
                		this.addContainerProperty(columnId, Button.class, null);
                	} else {
                		this.addContainerProperty(columnId, String.class, null);
                	}
                }
            }
        }
        
        // set column headers for the Table
        for (VariableType variable : variables) {
            String columnId = new StringBuffer().append(variable.getId()).append("-").append(variable.getLocalName()).toString();
            String columnHeader = variable.getLocalName();
            this.setColumnHeader(columnId, columnHeader);
        }
        
        populateDatasetTable(columnIds);
    }
    
    private void populateDatasetTable(List<String> columnIds) {
        List<Experiment> experiments = new ArrayList<Experiment>();
        int size = -1;
        try {
            Long count = Long.valueOf(studyDataManager.countExperiments(datasetId));
            size = count.intValue(); 
        } catch (MiddlewareQueryException ex) {
            LOG.error("Error with getting experiments for dataset: " + datasetId + "\n" + ex.toString());
        }
        
        try {
        	if(size < 100){
        		experiments = studyDataManager.getExperiments(datasetId, 0, size);
        	}else{
        		int noOfRecordToQuery=50;
        		int batchRecordCount=size%noOfRecordToQuery;
        		int remainingRecCount=size-(batchRecordCount*noOfRecordToQuery);
        		int start=0;
        		int end=noOfRecordToQuery;;
        		
        		for(int i=1;i<=batchRecordCount;i++){
        			List<Experiment> experimentList= new ArrayList<Experiment>();
        			experimentList=studyDataManager.getExperiments(datasetId, start, end);
        			start=end+1;
        			end=end+noOfRecordToQuery;
        			experiments.addAll(experimentList);
        		}
        		
        		if(remainingRecCount > 0){
        			start=end+1;
        			List<Experiment> experimentList= new ArrayList<Experiment>();
        			experimentList=studyDataManager.getExperiments(datasetId, start, remainingRecCount);
        			experiments.addAll(experimentList);
        		}
        		
        	}
        } catch (MiddlewareQueryException ex) {
            // Log error in log file
            LOG.error("Error with getting ounitids for representation: " + datasetId + "\n" + ex.toString());
            experiments = new ArrayList<Experiment>();
        }

        if (!experiments.isEmpty()) {
            
            for(Experiment experiment : experiments){
                List<Variable> variables = new ArrayList<Variable>();
                
                VariableList factors = experiment.getFactors();
                if(factors != null){
                    variables.addAll(factors.getVariables());
                }
                
                VariableList variates = experiment.getVariates();
                if(variates != null){
                    variables.addAll(variates.getVariables());
                }
                
                Item item = this.addItem(experiment.getId());
                if (item != null) {
	                for(Variable variable : variables){
	                    String columnId = new StringBuffer().append(variable.getVariableType().getId()).append("-")
	                            .append(variable.getVariableType().getLocalName()).toString();
	                    
	                    if (NUMERIC_VARIABLE.equals(variable.getVariableType().getStandardVariable().getDataType().getName())) {
	                    	String cellValue=variable.getDisplayValue();
	                    	
	                    	if(cellValue.contains("/")){   // value is in date format but defined as Numeric Variable eg. 10/21/2004
	                    		item.getItemProperty(columnId).setValue(formatDateToNumber(cellValue));
	                    	}if(cellValue.equals("") ){
	                    		// nothing to set
	                    	}else{
	                    		BigDecimal decimalValue = new BigDecimal(cellValue);
//	                    		if (cellValue.contains(".")){
//	                    			decimalValue = decimalValue.setScale(2, RoundingMode.CEILING);
//	                    		}
								item.getItemProperty(columnId).setValue(decimalValue);
	                    	}
	                    	
	                    } else {
	                    	String stringValue = variable.getDisplayValue();
	                    	if (stringValue != null) {
	                    		stringValue = stringValue.trim();
	                    		// display value as Link if GID, else display as string
		                    	if ("GID".equals(variable.getVariableType().getLocalName().trim())) {
	                                Button gidButton = new Button(stringValue, new GidLinkButtonClickListener(stringValue));
	                                gidButton.setStyleName(BaseTheme.BUTTON_LINK);
	                                gidButton.setDescription("Click to view Germplasm information");
	                                item.getItemProperty(columnId).setValue(gidButton);
		                    	} else {
			                    	item.getItemProperty(columnId).setValue(stringValue);
		                    	}
	                    	}
	                    }
	                }
                }
            }
        }
    }
	
    private BigDecimal formatDateToNumber(String cellValue) {
    	try{
    		String[] cellNewValue=cellValue.split("/");
    		cellValue=cellNewValue[2]+cellNewValue[0]+cellNewValue[1]; // format to YYYYMMDD
    		return new BigDecimal(cellValue);
    	}catch(Exception e){
    		return new BigDecimal(0);
    	}
    }

	public StudyDataManager getStudyDataManager(){
    	return studyDataManager;
    }
    
    public Integer getStudyId(){
    	return studyId;
    }
    
    public Integer getDatasetId(){
    	return datasetId;
    }
    
}
