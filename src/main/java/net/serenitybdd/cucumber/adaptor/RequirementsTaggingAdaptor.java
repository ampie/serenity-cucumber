package net.serenitybdd.cucumber.adaptor;

import com.google.common.collect.Lists;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.reports.TestOutcomeStream;
import net.thucydides.core.reports.adaptors.ExtendedTestOutcomeAdaptor;
import net.thucydides.core.reports.adaptors.common.FilebasedOutcomeAdaptor;
import net.thucydides.core.requirements.RequirementsProviderService;
import net.thucydides.core.requirements.RequirementsTagProvider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * An adapter that adds all the requirement based tags extracted from the REquirementsProvider to TestOutcomes
 */
public class RequirementsTaggingAdaptor extends FilebasedOutcomeAdaptor implements ExtendedTestOutcomeAdaptor {

    @Override
    public List<TestOutcome> loadOutcomesFrom(File sourceDir) throws IOException {
        List<TestOutcome> testOutcomes = Lists.newArrayList();
        Path directory = Paths.get(sourceDir.getAbsolutePath());
        try (TestOutcomeStream stream = TestOutcomeStream.testOutcomesInDirectory(directory)) {
            for (TestOutcome outcome : stream) {
                if (shouldRetain(outcome)) {
                    outcome.addTags(getTagsUsingRequirementsProviders(outcome));
                    testOutcomes.add(outcome);
                }
            }
        }
        return testOutcomes;
    }

    private List<TestTag> getTagsUsingRequirementsProviders(TestOutcome outcome) {
        try {
            Method getTagsUsingTagProviders = TestOutcome.class.getDeclaredMethod("getTagsUsingTagProviders", List.class);
            getTagsUsingTagProviders.setAccessible(true);
            List<RequirementsTagProvider> requirementsProviders = Injectors.getInjector().getInstance(RequirementsProviderService.class).getRequirementsProviders();
            Set<TestTag> tags = (Set<TestTag>) getTagsUsingTagProviders.invoke(outcome, requirementsProviders);
            return new ArrayList<>(tags);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private boolean shouldRetain(TestOutcome outcome) {
        return outcome.getUserStory() != null && !"scope".equals(outcome.getUserStory().getType()) && !"undefined".equals(outcome.getUserStory().getType()) && outcome.getUserStory().getType() != null;
    }

    @Override
    public void setSourceContext(String sourceContext) {

    }

    @Override
    public void setScenarioStatus(String scenarioStatus) {

    }

    @Override
    public void copySupportingResourcesTo(List<TestOutcome> outcomes, File targetDirectory) throws IOException {

    }
}
