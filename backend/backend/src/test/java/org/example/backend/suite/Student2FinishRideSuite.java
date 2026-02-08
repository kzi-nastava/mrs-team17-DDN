package org.example.backend.suite;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Student 2 Backend Tests (2.7 - Finish Ride)")
@SelectPackages("org.example.backend")
@IncludeClassNamePatterns({
        ".*DriverRideServiceTest",
        ".*JdbcDriverRepositoryFinishTest",
        ".*JdbcRideRepositoryFinishTest",
        ".*JdbcDriverRideRepositoryFinishTest",
        ".*RideControllerFinishWebMvcTest",
        ".*DriverControllerFinishWebMvcTest",
        ".*RideControllerFinishIntegrationTest",
        ".*DriverControllerFinishIntegrationTest"
})
@ExcludeClassNamePatterns(".*Suite")
public class Student2FinishRideSuite {
}
