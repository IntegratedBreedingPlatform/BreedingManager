
package org.generationcp.breeding.manager.cross.study.traitdonors.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter;
import org.generationcp.breeding.manager.cross.study.traitdonors.main.pojos.TraitItem;
import org.generationcp.breeding.manager.exception.GermplasmStudyBrowserException;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

import junit.framework.Assert;

public class PreselectTraitFilterTest {

	private PreselectTraitFilter preselectTraitFilter;
	private SimpleResourceBundleMessageSource messageSource;
	private Integer itemId;
	private String variableName;
	private TraitItem traitItem;
	private Table traitSelectTable;
	private Map<String, StandardVariableReference> traitMap;
	private TraitDonorsQueryMain traitDonorsQueryMain;
	private EnvironmentFilter environmentFilter;

	@Before
	public void setUp() throws GermplasmStudyBrowserException {
		this.traitMap = new HashMap<String, StandardVariableReference>();
		this.messageSource = Mockito.mock(SimpleResourceBundleMessageSource.class);
		this.traitDonorsQueryMain = Mockito.mock(TraitDonorsQueryMain.class);
		this.environmentFilter = Mockito.mock(EnvironmentFilter.class);
		this.preselectTraitFilter = Mockito.spy(new PreselectTraitFilter(this.traitDonorsQueryMain, this.environmentFilter));
		this.preselectTraitFilter.setOntologyDataManager(Mockito.mock(OntologyDataManager.class));
		this.preselectTraitFilter.setMessageSource(this.messageSource);
		this.preselectTraitFilter.populateTraitsTables();
		this.itemId = 1;
		this.variableName = "Test Variable Name";
		this.traitItem = new TraitItem();
		this.traitItem.setStdVarId(this.itemId);
		this.traitItem.setStdVarName(this.variableName);
		this.traitSelectTable = new Table();
		this.traitSelectTable.addItem(this.traitItem);

		Mockito.doReturn(new Window()).when(this.preselectTraitFilter).getWindow();
	}

	@Test
	public void testExtractSelectionsFromSelectTableWhenVariableIsExisting() {
		this.traitMap.put(this.variableName, new StandardVariableReference(this.itemId, this.variableName));
		this.traitSelectTable.addItem(this.traitItem);
		this.preselectTraitFilter.setTraitSelectTable(this.traitSelectTable);
		this.preselectTraitFilter.setTraitMap(this.traitMap);
		List<Integer> itemList = this.preselectTraitFilter.extractSelectionsFromSelectTable();
		Assert.assertEquals("Should return 1 trait variable id", 1, itemList.size());
	}

	@Test
	public void testExtractSelectionsFromSelectTableWhenVariableIsNotExisting() {
		this.preselectTraitFilter.setTraitSelectTable(this.traitSelectTable);
		this.preselectTraitFilter.setTraitMap(this.traitMap);
		List<Integer> itemList = this.preselectTraitFilter.extractSelectionsFromSelectTable();
		Assert.assertEquals("Should not return any trait variable since it is not in the map", 0, itemList.size());
	}

	@Test
	public void testNextButtonClickActionIfThereIsATrait() {
		this.traitMap.put(this.variableName, new StandardVariableReference(this.itemId, this.variableName));
		this.traitSelectTable.addItem(this.traitItem);
		this.preselectTraitFilter.setTraitSelectTable(this.traitSelectTable);
		this.preselectTraitFilter.setTraitMap(this.traitMap);
		this.preselectTraitFilter.nextButtonClickAction();
		List<Integer> itemList = this.preselectTraitFilter.extractSelectionsFromSelectTable();
		try {
			Mockito.verify(this.traitDonorsQueryMain, Mockito.times(1)).selectSecondTab();
			Mockito.verify(this.environmentFilter, Mockito.times(1)).populateEnvironmentsTable(itemList);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Should have triggered the populate environments table since there is at least 1 trait");
		}
	}

	@Test
	public void testNextButtonClickActionIfThereIsNoTrait() {
		this.traitSelectTable.addItem(this.traitItem);
		this.preselectTraitFilter.setTraitSelectTable(this.traitSelectTable);
		this.preselectTraitFilter.setTraitMap(this.traitMap);
		this.preselectTraitFilter.nextButtonClickAction();
		List<Integer> itemList = this.preselectTraitFilter.extractSelectionsFromSelectTable();
		try {
			Mockito.verify(this.traitDonorsQueryMain, Mockito.times(0)).selectSecondTab();
			Mockito.verify(this.environmentFilter, Mockito.times(0)).populateEnvironmentsTable(itemList);
		} catch (NeverWantedButInvoked e) {
			Assert.fail("Should not trigger the populate environments table since there is no traits");
		}
	}
}
