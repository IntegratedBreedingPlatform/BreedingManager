
package org.generationcp.breeding.manager.cross.study.h2h;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.h2h.pojos.Result;
import org.generationcp.breeding.manager.cross.study.h2h.pojos.TraitForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmDataManagerImpl;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@Configurable
public class ResultsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 2305982279660448571L;

	private final static Logger LOG = LoggerFactory.getLogger(ResultsComponent.class);

	private static final String TRAIT_COLUMN_ID = "ResultsComponent Trait Column ID";
	private static final String NUM_OF_ENV_COLUMN_ID = "ResultsComponent Num Of Env Column ID";
	private static final String NUM_SUP_COLUMN_ID = "ResultsComponent Num Sup Column ID";
	private static final String MEAN_TEST_COLUMN_ID = "ResultsComponent Mean Test Column ID";
	private static final String MEAN_STD_COLUMN_ID = "ResultsComponent Mean STD Column ID";
	private static final String MEAN_DIFF_COLUMN_ID = "ResultsComponent Mean Diff Column ID";
	private static final String PVAL_COLUMN_ID = "ResultsComponent Pval Column ID";

	private Table resultsTable;

	private Label testEntryLabel;
	private Label standardEntryLabel;
	private Label testEntryNameLabel;
	private Label standardEntryNameLabel;

	private final Integer currentTestEntryGID;
	private final Integer currentStandardEntryGID;

	@Autowired
	private GermplasmDataManager germplasmDataManager;
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public ResultsComponent() {
		this.currentStandardEntryGID = null;
		this.currentTestEntryGID = null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setHeight("550px");
		this.setWidth("1000px");

		this.testEntryLabel = new Label("<b>Test Entry:</b>");
		this.testEntryLabel.setContentMode(Label.CONTENT_XHTML);
		this.addComponent(this.testEntryLabel, "top:20px;left:30px");

		this.testEntryNameLabel = new Label();
		this.addComponent(this.testEntryNameLabel, "top:20px;left:100px");

		this.standardEntryLabel = new Label("<b>Standard Entry:</b>");
		this.standardEntryLabel.setContentMode(Label.CONTENT_XHTML);
		this.addComponent(this.standardEntryLabel, "top:20px;left:450px");

		this.standardEntryNameLabel = new Label();
		this.addComponent(this.standardEntryNameLabel, "top:20px;left:550px");

		this.resultsTable = new Table();
		this.resultsTable.setWidth("800px");
		this.resultsTable.setHeight("400px");
		this.resultsTable.setImmediate(true);
		this.resultsTable.setColumnCollapsingAllowed(true);
		this.resultsTable.setColumnReorderingAllowed(true);

		this.resultsTable.addContainerProperty(ResultsComponent.TRAIT_COLUMN_ID, String.class, null);
		this.resultsTable.addContainerProperty(ResultsComponent.NUM_OF_ENV_COLUMN_ID, Integer.class, null);
		this.resultsTable.addContainerProperty(ResultsComponent.NUM_SUP_COLUMN_ID, Integer.class, null);
		this.resultsTable.addContainerProperty(ResultsComponent.MEAN_TEST_COLUMN_ID, Double.class, null);
		this.resultsTable.addContainerProperty(ResultsComponent.MEAN_STD_COLUMN_ID, Double.class, null);
		this.resultsTable.addContainerProperty(ResultsComponent.MEAN_DIFF_COLUMN_ID, Double.class, null);
		this.resultsTable.addContainerProperty(ResultsComponent.PVAL_COLUMN_ID, Double.class, null);

		this.resultsTable.setColumnHeader(ResultsComponent.TRAIT_COLUMN_ID, "TRAIT");
		this.resultsTable.setColumnHeader(ResultsComponent.NUM_OF_ENV_COLUMN_ID, "# OF ENV");
		this.resultsTable.setColumnHeader(ResultsComponent.NUM_SUP_COLUMN_ID, "# SUP");
		this.resultsTable.setColumnHeader(ResultsComponent.MEAN_TEST_COLUMN_ID, "MEAN TEST");
		this.resultsTable.setColumnHeader(ResultsComponent.MEAN_STD_COLUMN_ID, "MEAN STD");
		this.resultsTable.setColumnHeader(ResultsComponent.MEAN_DIFF_COLUMN_ID, "MEAN DIFF");
		this.resultsTable.setColumnHeader(ResultsComponent.PVAL_COLUMN_ID, "PVAL");

		this.addComponent(this.resultsTable, "top:70px;left:30px");
	}

	public void populateResultsTable(Integer testEntryGID, Integer standardEntryGID, List<TraitForComparison> traitsForComparisonList) {
		if (this.areCurrentGIDsDifferentFromGiven(testEntryGID, standardEntryGID)) {
			this.resultsTable.removeAllItems();

			List<Result> results = this.getResults(testEntryGID, standardEntryGID, traitsForComparisonList);
			for (Result result : results) {
				this.resultsTable.addItem(new Object[] {result.getTraitName(), result.getNumberOfEnvironments(), result.getNumberOfSup(),
						result.getMeanTest(), result.getMeanStd(), result.getMeanDiff(), result.getPval()}, result.getTraitName());
			}

			this.resultsTable.setColumnCollapsed(ResultsComponent.NUM_SUP_COLUMN_ID, true);
			this.resultsTable.setColumnCollapsed(ResultsComponent.PVAL_COLUMN_ID, true);
			this.resultsTable.requestRepaint();
		}
	}

	@SuppressWarnings("rawtypes")
	private List<Result> getResults(Integer testEntryGID, Integer standardEntryGID, List<TraitForComparison> traitsForComparisonList) {
		List<Result> toreturn = new ArrayList<Result>();

		try {
			Germplasm testEntry = this.germplasmDataManager.getGermplasmWithPrefName(testEntryGID);
			Germplasm standardEntry = this.germplasmDataManager.getGermplasmWithPrefName(standardEntryGID);

			String testEntryPrefName = null;
			if (testEntry.getPreferredName() != null) {
				testEntryPrefName = testEntry.getPreferredName().getNval().trim();
			} else {
				MessageNotifier
						.showWarning(this.getWindow(), "Warning!",
								"The germplasm you selected as test entry doesn't have a preferred name, "
										+ "please select a different germplasm.");
				return new ArrayList<Result>();
			}

			String standardEntryPrefName = null;
			if (standardEntry.getPreferredName() != null) {
				standardEntryPrefName = standardEntry.getPreferredName().getNval().trim();
			} else {
				MessageNotifier.showWarning(this.getWindow(), "Warning!",
						"The standard entry germplasm you selected as standard entry doesn't have a preferred name, "
								+ "please select a different germplasm.");
				return new ArrayList<Result>();
			}

			GermplasmDataManagerImpl dataManagerImpl = (GermplasmDataManagerImpl) this.germplasmDataManager;
			String queryString =
					"select trait_name, entry_designation, count(observed_value), AVG(observed_value)" + " from h2h_details"
							+ " where entry_designation in ('" + testEntryPrefName + "','" + standardEntryPrefName + "')"
							+ " group by trait_name, entry_designation";
			Query query = dataManagerImpl.getCurrentSession().createSQLQuery(queryString);
			List results = query.list();
			Iterator resultsIterator = results.iterator();
			while (resultsIterator.hasNext()) {
				Object resultArray1[] = (Object[]) resultsIterator.next();
				Object resultArray2[] = null;

				if (resultsIterator.hasNext()) {
					resultArray2 = (Object[]) resultsIterator.next();
				} else {
					break;
				}

				String traitName = (String) resultArray1[0];
				if (traitName != null) {
					traitName = traitName.trim().toUpperCase();
				}
				Integer numberOfEnvironments = this.getNumberOfEnvironmentsForTrait(traitName, traitsForComparisonList);
				String entry1 = (String) resultArray1[1];
				Double averageOfEntry1 = (Double) resultArray1[3];
				averageOfEntry1 = Math.round(averageOfEntry1 * 100.0) / 100.0;
				Double averageOfEntry2 = (Double) resultArray2[3];
				averageOfEntry2 = Math.round(averageOfEntry2 * 100.0) / 100.0;

				Double meanDiff = null;
				if (numberOfEnvironments != 0) {
					if (testEntryPrefName.equals(entry1)) {
						meanDiff = averageOfEntry1 - averageOfEntry2;
						meanDiff = Math.round(meanDiff * 100.0) / 100.0;
						toreturn.add(new Result(traitName, numberOfEnvironments, null, averageOfEntry2, averageOfEntry1, meanDiff, null));
					} else {
						meanDiff = averageOfEntry2 - averageOfEntry1;
						meanDiff = Math.round(meanDiff * 100.0) / 100.0;
						toreturn.add(new Result(traitName, numberOfEnvironments, null, averageOfEntry1, averageOfEntry2, meanDiff, null));
					}
				}
			}
		} catch (MiddlewareQueryException ex) {
			ex.printStackTrace();
			ResultsComponent.LOG.error("Database error!", ex);
			MessageNotifier.showError(this.getWindow(), "Database Error!", this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			return new ArrayList<Result>();
		} catch (Exception ex) {
			ex.printStackTrace();
			ResultsComponent.LOG.error("Database error!", ex);
			MessageNotifier.showError(this.getWindow(), "Database Error!", this.messageSource.getMessage(Message.ERROR_REPORT_TO));
			return new ArrayList<Result>();
		}

		return toreturn;
	}

	private Integer getNumberOfEnvironmentsForTrait(String traitName, List<TraitForComparison> traitsForComparisonList) {
		Integer toreturn = 0;

		for (TraitForComparison trait : traitsForComparisonList) {
			if (trait.getName().equals(traitName)) {
				return trait.getNumberOfEnvironments();
			}
		}

		return toreturn;
	}

	private boolean areCurrentGIDsDifferentFromGiven(Integer currentTestEntryGID, Integer currentStandardEntryGID) {
		if (this.currentTestEntryGID != null && this.currentStandardEntryGID != null) {
			if (this.currentTestEntryGID == currentTestEntryGID && this.currentStandardEntryGID == currentStandardEntryGID) {
				return false;
			}
		}

		return true;
	}

	public void setEntriesLabel(String testEntryLabel, String standardEntryLabel) {
		this.testEntryNameLabel.setValue(testEntryLabel);
		this.standardEntryNameLabel.setValue(standardEntryLabel);
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
	}
}
