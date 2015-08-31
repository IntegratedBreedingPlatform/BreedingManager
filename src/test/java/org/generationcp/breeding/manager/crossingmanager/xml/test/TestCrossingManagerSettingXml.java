
package org.generationcp.breeding.manager.crossingmanager.xml.test;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.Assert;

import org.generationcp.breeding.manager.crossingmanager.xml.AdditionalDetailsSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.BreedingMethodSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossNameSetting;
import org.generationcp.breeding.manager.crossingmanager.xml.CrossingManagerSetting;
import org.junit.Test;

public class TestCrossingManagerSettingXml {

	@Test
	public void test() throws Exception {
		BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting(Integer.valueOf(104), false);
		CrossNameSetting crossNameSetting =
				new CrossNameSetting("IR", "M", true, true, Integer.valueOf(5), CrossNameSetting.DEFAULT_SEPARATOR, true);
		AdditionalDetailsSetting additionalDetailsSetting = new AdditionalDetailsSetting(Integer.valueOf(1000), "20140500");
		CrossingManagerSetting crossingManagerSetting =
				new CrossingManagerSetting("sample", breedingMethodSetting, crossNameSetting, additionalDetailsSetting);

		JAXBContext context = JAXBContext.newInstance(CrossingManagerSetting.class);
		Marshaller marshaller = context.createMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(crossingManagerSetting, writer);

		String xmlToRead =
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
						+ "<crossingManagerSetting name=\"sample\">"
						+ "<additionalDetailsSetting harvestLocationId=\"1000\" harvestDate=\"20140500\"/>"
						+ "<breedingMethodSetting methodId=\"104\" basedOnStatusOfParentalLines=\"false\"/>"
						+ "<crossNameSetting prefix=\"IR\" suffix=\"M\" numOfDigits=\"5\" "
						+ "			addSpaceBetweenPrefixAndCode=\"true\" addSpaceBetweenSuffixAndCode=\"true\" saveParentageDesignationAsAString=\"true\" separator=\"/\"/>"
						+ "</crossingManagerSetting>";
		Unmarshaller unmarshaller = context.createUnmarshaller();
		CrossingManagerSetting parsedSetting = (CrossingManagerSetting) unmarshaller.unmarshal(new StringReader(xmlToRead));
		Assert.assertEquals(crossingManagerSetting, parsedSetting);
	}

}
