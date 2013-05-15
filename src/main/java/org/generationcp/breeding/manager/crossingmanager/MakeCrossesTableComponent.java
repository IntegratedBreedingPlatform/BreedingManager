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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCross;
import org.generationcp.breeding.manager.util.CrossingManagerUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
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
public class MakeCrossesTableComponent extends VerticalLayout 
		implements InitializingBean, InternationalizableComponent, CrossesMadeContainerUpdateListener {

	public static final String PARENTS_DELIMITER = ",";
	public static final String CROSS_NAME_COLUMN = "Cross Name Column" ;
	public static final String FEMALE_PARENT_COLUMN = "Female Parent Column" ;
	public static final String MALE_PARENT_COLUMN = "Male Parent Column" ;
	
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
    
    private CrossesMadeContainer container;
    
	@Override
	public void setCrossesMadeContainer(CrossesMadeContainer container) {
		this.container = container;
		
	}

	@Override
	public boolean updateCrossesMadeContainer() {
		this.container.getCrossesMade().setCrossesMap(generateCrossesMadeMap());
		
		return true;
	}
    
	@SuppressWarnings("serial")
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
//		retrieveCrossingNameUserDefinedFieldType();
	}
	
	@Override
	public void updateLabels() {
		messageSource.setCaption(lblCrossMade, Message.LABEL_CROSS_MADE);
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
			
			CrossParents parents = new CrossParents(parent1, parent2);
			if (!crossAlreadyExists(parents)){
				tableCrossesMade.addItem(new Object[] {
						CrossingManagerUtil.generateFemaleandMaleCrossName(caption1, caption2), caption1, caption2 
					}, parents); 
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
				CrossParents parents = new CrossParents(parent1, parent2);
				
				if (!crossAlreadyExists(parents)){
					tableCrossesMade.addItem(new Object[] {
								CrossingManagerUtil.generateFemaleandMaleCrossName(caption1, caption2), caption1, caption2 
							}, parents); 					
				}
			}
		}
    }

    // Checks if combination of female and male parents already exists in Crossing Made table
	private boolean crossAlreadyExists(CrossParents parents) {
		for (Object itemId : tableCrossesMade.getItemIds()){
			CrossParents rowId = (CrossParents) itemId;
			if (rowId.equals(parents)){
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
    	if(tableCrossesMade.size()==0 && getParent() instanceof CrossingManagerMakeCrossesComponent)
    	    ((CrossingManagerMakeCrossesComponent) getParent()).disableNextButton();

    }
    
    private Map<Germplasm, Name > generateCrossesMadeMap(){
    	Map<Germplasm, Name> crossesMadeMap = new LinkedHashMap<Germplasm, Name>();
    	List<ImportedGermplasmCross> crossesToExport = new ArrayList<ImportedGermplasmCross>();
    	
    	//get ID of User Defined Field for Crossing Name
        Integer crossingNameTypeId = CrossingManagerUtil.getIDForUserDefinedFieldCrossingName(
                        germplasmListManager, getWindow(), messageSource);
        
    	int ctr = 1;
    	for (Object itemId : tableCrossesMade.getItemIds()){
    		Property crossNameProp = tableCrossesMade.getItem(itemId).getItemProperty(CROSS_NAME_COLUMN);
    		String crossName = String.valueOf(crossNameProp.toString());
    		
	    		// get GIDs and entryIDs of female and male parents
			CrossParents parents = (CrossParents) itemId;
			Integer gpId1 = parents.getFemaleParent().getGid();
			Integer gpId2 = parents.getMaleParent().getGid();
			Integer entryId1 = parents.getFemaleParent().getEntryId();
			Integer entryId2 = parents.getMaleParent().getEntryId();
						
			Germplasm germplasm = new Germplasm();
			germplasm.setGid(ctr);
			germplasm.setGpid1(gpId1);
			germplasm.setGpid2(gpId2);
			
			Name name = new Name();
			name.setNval(crossName);
			name.setTypeId(crossingNameTypeId);
			
			ImportedGermplasmCross cross = new ImportedGermplasmCross();
			cross.setCross(ctr);
			cross.setFemaleGId(gpId1);
			cross.setMaleGId(gpId2);
			cross.setFemaleEntryId(entryId1);
			cross.setMaleEntryId(entryId2);
			
			crossesMadeMap.put(germplasm, name);
			crossesToExport.add(cross);
			ctr++;
		}
    	
    	//update list of crosses to export in CrossingManagerUploader
    	this.container.getCrossesMade().getCrossingManagerUploader()
    			.getImportedGermplasmCrosses().setImportedGermplasmCross(crossesToExport);
    	
    	return crossesMadeMap;
    }
    
    //internal POJO for ad ID of each row in Crosses Made table (need both GID and entryid of parents)
    private class CrossParents{
	
		private GermplasmListEntry femaleParent;
		
		private GermplasmListEntry maleParent;
		
		public CrossParents(GermplasmListEntry femaleParent, GermplasmListEntry maleParent){
		    this.femaleParent = femaleParent;
		    this.maleParent = maleParent;
		}
		
		public GermplasmListEntry getFemaleParent() {
		    return femaleParent;
		}
		
		public GermplasmListEntry getMaleParent() {
		    return maleParent;
		}
	
		@Override
		public boolean equals(Object obj) {
		    if (this == obj)
		    	return true;
		    if (obj == null)
		    	return false;
		    if (getClass() != obj.getClass())
		    	return false;
		    CrossParents other = (CrossParents) obj;
		    if (femaleParent == null) {
				if (other.femaleParent != null)
				    return false;
			    } else if (!femaleParent.equals(other.femaleParent))
			    	return false;
		    if (maleParent == null) {
				if (other.maleParent != null)
				    return false;
			 } else if (!maleParent.equals(other.maleParent))
				return false;
		    
		    return true;
		}

    }

}
