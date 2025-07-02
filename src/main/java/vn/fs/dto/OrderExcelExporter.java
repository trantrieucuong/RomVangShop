package vn.fs.dto;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.Data;
import vn.fs.entities.Order;

@Data
public class OrderExcelExporter {
	
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;

	private List<Order> listOrDetails;

	public OrderExcelExporter(List<Order> listOrDetails) {

		this.listOrDetails = listOrDetails;
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("OrderDetails");
	}
	
	private void writeHeaderRow() {

		Row row = sheet.createRow(0);

		Cell cell = row.createCell(0);
		cell.setCellValue("Mã đơn hàng");
		
		cell = row.createCell(1);
		cell.setCellValue("Tên khách hàng");
		
		cell = row.createCell(2);
		cell.setCellValue("Số điện thoại");
		
		cell = row.createCell(3);
		cell.setCellValue("Địa chỉ");
		
		cell = row.createCell(4);
		cell.setCellValue("Email");

		cell = row.createCell(5);
		cell.setCellValue("Ngày đặt hàng");
		
		cell = row.createCell(6);
		cell.setCellValue("Tổng tiền");

	}
	
	private void writeDataRows() {
		int rowCount = 1;
		
		for (Order order : listOrDetails) {
			Row row = sheet.createRow(rowCount++);
			
			Date orderDate = order.getOrderDate();

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

			String formattedDate = dateFormat.format(orderDate);

			Cell cell = row.createCell(0);
			cell.setCellValue(order.getOrderId());
			
			cell = row.createCell(1);
			cell.setCellValue(order.getUser().getName());  
			
			cell = row.createCell(2);
			cell.setCellValue(order.getPhone());

			cell = row.createCell(3);
			cell.setCellValue(order.getAddress()); 
			
			cell = row.createCell(4);
			cell.setCellValue(order.getUser().getEmail());
			
			cell = row.createCell(5);
			cell.setCellValue(formattedDate);
			
			cell = row.createCell(6);
			cell.setCellValue(order.getAmount());
		}

	}
	
	public void export(HttpServletResponse response) throws IOException {

		writeHeaderRow();
		writeDataRows();

		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();
	}

}
