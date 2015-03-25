package org.generationcp.breeding.manager.listimport;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingLocationField;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;
import org.mockito.runners.MockitoJUnitRunner;

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
	private GermplasmFieldsComponent germplasmFieldsComponent = spy(new GermplasmFieldsComponent(parentWindow));

	@Before
	public void setUp(){

		when(contextUtil.getCurrentProgramUUID()).thenReturn(TEST_PROGRAMUUID);
		when(messageSource.getMessage(Message.ADD_GERMPLASM_DETAILS)).thenReturn(DUMMY_STRING);
		when(messageSource.getMessage(Message.GERMPLASM_BREEDING_METHOD_LABEL)).thenReturn(DUMMY_STRING);
		when(messageSource.getMessage(Message.GERMPLASM_LOCATION_LABEL)).thenReturn(DUMMY_STRING);
		when(messageSource.getMessage(Message.SEED_STORAGE_LOCATION_LABEL)).thenReturn(DUMMY_STRING);
		when(messageSource.getMessage(Message.GERMPLASM_DATE_LABEL)).thenReturn(DUMMY_STRING);
		when(messageSource.getMessage(Message.GERMPLASM_NAME_TYPE_LABEL)).thenReturn(DUMMY_STRING);
		
		germplasmFieldsComponent.instantiateComponents();
	}
	
	@Test
	public void testUpdateAllLocationFields_WhenLocationComboBoxValueIsNull(){
		ComboBox comboBox = new ComboBox();
		doReturn(comboBox).when(germplasmFieldsComponent).getLocationComboBox();
		doReturn(comboBox).when(germplasmFieldsComponent).getSeedLocationComboBox();
		Mockito.when(germplasmFieldsComponent.getLocationComponent()).thenReturn(locationComponent);
		Mockito.when(germplasmFieldsComponent.getSeedLocationComponent()).thenReturn(
				seedLocationComponent);
		
		germplasmFieldsComponent.updateAllLocationFields();
		
		verify(germplasmFieldsComponent,times(1)).getLocationComponent();
		verify(germplasmFieldsComponent,times(1)).getSeedLocationComponent();

		ArgumentCaptor<Integer> intArg0 = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> intArg1 = ArgumentCaptor.forClass(Integer.class);


		verify(locationComponent,times(1)).populateHarvestLocation(intArg0.capture(),eq(TEST_PROGRAMUUID));
		verify(seedLocationComponent,times(1)).populateHarvestLocation(intArg1.capture(),eq(TEST_PROGRAMUUID));

		assertNull("selected location must be null", intArg0.getValue());
		assertNull("selected location must be null", intArg1.getValue());

	}
	
	@Test
	public void testUpdateAllLocationFields_WhenLocationComboBoxValueIsNotNull(){
		ComboBox comboBox = new ComboBox();
		comboBox.addItem(1);
		comboBox.setValue(1);
		locationComponent.setBreedingLocationComboBox(comboBox);
		doReturn(comboBox).when(germplasmFieldsComponent).getLocationComboBox();
		
		ComboBox comboBox2 = new ComboBox();
		comboBox2.addItem(2);
		comboBox2.setValue(2);
		seedLocationComponent.setBreedingLocationComboBox(comboBox2);
		doReturn(comboBox2).when(germplasmFieldsComponent).getSeedLocationComboBox();
		
		Mockito.when(germplasmFieldsComponent.getLocationComponent()).thenReturn(locationComponent);
		Mockito.when(germplasmFieldsComponent.getSeedLocationComponent()).thenReturn(
				seedLocationComponent);
		
		germplasmFieldsComponent.updateAllLocationFields();
		
		verify(germplasmFieldsComponent,times(1)).getLocationComponent();
		verify(germplasmFieldsComponent,times(1)).getSeedLocationComponent();
		
		verify(locationComponent,times(1)).populateHarvestLocation(eq(1),eq(TEST_PROGRAMUUID));
		verify(seedLocationComponent,times(1)).populateHarvestLocation(eq(2),eq(TEST_PROGRAMUUID));
	}


}	
