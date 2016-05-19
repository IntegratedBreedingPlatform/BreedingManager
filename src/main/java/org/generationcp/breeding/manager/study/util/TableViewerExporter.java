
package org.generationcp.breeding.manager.study.util;

import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.generationcp.breeding.manager.study.TableViewerDatasetTable;

import com.vaadin.ui.Button;

public class TableViewerExporter {

	private final TableViewerDatasetTable table;
	private final TableViewerCellSelectorUtil tableViewerCellSelectorUtil;

	public TableViewerExporter(TableViewerDatasetTable table, TableViewerCellSelectorUtil tableViewerCellSelectorUtil) {
		this.table = table;
		this.tableViewerCellSelectorUtil = tableViewerCellSelectorUtil;
	}

	public FileOutputStream exportToExcel(String filename) throws DatasetExporterException {

		// create workbook
		Workbook workbook = new XSSFWorkbook();
		CellStyle cellStyleForObservationSheet = workbook.createCellStyle();

		// Create first sheet
		Sheet sheet1 = workbook.createSheet("Sheet 1");

		// Prepare data
		ArrayList<String> columnId = new ArrayList<String>();
		ArrayList<String> columnHeaders = new ArrayList<String>();
		Object[] columnHeadersObjectArray = this.table.getVisibleColumns();
		Object[] columnHeadersStringArray = this.table.getColumnHeaders();
		for (int x = 0; x < columnHeadersObjectArray.length; x++) {
			columnId.add(columnHeadersObjectArray[x].toString());
			columnHeaders.add(columnHeadersStringArray[x].toString());
		}
		Object tableItemIds[] = this.table.getItemIds().toArray();

		// Create headers row, and populate with data
		XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(171, 171, 171)));
		Font labelFont = workbook.createFont();
		labelFont.setColor(IndexedColors.BLACK.getIndex());
		labelFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(labelFont);

		Row headerRow = sheet1.createRow(0);
		for (int x = 0; x < columnHeaders.size(); x++) {
			headerRow.createCell(x).setCellValue(columnHeaders.get(x).toString());
			headerRow.getCell(x).setCellStyle(headerStyle);
		}

		// Traverse through table, and create rows/columns and populate with data
		ArrayList<XSSFColor> cellColor = new ArrayList<XSSFColor>();
		ArrayList<XSSFCellStyle> cellStyle = new ArrayList<XSSFCellStyle>();

		XSSFColor currentColor;

		Row rows[] = new Row[tableItemIds.length];

		for (int y = 0; y < tableItemIds.length; y++) {
			rows[y] = sheet1.createRow(y + 1);
			for (int x = 0; x < columnHeaders.size(); x++) {
				String stringValue = "";
				if (this.table.getItem(tableItemIds[y]).getItemProperty(columnId.get(x)).getValue() instanceof Button) {
					stringValue =
							((Button) this.table.getItem(tableItemIds[y]).getItemProperty(columnId.get(x)).getValue()).getCaption()
									.toString();
				} else {
					stringValue = this.table.getItem(tableItemIds[y]).getItemProperty(columnId.get(x)).toString();
				}

				// set as number if value is numeric
				if (NumberUtils.isNumber(stringValue)) {
					if (NumberUtils.isDigits(stringValue)) { // integer
						rows[y].createCell(x).setCellValue(Long.valueOf(stringValue));
					} else {
						rows[y].createCell(x).setCellValue(Double.valueOf(stringValue));
					}

				} else {
					rows[y].createCell(x).setCellValue(stringValue);
				}

				currentColor = this.tableViewerCellSelectorUtil.getColor(tableItemIds[y].toString(), columnId.get(x).toString());
				if (currentColor != null) {

					cellColor.add(currentColor);

					cellStyle.add((XSSFCellStyle) workbook.createCellStyle());
					cellStyle.get(cellStyle.size() - 1).setFillPattern(CellStyle.SOLID_FOREGROUND);
					cellStyle.get(cellStyle.size() - 1).setFillForegroundColor(cellColor.get(cellColor.size() - 1));
					rows[y].getCell(x).setCellStyle(cellStyle.get(cellStyle.size() - 1));

				}
			}

		}

		try {
			// write the excel file
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			workbook.write(fileOutputStream);
			fileOutputStream.close();
			return fileOutputStream;
		} catch (Exception ex) {
			throw new DatasetExporterException("Error with writing to: " + filename, ex);
		}
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}
}
