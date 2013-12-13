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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.listimport.GermplasmImportFileComponent;
import org.generationcp.breeding.manager.pojos.ImportedCondition;
import org.generationcp.breeding.manager.pojos.ImportedConstant;
import org.generationcp.breeding.manager.pojos.ImportedFactor;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.breeding.manager.pojos.ImportedVariate;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@Configurable
public class GermplasmListUploader implements Receiver, SucceededListener {
    
    private static final long serialVersionUID = 1L;
    
    private static final String ENTRY_PROPERTY = "GERMPLASM ENTRY";
    private static final String ENTRY_SCALE = "NUMBER";
    private static final String DESIG_PROPERTY = "GERMPLASM ID";
    private static final String DESIG_SCALE = "DBCV";
    private static final String GID_PROPERTY = "GERMPLASM ID";
    private static final String GID_SCALE = "DBID";
    
    private String entryFactor;
    private String desigFactor;
    private String gidFactor;
    
    private GermplasmImportFileComponent source;
    
    public File file;

    private String tempFileName;
    
    private Integer currentSheet;
    private Integer currentRow;
    
    private String originalFilename;
    private String listName;
    private String listTitle;
    private String listType;
    private Date listDate;
    
    private InputStream inp;
    private Workbook wb;
    
    private ImportedGermplasmList importedGermplasmList;
    
    private Boolean fileIsValid;
    private Boolean importFileIsAdvanced;

    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    
    public String getOriginalFilename() {
        return originalFilename;
    }

    public Boolean getFileIsValid(){
    	return fileIsValid;
    }
    
    public String getListName() {
        return listName;
    }

    public Date getListDate() {
        return listDate;
    }

    public String getListTitle() {
        return listTitle;
    }

    public String getListType() {
        return listType;
    }

    
    public GermplasmListUploader(GermplasmImportFileComponent source) {
        this.source = source;
    }


    public ImportedGermplasmList getImportedGermplasmList() {
        return importedGermplasmList;
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
        
        fileIsValid = true;
        
        importedGermplasmList = null;
        
        try {
            inp = new FileInputStream(tempFileName);
            wb = new HSSFWorkbook(inp);
            
            readSheet1();
            readSheet2();

            if(fileIsValid==false){
                importedGermplasmList = null;
                
            	if(source instanceof GermplasmImportFileComponent){
            		source.disableNextButton();
            	}
            	
            } else {
            	
            	Window.Notification notif;
            	notif = new Window.Notification("File was successfully uploaded", Notification.TYPE_HUMANIZED_MESSAGE);
            	notif.setDelayMsec(5000);
            	source.getAccordion().getWindow().showNotification(notif);
               
            	if(source instanceof GermplasmImportFileComponent){
            		source.updateFilenameLabelValue(originalFilename);
            		source.enableNextButton();
            	}
            	
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            showInvalidFileTypeError();
        } catch (ReadOnlyException e) {
            showInvalidFileTypeError();
        } catch (ConversionException e) {
            showInvalidFileTypeError();
        } catch (OfficeXmlFileException e){
            showInvalidFileTypeError();
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
                
        ImportedGermplasm importedGermplasm;
        Boolean entryColumnIsPresent = false;
        Boolean desigColumnIsPresent = false;
        Boolean gidColumnIsPresent = false;
    
        //Check if columns ENTRY and DESIG is present
        if(importedGermplasmList.getImportedFactors()!=null)
        for(int col=0;col<importedGermplasmList.getImportedFactors().size();col++){
        	String columnName = getCellStringValue(currentSheet, currentRow, col, true); 
            if(columnName.equals(entryFactor)){
                entryColumnIsPresent = true;
            } else if(desigFactor != null && columnName.equals(desigFactor)){
                desigColumnIsPresent = true;
            } else if(gidFactor != null && columnName.equals(gidFactor)){
                gidColumnIsPresent = true;
            }  
        }
        
        if(entryColumnIsPresent==false){
            showInvalidFileError("ENTRY column missing from Observation sheet.");
        } else if(gidColumnIsPresent==false && desigColumnIsPresent==false){
            showInvalidFileError("DESIGNATION column missing from Observation sheet.");
        } else if(gidFactor != null && !gidColumnIsPresent){
        	showInvalidFileError("GID column missing from Observation sheet.");
        }
        
        //If still valid (after checking headers for ENTRY and DESIG), proceed
        if(fileIsValid){
            currentRow++;
        
            while(!rowIsEmpty() && fileIsValid){
                System.out.println("");
                importedGermplasm = new ImportedGermplasm();
                for(int col=0;col<importedGermplasmList.getImportedFactors().size();col++){
                	
                	//Map cell (given a column label) with a pojo setter 
                    if(importedGermplasmList.getImportedFactors().get(col).getFactor().equals(entryFactor)){
                        importedGermplasm.setEntryId(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
                        System.out.println("DEBUG | ENTRY:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(importedGermplasmList.getImportedFactors().get(col).getFactor().equals(desigFactor)){
                        importedGermplasm.setDesig(getCellStringValue(currentSheet, currentRow, col, true));
                        System.out.println("DEBUG | DESIG:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(importedGermplasmList.getImportedFactors().get(col).getFactor().equals(gidFactor)){
                        importedGermplasm.setGid(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
                        System.out.println("DEBUG | GID:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(importedGermplasmList.getImportedFactors().get(col).getFactor().toUpperCase().equals("CROSS")){
                        importedGermplasm.setCross(getCellStringValue(currentSheet, currentRow, col, true));
                        System.out.println("DEBUG | CROSS:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(importedGermplasmList.getImportedFactors().get(col).getFactor().toUpperCase().equals("SOURCE")){
                        importedGermplasm.setSource(getCellStringValue(currentSheet, currentRow, col, true));
                        System.out.println("DEBUG | SOURCE:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(importedGermplasmList.getImportedFactors().get(col).getFactor().toUpperCase().equals("ENTRY CODE")){
                        importedGermplasm.setEntryCode(getCellStringValue(currentSheet, currentRow, col, true));
                        System.out.println("DEBUG | ENTRY CODE:"+getCellStringValue(currentSheet, currentRow, col));
                    } else {
                        System.out.println("DEBUG | Unhandled Column - "+importedGermplasmList.getImportedFactors().get(col).getFactor().toUpperCase()+":"+getCellStringValue(currentSheet, currentRow, col));
                    }
                }
                
                //For cases where GID is preset and Desig is not present, or GID is not present and desig is present, or both are present
                
                //GID is given, but no DESIG, get value of DESIG given GID
                if(importedGermplasm.getGid()!=null && importedGermplasm.getDesig()==null){
                	try {

                		//Check if germplasm exists
				        Germplasm currentGermplasm = germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid());
				        if(currentGermplasm==null){
				        	showInvalidFileError("Germplasm with GID "+importedGermplasm.getGid()+" not found in database");
				        } else {
                		
					        List<Integer> importedGermplasmGids = new ArrayList<Integer>();
					        importedGermplasmGids.add(importedGermplasm.getGid());
					        
							Map<Integer, String> preferredNames = germplasmDataManager.getPreferredNamesByGids(importedGermplasmGids);
							
							if(preferredNames.get(importedGermplasm.getGid())!=null)
								importedGermplasm.setDesig(preferredNames.get(importedGermplasm.getGid()));
							
				        }
					} catch (MiddlewareQueryException e) {
						e.printStackTrace();
					}
                	
                //GID and DESIG are given, make sure DESIG matches value of GID
                } else if (importedGermplasm.getGid()!=null && importedGermplasm.getDesig()!=null && importedGermplasm.getDesig()!=""){
                	try {
                		
                		//Check if germplasm exists
				        Germplasm currentGermplasm = germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid());
				        if(currentGermplasm==null){
				        	showInvalidFileError("Germplasm with GID "+importedGermplasm.getGid()+" not found in database");
				        } else {

					        List<Integer> importedGermplasmGids = new ArrayList<Integer>();
					        importedGermplasmGids.add(importedGermplasm.getGid());
					        
							Map<Integer, String> preferredNames = germplasmDataManager.getPreferredNamesByGids(importedGermplasmGids);
							
							if(preferredNames.get(importedGermplasm.getGid())!=null && !importedGermplasm.getDesig().toUpperCase().equals(preferredNames.get(importedGermplasm.getGid()).toUpperCase())){
								showInvalidFileError("Invalid GID and DESIG/DESIGNATION combination on Sheet 2, DESIG on file for GID "+importedGermplasm.getGid()+" is \""+importedGermplasm.getDesig()+"\" but preferred name on database is \""+preferredNames.get(importedGermplasm.getGid())+"\".");
							} else {
								importedGermplasm.setDesig(preferredNames.get(importedGermplasm.getGid()));
							}
							
				        }
					} catch (MiddlewareQueryException e) {
						e.printStackTrace();
					}
                	
                //GID is not given, and DESIG is given
                } else {
                	
                }
                
                importedGermplasmList.addImportedGermplasm(importedGermplasm);
                currentRow++;
            }
        }
    }

    private void readGermplasmListFileInfo(){
            listName = getCellStringValue(0,0,1,true);
            listTitle = getCellStringValue(0,1,1,true);
            
            try {
				Long matchingNamesCountOnLocal = germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL, Database.LOCAL);
				Long matchingNamesCountOnCentral = germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL, Database.CENTRAL);
				
				if(matchingNamesCountOnLocal>0 || matchingNamesCountOnCentral>0){
					showInvalidFileError("There is already an existing germplasm list with the name specified on the file");
				}
				
			} catch (MiddlewareQueryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

            if(getCellStringValue(0,2,0,true).toUpperCase().equals("LIST TYPE")){
            //LIST TYPE on ROW3, LIST DATE on ROW4
            	listType = getCellStringValue(0,2,1,true);
        		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        		try {
        			listDate = simpleDateFormat.parse(getCellStringValue(0,3,1,true));
        		} catch(ParseException e){
        			showInvalidFileError("Invalid file headers, list date value should be on column B row 4");
        		}
            } else {
            //LIST TYPE on ROW4, LIST DATE on ROW3
        		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        		try {
        			listDate = simpleDateFormat.parse(getCellStringValue(0,2,1,true));
        		} catch(ParseException e){
        			showInvalidFileError("Invalid file headers, list date value should be on column B row 4");
        		}
            	listType = getCellStringValue(0,3,1,true);
            }

            try {
				List<UserDefinedField> listTypes = germplasmListManager.getGermplasmListTypes();
				List<String> listTypeCodes = new ArrayList<String>();
				for(UserDefinedField listType : listTypes){
					if(listType.getFcode()!=null){
						listTypeCodes.add(listType.getFcode());
					}
				}
				
				System.out.println("List Types: "+listTypeCodes);
				
				if(!listTypeCodes.contains(listType)){
					showInvalidFileError("Invalid list type "+listType);
				}
			} catch (MiddlewareQueryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
            importedGermplasmList = new ImportedGermplasmList(originalFilename, listName, listTitle, listType, listDate); 
            
            System.out.println("DEBUG | Original Filename:" + originalFilename);
            System.out.println("DEBUG | List Name:" + listName);
            System.out.println("DEBUG | List Title:" + listTitle);
            System.out.println("DEBUG | List Type:" + listType);
            System.out.println("DEBUG | List Date:" + listDate);
            
        //Prepare for next set of data
        while(!rowIsEmpty()){
            currentRow++;
        }
        
    }
    
    private void readConditions(){
        
        currentRow++; //Skip row from file info
    
        //Conditions section is not required, do nothing if it's not there
        if(getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("CONDITION")){
	        //Check if headers are correct
	        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("CONDITION") 
	            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
	            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
	            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
	            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
	            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
	            || !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("VALUE")
	            || !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")){
	            showInvalidFileError("Incorrect headers for conditions.");
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
    }

    private void readFactors(){
        Boolean entryColumnIsPresent = false;
        Boolean desigColumnIsPresent = false;
        Boolean gidColumnIsPresent = false;
        
        importFileIsAdvanced = false;
        
        entryFactor = null;
        desigFactor = null;
        gidFactor = null;
        
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("FACTOR") 
            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
            || !getCellStringValue(currentSheet,currentRow,7,true).toUpperCase().equals("LABEL")) {
            showInvalidFileError("Incorrect headers for factors.");
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
                
                //Factors validation
                String property = importedFactor.getProperty().toUpperCase();
                String scale = importedFactor.getScale().toUpperCase();
                if(property.equals(ENTRY_PROPERTY) && scale.equals(ENTRY_SCALE)){
                	entryColumnIsPresent = true;
                	entryFactor = importedFactor.getFactor();
                } else if(property.equals(DESIG_PROPERTY) && scale.equals(DESIG_SCALE)){
                	desigColumnIsPresent = true;
                	desigFactor = importedFactor.getFactor();
                } else if(property.equals(GID_PROPERTY) && scale.equals(GID_SCALE)){
                	gidColumnIsPresent = true;
                	importFileIsAdvanced = true;
                	gidFactor = importedFactor.getFactor();
                }
                currentRow++;
            }
        }
        currentRow++;

        //If ENTRY or DESIG is not present on Factors, return error
        if(entryColumnIsPresent==false){
        	showInvalidFileError("There is no ENTRY factor.");
        } else if(desigColumnIsPresent==false && gidColumnIsPresent==false){
            showInvalidFileError("There is no DESIGNATION factor.");
        } 
	}


    private void readConstants(){
    	
    	//Constants section is not required, do nothing if it's not there
    	if(getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("CONSTANT")){
	        //Check if headers are correct
	        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("CONSTANT") 
	            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
	            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
	            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
	            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
	            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")
	            || !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("VALUE")) {
	            showInvalidFileError("Incorrect headers for constants.");
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
    }
    
    private void readVariates(){
    	
    	//Variates section is not required, do nothing if it's not there
    	if(getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("VARIATE")){
	        //Check if headers are correct
	        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("VARIATE")
	            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
	            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
	            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
	            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
	            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")) {
	            showInvalidFileError("Incorrect headers for variates.");
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
    
    private void showInvalidFileError(String message){
        if(fileIsValid){
            //source.getAccordion().getWindow().showNotification("Invalid Import File: " + message, Notification.TYPE_ERROR_MESSAGE);
            MessageNotifier.showError(source.getAccordion().getWindow(), "Invalid Import File: ", message
                    , Notification.POSITION_CENTERED);
            fileIsValid = false;
        }
    }
    
    private void showInvalidFileTypeError(){
        if(fileIsValid){
            //source.getAccordion().getWindow().showNotification("Invalid Import File Type, you need to upload an XLS file", Notification.TYPE_ERROR_MESSAGE);
            MessageNotifier.showError(source.getAccordion().getWindow(), "Invalid Import File Type.", "Please upload a properly formatted XLS file."
                    , Notification.POSITION_CENTERED);
            fileIsValid = false;
        }
    }    
    
    public Boolean importFileIsAdvanced(){
    	return importFileIsAdvanced;
    }
};