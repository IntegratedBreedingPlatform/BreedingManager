
package org.generationcp.breeding.manager.service;

import java.util.List;

import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Project;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 9/26/2014 Time: 1:49 PM
 */
public interface BreedingManagerService {

	public List<GermplasmList> doGermplasmListSearch(String q, Operation o) throws BreedingManagerSearchException;

	public Project getCurrentProject();

	public void validateEmptySearchString(String q) throws BreedingManagerSearchException;
}
