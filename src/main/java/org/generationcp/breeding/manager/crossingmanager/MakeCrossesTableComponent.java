/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.breeding.manager.crossingmanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.pojos.Germplasm;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.pojos.Name;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * This class contains UI components and functions related to
 * Crosses Made table in Make Crosses screen in Crossing Manager
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class MakeCrossesTableComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{

	public static final String PARENTS_DELIMITER = ",";
	public static final String CROSS_NAME_COLUMN = "Cross Name Column" ;
	public static final String FEMALE_PARENT_COLUMN = "Female Parent Column" ;
	public static final String MALE_PARENT_COLUMN = "Male Parent Column" ;
	public static final String[] USER_DEF_FIELD_CROSS_NAME = {"CROSS NAME", "CROSSING NAME"};
	
	private static final long serialVersionUID = 3702324761498666369L;
	private static final Logger LOG = LoggerFactory.getLogger(MakeCrossesTableComponent.class);
	
    private static final Action ACTION_SELECT_ALL = new Action("Select All");
	private static final Action ACTION_DELETE = new Action("Delete selected crosses");
	private static final Action[] ACTIONS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE };
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
	 
	private Table tableCrossesMade;
    private Label lblCrossMade;
    
    private Integer crossingNameTypeId;
    
  
	@Override
	public void afterPropertiesSet() throws Exception {
		lblCrossMade = new Label();
		
        tableCrossesMade = new Table();
        tableCrossesMade.setSizeFull();
        tableCrossesMade.setSelectable(true);	
        tableCrossesMade.setMultiSelect(true);
        
        tableCrossesMade.addContainerProperty(CROSS_NAME_COLUMN, String.class, null);
        tableCrossesMade.addContainerProperty(FEMALE_PARENT_COLUMN, String.class, null);
        tableCrossesMade.addContainerProperty(MALE_PARENT_COLUMN, String.class, null);
        
        tableCrossesMade.setColumnHeader(CROSS_NAME_COLUMN, messageSource.getMessage(Message.CROSS_NAME));
        tableCrossesMade.setColumnHeader(FEMALE_PARENT_COLUMN, messageSource.getMessage(Message.LABEL_FEMALE_PARENT));
        tableCrossesMade.setColumnHeader(MALE_PARENT_COLUMN, messageSource.getMessage(Message.LABEL_MALE_PARENT));
        
        tableCrossesMade.addActionHandler(new Action.Handler() {
			public Action[] getActions(Object target, Object sender) {
					return ACTIONS_TABLE_CONTEXT_MENU;
			}

			public void handleAction(Action action, Object sender, Object target) {
				if (ACTION_DELETE == action) {
					deleteCrossAction();
				} else if (ACTION_SELECT_ALL == action) {
					tableCrossesMade.setValue(tableCrossesMade.getItemIds());
				}
			}
		});
        
        addComponent(lblCrossMade);
        addComponent(tableCrossesMade);
	}
	
	@Override
	public void attach() {
		super.attach();
		updateLabels();
		retrieveCrossingNameUserDefinedFieldType();
	}
	
	@Override
	public void updateLabels() {
		messageSource.setCaption(lblCrossMade, Message.LABEL_CROSS_MADE);
	}
	
    // Concatenation of male and female parents' item caption
	private String getCrossingText(String caption1, String caption2) {
		return caption1 + "/" + caption2;
	}

    // Crossing ID = the GIDs of parents separated by delimiter (eg. 1,2)
	private String getCrossingID(Integer parent1, Integer parent2) {
		return parent1 + PARENTS_DELIMITER + parent2;
	}
	
	/*
	 * Get the id for UserDefinedField of Germplasm Name type for Crossing Name
	 * (matches upper case of UserDefinedField either fCode or fName)
	 * 
	 */
	private void retrieveCrossingNameUserDefinedFieldType(){
	    	
    	try {
			List<UserDefinedField> nameTypes = germplasmListManager.getGermplasmNameTypes();
			for (UserDefinedField type : nameTypes){
				for (String crossNameValue : USER_DEF_FIELD_CROSS_NAME){
					if (crossNameValue.equals(type.getFcode().toUpperCase()) || 
							crossNameValue.equals(type.getFname().toUpperCase())){
						crossingNameTypeId = type.getFldno();
						break;
					}
				}
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
            if (getWindow() != null){
                MessageNotifier.showWarning(getWindow(), 
                        messageSource.getMessage(Message.ERROR_DATABASE),
                    messageSource.getMessage(Message.ERROR_IN_GETTING_CROSSING_NAME_TYPE));
            }
		}
	}
	
  
	/**
     * Crosses each item on first list with its counterpart (same index or position) 
     * on second list. Assumes that checking if list sizes are equal was done beforehand.
     * The generated crossings are then added to Crossings Table.
     * 
     * @param parents1 - list of GermplasmList entries as first parents
     * @param parents2 - list of GermplasmList entries as second parents
     */
	public void makeTopToBottomCrosses(List<GermplasmListEntry> parents1, List<GermplasmListEntry> parents2) {
		
    	ListIterator<GermplasmListEntry> iterator1 = parents1.listIterator();
    	ListIterator<GermplasmListEntry> iterator2 = parents2.listIterator();
    	
    	while (iterator1.hasNext()){
    		GermplasmListEntry parent1 = iterator1.next();
			GermplasmListEntry parent2 = iterator2.next();
			String caption1 = parent1.getDesignation();
			String caption2 = parent2.getDesignation();
			String crossingId = getCrossingID(parent1.getGid(), parent2.getGid());
			
			if (!crossAlreadyExists(crossingId)){
				tableCrossesMade.addItem(new Object[] {
					getCrossingText(caption1, caption2), caption1, caption2 }, 
					crossingId); 
			}
    	}

	}
    
    /**
     * Multiplies each item on first list with each item on second list.
     * The generated crossings are then added to Crossings Table.
     * 
     * @param parents1 - list of GermplasmList entries as first parents
     * @param parents2 - list of GermplasmList entries as second parents
     */
    public void multiplyParents(List<GermplasmListEntry> parents1, List<GermplasmListEntry> parents2){
    	
    	for (GermplasmListEntry parent1 : parents1){
			String caption1 = parent1.getDesignation();
			
			for (GermplasmListEntry parent2 : parents2){
				String caption2 = parent2.getDesignation();
				String crossingId = getCrossingID(parent1.getGid(), parent2.getGid());
				
				if (!crossAlreadyExists(crossingId)){
					tableCrossesMade.addItem(new Object[] {
							getCrossingText(caption1, caption2), caption1, caption2 }, 
							crossingId); 					
				}
			}
		}
    }

    // Checks if crossing ID already exists in Crossing Made table
	private boolean crossAlreadyExists(String crossingId) {
		for (Object itemId : tableCrossesMade.getItemIds()){
			String idString = (String) itemId;
			if (idString.equals(crossingId)){
				return true;
			}
		}
		return false;
	}
    
    // Action handler for Delete Selected Crosses context menu option
    private void deleteCrossAction(){
    	final Collection<?> selectedIds = (Collection<?>) tableCrossesMade.getValue();
    	if (!selectedIds.isEmpty()){
    		for (Object itemId : selectedIds){
				tableCrossesMade.removeItem(itemId);
			}
    	} else {
    		MessageNotifier.showWarning(this.getWindow(), messageSource.getMessage(Message.ERROR_CROSS_MUST_BE_SELECTED), "");
    	}
    }
    
    public Map<Germplasm, Name> generateCrossesMadeMap(){
    	Map<Germplasm, Name> crossesMadeMap = new HashMap<Germplasm, Name>();
    	
    	int ctr = 1;
    	for (Object itemId : tableCrossesMade.getItemIds()){
    		Property crossNameProp = tableCrossesMade.getItem(itemId).getItemProperty(CROSS_NAME_COLUMN);
    		String crossName = String.valueOf(crossNameProp.toString());
    		
			String idString = (String) itemId;
			String[] parentIDs = idString.split(PARENTS_DELIMITER);
			Integer gpId1 = Integer.parseInt(parentIDs[0]);
			Integer gpId2 = Integer.parseInt(parentIDs[1]);
						
			Germplasm germplasm = new Germplasm(ctr++, gpId1, gpId2);
			Name name = new Name(crossName, crossingNameTypeId);
			
			crossesMadeMap.put(germplasm, name);
		}
    	
    	return crossesMadeMap;
    }

}
