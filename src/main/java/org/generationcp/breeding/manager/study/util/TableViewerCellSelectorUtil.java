
package org.generationcp.breeding.manager.study.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFColor;
import org.generationcp.breeding.manager.study.pojos.CellCoordinate;
import org.generationcp.breeding.manager.study.pojos.CellCoordinateColorAssignment;

import com.vaadin.addon.colorpicker.ColorPicker;
import com.vaadin.addon.colorpicker.events.ColorChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.Window;

public class TableViewerCellSelectorUtil {

	private final Table table;

	private String currentRow;
	private String currentColumn;

	private String previousRow;
	private String previousColumn;

	private String cssClassName;
	private Window contextWindow;
	private Boolean contextWindowDisplayed;
	private ColorPicker cp;

	private final ArrayList<String> columnHeaders;
	private final ArrayList<CellCoordinate> highlightedCellCoordinates;
	private final ArrayList<String> customCSSClassNames;
	private final ArrayList<CellCoordinateColorAssignment> cellCoordinateColorAssigments;

	private Object source;

	/**
	 * Utility used to allow a user to select cell/s by clicking, clicking + alt, clicking + shift or clicking + shift + alt, and it also
	 * allows a user to modify cell background colors
	 * 
	 * @param sourceTable
	 */
	public TableViewerCellSelectorUtil(Object source, Table sourceTable) {
		this.source = source;
		this.table = sourceTable;
		this.columnHeaders = new ArrayList<String>();
		this.highlightedCellCoordinates = new ArrayList<CellCoordinate>();
		this.customCSSClassNames = new ArrayList<String>();
		this.cellCoordinateColorAssigments = new ArrayList<CellCoordinateColorAssignment>();
		this.contextWindowDisplayed = false;
		this.initialize();

		// To clear selected cells when table is re-sorted
		this.table.addListener(new Table.HeaderClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void headerClick(HeaderClickEvent event) {
				TableViewerCellSelectorUtil.this.clearHighlightedCells();
			}

		});

	}

	public TableViewerCellSelectorUtil(Table sourceTable) {
		this.table = sourceTable;
		this.columnHeaders = new ArrayList<String>();
		this.highlightedCellCoordinates = new ArrayList<CellCoordinate>();
		this.customCSSClassNames = new ArrayList<String>();
		this.cellCoordinateColorAssigments = new ArrayList<CellCoordinateColorAssignment>();
		this.contextWindowDisplayed = false;
		this.initialize();
	}

	private void initialize() {

		if (this.source instanceof Window) {
			((Window) this.source)
					.addComponent(new Label(
							"<style> .v-table .v-table-cell-content-highlighted { background: #1B91E0; padding-bottom: -1px; border-bottom: 1px solid #146296; padding-right: -1px; border-right: 1px solid #146296; } </style>",
							Label.CONTENT_XHTML));
			((Window) this.source)
					.addComponent(new Label(
							"<style> .v-table .v-table-cell-content-currentlyselected { background: #1B91E0; padding: -2px; border: 2px solid #3B25B3; } </style>",
							Label.CONTENT_XHTML));
		} else {
			((AbstractOrderedLayout) this.source)
					.addComponent(new Label(
							"<style> .v-table .v-table-cell-content-highlighted { background: #1B91E0; padding-bottom: -1px; border-bottom: 1px solid #146296; padding-right: -1px; border-right: 1px solid #146296; } </style>",
							Label.CONTENT_XHTML));
			((AbstractOrderedLayout) this.source)
					.addComponent(new Label(
							"<style> .v-table .v-table-cell-content-currentlyselected { background: #1B91E0; padding: -2px; border: 2px solid #3B25B3; } </style>",
							Label.CONTENT_XHTML));
		}

		Object[] columnHeadersObjectArray = this.table.getVisibleColumns();
		for (int i = 0; i < columnHeadersObjectArray.length; i++) {
			this.columnHeaders.add(columnHeadersObjectArray[i].toString());
		}

		this.table.addListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void itemClick(ItemClickEvent event) {

				// Remove previous context windows and color pickers
				if (TableViewerCellSelectorUtil.this.contextWindowDisplayed) {
					TableViewerCellSelectorUtil.this.contextWindow.removeComponent(TableViewerCellSelectorUtil.this.cp);
					if (TableViewerCellSelectorUtil.this.source instanceof Window) {
						((Window) TableViewerCellSelectorUtil.this.source).getParent().getWindow()
								.removeWindow(TableViewerCellSelectorUtil.this.contextWindow);
					} else {
						TableViewerCellSelectorUtil.this.table.getWindow().removeWindow(TableViewerCellSelectorUtil.this.contextWindow);
					}
					TableViewerCellSelectorUtil.this.contextWindowDisplayed = false;
				}

				if (event.getButton() == ClickEvent.BUTTON_RIGHT) {
					if (TableViewerCellSelectorUtil.this.highlightedCellCoordinates.size() == 0) {
						@SuppressWarnings("unused")
						CellCoordinate currentCellCoordinate =
								new CellCoordinate(TableViewerCellSelectorUtil.this.currentColumn,
										TableViewerCellSelectorUtil.this.currentRow);
						TableViewerCellSelectorUtil.this.updateTableCellColors();
					}

					TableViewerCellSelectorUtil.this.contextWindow = new Window();
					TableViewerCellSelectorUtil.this.contextWindow.setCaption("Choose a color");
					TableViewerCellSelectorUtil.this.contextWindow.setWidth("170px");
					TableViewerCellSelectorUtil.this.contextWindow.setHeight("60px");

					// Computation doesn't work too well because table width is always 100.0 (a.k.a 100%) not pixels
					if (event.getClientX() > TableViewerCellSelectorUtil.this.table.getWidth()
							- TableViewerCellSelectorUtil.this.contextWindow.getWidth() - 50) {
						TableViewerCellSelectorUtil.this.contextWindow.setPositionX((int) (event.getClientX() - TableViewerCellSelectorUtil.this.contextWindow
								.getWidth()));
					} else {
						TableViewerCellSelectorUtil.this.contextWindow.setPositionX(event.getClientX());
					}

					// Computation doesn't work too well because table height is always 100.0 (a.k.a 100%) not pixels
					if (event.getClientY() > TableViewerCellSelectorUtil.this.table.getHeight()
							- TableViewerCellSelectorUtil.this.contextWindow.getHeight() - 120) {
						TableViewerCellSelectorUtil.this.contextWindow.setPositionY((int) (event.getClientY() - TableViewerCellSelectorUtil.this.contextWindow
								.getHeight()));
					} else {
						TableViewerCellSelectorUtil.this.contextWindow.setPositionY(event.getClientY());
					}

					TableViewerCellSelectorUtil.this.contextWindow.setResizable(false);

					TableViewerCellSelectorUtil.this.cp = new ColorPicker("Select color for the cells");

					TableViewerCellSelectorUtil.this.cp.setPosition(event.getClientX(), event.getClientY());

					TableViewerCellSelectorUtil.this.contextWindow.addComponent(TableViewerCellSelectorUtil.this.cp);

					if (TableViewerCellSelectorUtil.this.source instanceof Window) {
						if (!TableViewerCellSelectorUtil.this.contextWindowDisplayed) {
							((Window) TableViewerCellSelectorUtil.this.source).getParent().getWindow()
									.addWindow(TableViewerCellSelectorUtil.this.contextWindow);
						}
						TableViewerCellSelectorUtil.this.contextWindowDisplayed = true;
					} else {
						if (!TableViewerCellSelectorUtil.this.contextWindowDisplayed) {
							TableViewerCellSelectorUtil.this.table.getWindow().addWindow(TableViewerCellSelectorUtil.this.contextWindow);
						}
						TableViewerCellSelectorUtil.this.contextWindowDisplayed = true;
					}

					TableViewerCellSelectorUtil.this.cp.addListener(new ColorPicker.ColorChangeListener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void colorChanged(ColorChangeEvent event) {
							TableViewerCellSelectorUtil.this.cssClassName =
									TableViewerCellSelectorUtil.this.addColor(event.getColor().getRed(), event.getColor().getGreen(), event
											.getColor().getBlue());
							TableViewerCellSelectorUtil.this.applyColorToSelectedCells(TableViewerCellSelectorUtil.this.cssClassName, event
									.getColor().getRed(), event.getColor().getGreen(), event.getColor().getBlue());
							if (TableViewerCellSelectorUtil.this.source instanceof Window) {
								((Window) TableViewerCellSelectorUtil.this.source).getParent().removeWindow(
										TableViewerCellSelectorUtil.this.contextWindow);
								TableViewerCellSelectorUtil.this.contextWindowDisplayed = false;
							} else {
								TableViewerCellSelectorUtil.this.table.getWindow().removeWindow(
										TableViewerCellSelectorUtil.this.contextWindow);
								TableViewerCellSelectorUtil.this.contextWindowDisplayed = false;
							}
						}
					});

				} else {
					TableViewerCellSelectorUtil.this.previousRow = TableViewerCellSelectorUtil.this.currentRow;
					TableViewerCellSelectorUtil.this.previousColumn = TableViewerCellSelectorUtil.this.currentColumn;

					TableViewerCellSelectorUtil.this.currentRow = event.getItemId().toString();
					TableViewerCellSelectorUtil.this.currentColumn = event.getPropertyId().toString();

					CellCoordinate currentCellCoordinate =
							new CellCoordinate(TableViewerCellSelectorUtil.this.currentColumn, TableViewerCellSelectorUtil.this.currentRow);

					// If control is not pressed remove previously highlighted cells
					if (!event.isCtrlKey()) {
						TableViewerCellSelectorUtil.this.clearAllHighlightedCoordinates();
					}

					// If shift is held while clicked, compute included cells
					if (event.isShiftKey()) {
						TableViewerCellSelectorUtil.this.addCoordinatesSelectedByShift();
						// Else, do regular adding of single cells
					} else {
						TableViewerCellSelectorUtil.this.highlightedCellCoordinates.add(currentCellCoordinate);
					}

					TableViewerCellSelectorUtil.this.updateTableCellColors();
				}
			}
		});

	}

	/**
	 * Removes any highlighted cells
	 */
	private void clearAllHighlightedCoordinates() {
		this.highlightedCellCoordinates.clear();
		this.updateTableCellColors();
	}

	/**
	 * Add coordinates to "selected" cells when shift is held, this also computes which cells are covered from the initially selected cell
	 * to the one selected after
	 */
	@SuppressWarnings("unchecked")
	private void addCoordinatesSelectedByShift() {
		List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<Integer>) this.table.getItemIds());

		this.columnHeaders.clear();
		Object[] columnHeadersObjectArray = this.table.getVisibleColumns();
		for (int i = 0; i < columnHeadersObjectArray.length; i++) {
			this.columnHeaders.add(columnHeadersObjectArray[i].toString());
		}

		int indexOfCurrentColumn = this.columnHeaders.indexOf(this.currentColumn);
		int indexOfPreviousColumn = this.columnHeaders.indexOf(this.previousColumn);
		int indexOfCurrentRow = itemIds.indexOf(Integer.valueOf(this.currentRow));
		int indexOfPreviousRow = itemIds.indexOf(Integer.valueOf(this.previousRow));

		int minColumn = 0;
		int maxColumn = 0;
		int minRow = 0;
		int maxRow = 0;

		if (indexOfCurrentColumn > indexOfPreviousColumn) {
			minColumn = indexOfPreviousColumn;
			maxColumn = indexOfCurrentColumn;
		} else {
			maxColumn = indexOfPreviousColumn;
			minColumn = indexOfCurrentColumn;
		}

		if (indexOfCurrentRow > indexOfPreviousRow) {
			minRow = indexOfPreviousRow;
			maxRow = indexOfCurrentRow;
		} else {
			maxRow = indexOfPreviousRow;
			minRow = indexOfCurrentRow;
		}

		for (int x = minColumn; x <= maxColumn; x++) {
			for (int y = minRow; y <= maxRow; y++) {
				CellCoordinate currentCellCoordinate = new CellCoordinate(this.columnHeaders.get(x), itemIds.get(y));
				this.highlightedCellCoordinates.add(currentCellCoordinate);
			}
		}
	}

	/**
	 * Contains logic to color table cells and repaints the table
	 */

	private void updateTableCellColors() {
		this.table.setCellStyleGenerator(new CellStyleGenerator() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getStyle(Object itemId, Object propertyId) {

				Boolean isHighlighted = false;
				for (int i = 0; i < TableViewerCellSelectorUtil.this.highlightedCellCoordinates.size(); i++) {
					if (TableViewerCellSelectorUtil.this.highlightedCellCoordinates.get(i).getX().equals(propertyId)
							&& TableViewerCellSelectorUtil.this.highlightedCellCoordinates.get(i).getY().equals(itemId.toString())) {
						isHighlighted = true;
					}
				}

				Boolean hasColorSet = false;
				for (int i = 0; i < TableViewerCellSelectorUtil.this.cellCoordinateColorAssigments.size(); i++) {
					if (TableViewerCellSelectorUtil.this.cellCoordinateColorAssigments.get(i).getCellCoordinate().getX().equals(propertyId)
							&& TableViewerCellSelectorUtil.this.cellCoordinateColorAssigments.get(i).getCellCoordinate().getY()
									.equals(itemId.toString())
							&& (!TableViewerCellSelectorUtil.this.currentColumn.equals(propertyId) || !TableViewerCellSelectorUtil.this.currentRow
									.equals(itemId.toString())) && !isHighlighted) {
						hasColorSet = true;
						return TableViewerCellSelectorUtil.this.cellCoordinateColorAssigments.get(i).getCssClassName();
					}
				}

				if (hasColorSet == false) {
					if (TableViewerCellSelectorUtil.this.currentColumn != null
							&& TableViewerCellSelectorUtil.this.currentColumn.equals(propertyId)
							&& TableViewerCellSelectorUtil.this.currentRow != null
							&& TableViewerCellSelectorUtil.this.currentRow.equals(itemId.toString())) {
						return "currentlyselected";
					} else {
						if (isHighlighted) {
							return "highlighted";
						} else {
							return null;
						}
					}
				}
				return null;
			}
		});

		this.table.requestRepaint();
	}

	/**
	 * Adds color to CSS and creates a color class It checks if color is already added before adding a new class
	 * 
	 * @param R - red decimal (int) value
	 * @param G - green decimal (int) value
	 * @param B - blue decimal (int) value
	 * 
	 * @return - String css class name
	 */

	private String addColor(int R, int G, int B) {
		String className = String.valueOf(R) + String.valueOf(G) + String.valueOf(B);
		if (this.customCSSClassNames.indexOf(className) == -1) {
			this.customCSSClassNames.add(className);
			if (this.source instanceof Window) {
				((Window) this.source).addComponent(new Label("<style> .v-table-cell-content-" + className + " { background: rgb(" + R
						+ "," + G + "," + B + ");  border-bottom:1px solid #e0e0e0; } </style>", Label.CONTENT_XHTML));
			} else {
				((AbstractOrderedLayout) this.source).addComponent(new Label("<style> .v-table-cell-content-" + className
						+ " { background: rgb(" + R + "," + G + "," + B + "); border-bottom:1px solid #e0e0e0; } </style>",
						Label.CONTENT_XHTML));
			}
		}
		return className;
	}

	/**
	 * Keep track of cell coordinates and color selected by user, and call updateTableCellColors() to apply the colors
	 *
	 * @param className
	 */
	private void applyColorToSelectedCells(String className, int redValue, int greenValue, int blueValue) {
		for (int i = 0; i < this.highlightedCellCoordinates.size(); i++) {
			Boolean inColorAssigmentsList = false;
			for (int x = 0; x < this.cellCoordinateColorAssigments.size(); x++) {
				if (this.cellCoordinateColorAssigments.get(x).getCellCoordinate().getX()
						.equals(this.highlightedCellCoordinates.get(i).getX())
						&& this.cellCoordinateColorAssigments.get(x).getCellCoordinate().getY()
								.equals(this.highlightedCellCoordinates.get(i).getY())) {
					inColorAssigmentsList = true;
					this.cellCoordinateColorAssigments.get(x).setCssClassName(className);
					this.cellCoordinateColorAssigments.get(x).setRedValue(redValue);
					this.cellCoordinateColorAssigments.get(x).setGreenValue(greenValue);
					this.cellCoordinateColorAssigments.get(x).setBlueValue(blueValue);
				}

			}
			if (inColorAssigmentsList == false) {
				CellCoordinateColorAssignment cellCoordinateColorAssigment =
						new CellCoordinateColorAssignment(this.highlightedCellCoordinates.get(i), className, redValue, greenValue,
								blueValue);
				this.cellCoordinateColorAssigments.add(cellCoordinateColorAssigment);
			}
		}
		this.clearAllHighlightedCoordinates();
		this.updateTableCellColors();
	}

	public ArrayList<CellCoordinateColorAssignment> getCellCoordinateColorAssigments() {
		return this.cellCoordinateColorAssigments;
	}

	public XSSFColor getColor(String itemId, String propertyId) {
		for (int i = 0; i < this.cellCoordinateColorAssigments.size(); i++) {
			if (this.cellCoordinateColorAssigments.get(i).getCellCoordinate().getX().equals(propertyId)
					&& this.cellCoordinateColorAssigments.get(i).getCellCoordinate().getY().equals(itemId.toString())) {
				return new XSSFColor(
						new java.awt.Color(this.cellCoordinateColorAssigments.get(i).getRedValue(), this.cellCoordinateColorAssigments.get(
								i).getGreenValue(), this.cellCoordinateColorAssigments.get(i).getBlueValue()));
			}
		}
		return null;
	}

	public void clearHighlightedCells() {
		this.highlightedCellCoordinates.clear();
		this.updateTableCellColors();
		this.currentRow = "";
		this.currentColumn = "";
	}
}
