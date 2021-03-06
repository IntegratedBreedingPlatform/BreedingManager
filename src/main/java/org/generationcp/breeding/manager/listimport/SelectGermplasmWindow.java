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

package org.generationcp.breeding.manager.listimport;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listeners.InventoryLinkButtonClickListener;
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.breeding.manager.listimport.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listimport.listeners.ImportGermplasmEntryActionListener;
import org.generationcp.breeding.manager.listmanager.ListBuilderComponent;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Configurable
public class SelectGermplasmWindow extends BaseSubWindow implements InitializingBean, InternationalizableComponent, BreedingManagerLayout,
		Window.CloseListener, ImportGermplasmEntryActionListener {

	public static final String USE_SAME_GID = "Use this match for other instances of this name in the import list";
	public static final String IGNORE_MATCHES = "Ignore matches and add a new entry";
	private static final Logger LOG = LoggerFactory.getLogger(SelectGermplasmWindow.class);

	private static final long serialVersionUID = -8113004135173349534L;

	public static final String CANCEL_BUTTON_ID = "SelectGermplasmWindow Cancel Button";
	public static final String DONE_BUTTON_ID = "SelectGermplasmWindow Done Button";

	private VerticalLayout mainLayout;
	private Button cancelButton;
	private Button doneButton;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PedigreeService pedigreeService;

	private String designation;
	private List<Germplasm> germplasmMatches;
	private int germplasmCount;
	private Table germplasmTable;
	private int germplasmIndex;

	private final ProcessImportedGermplasmAction source;

	private Label selectGermplasmLabel;

	private CheckBox ignoreRemainingMatchesCheckbox;
	private final Window parentWindow;
	private Integer noOfImportedGermplasm;
	private OptionGroup groupRadioBtn;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private InventoryDataManager inventoryDataManager;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	public SelectGermplasmWindow(final ProcessImportedGermplasmAction source, final String designation, final int index,
			final Window parentWindow) {
		this.designation = designation;
		this.germplasmIndex = index;
		this.source = source;
		this.parentWindow = parentWindow;
	}

	public SelectGermplasmWindow(final ProcessImportedGermplasmAction source, final String designation, final int index,
			final Window parentWindow, final Integer noOfImportedGermplasm) {
		this.designation = designation;
		this.germplasmIndex = index;
		this.source = source;
		this.parentWindow = parentWindow;
		this.noOfImportedGermplasm = noOfImportedGermplasm;
	}

	protected void assemble() {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public void doneAction() {

		if (this.useSameGidOptionSelected()) {
			this.source.mapDesignationToGermplasmForReuse(this.designation, this.germplasmIndex);
		}
		if (!this.ignoreMatchesOptionSelected()) {
			final Germplasm selectedGermplasm = this.germplasmDataManager.getGermplasmByGID((Integer) this.germplasmTable.getValue());
			this.source.receiveGermplasmFromWindowAndUpdateGermplasmData(this.germplasmIndex, selectedGermplasm);
		}
		this.source.removeListener(this);
		if (this.ignoreRemainingMatchesCheckbox.booleanValue()) {
			this.source.ignoreRemainingMatches();
		} else {
			this.source.processNextItems();
		}

		this.removeWindow(this);

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.assemble();
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		this.messageSource.setCaption(this, Message.SELECT_MATCHING_GERMPLASM_OR_ADD_NEW_ENTRY);
		this.messageSource.setCaption(this.doneButton, Message.CONTINUE);
		this.messageSource.setCaption(this.cancelButton, Message.CANCEL);
	}

	private String getGermplasmNames(final int gid) {
		final List<Name> names = this.germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
		final StringBuilder germplasmNames = new StringBuilder("");
		int i = 0;
		for (final Name n : names) {
			if (i < names.size() - 1) {
				germplasmNames.append(n.getNval() + ", ");
			} else {
				germplasmNames.append(n.getNval());
			}
			i++;
		}

		return germplasmNames.toString();
	}

	public void cancelButtonClickAction() {
		if (this.source instanceof ProcessImportedGermplasmAction) {
			this.source.closeAllImportEntryListeners();
		}
	}

	@Override
	public void instantiateComponents() {
		this.selectGermplasmLabel = new Label("", Label.CONTENT_XHTML);
		this.selectGermplasmLabel.setDebugId("selectGermplasmLabel");
		this.selectGermplasmLabel.setWidth("100%");

		this.cancelButton = new Button();
		this.cancelButton.setDebugId("cancelButton");
		this.cancelButton.setData(SelectGermplasmWindow.CANCEL_BUTTON_ID);

		this.doneButton = new Button();
		this.doneButton.setDebugId("doneButton");
		this.doneButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.doneButton.setEnabled(false);
		this.doneButton.setData(SelectGermplasmWindow.DONE_BUTTON_ID);

		this.initGermplasmTable();

		this.ignoreRemainingMatchesCheckbox = new CheckBox("Ignore remaining matches and add new entries for all");
		this.ignoreRemainingMatchesCheckbox.setDebugId("ignoreRemainingMatchesCheckbox");
		this.ignoreRemainingMatchesCheckbox.setImmediate(true);
		this.ignoreRemainingMatchesCheckbox.setEnabled(false);

		this.groupRadioBtn = new OptionGroup();
		this.groupRadioBtn.setDebugId("groupRadioBtn");
		this.groupRadioBtn.setMultiSelect(false);
		this.groupRadioBtn.setImmediate(true);
		this.groupRadioBtn.addItem(SelectGermplasmWindow.USE_SAME_GID);
		this.groupRadioBtn.addItem(SelectGermplasmWindow.IGNORE_MATCHES);
	}

	protected void initGermplasmTable() {
		this.setGermplasmTable(new Table());

		this.germplasmTable = this.getGermplasmTable();
		this.germplasmTable.setHeight("200px");
		this.germplasmTable.setWidth("750px");
		this.germplasmTable.setSelectable(true);
		this.germplasmTable.setMultiSelect(false);
		this.germplasmTable.setNullSelectionAllowed(false);
		this.germplasmTable.setImmediate(true);

		this.germplasmTable.addContainerProperty(ColumnLabels.DESIGNATION.getName(), Button.class, null);
		this.germplasmTable.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
		this.germplasmTable.addContainerProperty(ColumnLabels.IMMEDIATE_SOURCE_PREFERRED_NAME.getName(), String.class, null);
		this.germplasmTable.addContainerProperty(ColumnLabels.TOTAL.getName(), Button.class, null);
		this.germplasmTable.addContainerProperty(ColumnLabels.GERMPLASM_LOCATION.getName(), String.class, null);
		this.germplasmTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_NAME.getName(), String.class, null);
		this.germplasmTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);

		this.germplasmTable.setColumnHeader(ColumnLabels.DESIGNATION.getName(), this.getTermNameFromOntology(ColumnLabels.DESIGNATION));
		this.germplasmTable.setColumnHeader(ColumnLabels.GID.getName(), this.getTermNameFromOntology(ColumnLabels.GID));
		this.germplasmTable.setColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName(),
				this.getTermNameFromOntology(ColumnLabels.GERMPLASM_LOCATION));
		this.germplasmTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName(),
				this.getTermNameFromOntology(ColumnLabels.BREEDING_METHOD_NAME));
		this.germplasmTable.setColumnHeader(ColumnLabels.PARENTAGE.getName(), this.getTermNameFromOntology(ColumnLabels.PARENTAGE));

	}

	protected String getTermNameFromOntology(final ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	@Override
	public void addListeners() {
		this.germplasmTable.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				SelectGermplasmWindow.this.toggleContinueButton();
			}
		});

		this.groupRadioBtn.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				SelectGermplasmWindow.this.toggleGermplasmTable();
				SelectGermplasmWindow.this.toggleIgnoreRemainingCheckBox();
				SelectGermplasmWindow.this.toggleContinueButton();
			}
		});

		this.germplasmTable.addListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void itemClick(final ItemClickEvent event) {
				final boolean disableSelection = SelectGermplasmWindow.this.isDisabledSelection();
				if (!disableSelection) {
					final Object item = event.getItemId();
					if (item != null) {
						SelectGermplasmWindow.this.germplasmTable.select(item);
					}
				}
				SelectGermplasmWindow.this.toggleContinueButton();
			}
		});

		this.doneButton.addListener(new GermplasmImportButtonClickListener(this));
		this.doneButton.addListener(new CloseWindowAction(this));

		this.cancelButton.addListener(new CloseWindowAction(this));
	}

	protected void toggleGermplasmTable() {
		final boolean disableSelection = this.isDisabledSelection();
		if (disableSelection) {
			this.germplasmTable.setSelectable(false);
			this.germplasmTable.setNullSelectionAllowed(true);
			this.germplasmTable.unselect(this.germplasmTable.getValue());
			this.germplasmTable.select(null);
			this.germplasmTable.refreshRowCache();
			this.germplasmTable.requestRepaint();
			this.germplasmTable.setImmediate(true);
		} else {
			this.germplasmTable.setSelectable(true);
			this.germplasmTable.setNullSelectionAllowed(false);
			this.germplasmTable.setMultiSelect(false);
			this.germplasmTable.refreshRowCache();
			this.germplasmTable.requestRepaint();
			this.germplasmTable.setImmediate(true);
		}
	}

	private boolean isDisabledSelection() {
		return this.ignoreMatchesOptionSelected() && !this.useSameGidOptionSelected();
	}

	protected void toggleContinueButton() {
		final boolean enableButton = this.germplasmTable.getValue() != null || this.ignoreMatchesOptionSelected();
		if (enableButton) {
			this.doneButton.setEnabled(true);
		} else {
			this.doneButton.setEnabled(false);
		}
	}

	public void toggleIgnoreRemainingCheckBox() {
		final boolean enableCheckBox = this.ignoreMatchesOptionSelected();
		if (enableCheckBox) {
			this.ignoreRemainingMatchesCheckbox.setEnabled(true);
		} else {
			this.ignoreRemainingMatchesCheckbox.setEnabled(false);
			this.ignoreRemainingMatchesCheckbox.setValue(false);
		}
	}

	@Override
	public void initializeValues() {
		this.initializeGuideMessage();
	}

	@Override
	public void layoutComponents() {
		// set as modal window, other components are disabled while window is open
		this.setModal(true);
		// define window size, set as not resizable
		this.setWidth("800px");
		this.setHeight("460px");
		this.setResizable(false);
		this.addStyleName(Reindeer.WINDOW_LIGHT);

		// center window within the browser
		this.center();
		this.mainLayout = new VerticalLayout();
		this.mainLayout.setDebugId("selectGermplasmMainLayout");
		this.mainLayout.setMargin(true);
		this.mainLayout.setSpacing(true);
		this.mainLayout.addComponent(this.selectGermplasmLabel);
		this.mainLayout.addComponent(this.germplasmTable);

		// Buttons Layout
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setDebugId("buttonLayout");
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("40px");
		buttonLayout.setSpacing(true);

		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.doneButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.doneButton, Alignment.BOTTOM_LEFT);

		this.mainLayout.addComponent(this.groupRadioBtn);

		// Display 3rd check box i.e. ignoreRemainingMatchesCheckBox as sub step of 2nd Check box i.e. ignoreMatchesCheckBox so small gap is
		// inserted using label.
		final HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setDebugId("horizontalLayout");
		final Label gap = new Label();
		gap.setDebugId("gap");
		gap.setWidth("2em");
		horizontalLayout.addComponent(gap);
		horizontalLayout.addComponent(this.ignoreRemainingMatchesCheckbox);

		this.mainLayout.addComponent(horizontalLayout);
		this.mainLayout.addComponent(buttonLayout);

		this.setContent(this.mainLayout);
	}

	void initializeGuideMessage() {
		// Initialize label with germplasm index + 1, total number of imported germplasm & Germplasm Name
		this.selectGermplasmLabel.setValue(this.messageSource.getMessage(Message.GERMPLASM_MATCHES_LABEL,
				new Object[] {this.germplasmIndex + 1, this.noOfImportedGermplasm, this.designation}));
	}

	public void initializeTableValues() {
		this.germplasmCount = (int) this.germplasmDataManager.countGermplasmByName(this.designation, Operation.EQUAL);
		this.germplasmMatches = this.germplasmDataManager.getGermplasmByName(this.designation, 0, this.germplasmCount, Operation.EQUAL);

		// Collect gids of matched germplasm and make one-off lookup to cross expansions, locations, methods, preferred names
		final List<Integer> gids = new ArrayList<>();
		final List<Integer> gidsMaleParents = new ArrayList<>();
		for (final Germplasm germplasm : this.germplasmMatches) {
			gids.add(germplasm.getGid());
			gidsMaleParents.add(germplasm.getGpid2());
		}
		final Map<Integer, String> preferredNamesByGids = this.germplasmDataManager.getPreferredNamesByGids(gids);
		final Map<Integer, String> preferredNamesMaleParentsByGID = this.germplasmDataManager.getPreferredNamesByGids(gidsMaleParents);
		final Map<Integer, String> crossExpansionsMap =
				this.pedigreeService.getCrossExpansions(new HashSet<>(gids), null, this.crossExpansionProperties);
		final Map<Integer, String> locationNamesMap = this.germplasmDataManager.getLocationNamesByGids(gids);
		final Map<Integer, Object> methodsMap = this.germplasmDataManager.getMethodsByGids(gids);

		final Map<Integer, Germplasm> germplasmWithInventoryByGID =
			Maps.uniqueIndex(this.inventoryDataManager.getAvailableBalanceForGermplasms(this.germplasmMatches),
				new Function<Germplasm, Integer>() {

					@Nullable
					@Override
					public Integer apply(@Nullable final Germplasm germplasm) {
						return germplasm.getGid();
					}
				});

		for (int i = 0; i < this.germplasmMatches.size(); i++) {
			final Germplasm germplasm = this.germplasmMatches.get(i);
			final Integer gid = germplasm.getGid();

			final Button gidButton =
					new Button(String.format("%s", gid.toString()), new GidLinkClickListener(gid.toString(), this.parentWindow));
			gidButton.setStyleName(BaseTheme.BUTTON_LINK);

			final String preferredName = preferredNamesByGids.get(gid);
			final Button desigButton = new Button(preferredName, new GidLinkClickListener(gid.toString(), this.parentWindow));
			desigButton.setStyleName(BaseTheme.BUTTON_LINK);

			final String crossExpansion = crossExpansionsMap.get(gid);
			final String locationName = locationNamesMap.get(gid);
			String methodName = "";
			final Method method = (Method) methodsMap.get(gid);
			if (method != null && method.getMname() != null) {
				methodName = method.getMname();
			}

			String immediateSource = "-";
			if (germplasm.getGnpgs() == -1) {
				// only for Derivative and Maintenance lines
				immediateSource = preferredNamesMaleParentsByGID.get(germplasm.getGpid2());
			}
			final Germplasm germplasmWithInventory = germplasmWithInventoryByGID.get(gid);

			String available = "";
			if (germplasmWithInventory != null) {
				available = germplasmWithInventory.getInventoryInfo().getAvailable();
			}
			final Button availableButton = new Button(available.toString(), new InventoryLinkButtonClickListener(this.parentWindow, gid));
			availableButton.setStyleName(BaseTheme.BUTTON_LINK);
			availableButton.setDescription(ListBuilderComponent.CLICK_TO_VIEW_INVENTORY_DETAILS);

			this.germplasmTable.addItem(new Object[] {desigButton, gidButton,
				immediateSource, availableButton, locationName, methodName, crossExpansion}, gid);
		}

		this.germplasmTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

			private static final long serialVersionUID = 1L;

			@Override
			public String generateDescription(final Component source, final Object itemId, final Object propertyId) {
				if (propertyId == ColumnLabels.DESIGNATION.getName()) {
					final Item item = SelectGermplasmWindow.this.germplasmTable.getItem(itemId);
					final Integer gid =
							Integer.valueOf(((Button) item.getItemProperty(ColumnLabels.GID.getName()).getValue()).getCaption());
					return SelectGermplasmWindow.this.getGermplasmNames(gid);
				} else {
					return null;
				}
			}
		});

	}

	@Override
	public void windowClose(final CloseEvent e) {
		super.close();
		this.source.closeAllImportEntryListeners();
	}

	@Override
	public String getDesignation() {
		return this.designation;
	}

	public void setDesignation(final String designation) {
		this.designation = designation;
	}

	@Override
	public int getGermplasmIndex() {
		return this.germplasmIndex;
	}

	public void setGermplasmIndex(final int germplasmIndex) {
		this.germplasmIndex = germplasmIndex;
	}

	public Table getGermplasmTable() {
		return this.germplasmTable;
	}

	public void setGermplasmTable(final Table germplasmTable) {
		this.germplasmTable = germplasmTable;
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	public InventoryDataManager getInventoryDataManager() {
		return inventoryDataManager;
	}

	public void setInventoryDataManager(final InventoryDataManager inventoryDataManager) {
		this.inventoryDataManager = inventoryDataManager;
	}

	public void setPedigreeService(final PedigreeService pedigreeService) {
		this.pedigreeService = pedigreeService;
	}

	public Integer getNoOfImportedGermplasm() {
		return this.noOfImportedGermplasm;
	}

	public void setSelectGermplasmLabel(final Label selectGermplasmLabel) {
		this.selectGermplasmLabel = selectGermplasmLabel;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	private boolean ignoreMatchesOptionSelected() {
		return SelectGermplasmWindow.IGNORE_MATCHES.equals(this.groupRadioBtn.getValue());
	}

	private boolean useSameGidOptionSelected() {
		return SelectGermplasmWindow.USE_SAME_GID.equals(this.groupRadioBtn.getValue());
	}

	public OptionGroup getGroupRadioBtn() {
		return this.groupRadioBtn;
	}

	public CheckBox getIgnoreRemainingMatchesCheckbox() {
		return this.ignoreRemainingMatchesCheckbox;
	}
}
