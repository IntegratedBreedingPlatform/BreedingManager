package org.generationcp.browser.cross.study.h2h;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.cross.study.h2h.listeners.H2HComparisonQueryButtonClickListener;
import org.generationcp.browser.cross.study.h2h.pojos.TraitForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;

@Configurable
public class TraitsAvailableComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 991899235025710803L;
    
    public static final String BACK_BUTTON_ID = "TraitsAvailableComponent Back Button ID";
    public static final String NEXT_BUTTON_ID = "TraitsAvailableComponent Next Button ID";
    
    private static final String TRAIT_COLUMN_ID = "TraitsAvailableComponent Trait Column Id";
    private static final String NUMBER_OF_ENV_COLUMN_ID = "TraitsAvailableComponent Number of Environments Column Id";
    
    private Table traitsTable;
    
    private Button nextButton;
    private Button backButton;
    
    private HeadToHeadComparisonMain mainScreen;
    private EnvironmentsAvailableComponent nextScreen;
    
    private Integer currentTestEntryGID;
    private Integer currentStandardEntryGID;
    
    public TraitsAvailableComponent(HeadToHeadComparisonMain mainScreen, EnvironmentsAvailableComponent nextScreen){
        this.mainScreen = mainScreen;
        this.nextScreen = nextScreen;
        this.currentStandardEntryGID = null;
        this.currentTestEntryGID = null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("500px");
        setWidth("1000px");
        
        traitsTable = new Table();
        traitsTable.setWidth("500px");
        traitsTable.setHeight("400px");
        traitsTable.setImmediate(true);
        
        traitsTable.addContainerProperty(TRAIT_COLUMN_ID, String.class, null);
        traitsTable.addContainerProperty(NUMBER_OF_ENV_COLUMN_ID, Integer.class, null);
        
        traitsTable.setColumnHeader(TRAIT_COLUMN_ID, "TRAIT");
        traitsTable.setColumnHeader(NUMBER_OF_ENV_COLUMN_ID, "# OF ENV");
        
        addComponent(traitsTable, "top:20px;left:30px");
        
        nextButton = new Button("Next");
        nextButton.setData(NEXT_BUTTON_ID);
        nextButton.addListener(new H2HComparisonQueryButtonClickListener(this));
        nextButton.setEnabled(false);
        addComponent(nextButton, "top:450px;left:900px");
        
        backButton = new Button("Back");
        backButton.setData(BACK_BUTTON_ID);
        backButton.addListener(new H2HComparisonQueryButtonClickListener(this));
        addComponent(backButton, "top:450px;left:820px");
    }

    public void populateTraitsAvailableTable(Integer testEntryGID, Integer standardEntryGID){
    	this.traitsTable.removeAllItems();
    	
    	List<TraitForComparison> tableItems = getAvailableTraitsForComparison(testEntryGID, standardEntryGID);
    	for(TraitForComparison tableItem : tableItems){
    	    this.traitsTable.addItem(new Object[]{tableItem.getName(), tableItem.getNumberOfEnvironments()}, tableItem.getName());
    	}
    	
    	this.traitsTable.requestRepaint();
    	
    	if(traitsTable.getItemIds().isEmpty()){
    	    this.nextButton.setEnabled(false);
    	} else{
    	    this.currentStandardEntryGID = standardEntryGID;
    	    this.currentTestEntryGID = testEntryGID;
    	    this.nextButton.setEnabled(true);
    	}
    }
    
    private List<TraitForComparison> getAvailableTraitsForComparison(Integer testEntryGID, Integer standardEntryGID){
    	List<TraitForComparison> toreturn = new ArrayList<TraitForComparison>();
    	
    	if(testEntryGID == 50533 && standardEntryGID == 50532){
    		toreturn.add(new TraitForComparison("GRAIN_YIELD", 8));
    		toreturn.add(new TraitForComparison("PLANT_HEIGHT", 5));
    		toreturn.add(new TraitForComparison("MATURITY", 9));
    		toreturn.add(new TraitForComparison("FOLWERING", 4));
    		toreturn.add(new TraitForComparison("BLB", 3));
    		toreturn.add(new TraitForComparison("CHALK", 6));
    		toreturn.add(new TraitForComparison("AROMA", 12));
    	} else if(testEntryGID == 1 && standardEntryGID == 2){
    		toreturn.add(new TraitForComparison("GRAIN_YIELD", 1));
    		toreturn.add(new TraitForComparison("PLANT_HEIGHT", 2));
    		toreturn.add(new TraitForComparison("MATURITY", 3));
    		toreturn.add(new TraitForComparison("FOLWERING", 4));
    		toreturn.add(new TraitForComparison("BLB", 5));
    		toreturn.add(new TraitForComparison("CHALK", 6));
    		toreturn.add(new TraitForComparison("AROMA", 7));
    	}
    	
    	return toreturn;
    }
    
    public void nextButtonClickAction(){
        this.nextScreen.populateEnvironmentsTable(this.currentTestEntryGID, this.currentStandardEntryGID);
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
