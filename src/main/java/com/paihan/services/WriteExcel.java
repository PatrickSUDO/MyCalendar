package com.paihan.services;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import com.paihan.entities.WorkItem;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class WriteExcel {

    private WritableCellFormat arialBoldUnderline;
    private WritableCellFormat arial;

    // Returns an InputStream that represents the Excel report
    public java.io.InputStream exportExcel(List<WorkItem> list) {
        try {
            java.io.InputStream is = write(list);
            return is;
        } catch (WriteException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Generates the report and returns an InputStream
    public java.io.InputStream write(List<WorkItem> list) throws IOException, WriteException {
        java.io.OutputStream os = new java.io.ByteArrayOutputStream();
        WorkbookSettings wbSettings = new WorkbookSettings();

        wbSettings.setLocale(new Locale("en", "IE"));

        // Create a workbook, pass the OutputStream
        WritableWorkbook workbook = Workbook.createWorkbook(os, wbSettings);
        workbook.createSheet("Work Item Report", 0);
        WritableSheet excelSheet = workbook.getSheet(0);
        createLabel(excelSheet);
        int size = createContent(excelSheet, list);

        // Close the workbook
        workbook.write();
        workbook.close();

        // Get an InputStream that represents the report
        java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
        stream = (java.io.ByteArrayOutputStream) os;
        byte[] myBytes = stream.toByteArray();
        java.io.InputStream is = new java.io.ByteArrayInputStream(myBytes);
        return is;
    }

    // Create headings in the Excel spreadsheet
    private void createLabel(WritableSheet sheet)
            throws WriteException {
        // Create a Arial font
        WritableFont arial10pt = new WritableFont(WritableFont.ARIAL, 10);
        // Define the cell format
        arial = new WritableCellFormat(arial10pt);
        // Automatically wrap the cells
        arial.setWrap(true);

        // Create a bold font with underlining
        WritableFont arial10ptBoldUnderline = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, false,
                UnderlineStyle.SINGLE);
        arialBoldUnderline = new WritableCellFormat(arial10ptBoldUnderline);
        // Automatically wrap the cells
        arialBoldUnderline.setWrap(true);

        CellView cv = new CellView();
        cv.setFormat(arial);
        cv.setFormat(arialBoldUnderline);
        cv.setAutosize(true);

        // Write a few headers
        addCaption(sheet, 0, 0, "User");
        addCaption(sheet, 1, 0, "Event");
        addCaption(sheet, 2, 0, "Event Date");
        addCaption(sheet, 3, 0, "Description");
        addCaption(sheet, 4, 0, "Date");

    }

    // Write the ItemData to the Excel report
    private int createContent(WritableSheet sheet, List<WorkItem> list) throws WriteException {

        int size = list.size();
        // Add data to the Excel report
        for (int i = 0; i < size; i++) {

            WorkItem wi = list.get(i);

            // Get the work item values
            String name = wi.getName();
            String event = wi.getEvent();
            String eventDate = wi.getEventDate();
            String description = wi.getDescription();
            String date = wi.getDate();


            // First column
            addLabel(sheet, 0, i + 2, name);
            // Second column
            addLabel(sheet, 1, i + 2, event);

            // Third column
            addLabel(sheet, 2, i + 2, eventDate);

            // Forth column
            addLabel(sheet, 3, i + 2, description);

            // Fifth column
            addLabel(sheet, 4, i + 2, date);

        }
        return size;
    }

    private void addCaption(WritableSheet sheet, int column, int row, String s)
            throws WriteException {
        Label label;
        label = new Label(column, row, s, arialBoldUnderline);

        int cc = countString(s);
        sheet.setColumnView(column, cc);
        sheet.addCell(label);
    }

    private void addNumber(WritableSheet sheet, int column, int row,
                           Integer integer) throws WriteException {
        Number number;
        number = new Number(column, row, integer, arial);
        sheet.addCell(number);
    }

    private void addLabel(WritableSheet sheet, int column, int row, String s)
            throws WriteException {
        Label label;
        label = new Label(column, row, s, arial);
        int cc = countString(s);
        if (cc > 200)
            sheet.setColumnView(column, 150);
        else
            sheet.setColumnView(column, cc + 6);

        sheet.addCell(label);

    }

    private int countString(String ss) {
        int count = 0;
        // Counts each character except spaces
        for (int i = 0; i < ss.length(); i++) {
            if (ss.charAt(i) != ' ')
                count++;
        }
        return count;
    }
}
