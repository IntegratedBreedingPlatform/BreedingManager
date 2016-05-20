
package org.generationcp.breeding.manager.study.containers;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.data.Item;

public class RepresentationDatasetQueryTest {

	@Test
	public void testIsCategoricalAcceptedValueIfVariableIsNotCategorical() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.DATE_VARIABLE.getId());
		standardVariable.setDataType(term);
		Assert.assertFalse("Should return false since its a non categorical variable",
				query.isCategoricalAcceptedValue("1", standardVariable));
	}

	@Test
	public void testIsCategoricalAcceptedValueIfVariableIsCategoricalAndDisplayValueIsNull() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.CATEGORICAL_VARIABLE.getId());
		standardVariable.setDataType(term);
		Assert.assertFalse("Should return false since its a value is null", query.isCategoricalAcceptedValue(null, standardVariable));
	}

	@Test
	public void testIsCategoricalAcceptedValueIfVariableIsCategoricalAndDisplayValueIsNotNullAndMatchingResults() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.CATEGORICAL_VARIABLE.getId());
		standardVariable.setDataType(term);
		List<Enumeration> enumerations = new ArrayList<Enumeration>();
		enumerations.add(new Enumeration(1, "name", "desc", 1));
		standardVariable.setEnumerations(enumerations);
		Assert.assertFalse("Should return false since its a value is null", query.isCategoricalAcceptedValue("Desc", standardVariable));
	}

	@Test
	public void testIsCategoricalAcceptedValueIfVariableIsCategoricalAndDisplayValueIsNotNullAndNoMatchingResults() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.CATEGORICAL_VARIABLE.getId());
		standardVariable.setDataType(term);
		List<Enumeration> enumerations = new ArrayList<Enumeration>();
		enumerations.add(new Enumeration(1, "name", "desc", 1));
		standardVariable.setEnumerations(enumerations);
		Assert.assertTrue("Should return true since its a value is not matching any valid values",
				query.isCategoricalAcceptedValue("Desc1", standardVariable));
	}

	@Test
	public void testIsCategoricalAcceptedValueIfVariableIsCategoricalAndDisplayValueIsNotNullAndNoEnumerations() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.CATEGORICAL_VARIABLE.getId());
		standardVariable.setDataType(term);
		standardVariable.setEnumerations(null);
		Assert.assertTrue("Should return true since its a value is not matching any valid values",
				query.isCategoricalAcceptedValue("Desc1", standardVariable));
	}

	@Test
	public void testSetAcceptedItemPropertyIfCategoricalAcceptedValueIfVariableIsNotCategorical() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.DATE_VARIABLE.getId());
		standardVariable.setDataType(term);
		Item item = Mockito.mock(Item.class);
		boolean isAccepted = query.setAcceptedItemProperty("1", standardVariable, item, "1");
		Assert.assertFalse("Should return false since its a non categorical variable", isAccepted);
	}

	@Test
	public void testSetAcceptedItemPropertyIfCategoricalAcceptedValueIfVariableIsCategoricalAndDisplayValueIsNull() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.CATEGORICAL_VARIABLE.getId());
		standardVariable.setDataType(term);
		Item item = Mockito.mock(Item.class);
		boolean isAccepted = query.setAcceptedItemProperty(null, standardVariable, item, "1");
		Assert.assertFalse("Should return false since its a value is null", isAccepted);
	}

	@Test
	public void testSetAcceptedItemPropertyIfCategoricalAcceptedValueIfVariableIsCategoricalAndDisplayValueIsNotNullAndMatchingResults() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.CATEGORICAL_VARIABLE.getId());
		standardVariable.setDataType(term);
		List<Enumeration> enumerations = new ArrayList<Enumeration>();
		enumerations.add(new Enumeration(1, "name", "desc", 1));
		standardVariable.setEnumerations(enumerations);
		Item item = Mockito.mock(Item.class);
		boolean isAccepted = query.setAcceptedItemProperty("Desc", standardVariable, item, "1");
		Assert.assertFalse("Should return false since its a value is null", isAccepted);
	}

	@Test
	public void testSetAcceptedItemPropertyIfCategoricalAcceptedValueIfVariableIsCategoricalAndDisplayValueIsNotNullAndNoMatchingResults() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.CATEGORICAL_VARIABLE.getId());
		standardVariable.setDataType(term);
		List<Enumeration> enumerations = new ArrayList<Enumeration>();
		enumerations.add(new Enumeration(1, "name", "desc", 1));
		standardVariable.setEnumerations(enumerations);
		Item item = Mockito.mock(Item.class);
		boolean isAccepted = query.setAcceptedItemProperty("Desc1", standardVariable, item, "1");
		Assert.assertTrue("Should return true since its a value is not matching any valid values", isAccepted);
	}

	@Test
	public void testSetAcceptedItemPropertyIfCategoricalAcceptedValueIfVariableIsCategoricalAndDisplayValueIsNotNullAndNoEnumerations() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.CATEGORICAL_VARIABLE.getId());
		standardVariable.setDataType(term);
		standardVariable.setEnumerations(null);
		Item item = Mockito.mock(Item.class);
		boolean isAccepted = query.setAcceptedItemProperty("Desc1", standardVariable, item, "1");
		Assert.assertTrue("Should return true since its a value is not matching any valid values", isAccepted);
	}

	@Test
	public void testIsNumericalAcceptedValueIfVariableIsNumericAndWithinMinMax() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.NUMERIC_VARIABLE.getId());
		standardVariable.setDataType(term);
		VariableConstraints constraints = new VariableConstraints(new Double(1), new Double(10));
		standardVariable.setConstraints(constraints);
		boolean isAccepted = query.isNumericalAcceptedValue("2", standardVariable);
		Assert.assertFalse("Should return false since its within the limit", isAccepted);
	}

	@Test
	public void testIsNumericalAcceptedValueIfVariableIsNonNumericAndNotWithinMinMax() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.CATEGORICAL_VARIABLE.getId());
		standardVariable.setDataType(term);
		VariableConstraints constraints = new VariableConstraints(new Double(1), new Double(10));
		standardVariable.setConstraints(constraints);
		boolean isAccepted = query.isNumericalAcceptedValue("20", standardVariable);
		Assert.assertFalse("Should return false since variable is non numberic", isAccepted);
	}

	@Test
	public void testIsNumericalAcceptedValueIfVariableIsNumericAndNotWithinMinMax() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.NUMERIC_VARIABLE.getId());
		standardVariable.setDataType(term);
		VariableConstraints constraints = new VariableConstraints(new Double(1), new Double(10));
		standardVariable.setConstraints(constraints);
		boolean isAccepted = query.isNumericalAcceptedValue("20", standardVariable);
		Assert.assertTrue("Should return true since its within the limit", isAccepted);
	}

	@Test
	public void testIsNumericalAcceptedValueIfVariableIsNumericAndNoWithinMinMax() {
		RepresentationDataSetQuery query =
				new RepresentationDataSetQuery(Mockito.mock(StudyDataManager.class), new Integer(1), new ArrayList<String>(), false);
		StandardVariable standardVariable = new StandardVariable();
		Term term = new Term();
		term.setId(TermId.NUMERIC_VARIABLE.getId());
		standardVariable.setDataType(term);
		boolean isAccepted = query.isNumericalAcceptedValue("20", standardVariable);
		Assert.assertFalse("Should return false since it has no limit", isAccepted);
	}
}
