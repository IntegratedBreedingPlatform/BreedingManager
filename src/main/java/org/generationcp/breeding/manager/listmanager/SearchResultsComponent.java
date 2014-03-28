package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.HeaderLabelLayout;
import org.generationcp.breeding.manager.listimport.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.listeners.SearchResultsItemClickListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerDetailsLayout;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class SearchResultsComponent extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 5314653969843976836L;

	private Label matchingListsLabel;
	private Label matchingListsDescription;
	private Table matchingListsTable;
	private TableWithSelectAllLayout matchingListsTableWithSelectAll;
	
	private Label matchingGermplasmsLabel;
	private Label matchingGermplasmsDescription;
	private Table matchingGermplasmsTable;
	private TableWithSelectAllLayout matchingGermplasmsTableWithSelectAll;
	
	private static final String CHECKBOX_COLUMN_ID = "Tag All Column";
	
	public static final String MATCHING_GEMRPLASMS_TABLE_DATA = "Matching Germplasms Table";
	public static final String MATCHING_LISTS_TABLE_DATA = "Matching Lists Table";
	
    static final Action ACTION_COPY_TO_NEW_LIST= new Action("Copy to new list");
    static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_COPY_TO_NEW_LIST };
	
	private org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerDetailsLayout displayDetailsLayout;
	private org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain listManagerMain;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	public SearchResultsComponent(ListManagerMain listManagerMain, org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerDetailsLayout displayDetailsLayout){
		this.listManagerMain = listManagerMain;
		this.displayDetailsLayout = displayDetailsLayout;
	}
	
	@Override
	public void updateLabels() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}
	
	@Override
	public void instantiateComponents() {
		matchingListsLabel = new Label();
		matchingListsLabel.setWidth("250px");
		matchingListsLabel.setValue(messageSource.getMessage(Message.MATCHING_LISTS)+": 0");
		matchingListsLabel.addStyleName(Bootstrap.Typography.H3.styleName());
		
		matchingListsDescription = new Label();
		matchingListsDescription.setValue(messageSource.getMessage(Message.SELECT_A_LIST_TO_VIEW_THE_DETAILS));
		
		matchingListsTableWithSelectAll = new TableWithSelectAllLayout(5, CHECKBOX_COLUMN_ID);
		matchingListsTable = matchingListsTableWithSelectAll.getTable();
		matchingListsTable.setData(MATCHING_LISTS_TABLE_DATA);
		matchingListsTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		matchingListsTable.addContainerProperty("NAME", String.class, null);
		matchingListsTable.addContainerProperty("DESCRIPTION", String.class, null);
		matchingListsTable.setWidth("350px");
		matchingListsTable.setMultiSelect(true);
		matchingListsTable.setSelectable(true);
		matchingListsTable.setImmediate(true);
		matchingListsTable.setDragMode(TableDragMode.ROW);
		matchingListsTable.addListener(new SearchResultsItemClickListener(MATCHING_LISTS_TABLE_DATA, displayDetailsLayout));
		messageSource.setColumnHeader(matchingListsTable, CHECKBOX_COLUMN_ID, Message.CHECK_ICON);
		
		matchingGermplasmsLabel = new Label();
		matchingGermplasmsLabel.setValue(messageSource.getMessage(Message.MATCHING_GERMPLASM)+": 0");
		matchingGermplasmsLabel.addStyleName(Bootstrap.Typography.H3.styleName());
		
		matchingGermplasmsDescription = new Label();
		matchingGermplasmsDescription.setValue(messageSource.getMessage(Message.SELECT_A_GERMPLASM_TO_VIEW_THE_DETAILS));
		
		matchingGermplasmsTableWithSelectAll = new TableWithSelectAllLayout(10, CHECKBOX_COLUMN_ID);
		matchingGermplasmsTable = matchingGermplasmsTableWithSelectAll.getTable();
		matchingGermplasmsTable.setData(MATCHING_GEMRPLASMS_TABLE_DATA);
		matchingGermplasmsTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		matchingGermplasmsTable.addContainerProperty("GID", Button.class, null);
		matchingGermplasmsTable.addContainerProperty("NAMES", String.class,null);
		matchingGermplasmsTable.addContainerProperty("PARENTAGE", String.class,null);
		matchingGermplasmsTable.setWidth("350px");
		matchingGermplasmsTable.setMultiSelect(true);
		matchingGermplasmsTable.setSelectable(true);
		matchingGermplasmsTable.setImmediate(true);
		matchingGermplasmsTable.setDragMode(TableDragMode.ROW);
		matchingGermplasmsTable.addListener(new SearchResultsItemClickListener(MATCHING_GEMRPLASMS_TABLE_DATA, displayDetailsLayout));
		messageSource.setColumnHeader(matchingGermplasmsTable, CHECKBOX_COLUMN_ID, Message.CHECK_ICON);
		
		matchingGermplasmsTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
			private static final long serialVersionUID = 1L;

			public String generateDescription(Component source, Object itemId,
					Object propertyId) {
				if(propertyId=="NAMES"){
					Item item = matchingGermplasmsTable.getItem(itemId);
					Integer gid = Integer.valueOf(((Button) item.getItemProperty("GID").getValue()).getCaption());
					return getGermplasmNames(gid);
				} else {
					return null;
				}
			}
        });

	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {
		matchingGermplasmsTable.addActionHandler(new Action.Handler() {
	       	 private static final long serialVersionUID = -897257270314381555L;

				public Action[] getActions(Object target, Object sender) {
					return GERMPLASMS_TABLE_CONTEXT_MENU;
	            }

				@SuppressWarnings("unchecked")
				@Override
				public void handleAction(Action action, Object sender, Object target) {
	             	if (ACTION_COPY_TO_NEW_LIST == action) {
	             		listManagerMain.showBuildNewListComponent();
	             		List<Integer> gids = new ArrayList<Integer>();
	             		gids.addAll((Collection<? extends Integer>) matchingGermplasmsTable.getValue());
	             		for(Integer gid : gids){
	             			listManagerMain.addGermplasmToBuildNewListTable(gid);
	             		}
	             	}
				}
			});
	}

	@Override
	public void layoutComponents() {
		HeaderLabelLayout matchingListsHeader = new HeaderLabelLayout(AppConstants.Icons.ICON_BUILD_NEW_LIST,matchingListsLabel);
		addComponent(matchingListsHeader, "top:0px; left:0px;");
		addComponent(matchingListsDescription, "top:23px; left:0px;");
		addComponent(matchingListsTableWithSelectAll, "top:40px; left:0px;");
		
		HeaderLabelLayout matchingGermplasmsHeader = new HeaderLabelLayout(AppConstants.Icons.ICON_MATCHING_GERMPLASMS,matchingGermplasmsLabel);
		addComponent(matchingGermplasmsHeader, "top:257px; left:0px;");
		addComponent(matchingGermplasmsDescription, "top:280px; left:0px;");
		addComponent(matchingGermplasmsTableWithSelectAll, "top:297px; left:0px;");
	}
		
	public void applyGermplasmListResults(List<GermplasmList> germplasmLists){
		matchingListsLabel.setValue(messageSource.getMessage(Message.MATCHING_LISTS)+": "+String.valueOf(germplasmLists.size()));
		matchingListsTable.removeAllItems();
		for(GermplasmList germplasmList:germplasmLists){
			
            CheckBox itemCheckBox = new CheckBox();
            itemCheckBox.setData(germplasmList.getId());
            itemCheckBox.setImmediate(true);
	   		itemCheckBox.addListener(new ClickListener() {
	 			private static final long serialVersionUID = 1L;
	 			@Override
	 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
	 				CheckBox itemCheckBox = (CheckBox) event.getButton();
	 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
	 					matchingListsTable.select(itemCheckBox.getData());
	 				} else {
	 					matchingListsTable.unselect(itemCheckBox.getData());
	 				}
	 			}
	 			 
	 		});
	   		
			matchingListsTable.addItem(new Object[]{itemCheckBox, germplasmList.getName(),germplasmList.getDescription()},germplasmList.getId());
		}
	}
	
	public void applyGermplasmResults(List<Germplasm> germplasms){
		matchingGermplasmsLabel.setValue(messageSource.getMessage(Message.MATCHING_GERMPLASM)+": "+String.valueOf(germplasms.size()));
		matchingGermplasmsTable.removeAllItems();
		for(Germplasm germplasm:germplasms){

        	Button gidButton = new Button(String.format("%s", germplasm.getGid().toString()), new GidLinkButtonClickListener(germplasm.getGid().toString(), true));
            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
			
			String shortenedNames = getShortenedGermplasmNames(germplasm.getGid());
			
            String crossExpansion = "";
            if(germplasm!=null){
            	try {
            		if(germplasmDataManager!=null)
            			crossExpansion = germplasmDataManager.getCrossExpansion(germplasm.getGid(), 1);
            	} catch(MiddlewareQueryException ex){
                    crossExpansion = "-";
                }
        	}

            CheckBox itemCheckBox = new CheckBox();
            itemCheckBox.setData(germplasm.getGid());
            itemCheckBox.setImmediate(true);
	   		itemCheckBox.addListener(new ClickListener() {
	 			private static final long serialVersionUID = 1L;
	 			@Override
	 			public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
	 				CheckBox itemCheckBox = (CheckBox) event.getButton();
	 				if(((Boolean) itemCheckBox.getValue()).equals(true)){
	 					matchingGermplasmsTable.select(itemCheckBox.getData());
	 				} else {
	 					matchingGermplasmsTable.unselect(itemCheckBox.getData());
	 				}
	 			}
	 			 
	 		});
            
            matchingGermplasmsTable.addItem(new Object[]{itemCheckBox, gidButton, shortenedNames, crossExpansion},germplasm.getGid());
		}
		
	}

    private String getGermplasmNames(int gid) throws InternationalizableException {

        try {
            List<Name> names = germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
            StringBuffer germplasmNames = new StringBuffer("");
            int i = 0;
            for (Name n : names) {
                if (i < names.size() - 1) {
                    germplasmNames.append(n.getNval() + ", ");
                } else {
                    germplasmNames.append(n.getNval());
                }
                i++;
            }

            return germplasmNames.toString();
        } catch (MiddlewareQueryException e) {
            return null;
        }
    }	
	
    private String getShortenedGermplasmNames(int gid) throws InternationalizableException {
        try {
            List<Name> names = germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
            StringBuffer germplasmNames = new StringBuffer("");
            int i = 0;
            for (Name n : names) {
                if (i < names.size() - 1) {
                    germplasmNames.append(n.getNval() + ", ");
                } else {
                    germplasmNames.append(n.getNval());
                }
                i++;
            }
            String n = germplasmNames.toString();
            if(n.length()>20){
            	n = n.substring(0, 20) + "...";
            }
            return n;
        } catch (MiddlewareQueryException e) {
            return null;
        }
    }
    
    public enum ResultType {
    	GERMPLASM, LIST
    }
	
    public Table getMatchingGermplasmsTable(){
    	return matchingGermplasmsTable;
    }
    
    public Table getMatchingListsTable(){
    	return matchingListsTable;
    }    
    
    public ListManagerDetailsLayout getListManagerDetailsLayout(){
    	return this.displayDetailsLayout;
    }
	
    /**
    private void syncItemCheckBoxes(Table table){
    	List<Integer> itemIds = getItemIds(table);
    	for(Integer itemId : itemIds){
    		CheckBox itemCheckBox = (CheckBox) table.getItem(itemId).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
    		itemCheckBox.setValue(false);
    	}
    	List<Integer> selectedItemIds = getSelectedItemIds(table);
    	for(Integer itemId : selectedItemIds){
    		CheckBox itemCheckBox = (CheckBox) table.getItem(itemId).getItemProperty(CHECKBOX_COLUMN_ID).getValue();
    		itemCheckBox.setValue(true);
    	}
    	
    	if(table.equals(matchingGermplasmsTable)){
    		if(matchingGermplasmsTagAllWasJustClicked || table.getValue().equals(table.getItemIds())){
    			matchingGermplasmsTagAllCheckBox.setValue(true);
    		} else {
    			matchingGermplasmsTagAllCheckBox.setValue(false);
    		}
        	matchingGermplasmsTagAllWasJustClicked = false;
    	} else {
    		if(matchingListsTagAllWasJustClicked || table.getValue().equals(table.getItemIds())){
    			matchingListsTagAllCheckBox.setValue(true);
    		} else {
    			matchingListsTagAllCheckBox.setValue(false);
    		}
    		matchingListsTagAllWasJustClicked = false;
    	}
    }**/
    
	/**
	 * Iterates through the whole table, gets selected item ID's, make sure it's sorted as seen on the UI
	 */
    /**
	@SuppressWarnings("unchecked")
	private List<Integer> getSelectedItemIds(Table table){
		List<Integer> itemIds = new ArrayList<Integer>();
		List<Integer> selectedItemIds = new ArrayList<Integer>();
		List<Integer> trueOrderedSelectedItemIds = new ArrayList<Integer>();
		
    	selectedItemIds.addAll((Collection<? extends Integer>) table.getValue());
    	itemIds = getItemIds(table);
        	
    	for(Integer itemId: itemIds){
    		if(selectedItemIds.contains(itemId)){
    			trueOrderedSelectedItemIds.add(itemId);
    		}
    	}
    	
    	return trueOrderedSelectedItemIds;
    }**/
    
	/**
	 * Get item id's of a table, and return it as a list 
	 * @param table
	 * @return
	 */
    /**
	@SuppressWarnings("unchecked")
	private List<Integer> getItemIds(Table table){
		List<Integer> itemIds = new ArrayList<Integer>();
    	itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
    	return itemIds;
	}**/

	/**
    private int getTotalWidth(Table table){
    	int totalWidth = 0;
    	List<Object> visibleColumnIds = new ArrayList<Object>();
    	visibleColumnIds = Arrays.asList(table.getVisibleColumns());
    	for(Object visibleColumnId : visibleColumnIds){
    		totalWidth += table.getColumnWidth(visibleColumnId) + 20; //padding on cells, 8px on each side
    	}
    	return totalWidth;
    }
    **/
        
}
