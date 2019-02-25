package org.generationcp.breeding.manager.crossingmanager.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.MakeCrossesParentsComponent;
import org.generationcp.breeding.manager.crossingmanager.ParentTabComponent;
import org.generationcp.breeding.manager.crossingmanager.constants.CrossType;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;



public class CrossTypeValidatorTest {
	
	private static final String AT_LEAST_ONE_FEMALE = "At least one female must be selected.";
	
	private static final String AT_LEAST_ONE_MALE = "At least one male must be selected.";
	
	private static final String MALE_FEMALE_MUST_BE_EQUAL = "Male and female must be equal.";
	
	private static final String MALE_WILL_BE_IGNORED = "Male will be ignored.";
	
	private static final String PLEASE_CHOOSE = "Please Choose";

	private static final String WARNING = "warning";

	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	
	@Mock
	private MakeCrossesParentsComponent parentsComponent;
	
	@Mock
	private Window window;
	
	@Mock
	private Table femaleTable;
	
	@Mock
	private Table maleTable;
	
	@Mock
	private ParentTabComponent maleTab;
	
	@InjectMocks
	private CrossTypeValidator validator;
	
	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.validator.setMessageSource(this.messageSource);
		this.validator.setParentsComponent(this.parentsComponent);
		
		Mockito.doReturn(this.maleTab).when(this.parentsComponent).getMaleParentTab();
		Mockito.doReturn(this.window).when(this.parentsComponent).getWindow();
		Mockito.doReturn(this.femaleTable).when(this.parentsComponent).getFemaleTable();
		Mockito.doReturn(this.maleTable).when(this.parentsComponent).getMaleTable();
		Mockito.doReturn(WARNING).when(this.messageSource).getMessage(Message.WARNING);
		Mockito.doReturn(PLEASE_CHOOSE).when(this.messageSource).getMessage(Message.PLEASE_CHOOSE_CROSSING_METHOD);
		Mockito.doReturn(AT_LEAST_ONE_FEMALE).when(this.messageSource).getMessage(Message.AT_LEAST_ONE_FEMALE_PARENT_MUST_BE_SELECTED);
		Mockito.doReturn(AT_LEAST_ONE_MALE).when(this.messageSource).getMessage(Message.AT_LEAST_ONE_MALE_PARENT_MUST_BE_SELECTED);
		Mockito.doReturn(MALE_WILL_BE_IGNORED).when(this.messageSource).getMessage(Message.MALE_PARENTS_WILL_BE_IGNORED);
		Mockito.doReturn(MALE_FEMALE_MUST_BE_EQUAL).when(this.messageSource).getMessage(Message.ERROR_MALE_AND_FEMALE_PARENTS_MUST_BE_EQUAL);
	}
	
	@Test
	public void testNoCrossTypeChosen() {
		assertFalse(this.validator.isValid(CrossType.PLEASE_CHOOSE));
		Mockito.verify(this.window).showNotification(this.notificationCaptor.capture());
		final Notification notification = this.notificationCaptor.getValue();
		assertEquals(WARNING, notification.getStyleName());
		assertEquals("</br>" + PLEASE_CHOOSE, notification.getDescription());
	}
	
	@Test
	public void testNoFemaleParents() {
		try {
			this.validator.isValid(CrossType.MULTIPLY);
			fail("Excepting InvalidValueException to be thrown but was not.");
		} catch (final InvalidValueException e) {
			assertEquals(AT_LEAST_ONE_FEMALE, e.getMessage());
		}
	}
	
	@Test
	public void testNoMaleParents() {
		Mockito.doReturn(this.createListEntries(5)).when(this.parentsComponent).getCorrectSortedValue(this.femaleTable);
		try {
			this.validator.isValid(CrossType.MULTIPLY);
			fail("Excepting InvalidValueException to be thrown but was not.");
		} catch (final InvalidValueException e) {
			assertEquals(AT_LEAST_ONE_MALE, e.getMessage());
		}
	}
	
	@Test
	public void testNoMaleParentsForUnknownMaleCrossType() {
		Mockito.doReturn(this.createListEntries(5)).when(this.parentsComponent).getCorrectSortedValue(this.femaleTable);
		assertTrue(this.validator.isValid(CrossType.UNKNOWN_MALE));
	}
	
	@Test
	public void testMaleParentsIgnoredForUnknownMaleCrossType() {
		Mockito.doReturn(this.createListEntries(5)).when(this.parentsComponent).getCorrectSortedValue(this.femaleTable);
		Mockito.doReturn(this.createListEntries(2)).when(this.parentsComponent).getCorrectSortedValue(this.maleTable);
		
		// Male parents will be cleared and crossing will continue
		assertTrue(this.validator.isValid(CrossType.UNKNOWN_MALE));
		Mockito.verify(this.window).showNotification(this.notificationCaptor.capture());
		final Notification notification = this.notificationCaptor.getValue();
		assertEquals(WARNING, notification.getStyleName());
		assertEquals("</br>" + MALE_WILL_BE_IGNORED, notification.getDescription());
		Mockito.verify(this.maleTab).resetList();
	}
	
	@Test
	public void testParentsAreNotEqualForTopToBottomCrossType() {
		Mockito.doReturn(this.createListEntries(5)).when(this.parentsComponent).getCorrectSortedValue(this.femaleTable);
		Mockito.doReturn(this.createListEntries(2)).when(this.parentsComponent).getCorrectSortedValue(this.maleTable);
		try {
			this.validator.isValid(CrossType.TOP_TO_BOTTOM);
			fail("Excepting InvalidValueException to be thrown but was not.");
		} catch (final InvalidValueException e) {
			assertEquals(MALE_FEMALE_MUST_BE_EQUAL, e.getMessage());
		}
	}
	
	@Test
	public void testParentsAreEqualForTopToBottomCrossType() {
		Mockito.doReturn(this.createListEntries(5)).when(this.parentsComponent).getCorrectSortedValue(this.femaleTable);
		Mockito.doReturn(this.createListEntries(5)).when(this.parentsComponent).getCorrectSortedValue(this.maleTable);
		Assert.assertTrue(this.validator.isValid(CrossType.TOP_TO_BOTTOM));	
	}
	
	@Test
	public void testParentsAreValidForMultiplyCrossType() {
		Mockito.doReturn(this.createListEntries(5)).when(this.parentsComponent).getCorrectSortedValue(this.femaleTable);
		Mockito.doReturn(this.createListEntries(2)).when(this.parentsComponent).getCorrectSortedValue(this.maleTable);
		Assert.assertTrue(this.validator.isValid(CrossType.MULTIPLY));	
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
