package net.serenitybdd.cucumber.integration;

import cucumber.api.CucumberOptions;
import gherkin.formatter.JSONFormatter;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

/**
 * Created by john on 23/07/2014.
 */
@RunWith(CucumberWithSerenity.class)
@CucumberOptions(features="src/test/resources/samples/simple_scenario_with_tags.feature", plugin = "gherkin.formatter.JSONFormatter:SimpleScenarioWithTags.json")
public class SimpleScenarioWithTags {}
