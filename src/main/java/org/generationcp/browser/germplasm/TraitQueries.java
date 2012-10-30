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

package org.generationcp.browser.germplasm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.generationcp.middleware.pojos.NumericRange;
import org.generationcp.middleware.pojos.Scale;
import org.generationcp.middleware.pojos.ScaleDiscrete;
import org.generationcp.middleware.pojos.Trait;
import org.generationcp.middleware.pojos.TraitCombinationFilter;
import org.generationcp.middleware.pojos.TraitMethod;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class TraitQueries implements Serializable, InitializingBean{

    private ArrayList<Trait> trait;
    private ArrayList<Scale> scale;
    private ArrayList<TraitMethod> traitMethod;
    private ArrayList<ScaleDiscrete> scaleDiscreteValue;

    private static final long serialVersionUID = 1L;

    @Autowired
    private TraitDataManager traitDataManager;
    
    @Autowired
    private StudyDataManager studyDataManager;

    public TraitQueries() {

    }

    public ArrayList<Trait> getTrait() throws InternationalizableException {
        try {
            long allTraitCount = traitDataManager.countAllTraits();
            return (ArrayList<Trait>) traitDataManager.getAllTraits(1, (int) allTraitCount, Database.CENTRAL);
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_COUNTING_TRAITS);
        }
    }

    public ArrayList<Scale> getScale(int traitID) throws InternationalizableException {
        try {
            return (ArrayList<Scale>) traitDataManager.getScalesByTraitId(traitID);
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_SCALES_BY_TRAIT_ID);
        }
    }

    public ArrayList<TraitMethod> getTraitMethod(int traitID)  throws InternationalizableException {
        try {
            return (ArrayList<TraitMethod>) traitDataManager.getTraitMethodsByTraitId(traitID);
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_TRAIT_METHOD);
        }
    }

    public ArrayList<ScaleDiscrete> getScaleDiscreteValue(int scaleID) throws InternationalizableException {
        try {
            return (ArrayList<ScaleDiscrete>) traitDataManager.getDiscreteValuesOfScale(scaleID);
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_DISCRETE_VALUES_OF_SCALE);
        }
    }

    public ArrayList<Integer> getGIDSByPhenotypicData() throws InternationalizableException {
        NumericRange range = new NumericRange(new Double(2000), new Double(3000));
        TraitCombinationFilter combination = new TraitCombinationFilter(Integer.valueOf(1003), Integer.valueOf(9), Integer.valueOf(30),
                range);
        List<TraitCombinationFilter> filters = new ArrayList<TraitCombinationFilter>();
        filters.add(combination);

        try {
            return (ArrayList<Integer>) studyDataManager.getGIDSByPhenotypicData(filters, 0, 10, Database.CENTRAL);
        } catch (MiddlewareQueryException e) {
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_GERMPLASM_IDS_BY_PHENO_DATA);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

}
