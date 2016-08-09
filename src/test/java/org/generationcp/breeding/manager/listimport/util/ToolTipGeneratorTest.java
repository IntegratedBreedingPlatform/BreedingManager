
package org.generationcp.breeding.manager.listimport.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.customcomponent.ViewListHeaderWindow;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;

public class ToolTipGeneratorTest {

	/**
	 * Simple test to check the tool tip generator is working
	 */
	@Test
	public void testGetItemDescriptionGenerator() {

		final List<UserDefinedField> createTestGermplasmListType = this.createTestGermplasmListType();
		final Map<Integer, String> singletonMap = Collections.singletonMap(1, "Test Name");
		final ToolTipGenerator toolTipGenerator =
				new ToolTipGenerator(singletonMap, createTestGermplasmListType, new ToolTipGenerator.ViewListHeader() {

					@Override
					public ViewListHeaderWindow createViewListHeaderWindow(GermplasmList germplasmList) {
						ViewListHeaderWindow viewListHeaderWindow =
								new ViewListHeaderWindow(germplasmList, singletonMap, createTestGermplasmListType);
						viewListHeaderWindow.instantiateComponents();
						return viewListHeaderWindow;
					}
				});
		final GermplasmList createGermplasmList = GermplasmListTestDataInitializer.createGermplasmList(1);
		final ItemDescriptionGenerator itemDescriptionGenerator =
				toolTipGenerator.getItemDescriptionGenerator(Collections.singleton(createGermplasmList));
		final String generateDescription = itemDescriptionGenerator.generateDescription(null, createGermplasmList.getId(), null);
		Assert.assertEquals("<table border=\"0\">\n<tr>\n<td><b>List Name:</b></td>\n<td>List 1</td>\n</tr>\n"
				+ "<tr>\n<td><b>List Owner:</b></td>\n<td>Test Name</td>\n</tr>\n"
				+ "<tr>\n<td><b>Status:</b></td>\n<td>Unlocked List</td>\n</tr>\n"
				+ "<tr>\n<td><b>Description:</b></td>\n<td>List 1 Description</td>\n</tr>\n"
				+ "<tr>\n<td><b>Type:</b></td>\n<td>LIST FOLDER</td>\n</tr>\n"
				+ "<tr>\n<td><b>Creation Date:</b></td>\n<td>20150101</td>\n</tr>\n"
				+ "<tr>\n<td><b>Notes:</b></td>\n<td>Some notes here</td>\n</tr>\n</table>", generateDescription);
	}

	private List<UserDefinedField> createTestGermplasmListType() {
		List<UserDefinedField> userDefinedFields = new ArrayList<UserDefinedField>();
		UserDefinedField listType = new UserDefinedField();
		listType.setFcode("LST");
		listType.setFname("LIST FOLDER");
		userDefinedFields.add(listType);
		UserDefinedField folderType = new UserDefinedField();
		folderType.setFcode("LST");
		folderType.setFname("FOLDER");
		userDefinedFields.add(folderType);
		return userDefinedFields;
	}

}
