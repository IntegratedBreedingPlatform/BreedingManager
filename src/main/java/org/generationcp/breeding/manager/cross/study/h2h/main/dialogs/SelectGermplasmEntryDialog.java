
package org.generationcp.breeding.manager.cross.study.h2h.main.dialogs;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.h2h.main.SpecifyGermplasmsComponent;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.breeding.manager.germplasm.GermplasmQueries;
import org.generationcp.breeding.manager.germplasm.GermplasmSearchFormComponent;
import org.generationcp.breeding.manager.germplasm.GermplasmSearchResultComponent;
import org.generationcp.breeding.manager.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.breeding.manager.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.breeding.manager.util.CloseWindowAction;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.constant.DefaultGermplasmStudyBrowserPath;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Item;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class SelectGermplasmEntryDialog extends BaseSubWindow implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -7651767452229107837L;
	private static final Logger LOG = LoggerFactory.getLogger(SelectGermplasmEntryDialog.class);

	private static final String GID = ColumnLabels.GID.getName();
	public static final String SEARCH_BUTTON_ID = "SelectGermplasmEntryDialog Search Button ID";
	public static final String CLOSE_SCREEN_BUTTON_ID = "SelectGermplasmEntryDialog Close Button ID";
	public static final String ADD_BUTTON_ID = "SelectGermplasmEntryDialog Add Button ID";

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private CrossStudyDataManager crossStudyDataManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private final Component source;
	private final Window parentWindow;

	private VerticalLayout mainLayout;

	private Button searchButton;
	private Button doneButton;
	private Button cancelButton;

	private GermplasmSearchFormComponent searchComponent;
	private GermplasmSearchResultComponent resultComponent;
	private final GermplasmIndexContainer dataResultIndexContainer;
	private final GermplasmQueries gQuery;

	private Integer selectedGid;
	private final boolean isTestEntry;

	private List<Integer> environmentIds;

	public SelectGermplasmEntryDialog(Component source, Window parentWindow, boolean isTestEntry) {
		this.source = source;
		this.parentWindow = parentWindow;
		this.isTestEntry = isTestEntry;
		this.gQuery = new GermplasmQueries();
		this.dataResultIndexContainer = new GermplasmIndexContainer(this.gQuery);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// set as modal window, other components are disabled while window is open
		this.setModal(true);
		// define window size, set as not resizable
		this.setWidth("600px");
		this.setHeight("530px");
		this.setResizable(false);
		this.setCaption("Select a Germplasm");
		// center window within the browser
		this.center();

		this.mainLayout = new VerticalLayout();
		this.mainLayout.setSpacing(true);

		HorizontalLayout searchFormLayout = new HorizontalLayout();

		this.searchComponent = new GermplasmSearchFormComponent();
		searchFormLayout.addComponent(this.searchComponent);

		this.searchButton = new Button("Search");
		this.searchButton.setData(SelectGermplasmEntryDialog.SEARCH_BUTTON_ID);
		this.searchButton.addStyleName("addTopSpace");
		this.searchButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
		this.searchButton.setClickShortcut(KeyCode.ENTER);
		this.searchButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		searchFormLayout.addComponent(this.searchButton);

		this.mainLayout.addComponent(searchFormLayout);

		this.resultComponent = new GermplasmSearchResultComponent(this.germplasmDataManager, SelectGermplasmEntryDialog.GID, "0");
		this.resultComponent.addListener(new GermplasmItemClickListener(this));
		this.resultComponent.setHeight("320px");
		this.mainLayout.addComponent(this.resultComponent);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		this.cancelButton = new Button("Close Screen");
		this.cancelButton.setData(SelectGermplasmEntryDialog.CLOSE_SCREEN_BUTTON_ID);
		this.cancelButton.addListener(new CloseWindowAction());

		String buttonlabel = "";
		if (this.isTestEntry) {
			buttonlabel = "Add as Test Entry";
		} else {
			buttonlabel = "Add as Standard Entry";
		}
		this.doneButton = new Button(buttonlabel);
		this.doneButton.setData(SelectGermplasmEntryDialog.ADD_BUTTON_ID);
		this.doneButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
		this.doneButton.addListener(new CloseWindowAction());
		this.doneButton.setEnabled(false);
		this.doneButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		buttonLayout.addComponent(this.doneButton);
		buttonLayout.addComponent(this.cancelButton);

		this.mainLayout.addComponent(buttonLayout);
		this.mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);

		this.addComponent(this.mainLayout);
	}

	public void searchButtonClickAction() {
		this.doneButton.setEnabled(false);
		this.selectedGid = null;

		String searchChoice = this.searchComponent.getChoice();
		String searchValue = this.searchComponent.getSearchValue();

		if (searchValue.length() > 0) {
			boolean withNoError = true;

			if ("GID".equals(searchChoice)) {
				try {
					Integer.parseInt(searchValue);
				} catch (NumberFormatException e) {
					withNoError = false;
					if (this.getWindow() != null) {
						MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
								this.messageSource.getMessage(Message.ERROR_INVALID_INPUT_MUST_BE_NUMERIC));
					}
				}
			}

			// TODO : perhaps default to full search to prevent NPE
			if (withNoError) {
				LazyQueryContainer dataSourceResultLazy = null;
				if (this.isTestEntry || this.environmentIds == null) {
					dataSourceResultLazy =
							this.dataResultIndexContainer.getGermplasmResultLazyContainer(this.germplasmDataManager, searchChoice,
									searchValue);
				} else {
					dataSourceResultLazy =
							this.dataResultIndexContainer.getGermplasmEnvironmentResultLazyContainer(this.crossStudyDataManager,
									searchChoice, searchValue, this.environmentIds);
				}
				this.resultComponent.setCaption("Germplasm Search Result: " + dataSourceResultLazy.size());
				if (!this.isTestEntry && this.environmentIds != null && this.environmentIds.isEmpty()) {
					this.resultComponent.setCaption("Selected Test Entries not used in Trials - no comparable data");
				}
				this.resultComponent.setContainerDataSource(dataSourceResultLazy);
				this.mainLayout.requestRepaintAll();
			}
		} else {
			MessageNotifier.showError(this.getWindow(), "Error", "Please input search string.");
		}
	}

	public void addButtonClickAction() {
		try {
			Germplasm selectedGermplasm = this.germplasmDataManager.getGermplasmWithPrefName(this.selectedGid);
			if (this.isTestEntry) {
				((SpecifyGermplasmsComponent) this.source).addTestGermplasm(selectedGermplasm);
			} else {
				((SpecifyGermplasmsComponent) this.source).addStandardGermplasm(selectedGermplasm);
			}
		} catch (MiddlewareQueryException ex) {
			SelectGermplasmEntryDialog.LOG.error("Error with getting germplasm with gid: " + this.selectedGid, ex);
			MessageNotifier.showError(this.getWindow(), "Database Error!", "Error with getting germplasm with gid: " + this.selectedGid
					+ ". " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		} catch (Exception ex) {
			SelectGermplasmEntryDialog.LOG.error("Error with setting selected germplasm.", ex);
			MessageNotifier.showError(this.getWindow(), "Application Error!", "Error with setting selected germplasm." + " "
					+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}
	}

	public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) {
		sourceTable.select(itemId);
		this.selectedGid = Integer.valueOf(item.getItemProperty(SelectGermplasmEntryDialog.GID).toString());
		this.doneButton.setEnabled(true);
	}

	public void resultTableItemDoubleClickAction(Table sourceTable, Object itemId, Item item) {
		sourceTable.select(itemId);
		int gid = Integer.valueOf(item.getItemProperty(SelectGermplasmEntryDialog.GID).toString());

		Tool tool = null;
		try {
			tool = this.workbenchDataManager.getToolWithName(ToolName.GERMPLASM_BROWSER.toString());
		} catch (MiddlewareQueryException qe) {
			SelectGermplasmEntryDialog.LOG.error("QueryException", qe);
		}

		ExternalResource germplasmBrowserLink;
		if (tool == null) {
			germplasmBrowserLink =
					new ExternalResource(WorkbenchAppPathResolver.getFullWebAddress(DefaultGermplasmStudyBrowserPath.GERMPLASM_BROWSER_LINK
							+ gid, "?restartApplication"));
		} else {
			germplasmBrowserLink =
					new ExternalResource(WorkbenchAppPathResolver.getWorkbenchAppPath(tool, String.valueOf(gid), "?restartApplication"));
		}

		Window germplasmWindow = new BaseSubWindow("Germplasm Information - " + gid);

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
		germplasmWindow.setWidth(Integer.valueOf((int) Math.round(this.parentWindow.getWidth() * .90)) + "px");
		germplasmWindow.setHeight(Integer.valueOf((int) Math.round(this.parentWindow.getHeight() * .90)) + "px");

		germplasmWindow.center();
		germplasmWindow.setResizable(false);

		germplasmWindow.setModal(true);

		this.parentWindow.addWindow(germplasmWindow);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public void setEnvironmentIds(List<Integer> environmentIds) {
		this.environmentIds = environmentIds;
	}

	public boolean isTestEntry() {
		return this.isTestEntry;
	}

}
