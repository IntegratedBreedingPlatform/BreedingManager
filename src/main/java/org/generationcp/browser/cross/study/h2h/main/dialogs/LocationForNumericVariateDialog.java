package org.generationcp.browser.cross.study.h2h.main.dialogs;

import java.util.List;

import org.generationcp.browser.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.TraitObservation;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.CrossStudyDataManager;
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
	private static final String LOCATION_1 = "LocationForNumericVariateDialog Location 1";
	private static final String LOCATION_2 = "LocationForNumericVariateDialog Location 2";
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private CrossStudyDataManager crossStudyDataManager;
	
	private Component source;
	private Window parentWindow;
	
	private Label popUpLabel;
	private VerticalLayout mainLayout;
	
	private int traitId;
	private List<Integer> environmentIds;
	private String traitName;
	
	private Table locationTable;
	
	public LocationForNumericVariateDialog(Component source, Window parentWindow, int traitId, List<Integer> environmentIds){
        this.source = source;
        this.parentWindow = parentWindow;
        this.traitId = traitId;
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
        setWidth("800px");
        setHeight("530px");
        setResizable(false);
        setCaption(messageSource.getMessage(Message.LINE_BY_LOCATION_FOR_NUMERIC_VARIATE));
        // center window within the browser
        center();
        
        initializeLocationTable();
        populateLocationTable();
        
        popUpLabel = new Label(messageSource.getMessage(Message.LINE_BY_LOCATION_FOR_TRAIT));

        AbsoluteLayout mainLayout = new AbsoluteLayout();
        mainLayout.setWidth("800px");
        mainLayout.setHeight("450px");
        
        mainLayout.addComponent(popUpLabel, "top:10px;left:20px");
        mainLayout.addComponent(locationTable, "top:30px;left:20px");
        
        addComponent(mainLayout);
	}

	private void initializeLocationTable() {
		locationTable = new Table();
    	locationTable.setWidth("720px");
    	locationTable.setHeight("350px");
        locationTable.setImmediate(true);
        locationTable.setPageLength(-1);
        //entriesTable.setCacheRate(cacheRate)
        locationTable.setSelectable(true);
        locationTable.setMultiSelect(true);
        locationTable.setNullSelectionAllowed(false);
        
        locationTable.addContainerProperty(OBSERVATION_NO, String.class, null);
        locationTable.addContainerProperty(LINE_NO, String.class, null);
        locationTable.addContainerProperty(LINE_GID, String.class, null);
        locationTable.addContainerProperty(LINE_DESIGNATION, String.class, null);
        locationTable.addContainerProperty(LOCATION_1, String.class, null);
        locationTable.addContainerProperty(LOCATION_2, String.class, null);
     
        locationTable.setColumnHeader(OBSERVATION_NO, messageSource.getMessage(Message.OBSERVATION_NO));
        locationTable.setColumnHeader(LINE_NO, messageSource.getMessage(Message.LINE_NO));
        locationTable.setColumnHeader(LINE_GID, messageSource.getMessage(Message.LINE_GID));
        locationTable.setColumnHeader(LINE_DESIGNATION, messageSource.getMessage(Message.LINE_DESIGNATION));
        locationTable.setColumnHeader(LOCATION_1, messageSource.getMessage(Message.LOCATION_1));
        locationTable.setColumnHeader(LOCATION_2, messageSource.getMessage(Message.LOCATION_2));
        
	}

	private void populateLocationTable() throws MiddlewareQueryException {
		List<TraitObservation> result = crossStudyDataManager.getObservationsForTrait(traitId, environmentIds);
		
		for(TraitObservation observation : result ){
			String locationNames = observation.getLocationName().replace(";;", ";"); 
			String[] locs = locationNames.split(";");
			String loc1, loc2;
			
			if(locs.length == 1){
				locs = locationNames.split(",");
				loc1 = locs[0];
				loc2 = "";
			}
			else{
				loc1 = locs[0];
				loc2 = locs[1];
			}
			
			Object[] itemObj = new Object[]{ observation.getObservationId(), observation.getId(), observation.getGid(), observation.getTraitValue(), loc1,loc2};
			locationTable.addItem(itemObj, observation.getObservationId());
		}
	}

}
