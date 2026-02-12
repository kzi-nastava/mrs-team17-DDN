package org.example.e2e.page;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        long waitSeconds = Long.parseLong(System.getProperty("e2e.wait.seconds", "15"));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
    }

    protected void open(String url) {
        driver.get(url);
    }

    protected By byTestId(String testId) {
        return By.cssSelector("[data-testid='" + testId + "']");
    }

    protected WebElement waitVisible(String testId) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(byTestId(testId)));
    }

    protected WebElement waitClickable(String testId) {
        return wait.until(ExpectedConditions.elementToBeClickable(byTestId(testId)));
    }

    protected void click(String testId) {
        waitClickable(testId).click();
    }

    protected void type(String testId, String value) {
        WebElement input = waitVisible(testId);
        input.clear();
        input.sendKeys(value);
    }

    protected String text(String testId) {
        return waitVisible(testId).getText().trim();
    }

    protected boolean isPresent(String testId) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(byTestId(testId)));
            return true;
        } catch (TimeoutException ex) {
            return false;
        }
    }

    protected void waitForUrlContains(String partial) {
        wait.until(ExpectedConditions.urlContains(partial));
    }
}
