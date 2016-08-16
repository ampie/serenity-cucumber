package net.serenitybdd.cucumber.adapter;


import com.google.common.base.Strings;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import gherkin.formatter.model.DataTableRow;
import net.thucydides.core.model.*;
import net.thucydides.core.model.features.ApplicationFeature;
import net.thucydides.core.model.stacktrace.FailureCause;
import net.thucydides.core.screenshots.ScreenshotAndHtmlSource;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class CucumberJsonReporterFormatter extends CucumberContextualFormatter implements Reporter, DetailedFormatter {
    private List<TestOutcome> testOutcomes = new ArrayList<>();
    private Story currentUserStory;
    private int embeddedCount = 0;
    private String currentUri;
    private int exampleCount = 0;
    private String currentScenarioOutline;
    private boolean processingChildSteps;
    private Map<String, ApplicationFeature> applicationFeatureMap = new HashMap<String, ApplicationFeature>();

    @Override
    public void syntaxError(String s, String s1, List<String> list, String s2, Integer integer) {

    }

    @Override
    public void uri(String s) {
        this.currentUri = s;
    }

    @Override
    public void feature(Feature feature) {
        ApplicationFeature applicationFeature = null;
        String parentRequirement = CucumberTagName.PARENT_REQUIREMENT.valueOn(feature);
        if (parentRequirement != null) {
            applicationFeature = applicationFeatureMap.get(parentRequirement);
            if (applicationFeature == null) {
                applicationFeatureMap.put(parentRequirement, applicationFeature = new ApplicationFeature(parentRequirement, parentRequirement));
            }
        }
        this.currentUserStory = new Story(contextualizeId(feature.getId()), contextualizeName(feature.getName()), null, currentUri, applicationFeature, feature.getDescription(), "story");
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
        String name = ensureUniqueName(scenario);
        TestOutcome outcome = new TestOutcome(contextualizeName(name)).withIssues(CucumberTagName.ISSUE.valuesOn(scenario));
        outcome.setUserStory(currentUserStory);
        this.testOutcomes.add(outcome);
        for (Tag tag : scenario.getTags()) {
            outcome.addTag(TestTag.withValue(tag.getName()));
        }
        outcome.addVersions(CucumberTagName.VERSION.valuesOn(scenario));
        outcome.setDescription(scenario.getDescription());
    }

    private String ensureUniqueName(Scenario scenario) {
        if (scenario.getKeyword().equals("Scenario Outline")) {
            if (scenario.getName().equals(currentScenarioOutline)) {
                exampleCount++;
            } else {
                currentScenarioOutline = scenario.getName();
                exampleCount = 1;
            }
            return scenario.getName() + ", Example " + exampleCount;
        } else {
            currentScenarioOutline = null;
        }
        return scenario.getName();
    }

    @Override
    public void step(Step step) {
        if (processingChildSteps) {
            currentOutcome().endGroup();
            processingChildSteps = false;
        }
        currentOutcome().recordStep(new TestStep(stepTitleFrom(step)));
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
        TestStep step = currentOutcome.currentStep();
        step.setResult(toTestResult(result));
        step.setDuration(result.getDuration() / 1000000);
        if (!Strings.isNullOrEmpty(result.getErrorMessage())) {
            String[] lines = result.getErrorMessage().split("\\\n");
            String message = lines[0];
            String errorType = lines[0];
            if (message.trim().endsWith(")") && message.lastIndexOf("(") > 0) {
                message = message.substring(0, message.lastIndexOf("(") - 1);
                errorType = errorType.substring(errorType.lastIndexOf("(") + 1, errorType.length() - 1);
            }
            StackTraceElement[] stackTrace = buildStackTrace(lines);
            FailureCause fc = new FailureCause(errorType, message, stackTrace);
            try {
                Field field = TestStep.class.getDeclaredField("exception");
                field.setAccessible(true);
                field.set(step, fc);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("should not happen");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("should not happen");
            }
        }
        currentOutcome.calculateDynamicFieldValues();
    }

    private StackTraceElement[] buildStackTrace(String[] lines) {
        StackTraceElement[] result = new StackTraceElement[lines.length - 1];
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String fileName = line.substring(0, line.indexOf(":"));
            line = line.substring(fileName.length() + 1);
            String lineNumberText = line.substring(0, line.indexOf(":in "));
            String methodName = line.substring(lineNumberText.length() + 4);
            result[i - 1] = new StackTraceElement(new File(fileName).getName(), methodName, fileName, Integer.valueOf(lineNumberText));
        }
        return result;

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
            return TestResult.PENDING;
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
    public void embedding(String mimeType, byte[] bytes) {
        if (mimeType.contains("/")) {
            try {
                String outputDir = CucumberJsonAdapter.getSerenityJsonDir();
                File dir = new File(outputDir);
                dir.mkdirs();
                File file = new File(dir, contextualizeId((embeddedCount++) + "") + "." + mimeType.split("/")[1]);
                FileUtils.writeByteArrayToFile(file, bytes);
                currentOutcome().currentStep().addScreenshot(new ScreenshotAndHtmlSource(file, null));
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

    @Override
    public void childStep(String name) {
        if (!this.processingChildSteps) {
            this.processingChildSteps = true;
            currentOutcome().startGroup();
        }
        currentOutcome().recordStep(new TestStep(name));
    }
}
