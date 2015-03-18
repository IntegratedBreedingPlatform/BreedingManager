package org.generationcp.breeding.manager.listimport;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingLocationField;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;

public class GermplasmFieldsComponentTest {
	private static final String DUMMY_STRING = "DUMMY STRING";

	private GermplasmFieldsComponent germplasmFieldsComponent;
	
	@Mock
	private Window parentWindow;
	@Mock
	private SimpleResourceBundleMessageSource messageSource;
	@Mock
	private BreedingLocationField locationComponent;
	@Mock
	private BreedingLocationField seedLoctionComponent;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		
		germplasmFieldsComponent = spy(new GermplasmFieldsComponent(parentWindow));
		germplasmFieldsComponent.setMessageSource(messageSource);

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
		Mockito.when(germplasmFieldsComponent.getSeedLocationComponent()).thenReturn(seedLoctionComponent);
		
		germplasmFieldsComponent.updateAllLocationFields();
		
		verify(germplasmFieldsComponent,times(1)).getLocationComponent();
		verify(germplasmFieldsComponent,times(1)).getSeedLocationComponent();
		
		verify(locationComponent,times(1)).populateHarvestLocation(null,anyString());
		verify(seedLoctionComponent,times(1)).populateHarvestLocation(null,anyString());
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
		seedLoctionComponent.setBreedingLocationComboBox(comboBox2);
		doReturn(comboBox2).when(germplasmFieldsComponent).getSeedLocationComboBox();
		
		Mockito.when(germplasmFieldsComponent.getLocationComponent()).thenReturn(locationComponent);
		Mockito.when(germplasmFieldsComponent.getSeedLocationComponent()).thenReturn(seedLoctionComponent);
		
		germplasmFieldsComponent.updateAllLocationFields();
		
		verify(germplasmFieldsComponent,times(1)).getLocationComponent();
		verify(germplasmFieldsComponent,times(1)).getSeedLocationComponent();
		
		verify(locationComponent,times(1)).populateHarvestLocation(eq(1),anyString());
		verify(seedLoctionComponent,times(1)).populateHarvestLocation(eq(2),anyString());
	}


}	
