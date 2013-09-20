package org.generationcp.browser.cross.study.h2h.main.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.cross.study.h2h.main.EnvironmentsAvailableComponent;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainButtonClickListener;
import org.generationcp.browser.cross.study.h2h.main.listeners.HeadToHeadCrossStudyMainValueChangeListener;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterByLocation;
import org.generationcp.browser.cross.study.h2h.main.pojos.FilterLocationDto;
import org.generationcp.browser.germplasmlist.listeners.CloseWindowAction;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
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
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class FilterLocationDialog extends Window implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7651767452229107837L;
    
    private final static Logger LOG = LoggerFactory.getLogger(FilterLocationDialog.class);
    
    public static final String CLOSE_SCREEN_BUTTON_ID = "FilterLocationDialog Close Button ID";
    public static final String APPLY_BUTTON_ID = "FilterLocationDialog Apply Button ID";
        	
    private static final String COUNTRY_LOCATION_COLUMN_ID = "FilterLocationDialog Country/Location Column Id";
    private static final String NUMBER_OF_ENV_COLUMN_ID = "FilterLocationDialog Number of Environments Column Id";
    private static final String TAG_COLUMN_ID = "FilterLocationDialog Tag Column Id";
    private static final String TAG_ALL = "FilterLocationDialog TAG_ALL Column Id";
    
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private Component source;
    private Label germplasmComponent;
    private Window parentWindow;
    
    private VerticalLayout mainLayout;
    
    private Button applyButton;
    private Button cancelButton;
    
    private TreeTable locationTreeTable;
    

    private Map<String, FilterByLocation> filterLocationCountryMap;
    public static String DELIMITER = "^^^^^^";
    private Label popupLabel;
    //private Map<String, Object[]> itemMap = new HashMap();
    private List<FilterLocationDto> checkFilterLocationLevel1DtoList = new ArrayList();
    private List<FilterLocationDto> checkFilterLocationLevel3DtoList = new ArrayList();
    private Map<String, CheckBox> locationCountryCheckBoxMap  = new HashMap();
    private Map<String, FilterLocationDto> locationCountryFilterDtoMap  = new HashMap();
    private Map<String, List<String>> countryLocationMapping = new HashMap();
    private CheckBox tagUnTagAll;
    
    
    public FilterLocationDialog(Component source, Window parentWindow, Map<String, FilterByLocation> filterLocationCountryMap){
        this.source = source;
        this.parentWindow = parentWindow;
        this.filterLocationCountryMap = filterLocationCountryMap;
       
        
        	
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("1000px");
        setHeight("530px");
        setResizable(false);
        setCaption("Filter by Location");
        // center window within the browser
        center();
        
        popupLabel = new Label("Specify filter by checking or unchecking countries/locations.");
        
         
        AbsoluteLayout mainLayout = new AbsoluteLayout();
        mainLayout.setWidth("1000px");
        mainLayout.setHeight("450px");
        
        
        initializeCountryLocationTable();
        //initializeProvinceTable();
        //initializeLocationStudyTable();
        
        showCountryLocationRows();
        

        tagUnTagAll = new CheckBox();
        tagUnTagAll.setValue(true);
        tagUnTagAll.setImmediate(true);
        tagUnTagAll.setData(TAG_ALL);
        tagUnTagAll.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, true));
        
        
        
        mainLayout.addComponent(popupLabel, "top:10px;left:20px");
        mainLayout.addComponent(locationTreeTable, "top:30px;left:20px");
        
        mainLayout.addComponent(tagUnTagAll, "top:32px;left:800px");
        
        
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
        mainLayout.addComponent(buttonLayout, "top:420px;left:810px");
        
        
        addComponent(mainLayout);
    }

    private void showCountryLocationRows(){
    	for(String countryName : filterLocationCountryMap.keySet()){
    		FilterByLocation filterByLocation = filterLocationCountryMap.get(countryName);
    		FilterLocationDto filterLocationDto = new FilterLocationDto(countryName, null, null, null, 1);
    		CheckBox box = new CheckBox();
    		box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, filterLocationDto));
    		box.setValue(true);
    		box.setImmediate(true);
    		//Object[] countryObj = ;
			Object countryObj = locationTreeTable.addItem(new Object[] {countryName, filterByLocation.getNumberOfEnvironmentForCountry(), box}, countryName);
			locationCountryCheckBoxMap.put(countryName, box);
			locationCountryFilterDtoMap.put(countryName, filterLocationDto);
			List<String> keyList = new ArrayList();
			for(String locationNames : filterByLocation.getListOfLocationNames()){
				CheckBox boxLocation = new CheckBox();
				FilterLocationDto filterLocationDto1 = new FilterLocationDto(countryName, null, locationNames, null, 3);
				boxLocation.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, filterLocationDto1));
				boxLocation.setImmediate(true);
				String key = countryName+FilterLocationDialog.DELIMITER+locationNames; 
				boxLocation.setValue(true);
				Object locationObj = locationTreeTable.addItem(new Object[] {locationNames, filterByLocation.getNumberOfEnvironmentForLocation(locationNames), boxLocation}, key);
				
				locationTreeTable.setParent(locationObj, countryObj);
				locationCountryCheckBoxMap.put(key, boxLocation);
				keyList.add(key);
				locationCountryFilterDtoMap.put(key, filterLocationDto1);
				locationTreeTable.setChildrenAllowed(locationObj, false);		      

			}   
			locationTreeTable.setCollapsed(countryObj, false);

			countryLocationMapping.put(countryName, keyList);
    	}
    }
    private void initializeCountryLocationTable(){
    	locationTreeTable = new TreeTable();
    	locationTreeTable.setWidth("900px");
    	locationTreeTable.setHeight("350px");
    	locationTreeTable.setImmediate(true);
    	locationTreeTable.setPageLength(-1);
        //entriesTable.setCacheRate(cacheRate)
    	locationTreeTable.setSelectable(true);
    	locationTreeTable.setMultiSelect(true);
        locationTreeTable.setNullSelectionAllowed(false);
        
        
        
        locationTreeTable.addContainerProperty(COUNTRY_LOCATION_COLUMN_ID, String.class, null);
        locationTreeTable.addContainerProperty(NUMBER_OF_ENV_COLUMN_ID, String.class, null);
        locationTreeTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        
        
        locationTreeTable.setColumnHeader(COUNTRY_LOCATION_COLUMN_ID, "Country/Location");
        locationTreeTable.setColumnHeader(NUMBER_OF_ENV_COLUMN_ID, "# of Environments");
        locationTreeTable.setColumnHeader(TAG_COLUMN_ID, "Tag");
        
        locationTreeTable.setColumnWidth(COUNTRY_LOCATION_COLUMN_ID, 607);
        locationTreeTable.setColumnWidth(NUMBER_OF_ENV_COLUMN_ID, 120);
        locationTreeTable.setColumnWidth(TAG_COLUMN_ID, 115);
        
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
    	
    	
    	
    	if(filterLocationDto.getLevel() == 1){
    		if(val){
    			//we check all the location
    			List<String> locationList = countryLocationMapping.get(filterLocationDto.getCountryName());
    			if(locationList != null){
	    			for(String locKey : locationList){
	    				CheckBox checkBox = locationCountryCheckBoxMap.get(locKey);
	    				checkBox.setValue(true);
	    			}
    			}
    		}else{
    			List<String> locationList = countryLocationMapping.get(filterLocationDto.getCountryName());
    			if(locationList != null){
	    			for(String locKey : locationList){
	    				CheckBox checkBox = locationCountryCheckBoxMap.get(locKey);
	    				checkBox.setValue(false);
	    			}
    			}
    		}
    		//tempList = checkFilterLocationLevel1DtoList;
    	}
    	else if(filterLocationDto.getLevel() == 3){
    		//tempList = checkFilterLocationLevel3DtoList;
    		if(val){
    			
    			Map<CheckBox, Boolean> prevStateMap = new HashMap();
				List<String> locationList = countryLocationMapping.get(filterLocationDto.getCountryName());
    			if(locationList != null){
	    			for(String locKey : locationList){
	    				CheckBox checkBox = locationCountryCheckBoxMap.get(locKey);	   
	    				if((Boolean)checkBox.getValue() == false)
	    					prevStateMap.put(checkBox, (Boolean)checkBox.getValue());
	    			}
    			}
    			
				CheckBox checkBoxCountry = locationCountryCheckBoxMap.get(filterLocationDto.getCountryName());
				checkBoxCountry.setValue(true);
				Iterator<CheckBox> iter = prevStateMap.keySet().iterator();
				while(iter.hasNext()){
					CheckBox temp = iter.next();
					temp.setValue(prevStateMap.get(temp));
				}
    			
    		}else{
    			//we check all the location
    			List<String> locationList = countryLocationMapping.get(filterLocationDto.getCountryName());
    			boolean isAtLeast1Check = false;
    			if(locationList != null){
	    			for(String locKey : locationList){
	    				CheckBox checkBox = locationCountryCheckBoxMap.get(locKey);
	    				//checkBox.setValue(true);
	    				if((Boolean)checkBox.getValue()){
	    					isAtLeast1Check = true;
	    				}
	    			}
    			}
    			if(isAtLeast1Check == false){
    				CheckBox checkBox = locationCountryCheckBoxMap.get(filterLocationDto.getCountryName());
    				checkBox.setValue(false);
    			}
    		}
    	}
    	/*
    	if(val){
    		tempList.add(filterLocationDto);
    	}else{
    		tempList.remove(filterLocationDto);
    	}
    	*/
    	
    	
    	setupApplyButton();
    }
    
    public void clickCheckBoxTag(boolean val){
    	for(String sKey : countryLocationMapping.keySet()){
        	CheckBox temp  = locationCountryCheckBoxMap.get(sKey);        	
        	temp.setValue(val);
        }
    	setupApplyButton();
    	((EnvironmentsAvailableComponent)source).reopenFilterWindow();
    }
    
    public void clickApplyButton(){
    	
        checkFilterLocationLevel1DtoList = new ArrayList();
        checkFilterLocationLevel3DtoList = new ArrayList();
        for(String sKey : locationCountryCheckBoxMap.keySet()){
        	CheckBox temp  = locationCountryCheckBoxMap.get(sKey);
        	if((Boolean)temp.getValue()){
        		FilterLocationDto dto = locationCountryFilterDtoMap.get(sKey);
        		if(dto.getLevel() == 1){
        			checkFilterLocationLevel1DtoList.add(dto);
        		}else if(dto.getLevel() == 3){
        			checkFilterLocationLevel3DtoList.add(dto);
        		}
        	}
        }
    	((EnvironmentsAvailableComponent)source).clickFilterByLocationApply(checkFilterLocationLevel1DtoList, checkFilterLocationLevel3DtoList);
    }
    
    public void initializeButtons(){
    	setupApplyButton();
    }
    
    private void setupApplyButton(){
    	if(applyButton != null){
	    	for(CheckBox checkBox : locationCountryCheckBoxMap.values()){
	    		if((Boolean)checkBox.getValue()){
	    			applyButton.setEnabled(true);
	    			break;
	    		}else{
	    			applyButton.setEnabled(false);
	    		}
	    	}
    	}
    	//locationTreeTable.requestRepaintAll();
    }
    
    @Override
    public void updateLabels() {
        // TODO Auto-generated method stub
        
    }
}
