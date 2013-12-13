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

package org.generationcp.browser.germplasm;

import org.generationcp.browser.application.Message;
import org.generationcp.browser.germplasm.containers.GermplasmIndexContainer;
import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
import org.generationcp.browser.germplasm.listeners.GermplasmSelectedTabChangeListener;
import org.generationcp.browser.germplasm.pedigree.GermplasmPedigreeGraphComponent;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@Configurable
public class GermplasmDetail extends Accordion implements InitializingBean, InternationalizableComponent{

    private static final long serialVersionUID = 1L;

    private static final int FIRST_TAB = 1;
    private static final int SECOND_TAB = 2;
    private static final int THIRD_TAB = 3;
    private static final int FOURTH_TAB = 4;
    private static final int FIFTH_TAB = 5;
    private static final int SIX_TAB = 6;
    private static final int SEVEN_TAB = 7;
    private static final int EIGHT_TAB = 8;
    private static final int NINE_TAB = 9;
    private static final int TEN_TAB = 10;
    private static final int ELEVEN_TAB = 11;
    private static final int TWELVE_TAB = 12;
    public final static String VIEW_PEDIGREE_GRAPH_ID = "View Pedigree Graph";
    public final static String INCLUDE_DERIVATIVE_LINES = "Include Derivative Lines";
    public final static String REFRESH_BUTTON_ID = "Refresh";
    private GermplasmIndexContainer dataIndexContainer;
    private GermplasmQueries qQuery;
    private GermplasmDetailModel gDetailModel;
    private VerticalLayout layoutDetails;
    private VerticalLayout layoutNames;
    private VerticalLayout layoutAttributes;
    private VerticalLayout layoutGenerationHistory;
    private VerticalLayout layoutPedigreeTree;
    private VerticalLayout layoutGermplasmList;
    private VerticalLayout layoutGroupRelatives;
    private VerticalLayout layoutManagementNeighbors;
    private VerticalLayout layoutInventoryInformation;
    private VerticalLayout layoutDerivativeNeighborhood;
    private VerticalLayout layoutMaintenanceNeighborhood;
    private VerticalLayout layoutGermplasmStudyInformation;
    private VerticalLayout mainLayout;
    private int gid;
    private TabSheet tabSheet;
    private GermplasmIndexContainer dataResultIndexContainer;
    private boolean fromUrl;                //this is true if this component is created by accessing the Germplasm Details page directly from the URL

    private GermplasmPedigreeTreeComponent germplasmPedigreeTreeComponent;
    private CheckBox pedigreeDerivativeCheckbox;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    private Button btnViewPedigreeGraph;

    public GermplasmDetail(int gid, GermplasmQueries qQuery, GermplasmIndexContainer dataResultIndexContainer, VerticalLayout mainLayout,
            TabSheet tabSheet, boolean fromUrl) throws InternationalizableException {
        this.qQuery = qQuery;
        this.mainLayout = mainLayout;
        this.gid = gid;
        this.tabSheet = tabSheet;
        this.dataResultIndexContainer = dataResultIndexContainer;
        this.dataIndexContainer = dataResultIndexContainer;
        this.fromUrl = fromUrl;
    }

    public GermplasmDetailModel getGermplasmDetailModel() {
        return gDetailModel;
    }

    public void selectedTabChangeAction() throws InternationalizableException {
        Component selected = this.getSelectedTab();
        Tab tab = this.getTab(selected);
        if (tab.getComponent() instanceof VerticalLayout) {

            if (((VerticalLayout) tab.getComponent()).getData().equals(SECOND_TAB)) {
                if (layoutNames.getComponentCount() == 0) {
                    layoutNames.addComponent(new GermplasmNamesComponent(dataIndexContainer, gDetailModel));
                    layoutNames.setMargin(true);
                }
            } else if (((VerticalLayout) tab.getComponent()).getData().equals(THIRD_TAB)) {
                if (layoutAttributes.getComponentCount() == 0) {
                    layoutAttributes.addComponent(new GermplasmAttributesComponent(dataIndexContainer, gDetailModel));
                    layoutAttributes.setMargin(true);
                }
            } else if (((VerticalLayout) tab.getComponent()).getData().equals(FOURTH_TAB)) {
                if (layoutGenerationHistory.getComponentCount() == 0) {
                    layoutGenerationHistory.addComponent(new GermplasmGenerationHistoryComponent(dataIndexContainer, gDetailModel));
                    layoutGenerationHistory.setMargin(true);
                }
            } else if (((VerticalLayout) tab.getComponent()).getData().equals(FIFTH_TAB)) {
                if (layoutPedigreeTree.getComponentCount() == 0) {
                    btnViewPedigreeGraph = new Button("View Pedigree Graph");
                    btnViewPedigreeGraph.setData(VIEW_PEDIGREE_GRAPH_ID);
                    btnViewPedigreeGraph.addListener(new GermplasmButtonClickListener(this));
                    layoutPedigreeTree.addComponent(btnViewPedigreeGraph);
                    
                    HorizontalLayout derivativeHorizontalLayout = new HorizontalLayout();
                    derivativeHorizontalLayout.setMargin(true); 
                    derivativeHorizontalLayout.setSpacing(true); 
                    pedigreeDerivativeCheckbox = new CheckBox();
                    pedigreeDerivativeCheckbox.setCaption("Include Derivative Lines");
                    pedigreeDerivativeCheckbox.setData(INCLUDE_DERIVATIVE_LINES);
                    derivativeHorizontalLayout.addComponent(pedigreeDerivativeCheckbox);
                    
                    Button refreshButton = new Button("Apply");
                    refreshButton.setData(REFRESH_BUTTON_ID);
                    refreshButton.addListener(new GermplasmButtonClickListener(this));
                    refreshButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
                    derivativeHorizontalLayout.addComponent(refreshButton);
                    
                    layoutPedigreeTree.addComponent(derivativeHorizontalLayout);
                    
                    germplasmPedigreeTreeComponent = new GermplasmPedigreeTreeComponent(gid, qQuery, dataResultIndexContainer, mainLayout, tabSheet, (Boolean) pedigreeDerivativeCheckbox.getValue());
                    layoutPedigreeTree.addComponent(germplasmPedigreeTreeComponent);
                }
            } else if (((VerticalLayout) tab.getComponent()).getData().equals(SIX_TAB)) {
                if (layoutGermplasmList.getComponentCount() == 0) {
                    layoutGermplasmList.addComponent(new GermplasmListComponent(germplasmListManager, gid, fromUrl));
                    layoutGermplasmList.setMargin(true);
                }
            }else if (((VerticalLayout) tab.getComponent()).getData().equals(SEVEN_TAB)) {
                if (layoutGroupRelatives.getComponentCount() == 0) {
                    layoutGroupRelatives.addComponent(new GermplasmGroupRelativesComponent(gid));
                    layoutGroupRelatives.setMargin(true);
                }
            }else if (((VerticalLayout) tab.getComponent()).getData().equals(EIGHT_TAB)) {
                if (layoutManagementNeighbors.getComponentCount() == 0) {
                    layoutManagementNeighbors.addComponent(new GermplasmManagementNeighborsComponent(gid));
                    layoutManagementNeighbors.setMargin(true);
                }
            }else if (((VerticalLayout) tab.getComponent()).getData().equals(NINE_TAB)) {
                if (layoutDerivativeNeighborhood.getComponentCount() == 0) {
                    layoutDerivativeNeighborhood.addComponent(new GermplasmDerivativeNeighborhoodComponent(gid, qQuery, dataResultIndexContainer, mainLayout, tabSheet));
                }
            }else if (((VerticalLayout) tab.getComponent()).getData().equals(TEN_TAB)) {
                if (layoutInventoryInformation.getComponentCount() == 0) {
                    layoutInventoryInformation.addComponent(new InventoryInformationComponent(dataIndexContainer, gDetailModel));
                    layoutInventoryInformation.setMargin(true);
                }
            }else if (((VerticalLayout) tab.getComponent()).getData().equals(ELEVEN_TAB)) {
                if (layoutGermplasmStudyInformation.getComponentCount() == 0) {
                    layoutGermplasmStudyInformation.addComponent(new GermplasmStudyInfoComponent(dataIndexContainer, gDetailModel, fromUrl));
                    layoutGermplasmStudyInformation.setMargin(true);
                }
            }else if (((VerticalLayout) tab.getComponent()).getData().equals(TWELVE_TAB)) {
                if (layoutMaintenanceNeighborhood.getComponentCount() == 0) {
                    layoutMaintenanceNeighborhood.addComponent(new GermplasmMaintenanceNeighborhoodComponent(gid, qQuery, dataResultIndexContainer, mainLayout, tabSheet));
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() {

        layoutDetails = new VerticalLayout();
        layoutDetails.setData(FIRST_TAB);
        gDetailModel = qQuery.getGermplasmDetails(gid);
        layoutDetails.addComponent(new GermplasmCharacteristicsComponent(gDetailModel));
        
        layoutDetails.setMargin(true);
        
        layoutNames = new VerticalLayout();
        layoutNames.setData(SECOND_TAB);

        layoutAttributes = new VerticalLayout();
        layoutAttributes.setData(THIRD_TAB);

        layoutGenerationHistory = new VerticalLayout();
        layoutGenerationHistory.setData(FOURTH_TAB);

        layoutPedigreeTree = new VerticalLayout();
        layoutPedigreeTree.setData(FIFTH_TAB);

        layoutGermplasmList = new VerticalLayout();
        layoutGermplasmList.setData(SIX_TAB);

        layoutGroupRelatives = new VerticalLayout();
        layoutGroupRelatives.setData(SEVEN_TAB);

        layoutManagementNeighbors = new VerticalLayout();
        layoutManagementNeighbors.setData(EIGHT_TAB);

        layoutDerivativeNeighborhood = new VerticalLayout();
        layoutDerivativeNeighborhood.setData(NINE_TAB);

        layoutInventoryInformation = new VerticalLayout();
        layoutInventoryInformation.setData(TEN_TAB);
        
        layoutGermplasmStudyInformation = new VerticalLayout();
        layoutGermplasmStudyInformation.setData(ELEVEN_TAB);
        
        layoutMaintenanceNeighborhood = new VerticalLayout();
        layoutMaintenanceNeighborhood.setData(TWELVE_TAB);

        layoutPedigreeTree.setMargin(true);

        this.addTab(layoutDetails, messageSource.getMessage(Message.CHARACTERISTICS_LABEL));
        this.addTab(layoutNames, messageSource.getMessage(Message.NAMES_LABEL));
        this.addTab(layoutAttributes, messageSource.getMessage(Message.ATTRIBUTES_LABEL));
        this.addTab(layoutGenerationHistory, messageSource.getMessage(Message.GENERATION_HISTORY_LABEL));
        this.addTab(layoutPedigreeTree, messageSource.getMessage(Message.PEDIGREE_TREE_LABEL));
        this.addTab(layoutGermplasmList, messageSource.getMessage(Message.LISTS_LABEL));
        this.addTab(layoutGroupRelatives, messageSource.getMessage(Message.GROUP_RELATIVES_LABEL));
        this.addTab(layoutManagementNeighbors, messageSource.getMessage(Message.MANAGEMENT_NEIGHBORS_LABEL));
        this.addTab(layoutDerivativeNeighborhood, messageSource.getMessage(Message.DERIVATIVE_NEIGHBORHOOD_LABEL));
        this.addTab(layoutMaintenanceNeighborhood, messageSource.getMessage(Message.MAINTENANCE_NEIGHBORHOOD_LABEL));
        this.addTab(layoutInventoryInformation, messageSource.getMessage(Message.INVENTORY_INFORMATION_LABEL));
        this.addTab(layoutGermplasmStudyInformation, messageSource.getMessage(Message.GERMPLASM_STUDY_INFORMATION_LABEL));
        

        this.addListener(new GermplasmSelectedTabChangeListener(this));
    }

    @Override
    public void attach() {
        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
        /*messageSource.setCaption(germplasmCharacteristicsComponent, Message.characteristics_label);
        messageSource.setCaption(layoutNames, Message.names_label);
        messageSource.setCaption(layoutAttributes, Message.attributes_label);
        messageSource.setCaption(layoutGenerationHistory, Message.generation_history_label);
        messageSource.setCaption(layoutPedigreeTree, Message.pedigree_tree_label);
        messageSource.setCaption(layoutGroupRelatives, Message.group_relatives_label);
        messageSource.setCaption(layoutManagementNeighbors, Message.management_neighbors_label);
        messageSource.setCaption(layoutDerivativeNeighborhood, Message.derivative_neighborhood_label);
        messageSource.setCaption(layoutInventoryInformation, Message.inventory_information_label);*/

    }
    
    
     public void viewPedigreeGraphClickAction() throws InternationalizableException {

            try {
                Window pedigreeGraphWindow = new Window("Pedigree Graph");
                pedigreeGraphWindow.setModal(true);
                pedigreeGraphWindow.setWidth("100%");
                pedigreeGraphWindow.setHeight("620px");
                pedigreeGraphWindow.setName("Pedigree Graph");
                pedigreeGraphWindow.addComponent(new GermplasmPedigreeGraphComponent(this.gid, this.qQuery));
                getWindow().addWindow(pedigreeGraphWindow);
                
            } catch (Exception e) {
                throw new InternationalizableException(e, Message.ERROR_IN_SEARCH, Message.EMPTY_STRING);
            }
        }

     public void refreshPedigreeTree() {
         if(germplasmPedigreeTreeComponent != null){
             layoutPedigreeTree.removeComponent(germplasmPedigreeTreeComponent);
         }
         germplasmPedigreeTreeComponent = new GermplasmPedigreeTreeComponent(gid, qQuery, dataResultIndexContainer, mainLayout, tabSheet, (Boolean) pedigreeDerivativeCheckbox.getValue());
         layoutPedigreeTree.addComponent(germplasmPedigreeTreeComponent);
     }
     
}