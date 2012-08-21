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
public class GermplasmDerivativeNeighborhoodComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{

    private static final long serialVersionUID = 1L;
    private GermplasmPedigreeTree germplasmDerivativeNeighborhood;
    private GermplasmQueries qQuery;
    private VerticalLayout mainLayout;
    private TabSheet tabSheet;
    private GermplasmIndexContainer dataIndexContainer;
    private Tree derivativeNeighborhoodTree;
    private int gid;
	private Label labelNumberOfStepsBackward;
	private Label labelNumberOfStepsForward;
	private Button btnDisplay;
	private HorizontalLayout hLayout;
	private Select selectNumberOfStepBackward;
	private Select selectNumberOfStepForward;
	public static final String  DISPLAY_BUTTON_ID="Display Derivative Neighborhood";
  
	@SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(GermplasmDerivativeNeighborhoodComponent.class);

    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmDerivativeNeighborhoodComponent(int gid, GermplasmQueries qQuery, GermplasmIndexContainer dataResultIndexContainer,
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
	        String leafNodeLabel = name + "(" + node.getGermplasm().getGid() + ")";
	        int leafNodeId = node.getGermplasm().getGid();
	        derivativeNeighborhoodTree.addItem(leafNodeId);
	        derivativeNeighborhoodTree.setItemCaption(leafNodeId, leafNodeLabel);
	        derivativeNeighborhoodTree.setParent(leafNodeId, leafNodeId);
	        derivativeNeighborhoodTree.setChildrenAllowed(leafNodeId, true);
	        derivativeNeighborhoodTree.expandItemsRecursively(leafNodeId);
    	 }
        for (GermplasmPedigreeTreeNode parent : node.getLinkedNodes()) {
        	String name = parent.getGermplasm().getPreferredName() != null ? parent.getGermplasm().getPreferredName().getNval() : null;
        	int leafNodeId = node.getGermplasm().getGid();
            String parentNodeLabel = name + "(" + parent.getGermplasm().getGid() + ")";
            int parentNodeId = parent.getGermplasm().getGid();
            derivativeNeighborhoodTree.addItem(parentNodeId);
            derivativeNeighborhoodTree.setItemCaption(parentNodeId, parentNodeLabel);
            derivativeNeighborhoodTree.setParent(parentNodeId, leafNodeId);
            derivativeNeighborhoodTree.setChildrenAllowed(parentNodeId, true);
            derivativeNeighborhoodTree.expandItemsRecursively(parentNodeId);
        	addNode(parent, level + 1);
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
        selectNumberOfStepBackward.select("1");
      
        labelNumberOfStepsForward= new Label();
        selectNumberOfStepForward= new Select ();
        selectNumberOfStepForward.setWidth("50px");
        populateSelectSteps(selectNumberOfStepForward);
        selectNumberOfStepForward.setNullSelectionAllowed(false);
        selectNumberOfStepForward.select("1");
        
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
        
        derivativeNeighborhoodTree= new Tree();
        addComponent(derivativeNeighborhoodTree);
        derivativeNeighborhoodTree.addListener(new GermplasmItemClickListener(this));

        derivativeNeighborhoodTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = 3442425534732855473L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.click_to_view_germplasm_details);
            }
        });
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
    	
        messageSource.setCaption(labelNumberOfStepsBackward, Message.number_of_steps_backward_label);
        messageSource.setCaption(labelNumberOfStepsForward, Message.number_of_steps_forward_label);
        messageSource.setCaption(btnDisplay, Message.display_button_label);
       
    }

	public void displayButtonClickAction() {
		
		this.removeComponent(derivativeNeighborhoodTree);
		derivativeNeighborhoodTree.removeAllItems();
		int numberOfStepsBackward=Integer.valueOf(selectNumberOfStepBackward.getValue().toString());
		int numberOfStepsForward=Integer.valueOf(selectNumberOfStepForward.getValue().toString());
		
		germplasmDerivativeNeighborhood = qQuery.getDerivativeNeighborhood(Integer.valueOf(gid), numberOfStepsBackward,numberOfStepsForward); // throws QueryException
        if (germplasmDerivativeNeighborhood != null) {
            addNode(germplasmDerivativeNeighborhood.getRoot(), 1);
        }
        
        addComponent(derivativeNeighborhoodTree);
		
	}
	
	 public void displayNewGermplasmDetailTab(int gid) throws InternationalizableException {
	        VerticalLayout detailLayout = new VerticalLayout();
	        detailLayout.setSpacing(true);

	        if (!Util.isTabExist(tabSheet, String.valueOf(gid))) {
	            detailLayout.addComponent(new GermplasmDetail(gid, qQuery, dataIndexContainer, mainLayout, tabSheet));
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
