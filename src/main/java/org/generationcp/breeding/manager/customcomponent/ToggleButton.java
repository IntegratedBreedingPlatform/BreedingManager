package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ToggleDirection;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("unchecked")
@Configurable
public class ToggleButton  extends Button implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;
	
	String description;
	ToggleDirection direction; //left, right or both
	
	public ToggleButton(String description){
		this.description = description;
		this.direction = ToggleDirection.BOTH;
	}
	
	public ToggleButton(String description, ToggleDirection direction){
		this.description = description;
		this.direction = direction;
	}
		
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		//setCaption("<span class='fa fa-bars' style='left: 2px; color: #717171;font-size: 18px; font-weight: bold;'></span>");
        setHtmlContentAllowed(true);
		setDescription(description);
		setStyleName(Reindeer.BUTTON_LINK);
		setWidth("25px");
		setHeight("25px");
		
		setDirection(direction);		
	}
	
	public void toggleLeft(){
		setIcon(AppConstants.Icons.ICON_TOGGLE_LEFT);
	}
	
	public void toggleRight(){
		setIcon(AppConstants.Icons.ICON_TOGGLE_RIGHT);
	}
	
	public void toggle(){
		setIcon(AppConstants.Icons.ICON_TOGGLE);
	}

	public ToggleDirection getDirection() {
		return direction;
	}

	public void setDirection(ToggleDirection direction) {
		this.direction = direction;
		
		if(direction.equals(ToggleDirection.LEFT)){
			toggleLeft();
		}
		else if(direction.equals(ToggleDirection.RIGHT)){
			toggleRight();
		}
		else{
			toggle();
		}
	}
	
	
}
