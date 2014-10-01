package org.generationcp.breeding.manager.service;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 9/26/2014
 * Time: 1:49 PM
 */
public interface BreedingManagerService {

	public String getOwnerListName(Integer userId) throws MiddlewareQueryException;

	public String getDefaultOwnerListName() throws MiddlewareQueryException;

	public Integer getCurrentUserLocalId() throws MiddlewareQueryException;
}