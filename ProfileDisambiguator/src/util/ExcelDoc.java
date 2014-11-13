/**
 * Copyright (c) 2014 [National University of Singapore]
 * @Developer Karthick [karthyuom@gmail.com]
 */

package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

/**
 * ExcelDoc has ability to record any data as table in fashionable manner. It
 * can be used for later analysis. It used Apache POI API for this purpose.
 * path: path where the file needs to be saved. currentRow: keep records of
 * currently written row in a specific excel sheet.
 * */
public class ExcelDoc {

    private String path;
    private String fileName;
    private HSSFWorkbook workbook; // class from parent API
    private HSSFSheet sheet; // class from parent API
    private int currentRow;

    /**
     * Constructor takes two arguments and initializes class attributes.
     * 
     * @param path
     *            - path where the file needs to be saved.
     * @param fileName
     *            - name of an excel file.
     */
    public ExcelDoc(String path, String fileName) {
	this.path = path;
	this.fileName = fileName;
	workbook = new HSSFWorkbook();
	this.currentRow = 0;
    }

    /**
     * This method creates excel sheet in existing excel file
     * 
     * @param sheetName
     *            - name of excel sheet
     * @param colNames
     *            - list of column names of the record
     */
    public void createSheet(String sheetName, List<String> colNames) {

	this.sheet = getWorkbook().createSheet(sheetName);

	int currentRow = getCurrentRow();

	Row row = sheet.createRow(currentRow);
	int colCount = colNames.size();
	for (int i = 0; i < colCount; i++) {
	    Cell cell = row.createCell(i);
	    cell.setCellValue(colNames.get(i));
	}

	try {
	    FileOutputStream out = new FileOutputStream(new File(path
		    + fileName));
	    workbook.write(out);
	    out.close();

	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	setCurrentRow(++currentRow);
    }

    /**
     * This method records each row entries under respective columns. This is
     * used to record the user profile informations of both social networks
     * which are considered for analysis.
     * 
     * @param id1
     *            - represents the user id of social network 1
     * @param id2
     *            - represents the user id of social network 2
     * @param name1
     *            - represents the name of the user of social network 1
     * @param name2
     *            - represents the name of the user of social network 2
     * @param currLoc1
     *            - represents the current location of the user of social
     *            network 1
     * @param currLoc2
     *            - represents the current location of the user of social
     *            network 2
     * @param homeLoc1
     *            - represents the home location of the user of social network 1
     * @param homeLoc2
     *            - represents the home location of the user of social network 2
     */
    public void createNextRow(String id1, String id2, String name1,
	    String name2, String currLoc1, String currLoc2, String homeLoc1,
	    String homeLoc2) {
	int currentRow = getCurrentRow();
	try {
	    FileInputStream file = new FileInputStream(
		    new File(path + fileName));
	    workbook = new HSSFWorkbook(file);
	    sheet = workbook.getSheetAt(0);

	    Row row = sheet.createRow(currentRow);
	    Cell cell1 = row.createCell(0);
	    cell1.setCellValue(id1);
	    Cell cell2 = row.createCell(1);
	    cell2.setCellValue(id2);
	    Cell cell3 = row.createCell(2);
	    cell3.setCellValue(name1);
	    Cell cell4 = row.createCell(3);
	    cell4.setCellValue(name2);
	    Cell cell5 = row.createCell(4);
	    cell5.setCellValue(currLoc1);
	    Cell cell6 = row.createCell(5);
	    cell6.setCellValue(currLoc2);
	    Cell cell7 = row.createCell(6);
	    cell7.setCellValue(homeLoc1);
	    Cell cell8 = row.createCell(7);
	    cell8.setCellValue(homeLoc2);

	    FileOutputStream out = new FileOutputStream(new File(this.path
		    + fileName));
	    workbook.write(out);
	    out.close();
	    System.out.println("\nExcel written successfully..");

	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	setCurrentRow(++currentRow);
    }

    /**
     * This method records each row entries under respective columns. This is
     * used to record svm prediction output.
     * 
     * @param id1
     *            - represents the user id of social network 1
     * @param id2
     *            - represents the user id of social network 2
     * @param predict1
     *            - probability of two users (id1 & id2) profile being matched
     * @param predict0
     *            - probability of two users (id1 & id2) profile being
     *            un-matched
     * @param prediction
     *            - prediction results whether it is matching ids or not. ('1' -
     *            match, '0' - un-match)
     */
    public void createNextRowSVM(String id1, String id2, double predict1,
	    double predict0, double prediction) {
	int currentRow = getCurrentRow();
	try {
	    FileInputStream file = new FileInputStream(
		    new File(path + fileName));
	    workbook = new HSSFWorkbook(file);
	    sheet = workbook.getSheetAt(0);

	    Row row = sheet.createRow(currentRow);
	    Cell cell1 = row.createCell(0);
	    cell1.setCellValue(id1);
	    Cell cell2 = row.createCell(1);
	    cell2.setCellValue(id2);
	    Cell cell3 = row.createCell(2);
	    cell3.setCellValue(predict1);
	    Cell cell4 = row.createCell(3);
	    cell4.setCellValue(predict0);
	    Cell cell5 = row.createCell(4);
	    cell5.setCellValue(prediction);

	    FileOutputStream out = new FileOutputStream(new File(path
		    + fileName));
	    workbook.write(out);
	    out.close();
	    System.out.println("\nExcel saved successfully...");

	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	setCurrentRow(++currentRow);
    }

    /**
     * @return current row in currently working excel sheet
     */
    public int getCurrentRow() {
	return currentRow;
    }

    /**
     * @param currentRow
     *            - set the current row that points to new row in the excel
     *            sheet
     */
    private void setCurrentRow(int currentRow) {
	this.currentRow = currentRow;
    }

    /**
     * @return the excel file
     */
    public HSSFWorkbook getWorkbook() {
	return workbook;
    }

    /**
     * @return the excel sheet
     */
    public HSSFSheet getSheet() {
	return sheet;
    }

}
