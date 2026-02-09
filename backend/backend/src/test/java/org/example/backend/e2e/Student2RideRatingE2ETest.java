package org.example.backend.e2e;

import org.example.backend.e2e.fixture.RatingE2eFixture;
import org.example.backend.e2e.page.LoginPage;
import org.example.backend.e2e.page.RideRatePage;
import org.example.backend.e2e.page.UserHomePage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("e2e")
class Student2RideRatingE2ETest {

    private WebDriver driver;
    private RatingE2eFixture fixture;
    private String frontendUrl;

    @BeforeEach
    void setUp() {
        this.frontendUrl = System.getProperty("e2e.frontend.url", "http://localhost:4200");
        this.fixture = RatingE2eFixture.fromSystemProperties();
        this.fixture.cleanup();

        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("e2e.headless", "false"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000");

        this.driver = new ChromeDriver(options);
        this.driver.manage().timeouts().implicitlyWait(Duration.ZERO);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (fixture != null) {
            fixture.cleanup();
        }
    }

    @Test
    void happyPath_shouldSubmitRatingAndPersistIt() {
        long rideId = fixture.prepareHappyPathScenario();

        loginAsPassenger();

        UserHomePage homePage = new UserHomePage(driver);
        homePage.waitForPendingRateBanner();
        homePage.clickPendingRateNow();

        RideRatePage ratePage = new RideRatePage(driver);
        ratePage.waitLoaded();
        assertTrue(driver.getCurrentUrl().contains("/user/rides/" + rideId + "/rate"));

        String comment = "Selenium happy path comment";
        ratePage.setDriverRating(4);
        ratePage.setVehicleRating(5);
        ratePage.setComment(comment);
        ratePage.submit();

        ratePage.waitForExistingSection();
        assertTrue(ratePage.hasExistingSection());
        assertEquals("4/5", ratePage.existingDriverRatingText());
        assertEquals("5/5", ratePage.existingVehicleRatingText());
        assertEquals(comment, ratePage.existingCommentText());
        assertEquals(1, fixture.countRatingsForRide(rideId));
    }

    @Test
    void alreadyRated_shouldShowReadOnlyStateAndNotInsertAnotherRating() {
        long rideId = fixture.prepareAlreadyRatedScenario();

        loginAsPassenger();
        driver.get(frontendUrl + "/user/rides/" + rideId + "/rate");

        RideRatePage ratePage = new RideRatePage(driver);
        ratePage.waitLoaded();
        ratePage.waitForExistingSection();

        assertTrue(ratePage.hasExistingSection());
        assertFalse(ratePage.hasSubmitButton());
        assertEquals("3/5", ratePage.existingDriverRatingText());
        assertEquals("4/5", ratePage.existingVehicleRatingText());
        assertEquals("Existing fixture rating", ratePage.existingCommentText());
        assertEquals(1, fixture.countRatingsForRide(rideId));
    }

    @Test
    void expiredWindow_shouldShowErrorAndKeepRatingTableUnchanged() {
        long rideId = fixture.prepareExpiredWindowScenario();

        loginAsPassenger();
        driver.get(frontendUrl + "/user/rides/" + rideId + "/rate");

        RideRatePage ratePage = new RideRatePage(driver);
        ratePage.waitLoaded();
        assertTrue(ratePage.hasSubmitButton());

        ratePage.setDriverRating(5);
        ratePage.setVehicleRating(5);
        ratePage.setComment("Late rating attempt");
        ratePage.submit();

        ratePage.waitForError();
        assertEquals("Submit failed", ratePage.errorText());
        assertEquals(0, fixture.countRatingsForRide(rideId));
    }

    private void loginAsPassenger() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.openLogin(frontendUrl);
        loginPage.login(fixture.passengerEmail(), fixture.passengerPassword());

        UserHomePage homePage = new UserHomePage(driver);
        homePage.waitLoaded();
    }
}
