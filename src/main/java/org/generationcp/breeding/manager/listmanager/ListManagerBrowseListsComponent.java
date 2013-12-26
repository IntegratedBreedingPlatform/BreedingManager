package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;

@Configurable
public class ListManagerBrowseListsComponent extends AbsoluteLayout implements
		InternationalizableComponent, InitializingBean {

	private static final long serialVersionUID = -224052511814636864L;
    private ListManagerTreeComponent listManagerTreeComponent;	
    private ListManagerMain listManagerMain;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	BreedingManagerApplication breedingManagerApplication;
	
	private boolean forGermplasmListWindow;
	
	private Integer listId;
	
    public ListManagerBrowseListsComponent(ListManagerMain listManagerMain) {
        super();
        this.listManagerMain = listManagerMain;
        this.listId = null;
    }
    
    public ListManagerBrowseListsComponent(ListManagerMain listManagerMain, Integer listId){
    	super();
    	this.listManagerMain = listManagerMain;
    	this.listId = listId;
    }
    
    public ListManagerBrowseListsComponent(BreedingManagerApplication breedingManagerApplication, boolean forGermplasmListWindow) {
        this.breedingManagerApplication = breedingManagerApplication;
        this.forGermplasmListWindow=forGermplasmListWindow;
    }
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
		if(listId != null){
			listManagerTreeComponent = new ListManagerTreeComponent(listManagerMain, this, forGermplasmListWindow, listId);
		} else{
			listManagerTreeComponent = new ListManagerTreeComponent(listManagerMain, this, forGermplasmListWindow);
		}
		
		addComponent(listManagerTreeComponent, "top:55px; left:20px");
		
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
	}

	public ListManagerTreeComponent getListManagerTreeComponent(){
		return listManagerTreeComponent;
	}
	
}
