
package org.generationcp.breeding.manager.util;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ibp.test.utilities.TestGetterAndSetter;

/**
 * A helper test to test getter and setter solely for reducing noise in the test coverage.
 *
 */
public class BreedingManagerGetterAndSetterTests extends TestSuite  {

	public static Test suite() {
		final TestGetterAndSetter TestGetterAndSetter = new TestGetterAndSetter();
		return TestGetterAndSetter.getTestSuite("BreedingManagerGetterAndSetterTests", "org.generationcp.breeding.manager");
	}

}
