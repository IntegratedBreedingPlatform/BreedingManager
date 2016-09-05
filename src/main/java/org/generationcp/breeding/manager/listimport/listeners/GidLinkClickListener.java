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

package org.generationcp.breeding.manager.listimport.listeners;

import org.generationcp.breeding.manager.util.Util;
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

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GidLinkClickListener implements Button.ClickListener, ItemClickListener {

	private static final long serialVersionUID = -6751894969990825730L;
	private final static Logger LOG = LoggerFactory.getLogger(GidLinkClickListener.class);
	public static final String GERMPLASM_IMPORT_WINDOW_NAME = "germplasm-import";

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	private String gid;
	private final Boolean viaToolURL;
	private final Window parentWindow;

	public GidLinkClickListener() {
		this.gid = null;
		this.viaToolURL = false;
		this.parentWindow = null;
	}

	public GidLinkClickListener(final String gid, final Boolean viaToolURL) {
		this.gid = gid;
		this.viaToolURL = viaToolURL;
		this.parentWindow = null;
	}

	public GidLinkClickListener(final String gid, final Window parentWindow) {
		this.gid = gid;
		this.viaToolURL = false;
		this.parentWindow = parentWindow;
	}

	@Override
	public void buttonClick(final ClickEvent event) {
		this.openDetailsWindow(event.getComponent());
	}

	@Override
	public void itemClick(final ItemClickEvent event) {
		this.gid = ((Integer) event.getItemId()).toString();
		this.openDetailsWindow(event.getComponent());

	}

	private void openDetailsWindow(final Component component) {
		Window mainWindow;

		if (this.parentWindow != null) {
			mainWindow = this.parentWindow;
		} else if (this.viaToolURL) {
			mainWindow = component.getWindow();
		} else {
			mainWindow = component.getApplication().getWindow(GidLinkClickListener.GERMPLASM_IMPORT_WINDOW_NAME);
		}

		Tool tool = null;
		try {
			tool = this.workbenchDataManager.getToolWithName(ToolName.GERMPLASM_BROWSER.toString());
		} catch (MiddlewareQueryException qe) {
			GidLinkClickListener.LOG.error("QueryException", qe);
		}
		String addtlParams = Util.getAdditionalParams(this.workbenchDataManager);

		ExternalResource germplasmBrowserLink = null;
		if (tool == null) {
			germplasmBrowserLink =
					new ExternalResource(WorkbenchAppPathResolver.getFullWebAddress(DefaultGermplasmStudyBrowserPath.GERMPLASM_BROWSER_LINK
							+ this.gid, "?restartApplication" + addtlParams));
		} else {
			germplasmBrowserLink =
					new ExternalResource(WorkbenchAppPathResolver.getWorkbenchAppPath(tool, this.gid, "?restartApplication" + addtlParams));
		}

		String preferredName = null;
		try {
			preferredName = this.germplasmDataManager.getPreferredNameValueByGID(Integer.valueOf(this.gid));
		} catch (MiddlewareQueryException ex) {
			GidLinkClickListener.LOG.error("Error with getting preferred name of " + this.gid, ex);
		}

		String windowTitle = "Germplasm Details: " + "(GID: " + this.gid + ")";
		if (preferredName != null) {
			windowTitle = "Germplasm Details: " + preferredName + " (GID: " + this.gid + ")";
		}
		Window germplasmWindow = new BaseSubWindow(windowTitle);

		VerticalLayout layoutForGermplasm = new VerticalLayout();
		layoutForGermplasm.setDebugId("layoutForGermplasm");
		layoutForGermplasm.setMargin(false);
		layoutForGermplasm.setWidth("100%");
		layoutForGermplasm.setHeight("100%");
		layoutForGermplasm.addStyleName("no-caption");

		Embedded germplasmInfo = new Embedded("", germplasmBrowserLink);
		germplasmInfo.setDebugId("germplasmInfo");
		germplasmInfo.setType(Embedded.TYPE_BROWSER);
		germplasmInfo.setSizeFull();

		layoutForGermplasm.addComponent(germplasmInfo);
		germplasmWindow.setContent(layoutForGermplasm);
		germplasmWindow.setWidth("90%");
		germplasmWindow.setHeight("90%");
		germplasmWindow.center();
		germplasmWindow.setResizable(false);

		germplasmWindow.setModal(true);
		germplasmWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		germplasmWindow.setCloseShortcut(ShortcutAction.KeyCode.ESCAPE);
		mainWindow.addWindow(germplasmWindow);
	}
}
