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

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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
import org.generationcp.breeding.manager.constants.ExportCrossesObservationSheetHeaders;
import org.generationcp.breeding.manager.constants.TemplateConditionHeader;
import org.generationcp.breeding.manager.constants.TemplateFactorHeader;
import org.generationcp.breeding.manager.constants.TemplateForExportListDetails;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.pojos.ImportedCondition;
import org.generationcp.breeding.manager.pojos.ImportedFactor;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCross;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class CrossingManagerExporter{
    
    private static final String LABEL_STYLE = "labelStyle";
    private static final String FACTOR_HEADING_STYLE = "factorHeadingStyle";
    private static final String VARIATE_HEADING_STYLE = "variateHeadingStyle";
    private static final String NUMERIC_STYLE = "numericStyle";
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    private static final int NUM_OF_COLUMNS = 8;
    
    private GermplasmList crossesList;
    private CrossesMade crossesMade;
    
    private List<ImportedCondition> conditionsToWriteOnFile;
    private List<ImportedFactor> factorsToWriteOnFile;
    
    public CrossingManagerExporter(GermplasmList crossesList, CrossesMade crossesMade, User listCreator, User exporter){
        this.crossesList = crossesList;
        this.crossesMade = crossesMade;
        
        conditionsToWriteOnFile = new ArrayList<ImportedCondition>();
        ImportedCondition userNameCondition = new ImportedCondition("LIST USER", "PERSON WHO MADE THE LIST", "PERSON", "DBCV" 
                ,"ASSIGNED", "C", listCreator.getName(), "LIST");
        conditionsToWriteOnFile.add(userNameCondition);
        ImportedCondition userIdCondition = new ImportedCondition("LIST USER ID", "ID OF LIST OWNER", "PERSON", "DBID" 
                ,"ASSIGNED", "N", listCreator.getUserid().toString(), "LIST");
        conditionsToWriteOnFile.add(userIdCondition);
        ImportedCondition exporterNameCondition = new ImportedCondition("LIST EXPORTER", "PERSON EXPORTING THE LIST", "PERSON", "DBCV" 
                ,"ASSIGNED", "C", exporter.getName(), "LIST");
        conditionsToWriteOnFile.add(exporterNameCondition);
        ImportedCondition exporterIdCondition = new ImportedCondition("LIST EXPORTER ID", "ID OF LIST EXPORTER", "PERSON", "DBID" 
                ,"ASSIGNED", "N", exporter.getUserid().toString(), "LIST");
        conditionsToWriteOnFile.add(exporterIdCondition);
        
        factorsToWriteOnFile = new ArrayList<ImportedFactor>();
        ImportedFactor entryIdFactor = new ImportedFactor("ENTRY", "The germplasm entry number", "GERMPLASM ENTRY", "NUMBER", "ENUMERATED", "N", "Entry ID");
        factorsToWriteOnFile.add(entryIdFactor);
        ImportedFactor gidFactor = new ImportedFactor("GID", "The GID of the germplasm", "GERMPLASM ID", "DBID", "ASSIGNED", "N", "Entry ID");
        factorsToWriteOnFile.add(gidFactor);
        ImportedFactor entryCodeFactor = new ImportedFactor("ENTRY CODE", "Germplasm entry code", "GERMPLASM ENTRY", "CODE", "ASSIGNED", "C", "Entry ID");
        factorsToWriteOnFile.add(entryCodeFactor);
        ImportedFactor designationFactor = new ImportedFactor("DESIGNATION", "The name of the germplasm", "GERMPLASM ID", "DBCV", "ASSIGNED", "C", "Entry ID");
        factorsToWriteOnFile.add(designationFactor);
        ImportedFactor crossFactor = new ImportedFactor("CROSS", "The pedigree string of the germplasm", "CROSS NAME", "NAME", "ASSIGNED", "C", "Entry ID");
        factorsToWriteOnFile.add(crossFactor);
        ImportedFactor femaleFactor = new ImportedFactor("FEMALE", "NAME OF FEMALE PARENT", "GERMPLASM ID", "DBCV", "FEMALE SELECTED", "C", "Entry ID");
        factorsToWriteOnFile.add(femaleFactor);
        ImportedFactor maleFactor = new ImportedFactor("MALE", "NAME OF MALE PARENT", "GERMPLASM ID", "DBCV", "MALE SELECTED", "C", "Entry ID");
        factorsToWriteOnFile.add(maleFactor);
        ImportedFactor femaleGIDFactor = new ImportedFactor("FEMALE GID", "GID OF FEMALE PARENT", "GERMPLASM ID", "DBID", "FEMALE SELECTED", "N", "Entry ID");
        factorsToWriteOnFile.add(femaleGIDFactor);
        ImportedFactor maleGIDFactor = new ImportedFactor("MALE GID", "GID OF MALE PARENT", "GERMPLASM ID", "DBID", "MALE SELECTED", "N", "Entry ID");
        factorsToWriteOnFile.add(maleGIDFactor);
        ImportedFactor sourceFactor = new ImportedFactor("SOURCE", "The seed source of the germplasm", "SEED SOURCE", "NAME", "Seed Source", "C", "Entry ID");
        factorsToWriteOnFile.add(sourceFactor);
    }
    
    public FileOutputStream exportCrossingManagerExcel(String filename) throws CrossingManagerExporterException {
        //create workbook
        HSSFWorkbook wb = new HSSFWorkbook();
        
        //create two worksheets - Description and Observations
        HSSFSheet descriptionSheet = wb.createSheet("Description");
        HSSFSheet observationSheet = wb.createSheet("Observation");  
        
        HashMap<String, CellStyle> sheetStyles = createStyles(wb);
                
        //write Description sheet
        int lastRow = 0;
        lastRow = writeListDetailsSection(sheetStyles, descriptionSheet, 1);
        lastRow = writeConditionSection(sheetStyles, descriptionSheet, lastRow + 1);
        lastRow = writeFactorSection(sheetStyles, descriptionSheet, lastRow + 2); //two rows before section
            
        //write Observation sheet
        writeObservationsSheet(sheetStyles, observationSheet, 1);
        
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
            FileOutputStream fileOutputStream = new FileOutputStream(filename.replace(" ", "_"));
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
    
    
    private int writeListDetailsSection(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
        int actualRow = startingRow - 1;
        int currentRow = actualRow;
        int ctr = 0;
        
        for (TemplateForExportListDetails listDetail : TemplateForExportListDetails.values()){
            String header = listDetail.getValue(); //get header from enum 
            currentRow = actualRow + ctr;
            
            HSSFRow row = descriptionSheet.createRow(currentRow);
            descriptionSheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 1, 7));
            
            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(header); 
            labelCell.setCellStyle(styles.get(LABEL_STYLE));
            
            Cell valueCell = row.createCell(1);
            setListDetailCellValue(listDetail, valueCell);
            
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
        
        currentRow++;
        for (ImportedCondition condition: this.conditionsToWriteOnFile){
            HSSFRow conditionRow = descriptionSheet.createRow(currentRow++); 
            conditionRow.createCell(0).setCellValue(condition.getCondition());
            conditionRow.createCell(1).setCellValue(condition.getDescription());
            conditionRow.createCell(2).setCellValue(condition.getProperty());
            conditionRow.createCell(3).setCellValue(condition.getScale());
            conditionRow.createCell(4).setCellValue(condition.getMethod());
            conditionRow.createCell(5).setCellValue(condition.getDataType());
            conditionRow.createCell(6).setCellValue(condition.getValue());
        }
    
        return currentRow;
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
        currentRow++;
        for (ImportedFactor factor: factorsToWriteOnFile){
            HSSFRow conditionRow = descriptionSheet.createRow(currentRow++); 
            conditionRow.createCell(0).setCellValue(factor.getFactor());
            conditionRow.createCell(1).setCellValue(factor.getDescription());
            conditionRow.createCell(2).setCellValue(factor.getProperty());
            conditionRow.createCell(3).setCellValue(factor.getScale());
            conditionRow.createCell(4).setCellValue(factor.getMethod());
            conditionRow.createCell(5).setCellValue(factor.getDataType());
            conditionRow.createCell(6).setCellValue(factor.getNestedIn());
        }
    
        return currentRow + 1;
    }
    
    private int writeObservationsSheet(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
        int actualRow = startingRow - 1;
        int currentRow = actualRow;
        
        HSSFRow conditionDetailsHeading = descriptionSheet.createRow(currentRow);
        int columnCtr = 0;
        // set Factor Headers on first row
        for (ExportCrossesObservationSheetHeaders header : ExportCrossesObservationSheetHeaders.values()){
            Cell headerCell = conditionDetailsHeading.createCell(columnCtr);
            headerCell.setCellValue(header.getValue());
            headerCell.setCellStyle(styles.get(FACTOR_HEADING_STYLE));
            columnCtr++;
        }
        
        // write the Crosses Made
        currentRow++;
        List<GermplasmListData> listDatas = null;
        int germplasmListId =  this.crossesList.getId();
        try {
        	long listDataCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmListId);
			listDatas = this.germplasmListManager.getGermplasmListDataByListId(
			        germplasmListId, 0, (int) listDataCount);
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        for (GermplasmListData cross: listDatas){
            HSSFRow conditionRow = descriptionSheet.createRow(currentRow++); 
            conditionRow.createCell(0).setCellValue(cross.getEntryId());
            conditionRow.createCell(1).setCellValue(cross.getGid());
            conditionRow.createCell(2).setCellValue(cross.getEntryCode());
            conditionRow.createCell(3).setCellValue(cross.getDesignation());
            conditionRow.createCell(4).setCellValue(cross.getGroupName());
            conditionRow.createCell(5).setCellValue(cross.getSeedSource());
            
            String splitOfGroupName[] = cross.getGroupName().split("/");
            String femaleName = splitOfGroupName[0];
            String maleName = splitOfGroupName[1];
            ImportedGermplasmCross importedCross = getImportedGermplasmCrossByNamesOfParents(femaleName, maleName);
            if(importedCross != null){
                conditionRow.createCell(6).setCellValue(importedCross.getFemaleDesignation());
                conditionRow.createCell(7).setCellValue(importedCross.getMaleDesignation());
                conditionRow.createCell(8).setCellValue(importedCross.getFemaleGId());
                conditionRow.createCell(9).setCellValue(importedCross.getMaleGId());
            }
        }
    
        return currentRow + 1;
    }
    
    private ImportedGermplasmCross getImportedGermplasmCrossByNamesOfParents(String femaleName, String maleName){
        List<ImportedGermplasmCross> crosses = this.crossesMade.getCrossingManagerUploader().getImportedGermplasmCrosses().getImportedGermplasmCrosses();
        for(ImportedGermplasmCross cross : crosses){
            if(cross.getFemaleDesignation().equals(femaleName) && cross.getMaleDesignation().equals(maleName)){
                return cross;
            }
        }
        
        return null;
    }
    
    private void setListDetailCellValue(TemplateForExportListDetails listDetail, Cell cell){
        switch (listDetail){
            case LIST_NAME : {
                cell.setCellValue(this.crossesList.getName());
                break;
            }
            case TITLE : {
                cell.setCellValue(this.crossesList.getDescription());
                break;
            }
            case LIST_TYPE : {
                cell.setCellValue(this.crossesList.getType());
                break;
            }
            case LIST_DATE : {
                cell.setCellValue(this.crossesList.getDate().toString());
                break;
            }
        }
    }

}
