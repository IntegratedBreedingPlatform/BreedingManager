package org.generationcp.browser.cross.study.adapted.main.pojos;

import java.util.List;

import org.generationcp.browser.cross.study.constants.NumericTraitCriteria;

public class NumericTraitEvaluator {
	NumericTraitCriteria condition;
	List<String> limits;
	Double value; 
	
	public NumericTraitEvaluator(NumericTraitCriteria condition,
			List<String> limits, Double value) {
		super();
		this.condition = condition;
		this.limits = limits;
		this.value = value;
		
	}
	
	public boolean evaluate(){
		boolean result = false;
		
		if(condition == NumericTraitCriteria.KEEP_ALL){
			result = true;
		}
		else if(condition == NumericTraitCriteria.LESS_THAN){
			Double limit = Double.valueOf(limits.get(0));
			result = (value < limit)? true: false;
		}
		else if(condition == NumericTraitCriteria.LESS_THAN_EQUAL){
			Double limit = Double.valueOf(limits.get(0));
			result = (value <= limit)? true: false;
		}
		else if(condition == NumericTraitCriteria.EQUAL){
			Double limit = Double.valueOf(limits.get(0));
			result = (value == limit)? true: false;
		}
		else if(condition == NumericTraitCriteria.GREATER_THAN){
			Double limit = Double.valueOf(limits.get(0));
			result = (value > limit)? true: false;
		}
		else if(condition == NumericTraitCriteria.GREATER_THAN_EQUAL){
			Double limit = Double.valueOf(limits.get(0));
			result = (value > limit)? true: false;
		}
		else if(condition == NumericTraitCriteria.BETWEEN){
			//limit a-b or a - b
			String[] limit = limits.get(0).split("-");
			Double lowerLimit = Double.valueOf(limit[0].trim());
			Double upperLimit = Double.valueOf(limit[1].trim());
			result = (value >= lowerLimit && value <= upperLimit)? true: false;
		}
		else if(condition == NumericTraitCriteria.IN){
			//limit a,b ,c, d
			String[] limit = limits.get(0).split(",");
			
			for(int i = 0; i < limit.length; i++){
				limit[i] = limit[i].trim();
				if(value == Double.valueOf(limit[i])){
					result = true;
				}
			}
		}
		else if(condition == NumericTraitCriteria.NOT_IN){
			//limit a,b ,c, d
			String[] limit = limits.get(0).split(",");
			
			boolean flag = true;
			for(int i = 0; i < limit.length; i++){
				limit[i] = limit[i].trim();
				if(value == Double.valueOf(limit[i])){
					flag = false;
				}
			}
			
			result = flag;
		}
		
		return result;
	}
	
	
}
