package net.serenitybdd.cucumber.adaptor;


import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import net.thucydides.core.requirements.model.Requirement;

import java.util.*;

public class CucumberRequirementExtractor implements Reporter, DetailedFormatter {
    public static final String SOURCE_CONTEXTS = "serenity.test.source.contexts";
    public static final String SCENARIO_STATUS = "serenity.scenario.statuses";
    Map<Feature, Requirement> requirements = new HashMap<Feature, Requirement>();
    private String currentUri;
    private ArrayList<Requirement> topLevelRequirements;
    private Contextualizer contextualizer;

    @Override
    public void syntaxError(String s, String s1, List<String> list, String s2, Integer integer) {

    }

    public void linkRequirements() {
        for (Requirement r : requirements.values()) {
            if (r.getChildrenCount() == 0) {
                r.setChildren(findChildren(r.getName(), r.getFeatureFileName()));
            }
        }
        topLevelRequirements = new ArrayList<Requirement>();
        for (Requirement r : requirements.values()) {
            if (isTopLevel(r)) {
                topLevelRequirements.add(r);
            }
        }
        Map<String,List<Requirement>> parentUris = extractParentUris();
        while(parentUris.size()>1){
            topLevelRequirements = new ArrayList<>();
            for (Map.Entry<String, List<Requirement>> entry: parentUris.entrySet()) {
                String name = chopOffDirectoryName(entry.getKey());
                Requirement capability = Requirement.named(name).withType("Capability").withNarrative(name).withFeatureFileyName(entry.getKey()).withDisplayName(name);
                topLevelRequirements.add(capability);
                capability.setChildren(entry.getValue());
            }
            parentUris = extractParentUris();
        }
    }

    private Map<String, List<Requirement>> extractParentUris() {
        Map<String,List<Requirement>> parentUris = new HashMap<>();
        for (Requirement topLevelRequirement : topLevelRequirements) {
            String parentPath = chopOffFilename(topLevelRequirement.getFeatureFileName());
            List<Requirement> list = parentUris.get(parentPath);
            if(list ==null){
                parentUris.put(parentPath,list=new ArrayList<Requirement>());
            }
            list.add(topLevelRequirement);
        }
        return parentUris;
    }

    private boolean isTopLevel(Requirement r) {
        for (Requirement parent : requirements.values()) {
            if (parent.getChildren().contains(r)) {
                return false;
            }
        }
        return true;
    }

    public List<Requirement> getTopLevelRequirements() {
        return topLevelRequirements;
    }

    private List<Requirement> findChildren(String parentId, String parentPath) {
        List<Requirement> result = new ArrayList<Requirement>();
        for (Map.Entry<Feature, Requirement> childEntry : requirements.entrySet()) {
            String childFileName = childEntry.getValue().getFeatureFileName();
            if (inDirectChildFolder(parentPath, childFileName) || isTaggedAsChild(parentId, childEntry)) {
                result.add(childEntry.getValue());
            }
        }
        return result;
    }

    private static boolean inDirectChildFolder(String parentFileName, String childFileName) {
        String parentFolder = chopOffFilename(parentFileName);
        String childFolder = chopOffFilename(childFileName);
        return childStartWithParent(parentFolder, childFolder) && childSuffixAtomic(parentFolder, childFolder);
    }

    private static boolean childSuffixAtomic(String parentFolder, String childFolder) {
        return lastSeperatorIndex(childFolder.substring(parentFolder.length() + 1)) < 0;
    }

    private static boolean childStartWithParent(String parentFolder, String childFolder) {
        return parentFolder.length() < childFolder.length()
                && childFolder.startsWith(parentFolder);
    }

    private static String chopOffFilename(String fileName) {
        return fileName.substring(0, lastSeperatorIndex(fileName));
    }
    private static String chopOffDirectoryName(String fileName) {
        return fileName.substring(lastSeperatorIndex(fileName)+1);
    }

    private static int lastSeperatorIndex(String fileName) {
        int lastIndexOf = -1;
        for (int i = fileName.length() - 1; i >= 0; i--) {
            char c = fileName.charAt(i);
            if (c == '\\' || c == '/') {
                lastIndexOf = i;
                break;
            }
        }
        return lastIndexOf;
    }

    private boolean isTaggedAsChild(String id, Map.Entry<Feature, Requirement> childEntry) {
        return CucumberTagName.PARENT_REQUIREMENT.valuesOn(childEntry.getKey()).contains(id);
    }

    @Override
    public void uri(String s) {
        this.currentUri = s;
    }

    @Override
    public void feature(Feature feature) {
        String id = CucumberTagName.ID.valueOn(feature);
        id = id == null ? feature.getId() : id;
        Requirement requirement = Requirement.named(id)
                .withOptionalDisplayName(feature.getName())
                .withOptionalCardNumber(CucumberTagName.ISSUE.valueOn(feature))
                .withType(contextualizer.typeOf(feature))
                .withNarrative(feature.getDescription())
                .withFeatureFileyName(currentUri)
                .withParent(CucumberTagName.PARENT_REQUIREMENT.valueOn(feature));
        requirements.put(feature, requirement);
    }


    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {

    }

    @Override
    public void examples(Examples examples) {

    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {


    }

    @Override
    public void background(Background background) {

    }

    @Override
    public void scenario(Scenario scenario) {

    }


    @Override
    public void step(Step step) {
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {

    }


    @Override
    public void done() {

    }

    @Override
    public void close() {

    }

    @Override
    public void eof() {

    }

    @Override
    public void before(Match match, Result result) {

    }

    @Override
    public void result(Result result) {

    }

    @Override
    public void after(Match match, Result result) {

    }

    @Override
    public void match(Match match) {

    }

    @Override
    public void embedding(String mimeType, byte[] bytes) {

    }

    @Override
    public void write(String s) {

    }


    @Override
    public void childStep(String name) {

    }

    public void setContextualizer(Contextualizer contextualizer) {
        this.contextualizer = contextualizer;
    }
}
