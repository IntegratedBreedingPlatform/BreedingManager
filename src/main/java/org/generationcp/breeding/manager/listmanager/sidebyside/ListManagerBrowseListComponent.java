package org.generationcp.breeding.manager.listmanager.sidebyside;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.constants.ListManagerDetailsTabSource;
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

import com.vaadin.ui.CssLayout;

@Configurable
public class ListManagerBrowseListComponent extends CssLayout implements
	InternationalizableComponent, InitializingBean, BreedingManagerLayout{
    
    private static final Logger LOG = LoggerFactory.getLogger(ListManagerBrowseListComponent.class);

	private static final long serialVersionUID = -383145225475654748L;

	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	private ListManagerDetailsLayout listManagerDetailsLayout;
	
	private final ListManagerMain source;
	
	private final Integer selectedListId;
		
	public ListManagerBrowseListComponent(ListManagerMain source) {
		super();
		this.source = source;
		this.selectedListId = null;
	}
	
	public ListManagerBrowseListComponent(ListManagerMain source, Integer listId) {
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
		
	}

	@Override
	public void instantiateComponents() {
		setSizeFull();
		listManagerDetailsLayout = new ListManagerDetailsLayout(source, ListManagerDetailsTabSource.BROWSE, selectedListId);
	}

	@Override
	public void initializeValues() {
		
	}

	@Override
	public void addListeners() {

	}

	@Override
	public void layoutComponents() {
		addComponent(listManagerDetailsLayout);
	}

    public void openListDetails(GermplasmList list) {
        try{
            listManagerDetailsLayout.createListDetailsTab(list.getId());
        } catch (MiddlewareQueryException e){
            LOG.error("Error in displaying germplasm list details.", e);
            throw new InternationalizableException(e, Message.ERROR_DATABASE,
                    Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
        }
	}

	public ListManagerDetailsLayout getListDetailsLayout() {
		return listManagerDetailsLayout;
	}
    
	// TODO eeek
	public ListManagerTreeComponent getListTreeComponent(){
		return null;
	}
}
