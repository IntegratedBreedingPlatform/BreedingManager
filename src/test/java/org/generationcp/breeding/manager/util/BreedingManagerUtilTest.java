
package org.generationcp.breeding.manager.util;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.generationcp.middleware.pojos.Location;
import org.junit.Test;

public class BreedingManagerUtilTest {

	@Test
	public void testGetLocationNameDisplayWhenAbbrIsNull() {
		Location loc = new Location();
		String lname = "Test";
		String labbr = null;

		loc.setLname(lname);
		loc.setLabbr(labbr);
		Assert.assertEquals("Abbreviation should not be appended", lname, BreedingManagerUtil.getLocationNameDisplay(loc));
	}

	@Test
	public void testGetLocationNameDisplayWhenAbbrIsNotNull() {
		Location loc = new Location();
		String lname = "Test";
		String labbr = "Abbr";

		loc.setLname(lname);
		loc.setLabbr(labbr);
		Assert.assertEquals("Abbreviation should be appended", lname + " - (" + labbr + ")",
				BreedingManagerUtil.getLocationNameDisplay(loc));
	}

	@Test
	public void testGetCurrentApplication() {
		HttpServletRequest req = BreedingManagerUtil.getApplicationRequest();
		Assert.assertNull("Request should be null since there is not application context", req);
	}
}
