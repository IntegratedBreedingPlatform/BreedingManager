package org.generationcp.breeding.manager.listmanager;

import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Table.TableTransferable;

public class DropHandlerComponent extends Panel {

	private static final long serialVersionUID = 1L;
	
	private ListManagerMain source;
	private AbsoluteLayout dropLayout;
	private Table dropTable;
	private Label noOfEntriesLbl;
	private int width;
	private int noOfEntries;
	private final String NO_OF_ENTRIES = "Total # of Entries:";
	
	@Autowired
	private SimpleResourceBundleMessageSource messageSource;
	
	public DropHandlerComponent(ListManagerMain source, int width) {
		super();
		
		this.source = source;
		this.width = width;
		noOfEntries = 0;
		this.setVisible(false); //hide the drop area by default, will be displayed when start new list button is clicked
		
		setWidth(width + "px");
		setHeight("105px");
		addStyleName("dropHandlerPanel");
		
		initializeComponents();
		
		setupDragSources();
		setupDropHandlers();
	}
	
	public void initializeComponents(){
		dropLayout = new AbsoluteLayout();
		dropLayout.setSizeFull();
		
		dropTable = new Table();
		dropTable.setWidth(getTableWidth(width) + "px");
		dropTable.setHeight("55px");
		dropTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		dropTable.setPageLength(0);
		dropTable.addContainerProperty("List Entry", String.class,  null);
		dropTable.setColumnWidth("List Entry", width);
		dropTable.addItem(new Object[]{ "Drag entries here to add to your new list" }, new Integer(1));
		dropTable.addStyleName("drop-handler");
		
		dropLayout.addComponent(dropTable,"top:15px;left:15px");
		
		dropLayout.addComponent(new Label("<style>.v-panel-content .v-absolutelayout div.drop-handler  { border-radius: 0px; text-align: center; } " +
				".v-panel-content .v-absolutelayout .drop-handler div.v-table-cell-wrapper { background-color: #fff; height: 45px; width: " + getColumnWidth(width) + "; display: table-cell; vertical-align: middle; white-space: normal;} " +
				".v-panel-content .v-absolutelayout .drop-handler div.v-table-body-wrapper { background-color: #fff; } " +
				".v-panel-content .v-absolutelayout  div.drop-handler tr.v-table-row { background-color: #fff; } " +
				".v-panel-content .v-absolutelayout  div.drop-handler tr.v-table-row-odd { background-color: #fff; } " +
				".v-panel-content .v-absolutelayout  div.drop-handler  table .v-table-cell-content { word-wrap: break-word; height: 45px; } " +
				".v-panel-content .v-absolutelayout  div.drop-handler div.v-table-body-noselection { height: 48px; } " +
				" </style>",Label.CONTENT_XHTML));
		
		noOfEntriesLbl = new Label(NO_OF_ENTRIES + " " + noOfEntries);
		dropLayout.addComponent(noOfEntriesLbl,"top:75px;left:15px");
		
		setContent(dropLayout);
		
	}
	
	public void enableDropHandler(){
		this.setVisible(true);
	}
	
	private void setupDragSources() {
		// TODO Auto-generated method stub
		
	}

	private void setupDropHandlers() {
		dropTable.setDropHandler(new DropHandler(){

			private static final long serialVersionUID = 1L;

			@Override
			public void drop(DragAndDropEvent dropEvent) {
				//Send the Transferable object to buildNewListComponent.germplasmTable
				
				TableTransferable transferable = (TableTransferable) dropEvent.getTransferable();
				Table sourceTable = (Table) transferable.getSourceComponent();
				AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
				
				source.getBuildListComponent().setFromDropHandler(true);
				source.getBuildListComponent().handleDrop(sourceTable, transferable, dropData);
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}
			
		});
	}

	public int getTableWidth(int width){
		return width - 32;
	}
	
	public int getColumnWidth(int width){
		return width - 40;
	}

	public void updateNoOfEntries() {
		this.noOfEntries = source.getBuildListComponent().getGermplasmsTable().size();
		this.noOfEntriesLbl.setValue(NO_OF_ENTRIES + " " + noOfEntries);
	}
	
}
