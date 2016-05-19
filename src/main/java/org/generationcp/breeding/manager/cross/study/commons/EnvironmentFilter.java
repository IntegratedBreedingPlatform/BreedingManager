
package org.generationcp.breeding.manager.cross.study.commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.adapted.main.QueryForAdaptedGermplasmMain;
import org.generationcp.breeding.manager.cross.study.adapted.main.SetUpTraitFilter;
import org.generationcp.breeding.manager.cross.study.constants.EnvironmentWeight;
import org.generationcp.breeding.manager.cross.study.h2h.main.HeadToHeadCrossStudyMain;
import org.generationcp.breeding.manager.cross.study.h2h.main.ResultsComponent;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.AddEnvironmentalConditionsDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.FilterLocationDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.dialogs.FilterStudyDialog;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.FilterByLocation;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.ObservationList;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.breeding.manager.cross.study.traitdonors.main.SetUpTraitDonorFilter;
import org.generationcp.breeding.manager.cross.study.traitdonors.main.TraitDonorsQueryMain;
import org.generationcp.breeding.manager.cross.study.util.CrossStudyUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.ui.ConfirmDialog;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.LocationDto;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.dms.TrialEnvironmentProperty;
import org.generationcp.middleware.domain.dms.TrialEnvironments;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.ColumnResizeEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

/**
 * Accordion tab used by all three queries (Adapted Germplasm, Trait Donor and H2H) in order to select environments/locations where
 * observations should be drawn from for analysis.
 *
 * @author rebecca
 *
 */
@Configurable
public class EnvironmentFilter extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -3667517088395779496L;

	private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter.class);

	private static final String TAG_COLUMN_ID = "EnvironmentFilter Tag Column Id";
	private static final String ENV_NUMBER_COLUMN_ID = "EnvironmentFilter Env Number Column Id";
	private static final String LOCATION_COLUMN_ID = "EnvironmentFilter Location Column Id";
	private static final String COUNTRY_COLUMN_ID = "EnvironmentFilter Country Column Id";
	private static final String STUDY_COLUMN_ID = "EnvironmentFilter Study Column Id";
	private static final String WEIGHT_COLUMN_ID = "EnvironmentFilter Weight Column Id";

	public static final String NEXT_BUTTON_ID = "EnvironmentFilter Next Button ID";
	public static final String BACK_BUTTON_ID = "EnvironmentFilter Back Button ID";

	public static final String FILTER_LOCATION_BUTTON_ID = "EnvironmentFilter Filter Location Button ID";
	public static final String FILTER_STUDY_BUTTON_ID = "EnvironmentFilter Filter Study Button ID";
	public static final String ADD_ENVIRONMENT_BUTTON_ID = "EnvironmentFilter Add Env Button ID";
	public static final String QUERY_FOR_ADAPTED_GERMPLASM_WINDOW_NAME = "Query_For_Adapted_Germplasm";

	private Map<String, ObservationList> observationMap;
	private Map<String, String> germplasmIdNameMap;
	private List<GermplasmPair> finalGermplasmPairs;
	private Set<TraitInfo> traitInfosNames;
	private Set<String> trialEnvironmentIds;
	private Map<String, Map<String, TrialEnvironment>> traitEnvMap;
	private Map<String, TrialEnvironment> trialEnvMap;
	private List<TraitForComparison> traitForComparisonsList;

	/* Adapted Germplasm Variables */
	private QueryForAdaptedGermplasmMain mainScreen2;
	private SetUpTraitFilter nextScreen2;

	/* Trait Donor Query Variables */
	private TraitDonorsQueryMain mainScreen3;
	private SetUpTraitDonorFilter nextScreen3;

	/* Head to Head Query Variables */
	private HeadToHeadCrossStudyMain mainScreen1;
	private ResultsComponent nextScreen1;

	private Label headerLabel;
	private Label headerValLabel;
	private Label chooseEnvLabel;
	private Label noOfEnvLabel;
	private Label numberOfEnvironmentSelectedLabel;

	private Button filterByLocationBtn;
	private Button filterByStudyBtn;
	private Button addEnvConditionsBtn;
	private Button nextButton;
	private Button backButton;

	private Table environmentsTable;
	private TrialEnvironments environments;

	private Map<String, FilterByLocation> filterLocationCountryMap;
	private Map<String, List<StudyReference>> studyEnvironmentMap;

	private FilterLocationDialog filterLocation;
	private FilterStudyDialog filterStudy;
	private AddEnvironmentalConditionsDialog addConditionsDialog;

	private Map<String, String> filterSetLevel1;
	private Map<String, String> filterSetLevel3;
	private Map<String, String> filterSetLevel4;

	private boolean isFilterLocationClicked = false;
	private boolean isFilterStudyClicked = false;

	private int tableColumnSize = 0;

	private Map<String, Object[]> tableEntriesMap;
	private Map<String, EnvironmentForComparison> environmentCheckBoxComparisonMap;
	private Set<String> environmentForComparison; // will contain all the tagged row
	private List<String> addedEnvironmentColumns;

	private Map<CheckBox, Item> environmentCheckBoxMap;

	private CheckBox tagAllCheckBox;

	Set<Integer> environmentIds;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private CrossStudyDataManager crossStudyDataManager;

	private final CrossStudyToolType crossStudyToolType;

	private Panel tablePanel;
	private AbsoluteLayout tableLayout;

	// This list allows us to limit environments based on selected traits (Trait Donors Query)
	private List<Integer> traitsList;

	// Keeping default to true to keep existing behavior intact for H2H and Trait Donor's queries.
	private boolean includePublicData = true;

	public EnvironmentFilter(HeadToHeadCrossStudyMain mainScreen, ResultsComponent nextScreen) {
		this.mainScreen1 = mainScreen;
		this.nextScreen1 = nextScreen;

		this.crossStudyToolType = CrossStudyToolType.HEAD_TO_HEAD_QUERY;
	}

	public EnvironmentFilter(QueryForAdaptedGermplasmMain mainScreen, SetUpTraitFilter nextScreen) {
		this.mainScreen2 = mainScreen;
		this.nextScreen2 = nextScreen;

		this.crossStudyToolType = CrossStudyToolType.QUERY_FOR_ADAPTED_GERMPLASM;
	}

	public EnvironmentFilter(TraitDonorsQueryMain mainScreen, SetUpTraitDonorFilter nextScreen) {
		this.mainScreen3 = mainScreen;
		this.nextScreen3 = nextScreen;

		this.crossStudyToolType = CrossStudyToolType.TRAIT_DONORS_QUERY;
	}

	@Override
	public void updateLabels() {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setHeight("550px");
		this.setWidth("1000px");

		this.headerLabel = new Label(this.messageSource.getMessage(Message.ENVIRONMENT_FILTER));
		this.headerLabel.setImmediate(true);
		this.addComponent(this.headerLabel, "top:20px;left:20px");

		this.headerValLabel = new Label(this.messageSource.getMessage(Message.ENVIRONMENT_FILTER_VAL));
		this.headerValLabel.setStyleName("gcp-bold-italic");
		this.headerValLabel.setContentMode(Label.CONTENT_XHTML);
		this.headerValLabel.setImmediate(true);
		this.addComponent(this.headerValLabel, "top:20px;left:150px");

		this.filterByLocationBtn = new Button(this.messageSource.getMessage(Message.FILTER_BY_LOCATION));
		this.filterByLocationBtn.setWidth("150px");
		this.filterByLocationBtn.setData(EnvironmentFilter.FILTER_LOCATION_BUTTON_ID);
		this.filterByLocationBtn.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6624555365983829849L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (event.getButton().getData().equals(EnvironmentFilter.FILTER_LOCATION_BUTTON_ID)) {
					EnvironmentFilter.this.selectFilterByLocationClickAction();
				}
			}
		});
		this.addComponent(this.filterByLocationBtn, "top:50px;left:20px");

		this.filterByStudyBtn = new Button(this.messageSource.getMessage(Message.FILTER_BY_STUDY));
		this.filterByStudyBtn.setWidth("150px");
		this.filterByStudyBtn.setData(EnvironmentFilter.FILTER_STUDY_BUTTON_ID);
		this.filterByStudyBtn.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = -8782138170364187141L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (event.getButton().getData().equals(EnvironmentFilter.FILTER_STUDY_BUTTON_ID)) {
					EnvironmentFilter.this.selectFilterByStudyClickAction();
				}
			}
		});
		this.addComponent(this.filterByStudyBtn, "top:50px;left:180px");

		this.addEnvConditionsBtn = new Button(this.messageSource.getMessage(Message.ADD_ENV_CONDITION));
		this.addEnvConditionsBtn.setWidth("400px");
		this.addEnvConditionsBtn.setData(EnvironmentFilter.ADD_ENVIRONMENT_BUTTON_ID);
		this.addEnvConditionsBtn.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 4763719750664067113L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (event.getButton().getData().equals(EnvironmentFilter.ADD_ENVIRONMENT_BUTTON_ID)) {
					EnvironmentFilter.this.addEnvironmentalConditionsClickAction();
				}
			}
		});
		this.addComponent(this.addEnvConditionsBtn, "top:50px;left:580px");

		this.chooseEnvLabel = new Label(this.messageSource.getMessage(Message.CHOOSE_ENVIRONMENTS));
		this.chooseEnvLabel.setImmediate(true);
		this.addComponent(this.chooseEnvLabel, "top:90px;left:20px");

		this.environmentsTable = new Table();
		this.environmentsTable.setWidth("960px");
		this.environmentsTable.setHeight("350px");
		this.environmentsTable.setImmediate(true);
		this.environmentsTable.setPageLength(11);
		this.environmentsTable.setColumnCollapsingAllowed(true);
		this.environmentsTable.setColumnReorderingAllowed(true);
		this.environmentsTable.addListener(new Table.ColumnResizeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void columnResize(ColumnResizeEvent event) {
				int diff = event.getCurrentWidth() - event.getPreviousWidth();
				float newWidth = diff + EnvironmentFilter.this.environmentsTable.getWidth();

				String widthPx = String.valueOf(newWidth) + "px";
				EnvironmentFilter.this.environmentsTable.setWidth(widthPx);
				EnvironmentFilter.this.tableLayout.setWidth(widthPx);
			}
		});

		this.tablePanel = new Panel();
		this.tablePanel.setWidth("960px");
		this.tablePanel.setHeight("370px");

		this.tableLayout = new AbsoluteLayout();

		if (this.crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY) {
			Set<TraitInfo> traitInfos = new HashSet<TraitInfo>();
			this.createEnvironmentsTable(traitInfos);
		} else if (this.crossStudyToolType == CrossStudyToolType.QUERY_FOR_ADAPTED_GERMPLASM
				|| this.crossStudyToolType == CrossStudyToolType.TRAIT_DONORS_QUERY) {
			this.createEnvironmentsTable();
		}

		this.tableLayout.addComponent(this.environmentsTable, "top:0px;left:0px");

		this.tagAllCheckBox = new CheckBox();
		this.tagAllCheckBox.setImmediate(true);

		this.tableLayout.addComponent(this.tagAllCheckBox, "top:5px;left:32px");

		this.tablePanel.setContent(this.tableLayout);

		this.addComponent(this.tablePanel, "top:110px;left:20px");

		this.tagAllCheckBox.addListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((Boolean) EnvironmentFilter.this.tagAllCheckBox.getValue() == true) {
					EnvironmentFilter.this.tagAllEnvironments();
				} else {
					EnvironmentFilter.this.untagAllEnvironments();
				}
			}
		});

		this.noOfEnvLabel = new Label(this.messageSource.getMessage(Message.NO_OF_SELECTED_ENVIRONMENT));
		this.noOfEnvLabel.setImmediate(true);
		this.addComponent(this.noOfEnvLabel, "top:500px;left:20px");

		this.numberOfEnvironmentSelectedLabel = new Label("0");
		this.numberOfEnvironmentSelectedLabel.setImmediate(true);
		this.addComponent(this.numberOfEnvironmentSelectedLabel, "top:500px;left:230px");

		this.nextButton = new Button(this.messageSource.getMessage(Message.NEXT));
		this.nextButton.setData(EnvironmentFilter.NEXT_BUTTON_ID);
		this.nextButton.addListener(new Button.ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				EnvironmentFilter.this.nextButtonClickAction();
			}
		});
		this.nextButton.setWidth("80px");
		this.nextButton.setEnabled(false);
		this.nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		// note that the nextButton is added below (line 374) as part of the H2H control block

		if (this.crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY) {
			this.backButton = new Button(this.messageSource.getMessage(Message.BACK));
			this.backButton.setData(EnvironmentFilter.BACK_BUTTON_ID);
			this.backButton.addListener(new Button.ClickListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					EnvironmentFilter.this.backButtonClickAction();
				}
			});
			this.backButton.setWidth("80px");
			this.backButton.setEnabled(true);
			this.addComponent(this.backButton, "top:490px;left:410px");
			this.addComponent(this.nextButton, "top:490px;left:500px");
		} else {
			this.addComponent(this.nextButton, "top:490px;left:460px");
		}
	}

	public boolean isIncludePublicData() {
		return this.includePublicData;
	}

	public void setIncludePublicData(boolean includePublicData) {
		this.includePublicData = includePublicData;
	}

	private void createEnvironmentsTable(Set<TraitInfo> traitInfos) {
		List<Object> propertyIds = new ArrayList<Object>();
		for (Object propertyId : this.environmentsTable.getContainerPropertyIds()) {
			propertyIds.add(propertyId);
		}

		this.tableColumnSize = 0;
		for (Object propertyId : propertyIds) {
			this.environmentsTable.removeContainerProperty(propertyId);
		}

		this.removeAddedEnvironmentConditionsColumns();

		this.environmentsTable.addContainerProperty(EnvironmentFilter.TAG_COLUMN_ID, CheckBox.class, null);
		this.environmentsTable.addContainerProperty(EnvironmentFilter.LOCATION_COLUMN_ID, String.class, null);
		this.environmentsTable.addContainerProperty(EnvironmentFilter.COUNTRY_COLUMN_ID, String.class, null);
		this.environmentsTable.addContainerProperty(EnvironmentFilter.STUDY_COLUMN_ID, String.class, null);

		this.environmentsTable.setColumnHeader(EnvironmentFilter.TAG_COLUMN_ID, "TAG");
		this.environmentsTable.setColumnHeader(EnvironmentFilter.LOCATION_COLUMN_ID, "LOCATION");
		this.environmentsTable.setColumnHeader(EnvironmentFilter.COUNTRY_COLUMN_ID, "COUNTRY");
		this.environmentsTable.setColumnHeader(EnvironmentFilter.STUDY_COLUMN_ID, "STUDY");
		this.tableColumnSize = 4;

		int tableWidth = 960;
		for (TraitInfo traitInfo : traitInfos) {
			this.environmentsTable.addContainerProperty(traitInfo.getId(), Integer.class, null);
			this.environmentsTable.setColumnHeader(traitInfo.getId(), traitInfo.getName());
			this.environmentsTable.setColumnWidth(traitInfo.getId(), 120);
			tableWidth += 120;
			this.tableColumnSize++;
		}

		this.environmentsTable.addContainerProperty(EnvironmentFilter.WEIGHT_COLUMN_ID, ComboBox.class, null);
		this.environmentsTable.setColumnHeader(EnvironmentFilter.WEIGHT_COLUMN_ID, "WEIGHT");
		this.tableColumnSize++;

		this.environmentsTable.setColumnWidth(EnvironmentFilter.TAG_COLUMN_ID, 41);
		this.environmentsTable.setColumnWidth(EnvironmentFilter.LOCATION_COLUMN_ID, 400);
		this.environmentsTable.setColumnWidth(EnvironmentFilter.COUNTRY_COLUMN_ID, 76);
		this.environmentsTable.setColumnWidth(EnvironmentFilter.STUDY_COLUMN_ID, 108);
		this.environmentsTable.setColumnWidth(EnvironmentFilter.WEIGHT_COLUMN_ID, 178);

		String width = String.valueOf(tableWidth) + "px";
		this.tableLayout.setWidth(width);
		this.environmentsTable.setWidth(width);

	}

	private void createEnvironmentsTable() {
		List<Object> propertyIds = new ArrayList<Object>();
		for (Object propertyId : this.environmentsTable.getContainerPropertyIds()) {
			propertyIds.add(propertyId);
		}

		this.tableColumnSize = 0;
		for (Object propertyId : propertyIds) {
			this.environmentsTable.removeContainerProperty(propertyId);
		}

		this.removeAddedEnvironmentConditionsColumns();

		this.environmentsTable.addContainerProperty(EnvironmentFilter.TAG_COLUMN_ID, CheckBox.class, null);
		this.environmentsTable.addContainerProperty(EnvironmentFilter.ENV_NUMBER_COLUMN_ID, Integer.class, null);
		this.environmentsTable.addContainerProperty(EnvironmentFilter.LOCATION_COLUMN_ID, String.class, null);
		this.environmentsTable.addContainerProperty(EnvironmentFilter.COUNTRY_COLUMN_ID, String.class, null);
		this.environmentsTable.addContainerProperty(EnvironmentFilter.STUDY_COLUMN_ID, String.class, null);

		this.environmentsTable.setColumnHeader(EnvironmentFilter.TAG_COLUMN_ID, "TAG");
		this.environmentsTable.setColumnHeader(EnvironmentFilter.ENV_NUMBER_COLUMN_ID, "ENV No");
		this.environmentsTable.setColumnHeader(EnvironmentFilter.LOCATION_COLUMN_ID, "LOCATION");
		this.environmentsTable.setColumnHeader(EnvironmentFilter.COUNTRY_COLUMN_ID, "COUNTRY");
		this.environmentsTable.setColumnHeader(EnvironmentFilter.STUDY_COLUMN_ID, "STUDY");
		this.tableColumnSize = 5;

		this.environmentsTable.addContainerProperty(EnvironmentFilter.WEIGHT_COLUMN_ID, ComboBox.class, null);
		this.environmentsTable.setColumnHeader(EnvironmentFilter.WEIGHT_COLUMN_ID, "WEIGHT");
		this.tableColumnSize++;

		this.environmentsTable.setColumnWidth(EnvironmentFilter.TAG_COLUMN_ID, 41);
		this.environmentsTable.setColumnWidth(EnvironmentFilter.ENV_NUMBER_COLUMN_ID, 60);
		this.environmentsTable.setColumnWidth(EnvironmentFilter.LOCATION_COLUMN_ID, 390);
		this.environmentsTable.setColumnWidth(EnvironmentFilter.COUNTRY_COLUMN_ID, 76);
		this.environmentsTable.setColumnWidth(EnvironmentFilter.STUDY_COLUMN_ID, 108);
		this.environmentsTable.setColumnWidth(EnvironmentFilter.WEIGHT_COLUMN_ID, 178);
	}

	public void populateEnvironmentsTable(List<TraitForComparison> traitForComparisonsListTemp,
			Map<String, Map<String, TrialEnvironment>> traitEnvMapTemp, Map<String, TrialEnvironment> trialEnvMapTemp,
			Set<Integer> germplasmIds, List<GermplasmPair> germplasmPairsTemp, Map<String, String> germplasmIdNameMap) {

		Map<String, Map<String, TrialEnvironment>> newTraitEnvMap = new HashMap<String, Map<String, TrialEnvironment>>();
		this.tableEntriesMap = new HashMap<String, Object[]>();
		this.trialEnvironmentIds = new HashSet<String>();
		this.traitInfosNames = new LinkedHashSet<TraitInfo>();

		this.nextButton.setEnabled(false);
		this.environmentCheckBoxComparisonMap = new HashMap<String, EnvironmentForComparison>();
		this.environmentCheckBoxMap = new HashMap<CheckBox, Item>();
		this.environmentForComparison = new HashSet<String>();
		this.numberOfEnvironmentSelectedLabel.setValue(Integer.toString(this.environmentForComparison.size()));

		this.germplasmIdNameMap = germplasmIdNameMap;
		this.finalGermplasmPairs = germplasmPairsTemp;

		List<Integer> traitIds = new ArrayList<Integer>();
		Set<Integer> environmentIds = new HashSet<Integer>();
		this.filterLocationCountryMap = new HashMap<String, FilterByLocation>();
		this.studyEnvironmentMap = new HashMap<String, List<StudyReference>>();
		this.traitEnvMap = traitEnvMapTemp;
		this.trialEnvMap = trialEnvMapTemp;
		this.traitForComparisonsList = traitForComparisonsListTemp;

		Iterator<TraitForComparison> iter = this.traitForComparisonsList.iterator();

		while (iter.hasNext()) {
			TraitForComparison comparison = iter.next();
			String id = Integer.toString(comparison.getTraitInfo().getId());
			if (this.traitEnvMap.containsKey(id)) {
				Map<String, TrialEnvironment> tempMap = this.traitEnvMap.get(id);
				newTraitEnvMap.put(id, tempMap);
				this.trialEnvironmentIds.addAll(tempMap.keySet());
				Iterator<String> envIdsIter = tempMap.keySet().iterator();
				while (envIdsIter.hasNext()) {
					environmentIds.add(Integer.valueOf(envIdsIter.next()));
				}
				traitIds.add(Integer.parseInt(id));
			}

			this.traitInfosNames.add(comparison.getTraitInfo());
		}
		List<Integer> germplasmIdsList = new ArrayList<Integer>(germplasmIds);
		List<Integer> environmentIdsList = new ArrayList<Integer>(environmentIds);
		try {
			this.observationMap = new HashMap<String, ObservationList>();
			List<Observation> observationList =
					this.crossStudyDataManager.getObservationsForTraitOnGermplasms(traitIds, germplasmIdsList, environmentIdsList);
			for (Observation obs : observationList) {
				String newKey = obs.getId().getTraitId() + ":" + obs.getId().getEnvironmentId() + ":" + obs.getId().getGermplasmId();

				ObservationList obsList = this.observationMap.get(newKey);
				if (obsList == null) {
					obsList = new ObservationList(newKey);
				}
				obsList.addObservation(obs);
				this.observationMap.put(newKey, obsList);
			}
		} catch (MiddlewareQueryException ex) {
			ex.printStackTrace();
			EnvironmentFilter.LOG.error("Database error!", ex);
			MessageNotifier.showError(this.getWindow(), "Database Error!", this.messageSource.getMessage(Message.ERROR_REPORT_TO));
		}
		// get trait names for columns
		this.recreateTable(true, false);

		Window parentWindow = this.getWindow();
		this.filterLocation = new FilterLocationDialog(this, parentWindow, this.filterLocationCountryMap);
		this.filterStudy = new FilterStudyDialog(this, parentWindow, this.studyEnvironmentMap);
		this.addConditionsDialog = new AddEnvironmentalConditionsDialog(this, parentWindow, environmentIdsList);

		this.isFilterLocationClicked = false;
		this.isFilterStudyClicked = false;

	}

	/**
	 * Used for the Trait Donor Query. The locations that measurements are assessed over are limited by the traits we are interested in. So
	 * a list is populated and we generate the environment table based on the traits selected
	 *
	 * @param selectedTraits
	 */
	public void populateEnvironmentsTable(List<Integer> selectedTraits) {

		this.traitsList = selectedTraits;
		this.populateEnvironmentsTable();
	}

	/**
	 * Sets up the environments table - this process undergoes query specific processing
	 *
	 */
	public void populateEnvironmentsTable() {
		this.tableEntriesMap = new HashMap<String, Object[]>();

		this.environmentCheckBoxComparisonMap = new HashMap<String, EnvironmentForComparison>();
		this.environmentCheckBoxMap = new HashMap<CheckBox, Item>();
		this.environmentForComparison = new HashSet<String>();

		this.filterLocationCountryMap = new HashMap<String, FilterByLocation>();
		this.studyEnvironmentMap = new HashMap<String, List<StudyReference>>();
		this.environmentIds = new HashSet<Integer>();

		this.recreateTable(true, false);

		List<Integer> environmentIdsList = new ArrayList<Integer>(this.environmentIds);

		Window parentWindow = this.getWindow();

		if (this.crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY) {
			this.filterStudy = new FilterStudyDialog(this, parentWindow, this.studyEnvironmentMap);
		} else if (this.crossStudyToolType == CrossStudyToolType.QUERY_FOR_ADAPTED_GERMPLASM) {
			this.filterStudy =
					new FilterStudyDialog(this, parentWindow, this.studyEnvironmentMap,
							EnvironmentFilter.QUERY_FOR_ADAPTED_GERMPLASM_WINDOW_NAME);
		} else {
			this.filterStudy = new FilterStudyDialog(this, parentWindow, this.studyEnvironmentMap);
		}

		this.filterLocation = new FilterLocationDialog(this, parentWindow, this.filterLocationCountryMap);
		this.addConditionsDialog = new AddEnvironmentalConditionsDialog(this, parentWindow, environmentIdsList);

		this.filterStudy.addStyleName(Reindeer.WINDOW_LIGHT);
		this.filterLocation.addStyleName(Reindeer.WINDOW_LIGHT);
		this.addConditionsDialog.addStyleName(Reindeer.WINDOW_LIGHT);

		this.isFilterLocationClicked = false;
		this.isFilterStudyClicked = false;

	}

	private void recreateTable(boolean recreateFilterLocationMap, boolean isAppliedClick) {
		this.environmentsTable.removeAllItems();

		if (recreateFilterLocationMap) {
			this.environmentCheckBoxComparisonMap = new HashMap<String, EnvironmentForComparison>();
			this.environmentCheckBoxMap = new HashMap<CheckBox, Item>();
		}
		this.environmentForComparison = new HashSet<String>();

		Map<String, Item> trialEnvIdTableMap = new HashMap<String, Item>();

		if (this.crossStudyToolType == CrossStudyToolType.QUERY_FOR_ADAPTED_GERMPLASM) {
			try {
				this.environments = this.crossStudyDataManager.getAllTrialEnvironments(this.includePublicData);

				Set<TrialEnvironment> trialEnvSet = this.environments.getTrialEnvironments();
				Iterator<TrialEnvironment> trialEnvIter = trialEnvSet.iterator();
				while (trialEnvIter.hasNext()) {

					TrialEnvironment trialEnv = trialEnvIter.next();

					String trialEnvIdString = String.valueOf(trialEnv.getId());

					if (!trialEnvIdTableMap.containsKey(trialEnvIdString)) {
						final String tableKey =
								trialEnv.getId() + FilterLocationDialog.DELIMITER + trialEnv.getLocation().getCountryName()
										+ FilterLocationDialog.DELIMITER + trialEnv.getLocation().getProvinceName()
										+ FilterLocationDialog.DELIMITER + trialEnv.getLocation().getLocationName()
										+ FilterLocationDialog.DELIMITER + trialEnv.getStudy().getName();
						this.environmentIds.add(trialEnv.getId());
						boolean isValidEntryAdd = true;
						if (isAppliedClick) {
							isValidEntryAdd = this.isValidEntry(trialEnv);
						}

						if (isValidEntryAdd) {
							Object[] objItem = new Object[this.tableColumnSize];

							if (this.tableEntriesMap.containsKey(tableKey)) {
								objItem = this.tableEntriesMap.get(tableKey);
								this.environmentsTable.addItem(objItem, tableKey);

								if (isAppliedClick) {
									// we simulate the checkbox
									((CheckBox) objItem[0]).setValue(true);
									this.clickCheckBox(tableKey, (ComboBox) objItem[objItem.length - 1], true);
								}
							} else {
								CheckBox box = new CheckBox();

								box.setImmediate(true);
								final ComboBox comboBox = this.getWeightComboBox();

								int counterTrait = 0;
								objItem[counterTrait++] = box;
								objItem[counterTrait++] = trialEnv.getId();
								objItem[counterTrait++] = trialEnv.getLocation().getLocationName();
								objItem[counterTrait++] = trialEnv.getLocation().getCountryName();
								objItem[counterTrait++] = trialEnv.getStudy().getName();

								if (recreateFilterLocationMap) {
									this.setupLocationMappings(trialEnv);
									this.tableEntriesMap.put(tableKey, objItem);
								}

								// insert environment condition here
								EnvironmentForComparison compare =
										new EnvironmentForComparison(trialEnv.getId(), trialEnv.getLocation().getLocationName(), trialEnv
												.getLocation().getCountryName(), trialEnv.getStudy().getName(), comboBox);

								objItem[counterTrait++] = comboBox;

								this.environmentsTable.addItem(objItem, tableKey);
								Item item = this.environmentsTable.getItem(tableKey);
								box.addListener(new ValueChangeListener() {

									private static final long serialVersionUID = -4759863142479248292L;

									@Override
									public void valueChange(ValueChangeEvent event) {
										EnvironmentFilter.this.clickCheckBox(tableKey, comboBox, (Boolean) event.getProperty().getValue());
									}
								});

								this.environmentCheckBoxMap.put(box, item);
								this.environmentCheckBoxComparisonMap.put(tableKey, compare);
								trialEnvIdTableMap.put(trialEnvIdString, item);
							}

						}
					}// end of if

				}

			} catch (MiddlewareQueryException ex) {
				ex.printStackTrace();
				EnvironmentFilter.LOG.error("Database error!", ex);
				MessageNotifier.showError(this.getWindow(), "Database Error!", this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			}
		} else if (this.crossStudyToolType == CrossStudyToolType.TRAIT_DONORS_QUERY) {
			try {

				// FIXME : hit a view for this query - too heavy
				this.environments = this.crossStudyDataManager.getEnvironmentsForTraits(this.traitsList);

				Set<TrialEnvironment> trialEnvSet = this.environments.getTrialEnvironments();
				Iterator<TrialEnvironment> trialEnvIter = trialEnvSet.iterator();
				while (trialEnvIter.hasNext()) {

					TrialEnvironment trialEnv = trialEnvIter.next();

					String trialEnvIdString = String.valueOf(trialEnv.getId());

					if (!trialEnvIdTableMap.containsKey(trialEnvIdString)) {
						final String tableKey =
								trialEnv.getId() + FilterLocationDialog.DELIMITER + trialEnv.getLocation().getCountryName()
										+ FilterLocationDialog.DELIMITER + trialEnv.getLocation().getProvinceName()
										+ FilterLocationDialog.DELIMITER + trialEnv.getLocation().getLocationName()
										+ FilterLocationDialog.DELIMITER + trialEnv.getStudy().getName();
						this.environmentIds.add(trialEnv.getId());
						boolean isValidEntryAdd = true;
						if (isAppliedClick) {
							isValidEntryAdd = this.isValidEntry(trialEnv);
						}

						if (isValidEntryAdd) {
							Object[] objItem = new Object[this.tableColumnSize];

							if (this.tableEntriesMap.containsKey(tableKey)) {
								objItem = this.tableEntriesMap.get(tableKey);
								this.environmentsTable.addItem(objItem, tableKey);

								if (isAppliedClick) {
									// we simulate the checkbox
									((CheckBox) objItem[0]).setValue(true);
									this.clickCheckBox(tableKey, (ComboBox) objItem[objItem.length - 1], true);
								}
							} else {
								CheckBox box = new CheckBox();

								box.setImmediate(true);
								final ComboBox comboBox = this.getWeightComboBox();

								int counterTrait = 0;
								objItem[counterTrait++] = box;
								objItem[counterTrait++] = trialEnv.getId();
								objItem[counterTrait++] = trialEnv.getLocation().getLocationName();
								objItem[counterTrait++] = trialEnv.getLocation().getCountryName();
								objItem[counterTrait++] = trialEnv.getStudy().getName();

								if (recreateFilterLocationMap) {
									this.setupLocationMappings(trialEnv);
									this.tableEntriesMap.put(tableKey, objItem);
								}

								// insert environment condition here
								EnvironmentForComparison compare =
										new EnvironmentForComparison(trialEnv.getId(), trialEnv.getLocation().getLocationName(), trialEnv
												.getLocation().getCountryName(), trialEnv.getStudy().getName(), comboBox);

								objItem[counterTrait++] = comboBox;

								this.environmentsTable.addItem(objItem, tableKey);
								Item item = this.environmentsTable.getItem(tableKey);
								box.addListener(new ValueChangeListener() {

									private static final long serialVersionUID = -4759863142479248292L;

									@Override
									public void valueChange(ValueChangeEvent event) {
										EnvironmentFilter.this.clickCheckBox(tableKey, comboBox, (Boolean) event.getProperty().getValue());
									}
								});

								this.environmentCheckBoxMap.put(box, item);
								this.environmentCheckBoxComparisonMap.put(tableKey, compare);
								trialEnvIdTableMap.put(trialEnvIdString, item);
							}

						}
					}// end of if

				}

			} catch (MiddlewareQueryException ex) {
				ex.printStackTrace();
				EnvironmentFilter.LOG.error("Database error!", ex);
				MessageNotifier.showError(this.getWindow(), "Database Error!", this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			}
		} else if (this.crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY) {
			this.createEnvironmentsTable(this.traitInfosNames);

			// clean the traitEnvMap
			Iterator<String> trialEnvIdsIter = this.trialEnvironmentIds.iterator();
			while (trialEnvIdsIter.hasNext()) {
				Integer trialEnvId = Integer.parseInt(trialEnvIdsIter.next());
				String trialEnvIdString = trialEnvId.toString();

				if (!trialEnvIdTableMap.containsKey(trialEnvIdString)) {
					TrialEnvironment trialEnv = this.trialEnvMap.get(trialEnvIdString);
					// we build the table
					String tableKey =
							trialEnvIdString + FilterLocationDialog.DELIMITER + trialEnv.getLocation().getCountryName()
									+ FilterLocationDialog.DELIMITER + trialEnv.getLocation().getProvinceName()
									+ FilterLocationDialog.DELIMITER + trialEnv.getLocation().getLocationName()
									+ FilterLocationDialog.DELIMITER + trialEnv.getStudy().getName();

					boolean isValidEntryAdd = true;
					if (isAppliedClick) {
						isValidEntryAdd = this.isValidEntry(trialEnv);

					}

					if (isValidEntryAdd) {

						Object[] objItem = new Object[this.tableColumnSize];

						if (this.tableEntriesMap.containsKey(tableKey)) {
							// to be use when filtering only
							// for recycling same object
							objItem = this.tableEntriesMap.get(tableKey);
							this.environmentsTable.addItem(objItem, tableKey);

							if (isAppliedClick) {

								// we simulate the checkbox
								((CheckBox) objItem[0]).setValue(true);
								this.clickCheckBox(tableKey, (ComboBox) objItem[objItem.length - 1], true);
							}
						} else {

							CheckBox box = new CheckBox();

							box.setImmediate(true);
							ComboBox comboBox = this.getWeightComboBox();

							int counterTrait = 0;
							objItem[counterTrait++] = box;
							objItem[counterTrait++] = trialEnv.getLocation().getLocationName();
							objItem[counterTrait++] = trialEnv.getLocation().getCountryName();
							objItem[counterTrait++] = trialEnv.getStudy().getName();

							if (recreateFilterLocationMap) {
								this.setupLocationMappings(trialEnv);
								this.tableEntriesMap.put(tableKey, objItem);
							}

							EnvironmentForComparison compare =
									new EnvironmentForComparison(trialEnv.getId(), trialEnv.getLocation().getLocationName(), trialEnv
											.getLocation().getCountryName(), trialEnv.getStudy().getName(), comboBox);
							LinkedHashMap<TraitForComparison, List<ObservationList>> traitAndObservationMap =
									new LinkedHashMap<TraitForComparison, List<ObservationList>>();
							Iterator<TraitForComparison> traitForCompareIter = this.traitForComparisonsList.iterator();
							while (traitForCompareIter.hasNext()) {
								TraitForComparison traitForCompare = traitForCompareIter.next();

								List<ObservationList> obsList = new ArrayList<ObservationList>();
								Integer count =
										this.getTraitCount(traitForCompare.getTraitInfo(), trialEnv.getId(), this.finalGermplasmPairs,
												obsList);
								traitAndObservationMap.put(traitForCompare, obsList);
								traitForCompare.setDisplay(true);
								objItem[counterTrait++] = count;
							}
							compare.setTraitAndObservationMap(traitAndObservationMap);

							objItem[counterTrait++] = comboBox;

							this.environmentsTable.addItem(objItem, tableKey);
							Item item = this.environmentsTable.getItem(tableKey);
							box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, comboBox, tableKey));
							this.environmentCheckBoxMap.put(box, item);
							this.environmentCheckBoxComparisonMap.put(tableKey, compare);
							trialEnvIdTableMap.put(trialEnvIdString, item);

						}
					}
				}

			}

			this.numberOfEnvironmentSelectedLabel.setValue(Integer.toString(this.environmentForComparison.size()));
		}

	}

	public void clickCheckBox(String key, Component combo, boolean boolVal) {

		if (combo != null) {
			ComboBox comboBox = (ComboBox) combo;
			comboBox.setEnabled(boolVal);

			if (boolVal) {
				comboBox.setValue(EnvironmentWeight.IMPORTANT);
			} else {
				comboBox.setValue(EnvironmentWeight.IGNORED);
			}

		}

		if (boolVal) {
			this.environmentForComparison.add(key);
		} else {
			this.environmentForComparison.remove(key);
		}

		if (this.environmentForComparison.isEmpty()) {
			this.nextButton.setEnabled(false);
		} else {
			this.nextButton.setEnabled(true);
		}

		this.numberOfEnvironmentSelectedLabel.setValue(Integer.toString(this.environmentForComparison.size()));
	}

	private boolean isValidEntry(TrialEnvironment trialEnv) {
		String countryName = trialEnv.getLocation().getCountryName();
		String locationName = trialEnv.getLocation().getLocationName();
		String studyName = trialEnv.getStudy().getName();

		boolean isValid = false;

		String level1Key = countryName;
		String level3Key = countryName + FilterLocationDialog.DELIMITER + locationName;
		String level4Key = studyName;

		// check against the map
		if (this.isFilterLocationClicked) {
			if (this.filterSetLevel1.containsKey(level1Key)) {
				isValid = true;
			} else if (this.filterSetLevel3.containsKey(level3Key)) {
				isValid = true;
			}
		}

		if (this.isFilterStudyClicked) {

			if (this.isFilterLocationClicked) {
				// meaning there is a filter in location already
				if (isValid) {
					// we only filter again if its valid
					if (this.filterSetLevel4.containsKey(level4Key)) {
						isValid = true;
					} else {
						isValid = false;
					}
				}
			} else {
				if (this.filterSetLevel4.containsKey(level4Key)) {
					isValid = true;
				} else {
					isValid = false;
				}
			}

		}

		return isValid;
	}

	private ComboBox getWeightComboBox() {
		return CrossStudyUtil.getWeightComboBox();
	}

	private void setupLocationMappings(TrialEnvironment trialEnv) {
		LocationDto location = trialEnv.getLocation();
		StudyReference study = trialEnv.getStudy();
		String trialEnvId = Integer.toString(trialEnv.getId());
		String countryName = location.getCountryName();
		String provinceName = location.getProvinceName();
		String locationName = location.getLocationName();
		String studyName = study.getName();

		FilterByLocation countryFilter = this.filterLocationCountryMap.get(countryName);

		if (countryFilter == null) {
			countryFilter = new FilterByLocation(countryName, trialEnvId);
		}

		countryFilter.addProvinceAndLocationAndStudy(provinceName, locationName, studyName);
		this.filterLocationCountryMap.put(countryName, countryFilter);

		// for the mapping in the study level
		String studyKey = study.getName() + FilterLocationDialog.DELIMITER + study.getDescription();
		List<StudyReference> studyReferenceList = this.studyEnvironmentMap.get(studyKey);
		if (studyReferenceList == null) {
			studyReferenceList = new ArrayList<StudyReference>();
		}
		studyReferenceList.add(study);
		this.studyEnvironmentMap.put(studyKey, studyReferenceList);

	}

	private Integer getTraitCount(TraitInfo traitInfo, int envId, List<GermplasmPair> germplasmPairs, List<ObservationList> obsList) {
		int counter = 0;

		for (GermplasmPair pair : germplasmPairs) {
			String keyToChecked1 = traitInfo.getId() + ":" + envId + ":" + pair.getGid1();
			String keyToChecked2 = traitInfo.getId() + ":" + envId + ":" + pair.getGid2();
			ObservationList obs1 = this.observationMap.get(keyToChecked1);
			ObservationList obs2 = this.observationMap.get(keyToChecked2);

			if (obs1 != null && obs2 != null) {
				if (obs1.isValidObservationList() && obs2.isValidObservationList()) {
					counter++;
					obsList.add(obs1);
					obsList.add(obs2);
				}

			}

		}
		return Integer.valueOf(counter);
	}

	public void nextButtonClickAction() {
		final List<EnvironmentForComparison> toBeCompared = new ArrayList<EnvironmentForComparison>();

		int total = 0;
		// get the total of weights
		for (String sKey : this.environmentForComparison) {
			EnvironmentForComparison envt = this.environmentCheckBoxComparisonMap.get(sKey);
			EnvironmentWeight envtWeight = (EnvironmentWeight) envt.getWeightComboBox().getValue();
			total += envtWeight.getWeight();
		}
		EnvironmentFilter.LOG.debug("TOTAL = " + total);

		for (String sKey : this.environmentForComparison) {
			EnvironmentForComparison envt = this.environmentCheckBoxComparisonMap.get(sKey);
			envt.computeWeight(total);

			toBeCompared.add(envt);
		}
		if (this.environmentForComparison.size() > 1000) {
			ConfirmDialog.show(this.getWindow(), "", this.messageSource.getMessage(Message.LOAD_TRAITS_CONFIRM),
					this.messageSource.getMessage(Message.OK_LABEL), this.messageSource.getMessage(Message.CANCEL_LABEL),
					new ConfirmDialog.Listener() {

						private static final long serialVersionUID = 1L;

				@Override
				public void onClose(ConfirmDialog dialog) {
					if (dialog.isConfirmed()) {
						EnvironmentFilter.this.nextTabAction(toBeCompared);
					}

						}
			});
		} else {
			this.nextTabAction(toBeCompared);
		}
	}

	/**
	 * Navigation to the next accorsion tab. The H2H goes to results, whereby the Adapted Germplasm and the Trait Donor Query move in to the
	 * presentation and range selection of trait observations
	 *
	 * @param toBeCompared : environments to select traits from
	 */
	public void nextTabAction(List<EnvironmentForComparison> toBeCompared) {

		if (this.crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY) {
			this.nextScreen1.populateResultsTable(toBeCompared, this.germplasmIdNameMap, this.finalGermplasmPairs, this.observationMap);
			this.mainScreen1.selectFourthTab();
		} else if (this.crossStudyToolType == CrossStudyToolType.QUERY_FOR_ADAPTED_GERMPLASM) {
			if (this.nextScreen2 != null) {
				this.nextScreen2.populateTraitsTables(toBeCompared);
			}
			this.mainScreen2.selectSecondTab();
		} else if (this.crossStudyToolType == CrossStudyToolType.TRAIT_DONORS_QUERY) {
			if (this.nextScreen3 != null) {
				this.nextScreen3.populateTraitsTables(toBeCompared, this.traitsList);
			}
			this.mainScreen3.selectThirdTab();
		}

	}

	public void backButtonClickAction() {
		if (this.crossStudyToolType == CrossStudyToolType.HEAD_TO_HEAD_QUERY) {
			this.mainScreen1.selectSecondTab();
		}
	}

	public void selectFilterByLocationClickAction() {

		Window parentWindow = this.getWindow();
		this.filterLocation.initializeButtons();
		parentWindow.addWindow(this.filterLocation);
	}

	public void selectFilterByStudyClickAction() {

		Window parentWindow = this.getWindow();
		this.filterStudy.initializeButtons();
		parentWindow.addWindow(this.filterStudy);
	}

	public void addEnvironmentalConditionsClickAction() {

		Window parentWindow = this.getWindow();
		parentWindow.addWindow(this.addConditionsDialog);
	}

	public void clickFilterByLocationApply(List<FilterLocationDto> filterLocationDtoListLevel1,
			List<FilterLocationDto> filterLocationDtoListLevel3) {

		this.isFilterLocationClicked = true;
		this.filterSetLevel1 = new HashMap<String, String>();
		this.filterSetLevel3 = new HashMap<String, String>();

		for (FilterLocationDto dto : filterLocationDtoListLevel1) {
			String countryName = dto.getCountryName();

			this.filterSetLevel1.put(countryName, countryName);
		}

		for (FilterLocationDto dto : filterLocationDtoListLevel3) {
			String countryName = dto.getCountryName();
			String locationName = dto.getLocationName();
			String key = countryName + FilterLocationDialog.DELIMITER + locationName;// + FilterLocationDialog.DELIMITER + studyName;

			this.filterSetLevel3.put(key, key);
			// we need to remove in the 1st level since this mean we want specific level 2 filter
			this.filterSetLevel1.remove(countryName);
		}

		this.recreateTable(false, true);

		this.headerValLabel.setValue("");
	}

	public void clickFilterByStudyApply(List<FilterLocationDto> filterLocationDtoListLevel4) {
		this.isFilterStudyClicked = true;
		this.filterSetLevel4 = new HashMap<String, String>();
		for (FilterLocationDto dto : filterLocationDtoListLevel4) {
			String studyName = dto.getStudyName();

			this.filterSetLevel4.put(studyName, studyName);
		}
		this.recreateTable(false, true);

		this.headerValLabel.setValue("");
	}

	public void reopenFilterWindow() {
		// this is to simulate and refresh checkboxes
		Window parentWindow = this.getWindow();
		parentWindow.removeWindow(this.filterLocation);

		this.filterLocation.initializeButtons();
		parentWindow.addWindow(this.filterLocation);
	}

	public void reopenFilterStudyWindow() {
		// this is to simulate and refresh checkboxes
		Window parentWindow = this.getWindow();
		parentWindow.removeWindow(this.filterStudy);

		this.filterStudy.initializeButtons();
		parentWindow.addWindow(this.filterStudy);
	}

	public void reopenAddEnvironmentConditionsWindow() {
		// this is to simulate and refresh checkboxes
		Window parentWindow = this.getWindow();
		parentWindow.removeWindow(this.addConditionsDialog);

		this.filterStudy.initializeButtons();
		parentWindow.addWindow(this.addConditionsDialog);
	}

	/*
	 * Callback method for AddEnvironmentalConditionsDialog button
	 */

	public void addEnviromentalConditionColumns(Set<TrialEnvironmentProperty> conditions) {
		// remove previously added envt conditions columns, if any
		this.removeAddedEnvironmentConditionsColumns();

		// add the selected envt condition column(s)
		for (final TrialEnvironmentProperty condition : conditions) {
			this.addedEnvironmentColumns.add(condition.getName());

			this.environmentsTable.addGeneratedColumn(condition.getName(), new ColumnGenerator() {

				private static final long serialVersionUID = 1L;

				@Override
				public Object generateCell(Table source, Object itemId, Object columnId) {
					StringTokenizer st = new StringTokenizer((String) itemId, FilterLocationDialog.DELIMITER);

					String envtIdStr = st.nextToken();
					if (envtIdStr != null && !envtIdStr.isEmpty()) {
						Integer envtId = Integer.parseInt(envtIdStr);

						return condition.getEnvironmentValuesMap().get(envtId);
					}

					return "";
				}

			});

		}

		this.resizeEnviromentTable(conditions);

	}

	private void resizeEnviromentTable(Set<TrialEnvironmentProperty> columns) {
		float tableWidth = this.environmentsTable.getWidth();

		for (int i = 0; i < columns.size(); i++) {
			tableWidth += 133;
		}

		List<String> cols = new ArrayList<String>();
		for (TrialEnvironmentProperty col : columns) {
			cols.add(col.getName());
		}

		Object[] visibleCols = this.environmentsTable.getVisibleColumns();
		for (Object col : visibleCols) {
			if (cols.contains(col)) {
				this.environmentsTable.setColumnWidth(col, 120);
			}
		}

		String width = String.valueOf(tableWidth) + "px";
		this.tableLayout.setWidth(width);
		this.environmentsTable.setWidth(width);
		this.tablePanel.requestRepaint();
	}

	private void removeAddedEnvironmentConditionsColumns() {
		if (this.environmentsTable != null && this.addedEnvironmentColumns != null) {
			for (String columnHeader : this.addedEnvironmentColumns) {
				String existingColumn = this.environmentsTable.getColumnHeader(columnHeader);
				if (existingColumn != null && !existingColumn.isEmpty()) {
					this.environmentsTable.removeGeneratedColumn(columnHeader);
					float previousWidth = this.environmentsTable.getWidth() - 133;
					String width = String.valueOf(previousWidth) + "px";
					this.environmentsTable.setWidth(width);
				}
			}
		}
		this.addedEnvironmentColumns = new ArrayList<String>();
	}

	private enum CrossStudyToolType {
		HEAD_TO_HEAD_QUERY(org.generationcp.breeding.manager.cross.study.h2h.main.HeadToHeadCrossStudyMain.class, "Head to Head Query"), QUERY_FOR_ADAPTED_GERMPLASM(
				org.generationcp.breeding.manager.cross.study.adapted.main.QueryForAdaptedGermplasmMain.class, "Query for Adapted Germplasm"), TRAIT_DONORS_QUERY(
				org.generationcp.breeding.manager.cross.study.traitdonors.main.TraitDonorsQueryMain.class, "Trait Donors Query");

		private Class<?> mainClass;
		private String className;

		private CrossStudyToolType(Class<?> mainClass, String className) {
			this.mainClass = mainClass;
			this.className = className;
		}

		@SuppressWarnings("unused")
		public Class<?> getMainClass() {
			return this.mainClass;
		}

		@SuppressWarnings("unused")
		public String getClassName() {
			return this.className;
		}

	}

	private void tagAllEnvironments() {
		Object tableItemIds[] = this.environmentsTable.getItemIds().toArray();
		for (int i = 0; i < tableItemIds.length; i++) {
			if (this.environmentsTable.getItem(tableItemIds[i].toString()).getItemProperty(EnvironmentFilter.TAG_COLUMN_ID).getValue() instanceof CheckBox) {
				((CheckBox) this.environmentsTable.getItem(tableItemIds[i]).getItemProperty(EnvironmentFilter.TAG_COLUMN_ID).getValue())
						.setValue(true);
			}
		}
	}

	private void untagAllEnvironments() {
		Object tableItemIds[] = this.environmentsTable.getItemIds().toArray();
		for (int i = 0; i < tableItemIds.length; i++) {
			if (this.environmentsTable.getItem(tableItemIds[i].toString()).getItemProperty(EnvironmentFilter.TAG_COLUMN_ID).getValue() instanceof CheckBox) {
				((CheckBox) this.environmentsTable.getItem(tableItemIds[i]).getItemProperty(EnvironmentFilter.TAG_COLUMN_ID).getValue())
						.setValue(false);
			}
		}
	}
}
