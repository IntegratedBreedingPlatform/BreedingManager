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
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmTreeExpandListener;
import org.generationcp.breeding.manager.listmanager.util.germplasm.GermplasmIndexContainer;
import org.generationcp.breeding.manager.listmanager.util.germplasm.GermplasmQueries;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

@Configurable
public class GermplasmPedigreeTreeComponent extends Tree implements InitializingBean, InternationalizableComponent{

    private static final long serialVersionUID = 1L;
    
    private GermplasmPedigreeTree germplasmPedigreeTree;
    private GermplasmQueries qQuery;
    private Boolean includeDerivativeLines;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;

    public GermplasmPedigreeTreeComponent(int gid, GermplasmQueries qQuery, GermplasmIndexContainer dataResultIndexContainer,
            VerticalLayout mainLayout, TabSheet tabSheet) throws InternationalizableException {

        super();

        this.qQuery = qQuery;
        this.includeDerivativeLines = false;

        this.setSizeFull();
        germplasmPedigreeTree = qQuery.generatePedigreeTree(Integer.valueOf(gid), 1); // throws QueryException
        addNode(germplasmPedigreeTree.getRoot(), 1);
        this.setImmediate(false);
        
        this.addListener(new GermplasmTreeExpandListener(this));

        this.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = 3442425534732855473L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.CLICK_TO_VIEW_GERMPLASM_DETAILS);
            }
        });
    }

    public GermplasmPedigreeTreeComponent(int gid, GermplasmQueries qQuery, GermplasmIndexContainer dataResultIndexContainer,
            VerticalLayout mainLayout, ListManagerDetailsLayout detailsLayout, Boolean includeDerivativeLines) throws InternationalizableException {

        super();

        this.qQuery = qQuery;
        this.includeDerivativeLines = includeDerivativeLines;

        this.setSizeFull();
        germplasmPedigreeTree = qQuery.generatePedigreeTree(Integer.valueOf(gid), 1, includeDerivativeLines); // throws QueryException
        addNode(germplasmPedigreeTree.getRoot(), 1);
        this.setImmediate(false);

        this.addListener(new GermplasmTreeExpandListener(this));

        this.addListener(new ItemClickEvent.ItemClickListener() {
			private static final long serialVersionUID = -6626097251439208783L;

			@Override
			public void itemClick(ItemClickEvent event) {
				String item = event.getItemId().toString();
				if(isExpanded(item)){
					collapseItem(item);
				} else{
					expandItem(item);
					pedigreeTreeExpandAction(item);
				}
			}
		});
        
        this.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = 3442425534732855473L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return messageSource.getMessage(Message.CLICK_TO_VIEW_GERMPLASM_DETAILS);
            }
        });
    }
   
    
    private void addNode(GermplasmPedigreeTreeNode node, int level) {
        if (level == 1) {
            String preferredName="";
            try{
                preferredName= node.getGermplasm().getPreferredName().getNval();
            }catch(Exception e){
                preferredName=String.valueOf(node.getGermplasm().getGid());
            }
            String leafNodeLabel = preferredName + "(" + node.getGermplasm().getGid() + ")";
            String leafNodeId = node.getGermplasm().getGid().toString();
            this.addItem(leafNodeId);
            this.setItemCaption(leafNodeId, leafNodeLabel);
            this.setParent(leafNodeId, leafNodeId);
            this.setChildrenAllowed(leafNodeId, true);
            // pedigreeTree.expandItemsRecursively(leafNode);
        }

        for (GermplasmPedigreeTreeNode parent : node.getLinkedNodes()) {
            String leafNodeId = node.getGermplasm().getGid().toString();
            String preferredName="";
            try{
                preferredName= parent.getGermplasm().getPreferredName().getNval();
            }catch(Exception e){
                preferredName=String.valueOf(parent.getGermplasm().getGid());
            }

            String parentNodeLabel = preferredName + "(" + parent.getGermplasm().getGid() + ")";
            String parentNodeId = node.getGermplasm().getGid() + "@" + parent.getGermplasm().getGid();
            this.addItem(parentNodeId);
            this.setItemCaption(parentNodeId, parentNodeLabel);
            this.setParent(parentNodeId, leafNodeId);
            this.setChildrenAllowed(parentNodeId, true);
            // pedigreeTree.expandItemsRecursively(parentNode);

            addNode(parent, level + 1);
        }
    }

    private void addNode(GermplasmPedigreeTreeNode node, String itemIdOfParent){
        for (GermplasmPedigreeTreeNode parent : node.getLinkedNodes()) {
            String leafNodeId = itemIdOfParent;
            String preferredName="";
            try{
                preferredName= parent.getGermplasm().getPreferredName().getNval();
            }catch(Exception e){
                preferredName=String.valueOf(parent.getGermplasm().getGid());
            }

            String parentNodeLabel = preferredName + "(" + parent.getGermplasm().getGid() + ")";
            String parentNodeId = node.getGermplasm().getGid() + "@" + parent.getGermplasm().getGid();
            this.addItem(parentNodeId);
            this.setItemCaption(parentNodeId, parentNodeLabel);
            this.setParent(parentNodeId, leafNodeId);
            this.setChildrenAllowed(parentNodeId, true);
        }
    }
    
    public void pedigreeTreeExpandAction(String itemId) throws InternationalizableException {
        if(itemId.contains("@")){
            String gidString = itemId.substring(itemId.indexOf("@")+1, itemId.length());
            germplasmPedigreeTree = qQuery.generatePedigreeTree(Integer.valueOf(gidString), 2, includeDerivativeLines);
            addNode(germplasmPedigreeTree.getRoot(), itemId);
        } else {
            germplasmPedigreeTree = qQuery.generatePedigreeTree(Integer.valueOf(itemId), 2, includeDerivativeLines);
            addNode(germplasmPedigreeTree.getRoot(), 2);
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
