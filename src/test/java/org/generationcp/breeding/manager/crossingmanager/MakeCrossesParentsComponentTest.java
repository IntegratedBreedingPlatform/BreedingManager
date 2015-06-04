
package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MakeCrossesParentsComponentTest {

	private static final String FEMALE_PARENT = "FEMALE PARENT";
	private static final Integer FEMALE_ROW_COUNT = 5;

	private static final String MALE_PARENT = "MALE PARENT";
	private static final Integer MALE_ROW_COUNT = 5;

	@Mock
	private CrossingManagerMakeCrossesComponent parentComponent;

	private MakeCrossesParentsComponent makeCrossesParentsComponent;

	private ParentTabComponent femaleParentTab;
	private ParentTabComponent maleParentTab;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		this.makeCrossesParentsComponent = Mockito.spy(new MakeCrossesParentsComponent(this.parentComponent));
		this.femaleParentTab =
				Mockito.spy(new ParentTabComponent(this.parentComponent, this.makeCrossesParentsComponent,
						MakeCrossesParentsComponentTest.FEMALE_PARENT, MakeCrossesParentsComponentTest.FEMALE_ROW_COUNT));
		this.maleParentTab =
				Mockito.spy(new ParentTabComponent(this.parentComponent, this.makeCrossesParentsComponent,
						MakeCrossesParentsComponentTest.MALE_PARENT, MakeCrossesParentsComponentTest.MALE_ROW_COUNT));

		this.makeCrossesParentsComponent.setFemaleParentTab(this.femaleParentTab);
		this.makeCrossesParentsComponent.setMaleParentTab(this.maleParentTab);

	}

	private GermplasmList createGermplasmList() {
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		germplasmList.setName("List Name");
		germplasmList.setDescription("This is a sample list.");
		germplasmList.setDate(20150109L);

		return germplasmList;
	}

	@Test
	public void testUpdateMaleParentList() {
		GermplasmList germplasmList = this.createGermplasmList();

		Mockito.doNothing().when(this.maleParentTab).enableReserveInventory();
		Mockito.doNothing().when(this.maleParentTab).enableEditListHeaderOption();
		Mockito.doNothing().when(this.maleParentTab).updateNoOfEntries();

		this.makeCrossesParentsComponent.updateMaleParentList(germplasmList);
		Mockito.verify(this.maleParentTab, Mockito.times(1)).enableEditListHeaderOption();
		Assert.assertTrue("Expecting the germplasm list is set on the maleParentTab.",
				germplasmList.getId().equals(this.maleParentTab.getGermplasmList().getId()));
	}

	@Test
	public void testUpdateFemaleParentList() {
		GermplasmList germplasmList = this.createGermplasmList();

		Mockito.doNothing().when(this.femaleParentTab).enableReserveInventory();
		Mockito.doNothing().when(this.femaleParentTab).enableEditListHeaderOption();
		Mockito.doNothing().when(this.femaleParentTab).updateNoOfEntries();

		this.makeCrossesParentsComponent.updateFemaleParentList(germplasmList);
		Mockito.verify(this.femaleParentTab, Mockito.times(1)).enableEditListHeaderOption();
		Assert.assertTrue("Expecting the germplasm list is set on the femaleParentTab.",
				germplasmList.getId().equals(this.femaleParentTab.getGermplasmList().getId()));
	}
}
