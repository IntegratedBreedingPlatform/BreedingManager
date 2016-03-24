package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.GermplasmGroup;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;


public class GermplasmGroupingResultsComponentTest {

	@Test
	public void testMemberDisplayAboveLimit() {

		Map<Integer, GermplasmGroup> groupingResults = new HashMap<Integer, GermplasmGroup>();

		GermplasmGroup group = new GermplasmGroup();

		Method method = new Method(1);
		method.setMtype("GEN");

		for (int i = 1; i <= GermplasmGroupingResultsComponent.MAX_MEMBERS_TO_DISPLAY + 5; i++) {
			Germplasm germplasm = new Germplasm(i);
			germplasm.setMethod(method);

			Name name = new Name();
			name.setNstat(1);
			name.setNval("Preferred Name " + i);

			germplasm.getNames().add(name);
			group.getGroupMembers().add(germplasm);
		}

		group.setFounder(group.getGroupMembers().get(0));

		groupingResults.put(1, group);

		GermplasmGroupingResultsComponent component = new GermplasmGroupingResultsComponent(groupingResults);
		SimpleResourceBundleMessageSource mockMessageService = Mockito.mock(SimpleResourceBundleMessageSource.class);
		component.setMessageSource(mockMessageService);
		component.instantiateComponents();
		component.initializeValues();

		Table groupingResultsTable = component.getGroupingResultsTable();
		Assert.assertEquals(1, groupingResultsTable.size());

		Item firstRow = groupingResultsTable.getItem(new Integer(1));
		Property groupMembersCell = firstRow.getItemProperty("Group Members");
		String memberDisplayString = (String) groupMembersCell.getValue();
		Assert.assertTrue("Expecting truncation with elipses when there are more members to display than max allowed.",
				memberDisplayString.endsWith("],...."));

		// Founder is GENerative. Message for generative germplasm should be retrieved for adding in NOTES column.
		Mockito.verify(mockMessageService, Mockito.times(1)).getMessage(Message.GENERATIVE_GERMPLASM_NOT_GROUPED);
	}

	@Test
	public void testMemberDisplayUnderLimit() {

		Map<Integer, GermplasmGroup> groupingResults = new HashMap<Integer, GermplasmGroup>();

		GermplasmGroup group = new GermplasmGroup();

		Method method = new Method(1);
		method.setMtype("DER");

		for (int i = 1; i <= GermplasmGroupingResultsComponent.MAX_MEMBERS_TO_DISPLAY - 5; i++) {
			Germplasm germplasm = new Germplasm(i);
			germplasm.setMethod(method);
			Name name = new Name();
			name.setNstat(1);
			name.setNval("Preferred Name " + i);

			germplasm.getNames().add(name);
			group.getGroupMembers().add(germplasm);
		}

		group.setFounder(group.getGroupMembers().get(0));

		groupingResults.put(1, group);

		GermplasmGroupingResultsComponent component = new GermplasmGroupingResultsComponent(groupingResults);
		SimpleResourceBundleMessageSource mockMessageService = Mockito.mock(SimpleResourceBundleMessageSource.class);
		component.setMessageSource(mockMessageService);
		component.instantiateComponents();
		component.initializeValues();

		Table groupingResultsTable = component.getGroupingResultsTable();
		Assert.assertEquals(1, groupingResultsTable.size());

		Item firstRow = groupingResultsTable.getItem(new Integer(1));
		Property groupMembersCell = firstRow.getItemProperty("Group Members");

		String memberDisplayString = (String) groupMembersCell.getValue();
		Assert.assertTrue("Expecting no truncation with full stop when members to display are within the max allowed.",
				memberDisplayString.endsWith("]."));

		// Founder is not GENrative. Message for generative germplasm should not be retrieved.
		Mockito.verify(mockMessageService, Mockito.never()).getMessage(Message.GENERATIVE_GERMPLASM_NOT_GROUPED);
	}

}
