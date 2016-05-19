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

package org.generationcp.breeding.manager.germplasm;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasm.containers.ListsForGermplasmQuery;
import org.generationcp.breeding.manager.germplasm.containers.ListsForGermplasmQueryFactory;
import org.generationcp.breeding.manager.germplasmlist.listeners.GermplasmListItemClickListener;
import org.generationcp.commons.constant.DefaultGermplasmStudyBrowserPath;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class GermplasmListComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(GermplasmListComponent.class);

	private final Integer gid;
	private final boolean fromUrl; // this is true if this component is created by accessing the Germplasm Details page directly from the
									// URL

	private Table listsTable;
	private Label noDataAvailableLabel;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private GermplasmListManager germplasmListManager;

	public GermplasmListComponent(Integer gid, boolean fromUrl) {
		this.gid = gid;
		this.fromUrl = fromUrl;
	}

	// Called by GermplasmListItemClickListener
	public void listItemClickAction(ItemClickEvent event, Integer listId) {
		Window mainWindow = event.getComponent().getWindow();

		Tool tool = null;
		try {
			tool = this.workbenchDataManager.getToolWithName(ToolName.GERMPLASM_LIST_BROWSER.toString());
		} catch (MiddlewareQueryException qe) {
			GermplasmListComponent.LOG.error("QueryException", qe);
		}

		ExternalResource listBrowserLink;
		if (tool == null) {
			listBrowserLink =
					new ExternalResource(WorkbenchAppPathResolver.getFullWebAddress(DefaultGermplasmStudyBrowserPath.LIST_BROWSER_LINK
							+ this.gid, "?restartApplication"));
		} else {
			listBrowserLink =
					new ExternalResource(
							WorkbenchAppPathResolver.getWorkbenchAppPath(tool, String.valueOf(this.gid), "?restartApplication"));
		}

		Window germplasmListWindow = new BaseSubWindow("Germplasm List Information - " + listId);

		VerticalLayout layoutForList = new VerticalLayout();
		layoutForList.setMargin(false);
		layoutForList.setWidth("640px");
		layoutForList.setHeight("560px");

		Embedded listInfoPage = new Embedded("", listBrowserLink);
		listInfoPage.setType(Embedded.TYPE_BROWSER);
		listInfoPage.setSizeFull();
		layoutForList.addComponent(listInfoPage);

		germplasmListWindow.setContent(layoutForList);
		germplasmListWindow.setWidth("645px");
		germplasmListWindow.setHeight("600px");
		germplasmListWindow.center();
		germplasmListWindow.setResizable(false);
		germplasmListWindow.setModal(true);

		mainWindow.addWindow(germplasmListWindow);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.initializeComponents();
		this.addListeners();
		this.layoutComponents();
	}

	private void initializeComponents() {
		ListsForGermplasmQueryFactory factory = new ListsForGermplasmQueryFactory(this.germplasmListManager, this.gid);
		LazyQueryContainer container = new LazyQueryContainer(factory, false, 50);

		// add the column ids to the LazyQueryContainer tells the container the columns to display for the Table
		container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_ID, String.class, null);
		container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_NAME, String.class, null);
		container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_DATE, String.class, null);
		container.addContainerProperty(ListsForGermplasmQuery.GERMPLASMLIST_DESCRIPTION, String.class, null);
		container.getQueryView().getItem(0); // initialize the first batch of data to be displayed

		if (container.size() > 0) {
			this.listsTable = new Table();
			this.listsTable.setWidth("90%");
			this.listsTable.setContainerDataSource(container);

			if (container.size() < 10) {
				this.listsTable.setPageLength(container.size());
			} else {
				this.listsTable.setPageLength(10);
			}

			this.listsTable.setSelectable(true);
			this.listsTable.setMultiSelect(false);
			this.listsTable.setImmediate(true); // react at once when something is selected turn on column reordering and collapsing
			this.listsTable.setColumnReorderingAllowed(true);
			this.listsTable.setColumnCollapsingAllowed(true);

			this.listsTable.setColumnHeader(ListsForGermplasmQuery.GERMPLASMLIST_ID, this.messageSource.getMessage(Message.ID_HEADER));
			this.listsTable.setColumnHeader(ListsForGermplasmQuery.GERMPLASMLIST_NAME, this.messageSource.getMessage(Message.NAME_HEADER));
			this.listsTable.setColumnHeader(ListsForGermplasmQuery.GERMPLASMLIST_DATE, this.messageSource.getMessage(Message.DATE_HEADER));
			this.listsTable.setColumnHeader(ListsForGermplasmQuery.GERMPLASMLIST_DESCRIPTION,
					this.messageSource.getMessage(Message.DESCRIPTION_HEADER));
			this.listsTable.setVisibleColumns(new String[] {(String) ListsForGermplasmQuery.GERMPLASMLIST_NAME,
					(String) ListsForGermplasmQuery.GERMPLASMLIST_DATE, (String) ListsForGermplasmQuery.GERMPLASMLIST_DESCRIPTION});
		} else {
			this.noDataAvailableLabel = new Label("There is no Lists Information for this germplasm.");
		}
	}

	private void addListeners() {
		if (!this.fromUrl && this.listsTable != null) {
			this.listsTable.addListener(new GermplasmListItemClickListener(this));
		}
	}

	private void layoutComponents() {
		if (this.listsTable != null) {
			this.addComponent(this.listsTable);
		} else {
			this.addComponent(this.noDataAvailableLabel);
		}
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {

	}

}
