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

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.breeding.manager.listimport.listeners.CloseWindowAction;
import org.generationcp.breeding.manager.listimport.listeners.GermplasmImportButtonClickListener;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.listimport.listeners.ImportGermplasmEntryActionListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Dennis Billano
 *
 */
@Configurable
public class SelectGermplasmWindow extends BaseSubWindow implements InitializingBean, InternationalizableComponent, BreedingManagerLayout,
Window.CloseListener, ImportGermplasmEntryActionListener {

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

	@Autowired
	private LocationDataManager locationDataManager;

	private String germplasmName;
	private List<Germplasm> germplasms;
	private int germplasmCount;
	private Table germplasmTable;
	private int germplasmIndex;
	private Germplasm germplasm;

	private final ProcessImportedGermplasmAction source;

	private Label selectGermplasmLabel;

	private CheckBox useSameGidCheckbox;
	private CheckBox ignoreMatchesCheckbox;
	private CheckBox ignoreRemainingMatchesCheckbox;
	private final Window parentWindow;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	public SelectGermplasmWindow(ProcessImportedGermplasmAction source, String germplasmName, int index, Germplasm germplasm,
			Window parentWindow) {
		this.germplasmName = germplasmName;
		this.germplasmIndex = index;
		this.germplasm = germplasm;
		this.source = source;
		this.parentWindow = parentWindow;
	}

	protected void assemble() {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public void doneAction() {
		try {
			if (this.useSameGidCheckbox.booleanValue()) {
				if (this.source.getNameGermplasmMap() == null) {
					this.source.setNameGermplasmMap(new HashMap<String, Germplasm>());
				}
				this.source.mapGermplasmNamesToGermplasm(this.germplasmName, this.germplasm);
			}
			if (!this.ignoreMatchesCheckbox.booleanValue()) {
				Germplasm selectedGermplasm = this.germplasmDataManager.getGermplasmByGID((Integer) this.germplasmTable.getValue());
				this.source.receiveGermplasmFromWindowAndUpdateGermplasmData(this.germplasmIndex, this.germplasm, selectedGermplasm);
			}
			this.source.removeListener(this);
			if (this.ignoreRemainingMatchesCheckbox.booleanValue()) {
				this.source.ignoreRemainingMatches();
			} else {
				this.source.processNextItems();
			}
			this.removeWindow(this);
		} catch (MiddlewareQueryException e) {
			SelectGermplasmWindow.LOG.error(e.getMessage(), e);
		}
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

	private String getGermplasmNames(int gid) {

		try {
			List<Name> names = this.germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
			StringBuilder germplasmNames = new StringBuilder("");
			int i = 0;
			for (Name n : names) {
				if (i < names.size() - 1) {
					germplasmNames.append(n.getNval() + ", ");
				} else {
					germplasmNames.append(n.getNval());
				}
				i++;
			}

			return germplasmNames.toString();
		} catch (MiddlewareQueryException e) {
			SelectGermplasmWindow.LOG.error(e.getMessage(), e);
			return null;
		}
	}

	public void cancelButtonClickAction() {
		if (this.source instanceof ProcessImportedGermplasmAction) {
			this.source.closeAllImportEntryListeners();
		}
	}

	@Override
	public void instantiateComponents() {
		this.selectGermplasmLabel = new Label("", Label.CONTENT_XHTML);
		this.selectGermplasmLabel.setWidth("100%");

		this.cancelButton = new Button();
		this.cancelButton.setData(SelectGermplasmWindow.CANCEL_BUTTON_ID);

		this.doneButton = new Button();
		this.doneButton.setStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.doneButton.setEnabled(false);
		this.doneButton.setData(SelectGermplasmWindow.DONE_BUTTON_ID);

		this.initGermplasmTable();

		this.useSameGidCheckbox = new CheckBox("Use this match for other instances of this name in the import list");
		this.useSameGidCheckbox.setImmediate(true);
		this.ignoreMatchesCheckbox = new CheckBox("Ignore matches and add a new entry");
		this.ignoreMatchesCheckbox.setImmediate(true);
		this.ignoreRemainingMatchesCheckbox = new CheckBox("Ignore remaining matches and add new entries for all");
		this.ignoreRemainingMatchesCheckbox.setImmediate(true);
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

	protected String getTermNameFromOntology(ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	@Override
	public void addListeners() {
		this.germplasmTable.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				SelectGermplasmWindow.this.toggleContinueButton();
			}
		});

		this.useSameGidCheckbox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				SelectGermplasmWindow.this.toggleGermplasmTable();
			}
		});

		this.ignoreMatchesCheckbox.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				SelectGermplasmWindow.this.toggleContinueButton();
				SelectGermplasmWindow.this.toggleGermplasmTable();
			}
		});

		this.doneButton.addListener(new GermplasmImportButtonClickListener(this));
		this.doneButton.addListener(new CloseWindowAction(this));

		this.cancelButton.addListener(new CloseWindowAction(this));
	}

	protected void toggleGermplasmTable() {
		boolean disableSelection = this.ignoreMatchesCheckbox.booleanValue() && !this.useSameGidCheckbox.booleanValue();
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

	protected void toggleContinueButton() {
		boolean enableButton = this.germplasmTable.getValue() != null || this.ignoreMatchesCheckbox.booleanValue();
		if (enableButton) {
			this.doneButton.setEnabled(true);
		} else {
			this.doneButton.setEnabled(false);
		}
	}

	@Override
	public void initializeValues() {
		this.initializeGuideMessage();
		this.initializeTableValues();
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
		this.mainLayout.setMargin(true);
		this.mainLayout.setSpacing(true);
		this.mainLayout.addComponent(this.selectGermplasmLabel);
		this.mainLayout.addComponent(this.germplasmTable);

		// Buttons Layout
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setWidth("100%");
		buttonLayout.setHeight("40px");
		buttonLayout.setSpacing(true);

		buttonLayout.addComponent(this.cancelButton);
		buttonLayout.addComponent(this.doneButton);
		buttonLayout.setComponentAlignment(this.cancelButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setComponentAlignment(this.doneButton, Alignment.BOTTOM_LEFT);

		this.mainLayout.addComponent(this.useSameGidCheckbox);
		this.mainLayout.addComponent(this.ignoreMatchesCheckbox);
		this.mainLayout.addComponent(this.ignoreRemainingMatchesCheckbox);

		this.mainLayout.addComponent(buttonLayout);

		this.setContent(this.mainLayout);
	}

	private void initializeGuideMessage() {
		this.selectGermplasmLabel.setValue("Matches were found with the name <b>" + this.germplasmName
				+ "</b>. Click on an entry below to choose it as a match. "
				+ "You can also choose to ignore the match and add a new entry.");
	}

	protected void initializeTableValues() {
		try {
			this.germplasmCount = (int) this.germplasmDataManager.countGermplasmByName(this.germplasmName, Operation.EQUAL);
			this.germplasms = this.germplasmDataManager.getGermplasmByName(this.germplasmName, 0, this.germplasmCount, Operation.EQUAL);
			for (int i = 0; i < this.germplasms.size(); i++) {

				Germplasm currentGermplasm = this.germplasms.get(i);
				Location location = this.locationDataManager.getLocationByID(currentGermplasm.getLocationId());
				Method method = this.germplasmDataManager.getMethodByID(currentGermplasm.getMethodId());
				Name preferredName = this.germplasmDataManager.getPreferredNameByGID(currentGermplasm.getGid());

				Button gidButton =
						new Button(String.format("%s", currentGermplasm.getGid().toString()), new GidLinkClickListener(currentGermplasm
								.getGid().toString(), this.parentWindow));
				gidButton.setStyleName(BaseTheme.BUTTON_LINK);

				Button desigButton =
						new Button(preferredName.getNval(), new GidLinkClickListener(currentGermplasm.getGid().toString(),
								this.parentWindow));
				desigButton.setStyleName(BaseTheme.BUTTON_LINK);

				String crossExpansion = "";
				if (currentGermplasm != null) {
					try {
						if (this.germplasmDataManager != null) {
							crossExpansion =
									this.pedigreeService.getCrossExpansion(currentGermplasm.getGid(), this.crossExpansionProperties);
						}
					} catch (MiddlewareQueryException ex) {
						crossExpansion = "-";
					}
				}

				String locationName = "";
				if (location != null && location.getLname() != null) {
					locationName = location.getLname();
				}

				String methodName = "";
				if (method != null && method.getMname() != null) {
					methodName = method.getMname();
				}

				this.germplasmTable.addItem(new Object[] {desigButton, gidButton, locationName, methodName, crossExpansion},
						currentGermplasm.getGid());
			}

			this.germplasmTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

				private static final long serialVersionUID = 1L;

				@Override
				public String generateDescription(Component source, Object itemId, Object propertyId) {
					if (propertyId == ColumnLabels.DESIGNATION.getName()) {
						Item item = SelectGermplasmWindow.this.germplasmTable.getItem(itemId);
						Integer gid = Integer.valueOf(((Button) item.getItemProperty(ColumnLabels.GID.getName()).getValue()).getCaption());
						return SelectGermplasmWindow.this.getGermplasmNames(gid);
					} else {
						return null;
					}
				}
			});

		} catch (MiddlewareQueryException e) {
			SelectGermplasmWindow.LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public String getGermplasmName() {
		return this.germplasmName;
	}

	public void setGermplasmName(String germplasmName) {
		this.germplasmName = germplasmName;
	}

	public Germplasm getGermplasm() {
		return this.germplasm;
	}

	public void setGermplasm(Germplasm germplasm) {
		this.germplasm = germplasm;
	}

	@Override
	public int getGermplasmIndex() {
		return this.germplasmIndex;
	}

	public void setGermplasmIndex(int germplasmIndex) {
		this.germplasmIndex = germplasmIndex;
	}

	public Table getGermplasmTable() {
		return this.germplasmTable;
	}

	public void setGermplasmTable(Table germplasmTable) {
		this.germplasmTable = germplasmTable;
	}

	public void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	@Override
	public void windowClose(CloseEvent e) {
		super.close();
		this.source.closeAllImportEntryListeners();
	}

}
