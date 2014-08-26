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
package org.generationcp.breeding.manager.nurserytemplate.util;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.generationcp.breeding.manager.constants.TemplateConditionHeader;
import org.generationcp.breeding.manager.constants.TemplateConstantHeader;
import org.generationcp.breeding.manager.constants.TemplateCrossingFactor;
import org.generationcp.breeding.manager.constants.TemplateFactorHeader;
import org.generationcp.breeding.manager.constants.TemplateStudyDetails;
import org.generationcp.breeding.manager.constants.TemplateVariateHeader;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerExporterException;
import org.generationcp.breeding.manager.pojos.ImportedCondition;
import org.generationcp.breeding.manager.pojos.ImportedConstant;
import org.generationcp.breeding.manager.pojos.ImportedFactor;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCrosses;
import org.generationcp.breeding.manager.pojos.ImportedVariate;
import org.generationcp.breeding.manager.util.Util;


public class NurseryTemplateManagerExporter{
    
    private static final String LABEL_STYLE = "labelStyle";
    private static final String FACTOR_HEADING_STYLE = "factorHeadingStyle";
    private static final String VARIATE_HEADING_STYLE = "variateHeadingStyle";
    private static final String NUMERIC_STYLE = "numericStyle";
    
    private static final int NUM_OF_COLUMNS = 8;
    
    private ImportedGermplasmCrosses nurseryTemplateData;
    private HashMap<String, String> conditionValue;
    
    public NurseryTemplateManagerExporter(ImportedGermplasmCrosses nurseryTemplateData, HashMap<String, String> conditionValue){
    this.nurseryTemplateData = nurseryTemplateData;
    this.conditionValue=conditionValue;
    }
    
    public FileOutputStream exportNurseryTemplateManagerExcel(String filename) throws CrossingManagerExporterException {
        //create workbook
        HSSFWorkbook wb = new HSSFWorkbook();
        
        //create two worksheets - Description and Observations
        HSSFSheet descriptionSheet = wb.createSheet("Description");
        HSSFSheet observationSheet = wb.createSheet("Observation");  
        
//        Map<Germplasm, Name> crossesMap = crossesMade.getCrossesMap();
//        if (crossesMap != null && !crossesMap.isEmpty()){
            HashMap<String, CellStyle> sheetStyles = createStyles(wb);
                
            //write Description sheet
            int lastRow = 0;
            lastRow = writeStudyDetailsSection(sheetStyles, descriptionSheet, 1);
            lastRow = writeConditionSection(sheetStyles, descriptionSheet, lastRow + 1);
            lastRow = writeFactorSection(sheetStyles, descriptionSheet, lastRow + 2); //two rows before section
            lastRow = writeConstantsSection(sheetStyles, descriptionSheet, lastRow + 1);
            lastRow = writeVariateSection(sheetStyles, descriptionSheet, lastRow + 1);
            
            //write Observation sheet
            writeObservationsSheet(sheetStyles, observationSheet, 1);
//        }
    
        //adjust column widths of description sheet to fit contents
        for(int ctr = 0; ctr < NUM_OF_COLUMNS; ctr++) {
            descriptionSheet.autoSizeColumn(ctr);
        }
        
        //adjust column widths of observation sheet to fit contents
        for(int ctr = 0; ctr < NUM_OF_COLUMNS; ctr++) {
            observationSheet.autoSizeColumn(ctr);
        }

        
        try {
            //write the excel file
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            wb.write(fileOutputStream);
            fileOutputStream.close();
            
            return fileOutputStream;
            
        } catch(Exception ex) {
            throw new CrossingManagerExporterException("Error writing file to: " + filename, ex);
        }
    }
    
    private HashMap<String, CellStyle> createStyles(HSSFWorkbook wb) {
        HashMap<String, CellStyle> styles = new HashMap<String, CellStyle>();
        
        // set cell style for labels in the description sheet
        CellStyle labelStyle = wb.createCellStyle();
        labelStyle.setFillForegroundColor(IndexedColors.BROWN.getIndex());
        labelStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        Font labelFont = wb.createFont();
        labelFont.setColor(IndexedColors.WHITE.getIndex());
        labelFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        labelStyle.setFont(labelFont);
        styles.put(LABEL_STYLE, labelStyle);
        
        // set cell style for headings related to Conditions/Factors
        CellStyle factorHeadingStyle = wb.createCellStyle();
        factorHeadingStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        factorHeadingStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        factorHeadingStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Font factorHeadingfont = wb.createFont();
        factorHeadingfont.setColor(IndexedColors.WHITE.getIndex());
        factorHeadingfont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        factorHeadingStyle.setFont(factorHeadingfont);
        styles.put(FACTOR_HEADING_STYLE, factorHeadingStyle);
        
        // set cell style for headings related to Constants/Variates
        CellStyle variateHeadingStyle = wb.createCellStyle();
        variateHeadingStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        variateHeadingStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        variateHeadingStyle.setAlignment(CellStyle.ALIGN_CENTER);
        Font variateHeadingFont = wb.createFont();
        variateHeadingFont.setColor(IndexedColors.WHITE.getIndex());
        variateHeadingFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        variateHeadingStyle.setFont(variateHeadingFont);
        styles.put(VARIATE_HEADING_STYLE, variateHeadingStyle);
        
        //set cell style for numeric values (left alignment)
        CellStyle numericStyle = wb.createCellStyle();
        numericStyle.setAlignment(CellStyle.ALIGN_LEFT);
        styles.put(NUMERIC_STYLE, numericStyle);
        
        return styles;
    }
    
    
    private int writeStudyDetailsSection(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
    int actualRow = startingRow - 1;
    int currentRow = actualRow;
    int ctr = 0;

    for (TemplateStudyDetails studyDetail : TemplateStudyDetails.values()){
        String header = studyDetail.getValue(); //get header from enum 
        currentRow = actualRow + ctr;

        HSSFRow row = descriptionSheet.createRow(currentRow);
        descriptionSheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 1, 7));

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(header); 
        labelCell.setCellStyle(styles.get(LABEL_STYLE));

        Cell valueCell = row.createCell(1);
        setStudyDetailCellValue(nurseryTemplateData, studyDetail, valueCell);

        ctr++;
    }

    //return the next row to write to. +2 because it's decremented at start of method
    return currentRow + 2;
    }
    
    
    private int writeConditionSection(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
        int actualRow = startingRow - 1;
        int currentRow = actualRow;
        
        // set section Headers
        HSSFRow conditionDetailsHeading = descriptionSheet.createRow(currentRow);
        int columnCtr = 0;
        for (TemplateConditionHeader header : TemplateConditionHeader.values()){
            Cell headerCell = conditionDetailsHeading.createCell(columnCtr);
            headerCell.setCellValue(header.getHeader());
            headerCell.setCellStyle(styles.get(FACTOR_HEADING_STYLE));
            columnCtr++;
        }
        
        // write the imported conditions
        List<ImportedCondition> importedConditions =nurseryTemplateData.getImportedConditions();
        currentRow++;
        for (ImportedCondition condition: importedConditions){
            HSSFRow conditionRow = descriptionSheet.createRow(currentRow++); 
            conditionRow.createCell(0).setCellValue(condition.getCondition());
            conditionRow.createCell(1).setCellValue(condition.getDescription());
            conditionRow.createCell(2).setCellValue(condition.getProperty());
            conditionRow.createCell(3).setCellValue(condition.getScale());
            conditionRow.createCell(4).setCellValue(condition.getMethod());
            conditionRow.createCell(5).setCellValue(condition.getDataType());
            conditionRow.createCell(6).setCellValue(conditionValue.get(condition.getCondition().toUpperCase()));
            conditionRow.createCell(7).setCellValue(condition.getLabel());
        }
    
        return currentRow + 1;
    }
    
    
    private int writeFactorSection(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
        int actualRow = startingRow - 1;
        int currentRow = actualRow;
        
        // set section Headers
        HSSFRow factorDetailsHeading = descriptionSheet.createRow(currentRow);
        int columnCtr = 0;
        for (TemplateFactorHeader header : TemplateFactorHeader.values()){
            Cell headerCell = factorDetailsHeading.createCell(columnCtr);
            headerCell.setCellValue(header.getHeader());
            headerCell.setCellStyle(styles.get(FACTOR_HEADING_STYLE));
            columnCtr++;
        }
        
        // write the imported factors
        List<ImportedFactor> importedFactors =nurseryTemplateData.getImportedFactors();
        currentRow++;
        for (ImportedFactor factor: importedFactors){
            HSSFRow conditionRow = descriptionSheet.createRow(currentRow++); 
            conditionRow.createCell(0).setCellValue(factor.getFactor());
            conditionRow.createCell(1).setCellValue(factor.getDescription());
            conditionRow.createCell(2).setCellValue(factor.getProperty());
            conditionRow.createCell(3).setCellValue(factor.getScale());
            conditionRow.createCell(4).setCellValue(factor.getMethod());
            conditionRow.createCell(5).setCellValue(factor.getDataType());
            conditionRow.createCell(6).setCellValue(factor.getNestedIn());
            conditionRow.createCell(7).setCellValue(factor.getLabel());
        }
    
        return currentRow + 1;
    }
    
    
    private int writeVariateSection(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
        int actualRow = startingRow - 1;
        int currentRow = actualRow;
        
        // set section Headers
        HSSFRow conditionDetailsHeading = descriptionSheet.createRow(currentRow);
        int columnCtr = 0;
        for (TemplateVariateHeader header : TemplateVariateHeader.values()){
            Cell headerCell = conditionDetailsHeading.createCell(columnCtr);
            headerCell.setCellValue(header.getHeader());
            headerCell.setCellStyle(styles.get(VARIATE_HEADING_STYLE));
            columnCtr++;
        }
        
        // write the imported variates
        List<ImportedVariate> importedVariates =nurseryTemplateData.getImportedVariates();
        currentRow++;
        for (ImportedVariate variate: importedVariates){
            HSSFRow conditionRow = descriptionSheet.createRow(currentRow++); 
            conditionRow.createCell(0).setCellValue(variate.getVariate());
            conditionRow.createCell(1).setCellValue(variate.getDescription());
            conditionRow.createCell(2).setCellValue(variate.getProperty());
            conditionRow.createCell(3).setCellValue(variate.getScale());
            conditionRow.createCell(4).setCellValue(variate.getMethod());
            conditionRow.createCell(5).setCellValue(variate.getDataType());
//            conditionRow.createCell(6).setCellValue(variate.getValue()); // empty column in template
            conditionRow.createCell(7).setCellValue(variate.getSampleLevel());
        }
    
        return currentRow + 1;
    }
    
    private int writeConstantsSection(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
        int actualRow = startingRow - 1;
        int currentRow = actualRow;
        
        // set section Headers
        HSSFRow conditionDetailsHeading = descriptionSheet.createRow(currentRow);
        int columnCtr = 0;
        for (TemplateConstantHeader header : TemplateConstantHeader.values()){
            Cell headerCell = conditionDetailsHeading.createCell(columnCtr);
            headerCell.setCellValue(header.getHeader());
            headerCell.setCellStyle(styles.get(VARIATE_HEADING_STYLE));
            columnCtr++;
        }
        
        // write the imported constants
        List<ImportedConstant> importedConstants =nurseryTemplateData.getImportedConstants();
        currentRow++;
        for (ImportedConstant constant: importedConstants){
            HSSFRow conditionRow = descriptionSheet.createRow(currentRow++); 
            conditionRow.createCell(0).setCellValue(constant.getConstant());
            conditionRow.createCell(1).setCellValue(constant.getDescription());
            conditionRow.createCell(2).setCellValue(constant.getProperty());
            conditionRow.createCell(3).setCellValue(constant.getScale());
            conditionRow.createCell(4).setCellValue(constant.getMethod());
            conditionRow.createCell(5).setCellValue(constant.getDataType());
            conditionRow.createCell(6).setCellValue(constant.getValue());
            conditionRow.createCell(7).setCellValue(constant.getSampleLevel());
        }
    
        return currentRow + 1;
    }
    
    private int writeObservationsSheet(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
        int actualRow = startingRow - 1;
        int currentRow = actualRow;
        
        HSSFRow conditionDetailsHeading = descriptionSheet.createRow(currentRow);
        int columnCtr = 0;
        // set Factor Headers on first row
        for (TemplateCrossingFactor header : TemplateCrossingFactor.values()){
            Cell headerCell = conditionDetailsHeading.createCell(columnCtr);
            headerCell.setCellValue(header.getValue());
            headerCell.setCellStyle(styles.get(FACTOR_HEADING_STYLE));
            columnCtr++;
        }
        //write Variates as headers on first row
        List<ImportedVariate> importedVariates =nurseryTemplateData.getImportedVariates();
        for (ImportedVariate variate : importedVariates){
            Cell headerCell = conditionDetailsHeading.createCell(columnCtr);
            headerCell.setCellValue(variate.getVariate());
            headerCell.setCellStyle(styles.get(VARIATE_HEADING_STYLE));
            columnCtr++;
        }
        
        
    
        return currentRow + 1;
    }
    
    
    private void setStudyDetailCellValue(ImportedGermplasmCrosses nurseryTemplateData, TemplateStudyDetails studyDetail, Cell cell){
    
        switch (studyDetail){
            case STUDY : {
                cell.setCellValue(nurseryTemplateData.getStudy());
                break;
            }
            case TITLE : {
                cell.setCellValue(nurseryTemplateData.getTitle());
                break;
            }
            case PMKEY : {
                cell.setCellValue(nurseryTemplateData.getPMKey());
                break;
            }
            case OBJECTIVE : {
                cell.setCellValue(nurseryTemplateData.getObjective());
                break;
            }
            case START_DATE : {
                SimpleDateFormat formatter = new SimpleDateFormat(Util.DATE_AS_NUMBER_FORMAT);
                cell.setCellValue(formatter.format(nurseryTemplateData.getStartDate()));
                break;
            }
            case END_DATE : {
                SimpleDateFormat formatter = new SimpleDateFormat(Util.DATE_AS_NUMBER_FORMAT);
                cell.setCellValue(formatter.format(nurseryTemplateData.getEndDate()));
                break;
            }
            case STUDY_TYPE : {
                cell.setCellValue(nurseryTemplateData.getType());
                break;
            }
        }
        
    }

}
