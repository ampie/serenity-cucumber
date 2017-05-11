package net.serenitybdd.cucumber.adaptor;

import com.google.gson.Gson;
import net.thucydides.core.model.TestOutcome;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CucumberJsonRequirementsAdaptor extends CucumberJsonAdaptor {

    private CucumberRequirementExtractor requirementsFormatter;

    @Override
    public List<TestOutcome> loadOutcomesFrom(File sourceDir) throws IOException {
        if(!sourceDir.exists()){
            throw new FileNotFoundException(sourceDir.getPath());
        }
        File[] jsonFiles = sourceDir.listFiles(thatEndWithJson());
        Arrays.sort(jsonFiles, alphabetically());
        this.requirementsFormatter = new CucumberRequirementExtractor();
        for (File jsonFile : jsonFiles) {
            String s = FileUtils.readFileToString(jsonFile);
            requirementsFormatter.setContextualizer(determineSourceContext(jsonFile));
            JsonParser parser = new JsonParser(requirementsFormatter, requirementsFormatter);
            parser.parse(s);
        }
        requirementsFormatter.linkRequirements();
        return Collections.emptyList();
    }

    protected Contextualizer determineSourceContext(File jsonFile) {
        Contextualizer sourceContext = super.determineSourceContext(jsonFile);
        if (sourceContext.getSourceContext().equals("requirements")) {
            sourceContext.setSourceContext(null);
        }
        return sourceContext;
    }


    @Override
    public void copySupportingResourcesTo(List<TestOutcome> outcomes, File targetDirectory) throws IOException {
        File requirementsFile = new File(targetDirectory, "requirements.json");
        Gson gson = new Gson();
        FileUtils.write(requirementsFile, gson.toJson(new RequirementsHolder(requirementsFormatter.getTopLevelRequirements())));
    }
}
