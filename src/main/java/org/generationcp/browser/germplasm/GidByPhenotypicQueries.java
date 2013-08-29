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
import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.manager.api.StudyDataManager;
//import org.generationcp.middleware.pojos.TraitCombinationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The Class GidByPhenotypicQueries.
 */
@Configurable
public class GidByPhenotypicQueries implements InitializingBean, Serializable{

    private static final Logger LOG = LoggerFactory.getLogger(GidByPhenotypicQueries.class);
    private static final long serialVersionUID = 1L;

    /** The StudyDataManager. */
    @Autowired
    private StudyDataManagerImpl studyDataManager;

    /**
     * Instantiates a new GidByPhenotypicQueries.
     *
     * @throws ConfigException the ConfigException
     */
    public GidByPhenotypicQueries() throws ConfigException {
        
    }

    /**
     * Gets the gIDS by phenotypic data.
     *
     * @param filters the filters
     * @return the gIDS by phenotypic data
     */
//    public ArrayList<Integer> getGIDSByPhenotypicData(List<TraitCombinationFilter> filters) throws InternationalizableException{
//        ArrayList<Integer> results = null;
//        try {
//            results = (ArrayList<Integer>) managerStudy.getGIDSByPhenotypicData(filters, 0, 100, Database.CENTRAL);
//        } catch (MiddlewareQueryException e) {
//            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_GERMPLASM_IDS_BY_PHENO_DATA);
//        }
//        return results;
//
//    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
