package  org.generationcp.browser.cross.study.h2h.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.commons.EnvironmentFilter;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.TraitForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.dms.TrialEnvironments;
import org.generationcp.middleware.domain.h2h.GermplasmPair;
import org.generationcp.middleware.domain.h2h.TraitInfo;
import org.generationcp.middleware.domain.h2h.TraitType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window.Notification;

@Configurable
public class TraitsAvailableComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 991899235025710803L;
    
    private final static Logger LOG = LoggerFactory.getLogger(org.generationcp.browser.cross.study.h2h.main.TraitsAvailableComponent.class);
    
    public static final String BACK_BUTTON_ID = "TraitsAvailableComponent Back Button ID";
    public static final String NEXT_BUTTON_ID = "TraitsAvailableComponent Next Button ID";
    public static final String CHECKBOX_ID = "TraitsAvailableComponent Checkbox ID";
    
    private static final String TRAIT_COLUMN_ID = "TraitsAvailableComponent Trait Column Id";
    private static final String TRAIT_DESCRIPTION_COLUMN_ID = "TraitsAvailableComponent Trait Description Column Id";
    private static final String NUMBER_OF_ENV_COLUMN_ID = "TraitsAvailableComponent Number of Environments Column Id";
    private static final String TAG_COLUMN_ID = "TraitsAvailableComponent Tag Column Id";
    private static final String DIRECTION_COLUMN_ID = "TraitsAvailableComponent Direction Column Id";
    private static final String TAG_ALL = "TraitsAvailableComponent TAG_ALL Column Id";
    
    private Table traitsTable;
    
    private Button nextButton;
    private Button backButton;
    
    private HeadToHeadCrossStudyMain mainScreen;
    private EnvironmentFilter nextScreen;
    
    private Label selectTraitLabel;
    
    private Label selectTraitReminderLabel;
    
    public static final Integer INCREASING = 1;
    public static final Integer DECREASING = 0;
    
    @Autowired
    private CrossStudyDataManager crossStudyDataManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private List<ComboBox> traitForComparisons; //will contain all the tagged row
    private Map<ComboBox, TraitInfo> traitMaps; //will contain the mapping from comboBox to the specific row
    private Map<String, Map<String, TrialEnvironment>> traitEnvironmentMap; //will contain the map of trait and trial environment
    private Map<String, TrialEnvironment> trialEnvironmentMap; //will contain the map of  trial environment
    private Map<String, String> germplasmIdNameMap;
    
    private Set<Integer> germplasmIds;
    private List<GermplasmPair> finalGermplasmPair;
    private List<GermplasmPair> prevfinalGermplasmPair;
    private List<GermplasmPair> environmentPairList;
    
    private CheckBox tagUnTagAll;
    
    public TraitsAvailableComponent(HeadToHeadCrossStudyMain mainScreen, EnvironmentFilter nextScreen){
        this.mainScreen = mainScreen;
        this.nextScreen = nextScreen;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("500px");
        setWidth("1000px");
        
        selectTraitLabel = new Label(messageSource.getMessage(Message.HEAD_TO_HEAD_SELECT_TRAITS));
        selectTraitLabel.setImmediate(true);
        addComponent(selectTraitLabel, "top:20px;left:30px");
        
        selectTraitReminderLabel = new Label(messageSource.getMessage(Message.HEAD_TO_HEAD_SELECT_TRAITS_REMINDER));
        selectTraitReminderLabel.setImmediate(true);
        selectTraitReminderLabel.setStyleName("gcp-bold-italic");
        addComponent(selectTraitReminderLabel, "top:20px;left:400px");
        
        traitsTable = new Table();
        traitsTable.setWidth("950px");
        traitsTable.setHeight("400px");
        traitsTable.setImmediate(true);
        
        traitsTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        traitsTable.addContainerProperty(TRAIT_COLUMN_ID, String.class, null);
        traitsTable.addContainerProperty(TRAIT_DESCRIPTION_COLUMN_ID, String.class, null);
        traitsTable.addContainerProperty(NUMBER_OF_ENV_COLUMN_ID, Integer.class, null);
        traitsTable.addContainerProperty(DIRECTION_COLUMN_ID, ComboBox.class, null);
                
        traitsTable.setColumnHeader(TAG_COLUMN_ID,  messageSource.getMessage(Message.HEAD_TO_HEAD_TAG));
        traitsTable.setColumnHeader(TRAIT_COLUMN_ID, messageSource.getMessage(Message.HEAD_TO_HEAD_TRAIT));
        traitsTable.setColumnHeader(TRAIT_DESCRIPTION_COLUMN_ID, "Description");        
        traitsTable.setColumnHeader(NUMBER_OF_ENV_COLUMN_ID, messageSource.getMessage(Message.HEAD_TO_HEAD_NO_OF_ENVS));        
        traitsTable.setColumnHeader(DIRECTION_COLUMN_ID, messageSource.getMessage(Message.HEAD_TO_HEAD_DIRECTION));
        
        traitsTable.setColumnWidth(TAG_COLUMN_ID, 50);        
        traitsTable.setColumnWidth(TRAIT_COLUMN_ID, 150);
        traitsTable.setColumnWidth(TRAIT_DESCRIPTION_COLUMN_ID, 400);
        traitsTable.setColumnWidth(NUMBER_OF_ENV_COLUMN_ID, 175);
        traitsTable.setColumnWidth(DIRECTION_COLUMN_ID, 200);        
        
        addComponent(traitsTable, "top:40px;left:30px");
        
        tagUnTagAll = new CheckBox();
        tagUnTagAll.setValue(false);
        tagUnTagAll.setImmediate(true);
        tagUnTagAll.setData(TAG_ALL);
        tagUnTagAll.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, true));
        addComponent(tagUnTagAll, "top:44px;left:60px");
        
        nextButton = new Button("Next");
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        nextButton.setEnabled(false);
        nextButton.setWidth("80px");
        nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        addComponent(nextButton, "top:450px;left:900px");
        
        backButton = new Button("Back");
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        backButton.setWidth("80px");
        addComponent(backButton, "top:450px;left:810px");
        
    }
    
    private ComboBox getDirectionComboBox(){
    	ComboBox combo = new ComboBox();
    	combo.setNullSelectionAllowed(false);
    	combo.setTextInputAllowed(false);
    	combo.setImmediate(true);
    	
		combo.addItem(INCREASING);
		combo.setItemCaption(INCREASING, messageSource.getMessage(Message.HEAD_TO_HEAD_INCREASING));
		
		combo.addItem(DECREASING);
		combo.setItemCaption(DECREASING, messageSource.getMessage(Message.HEAD_TO_HEAD_DECREASING));
		
		combo.setValue(INCREASING);
		
		combo.setEnabled(false);
		return combo;
		
    }
    
    public void populateTraitsAvailableTable(List<GermplasmPair> germplasmPairList, Map<String, String> germplasmIdNameMap){
        this.traitsTable.removeAllItems();
        nextButton.setEnabled(false);
        
        selectTraitReminderLabel.setVisible(true);
        this.germplasmIdNameMap = germplasmIdNameMap; 
        traitForComparisons = new ArrayList<ComboBox>();
        traitMaps = new HashMap<ComboBox, TraitInfo>();
        
        Map<String, List<TraitInfo>> traitMap = new HashMap<String, List<TraitInfo>>();
        Map<String, Set<String>> traitEnvMap = new HashMap<String, Set<String>>();
        traitEnvironmentMap = new HashMap<String, Map<String, TrialEnvironment>>();
        trialEnvironmentMap = new HashMap<String, TrialEnvironment>();
        germplasmIds = new HashSet<Integer>();
        //List<GermplasmPair> environmentPairList;
        finalGermplasmPair = germplasmPairList;
        boolean doRefresh = false;
        if(prevfinalGermplasmPair == null){
        	doRefresh = true;
        }else{
        	//we checked if its the same
        	if(prevfinalGermplasmPair.size() == finalGermplasmPair.size()){
        		doRefresh = false;
        		for(GermplasmPair pairOld : prevfinalGermplasmPair){
        			boolean isMatched = false;
        			for(GermplasmPair pairNew : finalGermplasmPair){
        				if(pairOld.getGid1() == pairNew.getGid1() && pairOld.getGid2() == pairNew.getGid2()){
        					isMatched = true;
        					break;
        				}
            		}
        			if(isMatched == false){
        				//meaning new pair
        				doRefresh = true;
        				break;
        			}
        		}
        	}else{
        		doRefresh = true;
        	}
        	
        }
        
		try {
			
			if(doRefresh){
				//only call when need to refresh
				prevfinalGermplasmPair = germplasmPairList;
				environmentPairList = crossStudyDataManager.getEnvironmentsForGermplasmPairs(germplasmPairList);
			}
		
	        for(GermplasmPair pair : environmentPairList){
	    		TrialEnvironments env = pair.getTrialEnvironments();
	    		
	    		germplasmIds.add(Integer.valueOf(pair.getGid1()));
	    		germplasmIds.add(Integer.valueOf(pair.getGid2()));
	    		
	    		java.util.Iterator<TrialEnvironment> envIterator = env.getTrialEnvironments().iterator();
	    		while(envIterator.hasNext())
	    		{
	    			TrialEnvironment trialEnv = envIterator.next();
	    			trialEnvironmentMap.put(Integer.toString(trialEnv.getId()), trialEnv);
	    			java.util.Iterator<TraitInfo> traitIterator = trialEnv.getTraits().iterator();
	    			while(traitIterator.hasNext())
	        		{
	    				TraitInfo info = traitIterator.next();
	    				
	    				//add here the checking if the trait is non numeric
	    				if(info.getType() != TraitType.NUMERIC)
	    					continue;
	    				
	    				String id = Integer.toString(info.getId());
	    				List<TraitInfo> tempList = new ArrayList<TraitInfo>();
	    				if(traitMap.containsKey(id)){
	    					tempList = traitMap.get(id);
	    				}
	    				tempList.add(info);
	    				traitMap.put(id, tempList);
	    				Set<String> envIds = traitEnvMap.get(id);
	    				if(envIds == null){
	    					envIds = new HashSet<String>();
	    				}
	    				envIds.add(trialEnv.getId()+"");
	    				traitEnvMap.put(id, envIds);
	    				
	    				//we need to keep track on the environments
	    				Map<String, TrialEnvironment> tempEnvMap = new HashMap<String, TrialEnvironment>();
	    				if(traitEnvironmentMap.containsKey(id)){
	    					tempEnvMap = traitEnvironmentMap.get(id);
	    				}
	    				tempEnvMap.put(Integer.toString(trialEnv.getId()), trialEnv);
	    				traitEnvironmentMap.put(id, tempEnvMap);
	    				
	        		}
	    		}
	    		
	        }
	        java.util.Iterator<String> traitsIterator = traitMap.keySet().iterator();
	        while(traitsIterator.hasNext()){
	        	String id = traitsIterator.next();
	        	List<TraitInfo> traitInfoList = traitMap.get(id);
	        	TraitInfo info = traitInfoList.get(0); //we get the 1st one since its all the same for this specific list
	        	CheckBox box = new CheckBox();
	        	ComboBox comboBox = getDirectionComboBox();
	        	box.setImmediate(true);
	        	Integer tableId = Integer.valueOf(id);
	        	
	        	Integer numOfEnv = traitEnvMap.get(id).size();
	        	traitsTable.addItem(new Object[] {box, info.getName(),info.getDescription(),
	        			numOfEnv,comboBox },tableId);
	        	//checkBoxMap.put(box, traitsTable.getItem(tableId));
	        	box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, comboBox));
	        	//traitMaps.put(comboBox, traitsTable.getItem(tableId));
	        	traitMaps.put(comboBox, info);
	        	
	        }
	        
	        if(traitsTable.getItemIds().isEmpty()){
	        	MessageNotifier.showWarning(getWindow(), "Warning!", "No environments and traits were found for the pairs of germplasm entries you have specified.", Notification.POSITION_CENTERED);
				return;
	        }
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void clickCheckBox(Component combo, boolean boolVal){
    	if(combo != null){
    		ComboBox comboBox = (ComboBox) combo;
    		comboBox.setEnabled(boolVal);
    		TraitInfo info = traitMaps.get(comboBox);
    		    			
    			if( info != null){    				
    				if(boolVal){
    					traitForComparisons.add(comboBox);	
    				}else{
    					traitForComparisons.remove(comboBox);
					}    				    				
    			}
    			
			if(traitForComparisons.isEmpty()){
				nextButton.setEnabled(false);
				selectTraitReminderLabel.setVisible(true);
			}else{
				nextButton.setEnabled(true);
				selectTraitReminderLabel.setVisible(false);
			}
    	}
    }
    
    public void clickTagAllCheckbox(boolean boxChecked){
    	Object tableItemIds[] = traitsTable.getItemIds().toArray();
    	for (int i=0; i<tableItemIds.length; i++) {
    		Item row = traitsTable.getItem(tableItemIds[i]);
    		CheckBox box = (CheckBox) row.getItemProperty(TAG_COLUMN_ID).getValue();
			box.setValue(boxChecked);
    	}
    }
    
    public void nextButtonClickAction(){
        //this.nextScreen.populateEnvironmentsTable(this.currentTestEntryGID, this.currentStandardEntryGID, this.traitsForComparisonList);
    	List<TraitForComparison> traitForComparisonsList = new ArrayList<TraitForComparison>();
    	for(ComboBox combo : traitForComparisons){
    		//ComboBox combo = (ComboBox) item.getItemProperty(DIRECTION_COLUMN_ID);
    		//item.getItemPropertyIds()
    		TraitInfo info = traitMaps.get(combo);
    		TraitForComparison traitForComparison = new TraitForComparison(info, (Integer)combo.getValue());
    		traitForComparisonsList.add(traitForComparison);    		
    	}
    	if(this.nextScreen != null){
    		this.nextScreen.populateEnvironmentsTable(traitForComparisonsList, traitEnvironmentMap, trialEnvironmentMap, germplasmIds, finalGermplasmPair, germplasmIdNameMap);
    	}
        this.mainScreen.selectThirdTab();
    }
    
    public void backButtonClickAction(){
        this.mainScreen.selectFirstTab();
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
    }
}
