
package org.generationcp.breeding.manager.listimport;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingLocationField;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;

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
	private final GermplasmFieldsComponent germplasmFieldsComponent = Mockito.spy(new GermplasmFieldsComponent(this.parentWindow));

	@Before
	public void setUp() {

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(GermplasmFieldsComponentTest.TEST_PROGRAMUUID);
		Mockito.when(this.messageSource.getMessage(Message.ADD_GERMPLASM_DETAILS)).thenReturn(GermplasmFieldsComponentTest.DUMMY_STRING);
		Mockito.when(this.messageSource.getMessage(Message.GERMPLASM_BREEDING_METHOD_LABEL)).thenReturn(
				GermplasmFieldsComponentTest.DUMMY_STRING);
		Mockito.when(this.messageSource.getMessage(Message.GERMPLASM_LOCATION_LABEL)).thenReturn(GermplasmFieldsComponentTest.DUMMY_STRING);
		Mockito.when(this.messageSource.getMessage(Message.SEED_STORAGE_LOCATION_LABEL)).thenReturn(
				GermplasmFieldsComponentTest.DUMMY_STRING);
		Mockito.when(this.messageSource.getMessage(Message.GERMPLASM_DATE_LABEL)).thenReturn(GermplasmFieldsComponentTest.DUMMY_STRING);
		Mockito.when(this.messageSource.getMessage(Message.GERMPLASM_NAME_TYPE_LABEL))
				.thenReturn(GermplasmFieldsComponentTest.DUMMY_STRING);

		this.germplasmFieldsComponent.instantiateComponents();
	}

	@Test
	public void testUpdateAllLocationFields_WhenLocationComboBoxValueIsNull() {
		ComboBox comboBox = new ComboBox();
		Mockito.doReturn(comboBox).when(this.germplasmFieldsComponent).getLocationComboBox();
		Mockito.doReturn(comboBox).when(this.germplasmFieldsComponent).getSeedLocationComboBox();
		Mockito.when(this.germplasmFieldsComponent.getLocationComponent()).thenReturn(this.locationComponent);
		Mockito.when(this.germplasmFieldsComponent.getSeedLocationComponent()).thenReturn(this.seedLocationComponent);

		this.germplasmFieldsComponent.updateAllLocationFields();

		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(1)).getLocationComponent();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(1)).getSeedLocationComponent();

		ArgumentCaptor<Integer> intArg0 = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> intArg1 = ArgumentCaptor.forClass(Integer.class);

		Mockito.verify(this.locationComponent, Mockito.times(1)).populateHarvestLocation(intArg0.capture(),
				Matchers.eq(GermplasmFieldsComponentTest.TEST_PROGRAMUUID));
		Mockito.verify(this.seedLocationComponent, Mockito.times(1)).populateHarvestLocation(intArg1.capture(),
				Matchers.eq(GermplasmFieldsComponentTest.TEST_PROGRAMUUID));

		Assert.assertNull("selected location must be null", intArg0.getValue());
		Assert.assertNull("selected location must be null", intArg1.getValue());

	}

	@Test
	public void testUpdateAllLocationFields_WhenLocationComboBoxValueIsNotNull() {
		ComboBox comboBox = new ComboBox();
		comboBox.addItem(1);
		comboBox.setValue(1);
		this.locationComponent.setBreedingLocationComboBox(comboBox);
		Mockito.doReturn(comboBox).when(this.germplasmFieldsComponent).getLocationComboBox();

		ComboBox comboBox2 = new ComboBox();
		comboBox2.addItem(2);
		comboBox2.setValue(2);
		this.seedLocationComponent.setBreedingLocationComboBox(comboBox2);
		Mockito.doReturn(comboBox2).when(this.germplasmFieldsComponent).getSeedLocationComboBox();

		Mockito.when(this.germplasmFieldsComponent.getLocationComponent()).thenReturn(this.locationComponent);
		Mockito.when(this.germplasmFieldsComponent.getSeedLocationComponent()).thenReturn(this.seedLocationComponent);

		this.germplasmFieldsComponent.updateAllLocationFields();

		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(1)).getLocationComponent();
		Mockito.verify(this.germplasmFieldsComponent, Mockito.times(1)).getSeedLocationComponent();

		Mockito.verify(this.locationComponent, Mockito.times(1)).populateHarvestLocation(Matchers.eq(1),
				Matchers.eq(GermplasmFieldsComponentTest.TEST_PROGRAMUUID));
		Mockito.verify(this.seedLocationComponent, Mockito.times(1)).populateHarvestLocation(Matchers.eq(2),
				Matchers.eq(GermplasmFieldsComponentTest.TEST_PROGRAMUUID));
	}

}
