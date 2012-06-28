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

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.TraitCombinationFilter;

public class GidByPhenotypicQueries{

    private StudyDataManager managerStudy;
    @SuppressWarnings("unused")
	private ManagerFactory factory;

    public GidByPhenotypicQueries(ManagerFactory factory, StudyDataManager managerStudy) throws ConfigException {
        this.factory = factory;
        this.managerStudy = factory.getStudyDataManager();
    }

    public ArrayList<Integer> getGIDSByPhenotypicData(List<TraitCombinationFilter> filters) {
        ArrayList<Integer> results = null;
        try {

            results = (ArrayList<Integer>) managerStudy.getGIDSByPhenotypicData(filters, 0, 100, Database.CENTRAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;

    }
}
