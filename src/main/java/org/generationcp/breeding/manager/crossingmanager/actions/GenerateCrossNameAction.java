package org.generationcp.breeding.manager.crossingmanager.actions;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Utility class for generating next cross name / number given
 * CrossNameSetting object
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class GenerateCrossNameAction {
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	private CrossNameSetting setting;
	
	private Integer nextNumberInSequence = 1;
	
	/**
	 * Returns the generated next name in sequence with the given
	 * CrossNameSetting parameters
	 * 
	 * @param setting
	 * @return
	 * @throws MiddlewareQueryException
	 */
	public String getNextNameInSequence(CrossNameSetting setting) throws MiddlewareQueryException{
		this.setting = setting;
        this.nextNumberInSequence = getNextNumberInSequence(setting);
        
        return buildNextNameInSequence(nextNumberInSequence);
	}
	
	/**
	 * Returns the generated next number in sequence with the given
	 * CrossNameSetting parameters
	 * 
	 * @param setting
	 * @return
	 * @throws MiddlewareQueryException
	 */
	public Integer getNextNumberInSequence(CrossNameSetting setting) throws MiddlewareQueryException{
		this.setting = setting;
        String lastPrefixUsed = buildPrefixString();
        this.nextNumberInSequence = 1;
        
        Integer startNumber = setting.getStartNumber();
        if (startNumber != null && startNumber > 0){
        	nextNumberInSequence = startNumber;
        } else {
        	String nextSequenceNumberString = this.germplasmDataManager.getNextSequenceNumberForCrossName(lastPrefixUsed.toUpperCase().trim());
        	nextNumberInSequence = Integer.parseInt(nextSequenceNumberString);
        }
        
        return nextNumberInSequence;
        
	}
	
	private String buildPrefixString(){
		String prefix = setting.getPrefix().trim();
        if(setting.isAddSpaceBetweenPrefixAndCode()){
            return prefix + " ";
        }
        return prefix;
    }
	
	private String buildSuffixString(){
		String suffix = setting.getSuffix().trim();
        if(setting.isAddSpaceBetweenSuffixAndCode()){
            return " " + suffix ;
        }
        return suffix;
    }

    public String buildNextNameInSequence(Integer number) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildPrefixString());
        sb.append(getNumberWithLeadingZeroesAsString(number));
        if (!StringUtils.isEmpty(setting.getSuffix())){
            sb.append(buildSuffixString());
        }
        return sb.toString();
    }
    
    private String getNumberWithLeadingZeroesAsString(Integer number){
        StringBuilder sb = new StringBuilder();
        String numberString = number.toString();
        Integer numOfDigits = setting.getNumOfDigits();
        
        if (numOfDigits != null && numOfDigits > 0){
        	int numOfZerosNeeded = numOfDigits - numberString.length();
        	if(numOfZerosNeeded > 0){
        		for (int i = 0; i < numOfZerosNeeded; i++){
        			sb.append("0");
        		}
        	}
        	
        }
        sb.append(number);
        return sb.toString();
    }
    

}
