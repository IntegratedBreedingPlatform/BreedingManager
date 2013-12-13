package org.generationcp.browser.cross.study.h2h;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.listeners.H2HComparisonQueryButtonClickListener;
import org.generationcp.browser.cross.study.h2h.pojos.TraitForComparison;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmDataManagerImpl;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window.Notification;

@Configurable
public class TraitsAvailableComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = 991899235025710803L;
    
    private final static Logger LOG = LoggerFactory.getLogger(TraitsAvailableComponent.class);
    
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
    
    private List<TraitForComparison> traitsForComparisonList;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
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
        this.traitsForComparisonList = tableItems;
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
    
    @SuppressWarnings("rawtypes")
    private List<TraitForComparison> getAvailableTraitsForComparison(Integer testEntryGID, Integer standardEntryGID){
        List<TraitForComparison> toreturn = new ArrayList<TraitForComparison>();
        
        try{
            Germplasm testEntry = this.germplasmDataManager.getGermplasmWithPrefName(testEntryGID);
            Germplasm standardEntry = this.germplasmDataManager.getGermplasmWithPrefName(standardEntryGID);
            
            String testEntryPrefName = null;
            if(testEntry.getPreferredName() != null){
                testEntryPrefName = testEntry.getPreferredName().getNval().trim();
            } else{
                MessageNotifier.showWarning(getWindow(), "Warning!", "The germplasm you selected as test entry doesn't have a preferred name, "
                    + "please select a different germplasm.", Notification.POSITION_CENTERED);
                return new ArrayList<TraitForComparison>();
            }
            
            String standardEntryPrefName = null;
            if(standardEntry.getPreferredName() != null){
                standardEntryPrefName = standardEntry.getPreferredName().getNval().trim();
            } else{
            MessageNotifier.showWarning(getWindow(), "Warning!", "The standard entry germplasm you selected as standard entry doesn't have a preferred name, "
                    + "please select a different germplasm.", Notification.POSITION_CENTERED);
                return new ArrayList<TraitForComparison>();
            }
            
            GermplasmDataManagerImpl dataManagerImpl = (GermplasmDataManagerImpl) this.germplasmDataManager;
            String queryString = "call h2h_traitXenv_summary('"+ testEntryPrefName + "','" + standardEntryPrefName + "')";
            Query query = dataManagerImpl.getCurrentSessionForCentral().createSQLQuery(queryString);
            List results = query.list();
            for(Object result : results){
                Object resultArray[] = (Object[]) result;
                String name = (String) resultArray[0];
                if(name != null){
                    name = name.trim().toUpperCase();
                }
                BigInteger numberOfEnvironments = (BigInteger) resultArray[1];
                toreturn.add(new TraitForComparison(name, numberOfEnvironments.intValue()));
            }
        } catch(MiddlewareQueryException ex){
            ex.printStackTrace();
            LOG.error("Database error!", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
            return new ArrayList<TraitForComparison>();
        } catch(Exception ex){
            ex.printStackTrace();
            LOG.error("Database error!", ex);
            MessageNotifier.showError(getWindow(), "Database Error!", messageSource.getMessage(Message.ERROR_REPORT_TO), Notification.POSITION_CENTERED);
            return new ArrayList<TraitForComparison>();
        }
        
        return toreturn;
    }
    
    public void nextButtonClickAction(){
        this.nextScreen.populateEnvironmentsTable(this.currentTestEntryGID, this.currentStandardEntryGID, this.traitsForComparisonList);
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
