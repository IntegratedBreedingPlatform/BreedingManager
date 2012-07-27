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
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
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
    // private HibernateUtil hibernateUtil;

    @Autowired
    private ManagerFactory managerFactory;

    private TraitDataManager traitDataManager;

    public TraitQueries() {

    }

    public ArrayList<Trait> getTrait() throws InternationalizableException {
        try {
            int allTraitCount = traitDataManager.countAllTraits();
            return (ArrayList<Trait>) traitDataManager.getAllTraits(1, allTraitCount, Database.CENTRAL);
        } catch (QueryException e) {
            throw new InternationalizableException(e, Message.error_database, Message.error_in_counting_traits);
        }
    }

    public ArrayList<Scale> getScale(int traitID) {
        return (ArrayList<Scale>) traitDataManager.getScalesByTraitId(traitID);
    }

    public ArrayList<TraitMethod> getTraitMethod(int traitID) {
        return (ArrayList<TraitMethod>) traitDataManager.getTraitMethodsByTraitId(traitID);
    }

    public ArrayList<ScaleDiscrete> getScaleDiscreteValue(int scaleID) throws InternationalizableException {
        try {
            return (ArrayList<ScaleDiscrete>) traitDataManager.getDiscreteValuesOfScale(scaleID);
        } catch (QueryException e) {
            throw new InternationalizableException(e, Message.error_database, Message.error_in_getting_discrete_values_of_scale);
        }
    }

    public ArrayList<Integer> getGIDSByPhenotypicData() throws InternationalizableException {

        StudyDataManager studyDataManager = managerFactory.getStudyDataManager();

        NumericRange range = new NumericRange(new Double(2000), new Double(3000));
        TraitCombinationFilter combination = new TraitCombinationFilter(Integer.valueOf(1003), Integer.valueOf(9), Integer.valueOf(30),
                range);
        List<TraitCombinationFilter> filters = new ArrayList<TraitCombinationFilter>();
        filters.add(combination);

        try {
            return (ArrayList<Integer>) studyDataManager.getGIDSByPhenotypicData(filters, 0, 10, Database.CENTRAL);
        } catch (QueryException e) {
            throw new InternationalizableException(e, Message.error_database, Message.error_in_getting_germplasm_ids_by_phenotypic_data);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.traitDataManager = managerFactory.getTraitDataManager();
    }

}
