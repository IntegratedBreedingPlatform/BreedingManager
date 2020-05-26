
package org.generationcp.breeding.manager.customcomponent;

import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ViewListHeaderWindow extends BaseSubWindow implements BreedingManagerLayout, InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -8930840553445674785L;

	private final GermplasmList germplasmList;
	private ViewListHeaderComponent listHeaderComponent;

	private Map<Integer, String> userNameMap;

	private List<UserDefinedField> listTypes;

	public ViewListHeaderWindow(final GermplasmList germplasmList, final Map<Integer, String> userNameMap,
			final List<UserDefinedField> listTypes) {
		super();
		this.germplasmList = germplasmList;
		this.userNameMap = userNameMap;
		this.listTypes = listTypes;
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

		// Just create a new GermplasmList with blank details if germplasmList is not available.
		this.listHeaderComponent = new ViewListHeaderComponent(this.germplasmList == null ? new GermplasmList() : this.germplasmList, userNameMap, listTypes);
		this.listHeaderComponent.setDebugId("listHeaderComponent");
	}

	public GermplasmList getGermplasmList() {
		return germplasmList;
	}

	/**
	 * We are using this function to set status, because we have 2 copies of germplasm list, one is in ViewListHeaderWindow and the other
	 * one is in ViewListHeaderComponent, which is due to the implementation of how we instantiateComponents() in Vaadin
	 * It could be improved in the future.
	 *
	 * @param status
	 */
	public void setGermplasmListStatus(final int status) {
		this.getGermplasmList().setStatus(status);
		this.getListHeaderComponent().setStatus(status);
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
