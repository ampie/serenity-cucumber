package net.serenitybdd.cucumber.adapter

import net.serenitybdd.cucumber.adaptor.Contextualizer
import net.serenitybdd.cucumber.requirement.MultiSourceContextFileSystemTagProvider
import net.thucydides.core.model.Story
import net.thucydides.core.model.TestOutcome
import net.thucydides.core.model.TestTag
import net.thucydides.core.requirements.FileSystemRequirementsTagProvider
import net.thucydides.core.requirements.model.Requirement
import spock.lang.Specification

class WhenUsingAMultiContextFileSystemRequirementsProvider  extends Specification{
    def "should dynamically build the requirement of a test outcome eminating from a specific source context"() {
        given:
        def provider = new MultiSourceContextFileSystemTagProvider("src/test/resources/features",0);
        def contextualizer = new Contextualizer();
        contextualizer.setSourceContext("android")
        def testOutcomeEminatingFomrAndroid = contextualizer.contextualize(TestOutcome.forTestInStory("Feature 1 completed successfully", Story.called("Feature 1").withPath("capability1/feature1.feature").asFeature()))

        when:

        def parentRequirement = provider.getParentRequirementOf(testOutcomeEminatingFomrAndroid).get()

        then:
        parentRequirement.name=="Feature 1 (android)"
        parentRequirement.cardNumber == "#PW-1235"
    }


}
