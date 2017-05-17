package net.serenitybdd.cucumber.adapter;

import gherkin.JSONParser;
import net.serenitybdd.cucumber.SerenityReporter;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.reports.adaptors.TestOutcomeAdaptor;
import net.thucydides.core.webdriver.Configuration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CucumberJsonAdapter implements TestOutcomeAdaptor {
    @Override
    public List<TestOutcome> loadOutcomes() throws IOException {
        return Collections.emptyList();
    }

    @Override
    public List<TestOutcome> loadOutcomesFrom(File source) throws IOException {
        if(source.exists()){
            if(source.isDirectory()){
                return load(source.listFiles(thatHaveJsonExtensions()));
            }else{
                return load(new File[]{source});
            }
        }else{
            throw new FileNotFoundException(source.getAbsolutePath());
        }
    }

    private List<TestOutcome> load(File[] files) throws IOException {
        List<TestOutcome> result = new ArrayList<>();
        for (File file : files) {
            result.addAll(testOutcomesExtractedFrom(file));
        }
        return result;
    }

    private Collection<? extends TestOutcome> testOutcomesExtractedFrom(File file) throws IOException {
        Configuration systemConfiguration = Injectors.getInjector().getInstance(Configuration.class);
        SerenityReporter reporter = new SerenityImportingReporter(systemConfiguration);
        JSONParser parser = new JSONParser(reporter, reporter);
        parser.parse(FileUtils.readFileToString(file, Charset.defaultCharset()));
        return reporter.getAllTestOutcomes();
    }

    FileFilter thatHaveJsonExtensions(){
        return new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".json");
            }
        };
    }
}
