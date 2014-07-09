
package org.generationcp.browser.cross.study.traitdonors.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.generationcp.browser.application.Message;
import org.generationcp.browser.cross.study.commons.EnvironmentFilter;
import org.generationcp.browser.cross.study.traitdonors.main.listeners.TraitDonorButtonClickListener;
import org.generationcp.browser.cross.study.traitdonors.main.pojos.TraitItem;
import org.generationcp.browser.exception.GermplasmStudyBrowserException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.theme.Bootstrap;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
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
 * This accordion panel provides a Trait tree from which to select traits of interest. The traits
 * appear as selected into an adjacent selection table. This selected table is then caried through to
 * the next panel.
 * 
 * @author rebecca
 *
 */
@Configurable
public class PreselectTraitFilter extends AbsoluteLayout implements InitializingBean, InternationalizableComponent, ItemClickListener {

	private static final long serialVersionUID = 2143984475747491163L;

	private static final Logger log = Logger.getLogger(PreselectTraitFilter.class);

	public static final String NEXT_BUTTON_ID = "PreselectTraitFilter Next Button ID";

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	private TraitDonorsQueryMain mainScreen;
	private EnvironmentFilter nextScreen;

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
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		setHeight("450px");
		setWidth("980px");

	}

	private void initializeComponents() throws GermplasmStudyBrowserException {

		// --- TRAIT TREE : setup the table that provides perusal of the Crop Traits

		traitMap = new HashMap<String, StandardVariableReference>();

		lblSectionTitle = new Label(messageSource.getMessage(Message.GET_NUMERIC_VARIATES));

		traitTreeTable = new TreeTable();
		traitTreeTable.setImmediate(true);
		traitTreeTable.setHeight("360px");
		traitTreeTable.setWidth("440px");

		traitTreeTable.addContainerProperty("Trait", String.class, "");
		traitTreeTable.setItemCaptionPropertyId("trait_name");

		Object root = traitTreeTable.addItem(new Object[] {"All Traits"}, null);

		try {
			// Use the standard service to load the entire Ontology in tree form
			List<TraitClassReference> traitClasses = ontologyDataManager.getAllTraitGroupsHierarchy(true);

			// top level = two classes - Crop Traits and Research Traits
			for (TraitClassReference traitClassReference : traitClasses) {
				// we want to display crop traits
				if (traitClassReference.getId().intValue() == TermId.ONTOLOGY_TRAIT_CLASS.getId()) {
					for (TraitClassReference subReference : traitClassReference.getTraitClassChildren()) {
						
						// add trait groups
						Object traitGroup = traitTreeTable.addItem(new Object[] {subReference.getName()}, null);
						traitTreeTable.setParent(traitGroup, root);
						
						// add traits for each group
						for (PropertyReference propertyReference : subReference.getProperties()) {
							if (!propertyReference.getStandardVariables().isEmpty()) {
								Object trait = traitTreeTable.addItem(new Object[] {propertyReference.getName()}, null);
								traitTreeTable.setParent(trait, traitGroup);
								
								// for each trait, there are number of standard
								// variables - add them all
								for (StandardVariableReference svr : propertyReference.getStandardVariables()) {
									traitMap.put(svr.getName(), svr);
									Object sv = traitTreeTable.addItem(new Object[] {svr.getName()}, null);
									traitTreeTable.setChildrenAllowed(sv, false);
									traitTreeTable.setParent(sv, trait);
								}
							}
						}
					}
				}
			}
			// FIXME : what will happen to this?
		} catch (MiddlewareQueryException e) {
			log.error("Problem occurred loading the Ontology Tree");
			throw new GermplasmStudyBrowserException("Problem occurred loading the Ontology Tree", e);
		}

		traitTreeTable.setCollapsed(root, false);

		traitTreeTable.setSelectable(true);
		traitTreeTable.setMultiSelect(false);

		traitTreeTable.addListener(this);

		// --- SELECTED TRAITS : setup the table where selections are collected ---

		traitSelectTable = new Table();
		traitSelectTable.setImmediate(true);
		traitSelectTable.setHeight("360px");
		traitSelectTable.setWidth("480px");
		traitSelectTable.addItem("Testing");

		final BeanItemContainer<TraitItem> tableContainer = new BeanItemContainer<TraitItem>(TraitItem.class);
		traitSelectTable.setContainerDataSource(tableContainer);
		traitSelectTable.setVisibleColumns(new Object[] {"traitName", "stdVarName"});
		traitSelectTable.setColumnHeaders(new String[] {"Trait Name", "Standard Variable Name"});

		traitSelectTable.addListener(this);

		addComponent(lblSectionTitle, "top:10px;left:20px");
		addComponent(traitTreeTable, "top:50px;left:20px");
		addComponent(traitSelectTable, "top:50px;left:500px");

	}

	public void populateTraitsTables() throws GermplasmStudyBrowserException {

		initializeComponents();
		createButtonLayout();

	}

	private void createButtonLayout() {

		nextButton = new Button(messageSource.getMessage(Message.NEXT));
		nextButton.setWidth("80px");
		nextButton.setData(NEXT_BUTTON_ID);
		nextButton.addListener(new TraitDonorButtonClickListener(this));
		nextButton.addStyleName(Bootstrap.Buttons.PRIMARY.styleName());
		addComponent(nextButton, "top:420px;left:900px");
		updateLabels();

	}

	/*
	 * Extract trait ids from the selected table tree and send them through to the Environment Filter. We
	 * will only process locations that record these traits as measured.
	 * 
	 */
	public void nextButtonClickAction() {

		List<Integer> traitsList = extractSelectionsFromSelectTable();
		this.mainScreen.selectSecondTab();
		this.nextScreen.populateEnvironmentsTable(traitsList);

	}

	/**
	 * Activates when an item is selected in the trait browse tree table. The item is selected if it is a Standard Variable (no children in
	 * the tree).
	 * 
	 */
	@Override
	public void itemClick(ItemClickEvent event) {

		if (event.isDoubleClick() && event.getSource().equals(traitSelectTable)) {
			Object itemId = event.getItemId();
			traitSelectTable.removeItem(itemId);
		} else if (event.getSource().equals(traitTreeTable)) {
			// this block only operates on leaf nodes (no children)
			if (!traitTreeTable.hasChildren(event.getItemId())) {
				Item item = traitTreeTable.getItem(event.getItemId());
				Item parent = traitTreeTable.getItem(traitTreeTable.getParent(event.getItemId()));
				TraitItem ti = new TraitItem();
				ti.setTraitName(parent.toString());
				ti.setStdVarName(item.toString());
				traitSelectTable.addItem(ti);
			}
		}

	}
	
	/*
	 * Pulls the trait IDs from the Items in the Selected traits tree
	 */
	private List<Integer> extractSelectionsFromSelectTable() {
		List<Integer> selectedTraitIds = new ArrayList<Integer>();
		for (Object item : traitSelectTable.getItemIds()) {
			TraitItem ti = (TraitItem) item;
			StandardVariableReference svr = traitMap.get(ti.getStdVarName());
			selectedTraitIds.add(svr.getId());
		}
		return selectedTraitIds;
	}
}
