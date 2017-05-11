package net.serenitybdd.cucumber.adaptor;


import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestTag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TagRemover {
    Set<String> ignoredTagTypes = new HashSet<>(Arrays.asList("capability", "low level feature", "feature", "story", "undefined"));
    Set<String> ignoredTagNames = new HashSet<>(Arrays.asList("service_regression", "service_wip", "service_backlog","android_wip", "android_regression","android_backlog", "ios_regression", "ios_wip",  "ios_backlog", "web_regression", "web_wip", "web_backlog", "dev"));

    private boolean shouldRetain(TestTag tag) {
        String name = tag.getName();
        if(name.startsWith("@")){
            name=name.substring(1);
        }
        return !(ignoredTagNames.contains(name.toLowerCase().replace(' ', '_')) || ignoredTagTypes.contains(tag.getType().toLowerCase()));

    }


    public Set<TestTag> removeIgnoredTags(TestOutcome outcome) {
        Set<TestTag> newTags = new HashSet<>();
        for (TestTag testTag : outcome.getTags()) {
            if (shouldRetain(testTag)) {
                newTags.add(testTag);
            }
        }
        //Recalculate the tags from the TagProviders
        newTags.addAll(outcome.withTags(null).getTags());
        return newTags;
    }


}
