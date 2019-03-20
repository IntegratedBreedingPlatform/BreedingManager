package org.generationcp.breeding.manager.crossingmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.breeding.manager.crossingmanager.constants.CrossType;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.validator.CrossTypeValidator;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.vaadin.data.Validator;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

@RunWith(MockitoJUnitRunner.class)
public class CrossingMethodComponentTest {
	
	private static final String MALE_LIST_NAME = RandomStringUtils.randomAlphabetic(20);
	private static final String FEMALE_LIST_NAME = RandomStringUtils.randomAlphabetic(20);

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	Window window;

	@Mock
	CrossingManagerMakeCrossesComponent makeCrossesMain;
	
	@Mock
	private CrossTypeValidator crossTypeValidator;
	
	@Mock
	private MakeCrossesParentsComponent parentsComponent;
	
	@Mock
	private Table femaleTable;
	
	@Mock
	private Table maleTable;

	private CrossingMethodComponent component;

	@Before
	public void setUp() throws Exception {
		this.component = new CrossingMethodComponent(this.makeCrossesMain);
		this.component.setMessageSource(this.messageSource);
		this.component.setParent(this.makeCrossesMain);
		this.component.setParentsComponent(this.parentsComponent);
		Mockito.when(this.makeCrossesMain.getWindow()).thenReturn(this.window);
		
		this.component.instantiateComponents();
		// It's important to set the mock CrossTypeValidator after instantiateComponents call to override the one created there
		this.component.setCrossTypeValidator(this.crossTypeValidator);
		this.component.initializeValues();
		this.component.addListeners();
	}
	
	@Test
	public void testInitialize() {
		for (final CrossType crossType : CrossType.values()) {
			Assert.assertTrue(this.component.getCrossingMethodComboBox().containsId(crossType));
		}
	}
	
	@Test
	public void testCrossingMethodComboboxValueChange() {
		Assert.assertTrue(this.component.getChkBoxExcludeSelfs().isEnabled());
		Assert.assertTrue(this.component.getChkBoxMakeReciprocalCrosses().isEnabled());
		
		this.component.getCrossingMethodComboBox().setValue(CrossType.UNKNOWN_MALE);
		Assert.assertFalse(this.component.getChkBoxExcludeSelfs().isEnabled());
		Assert.assertFalse(this.component.getChkBoxMakeReciprocalCrosses().isEnabled());
		
		this.component.getCrossingMethodComboBox().setValue(CrossType.MULTIPLY);
		Assert.assertTrue(this.component.getChkBoxExcludeSelfs().isEnabled());
		Assert.assertTrue(this.component.getChkBoxMakeReciprocalCrosses().isEnabled());
		Assert.assertTrue((boolean)this.component.getChkBoxExcludeSelfs().getValue());

		this.component.getCrossingMethodComboBox().setValue(CrossType.MULTIPLE_MALE);
		Assert.assertTrue(this.component.getChkBoxExcludeSelfs().isEnabled());
		Assert.assertFalse((boolean)this.component.getChkBoxExcludeSelfs().getValue());
		Assert.assertFalse(this.component.getChkBoxMakeReciprocalCrosses().isEnabled());
	}
	
	@Test
	public void testGenerateCrossWithValidatorException() {
		Mockito.doThrow(new Validator.InvalidValueException("SOME ERROR")).when(this.crossTypeValidator).isValid(ArgumentMatchers.any(CrossType.class));
		this.component.getGenerateCrossButton().click();
		Mockito.verify(this.parentsComponent, Mockito.never()).getFemaleTable();
		Mockito.verify(this.parentsComponent, Mockito.never()).getMaleTable();
	}
	
	@Test
	public void testGenerateCrossValidatorError() {
		Mockito.doReturn(false).when(this.crossTypeValidator).isValid(ArgumentMatchers.any(CrossType.class));
		this.component.getGenerateCrossButton().click();
		Mockito.verify(this.parentsComponent, Mockito.never()).getFemaleTable();
		Mockito.verify(this.parentsComponent, Mockito.never()).getMaleTable();
	}

	@Test
	public void testGenerateCross() {
		Mockito.doReturn(true).when(this.crossTypeValidator).isValid(ArgumentMatchers.any(CrossType.class));
		Mockito.when(parentsComponent.getFemaleTable()).thenReturn(this.femaleTable);
		Mockito.when(parentsComponent.getMaleTable()).thenReturn(this.maleTable);
		final List<GermplasmListEntry> femaleEntries = this.createListEntries(5);
		final List<GermplasmListEntry> maleEntries = this.createListEntries(2);
		Mockito.doReturn(femaleEntries).when(this.parentsComponent).getCorrectSortedValue(this.femaleTable);
		Mockito.doReturn(maleEntries).when(this.parentsComponent).getCorrectSortedValue(this.maleTable);
		Mockito.doReturn(FEMALE_LIST_NAME).when(this.parentsComponent).getFemaleListNameForCrosses();
		Mockito.doReturn(MALE_LIST_NAME).when(this.parentsComponent).getMaleListNameForCrosses();

		this.component.getCrossingMethodComboBox().setValue(CrossType.MULTIPLY);
		this.component.getChkBoxMakeReciprocalCrosses().setValue(new Random().nextBoolean());
		this.component.getChkBoxExcludeSelfs().setValue(new Random().nextBoolean());
		this.component.getGenerateCrossButton().click();

		Mockito.verify(parentsComponent).updateFemaleListNameForCrosses();
		Mockito.verify(parentsComponent).updateMaleListNameForCrosses();
		Mockito.verify(this.makeCrossesMain).makeCrossButtonAction(femaleEntries, maleEntries,
				FEMALE_LIST_NAME, MALE_LIST_NAME, CrossType.MULTIPLY, (Boolean) this.component.getChkBoxMakeReciprocalCrosses().getValue(),
				(Boolean) this.component.getChkBoxExcludeSelfs().getValue());
	}
	
	private List<GermplasmListEntry> createListEntries(final int numOfEntries) {
		final List<GermplasmListEntry> femaleEntries = new ArrayList<>();
		final Random random = new Random();
		for (int i = 1; i <= numOfEntries; i++){			
			femaleEntries.add(new GermplasmListEntry(random.nextInt(), random.nextInt(), i));
		}
		return femaleEntries;
	}
}
