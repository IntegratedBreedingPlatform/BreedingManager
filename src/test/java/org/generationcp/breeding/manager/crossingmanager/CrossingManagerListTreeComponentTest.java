package org.generationcp.breeding.manager.crossingmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class CrossingManagerListTreeComponentTest {

	public static final String CANCEL = "Cancel";
	public static final String OPEN_FOR_REVIEW = "Open for review";
	public static final String ADD_TO_FEMALE = "Add to Female";
	public static final String ADD_TO_MALE = "Add to Male";

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@Mock
	private ContextUtil util;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	@InjectMocks
	private final CrossingManagerListTreeComponent crossingManagerListTreeComponent = new CrossingManagerListTreeComponent(null);

	@Before
	public void init() {

		Mockito.when(messageSource.getMessage(Message.CANCEL)).thenReturn(CANCEL);
		Mockito.when(messageSource.getMessage(Message.DIALOG_OPEN_FOR_REVIEW_LABEL)).thenReturn(OPEN_FOR_REVIEW);
		Mockito.when(messageSource.getMessage(Message.DIALOG_ADD_TO_FEMALE_LABEL)).thenReturn(ADD_TO_FEMALE);
		Mockito.when(messageSource.getMessage(Message.DIALOG_ADD_TO_MALE_LABEL)).thenReturn(ADD_TO_MALE);

	}

	@Test
	public void testInstantiateComponents() {

		crossingManagerListTreeComponent.instantiateComponents();

		// The height of the component should always be 580 pixels
		Assert.assertEquals(580.0f, crossingManagerListTreeComponent.getHeight());
		Assert.assertEquals(0, crossingManagerListTreeComponent.getHeightUnits());

		// Make sure the buttons are initialized
		Assert.assertNotNull(crossingManagerListTreeComponent.getAddToFemaleListButton());
		Assert.assertNotNull(crossingManagerListTreeComponent.getAddToMaleListButton());
		Assert.assertNotNull(crossingManagerListTreeComponent.getCancelButton());
		Assert.assertNotNull(crossingManagerListTreeComponent.getOpenForReviewButton());

		Assert.assertFalse(crossingManagerListTreeComponent.getAddToFemaleListButton().isEnabled());
		Assert.assertFalse(crossingManagerListTreeComponent.getAddToMaleListButton().isEnabled());
		Assert.assertFalse(crossingManagerListTreeComponent.getOpenForReviewButton().isEnabled());

		Assert.assertEquals(ADD_TO_FEMALE, crossingManagerListTreeComponent.getAddToFemaleListButton().getCaption());
		Assert.assertEquals(ADD_TO_MALE, crossingManagerListTreeComponent.getAddToMaleListButton().getCaption());
		Assert.assertEquals(CANCEL, crossingManagerListTreeComponent.getCancelButton().getCaption());
		Assert.assertEquals(OPEN_FOR_REVIEW, crossingManagerListTreeComponent.getOpenForReviewButton().getCaption());

	}

}
