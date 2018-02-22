
package org.generationcp.breeding.manager.crossingmanager;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customcomponent.LinkButton;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;


public class CrossingManagerMakeCrossesComponentTest {

	public static final String LOCALHOST = "localhost";
	public static final String NURSERY_ID = "25019";
	public static final int PORT = 8080;
	public static final String LIST_ID = "38";
	public static final String HTTP = "http";

	@Mock
	private ManageCrossingSettingsMain manageCrossingSettingsMain;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private Window window;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	private CrossingManagerMakeCrossesComponent makeCrosses;
	private HttpServletRequest mockRequest;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		this.makeCrosses = Mockito.spy(new CrossingManagerMakeCrossesComponent(this.manageCrossingSettingsMain));

		Mockito.doReturn("Return to Nursery").when(this.messageSource).getMessage(Message.BACK_TO_NURSERY);
		this.makeCrosses.setMessageSource(this.messageSource);
		this.makeCrosses.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		Mockito.doReturn(this.window).when(this.makeCrosses).getWindow();

		this.mockRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.doReturn(new String[] {"not_a_valid_id"}).when(this.mockRequest)
				.getParameterValues(BreedingManagerApplication.REQ_PARAM_NURSERY_ID);
		Mockito.doReturn(new String[] {CrossingManagerMakeCrossesComponentTest.LIST_ID}).when(this.mockRequest)
				.getParameterValues(BreedingManagerApplication.REQ_PARAM_LIST_ID);
		Mockito.doReturn(CrossingManagerMakeCrossesComponentTest.LOCALHOST).when(this.mockRequest).getServerName();
		Mockito.doReturn(CrossingManagerMakeCrossesComponentTest.PORT).when(this.mockRequest).getServerPort();
		Mockito.doReturn(CrossingManagerMakeCrossesComponentTest.HTTP).when(this.mockRequest).getScheme();
	}

	@Test
	public void testShowNotificationAfterCrossing_WhenMakeCrossesTableHasNoEntry() {
		this.makeCrosses.showNotificationAfterCrossing(0);
		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.NO_CROSSES_GENERATED);
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting show a notification message but didn't.");
		}
	}

	@Test
	public void testShowNotificationAfterCrossing_WhenMakeCrossesTableHasEntry() {
		this.makeCrosses.showNotificationAfterCrossing(1);
		try {
			Mockito.verify(this.messageSource, Mockito.times(0)).getMessage(Message.NO_CROSSES_GENERATED);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail("Expecting not show a notification message but didn't.");
		}
	}

	@Test
	public void testConstructNurseryCancelButton_Edit() {
		this.makeCrosses.setNurseryId(CrossingManagerMakeCrossesComponentTest.NURSERY_ID);

		final LinkButton buttonToEditNursery = this.makeCrosses.constructNurseryCancelButton();

		Assert.assertEquals(
				"http://" + CrossingManagerMakeCrossesComponentTest.LOCALHOST + ":" + CrossingManagerMakeCrossesComponentTest.PORT
						+ BreedingManagerApplication.PATH_TO_EDIT_NURSERY + CrossingManagerMakeCrossesComponentTest.NURSERY_ID,
				buttonToEditNursery.getResource().getURL());
		Assert.assertEquals("Nursery ID must be set from query parameter.", CrossingManagerMakeCrossesComponentTest.NURSERY_ID,
				this.makeCrosses.getNurseryId());
	}

	@Test
	public void testConstructNurseryCancelButton_Create() {
		final LinkButton buttonToCreateNursery = this.makeCrosses.constructNurseryCancelButton();

		Assert.assertEquals("http://" + CrossingManagerMakeCrossesComponentTest.LOCALHOST + ":"
				+ CrossingManagerMakeCrossesComponentTest.PORT + BreedingManagerApplication.PATH_TO_NURSERY,
				buttonToCreateNursery.getResource().getURL());
	}

	@Test
	public void testConstructNurseryBackButton() {
		final Button testNurseryBackButton = this.makeCrosses.constructNurseryBackButton();

		Assert.assertNotNull(testNurseryBackButton);
		Assert.assertEquals("Return to Nursery", testNurseryBackButton.getCaption());
		Assert.assertNotNull(testNurseryBackButton.getListeners(Button.ClickEvent.class));
		Assert.assertEquals(1, testNurseryBackButton.getListeners(Button.ClickEvent.class).size());
	}

	@Test
	public void testInitializeNurseryContext() {
		Mockito.doReturn(new String[] {CrossingManagerMakeCrossesComponentTest.NURSERY_ID}).when(this.mockRequest)
				.getParameterValues(BreedingManagerApplication.REQ_PARAM_NURSERY_ID);
		Mockito.when(this.mockRequest.getPathInfo()).thenReturn("/BreedingManager/createcrosses");

		final Workbook testWorkbook = new Workbook();
		Mockito.when(this.fieldbookMiddlewareService.getNurseryDataSet(Integer.valueOf(CrossingManagerMakeCrossesComponentTest.NURSERY_ID)))
				.thenReturn(testWorkbook);

		this.makeCrosses.initializeNurseryContext(this.mockRequest);
		Assert.assertNotNull("Expect nurseryId to be initialized.", this.makeCrosses.getNurseryId());
		Assert.assertNotNull("Expect nurseryWorkbook to be initialized.", this.makeCrosses.getNurseryWorkbook());
		Assert.assertTrue("Expected isNavigatedFromNursery flag to be set to true.", this.makeCrosses.isNavigatedFromNursery());
		Mockito.verify(this.fieldbookMiddlewareService)
				.getNurseryDataSet(Matchers.eq(Integer.valueOf(CrossingManagerMakeCrossesComponentTest.NURSERY_ID)));
	}

	@Test
	public void testBackNavigationControlsWhenComingFromNursery() {

		// Setup Mocks
		this.makeCrosses.setSelectParentsComponent(Mockito.mock(SelectParentsComponent.class));
		this.makeCrosses.setParentsComponent(Mockito.mock(MakeCrossesParentsComponent.class));
		this.makeCrosses.setCrossingMethodComponent(Mockito.mock(CrossingMethodComponent.class));
		this.makeCrosses.setCrossesTableComponent(Mockito.mock(MakeCrossesTableComponent.class));

		Mockito.doReturn(new LinkButton(new ExternalResource("url"), "Back")).when(this.makeCrosses)
				.constructNurseryCancelButton();

		// Set "from nursery" flag to true
		this.makeCrosses.setNavigatedFromNursery(true);

		// Layout components
		this.makeCrosses.layoutComponents();

		// Expect cancel and back to nursery buttons to be initialized (non-null)
		Assert.assertNotNull("Expecting cancel button initialized when navigating to crossing manager from a Nursery.",
				this.makeCrosses.getNurseryCancelButton());
		Assert.assertNotNull("Expecting nursery back button initialized when navigating to crossing manager from a Nursery.",
				this.makeCrosses.getNurseryBackButton());
	}
	
	@Test
	public void testSendToNurseryAction() {
		LinkButton nurseryCancelButton = Mockito.mock(LinkButton.class);
		ExternalResource externalResource = Mockito.mock(ExternalResource.class);
		Mockito.when(nurseryCancelButton.getResource()).thenReturn(externalResource);
		Mockito.when(externalResource.getURL()).thenReturn("url");
		this.makeCrosses.setNurseryCancelButton(nurseryCancelButton);
		this.makeCrosses.sendToNurseryAction(1);
		Mockito.verify(nurseryCancelButton).getResource();
		Mockito.verify(externalResource).getURL();
		Mockito.verify(this.window).open(Matchers.any(ExternalResource.class), Matchers.anyString());
	}
}
