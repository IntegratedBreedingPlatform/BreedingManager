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

import org.generationcp.browser.germplasm.listeners.GermplasmItemClickListener;
import org.generationcp.browser.germplasm.listeners.GermplasmTreeExpandListener;
import org.generationcp.browser.util.Util;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

public class GermplasmPedigreeTreeComponent extends VerticalLayout{

    private Tree pedigreeTree;
    private GermplasmPedigreeTree germplasmPedigreeTree;
    private GermplasmQueries qQuery;
    private VerticalLayout mainLayout;
    private TabSheet tabSheet;
    private GermplasmIndexContainer DataIndexContainer;
    private final static Logger log = LoggerFactory.getLogger(GermplasmPedigreeTreeComponent.class);

    public GermplasmPedigreeTreeComponent(int gid, GermplasmQueries qQuery, GermplasmIndexContainer dataResultIndexContainer,
            VerticalLayout mainLayout, TabSheet tabSheet) throws QueryException {

        this.mainLayout = mainLayout;
        this.tabSheet = tabSheet;
        this.qQuery = qQuery;
        this.DataIndexContainer = dataResultIndexContainer;
        // this.gDetailModel = this.qQuery.getGermplasmDetails(gid);

        pedigreeTree = new Tree();
        pedigreeTree.setSizeFull();
        germplasmPedigreeTree = qQuery.generatePedigreeTree(new Integer(gid), 1);
        addNode(germplasmPedigreeTree.getRoot(), 1);
        pedigreeTree.setImmediate(false);

        pedigreeTree.addListener(new GermplasmItemClickListener(this));
        pedigreeTree.addListener(new GermplasmTreeExpandListener(this));

        pedigreeTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            private static final long serialVersionUID = 3442425534732855473L;

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                return "Click to view germplasm details";
            }
        });
        addComponent(pedigreeTree);
        setSpacing(true);
        setMargin(true);
    }

    private void addNode(GermplasmPedigreeTreeNode node, int level) {
        if (level == 1) {
            String leafNodeLabel = node.getGermplasm().getPreferredName().getNval() + "(" + node.getGermplasm().getGid() + ")";
            int leafNodeId = node.getGermplasm().getGid();
            pedigreeTree.addItem(leafNodeId);
            pedigreeTree.setItemCaption(leafNodeId, leafNodeLabel);
            pedigreeTree.setParent(leafNodeId, leafNodeId);
            pedigreeTree.setChildrenAllowed(leafNodeId, true);
            // pedigreeTree.expandItemsRecursively(leafNode);
        }

        for (GermplasmPedigreeTreeNode parent : node.getLinkedNodes()) {
            int leafNodeId = node.getGermplasm().getGid();
            String parentNodeLabel = parent.getGermplasm().getPreferredName().getNval() + "(" + parent.getGermplasm().getGid() + ")";
            int parentNodeId = parent.getGermplasm().getGid();
            pedigreeTree.addItem(parentNodeId);
            pedigreeTree.setItemCaption(parentNodeId, parentNodeLabel);
            pedigreeTree.setParent(parentNodeId, leafNodeId);
            pedigreeTree.setChildrenAllowed(parentNodeId, true);
            // pedigreeTree.expandItemsRecursively(parentNode);

            addNode(parent, level + 1);
        }
    }

    public void pedigreeTreeExpandAction(int gid) {
        try {
            germplasmPedigreeTree = qQuery.generatePedigreeTree(Integer.valueOf(gid), 2);
        } catch (QueryException e) {
            // Log the error
            log.error(e.toString() + "\n" + e.getStackTrace());
            e.printStackTrace();
        }
        addNode(germplasmPedigreeTree.getRoot(), 2);

    }

    public void displayNewGermplasmDetailTab(int gid) throws QueryException {
        VerticalLayout detailLayout = new VerticalLayout();
        detailLayout.setSpacing(true);

        if (!Util.isTabExist(tabSheet, String.valueOf(gid))) {
            detailLayout.addComponent(new GermplasmDetail(gid, qQuery, DataIndexContainer, mainLayout, tabSheet));
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
