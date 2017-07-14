
package org.generationcp.breeding.manager.util;

/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dellroad.stuff.vaadin.ContextApplication;
import org.generationcp.breeding.manager.exception.BreedingManagerException;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListTreeUtil;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.commons.util.ContextUtil;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class Util {

	private static final Logger LOG = LoggerFactory.getLogger(Util.class);

	public static final String USER_HOME = "user.home";

	public static final String LOCATION_MANAGER_TOOL_NAME = "locationmanager";
	public static final String LOCATION_MANAGER_DEFAULT_URL = "/ibpworkbench/content/ProgramLocations?programId=";

	public static final String METHOD_MANAGER_TOOL_NAME = "methodmanager";
	public static final String METHOD_MANAGER_DEFAULT_URL = "/ibpworkbench/content/ProgramMethods?programId=";

	private Util() {
		// do nothing
	}

	public static boolean isTabExist(TabSheet tabSheet, String tabCaption) {

		int countTabSheet = tabSheet.getComponentCount();
		for (int i = 0; i < countTabSheet; i++) {
			Tab tab = tabSheet.getTab(i);
			if (tab.getCaption().equals(tabCaption)) {
				return true;
			}
		}
		return false;

	}

	public static boolean isTabDescriptionExist(TabSheet tabSheet, String tabDescription) {

		int countTabSheet = tabSheet.getComponentCount();
		for (int i = 0; i < countTabSheet; i++) {
			Tab tab = tabSheet.getTab(i);

			String currentTabDescription = tab.getDescription();
			if (currentTabDescription != null && currentTabDescription.equals(tabDescription)) {
				return true;
			}
		}
		return false;

	}

	public static boolean isAccordionDatasetExist(Accordion accordion, String accordionCaption) {
		int countAccordionTab = accordion.getComponentCount();

		for (int i = 0; i < countAccordionTab; i++) {
			Tab tab = accordion.getTab(i);
			if (tab.getCaption().equals(accordionCaption)) {
				return true;
			}
		}

		return false;
	}

	public static Tab getTabAlreadyExist(TabSheet tabSheet, String tabCaption) {

		for (int i = 0; i < tabSheet.getComponentCount(); i++) {
			Tab tab = tabSheet.getTab(i);
			if (tab.getCaption().equals(tabCaption)) {
				return tab;
			}
		}
		return null;

	}

	public static Tab getTabWithDescription(TabSheet tabSheet, String tabDescription) {

		for (int i = 0; i < tabSheet.getComponentCount(); i++) {
			Tab tab = tabSheet.getTab(i);
			String description = tab.getDescription();
			if (description != null && description.equals(tabDescription)) {
				return tab;
			}
		}
		return null;

	}

	public static Tab getTabToFocus(TabSheet tabSheet, String tabCaption) {
		Tab tabToFocus = tabSheet.getTab(0);
		boolean rightTab = false;
		for (int i = 0; i < tabSheet.getComponentCount(); i++) {
			Tab tab = tabSheet.getTab(i);
			if (rightTab) {
				tabToFocus = tab;
				return tabToFocus;
			}
			if (tab.getCaption().equals(tabCaption)) {
				if (i == tabSheet.getComponentCount() - 1) {
					return tabToFocus;
				} else {
					rightTab = true;
				}
			}

			tabToFocus = tab;
		}
		return null;

	}

	public static void closeAllTab(TabSheet tabSheet) {

		for (int i = tabSheet.getComponentCount() - 1; i >= 0; i--) {
			tabSheet.removeTab(tabSheet.getTab(i));
		}

	}

	/**
	 * Validates if an existing path is a directory
	 * 
	 * @param path
	 * @return true if the given path is a directory
	 */
	public static boolean isDirectory(String path) {
		boolean isValid = true;
		File f = new File(path);
		if (!f.exists()) {
			isValid = false;
		} else if (!f.isDirectory()) {
			// The path is not a directory (it is a file)
			isValid = false;
		}
		return isValid;
	}

	/**
	 * Gets the desktop directory path or the base directory of the application
	 *
	 * @param application
	 * @return file pointing to desktop or application path
	 * 
	 */
	public static File getDefaultBrowseDirectory(Application application) throws BreedingManagerException {

		// Initially gets the Desktop path of the user
		String desktopPath = System.getProperty(Util.USER_HOME) + File.separator + "Desktop";
		File file = new File(desktopPath);

		// If desktop path is inaccessible, get the applicaton's base directory
		if (!Util.isDirectory(desktopPath) || file == null || !file.canRead() || file.getAbsolutePath() == null) {
			file = application.getContext().getBaseDirectory();
		}

		if (file != null && file.canRead() && file.getAbsolutePath() != null) {
			return file;
		} else {
			throw new BreedingManagerException("No valid default directories found");
		}

	}

	/**
	 * Gets the directory based on the given path string
	 * 
	 * @param path
	 * @return file pointing to the path
	 * @throws BreedingManagerException
	 */
	public static File getDefaultBrowseDirectory(String path) throws BreedingManagerException {
		File file = new File(path);

		if (file != null && Util.isDirectory(path) && file.canRead() && file.getAbsolutePath() != null) {
			return file;
		} else {
			throw new BreedingManagerException("Invalid path");
		}

	}

	/**
	 * Gets one directory up the tree
	 * 
	 * @param path
	 * @return
	 */
	public static String getOneFolderUp(String path) {
		String newPath = path;

		if (path != null && path.length() > 0) {
			try {
				newPath = path.substring(0, path.lastIndexOf(File.separator));
			} catch (StringIndexOutOfBoundsException e) {
				Util.LOG.error(e.getMessage(), e);
				newPath = "";
			}
		}

		// already at the root directory
		if ("".equalsIgnoreCase(newPath)) {
			newPath = File.separator;
		}

		return newPath;

	}

	/**
	 * Opens and attaches a modal window containing the location manager
	 * 
	 * @param workbenchDataManager - workbenchDataManager, this is used by this method to get tool URL (if available)
	 * @param programId - used to load the locations for the given programId
	 * @param window - modal window will be attached to this window
	 * @return
	 */
	public static Window launchLocationManager(WorkbenchDataManager workbenchDataManager, Long programId, Window window, String caption) {

		Tool tool = null;
		try {
			tool = workbenchDataManager.getToolWithName(Util.LOCATION_MANAGER_TOOL_NAME);
		} catch (MiddlewareQueryException qe) {
			Util.LOG.error("QueryException", qe);
		}

		ExternalResource listBrowserLink = null;
		if (tool == null) {
			listBrowserLink = new ExternalResource(Util.LOCATION_MANAGER_DEFAULT_URL + programId);
		} else {
			listBrowserLink = new ExternalResource(tool.getPath() + programId);
		}

		VerticalLayout layout = new VerticalLayout();
		layout.setDebugId("layout");
		layout.setMargin(false);
		layout.setSpacing(false);
		layout.setSizeFull();

		Embedded listInfoPage = new Embedded("", listBrowserLink);
		listInfoPage.setDebugId("listInfoPage");
		listInfoPage.setType(Embedded.TYPE_BROWSER);
		listInfoPage.setSizeFull();

		layout.addComponent(listInfoPage);

		Window popupWindow = new BaseSubWindow();
		popupWindow.setWidth("95%");
		popupWindow.setHeight("97%");
		popupWindow.setModal(true);
		popupWindow.setResizable(false);
		popupWindow.center();
		popupWindow.setCaption(caption);
		popupWindow.setContent(layout);
		popupWindow.addStyleName(Reindeer.WINDOW_LIGHT);

		window.addWindow(popupWindow);

		return popupWindow;
	 }

	 /**
	  * Opens and attaches a modal window containing the method manager
	  * 
	 * @param workbenchDataManager - workbenchDataManager, this is used by this method to get tool URL (if available)
	  * @param programId - used to load the locations for the given programId
	  * @param window - modal window will be attached to this window
	  * @return
	  */
	 public static Window launchMethodManager(WorkbenchDataManager workbenchDataManager, Long programId, Window window, String caption) {

		Tool tool = null;
		try {
			tool = workbenchDataManager.getToolWithName(Util.METHOD_MANAGER_TOOL_NAME);
		} catch (MiddlewareQueryException qe) {
			Util.LOG.error("QueryException", qe);
		}

		ExternalResource listBrowserLink = null;
		if (tool == null) {
			listBrowserLink = new ExternalResource(Util.METHOD_MANAGER_DEFAULT_URL + programId);
		} else {
			listBrowserLink = new ExternalResource(tool.getPath() + programId);
		}

		VerticalLayout layout = new VerticalLayout();
		layout.setDebugId("layout");
		layout.setMargin(false);
		layout.setSpacing(false);
		layout.setSizeFull();

		Embedded listInfoPage = new Embedded("", listBrowserLink);
		listInfoPage.setDebugId("listInfoPage");
		listInfoPage.setType(Embedded.TYPE_BROWSER);
		listInfoPage.setSizeFull();

		layout.addComponent(listInfoPage);

		Window popupWindow = new BaseSubWindow();
		popupWindow.setWidth("95%");
		popupWindow.setHeight("97%");
		popupWindow.setModal(true);
		popupWindow.setResizable(false);
		popupWindow.center();
		popupWindow.setCaption(caption);
		popupWindow.setContent(layout);
		popupWindow.addStyleName(Reindeer.WINDOW_LIGHT);

		window.addWindow(popupWindow);

		return popupWindow;
	 }

	/**
	  * Generates a string concatenation of full path of a folder eg. output "Program Lists > Folder 1 > Sub Folder 1 >"
	 *
	  * where "Sub Folder 1" is the name of the folder
	  *
	  * @param germplasmListManager
	  * @param folder
	  * @return
	  * @throws MiddlewareQueryException
	  */
	 public static String generateListFolderPathLabel(GermplasmListManager germplasmListManager, GermplasmList folder)
			throws MiddlewareQueryException {

		Deque<GermplasmList> parentFolders = new ArrayDeque<>();
		GermplasmListTreeUtil.traverseParentsOfList(germplasmListManager, folder, parentFolders);

		StringBuilder locationFolderString = new StringBuilder();
		locationFolderString.append("Program Lists");

		while (!parentFolders.isEmpty()) {
			locationFolderString.append(" > ");
			GermplasmList parentFolder = parentFolders.pop();
			locationFolderString.append(parentFolder.getName());
		}

		if (folder != null) {
			locationFolderString.append(" > ");
			locationFolderString.append(folder.getName());
		}

		String returnString = locationFolderString.toString();
		if (folder != null && folder.getName().length() >= 47) {
			returnString = folder.getName().substring(0, 47);

		} else if (locationFolderString.length() > 47) {
			int lengthOfFolderName = folder.getName().length();
			returnString = locationFolderString.substring(0, 47 - lengthOfFolderName - 6) + "... > " + folder.getName();
		}

		returnString += " > ";

		return returnString;
	 }

	public static Map<Integer, GermplasmList> getGermplasmLists(GermplasmListManager germplasmListManager, List<Integer> germplasmListIds) {
		 Map<Integer, GermplasmList> germplasmListsMap = new HashMap<>();
		 List<GermplasmList> lists = new ArrayList<>();

		try {
			 lists = germplasmListManager.getAllGermplasmLists(0, Integer.MAX_VALUE);
			 for (GermplasmList list : lists) {
				 Integer listId = list.getId();
				 if (germplasmListIds.contains(listId)) {
					 germplasmListsMap.put(listId, list);
				 }
			 }
		 } catch (MiddlewareQueryException e) {
			 Util.LOG.error("Error retrieving all germplasm list", e);
		 }
		 return germplasmListsMap;
	 }

	public static String getAdditionalParams(WorkbenchDataManager workbenchDataManager) {
		String addtlParams = "";

		try {
			Long projectId = ContextUtil.getProjectInContext(workbenchDataManager, ContextApplication.currentRequest()).getProjectId();
			Integer userId = ContextUtil.getCurrentWorkbenchUserId(workbenchDataManager, ContextApplication.currentRequest());
			String authenticationTokenString =
				ContextUtil.addQueryParameter(ContextConstants.PARAM_AUTH_TOKEN, SecurityUtil.getEncodedToken());

			addtlParams = ContextUtil.getContextParameterString(userId, projectId) + authenticationTokenString;
		 } catch (MiddlewareQueryException e) {
			 Util.LOG.error("Error occured while resolving context parameters: " + e.getMessage(), e);
		 }
		return addtlParams;
	 }

}
