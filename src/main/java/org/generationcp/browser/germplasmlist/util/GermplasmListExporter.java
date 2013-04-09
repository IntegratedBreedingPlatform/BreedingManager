package org.generationcp.browser.germplasmlist.util;

import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.generationcp.middleware.manager.api.GermplasmListManager;


public class GermplasmListExporter {

    private static final int conditionListHeaderRowIndex = 8;
    
    private GermplasmListManager germplasmListManager;
    private Integer listId;
    
    public GermplasmListExporter(GermplasmListManager germplasmListManager, Integer germplasmListId) {
        this.germplasmListManager = germplasmListManager;
        this.listId = germplasmListId;
    }
    
    public FileOutputStream exportGermplasmListExcel(String filename) throws GermplasmListExporterException {
        
        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFSheet sheet = wb.createSheet();

        HSSFRow row1 = sheet.createRow(0); 
        HSSFCell row1cell1 = row1.createCell(0); 
        row1cell1.setCellValue("First Name"); 
        HSSFCell row1cell2 = row1.createCell(1); 
        row1cell2.setCellValue("Last Name");

        HSSFRow row2 = sheet.createRow(1); 
        HSSFCell row2cell1 = row2.createCell(0); 
        row2cell1.setCellValue("Mang"); 
        HSSFCell row2cell2 = row2.createCell(1); 
        row2cell2.setCellValue("Donald");

        HSSFRow row3 = sheet.createRow(2); 
        HSSFCell row3cell1 = row3.createCell(0); 
        row3cell1.setCellValue("Kenny"); 
                  HSSFCell row3cell2 = row3.createCell(1); 
        row3cell2.setCellValue("Inasal");
        
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
