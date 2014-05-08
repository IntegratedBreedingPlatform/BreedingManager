package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.GermplasmDetailsComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class GermplasmSearchResultsComponent extends CssLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 5314653969843976836L;

	private Label matchingGermplasmsLabel;
	private Table matchingGermplasmsTable;
	private TableWithSelectAllLayout matchingGermplasmsTableWithSelectAll;
	
	private static final String CHECKBOX_COLUMN_ID = "Tag All Column";
	
	public static final String MATCHING_GEMRPLASMS_TABLE_DATA = "Matching Germplasms Table";
	
    static final Action ACTION_COPY_TO_NEW_LIST= new Action("Copy to new list");
    static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_COPY_TO_NEW_LIST };
	
	private final org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain listManagerMain;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	public GermplasmSearchResultsComponent(final ListManagerMain listManagerMain) {
		this.listManagerMain = listManagerMain;
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
		
		setWidth("100%");
		setHeight("500px");
		
		matchingGermplasmsLabel = new Label();
		matchingGermplasmsLabel.setWidth("100%");
		
		matchingGermplasmsLabel = new Label(messageSource.getMessage(Message.TOTAL_RESULTS) + ": " 
       		 + "  <b>" + 0 + "</b>", Label.CONTENT_XHTML);
		
		matchingGermplasmsLabel.setStyleName("lm-search-results-label");
       	
		matchingGermplasmsTableWithSelectAll = new TableWithSelectAllLayout(10, CHECKBOX_COLUMN_ID);
		matchingGermplasmsTableWithSelectAll.setHeight("410px");
		
		matchingGermplasmsTable = matchingGermplasmsTableWithSelectAll.getTable();
		matchingGermplasmsTable.setData(MATCHING_GEMRPLASMS_TABLE_DATA);
		matchingGermplasmsTable.addContainerProperty(CHECKBOX_COLUMN_ID, CheckBox.class, null);
		matchingGermplasmsTable.addContainerProperty("GID", Button.class, null);
		matchingGermplasmsTable.addContainerProperty("NAMES", String.class,null);
		matchingGermplasmsTable.addContainerProperty("PARENTAGE", String.class,null);
		matchingGermplasmsTable.setWidth("100%");
		matchingGermplasmsTable.setMultiSelect(true);
		matchingGermplasmsTable.setSelectable(true);
		matchingGermplasmsTable.setImmediate(true);
		matchingGermplasmsTable.setDragMode(TableDragMode.ROW);
		matchingGermplasmsTable.setHeight("360px");
		
		matchingGermplasmsTable.addListener(new ItemClickListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void itemClick(final ItemClickEvent event) {
				final Integer itemId = (Integer) event.getItemId(); 
				launchGermplasmDetailsWindow(getWindow(), "Germplasm " + itemId.toString() , itemId);
			}
		});
		
		messageSource.setColumnHeader(matchingGermplasmsTable, CHECKBOX_COLUMN_ID, Message.CHECK_ICON);
		
		matchingGermplasmsTable.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
			private static final long serialVersionUID = 1L;

			@Override
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

				@Override
				public Action[] getActions(Object target, Object sender) {
					return GERMPLASMS_TABLE_CONTEXT_MENU;
	            }

				@SuppressWarnings("unchecked")
				@Override
				public void handleAction(Action action, Object sender, Object target) {
	             	if (ACTION_COPY_TO_NEW_LIST == action) {
	             		List<Integer> gids = new ArrayList<Integer>();
	             		gids.addAll((Collection<? extends Integer>) matchingGermplasmsTable.getValue());
	             		for(Integer gid : gids){
	             			listManagerMain.addPlantToList(gid);
	             		}
	             	}
				}
			});
	}

	@Override
	public void layoutComponents() {
		addComponent(matchingGermplasmsLabel);
		addComponent(matchingGermplasmsTableWithSelectAll);
	}
		
	public void applyGermplasmResults(List<Germplasm> germplasms){
		matchingGermplasmsLabel.setValue(messageSource.getMessage(Message.TOTAL_RESULTS)+": "+String.valueOf(germplasms.size()));
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
		
		// Update total count
		final int count = matchingGermplasmsTable.getItemIds().size();
		matchingGermplasmsLabel.setValue(new Label(messageSource.getMessage(Message.TOTAL_RESULTS) + ": " 
	       		 + "  <b>" + count + "</b>", Label.CONTENT_XHTML));
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
	
    public Table getMatchingGermplasmsTable(){
    	return matchingGermplasmsTable;
    }
    
    private Window launchGermplasmDetailsWindow (final Window window, final String caption, final Integer gid) {

        final CssLayout layout = new CssLayout();
        layout.setMargin(true);

        layout.addComponent(new GermplasmDetailsComponent(listManagerMain, gid));
        
        final Window popupWindow = new Window();
        popupWindow.setWidth("900px");
        popupWindow.setHeight("550px");
        popupWindow.setModal(true);
        popupWindow.setResizable(false);
        popupWindow.center();
        popupWindow.setCaption(caption);
        popupWindow.setContent(layout);
        popupWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        popupWindow.addStyleName("lm-list-manager-popup");

        window.addWindow(popupWindow);
        
        return popupWindow;
	}
}
