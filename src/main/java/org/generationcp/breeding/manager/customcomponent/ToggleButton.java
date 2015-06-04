
package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.constants.AppConstants;
import org.generationcp.breeding.manager.constants.ToggleDirection;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("unchecked")
@Configurable
public class ToggleButton extends Button implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = 1L;

	String description;
	ToggleDirection direction; // left, right or both

	public ToggleButton(String description) {
		this.description = description;
		this.direction = ToggleDirection.BOTH;
	}

	public ToggleButton(String description, ToggleDirection direction) {
		this.description = description;
		this.direction = direction;
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.setHtmlContentAllowed(true);
		this.setDescription(this.description);
		this.setStyleName(BaseTheme.BUTTON_LINK);
		this.setWidth("25px");
		this.setHeight("25px");

		this.setDirection(this.direction);
	}

	public void toggleLeft() {
		this.setIcon(AppConstants.Icons.ICON_TOGGLE_LEFT);
	}

	public void toggleRight() {
		this.setIcon(AppConstants.Icons.ICON_TOGGLE_RIGHT);
	}

	public void toggle() {
		this.setIcon(AppConstants.Icons.ICON_TOGGLE);
	}

	public ToggleDirection getDirection() {
		return this.direction;
	}

	public void setDirection(ToggleDirection direction) {
		this.direction = direction;

		if (direction.equals(ToggleDirection.LEFT)) {
			this.toggleLeft();
		} else if (direction.equals(ToggleDirection.RIGHT)) {
			this.toggleRight();
		} else {
			this.toggle();
		}
	}

}
