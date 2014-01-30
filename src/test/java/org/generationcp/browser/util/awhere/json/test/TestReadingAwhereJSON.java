package org.generationcp.browser.util.awhere.json.test;

import junit.framework.Assert;

import org.generationcp.browser.util.awhere.json.pojos.SeasonProfileResult;
import org.junit.Test;

import com.google.gson.Gson;

public class TestReadingAwhereJSON {

	@Test
	public void test() {
		Gson gson = new Gson();
		//from sample in http://confluence.efficio.us.com/display/GCP/aWhere+Location+Information+-+User+Stories+and+API+Integration
		String toread = "{\"SeasonProfile\":\"JSON\",\"LocationName\":\"Machakos\",\"LocationCoordinates\":\"Latitude=-1.5089, Longitude=37.2947\"," +
				"\"Season_Profile\":[{\"PeriodFrom\":\"01-03-2013\",\"PeriodTo\":\"23-03-2013\",\"Avg_MinTemp\":\"16.2508695652174\",\"Avg_MaxTemp\":\"28.3230434782609\"," +
				"\"Avg_Solar\":\"5285.76695652174\",\"Avg_Rain\":\"0\",\"Total_GDD\":282.59999999999997,\"Count\":23},{\"PeriodFrom\":\"23-03-2013\",\"PeriodTo\":\"13-04-2013\"," +
				"\"Avg_MinTemp\":\"16\",\"Avg_MaxTemp\":\"25.8613636363636\",\"Avg_Solar\":\"4672.96454545455\",\"Avg_Rain\":\"5.60863636363636\",\"Total_GDD\":240.47499999999991," +
				"\"Count\":22},{\"PeriodFrom\":\"13-04-2013\",\"PeriodTo\":\"04-05-2013\",\"Avg_MinTemp\":\"14.6545454545455\",\"Avg_MaxTemp\":\"24.67\",\"Avg_Solar\":" +
				"\"5086.16318181818\",\"Avg_Rain\":\"0.986127727272728\",\"Total_GDD\":212.57000000000005,\"Count\":22},{\"PeriodFrom\":\"04-05-2013\",\"PeriodTo\":\"25-05-2013\"," +
				"\"Avg_MinTemp\":\"14.3140909090909\",\"Avg_MaxTemp\":\"24.2872727272727\",\"Avg_Solar\":\"4735.03318181818\",\"Avg_Rain\":\"0.644110954545455\",\"Total_GDD\":204.615," +
				"\"Count\":22},{\"PeriodFrom\":\"25-05-2013\",\"PeriodTo\":\"15-06-2013\",\"Avg_MinTemp\":\"13.3677272727273\",\"Avg_MaxTemp\":\"24.0581818181818\",\"Avg_Solar\":" +
				"\"4522.79909090909\",\"Avg_Rain\":\"0.0619569545454545\",\"Total_GDD\":191.685,\"Count\":22}],\"TenYearAverage\":[{\"PeriodFrom\":\"01-03-2004\",\"PeriodTo\":" +
				"\"23-03-2013\",\"Avg_MinTemp\":\"14.8228260869565\",\"Avg_MaxTemp\":\"27.77\",\"Avg_Solar\":\"5805.45065217391\",\"Avg_Rain\":\"0.803695652173913\"," +
				"\"Total_GDD\":155.89050000000003,\"Count\":138},{\"PeriodFrom\":\"23-03-2004\",\"PeriodTo\":\"13-04-2013\",\"Avg_MinTemp\":\"15.302196969697\",\"Avg_MaxTemp\":" +
				"\"26.1591666666667\",\"Avg_Solar\":\"5255.63962121212\",\"Avg_Rain\":\"1.90864015151515\",\"Total_GDD\":141.64499999999998,\"Count\":132},{\"PeriodFrom\":" +
				"\"13-04-2004\",\"PeriodTo\":\"04-05-2013\",\"Avg_MinTemp\":\"15.0831818181818\",\"Avg_MaxTemp\":\"24.8362878787879\",\"Avg_Solar\":\"4871.13128787879\"," +
				"\"Avg_Rain\":\"0.973569015151515\",\"Total_GDD\":131.4685,\"Count\":132},{\"PeriodFrom\":\"04-05-2004\",\"PeriodTo\":\"25-05-2013\",\"Avg_MinTemp\":" +
				"\"14.4481060606061\",\"Avg_MaxTemp\":\"23.820303030303\",\"Avg_Solar\":\"4595.39878787879\",\"Avg_Rain\":\"0.948690507575757\",\"Total_GDD\":120.60900000000001," +
				"\"Count\":132},{\"PeriodFrom\":\"25-05-2004\",\"PeriodTo\":\"15-06-2013\",\"Avg_MinTemp\":\"13.7876515151515\",\"Avg_MaxTemp\":\"23.552803030303\",\"Avg_Solar\":" +
				"\"4482.86431818182\",\"Avg_Rain\":\"0.0456914545454545\",\"Total_GDD\":114.447,\"Count\":132}],\"GDDModel\":\"Default Standard Model\"}";
		
		String expected = "SeasonProfileResult [seasonProfile=JSON, locationName=Machakos, locationCoordinates=Latitude=-1.5089, Longitude=37.2947, gDDModel=Default Standard Model, " +
				"season_Profile=[SeasonProfile [periodFrom=01-03-2013, periodTo=23-03-2013, avg_MinTemp=16.2508695652174, avg_MaxTemp=28.3230434782609, avg_Solar=5285.76695652174, " +
				"avg_Rain=0.0, total_GDD=282.59999999999997, count=23], SeasonProfile [periodFrom=23-03-2013, periodTo=13-04-2013, avg_MinTemp=16.0, avg_MaxTemp=25.8613636363636, " +
				"avg_Solar=4672.96454545455, avg_Rain=5.60863636363636, total_GDD=240.4749999999999, count=22], SeasonProfile [periodFrom=13-04-2013, periodTo=04-05-2013, " +
				"avg_MinTemp=14.6545454545455, avg_MaxTemp=24.67, avg_Solar=5086.16318181818, avg_Rain=0.986127727272728, total_GDD=212.57000000000005, count=22], " +
				"SeasonProfile [periodFrom=04-05-2013, periodTo=25-05-2013, avg_MinTemp=14.3140909090909, avg_MaxTemp=24.2872727272727, avg_Solar=4735.03318181818, " +
				"avg_Rain=0.644110954545455, total_GDD=204.615, count=22], SeasonProfile [periodFrom=25-05-2013, periodTo=15-06-2013, avg_MinTemp=13.3677272727273, " +
				"avg_MaxTemp=24.0581818181818, avg_Solar=4522.79909090909, avg_Rain=0.0619569545454545, total_GDD=191.685, count=22]], tenYearAverage=[TenYearAverage " +
				"[periodFrom=01-03-2004, periodTo=23-03-2013, avg_MinTemp=14.8228260869565, avg_MaxTemp=27.77, avg_Solar=5805.45065217391, avg_Rain=0.803695652173913, " +
				"total_GDD=155.89050000000003, count=138], TenYearAverage [periodFrom=23-03-2004, periodTo=13-04-2013, avg_MinTemp=15.302196969697, avg_MaxTemp=26.1591666666667, " +
				"avg_Solar=5255.63962121212, avg_Rain=1.90864015151515, total_GDD=141.64499999999998, count=132], TenYearAverage [periodFrom=13-04-2004, periodTo=04-05-2013, " +
				"avg_MinTemp=15.0831818181818, avg_MaxTemp=24.8362878787879, avg_Solar=4871.13128787879, avg_Rain=0.973569015151515, total_GDD=131.4685, count=132], " +
				"TenYearAverage [periodFrom=04-05-2004, periodTo=25-05-2013, avg_MinTemp=14.4481060606061, avg_MaxTemp=23.820303030303, avg_Solar=4595.39878787879, " +
				"avg_Rain=0.948690507575757, total_GDD=120.60900000000001, count=132], TenYearAverage [periodFrom=25-05-2004, periodTo=15-06-2013, avg_MinTemp=13.7876515151515, " +
				"avg_MaxTemp=23.552803030303, avg_Solar=4482.86431818182, avg_Rain=0.0456914545454545, total_GDD=114.447, count=132]]]";
		
		SeasonProfileResult fromReading = gson.fromJson(toread, SeasonProfileResult.class);
		Assert.assertEquals(expected, fromReading.toString());
	}

}
