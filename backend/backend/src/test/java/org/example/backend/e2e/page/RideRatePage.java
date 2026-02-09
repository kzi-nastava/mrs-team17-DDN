package org.example.backend.e2e.page;

import org.openqa.selenium.WebDriver;

public class RideRatePage extends BasePage {

    public RideRatePage(WebDriver driver) {
        super(driver);
    }

    public void waitLoaded() {
        waitForUrlContains("/user/rides/");
        waitVisible("ride-rate-page");
    }

    public void setDriverRating(int rating) {
        type("ride-rate-driver-input", Integer.toString(rating));
    }

    public void setVehicleRating(int rating) {
        type("ride-rate-vehicle-input", Integer.toString(rating));
    }

    public void setComment(String comment) {
        type("ride-rate-comment-input", comment);
    }

    public void submit() {
        click("ride-rate-submit-button");
    }

    public void waitForExistingSection() {
        waitVisible("ride-rate-existing");
    }

    public boolean hasExistingSection() {
        return isPresent("ride-rate-existing");
    }

    public boolean hasSubmitButton() {
        return !driver.findElements(byTestId("ride-rate-submit-button")).isEmpty();
    }

    public void waitForError() {
        waitVisible("ride-rate-error");
    }

    public String errorText() {
        return text("ride-rate-error");
    }

    public String infoText() {
        return text("ride-rate-info");
    }

    public String existingDriverRatingText() {
        return text("ride-rate-existing-driver-rating");
    }

    public String existingVehicleRatingText() {
        return text("ride-rate-existing-vehicle-rating");
    }

    public String existingCommentText() {
        return text("ride-rate-existing-comment");
    }
}
