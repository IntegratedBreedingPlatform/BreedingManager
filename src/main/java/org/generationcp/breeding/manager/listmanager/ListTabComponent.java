
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.pojos.GermplasmList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.VerticalLayout;

@Configurable
public class ListTabComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent, BreedingManagerLayout {

	private static final long serialVersionUID = 1L;

	private final ListManagerMain source;
	private final ListSelectionLayout listSelectionLayout;
	private ListComponent listComponent;
	private final GermplasmList germplasmList;

	public ListTabComponent(ListManagerMain source, ListSelectionLayout detailsLayout, GermplasmList germplasmList) {
		super();
		this.source = source;
		this.listSelectionLayout = detailsLayout;
		this.germplasmList = germplasmList;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instantiateComponents();
		this.initializeValues();
		this.addListeners();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		this.listComponent = new ListComponent(this.source, this, this.germplasmList);
		this.listComponent.setDebugId("listComponent");
		if (this.germplasmList != null) {
			this.listSelectionLayout.addUpdateListStatusForChanges(this.listComponent, false);
		}
	}

	@Override
	public void initializeValues() {
		// do nothing
	}

	@Override
	public void addListeners() {
		// do nothing
	}

	@Override
	public void layoutComponents() {
		this.setMargin(true);
		this.addComponent(this.listComponent);
	}

	@Override
	public void updateLabels() {
		// do nothing
	}

	public void setListNameLabel(String name) {
		// TODO implement changing of List Name in details section for Rename List
	}

	public ListSelectionLayout getListSelectionLayout() {
		return this.listSelectionLayout;
	}

	public ListComponent getListComponent() {
		return this.listComponent;
	}

	public GermplasmList getGermplasmList() {
		return this.germplasmList;
	}
}
