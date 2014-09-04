package org.generationcp.breeding.manager.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.generationcp.middleware.pojos.dms.ProgramFavorite.FavoriteType;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;


public class BreedingManagerUtil{
    
    public static final String[] USER_DEF_FIELD_CROSS_NAME = {"CROSS NAME", "CROSSING NAME"};

    /**
     * Get the id for UserDefinedField of Germplasm Name type for Crossing Name
     * (matches upper case of UserDefinedField either fCode or fName). Query is:
     * <b>
     *    SELECT fldno
     *      FROM udflds
     *   WHERE UPPER(fname) IN ('CROSSING NAME', 'CROSS NAME')
     *      OR UPPER(fcode) IN ('CROSSING NAME', 'CROSS NAME');
     * </b>
     * 
     * @param germplasmListManager
     * @return
     * @throws MiddlewareQueryException 
     */
    public static Integer getIDForUserDefinedFieldCrossingName(GermplasmListManager germplasmListManager) throws MiddlewareQueryException  {
            
        List<UserDefinedField> nameTypes = germplasmListManager.getGermplasmNameTypes();
        for (UserDefinedField type : nameTypes){
            for (String crossNameValue : USER_DEF_FIELD_CROSS_NAME){
                if (crossNameValue.equals(type.getFcode().toUpperCase()) || 
                        crossNameValue.equals(type.getFname().toUpperCase())){
                    return type.getFldno();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get the id for UserDefinedField of Germplasm Name type for Crossing Name
     * (matches upper case of UserDefinedField either fCode or fName). Query is:
     * <b>
     *    SELECT fldno
     *      FROM udflds
     *   WHERE UPPER(fname) IN ('CROSSING NAME', 'CROSS NAME')
     *      OR UPPER(fcode) IN ('CROSSING NAME', 'CROSS NAME');
     * </b>
     * If any error occurs, shows error message in passed in Window instance
     * @param germplasmListManager - instance of GermplasmListManager
     * @param window - window where error message will be shown
     * @param messageSource - resource bundle where the error message will be retrieved from
     * @return
     */
    public static Integer getIDForUserDefinedFieldCrossingName(GermplasmListManager germplasmListManager, 
            Window window, SimpleResourceBundleMessageSource messageSource){
        
        try {
            
            return getIDForUserDefinedFieldCrossingName(germplasmListManager);
        
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
            
            if (window != null && messageSource != null){
                MessageNotifier.showError(window, 
                        messageSource.getMessage(Message.ERROR_DATABASE),
                        messageSource.getMessage(Message.ERROR_IN_GETTING_CROSSING_NAME_TYPE));
            }
        }
        
        return null;
    }
    
    /**
     * Displays an error in window if field's value is null: "<fieldName> must be specified."
     * 
     * @param window - window where error message will be shown
     * @param field - field to check if null. If null, displays warning.
     * @param messageSource - resource bundle where the error message will be retrieved from
     * @param fieldName - name of the required Field which will appear in error message. 
     *                         If this is null, gets the field's caption as field name.
     * @return false if field is null. Else, return true;
     */
    public static boolean validateRequiredField(Window window, AbstractField field, 
            SimpleResourceBundleMessageSource messageSource, String fieldName){
        
        assert field.getCaption() !=null || fieldName != null; //either the field caption or fieldName param must be available
        
        if (window != null && field.getValue() == null){
            showFieldIsRequiredMessage(window, messageSource, fieldName != null ? fieldName : field.getCaption());
            return false;
        }

        return true;
    }
    
    /**
     * Displays an error in window if field's string value is null: "<fieldName> must be specified."
     * 
     * @param window - window where error message will be shown
     * @param field - field to check if empty string. If empty, display warning
     * @param messageSource - resource bundle where the error message will be retrieved from
     * @param fieldName - name of the required Field which will appear in error message. 
     *                         If this is null, gets the field's caption as field name.
     * @return false if field is null. Else, return true;
     */
    public static boolean validateRequiredStringField(Window window, AbstractField field,
            SimpleResourceBundleMessageSource messageSource, String fieldName){
        
        if (validateRequiredField(window, field, messageSource, fieldName)){
            if (StringUtils.isEmpty(((String) field.getValue()).trim())){
                showFieldIsRequiredMessage(window, messageSource, fieldName != null ? fieldName : field.getCaption());
                return false;
            }
            return true;
        }

        return false;
    }
    
    /**
     * Displays a error in window: "<fieldName> must be specified."
     * 
     * @param window - window where error message will be shown
     * @param field - field to check if null. If null, displays warning.
     * @param messageSource - resource bundle where the error message will be retrieved from
     * @param fieldName - name of the required Field which will appear in error message. 
     *                         If this is null, gets the field's caption as field name.
     * 
     */
    public static void showFieldIsRequiredMessage(Window window, SimpleResourceBundleMessageSource messageSource, String fieldName){
        
        assert fieldName != null; //either the field caption or fieldName param must be available
        assert messageSource != null;
        
        if (window != null){
            MessageNotifier.showError(window, MessageFormat.format(
                    messageSource.getMessage(Message.ERROR_MUST_BE_SPECIFIED), fieldName), "");
        }

    }
    
    /**
     * Queries for program's favorite locations and sets the values to combobox and map
     * 
     * @param workbenchDataManager
     * @param germplasmDataManager
     * @param locationComboBox
     * @param mapLocation
     * @param locationType
     * @throws MiddlewareQueryException
     */
    @SuppressWarnings("deprecation")
	public static void populateWithFavoriteLocations(WorkbenchDataManager workbenchDataManager, GermplasmDataManager germplasmDataManager, 
    		ComboBox locationComboBox, Map<String, Integer> mapLocation, Integer locationType) throws MiddlewareQueryException {
    	
    	locationComboBox.removeAllItems();
    	
        List<Integer> favoriteLocationIds = new ArrayList<Integer>();
        List<Location> favoriteLocations = new ArrayList<Location>();
         
		
        //Get location Id's
        List<ProgramFavorite> list = germplasmDataManager.getProgramFavorites(FavoriteType.LOCATION, 1000);
		for (ProgramFavorite f : list){
			favoriteLocationIds.add(f.getEntityId());
		}
        
        //Get locations
        favoriteLocations = germplasmDataManager.getLocationsByIDs(favoriteLocationIds);
	        

		for(Location favoriteLocation : favoriteLocations){
			if((locationType > 0) && (favoriteLocation.getLtype().equals(locationType)) || (locationType == 0)){
				Integer locId = favoriteLocation.getLocid();
				locationComboBox.addItem(locId);
				locationComboBox.setItemCaption(locId, favoriteLocation.getLname());
				if (mapLocation != null){
					mapLocation.put(favoriteLocation.getLname(), new Integer(locId));
				}
			}
		}
    }
    
    
    /**
     * Queries for program's favorite locations and sets the values to combobox and map
     * 
     * @param workbenchDataManager
     * @param germplasmDataManager
     * @param locationComboBox
     * @param mapLocation
     * @throws MiddlewareQueryException
     */
	public static void populateWithFavoriteLocations(WorkbenchDataManager workbenchDataManager, GermplasmDataManager germplasmDataManager, 
    		ComboBox locationComboBox, Map<String, Integer> mapLocation) throws MiddlewareQueryException {
    	populateWithFavoriteLocations(workbenchDataManager, germplasmDataManager, locationComboBox, mapLocation, 0);		
    }
    
    /**
     * Queries for program's favorite locations and sets the values to combobox and map.  Only selects locations with ltype = 410, 411, or 412
     * 
     * @param workbenchDataManager
     * @param germplasmDataManager
     * @param locationComboBox
     * @param mapLocation
     * @throws MiddlewareQueryException
     */
    @SuppressWarnings("deprecation")
	public static void populateWithFavoriteBreedingLocations(WorkbenchDataManager workbenchDataManager, GermplasmDataManager germplasmDataManager, 
    		ComboBox locationComboBox, Map<String, Integer> mapLocation) throws MiddlewareQueryException {
    	
    	locationComboBox.removeAllItems();
    	
        List<Integer> favoriteLocationIds = new ArrayList<Integer>();
        List<Location> favoriteLocations = new ArrayList<Location>();
        
        //Get location Id's
        List<ProgramFavorite> list = germplasmDataManager.getProgramFavorites(FavoriteType.LOCATION, 1000);
		for (ProgramFavorite f : list){
			favoriteLocationIds.add(f.getEntityId());
		}
        
        //Get locations
        favoriteLocations = germplasmDataManager.getLocationsByIDs(favoriteLocationIds);
	        

		for(Location favoriteLocation : favoriteLocations){
			if(favoriteLocation.getLtype() != null && (favoriteLocation.getLtype().equals(Integer.valueOf(410))
					|| favoriteLocation.getLtype().equals(Integer.valueOf(411))
					|| favoriteLocation.getLtype().equals(Integer.valueOf(412)))){
				Integer locId = favoriteLocation.getLocid();
				locationComboBox.addItem(locId);
				locationComboBox.setItemCaption(locId, favoriteLocation.getLname());
				if (mapLocation != null){
					mapLocation.put(favoriteLocation.getLname(), new Integer(locId));
				}
			}
		}
		
    }
    
    
    /**
     * Queries for program's favorite locations and sets the values to combobox and map
     * 
     * @param workbenchDataManager
     * @param germplasmDataManager
     * @param locationComboBox
     * @param mapLocation
     * @throws MiddlewareQueryException
     */
    public static void populateWithFavoriteMethods(WorkbenchDataManager workbenchDataManager, GermplasmDataManager germplasmDataManager, 
    		ComboBox methodComboBox, Map<String, Integer> mapMethods) throws MiddlewareQueryException {
    	
		methodComboBox.removeAllItems();
    	
        List<Integer> favoriteMethodIds = new ArrayList<Integer>();
        List<Method> favoriteMethods = new ArrayList<Method>();
         
		try {
			
			List<ProgramFavorite> list = germplasmDataManager.getProgramFavorites(FavoriteType.METHOD, 1000);
			for (ProgramFavorite f : list){
				favoriteMethodIds.add(f.getEntityId());
			}
	        
	        //Get Methods
	        if (!favoriteMethodIds.isEmpty()){
	        	favoriteMethods = germplasmDataManager.getMethodsByIDs(favoriteMethodIds);
	        }
	        
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}

		for(Method favoriteMethod : favoriteMethods){
			Integer methodId = favoriteMethod.getMid();
			methodComboBox.addItem(methodId);
			methodComboBox.setItemCaption(methodId, favoriteMethod.getMname());
			if (mapMethods != null){
				mapMethods.put(favoriteMethod.getMname(), methodId);
			}
		}
		
    }
    
    /**
     * Queries for program's favorite locations and sets the values to combobox and map.  Only selects method with GEN type.
     * 
     * @param workbenchDataManager
     * @param germplasmDataManager
     * @param locationComboBox
     * @param mapLocation
     * @throws MiddlewareQueryException
     */
    public static void populateWithFavoriteMethodsOfTypeGen(WorkbenchDataManager workbenchDataManager, GermplasmDataManager germplasmDataManager, 
    		ComboBox methodComboBox, Map<String, Integer> mapMethods) throws MiddlewareQueryException {
    	
		methodComboBox.removeAllItems();
    	
        List<Integer> favoriteMethodIds = new ArrayList<Integer>();
        List<Method> favoriteMethods = new ArrayList<Method>();
         
		try {

			List<ProgramFavorite> list = germplasmDataManager.getProgramFavorites(FavoriteType.METHOD, 1000);
			for (ProgramFavorite f : list){
				favoriteMethodIds.add(f.getEntityId());
			}
	        
	        //Get Methods
	        if (!favoriteMethodIds.isEmpty()){
	        	favoriteMethods = germplasmDataManager.getMethodsByIDs(favoriteMethodIds);
	        }
	        
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}

		for(Method favoriteMethod : favoriteMethods){
			if(favoriteMethod.getMtype() != null && favoriteMethod.getMtype().equals("GEN")){
				Integer methodId = favoriteMethod.getMid();
				methodComboBox.addItem(methodId);
				methodComboBox.setItemCaption(methodId, favoriteMethod.getMname());
				if (mapMethods != null){
					mapMethods.put(favoriteMethod.getMname(), methodId);
				}
			}
		}
		
    }

}
