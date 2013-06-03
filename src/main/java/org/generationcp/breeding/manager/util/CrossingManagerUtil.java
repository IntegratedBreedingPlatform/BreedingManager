package org.generationcp.breeding.manager.util;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.UserDefinedField;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;


public class CrossingManagerUtil{
	
	public static final String[] USER_DEF_FIELD_CROSS_NAME = {"CROSS NAME", "CROSSING NAME"};

    private GermplasmDataManager germplasmDataManager;


    public CrossingManagerUtil(GermplasmDataManager germplasmDataManager) {
	this.germplasmDataManager = germplasmDataManager;
    }
    
    /**
     * Sets Breeding Method of Germplasm based on status of parental lines
     * 
     * @param germplasmDataManager
     * @param germplasm
     * @param femaleGid
     * @param maleGid
     * @return
     * @throws MiddlewareQueryException
     */
    public static Germplasm setCrossingBreedingMethod(GermplasmDataManager germplasmDataManager, Germplasm germplasm,
    													Integer femaleGid, Integer maleGid) throws MiddlewareQueryException{
    	
    	CrossingManagerUtil util = new CrossingManagerUtil(germplasmDataManager);
    	return util.setCrossingBreedingMethod(germplasm, femaleGid, maleGid);
    }


    public Germplasm setCrossingBreedingMethod(Germplasm gc,Integer femaleGid, Integer maleGid) throws MiddlewareQueryException{

	Germplasm gf = germplasmDataManager.getGermplasmByGID(femaleGid); // germplasm female
	Germplasm gm = germplasmDataManager.getGermplasmByGID(maleGid); // germplasm male
	Germplasm gff = germplasmDataManager.getGermplasmByGID(gf.getGpid2()); // maternal male grand parent (daddy of female parent)
	Germplasm gfm =  germplasmDataManager.getGermplasmByGID(gf.getGpid1()); // maternal female grand parent (mommy of female parent)
	Germplasm gmf = germplasmDataManager.getGermplasmByGID(gm.getGpid1()); //  paternal female grand parent (mommy of male parent)
	Germplasm gmm =  germplasmDataManager.getGermplasmByGID(gm.getGpid2()); // paternal male grand parent (daddy of male parent)

	if(gf != null && gf.getGnpgs()<0)
	{
	    if(gm != null && gm.getGnpgs()<0)
	    {
		gc.setMethodId(101);
	    }
	    else
	    {
		if(gm != null && gm.getGnpgs()==1)
		{
		    gc.setMethodId(101);
		}
		else if(gm != null && gm.getGnpgs()==2)
		{
		    if((gmf != null && gmf.getGid()==gf.getGid()) || (gmm != null && gmm.getGid()==gf.getGid()))
		    {
			gc.setMethodId(107);
		    }
		    else
		    {
			gc.setMethodId(102);
		    }
		}
		else
		{
		    gc.setMethodId(106);
		}
	    }
	}
	else
	{
	    if(gm != null && gm.getGnpgs()<0)
	    {
		if(gf != null && gf.getGnpgs()==1)
		{
		    gc.setMethodId(101);
		}
		else if(gf != null && gf.getGnpgs()==2)
		{
		    if((gff != null && gff.getGid()==gm.getGid()) || (gfm != null && gfm.getGid()==gm.getGid()))
		    {
			gc.setMethodId(107);
		    }
		    else
		    {
			gc.setMethodId(102);
		    }
		}
		else
		{
		    gc.setMethodId(106);
		}
	    }
	    else
	    {
		if((gf != null && gf.getMethodId()==101) && (gm != null && gm.getMethodId()==101))
		{
		    gc.setMethodId(103);
		}
		else
		{
		    gc.setMethodId(106);
		}
	    }
	}

	if(gc.getMethodId() == null){
	    gc.setMethodId(101);
	}
	return gc;

    }
    
    public static String generateFemaleandMaleCrossName(String femaleName, String maleName){
    	return femaleName + "/" + maleName;
    }
    
    /**
     * Get the id for UserDefinedField of Germplasm Name type for Crossing Name
	 * (matches upper case of UserDefinedField either fCode or fName). Query is:
	 * <b>
	 *	SELECT fldno
     *	  FROM udflds
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
	 *	SELECT fldno
     *	  FROM udflds
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
                		messageSource.getMessage(Message.ERROR_IN_GETTING_CROSSING_NAME_TYPE), Notification.POSITION_CENTERED);
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
	 * 						If this is null, gets the field's caption as field name.
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
	 * 						If this is null, gets the field's caption as field name.
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
	 * 						If this is null, gets the field's caption as field name.
	 * 
	 */
	public static void showFieldIsRequiredMessage(Window window, SimpleResourceBundleMessageSource messageSource, String fieldName){
		
		assert fieldName != null; //either the field caption or fieldName param must be available
		assert messageSource != null;
		
		if (window != null){
			MessageNotifier.showError(window, MessageFormat.format(
					messageSource.getMessage(Message.ERROR_MUST_BE_SPECIFIED), fieldName), "", Notification.POSITION_CENTERED);
		}

	}
	
	


}
