package org.generationcp.breeding.manager.listimport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.breeding.manager.listimport.exceptions.GermplasmImportException;
import org.generationcp.breeding.manager.listimport.exceptions.InvalidFileTypeImportException;
import org.generationcp.breeding.manager.pojos.ImportedCondition;
import org.generationcp.breeding.manager.pojos.ImportedConstant;
import org.generationcp.breeding.manager.pojos.ImportedFactor;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.breeding.manager.pojos.ImportedVariate;
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
import org.vaadin.easyuploads.FileFactory;

import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Validator.InvalidValueException;

@Configurable
public class GermplasmListUploader implements FileFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(GermplasmListUploader.class);
    private static final String TEMP_FILE_DIR = new File(
            System.getProperty("java.io.tmpdir")).getPath();

    
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
	public static final String DATE_AS_NUMBER_FORMAT = "yyyyMMdd";
    
    private String entryFactor;
    private String desigFactor;
    private String gidFactor;
    private String entryCodeFactor;
    private String crossFactor;
    private String sourceFactor;
    
//    private GermplasmImportFileComponent source;
    
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
    
    private Boolean importFileIsAdvanced;

    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    
    public String getOriginalFilename() {
        return originalFilename;
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


    public ImportedGermplasmList getImportedGermplasmList() {
        return importedGermplasmList;
    }


    public void validate() throws GermplasmImportException {
        currentSheet = 0;
        currentRow = 0;
        
        importedGermplasmList = null;
        
        try {
            inp = new FileInputStream(tempFileName);
            wb = new HSSFWorkbook(inp);
            
            Sheet sheet1 = wb.getSheetAt(0);
            
            if(sheet1 == null || sheet1.getSheetName() == null || !(sheet1.getSheetName().equals("Description"))){
            	throw new GermplasmImportException("File doesn't have the first sheet - Description");
            }
            
            Sheet sheet2 = wb.getSheetAt(1);
            
            if(sheet2 == null || sheet2.getSheetName() == null || !(sheet2.getSheetName().equals("Observation"))){
            	throw new InvalidValueException("File doesn't have second sheet - Observation");
            }
            
            readSheet1();
            readSheet2();

        } catch (FileNotFoundException e) {
        	//	"File not found"
        } catch (IOException e) {
            throwInvalidFileTypeError();
        } catch (ReadOnlyException e) {
            throwInvalidFileTypeError();
        } catch (IllegalArgumentException e){
        	throwInvalidFileTypeError();
        } catch (ConversionException e) {
            throwInvalidFileTypeError();
        }
    }


    private void readSheet1() throws GermplasmImportException{
        readGermplasmListFileInfo();
        readConditions();
        readFactors();
        readConstants();
        readVariates();
    }
    
    private void readSheet2() throws GermplasmImportException{
        currentSheet = 1;
        currentRow = 0;
                
        ImportedGermplasm importedGermplasm;
        Boolean entryColumnIsPresent = false;
        Boolean desigColumnIsPresent = false;
        Boolean gidColumnIsPresent = false;
    
        //Check if columns ENTRY and DESIG is present
        if(importedGermplasmList.getImportedFactors()!=null){
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
        }
        
        if(entryColumnIsPresent==false){
            throwInvalidFileError("ENTRY column missing from Observation sheet.");
        } else if(gidColumnIsPresent==false && desigColumnIsPresent==false){
            throwInvalidFileError("DESIGNATION column missing from Observation sheet.");
        } else if(gidFactor != null && !gidColumnIsPresent){
        	throwInvalidFileError("GID column missing from Observation sheet.");
        }
        
        //If still valid (after checking headers for ENTRY and DESIG), proceed
        currentRow = 1;
        
        while(!rowIsEmpty()){
            importedGermplasm = new ImportedGermplasm();
            for(int col=0;col<importedGermplasmList.getImportedFactors().size();col++){
            	//Map cell (given a column label) with a pojo setter
            	String columnHeader = getCellStringValue(currentSheet, 0, col, false);
            	if(columnHeader.equals(entryFactor)){
                	importedGermplasm.setEntryId(Integer.valueOf(getCellStringValue(currentSheet, currentRow, col, true)));
                } else if(columnHeader.equals(desigFactor)){
                    importedGermplasm.setDesig(getCellStringValue(currentSheet, currentRow, col, true));
                } else if(columnHeader.equals(gidFactor)){
                	String gidString = getCellStringValue(currentSheet, currentRow, col, true);
                	Integer gidInteger = null;
                	if(gidString != null && gidString.length() > 0){
                		gidInteger = Integer.valueOf(gidString);
                	} 
                	importedGermplasm.setGid(gidInteger);
                } else if(columnHeader.equals(crossFactor)){
                    importedGermplasm.setCross(getCellStringValue(currentSheet, currentRow, col, true));
                } else if(columnHeader.equals(sourceFactor)){
                    importedGermplasm.setSource(getCellStringValue(currentSheet, currentRow, col, true));
                } else if(columnHeader.equals(entryCodeFactor)){
                    importedGermplasm.setEntryCode(getCellStringValue(currentSheet, currentRow, col, true));
                } else {
                	
                }
            }
            
            //For cases where GID is preset and Desig is not present, or GID is not present and desig is present, or both are present
            
            //GID is given, but no DESIG, get value of DESIG given GID
            if(importedGermplasm.getGid()!=null && importedGermplasm.getDesig()==null){
            	try {

            		//Check if germplasm exists
			        Germplasm currentGermplasm = germplasmDataManager.getGermplasmByGID(importedGermplasm.getGid());
			        if(currentGermplasm==null){
			        	throwInvalidFileError("Germplasm with GID "+importedGermplasm.getGid()+" not found in database");
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
            	throwInvalidFileError("Row " + currentRow + " on Observation sheet of file doesn't have a GID or a DESIGNATION value.");
            }
            
            importedGermplasmList.addImportedGermplasm(importedGermplasm);
            currentRow++;
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
    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AS_NUMBER_FORMAT);
    			try{
    				if(value != null && value.length() > 0){
    					listDate = simpleDateFormat.parse(value);
    				} else{
    					listDate = null;
    				}
    			} catch(ParseException ex){
    				throwInvalidFileError("LIST DATE has wrong format. Please follow the format - yyyyMMdd.");
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
    		throwInvalidFileError(errorString.toString());
    		return;
    	}
    	
        if(listName != null && listName.length() > 0){
            try {
				Long matchingNamesCountOnLocal = germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL, Database.LOCAL);
				Long matchingNamesCountOnCentral = germplasmListManager.countGermplasmListByName(listName, Operation.EQUAL, Database.CENTRAL);
				
				if(matchingNamesCountOnLocal>0 || matchingNamesCountOnCentral>0){
					throwInvalidFileError("There is already an existing germplasm list with the name specified on the file");
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
					throwInvalidFileError("Invalid list type "+listType);
				}
			} catch (MiddlewareQueryException e1) {
				LOG.error("Error with getting germplasm list types.", e1);
			}
        }
        
        importedGermplasmList = new ImportedGermplasmList(originalFilename, listName, listTitle, listType, listDate); 
            	    
        //Prepare for next set of data
        while(!rowIsEmpty()){
            currentRow++;
        }
    }
    
    private void readConditions() throws GermplasmImportException{
        
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
	            throwInvalidFileError("Incorrect headers for conditions.");
	        }
	        
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

    private void readFactors() throws GermplasmImportException {
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
            throwInvalidFileError("Incorrect headers for factors.");
        }

        
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
        currentRow++;

        //If ENTRY or DESIG is not present on Factors, return error
        if(entryColumnIsPresent==false){
        	throwInvalidFileError("There is no ENTRY factor.");
        } else if(desigColumnIsPresent==false && gidColumnIsPresent==false){
            throwInvalidFileError("There is no DESIGNATION factor.");
        } 
	}


    private void readConstants() throws GermplasmImportException{
    	
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
	            throwInvalidFileError("Incorrect headers for constants.");
	        }
	        
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
	        currentRow++;
    	}
    }
    
    private void readVariates() throws GermplasmImportException{
    	
    	//Variates section is not required, do nothing if it's not there
    	if(getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("VARIATE")){
	        //Check if headers are correct
	        if(!getCellStringValue(currentSheet,currentRow,0,true).toUpperCase().equals("VARIATE")
	            || !getCellStringValue(currentSheet,currentRow,1,true).toUpperCase().equals("DESCRIPTION")
	            || !getCellStringValue(currentSheet,currentRow,2,true).toUpperCase().equals("PROPERTY")
	            || !getCellStringValue(currentSheet,currentRow,3,true).toUpperCase().equals("SCALE")
	            || !getCellStringValue(currentSheet,currentRow,4,true).toUpperCase().equals("METHOD")
	            || !getCellStringValue(currentSheet,currentRow,5,true).toUpperCase().equals("DATA TYPE")) {
	            throwInvalidFileError("Incorrect headers for variates.");
	        }
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
    
    private void throwInvalidFileError(String message) throws GermplasmImportException{
    	throw new GermplasmImportException(message);
    }
    
    private void throwInvalidFileTypeError() throws GermplasmImportException {
    	throw new InvalidFileTypeImportException("Please upload a properly formatted XLS file.");
    }    
    
    public Boolean importFileIsAdvanced(){
    	return importFileIsAdvanced;
    }

	@Override
	public File createFile(String fileName, String mimeType) {
		File f = new File(TEMP_FILE_DIR + fileName);
		tempFileName = f.getAbsolutePath();
		originalFilename = fileName;
        return f;
	}
};