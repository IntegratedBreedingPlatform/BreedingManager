package org.generationcp.breeding.manager.crossingmanager;

import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class SelectParentsListDataComponent extends AbsoluteLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(SelectParentsListDataComponent.class);
	private static final long serialVersionUID = 7907737258051595316L;
	private static final String CHECKBOX_COLUMN_ID="Checkbox Column ID";
	
	private Integer germplasmListId;
	
	private Label listEntriesLabel;
	private Table listDataTable;
	private CheckBox selectAllCheckBox;
	private Button viewListHeaderButton;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;

	@Autowired
    private GermplasmListManager germplasmListManager;
	
	public SelectParentsListDataComponent(Integer germplasmListId){
		super();
		this.germplasmListId = germplasmListId;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void updateLabels() {
		
	}

	@Override
	public void instantiateComponents() {
		listEntriesLabel = new Label("LIST ENTRIES");
		listEntriesLabel.setStyleName(Bootstrap.Typography.H4.styleName());
		
		listDataTable = new Table();
		listDataTable.setSelectable(true);
		listDataTable.setMultiSelect(true);
		listDataTable.setColumnCollapsingAllowed(true);
		listDataTable.setColumnReorderingAllowed(true);
		listDataTable.setPageLength(9);
		listDataTable.setImmediate(true);
		
		listDataTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.PARENTAGE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
		listDataTable.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
		
		listDataTable.setColumnHeader(CHECKBOX_COLUMN_ID, messageSource.getMessage(Message.TAG));
		listDataTable.setColumnHeader(ListDataTablePropertyID.ENTRY_ID.getName(), "#");
		listDataTable.setColumnHeader(ListDataTablePropertyID.DESIGNATION.getName(), messageSource.getMessage(Message.LISTDATA_DESIGNATION_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.PARENTAGE.getName(), messageSource.getMessage(Message.LISTDATA_GROUPNAME_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.ENTRY_CODE.getName(), messageSource.getMessage(Message.LISTDATA_ENTRY_CODE_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.GID.getName(), messageSource.getMessage(Message.LISTDATA_GID_HEADER));
		listDataTable.setColumnHeader(ListDataTablePropertyID.SEED_SOURCE.getName(), messageSource.getMessage(Message.LISTDATA_SEEDSOURCE_HEADER));
		
		listDataTable.setVisibleColumns(new String[] { 
        		CHECKBOX_COLUMN_ID
        		,ListDataTablePropertyID.ENTRY_ID.getName()
        		,ListDataTablePropertyID.DESIGNATION.getName()
        		,ListDataTablePropertyID.PARENTAGE.getName()
        		,ListDataTablePropertyID.ENTRY_CODE.getName()
        		,ListDataTablePropertyID.GID.getName()
        		,ListDataTablePropertyID.SEED_SOURCE.getName()});
		
		selectAllCheckBox = new CheckBox(messageSource.getMessage(Message.SELECT_ALL));
		selectAllCheckBox.setImmediate(true);
		
		viewListHeaderButton = new Button("View List Header");
		viewListHeaderButton.setStyleName(BaseTheme.BUTTON_LINK);
	}

	@Override
	public void initializeValues() {
		try{
			List<GermplasmListData> listEntries = germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, Integer.MAX_VALUE);
			for(GermplasmListData entry : listEntries){
				String gid = String.format("%s", entry.getGid().toString());
                Button gidButton = new Button(gid, new GidLinkButtonClickListener(gid,true));
                gidButton.setStyleName(BaseTheme.BUTTON_LINK);
                gidButton.setDescription("Click to view Germplasm information");
                
                Button desigButton = new Button(entry.getDesignation(), new GidLinkButtonClickListener(gid,true));
                desigButton.setStyleName(BaseTheme.BUTTON_LINK);
                desigButton.setDescription("Click to view Germplasm information");
                
                CheckBox itemCheckBox = new CheckBox();
                itemCheckBox.setData(entry.getId());
                itemCheckBox.setImmediate(true);
    	   		itemCheckBox.addListener(new ClickListener() {
    	 			private static final long serialVersionUID = 1L;
    	 			@Override
    	 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
    	 				CheckBox itemCheckBox = (CheckBox) event.getButton();
    	 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
    	 					listDataTable.select(itemCheckBox.getData());
    	 				} else {
    	 					listDataTable.unselect(itemCheckBox.getData());
    	 				}
    	 			}
    	 		});
    	   		
    	   		listDataTable.addItem(new Object[] {
                        itemCheckBox, entry.getEntryId(), desigButton, entry.getGroupName(), entry.getEntryCode(), gidButton, entry.getSeedSource()}
    	   			, entry.getId());
			}
		} catch(MiddlewareQueryException ex){
			LOG.error("Error with getting list entries for list: " + germplasmListId);
			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE), "Error in getting list entries."
					, Notification.POSITION_CENTERED);
		}
	}

	@Override
	public void addListeners() {
		listDataTable.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 3013620721902728079L;

			@SuppressWarnings("unchecked")
			@Override
			public void valueChange(ValueChangeEvent event) {
				Collection<Integer> entries = (Collection<Integer>) listDataTable.getItemIds();
				Collection<Integer> selectedEntries = (Collection<Integer>) listDataTable.getValue();
				if(selectedEntries.size() == entries.size()){
					selectAllCheckBox.setValue(true);
				} else{
					selectAllCheckBox.setValue(false);
				}
				
				for(Integer entry : entries){
					CheckBox tag = (CheckBox) listDataTable.getItem(entry).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
					if(selectedEntries.contains(entry)){
						tag.setValue(true);
					} else{
						tag.setValue(false);
					}
				}
			}
		});
		
		selectAllCheckBox.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 5802358625446499994L;

			@Override
			public void buttonClick(ClickEvent event) {
				boolean checkBoxValue = event.getButton().booleanValue();
				if(checkBoxValue){
					for(Object itemId : listDataTable.getItemIds()){
						CheckBox tag = (CheckBox) listDataTable.getItem(itemId).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
						tag.setValue(true);
					}
					listDataTable.setValue(listDataTable.getItemIds());
				} else{
					for(Object itemId : listDataTable.getItemIds()){
						CheckBox tag = (CheckBox) listDataTable.getItem(itemId).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
						tag.setValue(false);
					}
					listDataTable.setValue(null);
				}
			}
		});
	}

	@Override
	public void layoutComponents() {
		setWidth("700px");
		setHeight("330px");
		addComponent(listEntriesLabel, "top:10px; left:10px;");
		addComponent(viewListHeaderButton, "top:10px; right:80px;");
		addComponent(listDataTable, "top:40px; left:10px;");
		addComponent(selectAllCheckBox, "top:305px; left:10px;");
	}
	
	public Table getListDataTable(){
		return this.listDataTable;
	}
}
