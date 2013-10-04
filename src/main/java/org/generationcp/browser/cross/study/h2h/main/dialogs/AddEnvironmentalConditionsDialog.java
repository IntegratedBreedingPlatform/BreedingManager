package org.generationcp.browser.cross.study.h2h.main.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.main.EnvironmentsAvailableComponent;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.dms.TrialEnvironmentProperty;
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
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class AddEnvironmentalConditionsDialog extends Window implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	private final static Logger LOG = LoggerFactory.getLogger(AddEnvironmentalConditionsDialog.class);
    
    public static final String CLOSE_SCREEN_BUTTON_ID = "AddEnvironmentalConditionsDialog Close Button ID";
    public static final String APPLY_BUTTON_ID = "AddEnvironmentalConditionsDialog Apply Button ID";
    public static final String STUDY_BUTTON_ID = "AddEnvironmentalConditionsDialog Study Button ID";
       	
    private static final String CONDITION_COLUMN_ID = "AddEnvironmentalConditionsDialog Condition Column Id";
    private static final String DESCRIPTION_COLUMN_ID = "AddEnvironmentalConditionsDialog Description Column Id";
    private static final String NUMBER_OF_ENV_COLUMN_ID = "AddEnvironmentalConditionsDialog Number of Environments Column Id";
    private static final String TAG_COLUMN_ID = "AddEnvironmentalConditionsDialog Tag Column Id";
    
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private CrossStudyDataManager crossStudyDataManager;
    
    private Component source;
    private Window parentWindow;
    private List<Integer> environmentIds;
    
    private VerticalLayout mainLayout;
    
    private Button applyButton;
    private Button cancelButton;
    
    private Table conditionsTable;

    public static String DELIMITER = "^^^^^^";
    private Label popupLabel;
    private Map<String, CheckBox> checkBoxMap = new HashMap();
    private List<String> conditionNames = new ArrayList<String>();
    private Set<TrialEnvironmentProperty> selectedProperties = new HashSet<TrialEnvironmentProperty>();    
    private CheckBox tagUnTagAll;
    
    public AddEnvironmentalConditionsDialog(Component source, Window parentWindow, List<Integer> environmentIds){
        this.source = source;
        this.parentWindow = parentWindow;
        this.environmentIds = environmentIds;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("800px");
        setHeight("530px");
        setResizable(false);
        setCaption(messageSource.getMessage(Message.ADD_ENVT_CONDITION_COLUMNS_LABEL));
        // center window within the browser
        center();
        
        popupLabel = new Label(messageSource.getMessage(Message.SELECTED_ENVT_CONDITIONS_WILL_BE_ADDED));
        
         
        AbsoluteLayout mainLayout = new AbsoluteLayout();
        mainLayout.setWidth("800px");
        mainLayout.setHeight("450px");
        
        initializeConditionsTable();
        populateConditionsTable();
        
        tagUnTagAll = new CheckBox();
        tagUnTagAll.setValue(true);
        tagUnTagAll.setImmediate(true);
        tagUnTagAll.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, true));
                
        mainLayout.addComponent(popupLabel, "top:10px;left:20px");
        mainLayout.addComponent(conditionsTable, "top:30px;left:20px");
        
        mainLayout.addComponent(tagUnTagAll, "top:30px;left:620px");
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        
        
        cancelButton = new Button();
        cancelButton.setData(CLOSE_SCREEN_BUTTON_ID);
        cancelButton.addListener(new CloseWindowAction());
        
               
        applyButton = new Button();
        applyButton.setData(APPLY_BUTTON_ID);
        applyButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        applyButton.addListener(new CloseWindowAction());
        //TODO disable
        applyButton.setEnabled(true);
        
        buttonLayout.addComponent(cancelButton);
        buttonLayout.addComponent(applyButton);
        mainLayout.addComponent(buttonLayout, "top:420px;left:600px");
        
        
        addComponent(mainLayout);
    }

    public void clickCheckBoxTag(boolean val){
    	java.util.Iterator<CheckBox> checkboxes = checkBoxMap.values().iterator();
    	while(checkboxes.hasNext()){
    		CheckBox box = checkboxes.next();
    		box.setValue(val);
    	}
    	((EnvironmentsAvailableComponent)source).reopenAddEnvironmentConditionsWindow();
    	
    }
    
    private void populateConditionsTable() throws MiddlewareQueryException{
    	List<TrialEnvironmentProperty> properties =  
    		crossStudyDataManager.getPropertiesForTrialEnvironments(environmentIds);
    	this.selectedProperties = new HashSet<TrialEnvironmentProperty>();
    	this.conditionNames = new ArrayList<String>();
    	
    	for (TrialEnvironmentProperty environmentCondition : properties){
    		CheckBox box = new CheckBox();
   		 	box.setImmediate(true);
   		 	box.setValue(true);
   		 	box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, environmentCondition));
   		 	
    		String condition = environmentCondition.getName();
			Object[] itemObj = new Object[]{ environmentCondition.getName(), environmentCondition.getDescription(), 
    				environmentCondition.getNumberOfEnvironments(), box};
    		conditionsTable.addItem(itemObj, environmentCondition);
    		
    		this.selectedProperties.add(environmentCondition);
    		this.conditionNames.add(condition);
    		
    		this.checkBoxMap.put(condition, box);
    	}
    }
    
    private void initializeConditionsTable(){
    	conditionsTable = new Table();
    	conditionsTable.setWidth("700px");
    	conditionsTable.setHeight("350px");
        conditionsTable.setImmediate(true);
        conditionsTable.setPageLength(-1);
        //entriesTable.setCacheRate(cacheRate)
        conditionsTable.setSelectable(true);
        conditionsTable.setMultiSelect(true);
        conditionsTable.setNullSelectionAllowed(false);
        
        
        conditionsTable.addContainerProperty(CONDITION_COLUMN_ID, String.class, null);
        conditionsTable.addContainerProperty(DESCRIPTION_COLUMN_ID, String.class, null);
        conditionsTable.addContainerProperty(NUMBER_OF_ENV_COLUMN_ID, Integer.class, null);
        conditionsTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        
        
        conditionsTable.setColumnHeader(CONDITION_COLUMN_ID, messageSource.getMessage(Message.CONDITION_HEADER));
        conditionsTable.setColumnHeader(DESCRIPTION_COLUMN_ID, messageSource.getMessage(Message.DESCRIPTION_HEADER));
        conditionsTable.setColumnHeader(NUMBER_OF_ENV_COLUMN_ID, messageSource.getMessage(Message.NUMBER_OF_ENVIRONMENTS_HEADER));
        conditionsTable.setColumnHeader(TAG_COLUMN_ID, messageSource.getMessage(Message.HEAD_TO_HEAD_TAG));
        
        conditionsTable.setColumnWidth(CONDITION_COLUMN_ID, 120);
        conditionsTable.setColumnWidth(DESCRIPTION_COLUMN_ID, 300);
        conditionsTable.setColumnWidth(NUMBER_OF_ENV_COLUMN_ID, 116);
        conditionsTable.setColumnWidth(TAG_COLUMN_ID, 110);
        
    }
    
    
    
    
    public void applyButtonClickAction(){
        // apply to previous screen the filter
    }

    public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        sourceTable.select(itemId);
        //this.selectedGid = Integer.valueOf(item.getItemProperty(GID).toString());
        //this.doneButton.setEnabled(true);
    }
    
    
    public void clickCheckBox(boolean val, TrialEnvironmentProperty environmentCondition){
    	if(val){
    		selectedProperties.add(environmentCondition);
    	}else{
    		selectedProperties.remove(environmentCondition);
    	}
    }
    
    public void clickApplyButton(){
    	((EnvironmentsAvailableComponent)source).addEnviromentalConditionColumns(this.conditionNames, this.selectedProperties);
    }
    
    
    @Override
    public void attach() {
    	super.attach();
    	updateLabels();
    }
    
    @Override
    public void updateLabels() {
        messageSource.setCaption(applyButton, Message.DONE);
        messageSource.setCaption(cancelButton, Message.CANCEL_LABEL);
    }
}
