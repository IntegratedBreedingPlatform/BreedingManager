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

package org.generationcp.breeding.manager.study;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.study.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @author Mark Agarrado
 *
 */
@Configurable
public class TableViewerDatasetTable extends Table implements InitializingBean {

	public static final int BATCH_SIZE = 50;

	private static final long serialVersionUID = 9114757066977945573L;
	private static final Logger LOG = LoggerFactory.getLogger(TableViewerDatasetTable.class);
	private static final String NUMERIC_VARIABLE = "Numeric variable";

	private final StudyDataManager studyDataManager;
	private final Integer studyId;
	private final Integer datasetId;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	public TableViewerDatasetTable(StudyDataManager studyDataManager, Integer studyId, Integer datasetId) {
		this.studyDataManager = studyDataManager;
		this.studyId = studyId;
		this.datasetId = datasetId;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.generateDatasetTable();
	}

	private void generateDatasetTable() {
		// set the column header ids
		List<DMSVariableType> variables;
		List<String> columnIds = new ArrayList<>();

		try {
			DataSet dataset = this.studyDataManager.getDataSet(this.datasetId);
			variables = dataset.getVariableTypes().getVariableTypes();
		} catch (MiddlewareException e) {
			TableViewerDatasetTable.LOG.error(
					"Error in getting variables of dataset: " + this.datasetId + "\n" + e.toString() + "\n" + e.getStackTrace(), e);
			variables = new ArrayList<>();
			if (this.getWindow() != null) {
				MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.ERROR_DATABASE),
						this.messageSource.getMessage(Message.ERROR_IN_GETTING_VARIABLES_OF_DATASET) + " " + this.datasetId);
			}
		}

		for (DMSVariableType variable : variables) {
			if (variable.getStandardVariable().getPhenotypicType() != PhenotypicType.STUDY) {
				String columnId = new StringBuffer().append(variable.getId()).append("-").append(variable.getLocalName()).toString();
				columnIds.add(columnId);

				// add the column ids to display for the Table
				if (TableViewerDatasetTable.NUMERIC_VARIABLE.equals(variable.getStandardVariable().getDataType().getName())) {
					this.addContainerProperty(columnId, BigDecimal.class, null);

					// define column as Button for GID, else define as String
				} else if (columnId.contains("GID")) {
					this.addContainerProperty(columnId, Button.class, null);

				} else {
					this.addContainerProperty(columnId, String.class, null);
				}
			}
		}

		// set column headers for the Table
		for (DMSVariableType variable : variables) {
			String columnId = new StringBuffer().append(variable.getId()).append("-").append(variable.getLocalName()).toString();
			String columnHeader = variable.getLocalName();
			this.setColumnHeader(columnId, columnHeader);
		}

		this.populateDatasetTable();
	}

	private void populateDatasetTable() {
		List<Experiment> experiments = this.getExperimentsByBatch();

		if (!experiments.isEmpty()) {

			for (Experiment experiment : experiments) {
				List<Variable> variables = new ArrayList<>();

				VariableList factors = experiment.getFactors();
				if (factors != null) {
					variables.addAll(factors.getVariables());
				}

				VariableList variates = experiment.getVariates();
				if (variates != null) {
					variables.addAll(variates.getVariables());
				}

				Item item = this.addItem(experiment.getId());
				if (item != null) {
					for (Variable variable : variables) {
						String columnId =
								new StringBuffer().append(variable.getVariableType().getId()).append("-")
										.append(variable.getVariableType().getLocalName()).toString();

						if (TableViewerDatasetTable.NUMERIC_VARIABLE.equals(variable.getVariableType().getStandardVariable().getDataType()
								.getName())) {
							String cellValue = variable.getDisplayValue();

							// value is in date format but defined as Numeric Variable eg. 10/21/2004
							if (cellValue.contains("/")) {
								item.getItemProperty(columnId).setValue(this.formatDateToNumber(cellValue));
							} else if (NumberUtils.isNumber(cellValue)) {
								BigDecimal decimalValue = new BigDecimal(cellValue);
								item.getItemProperty(columnId).setValue(decimalValue);
							}

						} else {
							String stringValue = variable.getDisplayValue();
							if (stringValue != null) {
								stringValue = stringValue.trim();
								// display value as Link if GID, else display as string
								if ("GID".equals(variable.getVariableType().getLocalName().trim())) {
									Button gidButton = new Button(stringValue, new GidLinkButtonClickListener(stringValue));
									gidButton.setStyleName(BaseTheme.BUTTON_LINK);
									gidButton.setDescription("Click to view Germplasm information");
									item.getItemProperty(columnId).setValue(gidButton);
								} else {
									item.getItemProperty(columnId).setValue(stringValue);
								}
							}
						}
					}
				}
			}
		}
	}

	protected List<Experiment> getExperimentsByBatch() {
		List<Experiment> experiments = new ArrayList<Experiment>();
		int size = -1;
		try {
			Long count = this.studyDataManager.countExperiments(this.datasetId);
			size = count.intValue();
		} catch (MiddlewareQueryException ex) {
			TableViewerDatasetTable.LOG.error("Error with getting experiments for dataset: " + this.datasetId + "\n" + ex.toString(), ex);
		}

		try {
			if (size < 100) {
				experiments = this.studyDataManager.getExperiments(this.datasetId, 0, size);
			} else {
				int batchRecordCount = size / TableViewerDatasetTable.BATCH_SIZE;
				int remainingRecCount = size % TableViewerDatasetTable.BATCH_SIZE;
				int start = 0;

				for (int i = 1; i <= batchRecordCount; i++) {
					List<Experiment> experimentList = new ArrayList<Experiment>();
					experimentList = this.studyDataManager.getExperiments(this.datasetId, start, TableViewerDatasetTable.BATCH_SIZE);
					start += TableViewerDatasetTable.BATCH_SIZE;
					experiments.addAll(experimentList);
				}

				if (remainingRecCount > 0) {
					List<Experiment> experimentList = new ArrayList<Experiment>();
					experimentList = this.studyDataManager.getExperiments(this.datasetId, start, remainingRecCount);
					experiments.addAll(experimentList);
				}

			}
		} catch (MiddlewareException ex) {
			// Log error in log file
			TableViewerDatasetTable.LOG.error("Error with getting ounitids for representation: " + this.datasetId + "\n" + ex.toString(),
					ex);
			experiments = new ArrayList<Experiment>();
		}
		return experiments;
	}

	private BigDecimal formatDateToNumber(String cellValue) {
		try {
			String[] cellNewValue = cellValue.split("/");
			// format to YYYYMMDD
			String value = cellNewValue[2] + cellNewValue[0] + cellNewValue[1];
			return new BigDecimal(value);
		} catch (Exception e) {
			TableViewerDatasetTable.LOG.debug("Format exception " + cellValue, e);
			return new BigDecimal(0);
		}
	}

	public StudyDataManager getStudyDataManager() {
		return this.studyDataManager;
	}

	public Integer getStudyId() {
		return this.studyId;
	}

	public Integer getDatasetId() {
		return this.datasetId;
	}

}
