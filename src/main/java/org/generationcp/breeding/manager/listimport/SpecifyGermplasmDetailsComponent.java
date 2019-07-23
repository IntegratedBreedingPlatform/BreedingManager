
package org.generationcp.breeding.manager.listimport;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.exception.BreedingManagerException;
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.breeding.manager.listimport.actions.SaveGermplasmListAction;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.listmanager.dialog.GenerateStockIDsDialog;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmList;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class SpecifyGermplasmDetailsComponent extends VerticalLayout
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, SaveListAsDialogSource {

	private static final long serialVersionUID = 2762965368037453497L;
	private static final Logger LOG = LoggerFactory.getLogger(SpecifyGermplasmDetailsComponent.class);

	public static final String NEXT_BUTTON_ID = "next button";
	public static final String BACK_BUTTON_ID = "back button";

	private final GermplasmImportMain source;

	private GermplasmFieldsComponent germplasmFieldsComponent;
	private Table germplasmDetailsTable;

	private Label reviewImportDetailsLabel;
	private Label reviewImportDetailsMessage;
	private Label totalEntriesLabel;
	private Label selectPedigreeOptionsLabel;
	private Label pedigreeOptionsLabel;

	private ComboBox pedigreeOptionComboBox;

	private Button backButton;
	private Button nextButton;

	private ImportedGermplasmList importedGermplasmList;

	private CheckBox automaticallyAcceptSingleMatchesCheckbox;

	private List<ImportedGermplasm> importedGermplasms;
	private GermplasmListUploader germplasmListUploader;

	private GermplasmList germplasmList;

	private SaveListAsDialog saveListAsDialog;

	private GenerateStockIDsDialog generateStockIdsDialog;

	private ProcessImportedGermplasmAction processGermplasmAction;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private ContextUtil contextUtil;

	private final Boolean viaToolURL;

	public SpecifyGermplasmDetailsComponent(final GermplasmImportMain source, final Boolean viaToolURL) {
		this.source = source;
		this.viaToolURL = viaToolURL;
	}

	public Table getGermplasmDetailsTable() {
		return this.germplasmDetailsTable;
	}

	public List<ImportedGermplasm> getImportedGermplasm() {
		return this.importedGermplasms;
	}

	public void setImportedGermplasms(final List<ImportedGermplasm> importedGermplasms) {
		this.importedGermplasms = importedGermplasms;
	}

	public GermplasmListUploader getGermplasmListUploader() {
		return this.germplasmListUploader;
	}

	public void setGermplasmListUploader(final GermplasmListUploader germplasmListUploader) {
		this.germplasmListUploader = germplasmListUploader;
	}

	public Boolean getViaToolURL() {
		return this.viaToolURL;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public GermplasmFieldsComponent getGermplasmFieldsComponent() {
		return this.germplasmFieldsComponent;
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this.backButton, Message.BACK);
		this.messageSource.setCaption(this.nextButton, Message.FINISH);
	}

	@Override
	public void updateListUI() {
		// no implementation for this method
	}

	public void nextButtonClickAction() {
		if (this.validateSeedLocation() && this.validatePedigreeOption()) {
			this.processGermplasmAction.processGermplasm();
		}
	}

	public void saveTheList() {
		// Only require the user to create stock id if there is at least one row with inventory amount but STOCKID column is blank
		// from the imported file
		if (this.germplasmListUploader.hasAtLeastOneRowWithInventoryAmountButNoDefinedStockID()) {
			this.popupGenerateStockIdsDialog();
		} else {
			this.popupSaveAsDialog();
		}
	}

	public void popupSaveAsDialog() {

		this.germplasmList = new GermplasmList();

		final String sDate = DateUtil.formatDateAsStringValue(this.germplasmListUploader.getImportedGermplasmList().getDate(),
				DateUtil.DATE_AS_NUMBER_FORMAT);
		this.germplasmList.setName(this.germplasmListUploader.getImportedGermplasmList().getName());
		this.germplasmList.setDate(Long.parseLong(sDate));
		this.germplasmList.setType(this.germplasmListUploader.getImportedGermplasmList().getType());
		this.germplasmList.setDescription(this.germplasmListUploader.getImportedGermplasmList().getTitle());
		this.germplasmList.setStatus(1);
		this.germplasmList.setUserId(this.germplasmListUploader.getImportedGermplasmList().getUserId());
		try {
			this.germplasmList.setUserId(this.contextUtil.getCurrentWorkbenchUserId());
			this.germplasmList.setProgramUUID(this.contextUtil.getCurrentProgramUUID());
		} catch (final MiddlewareQueryException e) {
			SpecifyGermplasmDetailsComponent.LOG.error(e.getMessage(), e);
		}

		final List<GermplasmName> germplasmNameObjects = this.getGermplasmNameObjects();
		final List<GermplasmName> germplasmNameObjectsToBeSaved = new ArrayList<GermplasmName>();

		for (int i = 0; i < germplasmNameObjects.size(); i++) {
			final Integer gid = germplasmNameObjects.get(i).getGermplasm().getGid();
			if (this.processGermplasmAction.getMatchedGermplasmIds().contains(gid)) {
				// Get germplasm using temporarily set GID, then create map
				Germplasm germplasmToBeUsed;
				try {
					germplasmToBeUsed = this.germplasmDataManager.getGermplasmByGID(gid);
					germplasmNameObjectsToBeSaved.add(new GermplasmName(germplasmToBeUsed, germplasmNameObjects.get(i).getName()));
				} catch (final MiddlewareQueryException e) {
					SpecifyGermplasmDetailsComponent.LOG.error(e.getMessage(), e);
				}
			} else {
				germplasmNameObjectsToBeSaved
						.add(new GermplasmName(germplasmNameObjects.get(i).getGermplasm(), germplasmNameObjects.get(i).getName()));
			}
		}

		this.saveListAsDialog = new SaveListAsDialog(this, this.germplasmList);
		this.saveListAsDialog.setDebugId("saveListAsDialog");
		// If not from popup
		if (this.source.getGermplasmImportPopupSource() == null) {
			this.getWindow().addWindow(this.saveListAsDialog);
		} else {
			this.source.getGermplasmImportPopupSource().getParentWindow().addWindow(this.saveListAsDialog);
		}

	}

	public void popupGenerateStockIdsDialog() {

		this.generateStockIdsDialog = new GenerateStockIDsDialog(this, this.germplasmList);
		this.generateStockIdsDialog.setDebugId("generateStockIdsDialog");
		// If not from popup
		if (this.source.getGermplasmImportPopupSource() == null) {
			this.getWindow().addWindow(this.generateStockIdsDialog);
		} else {
			this.source.getGermplasmImportPopupSource().getParentWindow().addWindow(this.generateStockIdsDialog);
		}

	}

	private boolean validatePedigreeOption() {
		return BreedingManagerUtil.validateRequiredField(this.getWindow(), this.pedigreeOptionComboBox, this.messageSource,
				this.messageSource.getMessage(Message.PEDIGREE_OPTIONS_LABEL));
	}

	boolean validateSeedLocation() {
		// BMS-2645 : If the germplasm import file contains inventory, the system must require a location to be specified whether or not
		// StockID column is populated.
		if (this.germplasmListUploader.hasInventoryAmount()
				&& this.getGermplasmFieldsComponent().getSeedLocationComboBox().getValue() == null) {
			return BreedingManagerUtil.validateRequiredField(this.getWindow(), this.germplasmFieldsComponent.getSeedLocationComboBox(),
					this.messageSource, this.messageSource.getMessage(Message.SEED_STORAGE_LOCATION_LABEL));
		}
		return true;
	}

	protected void updateTotalEntriesLabel() {
		final int count = this.germplasmDetailsTable.getItemIds().size();
		if (count == 0) {
			this.totalEntriesLabel.setValue(this.messageSource.getMessage(Message.NO_LISTDATA_RETRIEVED_LABEL));
		} else {
			this.totalEntriesLabel.setValue(this.messageSource.getMessage(Message.TOTAL_LIST_ENTRIES) + ": " + "  <b>" + count + "</b>");
		}
	}

	public void backButtonClickAction() {
		this.source.backStep();
	}

	public GermplasmImportMain getSource() {
		return this.source;
	}

	public void setGermplasmBreedingMethod(final String breedingMethod) {
		this.germplasmFieldsComponent.setGermplasmBreedingMethod(breedingMethod);
	}

	public void setGermplasmDate(final Date germplasmDate) throws ParseException {
		this.germplasmFieldsComponent.setGermplasmDate(germplasmDate);
	}

	public void setGermplasmLocation(final String germplasmLocation) {
		this.germplasmFieldsComponent.setGermplasmLocation(germplasmLocation);
	}

	public void setGermplasmListType(final String germplasmListType) {
		this.germplasmFieldsComponent.setGermplasmListType(germplasmListType);
	}

	protected void initializePedigreeOptions() {
		this.pedigreeOptionComboBox.addItem(1);
		this.pedigreeOptionComboBox.addItem(2);
		this.pedigreeOptionComboBox.addItem(3);
		this.pedigreeOptionComboBox.setItemCaption(1, this.messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_ONE));
		this.pedigreeOptionComboBox.setItemCaption(2, this.messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_TWO));
		this.pedigreeOptionComboBox.setItemCaption(3, this.messageSource.getMessage(Message.IMPORT_PEDIGREE_OPTION_THREE));
	}

	protected void showFirstPedigreeOption(final boolean visible) {
		final Item firstOption = this.pedigreeOptionComboBox.getItem(1);
		if (firstOption == null && visible) {
			this.pedigreeOptionComboBox.removeAllItems();
			this.initializePedigreeOptions();
		} else if (!visible) {
			this.pedigreeOptionComboBox.removeItem(1);
		}
	}

	public Integer getPedigreeOptionGroupValue() {
		return (Integer) this.pedigreeOptionComboBox.getValue();
	}

	public String getPedigreeOption() {
		return this.pedigreeOptionComboBox.getValue().toString();
	}

	public List<GermplasmName> getGermplasmNameObjects() {
		return this.processGermplasmAction.getGermplasmNameObjects();
	}

	private List<Name> getNewNames() {
		return this.processGermplasmAction.getNewNames();
	}

	@Override
	public void instantiateComponents() {

		if (this.source.getGermplasmImportPopupSource() == null) {
			this.germplasmFieldsComponent = new GermplasmFieldsComponent(this.getWindow(), 200);
			this.germplasmFieldsComponent.setDebugId("germplasmFieldsComponent");
		} else {
			this.germplasmFieldsComponent =
					new GermplasmFieldsComponent(this.source.getGermplasmImportPopupSource().getParentWindow(), 200);
		}

		this.reviewImportDetailsLabel = new Label(this.messageSource.getMessage(Message.GERMPLASM_DETAILS_LABEL).toUpperCase());
		this.reviewImportDetailsLabel.setDebugId("reviewImportDetailsLabel");
		this.reviewImportDetailsLabel.addStyleName(Bootstrap.Typography.H4.styleName());

		this.reviewImportDetailsMessage = new Label(this.messageSource.getMessage(Message.REVIEW_IMPORT_DETAILS_MESSAGE));
		this.reviewImportDetailsMessage.setDebugId("reviewImportDetailsMessage");

		this.totalEntriesLabel = new Label("Total Entries: 0", Label.CONTENT_XHTML);
		this.totalEntriesLabel.setDebugId("totalEntriesLabel");

		this.initGermplasmDetailsTable();

		this.selectPedigreeOptionsLabel = new Label(this.messageSource.getMessage(Message.SELECT_GID_ASSIGNMENT_OPTIONS).toUpperCase());
		this.selectPedigreeOptionsLabel.setDebugId("selectPedigreeOptionsLabel");
		this.selectPedigreeOptionsLabel.addStyleName(Bootstrap.Typography.H4.styleName());

		this.pedigreeOptionsLabel = new Label(this.messageSource.getMessage(Message.PEDIGREE_OPTIONS_LABEL) + ":");
		this.pedigreeOptionsLabel.setDebugId("pedigreeOptionsLabel");
		this.pedigreeOptionsLabel.addStyleName(AppConstants.CssStyles.BOLD);
		this.pedigreeOptionsLabel.setWidth("250px");

		this.pedigreeOptionComboBox = new ComboBox();
		this.pedigreeOptionComboBox.setDebugId("pedigreeOptionComboBox");
		this.pedigreeOptionComboBox.setImmediate(true);
		this.pedigreeOptionComboBox.setRequired(true);
		this.pedigreeOptionComboBox.setWidth("450px");
		this.pedigreeOptionComboBox.setInputPrompt("Please Choose");

		this.automaticallyAcceptSingleMatchesCheckbox =
				new CheckBox(this.messageSource.getMessage(Message.AUTOMATICALLY_ACCEPT_SINGLE_MATCHES_WHENEVER_FOUND));
		this.automaticallyAcceptSingleMatchesCheckbox.setVisible(false);

		this.backButton = new Button();
		this.backButton.setDebugId("backButton");
		this.backButton.setData(SpecifyGermplasmDetailsComponent.BACK_BUTTON_ID);

		this.nextButton = new Button();
		this.nextButton.setDebugId("nextButton");
		this.nextButton.setData(SpecifyGermplasmDetailsComponent.NEXT_BUTTON_ID);
		this.nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
	}

	protected void initGermplasmDetailsTable() {
		if (this.getGermplasmDetailsTable() == null) {
			this.setGermplasmDetailsTable(new Table());
		}

		this.germplasmDetailsTable.setHeight("200px");
		this.germplasmDetailsTable.setWidth("700px");

		this.germplasmDetailsTable.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.germplasmDetailsTable.addContainerProperty(ColumnLabels.ENTRY_CODE.getName(), String.class, null);
		this.germplasmDetailsTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), String.class, null);
		this.germplasmDetailsTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
		this.germplasmDetailsTable.addContainerProperty(ColumnLabels.GID.getName(), Integer.class, null);
		this.germplasmDetailsTable.addContainerProperty(ColumnLabels.STOCKID, String.class, null);
		this.germplasmDetailsTable.addContainerProperty(ColumnLabels.AMOUNT, Double.class, null);
		this.germplasmDetailsTable.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);

		this.germplasmDetailsTable.setColumnCollapsingAllowed(true);

		this.germplasmDetailsTable.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.getTermNameFromOntology(ColumnLabels.ENTRY_ID));
		this.germplasmDetailsTable.setColumnHeader(ColumnLabels.ENTRY_CODE.getName(),
				this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE));
		this.germplasmDetailsTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(),
				this.getTermNameFromOntology(ColumnLabels.DESIGNATION));
		this.germplasmDetailsTable.setColumnHeader(ColumnLabels.PARENTAGE.getName(), this.getTermNameFromOntology(ColumnLabels.PARENTAGE));
		this.germplasmDetailsTable.setColumnHeader(ColumnLabels.GID.getName(), this.getTermNameFromOntology(ColumnLabels.GID));

		this.germplasmDetailsTable.setColumnHeader(ColumnLabels.STOCKID.getName(), this.getTermNameFromOntology(ColumnLabels.STOCKID));

		this.germplasmDetailsTable.setColumnHeader(ColumnLabels.AMOUNT.getName(), this.getTermNameFromOntology(ColumnLabels.AMOUNT));

		this.germplasmDetailsTable.setColumnHeader(ColumnLabels.SEED_SOURCE.getName(),
				this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE));

		this.germplasmDetailsTable.setColumnCollapsed(ColumnLabels.STOCKID, true);
		this.germplasmDetailsTable.setColumnCollapsed(ColumnLabels.AMOUNT, true);

	}

	protected String getTermNameFromOntology(final ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	@Override
	public void initializeValues() {
		// 2nd section
		this.initializePedigreeOptions();
	}

	@Override
	public void addListeners() {
		final GermplasmImportButtonClickListener clickListener = new GermplasmImportButtonClickListener(this);
		this.backButton.addListener(clickListener);
		this.nextButton.addListener(clickListener);

		this.processGermplasmAction = new ProcessImportedGermplasmAction(this);

		this.pedigreeOptionComboBox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = -1796753441697604604L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				SpecifyGermplasmDetailsComponent.this.toggleAcceptSingleMatchesCheckbox();
			}
		});

	}

	@Override
	public void layoutComponents() {
		this.setWidth("700px");

		// Review Import Details Layout
		final VerticalLayout importDetailsLayout = new VerticalLayout();
		importDetailsLayout.setDebugId("importDetailsLayout");
		importDetailsLayout.setSpacing(true);
		importDetailsLayout.setHeight("280px");
		importDetailsLayout.addComponent(this.reviewImportDetailsLabel);
		importDetailsLayout.addComponent(this.reviewImportDetailsMessage);
		importDetailsLayout.addComponent(this.totalEntriesLabel);
		importDetailsLayout.addComponent(this.germplasmDetailsTable);

		// Pedigree Options Layout
		final VerticalLayout pedigreeOptionsLayout = new VerticalLayout();
		pedigreeOptionsLayout.setDebugId("pedigreeOptionsLayout");
		pedigreeOptionsLayout.setSpacing(true);

		final VerticalLayout pedigreeControlsLayoutVL = new VerticalLayout();
		pedigreeControlsLayoutVL.setDebugId("pedigreeControlsLayoutVL");
		pedigreeControlsLayoutVL.setSpacing(true);
		pedigreeControlsLayoutVL.addComponent(this.pedigreeOptionComboBox);
		pedigreeControlsLayoutVL.addComponent(this.automaticallyAcceptSingleMatchesCheckbox);

		final HorizontalLayout pedigreeControlsLayout = new HorizontalLayout();
		pedigreeControlsLayout.setDebugId("pedigreeControlsLayout");
		pedigreeControlsLayout.addComponent(this.pedigreeOptionsLabel);
		pedigreeControlsLayout.addComponent(pedigreeControlsLayoutVL);

		pedigreeOptionsLayout.addComponent(this.selectPedigreeOptionsLabel);
		pedigreeOptionsLayout.addComponent(pedigreeControlsLayout);

		// Buttons Layout
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("40px");
		buttonLayout.setSpacing(true);

		buttonLayout.addComponent(this.backButton);
		buttonLayout.addComponent(this.nextButton);
		buttonLayout.setComponentAlignment(this.backButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.nextButton, Alignment.BOTTOM_LEFT);

		final VerticalLayout spacerLayout = new VerticalLayout();
		spacerLayout.setDebugId("spacerLayout");
		spacerLayout.setHeight("20px");
		final VerticalLayout spacerLayout1 = new VerticalLayout();
		spacerLayout1.setDebugId("spacerLayout1");
		spacerLayout1.setHeight("20px");
		final VerticalLayout spacerLayout2 = new VerticalLayout();
		spacerLayout2.setDebugId("spacerLayout2");
		spacerLayout2.setHeight("20px");

		this.addComponent(this.germplasmFieldsComponent);
		this.addComponent(spacerLayout);
		this.addComponent(importDetailsLayout);
		this.addComponent(spacerLayout1);
		this.addComponent(pedigreeOptionsLayout);
		this.addComponent(spacerLayout2);
		this.addComponent(buttonLayout);
	}

	protected void toggleAcceptSingleMatchesCheckbox() {
		// by default hide it
		this.automaticallyAcceptSingleMatchesCheckbox.setVisible(false);
		this.automaticallyAcceptSingleMatchesCheckbox.setValue(true);

		if (this.pedigreeOptionComboBox.getValue() != null) {
			final boolean selectGermplasmOptionChosen = this.pedigreeOptionComboBox.getValue().equals(3);
			this.automaticallyAcceptSingleMatchesCheckbox.setVisible(selectGermplasmOptionChosen);
		}
	}

	public void initializeFromImportFile(final ImportedGermplasmList importedGermplasmList) {

		this.importedGermplasmList = importedGermplasmList;
		/*
		 * Seed Storage Location will be shown whenever there is an Inventory Variable (i.e SEED_AMOUNT_G) in the Description The second
		 * parameter is for toggling the instructions shown when inventory amount is present
		 */
		this.getGermplasmFieldsComponent().refreshLayout(this.germplasmListUploader.hasInventoryVariable(),
				this.germplasmListUploader.hasInventoryAmount());

		// Clear table contents first (possible that it has some rows in it from previous uploads, and then user went back to upload screen)
		this.getGermplasmDetailsTable().removeAllItems();

		if (this.germplasmListUploader.hasStockIdFactor()) {
			this.getGermplasmDetailsTable().setColumnCollapsed(ColumnLabels.STOCKID, false);
		} else {
			this.getGermplasmDetailsTable().setColumnCollapsed(ColumnLabels.STOCKID, true);
		}
		if (this.germplasmListUploader.hasInventoryAmount()) {
			this.getGermplasmDetailsTable().setColumnCollapsed(ColumnLabels.AMOUNT, false);
		} else {
			this.getGermplasmDetailsTable().setColumnCollapsed(ColumnLabels.AMOUNT, true);
		}

		String germplasmSource;
		for (int i = 0; i < this.getImportedGermplasm().size(); i++) {
			final ImportedGermplasm importedGermplasm = this.getImportedGermplasm().get(i);
			germplasmSource = importedGermplasm.getSource();

			this.getGermplasmDetailsTable()
					.addItem(new Object[] {importedGermplasm.getEntryId(), importedGermplasm.getEntryCode(), importedGermplasm.getDesig(),
							importedGermplasm.getCross(), importedGermplasm.getGid(), importedGermplasm.getInventoryId(),
							importedGermplasm.getSeedAmount(), germplasmSource}, new Integer(i + 1));
		}

		this.updateTotalEntriesLabel();

		if (this.germplasmListUploader.importFileIsAdvanced()) {
			this.showFirstPedigreeOption(false);
		} else {
			this.showFirstPedigreeOption(true);
		}
		this.toggleAcceptSingleMatchesCheckbox();
	}

	@Override
	public void saveList(final GermplasmList list) {

		final SaveGermplasmListAction saveGermplasmListAction = new SaveGermplasmListAction();
		final Window window = this.source.getWindow();

		try {
			final Integer listId = saveGermplasmListAction.saveRecords(list, this.getGermplasmNameObjects(), this.getNewNames(),
					this.germplasmListUploader.getOriginalFilename(), this.processGermplasmAction.getMatchedGermplasmIds(),
					this.importedGermplasmList, this.getSeedStorageLocation());

			if (listId != null) {
				MessageNotifier.showMessage(window, this.messageSource.getMessage(Message.SUCCESS),
						this.messageSource.getMessage(Message.GERMPLASM_LIST_SAVED_SUCCESSFULLY), 3000);

				this.source.reset();

				// If not via popup
				if (this.source.getGermplasmImportPopupSource() == null) {
					this.source.backStep();
				} else {
					this.source.getGermplasmImportPopupSource().openSavedGermplasmList(list);
					this.source.getGermplasmImportPopupSource().refreshListTreeAfterListImport();
					this.source.getGermplasmImportPopupSource().getParentWindow()
							.removeWindow((Window) this.source.getComponentContainer());
				}

				if (this.source.isViaPopup()) {
					this.notifyExternalApplication(window, listId);
				}
			}

		} catch (final MiddlewareException e) {
			MessageNotifier.showError(window, "ERROR", "Error with saving germplasm list. Please see log for details.");
			SpecifyGermplasmDetailsComponent.LOG.error(e.getMessage(), e);
		} catch (final BreedingManagerException e) {
			MessageNotifier.showError(window, "ERROR", e.getMessage());
			SpecifyGermplasmDetailsComponent.LOG.error(e.getMessage(), e);
		}
	}

	private Integer getSeedStorageLocation() {
		Integer storageLocationId = 0;
		try {
			if (this.germplasmFieldsComponent.getSeedLocationComboBox() != null
					&& this.germplasmFieldsComponent.getSeedLocationComboBox().getValue() != null) {
				storageLocationId = Integer.valueOf(this.germplasmFieldsComponent.getSeedLocationComboBox().getValue().toString());
			}
		} catch (final NumberFormatException e) {
			SpecifyGermplasmDetailsComponent.LOG.error("Error ar SpecifyGermplasmDetailsComponent: getSeedStorageLocation() " + e);
		}

		return storageLocationId;
	}

	private void notifyExternalApplication(final Window window, final Integer listId) {
		if (window != null) {
			window.executeJavaScript("window.parent.closeImportFrame(" + listId + ");");
		}
	}

	@Override
	public void setCurrentlySavedGermplasmList(final GermplasmList list) {
		this.germplasmList = list;
	}

	@Override
	public Component getParentComponent() {
		return this.source;
	}

	public GermplasmList getGermplasmList() {
		return this.germplasmList;
	}

	public void closeSaveListAsDialog() {
		if (this.saveListAsDialog != null) {
			this.getWindow().removeWindow(this.saveListAsDialog);
		}
	}

	public Boolean automaticallyAcceptSingleMatchesCheckbox() {
		return (Boolean) this.automaticallyAcceptSingleMatchesCheckbox.getValue();
	}

	public ImportedGermplasmList getImportedGermplasmList() {
		return this.importedGermplasmList;
	}

	public void setGermplasmDetailsTable(final Table germplasmDetailsTable) {
		this.germplasmDetailsTable = germplasmDetailsTable;
	}

	public void setProcessGermplasmAction(final ProcessImportedGermplasmAction processGermplasmAction) {
		this.processGermplasmAction = processGermplasmAction;
	}

	public void setGermplasmFieldsComponent(final GermplasmFieldsComponent germplasmFieldsComponent) {
		this.germplasmFieldsComponent = germplasmFieldsComponent;
	}

	public void setContextUtil(final ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}

}
