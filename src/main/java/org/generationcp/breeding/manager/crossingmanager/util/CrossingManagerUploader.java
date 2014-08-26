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

package org.generationcp.breeding.manager.crossingmanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.constants.TemplateCrossingCondition;
import org.generationcp.breeding.manager.constants.TemplateCrossingFactor;
import org.generationcp.breeding.manager.constants.TemplateUploadSource;
import org.generationcp.breeding.manager.crosses.NurseryTemplateImportFileComponent;
import org.generationcp.breeding.manager.crossingmanager.CrossingManagerImportFileComponent;
import org.generationcp.breeding.manager.pojos.ImportedCondition;
import org.generationcp.breeding.manager.pojos.ImportedConstant;
import org.generationcp.breeding.manager.pojos.ImportedFactor;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCross;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCrosses;
import org.generationcp.breeding.manager.pojos.ImportedVariate;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

@Deprecated
public class CrossingManagerUploader implements Receiver, SucceededListener {
        
    private static final long serialVersionUID = -1740972379887956957L;
    
    private final static Logger LOG = LoggerFactory.getLogger(CrossingManagerUploader.class);

	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
    
    public File file;
    
    private AbstractLayout source;
    private TemplateUploadSource uploadSourceType;

    private String tempFileName;

    private Integer currentSheet;
    private Integer currentRow;
    
    private InputStream inp;
    private Workbook wb;
    private String originalFilename;
    
    private ImportedGermplasmCrosses importedGermplasmCrosses;
    private GermplasmList maleGermplasmList;
    private boolean maleListIdIsSpecified = false;
    private GermplasmList femaleGermplasmList;
    private boolean femaleListIdIsSpecified = false;
    
    private List<String> requiredConditionRows;
    private List<String> requiredFactorRows;
    private Boolean fileIsValid;
    private Boolean hasInvalidData=false;
    private boolean readingBlankCrossesTemplateFile = false;
    
    private GermplasmListManager germplasmListManager;
    private GermplasmDataManager germplasmDataManager;
    
    private List<Integer> invalidMaleEntryIdsOnSecondSheet;
    private List<Integer> invalidFemaleEntryIdsOnSecondSheet;

    // TODO: consider renaming class to "NurseryTemplateUploader" or something so that it's a generic uploader utility class
    public CrossingManagerUploader(AbstractLayout source, GermplasmListManager germplasmListManager, GermplasmDataManager germplasmDataManager) {
        // take note of source type
        if (source instanceof CrossingManagerImportFileComponent) {
            this.uploadSourceType = TemplateUploadSource.CROSSING_MANAGER;
        } else if (source instanceof NurseryTemplateImportFileComponent) {
            this.uploadSourceType = TemplateUploadSource.NURSERY_TEMPLATE;
        }
        
        this.source = source;
        this.germplasmListManager = germplasmListManager;
        this.germplasmDataManager = germplasmDataManager;
        initializeUploadBehavior();
    }
    
    private void initializeUploadBehavior() {
        if (TemplateUploadSource.CROSSING_MANAGER.equals(this.uploadSourceType)) {
            requiredConditionRows = new ArrayList<String>(Arrays.asList(new String[] {
                    TemplateCrossingCondition.NID.getValue()
                    ,TemplateCrossingCondition.BREEDER_NAME.getValue()
                    ,TemplateCrossingCondition.BREEDER_ID.getValue()
                    ,TemplateCrossingCondition.SITE.getValue()
                    ,TemplateCrossingCondition.SITE_ID.getValue()
                    ,TemplateCrossingCondition.BREEDING_METHOD.getValue()
                    ,TemplateCrossingCondition.BREEDING_METHOD_ID.getValue()
                    ,TemplateCrossingCondition.FEMALE_LIST_NAME.getValue()
                    ,TemplateCrossingCondition.FEMALE_LIST_ID.getValue()
                    ,TemplateCrossingCondition.MALE_LIST_NAME.getValue()
                    ,TemplateCrossingCondition.MALE_LIST_ID.getValue()}));
            requiredFactorRows = new ArrayList<String>(Arrays.asList(new String[] {
                    TemplateCrossingFactor.CROSS.getValue()
                    ,TemplateCrossingFactor.FEMALE_ENTRY_ID.getValue()
                    ,TemplateCrossingFactor.MALE_ENTRY_ID.getValue()
                    ,TemplateCrossingFactor.FGID.getValue()
                    ,TemplateCrossingFactor.MGID.getValue()}));
        } else if (TemplateUploadSource.NURSERY_TEMPLATE.equals(this.uploadSourceType)) {
            requiredConditionRows = new ArrayList<String>(Arrays.asList(new String[] {
                    TemplateCrossingCondition.NID.getValue()
                    ,TemplateCrossingCondition.BREEDER_NAME.getValue()
                    ,TemplateCrossingCondition.BREEDER_ID.getValue()
                    ,TemplateCrossingCondition.SITE.getValue()
                    ,TemplateCrossingCondition.SITE_ID.getValue()
                    ,TemplateCrossingCondition.BREEDING_METHOD.getValue()
                    ,TemplateCrossingCondition.BREEDING_METHOD_ID.getValue()
                    ,TemplateCrossingCondition.FEMALE_LIST_NAME.getValue()
                    ,TemplateCrossingCondition.FEMALE_LIST_ID.getValue()
                    ,TemplateCrossingCondition.MALE_LIST_NAME.getValue()
                    ,TemplateCrossingCondition.MALE_LIST_ID.getValue()}));
            requiredFactorRows = new ArrayList<String>(Arrays.asList(new String[] {
                    TemplateCrossingFactor.CROSS.getValue()
                    ,TemplateCrossingFactor.FEMALE_ENTRY_ID.getValue()
                    ,TemplateCrossingFactor.MALE_ENTRY_ID.getValue()
                    ,TemplateCrossingFactor.FGID.getValue()
                    ,TemplateCrossingFactor.MGID.getValue()}));
        }
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) { 
        tempFileName = source.getApplication().getContext().getBaseDirectory().getAbsolutePath()+"/WEB-INF/uploads/imported_nurserytemplate.xls";
        FileOutputStream fos = null;    
        try {
            file = new File(tempFileName);
            fos = new FileOutputStream(file);
            
            originalFilename = filename;            
        } catch (final java.io.FileNotFoundException e) {
            return null;
        }
        return fos; // Return the output stream to write to
    }

    public void setTempFileNameForBlankTemplate(){
        tempFileName = source.getApplication().getContext().getBaseDirectory().getAbsolutePath()+"/WEB-INF/uploads/nursery_template.xls";
    }
    
    public void setReadingFromBlankTemplateFile(boolean readingBlankCrossesTemplateFile){
    	this.readingBlankCrossesTemplateFile = readingBlankCrossesTemplateFile;
    }
    
    @Override
    public void uploadSucceeded(SucceededEvent event) {
    	currentSheet = 0;
        currentRow = 0;
        
        maleGermplasmList = null;
        maleListIdIsSpecified = false;
        femaleGermplasmList = null;
        femaleListIdIsSpecified = false;
        importedGermplasmCrosses = null;

        fileIsValid = true;

        try {
            inp = new FileInputStream(tempFileName);
            wb = new HSSFWorkbook(inp);
            
            try{
                Sheet sheet1 = wb.getSheetAt(0);
                
                if(sheet1 == null || sheet1.getSheetName() == null || !(sheet1.getSheetName().equals("Description"))){
                    MessageNotifier.showError(source.getWindow(), "Error with reading file uploaded."
                            , "File doesn't have the first sheet - Description");
                    fileIsValid = false;
                    return;
                }
            } catch(Exception ex){
                MessageNotifier.showError(source.getWindow(), "Error with reading file uploaded."
                        , "File doesn't have the first sheet - Description");
                fileIsValid = false;
                return;
            }
            
            try{
                Sheet sheet2 = wb.getSheetAt(1);
                
                if(sheet2 == null || sheet2.getSheetName() == null || !(sheet2.getSheetName().equals("Observation"))){
                    MessageNotifier.showError(source.getWindow(), "Error with reading file uploaded."
                            , "File doesn't have the second sheet - Observation");
                    fileIsValid = false;
                    return;
                }
            } catch(Exception ex){
                MessageNotifier.showError(source.getWindow(), "Error with reading file uploaded."
                        , "File doesn't have the second sheet - Observation");
                fileIsValid = false;
                return;
            }
            
            readExcelSheets();
            
            if(fileIsValid==false){
                importedGermplasmCrosses = null;
            } 
            
            // <macky>: moved "selectManuallyMakeCrosses() / selectAlreadyDefinedCrossesInNurseryTemplateFile()"
            // code block to CrossingManagerImportFileComponent.uploadComponents.FinishedListener
                
            } catch (FileNotFoundException e) {
            	LOG.error("Error reading file.", e);
            } catch (IOException e) {
                showInvalidFileTypeError();
            } catch (ReadOnlyException e) {
                showInvalidFileTypeError();
            } catch (ConversionException e) {
                showInvalidFileTypeError();
            } catch (OfficeXmlFileException e){
                showInvalidFileTypeError();
            } catch (CrossingManagerUploaderException e){
                MessageNotifier.showError(source.getWindow(), 
                		"Error with reading file uploaded.", e.getMessage());
                fileIsValid = false;
            }
    }
    
    private void readExcelSheets() throws CrossingManagerUploaderException{
        if (TemplateUploadSource.CROSSING_MANAGER.equals(uploadSourceType)) {
            readSheet1();
            readSheet2();
        } else if (TemplateUploadSource.NURSERY_TEMPLATE.equals(uploadSourceType)) {
            readSheet1();
        }
    }

    private void readSheet1() throws CrossingManagerUploaderException{
        readNurseryTemplateFileInfo();
        readConditions();
        readFactors();
        readConstants();
        readVariates();
    }

    private void readNurseryTemplateFileInfo() throws CrossingManagerUploaderException{
        try {

            String study = getCellStringValue(0,0,1,true);
            String title = getCellStringValue(0,1,1,true);
            String pmKey = getCellStringValue(0,2,1,true);
            String objective = getCellStringValue(0,3,1,true);
            Date startDate = new SimpleDateFormat(DATE_AS_NUMBER_FORMAT).parse(getCellStringValue(0,4,1,true));
            Date endDate = new SimpleDateFormat(DATE_AS_NUMBER_FORMAT).parse(getCellStringValue(0,5,1,true));
            String studyType = getCellStringValue(0,6,1,true);
                        
            importedGermplasmCrosses = new ImportedGermplasmCrosses(originalFilename, study, title, pmKey, objective, startDate, endDate, studyType); 
            // <macky>: moved "crossingManagerSource.updateFilenameLabelValue(originalFilename);" 
            // to CrossingManagerImportFileComponent.uploadComponents.FinishedListener

        } catch (ParseException e) {
            LOG.error("Error with reading nursery basic info.", e);
            e.printStackTrace();
            throw new CrossingManagerUploaderException("Error with reading nursery basic info.", e);
        }

        //Prepare for next set of data
        while(!rowIsEmpty()){
            currentRow++;
        }
    }
    
    private void readConditions(){
        currentRow++; //Skip row from file info
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("CONDITION") 
                || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
                || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
                || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
                || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
                || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
                || !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("VALUE")
                || !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")) {
            showInvalidFileError("","Incorrect headers for conditions.");
        }

        //If file is still valid (after checking headers), proceed
        if(fileIsValid){
            ImportedCondition importedCondition;
            currentRow++; 
            while(!rowIsEmpty() && fileIsValid){
                importedCondition = new ImportedCondition(getCellStringValue(currentSheet,currentRow,0,true)
                        ,getCellStringValue(currentSheet,currentRow,1,true)
                        ,getCellStringValue(currentSheet,currentRow,2,true)
                        ,getCellStringValue(currentSheet,currentRow,3,true)
                        ,getCellStringValue(currentSheet,currentRow,4,true)
                        ,getCellStringValue(currentSheet,currentRow,5,true)
                        ,getCellStringValue(currentSheet,currentRow,6,true)
                        ,getCellStringValue(currentSheet,currentRow,7,true));
                importedGermplasmCrosses.addImportedCondition(importedCondition);
                
                //Retrieve Male GermplasmList object specified in the template file
                if (TemplateCrossingCondition.MALE_LIST_ID.getValue().equals(importedCondition.getCondition())){
                    maleGermplasmList = retrieveGermplasmList(importedCondition, "male");
                }
                //Retrieve Male GermplasmList object specified in the template file
                else if (TemplateCrossingCondition.FEMALE_LIST_ID.getValue().equals(importedCondition.getCondition())){
                    femaleGermplasmList = retrieveGermplasmList(importedCondition, "female");
                }

                currentRow++;
            }

        }

        validateRequiredConditions(requiredConditionRows);

        if (TemplateUploadSource.CROSSING_MANAGER.equals(this.uploadSourceType) && !readingBlankCrossesTemplateFile) {
        	// special validation for Crossing Manager only
        	//don't do this when manually making crosses and having a blank nursery template file read
        	validateCrossingConditionValues();
        }

        currentRow++;
    }
    
    private GermplasmList retrieveGermplasmList(ImportedCondition importedCondition, String parent) {
        GermplasmList germplasmList = null;
        if(importedCondition.getValue()!=null && importedCondition.getValue()!="") {
            // flag if list ID is specified
            if ("male".equals(parent)) {
                maleListIdIsSpecified = true;
            } else if ("female".equals(parent)) {
                femaleListIdIsSpecified = true;
            }
            
            try {
                germplasmList = germplasmListManager.getGermplasmListById(Integer.valueOf(importedCondition.getValue()));
            } catch (NumberFormatException e) {
                //reset values and flag if exception occurred
                if ("male".equals(parent)) {
                    maleGermplasmList = null;
                    maleListIdIsSpecified = false;
                } else if ("female".equals(parent)) {
                    femaleGermplasmList = null;
                    femaleListIdIsSpecified = false;
                }
            } catch (MiddlewareQueryException e) {
                //reset values and flag if exception occurred
                if ("male".equals(parent)) {
                    maleGermplasmList = null;
                    maleListIdIsSpecified = false;
                } else if ("female".equals(parent)) {
                    femaleGermplasmList = null;
                    femaleListIdIsSpecified = false;
                }
            }
        }
        return germplasmList;
    }
    
    private void validateRequiredConditions(List<String> requiredConditions) {
    	
        if (requiredConditions.size() > 0) {
            // build HashSet of Conditions read from the template file
            HashSet<String> fileConditions = new HashSet<String>();
            List<ImportedCondition> conditions = importedGermplasmCrosses.getImportedConditions();
            for (ImportedCondition c : conditions) {
                fileConditions.add(c.getCondition().toUpperCase());
            }
            
            // check Conditions in template file for each of the required conditions
            List<String> missingConditions = new ArrayList<String>();
            for (String reqCond : requiredConditions) {
                // if required condition is not in the list of conditions read from the file:
                if (!fileConditions.contains(reqCond)) {
                    missingConditions.add(reqCond);
                }
            }
            
            // display combined error messages of required conditions
            if (missingConditions.size() > 0) {
                // display error message of missing conditions
                String missingConditionString = missingConditions.toString().replace("[", "").replace("]", "").replace(", ", "<br/>");
                showInvalidFileError("Required Conditions not found in template file:", missingConditionString);
            }
            
            validateMethodInput();
        }
    }
    
    private void validateMethodInput() {
    	
    	//Do not check if file is not uploaded
    	if(originalFilename==null || originalFilename.equals(""))
    		return;
    	
    	Method method = null; 
    	String methodFromFile = "";
    	int methodIdFromFile = 0;
    	
        List<ImportedCondition> conditions = importedGermplasmCrosses.getImportedConditions();

        for (ImportedCondition c : conditions) {
            if(c.getCondition().equals(TemplateCrossingCondition.BREEDING_METHOD.getValue())){
            	methodFromFile = c.getValue();
            } else if(c.getCondition().equals(TemplateCrossingCondition.BREEDING_METHOD_ID.getValue()) && !"".equals(c.getValue())){
            	try {
            		methodIdFromFile = (Integer) Integer.valueOf(c.getValue());
            	} catch(NumberFormatException e){
    				showInvalidFileError("","Invalid Method ID.");            		
            	}
            }
        }
        
        if(!methodFromFile.equals("") && methodIdFromFile!=0 && conditions.size()>0){
	        //Check if BreedingMedthodID is valid
	    	try {
				method = germplasmDataManager.getMethodByID(methodIdFromFile);
				if(method==null){
					showInvalidFileError("","Invalid Method ID.");
				}
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				showInvalidFileError("","Invalid Method ID.");
			}
	        //Check if BreedingMethod name is the same with method of methodID
	        if(fileIsValid){
	        	if(method!=null && !method.getMname().toUpperCase().equals(methodFromFile.toUpperCase())){
	        		showInvalidFileError("","Method does not match given method ID.");
	        	}
	        }
    	} else if(methodFromFile.equals("") && methodIdFromFile!=0){
    		showInvalidFileError("","Method can't be blank.");
    	} else if(!methodFromFile.equals("") && methodIdFromFile == 0) {
    	    showInvalidFileError("","Method ID can't be blank.");
    	}
    }
    
    
    private void validateCrossingConditionValues() {
        // build HashMap of Conditions read from the template file
        HashMap<String, ImportedCondition> fileConditions = new HashMap<String, ImportedCondition>();
        List<ImportedCondition> conditions = importedGermplasmCrosses.getImportedConditions();
        for (ImportedCondition c : conditions) {
            fileConditions.put(c.getCondition(), c);
        }
        
        ImportedCondition templateFemaleListId = fileConditions.get(TemplateCrossingCondition.FEMALE_LIST_ID.getValue());
        if(templateFemaleListId == null){
        	showInvalidFileError("Invalid file", "Missing condition with name - " + TemplateCrossingCondition.FEMALE_LIST_ID.getValue() + ".");
        	return;
        }
        
        ImportedCondition templateFemaleListName = fileConditions.get(TemplateCrossingCondition.FEMALE_LIST_NAME.getValue());
        if(templateFemaleListName == null){
        	showInvalidFileError("Invalid file", "Missing condition with name - " + TemplateCrossingCondition.FEMALE_LIST_NAME.getValue() + ".");
        	return;
        }
        
        ImportedCondition templateMaleListId = fileConditions.get(TemplateCrossingCondition.MALE_LIST_ID.getValue());
        if(templateMaleListId == null){
        	showInvalidFileError("Invalid file", "Missing condition with name - " + TemplateCrossingCondition.MALE_LIST_ID.getValue() + ".");
        	return;
        }
        
        ImportedCondition templateMaleListName = fileConditions.get(TemplateCrossingCondition.MALE_LIST_NAME.getValue());
        if(templateMaleListName == null){
        	showInvalidFileError("Invalid file", "Missing condition with name - " + TemplateCrossingCondition.MALE_LIST_NAME.getValue() + ".");
        	return;
        }
        
        String conditionErrors = "";
        
        if (femaleGermplasmList == null) {
        	conditionErrors = conditionErrors + "<br/> The female germplasm list id \""+ templateFemaleListId.getValue() +"\" does not exist in the database.";
        } else {
        	String templateFemaleListNameValue = templateFemaleListName.getValue();
            String femaleGermplasmListName = femaleGermplasmList.getName();
            if (femaleListIdIsSpecified && !templateFemaleListNameValue.equals(femaleGermplasmListName)) {
                conditionErrors = conditionErrors + "<br/> The female list name \"" + templateFemaleListNameValue + "\" does not exist in the database.";
            }
        }
        
        if (maleGermplasmList == null) {
        	conditionErrors = conditionErrors + "<br/> The male germplasm list id \""+ templateMaleListId.getValue() +"\" does not exist in the database.";
        } else {
        	String templateMaleListNameValue = templateMaleListName.getValue();
        	String maleGermplasmListName = maleGermplasmList.getName();
        	if (maleListIdIsSpecified && !templateMaleListNameValue.equals(maleGermplasmListName)) {
        		conditionErrors = conditionErrors + "<br/> The male list name \"" + templateMaleListNameValue + "\" does not exist in the database.";
            }
        }
        
        if (!"".equals(conditionErrors)){
            showInvalidFileError("Invalid germplasm list information", conditionErrors);
        }
    }

    private void readFactors(){
        if(rowIsEmpty()) {
            currentRow++;
        }

        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("FACTOR") 
                || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
                || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
                || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
                || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
                || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
                || !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("NESTED IN")
                || !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")) {
            showInvalidFileError("", "Incorrect headers for factors.");
        }
        
        //If file is still valid (after checking headers), proceed
        if(fileIsValid){
            ImportedFactor importedFactor;                  
            currentRow++; //skip header
            while(!rowIsEmpty()){
                importedFactor = new ImportedFactor(getCellStringValue(currentSheet,currentRow,0,true)
                        ,getCellStringValue(currentSheet,currentRow,1,true)
                        ,getCellStringValue(currentSheet,currentRow,2,true)
                        ,getCellStringValue(currentSheet,currentRow,3,true)
                        ,getCellStringValue(currentSheet,currentRow,4,true)
                        ,getCellStringValue(currentSheet,currentRow,5,true)
                        ,getCellStringValue(currentSheet,currentRow,6,true)
                        ,getCellStringValue(currentSheet,currentRow,7,true));
                
                importedGermplasmCrosses.addImportedFactor(importedFactor);

                currentRow++;
            }
        }
        
        validateRequiredFactors(requiredFactorRows);
        
        currentRow++;
    }
    
    private void validateRequiredFactors(List<String> requiredFactors) {
        if (requiredFactors.size() > 0) {
            // build HashSet of Factors read from the template file
            HashSet<String> fileFactors = new HashSet<String>();
            List<ImportedFactor> factors = importedGermplasmCrosses.getImportedFactors();
            for (ImportedFactor f : factors) {
                fileFactors.add(f.getFactor().toUpperCase());
            }
            
            // check Factors in template file for each of the required factors
            List<String> missingFactors = new ArrayList<String>();
            for (String reqFact : requiredFactors) {
                // if required factor is not in the list of factors read from the file:
                if (!fileFactors.contains(reqFact)) {
                    missingFactors.add(reqFact);
                }
            }
            
            //  display combined error messages of required factors
            if (missingFactors.size() > 0) {
                // display error message of missing factors
                String missingFactorString = missingFactors.toString().replace("[", "").replace("]", "").replace(", ", "<br/>");
                showInvalidFileError("Required Factors not found in template file:", missingFactorString);
            }
        }
    }
    
    private void readConstants(){
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("CONSTANT") 
                || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
                || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
                || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
                || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
                || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
                || !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("VALUE")
                || !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("SAMPLE LEVEL")) {
            showInvalidFileError("", "Incorrect headers for constants.");
        }
        //If file is still valid (after checking headers), proceed
        if(fileIsValid){
            ImportedConstant importedConstant;
            currentRow++; //skip header
            while(!rowIsEmpty()){
                importedConstant = new ImportedConstant(getCellStringValue(currentSheet,currentRow,0,true)
                        ,getCellStringValue(currentSheet,currentRow,1,true)
                        ,getCellStringValue(currentSheet,currentRow,2,true)
                        ,getCellStringValue(currentSheet,currentRow,3,true)
                        ,getCellStringValue(currentSheet,currentRow,4,true)
                        ,getCellStringValue(currentSheet,currentRow,5,true)
                        ,getCellStringValue(currentSheet,currentRow,6,true)
                        ,getCellStringValue(currentSheet,currentRow,7,true));
                importedGermplasmCrosses.addImportedConstant(importedConstant);

                currentRow++;
            }
        }
        currentRow++;
    }
    
    private void readVariates(){
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("VARIATE")
                || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
                || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
                || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
                || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
                || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
                || !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("SAMPLE LEVEL")) {
            showInvalidFileError("", "Incorrect headers for variates.");
        }
        //If file is still valid (after checking headers), proceed
        if(fileIsValid){
            ImportedVariate importedVariate;
            currentRow++; //skip header
            while(!rowIsEmpty()){
                importedVariate = new ImportedVariate(getCellStringValue(currentSheet,currentRow,0,true)
                        ,getCellStringValue(currentSheet,currentRow,1,true)
                        ,getCellStringValue(currentSheet,currentRow,2,true)
                        ,getCellStringValue(currentSheet,currentRow,3,true)
                        ,getCellStringValue(currentSheet,currentRow,4,true)
                        ,getCellStringValue(currentSheet,currentRow,5,true)
                        ,getCellStringValue(currentSheet,currentRow,7,true));
                importedGermplasmCrosses.addImportedVariate(importedVariate);

                currentRow++;
            }
        }
        currentRow++;
    }
    
    private void readSheet2() throws CrossingManagerUploaderException{
        currentSheet = 1;
        currentRow = 0;
        
        ImportedGermplasmCross importedGermplasmCross;
        Boolean germplasmListDataAreValid = true;
        this.invalidMaleEntryIdsOnSecondSheet = new ArrayList<Integer>();
        this.invalidFemaleEntryIdsOnSecondSheet = new ArrayList<Integer>();
        
        if(fileIsValid){
            currentRow++;
            
            while(!rowIsEmpty()){
                importedGermplasmCross = new ImportedGermplasmCross();
                for(int col=0;col<importedGermplasmCrosses.getImportedFactors().size();col++){
                    if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("CROSS")){
                        importedGermplasmCross.setCross(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
                    } else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("FEMALE ENTRY ID")){
                        importedGermplasmCross.setFemaleEntryId(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
                    } else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("MALE ENTRY ID")){
                        importedGermplasmCross.setMaleEntryId(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
                    } else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("CROSSING DATE")){
                        try {
                            importedGermplasmCross.setCrossingDate(new SimpleDateFormat(DATE_AS_NUMBER_FORMAT).parse(getCellStringValue(currentSheet, currentRow, col, true)));
                        } catch (ParseException e) {
                        }
                    } else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("SEEDS HARVESTED")){
                        importedGermplasmCross.setSeedsHarvested(getCellStringValue(currentSheet, currentRow, col, true));
                    } else if(importedGermplasmCrosses.getImportedFactors().get(col).getFactor().toUpperCase().equals("NOTES")){
                        importedGermplasmCross.setNotes(getCellStringValue(currentSheet, currentRow, col, true));
                    }                 
                }
                importedGermplasmCrosses.addImportedGermplasmCross(importedGermplasmCross);
                
                //Check if male entryID and GID are valid
                Boolean maleFound = true; //set true for cases wherein there is no list id
                if(maleGermplasmList!=null){
                    maleFound = false;
                    for(int gd=0;gd<maleGermplasmList.getListData().size();gd++){
                        GermplasmListData germplasmListData = maleGermplasmList.getListData().get(gd);
                        if(germplasmListData.getEntryId().equals(importedGermplasmCross.getMaleEntryId())){
                            importedGermplasmCross.setMaleGId(germplasmListData.getGid());
                            importedGermplasmCross.setMaleDesignation(germplasmListData.getDesignation());
                            maleFound = true;
                        }
                    }
                    
                    if(!maleFound){
                        this.invalidMaleEntryIdsOnSecondSheet.add(importedGermplasmCross.getMaleEntryId());
                    }
                } else{
                    throw new CrossingManagerUploaderException("The male list specified on the Description sheet does not exist.");
                }
                
                //Check if female entryID and GID are valid
                Boolean femaleFound = true; //set true for cases wherein there is no list id
                if(femaleGermplasmList!=null){
                    femaleFound = false;                            
                    for(int gd=0;gd<femaleGermplasmList.getListData().size();gd++){
                        GermplasmListData germplasmListData = femaleGermplasmList.getListData().get(gd);
                        if(germplasmListData.getEntryId().equals(importedGermplasmCross.getFemaleEntryId())){
                            importedGermplasmCross.setFemaleGId(germplasmListData.getGid());
                            importedGermplasmCross.setFemaleDesignation(germplasmListData.getDesignation());
                            femaleFound = true;
                        }
                    }
                    
                    if(!femaleFound){
                        this.invalidFemaleEntryIdsOnSecondSheet.add(importedGermplasmCross.getFemaleEntryId());
                    }
                } else{
                    throw new CrossingManagerUploaderException("The female list specified on the Description sheet does not exist.");
                }
                
                if(maleFound==false || femaleFound==false)
                    germplasmListDataAreValid = false;                              
                
                currentRow++;
            }
        }
        if(germplasmListDataAreValid==false){
            hasInvalidData=true;
        }else{
            hasInvalidData=false;
        }
    }

    private Boolean rowIsEmpty(){
        return rowIsEmpty(currentRow);
    }

    private Boolean rowIsEmpty(Integer row){
        return rowIsEmpty(currentSheet, row);
    }

    private Boolean rowIsEmpty(Integer sheet, Integer row){
        for(int col=0;col<8;col++){
            if(getCellStringValue(sheet, row, col)!="" && getCellStringValue(sheet, row, col)!=null){
                return false;
            }
        }
        return true;            
    }    
    
    private String getCellStringValue(Integer sheetNumber, Integer rowNumber, Integer columnNumber){
        return getCellStringValue(sheetNumber, rowNumber, columnNumber, false);
    }
        
    private String getCellStringValue(Integer sheetNumber, Integer rowNumber, Integer columnNumber, Boolean followThisPosition){
        if(followThisPosition){
            currentSheet = sheetNumber;
            currentRow = rowNumber;
        }

        try {
            Sheet sheet = wb.getSheetAt(sheetNumber);
            Row row = sheet.getRow(rowNumber);
            Cell cell = row.getCell(columnNumber);
            return cell.getStringCellValue();
        } catch(IllegalStateException e) {
            Sheet sheet = wb.getSheetAt(sheetNumber);
            Row row = sheet.getRow(rowNumber);
            Cell cell = row.getCell(columnNumber);
            return String.valueOf(Integer.valueOf((int) cell.getNumericCellValue()));
        } catch(NullPointerException e) {
            return "";
        }
    }

    private void showInvalidFileError(String header, String message){
        if(fileIsValid){
            MessageNotifier.showError(source.getWindow(), header, message);
            fileIsValid = false;
        }
    }

    private void showInvalidFileTypeError(){
        if(fileIsValid){
            MessageNotifier.showError(source.getWindow(), "Error with file uploaded.", "Invalid import file type, you need to upload the correct XLS file.");
            fileIsValid = false;
        }
    }    
    
    public ImportedGermplasmCrosses getImportedGermplasmCrosses(){
        return importedGermplasmCrosses;
    }

    public GermplasmList getMaleGermplasmList() {
        return maleGermplasmList;
    }

    public GermplasmList getFemaleGermplasmList() {
        return femaleGermplasmList;
    }
    
    public boolean isMaleListIdSpecified(){
        return maleListIdIsSpecified;
    }
    
    public boolean isFemaleListIdSpecified(){
        return femaleListIdIsSpecified;
    }

    public Boolean hasInvalidData() {
        return hasInvalidData;
    }
    
    public boolean isFileValid(){
        return fileIsValid;
    }
    
    public List<Integer> getInvalidMaleEntryIds(){
        return this.invalidMaleEntryIdsOnSecondSheet;
    }
    
    public List<Integer> getInvalidFemaleEntryIds(){
        return this.invalidFemaleEntryIdsOnSecondSheet;
    }
};