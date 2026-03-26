package LoginPagePW;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProcurementInspectionPage {

	private final Page page;
	private int totalQuestionFilled = 0;

	public ProcurementInspectionPage(Page page) {
		this.page = page;
	}


	public void waitForNgxSpinner() {
		Locator spinner = page.locator("ngx-spinner");

		try {
			// Step 1: Wait up to 2 seconds for spinner to appear
			spinner.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(2000));

			// Step 2: If spinner appears, wait until it disappears 5 Minutes wait if we don't pass time then it wait for 30 seconds by default
			spinner.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(300000));

		} catch (PlaywrightException e) {
			// Spinner did NOT appear within 2 seconds → move on
//			System.out.println("Spinner did not appear, continuing...");
		}
	}

	public void waitForVahanLoading() {
		Locator loader = page.locator("div.loading-text");

		if (loader.count() > 0) {
			loader.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
			loader.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
		}

		handleVahanPopup();
	}

	public void handleVahanPopup() {
		Locator popup = page.locator("div.swal2-popup");
		if (popup.isVisible()) {
			page.locator("button.swal2-confirm").click();
			popup.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
		}
	}


	public void procurement() {
	    Locator procurement = page.locator("//span[normalize-space(text())='Procurement']").first();
	    procurement.scrollIntoViewIfNeeded();
	    procurement.click();
	}

	public void openInspectionInformation() {
		waitForNgxSpinner();
		page.locator("a[href='/procurement/inspector-information']").click();
		waitForNgxSpinner();
	}

	public void clickAllPending() {
		page.locator("button:has-text('All Pending')").first().click();
	}

	public void searchRegistration(String regNo) {

		page.locator("input[placeholder='Type to filter']").fill(regNo);
	}

	public void startInspection(String regNo) throws InterruptedException {
		Thread.sleep(10000);
		Locator startBtn = page.locator("//button[text()=\"Start Inspection\"]");
		if (!startBtn.isVisible()) {
			throw new RuntimeException(regNo + " not found");
		}
		startBtn.click();
		waitForNgxSpinner();
	}

	public void enterRegistrationNo(String regNo) {
		Locator regInput = page.locator("input[placeholder='Registration Number']");
		regInput.fill(regNo);
		regInput.blur();
	}

	public void waitForVehicleDetailsTab() {
		page.locator("span:has-text('Vehicle Details')").waitFor();
	}

	public void selectInspectionType() throws InterruptedException {
		Thread.sleep(2000);
		Locator btn = page.locator("//button[normalize-space(text())=\"Detail\"]");
		if (btn.isVisible()) {
			btn.click();
		}
	}

	public void tabName(String tab_name) {
		try {
			Locator tab = page.locator("//span[contains(text(),'" + tab_name + "')]");

			tab.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

			tab.scrollIntoViewIfNeeded();

			tab.click(new Locator.ClickOptions().setTimeout(5000));
		} catch (PlaywrightException e) {
			System.out.println(e.getMessage());
		}

	}

	public List<String> getQuestions() {
		List<String> questions = new ArrayList<>();
		Locator labels = page.locator("//label[normalize-space(text())!='Registration Number']");

		for (int i = 0; i < labels.count(); i++) {
			questions.add(labels.nth(i).innerText().replace(" *", "").trim());
		}
		return questions;
	}

	public void enterNgSelectValue(String question, String value) {
		Locator select = page
				.locator("//label[normalize-space(text())='" + question + "']/following-sibling::div//ng-select");
		if (!select.isVisible())
			return;

		select.click();
		page.keyboard().type(value);
		page.keyboard().press("Enter");
		totalQuestionFilled++;
	}


	public void imageType(String methodName, String filePath, String tabName) {

		try {
			File file = new File(filePath);

			if (!file.exists()) {

				return;
			}

			Locator fileInput = page
					.locator("//label[contains(@for,'" + methodName + "')]/following::input[@type='file'][1]");

			if (fileInput.count() == 0) {
				// same behavior as your Selenium code (silent skip)
				return;
			}

			fileInput.setInputFiles(Paths.get(filePath));

			totalQuestionFilled++;

		} catch (Exception e) {
			throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
		}
	}

	public void RadioType(String methodName, String condition, String tabName) {

		Locator labels = page.locator("//label[contains(@for,'" + methodName + "')]");
		if (labels.count() == 0)
			return;

		Locator shortestLabel = null;
		int shortestLength = Integer.MAX_VALUE;

		for (int i = 0; i < labels.count(); i++) {
			Locator label = labels.nth(i);
			String forValue = label.getAttribute("for");

			if (forValue != null && !forValue.toLowerCase().contains("image")) {
				if (forValue.length() < shortestLength) {
					shortestLength = forValue.length();
					shortestLabel = label;
				}
			}
		}

		if (shortestLabel == null)
			return;

		String match = shortestLabel.getAttribute("for");

		Locator options = page.locator("//label[@for='" + match + "']/following-sibling::div");

		for (int i = 0; i < options.count(); i++) {
			Locator option = options.nth(i);

			if (!option.isEnabled())
				return;

			if (option.innerText().trim().equalsIgnoreCase(condition)) {
				option.scrollIntoViewIfNeeded();
				option.click();
				totalQuestionFilled++;
				return;
			}
		}
	}

	public void dropDownType(String methodName, String dropdownValue, String tabName) {

	    try {

	        Locator dropdown = page.locator("//select[contains(@aria-describedby,'" + methodName + "')]");

	        dropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

	        if (!dropdown.isEnabled()) return;

	        // wait until options load
	        page.waitForCondition(() -> dropdown.locator("option").count() > 1);

	        // trim value from excel
	        dropdownValue = dropdownValue.trim();

	        dropdown.selectOption(new SelectOption().setLabel(dropdownValue));

	        totalQuestionFilled++;

	    } catch (Exception e) {
	        System.out.println("Dropdown selection failed for: " + methodName);
	        System.out.println(e.getMessage());
	    }
	}

	public void starRating(String methodName, String parameter, String tabName) {

		int starIndex;
		try {
			starIndex = Integer.parseInt(parameter);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Invalid star rating value: " + parameter);
		}

		Locator stars = page
				.locator("//label[contains(@for,'" + methodName + "')]/following-sibling::formly-field-star-rating//i");

		int count = stars.count();

		if (starIndex <= 0 || starIndex > count) {
			throw new RuntimeException(
					"You cannot select rating more than " + count + " for " + methodName + " in " + tabName);
		}

		stars.nth(starIndex - 1).click();
		totalQuestionFilled++;
	}

	public void inputBasedType(String methodName, String input, String tabName) {

		Locator inputField = page.locator("//input[contains(@aria-describedby,'" + methodName + "')]");

		if (!inputField.isVisible() || !inputField.isEnabled())
			return;

		inputField.scrollIntoViewIfNeeded();
		inputField.fill(input);
		totalQuestionFilled++;
	}

	public void executeMethod(String methodType, String methodName, String parameter, String tabName) {

		switch (methodType) {

		case "imagetype":
			imageType(methodName, parameter, tabName);
			break;

		case "RadioType":
		case "radiotype":
			RadioType(methodName, parameter, tabName);
			break;

		case "SelectType":
			dropDownType(methodName, parameter, tabName);
			break;

		case "InputBasedType":
			inputBasedType(methodName, parameter, tabName);
			break;

		case "starratingtype":
		case "starrating":
			starRating(methodName, parameter, tabName);
			break;

		default:
			throw new IllegalArgumentException("Unsupported method type: " + methodType);
		}
	}

	public int getFilledQuestionCount() {
		return totalQuestionFilled;
	}
	public void saveChangeBtn(String regNo) {

		Page page = this.page; // assuming page is already initialized

		// Click "Next" button until it disappears
		while (true) {
			try {
				Locator nextBtn = page.locator("//button[text()=' Next ']");

				if (nextBtn.count() == 0) {
					break;
				}

				nextBtn.first().click(new Locator.ClickOptions().setForce(true));
				page.waitForTimeout(300); // small pause to allow navigation

			} catch (PlaywrightException e) {
				break; // stop when Next button no longer exists
			}
		}

		try {
			Locator saveBtn = page.locator("//button[contains(text(),'Save Change')]");

			// If Save button is enabled, click once
			if (saveBtn.isEnabled()) {
				saveBtn.click();
			}

			// Check mandatory field validation
			try {
				page.waitForSelector("//formly-validation-message[text()='This field is required']",
						new Page.WaitForSelectorOptions().setTimeout(1000));
				throw new RuntimeException("Please enter all mandatory fields");
			} catch (TimeoutError e) {
				// no validation error shown → continue
			}

			// Wait until Save button becomes enabled
			page.waitForCondition(() -> saveBtn.isEnabled(), new Page.WaitForConditionOptions().setTimeout(60_000));

			if (!saveBtn.isEnabled()) {
				throw new RuntimeException("Please enter all mandatory fields");
			}

			// Final Save click
			saveBtn.scrollIntoViewIfNeeded();
			page.waitForTimeout(1000);
			saveBtn.click();

			waitForNgxSpinner();

			Locator alert = page.locator("//div[@id='swal2-html-container']");
			

			// wait until alert is visible
			alert.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
			
			String saveAlert = alert.innerText();

			if (saveAlert.toLowerCase().contains("success") || saveAlert.toLowerCase().contains("partially")) {

				Locator okBtn = page.locator("//button[text()='OK']");
				okBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED));
				okBtn.click();

				System.out.println(saveAlert + " for Registration No: " + regNo);

			} else {
				System.out.println(saveAlert + " for Registration No: " + regNo);
			}

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		waitForNgxSpinner();
	}
}
