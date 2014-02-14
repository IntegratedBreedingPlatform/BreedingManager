package org.generationcp.breeding.manager.listmanager.sidebyside;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.constants.ListDataTablePropertyID;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


@Configurable
public class BuildNewListComponentSidebyside extends VerticalLayout implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(BuildNewListComponentSidebyside.class);
    
    private static final long serialVersionUID = -7736422783255724272L;
    
    @Autowired
    private SimpleResourceBundleMessageSource messageSource;
    
    public static final String GERMPLASMS_TABLE_DATA = "Germplasms Table Data";
    static final Action ACTION_SELECT_ALL = new Action("Select All");
    static final Action ACTION_DELETE_SELECTED_ENTRIES = new Action("Delete Selected Entries");
    static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_SELECT_ALL, ACTION_DELETE_SELECTED_ENTRIES };
    
    private Label buildNewListTitle;
    private Label buildNewListDesc;
    
    private HorizontalLayout detailsSubLayout1;
    private Label listNameLabel;
    private TextField listNameText;
    private Label listTypeLabel;
    private ComboBox listTypeComboBox;
    
    private HorizontalLayout detailsSubLayout2;
    private Label listDateLabel;
    private DateField listDateField;
    private Label notesLabel;
    private TextArea notesTextArea;
    
    private HorizontalLayout detailsSubLayout3;
    private Label descriptionLabel;
    private TextField descriptionText;
    
    private HorizontalLayout detailsLayout;
    
    private Table germplasmsTable;
    
    public BuildNewListComponentSidebyside() {
        
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        assemble();
    }
    
    private void assemble() {
        initializeComponents();
        initializeLayout();
    }
    
    private void initializeComponents() {
        buildNewListTitle = new Label(messageSource.getMessage(Message.BUILD_A_NEW_LIST));
        buildNewListTitle.addStyleName(Bootstrap.Typography.H3.styleName());
        
        buildNewListDesc = new Label();
        buildNewListDesc.setValue(messageSource.getMessage(Message.BUILD_YOUR_LIST_BY_DRAGGING_LISTS_OR_GERMPLASM_RECORDS_INTO_THIS_NEW_LIST_WINDOW));
        buildNewListDesc.setWidth("500px");
        
        detailsSubLayout1 = new HorizontalLayout();
        
        listNameLabel = new Label();
        listNameLabel.setCaption(messageSource.getMessage(Message.NAME_LABEL)+":*");
        detailsSubLayout1.addComponent(listNameLabel);
        
        listNameText = new TextField();
        listNameText.setWidth("200px");
        listNameText.setMaxLength(50);
        detailsSubLayout1.addComponent(listNameText);        

        listTypeLabel = new Label();
        listTypeLabel.setCaption(messageSource.getMessage(Message.TYPE_LABEL)+":*");
        detailsSubLayout1.addComponent(listTypeLabel);
        
        listTypeComboBox = new ComboBox();
        listTypeComboBox.setWidth("200px");
        listTypeComboBox.setNullSelectionAllowed(false);
        detailsSubLayout1.addComponent(listTypeComboBox);
        
        
        detailsSubLayout2 = new HorizontalLayout();
        
        listDateLabel = new Label();
        listDateLabel.setCaption(messageSource.getMessage(Message.DATE_LABEL)+":*");
        detailsSubLayout2.addComponent(listDateLabel);
      
        listDateField = new DateField();
        listDateField.setDateFormat("yyyy-MM-dd");
        listDateField.setResolution(DateField.RESOLUTION_DAY);
        listDateField.setValue(new Date());
        detailsSubLayout2.addComponent(listDateField);
        
        notesLabel = new Label();
        notesLabel.setCaption(messageSource.getMessage(Message.NOTES)+":");
        detailsSubLayout2.addComponent(notesLabel);
        
        notesTextArea = new TextArea();
        notesTextArea.setWidth("300px");
        notesTextArea.setHeight("65px");
        notesTextArea.addStyleName("noResizeTextArea");
        detailsSubLayout2.addComponent(notesTextArea);
        
        detailsSubLayout3 = new HorizontalLayout();
        
        descriptionLabel = new Label();
        descriptionLabel.setCaption(messageSource.getMessage(Message.DESCRIPTION_LABEL)+"*");
        detailsSubLayout3.addComponent(descriptionLabel);
        
        descriptionText = new TextField();
        descriptionText.setWidth("420px");
        detailsSubLayout3.addComponent(descriptionText);
        
        createGermplasmTable();
    }
    
    private void initializeLayout() {
        this.setSpacing(true);
        this.addComponent(buildNewListTitle);
        this.addComponent(buildNewListDesc);
        
        detailsSubLayout1.setSpacing(true);        
        detailsSubLayout2.setSpacing(true);
        detailsSubLayout3.setSpacing(true);
        
        VerticalLayout detailsVertical = new VerticalLayout();
        detailsVertical.setSpacing(true);
        detailsVertical.addComponent(detailsSubLayout1);
        detailsVertical.addComponent(detailsSubLayout2);
        detailsVertical.addComponent(detailsSubLayout3);
        
        detailsLayout = new HorizontalLayout();
        detailsLayout.setSpacing(true);
        detailsLayout.addComponent(detailsVertical);
        
        this.addComponent(detailsLayout);
        this.addComponent(germplasmsTable);
    };
    
    public void createGermplasmTable(){
        
        germplasmsTable = new Table();
        germplasmsTable.setData(GERMPLASMS_TABLE_DATA);
        germplasmsTable.addContainerProperty(ListDataTablePropertyID.GID.getName(), Button.class, null);
        germplasmsTable.addContainerProperty(ListDataTablePropertyID.ENTRY_ID.getName(), Integer.class, null);
        germplasmsTable.addContainerProperty(ListDataTablePropertyID.ENTRY_CODE.getName(), String.class, null);
        germplasmsTable.addContainerProperty(ListDataTablePropertyID.SEED_SOURCE.getName(), String.class, null);
        germplasmsTable.addContainerProperty(ListDataTablePropertyID.DESIGNATION.getName(), String.class, null);
        germplasmsTable.addContainerProperty(ListDataTablePropertyID.PARENTAGE.getName(), String.class, null);
        
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.GID.getName(), Message.LISTDATA_GID_HEADER);
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.ENTRY_ID.getName(), Message.LISTDATA_ENTRY_ID_HEADER);
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.ENTRY_CODE.getName(), Message.LISTDATA_ENTRY_CODE_HEADER);
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.SEED_SOURCE.getName(), Message.LISTDATA_SEEDSOURCE_HEADER);
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.DESIGNATION.getName(), Message.LISTDATA_DESIGNATION_HEADER);
        messageSource.setColumnHeader(germplasmsTable, ListDataTablePropertyID.PARENTAGE.getName(), Message.LISTDATA_GROUPNAME_HEADER);
        
        germplasmsTable.setSelectable(true);
        germplasmsTable.setMultiSelect(true);
        germplasmsTable.setWidth("100%");
        germplasmsTable.setHeight("280px");
        
        germplasmsTable.addActionHandler(new Action.Handler() {

            private static final long serialVersionUID = 1884343225476178686L;

            public Action[] getActions(Object target, Object sender) {
                return GERMPLASMS_TABLE_CONTEXT_MENU;
            }

            @Override
            public void handleAction(Action action, Object sender, Object target) {
                if(ACTION_SELECT_ALL == action) {
                    germplasmsTable.setValue(germplasmsTable.getItemIds());
                } else if(ACTION_DELETE_SELECTED_ENTRIES == action) {
                    deleteSelectedEntries();
                }
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private void deleteSelectedEntries(){
        List<Integer> selectedItemIds = new ArrayList<Integer>();
        selectedItemIds.addAll((Collection<? extends Integer>) germplasmsTable.getValue());
        for(Integer selectedItemId:selectedItemIds){
            germplasmsTable.removeItem(selectedItemId);
        }
        assignSerializedEntryNumber();
    }
    
    /**
     * Iterates through the whole table, and sets the entry number from 1 to n based on the row position
     */
    private void assignSerializedEntryNumber(){
        List<Integer> itemIds = getItemIds(germplasmsTable);
                
        int id = 1;
        for(Integer itemId : itemIds){
            germplasmsTable.getItem(itemId).getItemProperty(ListDataTablePropertyID.ENTRY_ID.getName()).setValue(id);
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
