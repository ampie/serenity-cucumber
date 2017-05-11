package net.serenitybdd.cucumber.requirement;

import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;
import gherkin.parser.Parser;
import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.util.EnvironmentVariables;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class CucumberTagParser implements Formatter{
    private final String locale;
    private final String encoding;

    public CucumberTagParser() {
        this(Injectors.getInjector().getInstance(EnvironmentVariables.class));
    }


    public CucumberTagParser(EnvironmentVariables environmentVariables) {
        this(ThucydidesSystemProperty.FEATURE_FILE_LANGUAGE.from(environmentVariables,"en"), environmentVariables);
    }

    public CucumberTagParser(String locale, EnvironmentVariables environmentVariables) {
        this.locale = locale;
        this.encoding = ThucydidesSystemProperty.FEATURE_FILE_ENCODING.from(environmentVariables, Charset.defaultCharset().name());
    }

    public List<Tag> parse(File narrativeFile) throws IOException {
        String gherkinScenarios = FileUtils.readFileToString(narrativeFile, encoding);
        Parser parser = new Parser(this, true, "root", false, this.locale);
        parser.parse(gherkinScenarios, narrativeFile.getName(),0);
        return getTags();
    }

    private Feature feature;

    public List<Tag> getTags() {
        return feature.getTags();
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {

    }

    @Override
    public void uri(String uri) {

    }

    @Override
    public void feature(Feature feature) {
        this.feature=feature;

    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {

    }

    @Override
    public void examples(Examples examples) {

    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {

    }

    @Override
    public void background(Background background) {

    }

    @Override
    public void scenario(Scenario scenario) {

    }

    @Override
    public void step(Step step) {

    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {

    }

    @Override
    public void done() {

    }

    @Override
    public void close() {

    }

    @Override
    public void eof() {

    }
}
