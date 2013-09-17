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
public class FilterLocationDialog extends Window implements InitializingBean, InternationalizableComponent {

    private static final long serialVersionUID = -7651767452229107837L;
    
    private final static Logger LOG = LoggerFactory.getLogger(FilterLocationDialog.class);
    
    public static final String CLOSE_SCREEN_BUTTON_ID = "FilterLocationDialog Close Button ID";
    public static final String APPLY_BUTTON_ID = "FilterLocationDialog Apply Button ID";
    public static final String COUNTRY_BUTTON_ID = "FilterLocationDialog Country Button ID";
    public static final String PROVINCE_BUTTON_ID = "FilterLocationDialog Province Button ID";
        	
    private static final String COUNTRY_COLUMN_ID = "FilterLocationDialog Trait Column Id";
    private static final String NUMBER_OF_ENV_COLUMN_ID = "FilterLocationDialog Number of Environments Column Id";
    private static final String TAG_COLUMN_ID = "FilterLocationDialog Tag Column Id";
    private static final String PROVINCE_COLUMN_ID = "FilterLocationDialog Province Column Id";
    private static final String LOCATION_COLUMN_ID = "FilterLocationDialog Location Column Id";
    private static final String STUDY_COLUMN_ID = "FilterLocationDialog Study Column Id";
    
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    private Component source;
    private Label germplasmComponent;
    private Window parentWindow;
    
    private VerticalLayout mainLayout;
    
    private Button applyButton;
    private Button cancelButton;
    
    private Table countriesTable;
    private Table provinceTable;
    private Table locationTable;
    private Map<String, FilterByLocation> filterLocationCountryMap;
    public static String DELIMITER = "^^^^^^";
    private Label popupLabel;
    private Map<String, Object[]> itemMap = new HashMap();
    private List<FilterLocationDto> checkFilterLocationLevel1DtoList = new ArrayList();
    private List<FilterLocationDto> checkFilterLocationLevel2DtoList = new ArrayList();
    private List<FilterLocationDto> checkFilterLocationLevel3DtoList = new ArrayList();
    private ThemeResource resource;
    
    
    public FilterLocationDialog(Component source, Window parentWindow, Map<String, FilterByLocation> filterLocationCountryMap){
        this.source = source;
        this.parentWindow = parentWindow;
        this.filterLocationCountryMap = filterLocationCountryMap;
        setTheme("gcp-default");
       
        
        	
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("1200px");
        setHeight("530px");
        setResizable(false);
        setCaption("Filter by Location pop-up screen");
        // center window within the browser
        center();
        
        popupLabel = new Label("Filter by Location");
        
         
        AbsoluteLayout mainLayout = new AbsoluteLayout();
        mainLayout.setWidth("1200px");
        mainLayout.setHeight("450px");
        
        initializeCountryTable();
        initializeProvinceTable();
        initializeLocationStudyTable();
        
        showCountryRows();
        
        resource = new ThemeResource("image/arrow-right.png");	        
	     // Use the resource
        Embedded rightArrow1 = new Embedded("", resource);
        Embedded rightArrow2 = new Embedded("", resource);
       
        
        
        
        mainLayout.addComponent(popupLabel, "top:10px;left:20px");
        mainLayout.addComponent(countriesTable, "top:30px;left:20px");
        mainLayout.addComponent(provinceTable, "top:30px;left:390px");
        mainLayout.addComponent(locationTable, "top:30px;left:780px");
        
        mainLayout.addComponent(rightArrow1, "top:175px;left:335px");
        mainLayout.addComponent(rightArrow2, "top:175px;left:725px");
        
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
        mainLayout.addComponent(buttonLayout, "top:420px;left:950px");
        
        
        addComponent(mainLayout);
    }

    private void showCountryRows(){
    	for(String countryName : filterLocationCountryMap.keySet()){
    		FilterByLocation filterByLocation = filterLocationCountryMap.get(countryName);
    		
    		Object[] itemObj = itemMap.get(countryName);
    		if(itemObj == null){
	    		//item = countriesTable.addItem(countryName);
	   		 	CheckBox box = new CheckBox();
	   		 	box.setImmediate(true);
	   		 	FilterLocationDto filterLocationDto = new FilterLocationDto(countryName, null, null, null, 1);
	   		 	//box.setValue(filterLocationDto);
	            //item.getItemProperty(TAG_COLUMN_ID).setValue(box);
	            Button countryNameLink = new Button(countryName);
	            countryNameLink.setImmediate(true);
	            countryNameLink.setStyleName(Reindeer.BUTTON_LINK);
	            countryNameLink.setData(COUNTRY_BUTTON_ID);
	            countryNameLink.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this, countryName));
	            /*
	            item.getItemProperty(COUNTRY_COLUMN_ID).setValue(countryNameLink);
	            
	            item.getItemProperty(NUMBER_OF_ENV_COLUMN_ID).setValue(filterByLocation.getNumberOfEnvironmentForCountry());
	            */
	            box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, filterLocationDto));
	          
	            itemObj = new Object[] {box, countryNameLink, filterByLocation.getNumberOfEnvironmentForCountry()};
	            countriesTable.addItem(itemObj, countryName);
	            
	            itemMap.put(countryName, itemObj);
    		}else{
    			countriesTable.addItem(itemObj, countryName);
				
    		}
    		
    	}
    }
    private void initializeCountryTable(){
    	countriesTable = new Table();
        countriesTable.setWidth("310px");
        countriesTable.setHeight("350px");
        countriesTable.setImmediate(true);
        countriesTable.setPageLength(-1);
        //entriesTable.setCacheRate(cacheRate)
        countriesTable.setSelectable(true);
        countriesTable.setMultiSelect(true);
        countriesTable.setNullSelectionAllowed(false);
        
        countriesTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
        countriesTable.addContainerProperty(COUNTRY_COLUMN_ID, Button.class, null);
        countriesTable.addContainerProperty(NUMBER_OF_ENV_COLUMN_ID, String.class, null);
        
        
        countriesTable.setColumnHeader(TAG_COLUMN_ID, "Tag");
        countriesTable.setColumnHeader(COUNTRY_COLUMN_ID, "Country");
        countriesTable.setColumnHeader(NUMBER_OF_ENV_COLUMN_ID, "# of Environments");
        
        
    }
    
    private void initializeProvinceTable(){
    	provinceTable = new Table();
    	provinceTable.setWidth("330px");
    	provinceTable.setHeight("350px");
    	provinceTable.setImmediate(true);
    	provinceTable.setPageLength(-1);
        //entriesTable.setCacheRate(cacheRate)
    	provinceTable.setSelectable(true);
        provinceTable.setMultiSelect(true);
        provinceTable.setNullSelectionAllowed(false);
        
        setUpProvinceTable();        
        
    }
    
    private void initializeLocationStudyTable(){
    	locationTable = new Table();
    	locationTable.setWidth("330px");
    	locationTable.setHeight("350px");
    	locationTable.setImmediate(true);
    	locationTable.setPageLength(-1);
        //entriesTable.setCacheRate(cacheRate)
    	locationTable.setSelectable(true);
    	locationTable.setMultiSelect(true);
    	locationTable.setNullSelectionAllowed(false);
        
        setUpLocationTable();        
    }
    
    private void setUpProvinceTable(){
    	provinceTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
    	provinceTable.addContainerProperty(PROVINCE_COLUMN_ID, Button.class, null);
    	provinceTable.addContainerProperty(NUMBER_OF_ENV_COLUMN_ID, String.class, null);
        
        
        provinceTable.setColumnHeader(TAG_COLUMN_ID, "Tag");
        provinceTable.setColumnHeader(PROVINCE_COLUMN_ID, "Province");
        provinceTable.setColumnHeader(NUMBER_OF_ENV_COLUMN_ID, "# of Environments");
    }
   
    private void setUpLocationTable(){
    	locationTable.addContainerProperty(TAG_COLUMN_ID, CheckBox.class, null);
    	locationTable.addContainerProperty(LOCATION_COLUMN_ID, String.class, null);
    	locationTable.addContainerProperty(NUMBER_OF_ENV_COLUMN_ID, String.class, null);
        
        
    	locationTable.setColumnHeader(TAG_COLUMN_ID, "Tag");
    	locationTable.setColumnHeader(LOCATION_COLUMN_ID, "Location");
    	locationTable.setColumnHeader(NUMBER_OF_ENV_COLUMN_ID, "# of Environments");        
    }
    
    public void applyButtonClickAction(){
        // apply to previous screen the filter
    }

    public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) throws InternationalizableException {
        sourceTable.select(itemId);
        //this.selectedGid = Integer.valueOf(item.getItemProperty(GID).toString());
        //this.doneButton.setEnabled(true);
    }
    
    public void clickCountryName(String countryName){
    	//we need to remove all in the location study table
    	 provinceTable.removeAllItems();
    	 List<Object> propertyIds = new ArrayList<Object>();
         for(Object propertyId : provinceTable.getContainerPropertyIds()){
             propertyIds.add(propertyId);
         }
         
         for(Object propertyId : propertyIds){
        	 provinceTable.removeContainerProperty(propertyId);
         }
         
         locationTable.removeAllItems();
    	 propertyIds = new ArrayList<Object>();
         for(Object propertyId : locationTable.getContainerPropertyIds()){
             propertyIds.add(propertyId);
         }
         
         for(Object propertyId : propertyIds){
        	 locationTable.removeContainerProperty(propertyId);
         }
         
        
        setUpProvinceTable();
        setUpLocationTable();
        
        
		FilterByLocation filterByLocation = filterLocationCountryMap.get(countryName);
		for(String provinceName : filterByLocation.getListOfProvinceNames()){
			String key = countryName+DELIMITER+provinceName;
			Object[] itemObj = itemMap.get(key);
    		if(itemObj == null){
				//item = provinceTable.addItem(key);
			 	CheckBox box = new CheckBox();
			 	box.setImmediate(true);
			 	FilterLocationDto filterLocationDto = new FilterLocationDto(countryName, provinceName, null, null, 2);
			 	//box.setValue(filterLocationDto);
		        //item.getItemProperty(TAG_COLUMN_ID).setValue(box);
		        Button provinceNameLink = new Button(provinceName);
		        provinceNameLink.setImmediate(true);
		        provinceNameLink.setStyleName(Reindeer.BUTTON_LINK);
		        provinceNameLink.setData(PROVINCE_BUTTON_ID);
		        provinceNameLink.addListener(new HeadToHeadCrossStudyMainButtonClickListener(this, countryName, provinceName));
		        /*
		        item.getItemProperty(PROVINCE_COLUMN_ID).setValue(provinceNameLink);
		        
		        item.getItemProperty(NUMBER_OF_ENV_COLUMN_ID).setValue(filterByLocation.getNumberOfEnvironmentForProvince(provinceName));
		        itemMap.put(key, item);
		        */
		        box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, filterLocationDto));
		        itemObj = new Object[] {box, provinceNameLink, filterByLocation.getNumberOfEnvironmentForProvince(provinceName)};
	            provinceTable.addItem(itemObj, key);
	            
	            itemMap.put(key, itemObj);
    		}else{
    			provinceTable.addItem(itemObj, key);
    		}
		}
		
		
    }
    
    public void clickProvinceName(String countryName, String province){
    	
    	 List<Object> propertyIds = new ArrayList<Object>();
        
         
    	 locationTable.removeAllItems();
    	 propertyIds = new ArrayList<Object>();
         for(Object propertyId : locationTable.getContainerPropertyIds()){
             propertyIds.add(propertyId);
         }
         
         for(Object propertyId : propertyIds){
        	 locationTable.removeContainerProperty(propertyId);
         }
         
        
        setUpLocationTable();
        
        
		FilterByLocation filterByLocation = filterLocationCountryMap.get(countryName);
		Collection<LocationStudyDto> locationStudyDtoList = filterByLocation.getLocationStudyForProvince(province);
		for(LocationStudyDto locationStudyDto : locationStudyDtoList){
			String key = countryName+DELIMITER+province+DELIMITER+locationStudyDto.getLocationName();// + DELIMITER  + locationStudyDto.getStudyName();
			Object[] itemObj = itemMap.get(key);
    		if(itemObj == null){
				//item = locationStudyTable.addItem(key);
			 	CheckBox box = new CheckBox();
			 	box.setImmediate(true);
			 	FilterLocationDto filterLocationDto = new FilterLocationDto(countryName, province, locationStudyDto.getLocationName(), locationStudyDto.getStudyName(), 3);
			 	//box.setValue(filterLocationDto);
			 	/*
		        item.getItemProperty(TAG_COLUMN_ID).setValue(box);	       
		        item.getItemProperty(LOCATION_COLUMN_ID).setValue(locationStudyDto.getLocationName() );	        
		        item.getItemProperty(STUDY_COLUMN_ID).setValue(locationStudyDto.getStudyName());
		        */
		        //itemMap.put(key, item);
		        box.addListener(new HeadToHeadCrossStudyMainValueChangeListener(this, null, filterLocationDto));
		        itemObj = new Object[] {box, locationStudyDto.getLocationName() , filterByLocation.getNumberOfEnvironmentForLocation(locationStudyDto.getLocationName())};
		        locationTable.addItem(itemObj, key);
	            
	            itemMap.put(key, itemObj);
    		}else{
    			locationTable.addItem(itemObj, key);
    		}
		}
		
		
    }
   
    public void clickCheckBox(boolean val, FilterLocationDto filterLocationDto){
    	List tempList = new ArrayList();
    	if(filterLocationDto.getLevel() == 1)
    		tempList = checkFilterLocationLevel1DtoList;
    	else if(filterLocationDto.getLevel() == 2)
    		tempList = checkFilterLocationLevel2DtoList;
    	else if(filterLocationDto.getLevel() == 3)
    		tempList = checkFilterLocationLevel3DtoList;
    	
    	if(val){
    		tempList.add(filterLocationDto);
    	}else{
    		tempList.remove(filterLocationDto);
    	}
    	setupApplyButton();
    }
    
    public void clickApplyButton(){
    	((EnvironmentsAvailableComponent)source).clickFilterByLocationApply(checkFilterLocationLevel1DtoList, checkFilterLocationLevel2DtoList, checkFilterLocationLevel3DtoList);
    }
    
    public void initializeButtons(){
    	setupApplyButton();
    }
    
    private void setupApplyButton(){
    	if(!checkFilterLocationLevel1DtoList.isEmpty() || !checkFilterLocationLevel2DtoList.isEmpty() || !checkFilterLocationLevel3DtoList.isEmpty() ){
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
