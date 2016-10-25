
package org.generationcp.breeding.manager.listmanager;

import org.generationcp.breeding.manager.application.BreedingManagerLayout;
import org.generationcp.breeding.manager.application.Message;
import org.generationcp.breeding.manager.customfields.ListSelectorComponent;
import org.generationcp.breeding.manager.listeners.ListTreeActionsListener;
import org.generationcp.breeding.manager.listimport.GermplasmImportMain;
import org.generationcp.breeding.manager.listimport.GermplasmImportPopupSource;
import org.generationcp.commons.constant.ListTreeState;
import org.generationcp.commons.exceptions.InternationalizableException;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.commons.vaadin.util.SaveTreeStateListener;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
public class ListSelectionComponent extends VerticalLayout implements InternationalizableComponent, InitializingBean,
		BreedingManagerLayout, ListTreeActionsListener, GermplasmImportPopupSource {

	private static final Logger LOG = LoggerFactory.getLogger(ListSelectionComponent.class);

	private static final long serialVersionUID = -383145225475654748L;

	@Autowired
	private SimpleResourceBundleMessageSource messageSource;

	private final ListManagerMain source;

	private ListSelectionLayout listSelectionLayout;
	private ListManagerTreeComponent listTreeComponent;
	private ListSearchComponent listSearchComponent;
	private SaveTreeStateListener treeStateSaver;

	private final Integer selectedListId;

	public ListSelectionComponent(final ListManagerMain source, final Integer listId) {
		super();
		this.source = source;
		this.selectedListId = listId;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instantiateComponents();
		initializeValues();
		addListeners();
		layoutComponents();
	}

	@Override
	public void updateLabels() {
		// No op
	}

	@Override
	public void instantiateComponents() {
		listSelectionLayout = new ListSelectionLayout(source, selectedListId);
		listTreeComponent = new ListManagerTreeComponent(this, selectedListId);
		listSearchComponent = new ListSearchComponent(source, listSelectionLayout);
	}

	@Override
	public void initializeValues() {
		// No op
	}

	@Override
	public void addListeners() {
		// No op
	}

	@Override
	public void layoutComponents() {
		this.removeAllComponents();
		addComponent(listSelectionLayout);
		this.addStyleName("list-selection-component");
		listSelectionLayout.addStyleName("list-selection-layout");
	}

	@Override
	public void studyClicked(final GermplasmList list) {
		try {
			listSelectionLayout.createListDetailsTab(list.getId());
		} catch (final MiddlewareQueryException e) {
			LOG.error("Error in displaying germplasm list details.", e);
			throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_CREATING_GERMPLASMLIST_DETAILS_WINDOW);
		}
	}

	@Override
	public void updateUIForRenamedList(final GermplasmList list, final String newName) {
		this.listSelectionLayout.renameTab(list.getId(), newName);
	}

	public ListSelectionLayout getListDetailsLayout() {
		return listSelectionLayout;
	}

	public ListManagerTreeComponent getListTreeComponent() {
		return listTreeComponent;
	}

	private Window launchListSelectionWindow(final Window window, final Component content, final String caption) {

		final CssLayout layout = new CssLayout();
		layout.setDebugId("layout");
		layout.setMargin(true);
		layout.setWidth("100%");
		layout.setHeight("490px");

		layout.addComponent(content);

		final BaseSubWindow popupWindow = new BaseSubWindow();
		popupWindow.setDebugId("popupWindow");
		popupWindow.setWidth("900px");
		popupWindow.setHeight("550px");
		popupWindow.setModal(true);
		popupWindow.setResizable(false);
		popupWindow.center();
		popupWindow.setCaption(caption);
		popupWindow.setContent(layout);
		popupWindow.addStyleName(Reindeer.WINDOW_LIGHT);
		popupWindow.addStyleName("lm-list-manager-popup");

		if (caption.equals(messageSource.getMessage(Message.SEARCH_FOR_LISTS))) {
			popupWindow.setOverrideFocus(true);
			listSearchComponent.focusOnSearchField();
		}

		window.addWindow(popupWindow);

		return popupWindow;
	}

	public void openListBrowseDialog() {
		listTreeComponent.showAddRenameFolderSection(false);
		treeStateSaver =
				new SaveTreeStateListener((TreeTable) listTreeComponent.getGermplasmListSource(), ListTreeState.GERMPLASM_LIST.name(),
						ListSelectorComponent.LISTS);
		launchListSelectionWindow(getWindow(), listTreeComponent, messageSource.getMessage(Message.BROWSE_FOR_LISTS)).addListener(
				treeStateSaver);

		listTreeComponent.reinitializeTree(false);

	}

	public void openListSearchDialog() {
		launchListSelectionWindow(getWindow(), listSearchComponent, messageSource.getMessage(Message.SEARCH_FOR_LISTS));
	}

	@Override
	public void folderClicked(final GermplasmList list) {
		// do nothing
	}

	public void showNodeOnTree(final Integer listId) {
		listTreeComponent.setListId(listId);
		listTreeComponent.createTree();
	}

	public ListSearchComponent getListSearchComponent() {
		return listSearchComponent;
	}

	@Override
	public void openSavedGermplasmList(final GermplasmList germplasmList) {
		studyClicked(germplasmList);
	}

	@Override
	public void refreshListTreeAfterListImport() {
		listTreeComponent.refreshComponent();
	}

	@Override
	public Window getParentWindow() {
		return getWindow();
	}

}
