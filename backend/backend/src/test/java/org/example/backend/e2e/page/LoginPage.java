package org.example.backend.e2e.page;

import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void openLogin(String frontendBaseUrl) {
        open(frontendBaseUrl + "/login");
        waitLoaded();
    }

    public void waitLoaded() {
        waitVisible("login-email-input");
        waitVisible("login-password-input");
        waitVisible("login-submit-button");
    }

    public void login(String email, String password) {
        type("login-email-input", email);
        type("login-password-input", password);
        click("login-submit-button");
    }
}
