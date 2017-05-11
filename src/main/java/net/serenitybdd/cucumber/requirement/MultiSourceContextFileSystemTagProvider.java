package net.serenitybdd.cucumber.requirement;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import gherkin.formatter.model.Tag;
import net.serenitybdd.cucumber.adaptor.Contextualizer;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.requirements.FileSystemRequirementsTagProvider;
import net.thucydides.core.requirements.RequirementsTagProvider;
import net.thucydides.core.requirements.model.FeatureType;
import net.thucydides.core.requirements.model.Requirement;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.core.util.NameConverter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MultiSourceContextFileSystemTagProvider extends FileSystemRequirementsTagProvider {
    private Contextualizer contextualizer = new Contextualizer();
    private HashMap<String, Requirement> requirementMap;
    private Map<String, List<TestTag>> requirementTagMap;

    public MultiSourceContextFileSystemTagProvider(EnvironmentVariables environmentVariables) {
        super(environmentVariables);
    }

    public MultiSourceContextFileSystemTagProvider(EnvironmentVariables environmentVariables, String rootDirectoryPath) {
        super(environmentVariables, rootDirectoryPath);
    }

    public MultiSourceContextFileSystemTagProvider() {
    }

    public MultiSourceContextFileSystemTagProvider(String rootDirectory, int level) {
        super(rootDirectory, level);
    }

    public MultiSourceContextFileSystemTagProvider(String rootDirectory, int level, EnvironmentVariables environmentVariables) {
        super(rootDirectory, level, environmentVariables);
    }

    public MultiSourceContextFileSystemTagProvider(String rootDirectory) {
        super(rootDirectory);
    }

    public MultiSourceContextFileSystemTagProvider(String path, int i, EnvironmentVariables environmentVariables, Map<String, List<TestTag>> requirementTagMap, HashMap<String, Requirement> requirementMap) {
        this(path, i, environmentVariables);
        this.requirementTagMap = requirementTagMap;
        this.requirementMap = requirementMap;
    }

    @Override
    public List<Requirement> getRequirements() {
        if (this.requirementTagMap == null) {
            this.requirementTagMap = new HashMap<>();
        }
        List<Requirement> requirements = super.getRequirements();
        if (requirementMap == null) {
            this.requirementMap = new HashMap<String, Requirement>();
            putRequirements(requirements);

        }
        return requirements;
    }

    private void putRequirements(List<Requirement> requirements) {
        for (Requirement requirement : requirements) {
            this.requirementMap.put(requirement.qualifiedName(), requirement);
            this.putRequirements(requirement.getChildren());
        }
    }

    @Override
    public Optional<Requirement> getParentRequirementOf(TestOutcome testOutcome) {
        Optional<Requirement> result = super.getParentRequirementOf(testOutcome);
        if (result.isPresent()) {
            return result;
        } else if (testOutcome.getUserStory() == null) {
            return result;
        } else {
            String qualifiedName = decontextualizedStoryName(testOutcome) + "/" + testOutcome.getUserStory().getName();
            result = Optional.fromNullable(this.requirementMap.get(qualifiedName));
            if (result.isPresent()) {
                System.out.println("Found " + qualifiedName);
            }
            if (!result.isPresent()) {
                if (testOutcome.getQualifier() != null && testOutcome.getQualifier().isPresent()) {//Isn't it ironic
                    String decontextualizedPath = parentHumanName(testOutcome) + "/" + decontextualizedStoryName(testOutcome);
                    Requirement parentRequirement = this.requirementMap.get(decontextualizedPath);
                    if (parentRequirement != null) {
                        String cardNumber = null;
                        List<TestTag> parentTags = this.requirementTagMap.get(parentRequirement.getFeatureFileName());
                        if (parentTags != null) {
                            for (TestTag parentTag : parentTags) {
                                if (parentTag.getName().startsWith("@" + testOutcome.getQualifier().get())) {
                                    String[] split = parentTag.getName().split("_");
                                    if (split.length > 1) {
                                        cardNumber = split[1];
                                    }
                                }
                            }
                        }
                        List<Requirement> children = new ArrayList<>(parentRequirement.getChildren());
                        //NB!! Use UserStory.getName because if we use UserStory.getId it does not match it up with the tag
                        Requirement child = Requirement.named(testOutcome.getUserStory().getName()).
                                withOptionalDisplayName(testOutcome.getUserStory().getName()).
                                withOptionalCardNumber(cardNumber).
                                withType(testOutcome.getUserStory().getType()).
                                withNarrative(testOutcome.getUserStory().getNarrative()).
                                withParent(parentRequirement.getName()).
                                withFeatureFileyName(testOutcome.getUserStory().getPath());
                        children.add(child);
                        this.requirementMap.put(child.qualifiedName(), child);
                        parentRequirement.setChildren(children);
                        result = Optional.of(child);
                    } else {
//                    System.out.println("Could not find parent requirement " + path);
                    }
                } else {
//                System.out.println(path + " not available");
                }
            }
            return result;
        }
    }

    private String parentHumanName(TestOutcome testOutcome) {
        String path = testOutcome.getUserStory().getPath();
        path = path.substring(0, path.lastIndexOf('/'));
        return NameConverter.humanize(path.substring(path.lastIndexOf('/') + 1));
    }

    protected List<Requirement> readChildrenFrom(File requirementDirectory) {
        String childDirectory = rootDirectory + "/" + requirementDirectory.getName();
        if (childrenExistFor(childDirectory)) {
            RequirementsTagProvider childReader = new MultiSourceContextFileSystemTagProvider(childDirectory, level + 1, environmentVariables, requirementTagMap, requirementMap);
            return childReader.getRequirements();
        } else if (childrenExistFor(requirementDirectory.getPath())) {
            RequirementsTagProvider childReader = new MultiSourceContextFileSystemTagProvider(requirementDirectory.getPath(), level + 1, environmentVariables, requirementTagMap, requirementMap);
            return childReader.getRequirements();
        } else {
            return Lists.newArrayList();
        }
    }

    private String decontextualizedStoryName(TestOutcome testOutcome) {
        return contextualizer.decontextualizeName(testOutcome.getQualifier().get(), testOutcome.getUserStory().getName());
    }

    public Requirement readRequirementsFromStoryOrFeatureFile(File storyFile) {
        FeatureType type = featureTypeOf(storyFile);
        Requirement requirement = super.readRequirementsFromStoryOrFeatureFile(storyFile);
        List<TestTag> tags = new ArrayList<TestTag>();
        if (type == FeatureType.FEATURE) {
            CucumberTagParser cucumberTagParser = new CucumberTagParser(readLocaleFromFeatureFile(storyFile), environmentVariables);
            try {
                cucumberTagParser.parse(storyFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Tag tag : cucumberTagParser.getTags()) {
                tags.add(TestTag.withName(tag.getName()).andType("tag"));
            }
        }
        requirementTagMap.put(requirement.getFeatureFileName(), tags);
        return requirement;

    }

    private String readLocaleFromFeatureFile(File storyFile) {
        try {
            List<String> featureFileLines = FileUtils.readLines(storyFile);
            for (String line : featureFileLines) {
                if (line.startsWith("#") && line.contains("language:")) {
                    return line.substring(line.indexOf("language:") + 10).trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Locale.getDefault().getLanguage();
    }

    private FeatureType featureTypeOf(File storyFile) {
        return (storyFile.getName().endsWith("." + STORY_EXTENSION)) ? FeatureType.STORY : FeatureType.FEATURE;
    }

}
