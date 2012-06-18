/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the 
 * GNU General Public License (http://bit.ly/8Ztv8M) and the 
 * provisions of Part F of the Generation Challenge Programme 
 * Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/
package org.generationcp.browser.germplasm.table.indexcontainer;

import java.util.ArrayList;

import org.generationcp.browser.germplasm.datasource.helper.TraitQueries;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.generationcp.middleware.pojos.Scale;
import org.generationcp.middleware.pojos.ScaleDiscrete;
import org.generationcp.middleware.pojos.Trait;
import org.generationcp.middleware.pojos.TraitMethod;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.CheckBox;

public class TraitDataIndexContainer {


	public static final Object iso3166_PROPERTY_SHORT = "short";
	public static final Object iso3166_PROPERTY_NAME = "name";

	//Trait Object
	private static final Object TRAIT_ID= "traitID";
	private static final Object TRAIT_ABBR = "trabbr";
	private static final Object TRAIT_NAME = "traitName";
	private static final Object TRAIT_DESCRIPTION = "traitDesc";

	//Scale Object
	private static final Object SCALE_ID= "scaleID";
	private static final Object SCALE_NAME = "scaleName";
	private static final Object SCALE_TYPE = "scaleType";

	//Method Object
	private static final Object METHOD_ID= "methodID";
	private static final Object METHOD_NAME = "methodName";
	private static final Object METHOD_DESCRIPTION = "methodDescription";
	
	//ScaleDiscrete Value

	private static final Object SCALE_VALUE = "scaleValue";
	private static final Object SCALE_DISCRETE_VALUE = "scaleDiscreteValue";
	private static final Object SCALE_SELECTED = "select";
	private static final Object SCALE_VALUE_DESCRIPTION = "Value";
	
	//Criteria Value
	private static final Object CRITERIA_VALUES = "criteriaValue";
	TraitQueries queryTrait;


	//Results GIDS
	private static final Object GID = "gid";
	
	
	public TraitDataIndexContainer(ManagerFactory factory,TraitDataManager managerTrait){
		
		queryTrait= new TraitQueries(factory,managerTrait);
	}

	public IndexedContainer getAllTrait() throws QueryException {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		container.addContainerProperty(TRAIT_ID,Integer.class, "");
		container.addContainerProperty(TRAIT_NAME,String.class, "");
		container.addContainerProperty(TRAIT_DESCRIPTION,String.class, "");

		ArrayList<Trait> query = queryTrait.getTrait();

		for(Trait t: query){
			addTraitData(container,t.getTraitId(),t.getAbbreviation(),t.getName(),t.getDescripton());
		}
		return container;
	}


	private static void addTraitData(Container container,
			int traitID,String trabbr,String traitName,String traitDesc ) {
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		item.getItemProperty(TRAIT_ID).setValue(traitID);
		item.getItemProperty(TRAIT_NAME).setValue(traitName);
		item.getItemProperty(TRAIT_DESCRIPTION).setValue(traitDesc);

	}

	public IndexedContainer getScaleByTraitID(int traitID) {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		container.addContainerProperty(SCALE_ID,Integer.class, "");
		container.addContainerProperty(SCALE_NAME,String.class, "");
		container.addContainerProperty(SCALE_TYPE,String.class, "");

		ArrayList<Scale> query = queryTrait.getScale(traitID);

		for(Scale s: query){
			//addTraitData(container,t.getId(),t.getName(),t.getDescripton());
			String type = "discrete";
			if(s.getType().equals("C"))
				type = "continuous";
			addScaleData(container,s.getId(),s.getName(),type);
		}
		return container;
	}


	private static void addScaleData(Container container,
			int scaleID,String scaleName,String scaleType ) {
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		item.getItemProperty(SCALE_ID).setValue(scaleID);
		item.getItemProperty(SCALE_NAME).setValue(scaleName);
		item.getItemProperty(SCALE_TYPE).setValue(scaleType);

	}



	public IndexedContainer getMethodTraitID(int traitID) {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		container.addContainerProperty(METHOD_ID,Integer.class, "");
		container.addContainerProperty(METHOD_NAME,String.class, "");
		container.addContainerProperty(METHOD_DESCRIPTION,String.class, "");

		ArrayList<TraitMethod> query = queryTrait.getTraitMethod(traitID);
		
		for(TraitMethod m: query){
			//addTraitData(container,t.getId(),t.getName(),t.getDescripton());
			addMethodData(container,m.getId(),m.getName(),m.getDescription());
		}
		return container;
	}


	private static void addMethodData(Container container,
			int methodID,String methodName,String methodDescription ) {
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		item.getItemProperty(METHOD_ID).setValue(methodID);
		item.getItemProperty(METHOD_NAME).setValue(methodName);
		item.getItemProperty(METHOD_DESCRIPTION).setValue(methodDescription);

	}
	
	public IndexedContainer getValueByScaleID(int scaleID) {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		container.addContainerProperty(SCALE_SELECTED,CheckBox.class, "");
		container.addContainerProperty(SCALE_VALUE_DESCRIPTION,String.class, "");
		container.addContainerProperty(SCALE_VALUE, String.class, "");

		final ArrayList<ScaleDiscrete> query = queryTrait.getScaleDiscreteValue(scaleID);

		for(ScaleDiscrete v: query){
			addScaleValueData(container,v.getValueDescription(), v.getId().getValue());
		}
		return container;
	}


	private static void addScaleValueData(Container container,
			String scaleDescription, String scaleValue ) {
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		boolean  activ = false;
		item.getItemProperty(SCALE_SELECTED).setValue(new CheckBox(null,activ));
		item.getItemProperty(SCALE_VALUE_DESCRIPTION).setValue(scaleDescription);
		item.getItemProperty(SCALE_VALUE).setValue(scaleValue);

	}
	
	
	public IndexedContainer addSearchCriteria() {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		container.addContainerProperty(TRAIT_ID,Integer.class, "");
		container.addContainerProperty(SCALE_ID,Integer.class, "");
		container.addContainerProperty(METHOD_ID,Integer.class, "");
		container.addContainerProperty(TRAIT_NAME,String.class, "");
		container.addContainerProperty(SCALE_NAME,String.class, "");
		container.addContainerProperty(METHOD_NAME,String.class, "");
		container.addContainerProperty(CRITERIA_VALUES,String.class, "");
		container.addContainerProperty(SCALE_TYPE,String.class, "");
		container.addContainerProperty(SCALE_DISCRETE_VALUE, String.class, "");

		return container;
	}

	public IndexedContainer addGidsResult(ArrayList<Integer> gids) {
		IndexedContainer container = new IndexedContainer();

		// Create the container properties
		container.addContainerProperty(GID,Integer.class, "");
		
		for(Integer gid: gids){
			//addTraitData(container,t.getId(),t.getName(),t.getDescripton());
			addGids(container,gid);
		}
		return container;
	}
	
	private static void addGids(Container container,
			Integer gid ) {
		Object itemId = container.addItem();
		Item item = container.getItem(itemId);
		item.getItemProperty(GID).setValue(gid);

	}

}
