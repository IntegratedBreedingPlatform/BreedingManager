
package org.generationcp.breeding.manager.crossingmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listmanager.util.GermplasmListExporter;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class CrossesSummaryListDataComponent extends VerticalLayout implements BreedingManagerLayout, InitializingBean {

	private static final String CLICK_TO_VIEW_CROSS_INFORMATION = "Click to view Cross information";
	private static final String CLICK_TO_VIEW_FEMALE_INFORMATION = "Click to view Female Parent information";
	private static final String CLICK_TO_VIEW_MALE_INFORMATION = "Click to view Male Parent information";

	private static final long serialVersionUID = -6058352152291932651L;

	private static final Logger LOG = LoggerFactory.getLogger(CrossesSummaryListDataComponent.class);

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private GermplasmListExporter exporter;

	private Label listEntriesLabel;

	private Table listDataTable;
	private Button toolsButton;
	private Button viewHeaderButton;

	private Long count;

	// Menu for tools button
	private ContextMenu menu;
	private ContextMenuItem menuExportList;

	private ViewListHeaderWindow viewListHeaderWindow;

	@Autowired
	private OntologyDataManager ontologyDataManager;
	
	@Autowired
	private UserDataManager userDataManager;


	private final GermplasmList list;

	private List<GermplasmListData> listEntries;
	private Map<Integer, Germplasm> germplasmMap;
	// list data id, CrossParents info
	private Map<Integer, CrossParents> parentsInfo;

	// Used maps to make use of existing Middleware methods
	// gid of parent, preferred name
	private Map<Integer, String> parentGermplasmNames;
	// Gid, Method of germplasm
	private Map<Integer, Object> methodMap;

	public CrossesSummaryListDataComponent(final GermplasmList list) {
		this.list = list;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.listEntriesLabel = new Label(this.messageSource.getMessage(Message.CROSS_LIST_ENTRIES).toUpperCase());
		this.listEntriesLabel.setDebugId("listEntriesLabel");
		this.listEntriesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		this.listEntriesLabel.addStyleName(AppConstants.CssStyles.BOLD);
		this.listEntriesLabel.setWidth("180px");

		this.viewListHeaderWindow = new ViewListHeaderWindow(this.list, BreedingManagerUtil.getAllNamesAsMap(userDataManager),
				germplasmListManager.getGermplasmListTypes());

		this.viewHeaderButton = new Button(this.messageSource.getMessage(Message.VIEW_HEADER));
		this.viewHeaderButton.setDebugId("viewHeaderButton");
		this.viewHeaderButton.addStyleName(BaseTheme.BUTTON_LINK);
		this.viewHeaderButton.setDescription(this.viewListHeaderWindow.getListHeaderComponent().toString());
		this.viewHeaderButton.setHeight("14px");

		this.initializeListEntriesTable();

		this.toolsButton = new ActionButton();
		this.toolsButton.setDebugId("toolsButton");

		this.menu = new ContextMenu();
		this.menu.setDebugId("menu");
		this.menu.setWidth("200px");
		this.menu.setVisible(true);

		// Generate menu items
		this.menuExportList = this.menu.addItem(this.messageSource.getMessage(Message.EXPORT_CROSS_LIST));
		this.menuExportList.setVisible(true);

	}

	@Override
	public void initializeValues() {
		this.retrieveGermplasmsInformation();
		this.populateTable();
	}

	private void populateTable() {
		this.parentsInfo = new HashMap<>();

		for (final GermplasmListData entry : this.listEntries) {
			final Integer gid = entry.getGid();
			final String gidString = String.format("%s", gid.toString());

			final Button gidButton =
					this.generateLaunchGermplasmDetailsButton(gidString, gidString,
							CrossesSummaryListDataComponent.CLICK_TO_VIEW_CROSS_INFORMATION);
			final Button desigButton =
					this.generateLaunchGermplasmDetailsButton(entry.getDesignation(), gidString,
							CrossesSummaryListDataComponent.CLICK_TO_VIEW_CROSS_INFORMATION);

			final Germplasm germplasm = this.germplasmMap.get(gid);
			final Integer femaleGid = germplasm.getGpid1();
			final String femaleGidString = femaleGid.toString();
			final Button femaleGidButton =
					this.generateLaunchGermplasmDetailsButton(femaleGidString, femaleGidString,
							CrossesSummaryListDataComponent.CLICK_TO_VIEW_FEMALE_INFORMATION);
			final String femaleDesig = this.parentGermplasmNames.get(femaleGid);
			final Button femaleDesigButton =
					this.generateLaunchGermplasmDetailsButton(femaleDesig, femaleGidString,
							CrossesSummaryListDataComponent.CLICK_TO_VIEW_FEMALE_INFORMATION);

			final Integer maleGid = germplasm.getGpid2();
			final String maleGidString = maleGid.toString();
			final Button maleGidButton =
					this.generateLaunchGermplasmDetailsButton(maleGidString, maleGidString,
							CrossesSummaryListDataComponent.CLICK_TO_VIEW_MALE_INFORMATION);
			final String maleDesig = this.parentGermplasmNames.get(maleGid);
			final Button maleDesigButton =
					this.generateLaunchGermplasmDetailsButton(maleDesig, maleGidString,
							CrossesSummaryListDataComponent.CLICK_TO_VIEW_MALE_INFORMATION);

			final Method method = (Method) this.methodMap.get(gid);

			this.listDataTable.addItem(
					new Object[] {entry.getEntryId(), desigButton, entry.getGroupName(), entry.getEntryCode(), gidButton,
							entry.getSeedSource(), femaleDesigButton, femaleGidButton, maleDesigButton, maleGidButton, method.getMname()},
					entry.getId());

			this.addToParentsInfoMap(entry.getId(), femaleGid, femaleDesig, maleGid, maleDesig);
		}

	}

	private void addToParentsInfoMap(final Integer id, final Integer femaleGid, final String femaleDesig, final Integer maleGid,
			final String maleDesig) {

		final GermplasmListEntry femaleEntry = new GermplasmListEntry(null, femaleGid, null, femaleDesig);
		final GermplasmListEntry maleEntry = new GermplasmListEntry(null, maleGid, null, maleDesig);
		final CrossParents parents = new CrossParents(femaleEntry, maleEntry);
		this.parentsInfo.put(id, parents);
	}

	private Button generateLaunchGermplasmDetailsButton(final String caption, final String gid, final String description) {
		final Button gidButton = new Button(caption, new GidLinkClickListener(gid, true));
		gidButton.setDebugId("gidButton");
		gidButton.setStyleName(BaseTheme.BUTTON_LINK);
		gidButton.setDescription(description);
		return gidButton;
	}

	@Override
	public void addListeners() {
		this.toolsButton.addListener(new ClickListener() {

			private static final long serialVersionUID = -7600642919550425308L;

			@Override
			public void buttonClick(final ClickEvent event) {
				CrossesSummaryListDataComponent.this.menu.show(event.getClientX(), event.getClientY());
			}
		});

		this.menu.addListener(new ContextMenu.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void contextItemClick(final org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
				final TransactionTemplate transactionTemplate =
						new TransactionTemplate(CrossesSummaryListDataComponent.this.transactionManager);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {

					@Override
					protected void doInTransactionWithoutResult(final TransactionStatus status) {
						final ContextMenuItem clickedItem = event.getClickedItem();
						if (CrossesSummaryListDataComponent.this.menuExportList.equals(clickedItem)) {
							CrossesSummaryListDataComponent.this.exportCrossesMadeAction();
						}
					}
				});
			}
		});

		this.viewHeaderButton.addListener(new ClickListener() {

			private static final long serialVersionUID = 329434322390122057L;

			@Override
			public void buttonClick(final com.vaadin.ui.Button.ClickEvent event) {
				CrossesSummaryListDataComponent.this.openViewListHeaderWindow();
			}
		});

	}

	@Override
	public void layoutComponents() {
		this.setSpacing(true);

		final HorizontalLayout tableHeaderLayout = new HorizontalLayout();
		tableHeaderLayout.setDebugId("tableHeaderLayout");
		tableHeaderLayout.setHeight("27px");
		tableHeaderLayout.setWidth("100%");

		final HorizontalLayout leftHeaderLayout = new HorizontalLayout();
		leftHeaderLayout.setDebugId("leftHeaderLayout");
		leftHeaderLayout.setSpacing(true);
		leftHeaderLayout.setHeight("100%");
		leftHeaderLayout.addComponent(this.listEntriesLabel);
		leftHeaderLayout.addComponent(this.viewHeaderButton);
		leftHeaderLayout.setComponentAlignment(this.viewHeaderButton, Alignment.MIDDLE_RIGHT);

		tableHeaderLayout.addComponent(leftHeaderLayout);
		tableHeaderLayout.addComponent(this.toolsButton);
		tableHeaderLayout.setComponentAlignment(leftHeaderLayout, Alignment.MIDDLE_LEFT);
		tableHeaderLayout.setComponentAlignment(this.toolsButton, Alignment.MIDDLE_RIGHT);

		final VerticalLayout tableLayout = new VerticalLayout();
		tableLayout.setDebugId("tableLayout");
		this.listDataTable.setWidth("100%");
		tableLayout.addComponent(this.listDataTable);
		tableLayout.setComponentAlignment(this.listDataTable, Alignment.TOP_LEFT);

		this.addComponent(tableHeaderLayout);
		this.addComponent(tableLayout);
		this.addComponent(this.menu);
	}

	private void retrieveGermplasmsInformation() {
		try {
			final List<Integer> germplasmIds = new ArrayList<>();
			this.germplasmMap = new HashMap<>();

			// retrieve germplasm of list data to get its parent germplasms
			this.listEntries = this.germplasmListManager.getGermplasmListDataByListId(this.list.getId());
			for (final GermplasmListData entry : this.listEntries) {
				germplasmIds.add(entry.getGid());
			}
			final List<Germplasm> existingGermplasms = this.germplasmDataManager.getGermplasms(germplasmIds);

			// retrieve methods of germplasms
			this.methodMap = this.germplasmDataManager.getMethodsByGids(germplasmIds);

			// retrieve preferred names of parent germplasms
			final List<Integer> parentIds = new ArrayList<>();
			for (final Germplasm germplasm : existingGermplasms) {
				this.germplasmMap.put(germplasm.getGid(), germplasm);
				parentIds.add(germplasm.getGpid1());
				parentIds.add(germplasm.getGpid2());
			}
			this.parentGermplasmNames = this.germplasmDataManager.getPreferredNamesByGids(parentIds);

		} catch (final MiddlewareQueryException ex) {
			CrossesSummaryListDataComponent.LOG.error(ex.getMessage() + this.list.getId(), ex);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					"Error in getting list and/or germplasm information.");
		}
	}

	protected void initializeListEntriesTable() {
		this.count = Long.valueOf(0);
		try {
			this.count = this.germplasmListManager.countGermplasmListDataByListId(this.list.getId());
		} catch (final MiddlewareQueryException e) {
			CrossesSummaryListDataComponent.LOG.error(e.getMessage(), e);
		}

		this.setListDataTable(new BreedingManagerTable(this.count.intValue(), 8));
		this.listDataTable = this.getListDataTable();
		this.listDataTable.setColumnCollapsingAllowed(true);
		this.listDataTable.setColumnReorderingAllowed(true);
		this.listDataTable.setImmediate(true);

		this.listDataTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.FEMALE_PARENT.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.FGID.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.MALE_PARENT.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.MGID.getName(), Button.class, null);
		this.listDataTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_NAME.getName(), String.class, null);

		this.listDataTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));
		this.listDataTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(), this.getTermNameFromOntology(ColumnLabels.DESIGNATION));
		this.listDataTable.setColumnHeader(ColumnLabels.PARENTAGE.getName(), this.getTermNameFromOntology(ColumnLabels.PARENTAGE));
		this.listDataTable.setColumnHeader(ColumnLabels.ENTRY_CODE.getName(), this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE));
		this.listDataTable.setColumnHeader(ColumnLabels.GID.getName(), this.getTermNameFromOntology(ColumnLabels.GID));
		this.listDataTable.setColumnHeader(ColumnLabels.SEED_SOURCE.getName(), this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE));
		this.listDataTable.setColumnHeader(ColumnLabels.FEMALE_PARENT.getName(), this.getTermNameFromOntology(ColumnLabels.FEMALE_PARENT));
		this.listDataTable.setColumnHeader(ColumnLabels.FGID.getName(), this.getTermNameFromOntology(ColumnLabels.FGID));
		this.listDataTable.setColumnHeader(ColumnLabels.MALE_PARENT.getName(), this.getTermNameFromOntology(ColumnLabels.MALE_PARENT));
		this.listDataTable.setColumnHeader(ColumnLabels.MGID.getName(), this.getTermNameFromOntology(ColumnLabels.MGID));
		this.listDataTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName(),
				this.getTermNameFromOntology(ColumnLabels.BREEDING_METHOD_NAME));

		this.listDataTable.setVisibleColumns(new Object[] {ColumnLabels.ENTRY_ID.getName(), ColumnLabels.DESIGNATION.getName(),
				ColumnLabels.PARENTAGE.getName(), ColumnLabels.ENTRY_CODE.getName(), ColumnLabels.GID.getName(),
				ColumnLabels.SEED_SOURCE.getName(), ColumnLabels.FEMALE_PARENT.getName(), ColumnLabels.FGID.getName(),
				ColumnLabels.MALE_PARENT.getName(), ColumnLabels.MGID.getName(), ColumnLabels.BREEDING_METHOD_NAME.getName()});
	}

	private void exportCrossesMadeAction() {
		final String tempFileName = System.getProperty(AppConstants.USER_HOME) + "/temp.xls";

		try {
			this.exporter.exportGermplasmListXLS(this.list.getId(), tempFileName, this.listDataTable);
			final FileDownloadResource fileDownloadResource =
					new FileDownloadResource(new File(tempFileName),this.list.getName().replace(" ","_") + ".xls", this.getApplication());
			this.getWindow().open(fileDownloadResource);
		} catch (GermplasmListExporterException | MiddlewareQueryException e) {
			CrossesSummaryListDataComponent.LOG.error(e.getMessage(), e);
			MessageNotifier.showError(this.getWindow(), "Error with exporting crossing file.", e.getMessage());
		}
	}

	public void openViewListHeaderWindow() {
		this.getWindow().addWindow(this.viewListHeaderWindow);
	}

	@Override
	public void focus() {
		this.listDataTable.focus();
	}

	protected String getTermNameFromOntology(final ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	public Table getListDataTable() {
		return this.listDataTable;
	}

	public void setListDataTable(final Table listDataTable) {
		this.listDataTable = listDataTable;
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setGermplasmListManager(final GermplasmListManager germplasmListManager) {
		this.germplasmListManager = germplasmListManager;
	}

}
