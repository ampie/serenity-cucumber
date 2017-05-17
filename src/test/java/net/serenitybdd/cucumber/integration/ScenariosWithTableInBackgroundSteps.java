package net.serenitybdd.cucumber.integration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

/**
 * Created by john on 23/07/2014.
 */
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(features="src/test/resources/samples/scenario_with_table_in_background_steps.feature",plugin = "gherkin.formatter.JSONFormatter:ScenariosWithTableInBackgroundSteps.json")
public class ScenariosWithTableInBackgroundSteps {}
