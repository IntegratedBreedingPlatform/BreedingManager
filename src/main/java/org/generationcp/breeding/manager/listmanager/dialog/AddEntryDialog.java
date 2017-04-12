
package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingLocationField;
import org.generationcp.breeding.manager.customfields.BreedingLocationFieldSource;
import org.generationcp.breeding.manager.customfields.BreedingMethodField;
import org.generationcp.breeding.manager.customfields.ListDateField;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchBarComponent;
import org.generationcp.breeding.manager.listmanager.GermplasmSearchResultsComponent;
import org.generationcp.breeding.manager.listmanager.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListItemClickListener;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.constant.DefaultGermplasmStudyBrowserPath;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class AddEntryDialog extends BaseSubWindow
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, BreedingLocationFieldSource {

	private static final long serialVersionUID = -1627453790001229325L;

	private static final Logger LOG = LoggerFactory.getLogger(AddEntryDialog.class);

	public static final String OPTION_1_ID = "AddEntryDialog Option 1";
	public static final String OPTION_2_ID = "AddEntryDialog Option 2";
	public static final String OPTION_3_ID = "AddEntryDialog Option 3";
	public static final String NEXT_BUTTON_ID = "AddEntryDialog Next Button";
	public static final String CANCEL_BUTTON_ID = "AddEntryDialog Cancel Button";
	public static final String DONE_BUTTON_ID = "AddEntryDialog Done Button";
	private static final String DEFAULT_NAME_TYPE_CODE = "LNAME";

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Resource
	private ContextUtil contextUtil;

	private final Window parentWindow;
	private VerticalLayout topPart;
	private AbsoluteLayout bottomPart;
	private final AddEntryDialogSource source;
	private OptionGroup optionGroup;

	private BreedingMethodField breedingMethodField;
	private BreedingLocationField breedingLocationField;

	private Button doneButton;
	private Button cancelButton;

	private Label topPartHeader;
	private Label step2Label;

	private Label germplasmDateLabel;
	private Label nameTypeLabel;
	private Label bottomPartHeader;

	private ComboBox nameTypeComboBox;

	private ListDateField germplasmDateField;

	private GermplasmSearchBarComponent searchBarComponent;

	private GermplasmSearchResultsComponent searchResultsComponent;

	@Autowired
	private BreedingManagerService breedingManagerService;
	private String programUniqueId;

	public AddEntryDialog(final AddEntryDialogSource source, final Window parentWindow) {
		this.setOverrideFocus(true);
		this.source = source;
		this.parentWindow = parentWindow;
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

		try {
			this.programUniqueId = this.breedingManagerService.getCurrentProject().getUniqueID();
		} catch (final MiddlewareQueryException e) {
			AddEntryDialog.LOG.error(e.getMessage(), e);
		}

		this.initializeTopPart();
		this.initializeBottomPart();
		this.initializeButtonLayout();
	}

	@Override
	public void initializeValues() {
		this.populateNameTypeComboBox();
	}

	@Override
	public void addListeners() {
		this.addSearchResultsListeners();

		this.addListenerToOptionGroup();

		this.addButtonListeners();
	}

	void addButtonListeners() {
		this.cancelButton.addListener(new CloseWindowAction());
		this.doneButton.addListener(new GermplasmListButtonClickListener(this));
	}

	protected void addListenerToOptionGroup() {
		this.optionGroup.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				if (AddEntryDialog.this.optionGroup.getValue().equals(AddEntryDialog.OPTION_1_ID)) {
					AddEntryDialog.this.setSpecifyDetailsVisible(false);
					AddEntryDialog.this.toggleDoneButtonState();
				} else if (AddEntryDialog.this.optionGroup.getValue().equals(AddEntryDialog.OPTION_2_ID)) {
					AddEntryDialog.this.setSpecifyDetailsVisible(true);
					AddEntryDialog.this.toggleDoneButtonState();
				} else if (AddEntryDialog.this.optionGroup.getValue().equals(AddEntryDialog.OPTION_3_ID)) {
					AddEntryDialog.this.doneButton.setEnabled(true);
					AddEntryDialog.this.setSpecifyDetailsVisible(true);
				}
			}
		});
	}

	protected void addSearchResultsListeners() {
		this.searchResultsComponent.getMatchingGermplasmTable().addListener(new Table.ValueChangeListener() {

			private static final long serialVersionUID = 4523431033752074159L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				AddEntryDialog.this.toggleDoneButtonState();
			}
		});

		this.searchResultsComponent.getMatchingGermplasmTable().addListener(new GermplasmListItemClickListener(this));
	}

	@Override
	public void layoutComponents() {
		this.setModal(true);
		this.setWidth("800px");
		this.setResizable(false);
		this.setCaption(this.messageSource.getMessage(Message.ADD_LIST_ENTRIES));
		this.center();

		this.topPart = new VerticalLayout();
		this.topPart.setDebugId("topPart");
		this.topPart.setSpacing(true);
		this.topPart.setMargin(false);
		this.topPart.addComponent(this.topPartHeader);
		this.topPart.addComponent(this.searchBarComponent);
		this.topPart.addComponent(this.searchResultsComponent);
		this.topPart.addComponent(this.step2Label);
		this.topPart.addComponent(this.optionGroup);

		this.bottomPart = new AbsoluteLayout();
		this.bottomPart.setDebugId("bottomPart");
		this.bottomPart.setWidth("600px");
		this.bottomPart.setHeight("230px");
		this.bottomPart.addComponent(this.bottomPartHeader, "top:15px;left:0px");
		this.bottomPart.addComponent(this.breedingMethodField, "top:50px;left:0px");
		this.bottomPart.addComponent(this.germplasmDateLabel, "top:107px;left:0px");
		this.bottomPart.addComponent(this.germplasmDateField, "top:102px;left:124px");
		this.bottomPart.addComponent(this.breedingLocationField, "top:133px;left:0px");
		this.bottomPart.addComponent(this.nameTypeLabel, "top:206px;left:0px");
		this.bottomPart.addComponent(this.nameTypeComboBox, "top:206px;left:130px");

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("50px");
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.doneButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.doneButton, Alignment.BOTTOM_LEFT);

		this.addComponent(this.topPart);
		this.addComponent(this.bottomPart);
		this.addComponent(buttonLayout);

		this.setSpecifyDetailsVisible(false);
	}

	@SuppressWarnings("unchecked")
	private Boolean isTableHasEntriesSelected() {
		final Collection<Object> selectedEntries = (Collection<Object>) this.searchResultsComponent.getMatchingGermplasmTable().getValue();
		return !selectedEntries.isEmpty();
	}

	public void resultTableItemClickAction() {
		this.toggleDoneButtonState();
	}

	void toggleDoneButtonState() {
		this.doneButton.setEnabled(this.isTableHasEntriesSelected());
	}

	/**
	 * Launches the Germplasm Details details on pop-up window of double-clicked item
	 *
	 * @param sourceTable
	 * @param itemId
	 * @param item
	 */
	public void resultTableItemDoubleClickAction(final Table sourceTable, final Object itemId, final Item item) {
		sourceTable.select(itemId);
		final int gid = Integer.valueOf(item.getItemProperty(ColumnLabels.GID.getName() + "_REF").getValue().toString());

		final Tool tool = this.workbenchDataManager.getToolWithName(ToolName.GERMPLASM_BROWSER.toString());
		final String addtlParams = Util.getAdditionalParams(this.workbenchDataManager);
		ExternalResource germplasmBrowserLink;
		if (tool == null) {
			germplasmBrowserLink = new ExternalResource(WorkbenchAppPathResolver
					.getFullWebAddress(DefaultGermplasmStudyBrowserPath.GERMPLASM_BROWSER_LINK + gid, "?restartApplication" + addtlParams));
		} else {
			germplasmBrowserLink = new ExternalResource(
					WorkbenchAppPathResolver.getWorkbenchAppPath(tool, String.valueOf(gid), "?restartApplication" + addtlParams));
		}

		final Window germplasmWindow = new BaseSubWindow(this.messageSource.getMessage(Message.GERMPLASM_INFORMATION) + " - " + gid);

		final VerticalLayout layoutForGermplasm = new VerticalLayout();
		layoutForGermplasm.setDebugId("layoutForGermplasm");
		layoutForGermplasm.setMargin(false);
		layoutForGermplasm.setWidth("98%");
		layoutForGermplasm.setHeight("98%");

		final Embedded germplasmInfo = new Embedded("", germplasmBrowserLink);
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

		this.parentWindow.addWindow(germplasmWindow);
	}

	protected void initializeTopPart() {
		this.topPart = new VerticalLayout();
		this.topPart.setDebugId("topPart");
		this.topPart.setSpacing(true);
		this.topPart.setMargin(false);

		this.topPartHeader = new Label(this.messageSource.getMessage(Message.SELECT_A_GERMPLASM));
		this.topPartHeader.setDebugId("topPartHeader");
		this.topPartHeader.addStyleName("bold");
		this.topPartHeader.addStyleName("h3");

		this.searchResultsComponent = new GermplasmSearchResultsComponent(this.source.getListManagerMain(), false, false);
		this.searchResultsComponent.setDebugId("searchResultsComponent");
		this.searchResultsComponent.getMatchingGermplasmTable().setHeight("150px");
		this.searchResultsComponent.getMatchingGermplasmTableWithSelectAll().setHeight("220px");
		this.searchResultsComponent.setRightClickActionHandlerEnabled(false);

		this.searchBarComponent = new GermplasmSearchBarComponent(this.searchResultsComponent, this.source);
		this.searchBarComponent.setDebugId("searchBarComponent");

		this.step2Label = new Label(this.messageSource.getMessage(Message.HOW_DO_YOU_WANT_TO_ADD_THE_GERMPLASM_TO_THE_LIST));
		this.step2Label.setDebugId("step2Label");
		this.step2Label.addStyleName("bold");

		this.optionGroup = new OptionGroup();
		this.optionGroup.setDebugId("optionGroup");
		this.optionGroup.addItem(AddEntryDialog.OPTION_1_ID);
		this.optionGroup.setItemCaption(AddEntryDialog.OPTION_1_ID,
				this.messageSource.getMessage(Message.USE_SELECTED_GERMPLASM_FOR_THE_LIST_ENTRY));
		this.optionGroup.addItem(AddEntryDialog.OPTION_2_ID);
		this.optionGroup.setItemCaption(AddEntryDialog.OPTION_2_ID, this.messageSource
				.getMessage(Message.CREATE_A_NEW_GERMPLASM_RECORD_FOR_THE_LIST_ENTRY_AND_ASSIGN_THE_SELECTED_GERMPLASM_AS_ITS_SOURCE));
		this.optionGroup.addItem(AddEntryDialog.OPTION_3_ID);
		this.optionGroup.setItemCaption(AddEntryDialog.OPTION_3_ID,
				this.messageSource.getMessage(Message.CREATE_A_NEW_GERMPLASM_RECORD_FOR_THE_LIST_ENTRY));
		this.optionGroup.select(AddEntryDialog.OPTION_1_ID);
		this.optionGroup.setImmediate(true);
	}

	protected void initializeBottomPart() {
		this.bottomPart = new AbsoluteLayout();
		this.bottomPart.setDebugId("bottomPart");
		this.bottomPart.setWidth("600px");
		this.bottomPart.setHeight("230px");

		this.bottomPartHeader = new Label(this.messageSource.getMessage(Message.SPECIFY_ADDITIONAL_DETAILS));
		this.bottomPartHeader.setDebugId("bottomPartHeader");
		this.bottomPartHeader.addStyleName("bold");
		this.bottomPartHeader.addStyleName("h3");

		this.breedingMethodField = new BreedingMethodField(this.parentWindow);
		this.breedingMethodField.setDebugId("breedingMethodField");

		this.germplasmDateLabel = new Label("Creation Date: ");
		this.germplasmDateLabel.setDebugId("germplasmDateLabel");
		this.germplasmDateLabel.addStyleName("bold");

		this.germplasmDateField = new ListDateField("", false);
		this.germplasmDateField.setDebugId("germplasmDateField");
		this.germplasmDateField.getListDtDateField().setValue(DateUtil.getCurrentDate());

		this.breedingLocationField = new BreedingLocationField(this, this.parentWindow, true);
		this.breedingLocationField.setDebugId("breedingLocationField");

		this.nameTypeLabel = new Label("Name Type: ");
		this.nameTypeLabel.setDebugId("nameTypeLabel");
		this.nameTypeLabel.addStyleName("bold");

		this.nameTypeComboBox = new ComboBox();
		this.nameTypeComboBox.setDebugId("nameTypeComboBox");
		this.nameTypeComboBox.setWidth("400px");
	}

	public void initializeButtonLayout() {
		this.cancelButton = new Button(this.messageSource.getMessage(Message.CANCEL));
		this.cancelButton.setDebugId("cancelButton");
		this.cancelButton.setData(AddEntryDialog.CANCEL_BUTTON_ID);

		this.doneButton = new Button(this.messageSource.getMessage(Message.DONE));
		this.doneButton.setDebugId("doneButton");
		this.doneButton.setData(AddEntryDialog.DONE_BUTTON_ID);
		this.doneButton.setEnabled(false);
		this.doneButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	public void doneButtonClickAction(final ClickEvent event) {
		if (this.optionGroup.getValue().equals(AddEntryDialog.OPTION_1_ID)) {
			// add the germplasm selected as the list entry
			this.validateInputForOption1(event);

		} else if (this.optionGroup.getValue().equals(AddEntryDialog.OPTION_2_ID)) {
			this.validateInputForOption2(event);

		} else if (this.optionGroup.getValue().equals(AddEntryDialog.OPTION_3_ID)) {
			this.validateInputForOption3(event);
		}
	}

	private void validateInputForOption1(final ClickEvent event) {
		if (this.isTableHasEntriesSelected()) {
			final List<Integer> selectedGids = this.getSelectedItemGids();
			this.source.finishAddingEntry(selectedGids);
			final Window window = event.getButton().getWindow();
			window.getParent().removeWindow(window);
		} else {
			MessageNotifier.showWarning(this, this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.YOU_MUST_SELECT_A_GERMPLASM_FROM_THE_SEARCH_RESULTS));
		}
	}

	private void validateInputForOption2(final ClickEvent event) {
		if (this.breedingMethodField.getBreedingMethodComboBox().getValue() == null) {
			MessageNotifier.showRequiredFieldError(this, this.messageSource.getMessage(Message.YOU_MUST_SELECT_A_METHOD_FOR_THE_GERMPLASM));

		} else if (this.breedingLocationField.getBreedingLocationComboBox().getValue() == null) {
			MessageNotifier.showRequiredFieldError(this,
					this.messageSource.getMessage(Message.YOU_MUST_SELECT_A_LOCATION_FOR_THE_GERMPLASM));

		} else if (this.isTableHasEntriesSelected()) {
			if (this.saveNewGermplasm()) {
				final Window window = event.getButton().getWindow();
				window.getParent().removeWindow(window);
			}
		} else {
			MessageNotifier.showWarning(this, this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.YOU_MUST_SELECT_A_GERMPLASM_FROM_THE_SEARCH_RESULTS));
		}
	}

	private void validateInputForOption3(final ClickEvent event) {
		final String searchValue = this.searchBarComponent.getSearchField().getValue().toString();
		if (this.breedingMethodField.getBreedingMethodComboBox().getValue() == null) {
			MessageNotifier.showRequiredFieldError(this, this.messageSource.getMessage(Message.YOU_MUST_SELECT_A_METHOD_FOR_THE_GERMPLASM));

		} else if (this.breedingLocationField.getBreedingLocationComboBox().getValue() == null) {
			MessageNotifier.showRequiredFieldError(this,
					this.messageSource.getMessage(Message.YOU_MUST_SELECT_A_LOCATION_FOR_THE_GERMPLASM));

		} else if (searchValue != null && searchValue.length() != 0) {
			this.saveNewGermplasm();
			final Window window = event.getButton().getWindow();
			window.getParent().removeWindow(window);
		} else {
			MessageNotifier.showRequiredFieldError(this,
					this.messageSource.getMessage(Message.YOU_MUST_ENTER_A_GERMPLASM_NAME_IN_THE_TEXTBOX));
		}
	}

	/**
	 * Creates germplasm record/s from selected germplasm (for Option 2) or from search keyword (for Option 3)
	 *
	 * @return
	 */
	public Boolean saveNewGermplasm() {
		if (!this.optionGroup.getValue().equals(AddEntryDialog.OPTION_2_ID)
				&& !this.optionGroup.getValue().equals(AddEntryDialog.OPTION_3_ID)) {
			return false;
		}
		if (this.optionGroup.getValue().equals(AddEntryDialog.OPTION_2_ID)) {
			final List<Integer> addedGids = this.addGermplasm(this.getSelectedItemGids());
			this.source.finishAddingEntry(addedGids);

		} else {
			final List<Integer> addedGids = this.addGermplasm(new ArrayList<Integer>());
			if (addedGids == null || addedGids.isEmpty()) {
				return false;
			}
			// Expecting only one germplasm record to have been created
			this.source.finishAddingEntry(addedGids.get(0));
		}
		return true;
	}

	List<Integer> addGermplasm(final List<Integer> selectedGids) {
		final Integer breedingMethodId = (Integer) this.breedingMethodField.getBreedingMethodComboBox().getValue();
		final Integer nameTypeId = (Integer) this.nameTypeComboBox.getValue();
		final Integer locationId = (Integer) this.breedingLocationField.getBreedingLocationComboBox().getValue();

		final Integer date = this.getGermplasmDate();
		if (date == null) {
			return null;
		}

		final Integer currentUserLocalId = this.getCurrentUserLocalId();

		final Map<Germplasm, Name> germplasmNamesMap = new HashMap<>();
		if (!selectedGids.isEmpty()) {
			// One-off DB retrieval for germplasm and preferred names for selected germplasm
			final List<Germplasm> listOfGermplasm = this.germplasmDataManager.getGermplasms(selectedGids);
			final Map<Integer, String> preferrredNamesMap = this.germplasmDataManager.getPreferredNamesByGids(selectedGids);

			// Perform one-off saving for all new germplasm and names records
			for (final Germplasm selectedGermplasm : listOfGermplasm) {
				String germplasmName = this.searchBarComponent.getSearchField().getValue().toString();
				final String preferredName = preferrredNamesMap.get(selectedGermplasm.getGid());
				if (preferredName != null) {
					germplasmName = preferredName;
				}

				final Germplasm germplasm = this.createGermplasm(selectedGermplasm, date, locationId, breedingMethodId, currentUserLocalId);
				final Name name = this.createName(germplasmName, locationId, date, nameTypeId, currentUserLocalId);
				germplasmNamesMap.put(germplasm, name);
			}

			// If no selected GIDs, add new germplasm with name equals to search string
		} else {
			final Germplasm germplasm = this.createGermplasm(null, date, locationId, breedingMethodId, currentUserLocalId);
			final String germplasmName = this.searchBarComponent.getSearchField().getValue().toString();
			final Name name = this.createName(germplasmName, locationId, date, nameTypeId, currentUserLocalId);
			germplasmNamesMap.put(germplasm, name);
		}

		return this.saveGermplasmToDatabase(germplasmNamesMap);
	}

	private Integer getGermplasmDate() {
		final Date dateOfCreation = this.germplasmDateField.getValue();
		if (dateOfCreation == null) {
			AddEntryDialog.LOG.error("Invalid date on add list entries! - " + dateOfCreation);
			MessageNotifier.showRequiredFieldError(this.getWindow(), this.messageSource.getMessage(Message.VALIDATION_DATE_FORMAT));
			return null;
		}
		final String parsedDate = DateUtil.formatDateAsStringValue(dateOfCreation, DateUtil.DATE_AS_NUMBER_FORMAT);
		if (parsedDate == null) {
			AddEntryDialog.LOG.error("Invalid date on add list entries! - " + parsedDate);
			MessageNotifier.showRequiredFieldError(this.getWindow(), this.messageSource.getMessage(Message.VALIDATION_DATE_FORMAT));
			return null;
		}
		return Integer.parseInt(parsedDate);
	}

	private Integer getCurrentUserLocalId() {
		Integer currentUserLocalId = -1;
		try {
			currentUserLocalId = this.contextUtil.getCurrentUserLocalId();
		} catch (final MiddlewareQueryException e) {
			AddEntryDialog.LOG.error(e.getMessage(), e);
		}
		return currentUserLocalId;
	}

	private Germplasm createGermplasm(final Germplasm selectedGermplasm, final Integer date, final Integer locationId,
			final Integer breedingMethodId, final Integer currentUserLocalId) {

		final Germplasm germplasm = new Germplasm();
		germplasm.setGdate(date);
		germplasm.setGnpgs(Integer.valueOf(-1));
		germplasm.setGpid1(Integer.valueOf(0));
		germplasm.setGpid2(Integer.valueOf(0));
		germplasm.setGrplce(Integer.valueOf(0));
		germplasm.setLgid(Integer.valueOf(0));
		germplasm.setLocationId(locationId);
		germplasm.setMethodId(breedingMethodId);
		germplasm.setMgid(Integer.valueOf(0));
		germplasm.setReferenceId(Integer.valueOf(0));
		germplasm.setUserId(currentUserLocalId);

		if (selectedGermplasm != null) {
			// temporarily set GID so that it can be used as key in map for saving
			germplasm.setGid(selectedGermplasm.getGid());
			if (selectedGermplasm.getGnpgs() < 2) {
				germplasm.setGpid1(selectedGermplasm.getGpid1());
			} else {
				germplasm.setGpid1(selectedGermplasm.getGid());
			}
			germplasm.setGpid2(selectedGermplasm.getGid());
		}

		return germplasm;
	}

	private Name createName(final String germplasmName, final Integer locationId, final Integer date, final Integer nameTypeId,
			final Integer currentUserLocalId) {
		final Name name = new Name();
		name.setNval(germplasmName);
		name.setLocationId(locationId);
		name.setNdate(date);
		name.setNstat(Integer.valueOf(1));
		name.setReferenceId(Integer.valueOf(0));
		name.setTypeId(nameTypeId);
		name.setUserId(currentUserLocalId);
		return name;
	}

	private List<Integer> saveGermplasmToDatabase(final Map<Germplasm, Name> germplasmNameMap) {
		try {
			return this.germplasmDataManager.addGermplasm(germplasmNameMap);
		} catch (final MiddlewareQueryException ex) {
			AddEntryDialog.LOG.error("Error with saving germplasm and name records!", ex);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_WITH_SAVING_GERMPLASM_AND_NAME_RECORDS)
							+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			return null;
		}
	}

	private void populateNameTypeComboBox() {
		try {
			final List<UserDefinedField> nameTypes = this.germplasmListManager.getGermplasmNameTypes();
			for (final UserDefinedField nameType : nameTypes) {
				final Integer nameTypeId = nameType.getFldno();
				final String nameTypeString = nameType.getFname();
				final String nameTypeCode = nameType.getFcode();
				this.nameTypeComboBox.addItem(nameTypeId);
				this.nameTypeComboBox.setItemCaption(nameTypeId, nameTypeString);
				if (nameTypeCode.equals(AddEntryDialog.DEFAULT_NAME_TYPE_CODE)) {
					this.nameTypeComboBox.select(nameTypeId);
				}
			}
		} catch (final MiddlewareQueryException ex) {
			AddEntryDialog.LOG.error("Error with getting germplasm name types!", ex);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_WITH_GETTING_GERMPLASM_NAME_TYPES)
							+ this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			final Integer unknownId = Integer.valueOf(0);
			this.nameTypeComboBox.addItem(unknownId);
			this.nameTypeComboBox.setItemCaption(unknownId, this.messageSource.getMessage(Message.UNKNOWN));
		}
		this.nameTypeComboBox.setNullSelectionAllowed(false);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	/**
	 * Iterates through the whole table, gets selected item's GID's, make sure it's sorted as seen on the UI
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getSelectedItemGids() {
		final Table table = this.searchResultsComponent.getMatchingGermplasmTable();
		List<Integer> itemIds = new ArrayList<Integer>();
		final List<Integer> selectedItemIds = new ArrayList<Integer>();
		final List<Integer> trueOrderedSelectedItemIds = new ArrayList<Integer>();

		selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
		itemIds = this.getItemIds(table);

		for (final Integer itemId : itemIds) {
			if (selectedItemIds.contains(itemId)) {
				final Integer selectedGid =
						Integer.valueOf(table.getItem(itemId).getItemProperty(ColumnLabels.GID.getName() + "_REF").getValue().toString());
				trueOrderedSelectedItemIds.add(selectedGid);
			}
		}

		return trueOrderedSelectedItemIds;
	}

	/**
	 * Get item id's of a table, and return it as a list
	 *
	 * @param table
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> getItemIds(final Table table) {
		final List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
		return itemIds;
	}

	private void setSpecifyDetailsVisible(final Boolean visible) {
		int height = 530;
		if (visible) {
			// add height of bottom part + margin
			height += 230 + 10;
			this.setHeight(height + "px");
			this.bottomPart.setVisible(true);
			this.center();
		} else {
			this.setHeight(height + "px");
			this.bottomPart.setVisible(false);
			this.center();
		}
	}

	public void focusOnSearchField() {
		this.searchBarComponent.getSearchField().focus();
	}

	@Override
	public void updateAllLocationFields() {
		if (this.breedingLocationField != null && this.breedingLocationField.getBreedingLocationComboBox() != null
				&& this.breedingLocationField.getBreedingLocationComboBox().getValue() != null) {
			final Object lastValue = this.breedingLocationField.getBreedingLocationComboBox().getValue();
			this.breedingLocationField.populateHarvestLocation(Integer.valueOf(lastValue.toString()), this.programUniqueId);
		}
	}

	public void setOptionGroup(final OptionGroup optionGroup) {
		this.optionGroup = optionGroup;
	}

	public Button getDoneButton() {
		return this.doneButton;
	}

	public Button getCancelButton() {
		return this.cancelButton;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource msgSource) {
		this.messageSource = msgSource;
	}

	public void setBreedingManagerService(final BreedingManagerService breedingManagerService) {
		this.breedingManagerService = breedingManagerService;
	}

	public void setSearchResultsComponent(final GermplasmSearchResultsComponent searchResultsComponent) {
		this.searchResultsComponent = searchResultsComponent;
	}

	public void setSearchBarComponent(final GermplasmSearchBarComponent searchBarComponent) {
		this.searchBarComponent = searchBarComponent;
	}

	public void setBreedingMethodField(final BreedingMethodField breedingMethodField) {
		this.breedingMethodField = breedingMethodField;
	}

	public void setBreedingLocationField(final BreedingLocationField breedingLocationField) {
		this.breedingLocationField = breedingLocationField;
	}

	public void setNameTypeComboBox(final ComboBox nameTypeComboBox) {
		this.nameTypeComboBox = nameTypeComboBox;
	}

	public void setGermplasmDateField(final ListDateField germplasmDateField) {
		this.germplasmDateField = germplasmDateField;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

}
