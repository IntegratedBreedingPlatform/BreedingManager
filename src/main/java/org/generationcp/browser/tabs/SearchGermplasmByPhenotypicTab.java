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
package org.generationcp.browser.tabs;

import java.util.ArrayList;
import java.util.Iterator;

import org.generationcp.browser.germplasm.datasource.helper.GidByPhenotypicQueries;
import org.generationcp.browser.germplasm.table.indexcontainer.TraitDataIndexContainer;
import org.generationcp.middleware.exceptions.QueryException;
import org.generationcp.middleware.pojos.NumericRange;
import org.generationcp.middleware.pojos.TraitCombinationFilter;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class SearchGermplasmByPhenotypicTab extends GridLayout
{
	private static final long serialVersionUID = 455865362407450432L;
	
	private VerticalLayout componentTrait;
	private VerticalLayout componentTtraitValueInput;
	private Table traitTable;
	private Table scaleTable;
	private Table traitMethodTable;
	private Table scaleValueTable;
	private Table  criteriaTable;
	private Table searchResultTable;
	private IndexedContainer dataSourceTrait;
	private IndexedContainer dataSourceScale;
	private IndexedContainer dataSourceTraitMethod;
	private IndexedContainer dataSourceScaleValue;
	private IndexedContainer dataSourceSearchCriteria;
	private IndexedContainer dataSourceSearchResult;
	private TextField txtValueInput;
	private Button btnAddCriteria;
	private Button btnSearch;
	private Button btnDelete;
	private Button btnDeleteAll;
	private Label rangeInstructionLabel;
	private Label valueOptionsInstuctionLabel;
	private GidByPhenotypicQueries gidsByPhenotypic;
	private TraitDataIndexContainer dataIndexContainer;
	
	private int traitID;
	private int flagScale;
	private int flagResult=0;

	public SearchGermplasmByPhenotypicTab(GidByPhenotypicQueries gidsByPhenotypicParam, TraitDataIndexContainer dataIndexContainerParam) throws QueryException
	{
		this.gidsByPhenotypic = gidsByPhenotypicParam;
		this.dataIndexContainer = dataIndexContainerParam;
		
		this.setColumns(4);
		this.setRows(4);
		this.setSpacing(true);

		componentTrait = new VerticalLayout();
		componentTrait.setSpacing(true);

		componentTtraitValueInput= new VerticalLayout();
		componentTtraitValueInput.setSpacing(true);

		Label mainLabel = new Label("<h1>Retrieve Germplasms By Phenotypic Data</h1>");
		mainLabel.setContentMode(Label.CONTENT_XHTML);
		componentTrait.addComponent(mainLabel);
		
		displayTraitTable();


		Label step5Label = new Label("<h3>Step 5 - Add search criteria.</h3>");
		step5Label.setContentMode(Label.CONTENT_XHTML);
		componentTtraitValueInput.addComponent(step5Label);
		
		btnAddCriteria = new Button("Add Criteria");
		componentTtraitValueInput.addComponent(btnAddCriteria);

		btnAddCriteria.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {

				String valueCriteria="";
				String scaleDiscreteValue = "";

				if(withSelectedTraitScaleMethod()){
					if(flagScale==1){
						for(Iterator i= scaleValueTable.getItemIds().iterator(); i.hasNext();){
							int iid = (Integer) i.next();
							Item item=scaleValueTable.getItem(iid);
							Button button = (Button)(item.getItemProperty("select").getValue());

							if((Boolean)button.getValue()==true)
							{
								valueCriteria += item.getItemProperty("Value").getValue() + ",";
								scaleDiscreteValue = "" + item.getItemProperty("scaleValue").getValue();
							}
						}

					}else{
						valueCriteria=txtValueInput.getValue().toString();
					}

					addToCriteriaTable(valueCriteria, scaleDiscreteValue);

				}else{
					System.out.println("Error");
				}
			}

		});


		displaySearchCriteria();

		HorizontalLayout hButton=new HorizontalLayout();
		hButton.setSpacing(true);

		btnDelete = new Button("Delete");
		btnDelete.setDescription("You can delete the currently selected criteria.");
		hButton.addComponent(btnDelete);

		btnDelete.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				Object itemID=criteriaTable.getValue();
				if(criteriaTable.isSelected(itemID)){
					criteriaTable.removeItem(itemID);
				}
				
				if(criteriaTable.getItemIds().isEmpty())
					btnSearch.setEnabled(false);
			}

		});

		btnDeleteAll = new Button("Delete All");
		btnDeleteAll.setDescription("You can delete all the criteria.");
		hButton.addComponent(btnDelete);

		btnDeleteAll.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {

				criteriaTable.removeAllItems();
				btnSearch.setEnabled(false);
			}

		});

		hButton.addComponent(btnDeleteAll);

		componentTtraitValueInput.addComponent(hButton);

		Label finalStepLabel = new Label("<h3>Final Step - Perform the Search.</h3>");
		finalStepLabel.setContentMode(Label.CONTENT_XHTML);
		componentTtraitValueInput.addComponent(finalStepLabel);
		
		btnSearch = new Button("Search");
		btnSearch.setEnabled(false);
		componentTtraitValueInput.addComponent(btnSearch);

		btnSearch.addListener(new Button.ClickListener() {
			/**
			 * 
			 */

			public void buttonClick(ClickEvent event) {

				try{
					ArrayList<Integer> gids=gidsByPhenotypic.getGIDSByPhenotypicData(getSearchFilters());

					if(flagResult==0){
						displayGidsToResultTable(gids);
						componentTtraitValueInput.addComponent(searchResultTable);
						flagResult=1;
					}else{
						searchResultTable.removeAllItems();
						dataSourceSearchResult=dataIndexContainer.addGidsResult(gids);
						searchResultTable.setContainerDataSource(dataSourceSearchResult);
					}

				}catch(Exception e){
					System.out.println("Error");
					e.printStackTrace();
					
				}
			}

		});


		this.addComponent(componentTrait,1,1);
		this.addComponent(componentTtraitValueInput,3,1);

		traitTable.addListener(new ItemClickListener() {

			public void itemClick(ItemClickEvent event) {
				// TODO Auto-generated method stub

				try{
					((Table) event.getSource()).select(event.getItemId());
					int traitID=Integer.valueOf(event.getItem().getItemProperty("traitID").toString());
					dataSourceScale =dataIndexContainer.getScaleByTraitID(traitID);
					scaleTable.setContainerDataSource(dataSourceScale);
					scaleTable.setValue(scaleTable.firstItemId());
					scaleTable.setVisibleColumns(new String[]{"scaleName","scaleType"});
					scaleSetFirstRowSelected();
					updateScaleValueInputDisplay(getScaleType(),getScaleID());
					scaleTable.requestRepaint();

					dataSourceTraitMethod=dataIndexContainer.getMethodTraitID(traitID);
					traitMethodTable.setContainerDataSource(dataSourceTraitMethod);
					traitMethodTable.setVisibleColumns(new String[]{"methodName","methodDescription"});
					traitMethodTable.setValue(traitMethodTable.firstItemId());
					traitMethodTable.requestRepaint();
				}catch(Exception e){

				}

			}
		});



		scaleTable.addListener(new ItemClickListener() {

			public void itemClick(ItemClickEvent event) {
				// TODO Auto-generated method stub

				((Table) event.getSource()).select(event.getItemId());
				int scaleID=Integer.valueOf(event.getItem().getItemProperty("scaleID").toString());
				String scaleType=event.getItem().getItemProperty("scaleType").toString();
				updateScaleValueInputDisplay(scaleType,scaleID);
			}
		});
		
	}

	// Trait Table

	private void displayTraitTable() throws QueryException{
		dataSourceTrait=dataIndexContainer.getAllTrait();
		traitTable = new Table("",dataSourceTrait);

		// set a style name, so we can style rows and cells
		traitTable.setStyleName("iso3166");
		// size
		traitTable.setWidth("600px");
		traitTable.setHeight("200px");

		// selectable
		traitTable.setSelectable(true);
		traitTable.setMultiSelect(false);
		traitTable.setImmediate(true); // react at once when something is selected

		// turn on column reordering and collapsing
		traitTable.setColumnReorderingAllowed(true);
		traitTable.setColumnCollapsingAllowed(true);

		// set column headers
		traitTable.setColumnHeaders(new String[] {"TraitID","Trait Name","Description"});
		traitTable.setVisibleColumns(new String[]{"traitName","traitDesc"});

		// Column alignment
		traitTable.setCaption("Traits");

		traitTable.setValue(traitTable.firstItemId());
		Object itemID= traitTable.getValue();
		traitID=Integer.valueOf(traitTable.getItem(itemID).getItemProperty("traitID").toString());

		Label step1Label = new Label("<h3>Step 1 - Select a Trait.</h3>");
		step1Label.setContentMode(Label.CONTENT_XHTML);
		componentTrait.addComponent(step1Label);
		componentTrait.addComponent(traitTable);

		displayScaleTable(traitID);
		displayMethodTable(traitID);

	}

	// Scale Table
	private void displayScaleTable(int traitID){
		dataSourceScale=dataIndexContainer.getScaleByTraitID(traitID);
		scaleTable = new Table("",dataSourceScale);

		// set a style name, so we can style rows and cells
		scaleTable.setStyleName("iso3166");
		// size
		scaleTable.setWidth("600px");
		scaleTable.setHeight("200px");

		// selectable
		scaleTable.setSelectable(true);
		scaleTable.setMultiSelect(false);
		scaleTable.setImmediate(true); // react at once when something is selected

		// turn on column reordering and collapsing
		scaleTable.setColumnReorderingAllowed(true);
		scaleTable.setColumnCollapsingAllowed(true);

		// set column headers
		scaleTable.setColumnHeaders(new String[] {"ScaleID","Name","Type"});
		scaleTable.setVisibleColumns(new String[]{"scaleName","scaleType"});
		
		// Column alignment
		scaleTable.setCaption("Scales");

		scaleSetFirstRowSelected();
		displayInputValueForm();
		displayScaleValueTable();
		updateScaleValueInputDisplay(getScaleType(),getScaleID());

		Label step2Label = new Label("<h3>Step 2 - Select a scale.</h3>");
		step2Label.setContentMode(Label.CONTENT_XHTML);
		componentTrait.addComponent(step2Label);
		componentTrait.addComponent(scaleTable);

	}

	// TraitMethod Table
	private void displayMethodTable(int traitID){
		dataSourceTraitMethod=dataIndexContainer.getMethodTraitID(traitID);
		traitMethodTable = new Table("",dataSourceTraitMethod);

		// set a style name, so we can style rows and cells
		traitMethodTable.setStyleName("iso3166");
		// size
		traitMethodTable.setWidth("600px");
		traitMethodTable.setHeight("200px");

		// selectable
		traitMethodTable.setSelectable(true);
		traitMethodTable.setMultiSelect(false);
		traitMethodTable.setImmediate(true); // react at once when something is selected

		// turn on column reordering and collapsing
		traitMethodTable.setColumnReorderingAllowed(true);
		traitMethodTable.setColumnCollapsingAllowed(true);

		// set column headers
		traitMethodTable.setColumnHeaders(new String[] {"ID","Name","Description"});
		traitMethodTable.setVisibleColumns(new String[]{"methodName","methodDescription"});
		
		// Column alignment
		traitMethodTable.setCaption("Methods");

		traitMethodTable.setValue(traitMethodTable.firstItemId());

		Label step3Label = new Label("<h3>Step 3 - Select a Method.</h3>");
		step3Label.setContentMode(Label.CONTENT_XHTML);
		componentTrait.addComponent(step3Label);
		componentTrait.addComponent(traitMethodTable);

	}

	private void displayScaleValueTable(){
		IndexedContainer dataSourceScaleValue=dataIndexContainer.getValueByScaleID(-1);
		scaleValueTable = new Table("",dataSourceScaleValue);

		// set a style name, so we can style rows and cells
		scaleValueTable.setStyleName("iso3166");
		// size
		scaleValueTable.setWidth("400px");
		scaleValueTable.setHeight("150px");

		// selectable
		scaleValueTable.setSelectable(true);
		scaleValueTable.setMultiSelect(false);
		scaleValueTable.setImmediate(true); // react at once when something is selected

		// set column headers
		scaleValueTable.setColumnHeaders(new String[] {"Select","Value Description","Value"});
		
		// Column alignment
		scaleValueTable.setCaption("Value Options");
		valueOptionsInstuctionLabel = new Label("Select a value from the options below.");
		valueOptionsInstuctionLabel.setVisible(false);
		componentTtraitValueInput.addComponent(valueOptionsInstuctionLabel);
		scaleValueTable.setVisible(false);
		componentTtraitValueInput.addComponent(scaleValueTable);


	}

	private void displaySearchCriteria(){
		dataSourceSearchCriteria=dataIndexContainer.addSearchCriteria();
		criteriaTable = new Table("",dataSourceSearchCriteria);

		// set a style name, so we can style rows and cells
		criteriaTable.setStyleName("iso3166");
		// size
		criteriaTable.setWidth("500px");
		criteriaTable.setHeight("200px");
	

		// selectable
		criteriaTable.setSelectable(true);
		criteriaTable.setMultiSelect(false);
		criteriaTable.setImmediate(true); // react at once when something is selected

		// turn on column reordering and collapsing
		criteriaTable.setColumnReorderingAllowed(true);
		criteriaTable.setColumnCollapsingAllowed(true);

		// set column headers
		criteriaTable.setColumnHeaders(new String[] {"TraitID","ScaleID","MethodID","Trait Name",
				"Scale Name","Method Name","Value","Scale Datatype", "Scale Discrete Value"});
		criteriaTable.setVisibleColumns(new Object[]{"traitName","scaleName","methodName","criteriaValue", "scaleDiscreteValue"});

		// Column alignment
		criteriaTable.setCaption("Search Criteria");
		componentTtraitValueInput.addComponent(criteriaTable);

	}


	private void displayGidsToResultTable(ArrayList<Integer> gids){

		dataSourceSearchResult=dataIndexContainer.addGidsResult(gids);
		searchResultTable = new Table("",dataSourceSearchResult);

		// set a style name, so we can style rows and cells
		searchResultTable.setStyleName("iso3166");
		// size
		searchResultTable.setWidth("200px");
		searchResultTable.setHeight("400px");

		// selectable
		searchResultTable.setSelectable(true);
		searchResultTable.setMultiSelect(false);
		searchResultTable.setImmediate(true); // react at once when something is selected

		// turn on column reordering and collapsing
		searchResultTable.setColumnReorderingAllowed(true);
		searchResultTable.setColumnCollapsingAllowed(true);

		// set column headers
		searchResultTable.setColumnHeaders(new String[] {"GID"});
	}



	private void displayInputValueForm(){

		Label step4Label = new Label("<h3>Step 4 - Enter a Value.</h3>");
		step4Label.setContentMode(Label.CONTENT_XHTML);
		componentTtraitValueInput.addComponent(step4Label);
		txtValueInput = new TextField("Value");
		txtValueInput.setEnabled(false);
		txtValueInput.setVisible(false);
		componentTtraitValueInput.addComponent(txtValueInput);
		rangeInstructionLabel = new Label("To enter a range of values follow this example: 10 - 20");
		rangeInstructionLabel.setVisible(false);
		componentTtraitValueInput.addComponent(rangeInstructionLabel);
	}

	private void updateScaleValueInputDisplay(String scaleType,int scaleID){
		if(scaleType.equals("discrete")){
			dataSourceScaleValue =dataIndexContainer.getValueByScaleID(scaleID);
			scaleValueTable.setContainerDataSource(dataSourceScaleValue);
			scaleValueTable.setVisibleColumns(new String[]{"select", "Value"});
			valueOptionsInstuctionLabel.setVisible(true);
			scaleValueTable.setEnabled(true);
			scaleValueTable.setVisible(true);
			rangeInstructionLabel.setVisible(false);
			txtValueInput.setVisible(false);
			txtValueInput.setEnabled(false);
			flagScale=1;
			txtValueInput.setValue("");
		}else{
			valueOptionsInstuctionLabel.setVisible(false);
			scaleValueTable.removeAllItems();
			scaleValueTable.setEnabled(false);
			scaleValueTable.setVisible(false);
			txtValueInput.setVisible(true);
			txtValueInput.setEnabled(true);
			rangeInstructionLabel.setVisible(true);
			flagScale=2;
		}
	}

	private int getScaleID(){
		Object itemID= scaleTable.getValue();
		int scaleID=Integer.valueOf(scaleTable.getItem(itemID).getItemProperty("scaleID").toString());
		return scaleID;
	}

	private String getScaleType(){
		Object itemID= scaleTable.getValue();
		String scaleType=scaleTable.getItem(itemID).getItemProperty("scaleType").toString();
		return scaleType;
	}

	private void scaleSetFirstRowSelected(){
		scaleTable.setValue(scaleTable.firstItemId());
	}

	private void addToCriteriaTable(String criteriaValue, String scaleDiscreteValue){

		int traitID=Integer.valueOf(traitTable.getItem(traitTable.getValue()).getItemProperty("traitID").toString());
		String tName=traitTable.getItem(traitTable.getValue()).getItemProperty("traitName").toString();
		int scaleID=Integer.valueOf(scaleTable.getItem(scaleTable.getValue()).getItemProperty("scaleID").toString());
		String sName=scaleTable.getItem(scaleTable.getValue()).getItemProperty("scaleName").toString();
		int methodID=Integer.valueOf(traitMethodTable.getItem(traitMethodTable.getValue()).getItemProperty("methodID").toString());
		String mName=traitMethodTable.getItem(traitMethodTable.getValue()).getItemProperty("methodName").toString();
		String scaleType=scaleTable.getItem(scaleTable.getValue()).getItemProperty("scaleType").toString();


		Object itemId = dataSourceSearchCriteria.addItem();
		Item item = dataSourceSearchCriteria.getItem(itemId);
		item.getItemProperty("traitID").setValue(traitID);
		item.getItemProperty("scaleID").setValue(scaleID);
		item.getItemProperty("methodID").setValue(methodID);
		item.getItemProperty("traitName").setValue(tName);
		item.getItemProperty("scaleName").setValue(sName);
		item.getItemProperty("methodName").setValue(mName);
		item.getItemProperty("criteriaValue").setValue(criteriaValue);
		item.getItemProperty("scaleType").setValue(scaleType);
		item.getItemProperty("scaleDiscreteValue").setValue(scaleDiscreteValue);
		btnSearch.setEnabled(true);
	}

	private ArrayList<TraitCombinationFilter> getSearchFilters(){

		ArrayList<TraitCombinationFilter> tcf=new ArrayList<TraitCombinationFilter>();
		double start = 0;
		double end = 0;

		for(Iterator i= criteriaTable.getItemIds().iterator(); i.hasNext();){


			int iid = (Integer) i.next();
			Item item=criteriaTable.getItem(iid);
			int traitID=Integer.valueOf(item.getItemProperty("traitID").toString());
			int scaleID=Integer.valueOf(item.getItemProperty("scaleID").toString());
			int methodID=Integer.valueOf(item.getItemProperty("methodID").toString());
			String scaleType=item.getItemProperty("scaleType").toString();
			String criteriaValue=item.getItemProperty("criteriaValue").toString();
			String scaleDiscreteValue = item.getItemProperty("scaleDiscreteValue").toString();

			if(scaleType.equals("discrete"))
			{
				String valueToUse = (scaleDiscreteValue != null && !scaleDiscreteValue.equals("")) ? scaleDiscreteValue : criteriaValue;
				valueToUse = valueToUse.trim();
				
				//if the value can be a number add a filter with value as a double
				try
				{
					Double valueToUseInDouble = Double.valueOf(valueToUse);
					TraitCombinationFilter filter = new TraitCombinationFilter(new Integer(traitID), new Integer(scaleID), new Integer(methodID), valueToUseInDouble);
					System.out.println("" + traitID + ":" + scaleID + ":" + methodID + ":" + valueToUseInDouble);
					tcf.add(filter);
				}
				catch(NumberFormatException ex)
				{
				}
				
				TraitCombinationFilter filter = new TraitCombinationFilter(new Integer(traitID), new Integer(scaleID), new Integer(methodID), valueToUse);
				System.out.println("" + traitID + ":" + scaleID + ":" + methodID + ":" + valueToUse);
				tcf.add(filter);
			}
			else
			{
				//check if the value is a numeric range
				boolean notNumericRange = false;
				if(criteriaValue.contains("-"))
				{
					String[] c=criteriaValue.split("-");
					if(c.length == 2)
					{
						try
						{
							start=Double.valueOf(c[0].trim());
							end=Double.valueOf(c[1].trim());
							NumericRange ranges=new NumericRange(start, end);
							TraitCombinationFilter tcFilter=new TraitCombinationFilter(new Integer(traitID),new Integer(scaleID),new Integer(methodID),ranges);
							tcf.add(tcFilter);
						}
						catch(NumberFormatException ex)
						{
							notNumericRange = true;
						}
					}
					else
					{
						notNumericRange = true;
					}
				}
				else
				{
					notNumericRange = true;
				}
				
				if(notNumericRange)
				{
					TraitCombinationFilter tcFilter=new TraitCombinationFilter(new Integer(traitID),new Integer(scaleID),new Integer(methodID),criteriaValue);
					tcf.add(tcFilter);
				}
			}

		}

		return tcf;
	}

	boolean withSelectedTraitScaleMethod(){
		Object itemIDTrait=traitTable.getValue();
		Object itemIDScale=scaleTable.getValue();
		Object itemIDMethod=traitMethodTable.getValue();
		if(traitTable.isSelected(itemIDTrait) && scaleTable.isSelected(itemIDScale)
				&& traitMethodTable.isSelected(itemIDMethod)){
			return true;
		}
		return false;
	}
}
