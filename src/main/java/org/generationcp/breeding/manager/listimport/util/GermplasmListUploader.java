package org.generationcp.breeding.manager.listimport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.listimport.GermplasmImportFileComponent;
import org.generationcp.breeding.manager.listimport.pojos.ImportedCondition;
import org.generationcp.breeding.manager.listimport.pojos.ImportedConstant;
import org.generationcp.breeding.manager.listimport.pojos.ImportedFactor;
import org.generationcp.breeding.manager.listimport.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.listimport.pojos.ImportedGermplasmList;
import org.generationcp.breeding.manager.listimport.pojos.ImportedVariate;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.Window.Notification;

public class GermplasmListUploader implements Receiver, SucceededListener {
	
	private static final long serialVersionUID = 1L;
	private GermplasmImportFileComponent source;
	
	public File file;

	private String tempFileName;
	
	private Integer currentSheet;
	private Integer currentRow;
	private Integer currentColumn;
	
	private String originalFilename;
	private String listName;
	private String listTitle;
	private String listType;
	private Date listDate;
	
	private InputStream inp;
	private Workbook wb;
	
	private ImportedGermplasmList importedGermplasmList;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
	private Boolean fileIsValid;
	
	public GermplasmListUploader(GermplasmImportFileComponent source) {
		this.source = source;
	}
	
	public OutputStream receiveUpload(String filename, String mimeType) { 
		tempFileName = source.getAccordion().getApplication().getContext().getBaseDirectory().getAbsolutePath()+"/WEB-INF/uploads/imported_germplasmlist.xls";
		FileOutputStream fos = null;
        try {
        	file = new File(tempFileName);
            fos = new FileOutputStream(file);
            
            originalFilename = filename;            
        } catch (final java.io.FileNotFoundException e) {
            System.out.println("FileNotFoundException on receiveUpload(): "+e.getMessage());
            return null;
        }
        return fos; // Return the output stream to write to
    }

    public void uploadSucceeded(SucceededEvent event) {
    	System.out.println("DEBUG | "+tempFileName);
    	System.out.println("DEBUG | Upload succeeded!");
    	
    	currentSheet = 0;
    	currentRow = 0;
    	currentColumn = 0;
        
		try {
			inp = new FileInputStream(tempFileName);
			wb = new HSSFWorkbook(inp);
			
			fileIsValid = true;
			
        	readSheet1();
        	readSheet2();

        	if(fileIsValid==false){
        		importedGermplasmList = null;
        	}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			showInvalidFileError();
		} catch (ReadOnlyException e) {
			e.printStackTrace();
		} catch (ConversionException e) {
			e.printStackTrace();
		}
    }


    private void readSheet1(){
    	readGermplasmListFileInfo();
    	readConditions();
    	readFactors();
    	readConstants();
    	readVariates();
    }
    
    private void readSheet2(){
    	currentSheet = 1;
    	currentRow = 0;
    	currentColumn = 0;
    	    	
    	ImportedGermplasm importedGermplasm;
    	Boolean entryColumnIsPresent = false;
    	Boolean desigColumnIsPresent = false;    	
    
    	//Check if columns ENTRY and DESIG is present
    	if(importedGermplasmList.getImportedFactors()!=null)
		for(int col=0;col<importedGermplasmList.getImportedFactors().size();col++){
			if(getCellStringValue(currentSheet, currentRow, col, true).toUpperCase().equals("ENTRY"))
				entryColumnIsPresent = true;
			else if(getCellStringValue(currentSheet, currentRow, col, true).toUpperCase().equals("DESIG"))
				desigColumnIsPresent = true;
		}
		if(entryColumnIsPresent==false || desigColumnIsPresent==false){
			showInvalidFileError();
			System.out.println("DEBUG | Invalid file on missing ENTRY or DESIG on readSheet2");
		}
    	
		//If still valid (after checking headers for ENTRY and DESIG), proceed
		if(fileIsValid){
			currentRow++;
    	
			while(!rowIsEmpty()){
				System.out.println("");
				importedGermplasm = new ImportedGermplasm();
				for(int col=0;col<importedGermplasmList.getImportedFactors().size();col++){
					System.out.println("DEBUG | " + importedGermplasmList.getImportedFactors().get(col).getFactor() + ": " + getCellStringValue(currentSheet, currentRow, col, true));
				}
				currentRow++;
			}
    	}
    }

    private void readGermplasmListFileInfo(){
    	try {
    		listName = getCellStringValue(0,0,1,true);
        	listTitle = getCellStringValue(0,1,1,true);
        	listType = getCellStringValue(0,2,1,true);
			listDate = new SimpleDateFormat("yyyymmdd").parse(getCellStringValue(0,3,1,true));
        	
			importedGermplasmList = new ImportedGermplasmList(originalFilename, listName, listTitle, listType, listDate); 
			
	    	System.out.println("DEBUG | Original Filename:" + originalFilename);
	    	System.out.println("DEBUG | List Name:" + listName);
	    	System.out.println("DEBUG | List Title:" + listTitle);
	    	System.out.println("DEBUG | List Type:" + listType);
	    	System.out.println("DEBUG | List Date:" + listDate);
	    	
		} catch (ParseException e) {
			e.printStackTrace();
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
    		|| !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")){
    		showInvalidFileError();
    		System.out.println("DEBUG | Invalid file on readConditions header");
    		System.out.println(getCellStringValue(currentSheet,currentRow,0,true).toUpperCase());
    	}
    	//If file is still valid (after checking headers), proceed
    	if(fileIsValid){
    		ImportedCondition importedCondition;
    		currentRow++; 
    		while(!rowIsEmpty()){
    			importedCondition = new ImportedCondition(getCellStringValue(currentSheet,currentRow,0,true)
    				,getCellStringValue(currentSheet,currentRow,1,true)
    				,getCellStringValue(currentSheet,currentRow,2,true)
    				,getCellStringValue(currentSheet,currentRow,3,true)
    				,getCellStringValue(currentSheet,currentRow,4,true)
    				,getCellStringValue(currentSheet,currentRow,5,true)
    				,getCellStringValue(currentSheet,currentRow,6,true)
    				,getCellStringValue(currentSheet,currentRow,7,true));
    			importedGermplasmList.addImportedCondition(importedCondition);
    			currentRow++;
    		}
    	}
    	currentRow++;
    }

    private void readFactors(){
		Boolean entryColumnIsPresent = false;
		Boolean desigColumnIsPresent = false;

    	//Check if headers are correct
    	if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("FACTOR") 
        	|| !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
        	|| !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
        	|| !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
        	|| !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
        	|| !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
        	|| !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")) {
        	showInvalidFileError();
        	System.out.println("DEBUG | Invalid file on readFactors header");
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
    				,getCellStringValue(currentSheet,currentRow,7,true));
    			
   				importedGermplasmList.addImportedFactor(importedFactor);
    			
    			System.out.println("");
    			System.out.println("DEBUG | Factor:"+getCellStringValue(currentSheet,currentRow,0));
    			System.out.println("DEBUG | Description:"+getCellStringValue(currentSheet,currentRow,1));
    			System.out.println("DEBUG | Property:"+getCellStringValue(currentSheet,currentRow,2));
    			System.out.println("DEBUG | Scale:"+getCellStringValue(currentSheet,currentRow,3));
    			System.out.println("DEBUG | Method:"+getCellStringValue(currentSheet,currentRow,4));
    			System.out.println("DEBUG | Data Type:"+getCellStringValue(currentSheet,currentRow,5));
    			System.out.println("DEBUG | Value:"+getCellStringValue(currentSheet,currentRow,6));
    			System.out.println("DEBUG | Label:"+getCellStringValue(currentSheet,currentRow,7));
    			
    			//Check if the current factor is ENTRY or DESIG
    			if(importedFactor.getFactor().toUpperCase().equals("ENTRY")){
    				entryColumnIsPresent = true;
    			} else if(importedFactor.getFactor().toUpperCase().equals("DESIG")){
    				desigColumnIsPresent = true;
    			}
    			currentRow++;
    		}
    	}
    	currentRow++;

    	//If ENTRY or DESIG is not present on Factors, return error
    	if(entryColumnIsPresent == false || desigColumnIsPresent == false){
    		showInvalidFileError();
    		System.out.println("DEBUG | Invalid file on missing ENTRY or DESIG on readFactors");
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
        	|| !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("VALUE")) {
        	showInvalidFileError();
        	System.out.println("DEBUG | Invalid file on readConstants header");
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
    				,getCellStringValue(currentSheet,currentRow,6,true));
    			importedGermplasmList.addImportedConstant(importedConstant);   
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
        	|| !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")) {
        	showInvalidFileError();
        	System.out.println("DEBUG | Invalid file on readVariates header");
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
    				,getCellStringValue(currentSheet,currentRow,5,true));
    			importedGermplasmList.addImportedVariate(importedVariate);
    			currentRow++;
    		}
    	}
    	currentRow++;
    }

    
    
    private Boolean rowIsEmpty(){
    	return rowIsEmpty(currentRow);
    }
    
    private Boolean rowIsEmpty(Integer row){
    	return rowIsEmpty(currentSheet, row);
    }

    private Boolean rowIsEmpty(Integer sheet, Integer row){
        for(int col=0;col<8;col++){
        	if(getCellStringValue(sheet, row, col)!="" && getCellStringValue(sheet, row, col)!=null)
        		return false;
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
    		currentColumn = columnNumber;
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
    		return String.valueOf(cell.getNumericCellValue());
    	} catch(NullPointerException e) {
    		return "";
    	}
    }
    
    private void showInvalidFileError(){
    	if(fileIsValid){
    		source.getAccordion().getApplication().getMainWindow().showNotification("Invalid Import File", Notification.TYPE_ERROR_MESSAGE);
    		fileIsValid = false;
    	}
    }
    
};