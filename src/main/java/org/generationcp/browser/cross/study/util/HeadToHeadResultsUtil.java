package org.generationcp.browser.cross.study.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.generationcp.browser.cross.study.constants.EnvironmentWeight;
import org.generationcp.browser.cross.study.h2h.main.TraitsAvailableComponent;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.browser.cross.study.h2h.main.pojos.ObservationList;
import org.generationcp.browser.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.middleware.domain.h2h.GermplasmPair;


public class HeadToHeadResultsUtil {
	
	private static final double DEF_PROBABILITY = 0.5;
	
	
	public static double getPvalue(int numOfEnvts, Double standardMean){
		BinomialDistribution binomial = new BinomialDistribution(numOfEnvts, DEF_PROBABILITY);
		double cumulativeProbability = binomial.cumulativeProbability(standardMean.intValue());
		System.out.println("x="+ standardMean.intValue() + ", cumulativeProbability=" + cumulativeProbability);
		return 1 - cumulativeProbability;
	}
	
	
	public static Double getMeanDiff(GermplasmPair germplasmPair, TraitForComparison traitForComparison, 
    		Map<String, ObservationList> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	//r * ( summation of [ (Ek /summation of Ek)(Tijk-Silk) ] )
    	/*
    	 * Nijl = is the number of environment where both tijk and silk is not null and not empty string
    	 * r = 1 if increasing and -1 if decreasing
    	 * Ek - environment weight
    	 */
    	
    	boolean isIncreasing = false;
    	int totalWeight = 0;
    	if(traitForComparison.getDirection().intValue() == TraitsAvailableComponent.INCREASING.intValue()){
    		isIncreasing = true;
    	}else if(traitForComparison.getDirection().intValue() == TraitsAvailableComponent.DECREASING.intValue()){
    		isIncreasing = false;
    	}
    	
    	String gid1ForCompare = Integer.toString(germplasmPair.getGid1());
		String gid2ForCompare = Integer.toString(germplasmPair.getGid2());
		String traitId = Integer.toString(traitForComparison.getTraitInfo().getId());
		List<Double> listOfObsVal = new ArrayList<Double>();
		List<Integer> envtWeights = new ArrayList<Integer>();
		
		for(EnvironmentForComparison envForComparison: environmentForComparisonList){
			
			String envId = envForComparison.getEnvironmentNumber().toString();
			String keyToChecked1 = traitId + ":" + envId + ":" +gid1ForCompare;
			String keyToChecked2 = traitId + ":" + envId + ":" +gid2ForCompare;
			
			ObservationList obs1 = observationMap.get(keyToChecked1);
    		ObservationList obs2 = observationMap.get(keyToChecked2);
    		
    		if(obs1 != null && obs2 != null){
	    		if(obs1.isValidObservationList() && obs2.isValidObservationList()){
	    			double obs1Val = obs1.getObservationAverage();
	    			double obs2Val = obs2.getObservationAverage();
	    			
	    			EnvironmentWeight envtWeight = (EnvironmentWeight) envForComparison.getWeightComboBox().getValue();	
	    			int weight = envtWeight.getWeight();
	    			totalWeight += weight;
	    			envtWeights.add(weight);
	    		
	    			Double difference = Double.valueOf(obs1Val - obs2Val);
					listOfObsVal.add(difference);
	    		}
    		}
			
		}

		double summation = 0;
		Iterator<Double> diffIterator = listOfObsVal.iterator();
		Iterator<Integer> envtIterator = envtWeights.iterator();
		
		while (diffIterator.hasNext() && envtIterator.hasNext() && totalWeight != 0){
			Double difference = diffIterator.next();
			double envtWeight = (double) envtIterator.next();
			Double finalWeight = envtWeight / totalWeight;
			
			summation += (difference * finalWeight);
		}
		
		if(isIncreasing == false && summation != 0)
			summation = -1 * summation; 
    	
		return summation;
    }
	
	
    public static Integer getTotalNumOfSup(GermplasmPair germplasmPair, 
    		TraitForComparison traitForComparison, Map<String, ObservationList> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	boolean isIncreasing = false;
    	int counter = 0;
    	if(traitForComparison.getDirection().intValue() == TraitsAvailableComponent.INCREASING.intValue()){
    		isIncreasing = true;
    	}else if(traitForComparison.getDirection().intValue() == TraitsAvailableComponent.DECREASING.intValue()){
    		isIncreasing = false;
    	}
    	
    	String gid1ForCompare = Integer.toString(germplasmPair.getGid1());
		String gid2ForCompare = Integer.toString(germplasmPair.getGid2());
		String traitId = Integer.toString(traitForComparison.getTraitInfo().getId());
		for(EnvironmentForComparison envForComparison: environmentForComparisonList){
			
			String envId = envForComparison.getEnvironmentNumber().toString();
			String keyToChecked1 = traitId + ":" + envId + ":" +gid1ForCompare;
			String keyToChecked2 = traitId + ":" + envId + ":" +gid2ForCompare;
			
			ObservationList obs1 = observationMap.get(keyToChecked1);
    		ObservationList obs2 = observationMap.get(keyToChecked2);
    		
    		
    		//if(isValidObsValue(obs1, obs2)){
    		if(obs1 != null && obs2 != null){
	    		if(obs1.isValidObservationList() && obs2.isValidObservationList()){
	    			
	    			//double obs1Val = Double.parseDouble(obs1.getValue());
	    			//double obs2Val = Double.parseDouble(obs2.getValue());
	    			double obs1Val = obs1.getObservationAverage();
	    			double obs2Val = obs2.getObservationAverage();
	    			
	    			if(isIncreasing){
	    				if(obs1Val > obs2Val)
	    					counter++;
	    			}else{
	    				if(obs1Val < obs2Val)
	    					counter++;
	    			}
	    			
	    			
	    		}
    		}
			
		}
		
		return Integer.valueOf(counter);
    }
    
    
    public static Integer getTotalNumOfEnv(GermplasmPair germplasmPair, 
    		TraitForComparison traitForComparison, Map<String, ObservationList> observationMap,List<EnvironmentForComparison> environmentForComparisonList){
    	int counter = 0;
    		
    		
    		String gid1ForCompare = Integer.toString(germplasmPair.getGid1());
    		String gid2ForCompare = Integer.toString(germplasmPair.getGid2());
    		String traitId = Integer.toString(traitForComparison.getTraitInfo().getId());
    		for(EnvironmentForComparison envForComparison: environmentForComparisonList){
    			
    			String envId = envForComparison.getEnvironmentNumber().toString();
    			String keyToChecked1 = traitId + ":" + envId + ":" +gid1ForCompare;
    			String keyToChecked2 = traitId + ":" + envId + ":" +gid2ForCompare;
    			
    			ObservationList obs1 = observationMap.get(keyToChecked1);
        		ObservationList obs2 = observationMap.get(keyToChecked2);
        		if(obs1 != null && obs2 != null){
	        		if(obs1.isValidObservationList() && obs2.isValidObservationList()){
	        		//if(isValidObsValue(obs1, obs2)){
	        			counter++;
	        			
	        		}
        		}
    			
    		}
    	
    		
    	return Integer.valueOf(counter);
    }
    
    
    /*
     * Gets mean of all observed values for a trait for a germplasm in the pair.
     * Mean = sum of observed values for germplasm / # of envt's 
     * If index = 1, get mean for first germplasm in pair. If index = 2, get mean for second germplasm.
     */
    public static Double getMeanValue(GermplasmPair germplasmPair, int index, TraitForComparison traitForComparison, 
    		Map<String, ObservationList> observationMap,List<EnvironmentForComparison> environmentForComparisonList){

    	int numOfValidEnv = 0;
    	double summation = 0;
    	String gid1ForCompare = Integer.toString(germplasmPair.getGid1());
		String gid2ForCompare = Integer.toString(germplasmPair.getGid2());
		String traitId = Integer.toString(traitForComparison.getTraitInfo().getId());

		for(EnvironmentForComparison envForComparison: environmentForComparisonList){			
			String envId = envForComparison.getEnvironmentNumber().toString();
			String keyToChecked1 = traitId + ":" + envId + ":" +gid1ForCompare;
			String keyToChecked2 = traitId + ":" + envId + ":" +gid2ForCompare;
			
			ObservationList obs1 = observationMap.get(keyToChecked1);
    		ObservationList obs2 = observationMap.get(keyToChecked2);
    		
    		// get only values for envt's where trait has been observed for both germplasms
    		if(obs1 != null && obs2 != null){
	    		if(obs1.isValidObservationList() && obs2.isValidObservationList()){
	    			numOfValidEnv++;
	    			
	    			System.out.println("Env: "+envForComparison.getLocationName());
	    			System.out.println("Weight: "+envForComparison.getWeight());
	    			System.out.println("");
	    			
	    			if (index == 1){
	    				summation += obs1.getWeightedObservationAverage(envForComparison.getWeight());
	    			} else if (index == 2){
	    				summation += obs2.getWeightedObservationAverage(envForComparison.getWeight());
	    			}
	    			
	    		}
    		}
			
		}
		
		double mean = 0;
		if (numOfValidEnv > 0){
			mean = summation / numOfValidEnv;
		}
		
		return mean;
    }

}
