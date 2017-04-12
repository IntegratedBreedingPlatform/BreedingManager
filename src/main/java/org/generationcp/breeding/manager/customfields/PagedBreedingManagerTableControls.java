package org.generationcp.breeding.manager.customfields;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

import com.jensjansson.pagedtable.PagedTable;
import com.jensjansson.pagedtable.PagedTableContainer;

/**
 * THis class contains the custom paging controls that the PagedBreedingManagerTable uses
 */
public class PagedBreedingManagerTableControls extends HorizontalLayout {
	private final PagedBreedingManagerTable pagedBreedingManagerTable;
	private final PagedBreedingManagerTable.EntrySelectSyncHandler entrySelectSyncHandler;

	public PagedBreedingManagerTableControls(final PagedBreedingManagerTable pagedBreedingManagerTable, final PagedBreedingManagerTable.EntrySelectSyncHandler entrySelectSyncHandler) {
		this.pagedBreedingManagerTable = pagedBreedingManagerTable;
		this.entrySelectSyncHandler = entrySelectSyncHandler;

		// Create label controls
		final Label itemsPerPageLabel = new Label("Items per page:");
		itemsPerPageLabel.setDebugId("itemsPerPageLabel");
		final Label pageLabel = new Label("Page:&nbsp;", 3);
		pageLabel.setDebugId("pageLabel");
		final Label separatorLabel = new Label("&nbsp;/&nbsp;", 3);
		separatorLabel.setDebugId("separatorLabel");
		final Label totalPagesLabel = new Label(String.valueOf(pagedBreedingManagerTable.getTotalAmountOfPages()), 3);
		totalPagesLabel.setDebugId("totalPagesLabel");

		pageLabel.setWidth(null);
		separatorLabel.setWidth(null);
		totalPagesLabel.setWidth(null);

		// Create input controls
		final TextField currentPageTextField = initCurrentPageTextField();
		final ComboBox itemsPerPageSelect = initItemsPerPageSelect();
		final PagingButtons pagingButtons = new PagingButtons();

		// set the styles of the label and input controls
		this.addStyles(itemsPerPageLabel, itemsPerPageSelect, pageLabel, currentPageTextField, separatorLabel, totalPagesLabel);

		// create the container layout
		this.createAndLayoutControlBar(itemsPerPageLabel, itemsPerPageSelect, pageLabel, currentPageTextField, separatorLabel,
						totalPagesLabel, pagingButtons);

		// action when pagedTable changes page
		pagedBreedingManagerTable.addListener(new PagedTable.PageChangeListener() {

			public void pageChanged(final PagedTable.PagedTableChangeEvent event) {
				pagingButtons.getFirst()
						.setEnabled(((PagedTableContainer) pagedBreedingManagerTable.getContainerDataSource()).getStartIndex() > 0);
				pagingButtons.getPrevious()
						.setEnabled(((PagedTableContainer) pagedBreedingManagerTable.getContainerDataSource()).getStartIndex() > 0);
				pagingButtons.getNext().setEnabled(((PagedTableContainer) pagedBreedingManagerTable.getContainerDataSource()).getStartIndex()
						< ((PagedTableContainer) pagedBreedingManagerTable.getContainerDataSource()).getRealSize() - pagedBreedingManagerTable.getPageLength());
				pagingButtons.getLast().setEnabled(((PagedTableContainer) pagedBreedingManagerTable.getContainerDataSource()).getStartIndex()
						< ((PagedTableContainer) pagedBreedingManagerTable.getContainerDataSource()).getRealSize() - pagedBreedingManagerTable.getPageLength());
				currentPageTextField.setValue(String.valueOf(pagedBreedingManagerTable.getCurrentPage()));
				totalPagesLabel.setValue(pagedBreedingManagerTable.getTotalAmountOfPages());
				itemsPerPageSelect.setValue(String.valueOf(pagedBreedingManagerTable.getPageLength()));
			}
		});
	}

	void createAndLayoutControlBar(final Label itemsPerPageLabel, final ComboBox itemsPerPageSelect, final Label pageLabel,
			final TextField currentPageTextField, final Label separatorLabel, final Label totalPagesLabel,
			final PagingButtons pagingButtons) {
		final HorizontalLayout pageSize = new HorizontalLayout();
		pageSize.setDebugId("pageSize");
		final HorizontalLayout pageManagement = new HorizontalLayout();
		pageManagement.setDebugId("pageManagement");

		pageSize.addComponent(itemsPerPageLabel);
		pageSize.addComponent(itemsPerPageSelect);
		pageSize.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
		pageSize.setComponentAlignment(itemsPerPageSelect, Alignment.MIDDLE_LEFT);
		pageSize.setSpacing(true);

		pageManagement.addComponent(pagingButtons.getFirst());
		pageManagement.addComponent(pagingButtons.getPrevious());
		pageManagement.addComponent(pageLabel);
		pageManagement.addComponent(currentPageTextField);
		pageManagement.addComponent(separatorLabel);
		pageManagement.addComponent(totalPagesLabel);
		pageManagement.addComponent(pagingButtons.getNext());
		pageManagement.addComponent(pagingButtons.getLast());
		pageManagement.setComponentAlignment(pagingButtons.getFirst(), Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(pagingButtons.getPrevious(), Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(pageLabel, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(currentPageTextField, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(separatorLabel, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(totalPagesLabel, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(pagingButtons.getNext(), Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(pagingButtons.getLast(), Alignment.MIDDLE_LEFT);
		pageManagement.setWidth(null);
		pageManagement.setSpacing(true);

		this.addComponent(pageSize);
		this.addComponent(pageManagement);
		this.setComponentAlignment(pageManagement, Alignment.MIDDLE_CENTER);
		this.setWidth("100%");
		this.setExpandRatio(pageSize, 1.0F);
	}

	void addStyles(final Label itemsPerPageLabel, final ComboBox itemsPerPageSelect, final Label pageLabel,
			final TextField currentPageTextField, final Label separatorLabel, final Label totalPagesLabel) {
		itemsPerPageLabel.addStyleName("pagedtable-itemsperpagecaption");
		itemsPerPageSelect.addStyleName("pagedtable-itemsperpagecombobox");
		pageLabel.addStyleName("pagedtable-pagecaption");
		currentPageTextField.addStyleName("pagedtable-pagefield");
		separatorLabel.addStyleName("pagedtable-separator");
		totalPagesLabel.addStyleName("pagedtable-total");

		itemsPerPageLabel.addStyleName("pagedtable-label");
		itemsPerPageSelect.addStyleName("pagedtable-combobox");
		pageLabel.addStyleName("pagedtable-label");
		currentPageTextField.addStyleName("pagedtable-label");
		separatorLabel.addStyleName("pagedtable-label");
		totalPagesLabel.addStyleName("pagedtable-label");
	}

	TextField initCurrentPageTextField() {
		final TextField currentPageTextField = new TextField();
		currentPageTextField.setDebugId("currentPageTextField");
		currentPageTextField.setValue(String.valueOf(pagedBreedingManagerTable.getCurrentPage()));
		currentPageTextField.setStyleName("small");
		currentPageTextField.setImmediate(true);
		currentPageTextField.setWidth("30px");

		// this new handle will set to last previous valid value when set to non numeric
		currentPageTextField.addListener(new Property.ValueChangeListener() {

			String lastValue = "1";

			@Override
			public void valueChange(final Property.ValueChangeEvent valueChangeEvent) {
				final String value = (String) currentPageTextField.getValue();

				if (!StringUtils.isNumeric(value)) {
					currentPageTextField.setValue(lastValue);
					return;
				}

				lastValue = value;
				pagedBreedingManagerTable.setCurrentPage(Integer.valueOf(value));
				PagedBreedingManagerTableControls.this.entrySelectSyncHandler.dispatch();
			}
		});

		return currentPageTextField;
	}

	ComboBox initItemsPerPageSelect() {
		final ComboBox itemsPerPageSelect = new ComboBox();
		itemsPerPageSelect.setDebugId("itemsPerPageSelect");
		itemsPerPageSelect.setDebugId("itemsPerPageSelect");

		for (final String item : new String[] {"5", "10", "15", "20", "25"}) {
			itemsPerPageSelect.addItem(item);
		}

		itemsPerPageSelect.setImmediate(true);
		itemsPerPageSelect.setNullSelectionAllowed(false);
		itemsPerPageSelect.setWidth("50px");
		itemsPerPageSelect.select(String.valueOf(pagedBreedingManagerTable.getPageLength()));

		itemsPerPageSelect.addListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(final Property.ValueChangeEvent valueChangeEvent) {
				pagedBreedingManagerTable.setPageLength(Integer.valueOf(String.valueOf(valueChangeEvent.getProperty().getValue())));
				pagedBreedingManagerTable.updateBatchsize();

				PagedBreedingManagerTableControls.this.entrySelectSyncHandler.dispatch();
			}
		});
		return itemsPerPageSelect;
	}

	class PagingButtons {

		private Button first;
		private Button previous;
		private Button next;
		private Button last;

		public Button getFirst() {
			return first;
		}

		public Button getPrevious() {
			return previous;
		}

		public Button getNext() {
			return next;
		}

		public Button getLast() {
			return last;
		}

		public PagingButtons() {
			first = new Button("<<", new Button.ClickListener() {

				private static final long serialVersionUID = -355520120491283992L;

				public void buttonClick(final Button.ClickEvent event) {
					pagedBreedingManagerTable.setCurrentPage(0);
					PagedBreedingManagerTableControls.this.entrySelectSyncHandler.dispatch();

				}
			});
			previous = new Button("<", new Button.ClickListener() {

				private static final long serialVersionUID = -355520120491283992L;

				public void buttonClick(final Button.ClickEvent event) {
					pagedBreedingManagerTable.previousPage();
					PagedBreedingManagerTableControls.this.entrySelectSyncHandler.dispatch();

				}
			});
			next = new Button(">", new Button.ClickListener() {

				private static final long serialVersionUID = -1927138212640638452L;

				public void buttonClick(final Button.ClickEvent event) {
					pagedBreedingManagerTable.nextPage();
					PagedBreedingManagerTableControls.this.entrySelectSyncHandler.dispatch();

				}
			});
			last = new Button(">>", new Button.ClickListener() {

				private static final long serialVersionUID = -355520120491283992L;

				public void buttonClick(final Button.ClickEvent event) {
					pagedBreedingManagerTable.setCurrentPage(pagedBreedingManagerTable.getTotalAmountOfPages());
					PagedBreedingManagerTableControls.this.entrySelectSyncHandler.dispatch();
				}
			});

			first.addStyleName("pagedtable-first");
			previous.addStyleName("pagedtable-previous");
			next.addStyleName("pagedtable-next");
			last.addStyleName("pagedtable-last");

			first.addStyleName("pagedtable-button");
			previous.addStyleName("pagedtable-button");
			next.addStyleName("pagedtable-button");
			last.addStyleName("pagedtable-button");
		}
	}

}
