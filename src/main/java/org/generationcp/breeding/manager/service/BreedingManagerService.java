package org.generationcp.breeding.manager.service;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Project;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 9/26/2014
 * Time: 1:49 PM
 */
public interface BreedingManagerService {

	public String getOwnerListName(Integer userId) throws MiddlewareQueryException;

	public String getDefaultOwnerListName() throws MiddlewareQueryException;

    public List<GermplasmList> doGermplasmListSearch(String q, Operation o) throws BreedingManagerSearchException;

    public List<Germplasm> doGermplasmSearch(String q, Operation o, boolean includeParents) throws BreedingManagerSearchException;
    
    public Project getCurrentProject() throws MiddlewareQueryException;
}