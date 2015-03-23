package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.middleware.pojos.GermplasmList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

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
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		
		
		makeCrossesParentsComponent = spy(new MakeCrossesParentsComponent(parentComponent));
		femaleParentTab = spy(new ParentTabComponent(parentComponent,makeCrossesParentsComponent,FEMALE_PARENT, FEMALE_ROW_COUNT));
		maleParentTab = spy(new ParentTabComponent(parentComponent,makeCrossesParentsComponent,MALE_PARENT, MALE_ROW_COUNT));
		
		makeCrossesParentsComponent.setFemaleParentTab(femaleParentTab);
		makeCrossesParentsComponent.setMaleParentTab(maleParentTab);
		
	}
	
	private GermplasmList createGermplasmList(){
		GermplasmList germplasmList = new GermplasmList();
		germplasmList.setId(1);
		germplasmList.setName("List Name");
		germplasmList.setDescription("This is a sample list.");
		germplasmList.setDate(20150109L);
		
		return germplasmList;
	}
	
	@Test
	public void testUpdateMaleParentList(){
		GermplasmList germplasmList = createGermplasmList();
		
		doNothing().when(maleParentTab).enableReserveInventory();
		doNothing().when(maleParentTab).enableEditListHeaderOption();
		doNothing().when(maleParentTab).updateNoOfEntries();
		
		makeCrossesParentsComponent.updateMaleParentList(germplasmList);
		Mockito.verify(maleParentTab, Mockito.times(1)).enableEditListHeaderOption();
		Assert.assertTrue("Expecting the germplasm list is set on the maleParentTab.", germplasmList.getId().equals(maleParentTab.getGermplasmList().getId()));
	}
	
	@Test
	public void testUpdateFemaleParentList(){
		GermplasmList germplasmList = createGermplasmList();
		
		doNothing().when(femaleParentTab).enableReserveInventory();
		doNothing().when(femaleParentTab).enableEditListHeaderOption();
		doNothing().when(femaleParentTab).updateNoOfEntries();
		
		makeCrossesParentsComponent.updateFemaleParentList(germplasmList);
		Mockito.verify(femaleParentTab, Mockito.times(1)).enableEditListHeaderOption();
		Assert.assertTrue("Expecting the germplasm list is set on the femaleParentTab.", germplasmList.getId().equals(femaleParentTab.getGermplasmList().getId()));
	}
}
