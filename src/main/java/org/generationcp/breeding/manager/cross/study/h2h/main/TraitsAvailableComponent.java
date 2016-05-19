
package org.generationcp.breeding.manager.cross.study.h2h.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.dms.TrialEnvironments;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.domain.h2h.TraitType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnResizeEvent;

@Configurable
public class TraitsAvailableComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 991899235025710803L;

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(org.generationcp.breeding.manager.cross.study.h2h.main.TraitsAvailableComponent.class);

	public static final String BACK_BUTTON_ID = "TraitsAvailableComponent Back Button ID";
	public static final String NEXT_BUTTON_ID = "TraitsAvailableComponent Next Button ID";
	public static final String CHECKBOX_ID = "TraitsAvailableComponent Checkbox ID";

	private static final String TRAIT_COLUMN_ID = "TraitsAvailableComponent Trait Column Id";
	private static final String TRAIT_DESCRIPTION_COLUMN_ID = "TraitsAvailableComponent Trait Description Column Id";
	private static final String NUMBER_OF_ENV_COLUMN_ID = "TraitsAvailableComponent Number of Environments Column Id";
	private static final String TAG_COLUMN_ID = "TraitsAvailableComponent Tag Column Id";
	private static final String DIRECTION_COLUMN_ID = "TraitsAvailableComponent Direction Column Id";
	private static final String TAG_ALL = "TraitsAvailableComponent TAG_ALL Column Id";

	private Table traitsTable;

	private Button nextButton;
	private Button backButton;

	private final HeadToHeadCrossStudyMain mainScreen;
	private final EnvironmentFilter nextScreen;

	private Label selectTraitLabel;

	private Label selectTraitReminderLabel;

	public static final Integer INCREASING = 1;
	public static final Integer DECREASING = 0;

	@Autowired
	private CrossStudyDataManager crossStudyDataManager;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	// will contain all the tagged row
	private List<ComboBox> traitForComparisons;
	// will contain the mapping from comboBox to the specific row
	private Map<ComboBox, TraitInfo> traitMaps;
	// will contain the map of trait and trial environment
	private Map<String, Map<String, TrialEnvironment>> traitEnvironmentMap;
	// will contain the map of trial environment
	private Map<String, TrialEnvironment> trialEnvironmentMap;
	private Map<String, String> germplasmIdNameMap;

	private Set<Integer> germplasmIds;
	private List<GermplasmPair> finalGermplasmPair;
	private List<GermplasmPair> prevfinalGermplasmPair;
	private List<GermplasmPair> environmentPairList;

	private CheckBox tagUnTagAll;

	private Panel tablePanel;
	private AbsoluteLayout tableLayout;

	public TraitsAvailableComponent(HeadToHeadCrossStudyMain mainScreen, EnvironmentFilter nextScreen) {
		this.mainScreen = mainScreen;
		this.nextScreen = nextScreen;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setHeight("500px");
		this.setWidth("1000px");

		this.selectTraitLabel = new Label(this.messageSource.getMessage(Message.HEAD_TO_HEAD_SELECT_TRAITS));
		this.selectTraitLabel.setImmediate(true);
		this.addComponent(this.selectTraitLabel, "top:20px;left:30px");

		this.selectTraitReminderLabel = new Label(this.messageSource.getMessage(Message.HEAD_TO_HEAD_SELECT_TRAITS_REMINDER));
		this.selectTraitReminderLabel.setImmediate(true);
		this.selectTraitReminderLabel.setStyleName("gcp-bold-italic");
		this.addComponent(this.selectTraitReminderLabel, "top:20px;left:400px");

		this.tablePanel = new Panel();
		this.tablePanel.setWidth("950px");
		this.tablePanel.setHeight("400px");

		this.tableLayout = new AbsoluteLayout();
		this.tableLayout.setWidth("950px");

		this.traitsTable = new Table();
		this.traitsTable.setWidth("948px");
		this.traitsTable.setHeight("380px");
		this.traitsTable.setImmediate(true);

		this.traitsTable.addContainerProperty(TraitsAvailableComponent.TAG_COLUMN_ID, CheckBox.class, null);
		this.traitsTable.addContainerProperty(TraitsAvailableComponent.TRAIT_COLUMN_ID, String.class, null);
		this.traitsTable.addContainerProperty(TraitsAvailableComponent.TRAIT_DESCRIPTION_COLUMN_ID, String.class, null);
		this.traitsTable.addContainerProperty(TraitsAvailableComponent.NUMBER_OF_ENV_COLUMN_ID, Integer.class, null);
		this.traitsTable.addContainerProperty(TraitsAvailableComponent.DIRECTION_COLUMN_ID, ComboBox.class, null);

		this.traitsTable.setColumnHeader(TraitsAvailableComponent.TAG_COLUMN_ID, this.messageSource.getMessage(Message.HEAD_TO_HEAD_TAG));
		this.traitsTable.setColumnHeader(TraitsAvailableComponent.TRAIT_COLUMN_ID,
				this.messageSource.getMessage(Message.HEAD_TO_HEAD_TRAIT));
		this.traitsTable.setColumnHeader(TraitsAvailableComponent.TRAIT_DESCRIPTION_COLUMN_ID, "Description");
		this.traitsTable.setColumnHeader(TraitsAvailableComponent.NUMBER_OF_ENV_COLUMN_ID,
				this.messageSource.getMessage(Message.HEAD_TO_HEAD_NO_OF_ENVS));
		this.traitsTable.setColumnHeader(TraitsAvailableComponent.DIRECTION_COLUMN_ID,
				this.messageSource.getMessage(Message.HEAD_TO_HEAD_DIRECTION));

		this.traitsTable.setColumnWidth(TraitsAvailableComponent.TAG_COLUMN_ID, 50);
		this.traitsTable.setColumnWidth(TraitsAvailableComponent.TRAIT_COLUMN_ID, 150);
		this.traitsTable.setColumnWidth(TraitsAvailableComponent.TRAIT_DESCRIPTION_COLUMN_ID, 300);
		this.traitsTable.setColumnWidth(TraitsAvailableComponent.NUMBER_OF_ENV_COLUMN_ID, 155);
		this.traitsTable.setColumnWidth(TraitsAvailableComponent.DIRECTION_COLUMN_ID, 200);

		this.tableLayout.addComponent(this.traitsTable, "top:0px;left:0px");

		this.traitsTable.addListener(new Table.ColumnResizeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void columnResize(ColumnResizeEvent event) {
				int diff = event.getCurrentWidth() - event.getPreviousWidth();
				float newWidth = diff + TraitsAvailableComponent.this.traitsTable.getWidth();

				String widthPx = String.valueOf(newWidth) + "px";
				TraitsAvailableComponent.this.traitsTable.setWidth(widthPx);
				TraitsAvailableComponent.this.tableLayout.setWidth(widthPx);
			}
		});

		this.tagUnTagAll = new CheckBox();
		this.tagUnTagAll.setValue(false);
		this.tagUnTagAll.setImmediate(true);
		this.tagUnTagAll.setData(TraitsAvailableComponent.TAG_ALL);
		this.tagUnTagAll.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, true));

		this.tableLayout.addComponent(this.tagUnTagAll, "top:4px;left:30px");

		this.tablePanel.setContent(this.tableLayout);
		this.addComponent(this.tablePanel, "top:40px;left:30px");

		this.nextButton = new Button("Next");
		this.nextButton.setData(TraitsAvailableComponent.NEXT_BUTTON_ID);
		this.nextButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
		this.nextButton.setEnabled(false);
		this.nextButton.setWidth("80px");
		this.nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.addComponent(this.nextButton, "top:450px;left:500px");

		this.backButton = new Button("Back");
		this.backButton.setData(TraitsAvailableComponent.BACK_BUTTON_ID);
		this.backButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
		this.backButton.setWidth("80px");
		this.addComponent(this.backButton, "top:450px;left:410px");

	}

	private ComboBox getDirectionComboBox() {
		ComboBox combo = new ComboBox();
		combo.setNullSelectionAllowed(false);
		combo.setTextInputAllowed(false);
		combo.setImmediate(true);

		combo.addItem(TraitsAvailableComponent.INCREASING);
		combo.setItemCaption(TraitsAvailableComponent.INCREASING, this.messageSource.getMessage(Message.HEAD_TO_HEAD_INCREASING));

		combo.addItem(TraitsAvailableComponent.DECREASING);
		combo.setItemCaption(TraitsAvailableComponent.DECREASING, this.messageSource.getMessage(Message.HEAD_TO_HEAD_DECREASING));

		combo.setValue(TraitsAvailableComponent.INCREASING);

		combo.setEnabled(false);
		return combo;

	}

	public void populateTraitsAvailableTable(List<GermplasmPair> germplasmPairList, Map<String, String> germplasmIdNameMap) {
		this.traitsTable.removeAllItems();
		this.nextButton.setEnabled(false);

		this.selectTraitReminderLabel.setVisible(true);
		this.germplasmIdNameMap = germplasmIdNameMap;
		this.traitForComparisons = new ArrayList<ComboBox>();
		this.traitMaps = new HashMap<ComboBox, TraitInfo>();

		Map<String, List<TraitInfo>> traitMap = new HashMap<String, List<TraitInfo>>();
		Map<String, Set<String>> traitEnvMap = new HashMap<String, Set<String>>();
		this.traitEnvironmentMap = new HashMap<String, Map<String, TrialEnvironment>>();
		this.trialEnvironmentMap = new HashMap<String, TrialEnvironment>();
		this.germplasmIds = new HashSet<Integer>();

		this.finalGermplasmPair = germplasmPairList;
		boolean doRefresh = false;
		if (this.prevfinalGermplasmPair == null) {
			doRefresh = true;
		} else {
			// we checked if its the same
			if (this.prevfinalGermplasmPair.size() == this.finalGermplasmPair.size()) {
				doRefresh = false;
				for (GermplasmPair pairOld : this.prevfinalGermplasmPair) {
					boolean isMatched = false;
					for (GermplasmPair pairNew : this.finalGermplasmPair) {
						if (pairOld.getGid1() == pairNew.getGid1() && pairOld.getGid2() == pairNew.getGid2()) {
							isMatched = true;
							break;
						}
					}
					if (!isMatched) {
						// meaning new pair
						doRefresh = true;
						break;
					}
				}
			} else {
				doRefresh = true;
			}

		}

		try {

			if (doRefresh) {
				// only call when need to refresh
				this.prevfinalGermplasmPair = germplasmPairList;
				this.environmentPairList = this.crossStudyDataManager.getEnvironmentsForGermplasmPairs(germplasmPairList);
			}

			for (GermplasmPair pair : this.environmentPairList) {
				TrialEnvironments env = pair.getTrialEnvironments();

				this.germplasmIds.add(Integer.valueOf(pair.getGid1()));
				this.germplasmIds.add(Integer.valueOf(pair.getGid2()));

				java.util.Iterator<TrialEnvironment> envIterator = env.getTrialEnvironments().iterator();
				while (envIterator.hasNext()) {
					TrialEnvironment trialEnv = envIterator.next();
					this.trialEnvironmentMap.put(Integer.toString(trialEnv.getId()), trialEnv);
					java.util.Iterator<TraitInfo> traitIterator = trialEnv.getTraits().iterator();
					while (traitIterator.hasNext()) {
						TraitInfo info = traitIterator.next();

						// add here the checking if the trait is non numeric
						if (info.getType() != TraitType.NUMERIC) {
							continue;
						}

						String id = Integer.toString(info.getId());
						List<TraitInfo> tempList = new ArrayList<TraitInfo>();
						if (traitMap.containsKey(id)) {
							tempList = traitMap.get(id);
						}
						tempList.add(info);
						traitMap.put(id, tempList);
						Set<String> envIds = traitEnvMap.get(id);
						if (envIds == null) {
							envIds = new HashSet<String>();
						}
						envIds.add(trialEnv.getId() + "");
						traitEnvMap.put(id, envIds);

						// we need to keep track on the environments
						Map<String, TrialEnvironment> tempEnvMap = new HashMap<String, TrialEnvironment>();
						if (this.traitEnvironmentMap.containsKey(id)) {
							tempEnvMap = this.traitEnvironmentMap.get(id);
						}
						tempEnvMap.put(Integer.toString(trialEnv.getId()), trialEnv);
						this.traitEnvironmentMap.put(id, tempEnvMap);

					}
				}

			}
			java.util.Iterator<String> traitsIterator = traitMap.keySet().iterator();
			while (traitsIterator.hasNext()) {
				String id = traitsIterator.next();
				List<TraitInfo> traitInfoList = traitMap.get(id);
				// we get the 1st one since its all the same for this specific list
				TraitInfo info = traitInfoList.get(0);
				CheckBox box = new CheckBox();
				ComboBox comboBox = this.getDirectionComboBox();
				box.setImmediate(true);
				Integer tableId = Integer.valueOf(id);

				Integer numOfEnv = traitEnvMap.get(id).size();
				this.traitsTable.addItem(new Object[] {box, info.getName(), info.getDescription(), numOfEnv, comboBox}, tableId);

				box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, comboBox));

				this.traitMaps.put(comboBox, info);

			}

			if (this.traitsTable.getItemIds().isEmpty()) {
				MessageNotifier.showWarning(this.getWindow(), "Warning!",
						"No environments and traits were found for the pairs of germplasm entries you have specified.");
				return;
			}
		} catch (MiddlewareQueryException e) {
			TraitsAvailableComponent.LOG.error(e.getMessage(), e);
		}
	}

	public void clickCheckBox(Component combo, boolean boolVal) {
		if (combo != null) {
			ComboBox comboBox = (ComboBox) combo;
			comboBox.setEnabled(boolVal);
			TraitInfo info = this.traitMaps.get(comboBox);

			if (info != null) {
				if (boolVal) {
					this.traitForComparisons.add(comboBox);
				} else {
					this.traitForComparisons.remove(comboBox);
				}
			}

			if (this.traitForComparisons.isEmpty()) {
				this.nextButton.setEnabled(false);
				this.selectTraitReminderLabel.setVisible(true);
			} else {
				this.nextButton.setEnabled(true);
				this.selectTraitReminderLabel.setVisible(false);
			}
		}
	}

	public void clickTagAllCheckbox(boolean boxChecked) {
		Object[] tableItemIds = this.traitsTable.getItemIds().toArray();
		for (int i = 0; i < tableItemIds.length; i++) {
			Item row = this.traitsTable.getItem(tableItemIds[i]);
			CheckBox box = (CheckBox) row.getItemProperty(TraitsAvailableComponent.TAG_COLUMN_ID).getValue();
			box.setValue(boxChecked);
		}
	}

	public void nextButtonClickAction() {
		List<TraitForComparison> traitForComparisonsList = new ArrayList<TraitForComparison>();
		for (ComboBox combo : this.traitForComparisons) {
			TraitInfo info = this.traitMaps.get(combo);
			TraitForComparison traitForComparison = new TraitForComparison(info, (Integer) combo.getValue());
			traitForComparisonsList.add(traitForComparison);
		}
		if (this.nextScreen != null) {
			this.nextScreen.populateEnvironmentsTable(traitForComparisonsList, this.traitEnvironmentMap, this.trialEnvironmentMap,
					this.germplasmIds, this.finalGermplasmPair, this.germplasmIdNameMap);
		}
		this.mainScreen.selectThirdTab();
	}

	public void backButtonClickAction() {
		this.mainScreen.selectFirstTab();
	}

	@Override
	public void updateLabels() {
		// do nothing
	}
}
