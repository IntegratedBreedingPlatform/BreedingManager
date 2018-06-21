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

package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.actions.SaveCrossesMadeAction;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerActionHandler;
import org.generationcp.breeding.manager.crossingmanager.listeners.PreviewCrossesTabCheckBoxListener;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.settings.ApplyCrossingSettingAction;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkClickListener;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCross;
import org.generationcp.breeding.manager.util.BreedingManagerTransformationUtil;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.commons.service.impl.SeedSourceGenerator;
import org.generationcp.commons.util.CollectionTransformationUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.Util;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * This class contains UI components and functions related to Crosses Made table in Make Crosses screen in Crossing Manager
 *
 */
@Configurable
public class MakeCrossesTableComponent extends VerticalLayout
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, SaveListAsDialogSource {

	private static final int PARENTS_TABLE_ROW_COUNT = 5;
	public static final String PARENTS_DELIMITER = ",";
	private static final long serialVersionUID = 3702324761498666369L;
	private static final Logger LOG = LoggerFactory.getLogger(MakeCrossesTableComponent.class);
	private static final String TAG_COLUMN_ID = "Tag";
	private static final String FEMALE_CROSS = "FEMALE CROSS";
	private static final String MALE_CROSS = "MALE CROSS";

	private static final String CLICK_TO_VIEW_GERMPLASM_INFORMATION = "Click to view Germplasm information";

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Resource
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private SeedSourceGenerator seedSourceGenerator;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private PedigreeService pedigreeService;

	private Label lblReviewCrosses;

	private Label totalCrossesLabel;
	private Label totalSelectedCrossesLabel;

	private ContextMenu actionMenu;

	// Tables
	private TableWithSelectAllLayout tableWithSelectAllLayout;
	private Table tableCrossesMade;
	@SuppressWarnings("unused")
	private CheckBox selectAll;

	private Button actionButton;

	private CrossingManagerActionHandler crossingManagerActionListener;

	private GermplasmList crossList;

	private String separator;

	private final CrossingManagerMakeCrossesComponent makeCrossesMain;

	public MakeCrossesTableComponent(final CrossingManagerMakeCrossesComponent makeCrossesMain) {
		this.makeCrossesMain = makeCrossesMain;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}

	@Override
	public void updateLabels() {
		// no implementation for this method
	}

	/**
	 * Crosses each item on first list with its counterpart (same index or position) on second list. Assumes that checking if list sizes are
	 * equal was done beforehand. The generated crossings are then added to Crossings Table.
	 *
	 * @param parents1 - list of GermplasmList entries as first parents
	 * @param parents2 - list of GermplasmList entries as second parents
	 * @param listnameMaleParent
	 * @param listnameFemaleParent
	 * @param excludeSelf
	 */
	public void makeTopToBottomCrosses(final List<GermplasmListEntry> parents1, final List<GermplasmListEntry> parents2,
			final String listnameFemaleParent, final String listnameMaleParent, final boolean excludeSelf) {
		// make a copy first of the parents lists
		final List<GermplasmListEntry> femaleParents = new ArrayList<>();
		final List<GermplasmListEntry> maleParents = new ArrayList<>();
		femaleParents.addAll(parents1);
		maleParents.addAll(parents2);

		final ImmutableMap<Integer, Germplasm> germplasmWithPreferredName = getGermplasmWithPreferredNameForBothParents(femaleParents, maleParents);
		 
		final Map<Integer, String> parentsPedigreeString = pedigreeService.getCrossExpansions(germplasmWithPreferredName.keySet(), null, crossExpansionProperties);
		
		final ListIterator<GermplasmListEntry> femaleListIterator = femaleParents.listIterator();
		final ListIterator<GermplasmListEntry> maleListIterator = maleParents.listIterator();

		this.separator = this.makeCrossesMain.getSeparatorString();
		final Set<CrossParents> existingCrosses = new HashSet<>();

		while (femaleListIterator.hasNext()) {
			final GermplasmListEntry femaleParent = femaleListIterator.next();
			final GermplasmListEntry maleParent = maleListIterator.next();
			this.addItemToMakeCrossesTable(listnameFemaleParent, listnameMaleParent, excludeSelf, femaleParent, maleParent,
					existingCrosses, germplasmWithPreferredName, parentsPedigreeString);
		}
		this.updateCrossesMadeUI();

	}

	private ImmutableMap<Integer, Germplasm> getGermplasmWithPreferredNameForBothParents(final List<GermplasmListEntry> femaleParents,
			final List<GermplasmListEntry> maleParents) {
		final Set<Integer> germplasmListEntries = getAllGidsFromParents(femaleParents, maleParents);
		final List<Germplasm> germplasmWithAllNamesAndAncestry =
				germplasmDataManager.getGermplasmWithAllNamesAndAncestry(germplasmListEntries, 0);
		return CollectionTransformationUtil.getGermplasmMap(germplasmWithAllNamesAndAncestry);
	}

	private Set<Integer> getAllGidsFromParents(final List<GermplasmListEntry> femaleParents, final List<GermplasmListEntry> maleParents) {
		return new ImmutableSet.Builder<Integer>().addAll(BreedingManagerTransformationUtil.getAllGidsFromGermplasmEntry(femaleParents))
				.addAll(BreedingManagerTransformationUtil.getAllGidsFromGermplasmEntry(maleParents)).build();
	}

	void addItemToMakeCrossesTable(
		final String listnameFemaleParent, final String listnameMaleParent, final boolean excludeSelf,
		final GermplasmListEntry femaleParent, final GermplasmListEntry maleParent, final Set<CrossParents> existingCrosses,
		final Map<Integer, Germplasm> germplasmWithPreferredName, final Map<Integer, String> pedigreeString) {

		final String femaleSeedSource = listnameFemaleParent + ":" + femaleParent.getEntryId();
		final String maleSeedSource = listnameMaleParent + ":" + maleParent.getEntryId();
		final GermplasmListEntry femaleParentCopy = femaleParent.copy();
		femaleParentCopy.setSeedSource(femaleSeedSource);
		final GermplasmListEntry maleParentCopy = maleParent.copy();
		maleParentCopy.setSeedSource(maleSeedSource);

		final CrossParents parents = new CrossParents(femaleParentCopy, maleParentCopy);
		final String seedSource = this.generateSeedSource(femaleParent.getGid(), femaleSeedSource, maleParent.getGid(), maleSeedSource);

		if (shouldBeAddedToCrossesTable(parents, existingCrosses, excludeSelf, femaleParent, maleParent)) {
			final int entryCounter = this.tableCrossesMade.size() + 1;
			final String femalePreferredName = getGermplasmPreferredName(germplasmWithPreferredName.get(femaleParent.getGid()));
			final String maleParentPreferredName = getGermplasmPreferredName(germplasmWithPreferredName.get(maleParent.getGid()));
			final String femaleParentPedigreeString = pedigreeString.get(femaleParent.getGid());
			final String maleParentPedigreeString = pedigreeString.get(maleParent.getGid());

			final Button designationFemaleParentButton = new Button(femalePreferredName, new GidLinkClickListener(femaleParent.getGid().toString(), true));
			designationFemaleParentButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationFemaleParentButton.setDescription(CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			final Button designationMaleParentMaleButton = new Button(maleParentPreferredName, new GidLinkClickListener(maleParent.getGid().toString(), true));
			designationMaleParentMaleButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationMaleParentMaleButton.setDescription(CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			final CheckBox tag = new CheckBox();
			tag.setDebugId(TAG_COLUMN_ID);
			tag.addListener(new PreviewCrossesTabCheckBoxListener(tableCrossesMade, parents, this.tableWithSelectAllLayout.getCheckBox()));
			tag.setImmediate(true);

			Object[] item = new Object[] {
				tag, entryCounter, designationFemaleParentButton,
				designationMaleParentMaleButton, femaleParentPedigreeString, maleParentPedigreeString, seedSource};

			this.tableCrossesMade.addItem(item, parents);
			existingCrosses.add(parents);
		}

	}

	private void setMakeCrossesTableVisibleColumn() {
		this.tableCrossesMade.setVisibleColumns(new Object[] {
			TAG_COLUMN_ID, ColumnLabels.ENTRY_ID.getName(), ColumnLabels.FEMALE_PARENT.getName(),
			ColumnLabels.MALE_PARENT.getName(), FEMALE_CROSS,
			MALE_CROSS, ColumnLabels.SEED_SOURCE.getName()});
	}

	private void updateCrossesMadeUI() {
		final int crossesCount = this.tableCrossesMade.size();
		this.generateTotalCrossesLabel(crossesCount);
		this.updateCrossesMadeSaveButton();
		this.makeCrossesMain.toggleStudyBackButton();

		this.tableCrossesMade.setPageLength(0);
		this.tableCrossesMade.requestRepaint();
	}

	public void updateCrossesMadeSaveButton() {
		if (this.tableCrossesMade.getItemIds() == null) {
			return;
		}

	}

	/**
	 * Multiplies each item on first list with each item on second list. The generated crossings are then added to Crossings Table.
	 *
	 * @param parents1 - list of GermplasmList entries as first parents
	 * @param parents2 - list of GermplasmList entries as second parents
	 * @param listnameMaleParent
	 * @param listnameFemaleParent
	 * @param excludeSelf
	 */
	public void multiplyParents(final List<GermplasmListEntry> parents1, final List<GermplasmListEntry> parents2,
			final String listnameFemaleParent, final String listnameMaleParent, final boolean excludeSelf) {

		// make a copy first of the parents lists
		final List<GermplasmListEntry> femaleParents = new ArrayList<>();
		final List<GermplasmListEntry> maleParents = new ArrayList<>();
		femaleParents.addAll(parents1);
		maleParents.addAll(parents2);

		this.setMakeCrossesTableVisibleColumn();
		this.separator = this.makeCrossesMain.getSeparatorString();
		
		final ImmutableMap<Integer, Germplasm> germplasmWithPreferredName = getGermplasmWithPreferredNameForBothParents(femaleParents, maleParents);
		final Map<Integer, String> parentsPedigreeString = pedigreeService.getCrossExpansions(germplasmWithPreferredName.keySet(), null, crossExpansionProperties);

		final Set<CrossParents> existingCrosses = new HashSet<>();
		for (final GermplasmListEntry femaleParent : femaleParents) {

			final String femaleSource = listnameFemaleParent + ":" + femaleParent.getEntryId();
			final GermplasmListEntry femaleParentCopy = femaleParent.copy();
			femaleParentCopy.setSeedSource(femaleSource);

			for (final GermplasmListEntry maleParent : maleParents) {
				final String maleSource = listnameMaleParent + ":" + maleParent.getEntryId();
				final GermplasmListEntry maleParentCopy = maleParent.copy();
				maleParentCopy.setSeedSource(maleSource);

				final CrossParents parents = new CrossParents(femaleParentCopy, maleParentCopy);

				this.addItemToMakeCrossesTable(excludeSelf, femaleParent, femaleSource, maleParent, maleSource, 
						parents, existingCrosses, germplasmWithPreferredName, parentsPedigreeString);
			}
		}
		this.updateCrossesMadeUI();
	}

	void addItemToMakeCrossesTable(
		final boolean excludeSelf, final GermplasmListEntry femaleParent, final String femaleSource,
		final GermplasmListEntry maleParent, final String maleSource, final CrossParents parents, final Set<CrossParents> existingCrosses,
		final Map<Integer, Germplasm> germplasmWithPreferredName, final Map<Integer, String> parentsPedigreeString) {

		if (shouldBeAddedToCrossesTable(parents, existingCrosses, excludeSelf, femaleParent, maleParent)
			) {
			int entryCounter = this.tableCrossesMade.size() + 1;
			final String femaleParentPedigreeString = parentsPedigreeString.get(femaleParent.getGid());
			final String maleParentPedigreeString = parentsPedigreeString.get(maleParent.getGid());
			final String femalePreferredName = getGermplasmPreferredName(germplasmWithPreferredName.get(femaleParent.getGid()));
			final String maleParentPreferredName = getGermplasmPreferredName(germplasmWithPreferredName.get(maleParent.getGid()));
			final String seedSource = this.generateSeedSource(femaleParent.getGid(), femaleSource, maleParent.getGid(), maleSource);

			final Button designationFemaleParentButton = new Button(femalePreferredName, new GidLinkClickListener(femaleParent.getGid().toString(), true));
			designationFemaleParentButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationFemaleParentButton.setDescription(CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			final Button designationMaleParentMaleButton = new Button(maleParentPreferredName, new GidLinkClickListener(maleParent.getGid().toString(), true));
			designationMaleParentMaleButton.setStyleName(BaseTheme.BUTTON_LINK);
			designationMaleParentMaleButton.setDescription(CLICK_TO_VIEW_GERMPLASM_INFORMATION);

			final CheckBox tag = new CheckBox();
			tag.setDebugId(TAG_COLUMN_ID);
			tag.addListener(new PreviewCrossesTabCheckBoxListener(this.tableCrossesMade, parents, this.tableWithSelectAllLayout.getCheckBox()));
			tag.setImmediate(true);

			final Object[] item = new Object[] {
				tag, entryCounter, designationFemaleParentButton,
				designationMaleParentMaleButton, femaleParentPedigreeString, maleParentPedigreeString, seedSource};

			this.tableCrossesMade.addItem(item, parents);
			existingCrosses.add(parents);
		}
	}

	private boolean shouldBeAddedToCrossesTable(CrossParents parents, Set<CrossParents> existingCrosses, boolean excludeSelf,
		GermplasmListEntry femaleParent, GermplasmListEntry maleParent) {
		return !existingCrosses.contains(parents) && (this.tableCrossesMade.size() == 0 || this.tableCrossesMade.getItem(parents) == null)
			&& (excludeSelf && !this.hasSameParent(femaleParent, maleParent) || !excludeSelf);
	}

	String generateSeedSource(final Integer femaleParentGid, final String femaleSource, final Integer maleParentGid,
		final String maleSource) {

		// Default as before
		String seedSource = this.appendWithSeparator(femaleSource, maleSource);

		// If crossing for a Nursery, use the seed source generation service.
		final Workbook workbook = this.makeCrossesMain.getWorkbook();
		if (workbook != null) {
			String malePlotNo = "";
			String femalePlotNo = "";

			// Look at the observation rows of Nursery to find plot number assigned to the male/female parent germplasm of the cross.
			for (final MeasurementRow row : workbook.getObservations()) {
				final MeasurementData gidData = row.getMeasurementData(TermId.GID.getId());
				final MeasurementData plotNumberData = row.getMeasurementData(TermId.PLOT_NO.getId());

				if (gidData != null && gidData.getValue().equals(femaleParentGid.toString()) && plotNumberData != null) {
					femalePlotNo = plotNumberData.getValue();
				}

				if (gidData != null && gidData.getValue().equals(maleParentGid.toString()) && plotNumberData != null) {
					malePlotNo = plotNumberData.getValue();
				}
			}

			// Single nursery is in context here, so set the same study name as both male/female parts. For import crosses case, these
			// could be different Nurseries.
			seedSource = this.seedSourceGenerator.generateSeedSourceForCross(workbook, malePlotNo, femalePlotNo,
				workbook.getStudyName(), workbook.getStudyName());
		}
		return seedSource;
	}

	boolean hasSameParent(final GermplasmListEntry femaleParent, final GermplasmListEntry maleParent) {
		return femaleParent.getGid().intValue() == maleParent.getGid().intValue();
	}

	private String getGermplasmPreferredName(Germplasm germplasm) {
		if(germplasm != null && germplasm.getPreferredName() != null && StringUtils.isNotBlank(germplasm.getPreferredName().getNval())) {
			return germplasm.getPreferredName().getNval();
		}
		return "Unknown";
	}

	// Action handler for Delete Selected Crosses context menu option
	public void deleteCrossAction() {
		final Collection<?> selectedIds = (Collection<?>) this.tableCrossesMade.getValue();
		if (!selectedIds.isEmpty()) {
			for (final Object itemId : selectedIds) {
				this.tableCrossesMade.removeItem(itemId);
			}
			this.tableCrossesMade.setPageLength(0);
		} else {
			MessageNotifier.showWarning(this.getWindow(), "Warning!", this.messageSource.getMessage(Message.ERROR_CROSS_MUST_BE_SELECTED));
		}

		this.updateCrossesMadeUI();
	}

	private Map<Germplasm, Name> generateCrossesMadeMap() {
		final Map<Germplasm, Name> crossesMadeMap = new LinkedHashMap<>();

		// get ID of User Defined Field for Crossing Name
		final Integer crossingNameTypeId =
				BreedingManagerUtil.getIDForUserDefinedFieldCrossingName(this.germplasmListManager, this.getWindow(), this.messageSource);

		int ctr = 1;
		for (final Object itemId : this.tableCrossesMade.getItemIds()) {
			final Property crossSourceProp = this.tableCrossesMade.getItem(itemId).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
			final String crossSource = String.valueOf(crossSourceProp.toString());

			// get GIDs and entryIDs of female and male parents
			final CrossParents parents = (CrossParents) itemId;
			final Integer gpId1 = parents.getFemaleParent().getGid();
			final Integer gpId2 = parents.getMaleParent().getGid();
			final Integer entryId1 = parents.getFemaleParent().getEntryId();
			final Integer entryId2 = parents.getMaleParent().getEntryId();

			final Germplasm germplasm = new Germplasm();
			germplasm.setGid(ctr);
			germplasm.setGpid1(gpId1);
			germplasm.setGpid2(gpId2);

			final Name name = new Name();
			name.setNval("," + crossSource);
			name.setTypeId(crossingNameTypeId);

			final ImportedGermplasmCross cross = new ImportedGermplasmCross();
			cross.setCross(ctr);
			cross.setFemaleGId(gpId1);
			cross.setMaleGId(gpId2);
			cross.setFemaleEntryId(entryId1);
			cross.setMaleEntryId(entryId2);
			cross.setMaleDesignation(parents.getMaleParent().getDesignation());
			cross.setFemaleDesignation(parents.getFemaleParent().getDesignation());

			crossesMadeMap.put(germplasm, name);
			ctr++;
		}

		return crossesMadeMap;
	}

	// internal POJO for ad ID of each row in Crosses Made table (need both GID and entryid of parents)

	public void clearCrossesTable() {
		this.tableCrossesMade.removeAllItems();
		this.tableCrossesMade.setPageLength(0);
	}

	@Override
	public void instantiateComponents() {
		this.selectAll = new CheckBox("Select All");
		this.lblReviewCrosses = new Label(this.messageSource.getMessage(Message.PREVIEW_CROSSES));
		this.lblReviewCrosses.setDebugId("lblReviewCrosses");
		this.lblReviewCrosses.addStyleName(Bootstrap.Typography.H4.styleName());
		this.lblReviewCrosses.addStyleName(AppConstants.CssStyles.BOLD);
		this.lblReviewCrosses.setWidth("150px");

		this.totalCrossesLabel = new Label();
		this.totalCrossesLabel.setDebugId("totalCrossesLabel");
		this.totalCrossesLabel.setContentMode(Label.CONTENT_XHTML);
		this.totalCrossesLabel.setWidth("120px");

		this.totalSelectedCrossesLabel = new Label();
		this.totalSelectedCrossesLabel.setDebugId("totalSelectedCrossesLabel");
		this.totalSelectedCrossesLabel.setContentMode(Label.CONTENT_XHTML);
		this.totalSelectedCrossesLabel.setWidth("95px");

		this.actionButton = new ActionButton();
		this.actionButton.setDebugId("actionButton");

		this.actionMenu = new ContextMenu();
		this.actionMenu.setDebugId("actionMenu");
		this.actionMenu.setWidth("250px");
		this.actionMenu.addItem(this.messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES));

		this.initializeCrossesMadeTable(new TableWithSelectAllLayout(PARENTS_TABLE_ROW_COUNT, TAG_COLUMN_ID));
	}

	protected void initializeCrossesMadeTable(final TableWithSelectAllLayout tableWithSelectAllLayout) {
		this.tableWithSelectAllLayout = tableWithSelectAllLayout;
		this.selectAll = tableWithSelectAllLayout.getCheckBox();

		this.tableCrossesMade = tableWithSelectAllLayout.getTable();
		this.tableCrossesMade.setDebugId("tableCrossesMade");
		this.tableCrossesMade.setWidth("100%");
		this.tableCrossesMade.setHeight("183px");
		this.tableCrossesMade.setImmediate(true);
		this.tableCrossesMade.setSelectable(true);
		this.tableCrossesMade.setMultiSelect(true);
		this.tableCrossesMade.setPageLength(100);

		this.tableCrossesMade.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
		this.tableCrossesMade.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.tableCrossesMade.addContainerProperty(ColumnLabels.FEMALE_PARENT.getName(), Button.class, null);
		this.tableCrossesMade.addContainerProperty(ColumnLabels.MALE_PARENT.getName(), Button.class, null);
		this.tableCrossesMade.addContainerProperty(FEMALE_CROSS, String.class, null);
		this.tableCrossesMade.addContainerProperty(MALE_CROSS, String.class, null);
		this.tableCrossesMade.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);

		this.tableCrossesMade.setColumnHeader(TAG_COLUMN_ID, this.messageSource.getMessage(Message.CHECK_ICON));
		this.tableCrossesMade.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), this.messageSource.getMessage(Message.HASHTAG));

		this.tableCrossesMade.setColumnHeader(ColumnLabels.FEMALE_PARENT.getName(), this.getTermNameFromOntology(ColumnLabels.FEMALE_PARENT));
		this.tableCrossesMade.setColumnHeader(ColumnLabels.MALE_PARENT.getName(), this.getTermNameFromOntology(ColumnLabels.MALE_PARENT));
		this.tableCrossesMade.setColumnHeader(ColumnLabels.SEED_SOURCE.getName(), this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE));
		this.tableCrossesMade.setColumnWidth(ColumnLabels.SEED_SOURCE.getName(), 200);

		this.tableCrossesMade.setColumnCollapsingAllowed(true);
		this.tableCrossesMade.setColumnCollapsed(ColumnLabels.SEED_SOURCE.getName(), true);

		this.tableCrossesMade.setColumnHeader(FEMALE_CROSS,FEMALE_CROSS);
		this.tableCrossesMade.setColumnHeader(MALE_CROSS, MALE_CROSS);

		this.tableCrossesMade.setColumnWidth(TAG_COLUMN_ID, 25);

		this.tableCrossesMade.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

			private static final long serialVersionUID = -3207714818504151649L;

			@Override
			public String generateDescription(final Component source, final Object itemId, final Object propertyId) {
				if (propertyId != null && propertyId == ColumnLabels.FEMALE_PARENT.getName()) {
					final Table theTable = (Table) source;
					final Item item = theTable.getItem(itemId);
					return (String) item.getItemProperty(ColumnLabels.FEMALE_PARENT.getName()).getValue();
				}
				return null;
			}
		});
		this.tableCrossesMade.addActionHandler(new CrossingManagerActionHandler(this));
	}

	private void generateTotalCrossesLabel(final Integer size) {
		final String label = "Total Crosses: " + "<b>" + size + "</b>";
		this.totalCrossesLabel.setValue(label);
	}

	private void generateTotalSelectedCrossesLabel(final Integer size) {
		final String label = "<i>" + this.messageSource.getMessage(Message.SELECTED) + ": <b>" + size + "</b></i>";
		this.totalSelectedCrossesLabel.setValue(label);
	}

	private void generateTotalSelectedCrossesLabel() {
		final Collection<?> selectedItems = (Collection<?>) this.tableCrossesMade.getValue();
		final int count = selectedItems.size();
		this.generateTotalSelectedCrossesLabel(count);
	}

	@Override
	public void initializeValues() {
		this.generateTotalCrossesLabel(0);
		this.generateTotalSelectedCrossesLabel(0);
	}

	@Override
	public void addListeners() {

		this.crossingManagerActionListener = new CrossingManagerActionHandler(this);

		this.actionButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				MakeCrossesTableComponent.this.actionMenu.show(event.getClientX(), event.getClientY());
			}

		});

		this.tableCrossesMade.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				MakeCrossesTableComponent.this.generateTotalSelectedCrossesLabel();
			}
		});

		this.actionMenu.addListener(new MakeCrossesTableComponent.ActionMenuClickListener());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		this.setSpacing(true);
		this.setMargin(false, false, false, true);
		
		this.addComponent(this.actionMenu);
		final HorizontalLayout leftLabelContainer = new HorizontalLayout();
		leftLabelContainer.setDebugId("leftLabelContainer");
		leftLabelContainer.addComponent(this.totalCrossesLabel);
		leftLabelContainer.addComponent(this.totalSelectedCrossesLabel);
		leftLabelContainer.addComponent(this.actionButton);
		leftLabelContainer.setComponentAlignment(this.totalCrossesLabel, Alignment.MIDDLE_LEFT);
		leftLabelContainer.setComponentAlignment(this.totalSelectedCrossesLabel, Alignment.MIDDLE_LEFT);
		leftLabelContainer.setComponentAlignment(this.actionButton, Alignment.TOP_RIGHT);


		final HorizontalLayout labelContainer = new HorizontalLayout();
		labelContainer.setDebugId("labelContainer");
		labelContainer.setWidth("100%");
		labelContainer.setHeight("30px");
		labelContainer.addComponent(leftLabelContainer);
		labelContainer.addComponent(this.actionButton);
		labelContainer.setComponentAlignment(leftLabelContainer, Alignment.MIDDLE_LEFT);
		labelContainer.setComponentAlignment(this.actionButton, Alignment.MIDDLE_RIGHT);

		final VerticalLayout makeCrossesLayout = new VerticalLayout();
		makeCrossesLayout.setDebugId("makeCrossesLayout");
		makeCrossesLayout.setSpacing(true);
		makeCrossesLayout.setMargin(true);
		makeCrossesLayout.addComponent(labelContainer);
		makeCrossesLayout.setWidth("100%");
		makeCrossesLayout.addComponent(this.tableCrossesMade);

		final Panel makeCrossesPanel = new Panel();
		makeCrossesPanel.setDebugId("makeCrossesPanel");
		makeCrossesPanel.setLayout(makeCrossesLayout);
		makeCrossesPanel.addStyleName("section_panel_layout");
		makeCrossesPanel.addComponent(selectAll);
		makeCrossesPanel.setWidth("890px");

		final HeaderLabelLayout reviewCrossesLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_PREVIEW_CROSSES, this.lblReviewCrosses);
		reviewCrossesLayout.setDebugId("reviewCrossesLayout");
		this.addComponent(reviewCrossesLayout);
		this.addComponent(makeCrossesPanel);
	}

	/**
	 * Save Temporary list
	 */
	public Integer saveTemporaryList() {
		GermplasmList tempList = new GermplasmList();
		tempList.setType(GermplasmListType.F1CRT.toString());
		// use same pattern as deleted study
		tempList.setName("TEMP_LIST" + "#" + Util.getCurrentDateAsStringValue("yyyyMMddHHmmssSSS"));
		tempList.setDescription("");
		tempList.setDate(DateUtil.getCurrentDateAsLongValue());
		tempList.setNotes("");

		this.saveList(tempList);
		return this.crossList.getId();
	}

	@Override
	public void saveList(final GermplasmList list) {

		if (this.updateCrossesMadeContainer(this.makeCrossesMain.getCrossesMadeContainer(), list)) {
			this.saveRecords();
			this.makeCrossesMain.getSelectParentsComponent().selectListInTree(this.crossList.getId());
			this.makeCrossesMain.getSelectParentsComponent().updateUIForDeletedList(this.crossList);
		}

	}

	private boolean updateCrossesMadeContainer(final CrossesMadeContainer container, final GermplasmList list) {
		final CrossesMade crossesMade = container.getCrossesMade();
		crossesMade.setSetting(this.makeCrossesMain.getCurrentCrossingSetting());
		crossesMade.setGermplasmList(list);
		crossesMade.setCrossesMap(this.generateCrossesMadeMap());
		final ApplyCrossingSettingAction applySetting = new ApplyCrossingSettingAction();
		return applySetting.updateCrossesMadeContainer(container);
	}

	// Save records into DB and redirects to GermplasmListBrowser to view created list
	private void saveRecords() {
		final SaveCrossesMadeAction saveAction = new SaveCrossesMadeAction(this.getCrossList());

		try {
			this.crossList = saveAction.saveRecords(this.makeCrossesMain.getCrossesMadeContainer().getCrossesMade());
		} catch (final MiddlewareQueryException e) {
			MakeCrossesTableComponent.LOG.error(e.getMessage(), e);
			MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
					this.messageSource.getMessage(Message.ERROR_IN_SAVING_CROSSES_DEFINED));
		}
	}

	/**
	 * Update seed source of existing listdata records with new list names
	 *
	 * @param femaleListName
	 * @param maleListName
	 */
	@SuppressWarnings("unchecked")
	public void updateSeedSource(final String femaleListName, final String maleListName) {
		this.separator = this.makeCrossesMain.getSeparatorString();

		if (!this.tableCrossesMade.getItemIds().isEmpty()) {
			for (final Object itemId : this.tableCrossesMade.getItemIds()) {
				final CrossParents crossParents = (CrossParents) itemId;
				final Property crossSourceProp = this.tableCrossesMade.getItem(itemId).getItemProperty(ColumnLabels.SEED_SOURCE.getName());

				final GermplasmListEntry femaleParent = crossParents.getFemaleParent();
				final GermplasmListEntry maleParent = crossParents.getMaleParent();

				String newFemaleSource = "";
				String newMaleSource = "";
				if (femaleParent.isFromFemaleTable()) {
					newFemaleSource = femaleListName + ":" + femaleParent.getEntryId();
					newMaleSource = maleListName + ":" + maleParent.getEntryId();
				} else {
					newFemaleSource = maleListName + ":" + femaleParent.getEntryId();
					newMaleSource = femaleListName + ":" + maleParent.getEntryId();
				}

				femaleParent.setSeedSource(newFemaleSource);
				maleParent.setSeedSource(newMaleSource);

				final String newSeedSource = newFemaleSource + this.separator + newMaleSource;

				crossSourceProp.setValue(newSeedSource);
				crossParents.setSeedSource(newSeedSource);
			}

			if (this.getCrossList() != null) {
				this.makeCrossesMain.getSelectParentsComponent().updateUIForDeletedList(this.getCrossList());

				final SaveCrossesMadeAction saveAction = new SaveCrossesMadeAction(this.getCrossList());
				try {
					saveAction.updateSeedSource((Collection<CrossParents>) this.tableCrossesMade.getItemIds());
				} catch (final MiddlewareQueryException e) {
					MakeCrossesTableComponent.LOG.error(e.getMessage(), e);
					MessageNotifier.showError(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
							this.messageSource.getMessage(Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_CHANGES));
				}
			}
		}

	}

	public void updateSeparatorForCrossesMade() {
		this.separator = this.makeCrossesMain.getSeparatorString();

		for (final Object crossItem : this.tableCrossesMade.getItemIds()) {
			final CrossParents parents = (CrossParents) crossItem;

			final Property seedSourceProperty =
					this.tableCrossesMade.getItem(crossItem).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
			final String femaleSource = parents.getFemaleParent().getSeedSource();
			final String maleSource = parents.getMaleParent().getSeedSource();
			final String newSeedSource = this.appendWithSeparator(femaleSource, maleSource);
			seedSourceProperty.setValue(newSeedSource);
			parents.setSeedSource(newSeedSource);
		}
	}

	private String appendWithSeparator(final String string1, final String string2) {
		return string1 + this.separator + string2;
	}

	public GermplasmList getCrossList() {
		return this.crossList;
	}

	public String getSeparator() {
		return this.separator;
	}

	public void setSeparator(final String separator) {
		this.separator = separator;
	}

	@Override
	public void setCurrentlySavedGermplasmList(final GermplasmList list) {
		this.crossList = list;
	}

	@Override
	public Component getParentComponent() {
		return this.makeCrossesMain.getSource();
	}

	public Table getTableCrossesMade() {
		return this.tableCrossesMade;
	}

	public void setTableCrossesMade(final Table tableCrossesMade) {
		this.tableCrossesMade = tableCrossesMade;
	}

	protected String getTermNameFromOntology(final ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public void setMessageSource(final SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setSeedSourceGenerator(final SeedSourceGenerator seedSourceGenerator) {
		this.seedSourceGenerator = seedSourceGenerator;
	}

	private final class ActionMenuClickListener implements ContextMenu.ClickListener {

		private static final long serialVersionUID = -2343109406180457070L;

		@Override
		public void contextItemClick(final org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
			final TransactionTemplate transactionTemplate = new TransactionTemplate(MakeCrossesTableComponent.this.transactionManager);
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(final TransactionStatus status) {
					final ContextMenu.ContextMenuItem clickedItem = event.getClickedItem();

					if (clickedItem.getName().equals(
						MakeCrossesTableComponent.this.messageSource.getMessage(Message.REMOVE_SELECTED_ENTRIES))) {
						MakeCrossesTableComponent.this.crossingManagerActionListener.removeSelectedEntriesAction(MakeCrossesTableComponent.this.tableCrossesMade);
						MakeCrossesTableComponent.this.updateCrossesMadeUI();
					}
				}
			});
		}
	}

	public TableWithSelectAllLayout getTableWithSelectAllLayout() {
		return this.tableWithSelectAllLayout;
	}

	public void setTableWithSelectAllLayout(final TableWithSelectAllLayout tableWithSelectAllLayout) {
		this.tableWithSelectAllLayout = tableWithSelectAllLayout;
	}
}
