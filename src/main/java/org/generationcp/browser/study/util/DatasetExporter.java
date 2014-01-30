package org.generationcp.browser.study.util;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.generationcp.browser.study.constants.StudyTemplateConstants;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.util.PoiUtil;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.StudyDataManagerImpl;


public class DatasetExporter {

    private static final int CONDITION_LIST_HEADER_ROW_INDEX = 8;
    
    private StudyDataManagerImpl studyDataManager;
    private Integer studyId;
    private Integer datasetId;
    
    private HSSFCellStyle labelStyle;
    private HSSFCellStyle headingStyle;
    private HSSFCellStyle variateHeadingStyle;
    
    private int observationSheetColumnIndex;
    private List <String> factors = new ArrayList<String>();
    private List <String> variates = new ArrayList<String>();
    
    public DatasetExporter(StudyDataManagerImpl studyDataManager, Object traitDataManager, Integer studyId, Integer representationId) {
        this.studyDataManager = studyDataManager;
        this.studyId = studyId;
        this.datasetId = representationId;
    }
   
    public DatasetExporter(StudyDataManagerImpl studyDataManager, Integer studyId, Integer datasetId){
        this.studyDataManager = studyDataManager;
        this.studyId = studyId;
        this.datasetId = datasetId;
    }
    
    public FileOutputStream exportToFieldBookExcelUsingIBDBv2(String filename) throws DatasetExporterException {
        
        if(studyDataManager == null){
            throw new DatasetExporterException("studyDataManager should not be null.");
        }
        
        //create workbook
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFCellStyle cellStyleForObservationSheet = workbook.createCellStyle();
        
        initializeCellStyles(workbook);
        
        //create two sheets, one for description and another for measurements
        HSSFSheet descriptionSheet = workbook.createSheet(StudyTemplateConstants.DESCRIPTION_TAB.getHeader());
        HSSFSheet observationSheet = workbook.createSheet(StudyTemplateConstants.OBSERVATION_TAB.getHeader());
        
        observationSheetColumnIndex = 0;
        
        //write the details on the first sheet - description
        //get the study first
        org.generationcp.middleware.domain.dms.Study study = null;
        
        try {
            study = this.studyDataManager.getStudy(this.studyId);
        } catch (MiddlewareQueryException ex) {
            throw new DatasetExporterException("Error with getting Study with id: " + this.studyId, ex);
        }
        
        if(study != null) {
            //get the needed study details
            String name = study.getName();
            
            createDescriptionSheet(descriptionSheet, study, name);
            
            //populate the measurements sheet
            createObservationSheet(cellStyleForObservationSheet, observationSheet, name);
        }
        
        adjustColumnWidths(descriptionSheet, observationSheet);
        
        return writeExcelFile(filename, workbook);
    }

    /*
     * Generation the Observation sheet in the file. It contains the factors and variates
     * and their values across the experiments
     */
	private void createObservationSheet(HSSFCellStyle cellStyleForObservationSheet,
			HSSFSheet observationSheet,	String name) throws DatasetExporterException {
		//establish the columns of the dataset first
		createObservationSheetHeaderRow(observationSheet);
		
		//then work with the data
		//do it by 50 rows at a time
		int pageSize = 50;
		long totalNumberOfRows = 0;
		int sheetRowIndex = 1;
		
		try {
		    totalNumberOfRows = this.studyDataManager.countExperiments(this.datasetId);
		} catch(Exception ex) {
		    throw new DatasetExporterException("Error with getting count of experiments for study - " + name 
		            + ", dataset - " + this.datasetId, ex); 
		}
		
		for(int start = 0; start < totalNumberOfRows; start = start + pageSize) {
		    List<Experiment> experiments = new ArrayList<Experiment>();
		    try {
		        experiments = this.studyDataManager.getExperiments(this.datasetId, start, pageSize);
		    } catch(Exception ex) {
		        throw new DatasetExporterException("Error with getting ounit ids of study - " + name 
		                + ", representation - " + this.datasetId, ex); 
		    }
		    
		    //map each experiment into a row in the observation sheet
		    sheetRowIndex = writeExperiments(cellStyleForObservationSheet,
					observationSheet, sheetRowIndex, experiments);
		}
	}

	/*
	 * Create the header row in Observation sheet containing factors and variates names
	 */
	private void createObservationSheetHeaderRow(HSSFSheet observationSheet) {
		HSSFRow datasetHeaderRow = observationSheet.createRow(0);
		for(int i=0; i< factors.size(); i++) {
			String columnName = factors.get(i);
	        HSSFCell cell = datasetHeaderRow.createCell(i);
	        cell.setCellValue(columnName);
	        cell.setCellStyle(headingStyle);
		}
		int startColumn = factors.size();
		for(int i=0; i< variates.size(); i++) {
			String columnName = variates.get(i);
	        HSSFCell cell = datasetHeaderRow.createCell(i + startColumn);
	        cell.setCellValue(columnName);
	        cell.setCellStyle(variateHeadingStyle);
		}
	}

	/*
	 * Adjust column widths of description sheet to fit contents
	 */
	private void adjustColumnWidths(HSSFSheet descriptionSheet,
			HSSFSheet observationSheet) {
        for(int ctr = 0; ctr < 8; ctr++) {
            if(ctr != 1) {
                descriptionSheet.autoSizeColumn(ctr);
            }
        }
        
        for(int ctr = 0; ctr < observationSheetColumnIndex; ctr++) {
            observationSheet.autoSizeColumn(ctr);
        }
	}

	private FileOutputStream writeExcelFile(String filename,
			HSSFWorkbook workbook) throws DatasetExporterException {
		try {
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            return fileOutputStream;
        } catch(Exception ex) {
            throw new DatasetExporterException("Error with writing to: " + filename, ex);
        }
	}

	/*
	 * For all experiments and dataset, write the factors and variates values
	 * in the Observation sheet
	 */
	@SuppressWarnings("deprecation")
	private int writeExperiments(HSSFCellStyle cellStyleForObservationSheet,
			HSSFSheet observationSheet, int sheetRowIndex, List<Experiment> experiments) {
		for(Experiment experiment : experiments) {
		    HSSFRow row = observationSheet.createRow(sheetRowIndex);
		    sheetRowIndex++;
		        
		    List<Variable> factorsOfExperiments = experiment.getFactors().getVariables();
		    for(Variable factorVariable : factorsOfExperiments){
		        String factorName = factorVariable.getVariableType().getLocalName();
		        if(factorName != null){
		            factorName = factorName.trim();
		        }
		        Integer columnIndexInteger = factors.indexOf(factorName);
		        if(columnIndexInteger != null){
		            short columnIndex = columnIndexInteger.shortValue();
		            if(columnIndex >= 0) {
						HSSFCell cell = row.createCell(columnIndex);
		                boolean isNumeric = Util.isNumericVariable(
		                		factorVariable.getVariableType().getStandardVariable().getDataType().getId());
		                if(isNumeric){
		                    double elemValue = 0;
		                    if(factorVariable.getValue() != null){
		                        try{
		                            elemValue = Double.valueOf(factorVariable.getValue());
		                            cell.setCellValue(elemValue);
		                        }catch(NumberFormatException ex){
		                            String value = factorVariable.getValue();
		                            if(value != null) {
		                                value = value.trim();
		                            }
		                            cell.setCellValue(value);
		                        }
		                    } else {
		                        String nullValue = null;
		                        cell.setCellValue(nullValue);
		                    }
		                } else{
		                    String value = factorVariable.getDisplayValue();
		                    if(value != null) {
		                        value = value.trim();
		                    }
		                    cell.setCellValue(value);
		                }
		            }
		        }
		    }
		        
		    List<Variable> variateVariables = experiment.getVariates().getVariables();
		    for(Variable variateVariable : variateVariables){
		        String variateName = variateVariable.getVariableType().getLocalName();
		        if(variateName != null){
		            variateName = variateName.trim();
		        }
		        Integer columnIndexInteger = variates.indexOf(variateName);
		        if(columnIndexInteger != null){
		            short columnIndex = columnIndexInteger.shortValue();
		            if(columnIndex >= 0) {
		                Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
		                boolean isNumeric = Util.isNumericVariable(
		                		variateVariable.getVariableType().getStandardVariable().getDataType().getId());
		                if(isNumeric){
		                    double elemValue = 0;
		                    if(variateVariable.getValue() != null){
		                        try{
		                            elemValue = Double.valueOf(variateVariable.getDisplayValue());
		                            cell.setCellValue(elemValue);
		                        }catch(NumberFormatException ex){
		                            String value = variateVariable.getValue();
		                            if(value != null) {
		                                value = value.trim();
		                            }
		                            cell.setCellValue(value);
		                        }
		                    } else {
		                        String nullValue = null;
		                        cell.setCellValue(nullValue);
		                    }
		                } else{
		                    String value = variateVariable.getDisplayValue();
		                    if(value != null) {
		                        value = value.trim();
		                    }
		                    cell.setCellValue(value);
		                }
		            }
		        }
		    }
		}
		return sheetRowIndex;
	}

	/*
	 * Create description sheet containing the Study Details, Conditions, 
	 * Factors, Constants and Variates sections
	 */
	private void createDescriptionSheet(HSSFSheet descriptionSheet,
			org.generationcp.middleware.domain.dms.Study study, String name)
			throws DatasetExporterException {
		createStudyDetailsSection(labelStyle, descriptionSheet, study, name);
		
		int conditionRowIndex = createConditionsSection(headingStyle, descriptionSheet, study);
		
		DataSet dataset = null;
		try {
		    dataset = this.studyDataManager.getDataSet(this.datasetId);
		} catch (MiddlewareQueryException ex) {
		    throw new DatasetExporterException("Error with getting Dataset with id: " + this.datasetId, ex);
		}
		
		//get the factors and their details
		VariableTypeList datasetVariableTypes = dataset.getVariableTypes();
		int factorRowIndex = createFactorsSection(descriptionSheet, conditionRowIndex, datasetVariableTypes);
		
		//row with headings for variate list
		int constantHeaderRowIndex =  factorRowIndex + 1;
		createSectionHeaderRow(variateHeadingStyle, descriptionSheet, StudyTemplateConstants.getConstantsHeaders(), constantHeaderRowIndex);
		//TODO output actual constants
		int constantRowIndex = constantHeaderRowIndex + 1;
		descriptionSheet.createRow(constantRowIndex);
		
		int variateHeaderRowIndex = constantRowIndex + 1;
		createVariatesSection(descriptionSheet, datasetVariableTypes, variateHeaderRowIndex);
	}

	private void createVariatesSection(HSSFSheet descriptionSheet, 
			VariableTypeList datasetVariableTypes, int variateHeaderRowIndex) {
		createSectionHeaderRow(variateHeadingStyle, descriptionSheet, StudyTemplateConstants.getVariateHeaders(), variateHeaderRowIndex);
		
		//get the variates and their details
		VariableTypeList variateVariableTypeList = datasetVariableTypes.getVariates();
		List<VariableType> variateVariableTypes = variateVariableTypeList.getVariableTypes();
		
		populateVariates(descriptionSheet, variateHeaderRowIndex, variateVariableTypes);
	}

	private void populateVariates(HSSFSheet descriptionSheet, int variateHeaderRowIndex,
			List<VariableType> variateVariableTypes) {
		int variateRowIndex = variateHeaderRowIndex + 1;
		for(VariableType variate : variateVariableTypes) {
		    String dataType = variate.getStandardVariable().getDataType().getName();
		    String variateName = variate.getLocalName();
		    if(variateName != null) {
		        variateName = variateName.trim();
		    }
		    
		    HSSFRow variateRow = descriptionSheet.createRow(variateRowIndex);
		    variateRow.createCell(0).setCellValue(variateName);
		    if(variate.getLocalDescription() != null && variate.getLocalDescription().length() != 0){
		        variateRow.createCell(1).setCellValue(variate.getLocalDescription().trim());
		    } else{
		        variateRow.createCell(1).setCellValue(variate.getStandardVariable().getDescription());
		    }
		    if(variate.getStandardVariable().getProperty() != null){
		        variateRow.createCell(2).setCellValue(variate.getStandardVariable().getProperty().getName());
		    } else{
		        variateRow.createCell(2).setCellValue(variate.getStandardVariable().getName());
		    }
		    variateRow.createCell(3).setCellValue(variate.getStandardVariable().getScale().getName());
		    variateRow.createCell(4).setCellValue(variate.getStandardVariable().getMethod().getName());
		    variateRow.createCell(5).setCellValue(dataType);
		    // empty for "Value" column, that's why no index 6
		    variateRow.createCell(7).setCellValue(PhenotypicType.TRIAL_DESIGN.getLabelList().get(0));
		    
		    variates.add(variateName);
		    observationSheetColumnIndex++;
		    
		    variateRowIndex++;
		}
	}

	private int createFactorsSection(HSSFSheet descriptionSheet, int conditionRowIndex,
			VariableTypeList datasetVariableTypes) {
		VariableTypeList factorVariableTypeList = datasetVariableTypes.getFactors();
		List<VariableType> factorVariableTypes = factorVariableTypeList.getVariableTypes();
		
		int factorRowHeaderIndex = conditionRowIndex + 1;
		createSectionHeaderRow(headingStyle, descriptionSheet, StudyTemplateConstants.getFactorHeaders(), factorRowHeaderIndex);
		            
		int factorRowIndex = populateFactors(descriptionSheet, factorRowHeaderIndex, factorVariableTypes);
		return factorRowIndex;
	}

	private int populateFactors(HSSFSheet descriptionSheet, int factorRowHeaderIndex,
			List<VariableType> factorVariableTypes) {
		int factorRowIndex = factorRowHeaderIndex + 1;
		for(VariableType factor : factorVariableTypes) {
		    StandardVariable standardVariable = factor.getStandardVariable();
			String dataType = standardVariable.getDataType().getName();
		    String factorName = factor.getLocalName();
		    if(factorName != null) {
		        factorName = factorName.trim();
		    }
		    
		    //check if factor is already written as a condition
		    Integer temp = factors.indexOf(factorName);
		    if(temp == -1 && !factorName.equals("STUDY")) {
		        HSSFRow factorRow = descriptionSheet.createRow(factorRowIndex);
		        factorRow.createCell(0).setCellValue(factorName);
		        if(factor.getLocalDescription() != null && factor.getLocalDescription().length() != 0){
		            factorRow.createCell(1).setCellValue(factor.getLocalDescription());
		        } else{
		            factorRow.createCell(1).setCellValue(standardVariable.getDescription());
		        }
		        if(standardVariable.getProperty() != null){
		            factorRow.createCell(2).setCellValue(standardVariable.getProperty().getName());
		        } else{
		            factorRow.createCell(2).setCellValue(standardVariable.getName());
		        }
		        factorRow.createCell(3).setCellValue(standardVariable.getScale().getName());
		        factorRow.createCell(4).setCellValue(standardVariable.getMethod().getName());
		        factorRow.createCell(5).setCellValue(dataType);
		        //empty string for "Value" column - why no index 6
		        
		        //TODO to be compatible with old Fielbook, look for the actual main factor name 
		        // for PLOT, ENTRY and TRIAL to set as label value
		        PhenotypicType phenotypicType = standardVariable.getPhenotypicType();
		        if (phenotypicType != null){
		        	factorRow.createCell(7).setCellValue(phenotypicType.getLabelList().get(0));
		        }
		        
		        factors.add(factorName);
		        observationSheetColumnIndex++;
		            
		        factorRowIndex++;
		    }
		}
		//empty row
		descriptionSheet.createRow(factorRowIndex);

		return factorRowIndex;
	}

	private void initializeCellStyles(HSSFWorkbook workbook) {
		HSSFFont whiteFont = workbook.createFont();
        whiteFont.setColor(new HSSFColor.WHITE().getIndex());
        whiteFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        
		// set cell style for labels in the description sheet
        labelStyle = workbook.createCellStyle();
        labelStyle.setFillForegroundColor(new HSSFColor.BROWN().getIndex());
        labelStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        labelStyle.setFont(whiteFont);
        
        // set cell style for headings in the description sheet
        headingStyle = workbook.createCellStyle();
        headingStyle.setFillForegroundColor(new HSSFColor.SEA_GREEN().getIndex());
        headingStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        headingStyle.setFont(whiteFont);
        
        // set cell style for variate headings in the description sheet
        variateHeadingStyle = workbook.createCellStyle();
        variateHeadingStyle.setFillForegroundColor(new HSSFColor.BLUE().getIndex());
        variateHeadingStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        variateHeadingStyle.setFont(whiteFont);
	}

	private int createConditionsSection(HSSFCellStyle headingStyle,	HSSFSheet descriptionSheet, 
			org.generationcp.middleware.domain.dms.Study study) {
		//row with headings for condition list
		createSectionHeaderRow(headingStyle, descriptionSheet, StudyTemplateConstants.getConditionHeaders(), CONDITION_LIST_HEADER_ROW_INDEX);
		
		//get the conditions and their details
		int conditionRowIndex = populateStudyConditions(descriptionSheet, study);
		
		//empty row
         descriptionSheet.createRow(conditionRowIndex);
		
         return conditionRowIndex;
	}

    /*
     * Populates the condition section with the actual study conditions
     */
	private int populateStudyConditions(HSSFSheet descriptionSheet,
			org.generationcp.middleware.domain.dms.Study study) {
		
		VariableList conditions = study.getConditions();
		
		int conditionRowIndex = CONDITION_LIST_HEADER_ROW_INDEX + 1;
		List<Variable> conditionVariables = conditions.getVariables();
		for(Variable conditionVariable : conditionVariables) {
		    String conditionName = conditionVariable.getVariableType().getLocalName();
		    if(conditionName != null) {
		        conditionName = conditionName.trim();
		    }
		    Term dataType = conditionVariable.getVariableType().getStandardVariable().getDataType();
		    
		    HSSFRow conditionRow = descriptionSheet.createRow(conditionRowIndex);
		    conditionRow.createCell(0).setCellValue(conditionName);
		    if(conditionVariable.getVariableType().getLocalDescription() != null && conditionVariable.getVariableType().getLocalDescription().length() != 0){
		        conditionRow.createCell(1).setCellValue(conditionVariable.getVariableType().getLocalDescription());
		    } else{
		        conditionRow.createCell(1).setCellValue(conditionVariable.getVariableType().getStandardVariable().getDescription());
		    }
		    if(conditionVariable.getVariableType().getStandardVariable().getProperty() != null){
		        conditionRow.createCell(2).setCellValue(conditionVariable.getVariableType().getStandardVariable().getProperty().getName());
		    } else{
		        conditionRow.createCell(2).setCellValue(conditionVariable.getVariableType().getStandardVariable().getName());
		    }
		    conditionRow.createCell(3).setCellValue(conditionVariable.getVariableType().getStandardVariable().getScale().getName());
		    conditionRow.createCell(4).setCellValue(conditionVariable.getVariableType().getStandardVariable().getMethod().getName());
		    conditionRow.createCell(5).setCellValue(dataType.getName());
		    boolean isNumeric = Util.isNumericVariable(dataType.getId());
		    String conditionValue = conditionVariable.getValue();
		    if (isNumeric && conditionValue != null && !conditionValue.isEmpty()) {
				Double thevalue = Double.valueOf(conditionValue);
		        conditionRow.createCell(6).setCellValue(thevalue);
		    } else {
		        conditionRow.createCell(6).setCellValue(conditionVariable.getDisplayValue());
		    }
		    conditionRow.createCell(7).setCellValue(PhenotypicType.STUDY.getLabelList().get(0)); //use "STUDY" as label
		    
		    conditionRowIndex++;
		}
		return conditionRowIndex;
	}

    /*
     * Creates a row at given index, with given column headers and style
     */
	private void createSectionHeaderRow(HSSFCellStyle cellStyle, HSSFSheet descriptionSheet, 
			List<StudyTemplateConstants> columns, int index) {
		
		HSSFRow conditionHeaderRow = descriptionSheet.createRow(index);
		for (int i=0; i < columns.size(); i++){
			HSSFCell conditionHeaderCell = conditionHeaderRow.createCell(i);
			conditionHeaderCell.setCellValue(columns.get(i).getHeader());
			conditionHeaderCell.setCellStyle(cellStyle);
		}
		
		HSSFName namedCell = descriptionSheet.getWorkbook().createName();
        namedCell.setNameName(columns.get(0).getHeader()); // "CONDITION", "FACTOR", "CONSTANT", "VARIATES"
        int fixedRow = index +1;
        String reference = descriptionSheet.getSheetName() + "!$A$" + fixedRow +":$H$" + fixedRow; // area reference
        namedCell.setRefersToFormula(reference);
	}

	/*
	 * Generates study details section containing study name, title, objective, etc
	 */
	private void createStudyDetailsSection(HSSFCellStyle labelStyle,
			HSSFSheet descriptionSheet,
			org.generationcp.middleware.domain.dms.Study study, String name) {
		String title = study.getTitle();
		String objective = study.getObjective();
		String pmKey = study.getDisplayValue(TermId.PM_KEY);
		Integer startDate = study.getStartDate();
		Integer endDate = study.getEndDate();
		String type = study.getType();
		
		//add to the sheet
		createStudyDetailsRow(labelStyle, descriptionSheet, StudyTemplateConstants.STUDY, name, 0);
		createStudyDetailsRow(labelStyle, descriptionSheet, StudyTemplateConstants.TITLE, title, 1);
		createStudyDetailsRow(labelStyle, descriptionSheet, StudyTemplateConstants.PM_KEY, pmKey, 2);
		createStudyDetailsRow(labelStyle, descriptionSheet, StudyTemplateConstants.OBJECTIVE, objective, 3);
		
		String startDateString = startDate != null ? startDate.toString() : null;
		createStudyDetailsRow(labelStyle, descriptionSheet, StudyTemplateConstants.START_DATE, startDateString, 4);
		
		String endDateString = endDate != null ? endDate.toString() : null;
		createStudyDetailsRow(labelStyle, descriptionSheet, StudyTemplateConstants.END_DATE, endDateString, 5);
		
		createStudyDetailsRow(labelStyle, descriptionSheet, StudyTemplateConstants.STUDY_TYPE, type, 6);
		
		//formula reference needed file will be template for CIMMYT Fieldbook
		HSSFName namedCell = descriptionSheet.getWorkbook().createName();
        namedCell.setNameName(StudyTemplateConstants.STUDY.getHeader());
        String reference = descriptionSheet.getSheetName() + "!A" + 1 +":H" + 1; // area reference
        namedCell.setRefersToFormula(reference);  
		
        //merge cells for the study details
        for(int ctr = 0; ctr < CONDITION_LIST_HEADER_ROW_INDEX - 1; ctr++) {
            descriptionSheet.addMergedRegion(new CellRangeAddress(ctr, ctr, 1, 7));
        }
        
        //empty row
        descriptionSheet.createRow(CONDITION_LIST_HEADER_ROW_INDEX - 1);
	}

	/*
	 * Creates a row in the Study Details section
	 */
	private void createStudyDetailsRow(HSSFCellStyle labelStyle, HSSFSheet descriptionSheet, 
			StudyTemplateConstants constant, String value, int index) {
		HSSFRow row = descriptionSheet.createRow(index);
		HSSFCell typeCell = row.createCell(0);
		typeCell.setCellValue(constant.getHeader());
		typeCell.setCellStyle(labelStyle);
		row.createCell(1).setCellValue(value);
	}
	
}
