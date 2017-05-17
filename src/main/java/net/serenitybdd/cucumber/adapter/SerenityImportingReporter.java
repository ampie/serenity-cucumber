package net.serenitybdd.cucumber.adapter;

import cucumber.deps.difflib.StringUtills;
import gherkin.formatter.model.*;
import net.serenitybdd.cucumber.SerenityReporter;
import net.thucydides.core.steps.ExecutedStepDescription;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.webdriver.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Generates Thucydides reports, but following the Cucumber JSON import protocol. Most significantly, the Cucumber JSON
 * import protocol implemented by the JSONParser deviates from the runtime protocol since it does not call the following:
 * {@link net.serenitybdd.cucumber.SerenityReporter#startOfScenarioLifeCycle(Scenario)}
 * {@link net.serenitybdd.cucumber.SerenityReporter#endOfScenarioLifeCycle(Scenario)}
 * <p>
 * By implication, it means that {@link SerenityImportingReporter#background(gherkin.formatter.model.Background)} gets
 * called before a TestOutcome has been instantiated.
 */
public class SerenityImportingReporter extends SerenityReporter {

    private static final int NANOS_PER_MILLI = 100000;
    private Background currentBackground;
    private List<Step> currentBackgroundSteps;
    private List<Match> currentBackgroundMatches;
    private List<Result> currentBackgroundResults;
    private Scenario currentScenario;
    private long cumulativeTestDuration;
    private StringBuilder scenarioOutlineText;
    private long cumulativeExampleDuration;

    public SerenityImportingReporter(Configuration systemConfiguration) {
        super(systemConfiguration);
    }

    @Override
    public void background(Background background) {
        if (currentBackground == null) {
            currentBackground = background;
        } else {
            super.background(background);
        }
    }

    @Override
    public void examples(Examples examples) {
        if (this.currentScenario != null) {
            endOfScenarioLifeCycle(currentScenario);
            currentScenario = null;
        }
        if (scenarioOutlineText != null) {
            scenarioOutlineText.append("Examples:");
            scenarioOutlineText.append(examples.getName() == null ? "" : examples.getName());
            scenarioOutlineText.append("\n");
            for (ExamplesTableRow examplesTableRow : examples.getRows()) {
                scenarioOutlineText.append(StringUtills.join(examplesTableRow.getCells(), "|"));
                scenarioOutlineText.append("\n");
            }
        }
        super.examples(examples);
    }

    protected String extractScenarioOutline() {
        return scenarioOutlineText.toString();
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        if (examplesRunning) {
            finishStepRepresentingExample();
        }
        super.endOfScenarioLifeCycle(scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        if (this.currentScenario != null) {
            finishTestOutcome();
            endOfScenarioLifeCycle(currentScenario);
            currentScenario = null;
        }
        this.scenarioOutlineText = new StringBuilder();
        super.scenarioOutline(scenarioOutline);
    }

    @Override
    public void scenario(Scenario scenario) {
        if (this.currentScenario != null) {
            finishTestOutcome();
            endOfScenarioLifeCycle(currentScenario);
        }
        boolean newScenario = currentScenario == null || !scenarioIdFrom(scenario.getId()).equals(scenarioIdFrom(currentScenario.getId()));
        this.currentScenario = scenario;
        if (newScenario || examplesRunning) {
            startOfScenarioLifeCycle(scenario);
            configureDriver(currentFeature);
            clearScenarioResult();
            if (currentBackground != null) {
                background(currentBackground);
                currentBackground = null;
                for (int i = 0; i < currentBackgroundMatches.size(); i++) {
                    step(currentBackgroundSteps.get(i));
//                    match(currentBackgroundMatches.get(i));
//                    result(currentBackgroundResults.get(i));
                }
                for (int i = 0; i < currentBackgroundMatches.size(); i++) {
//                    step(currentBackgroundSteps.get(i));
                    match(currentBackgroundMatches.get(i));
                    //TODO process embeddings here
                    result(currentBackgroundResults.get(i));
                }
                currentBackgroundSteps = null;
                currentBackgroundMatches = null;
                currentBackgroundResults = null;
            }
        }
    }

    @Override
    public void step(Step step) {
        if (currentBackground != null) {
            if (currentBackgroundSteps == null) {
                currentBackgroundSteps = new ArrayList<>();
            }
            currentBackgroundSteps.add(step);
        } else {
            if (scenarioOutlineText != null && addingScenarioOutlineSteps) {
                scenarioOutlineText.append(step.getKeyword());
                scenarioOutlineText.append(" ");
                scenarioOutlineText.append(step.getName());
                scenarioOutlineText.append("\n");
            }
            super.step(step);
        }
    }

    @Override
    public void eof() {
        if (currentScenario != null) {
            finishTestOutcome();
            endOfScenarioLifeCycle(currentScenario);
            currentScenario = null;
        }
    }

    @Override
    public void result(Result result) {
        if (currentBackground != null) {
            if (currentBackgroundResults == null) {
                currentBackgroundResults = new ArrayList<>();
            }
            currentBackgroundResults.add(result);
        } else {
            long duration = result.getDuration() == null ? 0 : result.getDuration();
            cumulativeTestDuration += duration;
            cumulativeExampleDuration += duration;
            StepEventBus.getEventBus().getBaseStepListener().recordStepDuration(duration / NANOS_PER_MILLI);
            Step currentStep = stepQueue.poll();
            if (Result.PASSED.equals(result.getStatus())) {
                StepEventBus.getEventBus().stepFinished();
            } else if (Result.FAILED.equals(result.getStatus())) {
                if (result.getError() == null) {
                    failed(stepTitleFrom(currentStep), extractRootCauseFrom(result.getErrorMessage()));
                } else {
                    failed(stepTitleFrom(currentStep), result.getError());
                }
            } else if (Result.SKIPPED.getStatus().equals(result.getStatus())) {
                StepEventBus.getEventBus().stepIgnored();
            } else if (PENDING_STATUS.equals(result.getStatus())) {
                StepEventBus.getEventBus().stepPending();
            } else if (Result.UNDEFINED.getStatus().equals(result.getStatus())) {
                StepEventBus.getEventBus().stepPending();
            }
        }
    }

    private void finishStepRepresentingExample() {
        StepEventBus.getEventBus().getBaseStepListener().recordStepDuration(cumulativeExampleDuration / NANOS_PER_MILLI);
        StepEventBus.getEventBus().stepFinished();
        cumulativeExampleDuration = 0;
    }

    private void finishTestOutcome() {
        updatePendingResults();
        updateSkippedResults();
        if (!examplesRunning) {
            StepEventBus.getEventBus().getBaseStepListener().recordTestDuration(cumulativeTestDuration / NANOS_PER_MILLI);
            cumulativeTestDuration = 0;
            StepEventBus.getEventBus().testFinished();
        }
    }

    private Throwable extractRootCauseFrom(String errorMessage) {
        if (errorMessage.indexOf(":") > 0) {
            String className = errorMessage.substring(0, errorMessage.indexOf(":"));
            //Attempt to reconstruct the original if possible
            try {
                return (Throwable) Class.forName(className).getConstructor(String.class).newInstance(errorMessage.substring(errorMessage.indexOf(":") + 1));
            } catch (Exception e) {
                //COuld not, just return an AssertionError
            }
        }
        return new AssertionError(errorMessage);
    }

    @Override
    public void match(Match match) {
        if (currentBackground != null) {
            if (currentBackgroundMatches == null) {
                currentBackgroundMatches = new ArrayList<>();
            }
            currentBackgroundMatches.add(match);
        } else {
            Step currentStep = stepQueue.peek();
            String stepTitle = stepTitleFrom(currentStep);
            StepEventBus.getEventBus().stepStarted(ExecutedStepDescription.withTitle(stepTitle));
            StepEventBus.getEventBus().updateCurrentStepTitle(normalized(stepTitle));
        }
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        ServiceLoader<EmbeddingHandler> embeddingHandlers = ServiceLoader.load(EmbeddingHandler.class);
        for (EmbeddingHandler embeddingHandler : embeddingHandlers) {
            embeddingHandler.attemptHandling(mimeType, data);
        }
    }

}
