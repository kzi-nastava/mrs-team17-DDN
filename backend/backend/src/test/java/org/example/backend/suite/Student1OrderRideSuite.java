package org.example.backend.suite;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Student 1 Backend Tests (2.4.1 - Order Ride)")
@SelectPackages("org.example.backend")
@IncludeClassNamePatterns({
        ".*RideOrderServiceTest",
        ".*JdbcRideOrderRepositoryOrderTest",
        ".*JdbcRidePassengerRepositoryOrderTest",
        ".*JdbcRideStopRepositoryOrderTest",
        ".*JdbcUserLookupRepositoryOrderTest",
        ".*JdbcDriverMatchingRepositoryOrderTest",
        ".*RideOrderControllerWebMvcTest",
        ".*RideOrderControllerIntegrationTest"
})
@ExcludeClassNamePatterns(".*Suite")
public class Student1OrderRideSuite {
}
