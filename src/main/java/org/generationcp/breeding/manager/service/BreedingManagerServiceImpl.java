package org.generationcp.breeding.manager.service;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
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

	@Autowired
	private UserDataManager userDataManager;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public String getOwnerListName(final Integer userId) {
		final String username = "";

		try {
			if (userId != null) {
				return this.computeListName(this.userDataManager.getUserById(userId));
			}

		} catch (final MiddlewareQueryException ex) {
			BreedingManagerServiceImpl.LOG.error("Error with getting list owner name of user with id: " + userId, ex);
			throw ex;
		}

		return username;
	}

	protected String computeListName(final User user) {
		String userName = "";
		if (user != null) {
			final int personId = user.getPersonid();
			final Person p = this.userDataManager.getPersonById(personId);

			if (p != null) {
				userName = p.getFirstName() + " " + p.getMiddleName() + " " + p.getLastName();
			} else {
				userName = user.getName();
			}
		}

		return userName;
	}

	@Override
	public String getDefaultOwnerListName() {
		try {
			final int currentUser = this.contextUtil.getCurrentUserLocalId();

			return this.computeListName(this.userDataManager.getUserById(currentUser));
		} catch (final MiddlewareQueryException e) {
			BreedingManagerServiceImpl.LOG.error("Error with getting list owner name of default user ", e);
			throw e;
		}
	}

	@Override
	public List<Germplasm> doGermplasmSearch(final String q, final Operation o, final boolean includeParents,
			final boolean withInventoryOnly, final boolean includeMGMembers) throws BreedingManagerSearchException {
		this.validateEmptySearchString(q);
		try {
			final List<Germplasm> results = this.germplasmDataManager.searchForGermplasm(q, o, includeParents, withInventoryOnly, includeMGMembers);

			if (null == results || results.isEmpty()) {
				throw new BreedingManagerSearchException(Message.NO_SEARCH_RESULTS);
			}

			return results;

		} catch (final MiddlewareQueryException e) {
			BreedingManagerServiceImpl.LOG.error(e.getMessage(), e);
			throw new BreedingManagerSearchException(Message.ERROR_IN_SEARCH, e);
		}
	}

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

	protected void validateEmptySearchString(final String q) throws BreedingManagerSearchException {
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
