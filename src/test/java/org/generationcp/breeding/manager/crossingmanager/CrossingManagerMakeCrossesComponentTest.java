
package org.generationcp.breeding.manager.crossingmanager;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customcomponent.LinkButton;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

import javax.servlet.http.HttpServletRequest;


public class CrossingManagerMakeCrossesComponentTest {

	public static final String STUDY_ID = "25019";
	public static final String LIST_ID = "38";

	public static final Integer USER_ID = 1;
	public static final Long PROJECT_ID = 2018L;
	public static final String AUTHTOKEN = "ABCDE";

	@Mock
	private ManageCrossingSettingsMain manageCrossingSettingsMain;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@Mock
	private Window window;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	private CrossingManagerMakeCrossesComponent makeCrosses;
	private HttpServletRequest mockRequest;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		this.makeCrosses = Mockito.spy(new CrossingManagerMakeCrossesComponent(this.manageCrossingSettingsMain));

		Mockito.doReturn("Return to Study").when(this.messageSource).getMessage(Message.BACK_TO_STUDY);
		this.makeCrosses.setMessageSource(this.messageSource);
		this.makeCrosses.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		Mockito.doReturn(this.window).when(this.makeCrosses).getWindow();

		this.mockRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.doReturn(new String[] {"not_a_valid_id"}).when(this.mockRequest)
				.getParameterValues(BreedingManagerApplication.REQ_PARAM_STUDY_ID);
		Mockito.doReturn(new String[] {CrossingManagerMakeCrossesComponentTest.LIST_ID}).when(this.mockRequest)
				.getParameterValues(BreedingManagerApplication.REQ_PARAM_LIST_ID);

		ContextInfo cxt = new ContextInfo(CrossingManagerMakeCrossesComponentTest.USER_ID,CrossingManagerMakeCrossesComponentTest.PROJECT_ID,CrossingManagerMakeCrossesComponentTest.AUTHTOKEN);
		Mockito.when(this.contextUtil.getContextInfoFromSession()).thenReturn(cxt);
		this.makeCrosses.setContextUtil(this.contextUtil);

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
	public void testConstructStudyCancelButton_Edit() {
		this.makeCrosses.setStudyId(CrossingManagerMakeCrossesComponentTest.STUDY_ID);
		final String aditionalParameters =
			"?restartApplication&loggedInUserId=" + CrossingManagerMakeCrossesComponentTest.USER_ID + "&selectedProjectId="
				+ CrossingManagerMakeCrossesComponentTest.PROJECT_ID + "&authToken=" + CrossingManagerMakeCrossesComponentTest.AUTHTOKEN;
		final LinkButton buttonToEditStudy = this.makeCrosses.constructStudyCancelButton();

		Assert.assertEquals(BreedingManagerApplication.URL_STUDY[0] + CrossingManagerMakeCrossesComponentTest.STUDY_ID + aditionalParameters
			+ BreedingManagerApplication.URL_STUDY[1], buttonToEditStudy.getResource().getURL());
		Assert.assertEquals("Study ID must be set from query parameter.", CrossingManagerMakeCrossesComponentTest.STUDY_ID,
			this.makeCrosses.getStudyId());
	}

	@Test
	public void testConstructStudyCancelButton_Create() {
		this.makeCrosses.setStudyId(CrossingManagerMakeCrossesComponentTest.STUDY_ID);
		final LinkButton buttonToCreateStudy = this.makeCrosses.constructStudyCancelButton();
		final String aditionalParameters =
			"?restartApplication&loggedInUserId=" + CrossingManagerMakeCrossesComponentTest.USER_ID + "&selectedProjectId="
				+ CrossingManagerMakeCrossesComponentTest.PROJECT_ID + "&authToken=" + CrossingManagerMakeCrossesComponentTest.AUTHTOKEN;
		Assert.assertEquals(BreedingManagerApplication.URL_STUDY[0] + CrossingManagerMakeCrossesComponentTest.STUDY_ID + aditionalParameters
			+ BreedingManagerApplication.URL_STUDY[1], buttonToCreateStudy.getResource().getURL());
	}

	@Test
	public void testConstructStudyBackButton() {
		final Button testStudyBackButton = this.makeCrosses.constructStudyBackButton();

		Assert.assertNotNull(testStudyBackButton);
		Assert.assertEquals("Return to Study", testStudyBackButton.getCaption());
		Assert.assertNotNull(testStudyBackButton.getListeners(Button.ClickEvent.class));
		Assert.assertEquals(1, testStudyBackButton.getListeners(Button.ClickEvent.class).size());
	}

	@Test
	public void testInitializeStudyContext() {
		Mockito.doReturn(new String[] {CrossingManagerMakeCrossesComponentTest.STUDY_ID}).when(this.mockRequest)
			.getParameterValues(BreedingManagerApplication.REQ_PARAM_STUDY_ID);
		Mockito.when(this.mockRequest.getPathInfo()).thenReturn("/BreedingManager/createcrosses");

		final Workbook testWorkbook = new Workbook();
		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(Integer.valueOf(CrossingManagerMakeCrossesComponentTest.STUDY_ID)))
			.thenReturn(testWorkbook);

		this.makeCrosses.initializeStudyContext(this.mockRequest);
		Assert.assertNotNull("Expect StudyId to be initialized.", this.makeCrosses.getStudyId());
		Assert.assertNotNull("Expect StudyWorkbook to be initialized.", this.makeCrosses.getWorkbook());
		Assert.assertTrue("Expected isNavigatedFromStudy flag to be set to true.", this.makeCrosses.isNavigatedFromStudy());
		Mockito.verify(this.fieldbookMiddlewareService)
			.getStudyDataSet(Matchers.eq(Integer.valueOf(CrossingManagerMakeCrossesComponentTest.STUDY_ID)));
	}

	@Test
	public void testBackNavigationControlsWhenComingFromNursery() {

		// Setup Mocks
		this.makeCrosses.setSelectParentsComponent(Mockito.mock(SelectParentsComponent.class));
		this.makeCrosses.setParentsComponent(Mockito.mock(MakeCrossesParentsComponent.class));
		this.makeCrosses.setCrossingMethodComponent(Mockito.mock(CrossingMethodComponent.class));
		this.makeCrosses.setCrossesTableComponent(Mockito.mock(MakeCrossesTableComponent.class));

		Mockito.doReturn(new LinkButton(new ExternalResource("url"), "Back")).when(this.makeCrosses).constructStudyCancelButton();

		// Set "from nursery" flag to true
		this.makeCrosses.setNavigatedFromStudy(true);

		// Layout components
		this.makeCrosses.layoutComponents();

		// Expect cancel and back to nursery buttons to be initialized (non-null)
		Assert.assertNotNull("Expecting cancel button initialized when navigating to crossing manager from a Study.",
			this.makeCrosses.getStudyCancelButton());
		Assert.assertNotNull("Expecting nursery back button initialized when navigating to crossing manager from a Study.",
			this.makeCrosses.getStudyBackButton());
	}

	@Test
	@Ignore("Fix this Test")
	public void testSendToStudyAction() {
		LinkButton studyCancelButton = Mockito.mock(LinkButton.class);
		ExternalResource externalResource = Mockito.mock(ExternalResource.class);
		Mockito.when(studyCancelButton.getResource()).thenReturn(externalResource);
		Mockito.when(externalResource.getURL()).thenReturn("url");
		this.makeCrosses.setStudyCancelButton(studyCancelButton);
		this.makeCrosses.sendToNurseryAction(1);
		Mockito.verify(studyCancelButton).getResource();
		Mockito.verify(externalResource).getURL();
		Mockito.verify(this.window).open(Matchers.any(ExternalResource.class), Matchers.anyString());
	}
}
