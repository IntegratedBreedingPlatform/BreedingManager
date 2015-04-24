package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.ActionButton;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.breeding.manager.util.Util;
import org.generationcp.commons.constant.DefaultGermplasmStudyBrowserPath;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.tomcat.util.TomcatUtil;
import org.generationcp.commons.tomcat.util.WebAppStatusInfo;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Configurable
public class GermplasmSearchResultsComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmSearchResultsComponent.class);
	private static final long serialVersionUID = 5314653969843976836L;

	private Label totalMatchingGermplasmsLabel;
	private Label totalSelectedMatchingGermplasmsLabel;
	private Table matchingGermplasmsTable;
	
	private Button actionButton;
	private ContextMenu menu;
	private ContextMenuItem menuSelectAll;
	private ContextMenuItem menuAddNewEntry;
	
	private TableWithSelectAllLayout matchingGermplasmsTableWithSelectAll;
	
	public static final String CHECKBOX_COLUMN_ID = "Tag All Column";
	public static final String NAMES = "NAMES";
	
	public static final String MATCHING_GEMRPLASMS_TABLE_DATA = "Matching Germplasms Table";

    static final Action ACTION_COPY_TO_NEW_LIST= new Action("Add Selected Entries to New List");
    static final Action ACTION_SELECT_ALL= new Action("Select All");
    static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_COPY_TO_NEW_LIST, ACTION_SELECT_ALL };
	
    private Action.Handler rightClickActionHandler;
    
	private final org.generationcp.breeding.manager.listmanager.ListManagerMain listManagerMain;
	
	private boolean viaToolUrl = true;
	
	private boolean showAddToList = true;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private OntologyDataManager ontologyDataManager;
    
    
    public GermplasmSearchResultsComponent(){
    	listManagerMain = null;
    }

	public GermplasmSearchResultsComponent(final ListManagerMain listManagerMain) {
		this.listManagerMain = listManagerMain;
	}
	
    public GermplasmSearchResultsComponent(final ListManagerMain listManagerMain, boolean viaToolUrl, boolean showAddToList) {
        this(listManagerMain);
        
        this.viaToolUrl = viaToolUrl;
        this.showAddToList = showAddToList;
    }
	
	@Override
	public void updateLabels() {
		//do nothing
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
		
		setWidth("100%");
		
		totalMatchingGermplasmsLabel = new Label("", Label.CONTENT_XHTML);
		totalMatchingGermplasmsLabel.setWidth("120px");
		updateNoOfEntries(0);
		
		totalSelectedMatchingGermplasmsLabel = new Label("", Label.CONTENT_XHTML);
		totalSelectedMatchingGermplasmsLabel.setWidth("95px");
		updateNoOfSelectedEntries(0);
		
		actionButton = new ActionButton();
		
		menu = new ContextMenu();
		menu.setWidth("250px");
		menuAddNewEntry = menu.addItem(messageSource.getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST));
		menuSelectAll = menu.addItem(messageSource.getMessage(Message.SELECT_ALL));
		updateActionMenuOptions(false);		
		
		matchingGermplasmsTableWithSelectAll = getTableWithSelectAllLayout();
		matchingGermplasmsTableWithSelectAll.setHeight("500px");

		matchingGermplasmsTable = matchingGermplasmsTableWithSelectAll.getTable();
		matchingGermplasmsTable.setData(MATCHING_GEMRPLASMS_TABLE_DATA);
		matchingGermplasmsTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		matchingGermplasmsTable.addContainerProperty(NAMES, Button.class,null);
		matchingGermplasmsTable.addContainerProperty(ColumnLabels.PARENTAGE.getName(), String.class,null);
		matchingGermplasmsTable.addContainerProperty(ColumnLabels.STOCKID.getName(), Label.class, null);
		matchingGermplasmsTable.addContainerProperty(ColumnLabels.GID.getName(), Button.class, null);
		matchingGermplasmsTable.addContainerProperty(ColumnLabels.GERMPLASM_LOCATION.getName(), String.class,null);
		matchingGermplasmsTable.addContainerProperty(ColumnLabels.BREEDING_METHOD_NAME.getName(), String.class,null);
		matchingGermplasmsTable.setWidth("100%");
		matchingGermplasmsTable.setMultiSelect(true);
		matchingGermplasmsTable.setSelectable(true);
		matchingGermplasmsTable.setImmediate(true);
		matchingGermplasmsTable.setDragMode(TableDragMode.ROW);
		matchingGermplasmsTable.setHeight("470px");

		messageSource.setColumnHeader(matchingGermplasmsTable, CHECKBOX_COLUMN_ID, Message.CHECK_ICON);
		matchingGermplasmsTable.setColumnHeader(ColumnLabels.PARENTAGE.getName(), ColumnLabels.PARENTAGE.getTermNameFromOntology(ontologyDataManager));
		matchingGermplasmsTable.setColumnHeader(ColumnLabels.STOCKID.getName(), ColumnLabels.STOCKID.getTermNameFromOntology(ontologyDataManager));
		matchingGermplasmsTable.setColumnHeader(ColumnLabels.GID.getName(), ColumnLabels.GID.getTermNameFromOntology(ontologyDataManager));
		matchingGermplasmsTable.setColumnHeader(ColumnLabels.GERMPLASM_LOCATION.getName(), ColumnLabels.GERMPLASM_LOCATION.getTermNameFromOntology(ontologyDataManager));
		matchingGermplasmsTable.setColumnHeader(ColumnLabels.BREEDING_METHOD_NAME.getName(), ColumnLabels.BREEDING_METHOD_NAME.getTermNameFromOntology(ontologyDataManager));
		
		matchingGermplasmsTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
			private static final long serialVersionUID = 1L;

			@Override
			public String generateDescription(Component source, Object itemId,
					Object propertyId) {
				if(propertyId==NAMES){
					Item item = matchingGermplasmsTable.getItem(itemId);
					Integer gid = Integer.valueOf(((Button) item.getItemProperty(ColumnLabels.GID.getName()).getValue()).getCaption());
					return getGermplasmNames(gid);
				} else {
					return null;
				}
			}
        });

		rightClickActionHandler = new Action.Handler() {
	       	 private static final long serialVersionUID = -897257270314381555L;

				@Override
				public Action[] getActions(Object target, Object sender) {
					return GERMPLASMS_TABLE_CONTEXT_MENU;
	            }

				@Override
				public void handleAction(Action action, Object sender, Object target) {
	             	if (ACTION_COPY_TO_NEW_LIST == action) {
	             		addSelectedEntriesToNewList();
	             	} else if(ACTION_SELECT_ALL == action){
	             		matchingGermplasmsTable.setValue(matchingGermplasmsTable.getItemIds());
	             	}
				}
			};
		
	}

	protected TableWithSelectAllLayout getTableWithSelectAllLayout() {
		return new TableWithSelectAllLayout(10, CHECKBOX_COLUMN_ID);
	}

	private void updateActionMenuOptions(boolean status) {
		menuAddNewEntry.setEnabled(status);
		menuSelectAll.setEnabled(status);
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		
        actionButton.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				menu.show(event.getClientX(), event.getClientY());
			}
        	
        });
        
		menu.addListener(new ContextMenu.ClickListener() {
			private static final long serialVersionUID = -2343109406180457070L;
			
			@Override
			public void contextItemClick(
					org.vaadin.peter.contextmenu.ContextMenu.ClickEvent event) {
				 ContextMenuItem clickedItem = event.getClickedItem();
				 
				 if(clickedItem.getName().equals(messageSource.getMessage(Message.ADD_SELECTED_ENTRIES_TO_NEW_LIST))){
					 addSelectedEntriesToNewList();
				 } else if(clickedItem.getName().equals(messageSource.getMessage(Message.SELECT_ALL))){
					 matchingGermplasmsTable.setValue(matchingGermplasmsTable.getItemIds());
				 }
				
			}
		});
        
		matchingGermplasmsTable.addActionHandler(rightClickActionHandler);
        
		matchingGermplasmsTable.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				updateNoOfSelectedEntries();
			}
		});
	}

	public void setRightClickActionHandlerEnabled(Boolean isEnabled){
		matchingGermplasmsTable.removeActionHandler(rightClickActionHandler);
		if(isEnabled) {
            matchingGermplasmsTable.addActionHandler(rightClickActionHandler);
        }
	}
	
	@Override
	public void layoutComponents() {
		setSpacing(true);
		
		HorizontalLayout leftHeaderLayout = new HorizontalLayout();
		leftHeaderLayout.setSpacing(true);
		leftHeaderLayout.addComponent(totalMatchingGermplasmsLabel);
		leftHeaderLayout.addComponent(totalSelectedMatchingGermplasmsLabel);
		leftHeaderLayout.setComponentAlignment(totalMatchingGermplasmsLabel, Alignment.MIDDLE_LEFT);
		leftHeaderLayout.setComponentAlignment(totalSelectedMatchingGermplasmsLabel, Alignment.MIDDLE_LEFT);
		
		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setWidth("100%");
		headerLayout.setSpacing(true);
		headerLayout.addComponent(leftHeaderLayout);
		headerLayout.addComponent(actionButton);
		headerLayout.setComponentAlignment(leftHeaderLayout, Alignment.BOTTOM_LEFT);
		headerLayout.setComponentAlignment(actionButton, Alignment.BOTTOM_RIGHT);
		
		addComponent(menu);
		addComponent(headerLayout);
		addComponent(matchingGermplasmsTableWithSelectAll);
	}
		
	public void applyGermplasmResults(List<Germplasm> germplasms){
		
		Monitor monitor = MonitorFactory.start("GermplasmSearchResultsComponent.applyGermplasmResults()");
		updateNoOfEntries(germplasms.size());
		matchingGermplasmsTable.removeAllItems();
		for(Germplasm germplasm:germplasms){

		    GidLinkButtonClickListener listener = new GidLinkButtonClickListener(listManagerMain, germplasm.getGid().toString(), viaToolUrl, showAddToList);
        	Button gidButton = new Button(String.format("%s", germplasm.getGid().toString()), listener);
            gidButton.setStyleName(BaseTheme.BUTTON_LINK);
			
            String germplasmFullName = getGermplasmNames(germplasm.getGid());
            String shortenedNames = germplasmFullName.length() > 20 ? germplasmFullName.substring(0, 20) + "..." : germplasmFullName;
            
        	Button namesButton = new Button(shortenedNames, listener);
        	namesButton.setStyleName(BaseTheme.BUTTON_LINK);
			namesButton.setDescription(germplasmFullName);
            
            String crossExpansion = "";
            if(germplasm!=null){
            	try {
            		if(germplasmDataManager!=null) {
                        crossExpansion = germplasmDataManager.getCrossExpansion(germplasm.getGid(), 1);
                    }
            	} catch(MiddlewareQueryException ex){
            		LOG.error(ex.getMessage(), ex);
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
	   		
	   		String methodName = "-";
	   		try {
	   		    Method germplasmMethod = germplasmDataManager.getMethodByID(germplasm.getMethodId());
	   		    if(germplasmMethod!=null && germplasmMethod.getMname()!=null){
	   		        methodName = germplasmMethod.getMname();
	   		    }
	   		} catch (MiddlewareQueryException e) {
	   		    LOG.error(e.getMessage(), e);
	   		}

	   		String locationName = "-";
	   		try {
	   		    @SuppressWarnings("deprecation")
				Location germplasmLocation = germplasmDataManager.getLocationByID(germplasm.getLocationId());
	   		    if(germplasmLocation!=null && germplasmLocation.getLname()!=null){
	   		        locationName = germplasmLocation.getLname();
	   		    }
	   		} catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
            }
            
	   		String stockIDs = germplasm.getStockIDs();
			Label stockLabel = new Label(stockIDs);
	   		stockLabel.setDescription(stockIDs);
            matchingGermplasmsTable.addItem(new Object[]{itemCheckBox, namesButton, crossExpansion, stockLabel, gidButton, locationName, methodName},germplasm.getGid());
		}
		
		updateNoOfEntries();
		
		if(!matchingGermplasmsTable.getItemIds().isEmpty()){
			updateActionMenuOptions(true);
		}
		LOG.debug("" + monitor.stop());
	}

    private String getGermplasmNames(int gid) {

        try {
            List<Name> names = germplasmDataManager.getNamesByGID(new Integer(gid), null, null);
            StringBuilder germplasmNames = new StringBuilder("");
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
        	LOG.error(e.getMessage(), e);
            return null;
        }
    }	
  
    public TableWithSelectAllLayout getMatchingGermplasmsTableWithSelectAll() {
        return matchingGermplasmsTableWithSelectAll;
    }
	
    public Table getMatchingGermplasmsTable(){
    	return matchingGermplasmsTable;
    }
    
    private void updateNoOfEntries(long count){
		totalMatchingGermplasmsLabel.setValue(messageSource.getMessage(Message.TOTAL_RESULTS) + ": " 
       		 + "  <b>" + count + "</b>");
	}
	
	private void updateNoOfEntries(){
		int count = 0;
		count = matchingGermplasmsTable.getItemIds().size();
		updateNoOfEntries(count);
	}
	
	private void updateNoOfSelectedEntries(int count){
		totalSelectedMatchingGermplasmsLabel.setValue("<i>" + messageSource.getMessage(Message.SELECTED) + ": " 
	        		 + "  <b>" + count + "</b></i>");
	}
	
	private void updateNoOfSelectedEntries(){
		int count = 0;
		
		Collection<?> selectedItems = (Collection<?>)matchingGermplasmsTable.getValue();
		count = selectedItems.size();
		
		updateNoOfSelectedEntries(count);
	}
    
	@SuppressWarnings("unchecked")
	public void addSelectedEntriesToNewList() {
		List<Integer> gids = new ArrayList<Integer>();
 		gids.addAll((Collection<? extends Integer>) matchingGermplasmsTable.getValue());

 		if(gids.isEmpty()){
 			MessageNotifier.showError(getWindow(), messageSource.getMessage(Message.WARNING) 
                    , messageSource.getMessage(Message.ERROR_GERMPLASM_MUST_BE_SELECTED));
 		} else{
 			for(Integer gid : gids){
 	 			listManagerMain.addPlantToList(gid);
 	 		}
 		}	
	}

    public boolean isViaToolUrl() {
        return viaToolUrl;
    }

    public void setViaToolUrl(boolean viaToolUrl) {
        this.viaToolUrl = viaToolUrl;
    }

    public boolean isShowAddToList() {
        return showAddToList;
    }

    public void setShowAddToList(boolean showAddToList) {
        this.showAddToList = showAddToList;
    }
}
