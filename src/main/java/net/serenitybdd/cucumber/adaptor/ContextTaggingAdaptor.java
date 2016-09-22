package net.serenitybdd.cucumber.adaptor;

import com.google.common.collect.Lists;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestOutcomeSummary;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.reports.JiraUpdaterService;
import net.thucydides.core.reports.TestOutcomeStream;
import net.thucydides.core.reports.adaptors.ExtendedTestOutcomeAdaptor;
import net.thucydides.core.reports.adaptors.common.FilebasedOutcomeAdaptor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class ContextTaggingAdaptor extends FilebasedOutcomeAdaptor implements ExtendedTestOutcomeAdaptor {


    private String sourceContext;

    @Override
    public List<TestOutcome> loadOutcomesFrom(File sourceDir) throws IOException {
        List<TestOutcome> testOutcomes = Lists.newArrayList();
        Path directory = Paths.get(sourceDir.getAbsolutePath());
        try (TestOutcomeStream stream = TestOutcomeStream.testOutcomesInDirectory(directory)) {
            for (TestOutcome outcome : stream) {
                outcome.addTag(TestTag.withValue(sourceContext));
                testOutcomes.add(outcome);
            }
        }
        return testOutcomes;
    }

    @Override
    public void setSourceContext(String sourceContext) {
        this.sourceContext=sourceContext;
    }

    @Override
    public void copySupportingResourcesTo(List<TestOutcome> outcomes, File targetDirectory) throws IOException {

    }
}
