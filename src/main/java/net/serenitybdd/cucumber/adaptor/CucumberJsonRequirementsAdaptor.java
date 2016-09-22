package net.serenitybdd.cucumber.adaptor;

import com.google.gson.Gson;
import net.thucydides.core.model.TestOutcome;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CucumberJsonRequirementsAdaptor extends CucumberJsonAdaptor {

    private CucumberRequirementExtractor requirementsFormatter;

    @Override
    public List<TestOutcome> loadOutcomesFrom(File sourceDir) throws IOException {
        File[] jsonFiles = sourceDir.listFiles(thatEndWithJson());
        Arrays.sort(jsonFiles, alphabetically());
        this.requirementsFormatter = new CucumberRequirementExtractor();
        for (File jsonFile : jsonFiles) {
            String s = FileUtils.readFileToString(jsonFile);
            requirementsFormatter.setSourceContext(determineSourceContext(jsonFile));
            JsonParser parser = new JsonParser(requirementsFormatter, requirementsFormatter);
            parser.parse(s);
        }
        requirementsFormatter.linkRequirements();
        return Collections.emptyList();
    }

    protected String determineSourceContext(File jsonFile) {
        String sourceContext = super.determineSourceContext(jsonFile);
        if (sourceContext.equals("requirements")) {
            sourceContext = null;
        }
        return sourceContext;
    }


    @Override
    public void setSourceContext(String sourceContext) {

    }

    @Override
    public void copySupportingResourcesTo(List<TestOutcome> outcomes, File targetDirectory) throws IOException {
        File requirementsFile = new File(targetDirectory, "requirements.json");
        Gson gson = new Gson();
        FileUtils.write(requirementsFile, gson.toJson(new RequirementsHolder(requirementsFormatter.getTopLevelRequirements())));
    }
}
