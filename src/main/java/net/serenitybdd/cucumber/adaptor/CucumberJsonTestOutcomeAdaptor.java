package net.serenitybdd.cucumber.adaptor;

import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.screenshots.ScreenshotAndHtmlSource;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CucumberJsonTestOutcomeAdaptor extends CucumberJsonAdaptor {

    @Override
    public List<TestOutcome> loadOutcomesFrom(File sourceDir) throws IOException {
        File dir = makeTempDirectory(sourceDir);
        CucumberTestOutcomeExtractor cucumberJSONFormatter = new CucumberTestOutcomeExtractor(dir);
        File[] jsonFiles = sourceDir.listFiles(thatEndWithJson());
        Arrays.sort(jsonFiles, alphabetically());
        for (File jsonFile : jsonFiles) {
            String s = FileUtils.readFileToString(jsonFile);
            cucumberJSONFormatter.setSourceContext(determineSourceContext(jsonFile));
            JsonParser parser = new JsonParser(cucumberJSONFormatter, cucumberJSONFormatter);
            parser.parse(s);
        }
        return cucumberJSONFormatter.getTestOutcomes();
    }

    private File makeTempDirectory(File sourceDir) {
        String dirName = sourceDir.getName() + ((int) (Math.random() * 10000000));
        File dir = new File(System.getProperty("java.io.tmpdir"), dirName);
        dir.mkdirs();
        dir.deleteOnExit();
        return dir;
    }


    @Override
    public void copySupportingResourcesTo(List<TestOutcome> outcomes, File targetDirectory) throws IOException {
        for (TestOutcome outcome : outcomes) {
            for (ScreenshotAndHtmlSource screenshot : outcome.getScreenshotAndHtmlSources()) {
                File file = screenshot.getScreenshot();
                System.out.println("Writing file: " + new File(targetDirectory, file.getName()));
                FileUtils.copyFile(file, new File(targetDirectory, file.getName()));
            }
        }

    }
}
