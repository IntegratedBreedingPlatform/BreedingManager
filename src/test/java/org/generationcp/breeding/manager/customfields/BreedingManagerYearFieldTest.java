
package org.generationcp.breeding.manager.customfields;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

public class BreedingManagerYearFieldTest {

	private static final Integer RANGE_INTERVAL_FROM_BASE_YEAR = 30;

	@Test
	public void testHarvestYearFieldHasMinOf30YearsEarlierThanTheCurrentYear() {

		Calendar calendar = new GregorianCalendar();
		Integer currentYear = calendar.get(Calendar.YEAR);
		BreedingManagerYearField yearField = new BreedingManagerYearField(currentYear);

		Integer expectedMinYear = currentYear - BreedingManagerYearFieldTest.RANGE_INTERVAL_FROM_BASE_YEAR;
		Collection<?> yearOptions = yearField.getItemIds();
		Assert.assertTrue("Expecting that the year field given the base year is the current year, " + currentYear + ", has "
				+ expectedMinYear + " for " + "minimum year.", yearOptions.contains(expectedMinYear));
	}

	@Test
	public void testHarvestYearFieldHasMaxOf30YearsLaterThanTheCurrentYear() {
		Calendar calendar = new GregorianCalendar();
		Integer currentYear = calendar.get(Calendar.YEAR);
		BreedingManagerYearField yearField = new BreedingManagerYearField(currentYear);

		Integer expectedMaxYear = currentYear + BreedingManagerYearFieldTest.RANGE_INTERVAL_FROM_BASE_YEAR;
		Collection<?> yearOptions = yearField.getItemIds();
		Assert.assertTrue("Expecting that the year field given the base year is the current year, " + currentYear + ", has "
				+ expectedMaxYear + " for " + "maximum year.", yearOptions.contains(expectedMaxYear));
	}
}
