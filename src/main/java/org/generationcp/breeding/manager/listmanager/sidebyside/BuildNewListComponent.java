package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.BreedingManagerListDetailsComponent;
import org.generationcp.breeding.manager.customfields.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.breeding.manager.listmanager.listeners.ResetListButtonClickListener;
import org.generationcp.breeding.manager.listmanager.util.BuildNewListDropHandler;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


@Configurable
public class BuildNewListComponent extends VerticalLayout implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(BuildNewListComponent.class);
    
    private static final long serialVersionUID = -7736422783255724272L;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    @Autowired
    private GermplasmDataManager germplasmDataManager;
    
    @Autowired
    private GermplasmListManager germplasmListManager;
    
    public static final String GERMPLASMS_TABLE_DATA = "Germplasms Table Data";
    static final Action ACTION_SELECT_ALL = new Action("Select All");
    static final Action ACTION_DELETE_SELECTED_ENTRIES = new Action("Delete Selected Entries");
    static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE_SELECTED_ENTRIES };
    
    private Label buildNewListTitle;
    private Label buildNewListDesc;
    
    private BreedingManagerListDetailsComponent breedingManagerListDetailsComponent;
    
    private TableWithSelectAllLayout tableWithSelectAllLayout;
    
    private ListManagerMain source;
    
    private HorizontalLayout buttonRow;
    private Button saveButton;
    private Button resetButton;
    
    public BuildNewListComponent() {
        super();
    }
    
    public BuildNewListComponent(ListManagerMain source) {
        super();
        this.source = source;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        assemble();
    }
    
    private void assemble() {
        initializeComponents();
        initializeLayout();
        initializeHandlers();
    }
    
    private void initializeHandlers() {
    	tableWithSelectAllLayout.getTable().setDropHandler(new BuildNewListDropHandler(germplasmDataManager, germplasmListManager));
    }
    
    private void initializeComponents() {
        buildNewListTitle = new Label(messageSource.getMessage(Message.BUILD_A_NEW_LIST));
        buildNewListTitle.addStyleName(Bootstrap.Typography.H3.styleName());
        
        buildNewListDesc = new Label();
        buildNewListDesc.setValue(messageSource.getMessage(Message.BUILD_YOUR_LIST_BY_DRAGGING_LISTS_OR_GERMPLASM_RECORDS_INTO_THIS_NEW_LIST_WINDOW));
        buildNewListDesc.setWidth("500px");
        
        breedingManagerListDetailsComponent = new BreedingManagerListDetailsComponent();
        breedingManagerListDetailsComponent.getContainerPanel().setWidth("631px");
        
        tableWithSelectAllLayout = new TableWithSelectAllLayout(ListDataTablePropertyID.TAG.getName());
        createGermplasmTable(tableWithSelectAllLayout.getTable());
        
        buttonRow = new HorizontalLayout();
        saveButton = new Button();
        resetButton = new Button();
        
        buttonRow.setWidth("100%");
        buttonRow.setHeight("50px");
        buttonRow.setSpacing(true);

        saveButton.setCaption(messageSource.getMessage(Message.SAVE_LIST));
        saveButton.setWidth("80px");
        saveButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());

        resetButton.setCaption(messageSource.getMessage(Message.RESET));
        resetButton.setWidth("80px");
        resetButton.addStyleName(Bootstrap.Buttons.DEFAULT.styleName());

    }
    
    private void initializeLayout() {
        this.setSpacing(true);
        this.addComponent(buildNewListTitle);
        this.addComponent(buildNewListDesc);
        this.addComponent(breedingManagerListDetailsComponent);
        this.addComponent(tableWithSelectAllLayout);
        
        buttonRow.addComponent(resetButton);
        buttonRow.setComponentAlignment(resetButton, Alignment.MIDDLE_RIGHT);
        buttonRow.addComponent(saveButton);
        buttonRow.setComponentAlignment(saveButton, Alignment.MIDDLE_LEFT);
        
        this.addComponent(buttonRow);
        this.setComponentAlignment(buttonRow, Alignment.MIDDLE_CENTER);
    };
    
    public void createGermplasmTable(final Table table){
        
    	table.setData(GERMPLASMS_TABLE_DATA);
    	table.addContainerProperty(ListDataTablePropertyID.TAG.getName(), CheckBox.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), String.class, null);
    	table.addContainerProperty(ListDataTablePropertyID.PARENTAGE.getName(), String.class, null);
        
        messageSource.setColumnHeader(table, ListDataTablePropertyID.TAG.getName(), Message.CHECK_ICON);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.GID.getName(), Message.LISTDATA_GID_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.ENTRY_ID.getName(), Message.LISTDATA_ENTRY_ID_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.ENTRY_CODE.getName(), Message.LISTDATA_ENTRY_CODE_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.SEED_SOURCE.getName(), Message.LISTDATA_SEEDSOURCE_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.DESIGNATION.getName(), Message.LISTDATA_DESIGNATION_HEADER);
        messageSource.setColumnHeader(table, ListDataTablePropertyID.PARENTAGE.getName(), Message.LISTDATA_GROUPNAME_HEADER);
        
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setWidth("100%");
        table.setHeight("280px");
        
        table.addActionHandler(new Action.Handler() {

            private static final long serialVersionUID = 1884343225476178686L;

            public Action[] getActions(Object target, Object sender) {
                return GERMPLASMS_TABLE_CONTEXT_MENU;
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                if(ACTION_SELECT_ALL == action) {
                	table.setValue(table.getItemIds());
                } else if(ACTION_DELETE_SELECTED_ENTRIES == action) {
                    deleteSelectedEntries();
                }
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private void deleteSelectedEntries(){
        List<Integer> selectedItemIds = new ArrayList<Integer>();
        selectedItemIds.addAll((Collection<? extends Integer>) tableWithSelectAllLayout.getTable().getValue());
        for(Integer selectedItemId:selectedItemIds){
        	tableWithSelectAllLayout.getTable().removeItem(selectedItemId);
        }
        assignSerializedEntryNumber();
    }
    
    /**
     * Iterates through the whole table, and sets the entry number from 1 to n based on the row position
     */
    private void assignSerializedEntryNumber(){
        List<Integer> itemIds = getItemIds(tableWithSelectAllLayout.getTable());
                
        int id = 1;
        for(Integer itemId : itemIds){
        	tableWithSelectAllLayout.getTable().getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(id);
            id++;
        }
    }
    
    /**
     * Get item id's of a table, and return it as a list 
     * @param table
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Integer> getItemIds(Table table){
        List<Integer> itemIds = new ArrayList<Integer>();
        itemIds.addAll((Collection<? extends Integer>) table.getItemIds());
        return itemIds;
    }

}
