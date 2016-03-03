package org.generationcp.breeding.manager.listimport.actions;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmName;
import org.generationcp.breeding.manager.listimport.GermplasmFieldsComponent;
import org.generationcp.breeding.manager.listimport.SpecifyGermplasmDetailsComponent;
import org.generationcp.breeding.manager.pojos.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.ComboBox;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NoPedigreeConncetionProcessUT {

	public static final int IBDB_USER_ID = 2;
	public static final int DATE_INT_VALUE = 2032016;
	public static final int DUMMY_GID = 3;
	public static final String DUMMY_DESIG = "DUMMY_DESIG";
	public static final int COMBO_BOX_LOCATION_OPTION = 5;
	private static final int COMBO_BOX_METHOD_OPTION = 6;
	private static final int COMBO_BOX_NAME_OPTION = 6;
	public static final int DUMMY_METHOD_ID = 5;

	NoPedigreeConncetionProcess process;

	@Mock
	SpecifyGermplasmDetailsComponent germplasmDetailsComponentMock;
	@Mock
	private ContextUtil contextUtilMock;
	@Mock
	GermplasmDataManager germplasmDataManagerMock;



	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		process = new ProcessImportedGermplasmAction(germplasmDetailsComponentMock);
		((ProcessImportedGermplasmAction) process).setContextUtil(contextUtilMock);
		((ProcessImportedGermplasmAction) process).setGermplasmDataManager(germplasmDataManagerMock);

		GermplasmFieldsComponent componentMock = mock(GermplasmFieldsComponent.class);
		ComboBox locationComboMock = mock(ComboBox.class);
		ComboBox methodComboMock = mock(ComboBox.class);
		ComboBox nameTypeComboMock = mock(ComboBox.class);
		when(germplasmDetailsComponentMock.getGermplasmFieldsComponent()).thenReturn(componentMock);
		when(componentMock.getLocationComboBox()).thenReturn(locationComboMock);
		when(locationComboMock.getValue()).thenReturn(COMBO_BOX_LOCATION_OPTION);

		when(componentMock.getBreedingMethodComboBox()).thenReturn(methodComboMock);
		when(methodComboMock.getValue()).thenReturn(COMBO_BOX_METHOD_OPTION);

		when(componentMock.getNameTypeComboBox()).thenReturn(nameTypeComboMock);
		when(nameTypeComboMock.getValue()).thenReturn(COMBO_BOX_NAME_OPTION);

		Method method = new Method(DUMMY_METHOD_ID);
		method.setMtype("GEN");
		when(germplasmDataManagerMock.getMethodByID(COMBO_BOX_METHOD_OPTION)).thenReturn(method);

	}


	@Test
	public void processGermplasmWithoutConnectionsCreateOnlyCreatesNameObjectWhenGermplasmAlreadyAdded(){
		Map<String, Germplasm> germplasms = new HashMap<>();

		Germplasm germplasm = new Germplasm(DUMMY_GID);
		germplasms.put(DUMMY_DESIG,germplasm);

		GermplasmName germplasmName =
				process.generateGermplasmNameProcess(IBDB_USER_ID, DATE_INT_VALUE, germplasms, DUMMY_GID, DUMMY_DESIG);

		assertEquals(germplasm , germplasmName.getGermplasm());
	}

	@Test
	public void processGermplasmWithoutConnectionsCreateGermplasmNameWithExpectedGermplasmId(){

		Map<String, Germplasm> germplasms = new HashMap<>();



		GermplasmName germplasmName =
				process.generateGermplasmNameProcess(IBDB_USER_ID, DATE_INT_VALUE, germplasms, DUMMY_GID, DUMMY_DESIG);

		int gid = germplasmName.getGermplasm().getGid();
		assertEquals(DUMMY_GID , gid);

	}


}
