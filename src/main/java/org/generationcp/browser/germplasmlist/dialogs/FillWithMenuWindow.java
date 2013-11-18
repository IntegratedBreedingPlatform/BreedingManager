package org.generationcp.browser.germplasmlist.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.browser.germplasmlist.GermplasmListDataComponent;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class FillWithMenuWindow extends Window {
	
	private static final long serialVersionUID = 8256299016887449218L;
	
	private static final Logger LOG = LoggerFactory.getLogger(FillWithMenuWindow.class);
	
	private FillColumn targetColumn;
	private int positionX;
	private int positionY;
	private GermplasmDataManager dataManager;
	private Table listDataTable;
	
	private ListSelect fillWithOptionsSelect;
	private Button cancelButton;
	
	public enum FillColumn {
		ENTRY_CODE
		,SOURCE;
	}

	public enum FillEntryCodeOptions {
		PREFERRED_NAME("Preferred Name")
		,PREFERRED_ID("Preferred ID");
		
		private String name;
		
		private FillEntryCodeOptions(String name){
			this.name = name;
		}
		
		public String getName(){
			return this.name;
		}
	}
	
	public enum FillSourceOptions {
		LOCATION_NAME("Location Name");
		
		private String name;
		
		private FillSourceOptions(String name){
			this.name = name;
		}
		
		public String getName(){
			return this.name;
		}
	}
	
	public FillWithMenuWindow(FillColumn targetColumn, int positionX, int positionY, GermplasmDataManager dataManager, 
			Table listDataTable){
		super("Fill with...");
		this.targetColumn = targetColumn;
		this.positionX = positionX;
		this.positionY = positionY;
		this.dataManager = dataManager;
		this.listDataTable = listDataTable;
		initializeComponents();
		initializeLayout();
	}
	
	private void initializeComponents(){
		fillWithOptionsSelect = new ListSelect();
		fillWithOptionsSelect.setRows(6);
		fillWithOptionsSelect.setImmediate(true);
		
		if(this.targetColumn == FillColumn.ENTRY_CODE){
			fillWithOptionsSelect.addItem(FillEntryCodeOptions.PREFERRED_ID.getName());
			fillWithOptionsSelect.addItem(FillEntryCodeOptions.PREFERRED_NAME.getName());
		} else if(this.targetColumn == FillColumn.SOURCE){
			fillWithOptionsSelect.addItem(FillSourceOptions.LOCATION_NAME.getName());
		}
		
		fillWithOptionsSelect.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -3951054867322060680L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				fillColumnValue(event.getProperty().toString());
				closeWindow();
			}
		});
		
		cancelButton = new Button("Cancel");
		cancelButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 5562031553753182319L;

			@Override
			public void buttonClick(ClickEvent event) {
				closeWindow();
			}
		});
	}
	
	private void initializeLayout(){
		this.setHeight("200px");
		this.setWidth("120px");
		this.setModal(true);
		this.setResizable(false);
		this.setPositionX(this.positionX);
		this.setPositionY(this.positionY);
		
		this.addComponent(fillWithOptionsSelect);
		this.addComponent(cancelButton);
	}
	
	private void closeWindow(){
		Window parentWindow = this.getParent();
		parentWindow.removeWindow(this);
	}
	
	private void fillColumnValue(String fillWithOptionName){
		List<Integer> gids = new ArrayList<Integer>();
		for(Object itemId : this.listDataTable.getItemIds()){
			Item listEntry = this.listDataTable.getItem(itemId);
			Integer gid = (Integer) listEntry.getItemProperty(GermplasmListDataComponent.GID_VALUE).getValue();
			gids.add(gid);
		}
		
		if(fillWithOptionName.equals(FillEntryCodeOptions.PREFERRED_NAME.getName())){
			try{
				Map<Integer, String> names = this.dataManager.getPreferredNamesByGids(gids);
				
				if(names != null){
					for(Object itemId : this.listDataTable.getItemIds()){
						Item listEntry = this.listDataTable.getItem(itemId);
						Integer gid = (Integer) listEntry.getItemProperty(GermplasmListDataComponent.GID_VALUE).getValue();
						String name = names.get(gid);
						listEntry.getItemProperty(GermplasmListDataComponent.ENTRY_CODE).setValue(name);
					}
				}
			} catch(MiddlewareQueryException ex){
				LOG.error("Error with getting preferred names by gids, given gids: " + gids, ex);
				MessageNotifier.showError(this, "Database Error", "Error with getting preferred names by gids. Please report to IBP.", Notification.POSITION_CENTERED);
			}
		}
	}
}
