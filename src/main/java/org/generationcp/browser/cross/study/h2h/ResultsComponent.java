package org.generationcp.browser.cross.study.h2h;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.cross.study.h2h.pojos.Result;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@Configurable
public class ResultsComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 2305982279660448571L;
	
    private static final String TRAIT_COLUMN_ID = "ResultsComponent Trait Column ID";
    private static final String NUM_OF_ENV_COLUMN_ID = "ResultsComponent Num Of Env Column ID";
    private static final String NUM_SUP_COLUMN_ID = "ResultsComponent Num Sup Column ID";
    private static final String MEAN_TEST_COLUMN_ID = "ResultsComponent Mean Test Column ID";
    private static final String MEAN_STD_COLUMN_ID = "ResultsComponent Mean STD Column ID";
    private static final String MEAN_DIFF_COLUMN_ID = "ResultsComponent Mean Diff Column ID";
    private static final String PVAL_COLUMN_ID = "ResultsComponent Pval Column ID";

    private Table resultsTable;
	
    private Label testEntryLabel;
    private Label standardEntryLabel;
    private Label testEntryNameLabel;
    private Label standardEntryNameLabel;
    
    private HeadToHeadComparisonMain mainScreen;
    
    private Integer currentTestEntryGID;
    private Integer currentStandardEntryGID;
    
    public ResultsComponent(HeadToHeadComparisonMain mainScreen){
        this.mainScreen = mainScreen;
        this.currentStandardEntryGID = null;
        this.currentTestEntryGID = null;
    }
	
    @Override
    public void afterPropertiesSet() throws Exception {
        setHeight("550px");
        setWidth("1000px");
        
	testEntryLabel = new Label("<b>Test Entry:</b>");
	testEntryLabel.setContentMode(Label.CONTENT_XHTML);
	addComponent(testEntryLabel, "top:20px;left:30px");
		
	testEntryNameLabel = new Label();
	addComponent(testEntryNameLabel, "top:20px;left:100px");
		
	standardEntryLabel = new Label("<b>Standard Entry:</b>");
	standardEntryLabel.setContentMode(Label.CONTENT_XHTML);
	addComponent(standardEntryLabel, "top:20px;left:450px");
	
	standardEntryNameLabel = new Label();
	addComponent(standardEntryNameLabel, "top:20px;left:550px");
		
	resultsTable = new Table();
	resultsTable.setWidth("800px");
	resultsTable.setHeight("400px");
	resultsTable.setImmediate(true);
	resultsTable.setColumnCollapsingAllowed(true);
	resultsTable.setColumnReorderingAllowed(true);
		
	resultsTable.addContainerProperty(TRAIT_COLUMN_ID, String.class, null);
	resultsTable.addContainerProperty(NUM_OF_ENV_COLUMN_ID, Integer.class, null);
	resultsTable.addContainerProperty(NUM_SUP_COLUMN_ID, Integer.class, null);
	resultsTable.addContainerProperty(MEAN_STD_COLUMN_ID, Double.class, null);
	resultsTable.addContainerProperty(MEAN_TEST_COLUMN_ID, Double.class, null);
	resultsTable.addContainerProperty(MEAN_DIFF_COLUMN_ID, Double.class, null);
	resultsTable.addContainerProperty(PVAL_COLUMN_ID, Double.class, null);
		
	resultsTable.setColumnHeader(TRAIT_COLUMN_ID, "TRAIT");
	resultsTable.setColumnHeader(NUM_OF_ENV_COLUMN_ID, "# OF ENV");
	resultsTable.setColumnHeader(NUM_SUP_COLUMN_ID, "# SUP");
	resultsTable.setColumnHeader(MEAN_STD_COLUMN_ID, "MEAN STD");
	resultsTable.setColumnHeader(MEAN_TEST_COLUMN_ID, "MEAN TEST");
	resultsTable.setColumnHeader(MEAN_DIFF_COLUMN_ID, "MEAN DIFF");
	resultsTable.setColumnHeader(PVAL_COLUMN_ID, "PVAL");
		
	addComponent(resultsTable, "top:70px;left:30px");
    }

    public void populateResultsTable(Integer testEntryGID, Integer standardEntryGID){
        if(areCurrentGIDsDifferentFromGiven(testEntryGID, standardEntryGID)){
            this.resultsTable.removeAllItems();
            
            List<Result> results = getResults(testEntryGID, standardEntryGID);
            for(Result result : results){
                this.resultsTable.addItem(new Object[]{result.getTraitName(), result.getNumberOfEnvironments()
                        , result.getNumberOfSup(), result.getMeanStd(), result.getMeanTest(), result.getMeanDiff()
                        , result.getPval()}, result.getTraitName());
            }
            
            this.resultsTable.requestRepaint();
        }
    }
    
    private List<Result> getResults(Integer testEntryGID, Integer standardEntryGID){
        List<Result> toreturn = new ArrayList<Result>();
        
        if(testEntryGID == 50533 && standardEntryGID == 50532){
            toreturn.add(new Result("GRAIN_YIELD", Integer.valueOf(6), Integer.valueOf(5), Double.valueOf(3.86), Double.valueOf(3.16)
                    , Double.valueOf(0.7), Double.valueOf(0.06)));
            toreturn.add(new Result("PLANT_HEIGHT", Integer.valueOf(5), Integer.valueOf(4), Double.valueOf(79.1), Double.valueOf(91.2)
                    , Double.valueOf(-12.1), Double.valueOf(0.11)));
            toreturn.add(new Result("MATURITY", Integer.valueOf(5), Integer.valueOf(4), Double.valueOf(3.86), Double.valueOf(3.16)
                    , Double.valueOf(1), Double.valueOf(0.12)));
            toreturn.add(new Result("FLOWERING", Integer.valueOf(3), Integer.valueOf(3), Double.valueOf(3.27), Double.valueOf(0.16)
                    , Double.valueOf(0.7), Double.valueOf(0.12)));
            toreturn.add(new Result("BLB", Integer.valueOf(5), Integer.valueOf(1), Double.valueOf(3.86), Double.valueOf(3.16)
                    , Double.valueOf(0.7), Double.valueOf(0.06)));
        }
        
        return toreturn;
    }
    
    private boolean areCurrentGIDsDifferentFromGiven(Integer currentTestEntryGID, Integer currentStandardEntryGID){
        if(this.currentTestEntryGID != null && this.currentStandardEntryGID != null){
            if(this.currentTestEntryGID == currentTestEntryGID && this.currentStandardEntryGID == currentStandardEntryGID){
                return false;
            }
        }
        
        return true;
    }
    
    public void setEntriesLabel(String testEntryLabel, String standardEntryLabel){
        this.testEntryNameLabel.setValue(testEntryLabel);
        this.standardEntryNameLabel.setValue(standardEntryLabel);
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
    }
}
