package org.generationcp.breeding.manager.listmanager;

import java.util.List;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listmanager.listeners.GermplasmListManagerButtonClickListener;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Runo;

@Configurable
public class ListManagerSearchListsComponent extends AbsoluteLayout implements
		InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 5314653969843976836L;
	public static final String SEARCH_BUTTON = "List Manager Search Button";
	private static final String GUIDE = 
	        "You may search for germplasms and germplasm lists using GID's, germplasm names (partial/full), or list names (partial/full)" +
	        " <br/><br/><b>Matching lists would contain</b> <br/>" +
	        "  - Lists with names containing the search query <br/>" +
	        "  - Lists containing germplasms given a GID <br/>" +
	        "  - Lists containing germplasms with names <br/>" +
	        " containing the search query" +
	        " <br/><br/><b>Matching germplasms would contain</b> <br/>" +
	        "  - Germplasms with matching GID's <br/>" +
	        "  - Germplasms with name containing search query <br/>" +
	        "  - Parents of the result germplasms (if selected)" +
	        " <br/><br/>The <b>Exact matches only</b> checkbox allows you search using partial names (when unchecked)" +
	        " or to only return results which match the query exactly (when checked).";
	
	private AbsoluteLayout searchBar;
	private Label searchLabel;
	private TextField searchField;
	private GermplasmSearchResultsComponent searchResultsComponent;
	private final ListManagerMain listManagerMain;
	private Button searchButton;
    private CheckBox likeOrEqualCheckBox;
    private CheckBox includeParentsCheckBox;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;	
	
	@Autowired
	private GermplasmDataManager germplasmDataManager;
	
	@Autowired
	private GermplasmListManager germplasmListManager;
	
	public ListManagerSearchListsComponent(ListManagerMain listManagerMain){
		this.listManagerMain = listManagerMain;
	}
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
    @Override
	public void afterPropertiesSet() throws Exception {
	    
        
        Panel p = new Panel();
        //p.setWidth("100%");
        //p.setHeight("47px");
        p.addStyleName("search-panel");
        p.addStyleName(Runo.PANEL_LIGHT);
        p.addStyleName("list-manager-search-bar");
        p.setScrollable(false);
        
        searchBar = new AbsoluteLayout();
        searchBar.setWidth("80%");
        searchBar.setHeight("40px");
        //searchBar.addStyleName("list-manager-search-bar");
        
        searchLabel = new Label();
        searchLabel.setValue(messageSource.getMessage(Message.SEARCH_FOR)+": ");
        searchLabel.setWidth("200px");
        
        searchField = new TextField();
        searchField.setImmediate(true);
        
        /**
         * TODO: replace with image button
         */
        
        
        searchButton = new Button();
        searchButton.setWidth("30px");
        searchButton.setHeight("30px");
        searchButton.setStyleName(BaseTheme.BUTTON_LINK);
        searchButton.addStyleName("search-button");
        searchButton.setData(SEARCH_BUTTON);
        searchButton.addListener(new GermplasmListManagerButtonClickListener(this));
        //searchButton.setClickShortcut(KeyCode.ENTER);
        /*
        p.addComponent(searchLabel);
        p.addComponent(searchField);
        p.addComponent(searchButton);
        */
        p.addAction(new ShortcutListener("Next field", KeyCode.ENTER, null) {
            private static final long serialVersionUID = 288627665348761948L;

            @Override
            public void handleAction(Object sender, Object target) {
                // The panel is the sender, loop trough content
                searchButtonClickAction();
            }
        });
        
            
            //p.addComponent(searchButton);
        //searchPanel.addComponent(p, "top:0px; left:3px;");
       

        Label descLbl = new Label(GUIDE, Label.CONTENT_XHTML);
        descLbl.setWidth("300px");
        PopupView popup = new PopupView(" ? ",descLbl);
        popup.setStyleName("gcp-popup-view");
        
        likeOrEqualCheckBox = new CheckBox();
        likeOrEqualCheckBox.setCaption(messageSource.getMessage(Message.EXACT_MATCHES_ONLY));
        
        includeParentsCheckBox = new CheckBox();
        includeParentsCheckBox.setCaption(messageSource.getMessage(Message.INCLUDE_PARENTS));
        
        searchBar.addComponent(searchLabel, "top:13px; left:20px;");
        searchBar.addComponent(searchField, "top:10px; left:100px;");
        searchBar.addComponent(searchButton, "top:8px; left:280px;");
        searchBar.addComponent(popup, "top:12px; left:330px;");
        searchBar.addComponent(likeOrEqualCheckBox, "top:13px; left: 350px;");
        searchBar.addComponent(includeParentsCheckBox, "top:13px; left: 500px;");
        
        //searchResultsComponent = new SearchResultsComponent(this.listManagerMain, this);
        
        //addComponent(searchBar, "top:20px; left:20px;");
        //p.addComponent(searchBar);
        p.setLayout(searchBar);
        addComponent(p, "top:10px; left:20px;");
        addComponent(searchResultsComponent, "top:50px; left:20px;");	    

	}

	public void searchButtonClickAction(){
		String q = searchField.getValue().toString();
		doSearch(q);
	}
	
	public void doSearch(String q){
		try {
			
			List<GermplasmList> germplasmLists;
			List<Germplasm> germplasms;
			boolean includeParents = (Boolean) includeParentsCheckBox.getValue();
			if((Boolean) likeOrEqualCheckBox.getValue() == true){
				germplasmLists = doGermplasmListSearch(q, Operation.EQUAL);
				germplasms = doGermplasmSearch(q, Operation.EQUAL, includeParents);
			} else {
				germplasmLists = doGermplasmListSearch(q, Operation.LIKE);
				germplasms = doGermplasmSearch(q, Operation.LIKE, includeParents);
			}
			
			if ((germplasmLists == null || germplasmLists.isEmpty()) &&
					(germplasms == null || germplasms.isEmpty())){
				MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.SEARCH_RESULTS), 
						messageSource.getMessage(Message.NO_SEARCH_RESULTS));
			} 
			searchResultsComponent.applyGermplasmResults(germplasms);
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private List<GermplasmList> doGermplasmListSearch(String q, Operation o) throws MiddlewareQueryException{
		return germplasmListManager.searchForGermplasmList(q, o, true);
	}
	
	private List<Germplasm> doGermplasmSearch(String q, Operation o, boolean includeParents) throws MiddlewareQueryException{
		return germplasmDataManager.searchForGermplasm(q, o, includeParents, true);
	}	
	
	public GermplasmSearchResultsComponent getSearchResultsComponent(){
		return searchResultsComponent;
	}
	
	public TextField getSearchTextfield(){
		return searchField;
	}
	
}
