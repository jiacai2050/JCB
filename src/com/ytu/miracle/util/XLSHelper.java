package com.ytu.miracle.util;
import java.io.File;
import java.io.IOException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class XLSHelper {

	public void writeXLS(int row, int col, String content, String file) {
		try {
			// 打开文件
			WritableWorkbook book = Workbook.createWorkbook(new File(file));
			WritableSheet sheet = book.createSheet("第一页 ", 0);
			Label label = new Label(row, col, content);
			sheet.addCell(label);
			book.write();
			book.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void readerXLS(String fileName) {
		try {
			File file = new File(fileName);
			Workbook workbook = Workbook.getWorkbook(file);
			for (int m = 0; m < workbook.getSheets().length; m++) {
				Sheet sheet = workbook.getSheet(m);
				for (int i = 0; i < sheet.getRows(); i++) {
					Cell[] cell = sheet.getRow(i);
					for (int j = 0; j < cell.length; j++) {
						System.out.print(cell[j].getContents() + "   ");
					}
					System.out.println();
				}
			}
			workbook.close();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
