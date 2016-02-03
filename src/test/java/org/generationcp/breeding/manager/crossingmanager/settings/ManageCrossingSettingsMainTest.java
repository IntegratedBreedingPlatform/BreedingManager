
package org.generationcp.breeding.manager.crossingmanager.settings;

import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

public class ManageCrossingSettingsMainTest {

	private Window parent;
	private ManageCrossingSettingsMain crossingSettingsMain;

	@Before
	public void setUp() {
		this.parent = new Window();

		this.crossingSettingsMain = new ManageCrossingSettingsMain(this.parent);
		this.parent.setContent(this.crossingSettingsMain);
	}

	@Test
	public void testReset() {
		final ManageCrossingSettingsMain manageCrossingSettingsMainBeforeReset = this.crossingSettingsMain;

		this.crossingSettingsMain.reset();

		final Iterator<Component> componentIterator = this.parent.getContent().getComponentIterator();
		Assert.assertTrue("Expecting the parent layout contains only 1 component, ManagerCrossingSettingsMain", componentIterator.hasNext());
		componentIterator.next();
		Assert.assertFalse("Expecting the parent layout contains only 1 component, ManagerCrossingSettingsMain",
				componentIterator.hasNext());

		Assert.assertNotSame("Expected two different ManagerCrossingSettingsMain instances here but didn't",
				manageCrossingSettingsMainBeforeReset, (ManageCrossingSettingsMain) this.parent.getContent().getComponentIterator().next());
	}

}
