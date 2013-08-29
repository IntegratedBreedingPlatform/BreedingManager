package org.generationcp.browser.study.util;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.generationcp.commons.util.PoiUtil;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.StudyDataManagerImpl;


public class DatasetExporter {

    private static final int CONDITION_LIST_HEADER_ROW_INDEX = 7;
    private static final String NUMERIC_VARIABLE = "Numeric variable";
    
    private StudyDataManagerImpl studyDataManager;
//    private Object traitDataManager;
    private Integer studyId;
    private Integer datasetId;
    
    public DatasetExporter(StudyDataManagerImpl studyDataManager, Object traitDataManager, Integer studyId, Integer representationId) {
        this.studyDataManager = studyDataManager;
//        this.traitDataManager = traitDataManager;
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
        Workbook workbook = new HSSFWorkbook();
        CellStyle cellStyleForObservationSheet = workbook.createCellStyle();
        
        // set cell style for labels in the description sheet
        CellStyle labelStyle = workbook.createCellStyle();
        labelStyle.setFillForegroundColor(IndexedColors.BROWN.getIndex());
        labelStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        Font labelFont = workbook.createFont();
        labelFont.setColor(IndexedColors.WHITE.getIndex());
        labelFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        labelStyle.setFont(labelFont);
        
        // set cell style for headings in the description sheet
        CellStyle headingStyle = workbook.createCellStyle();
        headingStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        headingStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        Font headingFont = workbook.createFont();
        headingFont.setColor(IndexedColors.WHITE.getIndex());
        headingFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headingStyle.setFont(headingFont);
        
        // set cell style for variate headings in the description sheet
        CellStyle variateHeadingStyle = workbook.createCellStyle();
        variateHeadingStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        variateHeadingStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        variateHeadingStyle.setFont(headingFont);
        
        //create two sheets, one for description and another for measurements
        Sheet descriptionSheet = workbook.createSheet("Description");
        Sheet observationSheet = workbook.createSheet("Observation");
        
        //this map is for mapping the columns names of the dataset to their column index in the excel sheet
        Map<String, Integer> columnsMap = new HashMap<String, Integer>(); 
        int observationSheetColumnIndex = 0;
        
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
            String title = study.getTitle();
            String objective = study.getObjective();
            Integer startDate = study.getStartDate();
            Integer endDate = study.getEndDate();
            String type = study.getType();
            
            //add to the sheet
            Row row0 = descriptionSheet.createRow(0);
            Cell studyNameCell = row0.createCell(0);
            studyNameCell.setCellValue("STUDY");
            studyNameCell.setCellStyle(labelStyle);
            row0.createCell(1).setCellValue(name);
            
            Row row1 = descriptionSheet.createRow(1);
            Cell titleCell = row1.createCell(0);
            titleCell.setCellValue("TITLE");
            titleCell.setCellStyle(labelStyle);
            row1.createCell(1).setCellValue(title);
            
            Row row2 = descriptionSheet.createRow(2);
            Cell objectiveCell = row2.createCell(0);
            objectiveCell.setCellValue("OBJECTIVE");
            objectiveCell.setCellStyle(labelStyle);
            row2.createCell(1).setCellValue(objective);
            
            Row row3 = descriptionSheet.createRow(3);
            Cell startDateCell = row3.createCell(0);
            startDateCell.setCellValue("START DATE");
            startDateCell.setCellStyle(labelStyle);
            if(startDate != null){
                row3.createCell(1).setCellValue(startDate.toString());
            } else {
                String nullString = null;
                row3.createCell(1).setCellValue(nullString);
            }
            
            Row row4 = descriptionSheet.createRow(4);
            Cell endDateCell = row4.createCell(0);
            endDateCell.setCellValue("END DATE");
            endDateCell.setCellStyle(labelStyle);
            if(endDate != null){
                row4.createCell(1).setCellValue(endDate.toString());
            } else {
                String nullString = null;
                row4.createCell(1).setCellValue(nullString);
            }
            
            Row row5 = descriptionSheet.createRow(5);
            Cell typeCell = row5.createCell(0);
            typeCell.setCellValue("STUDY TYPE");
            typeCell.setCellStyle(labelStyle);
            row5.createCell(1).setCellValue(type);
            
            //merge cells for the study details
            for(int ctr = 0; ctr < 7; ctr++) {
                descriptionSheet.addMergedRegion(new CellRangeAddress(ctr, ctr, 1, 7));
            }
            
            //empty row
            Row row6 = descriptionSheet.createRow(6);
            
            //row with headings for condition list
            Row conditionHeaderRow = descriptionSheet.createRow(CONDITION_LIST_HEADER_ROW_INDEX);
            Cell conditionHeaderCell = conditionHeaderRow.createCell(0);
            conditionHeaderCell.setCellValue("CONDITION");
            conditionHeaderCell.setCellStyle(headingStyle);
            Cell descriptionHeaderCell = conditionHeaderRow.createCell(1);
            descriptionHeaderCell.setCellValue("DESCRIPTION");
            descriptionHeaderCell.setCellStyle(headingStyle);
            Cell propertyHeaderCell = conditionHeaderRow.createCell(2);
            propertyHeaderCell.setCellValue("PROPERTY");
            propertyHeaderCell.setCellStyle(headingStyle);
            Cell scaleHeaderCell = conditionHeaderRow.createCell(3);
            scaleHeaderCell.setCellValue("SCALE");
            scaleHeaderCell.setCellStyle(headingStyle);
            Cell methodHeaderCell = conditionHeaderRow.createCell(4);
            methodHeaderCell.setCellValue("METHOD");
            methodHeaderCell.setCellStyle(headingStyle);
            Cell dataTypeHeaderCell = conditionHeaderRow.createCell(5);
            dataTypeHeaderCell.setCellValue("DATA TYPE");
            dataTypeHeaderCell.setCellStyle(headingStyle);
            Cell valueHeaderCell = conditionHeaderRow.createCell(6);
            valueHeaderCell.setCellValue("VALUE");
            valueHeaderCell.setCellStyle(headingStyle);
            
            //get the conditions and their details
            VariableList conditions = study.getConditions();
            
            int conditionRowIndex = CONDITION_LIST_HEADER_ROW_INDEX + 1;
            List<Variable> conditionVariables = conditions.getVariables();
            for(Variable conditionVariable : conditionVariables) {
                String conditionName = conditionVariable.getVariableType().getLocalName();
                if(conditionName != null) {
                    conditionName = conditionName.trim();
                }
                String conditionType = conditionVariable.getVariableType().getStandardVariable().getDataType().getName();
                
                Row conditionRow = descriptionSheet.createRow(conditionRowIndex);
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
                conditionRow.createCell(5).setCellValue(conditionType);
                if(conditionType.equals(NUMERIC_VARIABLE)) {
                    Double thevalue = Double.valueOf(conditionVariable.getValue());
                    conditionRow.createCell(6).setCellValue(thevalue);
                } else {
                    conditionRow.createCell(6).setCellValue(conditionVariable.getDisplayValue());
                }
                
                //add entry to columns mapping
                //we set the value to -1 to signify that this should not be a column in the observation sheet
                if(!conditionName.equals("STUDY")) {
                    columnsMap.put(conditionName, Integer.valueOf(-1));
                }
                
                conditionRowIndex++;
            }
            
            //empty row
            Row emptyRowBeforeFactors = descriptionSheet.createRow(conditionRowIndex);
            
            DataSet dataset = null;
            try {
                dataset = this.studyDataManager.getDataSet(this.datasetId);
            } catch (MiddlewareQueryException ex) {
                throw new DatasetExporterException("Error with getting Dataset with id: " + this.studyId, ex);
            }
            
            //row with headings for factor list
            int factorRowHeaderIndex = conditionRowIndex + 1;
            Row factorHeaderRow = descriptionSheet.createRow(factorRowHeaderIndex);
            Cell factorHeaderCell = factorHeaderRow.createCell(0);
            factorHeaderCell.setCellValue("FACTOR");
            factorHeaderCell.setCellStyle(headingStyle);
            Cell factorDescriptionHeaderCell = factorHeaderRow.createCell(1);
            factorDescriptionHeaderCell.setCellValue("DESCRIPTION");
            factorDescriptionHeaderCell.setCellStyle(headingStyle);
            Cell factorPropertyHeaderCell = factorHeaderRow.createCell(2);
            factorPropertyHeaderCell.setCellValue("PROPERTY");
            factorPropertyHeaderCell.setCellStyle(headingStyle);
            Cell factorScaleHeaderCell = factorHeaderRow.createCell(3);
            factorScaleHeaderCell.setCellValue("SCALE");
            factorScaleHeaderCell.setCellStyle(headingStyle);
            Cell factorMethodHeaderCell = factorHeaderRow.createCell(4);
            factorMethodHeaderCell.setCellValue("METHOD");
            factorMethodHeaderCell.setCellStyle(headingStyle);
            Cell factorDataTypeHeaderCell = factorHeaderRow.createCell(5);
            factorDataTypeHeaderCell.setCellValue("DATA TYPE");
            factorDataTypeHeaderCell.setCellStyle(headingStyle);
            
            //get the factors and their details
            VariableTypeList datasetVariableTypes = dataset.getVariableTypes();
            VariableTypeList factorVariableTypeList = datasetVariableTypes.getFactors();
            List<VariableType> factorVariableTypes = factorVariableTypeList.getVariableTypes();
            
            int factorRowIndex = factorRowHeaderIndex + 1;
            for(VariableType factor : factorVariableTypes) {
                String dataType = factor.getStandardVariable().getDataType().getName();
                String factorName = factor.getLocalName();
                if(factorName != null) {
                    factorName = factorName.trim();
                }
                
                //check if factor is already written as a condition
                Integer temp = columnsMap.get(factorName);
                if(temp == null && !factorName.equals("STUDY")) {
                    Row factorRow = descriptionSheet.createRow(factorRowIndex);
                    factorRow.createCell(0).setCellValue(factorName);
                    if(factor.getLocalDescription() != null && factor.getLocalDescription().length() != 0){
                        factorRow.createCell(1).setCellValue(factor.getLocalDescription());
                    } else{
                        factorRow.createCell(1).setCellValue(factor.getStandardVariable().getDescription());
                    }
                    if(factor.getStandardVariable().getProperty() != null){
                        factorRow.createCell(2).setCellValue(factor.getStandardVariable().getProperty().getName());
                    } else{
                        factorRow.createCell(2).setCellValue(factor.getStandardVariable().getName());
                    }
                    factorRow.createCell(3).setCellValue(factor.getStandardVariable().getScale().getName());
                    factorRow.createCell(4).setCellValue(factor.getStandardVariable().getMethod().getName());
                    factorRow.createCell(5).setCellValue(dataType);
                    
                    //add entry to columns mapping
                    columnsMap.put(factorName, Integer.valueOf(observationSheetColumnIndex));
                    observationSheetColumnIndex++;
                        
                    factorRowIndex++;
                }
            }
            
            //empty row
            Row emptyRowBeforeVariate = descriptionSheet.createRow(factorRowIndex);
            
            //row with headings for variate list
            int variateHeaderRowIndex = factorRowIndex + 1;
            Row variateHeaderRow = descriptionSheet.createRow(variateHeaderRowIndex);
            Cell variateHeaderCell = variateHeaderRow.createCell(0);
            variateHeaderCell.setCellValue("VARIATE");
            variateHeaderCell.setCellStyle(variateHeadingStyle);
            Cell variateDescriptionHeaderCell = variateHeaderRow.createCell(1);
            variateDescriptionHeaderCell.setCellValue("DESCRIPTION");
            variateDescriptionHeaderCell.setCellStyle(variateHeadingStyle);
            Cell variatePropertyHeaderCell = variateHeaderRow.createCell(2);
            variatePropertyHeaderCell.setCellValue("PROPERTY");
            variatePropertyHeaderCell.setCellStyle(variateHeadingStyle);
            Cell variateScaleHeaderCell = variateHeaderRow.createCell(3);
            variateScaleHeaderCell.setCellValue("SCALE");
            variateScaleHeaderCell.setCellStyle(variateHeadingStyle);
            Cell variateMethodHeaderCell = variateHeaderRow.createCell(4);
            variateMethodHeaderCell.setCellValue("METHOD");
            variateMethodHeaderCell.setCellStyle(variateHeadingStyle);
            Cell variateDataTypeHeaderCell = variateHeaderRow.createCell(5);
            variateDataTypeHeaderCell.setCellValue("DATA TYPE");
            variateDataTypeHeaderCell.setCellStyle(variateHeadingStyle);
            
            //get the variates and their details
            VariableTypeList variateVariableTypeList = datasetVariableTypes.getVariates();
            List<VariableType> variateVariableTypes = variateVariableTypeList.getVariableTypes();
            
            int variateRowIndex = variateHeaderRowIndex + 1;
            for(VariableType variate : variateVariableTypes) {
                String dataType = variate.getStandardVariable().getDataType().getName();
                String variateName = variate.getLocalName();
                if(variateName != null) {
                    variateName = variateName.trim();
                }
                
                Row variateRow = descriptionSheet.createRow(variateRowIndex);
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
                
                //add entry to columns mapping
                columnsMap.put(variateName, Integer.valueOf(observationSheetColumnIndex));
                observationSheetColumnIndex++;
                
                variateRowIndex++;
            }
            
            //populate the measurements sheet
            //establish the columns of the dataset first
            Row datasetHeaderRow = observationSheet.createRow(0);
            for(String columnName : columnsMap.keySet()) {
                short columnIndex = columnsMap.get(columnName).shortValue();
                if(columnIndex >= 0) {
                    Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, datasetHeaderRow, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                    cell.setCellValue(columnName);
                }
            }
            
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
                for(Experiment experiment : experiments) {
                    Row row = observationSheet.createRow(sheetRowIndex);
                    sheetRowIndex++;
                        
                    List<Variable> factorsOfExperiments = experiment.getFactors().getVariables();
                    for(Variable factorVariable : factorsOfExperiments){
                        String factorName = factorVariable.getVariableType().getLocalName();
                        if(factorName != null){
                            factorName = factorName.trim();
                        }
                        Integer columnIndexInteger = columnsMap.get(factorName); 
                        if(columnIndexInteger != null){
                            short columnIndex = columnIndexInteger.shortValue();
                            if(columnIndex >= 0) {
                                Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                                if(factorVariable.getVariableType().getStandardVariable().getDataType().getName().equals(NUMERIC_VARIABLE)){
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
                        Integer columnIndexInteger = columnsMap.get(variateName); 
                        if(columnIndexInteger != null){
                            short columnIndex = columnIndexInteger.shortValue();
                            if(columnIndex >= 0) {
                                Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                                if(variateVariable.getVariableType().getStandardVariable().getDataType().getName().equals(NUMERIC_VARIABLE)){
                                    double elemValue = 0;
                                    if(variateVariable.getValue() != null){
                                        try{
                                            elemValue = Double.valueOf(variateVariable.getValue());
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
            }
        }
        
        //adjust column widths of description sheet to fit contents
        for(int ctr = 0; ctr < 8; ctr++) {
            if(ctr != 1) {
                descriptionSheet.autoSizeColumn(ctr);
            }
        }
        
        //adjust column widths of observation sheet to fit contents
        for(int ctr = 0; ctr < observationSheetColumnIndex; ctr++) {
            observationSheet.autoSizeColumn(ctr);
        }
        
        try {
            //write the excel file
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            return fileOutputStream;
        } catch(Exception ex) {
            throw new DatasetExporterException("Error with writing to: " + filename, ex);
        }
    }
    
    /*
    public FileOutputStream exportToFieldBookExcel(String filename) throws DatasetExporterException {
        //create workbook
        Workbook workbook = new HSSFWorkbook();
        CellStyle cellStyleForObservationSheet = workbook.createCellStyle();
        
        //create two sheets, one for description and another for measurements
        Sheet descriptionSheet = workbook.createSheet("Description");
        Sheet observationSheet = workbook.createSheet("Observation");
        
        //this map is for mapping the columns names of the dataset to their column index in the excel sheet
        Map<String, Integer> columnsMap = new HashMap<String, Integer>(); 
        int observationSheetColumnIndex = 0;
        
        //write the details on the first sheet - description
        //get the study first
        Study study = null;
        
        try {
            study = this.studyDataManager.getStudy(this.studyId);
        } catch (MiddlewareQueryException ex) {
            throw new DatasetExporterException("Error with getting Study with id: " + this.studyId, ex);
        }
        
        if(study != null) {
            //get the needed study details
            String name = study.getName();
            String title = study.getTitle();
            Integer pmkey = 0;//study.getProjectKey();
            String objective = study.getObjective();
            Integer startDate = study.getStartDate();
            Integer endDate = study.getEndDate();
            String type = study.getType();
            
              
            //add to the sheet
            Row row0 = descriptionSheet.createRow(0);
            row0.createCell(0).setCellValue("STUDY");
            row0.createCell(1).setCellValue(name);
            
            Row row1 = descriptionSheet.createRow(1);
            row1.createCell(0).setCellValue("TITLE");
            row1.createCell(1).setCellValue(title);
            
            Row row2 = descriptionSheet.createRow(2);
            row2.createCell(0).setCellValue("PMKEY");
            if(pmkey != null){
                row2.createCell(1).setCellValue(pmkey.toString());
            } else{
                row2.createCell(1).setCellValue(pmkey);
            }
            
            Row row3 = descriptionSheet.createRow(3);
            row3.createCell(0).setCellValue("OBJECTIVE");
            row3.createCell(1).setCellValue(objective);
            
            Row row4 = descriptionSheet.createRow(4);
            row4.createCell(0).setCellValue("START DATE");
            if(startDate != null){
                row4.createCell(1).setCellValue(startDate.toString());
            } else {
                row4.createCell(1).setCellValue(startDate);
            }
            
            Row row5 = descriptionSheet.createRow(5);
            row5.createCell(0).setCellValue("END DATE");
            if(endDate != null){
                row5.createCell(1).setCellValue(endDate.toString());
            } else {
                row5.createCell(1).setCellValue(endDate);
            }
            
            Row row6 = descriptionSheet.createRow(6);
            row6.createCell(0).setCellValue("STUDY TYPE");
            row6.createCell(1).setCellValue(type);
            
            //merge cells for the study details
            for(int ctr = 0; ctr < 7; ctr++) {
                descriptionSheet.addMergedRegion(new CellRangeAddress(ctr, ctr, 1, 7));
            }
            
            //empty row
            Row row7 = descriptionSheet.createRow(7);
            
            //row with headings for condition list
            Row conditionHeaderRow = descriptionSheet.createRow(CONDITION_LIST_HEADER_ROW_INDEX);
            conditionHeaderRow.createCell(0).setCellValue("CONDITION");
            conditionHeaderRow.createCell(1).setCellValue("DESCRIPTION");
            conditionHeaderRow.createCell(2).setCellValue("PROPERTY");
            conditionHeaderRow.createCell(3).setCellValue("SCALE");
            conditionHeaderRow.createCell(4).setCellValue("METHOD");
            conditionHeaderRow.createCell(5).setCellValue("DATA TYPE");
            conditionHeaderRow.createCell(6).setCellValue("VALUE");
            conditionHeaderRow.createCell(7).setCellValue("LABEL");
            
            //get the conditions and their details
            List<DatasetCondition> conditions = new ArrayList<DatasetCondition>();
            try {
                conditions.addAll(this.studyDataManager.getConditionsByRepresentationId(this.datasetId));
             
            } catch(Exception ex) {
                throw new DatasetExporterException("Error with getting conditions of study - " + name 
                        + ", representation - " + this.datasetId, ex);
            }
            
            int conditionRowIndex = CONDITION_LIST_HEADER_ROW_INDEX + 1;
            for(DatasetCondition condition : conditions) {
                String traitScaleMethodInfo[] = getTraitScaleMethodInfo(condition.getTraitId(), condition.getScaleId(), condition.getMethodId());
                
                String conditionName = condition.getName();
                if(conditionName != null) {
                    conditionName = conditionName.trim();
                }
                String conditionType = condition.getType();
                
                String conditionLabel = "";
                try {
                    conditionLabel = "conditionLabel";//this.studyDataManager.getMainLabelOfFactorByFactorId(condition.getFactorId());
                } catch (MiddlewareQueryException ex) {
                    conditionLabel = "";
                }
                
                Row conditionRow = descriptionSheet.createRow(conditionRowIndex);
                conditionRow.createCell(0).setCellValue(conditionName);
                conditionRow.createCell(1).setCellValue(traitScaleMethodInfo[0]);
                conditionRow.createCell(2).setCellValue(traitScaleMethodInfo[1]);
                conditionRow.createCell(3).setCellValue(traitScaleMethodInfo[2]);
                conditionRow.createCell(4).setCellValue(traitScaleMethodInfo[3]);
                conditionRow.createCell(5).setCellValue(conditionType);
                if(conditionType.equals("N")) {
                    Double thevalue = (Double) condition.getValue();
                    conditionRow.createCell(6).setCellValue(thevalue);
                } else {
                    conditionRow.createCell(6).setCellValue(condition.getValue().toString());
                }
                conditionRow.createCell(7).setCellValue(conditionLabel);
                
                //add entry to columns mapping
                //we set the value to -1 to signify that this should not be a column in the observation sheet
                if(!conditionName.equals("STUDY")) {
                    columnsMap.put(conditionName, Integer.valueOf(-1));
                }
                
                conditionRowIndex++;
            }
            
            //empty row
            Row emptyRowBeforeFactors = descriptionSheet.createRow(conditionRowIndex);
            
            //row with headings for factor list
            int factorRowHeaderIndex = conditionRowIndex + 1;
            Row factorHeaderRow = descriptionSheet.createRow(factorRowHeaderIndex);
            factorHeaderRow.createCell(0).setCellValue("FACTOR");
            factorHeaderRow.createCell(1).setCellValue("DESCRIPTION");
            factorHeaderRow.createCell(2).setCellValue("PROPERTY");
            factorHeaderRow.createCell(3).setCellValue("SCALE");
            factorHeaderRow.createCell(4).setCellValue("METHOD");
            factorHeaderRow.createCell(5).setCellValue("DATA TYPE");
            factorHeaderRow.createCell(6).setCellValue("");
            factorHeaderRow.createCell(7).setCellValue("LABEL");
            
            //get the factors and their details
            List<Factor> factors = new ArrayList<Factor>();
            try {
                factors.addAll(this.studyDataManager.getFactorsByRepresentationId(this.datasetId));
            } catch(Exception ex) {
                throw new DatasetExporterException("Error with getting factors of study - " + name 
                        + ", representation - " + this.datasetId, ex);
            }
            
            int factorRowIndex = factorRowHeaderIndex + 1;
            for(Factor factor : factors) {
                String dataType = factor.getDataType();
                String factorName = factor.getName();
                if(factorName != null) {
                    factorName = factorName.trim();
                }
                
                //check if factor is already written as a condition
                Integer temp = columnsMap.get(factorName);
                if(temp == null && !factorName.equals("STUDY")) {
                    String traitScaleMethodInfo[] = getTraitScaleMethodInfo(factor.getTraitId(), factor.getScaleId(), factor.getMethodId());
                    
                    String factorLabel = "";
                    try {
                        factorLabel = this.studyDataManager.getMainLabelOfFactorByFactorId(factor.getFactorId());
                    } catch (MiddlewareQueryException ex) {
                        factorLabel = "";
                    }
                    
                    Row factorRow = descriptionSheet.createRow(factorRowIndex);
                    factorRow.createCell(0).setCellValue(factorName);
                    factorRow.createCell(1).setCellValue(traitScaleMethodInfo[0]);
                    factorRow.createCell(2).setCellValue(traitScaleMethodInfo[1]);
                    factorRow.createCell(3).setCellValue(traitScaleMethodInfo[2]);
                    factorRow.createCell(4).setCellValue(traitScaleMethodInfo[3]);
                    factorRow.createCell(5).setCellValue(dataType);
                    factorRow.createCell(6).setCellValue("");
                    factorRow.createCell(7).setCellValue(factorLabel);
                    
                    //add entry to columns mapping
                    columnsMap.put(factorName, Integer.valueOf(observationSheetColumnIndex));
                    observationSheetColumnIndex++;
                        
                    factorRowIndex++;
                }
            }
            
            //empty row
            Row emptyRowBeforeVariate = descriptionSheet.createRow(factorRowIndex);
            
            //row with headings for variate list
            int variateHeaderRowIndex = factorRowIndex + 1;
            Row variateHeaderRow = descriptionSheet.createRow(variateHeaderRowIndex);
            variateHeaderRow.createCell(0).setCellValue("VARIATE");
            variateHeaderRow.createCell(1).setCellValue("DESCRIPTION");
            variateHeaderRow.createCell(2).setCellValue("PROPERTY");
            variateHeaderRow.createCell(3).setCellValue("SCALE");
            variateHeaderRow.createCell(4).setCellValue("METHOD");
            variateHeaderRow.createCell(5).setCellValue("DATA TYPE");
            
            //get the variates and their details
            List<Variate> variates = new ArrayList<Variate>();
            try {
                variates.addAll(this.studyDataManager.getVariatesByRepresentationId(this.datasetId));
            }
            catch(Exception ex) {
                throw new DatasetExporterException("Error with getting variates of study - " + name 
                        + ", representation - " + this.datasetId, ex);
            }
            
            int variateRowIndex = variateHeaderRowIndex + 1;
            for(Variate variate : variates) {
                String dataType = variate.getDataType();
                String variateName = variate.getName();
                if(variateName != null) {
                    variateName = variateName.trim();
                }
                
                String traitScaleMethodInfo[] = getTraitScaleMethodInfo(variate.getTraitId(), variate.getScaleId(), variate.getMethodId());
                
                Row variateRow = descriptionSheet.createRow(variateRowIndex);
                variateRow.createCell(0).setCellValue(variateName);
                variateRow.createCell(1).setCellValue(traitScaleMethodInfo[0]);
                variateRow.createCell(2).setCellValue(traitScaleMethodInfo[1]);
                variateRow.createCell(3).setCellValue(traitScaleMethodInfo[2]);
                variateRow.createCell(4).setCellValue(traitScaleMethodInfo[3]);
                variateRow.createCell(5).setCellValue(dataType);
                
                //add entry to columns mapping
                columnsMap.put(variateName, Integer.valueOf(observationSheetColumnIndex));
                observationSheetColumnIndex++;
                
                variateRowIndex++;
            }
            
            //populate the measurements sheet
            //establish the columns of the dataset first
            Row datasetHeaderRow = observationSheet.createRow(0);
            for(String columnName : columnsMap.keySet()) {
                short columnIndex = columnsMap.get(columnName).shortValue();
                if(columnIndex >= 0) {
                    Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, datasetHeaderRow, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                    cell.setCellValue(columnName);
                }
            }
            
            //then work with the data
            //do it by 50 rows at a time
            int pageSize = 50;
            long totalNumberOfRows = 0;
            int sheetRowIndex = 1;
            
            try {
                totalNumberOfRows = this.studyDataManager.countOunitIDsByRepresentationId(this.datasetId);
            } catch(Exception ex) {
                throw new DatasetExporterException("Error with getting count of ounit ids for study - " + name 
                        + ", representation - " + this.datasetId, ex); 
            }
            
            for(int start = 0; start < totalNumberOfRows; start = start + pageSize) {
                List<Integer> ounitIds = new ArrayList<Integer>();
                try {
                    //first get the ounit ids, these are the ids of the rows in the dataset
                    ounitIds.addAll(this.studyDataManager.getOunitIDsByRepresentationId(this.datasetId, start, pageSize));
                } catch(Exception ex) {
                    throw new DatasetExporterException("Error with getting ounit ids of study - " + name 
                            + ", representation - " + this.datasetId, ex); 
                }
                
                if(!ounitIds.isEmpty()) {
                    //map each ounit id into a row in the observation sheet
                    Map<Integer, Row> rowMap = new HashMap<Integer, Row>();
                    for(Integer ounitId : ounitIds) {
                        Row row = observationSheet.createRow(sheetRowIndex);
                        sheetRowIndex++;
                        rowMap.put(ounitId, row);
                    }
                    
                    //then get the data for each of the observation units (ounits)
                    List<CharacterLevelElement> charLevels = new ArrayList<CharacterLevelElement>();
                    try {
                        charLevels.addAll(this.studyDataManager.getCharacterLevelValuesByOunitIdList(ounitIds));
                    } catch(Exception ex) {
                        throw new DatasetExporterException("Error with getting character level values of study - " + name 
                                + ", representation - " + this.datasetId, ex);
                    }
                    
                    for(CharacterLevelElement elem : charLevels) {
                        String factorName = elem.getFactorName();
                        if(factorName != null) {
                            factorName = factorName.trim();
                        }
                        if(!factorName.equals("STUDY")) {
                            Row row = rowMap.get(elem.getOunitId());
                            if(row != null) {
                                short columnIndex = columnsMap.get(factorName).shortValue();
                                if(columnIndex >= 0) {
                                    Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                                    String value = elem.getValue();
                                    if(value != null) {
                                        value = value.trim();
                                    }
                                    cell.setCellValue(value);
                                }
                            }
                        }
                    }
                    
                    List<NumericLevelElement> numericLevels = new ArrayList<NumericLevelElement>();
                    try {
                        numericLevels.addAll(this.studyDataManager.getNumericLevelValuesByOunitIdList(ounitIds));
                    } catch(Exception ex) {
                        throw new DatasetExporterException("Error with getting numeric level values of study - " + name 
                                + ", representation - " + this.datasetId, ex);
                    }
                    
                    for(NumericLevelElement elem : numericLevels) {
                        String factorName = elem.getFactorName();
                        if(factorName != null) {
                            factorName = factorName.trim();
                        }
                        if(!factorName.equals("STUDY")) {
                            Row row = rowMap.get(elem.getOunitId());
                            if(row != null) {
                                short columnIndex = columnsMap.get(factorName).shortValue();
                                if(columnIndex >= 0) {
                                    Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                                    double elemValue = 0;
                                    if(elem.getValue() != null){
                                        elemValue = elem.getValue().doubleValue();
                                        cell.setCellValue(elemValue);
                                    } else {
                                        String nullValue = null;
                                        cell.setCellValue(nullValue);
                                    }
                                }
                            }
                        }
                    }
                    
                    List<CharacterDataElement> charDatas = new ArrayList<CharacterDataElement>();
                    try {
                        charDatas.addAll(this.studyDataManager.getCharacterDataValuesByOunitIdList(ounitIds));
                    } catch(Exception ex) {
                        throw new DatasetExporterException("Error with getting character data values of study - " + name 
                                + ", representation - " + this.datasetId, ex);
                    }
                    
                    for(CharacterDataElement elem : charDatas) {
                        Row row = rowMap.get(elem.getOunitId());
                        if(row != null) {
                            String variateName = elem.getVariateName();
                            if(variateName != null) {
                                variateName = variateName.trim();
                            }
                            short columnIndex = columnsMap.get(variateName).shortValue();
                            Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                            String value = elem.getValue();
                            if(value != null) {
                                value = value.trim();
                            }
                            cell.setCellValue(value);
                        }
                    }
                    
                    List<NumericDataElement> numericDatas = new ArrayList<NumericDataElement>();
                    try {
                        numericDatas.addAll(this.studyDataManager.getNumericDataValuesByOunitIdList(ounitIds));
                    } catch(Exception ex) {
                        throw new DatasetExporterException("Error with getting numeric data values of study - " + name 
                                + ", representation - " + this.datasetId, ex);
                    }
                    
                    for(NumericDataElement elem : numericDatas) {
                        Row row = rowMap.get(elem.getOunitId());
                        if(row != null) {
                            String variateName = elem.getVariateName();
                            if(variateName != null) {
                                variateName = variateName.trim();
                            }
                            short columnIndex = columnsMap.get(variateName).shortValue();
                            Cell cell = PoiUtil.createCell(cellStyleForObservationSheet, row, columnIndex, CellStyle.ALIGN_CENTER, CellStyle.ALIGN_CENTER);
                            double elemValue = 0;
                            if(elem.getValue() != null){
                                elemValue = elem.getValue().doubleValue();
                                
                                if(elemValue <= -1.0e36){
                                    //this means the values is lost so set it to null
                                    String nullValue = null;
                                    cell.setCellValue(nullValue);
                                } else {
                                    cell.setCellValue(elemValue);
                                }
                            } else {
                                String nullValue = null;
                                cell.setCellValue(nullValue);
                            }
                        }
                    }
                }
            }
            
        }
        
        //adjust column widths of description sheet to fit contents
        for(int ctr = 0; ctr < 8; ctr++) {
            if(ctr != 1) {
                descriptionSheet.autoSizeColumn(ctr);
            }
        }
        
        //adjust column widths of observation sheet to fit contents
        for(int ctr = 0; ctr < observationSheetColumnIndex; ctr++) {
            observationSheet.autoSizeColumn(ctr);
        }
        
        try {
            //write the excel file
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            return fileOutputStream;
        } catch(Exception ex) {
            throw new DatasetExporterException("Error with writing to: " + filename, ex);
        }
    }
    
    private String[] getTraitScaleMethodInfo(Integer traitId, Integer scaleId, Integer methodId) throws DatasetExporterException {
        String toreturn[] = new String[4];
        
        try {
            Trait trait = this.traitDataManager.getTraitById(traitId);
            Scale scale = this.traitDataManager.getScaleByID(scaleId);
            TraitMethod method = this.traitDataManager.getTraitMethodById(methodId);
            
            if(trait != null){
                toreturn[0] = trait.getDescripton();
                toreturn[1] = trait.getName();
            } else {
                toreturn[0] = "Not specified";
                toreturn[1] = "Not specified";
            }
            
            if(scale != null){
                toreturn[2] = scale.getName();
            } else {
                toreturn[2] = "Not specified";
            }
            
            if(method != null){
                toreturn[3] = method.getName();
            } else {
                toreturn[3] = "Not specified";
            }
        }
        catch(Exception ex) {
            throw new DatasetExporterException("Error with getting trait, scale, and method information for " +
                    "trait id = " + traitId +
                    " scale id = " + scaleId + 
                    " method id = " + methodId, ex);
        }
        
        return toreturn;
    }
    */
   
}
