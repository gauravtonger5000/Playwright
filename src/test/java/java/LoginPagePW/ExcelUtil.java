package java.LoginPagePW;

import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

  private Workbook workbook;
  private DataFormatter formatter = new DataFormatter();

  public ExcelUtil(String filePath) {
    try {
      FileInputStream fis = new FileInputStream(filePath);
      workbook = new XSSFWorkbook(fis);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load Excel", e);
    }
  }

  public String getCell(String sheet, int row, int col) {
    Sheet sh = workbook.getSheet(sheet);
    return formatter.formatCellValue(sh.getRow(row).getCell(col));
  }

  public int getLastRow(String sheet) {
    return workbook.getSheet(sheet).getLastRowNum();
  }

  public void close() {
    try {
      workbook.close();
    } catch (Exception e) {
    }
  }
}
