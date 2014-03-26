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

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerActionHandler;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.settings.ApplyCrossingSettingAction;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCross;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

/**
 * This class contains UI components and functions related to
 * Crosses Made table in Make Crosses screen in Crossing Manager
 * 
 * @author Darla Ani
 *
 */
@Configurable
public class MakeCrossesTableComponent extends VerticalLayout 
        implements InitializingBean, InternationalizableComponent, BreedingManagerLayout,  
        		SaveListAsDialogSource {

    public static final String PARENTS_DELIMITER = ",";
    public static final String SOURCE = "Source Column" ;
    public static final String NUMBER = "Number" ;
    public static final String PARENTAGE = "Parentage Column" ;
    public static final String FEMALE_PARENT_COLUMN = "Female Parent Column" ;
    public static final String MALE_PARENT_COLUMN = "Male Parent Column" ;
    
    private static final long serialVersionUID = 3702324761498666369L;
	private static final Logger LOG = LoggerFactory.getLogger(MakeCrossesTableComponent.class);
     
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
     
    private Table tableCrossesMade;
    private Label lblCrossMade;
    
    private Label totalCrossesLabel;
    private Button saveButton;
    
    private SaveListAsDialog saveListAsWindow;
    private GermplasmList crossList;
    
    private CrossingManagerMakeCrossesComponent makeCrossesMain;
    
    public MakeCrossesTableComponent(CrossingManagerMakeCrossesComponent makeCrossesMain){
    	this.makeCrossesMain = makeCrossesMain;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
    	instantiateComponents();
    	initializeValues();
    	addListeners();
    	layoutComponents();
    }
    
    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }
    
    @Override
    public void updateLabels() {
    	lblCrossMade.setValue(messageSource.getMessage(Message.LABEL_CROSS_MADE).toUpperCase());
    }
    
  
    /**
     * Crosses each item on first list with its counterpart (same index or position) 
     * on second list. Assumes that checking if list sizes are equal was done beforehand.
     * The generated crossings are then added to Crossings Table.
     * 
     * @param parents1 - list of GermplasmList entries as first parents
     * @param parents2 - list of GermplasmList entries as second parents
     * @param listnameMaleParent 
     * @param listnameFemaleParent 
     */
    public void makeTopToBottomCrosses(List<GermplasmListEntry> parents1, List<GermplasmListEntry> parents2, 
    		String listnameFemaleParent, String listnameMaleParent) {
        
        ListIterator<GermplasmListEntry> iterator1 = parents1.listIterator();
        ListIterator<GermplasmListEntry> iterator2 = parents2.listIterator();

        tableCrossesMade.setVisibleColumns(new Object[]{NUMBER,PARENTAGE,FEMALE_PARENT_COLUMN,MALE_PARENT_COLUMN,SOURCE});
        while (iterator1.hasNext()){
            GermplasmListEntry parent1 = iterator1.next();
            GermplasmListEntry parent2 = iterator2.next();
            String caption1 = parent1.getDesignation();
            String caption2 = parent2.getDesignation();
            String seedSource =listnameFemaleParent+":"+parent1.getEntryId() + "/"
            +listnameMaleParent+":"+parent2.getEntryId();
            
            
            CrossParents parents = new CrossParents(parent1, parent2);
            
            if (!crossAlreadyExists(parents)){
                tableCrossesMade.addItem(new Object[] {1,
                        BreedingManagerUtil.generateFemaleandMaleCrossName(caption1, caption2), caption1, caption2,seedSource 
                    }, parents); 
               
            }     
        }
        updateCrossesMadeUI();
    }
    
	private void updateCrossesMadeUI() {
		int crossesCount = tableCrossesMade.size();
		generateTotalCrossesLabel(crossesCount);
        saveButton.setEnabled(true);
        tableCrossesMade.setPageLength(0);
        tableCrossesMade.requestRepaint();
        addTableCrossesMadeCounter();
	}
    
    /**
     * Multiplies each item on first list with each item on second list.
     * The generated crossings are then added to Crossings Table.
     * 
     * @param parents1 - list of GermplasmList entries as first parents
     * @param parents2 - list of GermplasmList entries as second parents
     * @param listnameMaleParent 
     * @param listnameFemaleParent 
     */
    public void multiplyParents(List<GermplasmListEntry> parents1, List<GermplasmListEntry> parents2, 
    		String listnameFemaleParent, String listnameMaleParent){
	
	tableCrossesMade.setVisibleColumns(new Object[]{NUMBER,PARENTAGE,FEMALE_PARENT_COLUMN,MALE_PARENT_COLUMN,SOURCE});
        
        for (GermplasmListEntry parent1 : parents1){
            String caption1 = parent1.getDesignation();
            String parent1Source =listnameFemaleParent +":"+parent1.getEntryId();
            
            for (GermplasmListEntry parent2 : parents2){
                String caption2 = parent2.getDesignation();
                String parent2Source =listnameMaleParent +":"+parent2.getEntryId();
                CrossParents parents = new CrossParents(parent1, parent2);
                
                if (!crossAlreadyExists(parents)){
                    String caption3=parent1Source+"/"+parent2Source;
                   
                    tableCrossesMade.addItem(new Object[] {1,
                                BreedingManagerUtil.generateFemaleandMaleCrossName(caption1, caption2), caption1, caption2,caption3
                            }, parents);     
                    
                }
                
            }
        }
        updateCrossesMadeUI();
//        generateTotalCrossesLabel(tableCrossesMade.size());
//        
////        tableCrossesMade.setVisibleColumns(new Object[]{NUMBER,PARENTAGE,FEMALE_PARENT_COLUMN,MALE_PARENT_COLUMN});
//
//        tableCrossesMade.setPageLength(0);
//        tableCrossesMade.requestRepaint();
//        addTableCrossesMadeCounter();
    }

    private void addTableCrossesMadeCounter() {
    	
    	int counter=1;
    	 for (Object itemId : tableCrossesMade.getItemIds()){
			tableCrossesMade.getItem(itemId).getItemProperty(NUMBER).setValue(counter);
			counter++;
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
    public void deleteCrossAction(){
        final Collection<?> selectedIds = (Collection<?>) tableCrossesMade.getValue();
        if (!selectedIds.isEmpty()){
            for (Object itemId : selectedIds){
                tableCrossesMade.removeItem(itemId);
            }
            tableCrossesMade.setPageLength(0);
        } else {
            MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_CROSS_MUST_BE_SELECTED), Notification.POSITION_CENTERED);
        }
        if(tableCrossesMade.size()==0 && getParent() instanceof CrossingManagerMakeCrossesComponent)
            ((CrossingManagerMakeCrossesComponent) getParent()).disableNextButton();

        updateCrossesMadeUI();
    }
    
    private Map<Germplasm, Name > generateCrossesMadeMap(CrossesMadeContainer container){
        Map<Germplasm, Name> crossesMadeMap = new LinkedHashMap<Germplasm, Name>();
        List<ImportedGermplasmCross> crossesToExport = new ArrayList<ImportedGermplasmCross>();
        
        //get ID of User Defined Field for Crossing Name
        Integer crossingNameTypeId = BreedingManagerUtil.getIDForUserDefinedFieldCrossingName(
                        germplasmListManager, getWindow(), messageSource);
        
        int ctr = 1;
        for (Object itemId : tableCrossesMade.getItemIds()){
            Property crossNameProp = tableCrossesMade.getItem(itemId).getItemProperty(PARENTAGE);
            Property crossSourceProp=tableCrossesMade.getItem(itemId).getItemProperty(SOURCE);
            String crossName = String.valueOf(crossNameProp.toString());
            String crossSource= String.valueOf(crossSourceProp.toString());
            
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
            name.setNval(crossName+","+crossSource);
            name.setTypeId(crossingNameTypeId);
            
            ImportedGermplasmCross cross = new ImportedGermplasmCross();
            cross.setCross(ctr);
            cross.setFemaleGId(gpId1);
            cross.setMaleGId(gpId2);
            cross.setFemaleEntryId(entryId1);
            cross.setMaleEntryId(entryId2);
            cross.setMaleDesignation(parents.getMaleParent().getDesignation());
            cross.setFemaleDesignation(parents.getFemaleParent().getDesignation());
            
            crossesMadeMap.put(germplasm, name);
            crossesToExport.add(cross);
            ctr++;
        }
        
        //update list of crosses to export in CrossingManagerUploader
//        container.getCrossesMade().getCrossingManagerUploader()
//                .getImportedGermplasmCrosses().setImportedGermplasmCross(crossesToExport);
        
        return crossesMadeMap;
    }
    
    //internal POJO for ad ID of each row in Crosses Made table (need both GID and entryid of parents)
   
    
    public void clearCrossesTable(){
        this.tableCrossesMade.removeAllItems();
        tableCrossesMade.setPageLength(0);
    }

	@Override
	public void instantiateComponents() {
		setSpacing(true);
		lblCrossMade = new Label();
		lblCrossMade.addStyleName(Bootstrap.Typography.H3.styleName());
		lblCrossMade.addStyleName(AppConstants.CssStyles.BOLD);
		
        totalCrossesLabel = new Label();
        totalCrossesLabel.setContentMode(Label.CONTENT_XHTML);
        
        saveButton = new Button(messageSource.getMessage(Message.SAVE_LABEL));
        saveButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        saveButton.setEnabled(false);
        initializeCrossesMadeTable();
	}

	private void initializeCrossesMadeTable() {
		tableCrossesMade = new Table();
        tableCrossesMade.setWidth("100%");
        tableCrossesMade.setHeight("420px");
        tableCrossesMade.setImmediate(true);
        tableCrossesMade.setSelectable(true);    
        tableCrossesMade.setMultiSelect(true);
        tableCrossesMade.setPageLength(0);
        
        tableCrossesMade.addContainerProperty(NUMBER, Integer.class, null);
        tableCrossesMade.addContainerProperty(PARENTAGE, String.class, null);
        tableCrossesMade.addContainerProperty(FEMALE_PARENT_COLUMN, String.class, null);
        tableCrossesMade.addContainerProperty(MALE_PARENT_COLUMN, String.class, null);
        tableCrossesMade.addContainerProperty(SOURCE, String.class, null);
        
        tableCrossesMade.setColumnHeader(NUMBER, "#");
        tableCrossesMade.setColumnHeader(PARENTAGE, messageSource.getMessage(Message.PARENTAGE));
        tableCrossesMade.setColumnHeader(FEMALE_PARENT_COLUMN, messageSource.getMessage(Message.LABEL_FEMALE_PARENT));
        tableCrossesMade.setColumnHeader(MALE_PARENT_COLUMN, messageSource.getMessage(Message.LABEL_MALE_PARENT));
        tableCrossesMade.setColumnHeader(SOURCE, "SOURCE");
        
        tableCrossesMade.setVisibleColumns(new Object[]{NUMBER,PARENTAGE, FEMALE_PARENT_COLUMN, MALE_PARENT_COLUMN, SOURCE});
        tableCrossesMade.addActionHandler(new CrossingManagerActionHandler(this));
	}

	private void generateTotalCrossesLabel(Integer size){
		String label = "Total Crosses: " + "<b>" + size + "</b>";
		totalCrossesLabel.setValue(label);
	}
	@Override
	public void initializeValues() {
		generateTotalCrossesLabel(0);
	}

	@SuppressWarnings("serial")
	@Override
	public void addListeners() {
		saveButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				launchSaveListAsWindow();
			}
		});
	}

	@Override
	public void layoutComponents() {
		HorizontalLayout labelContainer = new HorizontalLayout();
        labelContainer.setWidth("100%");
        
        labelContainer.addComponent(lblCrossMade);
        labelContainer.addComponent(totalCrossesLabel);
        labelContainer.addComponent(saveButton);
        
        labelContainer.setComponentAlignment(lblCrossMade, Alignment.MIDDLE_LEFT);
        labelContainer.setComponentAlignment(totalCrossesLabel, Alignment.MIDDLE_CENTER);
        labelContainer.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
        
        addComponent(labelContainer);
        addComponent(tableCrossesMade);
	}
	
	private void launchSaveListAsWindow() {
    	saveListAsWindow = null;
    	if(crossList != null){
    		saveListAsWindow = new SaveCrossListAsDialog(this, crossList);
    	}
    	else{
    		saveListAsWindow = new SaveCrossListAsDialog(this,null);
    	}
        
        saveListAsWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        this.getWindow().addWindow(saveListAsWindow);
    }

	@Override
	public void saveList(GermplasmList list) {
		this.saveButton.setEnabled(false);
		
		if (updateCrossesMadeContainer(makeCrossesMain.getCrossesMadeContainer(), list)){
			saveRecords();
			makeCrossesMain.selectListInTree(crossList.getId());
		}
		
	}
	
	private boolean updateCrossesMadeContainer(CrossesMadeContainer container, GermplasmList list) {
		CrossesMade crossesMade = container.getCrossesMade();
		crossesMade.setSetting(makeCrossesMain.getCurrentCrossingSetting());
		crossesMade.setGermplasmList(list);
		crossesMade.setCrossesMap(generateCrossesMadeMap(container));
		ApplyCrossingSettingAction applySetting = new ApplyCrossingSettingAction(makeCrossesMain.getCurrentCrossingSetting());
		return applySetting.updateCrossesMadeContainer(container);
	}
	
	 //Save records into DB and redirects to GermplasmListBrowser to view created list
    private void saveRecords() {
        SaveCrossesMadeAction saveAction = new SaveCrossesMadeAction(this.getCrossList());

        try {
            crossList = saveAction.saveRecords(makeCrossesMain.getCrossesMadeContainer().getCrossesMade());
            MessageNotifier.showMessage(getWindow(), messageSource.getMessage(Message.SUCCESS), 
                    messageSource.getMessage(Message.CROSSES_SAVED_SUCCESSFULLY), 3000, Notification.POSITION_CENTERED);
            
            // enable NEXT button if all lists saved
            makeCrossesMain.toggleNextButton();
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage() + " " + e.getStackTrace());
            e.printStackTrace();
            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), 
                messageSource.getMessage(Message.ERROR_IN_SAVING_CROSSES_DEFINED), Notification.POSITION_CENTERED);
        }
    }
    
    /**
     * Update seed source of existing listdata records with new list names
     * 
     * @param femaleListName
     * @param maleListName
     */
    @SuppressWarnings("unchecked")
	public void updateSeedSource(String femaleListName, String maleListName){
    	if (!tableCrossesMade.getItemIds().isEmpty()){
    		for (Object itemId : tableCrossesMade.getItemIds()){
    			CrossParents crossParents = (CrossParents) itemId;
    			
    			Property crossSourceProp=tableCrossesMade.getItem(itemId).getItemProperty(SOURCE);
    			String crossSource= String.valueOf(crossSourceProp.toString());
    			String[] parents = crossSource.split("/");
    			
    			String[] femaleSource = parents[0].split(":");
    			String[] maleSource = parents[1].split(":");
    			
    			String newFemaleSource = femaleListName + ":" + femaleSource[1].trim();
    			String newMaleSource = maleListName + ":" + maleSource[1].trim();
    			String newSeedSource = newFemaleSource + "/" + newMaleSource;

    			crossSourceProp.setValue(newSeedSource);
    			crossParents.setSeedSource(newSeedSource);
    		}

    		
    		SaveCrossesMadeAction saveAction = new SaveCrossesMadeAction(this.getCrossList());
    		try {
    			saveAction.updateSeedSource((Collection<CrossParents>) tableCrossesMade.getItemIds());
    		} catch (MiddlewareQueryException e) {
    			e.printStackTrace();
    			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), 
    					messageSource.getMessage(Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_CHANGES), Notification.POSITION_CENTERED);
    		}
    		
    	}
		   
    }
    
    public GermplasmList getCrossList(){
    	return crossList;
    }
}
