
package org.generationcp.breeding.manager.study;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.ui.Table;

import junit.framework.Assert;

public class RepresentationDatasetComponentTest {

	private static final int DATASET_ID = 2;
	private static StudyDataManager studyDataManager = Mockito.mock(StudyDataManager.class);
	private static RepresentationDatasetComponent datasetComponent;

	@BeforeClass
	public static void setup() throws MiddlewareQueryException {
		RepresentationDatasetComponentTest.datasetComponent =
				new RepresentationDatasetComponent(RepresentationDatasetComponentTest.studyDataManager,
						RepresentationDatasetComponentTest.DATASET_ID, "", 1, false, false);
	}

	private static DataSet createMockDataset() {
		DataSet dataSet = new DataSet();
		VariableTypeList variables = new VariableTypeList();
		for (DMSVariableType variable : RepresentationDatasetComponentTest.createTestFactors()) {
			variables.add(variable);
		}
		dataSet.setVariableTypes(variables);
		return dataSet;
	}

	private static List<DMSVariableType> createTestFactors() {
		List<DMSVariableType> factors = new ArrayList<>();

		StandardVariable entryNoVariable = new StandardVariable();
		entryNoVariable.setId(TermId.ENTRY_NO.getId());
		entryNoVariable.setPhenotypicType(PhenotypicType.GERMPLASM);
		entryNoVariable.setProperty(new Term(1, "GERMPLASM ENTRY", "GERMPLASM ENTRY"));
		DMSVariableType varType = new DMSVariableType("ENTRY_NO", "ENTRY_NO", entryNoVariable, 1);
		varType.setLocalName("ENTRY_NO");
		factors.add(varType);

		StandardVariable gidVariable = new StandardVariable();
		gidVariable.setId(TermId.GID.getId());
		gidVariable.setPhenotypicType(PhenotypicType.GERMPLASM);
		gidVariable.setProperty(new Term(1, "GERMPLASM ID", "GERMPLASM ID"));
		varType = new DMSVariableType("GID", "GID", gidVariable, 2);
		varType.setLocalName("GID");
		factors.add(varType);

		StandardVariable desigVariable = new StandardVariable();
		desigVariable.setId(TermId.DESIG.getId());
		desigVariable.setPhenotypicType(PhenotypicType.GERMPLASM);
		desigVariable.setProperty(new Term(1, "GERMPLASM ID", "GERMPLASM ID"));
		varType = new DMSVariableType("DESIGNATION", "DESIGNATION", desigVariable, 3);
		varType.setLocalName("DESIG");
		factors.add(varType);

		StandardVariable entryTypeVariable = new StandardVariable();
		entryTypeVariable.setId(TermId.ENTRY_TYPE.getId());
		entryTypeVariable.setPhenotypicType(PhenotypicType.GERMPLASM);
		entryTypeVariable.setProperty(new Term(1, "ENTRY TYPE", "ENTRY_TYPE"));
		varType = new DMSVariableType("ENTRY_TYPE", "ENTRY_TYPE", entryTypeVariable, 4);
		varType.setLocalName("ENTRY_TYPE");
		factors.add(varType);

		StandardVariable repVariable = new StandardVariable();
		repVariable.setId(TermId.REP_NO.getId());
		repVariable.setPhenotypicType(PhenotypicType.TRIAL_DESIGN);
		repVariable.setProperty(new Term(1, "REP_NO", "REP_NO"));
		varType = new DMSVariableType("REP_NO", "REP_NO", repVariable, 5);
		varType.setLocalName("REP_NO");
		factors.add(varType);

		return factors;
	}

	@Test
	public void testValidateNoDuplicateColumns() throws MiddlewareException {
		DataSet mockDataset = RepresentationDatasetComponentTest.createMockDataset();

		// duplicate GID variable
		StandardVariable gidVariable = new StandardVariable();
		gidVariable = new StandardVariable();
		gidVariable.setId(TermId.GID.getId());
		gidVariable.setPhenotypicType(PhenotypicType.GERMPLASM);
		gidVariable.setProperty(new Term(1, "GERMPLASM ID", "GERMPLASM ID"));
		DMSVariableType varType = new DMSVariableType("GID", "GID", gidVariable, 6);
		varType.setLocalName("GID");
		mockDataset.getVariableTypes().add(varType);

		Mockito.doReturn(mockDataset).when(RepresentationDatasetComponentTest.studyDataManager)
				.getDataSet(RepresentationDatasetComponentTest.DATASET_ID);

		Table table = RepresentationDatasetComponentTest.datasetComponent.generateLazyDatasetTable(false);
		Assert.assertTrue("Table should only have 5 columns, excluding duplicate variables", table.getColumnHeaders().length == 5);
	}

	@Test
	public void testValidateDatasetVariablesAreExcludedFromTable() throws MiddlewareException {
		DataSet mockDataset = RepresentationDatasetComponentTest.createMockDataset();

		// add DatasetVariables
		StandardVariable datasetVariable = new StandardVariable();
		datasetVariable = new StandardVariable();
		datasetVariable.setId(TermId.DATASET_NAME.getId());
		datasetVariable.setPhenotypicType(PhenotypicType.DATASET);
		DMSVariableType varType = new DMSVariableType("DATASET_NAME", "DATASET_NAME", datasetVariable, 6);
		varType.setLocalName("DATASET_NAME");
		mockDataset.getVariableTypes().add(varType);

		datasetVariable = new StandardVariable();
		datasetVariable.setId(TermId.DATASET_TITLE.getId());
		datasetVariable.setPhenotypicType(PhenotypicType.DATASET);
		varType = new DMSVariableType("DATASET_TITLE", "DATASET_TITLE", datasetVariable, 7);
		varType.setLocalName("DATASET_TITLE");
		mockDataset.getVariableTypes().add(varType);

		datasetVariable = new StandardVariable();
		datasetVariable.setId(TermId.DATASET_NAME.getId());
		datasetVariable.setPhenotypicType(PhenotypicType.DATASET);
		varType = new DMSVariableType("DATASET_TYPE", "DATASET_TYPE", datasetVariable, 6);
		varType.setLocalName("DATASET_TYPE");
		mockDataset.getVariableTypes().add(varType);

		Mockito.doReturn(mockDataset).when(RepresentationDatasetComponentTest.studyDataManager)
				.getDataSet(RepresentationDatasetComponentTest.DATASET_ID);

		Table table = RepresentationDatasetComponentTest.datasetComponent.generateLazyDatasetTable(false);
		Assert.assertTrue("Table should only have 5 columns, excluding dataset variables", table.getColumnHeaders().length == 5);
	}

}
