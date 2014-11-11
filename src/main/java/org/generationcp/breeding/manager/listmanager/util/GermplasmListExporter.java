package org.generationcp.breeding.manager.listmanager.util;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Table;

@Configurable
public class GermplasmListExporter {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListExporter.class);
	
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private UserDataManager userDataManager;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    private GermplasmList germplasmList = null;
    private Integer listId;
    
    private static final String LABEL_STYLE = "labelStyle";
    private static final String HEADING_STYLE = "headingStyle";
    private static final String NUMERIC_STYLE = "numericStyle";
    
    public GermplasmListExporter(Integer germplasmListId) {
        this.listId = germplasmListId;
    }
    
    public FileOutputStream exportGermplasmListExcel(String filename, Table listDataTable) throws GermplasmListExporterException {
    	
    	Map<String,Boolean> visibleColumnMap =  getVisibleColumnMap(listDataTable);
    	
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
            Map<String, CellStyle> sheetStyles = createStyles(wb);
            writeListDetailsSection(sheetStyles, descriptionSheet, 1);
            writeListConditionSection(sheetStyles, descriptionSheet, 6);
            writeListFactorSection(sheetStyles, descriptionSheet, 12, visibleColumnMap);
            writeObservationSheet(sheetStyles, observationSheet, visibleColumnMap);
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
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            wb.write(fileOutputStream);
            fileOutputStream.close();
            return fileOutputStream;
        } catch(Exception ex) {
            throw new GermplasmListExporterException("Error with writing to: " + filename, ex);
        }
    }
    
    private Map<String,Boolean> getVisibleColumnMap(Table listDataTable) {
    	
    	Map<String,Boolean> columnHeaderMap = new HashMap<String,Boolean>();
    	
    	Collection<?> columnHeaders = listDataTable.getContainerPropertyIds();
    	Object[] visibleColumns = listDataTable.getVisibleColumns();
    	
    	// change the visibleColumns array to list
    	List<String> visibleColumnList = new ArrayList<String>();
		for(Object column : visibleColumns){
			if(!listDataTable.isColumnCollapsed(column)){
				visibleColumnList.add(column.toString());
			}
		}
		
		for(Object column : columnHeaders){
			String columnHeader = column.toString();
			// always set to true for required columns
			if(ListDataTablePropertyID.ENTRY_ID.getName().equalsIgnoreCase(columnHeader)
					|| ListDataTablePropertyID.GID.getName().equalsIgnoreCase(columnHeader)
					|| ListDataTablePropertyID.DESIGNATION.getName().equalsIgnoreCase(columnHeader)){
				columnHeaderMap.put(columnHeader, true);
			} else {
				columnHeaderMap.put(columnHeader, visibleColumnList.contains(columnHeader));
			}
		}
		return columnHeaderMap;
	}

	private Map<String, CellStyle> createStyles(HSSFWorkbook wb) {
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        
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
    
    private void writeListDetailsSection(Map<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) {
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
        titleLabel.setCellValue("LIST DESCRIPTION");
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
    
    private void writeListConditionSection(Map<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow) throws GermplasmListExporterException {
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
        
        // retrieve user details
        String ownerName = "";
        try {
            User ownerUser = userDataManager.getUserById(germplasmList.getUserId());
            Person ownerPerson = userDataManager.getPersonById(ownerUser.getPersonid());
            if(ownerPerson != null){
            	ownerName = ownerPerson.getFirstName() + " " + ownerPerson.getLastName();
            } else{
            	ownerName = ownerUser.getName();
            }
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting user information.", e);
        } catch (NullPointerException ex){
        	LOG.error("Error with getting user information for list owner with id = " + germplasmList.getUserId(), ex);
        }
        
        HSSFRow listUserRow = descriptionSheet.createRow(actualRow + 1); 
        listUserRow.createCell(0).setCellValue("LIST USER");
        listUserRow.createCell(1).setCellValue("PERSON WHO MADE THE LIST");
        listUserRow.createCell(2).setCellValue("PERSON");
        listUserRow.createCell(3).setCellValue("DBCV");
        listUserRow.createCell(4).setCellValue("ASSIGNED");
        listUserRow.createCell(5).setCellValue("C");
        listUserRow.createCell(6).setCellValue(ownerName.trim());
        
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
        
        // retrieve current workbench user and last opened project
        
        String exporterName = "";
        Integer currentWorkbenchUserId = 0;
        Integer currentLocalIbdbUserId = 0;
        try {
            currentWorkbenchUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
            Project lastProject = workbenchDataManager.getLastOpenedProject(currentWorkbenchUserId);
            currentLocalIbdbUserId = workbenchDataManager.getLocalIbdbUserId(currentWorkbenchUserId, lastProject.getProjectId());
            User exporterUser = userDataManager.getUserById(currentLocalIbdbUserId);
            Person exporterPerson = userDataManager.getPersonById(exporterUser.getPersonid());
            exporterName = exporterPerson.getFirstName() + " " + exporterPerson.getLastName();
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting current workbench user information.", e);
        } catch (NullPointerException ex){
        	LOG.error("Error with getting user information for exporter with id = " + currentLocalIbdbUserId, ex);
        }
        
        HSSFRow listExporterRow = descriptionSheet.createRow(actualRow + 3); 
        listExporterRow.createCell(0).setCellValue("LIST EXPORTER");
        listExporterRow.createCell(1).setCellValue("PERSON EXPORTING THE LIST");
        listExporterRow.createCell(2).setCellValue("PERSON");
        listExporterRow.createCell(3).setCellValue("DBCV");
        listExporterRow.createCell(4).setCellValue("ASSIGNED");
        listExporterRow.createCell(5).setCellValue("C");
        listExporterRow.createCell(6).setCellValue(exporterName.trim());
        
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
    }
    
    private void writeListFactorSection(Map<String, CellStyle> styles, HSSFSheet descriptionSheet, int startingRow, Map<String, Boolean> visibleColumnMap) {
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
        spaceCell.setCellValue("NESTED IN");
        spaceCell.setCellStyle(styles.get(HEADING_STYLE));
        
        if(visibleColumnMap.get(ListDataTablePropertyID.ENTRY_ID.getName())){
	        HSSFRow entryIdRow = descriptionSheet.createRow(++actualRow);
	        entryIdRow.createCell(0).setCellValue("ENTRY");
	        entryIdRow.createCell(1).setCellValue("The germplasm entry number");
	        entryIdRow.createCell(2).setCellValue("GERMPLASM ENTRY");
	        entryIdRow.createCell(3).setCellValue("NUMBER");
	        entryIdRow.createCell(4).setCellValue("ENUMERATED");
	        entryIdRow.createCell(5).setCellValue("N");
	        entryIdRow.createCell(6).setCellValue("");
        }
        
        if(visibleColumnMap.get(ListDataTablePropertyID.GID.getName())){
        	HSSFRow gidRow = descriptionSheet.createRow(++actualRow);
            gidRow.createCell(0).setCellValue("GID");
            gidRow.createCell(1).setCellValue("The GID of the germplasm");
            gidRow.createCell(2).setCellValue("GERMPLASM ID");
            gidRow.createCell(3).setCellValue("DBID");
            gidRow.createCell(4).setCellValue("ASSIGNED");
            gidRow.createCell(5).setCellValue("N");
            gidRow.createCell(6).setCellValue("");
        }
        
        if(visibleColumnMap.get(ListDataTablePropertyID.ENTRY_CODE.getName())){
        	HSSFRow entryCodeRow = descriptionSheet.createRow(++actualRow);
            entryCodeRow.createCell(0).setCellValue("ENTRY CODE");
            entryCodeRow.createCell(1).setCellValue("Germplasm entry code");
            entryCodeRow.createCell(2).setCellValue("GERMPLASM ENTRY");
            entryCodeRow.createCell(3).setCellValue("CODE");
            entryCodeRow.createCell(4).setCellValue("ASSIGNED");
            entryCodeRow.createCell(5).setCellValue("C");
            entryCodeRow.createCell(6).setCellValue("");
        }
        
        if(visibleColumnMap.get(ListDataTablePropertyID.DESIGNATION.getName())){
        	HSSFRow designationRow = descriptionSheet.createRow(++actualRow);
            designationRow.createCell(0).setCellValue("DESIGNATION");
            designationRow.createCell(1).setCellValue("The name of the germplasm");
            designationRow.createCell(2).setCellValue("GERMPLASM ID");
            designationRow.createCell(3).setCellValue("DBCV");
            designationRow.createCell(4).setCellValue("ASSIGNED");
            designationRow.createCell(5).setCellValue("C");
            designationRow.createCell(6).setCellValue("");
        }
        
        if(visibleColumnMap.get(ListDataTablePropertyID.PARENTAGE.getName())){
        	HSSFRow crossRow = descriptionSheet.createRow(++actualRow);
            crossRow.createCell(0).setCellValue("CROSS");
            crossRow.createCell(1).setCellValue("The pedigree string of the germplasm");
            crossRow.createCell(2).setCellValue("CROSS NAME");
            crossRow.createCell(3).setCellValue("NAME");
            crossRow.createCell(4).setCellValue("ASSIGNED");
            crossRow.createCell(5).setCellValue("C");
            crossRow.createCell(6).setCellValue("");
        }
        
        if(visibleColumnMap.get(ListDataTablePropertyID.SEED_SOURCE.getName())){
        	HSSFRow sourceRow = descriptionSheet.createRow(++actualRow);
            sourceRow.createCell(0).setCellValue("SOURCE");
            sourceRow.createCell(1).setCellValue("The seed source of the germplasm");
            sourceRow.createCell(2).setCellValue("SEED SOURCE");
            sourceRow.createCell(3).setCellValue("NAME");
            sourceRow.createCell(4).setCellValue("Seed Source");
            sourceRow.createCell(5).setCellValue("C");
            sourceRow.createCell(6).setCellValue("");
        }
    }
    
    private void writeObservationSheet(Map<String, CellStyle> styles, HSSFSheet observationSheet, Map<String, Boolean> visibleColumnMap) throws GermplasmListExporterException {
        HSSFRow listEntriesHeader = observationSheet.createRow(0);
        
        int columnIndex = 0;
        if(visibleColumnMap.get(ListDataTablePropertyID.ENTRY_ID.getName())){
        	Cell entryIdCell = listEntriesHeader.createCell(columnIndex);
            entryIdCell.setCellValue("ENTRY");
            entryIdCell.setCellStyle(styles.get(HEADING_STYLE));
            columnIndex++;
        }
        
        if(visibleColumnMap.get(ListDataTablePropertyID.GID.getName())){
	        Cell gidCell = listEntriesHeader.createCell(columnIndex);
	        gidCell.setCellValue("GID");
	        gidCell.setCellStyle(styles.get(HEADING_STYLE));
	        columnIndex++;
        }
        
        if(visibleColumnMap.get(ListDataTablePropertyID.ENTRY_CODE.getName())){
	        Cell entryCodeCell = listEntriesHeader.createCell(columnIndex);
	        entryCodeCell.setCellValue("ENTRY CODE");
	        entryCodeCell.setCellStyle(styles.get(HEADING_STYLE));
	        columnIndex++;
        }
        
        if(visibleColumnMap.get(ListDataTablePropertyID.DESIGNATION.getName())){
	        Cell designationCell = listEntriesHeader.createCell(columnIndex);
	        designationCell.setCellValue("DESIGNATION");
	        designationCell.setCellStyle(styles.get(HEADING_STYLE));
	        columnIndex++;
        }
        
        if(visibleColumnMap.get(ListDataTablePropertyID.PARENTAGE.getName())){
	        Cell crossCell = listEntriesHeader.createCell(columnIndex);
	        crossCell.setCellValue("CROSS");
	        crossCell.setCellStyle(styles.get(HEADING_STYLE));
	        columnIndex++;
        }
        
        if(visibleColumnMap.get(ListDataTablePropertyID.SEED_SOURCE.getName())){
	        Cell sourceCell = listEntriesHeader.createCell(columnIndex);
	        sourceCell.setCellValue("SOURCE");
	        sourceCell.setCellStyle(styles.get(HEADING_STYLE));
	        columnIndex++;
        }
        
        
        List<GermplasmListData> listDatas = new ArrayList<GermplasmListData>();
        try {
            long listDataCount = germplasmListManager.countGermplasmListDataByListId(listId);
            listDatas = germplasmListManager.getGermplasmListDataByListId(listId, 0, (int) listDataCount);
            List<Name> preferredIds = germplasmDataManager.getPreferredIdsByListId(listId);
            Map<Integer, String> preferredIdsMap = new HashMap<Integer, String>();
            for (Name name : preferredIds) {
                preferredIdsMap.put(name.getGermplasmId(), name.getNval());
            }
            
            int i = 1;
            for (GermplasmListData listData : listDatas) {
                HSSFRow listEntry = observationSheet.createRow(i);
                
                int j = 0;
                if(visibleColumnMap.get(ListDataTablePropertyID.ENTRY_ID.getName())){
                	listEntry.createCell(j).setCellValue(listData.getEntryId());
                	j++;
                }
                
                if(visibleColumnMap.get(ListDataTablePropertyID.GID.getName())){
                	listEntry.createCell(j).setCellValue(listData.getGid());
                	j++;
                }
                
                if(visibleColumnMap.get(ListDataTablePropertyID.ENTRY_CODE.getName())){
                	listEntry.createCell(j).setCellValue(listData.getEntryCode());
                	j++;
                }
                
                if(visibleColumnMap.get(ListDataTablePropertyID.DESIGNATION.getName())){
                	listEntry.createCell(j).setCellValue(listData.getDesignation());
                	j++;
                }
                
                if(visibleColumnMap.get(ListDataTablePropertyID.PARENTAGE.getName())){
                	listEntry.createCell(j).setCellValue(listData.getGroupName());
                	j++;
                }
                
                if(visibleColumnMap.get(ListDataTablePropertyID.SEED_SOURCE.getName())){
                	listEntry.createCell(j).setCellValue(listData.getSeedSource());
                	j++;
                }
                
                i+=1;
            }
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting germplasm list entries.", e);
        }
        
    }
    
    public FileOutputStream exportListForKBioScienceGenotypingOrder(String filename, int plateSize) throws GermplasmListExporterException{
        String[] wellLetters = {"A", "B", "C", "D", "E", "F", "G", "H"};
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
            if(plateSize == 96 && listDataCount > 95){
                plateNum = 1;
                plateName = plateName + "-" + plateNum;
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
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            wb.write(fileOutputStream);
            fileOutputStream.close();
            return fileOutputStream;
        } catch(Exception ex) {
            throw new GermplasmListExporterException("Error with writing to: " + filename, ex);
        }
    }
}
