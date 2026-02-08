package org.example.backend.suite;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("All Backend Tests")
@SelectPackages("org.example.backend")
@IncludeClassNamePatterns({
        ".*Test",
        ".*Tests"
})
@ExcludeClassNamePatterns(".*Suite")
public class AllBackendTestsSuite {
}
