
package org.generationcp.breeding.manager.germplasm.dialogs;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.germplasm.GermplasmQueries;
import org.generationcp.breeding.manager.germplasm.GermplasmSearchFormComponent;
import org.generationcp.breeding.manager.germplasm.GermplasmSearchResultComponent;
import org.generationcp.breeding.manager.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.breeding.manager.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.breeding.manager.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.breeding.manager.util.CloseWindowAction;
import org.generationcp.commons.constant.DefaultGermplasmStudyBrowserPath;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
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
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class SelectAGermplasmDialog extends Window implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -7651767452229107837L;

	private final static Logger LOG = LoggerFactory.getLogger(SelectAGermplasmDialog.class);

	private static final String GID = "gid";

	public static final String SEARCH_BUTTON_ID = "SelectAGermplasmDialog Search Button ID";
	public static final String CANCEL_BUTTON_ID = "SelectAGermplasmDialog Cancel Button ID";
	public static final String DONE_BUTTON_ID = "SelectAGermplasmDialog Done Button ID";

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private final Label germplasmComponent;
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

	public SelectAGermplasmDialog(Component source, Window parentWindow, Label germplasmComponent) {
		this.parentWindow = parentWindow;
		this.germplasmComponent = germplasmComponent;
		this.gQuery = new GermplasmQueries();
		this.dataResultIndexContainer = new GermplasmIndexContainer(this.gQuery);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// set as modal window, other components are disabled while window is open
		this.setModal(true);
		// define window size, set as not resizable
		this.setWidth("600px");
		this.setHeight("500px");
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
		this.searchButton.setData(SelectAGermplasmDialog.SEARCH_BUTTON_ID);
		this.searchButton.addStyleName("addTopSpace");
		this.searchButton.addListener(new GermplasmButtonClickListener(this));
		this.searchButton.setClickShortcut(KeyCode.ENTER);
		searchFormLayout.addComponent(this.searchButton);

		this.mainLayout.addComponent(searchFormLayout);

		this.resultComponent = new GermplasmSearchResultComponent(this.germplasmDataManager, SelectAGermplasmDialog.GID, "0");
		this.resultComponent.addListener(new GermplasmItemClickListener(this));
		this.mainLayout.addComponent(this.resultComponent);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		this.cancelButton = new Button("Cancel");
		this.cancelButton.setData(SelectAGermplasmDialog.CANCEL_BUTTON_ID);
		this.cancelButton.addListener(new CloseWindowAction());
		buttonLayout.addComponent(this.cancelButton);

		this.doneButton = new Button("Done");
		this.doneButton.setData(SelectAGermplasmDialog.DONE_BUTTON_ID);
		this.doneButton.addListener(new GermplasmButtonClickListener(this));
		this.doneButton.addListener(new CloseWindowAction());
		this.doneButton.setEnabled(false);
		buttonLayout.addComponent(this.doneButton);

		this.mainLayout.addComponent(buttonLayout);

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

			if (withNoError) {
				LazyQueryContainer dataSourceResultLazy =
						this.dataResultIndexContainer.getGermplasmResultLazyContainer(this.germplasmDataManager, searchChoice, searchValue);
				this.resultComponent.setCaption("Germplasm Search Result: " + dataSourceResultLazy.size());
				this.resultComponent.setContainerDataSource(dataSourceResultLazy);
				this.mainLayout.requestRepaintAll();
			}
		} else {
			MessageNotifier.showError(this.getWindow(), "Error", "Please input search string.");
		}
	}

	public void doneButtonClickAction() {
		try {
			Germplasm selectedGermplasm = this.germplasmDataManager.getGermplasmWithPrefName(this.selectedGid);
			this.germplasmComponent.setData(selectedGermplasm.getGid());
			if (selectedGermplasm.getPreferredName() != null) {
				String preferredName = selectedGermplasm.getPreferredName().getNval();
				this.germplasmComponent.setValue("" + selectedGermplasm.getGid() + " - " + preferredName);
			} else {
				this.germplasmComponent.setValue(selectedGermplasm.getGid());
				MessageNotifier.showWarning(this.getWindow(), "Warning!", "The germplasm you selected doesn't have a preferred name, "
						+ "please select a different germplasm.");
			}
			this.germplasmComponent.requestRepaint();
		} catch (MiddlewareQueryException ex) {
			SelectAGermplasmDialog.LOG.error("Error with getting germplasm with gid: " + this.selectedGid, ex);
			MessageNotifier.showError(this.getWindow(), "Database Error!", "Error with getting germplasm with gid: " + this.selectedGid
					+ ". " + this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		} catch (Exception ex) {
			SelectAGermplasmDialog.LOG.error("Error with setting selected germplasm.", ex);
			MessageNotifier.showError(this.getWindow(), "Application Error!", "Error with setting selected germplasm." + " "
					+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}
	}

	public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
		sourceTable.select(itemId);
		this.selectedGid = Integer.valueOf(item.getItemProperty(SelectAGermplasmDialog.GID).toString());
		this.doneButton.setEnabled(true);
	}

	public void resultTableItemDoubleClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
		sourceTable.select(itemId);
		int gid = Integer.valueOf(item.getItemProperty(SelectAGermplasmDialog.GID).toString());

		Tool tool = null;
		try {
			tool = this.workbenchDataManager.getToolWithName(ToolName.GERMPLASM_BROWSER.toString());
		} catch (MiddlewareQueryException qe) {
			SelectAGermplasmDialog.LOG.error("QueryException", qe);
		}

		ExternalResource germplasmBrowserLink;
		if (tool == null) {
			germplasmBrowserLink =
					new ExternalResource(WorkbenchAppPathResolver.getFullWebAddress(DefaultGermplasmStudyBrowserPath.GERMPLASM_BROWSER_LINK
							+ gid));
		} else {
			germplasmBrowserLink = new ExternalResource(WorkbenchAppPathResolver.getWorkbenchAppPath(tool, String.valueOf(gid)));
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

	}
}
