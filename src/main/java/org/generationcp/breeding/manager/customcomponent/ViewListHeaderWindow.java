
package org.generationcp.breeding.manager.customcomponent;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ViewListHeaderWindow extends BaseSubWindow implements BreedingManagerLayout, InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -8930840553445674785L;

	private final GermplasmList germplasmList;
	private ViewListHeaderComponent listHeaderComponent;

	public ViewListHeaderWindow(GermplasmList germplasmList) {
		super();
		this.germplasmList = germplasmList;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	public ViewListHeaderComponent getListHeaderComponent() {
		return this.listHeaderComponent;
	}

	@Override
	public void instantiateComponents() {
		this.setCaption("List Header Details");
		this.addStyleName(Reindeer.WINDOW_LIGHT);
		this.setResizable(false);
		this.setModal(true);
		this.setHeight("340px");
		this.setWidth("350px");

		this.listHeaderComponent = new ViewListHeaderComponent(this.germplasmList);
	}

	@Override
	public void initializeValues() {
		// not implemented
	}

	@Override
	public void addListeners() {
		// not implemented
	}

	@Override
	public void layoutComponents() {
		this.addComponent(this.listHeaderComponent);
	}

	@Override
	public void updateLabels() {
		// not implemented
	}

}
