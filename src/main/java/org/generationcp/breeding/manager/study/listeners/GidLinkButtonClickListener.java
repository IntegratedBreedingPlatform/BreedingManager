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

package org.generationcp.breeding.manager.study.listeners;

import org.apache.commons.lang3.ArrayUtils;
import org.generationcp.breeding.manager.cross.study.adapted.dialogs.ViewTraitObservationsDialog;
import org.generationcp.breeding.manager.study.TableViewerComponent;
import org.generationcp.commons.constant.DefaultGermplasmStudyBrowserPath;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GidLinkButtonClickListener implements Button.ClickListener {

	private static final long serialVersionUID = -6751894969990825730L;
	private final static Logger LOG = LoggerFactory.getLogger(GidLinkButtonClickListener.class);
	private final String[] CHILD_WINDOWS = {TableViewerComponent.TABLE_VIEWER_WINDOW_NAME,
			ViewTraitObservationsDialog.LINE_BY_TRAIT_WINDOW_NAME};

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private final String gid;

	public GidLinkButtonClickListener(String gid) {
		this.gid = gid;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Window mainWindow;
		Window eventWindow = event.getComponent().getWindow();
		if (ArrayUtils.contains(this.CHILD_WINDOWS, eventWindow.getName())) {
			mainWindow = eventWindow.getParent();
		} else {
			mainWindow = eventWindow;
		}

		Tool tool = null;
		try {
			tool = this.workbenchDataManager.getToolWithName(ToolName.GERMPLASM_BROWSER.toString());
		} catch (MiddlewareQueryException qe) {
			GidLinkButtonClickListener.LOG.error("QueryException", qe);
		}

		ExternalResource germplasmBrowserLink;
		if (tool == null) {
			germplasmBrowserLink =
					new ExternalResource(WorkbenchAppPathResolver.getFullWebAddress(DefaultGermplasmStudyBrowserPath.GERMPLASM_BROWSER_LINK
							+ this.gid));
		} else {
			germplasmBrowserLink = new ExternalResource(WorkbenchAppPathResolver.getWorkbenchAppPath(tool, this.gid));
		}

		String preferredName = null;
		try {
			preferredName = this.germplasmDataManager.getPreferredNameValueByGID(Integer.valueOf(this.gid));
		} catch (MiddlewareQueryException ex) {
			GidLinkButtonClickListener.LOG.error("Error with getting preferred name of " + this.gid, ex);
		}

		String windowTitle = "Germplasm Details: " + "(GID: " + this.gid + ")";
		if (preferredName != null) {
			windowTitle = "Germplasm Details: " + preferredName + " (GID: " + this.gid + ")";
		}
		Window germplasmWindow = new BaseSubWindow(windowTitle);

		VerticalLayout layoutForGermplasm = new VerticalLayout();
		layoutForGermplasm.setMargin(false);
		layoutForGermplasm.setWidth("98%");
		layoutForGermplasm.setHeight("98%");

		Embedded germplasmInfo = new Embedded("", germplasmBrowserLink);
		germplasmInfo.setType(Embedded.TYPE_BROWSER);
		germplasmInfo.setSizeFull();
		layoutForGermplasm.addComponent(germplasmInfo);

		germplasmWindow.setContent(layoutForGermplasm);

		// Instead of setting by percentage, compute it
		germplasmWindow.setWidth(Integer.valueOf((int) Math.round(mainWindow.getWidth() * .90)) + "px");
		germplasmWindow.setHeight(Integer.valueOf((int) Math.round(mainWindow.getHeight() * .90)) + "px");

		germplasmWindow.center();
		germplasmWindow.setResizable(false);
		germplasmWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		germplasmWindow.setModal(true);

		mainWindow.addWindow(germplasmWindow);
	}

}
