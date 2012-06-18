/***************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the 
 * GNU General Public License (http://bit.ly/8Ztv8M) and the 
 * provisions of Part F of the Generation Challenge Programme 
 * Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 **************************************************************/
package org.generationcp.browser.germplasm.application;

import org.generationcp.browser.germplasm.datasource.helper.GermplasmQueries;
import org.generationcp.browser.germplasm.table.indexcontainer.GermplasmIndexContainer;
import org.generationcp.browser.germplasm.wrap.model.GermplasmDetailModel;
import org.generationcp.browser.util.Util;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.VerticalLayout;

public class GermplasmDetail extends HorizontalLayout implements TabSheet.SelectedTabChangeListener {

    private final static Logger log = LoggerFactory.getLogger(GermplasmDetail.class);

    private VerticalLayout detailLayout;

	private Panel panelTree;
	private Panel panelDetail;
	private GermplasmIndexContainer DataIndexContainer;
	private GermplasmQueries qQuery;
	private Table attributeTable;
	private Table namesTable;
	private Table generationHistoryTable;
	private TabSheet tabSheetForTables;
	private Tree pedigreeTree;
	private VerticalLayout tabLayoutNames;
	private VerticalLayout tabLayoutAttributes;
	private VerticalLayout tabLayoutGHistory;
	private GermplasmDetailModel g;
	private boolean tabAttributeVisited=false;
	private boolean tabGHistoryVisited=false;
	private GermplasmPedigreeTree treeData;
	private VerticalLayout mainLayout;
	private TabSheet tabSheet;
	private int screenWidth;


	public GermplasmDetail(int gid,GermplasmQueries qQuery,GermplasmIndexContainer dataResultIndexContainer,VerticalLayout mainLayout,TabSheet tabSheet,int screenWidth) throws QueryException{

		this.mainLayout=mainLayout;
		this.tabSheet=tabSheet;
		this.qQuery= qQuery;
		this.DataIndexContainer=dataResultIndexContainer;
		this.screenWidth=screenWidth;
		g = this.qQuery.getGermplasmDetails(gid);
		
		//panel tree 
		panelTree= new Panel("Pedigree Tree");
		int panelTreeWidth=(int) (screenWidth*.30);
		int panelDetailWidth=(screenWidth-panelTreeWidth) - 60; // 60 extra px to less;
		panelTree.setWidth(String.valueOf(panelTreeWidth)+"px");
		panelTree.setHeight("420px");
		panelTree.setScrollable(true);
		displayTreePanel(Integer.valueOf(gid));
		panelTree.getContent().setSizeUndefined();
		addComponent(panelTree);
		
		//panel germplasm detail 
		panelDetail=new Panel("Germplasm Details");
		detailLayout= new VerticalLayout();
		displayGermplasmInfo();
		createOtherGermplasmInfoTab();
		addGermplasmNamesTable();
		detailLayout.addComponent(tabSheetForTables);
		panelDetail.addComponent(detailLayout);
		panelDetail.setWidth(String.valueOf(panelDetailWidth)+"px");
		panelDetail.setHeight("420px");
		addComponent(panelDetail);
		setMargin(true);
		setSpacing(true);

	}



	private void displayGermplasmInfo(){
		Label lblGID = new Label("GID : "+g.getGid());
		detailLayout.addComponent(lblGID);
		Label lblPrefName = new Label("Preferred Name : "+g.getGermplasmPreferredName());
		detailLayout.addComponent(lblPrefName);
		Label lblLocation = new Label("Location : "+g.getGermplasmLocation());
		detailLayout.addComponent(lblLocation);
		Label lblGermplasmMethod = new Label("Method : "+g.getGermplasmMethod());
		detailLayout.addComponent(lblGermplasmMethod);
		Label lblCreationDate = new Label("Creation Date : "+g.getGermplasmCreationDate());
		detailLayout.addComponent(lblCreationDate);
		Label lblReference = new Label("Reference : "+g.getReference());
		detailLayout.addComponent(lblReference);
		addComponent(detailLayout);

	}


	private void addGermplasmNamesTable(){

		IndexedContainer dataSourceNames=DataIndexContainer.getGermplasNames(g);
		namesTable = new Table("",dataSourceNames);
		namesTable.setSelectable(true);
		namesTable.setMultiSelect(false);
		namesTable.setImmediate(true); // react at once when something is selected
		// turn on column reordering and collapsing
		namesTable.setColumnReorderingAllowed(true);
		namesTable.setColumnCollapsingAllowed(true);
		// set column headers
		namesTable.setColumnHeaders(new String[] {"Type","Name","Date","Location","Type Desc"});
		tabLayoutNames.addComponent(namesTable);
		tabSheetForTables.addComponent(tabLayoutNames);
		namesTable.setSizeFull();
		detailLayout.addComponent(tabSheetForTables);
	}


	private void addGermplasmAttributesTable(){

		IndexedContainer dataSourceAttribute=DataIndexContainer.getGermplasAttribute(g);
		attributeTable = new Table("",dataSourceAttribute);
		// selectable
		attributeTable.setSelectable(true);
		attributeTable.setMultiSelect(false);
		attributeTable.setImmediate(true); // react at once when something is selected

		// turn on column reordering and collapsing
		attributeTable.setColumnReorderingAllowed(true);
		attributeTable.setColumnCollapsingAllowed(true);

		// set column headers
		attributeTable.setColumnHeaders(new String[] {"Type","Name","Date","Location","Type Desc"});
		attributeTable.setSizeFull();
		tabLayoutAttributes.addComponent(attributeTable);
	}

	private void addGenerationHistoryTable(){

		IndexedContainer dataSourceGenerationHistory=DataIndexContainer.getGermplasGenerationHistory(g);
		generationHistoryTable = new Table("",dataSourceGenerationHistory);
		generationHistoryTable.setSelectable(true);
		generationHistoryTable.setMultiSelect(false);
		generationHistoryTable.setImmediate(true); // react at once when something is selected

		// turn on column reordering and collapsing
		generationHistoryTable.setColumnReorderingAllowed(true);
		generationHistoryTable.setColumnCollapsingAllowed(true);
		generationHistoryTable.setSizeFull();

		// set column headers
		generationHistoryTable.setColumnHeaders(new String[] {"GID","PREFNAME"});
		tabLayoutGHistory.addComponent(generationHistoryTable);
		
	}



	private void createOtherGermplasmInfoTab(){
		tabSheetForTables = new TabSheet();
		tabSheetForTables.setWidth("100%");
		tabSheetForTables.setHeight("250px");
		tabSheetForTables.addListener(this);
		tabLayoutNames= new VerticalLayout();
		tabLayoutAttributes= new VerticalLayout();
		tabLayoutGHistory= new VerticalLayout();
		tabLayoutNames.setMargin(true);
		tabLayoutAttributes.setMargin(true);
		tabLayoutGHistory.setMargin(true); 
		
		tabSheetForTables.addTab(tabLayoutNames).setCaption("Names");
		tabSheetForTables.addTab(tabLayoutAttributes).setCaption("Attributes");
		tabSheetForTables.addTab(tabLayoutGHistory).setCaption("Generation History");
		
	}


	public void selectedTabChange(SelectedTabChangeEvent event) {
		final TabSheet source = (TabSheet) event.getSource();
		if (source == tabSheetForTables) {
			// If the first tab was selected.
			if (source.getSelectedTab() == tabLayoutAttributes) {
				if(!tabAttributeVisited){
					addGermplasmAttributesTable();
					tabAttributeVisited=true;
				}
			}else if(source.getSelectedTab() == tabLayoutGHistory){
				if(!tabGHistoryVisited){
					addGenerationHistoryTable();
					tabGHistoryVisited=true;
				}
			}
		}
	}

	private void displayTreePanel(int gid) throws QueryException{

		pedigreeTree = new Tree();
		pedigreeTree.setSizeFull();
		treeData = qQuery.generatePedigreeTree(new Integer(gid), 1);
		addNode(treeData.getRoot(), 1);
		pedigreeTree.setImmediate(false);


		pedigreeTree.addListener(new ItemClickEvent.ItemClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void itemClick(ItemClickEvent event)
			{
				int gid=(Integer) event.getItemId();
				if (event.getButton()==ItemClickEvent.BUTTON_LEFT && event.isDoubleClick()){
					try {
						 displayGermplasmDetailTab(gid);
					} catch (QueryException e) {
						e.printStackTrace();
					}

				}
			}
		});
		
		pedigreeTree.addListener(new Tree.ExpandListener() {
			
			public void nodeExpand(ExpandEvent event) {
				// TODO Auto-generated method stub
				int gid=(Integer) event.getItemId();
				
				try {
					treeData = qQuery.generatePedigreeTree(new Integer(gid), 2);
				} catch (QueryException e) {
					//Log the error
					log.error(e.toString() + "\n" + e.getStackTrace());
					e.printStackTrace();
				}
				addNode(treeData.getRoot(), 2);
			}
	
		});
		
		panelTree.addComponent(pedigreeTree);
	}
	private void addNode(GermplasmPedigreeTreeNode node, int level)
	{
		if (level == 1) {
			String leafNodeLabel=node.getGermplasm().getPreferredName().getNval()+"("+node.getGermplasm().getGid()+")";
			int leafNodeId=node.getGermplasm().getGid();
			pedigreeTree.addItem(leafNodeId);
			pedigreeTree.setItemCaption(leafNodeId, leafNodeLabel);
			pedigreeTree.setParent(leafNodeId,leafNodeId);
			pedigreeTree.setChildrenAllowed(leafNodeId, true);
			//pedigreeTree.expandItemsRecursively(leafNode);
		}

		for(GermplasmPedigreeTreeNode parent : node.getLinkedNodes())
		{
			int leafNodeId=node.getGermplasm().getGid();
			String parentNodeLabel=parent.getGermplasm().getPreferredName().getNval()+"("+parent.getGermplasm().getGid()+")";
			int parentNodeId=parent.getGermplasm().getGid();
			pedigreeTree.addItem(parentNodeId);
			pedigreeTree.setItemCaption(parentNodeId, parentNodeLabel);
			pedigreeTree.setParent(parentNodeId,leafNodeId);
			pedigreeTree.setChildrenAllowed(parentNodeId, true);
			//pedigreeTree.expandItemsRecursively(parentNode);

			addNode(parent, level+1);
		}
	}
	
	private void displayGermplasmDetailTab(int gid) throws QueryException{
		VerticalLayout detailLayout = new VerticalLayout();
		detailLayout.setSpacing(true);
		
		if(!Util.isTabExist(tabSheet,String.valueOf(gid))){
			detailLayout.addComponent(new GermplasmDetail(gid,qQuery,DataIndexContainer,mainLayout,tabSheet,screenWidth));
			Tab tab =tabSheet.addTab(detailLayout, String.valueOf(gid),null);
			tab.setClosable(true);
			tabSheet.setSelectedTab(detailLayout);
			mainLayout.addComponent(tabSheet);
		}else{
			Tab tab=Util.getTabAlreadyExist(tabSheet, String.valueOf(gid));
			tabSheet.setSelectedTab(tab.getComponent());
		}

	}

}
