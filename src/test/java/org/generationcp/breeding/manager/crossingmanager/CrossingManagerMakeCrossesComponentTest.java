
package org.generationcp.breeding.manager.crossingmanager;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.settings.ManageCrossingSettingsMain;
import org.generationcp.breeding.manager.customcomponent.LinkButton;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.springframework.mock.web.MockHttpServletRequest;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Link;
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

	private CrossingManagerMakeCrossesComponent makeCrosses;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		this.makeCrosses = Mockito.spy(new CrossingManagerMakeCrossesComponent(this.manageCrossingSettingsMain));
		this.makeCrosses.setMessageSource(this.messageSource);
		Mockito.doReturn(this.window).when(this.makeCrosses).getWindow();
	}

	@Test
	public void testShowNotificationAfterCrossing_WhenMakeCrossesTableHasNoEntry() {
		this.makeCrosses.showNotificationAfterCrossing(0);
		try {
			Mockito.verify(this.messageSource, Mockito.times(1)).getMessage(Message.NO_CROSSES_GENERATED);
		} catch (TooLittleActualInvocations e) {
			Assert.fail("Expecting show a notification message but didn't.");
		}
	}

	@Test
	public void testShowNotificationAfterCrossing_WhenMakeCrossesTableHasEntry() {
		this.makeCrosses.showNotificationAfterCrossing(1);
		try {
			Mockito.verify(this.messageSource, Mockito.times(0)).getMessage(Message.NO_CROSSES_GENERATED);
		} catch (NeverWantedButInvoked e) {
			Assert.fail("Expecting not show a notification message but didn't.");
		}
	}

	@Test
	public void testConstructNurseryCancelButton_Edit() {
		HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.doReturn(new String[]{NURSERY_ID}).when(mockRequest).getParameterValues(BreedingManagerApplication.REQ_PARAM_NURSERY_ID);
		Mockito.doReturn(new String[]{LIST_ID}).when(mockRequest).getParameterValues(BreedingManagerApplication.REQ_PARAM_LIST_ID);
		Mockito.doReturn(LOCALHOST).when(mockRequest).getServerName();
		Mockito.doReturn(PORT).when(mockRequest).getServerPort();
		Mockito.doReturn(HTTP).when(mockRequest).getScheme();

		final LinkButton linkToEditNursery = this.makeCrosses.constructNurseryCancelButton(mockRequest);

		Assert.assertEquals("http://" + LOCALHOST + ":" + PORT + BreedingManagerApplication.PATH_TO_EDIT_NURSERY + NURSERY_ID,
				((ExternalResource) linkToEditNursery.getResource()).getURL());
	}

	@Test
	public void testConstructNurseryCancelButton_Create() {
		HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.doReturn(new String[]{"not_a_valid_id"}).when(mockRequest).getParameterValues(BreedingManagerApplication
				.REQ_PARAM_NURSERY_ID);
		Mockito.doReturn(new String[]{LIST_ID}).when(mockRequest).getParameterValues(BreedingManagerApplication.REQ_PARAM_LIST_ID);
		Mockito.doReturn(LOCALHOST).when(mockRequest).getServerName();
		Mockito.doReturn(PORT).when(mockRequest).getServerPort();
		Mockito.doReturn(HTTP).when(mockRequest).getScheme();

		final LinkButton linkToCreateNursery = this.makeCrosses.constructNurseryCancelButton(mockRequest);

		Assert.assertEquals("http://" + LOCALHOST + ":" + PORT + BreedingManagerApplication.PATH_TO_NURSERY,
				((ExternalResource) linkToCreateNursery.getResource()).getURL());
	}
}
