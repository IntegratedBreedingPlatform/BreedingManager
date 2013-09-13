package org.generationcp.browser.cross.study.h2h.main.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.cross.study.h2h.main.EnvironmentsAvailableComponent;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.h2h.main.SpecifyGermplasmsComponent;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterByLocation;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.browser.cross.study.h2h.main.pojos.LocationStudyDto;
import org.generationcp.browser.germplasm.GermplasmQueries;
import org.generationcp.browser.germplasm.GermplasmSearchFormComponent;
import org.generationcp.browser.germplasm.GermplasmSearchResultComponent;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.browser.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class FilterStudyDialog extends Window implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7651767452229107837L;
    
    private final static Logger LOG = LoggerFactory.getLogger(FilterLocationDialog.class);
    
    public static final String CLOSE_SCREEN_BUTTON_ID = "FilterStudyDialog Close Button ID";
    public static final String APPLY_BUTTON_ID = "FilterStudyDialog Apply Button ID";
       	
    private static final String STUDY_NAME_COLUMN_ID = "FilterStudyDialog Study Name Column Id";
    private static final String STUDY_DESCRIPTION_COLUMN_ID = "FilterStudyDialog Study Description Column Id";
    private static final String NUMBER_OF_ENV_COLUMN_ID = "FilterStudyDialog Number of Environments Column Id";
    private static final String TAG_COLUMN_ID = "FilterStudyDialog Tag Column Id";
    
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private Component source;
    private Window parentWindow;
    
    private VerticalLayout mainLayout;
    
    private Button applyButton;
    private Button cancelButton;
    
    private Table studyTable;

    private Map<String, List<StudyReference>> filterStudyMap;
    public static String DELIMITER = "^^^^^^";
    private Label popupLabel;
    private Map<String, Object[]> itemMap = new HashMap();
    private List<FilterLocationDto> checkFilterLocationLevel4DtoList = new ArrayList();    
    
    
    public FilterStudyDialog(Component source, Window parentWindow, Map<String, List<StudyReference>> filterStudyMap){
        this.source = source;
        this.parentWindow = parentWindow;
        this.filterStudyMap = filterStudyMap;                        	
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("800px");
        setHeight("530px");
        setResizable(false);
        setCaption("Filter by Study pop-up screen");
        // center window within the browser
        center();
        
        popupLabel = new Label("Filter by Study");
        
         
        AbsoluteLayout mainLayout = new AbsoluteLayout();
        mainLayout.setWidth("800px");
        mainLayout.setHeight("450px");
        
        initializeStudyTable();
      
        
        showStudyRows();
        
        
       
        
        
        
        mainLayout.addComponent(popupLabel, "top:10px;left:20px");
        mainLayout.addComponent(studyTable, "top:30px;left:20px");
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        
        
        cancelButton = new Button("Cancel");
        cancelButton.setData(CLOSE_SCREEN_BUTTON_ID);
        cancelButton.addListener(new CloseWindowAction());
        
        
        String buttonlabel = "Apply";
       
        applyButton = new Button(buttonlabel);
        applyButton.setData(APPLY_BUTTON_ID);
        applyButton.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this));
        applyButton.addListener(new CloseWindowAction());
        applyButton.setEnabled(false);
        
        buttonLayout.addComponent(cancelButton);
        buttonLayout.addComponent(applyButton);
        //buttonLayout.setComponentAlignment(doneButton, Alignment.MIDDLE_RIGHT);
        //buttonLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);
        mainLayout.addComponent(buttonLayout, "top:420px;left:600px");
        
        
        addComponent(mainLayout);
    }

    private void showStudyRows(){
    	for(String studyKey : filterStudyMap.keySet()){
    		List<StudyReference> studyReferenceList = filterStudyMap.get(studyKey);
    		StudyReference studyRef = studyReferenceList.get(0);
    		
    		Object[] itemObj = itemMap.get(studyKey);
    		if(itemObj == null){
	    		//item = countriesTable.addItem(countryName);
	   		 	CheckBox box = new CheckBox();
	   		 	box.setImmediate(true);
	   		 	FilterLocationDto filterLocationDto = new FilterLocationDto(null, null, null, studyRef.getName(), 4);
	   		 	//box.setValue(filterLocationDto);
	            //item.getItemProperty(TAG_COLUMN_ID).setValue(box);
	            
	            /*
	            item.getItemProperty(COUNTRY_COLUMN_ID).setValue(countryNameLink);
	            
	            item.getItemProperty(NUMBER_OF_ENV_COLUMN_ID).setValue(filterByLocation.getNumberOfEnvironmentForCountry());
	            */
	            box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, filterLocationDto));
	          
	            itemObj = new Object[] {box, studyRef.getName(), studyRef.getDescription(), studyReferenceList.size()};
	            studyTable.addItem(itemObj, studyKey);
	            
	            itemMap.put(studyKey, itemObj);
    		}else{
    			studyTable.addItem(itemObj, studyKey);
				
    		}
    		
    	}
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
        
        studyTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        studyTable.addContainerProperty(STUDY_NAME_COLUMN_ID, String.class, null);
        studyTable.addContainerProperty(STUDY_DESCRIPTION_COLUMN_ID, String.class, null);
        studyTable.addContainerProperty(NUMBER_OF_ENV_COLUMN_ID, Integer.class, null);
        
        
        studyTable.setColumnHeader(TAG_COLUMN_ID, "Tag");
        studyTable.setColumnHeader(STUDY_NAME_COLUMN_ID, "Study Name");
        studyTable.setColumnHeader(STUDY_DESCRIPTION_COLUMN_ID, "Study Title");
        studyTable.setColumnHeader(NUMBER_OF_ENV_COLUMN_ID, "# of Environments");
        
        
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
    	List tempList = new ArrayList();
    	if(filterLocationDto.getLevel() == 4)
    		tempList = checkFilterLocationLevel4DtoList;
    	
    	if(val){
    		tempList.add(filterLocationDto);
    	}else{
    		tempList.remove(filterLocationDto);
    	}
    	setupApplyButton();
    }
    
    public void clickApplyButton(){
    	((EnvironmentsAvailableComponent)source).clickFilterByStudyApply(checkFilterLocationLevel4DtoList);
    }
    
    public void initializeButtons(){
    	setupApplyButton();
    }
    
    private void setupApplyButton(){
    	if(!checkFilterLocationLevel4DtoList.isEmpty()){
    		applyButton.setEnabled(true);
    	}else{
    		applyButton.setEnabled(false);
    	}
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
        
    }
}
