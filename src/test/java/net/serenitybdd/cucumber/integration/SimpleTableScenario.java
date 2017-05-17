package net.serenitybdd.cucumber.integration;

import cucumber.api.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

/**
 * Created by john on 23/07/2014.
 */
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(features="src/test/resources/samples/simple_table_based_scenario.feature", plugin="gherkin.formatter.JSONFormatter:SimpleTableScenario.json")
public class SimpleTableScenario {}
