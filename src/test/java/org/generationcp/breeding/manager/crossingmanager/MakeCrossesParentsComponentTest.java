
package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

public class MakeCrossesParentsComponentTest {

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private CrossingManagerMakeCrossesComponent parentComponent;

	@InjectMocks
	private MakeCrossesParentsComponent makeCrossesParentsComponent = new MakeCrossesParentsComponent(this.parentComponent);

	@Mock
	private ParentTabComponent femaleParentTab;
	@Mock
	private ParentTabComponent maleParentTab;

	private GermplasmList germplasmList;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		Mockito.doReturn("Parent List").when(this.messageSource).getMessage(Message.PARENTS_LISTS);
		Mockito.doReturn("Reserve Inventory").when(this.messageSource).getMessage(Message.RESERVE_INVENTORY);

		this.makeCrossesParentsComponent.instantiateComponents();
		this.makeCrossesParentsComponent.setMaleParentTab(this.maleParentTab);
		this.makeCrossesParentsComponent.setFemaleParentTab(this.femaleParentTab);

		this.germplasmList = this.createGermplasmList();
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
		this.makeCrossesParentsComponent.updateMaleParentList(this.germplasmList);
		try {
			Mockito.verify(this.maleParentTab, Mockito.atLeast(1)).setGermplasmList(this.germplasmList);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting the germplasm list in male parent tab is set but didn't");
		}
	}

	@Test
	public void testUpdateFemaleParentList() {
		this.makeCrossesParentsComponent.updateFemaleParentList(this.germplasmList);
		try {
			Mockito.verify(this.femaleParentTab, Mockito.atLeast(1)).setGermplasmList(this.germplasmList);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting the germplasm list in female parent tab is set but didn't");
		}
	}
}
