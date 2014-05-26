package org.generationcp.breeding.manager.listmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.customcomponent.TableWithSelectAllLayout;
import org.generationcp.breeding.manager.listmanager.listeners.GidLinkButtonClickListener;
import org.generationcp.breeding.manager.listmanager.sidebyside.GermplasmDetailsComponent;
import org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.tomcat.util.TomcatUtil;
import org.generationcp.commons.tomcat.util.WebAppStatusInfo;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
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
	
	private final static Logger LOG = LoggerFactory.getLogger(GermplasmSearchResultsComponent.class);
	
	private static final String CHECKBOX_COLUMN_ID = "Tag All Column";
	
	public static final String MATCHING_GEMRPLASMS_TABLE_DATA = "Matching Germplasms Table";
	public static final String GERMPLASM_BROWSER_LINK = "http://localhost:18080/GermplasmStudyBrowser/main/germplasm-";
	
    static final Action ACTION_COPY_TO_NEW_LIST= new Action("Copy to new list");
    static final Action[] GERMPLASMS_TABLE_CONTEXT_MENU = new Action[] { ACTION_COPY_TO_NEW_LIST };
	
	private final org.generationcp.breeding.manager.listmanager.sidebyside.ListManagerMain listManagerMain;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
    @Autowired
    private WorkbenchDataManager workbenchDataManager;
    
    @Autowired
    private TomcatUtil tomcatUtil;	
	
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

        	Button gidButton = new Button(String.format("%s", germplasm.getGid().toString()), new GidLinkButtonClickListener(germplasm.getGid().toString(), true, true));
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

    	/*
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

        window.addWindow(popupWindow);*/
        
    	launchWebTool();
    	
    	Tool tool = null;
        try {
            tool = workbenchDataManager.getToolWithName(ToolName.germplasm_browser.toString());
        } catch (MiddlewareQueryException qe) {
            LOG.error("QueryException", qe);
            /*MessageNotifier.showError(mainWindow, messageSource.getMessage(Message.DATABASE_ERROR),
                    "<br />" + messageSource.getMessage(Message.CONTACT_ADMIN_ERROR_DESC));*/
        }
        
        ExternalResource germplasmBrowserLink = null;
        if (tool == null) {
            germplasmBrowserLink = new ExternalResource(GERMPLASM_BROWSER_LINK + gid + "?restartApplication");
        } else {
            germplasmBrowserLink = new ExternalResource(tool.getPath().replace("germplasm/", "germplasm-") + gid + "?restartApplication");
        }
        
        String preferredName = null;
        try{
        	Name prefName = germplasmDataManager.getPreferredNameByGID(Integer.valueOf(gid));
        	if(prefName != null){
        		preferredName = prefName.getNval();
        	}
        } catch(MiddlewareQueryException ex){
        	LOG.error("Error with getting preferred name of " + gid, ex);
        }
        
        String windowTitle = "Germplasm Details: " + "(" + gid + ")";
        if(preferredName != null){
        	windowTitle = "Germplasm Details: " + preferredName + " (GID: " + gid + ")";
        }
        
        final Window germplasmWindow = new Window(windowTitle);
        
        AbsoluteLayout layoutForGermplasm = new AbsoluteLayout();
        layoutForGermplasm.setMargin(false);
        layoutForGermplasm.setWidth("100%");
        layoutForGermplasm.setHeight("100%");
        layoutForGermplasm.addStyleName("no-caption");

        Embedded germplasmInfo = new Embedded("", germplasmBrowserLink);
        germplasmInfo.setType(Embedded.TYPE_BROWSER);
        germplasmInfo.setSizeFull();
        
        Button addToListLink = new Button("Add to list");
		//addToListLink.setData(ADD_TO_LIST);
		addToListLink.setImmediate(true);
		addToListLink.setStyleName(Bootstrap.Buttons.INFO.styleName());
		addToListLink.setIcon(AppConstants.Icons.ICON_PLUS);
        layoutForGermplasm.addComponent(addToListLink, "top:15px; right:15px;");
        layoutForGermplasm.addComponent(germplasmInfo, "top:44px; left:0;");
        
        addToListLink.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				
				Window listManagerWindow = ((BreedingManagerApplication) event.getComponent().getApplication()).getWindow(BreedingManagerApplication.LIST_MANAGER_WINDOW_NAME);
				Iterator<Component> i = listManagerWindow.getComponentIterator();
				while (i.hasNext()) {
				    Component c = (Component) i.next();
				    if(c instanceof ListManagerMain){
				    	((ListManagerMain) c).getListBuilderComponent().getBuildNewListDropHandler().addGermplasm(Integer.valueOf(gid));
				    }
				}
				window.removeWindow(germplasmWindow);
			}
        	
        });
	        
        germplasmWindow.setContent(layoutForGermplasm);
        germplasmWindow.setWidth("90%");
        germplasmWindow.setHeight("90%");
        germplasmWindow.center();
        germplasmWindow.setResizable(false);
        
        germplasmWindow.setModal(true);
        germplasmWindow.addStyleName(Reindeer.WINDOW_LIGHT);
        germplasmWindow.addStyleName("graybg");
        
        window.addWindow(germplasmWindow);
    	
        return germplasmWindow;
	}
    
    private void launchWebTool(){
    	
		try {
			Tool germplasmBrowserTool;
			germplasmBrowserTool = workbenchDataManager.getToolWithName(ToolName.germplasm_browser.name());
			
			String url = germplasmBrowserTool.getPath();
	    	
	        WebAppStatusInfo statusInfo = null;
	        String contextPath = null;
	        String localWarPath = null;
	        try {
	        	
	            statusInfo = tomcatUtil.getWebAppStatus();
	            contextPath = TomcatUtil.getContextPathFromUrl(url);
	            localWarPath = TomcatUtil.getLocalWarPathFromUrl(url);
	            
	        }
	        catch (Exception e1) {
	          e1.printStackTrace();
	        }
	    	        
	    	      
	        try {
	            boolean deployed = statusInfo.isDeployed(contextPath);
	            boolean running = statusInfo.isRunning(contextPath);
	            
	            if (!running) {
	                if (!deployed) {
	                    // deploy the webapp
	                    tomcatUtil.deployLocalWar(contextPath, localWarPath);
	                } else {
	                    // start the webapp
	                    tomcatUtil.startWebApp(contextPath);
	                }
	            }
	        }
	        catch (Exception e) {
	           //e.printStackTrace();
	        }
			
		} catch (MiddlewareQueryException e2) {
			// TODO Auto-generated catch block
			//e2.printStackTrace();
		}
    	
    	        
    }    
}
