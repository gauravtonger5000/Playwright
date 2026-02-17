package java.LoginPagePW;

import com.microsoft.playwright.*;

import org.testng.Assert;

public class MainClassInspection {

//	@Test
	public void runNewEnquiryFromExcel() throws InterruptedException {

		String excelPath = "C:\\Users\\ACS-90\\Downloads\\ProcurementNewPW.xlsx";
		ExcelUtil excel = new ExcelUtil(excelPath);

		try (Playwright playwright = Playwright.create()) {
			
			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

			// ✅ Always use BrowserContext
			BrowserContext context = browser.newContext();
			Page page = context.newPage();

			LoginPage loginPage = new LoginPage(page);
			ProcurementInspectionPage pi = new ProcurementInspectionPage(page);

			// -------- Read credentials --------
			String url = excel.getCell("Credentials", 1, 0);
			String otp = excel.getCell("Credentials", 1, 1);

			page.navigate(url);

			int lastRow = excel.getLastRow("New Enquiry");

			for (int i = 1; i <= 1; i++) {

				String username = excel.getCell("Inspection Information", i, 0);
				String password = excel.getCell("Inspection Information", i, 1);
				String inspectiontype = excel.getCell("Inspection Information", 1, 6);
				String reg_no = excel.getCell("Inspection Information", 1, 3);

				try {
					// 🔐 Login
					loginPage.login(username, password);
					pi.procurement();
					pi.openInspectionInformation();
					pi.clickAllPending();
					pi.searchRegistration(reg_no);
					pi.startInspection(reg_no);
					pi.selectInspectionType();
					Thread.sleep(500);
					int lastRow1 = excel.getLastRow("Inspection Information");
					pi.waitForNgxSpinner();
					String previousTabName = "";
					for (int j = 1; j <= lastRow1; j++) {
						String tab_name = excel.getCell("Inspection Information", j, 7);
						String method_type = excel.getCell("Inspection Information", j, 8);
						String method_name = excel.getCell("Inspection Information", j, 9);
						String parameter = excel.getCell("Inspection Information", j, 10);

						if (!tab_name.equals(previousTabName)) {
							pi.tabName(tab_name);
							previousTabName = tab_name; // Update the previous tab name
						}
						pi.executeMethod(method_type, method_name, parameter, previousTabName);
					}
					pi.saveChangeBtn(reg_no);
				} catch (Exception e) {
			        // ✅ ONE clean failure line
					System.out.println(e.getMessage());
			      //  Assert.fail("Inspection failed | " + e.getMessage());
			    }
			}
//			context.close();
//			browser.close();

		} finally {
//			excel.close();
		}
		
	}
	public static void main(String[] args) throws InterruptedException {
		MainClassInspection main = new MainClassInspection();
		main.runNewEnquiryFromExcel();
	}
}
