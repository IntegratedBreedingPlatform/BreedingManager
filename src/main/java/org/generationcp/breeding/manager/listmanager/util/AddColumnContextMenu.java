package org.generationcp.breeding.manager.listmanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickListener;

@Configurable
public class AddColumnContextMenu implements InternationalizableComponent  {
	private static final Logger LOG = LoggerFactory.getLogger(AddColumnContextMenu.class);

    @Autowired
    private GermplasmDataManager germplasmDataManager;

    private AbsoluteLayout absoluteLayoutSource;
    private String GIDPropertyId;
    private Button addColumnButton;
    private Table targetTable;
    
    private ContextMenu menu;
    private ContextMenuItem menuFillWithPreferredId;
    private ContextMenuItem menuFillWithPreferredName;
    private ContextMenuItem menuFillWithLocations;
    
    public static String FILL_WITH_PREFERRED_ID = "Fill with Preferred ID";
    public static String FILL_WITH_PREFERRED_NAME = "Fill with Preferred Name";
    public static String FILL_WITH_LOCATION = "Fill with Location";
    
    @SuppressWarnings("rawtypes")
	public static Class PREFERRED_ID_TYPE = String.class;
    public static String PREFERRED_ID = "PREFERRED ID";
    
    @SuppressWarnings("rawtypes")
    public static Class PREFERRED_NAME_TYPE = String.class;
    public static String PREFERRED_NAME = "PREFERRED NAME";
    
    @SuppressWarnings("rawtypes")
    public static Class LOCATIONS_TYPE = String.class;
    public static String LOCATIONS = "LOCATIONS";
    
    
	/**
	 * Add "Add column" context menu to a table
	 * @param source - context menu will attach to this
	 * @param addColumnButton - util will attach event listener to this
	 * @param targetTable - table where data will be manipulated
	 * @param gid - property of GID (button with GID as caption) on that table
	 */
    public AddColumnContextMenu(AbsoluteLayout absoluteLayoutSource, Button addColumnButton,Table targetTable, String gid){
    	this.GIDPropertyId = gid;
    	this.targetTable = targetTable;
    	this.addColumnButton = addColumnButton;
    	this.absoluteLayoutSource = absoluteLayoutSource;
    	
    	setupContextMenu();
    }
    
    private void setupContextMenu(){
    	
    	menu = new ContextMenu();
		menuFillWithPreferredId = menu.addItem(FILL_WITH_PREFERRED_ID);
		menuFillWithPreferredName = menu.addItem(FILL_WITH_PREFERRED_NAME);
		menuFillWithLocations = menu.addItem(FILL_WITH_LOCATION);
    	
    	menu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = 1L;

		    //Handle clicks on menu items
			@Override
			public void contextItemClick(ClickEvent event) {
			    ContextMenuItem clickedItem = event.getClickedItem();
			    if(clickedItem.getName().equals(FILL_WITH_PREFERRED_ID)){
			      	addPreferredIdColumn();
			    }else if(clickedItem.getName().equals(FILL_WITH_PREFERRED_NAME)){
			    	addPreferredNameColumn();
			    }else if(clickedItem.getName().equals(FILL_WITH_LOCATION)){
			    	addLocationColumn();
			    }				
			}
			
        });
    	
    	//Attach menu to whatever source passed to the constructor of this class/util
    	if(absoluteLayoutSource!=null)
    		absoluteLayoutSource.addComponent(menu);
    	
    	//Attach listener to the "Add Column" button passed to the constructor of this class/util
    	addColumnButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
				
				//Check if columns already exist in the table
				if(propertyExists(PREFERRED_ID)){
					menuFillWithPreferredId.setEnabled(false);
				} else {
					menuFillWithPreferredId.setEnabled(true);
				}
				
				if(propertyExists(PREFERRED_NAME)){
					menuFillWithPreferredName.setEnabled(false);
				} else {
					menuFillWithPreferredName.setEnabled(true);
				}
				
				if(propertyExists(LOCATIONS)){
					menuFillWithLocations.setEnabled(false);
				} else {
					menuFillWithLocations.setEnabled(true);
				}
				
				//Display context menu
				menu.show(event.getClientX(), event.getClientY());
			}
		 });
	 
    }
    
    
    private void addPreferredIdColumn(){
    	if(!propertyExists(PREFERRED_ID)){
    		targetTable.addContainerProperty(PREFERRED_ID, PREFERRED_ID_TYPE, null);
    		setPreferredIdColumnValues();
    	}
    }
    
    public void setPreferredIdColumnValues(){
    	if(propertyExists(PREFERRED_ID)){
    		try {
    			List<Integer> itemIds = getItemIds(targetTable);
    			for(Integer itemId: itemIds){
    				Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
    				String preferredID = "";
    				Name name = germplasmDataManager.getPreferredIdByGID(gid);
    				if(name!=null && name.getNval()!=null)
    					preferredID = name.getNval();
    				targetTable.getItem(itemId).getItemProperty(PREFERRED_ID).setValue(preferredID);
    			}
			   
    		} catch (MiddlewareQueryException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    private void addPreferredNameColumn(){
    	if(!propertyExists(PREFERRED_NAME)){
    		targetTable.addContainerProperty(PREFERRED_NAME, PREFERRED_NAME_TYPE, null);
    		setPreferredNameColumnValues();
    	}
    }
    
    public void setPreferredNameColumnValues(){
    	if(propertyExists(PREFERRED_NAME)){
			try {
				List<Integer> itemIds = getItemIds(targetTable);
				for(Integer itemId: itemIds){
					Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
					String preferredName = germplasmDataManager.getPreferredNameByGID(gid).getNval();
					targetTable.getItem(itemId).getItemProperty(PREFERRED_NAME).setValue(preferredName);
				}
			   
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}  
    	}
    }
    
    private void addLocationColumn(){
    	if(!propertyExists(LOCATIONS)){
    		targetTable.addContainerProperty(LOCATIONS, LOCATIONS_TYPE, null);
    		setLocationColumnValues();
    	}
    }
    
    public void setLocationColumnValues(){
    	if(propertyExists(LOCATIONS)){
			try {
				List<Integer> itemIds = getItemIds(targetTable);
				for(Integer itemId: itemIds){
					Integer gid = Integer.valueOf(((Button) targetTable.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString());
					
					List<Integer> gids = new ArrayList<Integer>();
					gids.add(gid);
					
					Map<Integer, String> locationNamesMap = germplasmDataManager.getLocationNamesByGids(gids);
					targetTable.getItem(itemId).getItemProperty(LOCATIONS).setValue(locationNamesMap.get(gid));
				}
			   
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
			}    	
    	}
    }
    
    public Boolean propertyExists(String propertyId){
    	List<String> propertyIds = getTablePropertyIds(targetTable);
    	return propertyIds.contains(propertyId);
    }
    
    @SuppressWarnings("unchecked")
	public List<String> getTablePropertyIds(Table table){
    	if(table!=null){
    		List<String> propertyIds = new ArrayList<String>();
    		propertyIds.addAll((Collection<? extends String>) table.getContainerPropertyIds());
    		return propertyIds;
    	} else {
    		return new ArrayList<String>();
    	}
    }    
    
    public List<Integer> getGidsFromTable(Table table){
    	List<Integer> gids = new ArrayList<Integer>();
    	List<Integer> listDataItemIds = getItemIds(table);
    	for(Integer itemId: listDataItemIds){
    		//gids.add((Integer) table.getItem(itemId).getItemProperty(GID_VALUE).getValue());
    		gids.add(Integer.valueOf(((Button) table.getItem(itemId).getItemProperty(GIDPropertyId).getValue()).getCaption().toString()));
    	}
    	return gids;
    }
   
	@SuppressWarnings("unchecked")
	public List<Integer> getItemIds(Table table){
		List<Integer> itemIds = new ArrayList<Integer>();
    	itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
    	return itemIds;
	}
	
    
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}
 

}
