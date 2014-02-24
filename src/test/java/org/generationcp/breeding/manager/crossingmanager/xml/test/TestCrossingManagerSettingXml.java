package org.generationcp.breeding.manager.crossingmanager.xml.test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

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
		BreedingMethodSetting breedingMethodSetting = new BreedingMethodSetting(Integer.valueOf(104), false, true);
		CrossNameSetting crossNameSetting = new CrossNameSetting("IR", null, true, Integer.valueOf(5));
		AdditionalDetailsSetting additionalDetailsSetting = new AdditionalDetailsSetting(Integer.valueOf(1000), new Date(1000000000));
		CrossingManagerSetting crossingManagerSetting = new CrossingManagerSetting("sample", breedingMethodSetting, crossNameSetting, additionalDetailsSetting);
		
		JAXBContext context = JAXBContext.newInstance(CrossingManagerSetting.class);
        Marshaller marshaller = context.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(crossingManagerSetting, writer);
        
        String xmlToRead = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><crossingManagerSetting name=\"sample\"><additionalDetailsSetting " +
        		"harvestLocationId=\"1000\" harvestDate=\"1970-01-12T21:46:40+08:00\"/><breedingMethodSetting useAMethodForAllCrosses=\"true\" methodId=\"104\" " +
        		"basedOnStatusOfParentalLines=\"false\"/><crossNameSetting prefix=\"IR\" numOfDigits=\"5\" addSpaceBetweenPrefixAndCode=\"true\"/></crossingManagerSetting>";
        Unmarshaller unmarshaller = context.createUnmarshaller();
        CrossingManagerSetting parsedSetting = (CrossingManagerSetting) unmarshaller.unmarshal(new StringReader(xmlToRead));
        Assert.assertEquals(crossingManagerSetting, parsedSetting);
	}

}
