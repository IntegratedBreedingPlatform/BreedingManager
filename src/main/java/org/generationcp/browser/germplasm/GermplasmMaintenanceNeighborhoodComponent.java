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
import org.generationcp.browser.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmMaintenanceNeighborhoodComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{

    private static final long serialVersionUID = 1L;
    private GermplasmPedigreeTree germplasmDerivativeNeighborhood;
    private GermplasmQueries qQuery;
    private VerticalLayout mainLayout;
    private TabSheet tabSheet;
    private GermplasmIndexContainer dataIndexContainer;
    private Tree maintenanceNeighborhoodTree;
    private int gid;
    private Label labelNumberOfStepsBackward;
    private Label labelNumberOfStepsForward;
    private Button btnDisplay;
    private HorizontalLayout hLayout;
    private Select selectNumberOfStepBackward;
    private Select selectNumberOfStepForward;
    public static final String  DISPLAY_BUTTON_ID="Display Derivative Neighborhood";
  
    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(GermplasmMaintenanceNeighborhoodComponent.class);

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmMaintenanceNeighborhoodComponent(int gid, GermplasmQueries qQuery, GermplasmIndexContainer dataResultIndexContainer,
            VerticalLayout mainLayout, TabSheet tabSheet) throws InternationalizableException {

        super();

        this.mainLayout = mainLayout;
        this.tabSheet = tabSheet;
        this.qQuery = qQuery;
        this.dataIndexContainer = dataResultIndexContainer;
        this.gid=gid;
    }

    private void addNode(GermplasmPedigreeTreeNode node, int level) {

    	 if (level == 1) {
    	     String name = node.getGermplasm().getPreferredName() != null ? node.getGermplasm().getPreferredName().getNval() : null;
	     String rootNodeLabel = name + "(" + node.getGermplasm().getGid() + ")";
	     int rootNodeId = node.getGermplasm().getGid();
	     maintenanceNeighborhoodTree.addItem(rootNodeId);
	     maintenanceNeighborhoodTree.setItemCaption(rootNodeId, rootNodeLabel);
	     maintenanceNeighborhoodTree.setParent(rootNodeId, rootNodeId);
	     maintenanceNeighborhoodTree.setChildrenAllowed(rootNodeId, true);
	     maintenanceNeighborhoodTree.expandItemsRecursively(rootNodeId);
    	 }
        for (GermplasmPedigreeTreeNode child : node.getLinkedNodes()) {
            String name = child.getGermplasm().getPreferredName() != null ? child.getGermplasm().getPreferredName().getNval() : null;
            int parentNodeId = node.getGermplasm().getGid();
            String childNodeLabel = name + "(" + child.getGermplasm().getGid() + ")";
            int childNodeId = child.getGermplasm().getGid();
            maintenanceNeighborhoodTree.addItem(childNodeId);
            maintenanceNeighborhoodTree.setItemCaption(childNodeId, childNodeLabel);
            maintenanceNeighborhoodTree.setParent(childNodeId, parentNodeId);
            maintenanceNeighborhoodTree.setChildrenAllowed(childNodeId, true);
            maintenanceNeighborhoodTree.expandItemsRecursively(childNodeId);
            
            if(child.getGermplasm().getGid()==gid){
            	maintenanceNeighborhoodTree.setValue(childNodeId);
            	maintenanceNeighborhoodTree.setImmediate(true);
            }
            
            addNode(child, level + 1);
        }
    }
    
    @Override
    public void afterPropertiesSet() {
    	setSpacing(true);
        setMargin(true);
        
        hLayout= new HorizontalLayout();
        hLayout.setSpacing(true);
        
        labelNumberOfStepsBackward=new Label();
        selectNumberOfStepBackward= new Select ();
        selectNumberOfStepBackward.setWidth("50px");
        populateSelectSteps(selectNumberOfStepBackward);
        selectNumberOfStepBackward.setNullSelectionAllowed(false);
        selectNumberOfStepBackward.select("2");
      
        labelNumberOfStepsForward= new Label();
        selectNumberOfStepForward= new Select ();
        selectNumberOfStepForward.setWidth("50px");
        populateSelectSteps(selectNumberOfStepForward);
        selectNumberOfStepForward.setNullSelectionAllowed(false);
        selectNumberOfStepForward.select("3");
        
        btnDisplay = new Button();
        btnDisplay.setData(DISPLAY_BUTTON_ID);
        btnDisplay.setDescription("Display Germplasm Derivative Neighborhood ");
        btnDisplay.addListener(new GermplasmButtonClickListener(this));
        
        hLayout.addComponent(labelNumberOfStepsBackward);
        hLayout.addComponent(selectNumberOfStepBackward);
        hLayout.addComponent(labelNumberOfStepsForward);
        hLayout.addComponent(selectNumberOfStepForward);
        hLayout.addComponent(btnDisplay);
        addComponent(hLayout);
        
        maintenanceNeighborhoodTree= new Tree();
        addComponent(maintenanceNeighborhoodTree);
        maintenanceNeighborhoodTree.addListener(new GermplasmItemClickListener(this));

        maintenanceNeighborhoodTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = 3442425534732855473L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.CLICK_TO_VIEW_GERMPLASM_DETAILS);
            }
        });
        
        displayButtonClickAction();
    }

    private void populateSelectSteps(Select select) {
    	
    	for(int i=1;i<=10;i++){
    		select.addItem(String.valueOf(i));
    	}
	}


	@Override
    public void attach() {

        super.attach();
        updateLabels();
    }

    @Override
    public void updateLabels() {
    	
        messageSource.setCaption(labelNumberOfStepsBackward, Message.NUMBER_OF_STEPS_BACKWARD_LABEL);
        messageSource.setCaption(labelNumberOfStepsForward, Message.NUMBER_OF_STEPS_FORWARD_LABEL);
        messageSource.setCaption(btnDisplay, Message.DISPLAY_BUTTON_LABEL);
       
    }

	public void displayButtonClickAction() {
		
		this.removeComponent(maintenanceNeighborhoodTree);
		maintenanceNeighborhoodTree.removeAllItems();
		int numberOfStepsBackward=Integer.valueOf(selectNumberOfStepBackward.getValue().toString());
		int numberOfStepsForward=Integer.valueOf(selectNumberOfStepForward.getValue().toString());
		
		germplasmDerivativeNeighborhood = qQuery.getMaintenanceNeighborhood(Integer.valueOf(gid), numberOfStepsBackward,numberOfStepsForward); // throws QueryException
        if (germplasmDerivativeNeighborhood != null) {
            addNode(germplasmDerivativeNeighborhood.getRoot(), 1);
        }
        
        addComponent(maintenanceNeighborhoodTree);
		
	}
	
	 public void displayNewGermplasmDetailTab(int gid) throws InternationalizableException {
	     if(this.mainLayout != null && this.tabSheet != null) {
	        VerticalLayout detailLayout = new VerticalLayout();
	        detailLayout.setSpacing(true);

	        if (!Util.isTabExist(tabSheet, String.valueOf(gid))) {
	            detailLayout.addComponent(new GermplasmDetail(gid, qQuery, dataIndexContainer, mainLayout, tabSheet, false));
	            Tab tab = tabSheet.addTab(detailLayout, String.valueOf(gid), null);
	            tab.setClosable(true);
	            tabSheet.setSelectedTab(detailLayout);
	            mainLayout.addComponent(tabSheet);

	        } else {
	            Tab tab = Util.getTabAlreadyExist(tabSheet, String.valueOf(gid));
	            tabSheet.setSelectedTab(tab.getComponent());
	        }

	     }
	 }
}
