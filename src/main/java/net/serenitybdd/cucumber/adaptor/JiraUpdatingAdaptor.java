package net.serenitybdd.cucumber.adaptor;

import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.reports.JiraUpdaterService;
import net.thucydides.core.reports.adaptors.common.FilebasedOutcomeAdaptor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class JiraUpdatingAdaptor extends FilebasedOutcomeAdaptor {
    @Override
    public List<TestOutcome> loadOutcomesFrom(File sourceDir) throws IOException {
        if (!"true".equals(System.getProperty("serenity.skip.jira.updates"))) {
            try {
                Iterator<JiraUpdaterService> serviceIterator = ServiceLoader.load(JiraUpdaterService.class).iterator();
                if (serviceIterator.hasNext()) {
                    JiraUpdaterService jira = serviceIterator.next();
                    System.out.println("Updating JIRA");
                    jira.updateJiraForTestResultsFrom(sourceDir.getAbsolutePath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return Collections.emptyList();
    }
}
