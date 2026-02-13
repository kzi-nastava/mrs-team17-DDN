package org.example.e2e;

import org.example.e2e.fixture.FavoriteRouteOrderE2eFixture;
import org.example.e2e.page.FavouriteRideDetailsPage;
import org.example.e2e.page.FavouriteRidesPage;
import org.example.e2e.page.LoginPage;
import org.example.e2e.page.OrderRidePage;
import org.example.e2e.page.UserHomePage;
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
class Student1FavoriteRouteOrderE2ETest {

    private WebDriver driver;
    private FavoriteRouteOrderE2eFixture fixture;
    private String frontendUrl;

    @BeforeEach
    void setUp() {
        this.frontendUrl = System.getProperty("e2e.frontend.url", "http://localhost:4200");
        this.fixture = FavoriteRouteOrderE2eFixture.fromSystemProperties();
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
    void happyPath_shouldOrderRideFromFavoriteRouteAndPersistStops() {
        long favoriteRouteId = fixture.prepareHappyPathScenario();

        loginAsPassenger();
        driver.get(frontendUrl + "/user/favourite-rides/" + favoriteRouteId);

        FavouriteRideDetailsPage detailsPage = new FavouriteRideDetailsPage(driver);
        detailsPage.waitLoaded(favoriteRouteId);
        detailsPage.waitForRouteData(fixture.happyStartAddress(), fixture.happyDestinationAddress());

        assertTrue(detailsPage.startLineText().contains(fixture.happyStartAddress()));
        assertTrue(detailsPage.endLineText().contains(fixture.happyDestinationAddress()));
        assertEquals(fixture.happyCheckpointCount(), detailsPage.checkpointCount());

        detailsPage.clickOrderAgain();

        OrderRidePage orderPage = new OrderRidePage(driver);
        orderPage.waitLoaded();
        orderPage.waitForPrefill(fixture.happyStartAddress(), fixture.happyDestinationAddress());

        assertEquals(fixture.happyStartAddress(), orderPage.fromAddressValue());
        assertEquals(fixture.happyDestinationAddress(), orderPage.toAddressValue());
        assertEquals(fixture.happyCheckpointCount(), orderPage.checkpointCount());

        orderPage.submit();
        orderPage.waitForOutcome();

        assertTrue(orderPage.hasSuccessMessage(), "Ride order failed with message: " + orderPage.errorText());
        assertTrue(orderPage.successText().contains("Order placed."));
        assertEquals(1, fixture.countRidesForPassengerOnHappyRoute());

        long rideId = fixture.latestRideIdForPassengerOnHappyRoute();
        assertTrue(rideId > 0);
        assertEquals("ACCEPTED", fixture.rideStatus(rideId));
        assertEquals(fixture.happyCheckpointCount(), fixture.countRideStops(rideId));
    }

    @Test
    void missingFavorite_shouldShowErrorAndPreventOrderCreation() {
        long missingFavoriteRouteId = fixture.prepareMissingFavoriteScenario();

        loginAsPassenger();
        driver.get(frontendUrl + "/user/favourite-rides/" + missingFavoriteRouteId);

        FavouriteRideDetailsPage detailsPage = new FavouriteRideDetailsPage(driver);
        detailsPage.waitLoaded(missingFavoriteRouteId);
        detailsPage.waitForError();
        String firstError = detailsPage.errorText();
        assertTrue(
                firstError.contains("Favorite route not found")
                        || firstError.contains("Failed to load favourite ride details."),
                "Unexpected error text: " + firstError
        );

        detailsPage.clickOrderAgain();
        detailsPage.waitForError();
        assertTrue(detailsPage.errorText().contains("Route is not ready for ordering (missing coordinates)."));
        assertTrue(driver.getCurrentUrl().contains("/user/favourite-rides/" + missingFavoriteRouteId));

        assertEquals(0, fixture.countRidesForPassengerOnHappyRoute());
    }

    @Test
    void emptyFavorites_shouldRenderEmptyStateWithoutDetailsAction() {
        fixture.prepareEmptyFavoritesScenario();

        loginAsPassenger();
        driver.get(frontendUrl + "/user/favourite-rides");

        FavouriteRidesPage ridesPage = new FavouriteRidesPage(driver);
        ridesPage.waitLoaded();
        ridesPage.waitForEmptyState();

        assertFalse(ridesPage.hasRows());
        assertFalse(ridesPage.hasDetailsButtons());
        assertEquals("No favourite routes yet.", ridesPage.emptyStateText());
        assertEquals(0, fixture.countFavoritesForPassenger());
    }

    private void loginAsPassenger() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.openLogin(frontendUrl);
        loginPage.login(fixture.passengerEmail(), fixture.passengerPassword());

        UserHomePage homePage = new UserHomePage(driver);
        homePage.waitLoaded();
    }
}
