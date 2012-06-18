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
package org.generationcp.browser.germplasm.datasource.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.TraitDataManager;
import org.generationcp.middleware.pojos.NumericRange;
import org.generationcp.middleware.pojos.Scale;
import org.generationcp.middleware.pojos.ScaleDiscrete;
import org.generationcp.middleware.pojos.Trait;
import org.generationcp.middleware.pojos.TraitCombinationFilter;
import org.generationcp.middleware.pojos.TraitMethod;



public class TraitQueries implements Serializable{

	ArrayList<Trait> trait;
	ArrayList<Scale> scale;
	ArrayList<TraitMethod> traitMethod;
	ArrayList<ScaleDiscrete> scaleDiscreteValue;
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private HibernateUtil hibernateUtil;
	private ManagerFactory factory;
	private TraitDataManager managerTrait;


	public TraitQueries(ManagerFactory factory,TraitDataManager managerTrait){
		this.factory=factory;
		this.managerTrait = managerTrait;
	}
	
	public ArrayList<Trait> getTrait() throws QueryException {
		int allTraitCount=managerTrait.countAllTraits();
		ArrayList<Trait> trait=(ArrayList<Trait>) managerTrait.getAllTraits(1, allTraitCount, Database.CENTRAL);
		return trait;
	}

	public ArrayList<Scale> getScale(int traitID) {
		ArrayList<Scale> scale=(ArrayList<Scale>) managerTrait.getScalesByTraitId(traitID);
		return scale;
	}

	public ArrayList<TraitMethod> getTraitMethod(int traitID) {
		ArrayList<TraitMethod> traitMethod=(ArrayList<TraitMethod>) managerTrait.getTraitMethodsByTraitId(traitID);
		return traitMethod;
	}

	public ArrayList<ScaleDiscrete> getScaleDiscreteValue(int scaleID) {
		ArrayList<ScaleDiscrete> scaleDiscreteValue=(ArrayList<ScaleDiscrete>) managerTrait.getDiscreteValuesOfScale(scaleID);
		return scaleDiscreteValue;
	}
	
	public ArrayList<Integer>  getGIDSByPhenotypicData() throws Exception{

		factory=new DatasourceConfig().getManagerFactory();
		StudyDataManager managerStudy = factory.getStudyDataManager();

		NumericRange range = new NumericRange(new Double(2000), new Double(3000));
		TraitCombinationFilter combination = new TraitCombinationFilter(new Integer(1003), new Integer(9), new Integer(30), range);
		List<TraitCombinationFilter> filters = new ArrayList<TraitCombinationFilter>();
		filters.add(combination);

		ArrayList<Integer> results = (ArrayList<Integer>) managerStudy.getGIDSByPhenotypicData(filters, 0, 10, Database.CENTRAL);
		return (ArrayList<Integer>) results;

	}

}
