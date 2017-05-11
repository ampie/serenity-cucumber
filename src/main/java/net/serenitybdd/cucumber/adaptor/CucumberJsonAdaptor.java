package net.serenitybdd.cucumber.adaptor;

import net.thucydides.core.reports.adaptors.ExtendedTestOutcomeAdaptor;
import net.thucydides.core.reports.adaptors.common.FilebasedOutcomeAdaptor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;

public abstract class CucumberJsonAdaptor extends FilebasedOutcomeAdaptor implements ExtendedTestOutcomeAdaptor {
    private Contextualizer contextualizer = new Contextualizer();
    private String sourceContext;

    protected Contextualizer determineSourceContext(File jsonFile) {
        if (sourceContext== null) {
            contextualizer.setSourceContext(jsonFile.getName().substring(0, jsonFile.getName().length() - 5));
        }
        return contextualizer;
    }

    protected Comparator<File> alphabetically() {
        return new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };
    }

    protected FilenameFilter thatEndWithJson() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        };
    }

    @Override
    public void setScenarioStatus(String scenarioStatus) {
        contextualizer.setScenarioStatus(scenarioStatus);
    }

    @Override
    public void setSourceContext(String sourceContext) {
        this.sourceContext=sourceContext;
        contextualizer.setSourceContext(sourceContext);
    }
}
