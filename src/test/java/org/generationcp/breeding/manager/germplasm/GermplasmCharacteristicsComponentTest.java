
package org.generationcp.breeding.manager.germplasm;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.vaadin.ui.CheckBox;

public class GermplasmCharacteristicsComponentTest {

	private static final int MGID = 1;

	private GermplasmDetailModel gDetailModel;

	@Mock
	private SimpleResourceBundleMessageSource messageSource;

	private GermplasmCharacteristicsComponent component;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		this.initializeDetailModel();
		this.component = new GermplasmCharacteristicsComponent(this.gDetailModel);
		this.component.setMessageSource(this.messageSource);
		this.component.instantiateComponents();

		Mockito.doReturn("DummyLable").when(this.messageSource).getMessage(Mockito.any(Message.class));
	}

	private void initializeDetailModel() {
		this.gDetailModel = new GermplasmDetailModel();
		this.gDetailModel.setmGid(MGID);
	}

	@Test
	public void testInitializeFixedLineIsCheckedIfMgidGreaterThanZero() {
		final CheckBox chkFixedLines = new CheckBox();

		this.component.initializeFixedLine(chkFixedLines, 1);

		Assert.assertTrue("Expecting that the fixed line check box is set to readonly.", chkFixedLines.isReadOnly());
		Assert.assertTrue("Expecting that the fixed line check box is check when mgid is greater that zero.",
				Boolean.valueOf(chkFixedLines.getValue().toString()));
	}

	@Test
	public void testInitializeFixedLineIsUnCheckedIfMgidEqualToZero() {
		final CheckBox chkFixedLines = new CheckBox();

		this.component.initializeFixedLine(chkFixedLines, 0);

		Assert.assertTrue("Expecting that the fixed line check box is set to readonly.", chkFixedLines.isReadOnly());
		Assert.assertFalse("Expecting that the fixed line check box is uncheck when mgid is greater that zero.",
				Boolean.valueOf(chkFixedLines.getValue().toString()));
	}
}
