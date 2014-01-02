package org.generationcp.browser.germplasmlist.util;

import java.io.FileOutputStream;
import java.util.ArrayList;
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
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class GermplasmListExporter {

    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private UserDataManager userDataManager;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    GermplasmList germplasmList = null;
    private Integer listId;
    
    private static final String LABEL_STYLE = "labelStyle";
    private static final String HEADING_STYLE = "headingStyle";
    private static final String NUMERIC_STYLE = "numericStyle";
    
    public GermplasmListExporter(Integer germplasmListId) {
        this.listId = germplasmListId;
    }
    
    public FileOutputStream exportGermplasmListExcel(String filename) throws GermplasmListExporterException {
        //create workbook
        HSSFWorkbook wb = new HSSFWorkbook();

        //create two worksheets - Description and Observations
        HSSFSheet descriptionSheet = wb.createSheet("Description");
        HSSFSheet observationSheet = wb.createSheet("Observation");        

        // write germplasmlist description details
        // retrieve GermplasmList
        
        try {
            germplasmList = this.germplasmListManager.getGermplasmListById(this.listId);
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting Germplasm List with id: " + this.listId, e);
        }
        
        if (germplasmList != null) {
            HashMap<String, CellStyle> sheetStyles = createStyles(wb);
            writeListDetailsSection(sheetStyles, descriptionSheet, 1);
            writeListConditionSection(sheetStyles, descriptionSheet, 6);
            writeListFactorSection(sheetStyles, descriptionSheet, 12);
            writeObservationSheet(sheetStyles, observationSheet);
        }
        
        //adjust column widths of description sheet to fit contents
        for(int ctr = 0; ctr < 7; ctr++) {
            descriptionSheet.autoSizeColumn(ctr);
        }
        
        //adjust column widths of observation sheet to fit contents
        for(int ctr = 0; ctr < 7; ctr++) {
            observationSheet.autoSizeColumn(ctr);
        }
        
        try {
            //write the excel file
            FileOutputStream fileOutputStream = new FileOutputStream(filename.replace(" ", "_"));
            wb.write(fileOutputStream);
            fileOutputStream.close();
            return fileOutputStream;
        } catch(Exception ex) {
            throw new GermplasmListExporterException("Error with writing to: " + filename, ex);
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
        
        // set cell style for headings in the description sheet
        CellStyle headingStyle = wb.createCellStyle();
        headingStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        headingStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        Font headingFont = wb.createFont();
        headingFont.setColor(IndexedColors.WHITE.getIndex());
        headingFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        headingStyle.setFont(headingFont);
        styles.put(HEADING_STYLE, headingStyle);
        
        //set cell style for numeric values (left alignment)
        CellStyle numericStyle = wb.createCellStyle();
        numericStyle.setAlignment(CellStyle.ALIGN_LEFT);
        styles.put(NUMERIC_STYLE, numericStyle);
        
        return styles;
    }
    
    private void writeListDetailsSection(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
        int actualRow = startingRow - 1;
        
        HSSFRow nameRow = descriptionSheet.createRow(actualRow);
        descriptionSheet.addMergedRegion(new CellRangeAddress(actualRow, actualRow, 1, 7));
        Cell nameLabel = nameRow.createCell(0);
        nameLabel.setCellValue("LIST NAME"); 
        nameLabel.setCellStyle(styles.get(LABEL_STYLE));
        nameRow.createCell(1).setCellValue(germplasmList.getName());

        HSSFRow titleRow = descriptionSheet.createRow(actualRow + 1); 
        descriptionSheet.addMergedRegion(new CellRangeAddress(actualRow + 1, actualRow + 1, 1, 7));
        Cell titleLabel = titleRow.createCell(0);
        titleLabel.setCellValue("TITLE");
        titleLabel.setCellStyle(styles.get(LABEL_STYLE));
        titleRow.createCell(1).setCellValue(germplasmList.getDescription());

        HSSFRow typeRow = descriptionSheet.createRow(actualRow + 2);
        descriptionSheet.addMergedRegion(new CellRangeAddress(actualRow + 2, actualRow + 2, 1, 7));
        Cell typeLabel = typeRow.createCell(0);
        typeLabel.setCellValue("LIST TYPE"); 
        typeLabel.setCellStyle(styles.get(LABEL_STYLE));
        typeRow.createCell(1).setCellValue(germplasmList.getType());
        
        HSSFRow dateRow = descriptionSheet.createRow(actualRow + 3);
        descriptionSheet.addMergedRegion(new CellRangeAddress(actualRow + 3, actualRow + 3, 1, 7));
        Cell dateLabel = dateRow.createCell(0);
        dateLabel.setCellValue("LIST DATE"); 
        dateLabel.setCellStyle(styles.get(LABEL_STYLE));
        Cell dateCell = dateRow.createCell(1);
        dateCell.setCellValue(germplasmList.getDate());
        dateCell.setCellStyle(styles.get(NUMERIC_STYLE));
    }
    
    private void writeListConditionSection(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) throws GermplasmListExporterException {
        int actualRow = startingRow - 1;
        
        // write user details
        HSSFRow conditionDetailsHeading = descriptionSheet.createRow(actualRow);
        Cell conditionCell = conditionDetailsHeading.createCell(0);
        conditionCell.setCellValue("CONDITION");
        conditionCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell descriptionCell = conditionDetailsHeading.createCell(1);
        descriptionCell.setCellValue("DESCRIPTION");
        descriptionCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell propertyCell = conditionDetailsHeading.createCell(2);
        propertyCell.setCellValue("PROPERTY");
        propertyCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell scaleCell = conditionDetailsHeading.createCell(3);
        scaleCell.setCellValue("SCALE");
        scaleCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell methodCell = conditionDetailsHeading.createCell(4);
        methodCell.setCellValue("METHOD");
        methodCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell dataTypeCell = conditionDetailsHeading.createCell(5);
        dataTypeCell.setCellValue("DATA TYPE");
        dataTypeCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell valueCell = conditionDetailsHeading.createCell(6);
        valueCell.setCellValue("VALUE");
        valueCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell labelCell = conditionDetailsHeading.createCell(7);
        labelCell.setCellValue("LABEL");
        labelCell.setCellStyle(styles.get(HEADING_STYLE));
        
        // retrieve user details
        User listUser = new User();
        try {
            listUser = userDataManager.getUserById(germplasmList.getUserId());
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting user information.", e);
        }
        
        HSSFRow listUserRow = descriptionSheet.createRow(actualRow + 1); 
        listUserRow.createCell(0).setCellValue("LIST USER");
        listUserRow.createCell(1).setCellValue("PERSON WHO MADE THE LIST");
        listUserRow.createCell(2).setCellValue("PERSON");
        listUserRow.createCell(3).setCellValue("DBCV");
        listUserRow.createCell(4).setCellValue("ASSIGNED");
        listUserRow.createCell(5).setCellValue("C");
        if(listUser!=null && listUser.getName()!=null)
            listUserRow.createCell(6).setCellValue(listUser.getName());
        listUserRow.createCell(7).setCellValue("LIST");
        
        HSSFRow listUserIdRow = descriptionSheet.createRow(actualRow + 2); 
        listUserIdRow.createCell(0).setCellValue("LIST USER ID");
        listUserIdRow.createCell(1).setCellValue("ID OF LIST OWNER");
        listUserIdRow.createCell(2).setCellValue("PERSON");
        listUserIdRow.createCell(3).setCellValue("DBID");
        listUserIdRow.createCell(4).setCellValue("ASSIGNED");
        listUserIdRow.createCell(5).setCellValue("N");
        Cell userIdCell = listUserIdRow.createCell(6);
        userIdCell.setCellValue(germplasmList.getUserId());
        userIdCell.setCellStyle(styles.get(NUMERIC_STYLE));
        listUserIdRow.createCell(7).setCellValue("LIST");
        
        // retrieve current workbench user and last opened project
        Integer currentWorkbenchUserId = 0;
        Integer currentLocalIbdbUserId = 0;
        User exporterUser = new User();
        try {
            currentWorkbenchUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
            Project lastProject = workbenchDataManager.getLastOpenedProject(currentWorkbenchUserId);
            currentLocalIbdbUserId = workbenchDataManager.getLocalIbdbUserId(currentWorkbenchUserId, lastProject.getProjectId());
            exporterUser = workbenchDataManager.getUserById(currentWorkbenchUserId);
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting current workbench user information.", e);
        }
        
        HSSFRow listExporterRow = descriptionSheet.createRow(actualRow + 3); 
        listExporterRow.createCell(0).setCellValue("LIST EXPORTER");
        listExporterRow.createCell(1).setCellValue("PERSON EXPORTING THE LIST");
        listExporterRow.createCell(2).setCellValue("PERSON");
        listExporterRow.createCell(3).setCellValue("DBCV");
        listExporterRow.createCell(4).setCellValue("ASSIGNED");
        listExporterRow.createCell(5).setCellValue("C");
        listExporterRow.createCell(6).setCellValue(exporterUser.getName());
        listExporterRow.createCell(7).setCellValue("LIST");
        
        HSSFRow listExporterIdRow = descriptionSheet.createRow(actualRow + 4); 
        listExporterIdRow.createCell(0).setCellValue("LIST EXPORTER ID");
        listExporterIdRow.createCell(1).setCellValue("ID OF LIST EXPORTER");
        listExporterIdRow.createCell(2).setCellValue("PERSON");
        listExporterIdRow.createCell(3).setCellValue("DBID");
        listExporterIdRow.createCell(4).setCellValue("ASSIGNED");
        listExporterIdRow.createCell(5).setCellValue("N");
        Cell localIdCell = listExporterIdRow.createCell(6);
        localIdCell.setCellValue(currentLocalIbdbUserId);
        localIdCell.setCellStyle(styles.get(NUMERIC_STYLE));
        listExporterIdRow.createCell(7).setCellValue("LIST");
    }
    
    private void writeListFactorSection(HashMap<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
        int actualRow = startingRow - 1;
        
        HSSFRow factorDetailsHeader = descriptionSheet.createRow(actualRow);
        Cell factorCell = factorDetailsHeader.createCell(0);
        factorCell.setCellValue("FACTOR");
        factorCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell descriptionCell = factorDetailsHeader.createCell(1);
        descriptionCell.setCellValue("DESCRIPTION");
        descriptionCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell propertyCell = factorDetailsHeader.createCell(2);
        propertyCell.setCellValue("PROPERTY");
        propertyCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell scaleCell = factorDetailsHeader.createCell(3);
        scaleCell.setCellValue("SCALE");
        scaleCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell methodCell = factorDetailsHeader.createCell(4);
        methodCell.setCellValue("METHOD");
        methodCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell dataTypeCell = factorDetailsHeader.createCell(5);
        dataTypeCell.setCellValue("DATA TYPE");
        dataTypeCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell spaceCell = factorDetailsHeader.createCell(6);
        spaceCell.setCellValue("");
        spaceCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell labelCell = factorDetailsHeader.createCell(7);
        labelCell.setCellValue("LABEL");
        labelCell.setCellStyle(styles.get(HEADING_STYLE));
        
        HSSFRow entryIdRow = descriptionSheet.createRow(actualRow + 1);
        entryIdRow.createCell(0).setCellValue("Entry ID");
        entryIdRow.createCell(1).setCellValue("ENTRY NUMBER");
        entryIdRow.createCell(2).setCellValue("GERMPLASM ENTRY");
        entryIdRow.createCell(3).setCellValue("NUMBER");
        entryIdRow.createCell(4).setCellValue("ASSIGNED");
        entryIdRow.createCell(5).setCellValue("N");
        entryIdRow.createCell(6).setCellValue("");
        entryIdRow.createCell(7).setCellValue("Entry ID");
        
        HSSFRow gidRow = descriptionSheet.createRow(actualRow + 2);
        gidRow.createCell(0).setCellValue("GID");
        gidRow.createCell(1).setCellValue("GERMPLASM IDENTIFIER");
        gidRow.createCell(2).setCellValue("GERMPLASM ID");
        gidRow.createCell(3).setCellValue("DBID");
        gidRow.createCell(4).setCellValue("ASSIGNED");
        gidRow.createCell(5).setCellValue("N");
        gidRow.createCell(6).setCellValue("");
        gidRow.createCell(7).setCellValue("Entry ID");
        
        HSSFRow entryCodeRow = descriptionSheet.createRow(actualRow + 3);
        entryCodeRow.createCell(0).setCellValue("Entry Code");
        entryCodeRow.createCell(1).setCellValue("ENTRY CODE");
        entryCodeRow.createCell(2).setCellValue("GERMPLASM ENTRY");
        entryCodeRow.createCell(3).setCellValue("TEXT");
        entryCodeRow.createCell(4).setCellValue("ASSIGNED");
        entryCodeRow.createCell(5).setCellValue("C");
        entryCodeRow.createCell(6).setCellValue("");
        entryCodeRow.createCell(7).setCellValue("Entry ID");
        
        HSSFRow designationRow = descriptionSheet.createRow(actualRow + 4);
        designationRow.createCell(0).setCellValue("Designation");
        designationRow.createCell(1).setCellValue("ENTRY NAME");
        designationRow.createCell(2).setCellValue("GERMPLASM ID");
        designationRow.createCell(3).setCellValue("DBCV");
        designationRow.createCell(4).setCellValue("ASSIGNED");
        designationRow.createCell(5).setCellValue("C");
        designationRow.createCell(6).setCellValue("");
        designationRow.createCell(7).setCellValue("Entry ID");
        
        HSSFRow crossRow = descriptionSheet.createRow(actualRow + 5);
        crossRow.createCell(0).setCellValue("Cross");
        crossRow.createCell(1).setCellValue("PEDIGREE");
        crossRow.createCell(2).setCellValue("CROSS HISTORY");
        crossRow.createCell(3).setCellValue("PEDIGREE STRING");
        crossRow.createCell(4).setCellValue("ASSIGNED");
        crossRow.createCell(5).setCellValue("C");
        crossRow.createCell(6).setCellValue("");
        crossRow.createCell(7).setCellValue("Entry ID");
        
        HSSFRow sourceRow = descriptionSheet.createRow(actualRow + 6);
        sourceRow.createCell(0).setCellValue("Source");
        sourceRow.createCell(1).setCellValue("SEED SOURCE");
        sourceRow.createCell(2).setCellValue("SEED SOURCE");
        sourceRow.createCell(3).setCellValue("NAME");
        sourceRow.createCell(4).setCellValue("ASSIGNED");
        sourceRow.createCell(5).setCellValue("C");
        sourceRow.createCell(6).setCellValue("");
        sourceRow.createCell(7).setCellValue("Entry ID");
        
        HSSFRow uniqueIdRow = descriptionSheet.createRow(actualRow + 7);
        uniqueIdRow.createCell(0).setCellValue("Unique ID");
        uniqueIdRow.createCell(1).setCellValue("UNIQUE ID");
        uniqueIdRow.createCell(2).setCellValue("GERMPLASM ID");
        uniqueIdRow.createCell(3).setCellValue("UNIQUE NAME");
        uniqueIdRow.createCell(4).setCellValue("ASSIGNED");
        uniqueIdRow.createCell(5).setCellValue("C");
        uniqueIdRow.createCell(6).setCellValue("");
        uniqueIdRow.createCell(7).setCellValue("Entry ID");
    }
    
    private void writeObservationSheet(HashMap<String, CellStyle> styles, HSSFSheet observationSheet) throws GermplasmListExporterException {
        HSSFRow listEntriesHeader = observationSheet.createRow(0);
        Cell entryIdCell = listEntriesHeader.createCell(0);
        entryIdCell.setCellValue("Entry ID");
        entryIdCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell gidCell = listEntriesHeader.createCell(1);
        gidCell.setCellValue("GID");
        gidCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell entryCodeCell = listEntriesHeader.createCell(2);
        entryCodeCell.setCellValue("Entry Code");
        entryCodeCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell designationCell = listEntriesHeader.createCell(3);
        designationCell.setCellValue("Designation");
        designationCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell crossCell = listEntriesHeader.createCell(4);
        crossCell.setCellValue("Cross");
        crossCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell sourceCell = listEntriesHeader.createCell(5);
        sourceCell.setCellValue("Source");
        sourceCell.setCellStyle(styles.get(HEADING_STYLE));
        Cell uniqueIdCell = listEntriesHeader.createCell(6);
        uniqueIdCell.setCellValue("Unique ID");
        uniqueIdCell.setCellStyle(styles.get(HEADING_STYLE));
        
        List<GermplasmListData> listDatas = new ArrayList<GermplasmListData>();
        try {
            long listDataCount = germplasmListManager.countGermplasmListDataByListId(listId);
            listDatas = germplasmListManager.getGermplasmListDataByListId(listId, 0, (int) listDataCount);
            List<Name> preferredIds = germplasmDataManager.getPreferredIdsByListId(listId);
            HashMap<Integer, String> preferredIdsMap = new HashMap<Integer, String>();
            for (Name name : preferredIds) {
                preferredIdsMap.put(name.getGermplasmId(), name.getNval());
            }
            
            int i = 1;
            for (GermplasmListData listData : listDatas) {
                //Name preferredId = germplasmDataManager.getPreferredIdByGID(listData.getGid());
                HSSFRow listEntry = observationSheet.createRow(i);
                listEntry.createCell(0).setCellValue(listData.getEntryId());
                listEntry.createCell(1).setCellValue(listData.getGid());
                listEntry.createCell(2).setCellValue(listData.getEntryCode());
                listEntry.createCell(3).setCellValue(listData.getDesignation());
                listEntry.createCell(4).setCellValue(listData.getGroupName());
                listEntry.createCell(5).setCellValue(listData.getSeedSource());
                //listEntry.createCell(6).setCellValue(preferredId == null ? "" : preferredId.getNval());
                listEntry.createCell(6).setCellValue(preferredIdsMap.containsKey(listData.getGid()) 
                        ? preferredIdsMap.get(listData.getGid()) : "");
                i+=1;
            }
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting germplasm list entries.", e);
        }
        
    }
    
    public FileOutputStream exportListForKBioScienceGenotypingOrder(String filename, int plateSize) throws GermplasmListExporterException{
        String wellLetters[] = {"A", "B", "C", "D", "E", "F", "G", "H"};
        HSSFWorkbook wb = new HSSFWorkbook();
        
        HSSFSheet sheet = wb.createSheet("List"); 
        
        try {
            germplasmList = this.germplasmListManager.getGermplasmListById(this.listId);
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting Germplasm List with id: " + this.listId, e);
        }
        
        if(germplasmList == null){
            throw new GermplasmListExporterException("There is no Germplasm List with id: " + this.listId);
        }
        
        String listName = germplasmList.getName();
        
        HSSFRow header = sheet.createRow(0);
        header.createCell(0).setCellValue("Subject ID");
        header.createCell(1).setCellValue("Plate ID");
        header.createCell(2).setCellValue("Well");
        header.createCell(3).setCellValue("Sample type");
        header.createCell(4).setCellValue(plateSize);
        header.createCell(5).setCellValue("Primer");
        header.createCell(6).setCellValue("Subject BC");
        header.createCell(7).setCellValue("Plate BC");
        
        try {
            long listDataCount = germplasmListManager.countGermplasmListDataByListId(listId);
            List<GermplasmListData> listDatas = germplasmListManager.getGermplasmListDataByListId(listId, 0, (int) listDataCount);
            
            String plateName = listName;
            int plateNum = 0;
            if(plateSize == 96){
                if(listDataCount > 95){
                    plateNum = 1;
                    plateName = plateName + "-" + plateNum;
                }
            }
            
            int wellLetterIndex = 0;
            int wellNumberIndex = 1; 
            int rowNum = 1;
            for(GermplasmListData listData : listDatas){
                if(wellLetterIndex == 7 && wellNumberIndex == 12){
                    //skip H12
                    wellLetterIndex = 0;
                    wellNumberIndex = 1;
                    if(plateNum != 0){
                        plateNum++;
                        plateName = listName + "-" + plateNum;
                    }
                }
                
                if(wellNumberIndex == 13){
                    wellLetterIndex++;
                    wellNumberIndex = 1;
                }
                
                String well = wellLetters[wellLetterIndex];
                if(wellNumberIndex < 10){
                    well = well + "0" + wellNumberIndex;
                } else {
                    well = well + wellNumberIndex;
                }
                
                String nullString = null;
                HSSFRow row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(listData.getEntryId());
                row.createCell(1).setCellValue(plateName);
                row.createCell(2).setCellValue(well);
                row.createCell(3).setCellValue(nullString);
                row.createCell(4).setCellValue(nullString);
                row.createCell(5).setCellValue(nullString);
                row.createCell(6).setCellValue(nullString);
                row.createCell(7).setCellValue(nullString);
                
                rowNum++;
                wellNumberIndex++;
            }
            
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting germplasm list entries for list id: " + this.listId, e);
        }
        
        for(int ctr = 0; ctr < 8; ctr++) {
            sheet.autoSizeColumn(ctr);
        }
        
        try {
            //write the excel file
            FileOutputStream fileOutputStream = new FileOutputStream(filename.replace(" ", "_"));
            wb.write(fileOutputStream);
            fileOutputStream.close();
            return fileOutputStream;
        } catch(Exception ex) {
            throw new GermplasmListExporterException("Error with writing to: " + filename, ex);
        }
    }
}
