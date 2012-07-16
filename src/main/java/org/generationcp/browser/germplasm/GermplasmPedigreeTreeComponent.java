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
import org.generationcp.browser.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.browser.germplasm.listeners.GermplasmTreeExpandListener;
import org.generationcp.browser.util.Util;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmPedigreeTreeComponent extends Tree implements InitializingBean, InternationalizableComponent{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private GermplasmPedigreeTree germplasmPedigreeTree;
    private GermplasmQueries qQuery;
    private VerticalLayout mainLayout;
    private TabSheet tabSheet;
    private GermplasmIndexContainer dataIndexContainer;
    private final static Logger LOG = LoggerFactory.getLogger(GermplasmPedigreeTreeComponent.class);
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmPedigreeTreeComponent(int gid, GermplasmQueries qQuery, GermplasmIndexContainer dataResultIndexContainer,
            VerticalLayout mainLayout, TabSheet tabSheet) throws QueryException {


        super();
        
        this.mainLayout = mainLayout;
        this.tabSheet = tabSheet;
        this.qQuery = qQuery;
        this.dataIndexContainer = dataResultIndexContainer;
        // this.gDetailModel = this.qQuery.getGermplasmDetails(gid);

        this.setSizeFull();
        germplasmPedigreeTree = qQuery.generatePedigreeTree(Integer.valueOf(gid), 1);
        addNode(germplasmPedigreeTree.getRoot(), 1);
        this.setImmediate(false);

        this.addListener(new GermplasmItemClickListener(this));
        this.addListener(new GermplasmTreeExpandListener(this));

        this.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = 3442425534732855473L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.click_to_view_germplasm_details);
            }
        });
    }

    private void addNode(GermplasmPedigreeTreeNode node, int level) {
        if (level == 1) {
            String leafNodeLabel = node.getGermplasm().getPreferredName().getNval() + "(" + node.getGermplasm().getGid() + ")";
            int leafNodeId = node.getGermplasm().getGid();
            this.addItem(leafNodeId);
            this.setItemCaption(leafNodeId, leafNodeLabel);
            this.setParent(leafNodeId, leafNodeId);
            this.setChildrenAllowed(leafNodeId, true);
            // pedigreeTree.expandItemsRecursively(leafNode);
        }

        for (GermplasmPedigreeTreeNode parent : node.getLinkedNodes()) {
            int leafNodeId = node.getGermplasm().getGid();
            String parentNodeLabel = parent.getGermplasm().getPreferredName().getNval() + "(" + parent.getGermplasm().getGid() + ")";
            int parentNodeId = parent.getGermplasm().getGid();
            this.addItem(parentNodeId);
            this.setItemCaption(parentNodeId, parentNodeLabel);
            this.setParent(parentNodeId, leafNodeId);
            this.setChildrenAllowed(parentNodeId, true);
            // pedigreeTree.expandItemsRecursively(parentNode);

            addNode(parent, level + 1);
        }
    }

    public void pedigreeTreeExpandAction(int gid) {
        try {
            germplasmPedigreeTree = qQuery.generatePedigreeTree(Integer.valueOf(gid), 2);
        } catch (QueryException e) {
            // Log the error
            LOG.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
        }
        addNode(germplasmPedigreeTree.getRoot(), 2);

    }

    public void displayNewGermplasmDetailTab(int gid) throws QueryException {
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
    
    @Override
    public void afterPropertiesSet() {
    	

    }
    
    @Override
    public void attach() {
    	
        super.attach();
        
        updateLabels();
    }
    

	@Override
	public void updateLabels() {

        
	}
    
}
