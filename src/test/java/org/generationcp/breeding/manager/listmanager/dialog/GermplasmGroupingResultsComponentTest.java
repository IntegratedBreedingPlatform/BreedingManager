package org.generationcp.breeding.manager.listmanager.dialog;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.impl.GermplasmGroup;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;


public class GermplasmGroupingResultsComponentTest {

	@Test
	public void testMemberDisplayAboveLimit() {

		Map<Integer, GermplasmGroup> groupingResults = new HashMap<Integer, GermplasmGroup>();

		GermplasmGroup group = new GermplasmGroup();

		for (int i = 1; i <= GermplasmGroupingResultsComponent.MAX_MEMBERS_TO_DISPLAY + 5; i++) {
			Germplasm germplasm = new Germplasm(i);

			Name name = new Name();
			name.setNstat(1);
			name.setNval("Preferred Name " + i);

			germplasm.getNames().add(name);
			group.getGroupMembers().add(germplasm);
		}

		groupingResults.put(1, group);

		GermplasmGroupingResultsComponent component = new GermplasmGroupingResultsComponent(groupingResults);
		component.instantiateComponents();
		component.initializeValues();

		Table groupingResultsTable = component.getGroupingResultsTable();
		Assert.assertEquals(1, groupingResultsTable.size());

		Item firstRow = groupingResultsTable.getItem(new Integer(1));
		Property groupMembersCell = firstRow.getItemProperty("Group Members");
		String memberDisplayString = (String) groupMembersCell.getValue();
		Assert.assertTrue("Expecting truncation with elipses when there are more members to display than max allowed.",
				memberDisplayString.endsWith("],...."));

	}

	@Test
	public void testMemberDisplayUnderLimit() {

		Map<Integer, GermplasmGroup> groupingResults = new HashMap<Integer, GermplasmGroup>();

		GermplasmGroup group = new GermplasmGroup();

		for (int i = 1; i <= GermplasmGroupingResultsComponent.MAX_MEMBERS_TO_DISPLAY - 5; i++) {
			Germplasm germplasm = new Germplasm(i);

			Name name = new Name();
			name.setNstat(1);
			name.setNval("Preferred Name " + i);

			germplasm.getNames().add(name);
			group.getGroupMembers().add(germplasm);
		}

		groupingResults.put(1, group);

		GermplasmGroupingResultsComponent component = new GermplasmGroupingResultsComponent(groupingResults);
		component.instantiateComponents();
		component.initializeValues();

		Table groupingResultsTable = component.getGroupingResultsTable();
		Assert.assertEquals(1, groupingResultsTable.size());

		Item firstRow = groupingResultsTable.getItem(new Integer(1));
		Property groupMembersCell = firstRow.getItemProperty("Group Members");

		String memberDisplayString = (String) groupMembersCell.getValue();
		Assert.assertTrue("Expecting no truncation with full stop when members to display are within the max allowed.",
				memberDisplayString.endsWith("]."));

	}

}
