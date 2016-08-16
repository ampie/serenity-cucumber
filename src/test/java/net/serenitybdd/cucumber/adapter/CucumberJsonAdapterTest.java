package net.serenitybdd.cucumber.adapter;


import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestResult;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class CucumberJsonAdapterTest {
    @Test
    public void testLoad() throws Exception {
        List<TestOutcome> testOutcomes = new CucumberJsonAdapter().loadOutcomesFrom(new File("src/test/resources/sample-importing-pom/cucumber-reports"));
        assertThat(testOutcomes.size(), is(equalTo(18)));
        assertSame(testOutcomes.get(0).getUserStory(), testOutcomes.get(1).getUserStory());
        assertThat(testOutcomes.get(0).getUserStory().getName(), is(equalTo("Low Level Feature 1.1.1 (Android)")));
        assertThat(testOutcomes.get(0).getUserStory().getFeature().getId(), is(equalTo("feature_1_1")));
        assertThat(testOutcomes.get(0).getUserStory().getNarrative(), is(equalTo("As a user I want low level feature 1.1.1. so that I derive fantastic benefits")));
        assertThat(testOutcomes.get(0).getName(), is(equalTo("Verify low level feature 1.1.1. from the perspective a monkey (Android)")));
        assertThat(testOutcomes.get(1).getName(), is(equalTo("Verify Something else, Example 1 (Android)")));
        assertThat(testOutcomes.get(2).getName(), is(equalTo("Verify Something else, Example 2 (Android)")));
        assertThat(testOutcomes.get(0).getDescription(), is(equalTo("with a hat\nand maybe a draw organ")));
        assertThat(testOutcomes.get(1).getDescription(), isEmptyOrNullString());
        assertThat(testOutcomes.get(2).getDescription(), isEmptyOrNullString());
        assertThat(testOutcomes.get(0).getIssues().size(), is(equalTo(1)));
        assertThat(testOutcomes.get(0).getIssues(), contains("#23456"));
        assertThat(testOutcomes.get(1).getIssues().size(), is(equalTo(2)));
        assertThat(testOutcomes.get(1).getIssues(), contains("#34567", "#45678"));
        assertThat(testOutcomes.get(2).getIssues().size(), is(equalTo(2)));
        assertThat(testOutcomes.get(2).getIssues(), contains("#34567", "#45678"));
        assertThat(testOutcomes.get(0).getStepCount(), is(equalTo(7)));
        assertThat(testOutcomes.get(0).getTestSteps().get(0).getDescription(), is(equalTo("Given I have the following cards:\n| number | type |\n| 5120-5503-0640-5489 | CREDIT CARD |\n| 5120-5503-0640-1234 | CREDIT CARD |")));
        assertThat(testOutcomes.get(0).getTestSteps().get(0).getScreenshotCount(), is(equalTo(1)));
        assertThat(testOutcomes.get(0).getTestSteps().get(0).getError(), isEmptyOrNullString());
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getDescription(), is(equalTo("And this should fail")));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getScreenshotCount(), is(equalTo(0)));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getError(), is(equalTo("Yeah I told you so")));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getException().getErrorType(), is(equalTo("RuntimeError")));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getException().getMessage(), is(equalTo("Yeah I told you so")));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getException().getStackTrace()[0].getFileName(), endsWith("features/step_definitions/card_steps.rb"));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getException().getStackTrace()[0].getLineNumber(), is(equalTo(13)));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getException().getStackTrace()[0].getMethodName(), is(equalTo("`block (2 levels) in <top (required)>'")));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getChildren().size(),is(equalTo(1)));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getChildren().get(0).getDescription(), is(equalTo("Child step that fails")));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getChildren().get(0).getResult(), is(equalTo(TestResult.FAILURE)));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getChildren().get(0).getError(), is(equalTo("Yeah I told you so")));
        assertThat(testOutcomes.get(0).getTestSteps().get(4).getChildren().get(0).getScreenshotCount(), is(equalTo(1)));
    }
}
