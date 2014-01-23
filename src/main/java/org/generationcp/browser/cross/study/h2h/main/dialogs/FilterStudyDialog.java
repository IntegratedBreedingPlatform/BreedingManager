package org.generationcp.browser.cross.study.h2h.main.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.cross.study.commons.EnvironmentFilter;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class FilterStudyDialog extends Window implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7651767452229107837L;
    
    @SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory.getLogger(FilterLocationDialog.class);
    
    public static final String CLOSE_SCREEN_BUTTON_ID = "FilterStudyDialog Close Button ID";
    public static final String APPLY_BUTTON_ID = "FilterStudyDialog Apply Button ID";
    public static final String STUDY_BUTTON_ID = "FilterStudyDialog Study Button ID";
       	
    private static final String STUDY_NAME_COLUMN_ID = "FilterStudyDialog Study Name Column Id";
    private static final String STUDY_DESCRIPTION_COLUMN_ID = "FilterStudyDialog Study Description Column Id";
    private static final String NUMBER_OF_ENV_COLUMN_ID = "FilterStudyDialog Number of Environments Column Id";
    private static final String TAG_COLUMN_ID = "FilterStudyDialog Tag Column Id";
    
    private Component source;
    private Window parentWindow;
    
    private Button applyButton;
    private Button cancelButton;
    
    private Table studyTable;

    private Map<String, List<StudyReference>> filterStudyMap;
    public static String DELIMITER = "^^^^^^";
    private Label popupLabel;
    private Map<String, CheckBox> checkBoxMap = new HashMap<String, CheckBox>();
    private List<FilterLocationDto> checkFilterLocationLevel4DtoList = new ArrayList<FilterLocationDto>();    
    private CheckBox tagUnTagAll;
    boolean h2hCall=true;
    
    private String windowName; 
    
    public FilterStudyDialog(Component source, Window parentWindow, Map<String, List<StudyReference>> filterStudyMap){
        this.source = source;
        this.parentWindow = parentWindow;
        this.filterStudyMap = filterStudyMap;                        	
    }

    public FilterStudyDialog(Component source, Window parentWindow, Map<String, List<StudyReference>> filterStudyMap, String windowName){
        this.source = source;
        this.parentWindow = parentWindow;
        this.filterStudyMap = filterStudyMap;                        	
        this.windowName = windowName;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        //set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("785px");
        setHeight("530px");
        setResizable(false);
        setCaption("Filter by Study");
        // center window within the browser
        center();
        
        popupLabel = new Label("Specify filter by checking or unchecking studies.");
        
         
        AbsoluteLayout mainLayout = new AbsoluteLayout();
        mainLayout.setWidth("780px");
        mainLayout.setHeight("450px");
        
        initializeStudyTable();
      
        
        showStudyRows();
        
        tagUnTagAll = new CheckBox();
        tagUnTagAll.setValue(true);
        tagUnTagAll.setImmediate(true);
        tagUnTagAll.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, true));
       
        
        
        
        mainLayout.addComponent(popupLabel, "top:10px;left:20px");
        mainLayout.addComponent(studyTable, "top:30px;left:20px");
        
        mainLayout.addComponent(tagUnTagAll, "top:33px;left:630px");
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        
        
        cancelButton = new Button("Cancel");
        cancelButton.setData(CLOSE_SCREEN_BUTTON_ID);
        cancelButton.addListener(new CloseWindowAction());
        
        
        String buttonlabel = "Apply";
       
        applyButton = new Button(buttonlabel);
        applyButton.setData(APPLY_BUTTON_ID);
        applyButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this,source));
        applyButton.addListener(new CloseWindowAction());
        applyButton.setEnabled(false);
        applyButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
        
        buttonLayout.addComponent(cancelButton);
        buttonLayout.addComponent(applyButton);
        //buttonLayout.setComponentAlignment(doneButton, Alignment.MIDDLE_RIGHT);
        //buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);
        mainLayout.addComponent(buttonLayout, "top:420px;left:587px");
        
        
        addComponent(mainLayout);
    }

    public void clickCheckBoxTag(boolean val, String className){
    	/*
    	for(String sKey : countryLocationMapping.keySet()){
        	CheckBox temp  = locationCountryCheckBoxMap.get(sKey);        	
        	temp.setValue(val);
        }
    	setupApplyButton();
    	((EnvironmentsAvailableComponent)source).reopenFilterWindow();
    	*/
    	java.util.Iterator<CheckBox> checkboxes = checkBoxMap.values().iterator();
    	while(checkboxes.hasNext()){
    		CheckBox box = checkboxes.next();
    		box.setValue(val);
    	}
    	setupApplyButton();		
    	
        if(className.equals("EnvironmentFilter")){
        	((EnvironmentFilter)source).reopenFilterStudyWindow();
        }
    }
    private void showStudyRows(){
    	for(String studyKey : filterStudyMap.keySet()){
    		List<StudyReference> studyReferenceList = filterStudyMap.get(studyKey);
    		StudyReference studyRef = studyReferenceList.get(0);
    		
    		 	CheckBox box = new CheckBox();
	   		 	box.setImmediate(true);
	   		 	FilterLocationDto filterLocationDto = new FilterLocationDto(null, null, null, studyRef.getName(), 4);
	   		 	
	            box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, filterLocationDto));
	            
	        	Button studyNameLink = new Button(studyRef.getName());
	        	studyNameLink.setImmediate(true);
	        	studyNameLink.setStyleName(Reindeer.BUTTON_LINK);
	        	studyNameLink.setData(STUDY_BUTTON_ID);
	        	studyNameLink.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this, null, null, studyRef.getId()));
	        	
	            Object[] itemObj = new Object[] {studyNameLink, studyRef.getDescription(), studyReferenceList.size(), box};
	            studyTable.addItem(itemObj, studyKey);
	            checkBoxMap.put(studyKey, box);   		
	            box.setValue(true);
    	}
    }
    
    public void showStudyInfo(Integer studyId){
    	if(parentWindow==null && windowName!=null)
    		parentWindow = this.getApplication().getWindow(windowName);
    	
    	StudyInfoDialog studyInfoDialog = new StudyInfoDialog(this, this.parentWindow, studyId,h2hCall);
    	studyInfoDialog.addStyleName(Reindeer.WINDOW_LIGHT);
    	this.parentWindow.addWindow(studyInfoDialog);
    }
    
    private void initializeStudyTable(){
    	studyTable = new Table();
    	studyTable.setWidth("700px");
    	studyTable.setHeight("350px");
        studyTable.setImmediate(true);
        studyTable.setPageLength(-1);
        //entriesTable.setCacheRate(cacheRate)
        studyTable.setSelectable(true);
        studyTable.setMultiSelect(true);
        studyTable.setNullSelectionAllowed(false);
        
        
        studyTable.addContainerProperty(STUDY_NAME_COLUMN_ID, Button.class, null);
        studyTable.addContainerProperty(STUDY_DESCRIPTION_COLUMN_ID, String.class, null);
        studyTable.addContainerProperty(NUMBER_OF_ENV_COLUMN_ID, Integer.class, null);
        studyTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        
        
        studyTable.setColumnHeader(STUDY_NAME_COLUMN_ID, "Study Name");
        studyTable.setColumnHeader(STUDY_DESCRIPTION_COLUMN_ID, "Study Title");
        studyTable.setColumnHeader(NUMBER_OF_ENV_COLUMN_ID, "# of Environments");
        studyTable.setColumnHeader(TAG_COLUMN_ID, "Tag");
        
        studyTable.setColumnWidth(STUDY_NAME_COLUMN_ID, 111);
        studyTable.setColumnWidth(STUDY_DESCRIPTION_COLUMN_ID, 295);
        studyTable.setColumnWidth(NUMBER_OF_ENV_COLUMN_ID, 130);
        studyTable.setColumnWidth(TAG_COLUMN_ID, 110);
        
    }
    
    
    
    
    public void applyButtonClickAction(){
        // apply to previous screen the filter
    }

    public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        sourceTable.select(itemId);
        //this.selectedGid = Integer.valueOf(item.getItemProperty(GID).toString());
        //this.doneButton.setEnabled(true);
    }
    
    
    public void clickCheckBox(boolean val, FilterLocationDto filterLocationDto){
    	List<FilterLocationDto> tempList = new ArrayList<FilterLocationDto>();
    	if(filterLocationDto.getLevel() == 4)
    		tempList = checkFilterLocationLevel4DtoList;
    	
    	if(val){
    		tempList.add(filterLocationDto);
    	}else{
    		tempList.remove(filterLocationDto);
    	}
    	setupApplyButton();
    }
    
    public void clickApplyButton(String classname){
    	if(classname.equals("EnvironmentFilter")){
    		((EnvironmentFilter)source).clickFilterByStudyApply(checkFilterLocationLevel4DtoList);
    	} 
    }
    
    public void initializeButtons(){
    	setupApplyButton();
    }
    
    private void setupApplyButton(){
    	if(applyButton != null){
	    	if(!checkFilterLocationLevel4DtoList.isEmpty()){
	    		applyButton.setEnabled(true);
	    	}else{
	    		applyButton.setEnabled(false);
	    	}
    	}
    }
    
    @Override
    public void updateLabels() {
        
    }
}
