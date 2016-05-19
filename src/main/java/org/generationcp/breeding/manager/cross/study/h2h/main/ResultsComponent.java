
package org.generationcp.breeding.manager.cross.study.h2h.main;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.breeding.manager.application.GermplasmStudyBrowserApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.ObservationList;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.ResultsData;
import org.generationcp.breeding.manager.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.breeding.manager.cross.study.h2h.main.util.HeadToHeadDataListExport;
import org.generationcp.breeding.manager.cross.study.h2h.main.util.HeadToHeadDataListExportException;
import org.generationcp.breeding.manager.cross.study.util.HeadToHeadResultsUtil;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.util.FileDownloadResource;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class ResultsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 2305982279660448571L;

	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.breeding.manager.cross.study.h2h.main.ResultsComponent.class);

	private static final String MEAN_TEST_COLUMN_ID = "ResultsComponent Mean Test Column ID";
	private static final String MEAN_STD_COLUMN_ID = "ResultsComponent Mean STD Column ID";

	public static final String TEST_COLUMN_ID = "ResultsComponent Test Column ID";
	public static final String STANDARD_COLUMN_ID = "ResultsComponent Standard Column ID";
	public static final String NUM_OF_ENV_COLUMN_ID = "ResultsComponent Num Of Env Column ID";
	public static final String NUM_SUP_COLUMN_ID = "ResultsComponent Num Sup Column ID";
	public static final String PVAL_COLUMN_ID = "ResultsComponent Pval Column ID";
	public static final String MEAN_DIFF_COLUMN_ID = "ResultsComponent Mean Diff Column ID";

	private Table[] resultsTable;

	private Label testEntryNameLabel;
	private Label standardEntryNameLabel;

	public static final String BACK_BUTTON_ID = "ResultsComponent Back Button ID";
	public static final String EXPORT_BUTTON_ID = "ResultsComponent Export Button ID";

	public static final String USER_HOME = "user.home";

	private Button exportButton;
	private Button backButton;

	private final HeadToHeadCrossStudyMain mainScreen;
	private List<EnvironmentForComparison> finalEnvironmentForComparisonList;

	private final String[] columnIdData = {ResultsComponent.NUM_OF_ENV_COLUMN_ID, ResultsComponent.NUM_SUP_COLUMN_ID,
			ResultsComponent.MEAN_TEST_COLUMN_ID, ResultsComponent.MEAN_STD_COLUMN_ID, ResultsComponent.PVAL_COLUMN_ID,
			ResultsComponent.MEAN_DIFF_COLUMN_ID};
	private final Map<String, String> columnIdDataMsgMap = new HashMap<String, String>();

	public static DecimalFormat decimalFormmatter = new DecimalFormat("#,##0.00");
	public List<ResultsData> resultsDataList = new ArrayList<ResultsData>();
	private TabSheet mainTabs;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public ResultsComponent(HeadToHeadCrossStudyMain mainScreen) {
		this.mainScreen = mainScreen;

		// initialize the data map
		this.columnIdDataMsgMap.put(ResultsComponent.NUM_OF_ENV_COLUMN_ID, "#Env");
		this.columnIdDataMsgMap.put(ResultsComponent.NUM_SUP_COLUMN_ID, "#Sup");
		this.columnIdDataMsgMap.put(ResultsComponent.MEAN_TEST_COLUMN_ID, "MeanTest");
		this.columnIdDataMsgMap.put(ResultsComponent.MEAN_STD_COLUMN_ID, "MeanStd");
		this.columnIdDataMsgMap.put(ResultsComponent.PVAL_COLUMN_ID, "Pval");
		this.columnIdDataMsgMap.put(ResultsComponent.MEAN_DIFF_COLUMN_ID, "MeanDiff");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setHeight("550px");
		this.setWidth("1000px");

		this.exportButton = new Button("Export");
		this.exportButton.setData(ResultsComponent.EXPORT_BUTTON_ID);
		this.exportButton
				.addListener(new org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener(this));
		this.exportButton.setEnabled(true);
		this.exportButton.setWidth("80px");
		this.exportButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

		this.backButton = new Button("Back");
		this.backButton.setData(ResultsComponent.BACK_BUTTON_ID);
		this.backButton.setWidth("80px");
		this.backButton
				.addListener(new org.generationcp.breeding.manager.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener(this));

	}

	private void createEnvironmentsResultTable(List<EnvironmentForComparison> environmentForComparisonList,
			Map<String, String> germplasmNameIdMap, List<GermplasmPair> germplasmPairList, Map<String, ObservationList> observationMap) {

		this.removeAllComponents();
		this.addComponent(this.exportButton, "top:505px;left:500px");
		this.addComponent(this.backButton, "top:505px;left:410px");

		this.mainTabs = new TabSheet();
		this.mainTabs.setWidth("957px");
		this.mainTabs.setHeight("475px");
		this.addComponent(this.mainTabs, "top:20px;left:20px");

		this.finalEnvironmentForComparisonList = environmentForComparisonList;
		EnvironmentForComparison envForComparison = environmentForComparisonList.get(0);
		Set<TraitForComparison> traitsIterator = envForComparison.getTraitAndObservationMap().keySet();
		Iterator<TraitForComparison> iter = traitsIterator.iterator();
		TraitForComparison[] traitsIteratorArray = new TraitForComparison[traitsIterator.size()];
		int x = 0;
		while (iter.hasNext()) {

			traitsIteratorArray[x++] = iter.next();
		}
		int traitSize = envForComparison.getTraitAndObservationMap().keySet().size();
		this.resultsTable = new Table[traitSize];
		VerticalLayout[] layouts = new VerticalLayout[traitSize];
		this.resultsDataList = new ArrayList<ResultsData>();

		for (int counter = 0; counter < traitsIteratorArray.length; counter++) {
			TraitForComparison traitForCompare = traitsIteratorArray[counter];
			if (traitForCompare.isDisplay()) {
				this.resultsTable[counter] = new Table();
				this.resultsTable[counter].setWidth("912px");
				this.resultsTable[counter].setHeight("400px");
				this.resultsTable[counter].setImmediate(true);
				this.resultsTable[counter].setColumnCollapsingAllowed(true);
				this.resultsTable[counter].setColumnReorderingAllowed(true);

				this.resultsTable[counter].addContainerProperty(ResultsComponent.TEST_COLUMN_ID, String.class, null);
				this.resultsTable[counter].addContainerProperty(ResultsComponent.STANDARD_COLUMN_ID, String.class, null);

				this.resultsTable[counter].setColumnHeader(ResultsComponent.TEST_COLUMN_ID, "Test Entry");
				this.resultsTable[counter].setColumnHeader(ResultsComponent.STANDARD_COLUMN_ID, "Standard Entry");

				this.resultsTable[counter].setColumnAlignment(ResultsComponent.TEST_COLUMN_ID, Table.ALIGN_CENTER);
				this.resultsTable[counter].setColumnAlignment(ResultsComponent.STANDARD_COLUMN_ID, Table.ALIGN_CENTER);

				for (String columnKey : this.columnIdData) {
					String msg = this.columnIdDataMsgMap.get(columnKey);
					this.resultsTable[counter].addContainerProperty(traitForCompare.getTraitInfo().getName() + columnKey, String.class,
							null);
					this.resultsTable[counter].setColumnHeader(traitForCompare.getTraitInfo().getName() + columnKey, msg);
					this.resultsTable[counter].setColumnAlignment(traitForCompare.getTraitInfo().getName() + columnKey, Table.ALIGN_CENTER);

				}
				layouts[counter] = new VerticalLayout();
				layouts[counter].setMargin(true);
				layouts[counter].setSpacing(true);

				layouts[counter].addComponent(this.resultsTable[counter]);
				this.mainTabs.addTab(layouts[counter], traitForCompare.getTraitInfo().getName());

			}
		}

		for (GermplasmPair germplasmPair : germplasmPairList) {
			String uniquieId = germplasmPair.getGid1() + ":" + germplasmPair.getGid2();
			String testEntry = germplasmNameIdMap.get(Integer.toString(germplasmPair.getGid1()));
			String standardEntry = germplasmNameIdMap.get(Integer.toString(germplasmPair.getGid2()));
			Map<String, String> traitDataMap = new HashMap<String, String>();
			ResultsData resData = new ResultsData(germplasmPair.getGid1(), testEntry, germplasmPair.getGid2(), standardEntry, traitDataMap);

			for (int i = 0; i < this.resultsTable.length; i++) {
				Table table = this.resultsTable[i];
				Item item = table.addItem(uniquieId);
				item.getItemProperty(ResultsComponent.TEST_COLUMN_ID).setValue(testEntry);
				item.getItemProperty(ResultsComponent.STANDARD_COLUMN_ID).setValue(standardEntry);
				TraitForComparison traitForCompare = traitsIteratorArray[i];
				// check for number of environments that are compatible - because if there are not any, we would
				// not show any data on the screen. This improves clarity.
				Integer envValue =
						HeadToHeadResultsUtil
								.getTotalNumOfEnv(germplasmPair, traitForCompare, observationMap, environmentForComparisonList);
				if (traitForCompare.isDisplay() && envValue > 0) {
					Map<String, Object> valuesMap = new HashMap<String, Object>();
					for (String columnKey : this.columnIdData) {
						String cellKey = traitForCompare.getTraitInfo().getName() + columnKey;
						String cellVal =
								this.getColumnValue(valuesMap, columnKey, germplasmPair, traitForCompare, observationMap,
										environmentForComparisonList);
						traitDataMap.put(cellKey, cellVal);
						item.getItemProperty(cellKey).setValue(cellVal);
					}
				}
			}

			resData.setTraitDataMap(traitDataMap);
			this.resultsDataList.add(resData);
		}

	}

	private String getColumnValue(Map<String, Object> valuesMap, String columnId, GermplasmPair germplasmPair,
			TraitForComparison traitForComparison, Map<String, ObservationList> observationMap,
			List<EnvironmentForComparison> environmentForComparisonList) {
		Object value = 0;
		if (ResultsComponent.NUM_OF_ENV_COLUMN_ID.equalsIgnoreCase(columnId)) {
			// get the total number of environment where the germplasm pair was observer and the observation value is not null and not empty
			// string
			value = HeadToHeadResultsUtil.getTotalNumOfEnv(germplasmPair, traitForComparison, observationMap, environmentForComparisonList);

		} else if (ResultsComponent.NUM_SUP_COLUMN_ID.equalsIgnoreCase(columnId)) {
			value = HeadToHeadResultsUtil.getTotalNumOfSup(germplasmPair, traitForComparison, observationMap, environmentForComparisonList);

		} else if (ResultsComponent.MEAN_TEST_COLUMN_ID.equalsIgnoreCase(columnId)) {
			value = HeadToHeadResultsUtil.getMeanValue(germplasmPair, 1, traitForComparison, observationMap, environmentForComparisonList);

		} else if (ResultsComponent.MEAN_STD_COLUMN_ID.equalsIgnoreCase(columnId)) {
			value = HeadToHeadResultsUtil.getMeanValue(germplasmPair, 2, traitForComparison, observationMap, environmentForComparisonList);

		} else if (ResultsComponent.PVAL_COLUMN_ID.equalsIgnoreCase(columnId)) {
			Integer numOfEnvts = (Integer) valuesMap.get(ResultsComponent.NUM_OF_ENV_COLUMN_ID);
			Integer numOfSucceses = (Integer) valuesMap.get(ResultsComponent.NUM_SUP_COLUMN_ID);
			value = HeadToHeadResultsUtil.getPvalue(numOfEnvts, numOfSucceses);

		} else if (ResultsComponent.MEAN_DIFF_COLUMN_ID.equalsIgnoreCase(columnId)) {
			value = HeadToHeadResultsUtil.getMeanDiff(germplasmPair, traitForComparison, observationMap, environmentForComparisonList);
		}

		valuesMap.put(columnId, value);
		if (value instanceof Double) {
			value = ResultsComponent.decimalFormmatter.format(value);
		}
		return value.toString();
	}

	public static boolean isValidDoubleValue(String val) {
		if (val != null && !val.equalsIgnoreCase("")) {
			try {
				Double.parseDouble(val);
				return true;
			} catch (NumberFormatException ee) {
				return false;
			}
		}
		return false;
	}

	public void populateResultsTable(List<EnvironmentForComparison> environmentForComparisonList, Map<String, String> germplasmNameIdMap,
			List<GermplasmPair> germplasmPair, Map<String, ObservationList> observationMap) {
		this.createEnvironmentsResultTable(environmentForComparisonList, germplasmNameIdMap, germplasmPair, observationMap);

	}

	public void setEntriesLabel(String testEntryLabel, String standardEntryLabel) {
		this.testEntryNameLabel.setValue(testEntryLabel);
		this.standardEntryNameLabel.setValue(standardEntryLabel);
	}

	@Override
	public void updateLabels() {

	}

	public void exportButtonClickAction() {

		EnvironmentForComparison envForComparison = this.finalEnvironmentForComparisonList.get(0);
		Set<TraitForComparison> traitsIterator = envForComparison.getTraitAndObservationMap().keySet();

		// in current export format, if # of traits > 42, will exceed Excel's 255 columns limitation
		if (traitsIterator.size() > 42) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.H2H_NUM_OF_TRAITS_EXCEEDED));

		} else {
			String tempFileName = System.getProperty(ResultsComponent.USER_HOME) + "/HeadToHeadDataList.xls";
			HeadToHeadDataListExport listExporter = new HeadToHeadDataListExport();

			final String userAgent = Util.getApplicationRequest().getHeader("User-Agent");

			try {

				listExporter.exportHeadToHeadDataListExcel(tempFileName, this.resultsDataList, traitsIterator, this.columnIdData,
						this.columnIdDataMsgMap);
				FileDownloadResource fileDownloadResource =
						new FileDownloadResource(new File(tempFileName), this.getApplication(), userAgent);
				fileDownloadResource.setFilename("HeadToHeadDataList.xls");

				this.getWindow().open(fileDownloadResource);
				this.mainScreen.selectFirstTab();
				// TODO must figure out other way to clean-up file because deleting it here makes it unavailable for download
			} catch (HeadToHeadDataListExportException e) {
				MessageNotifier.showError(
						this.getApplication().getWindow(GermplasmStudyBrowserApplication.HEAD_TO_HEAD_COMPARISON_WINDOW_NAME),
						"Error with exporting list.", e.getMessage());
			}

		}

	}

	public void backButtonClickAction() {
		this.mainScreen.selectThirdTab();
	}
}
