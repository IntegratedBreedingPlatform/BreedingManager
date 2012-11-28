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

package org.generationcp.browser.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.exception.GermplasmStudyBrowserException;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.Application;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;

public class Util{
    
    @Autowired
    private static SimpleResourceBundleMessageSource messageSource;


    public static boolean isTabExist(TabSheet tabSheet, String tabCaption) {

        int countTabSheet = tabSheet.getComponentCount();

        for (int i = 0; i < countTabSheet; i++) {
            Tab tab = tabSheet.getTab(i);
            if (tab.getCaption().equals(tabCaption)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAccordionDatasetExist(Accordion accordion, String accordionCaption) {
        int countAccordionTab = accordion.getComponentCount();

        for (int i = 0; i < countAccordionTab; i++) {
            Tab tab = accordion.getTab(i);
            if (tab.getCaption().equals(accordionCaption)) {
                return true;
            }
        }

        return false;
    }

    public static Tab getTabAlreadyExist(TabSheet tabSheet, String tabCaption) {

        for (int i = 0; i < tabSheet.getComponentCount(); i++) {
            Tab tab = tabSheet.getTab(i);
            if (tab.getCaption().equals(tabCaption)) {
                return tab;
            }
        }
        return null;

    }
    
    public static void closeAllTab(TabSheet tabSheet){
    	
    	 for (int i =  tabSheet.getComponentCount()-1; i >=0; i--) {
            tabSheet.removeTab(tabSheet.getTab(i));
          } 
    	
    }


    /**
     * Validates if an existing path is a directory
     * @param path
     * @return true if the given path is a directory
     */
    public static boolean isDirectory(String path){
        boolean isValid = true;
        File f = new File(path);
        if (!f.exists()) {  // The directory does not exist
            isValid = false;
        } else if (!f.isDirectory()) { // The path is not a directory (it is a file)
            isValid = false;
        }        
        return isValid;
    }


    /**
     * Gets the desktop directory path or the base directory of the application
     *
     * @param application
     * @return file pointing to desktop or application path
     * 
     */
    public static File getDefaultBrowseDirectory(Application application) throws GermplasmStudyBrowserException{

        // Initially gets the Desktop path of the user
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";// + File.separator ;
        File file = new File(desktopPath);
        
        // If desktop path is inaccessible, get the applicaton's base directory
        if (!Util.isDirectory(desktopPath) ||
            ((file == null) || (!file.canRead())
                    || (file.getAbsolutePath() == null))){
            file = application.getContext().getBaseDirectory();
        }
        
        if ((file != null) && (file.canRead())
                && (file.getAbsolutePath() != null)) {
            return file;
        } else {
            throw new GermplasmStudyBrowserException("No valid default directories found");
        }

    }

    /**
     * Gets the directory based on the given path string
     * 
     * @param path
     * @return file pointing to the path
     * @throws GermplasmStudyBrowserException
     */
    public static File getDefaultBrowseDirectory(String path) throws GermplasmStudyBrowserException{
        File file = new File(path);
        
        if ((file != null) && (Util.isDirectory(path)) && (file.canRead())
                && (file.getAbsolutePath() != null)) {
            return file;
        } else {
            throw new GermplasmStudyBrowserException("Invalid path");
        }

    }

    
    /**
     * Gets one directory up the tree
     * 
     * @param path
     * @return
     */
    public static String getOneFolderUp(String path){
        String newPath = path;
        
        if (path != null && path.length() > 0){
            newPath = path.substring(0, path.lastIndexOf(File.separator));
        }
            
        if (newPath.equals("")) { // already at the root directory
                newPath = File.separator; 
        }
                
        return newPath;
        
    }
    

    public static Integer getCurrentDate(){
        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dateNowStr = formatter.format(now.getTime());
        Integer dateNowInt = Integer.valueOf(dateNowStr);
        return dateNowInt;

    }
    

    /**
     * Returns in format "yyyyMMdd"
     * 
     * @param time - the date in Integer format
     * @return
     */
    public static Integer getIBPDate(Integer time){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dateStr = formatter.format(time);
        Integer dateInt = Integer.valueOf(dateStr);
        return dateInt;
    }
    
    /**
     * Returns in format "yyyyMMdd"
     * 
     * @param time - the date in long format
     * @return
     */
    public static Integer getIBPDate(long time){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dateStr = formatter.format(time);
        Integer dateInt = Integer.valueOf(dateStr);
        return dateInt;
    }

    /** 
     * Returns in format "yyyyMMdd"
     * 
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static Integer getIBPDate(int year, int month, int day) throws InvalidDateException{
        validateDate(year, month, day);
        return Integer.valueOf(year * 10000 + month * 100 + day);

    }
    
    /**
     * Checks if a given date is valid.
     * 
     * @param year
     * @param month
     * @param day
     * @return
     * @throws InvalidDateException
     */
    public static boolean validateDate(int year, int month, int day) throws InvalidDateException{
        if (month < 0 || month > 12) {
            throw new InvalidDateException(messageSource.getMessage(Message.ERROR_MONTH_OUT_OF_RANGE)); //"Month out of range"        
        }
        if (month == 2){
           if (isLeapYear(year)){
               if (day < 0 || day > 29){
                   throw new InvalidDateException(messageSource.getMessage(Message.ERROR_DAY_OUT_OF_RANGE)); //"Day out of range"
               }
           } else {
               if (day < 0 || day > 28){
                   throw new InvalidDateException(messageSource.getMessage(Message.ERROR_DAY_OUT_OF_RANGE));
               }               
           }
        } else if (((month == 4 || month == 6 || month == 9 || month == 11) && (day > 30))  || (day < 0 || day > 31)){
            throw new InvalidDateException(messageSource.getMessage(Message.ERROR_DAY_OUT_OF_RANGE));                    
        }
        return true;
                
    }
    
    /**
     * Checks if the given year is a leap year.
     * 
     * @param year
     * @return
     */
    public static boolean isLeapYear(int year){
        boolean isLeapYear = false;
        if (year % 400 == 0) {
            isLeapYear = true;
        } else if (year % 100 == 0) {
            isLeapYear = false;
        } else if (year % 4 == 0 ) {
            isLeapYear = true;
        } else {
            isLeapYear = false;
        }
        return isLeapYear;
    }    

}
