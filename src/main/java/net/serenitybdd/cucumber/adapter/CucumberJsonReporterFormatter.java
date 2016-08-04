package net.serenitybdd.cucumber.adapter;


import com.google.common.base.Strings;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import gherkin.formatter.model.DataTableRow;
import net.thucydides.core.model.*;
import net.thucydides.core.screenshots.ScreenshotAndHtmlSource;
import org.apache.commons.io.FileUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CucumberJsonReporterFormatter implements Formatter, Reporter {
    private List<TestOutcome> testOutcomes = new ArrayList<>();
    private Story currentUserStory;
    private Step currentStep;
    private int embeddedCount=0;
    private List<ScreenshotAndHtmlSource> currentEmbeddings;
    private String currentUri;

    @Override
    public void syntaxError(String s, String s1, List<String> list, String s2, Integer integer) {

    }

    @Override
    public void uri(String s) {
        this.currentUri=s;
    }

    @Override
    public void feature(Feature feature) {
        this.currentUserStory = Story.withId(feature.getId(), feature.getName()).withPath(currentUri).asFeature();
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
        TestOutcome outcome = new TestOutcome(scenario.getName());
        outcome.setUserStory(currentUserStory);
        this.testOutcomes.add(outcome);
        for (Tag tag : scenario.getTags()) {
            outcome.addTag(TestTag.withValue(tag.getName()));
        }
        if (Strings.isNullOrEmpty(scenario.getDescription())) {
            StringBuilder sb = new StringBuilder();
            for (Comment comment : scenario.getComments()) {
                sb.append(comment.getValue());
                sb.append("\n");
            }
            if (sb.length() > 0) {
                outcome.setDescription(sb.toString());
            }
        } else {
            outcome.setDescription(scenario.getDescription());
        }
    }

    @Override
    public void step(Step step) {
        this.currentStep = step;
        currentEmbeddings=new ArrayList<>();
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {

    }

    private TestOutcome currentOutcome() {
        return this.testOutcomes.get(this.testOutcomes.size() - 1);
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

    @Override
    public void before(Match match, Result result) {

    }

    @Override
    public void result(Result result) {
        TestOutcome currentOutcome = currentOutcome();
        TestStep step = TestStep.forStepCalled(stepTitleFrom(currentStep)).withResult(toTestResult(result));
        step.setDuration(result.getDuration());
        if(!Strings.isNullOrEmpty(result.getErrorMessage())){
            step.failedWith(new Exception(result.getErrorMessage()));
        }
        for (ScreenshotAndHtmlSource embedding : this.currentEmbeddings) {
            step.addScreenshot(embedding);
        }

        currentOutcome.recordStep(step);
    }

    private String stepTitleFrom(Step currentStep) {
        return currentStep.getKeyword()
                + currentStep.getName()
                + embeddedTableDataIn(currentStep);
    }

    private String embeddedTableDataIn(Step currentStep) {
        return (currentStep.getRows() == null || currentStep.getRows().isEmpty()) ?
                "" : convertToTextTable(currentStep.getRows());
    }
    private String convertToTextTable(List<DataTableRow> rows) {
        StringBuilder textTable = new StringBuilder();
        textTable.append(System.lineSeparator());
        for (DataTableRow row : rows) {
            textTable.append("|");
            for (String cell : row.getCells()) {
                textTable.append(" ");
                textTable.append(cell);
                textTable.append(" |");
            }
            if (row != rows.get(rows.size() - 1)) {
                textTable.append(System.lineSeparator());
            }
        }
        return textTable.toString();
    }

    private TestResult toTestResult(Result result) {
        if (Result.UNDEFINED.getStatus().equals(result.getStatus())) {
            return TestResult.UNDEFINED;
        } else if (Result.SKIPPED.getStatus().equals(result.getStatus())) {
            return TestResult.SKIPPED;
        } else if (Result.FAILED.equals(result.getStatus())) {
            return TestResult.FAILURE;
        } else if (Result.PASSED.equals(result.getStatus())) {
            return TestResult.SUCCESS;
        }
        return TestResult.PENDING;
    }

    @Override
    public void after(Match match, Result result) {

    }

    @Override
    public void match(Match match) {

    }

    @Override
    public void embedding(String s, byte[] bytes) {
        if(s.contains("/")){
            try {
                File file = new File("embeddings/" + (embeddedCount++) + "." + s.split("/")[1]);
                FileUtils.writeByteArrayToFile(file,bytes);
                currentEmbeddings.add(new ScreenshotAndHtmlSource(file,null));
            } catch (IOException e) {
                throw new RuntimeException(e);

            }
        }
    }

    @Override
    public void write(String s) {

    }

    public List<TestOutcome> getTestOutcomes() {
        return testOutcomes;
    }
}
