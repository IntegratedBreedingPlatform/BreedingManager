package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainer;
import org.generationcp.breeding.manager.crossingmanager.CrossesMadeContainerUpdateListener;
import org.generationcp.breeding.manager.crossingmanager.GenerateCrossNameAction;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.util.CrossingManagerUtil;
import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.BreedingMethodSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class ApplyCrossingSettingAction implements
		CrossesMadeContainerUpdateListener {
	
	private final static Logger LOG = LoggerFactory.getLogger(ApplyCrossingSettingAction.class);
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	private CrossingManagerSetting setting;
	private CrossesMadeContainer container;
	
	public ApplyCrossingSettingAction(CrossingManagerSetting setting){
		this.setting = setting;
	}

	@Override
	public boolean updateCrossesMadeContainer(CrossesMadeContainer container) {
		this.container = container;
		
		return applyBreedingMethodSetting() && applyNameSetting() && applyAdditionalDetailsSetting();
	}

	/**
	 * Set breeding method of germplasm based on configuration in setting.
	 * Can be same for all crosses or based on status of parental lines
	 * 
	 * @return
	 */
	private boolean applyBreedingMethodSetting() {
		BreedingMethodSetting methodSetting = setting.getBreedingMethodSetting();
		
		if (container != null && container.getCrossesMade() != null && 
	                container.getCrossesMade().getCrossesMap()!= null){
	            
            //Use same breeding method for all crosses
            if (!methodSetting.isBasedOnStatusOfParentalLines()){
                Integer breedingMethodSelected = methodSetting.getMethodId();
                for (Germplasm germplasm : container.getCrossesMade().getCrossesMap().keySet()){
                    germplasm.setMethodId(breedingMethodSelected);
                }
            
            // Use CrossingManagerUtil to set breeding method based on parents    
            } else {
                for (Germplasm germplasm : container.getCrossesMade().getCrossesMap().keySet()){
                    Integer femaleGid = germplasm.getGpid1();
                    Integer maleGid = germplasm.getGpid2();
                    
                    try {
                    	Germplasm female = germplasmDataManager.getGermplasmByGID(femaleGid);
                    	Germplasm male = germplasmDataManager.getGermplasmByGID(maleGid);
                    	
                    	Germplasm motherOfFemale = null;
                    	Germplasm fatherOfFemale = null;
                    	if(female != null){
                    		motherOfFemale = germplasmDataManager.getGermplasmByGID(female.getGpid1());
                    		fatherOfFemale = germplasmDataManager.getGermplasmByGID(female.getGpid2());
                    	}
                    	
                    	Germplasm motherOfMale = null;
                    	Germplasm fatherOfMale = null;
                    	if(male != null){
                    		motherOfMale = germplasmDataManager.getGermplasmByGID(male.getGpid1());
                    		fatherOfMale = germplasmDataManager.getGermplasmByGID(male.getGpid2());
                    	}
                    	CrossingManagerUtil.setCrossingBreedingMethod(germplasm, female, male, motherOfFemale, fatherOfFemale, motherOfMale, fatherOfMale);	
                    
                    } catch (MiddlewareQueryException e) {
                        LOG.error(e.toString() + "\n" + e.getStackTrace());
                        e.printStackTrace();
                        return false;
                    }
                
                }
            }
            return true;
            
        }
        
        return false;
	}

	/**
	 * Generate values for NAME record plus Germplasm List Entry designation
	 * based on cross name setting configuration
	 * 
	 * @return
	 */
	private boolean applyNameSetting(){
		CrossNameSetting nameSetting = setting.getCrossNameSetting();
		
		if (this.container != null && this.container.getCrossesMade() != null && 
                this.container.getCrossesMade().getCrossesMap()!= null) { 
            
			GenerateCrossNameAction generateNameAction = new GenerateCrossNameAction();
            int ctr = 1; 
            try {
				ctr = generateNameAction.getNextNumberInSequence(nameSetting);
			} catch (MiddlewareQueryException e) {
				 LOG.error(e.toString() + "\n" + e.getStackTrace());
                 e.printStackTrace();
                 return false;
			}
			
            Map<Germplasm, Name> crossesMap = this.container.getCrossesMade().getCrossesMap();
            List<GermplasmListEntry> oldCrossNames = new ArrayList<GermplasmListEntry>();
            
            // Store old cross name and generate new names based on prefix, suffix specifications
            for (Map.Entry<Germplasm, Name> entry : crossesMap.entrySet()){
                Name nameObject = entry.getValue();
                String oldCrossName = nameObject.getNval();
                String nextName = generateNameAction.buildNextNameInSequence(ctr++);
				nameObject.setNval(nextName);
                
                Germplasm germplasm = entry.getKey();
                Integer tempGid = germplasm.getGid();
                GermplasmListEntry oldNameEntry = new GermplasmListEntry(tempGid, tempGid, tempGid, oldCrossName);
                
                oldCrossNames.add(oldNameEntry);
            }
            // Only store the "original" cross names, would not store previous names on 2nd, 3rd, ... change
            if (this.container.getCrossesMade().getOldCrossNames()== null ||
                    this.container.getCrossesMade().getOldCrossNames().isEmpty()){
                this.container.getCrossesMade().setOldCrossNames(oldCrossNames);                
            }
            
            return true;
                
        }
        
        return false;
		
	}
	
	/**
	 * Set GERMPLSM location id and gdate and NAME location id and ndate
	 * based on harvest date and location information given in setting
	 * 
	 * @return
	 */
	private boolean applyAdditionalDetailsSetting(){
        AdditionalDetailsSetting detailsSetting =  setting.getAdditionalDetailsSetting();    
	    if (this.container != null && this.container.getCrossesMade() != null && 
            this.container.getCrossesMade().getCrossesMap()!= null) {
	    	
            Integer harvestLocationId = 0;
            
            if(detailsSetting.getHarvestLocationId() != null){
                harvestLocationId = detailsSetting.getHarvestLocationId();
            }
        
            Map<Germplasm, Name> crossesMap = container.getCrossesMade().getCrossesMap();
	        for (Map.Entry<Germplasm, Name> entry : crossesMap.entrySet()){
	            Germplasm germplasm = entry.getKey();
	            germplasm.setLocationId(harvestLocationId);
	            
	            Name name = entry.getValue();
	            name.setLocationId(harvestLocationId);
	        }
	        return true;
	    }
	    
        return false;
	}
}
