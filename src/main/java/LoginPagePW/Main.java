package LoginPagePW;

import com.microsoft.playwright.*;
import org.testng.annotations.Test;

public class Main {

  @Test
  public void runNewEnquiryFromExcel() throws InterruptedException {

    String excelPath = "C:\\Users\\ACS-90\\Downloads\\ProcurementNew.xlsx";
    ExcelUtil excel = new ExcelUtil(excelPath);

    try (Playwright playwright = Playwright.create()) {

      Browser browser = playwright.chromium().launch(
        new BrowserType.LaunchOptions().setHeadless(false)
      );

      
      Page page = browser.newPage();
      LoginPage loginPage = new LoginPage(page);
      NewEnquiry ne = new NewEnquiry(page);

      // -------- Read credentials --------
      String url = excel.getCell("Credentials", 1, 0);
      String otp = excel.getCell("Credentials", 1, 1);

      page.navigate(url);

      int lastRow = excel.getLastRow("New Enquiry");

      for (int i = 1; i <= lastRow; i++) {

        String username = excel.getCell("New Enquiry", i, 0);
        String password = excel.getCell("New Enquiry", i, 1);
        String location = excel.getCell("New Enquiry", i, 2);
        String date = excel.getCell("New Enquiry", i, 3);
        String enquiry_assign_to = excel.getCell("New Enquiry", i, 4);
        String reg_no = excel.getCell("New Enquiry", i, 5);
        String mobileNo = excel.getCell("New Enquiry", i, 6);
        String customerName = excel.getCell("New Enquiry", i, 7);
        String email = excel.getCell("New Enquiry", i, 8);
        String companyName = excel.getCell("New Enquiry", i, 9);
        String source = excel.getCell("New Enquiry", i, 10);

        
        try {
			loginPage.login(username, password);

			ne.procurement();
			ne.newEnquiry();
			ne.location(location);
			ne.date(date);
			ne.enquiryAssignTo(enquiry_assign_to);
			Thread.sleep(2000);
			ne.registrationNo(reg_no);
			ne.mobileNo(mobileNo, customerName, email, companyName, otp);
			ne.source(source);
        }catch(Exception e) {
        	System.out.println(e.getMessage());
        }
      }
    } finally {
      excel.close();
    }
  }
}
