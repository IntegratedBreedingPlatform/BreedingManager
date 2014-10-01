package org.generationcp.breeding.manager.service;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 9/26/2014
 * Time: 1:50 PM
 */
@Configurable
public class BreedingManagerServiceImpl implements BreedingManagerService {

	private static final Logger LOG = LoggerFactory.getLogger(BreedingManagerServiceImpl.class);

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private UserDataManager userDataManager;

	@Override
	public String getOwnerListName(Integer userId) throws MiddlewareQueryException {
		String username = "";

		try {
			if (userId != null) {
				return computeListName(userDataManager.getUserById(userId));
			}

		} catch (MiddlewareQueryException ex) {
			LOG.error("Error with getting list owner name of user with id: " + userId, ex);
			throw ex;
		}

		return username;
	}

	protected String computeListName(User user) throws MiddlewareQueryException {
		String userName = "";
		if (user != null) {
			int personId = user.getPersonid();
			Person p = userDataManager.getPersonById(personId);

			if (p != null) {
				userName = p.getFirstName() + " " + p.getMiddleName() + " " + p.getLastName();
			} else {
				userName = user.getName();
			}
		}

		return userName;
	}

	@Override
	public String getDefaultOwnerListName() throws MiddlewareQueryException{
		try {
			int currentUser = getCurrentUserLocalId();

			return computeListName(userDataManager.getUserById(currentUser));
		} catch (MiddlewareQueryException e) {
			LOG.error("Error with getting list owner name of default user ", e);
						throw e;
		}
	}

	@Override
	public Integer getCurrentUserLocalId() throws MiddlewareQueryException {
		Integer workbenchUserId = workbenchDataManager.getWorkbenchRuntimeData().getUserId();
		Project lastProject = workbenchDataManager.getLastOpenedProject(workbenchUserId);
		Integer localIbdbUserId = workbenchDataManager
				.getLocalIbdbUserId(workbenchUserId, lastProject.getProjectId());
		if (localIbdbUserId != null) {
			return localIbdbUserId;
		} else {
			return -1;
		}
	}

	public void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setUserDataManager(UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}
}