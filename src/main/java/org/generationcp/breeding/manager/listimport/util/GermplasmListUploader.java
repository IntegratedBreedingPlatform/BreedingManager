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
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListUploader.class);
    
    private static final String LIST_NAME_HEADER_LABEL = "LIST NAME";
    private static final String LIST_DESC_HEADER_LABEL = "LIST DESCRIPTION";
    private static final String TITLE_HEADER_LABEL = "TITLE";
    private static final String LIST_DATE_HEADER_LABEL = "LIST DATE";
    private static final String LIST_TYPE_HEADER_LABEL = "LIST TYPE";
    
    private static final String ENTRY_PROPERTY = "GERMPLASM ENTRY";
    private static final String ENTRY_SCALE = "NUMBER";
    private static final String DESIG_PROPERTY = "GERMPLASM ID";
    private static final String DESIG_SCALE = "DBCV";
    private static final String GID_PROPERTY = "GERMPLASM ID";
    private static final String GID_SCALE = "DBID";
    private static final String ENTRY_CODE_PROPERTY = "GERMPLASM ENTRY";
    private static final String ENTRY_CODE_SCALE = "CODE";
    private static final String CROSS_PROPERTY = "CROSS NAME";
    private static final String CROSS_SCALE = "NAME";
    private static final String SOURCE_PROPERTY = "SEED SOURCE";
    private static final String SOURCE_SCALE = "NAME";
    
    private String entryFactor;
    private String desigFactor;
    private String gidFactor;
    private String entryCodeFactor;
    private String crossFactor;
    private String sourceFactor;
    
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
//            System.out.println("FileNotFoundException on receiveUpload(): "+e.getMessage());
            return null;
        }
        return fos; // Return the output stream to write to
    }

    public void uploadSucceeded(SucceededEvent event) {
//        System.out.println("DEBUG | "+tempFileName);
//        System.out.println("DEBUG | Upload succeeded!");
        
        currentSheet = 0;
        currentRow = 0;
        
        fileIsValid = true;
        
        importedGermplasmList = null;
        
        try {
            inp = new FileInputStream(tempFileName);
            wb = new HSSFWorkbook(inp);
            
            try{
                Sheet sheet1 = wb.getSheetAt(0);
                
                if(sheet1 == null || sheet1.getSheetName() == null || !(sheet1.getSheetName().equals("Description"))){
                    MessageNotifier.showError(source.getWindow(), "Error with reading file uploaded."
                            , "File doesn't have the first sheet - Description", Notification.POSITION_CENTERED);
                    fileIsValid = false;
                    return;
                }
            } catch(Exception ex){
                MessageNotifier.showError(source.getWindow(), "Error with reading file uploaded."
                        , "File doesn't have the first sheet - Description", Notification.POSITION_CENTERED);
                fileIsValid = false;
                return;
            }
            
            try{
                Sheet sheet2 = wb.getSheetAt(1);
                
                if(sheet2 == null || sheet2.getSheetName() == null || !(sheet2.getSheetName().equals("Observation"))){
                    MessageNotifier.showError(source.getWindow(), "Error with reading file uploaded."
                            , "File doesn't have the second sheet - Observation", Notification.POSITION_CENTERED);
                    fileIsValid = false;
                    return;
                }
            } catch(Exception ex){
                MessageNotifier.showError(source.getWindow(), "Error with reading file uploaded."
                        , "File doesn't have the second sheet - Observation", Notification.POSITION_CENTERED);
                fileIsValid = false;
                return;
            }
            
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
//            System.out.println("File not found");
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
            currentRow = 1;
        
            while(!rowIsEmpty() && fileIsValid){
//                System.out.println("");
                importedGermplasm = new ImportedGermplasm();
                for(int col=0;col<importedGermplasmList.getImportedFactors().size();col++){
                	//Map cell (given a column label) with a pojo setter
                	String columnHeader = getCellStringValue(currentSheet, 0, col, false);
                	if(columnHeader.equals(entryFactor)){
                    	importedGermplasm.setEntryId(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
//                        System.out.println("DEBUG | ENTRY:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(columnHeader.equals(desigFactor)){
                        importedGermplasm.setDesig(getCellStringValue(currentSheet, currentRow, col, true));
//                        System.out.println("DEBUG | DESIG:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(columnHeader.equals(gidFactor)){
                    	String gidString = getCellStringValue(currentSheet, currentRow, col, true);
                    	Integer gidInteger = null;
                    	if(gidString != null && gidString.length() > 0){
                    		gidInteger = Integer.valueOf(gidString);
                    	} 
                    	importedGermplasm.setGid(gidInteger);
//                        System.out.println("DEBUG | GID:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(columnHeader.equals(crossFactor)){
                        importedGermplasm.setCross(getCellStringValue(currentSheet, currentRow, col, true));
//                        System.out.println("DEBUG | CROSS:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(columnHeader.equals(sourceFactor)){
                        importedGermplasm.setSource(getCellStringValue(currentSheet, currentRow, col, true));
//                        System.out.println("DEBUG | SOURCE:"+getCellStringValue(currentSheet, currentRow, col));
                    } else if(columnHeader.equals(entryCodeFactor)){
                        importedGermplasm.setEntryCode(getCellStringValue(currentSheet, currentRow, col, true));
//                        System.out.println("DEBUG | ENTRY CODE:"+getCellStringValue(currentSheet, currentRow, col));
                    } else {
//                        System.out.println("DEBUG | Unhandled Column - " + columnHeader + ":" + getCellStringValue(currentSheet, currentRow, col));
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
                	
                //GID is not given or 0, and DESIG is not given
                } else if((importedGermplasm.getGid() == null || importedGermplasm.getGid().equals(Integer.valueOf(0)))
                		 && (importedGermplasm.getDesig() == null || importedGermplasm.getDesig().length() == 0)){
                	showInvalidFileError("Row " + currentRow + " on Observation sheet of file doesn't have a GID or a DESIGNATION value.");
                }
                
                importedGermplasmList.addImportedGermplasm(importedGermplasm);
                currentRow++;
            }
        }
    }

    private void readGermplasmListFileInfo(){
    	boolean listNameHeaderFound = false;
    	boolean listDescHeaderFound = false;
    	boolean listTypeHeaderFound = false;
    	boolean listDateHeaderFound = false;
    	
    	for(int ctr = 0; ctr < 4; ctr++){
    		String header =  getCellStringValue(0,ctr,0,true).trim().toUpperCase();
    		String value = getCellStringValue(0,ctr,1,true);
    		
    		if(header.equals(LIST_NAME_HEADER_LABEL)){
    			listName = value.trim();
    			listNameHeaderFound = true;
    		} else if(header.equals(LIST_DESC_HEADER_LABEL) || header.equals(TITLE_HEADER_LABEL)){
    			listTitle = value.trim();
    			listDescHeaderFound = true;
    		} else if(header.equals(LIST_DATE_HEADER_LABEL)){
    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    			try{
    				if(value != null && value.length() > 0){
    					listDate = simpleDateFormat.parse(value);
    				} else{
    					listDate = null;
    				}
    			} catch(ParseException ex){
    				showInvalidFileError("LIST DATE has wrong format. Please follow the format - yyyyMMdd.");
    				return;
    			}
    			listDateHeaderFound = true;
    		} else if(header.equals(LIST_TYPE_HEADER_LABEL)){
    			listType = value.trim();
    			listTypeHeaderFound = true;
    		}
    	}
    	
    	StringBuilder errorString = new StringBuilder();
    	if(!listNameHeaderFound){
    		errorString.append(LIST_NAME_HEADER_LABEL + " header not found. ");
    	} else if(!listDescHeaderFound){
    		errorString.append(LIST_DESC_HEADER_LABEL + " or " + TITLE_HEADER_LABEL + " header not found. ");
    	} else if(!listDateHeaderFound){
    		errorString.append(LIST_DATE_HEADER_LABEL + " header not found. ");
    	} else if(!listTypeHeaderFound){
    		errorString.append(LIST_TYPE_HEADER_LABEL + " header not found. ");
    	}
    	
    	if(errorString.toString().length() > 0){
    		showInvalidFileError(errorString.toString());
    		return;
    	}
    	
        if(listName != null && listName.length() > 0){
            try {
				Long matchingNamesCountOnLocal = germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL, Database.LOCAL);
				Long matchingNamesCountOnCentral = germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL, Database.CENTRAL);
				
				if(matchingNamesCountOnLocal>0 || matchingNamesCountOnCentral>0){
					showInvalidFileError("There is already an existing germplasm list with the name specified on the file");
				}
			} catch (MiddlewareQueryException e1) {
				LOG.error("Error with count Germplasm List by name = " + listName, e1);
			}
        }

        if(listType != null && listType.length() > 0){
	        try {
				List<UserDefinedField> listTypes = germplasmListManager.getGermplasmListTypes();
				List<String> listTypeCodes = new ArrayList<String>();
				for(UserDefinedField listType : listTypes){
					if(listType.getFcode()!=null){
						listTypeCodes.add(listType.getFcode());
					}
				}
				
				if(!listTypeCodes.contains(listType)){
					showInvalidFileError("Invalid list type "+listType);
				}
			} catch (MiddlewareQueryException e1) {
				LOG.error("Error with getting germplasm list types.", e1);
			}
        }
        
        importedGermplasmList = new ImportedGermplasmList(originalFilename, listName, listTitle, listType, listDate); 
        
//        System.out.println("DEBUG | Original Filename:" + originalFilename);
//        System.out.println("DEBUG | List Name:" + listName);
//        System.out.println("DEBUG | List Title:" + listTitle);
//        System.out.println("DEBUG | List Type:" + listType);
//        System.out.println("DEBUG | List Date:" + listDate);
    	    
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
	            || !getCellStringValue(currentSheet,currentRow,6,true).toUpperCase().equals("VALUE")){
	            showInvalidFileError("Incorrect headers for conditions.");
//	            System.out.println("DEBUG | Invalid file on readConditions header");
//	            System.out.println(getCellStringValue(currentSheet,currentRow,0,true).toUpperCase());
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
        entryCodeFactor = null;
        sourceFactor = null;
        crossFactor = null;
        
        //Check if headers are correct
        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("FACTOR") 
            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")){
            showInvalidFileError("Incorrect headers for factors.");
//            System.out.println("DEBUG | Invalid file on readFactors header");
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
                
//                System.out.println("");
//                System.out.println("DEBUG | Factor:"+getCellStringValue(currentSheet,currentRow,0));
//                System.out.println("DEBUG | Description:"+getCellStringValue(currentSheet,currentRow,1));
//                System.out.println("DEBUG | Property:"+getCellStringValue(currentSheet,currentRow,2));
//                System.out.println("DEBUG | Scale:"+getCellStringValue(currentSheet,currentRow,3));
//                System.out.println("DEBUG | Method:"+getCellStringValue(currentSheet,currentRow,4));
//                System.out.println("DEBUG | Data Type:"+getCellStringValue(currentSheet,currentRow,5));
//                System.out.println("DEBUG | Value:"+getCellStringValue(currentSheet,currentRow,6));
//                System.out.println("DEBUG | Label:"+getCellStringValue(currentSheet,currentRow,7));
                
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
                } else if(property.equals(ENTRY_CODE_PROPERTY) && scale.equals(ENTRY_CODE_SCALE)){
                	entryCodeFactor = importedFactor.getFactor();
                } else if(property.equals(SOURCE_PROPERTY) && scale.equals(SOURCE_SCALE)){
                	sourceFactor = importedFactor.getFactor();
                } else if(property.equals(CROSS_PROPERTY) && scale.equals(CROSS_SCALE)){
                	crossFactor = importedFactor.getFactor();
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
//	            System.out.println("DEBUG | Invalid file on readConstants header");
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
//	            System.out.println("DEBUG | Invalid file on readVariates header");
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