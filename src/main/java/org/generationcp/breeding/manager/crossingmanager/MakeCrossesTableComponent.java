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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.actions.SaveCrossesMadeAction;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerActionHandler;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.settings.ApplyCrossingSettingAction;
import org.generationcp.breeding.manager.crossingmanager.xml.BreedingMethodSetting;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.customfields.BreedingManagerTable;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCross;
import org.generationcp.breeding.manager.util.BreedingManagerTransformationUtil;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.constant.ColumnLabels;
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

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * This class contains UI components and functions related to Crosses Made table in Make Crosses screen in Crossing Manager
 *
 */
@Configurable
public class MakeCrossesTableComponent extends VerticalLayout
		implements InitializingBean, InternationalizableComponent, BreedingManagerLayout, SaveListAsDialogSource {

	private static final int PAGE_LENGTH = 5;
	public static final String PARENTS_DELIMITER = ",";
	private static final long serialVersionUID = 3702324761498666369L;
	private static final Logger LOG = LoggerFactory.getLogger(MakeCrossesTableComponent.class);

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
	private BreedingManagerTable tableCrossesMade;

	private Label totalCrossesLabel;
	private Label totalSelectedCrossesLabel;

	private PopupView applyGroupingToNewCrossesOnlyHelpPopup;
	private Label applyGroupingToNewCrossesOnlyHelpText;
	private CheckBox applyGroupingToNewCrossesOnly;

//	private Button saveButton;

	private SaveListAsDialog saveListAsWindow;
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
		final List<GermplasmListEntry> femaleParents = new ArrayList<GermplasmListEntry>();
		final List<GermplasmListEntry> maleParents = new ArrayList<GermplasmListEntry>();
		femaleParents.addAll(parents1);
		maleParents.addAll(parents2);

		final ImmutableMap<Integer, Germplasm> germplasmWithPreferredName = getGermplasmWithPreferredNameForBothParents(femaleParents, maleParents);
		 
		final Map<Integer, String> parentsPedigreeString = pedigreeService.getCrossExpansions(germplasmWithPreferredName.keySet(), null, crossExpansionProperties);
		
		final ListIterator<GermplasmListEntry> femaleListIterator = femaleParents.listIterator();
		final ListIterator<GermplasmListEntry> maleListIterator = maleParents.listIterator();

		this.separator = this.makeCrossesMain.getSeparatorString();
		final Set<CrossParents> existingCrosses = new HashSet<>();
		this.tableCrossesMade.disableContentRefreshing();

		while (femaleListIterator.hasNext()) {
			final GermplasmListEntry femaleParent = femaleListIterator.next();
			final GermplasmListEntry maleParent = maleListIterator.next();
			this.addItemToMakeCrossesTable(listnameFemaleParent, listnameMaleParent, excludeSelf, femaleParent, maleParent,
					existingCrosses, germplasmWithPreferredName, parentsPedigreeString);
		}
		this.updateCrossesMadeUI();
		this.tableCrossesMade.enableContentRefreshing(true);

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

	void addItemToMakeCrossesTable(final String listnameFemaleParent, final String listnameMaleParent, final boolean excludeSelf,
			final GermplasmListEntry femaleParent, final GermplasmListEntry maleParent, final Set<CrossParents> existingCrosses,
			final Map<Integer, Germplasm> germplasmWithPreferredName, final Map<Integer, String> pedigreeString ) {

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
			final String maleParentPeferredName = getGermplasmPreferredName(germplasmWithPreferredName.get(maleParent.getGid()));
			final String femaleParentPedigreeString = pedigreeString.get(femaleParent.getGid());
			final String maleParentPedigreeString = pedigreeString.get(maleParent.getGid());

			this.tableCrossesMade.addItem(
				new Object[] {entryCounter, femaleParentPedigreeString, maleParentPedigreeString, femalePreferredName,
					maleParentPeferredName, seedSource}, parents);
			existingCrosses.add(parents);
		}

	}

	private void setMakeCrossesTableVisibleColumn() {
		this.tableCrossesMade.addContainerProperty(ColumnLabels.CROSS_FEMALE_GID.getName(), String.class, null);
		this.tableCrossesMade.addContainerProperty(ColumnLabels.CROSS_MALE_GID.getName(), String.class, null);

		this.tableCrossesMade.setVisibleColumns(new Object[] {ColumnLabels.ENTRY_ID.getName(), ColumnLabels.CROSS_FEMALE_GID.getName(),
				ColumnLabels.CROSS_MALE_GID.getName(), ColumnLabels.FEMALE_PARENT.getName(), 
				ColumnLabels.MALE_PARENT.getName(), ColumnLabels.SEED_SOURCE.getName()});
	}

	private void updateCrossesMadeUI() {
		final int crossesCount = this.tableCrossesMade.size();
		this.generateTotalCrossesLabel(crossesCount);
		this.updateCrossesMadeSaveButton();
		this.makeCrossesMain.toggleNurseryBackButton();

		this.tableCrossesMade.setPageLength(0);
		this.tableCrossesMade.requestRepaint();
	}

	public void updateCrossesMadeSaveButton() {
		if (this.tableCrossesMade.getItemIds() == null) {
			return;
		}

//		final boolean isCrossesInTable = !this.tableCrossesMade.getItemIds().isEmpty();
//		this.saveButton.setEnabled(isCrossesInTable);
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
		final List<GermplasmListEntry> femaleParents = new ArrayList<GermplasmListEntry>();
		final List<GermplasmListEntry> maleParents = new ArrayList<GermplasmListEntry>();
		femaleParents.addAll(parents1);
		maleParents.addAll(parents2);

		this.setMakeCrossesTableVisibleColumn();
		this.separator = this.makeCrossesMain.getSeparatorString();
		
		final ImmutableMap<Integer, Germplasm> germplasmWithPreferredName = getGermplasmWithPreferredNameForBothParents(femaleParents, maleParents);
		final Map<Integer, String> parentsPedigreeString = pedigreeService.getCrossExpansions(germplasmWithPreferredName.keySet(), null, crossExpansionProperties);

		final Set<CrossParents> existingCrosses = new HashSet<>();
		this.tableCrossesMade.disableContentRefreshing();
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
		this.tableCrossesMade.enableContentRefreshing(true);


	}

	void addItemToMakeCrossesTable(final boolean excludeSelf, final GermplasmListEntry femaleParent, final String femaleSource,
		final GermplasmListEntry maleParent, final String maleSource, final CrossParents parents, final Set<CrossParents> existingCrosses,
		final Map<Integer, Germplasm> germplasmWithPreferredName, final Map<Integer, String> parentsPedigreeString) {
		if (shouldBeAddedToCrossesTable(parents, existingCrosses, excludeSelf, femaleParent, maleParent)
		 ) {
				int entryCounter = this.tableCrossesMade.size() + 1;
				final String femaleParentPedigreeString = parentsPedigreeString.get(femaleParent.getGid());
				final String maleParentPedigreeString = parentsPedigreeString.get(maleParent.getGid());
				final String femalePreferredName = getGermplasmPreferredName(germplasmWithPreferredName.get(femaleParent.getGid()));
				final String maleParentPeferredName = getGermplasmPreferredName(germplasmWithPreferredName.get(maleParent.getGid()));
				final String seedSource = this.generateSeedSource(femaleParent.getGid(), femaleSource, maleParent.getGid(), maleSource);
				this.tableCrossesMade.addItem(
					new Object[] {entryCounter, femaleParentPedigreeString, maleParentPedigreeString, femalePreferredName,
						maleParentPeferredName, seedSource}, parents);
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
		final Workbook nurseryWorkbook = this.makeCrossesMain.getNurseryWorkbook();
		if (nurseryWorkbook != null) {
			String malePlotNo = "";
			String femalePlotNo = "";

			// Look at the observation rows of Nursery to find plot number assigned to the male/female parent germplasm of the cross.
			for (final MeasurementRow row : nurseryWorkbook.getObservations()) {
				final MeasurementData gidData = row.getMeasurementData(TermId.GID.getId());
				final MeasurementData plotNumberData = row.getMeasurementData(TermId.PLOT_NO.getId());

				if (gidData != null && gidData.getValue().equals(femaleParentGid.toString())) {
					if (plotNumberData != null) {
						femalePlotNo = plotNumberData.getValue();
					}
				}

				if (gidData != null && gidData.getValue().equals(maleParentGid.toString())) {
					if (plotNumberData != null) {
						malePlotNo = plotNumberData.getValue();
					}
				}
			}

			// Single nursery is in context here, so set the same study name as both male/female parts. For import crosses case, these
			// could be different Nurseries.
			seedSource = this.seedSourceGenerator.generateSeedSourceForCross(nurseryWorkbook, malePlotNo, femalePlotNo,
					nurseryWorkbook.getStudyName(), nurseryWorkbook.getStudyName());
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

		if (this.tableCrossesMade.getItemIds().isEmpty() && this.getParent() instanceof CrossingManagerMakeCrossesComponent) {
			((CrossingManagerMakeCrossesComponent) this.getParent()).disableNextButton();
		}

		this.updateCrossesMadeUI();
	}

	private Map<Germplasm, Name> generateCrossesMadeMap() {
		final Map<Germplasm, Name> crossesMadeMap = new LinkedHashMap<Germplasm, Name>();

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
		this.lblReviewCrosses = new Label(this.messageSource.getMessage(Message.REVIEW_CROSSES));
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

		// TODO Move Grouping logic to Nursery since breeding method is there now
		this.applyGroupingToNewCrossesOnly = new CheckBox(this.messageSource.getMessage(Message.APPLY_NEW_GROUP_TO_CURRENT_CROSS_ONLY));
		this.applyGroupingToNewCrossesOnly.setDebugId("applyGroupingToNewCrossesOnly");

		this.applyGroupingToNewCrossesOnlyHelpText = new Label(this.messageSource.getMessage(Message.GROUP_INHERITANCE_OPTION_MESSAGE));
		this.applyGroupingToNewCrossesOnlyHelpText.setDebugId("applyGroupingToNewCrossesOnlyHelpText");
		this.applyGroupingToNewCrossesOnlyHelpText.setWidth("300px");
		this.applyGroupingToNewCrossesOnlyHelpText.addStyleName("gcp-content-help-text");

		this.applyGroupingToNewCrossesOnlyHelpPopup = new PopupView("?", this.applyGroupingToNewCrossesOnlyHelpText);
		this.applyGroupingToNewCrossesOnlyHelpPopup.setDebugId("applyGroupingToNewCrossesOnlyHelpPopup");
		this.applyGroupingToNewCrossesOnlyHelpPopup.addStyleName(AppConstants.CssStyles.POPUP_VIEW);
		this.applyGroupingToNewCrossesOnlyHelpPopup.addStyleName("cs-inline-icon");

//		this.saveButton = new Button(this.messageSource.getMessage(Message.SAVE_LABEL));
//		this.saveButton.setDebugId("saveButton");
//		this.saveButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
//		this.saveButton.setEnabled(false);
		this.initializeCrossesMadeTable();
	}

	protected void initializeCrossesMadeTable() {
		this.setTableCrossesMade(new BreedingManagerTable(MakeCrossesTableComponent.PAGE_LENGTH, MakeCrossesTableComponent.PAGE_LENGTH));
		this.tableCrossesMade = this.getTableCrossesMade();
		this.tableCrossesMade.setDebugId("tableCrossesMade");
		this.tableCrossesMade.setWidth("850");
		this.tableCrossesMade.setHeight("365px");
		this.tableCrossesMade.setImmediate(true);
		this.tableCrossesMade.setSelectable(true);
		this.tableCrossesMade.setMultiSelect(true);
		this.tableCrossesMade.setPageLength(100);

		this.tableCrossesMade.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
		this.tableCrossesMade.addContainerProperty(ColumnLabels.CROSS_FEMALE_GID.getName(), String.class, null);
		this.tableCrossesMade.addContainerProperty(ColumnLabels.CROSS_MALE_GID.getName(), String.class, null);
		this.tableCrossesMade.addContainerProperty(ColumnLabels.FEMALE_PARENT.getName(), String.class, null);
		this.tableCrossesMade.addContainerProperty(ColumnLabels.MALE_PARENT.getName(), String.class, null);
		this.tableCrossesMade.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);

		this.tableCrossesMade.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), "#");
		this.tableCrossesMade.setColumnHeader(ColumnLabels.CROSS_FEMALE_GID.getName(), this.getTermNameFromOntology(ColumnLabels.CROSS_FEMALE_GID));
		this.tableCrossesMade.setColumnHeader(ColumnLabels.CROSS_MALE_GID.getName(), this.getTermNameFromOntology(ColumnLabels.CROSS_MALE_GID));

		this.tableCrossesMade.setColumnHeader(ColumnLabels.FEMALE_PARENT.getName(),
				this.getTermNameFromOntology(ColumnLabels.FEMALE_PARENT));
		this.tableCrossesMade.setColumnHeader(ColumnLabels.MALE_PARENT.getName(), this.getTermNameFromOntology(ColumnLabels.MALE_PARENT));
		this.tableCrossesMade.setColumnHeader(ColumnLabels.SEED_SOURCE.getName(), this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE));

		this.tableCrossesMade.setColumnWidth(ColumnLabels.SEED_SOURCE.getName(), 200);

		this.tableCrossesMade.setColumnCollapsingAllowed(true);
		this.tableCrossesMade.setColumnCollapsed(ColumnLabels.SEED_SOURCE.getName(), true);

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
//		this.saveButton.addListener(new Button.ClickListener() {
//
//			private static final long serialVersionUID = 5123058086826023128L;
//
//			@Override
//			public void buttonClick(final ClickEvent event) {
//				if (MakeCrossesTableComponent.this.makeCrossesMain.isValidationsBeforeSavePassed()) {
//					MakeCrossesTableComponent.this.launchSaveListAsWindow();
//				}
//			}
//		});

		this.tableCrossesMade.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				MakeCrossesTableComponent.this.generateTotalSelectedCrossesLabel();
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		this.setSpacing(true);
		this.setMargin(false, false, false, true);
		this.setWidth("900px");

		final HorizontalLayout leftLabelContainer = new HorizontalLayout();
		leftLabelContainer.setDebugId("leftLabelContainer");
//		leftLabelContainer.setSpacing(true);
		leftLabelContainer.addComponent(this.totalCrossesLabel);
		leftLabelContainer.addComponent(this.totalSelectedCrossesLabel);
		leftLabelContainer.setComponentAlignment(this.totalCrossesLabel, Alignment.MIDDLE_LEFT);
		leftLabelContainer.setComponentAlignment(this.totalSelectedCrossesLabel, Alignment.MIDDLE_LEFT);

		final HorizontalLayout labelContainer = new HorizontalLayout();
		labelContainer.setDebugId("labelContainer");
//		labelContainer.setSpacing(true);
		labelContainer.setWidth("100%");
		labelContainer.setHeight("30px");
		labelContainer.addComponent(leftLabelContainer);
//		labelContainer.addComponent(this.saveButton);
		labelContainer.setComponentAlignment(leftLabelContainer, Alignment.MIDDLE_LEFT);
//		labelContainer.setComponentAlignment(this.saveButton, Alignment.MIDDLE_RIGHT);

		final VerticalLayout makeCrossesLayout = new VerticalLayout();
		makeCrossesLayout.setDebugId("makeCrossesLayout");
		makeCrossesLayout.setWidth("900px"); //era 850
		makeCrossesLayout.setHeight("300px"); //365px
		makeCrossesLayout.setSpacing(true);
		makeCrossesLayout.setMargin(true);
		makeCrossesLayout.addComponent(labelContainer);

//		final HorizontalLayout groupInheritanceOptionsContainer = new HorizontalLayout();
//		groupInheritanceOptionsContainer.setDebugId("groupInheritanceOptionsContainer");
//		groupInheritanceOptionsContainer.setSpacing(true);
//		groupInheritanceOptionsContainer.addComponent(this.applyGroupingToNewCrossesOnly);
//		groupInheritanceOptionsContainer.addComponent(this.applyGroupingToNewCrossesOnlyHelpPopup);
//		makeCrossesLayout.addComponent(groupInheritanceOptionsContainer);

		makeCrossesLayout.addComponent(this.tableCrossesMade);

		final Panel makeCrossesPanel = new Panel();
		makeCrossesPanel.setDebugId("makeCrossesPanel");
		makeCrossesPanel.setWidth("900px");
		makeCrossesPanel.setHeight("530px");
		makeCrossesPanel.setLayout(makeCrossesLayout);
		makeCrossesPanel.addStyleName("section_panel_layout");

		final HeaderLabelLayout reviewCrossesLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_REVIEW_CROSSES, this.lblReviewCrosses);
		reviewCrossesLayout.setDebugId("reviewCrossesLayout");
		this.addComponent(reviewCrossesLayout);
		this.addComponent(makeCrossesPanel);
	}

	// TODO Move Grouping logic to Nursery since breeding method is there now
	public void showOrHideGroupInheritanceOptions() {
		// Only show group inheritance options if breeding method chosen is hybrid
		final BreedingMethodSetting currentBreedingSetting = this.makeCrossesMain.getCurrentBreedingMethodSetting();
		final Integer selectedBreedingMethodId = currentBreedingSetting.getMethodId();
		if (this.crossExpansionProperties.getHybridBreedingMethods().contains(selectedBreedingMethodId)) {
			this.applyGroupingToNewCrossesOnlyHelpPopup.setVisible(true);
			this.applyGroupingToNewCrossesOnly.setVisible(true);
		} else {
			this.applyGroupingToNewCrossesOnlyHelpPopup.setVisible(false);
			this.applyGroupingToNewCrossesOnly.setVisible(false);
		}
	}

	void launchSaveListAsWindow() {
		this.saveListAsWindow = null;
		if (this.crossList != null) {
			this.saveListAsWindow = new SaveCrossListAsDialog(this, this.crossList);
			this.saveListAsWindow.setDebugId("saveListAsWindow");
		} else {
			this.saveListAsWindow = new SaveCrossListAsDialog(this, null);
			this.saveListAsWindow.setDebugId("saveListAsWindow");
		}

		this.saveListAsWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		this.getWindow().addWindow(this.saveListAsWindow);
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
//		this.saveButton.setEnabled(false);

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
		final ApplyCrossingSettingAction applySetting = new ApplyCrossingSettingAction(this.makeCrossesMain.getCurrentCrossingSetting());
		return applySetting.updateCrossesMadeContainer(container);
	}

	// Save records into DB and redirects to GermplasmListBrowser to view created list
	private void saveRecords() {
		final SaveCrossesMadeAction saveAction = new SaveCrossesMadeAction(this.getCrossList());

		try {
			final boolean applyNewGroupToCurrentCrossOnly = this.applyGroupingToNewCrossesOnly.booleanValue();

			this.crossList = saveAction.saveRecords(this.makeCrossesMain.getCrossesMadeContainer().getCrossesMade(),
					applyNewGroupToCurrentCrossOnly);

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

//	public Button getSaveButton() {
//		return this.saveButton;
//	}

	public BreedingManagerTable getTableCrossesMade() {
		return this.tableCrossesMade;
	}

	public void setTableCrossesMade(final BreedingManagerTable tableCrossesMade) {
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
}
