package net.serenitybdd.cucumber.adaptor;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import gherkin.formatter.model.Feature;
import net.thucydides.core.model.Story;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestTag;
import net.thucydides.core.model.features.ApplicationFeature;

import javax.annotation.Nullable;
import java.util.Set;

public class Contextualizer {
    private TagRemover tagRemover = new TagRemover();
        private String sourceContext;
    private String scenarioStatus;
    public void setSourceContext(String ctx){
        this.sourceContext=ctx;
    }
    protected String contextualizeId(String id) {
        return sourceContext==null?id:id+"-"+sourceContext;
    }
    protected String contextualizePath(String path) {
        if(path!=null && path.indexOf('.')>0) {
            String s = path.substring(0, path.lastIndexOf('.')) + "(" + sourceContext + ")" + path.substring(path.lastIndexOf('.'));
            return s;
        }else{
            return contextualizeName(path);
        }

    }

    protected String contextualizeName(String name) {
        return sourceContext==null?name:name+" ("+sourceContext +")";
    }
    protected String typeOf(Feature feature) {
        String s = CucumberTagName.REQUIREMENT_TYPE.valueOn(feature);
        return s==null?"feature":s;
    }

    public String getSourceContext() {
        return sourceContext;
    }

    public String getScenarioStatus() {
        return scenarioStatus;
    }

    public void setScenarioStatus(String scenarioStatus) {
        this.scenarioStatus = scenarioStatus;
    }

    public TestOutcome contextualize(TestOutcome outcome) {
        outcome = outcome.withMethodName(contextualizeName(outcome.getName()));
        outcome.setTitle(contextualizeName(outcome.getTitle()));
        Story story = outcome.getUserStory();
        String requirementType = story.getType();
        if(getSourceContext()!=null){
            requirementType = "Low Level " + requirementType;
        }
        ApplicationFeature feature = story.getFeature();
        if(feature==null && getSourceContext() !=null){
            feature=new ApplicationFeature(story.getName(),story.getName());
        }

        Story newStory = new Story(
                contextualizeId(story.getId()),
                contextualizeName(story.getName()),
                null,
                contextualizePath(story.getPath()),
                null,
                story.getNarrative(),
                requirementType);
        //NB!!! remove tags just before setting story
        Set<TestTag> tags = tagRemover.removeIgnoredTags(outcome);
        tags.remove(outcome.getFeatureTag().get());
        outcome.setTags(tags);
        outcome.setUserStory(newStory);
        if(getSourceContext() != null) {
            outcome.addTag(TestTag.withValue("Source Context:" + getSourceContext()));
        }
        if(getScenarioStatus()!=null) {
            outcome.addTag(TestTag.withValue("Scenario Status:" + getScenarioStatus()));
        }
//        System.out.println(outcome.getName()+" tags: " + outcome.getTags());
        return outcome.withQualifier(getSourceContext());
    }

    public String decontextualizePath(String qualifier, String path) {
        if(path!=null && path.indexOf('.')>0) {
            String s = path.substring(0, path.lastIndexOf('.') - (qualifier.length() + 2)) + path.substring(path.lastIndexOf('.'));
            return s;
        }else{
            return decontextualizeName(qualifier, path);
        }

    }

    public String decontextualizeName(String qualifier, String name) {
        return name.substring(0, name.length() - (qualifier.length() + 3));
    }
}
