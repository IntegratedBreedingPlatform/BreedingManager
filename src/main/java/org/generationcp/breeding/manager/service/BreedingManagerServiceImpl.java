
package org.generationcp.breeding.manager.service;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 9/26/2014 Time: 1:50 PM
 */
@Configurable
public class BreedingManagerServiceImpl implements BreedingManagerService {

	private static final Logger LOG = LoggerFactory.getLogger(BreedingManagerServiceImpl.class);

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public List<GermplasmList> doGermplasmListSearch(final String q, final Operation o) throws BreedingManagerSearchException {
		this.validateEmptySearchString(q);

		try {
			final List<GermplasmList> results =
					this.germplasmListManager.searchForGermplasmList(q, this.contextUtil.getCurrentProgramUUID(), o);

			if (null == results || results.isEmpty()) {
				throw new BreedingManagerSearchException(Message.NO_SEARCH_RESULTS);
			}

			return results;

		} catch (final MiddlewareQueryException e) {
			BreedingManagerServiceImpl.LOG.error(e.getMessage(), e);
			throw new BreedingManagerSearchException(Message.ERROR_DATABASE, e);
		}
	}

	@Override
	public void validateEmptySearchString(final String q) throws BreedingManagerSearchException {
		if ("".equals(q.replaceAll(" ", "").trim())) {
			throw new BreedingManagerSearchException(Message.SEARCH_QUERY_CANNOT_BE_EMPTY);
		}
	}

	public GermplasmDataManager getGermplasmDataManager() {
		return this.germplasmDataManager;
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	@Override
	public Project getCurrentProject() {
		return this.contextUtil.getProjectInContext();
	}

}
