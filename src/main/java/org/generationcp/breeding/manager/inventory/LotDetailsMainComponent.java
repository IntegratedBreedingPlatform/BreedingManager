package org.generationcp.breeding.manager.inventory;

import java.util.List;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.VerticalLayout;

@Configurable
public class LotDetailsMainComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent,
		BreedingManagerLayout {

	@Autowired
	private InventoryDataManager inventoryDataManager;

	private final Integer gid;
	private List<LotDetails> lotDetails;

	public LotDetailsMainComponent(Integer gid) {
		this.gid = gid;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.initializeValues();
		this.layoutComponents();
	}

	@Override
	public void instantiateComponents() {
		//No Implementation
	}

	@Override
	public void initializeValues() {
		this.lotDetails = this.inventoryDataManager.getLotDetailsForGermplasm(this.gid);
	}

	@Override
	public void addListeners() {
		//No Implementation
	}

	@Override
	public void layoutComponents() {
		for (LotDetails lot : this.lotDetails) {
			this.addComponent(new LotDetailsViewComponent(lot));
		}
	}

	@Override
	public void updateLabels() {
		//No Implementation
	}

	@Override
	public void attach() {
		super.attach();
		this.updateLabels();
	}
}
