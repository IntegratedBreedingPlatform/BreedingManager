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

import org.generationcp.middleware.exceptions.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The Class GidByPhenotypicQueries.
 */
@Configurable
public class GidByPhenotypicQueries implements InitializingBean, Serializable{

    @SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(GidByPhenotypicQueries.class);
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new GidByPhenotypicQueries.
     *
     * @throws ConfigException the ConfigException
     */
    public GidByPhenotypicQueries() throws ConfigException {
        
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
