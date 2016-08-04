package net.serenitybdd.cucumber.adapter;

import gherkin.JSONParser;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import net.serenitybdd.cucumber.SerenityReporter;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.reports.adaptors.common.FilebasedOutcomeAdaptor;
import net.thucydides.core.util.SystemEnvironmentVariables;
import net.thucydides.core.webdriver.SystemPropertiesConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CucumberJsonAdapter extends FilebasedOutcomeAdaptor {
    @Override
    public List<TestOutcome> loadOutcomesFrom(File source) throws IOException {
        String s = FileUtils.readFileToString(source);

        CucumberJsonReporterFormatter cucumberJSONFormatter=new CucumberJsonReporterFormatter();
        JSONParser parser = new JSONParser(cucumberJSONFormatter, cucumberJSONFormatter);
        parser.parse(s);
        return cucumberJSONFormatter.getTestOutcomes();
    }
}
