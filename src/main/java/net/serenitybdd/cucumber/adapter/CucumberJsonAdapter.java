package net.serenitybdd.cucumber.adapter;

import com.google.gson.Gson;
import gherkin.JSONParser;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Step;
import net.serenitybdd.cucumber.SerenityReporter;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestOutcomeSummary;
import net.thucydides.core.reports.JiraUpdaterService;
import net.thucydides.core.reports.TestOutcomeStream;
import net.thucydides.core.reports.adaptors.common.FilebasedOutcomeAdaptor;
import net.thucydides.core.reports.json.JSONTestOutcomeReporter;
import net.thucydides.core.util.SystemEnvironmentVariables;
import net.thucydides.core.webdriver.SystemPropertiesConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class CucumberJsonAdapter extends FilebasedOutcomeAdaptor {
    public static String getSerenityJsonDir() {
        return System.getProperty("serenity.adaptors.cucumber-json.outputdir", "target/site/serenity");
    }

    @Override
    public List<TestOutcome> loadOutcomesFrom(File sourceDir) throws IOException {
        File[] jsonFiles = sourceDir.listFiles(thatEndWithJson());
        Arrays.sort(jsonFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        List<TestOutcome> result = new ArrayList<TestOutcome>();
        CucumberJsonRequirementReporter requirementsFormatter = new CucumberJsonRequirementReporter();
        CucumberJsonReporterFormatter cucumberJSONFormatter = new CucumberJsonReporterFormatter();
        for (File jsonFile : jsonFiles) {
            String s = FileUtils.readFileToString(jsonFile);
            String sourceContext = jsonFile.getName().substring(0, jsonFile.getName().length() - 5);
            if (sourceContext.equals("requirements")) {
                sourceContext = null;
            }
            requirementsFormatter.setSourceContext(sourceContext);
            cucumberJSONFormatter.setSourceContext(sourceContext);
            JsonParser parser = new JsonParser(cucumberJSONFormatter, cucumberJSONFormatter);
            parser.parse(s);
            parser = new JsonParser(requirementsFormatter, requirementsFormatter);
            try {
                parser.parse(s);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        result.addAll(cucumberJSONFormatter.getTestOutcomes());
        Gson gson = new Gson();
        int i = 0;
        for (TestOutcome testOutcome : result) {
            JSONTestOutcomeReporter r = new JSONTestOutcomeReporter();
            File outputDirectory = new File(getSerenityJsonDir(), "tmp");
            outputDirectory.mkdirs();
            r.setOutputDirectory(outputDirectory);
            r.generateReportFor(testOutcome);
        }
        requirementsFormatter.linkRequirements();
        File requirementsFile = new File(new File(getSerenityJsonDir()), "requirements.json");
        FileUtils.write(requirementsFile, gson.toJson(new RequirementsHolder(requirementsFormatter.getTopLevelRequirements())));
        try {
            Iterator<JiraUpdaterService> serviceIterator = ServiceLoader.load(JiraUpdaterService.class).iterator();
            if (serviceIterator.hasNext()) {
                JiraUpdaterService jira = serviceIterator.next();
                jira.updateJiraForTestResultsFrom(new File(getSerenityJsonDir(), "tmp").getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private FilenameFilter thatEndWithJson() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        };
    }
}
