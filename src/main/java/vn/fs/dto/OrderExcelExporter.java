package vn.fs.dto;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.Data;
import vn.fs.entities.Order;

@Data
public class OrderExcelExporter {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Order> listOrDetails;
    private int month; // Tháng thống kê

    public OrderExcelExporter(List<Order> listOrDetails, int month) {
        this.listOrDetails = listOrDetails;
        this.month = month;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("OrderDetails");
    }

    // --- Tiêu đề ---
    private void writeTitleRow() {
        Row titleRow = sheet.createRow(0);
        Cell cell = titleRow.createCell(0);
        cell.setCellValue("THỐNG KÊ ĐƠN HÀNG THÁNG " + month);

        // Merge các cột để tiêu đề nằm giữa
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

        // Style tiêu đề
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.setCellStyle(style);

        // Tăng chiều cao hàng
        titleRow.setHeightInPoints(25);
    }

    // --- Header ---
    private void writeHeaderRow() {
        Row row = sheet.createRow(1);

        String[] headers = {"Mã đơn hàng", "Tên khách hàng", "Số điện thoại", "Địa chỉ", "Email", "Ngày đặt hàng", "Tổng tiền"};
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }

        row.setHeightInPoints(20);
    }

    // --- Dữ liệu ---
    private void writeDataRows() {
        int rowCount = 2; // Bắt đầu từ dòng thứ 3

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        CellStyle textStyle = workbook.createCellStyle();
        textStyle.setAlignment(HorizontalAlignment.LEFT);

        CellStyle numberStyle = workbook.createCellStyle();
        numberStyle.setAlignment(HorizontalAlignment.RIGHT);

        for (Order order : listOrDetails) {
            Row row = sheet.createRow(rowCount++);

            Date orderDate = order.getOrderDate();
            String formattedDate = orderDate != null ? dateFormat.format(orderDate) : "";

            row.createCell(0).setCellValue(order.getOrderId());
            row.getCell(0).setCellStyle(textStyle);

            row.createCell(1).setCellValue(order.getUser() != null ? order.getUser().getName() : "");
            row.getCell(1).setCellStyle(textStyle);

            row.createCell(2).setCellValue(order.getPhone() != null ? order.getPhone() : "");
            row.getCell(2).setCellStyle(textStyle);

            row.createCell(3).setCellValue(order.getAddress() != null ? order.getAddress() : "");
            row.getCell(3).setCellStyle(textStyle);

            row.createCell(4).setCellValue(order.getUser() != null ? order.getUser().getEmail() : "");
            row.getCell(4).setCellStyle(textStyle);

            row.createCell(5).setCellValue(formattedDate);
            row.getCell(5).setCellStyle(textStyle);

            row.createCell(6).setCellValue(order.getAmount() != null ? order.getAmount() : 0);
            row.getCell(6).setCellStyle(numberStyle);
        }
    }

    // --- Xuất Excel ---
    public void export(HttpServletResponse response) throws IOException {
        writeTitleRow();
        writeHeaderRow();
        writeDataRows();

        // Auto resize tất cả cột
        for (int i = 0; i <= 6; i++) {
            sheet.autoSizeColumn(i);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
