
package org.generationcp.breeding.manager.cross.study.traitdonors.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.cross.study.commons.EnvironmentFilter;
import org.generationcp.breeding.manager.cross.study.traitdonors.main.listeners.TraitDonorButtonClickListener;
import org.generationcp.breeding.manager.cross.study.traitdonors.main.pojos.TraitItem;
import org.generationcp.breeding.manager.exception.GermplasmStudyBrowserException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.commons.vaadin.util.MessageNotifier;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;

/**
 * This accordion panel provides a Trait tree from which to select traits of interest. The traits appear as selected into an adjacent
 * selection table. This selected table is then caried through to the next panel.
 * 
 * @author rebecca
 * 
 */
@Configurable
public class PreselectTraitFilter extends AbsoluteLayout implements InitializingBean, InternationalizableComponent, ItemClickListener {

	private static final long serialVersionUID = 2143984475747491163L;

	private static final Logger LOG = LoggerFactory.getLogger(PreselectTraitFilter.class);

	public static final String NEXT_BUTTON_ID = "PreselectTraitFilter Next Button ID";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	private final TraitDonorsQueryMain mainScreen;
	private final EnvironmentFilter nextScreen;

	private Button nextButton;

	private Label lblSectionTitle;
	private TreeTable traitTreeTable;
	private Table traitSelectTable;

	private Map<String, StandardVariableReference> traitMap;

	public PreselectTraitFilter(TraitDonorsQueryMain mainScreen, EnvironmentFilter nextScreen) {

		this.mainScreen = mainScreen;
		this.nextScreen = nextScreen;

	}

	@Override
	public void updateLabels() {
		// not implemented
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		this.setHeight("450px");
		this.setWidth("980px");

	}

	private void initializeComponents() throws GermplasmStudyBrowserException {

		// --- TRAIT TREE : setup the table that provides perusal of the Crop Traits

		this.traitMap = new HashMap<String, StandardVariableReference>();

		this.lblSectionTitle = new Label(this.messageSource.getMessage(Message.GET_NUMERIC_VARIATES));

		this.traitTreeTable = new TreeTable();
		this.traitTreeTable.setImmediate(true);
		this.traitTreeTable.setHeight("360px");
		this.traitTreeTable.setWidth("470px");

		this.traitTreeTable.addContainerProperty("Trait", String.class, "");
		this.traitTreeTable.setItemCaptionPropertyId("trait_name");

		Object root = this.traitTreeTable.addItem(new Object[] {"All Traits"}, null);

		try {
			// Use the standard service to load the entire Ontology in tree form
			List<TraitClassReference> traitClasses = this.ontologyDataManager.getAllTraitGroupsHierarchy(true);

			// top level = two classes - Crop Traits and Research Traits
			for (TraitClassReference traitClassReference : traitClasses) {
				// we want to display crop traits
				if (traitClassReference.getId().intValue() == TermId.ONTOLOGY_TRAIT_CLASS.getId()) {
					for (TraitClassReference subReference : traitClassReference.getTraitClassChildren()) {

						// add trait groups
						Object traitGroup = this.traitTreeTable.addItem(new Object[] {subReference.getName()}, null);
						this.traitTreeTable.setParent(traitGroup, root);

						// add traits for each group
						for (PropertyReference propertyReference : subReference.getProperties()) {
							if (!propertyReference.getStandardVariables().isEmpty()) {
								Object trait = this.traitTreeTable.addItem(new Object[] {propertyReference.getName()}, null);
								this.traitTreeTable.setParent(trait, traitGroup);

								// for each trait, there are number of standard
								// variables - add them all
								for (StandardVariableReference svr : propertyReference.getStandardVariables()) {
									this.traitMap.put(svr.getName(), svr);
									Object sv = this.traitTreeTable.addItem(new Object[] {svr.getName()}, null);
									this.traitTreeTable.setChildrenAllowed(sv, false);
									this.traitTreeTable.setParent(sv, trait);
								}
							}
						}
					}
				}
			}
			// FIXME : what will happen to this?
		} catch (MiddlewareQueryException e) {
			PreselectTraitFilter.LOG.error("Problem occurred loading the Ontology Tree");
			throw new GermplasmStudyBrowserException("Problem occurred loading the Ontology Tree", e);
		}

		this.traitTreeTable.setCollapsed(root, false);

		this.traitTreeTable.setSelectable(true);
		this.traitTreeTable.setMultiSelect(false);

		this.traitTreeTable.addListener(this);

		// --- SELECTED TRAITS : setup the table where selections are collected ---

		this.traitSelectTable = new Table();
		this.traitSelectTable.setImmediate(true);
		this.traitSelectTable.setHeight("360px");
		this.traitSelectTable.setWidth("480px");
		this.traitSelectTable.addItem("Testing");

		final BeanItemContainer<TraitItem> tableContainer = new BeanItemContainer<TraitItem>(TraitItem.class);
		this.traitSelectTable.setContainerDataSource(tableContainer);
		this.traitSelectTable.setVisibleColumns(new Object[] {"traitName", "stdVarName"});
		this.traitSelectTable.setColumnHeaders(new String[] {"Trait Name", "Standard Variable Name"});

		this.traitSelectTable.addListener(this);

		this.addComponent(this.lblSectionTitle, "top:10px;left:20px");
		this.addComponent(this.traitTreeTable, "top:50px;left:20px");
		this.addComponent(this.traitSelectTable, "top:50px;left:500px");

	}

	public void populateTraitsTables() throws GermplasmStudyBrowserException {

		this.initializeComponents();
		this.createButtonLayout();
	}

	private void createButtonLayout() {

		this.nextButton = new Button(this.messageSource.getMessage(Message.NEXT));
		this.nextButton.setWidth("80px");
		this.nextButton.setData(PreselectTraitFilter.NEXT_BUTTON_ID);
		this.nextButton.addListener(new TraitDonorButtonClickListener(this));
		this.nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		this.addComponent(this.nextButton, "top:420px;left:460px");
		this.updateLabels();

	}

	/*
	 * Extract trait ids from the selected table tree and send them through to the Environment Filter. We will only process locations that
	 * record these traits as measured.
	 */
	public void nextButtonClickAction() {

		List<Integer> traitsList = this.extractSelectionsFromSelectTable();
		if (traitsList.isEmpty()) {
			MessageNotifier.showWarning(this.getWindow(), this.messageSource.getMessage(Message.WARNING),
					this.messageSource.getMessage(Message.NO_AVAILABLE_TRAIT_VARIABLE));
		} else {
			this.mainScreen.selectSecondTab();
			this.nextScreen.populateEnvironmentsTable(traitsList);
		}

	}

	/**
	 * Activates when an item is selected in the trait browse tree table. The item is selected if it is a Standard Variable (no children in
	 * the tree).
	 * 
	 */
	@Override
	public void itemClick(ItemClickEvent event) {

		if (event.isDoubleClick() && event.getSource().equals(this.traitSelectTable)) {
			Object itemId = event.getItemId();
			this.traitSelectTable.removeItem(itemId);

		} else if ((event.getSource().equals(this.traitTreeTable)) && (!this.traitTreeTable.hasChildren(event.getItemId()))) {
			// this block only operates on leaf nodes (no children)
			Item item = this.traitTreeTable.getItem(event.getItemId());
			Item parent = this.traitTreeTable.getItem(this.traitTreeTable.getParent(event.getItemId()));
			TraitItem ti = new TraitItem();
			ti.setTraitName(parent.toString());
			ti.setStdVarName(item.toString());
			this.traitSelectTable.addItem(ti);
		}
	}

	/*
	 * Pulls the trait IDs from the Items in the Selected traits tree
	 */
	protected List<Integer> extractSelectionsFromSelectTable() {
		List<Integer> selectedTraitIds = new ArrayList<Integer>();
		for (Object item : this.traitSelectTable.getItemIds()) {
			TraitItem ti = (TraitItem) item;
			StandardVariableReference svr = this.traitMap.get(ti.getStdVarName());
			if (svr != null) {
				selectedTraitIds.add(svr.getId());
			}
		}
		return selectedTraitIds;
	}

	protected void setMessageSource(SimpleResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	protected void setTraitSelectTable(Table traitSelectTable) {
		this.traitSelectTable = traitSelectTable;
	}

	protected void setTraitMap(Map<String, StandardVariableReference> traitMap) {
		this.traitMap = traitMap;
	}

	public void setOntologyDataManager(OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

}
