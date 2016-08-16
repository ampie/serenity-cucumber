package net.serenitybdd.cucumber.adapter;


import com.google.common.base.Optional;
import com.google.gson.Gson;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.requirements.RequirementsTagProvider;
import net.thucydides.core.requirements.model.Requirement;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ImportedCucumberTagProvider implements RequirementsTagProvider {
    List<Requirement> requirements;
    private HashMap<String, Requirement> requirementMap;

    @Override
    public List<Requirement> getRequirements() {
        maybeLoadRequirements();
        return requirements;
    }

    private void maybeLoadRequirements() {
        if (requirements == null) {
            try {
                Gson gson = new Gson();
                String outputDir = CucumberJsonAdapter.getSerenityJsonDir();
                File file = new File(new File(outputDir), "requirements.json");
                if (file.exists()) {
                    String json = FileUtils.readFileToString(file);
                    RequirementsHolder holder = gson.fromJson(json, RequirementsHolder.class);
                    requirements = holder.getRequirements();
                }else{
                    requirements= Collections.emptyList();
                }
                this.requirementMap = new HashMap<String, Requirement>();
                List<Requirement> requirements = this.requirements;
                putRequirements(requirements);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void putRequirements(List<Requirement> requirements) {
        for (Requirement requirement : requirements) {
            this.requirementMap.put(requirement.getName(), requirement);
            this.putRequirements(requirement.getChildren());
        }
    }

    @Override
    public Optional<Requirement> getParentRequirementOf(TestOutcome testOutcome) {
        if (testOutcome.getUserStory() == null) {
            System.out.println(testOutcome.getDescription() + " has no story");
            return Optional.absent();
        }
        maybeLoadRequirements();
        Optional<Requirement> result = Optional.fromNullable(this.requirementMap.get(testOutcome.getUserStory().getId()));
        if (!result.isPresent()) {
            System.out.println(testOutcome.getUserStory().getId() + " not available");
        }
        return result;
    }

    @Override
    public Optional<Requirement> getRequirementFor(TestTag testTag) {
        maybeLoadRequirements();
        return Optional.fromNullable(this.requirementMap.get(testTag.getName()));
    }

    @Override
    public Set<TestTag> getTagsFor(TestOutcome testOutcome) {
        maybeLoadRequirements();
        Set<TestTag> result = new HashSet<TestTag>();
        Optional<Requirement> parentRequirement = getParentRequirementOf(testOutcome);
        if (parentRequirement.isPresent()) {
            result.add(parentRequirement.get().asTag());
            addParents(result, parentRequirement.get());
        }
        return result;
    }

    private void addParents(Set<TestTag> result, Requirement child) {
        for (Requirement parent : this.requirementMap.values()) {
            if (parent.getChildren().contains(child)) {
                result.add(parent.asTag());
                addParents(result, parent);
            }
        }
        ;
    }
}
