
package org.generationcp.breeding.manager.crossingmanager.settings;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.VerticalLayout;

public class ManageCrossingSettingsMainTest {

	private VerticalLayout parent;
	private ManageCrossingSettingsMain crossingSettingsMain;

	@Before
	public void setUp() {
		this.parent = new VerticalLayout();

		this.crossingSettingsMain = new ManageCrossingSettingsMain(this.parent);
		this.parent.addComponent(this.crossingSettingsMain);
	}

	@Test
	public void testReset() {
		int index;

		index = this.parent.getComponentIndex(this.crossingSettingsMain);
		ManageCrossingSettingsMain crossingSettingsMainBeforeReset = (ManageCrossingSettingsMain) this.parent.getComponent(index);

		this.crossingSettingsMain.reset();

		Assert.assertTrue("Expecting the parent layout contains only 1 component, ManagerCrossingSettingsMain",
				this.parent.getComponentCount() == 1);

		index = this.parent.getComponentIndex(this.parent.getComponent(0));
		ManageCrossingSettingsMain crossingSettingsMainAfterReset = (ManageCrossingSettingsMain) this.parent.getComponent(index);

		Assert.assertNotSame("Expected two different ManagerCrossingSettingsMain instances here but didn't",
				crossingSettingsMainBeforeReset, crossingSettingsMainAfterReset);
	}

}
