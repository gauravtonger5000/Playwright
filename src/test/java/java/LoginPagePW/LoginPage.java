package java.LoginPagePW;

import com.microsoft.playwright.*;

public class LoginPage {

  private Page page;

  // Locators
  private Locator usernameField;
  private Locator passwordField;
  private Locator loginButton;

  public LoginPage(Page page) {
    this.page = page;

    // Initialize locators (same XPath as Selenium)
    this.usernameField = page.locator("//input[@formcontrolname='UserName']");
    this.passwordField = page.locator("//input[@formcontrolname='Pwd']");
    this.loginButton = page.locator("//button[contains(@class,'btn-primary')]");
   
  }

  // ------------------ Actions ------------------

  public void open() {
    page.navigate("https://preowned.acsinfotech.com/");
  }

  public void username(String username) {
    usernameField.fill(username);   // auto-wait + retry
  }

  public void password(String password) {
    passwordField.fill(password);   // auto-wait + retry
  }

  public void loginButton() {
    loginButton.click();            // auto-handles click interception
  }

 

  public void login(String username, String password) {
    username(username);
    password(password);
    loginButton();
  }
}
