
package org.generationcp.breeding.manager.listimport;

import java.util.Iterator;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingLocationField;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class GermplasmFieldsComponentTest {

	private static final String DUMMY_STRING = "DUMMY STRING";
	private static final String TEST_PROGRAMUUID = "TEST_UUID";

	@Mock
	private Window parentWindow;
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	@Mock
	private BreedingLocationField locationComponent;
	@Mock
	private BreedingLocationField seedLocationComponent;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private final GermplasmFieldsComponent germplasmFieldsComponent = new GermplasmFieldsComponent(this.parentWindow);

	@Before
	public void setUp() {
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(GermplasmFieldsComponentTest.TEST_PROGRAMUUID);
		Mockito.when(this.messageSource.getMessage(Matchers.any(Message.class))).thenReturn(GermplasmFieldsComponentTest.DUMMY_STRING);

		this.germplasmFieldsComponent.instantiateComponents();

		this.germplasmFieldsComponent.setLocationComponent(this.locationComponent);
		this.germplasmFieldsComponent.setSeedLocationComponent(this.seedLocationComponent);
	}

	@Test
	public void testUpdateAllLocationFieldsWhenLocationComboBoxValueIsNull() {
		final ComboBox comboBox = new ComboBox();
		Mockito.doReturn(comboBox).when(this.locationComponent).getBreedingLocationComboBox();
		Mockito.doReturn(comboBox).when(this.seedLocationComponent).getBreedingLocationComboBox();

		this.germplasmFieldsComponent.updateAllLocationFields();

		final ArgumentCaptor<Integer> selectedLocation = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Integer> selectedSeedLocation = ArgumentCaptor.forClass(Integer.class);

		Mockito.verify(this.locationComponent, Mockito.times(1)).populateHarvestLocation(selectedLocation.capture(),
				Matchers.eq(GermplasmFieldsComponentTest.TEST_PROGRAMUUID));
		Mockito.verify(this.seedLocationComponent, Mockito.times(1)).populateHarvestLocation(selectedSeedLocation.capture(),
				Matchers.eq(GermplasmFieldsComponentTest.TEST_PROGRAMUUID));

		Assert.assertNull("Selected Germplasm location must be null", selectedLocation.getValue());
		Assert.assertNull("Selected Seed Storage location must be null", selectedSeedLocation.getValue());

	}

	@Test
	public void testUpdateAllLocationFieldsWhenLocationComboBoxValueIsNotNull() {
		final ComboBox comboBox = new ComboBox();
		comboBox.addItem(1);
		comboBox.setValue(1);
		Mockito.doReturn(comboBox).when(this.locationComponent).getBreedingLocationComboBox();

		final ComboBox comboBox2 = new ComboBox();
		comboBox2.addItem(2);
		comboBox2.setValue(2);
		Mockito.doReturn(comboBox2).when(this.seedLocationComponent).getBreedingLocationComboBox();

		this.germplasmFieldsComponent.updateAllLocationFields();

		Mockito.verify(this.locationComponent, Mockito.times(1)).populateHarvestLocation(Matchers.eq(1),
				Matchers.eq(GermplasmFieldsComponentTest.TEST_PROGRAMUUID));
		Mockito.verify(this.seedLocationComponent, Mockito.times(1)).populateHarvestLocation(Matchers.eq(2),
				Matchers.eq(GermplasmFieldsComponentTest.TEST_PROGRAMUUID));
	}

	@Test
	public void testGetGermplasmDetailsInstructionsDefaultView() {
		// GetGermplasmDetailsInstructionsDefaultView method was already called in instantiateComponents method
		Mockito.verify(this.messageSource).getMessage(Message.SPECIFY_DETAILS_FOR_IMPORTED_GERMPLASM);
		Mockito.verify(this.messageSource).getMessage(Message.DETAILS_ARE_OPTIONAL);
	}

	@Test
	public void testGetGermplasmDetailsInstructionsWhenInventoryAmountIsPresent() {
		this.germplasmFieldsComponent.setHasInventoryAmount(true);
		this.germplasmFieldsComponent.getGermplasmDetailsInstructions();

		/*
		 * SPECIFY_DETAILS_FOR_IMPORTED_GERMPLASM message was already called in instantiateComponents method, hence making total call after
		 * refreshLayout equals two since it's a fixed sub-string of instruction
		 */
		Mockito.verify(this.messageSource, Mockito.times(2)).getMessage(Message.SPECIFY_DETAILS_FOR_IMPORTED_GERMPLASM);
		Mockito.verify(this.messageSource).getMessage(Message.SEED_STORAGE_REQUIRED_WHEN_INVENTORY_IS_PRESENT);
	}

	@Test
	public void testRefreshLayoutWhenInventoryAmountIsPresent() {
		Mockito.doReturn("Seed storage required.").when(this.messageSource)
				.getMessage(Message.SEED_STORAGE_REQUIRED_WHEN_INVENTORY_IS_PRESENT);
		final String oldDisplayMessage = (String) this.germplasmFieldsComponent.getGermplasmDetailsMessage().getValue();

		this.germplasmFieldsComponent.refreshLayout(true, true);

		/*
		 * SPECIFY_DETAILS_FOR_IMPORTED_GERMPLASM message was already called in instantiateComponents method, hence making total call after
		 * refreshLayout equals two since it's a fixed sub-string of instruction
		 */
		Mockito.verify(this.messageSource, Mockito.times(2)).getMessage(Message.SPECIFY_DETAILS_FOR_IMPORTED_GERMPLASM);
		Mockito.verify(this.messageSource).getMessage(Message.SEED_STORAGE_REQUIRED_WHEN_INVENTORY_IS_PRESENT);

		// Check that instructions text changed after refresh layout
		final String newDisplayMessage = (String) this.germplasmFieldsComponent.getGermplasmDetailsMessage().getValue();
		Assert.assertFalse("Instructions text should have changed upon refresh when inventory is present",
				oldDisplayMessage.equals(newDisplayMessage));
	}

	@Test
	public void testRefreshLayoutWhenNoInventoryVariableInImportFile() {
		final boolean hasInventoryVariable = true;
		this.germplasmFieldsComponent.refreshLayout(hasInventoryVariable, false);

		// Check the height of the layout. Layout is longer when inventory variable is present
		Assert.assertEquals("Layout height is 360px", 360.0f, this.germplasmFieldsComponent.getHeight());
		Assert.assertEquals(0, this.germplasmFieldsComponent.getHeightUnits());

		// Check the # of components added in the layout
		Assert.assertEquals("Expecting 9 UI components in the layout", 9, this.germplasmFieldsComponent.getComponentCount());
		final Iterator<Component> componentIterator = this.germplasmFieldsComponent.getComponentIterator();
		boolean seedStorageLocationAdded = false;
		while (componentIterator.hasNext()) {
			final Component component = componentIterator.next();
			if (this.germplasmFieldsComponent.getSeedLocationComponent().equals(component)) {
				seedStorageLocationAdded = true;
			}
		}
		// Expecting Seed Storage Location to be displayed
		Assert.assertTrue("Expecting Seed Storage location field to be displayed", seedStorageLocationAdded);

	}

	@Test
	public void testRefreshLayoutWhenInventoryVariablePresentInImportFile() {
		final boolean hasInventoryVariable = false;
		this.germplasmFieldsComponent.refreshLayout(hasInventoryVariable, false);

		// Check the height of the layout. Layout is longer when inventory variable is present
		Assert.assertEquals("Layout height is 300px", 300.0f, this.germplasmFieldsComponent.getHeight());
		Assert.assertEquals(0, this.germplasmFieldsComponent.getHeightUnits());

		// Check the # of components added in the layout
		Assert.assertEquals("Expecting 8 UI components in the layout", 8, this.germplasmFieldsComponent.getComponentCount());
		final Iterator<Component> componentIterator = this.germplasmFieldsComponent.getComponentIterator();

		// Expecting Seed Storage Location not included in layout
		while (componentIterator.hasNext()) {
			final Component component = componentIterator.next();
			if (this.germplasmFieldsComponent.getSeedLocationComponent().equals(component)) {
				Assert.fail("Expecting Seed Storage location to not be included in layout but was present.");
			}
		}

	}
}
