package org.generationcp.browser.cross.study.adapted.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.adapted.main.pojos.CategoricalTraitEvaluator;
import org.generationcp.browser.cross.study.adapted.main.pojos.CategoricalTraitFilter;
import org.generationcp.browser.cross.study.adapted.main.pojos.CharacterTraitEvaluator;
import org.generationcp.browser.cross.study.adapted.main.pojos.CharacterTraitFilter;
import org.generationcp.browser.cross.study.adapted.main.pojos.NumericTraitEvaluator;
import org.generationcp.browser.cross.study.adapted.main.pojos.NumericTraitFilter;
import org.generationcp.browser.cross.study.adapted.main.pojos.ObservationList;
import org.generationcp.browser.cross.study.adapted.main.pojos.TableResultRow;
import org.generationcp.browser.cross.study.adapted.main.pojos.TraitObservationScore;
import org.generationcp.browser.cross.study.constants.EnvironmentWeight;
import org.generationcp.browser.cross.study.constants.NumericTraitCriteria;
import org.generationcp.browser.cross.study.h2h.main.pojos.EnvironmentForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.ObservationKey;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window.Notification;

@Configurable
public class DisplayResults extends AbsoluteLayout implements InitializingBean, InternationalizableComponent{

	private static final long serialVersionUID = 1L;

	private final static Logger LOG = LoggerFactory.getLogger(DisplayResults.class);
	
	private static final String TAG_COLUMN_ID = "DisplayResults Tag Column Id";
	private static final String LINE_NO = "DisplayResults Line No";
	private static final String LINE_GID = "DisplayResults Line GID";
	private static final String LINE_DESIGNATION = "DisplayResults Line Designation";
	private static final String COMBINED_SCORE_COLUMN_ID = "DisplayResults Combined Score";
	
    public static final String SAVE_BUTTON_ID = "DisplayResults Save Button ID";
    public static final String BACK_BUTTON_ID = "DisplayResults Back Button ID";
    
	private QueryForAdaptedGermplasmMain mainScreen;
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private CrossStudyDataManager crossStudyDataManager;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	private Table resultsTable;
	private Integer NoOfTableColumns;
	private List<String> columnHeaders;
	
	private Button backButton;
	private Button saveButton;
	
	private List<Integer> environmentIds;
	List<EnvironmentForComparison> environments;
	
	private List<Integer> traitIds;
	private Map<Integer, String> germplasmIdNameMap;
	
	private List<NumericTraitFilter> numericTraitFilter;
	private List<CharacterTraitFilter> characterTraitFilter;
	private List<CategoricalTraitFilter> categoricalTraitFilter;
	
	private List<Observation> observations;
	private Map<ObservationKey, ObservationList> observationsMap; 
	
	public DisplayResults(QueryForAdaptedGermplasmMain mainScreen) {
		this.mainScreen = mainScreen;
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setHeight("550px");
		setWidth("1000px");	
		
		resultsTable = new Table();
		resultsTable.setWidth("960px");
		resultsTable.setHeight("450px");
		resultsTable.setImmediate(true);
		resultsTable.setPageLength(-1);
		resultsTable.setColumnCollapsingAllowed(true);
		
		addComponent(resultsTable, "top:20px;left:20px");
		
		backButton = new Button(messageSource.getMessage(Message.BACK));
		backButton.setData(BACK_BUTTON_ID);
		backButton.addListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				backButtonClickAction();
			}
		});
		backButton.setWidth("100px");
		backButton.setEnabled(true);
		addComponent(backButton, "top:490px;left:770px");

		saveButton = new Button(messageSource.getMessage(Message.SAVE));
		saveButton.setData(SAVE_BUTTON_ID);
		saveButton.addListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				saveButtonClickAction();
			}
		});
		saveButton.setWidth("100px");
		saveButton.setEnabled(true);
		addComponent(saveButton, "top:490px;left:880px");
	}
	
	public void populateResultsTable(List<EnvironmentForComparison> environments, List<NumericTraitFilter> numericTraitFilter, 
			List<CharacterTraitFilter> characterTraitFilter, List<CategoricalTraitFilter> categoricalTraitFilter){
		this.environments = environments;
		this.environmentIds = getEnvironmentIds(environments);
		this.numericTraitFilter = numericTraitFilter;
		this.characterTraitFilter = characterTraitFilter;
		this.categoricalTraitFilter = categoricalTraitFilter;
		
		this.traitIds = getTraitIds(numericTraitFilter, characterTraitFilter, categoricalTraitFilter);
		
		this.germplasmIdNameMap = getGermplasm(traitIds,environmentIds);
		
		createResultsTable();
	}
	
	public void createResultsTable(){
		
		List<Object> propertyIds = new ArrayList<Object>();
        for(Object propertyId : resultsTable.getContainerPropertyIds()){
            propertyIds.add(propertyId);
        }
        
        for(Object propertyId : propertyIds){
        	resultsTable.removeContainerProperty(propertyId);
        	resultsTable.removeGeneratedColumn(propertyId);
        }
        
        resultsTable.removeAllItems();
		
		resultsTable.addContainerProperty(LINE_NO, Integer.class, null);
		resultsTable.addContainerProperty(LINE_GID, Integer.class, null);
		resultsTable.addContainerProperty(LINE_DESIGNATION, String.class, null);
		
		resultsTable.setColumnHeader(LINE_NO, "Line No");
		resultsTable.setColumnHeader(LINE_GID, "Line GID");
		resultsTable.setColumnHeader(LINE_DESIGNATION, "Line Designation");
		
		NoOfTableColumns = 3;
		
		for(NumericTraitFilter trait : numericTraitFilter){
			String name = trait.getTraitInfo().getName() + "\n No Obs";
			String weight = "Wt = " + trait.getPriority().getWeight() + "\n Score";
			Integer traitId = trait.getTraitInfo().getId();
			
			resultsTable.addContainerProperty("DisplayResults " + name, Integer.class, null);
			resultsTable.addContainerProperty("DisplayResults " + weight + traitId, Double.class, null);
			
			resultsTable.setColumnHeader("DisplayResults " + name, name);
			resultsTable.setColumnHeader("DisplayResults " + weight + traitId, weight);
			
			NoOfTableColumns+=2;
		}
		
		for(CharacterTraitFilter trait : characterTraitFilter){
			Integer traitId = trait.getTraitInfo().getId();
			String name = trait.getTraitInfo().getName() + "\n No Obs";
			String weight = "Wt = " + trait.getPriority().getWeight() + "\n Score";
			
			resultsTable.addContainerProperty("DisplayResults " + name, Integer.class, null);
			resultsTable.addContainerProperty("DisplayResults " + weight + traitId, Double.class, null);
			
			resultsTable.setColumnHeader("DisplayResults " + name, name);
			resultsTable.setColumnHeader("DisplayResults " + weight + traitId, weight);
			
			NoOfTableColumns+=2;
		}
		
		for(CategoricalTraitFilter trait : categoricalTraitFilter){
			String name = trait.getTraitInfo().getName() + "\n No Obs";
			String weight = "Wt = " + trait.getPriority().getWeight() + "\n Score";
			Integer traitId = trait.getTraitInfo().getId();
			
			resultsTable.addContainerProperty("DisplayResults " + name, Integer.class, null);
			resultsTable.addContainerProperty("DisplayResults " + weight + traitId, Double.class, null);
			
			resultsTable.setColumnHeader("DisplayResults " + name, name);
			resultsTable.setColumnHeader("DisplayResults " + weight + traitId, weight);
			
			NoOfTableColumns+=2;
		}
		
		resultsTable.addContainerProperty(COMBINED_SCORE_COLUMN_ID, Double.class, null);
		resultsTable.setColumnHeader(COMBINED_SCORE_COLUMN_ID, "Combined Score");
		NoOfTableColumns++;
		
		resultsTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
		resultsTable.setColumnHeader(TAG_COLUMN_ID, "Tag");
		NoOfTableColumns++;
		
		populateRowsResultsTable();
		
	}
	
	public void populateRowsResultsTable(){
		List<TableResultRow> tableRows = getTableRowsResults();
		
		int line_no = 1;
		for(TableResultRow row : tableRows){
			int gid = row.getGermplasmId();
			String germplasmName = germplasmIdNameMap.get(gid);
			
			Object[] itemObj = new Object[NoOfTableColumns];   
			
			itemObj[0] = line_no;
			itemObj[1] = gid;
			itemObj[2] = germplasmName;
			
			columnHeaders = getColumnHeaders(resultsTable.getColumnHeaders());
			//System.out.println("TOTAL COLUMNS NO: " + NoOfTableColumns);
			
			Map<NumericTraitFilter,TraitObservationScore> numericTOSMap = row.getNumericTOSMap();
			for(Map.Entry<NumericTraitFilter, TraitObservationScore> numericTOS : numericTOSMap.entrySet()){
				String traitName = numericTOS.getKey().getTraitInfo().getName();
				
				String name = traitName + "\n No Obs";
				
				int index = columnHeaders.indexOf(name);
				//System.out.println("CURRENT INDEX: " + index);
				
				itemObj[index] = numericTOS.getValue().getNoOfObservation();
				itemObj[index + 1] = numericTOS.getValue().getWtScore();
			}
			
			Map<CharacterTraitFilter,TraitObservationScore> characterTOSMap = row.getCharacterTOSMap();
			for(Map.Entry<CharacterTraitFilter, TraitObservationScore> characterTOS : characterTOSMap.entrySet()){
				String traitName = characterTOS.getKey().getTraitInfo().getName();
				
				String name = traitName + "\n No Obs";
				
				int index = columnHeaders.indexOf(name);
				
				itemObj[index] = characterTOS.getValue().getNoOfObservation();
				itemObj[index + 1] = characterTOS.getValue().getWtScore();
			}
			
			Map<CategoricalTraitFilter,TraitObservationScore> categoricalTOSMap = row.getCategoricalTOSMap();
			for(Map.Entry<CategoricalTraitFilter, TraitObservationScore> categoricalTOS : categoricalTOSMap.entrySet()){
				String traitName = categoricalTOS.getKey().getTraitInfo().getName();
				
				String name = traitName + "\n No Obs";
				
				int index = columnHeaders.indexOf(name);
				
				itemObj[index] = categoricalTOS.getValue().getNoOfObservation();
				itemObj[index + 1] = categoricalTOS.getValue().getWtScore();
			}
			
			itemObj[NoOfTableColumns - 2] = row.getCombinedScore();
			
			CheckBox box = new CheckBox();
			box.setImmediate(true);
			itemObj[NoOfTableColumns - 1] = box;
			
			resultsTable.addItem(itemObj,row);
			
			line_no++;
		}
	}
	
	public List<String> getColumnHeaders(String[] headers){
		List<String> columnHeaders = new ArrayList<String>();
		
		for(int i = 0; i < headers.length; i++){
			columnHeaders.add(headers[i]);
			//System.out.println(columnHeaders.get(i));
		}
		
		return columnHeaders;
	}
	
	public List<TableResultRow> getTableRowsResults(){
		List<TableResultRow> tableRows = new ArrayList<TableResultRow>();
		
		try {
			List<Observation> observations = crossStudyDataManager.getObservationsForTraits(traitIds, environmentIds);
			observationsMap = getObservationsMap(observations);
			
			List<Integer> germplasmIds = new ArrayList<Integer>();
			germplasmIds.addAll(germplasmIdNameMap.keySet());
			
			for(Map.Entry<Integer, String> germplasm : germplasmIdNameMap.entrySet()){
				int germplasmId = germplasm.getKey();
				
				Map<NumericTraitFilter,TraitObservationScore> numericTOSMap = new HashMap<NumericTraitFilter,TraitObservationScore>();
				Map<CharacterTraitFilter,TraitObservationScore> characterTOSMap = new HashMap<CharacterTraitFilter,TraitObservationScore>();
				Map<CategoricalTraitFilter,TraitObservationScore> categoricalTOSMap = new HashMap<CategoricalTraitFilter,TraitObservationScore>();
				
				
				//NUMERIC TRAIT
				for(NumericTraitFilter trait : numericTraitFilter){
					Double envWt = 0.0;
					Integer noOfObservation = 0;
					Double scorePerTrait = 0.0;
					List<Integer> obsResults = new ArrayList<Integer>();
					
					for(EnvironmentForComparison env : environments){
						ObservationKey key = new ObservationKey(trait.getTraitInfo().getId(), germplasm.getKey(), env.getEnvironmentNumber());
						ObservationList obsList = observationsMap.get(key);
						
						
						if(obsList != null){ // if the observation exist
							ComboBox weightComboBox = env.getWeightComboBox();
							EnvironmentWeight weight = (EnvironmentWeight) weightComboBox.getValue();
							envWt = Double.valueOf(weight.getWeight());
							
							noOfObservation = obsList.getObservationList().size();
							Double scorePerEnv = 0.0;
							for(Observation obs : obsList.getObservationList()){
								if(testNumericTraitVal(trait, obs)){
									scorePerEnv = scorePerEnv + 1;
								}
								else{
									scorePerEnv = scorePerEnv + (-1);
								}
							}
							//System.out.println(scorePerEnv +" = " + envWt + " * ( " + scorePerEnv +" / " + noOfObservation + " );");
							scorePerEnv = envWt * ( scorePerEnv / noOfObservation );
							
							//System.out.println(scorePerTrait+"+=" + scorePerEnv + ";");
							scorePerTrait += scorePerEnv;
						}
					}
					//System.out.println(trait.getTraitInfo().getName() + " scorePerTrait:" + scorePerTrait);
					//No Of Observation and Wt Score Per Trait
					TraitObservationScore tos = new TraitObservationScore(germplasmId,noOfObservation,scorePerTrait);
					numericTOSMap.put(trait,tos);
				}
				
				
				//CHARACTER TRAIT
				for(CharacterTraitFilter trait : characterTraitFilter){
					Double envWt = 0.0;
					Integer noOfObservation = 0;
					Double scorePerTrait = 0.0;
					List<Integer> obsResults = new ArrayList<Integer>();
					
					for(EnvironmentForComparison env : environments){
						ObservationKey key = new ObservationKey(trait.getTraitInfo().getId(), germplasm.getKey(), env.getEnvironmentNumber());
						ObservationList obsList = observationsMap.get(key);
						
						
						if(obsList != null){ // if the observation exist
							ComboBox weightComboBox = env.getWeightComboBox();
							EnvironmentWeight weight = (EnvironmentWeight) weightComboBox.getValue();
							envWt = Double.valueOf(weight.getWeight());
							
							noOfObservation = obsList.getObservationList().size();
							Double scorePerEnv = 0.0;
							for(Observation obs : obsList.getObservationList()){
								if(testCharacterTraitVal(trait, obs)){
									scorePerEnv = scorePerEnv + 1;
								}
								else{
									scorePerEnv = scorePerEnv + (-1);
								}
							}
							//System.out.println(scorePerEnv +" = " + envWt + " * ( " + scorePerEnv +" / " + noOfObservation + " );");
							scorePerEnv = envWt * ( scorePerEnv / noOfObservation );
							
							//System.out.println(scorePerTrait+"+=" + scorePerEnv + ";");
							scorePerTrait += scorePerEnv;
						}
					}
					
					//No Of Observation and Wt Score Per Trait
					TraitObservationScore tos = new TraitObservationScore(germplasmId,noOfObservation,scorePerTrait);
					characterTOSMap.put(trait,tos);
				}
				
				//CATEGORICAL TRAIT
				for(CategoricalTraitFilter trait : categoricalTraitFilter){
					Double envWt = 0.0;
					Integer noOfObservation = 0;
					Double scorePerTrait = 0.0;
					List<Integer> obsResults = new ArrayList<Integer>();
					
					for(EnvironmentForComparison env : environments){
						ObservationKey key = new ObservationKey(trait.getTraitInfo().getId(), germplasm.getKey(), env.getEnvironmentNumber());
						ObservationList obsList = observationsMap.get(key);
						
						
						if(obsList != null){ // if the observation exist
							ComboBox weightComboBox = env.getWeightComboBox();
							EnvironmentWeight weight = (EnvironmentWeight) weightComboBox.getValue();
							envWt = Double.valueOf(weight.getWeight());
							
							noOfObservation = obsList.getObservationList().size();
							Double scorePerEnv = 0.0;
							for(Observation obs : obsList.getObservationList()){
								if(testCategoricalTraitVal(trait, obs)){
									scorePerEnv = scorePerEnv + 1;
								}
								else{
									scorePerEnv = scorePerEnv + (-1);
								}
							}
							//System.out.println(scorePerEnv +" = " + envWt + " * ( " + scorePerEnv +" / " + noOfObservation + " );");
							scorePerEnv = envWt * ( scorePerEnv / noOfObservation );
							
							//System.out.println(scorePerTrait+"+=" + scorePerEnv + ";");
							scorePerTrait += scorePerEnv;
						}
					}
					
					//No Of Observation and Wt Score Per Trait
					TraitObservationScore tos = new TraitObservationScore(germplasmId,noOfObservation,scorePerTrait);
					categoricalTOSMap.put(trait,tos);
				}
				
				tableRows.add(new TableResultRow(germplasmId,numericTOSMap,characterTOSMap,categoricalTOSMap));
			}
			
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tableRows;
	}
	
	public boolean testNumericTraitVal(NumericTraitFilter trait, Observation obsevation){
		NumericTraitEvaluator eval = new NumericTraitEvaluator(trait.getCondition(),
				trait.getLimits(), Double.valueOf(obsevation.getValue()));
		
		return eval.evaluate();
	}
	
	public boolean testCharacterTraitVal(CharacterTraitFilter trait, Observation obsevation){
		CharacterTraitEvaluator eval = new CharacterTraitEvaluator(trait.getCondition(),
				trait.getLimits(), obsevation.getValue());
		
		return eval.evaluate();
	}
	
	public boolean testCategoricalTraitVal(CategoricalTraitFilter trait, Observation obsevation){
		CategoricalTraitEvaluator eval = new CategoricalTraitEvaluator(trait.getCondition(),
				trait.getLimits(), obsevation.getValue());
		
		return eval.evaluate();
	}
	
	public Map<ObservationKey, ObservationList> getObservationsMap(List<Observation> observations){
		Map<ObservationKey, ObservationList> observationsMap = new HashMap<ObservationKey, ObservationList>();
		
		for(Observation obs : observations){
			ObservationKey key = obs.getId();
			
			if(!observationsMap.containsKey(key)){
				ObservationList list = new ObservationList(key);
				list.add(obs);
				observationsMap.put(key,list);
			}
			else{
				ObservationList obslist = observationsMap.get(key);
				List<Observation> list = obslist.getObservationList();
				list.add(obs);
				obslist.setObservationList(list);
				
				observationsMap.put(key, obslist);
			}
		}
		
		System.out.println("# of observations: " + observations.size());
		int countList = 0;
		for(Map.Entry<ObservationKey, ObservationList> obs : observationsMap.entrySet()){
			countList += obs.getValue().getObservationList().size();
		}
		System.out.println("# of observationsMap: " + countList);
		return observationsMap;
	}
	
	public List<Integer> getTraitIds(List<NumericTraitFilter> numericTraitFilter, 
			List<CharacterTraitFilter> characterTraitFilter, List<CategoricalTraitFilter> categoricalTraitFilter){
		List<Integer> traitIds = new ArrayList<Integer>();
		
		for(NumericTraitFilter trait : numericTraitFilter){
			traitIds.add(trait.getTraitInfo().getId());
		}
		
		for(CharacterTraitFilter trait : characterTraitFilter){
			traitIds.add(trait.getTraitInfo().getId());
		}
		
		for(CategoricalTraitFilter trait : categoricalTraitFilter){
			traitIds.add(trait.getTraitInfo().getId());
		}
		
		return traitIds;
	}
	
	public List<Integer> getEnvironmentIds(List<EnvironmentForComparison> environments){
		List<Integer> environmentIds = new ArrayList<Integer>();
		
		for(EnvironmentForComparison env : environments){
			environmentIds.add(env.getEnvironmentNumber());
		}
		
		return environmentIds;
	}
	
	public Map<Integer, String> getGermplasm(List<Integer> traitIds, List<Integer> environmentIds){
		Map<Integer, String> germplasmIdNameMap = new HashMap<Integer, String>();
		
		List<Integer> germplasmIds = new ArrayList<Integer>();
		List<Integer> traitIdList = new ArrayList<Integer>();
		traitIdList.addAll(traitIds);
		
		try {
			observations = crossStudyDataManager.getObservationsForTraits(traitIdList, environmentIds);
			
			Iterator obsIter = observations.iterator();
			while(obsIter.hasNext()){
				Observation observation = (Observation) obsIter.next();
				int id = observation.getId().getGermplasmId();
				if(!germplasmIds.contains(id)){
					germplasmIds.add(id);
				}
			}
			
			germplasmIdNameMap = germplasmDataManager.getPreferredNamesByGids(germplasmIds);
		} catch (MiddlewareQueryException ex) {
			ex.printStackTrace();
            LOG.error("Database error!", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", "Please report to IBP.", Notification.POSITION_CENTERED);
		}
		
		return germplasmIdNameMap;
	}
	
	public void backButtonClickAction(){
		this.mainScreen.selectSecondTab();
	}
	
	public void saveButtonClickAction(){
		//this.mainScreen.selectThirdTab();
	}

}
