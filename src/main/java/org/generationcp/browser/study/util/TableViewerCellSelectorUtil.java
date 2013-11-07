package org.generationcp.browser.study.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFColor;
import org.generationcp.browser.study.pojos.CellCoordinate;
import org.generationcp.browser.study.pojos.CellCoordinateColorAssignment;

import com.vaadin.addon.colorpicker.ColorPicker;
import com.vaadin.addon.colorpicker.events.ColorChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.Window;

public class TableViewerCellSelectorUtil {

	private Table table;

	private String currentRow;
	private String currentColumn;

	private String previousRow;
	private String previousColumn;
	
	private String cssClassName;
	private Window contextWindow;
	private Boolean contextWindowDisplayed;
	private ColorPicker cp;
	
	private ArrayList<String> columnHeaders;
	private ArrayList<CellCoordinate> highlightedCellCoordinates;
	private ArrayList<String> customCSSClassNames;
	private ArrayList<CellCoordinateColorAssignment> cellCoordinateColorAssigments;
	
	private Object source;
	/**
	* Utility used to allow a user to select cell/s
	* by clicking, clicking + alt, clicking + shift
	* or clicking + shift + alt, and it also allows
	* a user to modify cell background colors
	* 
	* @param sourceTable
	*/
	
	public TableViewerCellSelectorUtil(Object source, Table sourceTable){
		this.source = source;
		table = sourceTable;
		columnHeaders = new ArrayList<String>();
		highlightedCellCoordinates = new ArrayList<CellCoordinate>();
		customCSSClassNames = new ArrayList<String>();
		cellCoordinateColorAssigments = new ArrayList<CellCoordinateColorAssignment>();
		contextWindowDisplayed = false;
		initialize();
		
		//To clear selected cells when table is re-sorted
		table.addListener(new Table.HeaderClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void headerClick(HeaderClickEvent event) {
				clearHighlightedCells();
			}

		});
		
		
	}
	
	public TableViewerCellSelectorUtil(Table sourceTable){
		table = sourceTable;
		columnHeaders = new ArrayList<String>();
		highlightedCellCoordinates = new ArrayList<CellCoordinate>();
		customCSSClassNames = new ArrayList<String>();
		cellCoordinateColorAssigments = new ArrayList<CellCoordinateColorAssignment>();
		contextWindowDisplayed = false;
		initialize();
	}
	
	private void initialize(){

		
		if(source instanceof Window){
			((Window) source).addComponent(new Label("<style> .v-table .v-table-cell-content-highlighted { background: #1B91E0; padding-bottom: -1px; border-bottom: 1px solid #146296; padding-right: -1px; border-right: 1px solid #146296; } </style>",Label.CONTENT_XHTML));	
			((Window) source).addComponent(new Label("<style> .v-table .v-table-cell-content-currentlyselected { background: #1B91E0; padding: -2px; border: 2px solid #3B25B3; } </style>",Label.CONTENT_XHTML));
		} else {
			((AbstractOrderedLayout) source).addComponent(new Label("<style> .v-table .v-table-cell-content-highlighted { background: #1B91E0; padding-bottom: -1px; border-bottom: 1px solid #146296; padding-right: -1px; border-right: 1px solid #146296; } </style>",Label.CONTENT_XHTML));	
			((AbstractOrderedLayout) source).addComponent(new Label("<style> .v-table .v-table-cell-content-currentlyselected { background: #1B91E0; padding: -2px; border: 2px solid #3B25B3; } </style>",Label.CONTENT_XHTML));			
		}
		
		
		
		Object[] columnHeadersObjectArray = table.getVisibleColumns();
		for(int i=0;i<columnHeadersObjectArray.length;i++){
			columnHeaders.add(columnHeadersObjectArray[i].toString());
		}
		
	    table.addListener(new ItemClickEvent.ItemClickListener(){
			private static final long serialVersionUID = 1L;
			public void itemClick(ItemClickEvent event) {
				
				//Remove previous context windows and color pickers
				if(contextWindowDisplayed){
					contextWindow.removeComponent(cp);
					if(source instanceof Window){
						((Window) source).getParent().getWindow().removeWindow(contextWindow);
					} else {
						table.getWindow().removeWindow(contextWindow);
					}
					contextWindowDisplayed = false;
				}
				
				
				//System.out.println("Clicked");
				//System.out.println("Row: "+event.getItemId());
				//System.out.println("Column: "+event.getPropertyId());
				//System.out.println("Ctrl Pressed: "+event.isCtrlKey());
				//System.out.println("Shift Pressed: "+event.isShiftKey());
				//System.out.println("");
				
				//table.getItem(event.getItemId()).getItemProperty(event.getPropertyId()).addStyleName("highlighted");
				
				if(event.getButton()==ItemClickEvent.BUTTON_RIGHT){
					if(highlightedCellCoordinates.size()==0){
						@SuppressWarnings("unused")
						CellCoordinate currentCellCoordinate = new CellCoordinate(currentColumn, currentRow);
						updateTableCellColors();
					}
					
					contextWindow = new Window();
					contextWindow.setCaption("Choose a color");
					contextWindow.setWidth("170px");
					contextWindow.setHeight("60px");

					//Computation doesn't work too well because table width is always 100.0 (a.k.a 100%) not pixels
					if((int) event.getClientX()>(table.getWidth()-contextWindow.getWidth()-50)){
						contextWindow.setPositionX((int) (event.getClientX()-contextWindow.getWidth()));
					} else {
						contextWindow.setPositionX(event.getClientX());
					}
					
					//Computation doesn't work too well because table height is always 100.0 (a.k.a 100%) not pixels
					if((int) event.getClientY()>(table.getHeight()-contextWindow.getHeight()-120)){
						contextWindow.setPositionY((int) (event.getClientY()-contextWindow.getHeight()));
					} else {
						contextWindow.setPositionY(event.getClientY());	
					}
					
					contextWindow.setResizable(false);
					
					cp = new ColorPicker("Select color for the cells");
					//cp.setPopupStyle(ColorPicker.PopupStyle.POPUP_SIMPLE);
					cp.setPosition(event.getClientX(), event.getClientY());
					
					
					
					contextWindow.addComponent(cp);
					
					if(source instanceof Window){
						//((Window) source).getApplication().getMainWindow().addWindow(contextWindow);
						//((Window) source).getApplication().getWindow(GermplasmStudyBrowserApplication.STUDY_WINDOW_NAME).addWindow(contextWindow);
						if(!contextWindowDisplayed)
							((Window) source).getParent().getWindow().addWindow(contextWindow);
						contextWindowDisplayed = true;
					} else {
						if(!contextWindowDisplayed)
							table.getWindow().addWindow(contextWindow);
						contextWindowDisplayed = true;
					}
					
					cp.addListener(new ColorPicker.ColorChangeListener(){
						private static final long serialVersionUID = 1L;
						public void colorChanged(ColorChangeEvent event) {
							cssClassName = addColor(event.getColor().getRed(), event.getColor().getGreen(), event.getColor().getBlue());
							applyColorToSelectedCells(cssClassName, event.getColor().getRed(), event.getColor().getGreen(), event.getColor().getBlue());
							if(source instanceof Window){
								((Window) source).getParent().removeWindow(contextWindow);
								contextWindowDisplayed = false;
							} else {
								table.getWindow().removeWindow(contextWindow);
								contextWindowDisplayed = false;
							}
						}
					});
					
				} else {
					previousRow = currentRow;
					previousColumn = currentColumn;
					
					currentRow = event.getItemId().toString();
					currentColumn = event.getPropertyId().toString();
	
					CellCoordinate currentCellCoordinate = new CellCoordinate(currentColumn, currentRow);
					
					//If control is not pressed remove previously highlighted cells
					if(!event.isCtrlKey()){
						clearAllHighlightedCoordinates();
					}
					
					//If shift is held while clicked, compute included cells
					if(event.isShiftKey()){
						addCoordinatesSelectedByShift();
					//Else, do regular adding of single cells	
					} else {
						highlightedCellCoordinates.add(currentCellCoordinate);
					}
					
					updateTableCellColors();
				}
			}
	    });
	    
	    
	    
	}
	
	
	
	/**
	* Removes any highlighted cells
	*/
	
	private void clearAllHighlightedCoordinates(){
		highlightedCellCoordinates.clear();
		updateTableCellColors();
	}
	
	
	
	/**
	* Add coordinates to "selected" cells when shift
	* is held, this also computes which cells are covered
	* from the initially selected cell to the one 
	* selected after
	*/
	
	private void addCoordinatesSelectedByShift(){
		List<Integer> itemIds = new ArrayList<Integer>();
		itemIds.addAll((Collection<Integer>) table.getItemIds());
		
		columnHeaders.clear();
		Object[] columnHeadersObjectArray = table.getVisibleColumns();
		for(int i=0;i<columnHeadersObjectArray.length;i++){
			columnHeaders.add(columnHeadersObjectArray[i].toString());
		}
		
		int indexOfCurrentColumn = columnHeaders.indexOf(currentColumn);
		int indexOfPreviousColumn = columnHeaders.indexOf(previousColumn);
		int indexOfCurrentRow = itemIds.indexOf(Integer.valueOf(currentRow));
		int indexOfPreviousRow = itemIds.indexOf(Integer.valueOf(previousRow));
		
		int minColumn = 0;
		int maxColumn = 0;
		int minRow = 0;
		int maxRow = 0;
		
		if(indexOfCurrentColumn>indexOfPreviousColumn){
			minColumn = indexOfPreviousColumn;
			maxColumn = indexOfCurrentColumn;
		} else {
			maxColumn = indexOfPreviousColumn;
			minColumn = indexOfCurrentColumn;
		}
		
		if(indexOfCurrentRow>indexOfPreviousRow){
			minRow = indexOfPreviousRow;
			maxRow = indexOfCurrentRow;
		} else {
			maxRow = indexOfPreviousRow;
			minRow = indexOfCurrentRow;
		}
		
		for(int x=minColumn;x<=maxColumn;x++){
			for(int y=minRow;y<=maxRow;y++){
				CellCoordinate currentCellCoordinate = new CellCoordinate(columnHeaders.get(x), itemIds.get(y));
				highlightedCellCoordinates.add(currentCellCoordinate);
			}
		}
	}

	
	
	/**
	* Contains logic to color table cells
	* and repaints the table
	*/
	
	private void updateTableCellColors(){
        table.setCellStyleGenerator(new CellStyleGenerator(){
			private static final long serialVersionUID = 1L;
			public String getStyle(Object itemId, Object propertyId) {

				Boolean isHighlighted = false;
				for(int i=0;i<highlightedCellCoordinates.size();i++){
					if(highlightedCellCoordinates.get(i).getX().equals(propertyId) && highlightedCellCoordinates.get(i).getY().equals(itemId.toString()))
						isHighlighted = true;
				}
				
				Boolean hasColorSet = false;
				for(int i=0;i<cellCoordinateColorAssigments.size();i++){
					if(cellCoordinateColorAssigments.get(i).getCellCoordinate().getX().equals(propertyId) && 
							cellCoordinateColorAssigments.get(i).getCellCoordinate().getY().equals(itemId.toString()) && 
							(!currentColumn.equals(propertyId) || !currentRow.equals(itemId.toString())) &&
							!isHighlighted
							){
						hasColorSet = true;
						//System.out.println("Setting cell style to - "+cellCoordinateColorAssigments.get(i).getCssClassName());
						return cellCoordinateColorAssigments.get(i).getCssClassName();
					}
				}
				
				if(hasColorSet == false){
					if(currentColumn!=null && currentColumn.equals(propertyId) && currentRow!=null && currentRow.equals(itemId.toString())){
						//System.out.println("Setting cell style to - currentlySelected");
						return "currentlyselected";
					} else {
						if(isHighlighted){
							//System.out.println("Setting cell style to - highlighted");
							return "highlighted";
						} else {
							//System.out.println("Setting cell style to - [null]");
							return null;
						}
					}
				}
				return null;
			} 
        });
		
		table.requestRepaint();
	}

	
	
	
	/**
	* Adds color to CSS and creates a color class
	* It checks if color is already added before adding 
	* a new class
	* 
	* @param R - red decimal (int) value
	* @param G - green decimal (int) value
	* @param B - blue decimal (int) value
	* 
	* @return - String css class name
	*/
	
	private String addColor(int R, int G, int B){
		String className = String.valueOf(R) + String.valueOf(G) + String.valueOf(B);
		if(customCSSClassNames.indexOf(className) == -1){
			customCSSClassNames.add(className);
			if(source instanceof Window){
				((Window) source).addComponent(new Label("<style> .v-table-cell-content-"+className+" { background: rgb("+R+","+G+","+B+");  border-bottom:1px solid #e0e0e0; } </style>",Label.CONTENT_XHTML));
			} else {
				((AbstractOrderedLayout) source).addComponent(new Label("<style> .v-table-cell-content-"+className+" { background: rgb("+R+","+G+","+B+"); border-bottom:1px solid #e0e0e0; } </style>",Label.CONTENT_XHTML));
			}
		}
		return className;
	}
	
	
	/**
	 * Keep track of cell coordinates and color
	 * selected by user, and call updateTableCellColors()
	 * to apply the colors
	 * 
	 * @param className
	 */
	private void applyColorToSelectedCells(String className, int redValue, int greenValue, int blueValue){
		for(int i=0;i<highlightedCellCoordinates.size();i++){
			Boolean inColorAssigmentsList = false;
			for(int x=0;x<cellCoordinateColorAssigments.size();x++){
				//if(cellCoordinateColorAssigments.get(x).getCellCoordinate().equals(highlightedCellCoordinates.get(i))){
				if(cellCoordinateColorAssigments.get(x).getCellCoordinate().getX().equals(highlightedCellCoordinates.get(i).getX()) &&
				   cellCoordinateColorAssigments.get(x).getCellCoordinate().getY().equals(highlightedCellCoordinates.get(i).getY()) 
						){
					inColorAssigmentsList = true;
					cellCoordinateColorAssigments.get(x).setCssClassName(className);
					cellCoordinateColorAssigments.get(x).setRedValue(redValue);
					cellCoordinateColorAssigments.get(x).setGreenValue(greenValue);
					cellCoordinateColorAssigments.get(x).setBlueValue(blueValue);
				}
					
			}
			if(inColorAssigmentsList == false){
				CellCoordinateColorAssignment cellCoordinateColorAssigment = new CellCoordinateColorAssignment(highlightedCellCoordinates.get(i), className, redValue, greenValue, blueValue);
				cellCoordinateColorAssigments.add(cellCoordinateColorAssigment);
			}
		}
		clearAllHighlightedCoordinates();
		updateTableCellColors();
	}
	
	public ArrayList<CellCoordinateColorAssignment> getCellCoordinateColorAssigments(){
		return cellCoordinateColorAssigments;
	}
	
	
	public XSSFColor getColor(String itemId, String propertyId){
		for(int i=0;i<cellCoordinateColorAssigments.size();i++){
			if(cellCoordinateColorAssigments.get(i).getCellCoordinate().getX().equals(propertyId) && cellCoordinateColorAssigments.get(i).getCellCoordinate().getY().equals(itemId.toString())){
				System.out.println("R:"+cellCoordinateColorAssigments.get(i).getRedValue()+" G:"+cellCoordinateColorAssigments.get(i).getGreenValue()+" B:"+cellCoordinateColorAssigments.get(i).getBlueValue());
				return new XSSFColor(new java.awt.Color(cellCoordinateColorAssigments.get(i).getRedValue(), cellCoordinateColorAssigments.get(i).getGreenValue(), cellCoordinateColorAssigments.get(i).getBlueValue()));
			}
		}
	    return null;
	}
	
	public void clearHighlightedCells(){
		highlightedCellCoordinates.clear();
		updateTableCellColors();
		currentRow = "";
		currentColumn = "";
	}
}
