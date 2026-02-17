package LoginPagePW;



import com.microsoft.playwright.*;

public class NewEnquiry {
	private Page page;

	public NewEnquiry(Page page) {
		this.page = page;
	}

	public void newEnquiry() {
		page.locator("//a[@href='/procurement/new-enquiry']").click();
	}

	public void procurement() {
		Locator procurement = page.locator("//span[normalize-space(text())='Procurement']");
		procurement.scrollIntoViewIfNeeded();
		procurement.click();
	}

	public void location(String location) {
		Locator field = page.locator("//ng-select[@formcontrolname='locationName']");
		field.click();
		field.press("Control+A");
		field.press("Backspace");
		field.type(location);
		field.press("Enter");
		field.press("Tab");

		Locator error = page.locator("//span[text()=' Please Select Location ']");
		if (error.isVisible()) {
			throw new RuntimeException("Please Select Location.");
		}
	}

	public void date(String date) {
		Locator dateInput = page.locator("//input[@formcontrolname='todayDate']");

		dateInput.fill("");
		dateInput.type(date);

		page.locator("//input[@placeholder='Email']").click();

		Locator error = page.locator("//span[text()=' Please enter valid Date ']");

		if (error.isVisible()) {
			throw new RuntimeException("Please enter valid Date.");
		}
	}

	public void enquiryAssignTo(String enquiry_assign_to) throws InterruptedException {
		Locator field = page.locator("//ng-select[@formcontrolname=\"enquiryAssign\"]");
		field.click();
		Thread.sleep(1000);
		field.type(enquiry_assign_to);
		Thread.sleep(1000);
		field.press("Enter");
		field.press("Tab");
		field.press("Tab");

		Locator error = page.locator("//span[text()=' Please select Enquiry Assign To ']");
		if (error.isVisible()) {
			throw new RuntimeException("Please select Enquiry Assign To.");
		}
	}

	public void registrationNo(String regNo) {

		// 1️⃣ Enter registration number
		Locator regNoField = page.locator("//input[@formcontrolname='registerNumber']");

		regNoField.fill(regNo);
		page.keyboard().press("Tab");

		// 2️⃣ Wait up to 5 sec for "already registered" popup
		Locator closeBtn = page.locator("//button[@class='close']");

		try {
			closeBtn.waitFor(new Locator.WaitForOptions().setTimeout(5000));

			// If we reach here → popup appeared
			closeBtn.click();
			throw new RuntimeException(regNo + " This registration number is already registered.");

		} catch (PlaywrightException e) {
			// Popup did NOT appear within 5 sec → continue
		}

		// 3️⃣ Check dealer stock message (instant)
		Locator dealerStockMsg = page.locator("//div[text()='This Vehicle is already in Dealer Stock!']");
		if (dealerStockMsg.isVisible()) {
			throw new RuntimeException(regNo + " This Vehicle is already in Dealer Stock.");
		}

		// 4️⃣ Check invalid registration format
		Locator invalidRegError = page
				.locator("//span[text()=' Please Provide Register Number Like DL8BH4516 OR 22BH1234AA ']");
		if (invalidRegError.isVisible()) {
			throw new RuntimeException("Enter a valid registration number.");
		}
	}

	public void mobileNo(String mobileNo, String customerName, String email, String companyName, String otp)
			throws InterruptedException {

		// Enter mobile number
		Locator mobileField = page.locator("//input[@formcontrolname='mobileNumber']");

		mobileField.fill(mobileNo);
		page.keyboard().press("Tab");

		// 🔴 Wait up to 2 sec for "registered owner" popup
		Locator registeredOwnerMsg = page.locator("//div[contains(text(),'There is a registered owner')]");

		try {
			registeredOwnerMsg.waitFor(new Locator.WaitForOptions().setTimeout(2000));

			// Popup appeared → click YES
			page.locator("//button[text()='Yes']").click();

			// Enter OTP flow
			enterOTP(otp);

			// Update customer details if provided
			if (!customerName.isBlank() || !email.isBlank() || !companyName.isBlank()) {

				page.locator("//button[contains(@class,'bi-pencil-square')]").click();

				customerNameUpdated(customerName);
				emailUpdated(email);
				companyNameUpdated(companyName);

				page.locator("//button[text()='Save Changes']").click();
				page.locator("//button[text()='OK']").click();
			}

		} catch (PlaywrightException e) {
			// Popup did NOT appear → fresh customer
			customerName(customerName);
			email(email);
			companyName(companyName);
		}

		// ❌ Invalid mobile number check
		Locator invalidMobile = page.locator("//span[text()=' Please Provide Valid Mobile Number ']");

		if (invalidMobile.isVisible()) {
			throw new RuntimeException("Invalid mobile No. Please enter a 10-digit number.");
		}
	}

	private void customerName(String customer_name) {
		if (customer_name.isBlank()) {
			return;
		}
		Locator field = page.locator("//input[@formcontrolname=\"updateCustomerName\"]");
		field.clear();
		field.type(customer_name);
		Locator errorMsg = page.locator("//span[text()=\" Please Provide Valid Customer Name \"]");
		if (errorMsg.isVisible()) {
			throw new RuntimeException("Please Enter Valid Customer Name.");
		}
	}

	public void enterOTP(String otp) {
		// Wait for OTP modal
		page.locator("//h4[text()='OTP Verification']").waitFor(new Locator.WaitForOptions().setTimeout(5000));
		
		Locator otpField = page.locator("//input[@placeholder='Enter OTP']");
		otpField.fill(otp);
		page.keyboard().press("Tab");

		// Validate 6-digit OTP
		Locator invalidLength = page.locator("//span[text()='Please enter 6 digit OTP']");
		if (invalidLength.isVisible()) {
			throw new RuntimeException("Please Enter 6 Digit OTP");
		}

		// Verify OTP
		page.locator("//button[text()='Verify OTP']").click();

		// Incorrect OTP
		Locator incorrectOtp = page.locator("//span[text()='OTP is incorrect']");
		if (incorrectOtp.isVisible()) {
			throw new RuntimeException("OTP is incorrect");
		}

		// Success alert
		Locator successAlert = page.locator("//div[@id='swal2-html-container']");
		successAlert.waitFor();

		if (successAlert.textContent().contains("Successfully")) {
			page.locator("//button[text()='OK']").click();
		}

	}

	public void customerNameUpdated(String name) {
		if (name.isBlank())
			return;

		Locator field = page.locator("//input[@formcontrolname='updateCustomerName']");
		field.fill(name);
		page.keyboard().press("Tab");
	}

	public void email(String email) {
		if (email.isBlank())
			return;

		page.locator("//input[@placeholder='Email']").fill(email);

		if (!email.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")) {
			throw new RuntimeException("Please enter valid email address.");
		}
	}

	public void emailUpdated(String email) {
		if (email.isBlank())
			return;

		page.locator("//input[@formcontrolname=\"updateCustomerEmail\"]").fill(email);

		if (!email.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")) {
			throw new RuntimeException("Please enter valid email address.");
		}
	}

	public void companyName(String company) {
		if (company.isBlank())
			return;

		page.locator("//label[contains(@class,'ckbox')]").click();
		page.locator("//input[@placeholder='Company Name']").fill(company);
	}

	public void companyNameUpdated(String company_name) throws InterruptedException {
		if (company_name.isBlank()) {
			return;
		}
		Locator companyCheckBox = page.locator("(//label[@class=\"ckbox wd-16 mg-b-0\"])[2]");
		companyCheckBox.click();
		Locator field = page.locator("//input[@formcontrolname=\"updateCompanyName\"]");
		field.clear();
		Thread.sleep(200);
		field.type(company_name);
	}

	public void source(String source) throws InterruptedException {
		Locator Field = page.locator("//ng-select[@formcontrolname=\"sourceName\"]");
		Locator Field2 = page.locator("//ng-select[@formcontrolname=\"sourceName\"]//span[@title=\"Clear all\"]");
		Field2.click();
		Field.type(source);
		Field.press("ENTER");

		Field.press("TAB");
		Locator error = page.locator("//span[text()=' Please Select Source Name ']");
		if (error.isVisible()) {
			throw new RuntimeException("Please Select Source Name.");
		}
	}
}
