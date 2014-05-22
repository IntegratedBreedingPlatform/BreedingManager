package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ListSelectionComponent extends CssLayout implements InternationalizableComponent, InitializingBean, BreedingManagerLayout, ListTreeActionsListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(ListSelectionComponent.class);

	private static final long serialVersionUID = -383145225475654748L;

	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private final ListManagerMain source;
	
	private ListSelectionLayout listSelectionLayout;
	private ListManagerTreeComponent listTreeComponent;
	private ListSearchComponent listSearchComponent;
	
	private final Integer selectedListId;
	
	public ListSelectionComponent(final ListManagerMain source, final Integer listId) {
		super();
		this.source = source;
		this.selectedListId = listId;
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
		// No op
	}

	@Override
	public void instantiateComponents() {
		
		setSizeFull();
		
		listSelectionLayout = new ListSelectionLayout(source, selectedListId);
		listTreeComponent = new ListManagerTreeComponent(this, selectedListId);
		listSearchComponent = new ListSearchComponent(listSelectionLayout);
	}

	@Override
	public void initializeValues() {
		// No op
	}

	@Override
	public void addListeners() {
		// No op
	}

	@Override
	public void layoutComponents() {
		
		setMargin(true);
		
		addComponent(listSelectionLayout);
		this.addStyleName("list-selection-component");
		listSelectionLayout.addStyleName("list-selection-layout");
	}

	@Override
    public void openListDetails(final GermplasmList list) {
        try {
            listSelectionLayout.createListDetailsTab(list.getId());
        } catch (MiddlewareQueryException e) {
            LOG.error("Error in displaying germplasm list details.", e);
            throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
        }
	}
	
	@Override
	public void updateUIForDeletedList(GermplasmList list) {
		this.listSelectionLayout.removeTab(list.getId());
	}

	@Override
	public void updateUIForRenamedList(GermplasmList list, String newName) {
		this.listSelectionLayout.renameTab(list.getId(), newName);
	}

	public ListSelectionLayout getListDetailsLayout() {
		return listSelectionLayout;
	}
    
	public ListManagerTreeComponent getListTreeComponent(){
		return listTreeComponent;
	}
	
    
    private Window launchListSelectionWindow (final Window window, final Component content, final String caption) {

        final CssLayout layout = new CssLayout();
        layout.setMargin(true);
        layout.setWidth("100%");
        layout.setHeight("490px");

        layout.addComponent(content);
        
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

	public void openListBrowseDialog() {
		launchListSelectionWindow(getWindow(), listTreeComponent, messageSource.getMessage(Message.BROWSE_FOR_LISTS));
	}

	public void openListSearchDialog() {
		launchListSelectionWindow(getWindow(), listSearchComponent, messageSource.getMessage(Message.SEARCH_FOR_LISTS));
	}

	@Override
	public void folderClicked(GermplasmList list) {
		// TODO Auto-generated method stub
		
	}
}
