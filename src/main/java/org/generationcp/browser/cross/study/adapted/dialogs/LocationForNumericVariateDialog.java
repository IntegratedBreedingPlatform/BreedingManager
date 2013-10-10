package org.generationcp.browser.cross.study.adapted.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.h2h.TraitObservation;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class LocationForNumericVariateDialog extends Window implements InitializingBean, InternationalizableComponent {
	
	private static final long serialVersionUID = 1L;

	private final static Logger LOG = LoggerFactory.getLogger(LocationForNumericVariateDialog.class);
	
	private static final String OBSERVATION_NO = "LocationForNumericVariateDialog Observation No";
	private static final String LINE_NO = "LocationForNumericVariateDialog Line No";
	private static final String LINE_GID = "LocationForNumericVariateDialog Line GID";
	private static final String LINE_DESIGNATION = "LocationForNumericVariateDialog Line Designation";
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private CrossStudyDataManager crossStudyDataManager;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	private Component source;
	private Window parentWindow;
	
	private Label popUpLabel;
	private VerticalLayout mainLayout;
	
	private int traitId;
	private List<Integer> environmentIds;
	private String traitName;
	
	private Table locationTable;
	private Integer maxNoOfLocation;
	List<TraitObservation> traitObservations;
	List<Integer> gidList;
	List<String> locationList;
	Map<Integer, String> gidPreferredNameMap;
	
	Map<Integer, String> gidLocMap;
	
	public LocationForNumericVariateDialog(Component source, Window parentWindow, int traitId, String traitName, List<Integer> environmentIds){
        this.source = source;
        this.parentWindow = parentWindow;
        this.traitId = traitId;
        this.traitName = traitName;
        this.environmentIds = environmentIds;
    }
	
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        //set as modal window, other components are disabled while window is open
        setModal(true);
        // define window size, set as not resizable
        setWidth("900px");
        setHeight("530px");
        setResizable(false);
        setCaption(messageSource.getMessage(Message.LINE_BY_LOCATION_FOR_NUMERIC_VARIATE));
        // center window within the browser
        center();
        
        initializeLocationTable();
        populateLocationTable();
        
        popUpLabel = new Label(messageSource.getMessage(Message.LINE_BY_LOCATION_FOR_TRAIT) + " " + traitName);

        AbsoluteLayout mainLayout = new AbsoluteLayout();
        mainLayout.setWidth("900px");
        mainLayout.setHeight("450px");
        
        mainLayout.addComponent(popUpLabel, "top:10px;left:20px");
        mainLayout.addComponent(locationTable, "top:35px;left:20px");
        
        addComponent(mainLayout);
	}

	private void initializeLocationTable() throws MiddlewareQueryException {
		traitObservations = crossStudyDataManager.getObservationsForTrait(traitId, environmentIds);
		
		locationList = getLocations(traitObservations);
		
		locationTable = new Table();
    	locationTable.setWidth("820px");
    	locationTable.setHeight("380px");
        locationTable.setImmediate(true);
        locationTable.setSelectable(true);
        locationTable.setColumnCollapsingAllowed(true);
        locationTable.setColumnReorderingAllowed(true);
        
        locationTable.addContainerProperty(OBSERVATION_NO, Integer.class, null);
        locationTable.addContainerProperty(LINE_NO, Integer.class, null);
        locationTable.addContainerProperty(LINE_GID, Integer.class, null);
        locationTable.addContainerProperty(LINE_DESIGNATION, String.class, null);
        
        locationTable.setColumnHeader(OBSERVATION_NO, messageSource.getMessage(Message.OBSERVATION_NO));
        locationTable.setColumnHeader(LINE_NO, messageSource.getMessage(Message.LINE_NO));
        locationTable.setColumnHeader(LINE_GID, messageSource.getMessage(Message.LINE_GID));
        locationTable.setColumnHeader(LINE_DESIGNATION, messageSource.getMessage(Message.LINE_DESIGNATION));
        
        for (String locationName : locationList){
        	String columnName = "LocationForNumericVariateDialog " + locationName;
        	locationTable.addContainerProperty(columnName, String.class, null);
        	locationTable.setColumnHeader(columnName, locationName);
        }
        
	}

	private void populateLocationTable() throws MiddlewareQueryException {
		gidList = getGIDs(traitObservations);
		gidPreferredNameMap = germplasmDataManager.getPreferredNamesByGids(gidList);
		
		Integer observationNo = 1;
		Integer lineNo = 0;
		int currentGid = 0;
		for(TraitObservation traitObservation : traitObservations){
			
			int gid = traitObservation.getGid();
			String gidName = gidPreferredNameMap.get(gid);
			String location = traitObservation.getLocationName();
			String traitVal = traitObservation.getTraitValue();
			
			if(gid != currentGid){
				lineNo++;
				currentGid = gid;
			}
			
			Object[] itemObj = getTableRow(observationNo, lineNo, gid, gidName, location, traitVal);
			locationTable.addItem(itemObj, observationNo);
			
			
			observationNo++;
		}
		
		
	}
	
	private Object[] getTableRow(int observationNo, int lineNo, int gid, String gidName, String location, String traitVal){
		int noOfCols = 4 + locationList.size();
		Object[] row = new Object[noOfCols];
		
		row[0] = observationNo;
		row[1] = lineNo;
		row[2] = gid;
		row[3] = gidName;
		row[4 + locationList.indexOf(location)] = traitVal;
		
		return row;
	}
	
	private List<Integer> getGIDs(List<TraitObservation> result){
		List<Integer> gids = new ArrayList<Integer>();
		
		for(TraitObservation trait: result){
			
			int id = trait.getGid();
			
			if(!gids.contains(id)){
				gids.add(id);
			}
		}
		
		return gids;
	}
	
	private List<String> getLocations(List<TraitObservation> result){
		List<String> locationList = new ArrayList<String>();
		
		for(TraitObservation trait : result){
			String location = trait.getLocationName();
			
			if(!locationList.contains(location)){
				locationList.add(location);
			}
		}
		
		return locationList;
	}
}
