package org.generationcp.breeding.manager.listmanager.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.pojo.GermplasmListExportInputValues;
import org.generationcp.commons.service.ExportService;
import org.generationcp.commons.service.impl.ExportServiceImpl;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

import javax.annotation.Resource;

@Configurable
public class GermplasmListExporter {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmListExporter.class);
	
    @Autowired
    private GermplasmListManager germplasmListManager;

	@Autowired
    private UserDataManager userDataManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	private ExportService exportService;
    private Integer listId;
    
    public GermplasmListExporter(Integer germplasmListId) {
        this.listId = germplasmListId;
        this.exportService = new ExportServiceImpl();
    }
    
	public FileOutputStream exportKBioScienceGenotypingOrderXLS(String filename, int plateSize) throws GermplasmListExporterException {
		 
        List<ExportColumnHeader> exportColumnHeaders = getColumnHeadersForGenotypingData(plateSize);
        List<Map<Integer, ExportColumnValue>> exportColumnValues = getColumnValuesForGenotypingData(plateSize);
        
        try {
        	exportService = new ExportServiceImpl();
			return exportService.generateExcelFileForSingleSheet(exportColumnValues, exportColumnHeaders, filename, "List");
		} catch (IOException e) {
			throw new GermplasmListExporterException("Error with writing to: " + filename, e);
		}
	}
	
	protected List<ExportColumnHeader> getColumnHeadersForGenotypingData(int plateSize) {
		//generate columns headers
        List<ExportColumnHeader> exportColumnHeaders = new ArrayList<ExportColumnHeader>();
        exportColumnHeaders.add(new ExportColumnHeader(0,"Subject ID",true));
        exportColumnHeaders.add(new ExportColumnHeader(1,"Plate ID",true));
        exportColumnHeaders.add(new ExportColumnHeader(2,"Well",true));
        exportColumnHeaders.add(new ExportColumnHeader(3,"Sample type",true));
        exportColumnHeaders.add(new ExportColumnHeader(4,String.valueOf(plateSize),true));
        exportColumnHeaders.add(new ExportColumnHeader(5,"Primer",true));
        exportColumnHeaders.add(new ExportColumnHeader(6,"Subject BC",true));
        exportColumnHeaders.add(new ExportColumnHeader(7,"Plate BC",true));
		return exportColumnHeaders;
	}
	
	protected List<Map<Integer, ExportColumnValue>> getColumnValuesForGenotypingData(int plateSize) throws GermplasmListExporterException {
		
		List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<Map<Integer,ExportColumnValue>>();

        GermplasmList germplasmList = getGermplasmListAndListData(listId);
		String listName = germplasmList.getName();
		
		List<GermplasmListData> listDatas = germplasmList.getListData();
		
		String plateName = listName;
		int plateNum = 0;
		if(plateSize == 96 && listDatas.size() > 95){
		    plateNum = 1;
		    plateName = plateName + "-" + plateNum;
		}
		
		String[] wellLetters = {"A", "B", "C", "D", "E", "F", "G", "H"};
		int wellLetterIndex = 0;
		int wellNumberIndex = 1;
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
		    
		    Map<Integer, ExportColumnValue> exportRowValue = new HashMap<Integer, ExportColumnValue>();
		    exportRowValue.put(0, new ExportColumnValue(0,listData.getEntryId().toString()));
		    exportRowValue.put(1, new ExportColumnValue(1,plateName));
		    exportRowValue.put(2, new ExportColumnValue(2,well));
		    exportRowValue.put(3, new ExportColumnValue(3,null));
		    exportRowValue.put(4, new ExportColumnValue(4,null));
		    exportRowValue.put(5, new ExportColumnValue(5,null));
		    exportRowValue.put(6, new ExportColumnValue(6,null));
		    exportRowValue.put(7, new ExportColumnValue(7,null));
		    
		    exportColumnValues.add(exportRowValue);
		    
		    wellNumberIndex++;
		}
		return exportColumnValues;
	}

	public FileOutputStream exportGermplasmListXLS(String fileName, Table listDataTable)
			throws GermplasmListExporterException, MiddlewareQueryException {
    	
		GermplasmListExportInputValues input = new GermplasmListExportInputValues();
		input.setFileName(fileName);
		
		GermplasmList germplasmList = getGermplasmListAndListData(listId);
		input.setGermplasmList(germplasmList);
		
		input.setOwnerName(getOwnerName(germplasmList.getUserId()));
		
		Integer currentLocalIbdbUserId = contextUtil.getCurrentUserLocalId();
		input.setCurrentLocalIbdbUserId(currentLocalIbdbUserId);
		
        input.setExporterName(getExporterName(currentLocalIbdbUserId));
        
        input.setVisibleColumnMap(getVisibleColumnMap(listDataTable));
        
        return exportService.generateGermplasmListExcelFile(input);
    }

	protected String getExporterName(Integer currentLocalIbdbUserId)
			throws GermplasmListExporterException {
		String exporterName = "";
        try {
            User exporterUser = userDataManager.getUserById(currentLocalIbdbUserId);
            Person exporterPerson = userDataManager.getPersonById(exporterUser.getPersonid());
            exporterName = exporterPerson.getFirstName() + " " + exporterPerson.getLastName();
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting current workbench user information.", e);
        } catch (NullPointerException ex){
        	LOG.error("Error with getting user information for exporter with id = " + currentLocalIbdbUserId, ex);
        }
		return exporterName;
	}

	protected String getOwnerName(Integer userId) throws GermplasmListExporterException {
		// retrieve user details
        String ownerName = "";
        try {
            User ownerUser = userDataManager.getUserById(userId);
            Person ownerPerson = userDataManager.getPersonById(ownerUser.getPersonid());
            if(ownerPerson != null){
            	ownerName = ownerPerson.getFirstName() + " " + ownerPerson.getLastName();
            } else{
            	ownerName = ownerUser.getName();
            }
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting user information.", e);
        } catch (NullPointerException ex){
        	LOG.error("Error with getting user information for list owner with id = " + userId, ex);
        }
		return ownerName;
	}

	protected GermplasmList getGermplasmListAndListData(Integer listId) throws GermplasmListExporterException {
		GermplasmList germplasmList; 
		//set germplasmList and germplasmListData
        try {
            germplasmList = this.germplasmListManager.getGermplasmListById(listId);
        } catch (MiddlewareQueryException e) {
            throw new GermplasmListExporterException("Error with getting Germplasm List with id: " + listId, e);
        }
        
        List<GermplasmListData> germplasmlistData = new ArrayList<GermplasmListData>();
		try {
			long listDataCount = germplasmListManager.countGermplasmListDataByListId(listId);
			germplasmlistData = germplasmListManager.getGermplasmListDataByListId(listId, 0, (int) listDataCount);
		} catch (MiddlewareQueryException e1) {
			LOG.error(e1.getMessage(),e1);
		}
		germplasmList.setListData(germplasmlistData);
		
		return germplasmList;
	}
	
    protected Map<String,Boolean> getVisibleColumnMap(Table listDataTable) {
    	
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
    
	public void exportGermplasmListCSV(String fileName, Table listDataTable)
			throws GermplasmListExporterException {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = getExportColumnValuesFromTable(listDataTable);
		List<ExportColumnHeader> exportColumnHeaders = getExportColumnHeadersFromTable(listDataTable);

		try {

			exportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName);

		} catch (IOException e) {
			throw new GermplasmListExporterException("Error with exporting list to CSV File.", e);
		}

	}

	protected List<ExportColumnHeader> getExportColumnHeadersFromTable(Table listDataTable) {

		Map<String, Boolean> visibleColumns = this.getVisibleColumnMap(listDataTable);

		List<ExportColumnHeader> exportColumnHeaders = new ArrayList<>();

		exportColumnHeaders.add(new ExportColumnHeader(0, messageSource
				.getMessage(ListDataTablePropertyID.ENTRY_ID.getColumnDisplay()), visibleColumns
				.get(ListDataTablePropertyID.ENTRY_ID.getName())));

		exportColumnHeaders.add(new ExportColumnHeader(1, messageSource
				.getMessage(ListDataTablePropertyID.GID.getColumnDisplay()), visibleColumns
				.get(ListDataTablePropertyID.GID.getName())));

		exportColumnHeaders.add(new ExportColumnHeader(2, messageSource
				.getMessage(ListDataTablePropertyID.ENTRY_CODE.getColumnDisplay()), visibleColumns
				.get(ListDataTablePropertyID.ENTRY_CODE.getName())));

		exportColumnHeaders.add(new ExportColumnHeader(3, messageSource
				.getMessage(ListDataTablePropertyID.DESIGNATION.getColumnDisplay()), visibleColumns
				.get(ListDataTablePropertyID.DESIGNATION.getName())));

		exportColumnHeaders.add(new ExportColumnHeader(4, messageSource
				.getMessage(ListDataTablePropertyID.PARENTAGE.getColumnDisplay()), visibleColumns
				.get(ListDataTablePropertyID.PARENTAGE.getName())));

		exportColumnHeaders.add(new ExportColumnHeader(5, messageSource
				.getMessage(ListDataTablePropertyID.SEED_SOURCE.getColumnDisplay()), visibleColumns
				.get(ListDataTablePropertyID.SEED_SOURCE.getName())));

		return exportColumnHeaders;
	}

	protected List<Map<Integer, ExportColumnValue>> getExportColumnValuesFromTable(Table listDataTable) {

		List<Map<Integer, ExportColumnValue>> exportColumnValues = new ArrayList<>();

		for (Object itemId : listDataTable.getItemIds()) {
			Map<Integer, ExportColumnValue> row = new HashMap<>();

			String entryIdValue = listDataTable.getItem(itemId)
					.getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).getValue()
					.toString();
			String gidValue = ((Button) listDataTable.getItem(itemId)
					.getItemProperty(ListDataTablePropertyID.GID.getName()).getValue()).getCaption();
			String entryCodeValue = listDataTable.getItem(itemId)
					.getItemProperty(ListDataTablePropertyID.ENTRY_CODE.getName()).getValue()
					.toString();
			String designationValue = ((Button) listDataTable.getItem(itemId)
					.getItemProperty(ListDataTablePropertyID.DESIGNATION.getName()).getValue()).getCaption();
			String parentageValue = listDataTable.getItem(itemId)
					.getItemProperty(ListDataTablePropertyID.PARENTAGE.getName()).getValue()
					.toString();
			String seedSourceValue = listDataTable.getItem(itemId)
					.getItemProperty(ListDataTablePropertyID.SEED_SOURCE.getName()).getValue()
					.toString();

			row.put(0, new ExportColumnValue(0, entryIdValue));
			row.put(1, new ExportColumnValue(1, gidValue));
			row.put(2, new ExportColumnValue(2, entryCodeValue));
			row.put(3, new ExportColumnValue(3, designationValue));
			row.put(4, new ExportColumnValue(4, parentageValue));
			row.put(5, new ExportColumnValue(5, seedSourceValue));

			exportColumnValues.add(row);
		}

		return exportColumnValues;
	}
	
	protected void setExportService(ExportService exportService) {
		this.exportService = exportService;
	}
	
	protected void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	protected void setGermplasmListManager(GermplasmListManager germplasmListManager){
		this.germplasmListManager = germplasmListManager;
	}

	protected void setUserDataManager(UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}
}
