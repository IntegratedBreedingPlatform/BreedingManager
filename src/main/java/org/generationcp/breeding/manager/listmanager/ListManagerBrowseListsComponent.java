package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.BreedingManagerApplication;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
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
	
	private Label heading;
	
	@Autowired
    private SimpleResourceBundleMessageSource messageSource;
	
	BreedingManagerApplication breedingManagerApplication;
	
	private boolean forGermplasmListWindow;
	
	private Integer listId;
	
    public ListManagerBrowseListsComponent() {
        super();
        this.listId = null;
    }
    
    public ListManagerBrowseListsComponent(Integer listId){
    	super();
    	this.listId = listId;
    }
    
    public ListManagerBrowseListsComponent(BreedingManagerApplication breedingManagerApplication, boolean forGermplasmListWindow) {
        this.breedingManagerApplication = breedingManagerApplication;
        this.forGermplasmListWindow=forGermplasmListWindow;
    }
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		heading = new Label();
		heading.setValue(messageSource.getMessage(Message.BROWSE_LISTS));
		heading.addStyleName("gcp-content-title");
		
		if(listId != null){
			listManagerTreeComponent = new ListManagerTreeComponent(this, forGermplasmListWindow, listId);
		} else{
			listManagerTreeComponent = new ListManagerTreeComponent(this, forGermplasmListWindow);
		}
		
		addComponent(heading,"top:30px; left:20px;");
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
