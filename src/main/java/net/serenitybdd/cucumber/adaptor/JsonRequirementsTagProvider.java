package net.serenitybdd.cucumber.adaptor;


import com.google.common.base.Optional;
import com.google.gson.Gson;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.requirements.RequirementsTagProvider;
import net.thucydides.core.requirements.model.Requirement;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class JsonRequirementsTagProvider implements RequirementsTagProvider {
    List<Requirement> requirements;
    Contextualizer contextualizer = new Contextualizer();
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
                String filePath = System.getProperty("serenity.requirements.json.file");
                if (filePath == null) {
                    requirements = Collections.emptyList();
                } else {
                    File file = new File(filePath);
                    if (file.exists()) {
                        String json = FileUtils.readFileToString(file);
                        RequirementsHolder holder = gson.fromJson(json, RequirementsHolder.class);
                        requirements = holder.getRequirements();
                    } else {
                        requirements = Collections.emptyList();
                    }
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
            this.requirementMap.put(requirement.getDisplayName(), requirement);
            this.putRequirements(requirement.getChildren());
        }
    }

    @Override
    public Optional<Requirement> getParentRequirementOf(TestOutcome testOutcome) {
        if (testOutcome.getUserStory() == null) {
            return Optional.absent();
        }
        maybeLoadRequirements();
        String displayName = testOutcome.getUserStory().getName();
        Optional<Requirement> result = Optional.fromNullable(this.requirementMap.get(displayName));
        if(result.isPresent()){
            System.out.println("Found " + displayName);
        }
        if (!result.isPresent()) {
            if (testOutcome.getQualifier()!=null && testOutcome.getQualifier().isPresent()) {//Isn't it ironic
                displayName =contextualizer.decontextualizeName(testOutcome.getQualifier().get(), displayName);
                Requirement parentRequirement = this.requirementMap.get(displayName);
                if(parentRequirement!=null){
                    List<Requirement> children = new ArrayList<Requirement>(parentRequirement.getChildren());
                    //NB!! Use UserStory.getName because if we use UserStory.getId it does not match it up with the tag
                    Requirement child = Requirement.named(testOutcome.getUserStory().getName()).
                            withOptionalDisplayName(testOutcome.getUserStory().getName()).
                            withType(testOutcome.getUserStory().getType()).
                            withNarrative(testOutcome.getUserStory().getNarrative()).
                            withParent(parentRequirement.getName()).
                            withFeatureFileyName(testOutcome.getUserStory().getPath());
                    children.add(child);
                    this.requirementMap.put(child.getDisplayName(),child);
                    parentRequirement.setChildren(children);
                    result = Optional.of(child);
                }else{
//                    System.out.println("Could not find parent requirement " + displayName);
                }
            } else {
//                System.out.println(displayName + " not available");
            }
        }
        return result;
    }

    @Override
    public Optional<Requirement> getParentRequirementOf(Requirement requirement) {
        return Optional.of(this.requirementMap.get(requirement.getParent()));
    }


    @Override
    public Optional<Requirement> getRequirementFor(TestTag testTag) {
        maybeLoadRequirements();
        Requirement nullableReference = this.requirementMap.get(testTag.getName());
        return Optional.fromNullable(nullableReference);
    }

    @Override
    public Set<TestTag> getTagsFor(TestOutcome testOutcome) {
        maybeLoadRequirements();
        Set<TestTag> result = new HashSet<TestTag>();
        Optional<Requirement> parentRequirement = getParentRequirementOf(testOutcome);
        if (parentRequirement.isPresent()) {
            result.add(TestTag.withValue(parentRequirement.get().getType() +":" +parentRequirement.get().getDisplayName()));
            addParents(result, parentRequirement.get());
        }
        return result;
    }

    private void addParents(Set<TestTag> result, Requirement child) {
        for (Requirement parent : this.requirementMap.values()) {
            if (parent.getChildren().contains(child)) {
                result.add(TestTag.withValue(parent.getType() +":" +parent.getDisplayName()));
                addParents(result, parent);
            }
        }
    }
}
