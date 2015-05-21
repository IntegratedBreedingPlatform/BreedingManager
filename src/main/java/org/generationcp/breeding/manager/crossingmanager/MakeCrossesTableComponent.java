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

import javax.annotation.Resource;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.crossingmanager.actions.SaveCrossesMadeAction;
import org.generationcp.breeding.manager.crossingmanager.listeners.CrossingManagerActionHandler;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossParents;
import org.generationcp.breeding.manager.crossingmanager.pojos.CrossesMade;
import org.generationcp.breeding.manager.crossingmanager.pojos.GermplasmListEntry;
import org.generationcp.breeding.manager.crossingmanager.settings.ApplyCrossingSettingAction;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialog;
import org.generationcp.breeding.manager.customcomponent.SaveListAsDialogSource;
import org.generationcp.breeding.manager.pojos.ImportedGermplasmCross;
import org.generationcp.breeding.manager.util.BreedingManagerUtil;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.util.CrossingUtil;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
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
    private static final long serialVersionUID = 3702324761498666369L;
	private static final Logger LOG = LoggerFactory.getLogger(MakeCrossesTableComponent.class);
     
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
	private OntologyDataManager ontologyDataManager;
 
	@Resource
	private CrossExpansionProperties crossExpansionProperties;
	
    private Label lblReviewCrosses;
    private Table tableCrossesMade;
    private Label lblCrossMade;
    
    private Label totalCrossesLabel;
    private Label totalSelectedCrossesLabel;
    private Button saveButton;
    
    private SaveListAsDialog saveListAsWindow;
    private GermplasmList crossList;
    
    private String separator;
    
    private CrossingManagerMakeCrossesComponent makeCrossesMain;

    private final PedigreeService pedigreeService;
	private final String pedigreeProfile;
	private final String currentCropName;
    
    public MakeCrossesTableComponent(CrossingManagerMakeCrossesComponent makeCrossesMain){
    	this.makeCrossesMain = makeCrossesMain;
		ManagerFactory managerFactory = ManagerFactory.getCurrentManagerFactoryThreadLocal().get();
		if (managerFactory != null) {
			this.pedigreeService = managerFactory.getPedigreeService();
			this.currentCropName = managerFactory.getCropName();
			this.pedigreeProfile = managerFactory.getPedigreeProfile();
		} else {
			throw new IllegalStateException("Must have access to the Manager Factory thread local valiable. "
					+ "Please contact support for further help.");
		}
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
    	lblCrossMade.setValue(messageSource.getMessage(Message.LABEL_CROSS_MADE));
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
        
    	//make a copy first of the parents lists
    	List<GermplasmListEntry> femaleParents = new ArrayList<GermplasmListEntry>();
    	List<GermplasmListEntry> maleParents = new ArrayList<GermplasmListEntry>();
    	femaleParents.addAll(parents1);
    	maleParents.addAll(parents2);
    	
        ListIterator<GermplasmListEntry> femaleListIterator = femaleParents.listIterator();
        ListIterator<GermplasmListEntry> maleListIterator = maleParents.listIterator();
        
        setMakeCrossesTableVisibleColumn();
        
        separator = makeCrossesMain.getSeparatorString();

        while (femaleListIterator.hasNext()){
            GermplasmListEntry femaleParent = femaleListIterator.next();
            GermplasmListEntry maleParent = maleListIterator.next();
            
            String femaleDesig = femaleParent.getDesignation();
            String maleDesig = maleParent.getDesignation();
			String femaleSeedSource = listnameFemaleParent+":"+femaleParent.getEntryId();
			String maleSeedSource = listnameMaleParent + ":" + maleParent.getEntryId();
			GermplasmListEntry femaleParentCopy = femaleParent.copy();
			femaleParentCopy.setSeedSource(femaleSeedSource);
			GermplasmListEntry maleParentCopy = maleParent.copy();
			maleParentCopy.setSeedSource(maleSeedSource);
			
            CrossParents parents = new CrossParents(femaleParentCopy, maleParentCopy);
			final Germplasm germplasm = new Germplasm();
			germplasm.setGnpgs(2);
			germplasm.setGid(Integer.MAX_VALUE);
			germplasm.setGpid1(femaleParent.getGid());
			germplasm.setGpid2(maleParent.getGid());
            String cross = getCross(germplasm, femaleDesig, maleDesig);
            String seedSource = appendWithSeparator(femaleSeedSource, maleSeedSource);
            if (!crossAlreadyExists(parents)){
                tableCrossesMade.addItem(new Object[] {1,
                		cross, femaleDesig, maleDesig,seedSource 
                    }, parents); 
               
            }     
        }
        updateCrossesMadeUI();
    }
    
	private void setMakeCrossesTableVisibleColumn() {
		tableCrossesMade.setVisibleColumns(new Object[]{
				ColumnLabels.ENTRY_ID.getName(),
				ColumnLabels.PARENTAGE.getName(),
				ColumnLabels.FEMALE_PARENT.getName(),
				ColumnLabels.MALE_PARENT.getName(),
				ColumnLabels.SEED_SOURCE.getName()
				});
	}
	
	private void updateCrossesMadeUI() {
		int crossesCount = tableCrossesMade.size();
		generateTotalCrossesLabel(crossesCount);
		updateCrossesMadeSaveButton();
        
        tableCrossesMade.setPageLength(0);
        tableCrossesMade.requestRepaint();
        addTableCrossesMadeCounter();
	}
	
	public void updateCrossesMadeSaveButton(){
		boolean isFemaleListSave = makeCrossesMain.getParentsComponent().isFemaleListSaved();
		boolean isMaleListSave = makeCrossesMain.getParentsComponent().isMaleListSaved();
		
		if( isFemaleListSave && isMaleListSave && (!tableCrossesMade.getItemIds().isEmpty()) ){
			saveButton.setEnabled(true);
	        saveButton.setDescription("");
		} else {
			saveButton.setEnabled(false);
	        saveButton.setDescription(messageSource.getMessage(Message.SAVE_CROSS_LIST_DESCRIPTION));
		}
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
	public void multiplyParents(List<GermplasmListEntry> parents1, List<GermplasmListEntry> parents2, String listnameFemaleParent,
			String listnameMaleParent) {

		// make a copy first of the parents lists
		List<GermplasmListEntry> femaleParents = new ArrayList<GermplasmListEntry>();
		List<GermplasmListEntry> maleParents = new ArrayList<GermplasmListEntry>();
		femaleParents.addAll(parents1);
		maleParents.addAll(parents2);

		setMakeCrossesTableVisibleColumn();
		separator = makeCrossesMain.getSeparatorString();

		for (GermplasmListEntry femaleParent : femaleParents) {
			String femaleDesig = femaleParent.getDesignation();
			String femaleSource = listnameFemaleParent + ":" + femaleParent.getEntryId();
			GermplasmListEntry femaleParentCopy = femaleParent.copy();
			femaleParentCopy.setSeedSource(femaleSource);

			for (GermplasmListEntry maleParent : maleParents) {
				String maleDesig = maleParent.getDesignation();
				String maleSource = listnameMaleParent + ":" + maleParent.getEntryId();
				GermplasmListEntry maleParentCopy = maleParent.copy();
				maleParentCopy.setSeedSource(maleSource);

				CrossParents parents = new CrossParents(femaleParentCopy, maleParentCopy);

				if (!crossAlreadyExists(parents)) {
					String seedSource = appendWithSeparator(femaleSource, maleSource);
					final Germplasm germplasm = new Germplasm();
					germplasm.setGnpgs(2);
					germplasm.setGid(Integer.MAX_VALUE);
					germplasm.setGpid1(femaleParent.getGid());
					germplasm.setGpid2(maleParent.getGid());

					String cross = getCross(germplasm, femaleDesig, maleDesig);

					tableCrossesMade.addItem(new Object[] {1, cross, femaleDesig, maleDesig, seedSource}, parents);
				}

			}
		}
		updateCrossesMadeUI();
	}
    
	private String getCross(final Germplasm germplasm, final String femaleDesignation, final String maleDesignation) {
		try {
			if (CrossingUtil.isCimmytWheat(pedigreeProfile, currentCropName)) {
				return pedigreeService.getCrossExpansion(germplasm, null, crossExpansionProperties);
			}
			return appendWithSeparator(femaleDesignation, maleDesignation);
		} catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(),e);
            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), 
                messageSource.getMessage(Message.ERROR_ACCESSING_PEDIGREE_STRING));
		}
		return "";

	}

    private void addTableCrossesMadeCounter() {
    	
    	int counter=1;
    	 for (Object itemId : tableCrossesMade.getItemIds()){
			tableCrossesMade.getItem(itemId).getItemProperty(ColumnLabels.ENTRY_ID.getName()).setValue(counter);
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
            MessageNotifier.showWarning(this.getWindow(), "Warning!", messageSource.getMessage(Message.ERROR_CROSS_MUST_BE_SELECTED));
        }
        
        if(tableCrossesMade.getItemIds().isEmpty() && getParent() instanceof CrossingManagerMakeCrossesComponent) {
            ((CrossingManagerMakeCrossesComponent) getParent()).disableNextButton();
        }

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
            Property crossNameProp = tableCrossesMade.getItem(itemId).getItemProperty(ColumnLabels.PARENTAGE.getName());
            Property crossSourceProp=tableCrossesMade.getItem(itemId).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
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
        
        return crossesMadeMap;
    }
    
    //internal POJO for ad ID of each row in Crosses Made table (need both GID and entryid of parents)
   
    
    public void clearCrossesTable(){
        this.tableCrossesMade.removeAllItems();
        tableCrossesMade.setPageLength(0);
    }

	@Override
	public void instantiateComponents() {
		lblReviewCrosses = new Label(messageSource.getMessage(Message.REVIEW_CROSSES));
		lblReviewCrosses.addStyleName(Bootstrap.Typography.H4.styleName());
		lblReviewCrosses.addStyleName(AppConstants.CssStyles.BOLD);
		lblReviewCrosses.setWidth("150px");
		
		lblCrossMade = new Label();
		lblCrossMade.addStyleName(Bootstrap.Typography.H4.styleName());
		lblCrossMade.setWidth("160px");
		
        totalCrossesLabel = new Label();
        totalCrossesLabel.setContentMode(Label.CONTENT_XHTML);
        totalCrossesLabel.setWidth("120px");
        
        totalSelectedCrossesLabel = new Label();
        totalSelectedCrossesLabel.setContentMode(Label.CONTENT_XHTML);
        totalSelectedCrossesLabel.setWidth("95px");
        
        saveButton = new Button(messageSource.getMessage(Message.SAVE_LABEL));
        saveButton.addStyleName(Bootstrap.Buttons.INFO.styleName());
        saveButton.setEnabled(false);
        saveButton.setDescription(messageSource.getMessage(Message.SAVE_CROSS_LIST_DESCRIPTION));
        initializeCrossesMadeTable();
	}

	protected void initializeCrossesMadeTable() {
		setTableCrossesMade(new Table());
		tableCrossesMade = getTableCrossesMade();
        tableCrossesMade.setWidth("100%");
        tableCrossesMade.setHeight("407px");
        tableCrossesMade.setImmediate(true);
        tableCrossesMade.setSelectable(true);    
        tableCrossesMade.setMultiSelect(true);
        tableCrossesMade.setPageLength(0);
        
        tableCrossesMade.addContainerProperty(ColumnLabels.ENTRY_ID.getName(), Integer.class, null);
        tableCrossesMade.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class, null);
        tableCrossesMade.addContainerProperty(ColumnLabels.FEMALE_PARENT.getName(), String.class, null);
        tableCrossesMade.addContainerProperty(ColumnLabels.MALE_PARENT.getName(), String.class, null);
        tableCrossesMade.addContainerProperty(ColumnLabels.SEED_SOURCE.getName(), String.class, null);
        
        tableCrossesMade.setColumnHeader(ColumnLabels.ENTRY_ID.getName(), "#");
        tableCrossesMade.setColumnHeader(ColumnLabels.PARENTAGE.getName(), getTermNameFromOntology(ColumnLabels.PARENTAGE));
        tableCrossesMade.setColumnHeader(ColumnLabels.FEMALE_PARENT.getName(), getTermNameFromOntology(ColumnLabels.FEMALE_PARENT));
        tableCrossesMade.setColumnHeader(ColumnLabels.MALE_PARENT.getName(), getTermNameFromOntology(ColumnLabels.MALE_PARENT));
        tableCrossesMade.setColumnHeader(ColumnLabels.SEED_SOURCE.getName(), getTermNameFromOntology(ColumnLabels.SEED_SOURCE));
        
        tableCrossesMade.setColumnWidth(ColumnLabels.SEED_SOURCE.getName(), 200);
        
        tableCrossesMade.setColumnCollapsingAllowed(true);
        
        tableCrossesMade.setColumnCollapsed(ColumnLabels.FEMALE_PARENT.getName(), true);
        tableCrossesMade.setColumnCollapsed(ColumnLabels.MALE_PARENT.getName(), true);
        tableCrossesMade.setColumnCollapsed(ColumnLabels.SEED_SOURCE.getName(), true);
        
        tableCrossesMade.addActionHandler(new CrossingManagerActionHandler(this));
	}

	private void generateTotalCrossesLabel(Integer size){
		String label = "Total Crosses: " + "<b>" + size + "</b>";
		totalCrossesLabel.setValue(label);
	}
	
	private void generateTotalSelectedCrossesLabel(Integer size){
		String label = "<i>" + messageSource.getMessage(Message.SELECTED) + ": <b>" + size + "</b></i>";
		totalSelectedCrossesLabel.setValue(label);
	}
	
	private void generateTotalSelectedCrossesLabel(){
		Collection<?> selectedItems = (Collection<?>)tableCrossesMade.getValue();
		int count = selectedItems.size();
		generateTotalSelectedCrossesLabel(count);
	}
	
	@Override
	public void initializeValues() {
		generateTotalCrossesLabel(0);
		generateTotalSelectedCrossesLabel(0);
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
		
		tableCrossesMade.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				generateTotalSelectedCrossesLabel();
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layoutComponents() {
		setSpacing(true);
        setMargin(false,false,false,true);
        setWidth("450px");
		
        
        HeaderLabelLayout headingLayout = new HeaderLabelLayout(null, lblCrossMade);
        
		HorizontalLayout leftLabelContainer = new HorizontalLayout();
		leftLabelContainer.setSpacing(true);
		leftLabelContainer.addComponent(totalCrossesLabel);
		leftLabelContainer.addComponent(totalSelectedCrossesLabel);
		leftLabelContainer.setComponentAlignment(totalCrossesLabel, Alignment.MIDDLE_LEFT);
		leftLabelContainer.setComponentAlignment(totalSelectedCrossesLabel, Alignment.MIDDLE_LEFT);
        
		HorizontalLayout labelContainer = new HorizontalLayout();
		labelContainer.setSpacing(true);
        labelContainer.setWidth("100%");
        labelContainer.addComponent(leftLabelContainer);
        labelContainer.addComponent(saveButton);
        labelContainer.setComponentAlignment(leftLabelContainer, Alignment.MIDDLE_LEFT);
        labelContainer.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
        
		VerticalLayout makeCrossesLayout = new VerticalLayout();
		makeCrossesLayout.setSpacing(true);
		makeCrossesLayout.setMargin(true);
		makeCrossesLayout.addComponent(headingLayout);
        makeCrossesLayout.addComponent(labelContainer);
        makeCrossesLayout.addComponent(tableCrossesMade);
		
        Panel makeCrossesPanel = new Panel();
        makeCrossesPanel.setWidth("420px");
        makeCrossesPanel.setLayout(makeCrossesLayout);
        makeCrossesPanel.addStyleName("section_panel_layout");
		
        HeaderLabelLayout reviewCrossesLayout = new HeaderLabelLayout(AppConstants.Icons.ICON_REVIEW_CROSSES,lblReviewCrosses);
        addComponent(reviewCrossesLayout);
        addComponent(makeCrossesPanel);
	}
	
	private void launchSaveListAsWindow() {
    	saveListAsWindow = null;
    	if(crossList != null){
    		saveListAsWindow = new SaveCrossListAsDialog(this, crossList);
    	} else {
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
			makeCrossesMain.getSelectParentsComponent().selectListInTree(crossList.getId());
			makeCrossesMain.getSelectParentsComponent().updateUIForDeletedList(crossList);
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
                    messageSource.getMessage(Message.CROSSES_SAVED_SUCCESSFULLY), 3000);
            
            // enable NEXT button if all lists saved
            makeCrossesMain.toggleNextButton();
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(),e);
            MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), 
                messageSource.getMessage(Message.ERROR_IN_SAVING_CROSSES_DEFINED));
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
    	separator = makeCrossesMain.getSeparatorString();
    	
    	if (!tableCrossesMade.getItemIds().isEmpty()){
    		for (Object itemId : tableCrossesMade.getItemIds()){
    			CrossParents crossParents = (CrossParents) itemId;
    			Property crossSourceProp=tableCrossesMade.getItem(itemId).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
    			
    			GermplasmListEntry femaleParent = crossParents.getFemaleParent();
    			GermplasmListEntry maleParent = crossParents.getMaleParent();

    			String newFemaleSource = "";
    			String newMaleSource = "";
    			if(femaleParent.isFromFemaleTable()){
    				newFemaleSource = femaleListName + ":" + femaleParent.getEntryId();
    				newMaleSource = maleListName + ":" + maleParent.getEntryId();
    			} else{
    				newFemaleSource = maleListName + ":" + femaleParent.getEntryId();
    				newMaleSource = femaleListName + ":" + maleParent.getEntryId();
    			}
    			
    			femaleParent.setSeedSource(newFemaleSource);
    			maleParent.setSeedSource(newMaleSource);
    			
    			String newSeedSource = newFemaleSource + separator + newMaleSource;

    			crossSourceProp.setValue(newSeedSource);
    			crossParents.setSeedSource(newSeedSource);
    		}

    		if (getCrossList() != null){
    			makeCrossesMain.getSelectParentsComponent().updateUIForDeletedList(this.getCrossList());
    			
    			SaveCrossesMadeAction saveAction = new SaveCrossesMadeAction(this.getCrossList());
    			try {
    				saveAction.updateSeedSource((Collection<CrossParents>) tableCrossesMade.getItemIds());
    			} catch (MiddlewareQueryException e) {
    				LOG.error(e.getMessage(),e);
    				MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), 
    						messageSource.getMessage(Message.ERROR_IN_SAVING_GERMPLASMLIST_DATA_CHANGES));
    			}
    		}
    	}
		   
    }
    
    public void updateSeparatorForCrossesMade(){
    	separator = makeCrossesMain.getSeparatorString();
    	
    	for (Object crossItem : tableCrossesMade.getItemIds()){
    		CrossParents parents = (CrossParents) crossItem;
    		
    		Property parentageProperty = tableCrossesMade.getItem(crossItem).getItemProperty(ColumnLabels.PARENTAGE.getName());
    		String femaleName = parents.getFemaleParent().getDesignation();
    		String maleName = parents.getMaleParent().getDesignation();
    		parentageProperty.setValue(appendWithSeparator(femaleName, maleName));
    		
    		Property seedSourceProperty = tableCrossesMade.getItem(crossItem).getItemProperty(ColumnLabels.SEED_SOURCE.getName());
    		String femaleSource = parents.getFemaleParent().getSeedSource();
    		String maleSource = parents.getMaleParent().getSeedSource();
    		String newSeedSource = appendWithSeparator(femaleSource,maleSource);
    		seedSourceProperty.setValue(newSeedSource);
    		parents.setSeedSource(newSeedSource);
    	}
    }
    
    private String appendWithSeparator(String string1, String string2){
    	return string1 + separator + string2;
    }
    
    public GermplasmList getCrossList(){
    	return crossList;
    }
    
    public String getSeparator(){
    	return this.separator;
    }
    
	@Override
	public void setCurrentlySavedGermplasmList(GermplasmList list) {
		this.crossList = list;
	}
	
	@Override
	public Component getParentComponent() {
		return makeCrossesMain.getSource();
	}
	
	public Table getTableCrossesMade() {
		return tableCrossesMade;
	}
	
	public void setTableCrossesMade(Table tableCrossesMade) {
		this.tableCrossesMade = tableCrossesMade;
	}
	
	protected String getTermNameFromOntology(ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(ontologyDataManager);
	}
	
	public void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

}
