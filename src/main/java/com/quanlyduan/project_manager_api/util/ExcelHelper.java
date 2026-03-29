package com.quanlyduan.project_manager_api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Lop tien ich ho tro thao tac voi tep tin Excel su dung thu vien Apache POI.
 */
public class ExcelHelper {

    // Cac hang so cau hinh cho Style
    private static final short HEADER_FONT_SIZE = 12;
    private static final short HEADER_FILL_COLOR = IndexedColors.GREY_25_PERCENT.getIndex();
    
    // Thong bao loi
    private static final String ERR_WRITE_EXCEL = "Error occurred while writing data to Excel: %s";

    /**
     * Tao mot Workbook moi (dinh dang .xlsx).
     */
    public static Workbook createWorkbook() {
        return new XSSFWorkbook();
    }

    /**
     * Tao phong cach hien thi cho tieu de bang (Header).
     * Bao gom: In dam, nen xam nhat, can giua va co vien (Border).
     */
    public static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // Cau hinh Font
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(HEADER_FONT_SIZE);
        style.setFont(font);
        
        // Cau hinh mau nen
        style.setFillForegroundColor(HEADER_FILL_COLOR);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Cau hinh can le
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Cau hinh duong vien
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        
        return style;
    }

    /**
     * Chuyen doi Workbook sang mang Byte de phuc vu viec tai xuong (Download) tu Controller.
     */
    public static byte[] workbookToBytes(Workbook workbook) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(String.format(ERR_WRITE_EXCEL, e.getMessage()));
        }
    }
}