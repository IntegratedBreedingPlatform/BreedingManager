package org.generationcp.breeding.manager.listimport;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.listimport.actions.ProcessImportedGermplasmAction;
import org.generationcp.breeding.manager.listimport.util.GermplasmListUploader;
import org.generationcp.breeding.manager.service.BreedingManagerService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.GermplasmList;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Window;

public class SpecifyGermplasmDetailsComponentTest {

	private SpecifyGermplasmDetailsComponent specifyGermplasmDetailsComponent;
	private GermplasmImportMain source;
	private GermplasmListUploader germplasmListUploader;
	private ContextUtil contextUtil;
	private ProcessImportedGermplasmAction processGermplasmAction;
	private BreedingManagerService breedingManagerService;
	private SimpleResourceBundleMessageSource messageSource;
	private static final String LIST_NAME = "Test List Name";
	private static final String LIST_TYPE = "LST";
	private static final String LIST_TITLE = "Test List Title";
	private static final String LIST_OWNER = "Test User";
	private static final Integer CURRENT_USER_LOCAL_ID = 1;
	private static final String TEST_WINDOW = "Test Window";
	private static final Date LIST_DATE = new Date();
	
	@Before
	public void setUp() throws Exception {
		createSource();
		createGermplasmListUploader();
		createProcessGermplasmAction();
		createContextUtil();
		createBreedingManagerService();
		createMessageSource();
		specifyGermplasmDetailsComponent = new SpecifyGermplasmDetailsComponent(
				source, false);
		specifyGermplasmDetailsComponent.setGermplasmListUploader(germplasmListUploader);
		specifyGermplasmDetailsComponent.setContextUtil(contextUtil);
		specifyGermplasmDetailsComponent.setParent(source);
		specifyGermplasmDetailsComponent.setProcessGermplasmAction(processGermplasmAction);
	}
	
	private void createMessageSource() {
		messageSource = mock(SimpleResourceBundleMessageSource.class);
	}

	private void createProcessGermplasmAction() {
		processGermplasmAction = new ProcessImportedGermplasmAction(specifyGermplasmDetailsComponent);
	}

	private void createBreedingManagerService() {
		breedingManagerService = mock(BreedingManagerService.class);
	}

	private void createSource() {
		Window window = new Window(TEST_WINDOW);
		window.setName(TEST_WINDOW);
		window.setSizeUndefined();
		source = new GermplasmImportMain(window, false);
		source.setParent(window);
	}

	private void createContextUtil() throws Exception {
		contextUtil = mock(ContextUtil.class);
		doReturn(CURRENT_USER_LOCAL_ID).when(contextUtil).getCurrentUserLocalId();
	}

	private void createGermplasmListUploader() {
		germplasmListUploader = new GermplasmListUploader();
		germplasmListUploader.setListName(LIST_NAME);
		germplasmListUploader.setListDate(LIST_DATE);
		germplasmListUploader.setListType(LIST_TYPE);
        germplasmListUploader.setListTitle(LIST_TITLE);
	}

	@Test
	public void testPopupSaveAsDialog_VerifyListOwner() throws Exception {
		//call the method to test
		specifyGermplasmDetailsComponent.popupSaveAsDialog();
		//verify that the created germplasm list is correct
		GermplasmList germplasmList = specifyGermplasmDetailsComponent.getGermplasmList();
		assertEquals("Expected list name is "+LIST_NAME+" but got "+
				germplasmList.getName(),LIST_NAME,germplasmList.getName());
		SimpleDateFormat formatter = new SimpleDateFormat(GermplasmImportMain.DATE_FORMAT);
        String sDate = formatter.format(LIST_DATE);
        Long expectedDate = Long.parseLong(sDate.replace("-", ""));
		assertEquals("Expected list date is "+expectedDate+" but got "+
				germplasmList.getDate(),expectedDate,germplasmList.getDate());
		assertEquals("Expected list type is "+LIST_TYPE+" but got "+
				germplasmList.getType(),LIST_TYPE,germplasmList.getType());
		assertEquals("Expected list title is "+LIST_TITLE+" but got "+
				germplasmList.getDescription(),LIST_TITLE,germplasmList.getDescription());
		assertEquals("Expected list user id is "+CURRENT_USER_LOCAL_ID+" but got "+
				germplasmList.getUserId(),CURRENT_USER_LOCAL_ID,germplasmList.getUserId());
		//verify if the list owner will be set based on the user id of the germplasm list
		
		BreedingManagerListDetailsComponent listDetailsComponent = 
				createBreedingManagerListDetailsComponent();
		doReturn(LIST_OWNER).when(breedingManagerService).getOwnerListName(germplasmList.getUserId());
		listDetailsComponent.setGermplasmListDetails(germplasmList);
		assertEquals("Expected list Owner is "+LIST_OWNER+" but got "+
				listDetailsComponent.getListOwnerField().getValue(), 
				LIST_OWNER,listDetailsComponent.getListOwnerField().getValue());
		
	}

	private BreedingManagerListDetailsComponent createBreedingManagerListDetailsComponent() {
		BreedingManagerListDetailsComponent listDetailsComponent = new BreedingManagerListDetailsComponent();
		listDetailsComponent.setBreedingManagerService(breedingManagerService);
		listDetailsComponent.setMessageSource(messageSource);
		listDetailsComponent.instantiateComponents();
		listDetailsComponent.getListNameField().instantiateComponents();
		listDetailsComponent.getListDescriptionField().instantiateComponents();
		listDetailsComponent.getListNotesField().instantiateComponents();
		listDetailsComponent.getListTypeField().instantiateComponents();
		listDetailsComponent.getListDateField().instantiateComponents();
		listDetailsComponent.getListOwnerField().instantiateComponents();
		return listDetailsComponent;
	}
}
