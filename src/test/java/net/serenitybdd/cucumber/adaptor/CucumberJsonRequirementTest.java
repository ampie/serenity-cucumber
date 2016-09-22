package net.serenitybdd.cucumber.adaptor;


import net.thucydides.core.requirements.model.Requirement;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

public class CucumberJsonRequirementTest {
    @Test
    public void testLoad() throws Exception {
        CucumberRequirementExtractor reporter = new CucumberRequirementExtractor();
        JsonParser p = new JsonParser(reporter, reporter);
        p.parse(FileUtils.readFileToString(new File("src/test/resources/sample-importing-pom/cucumber-reports/requirements.json")));
        reporter.linkRequirements();
        Map<String, Requirement> requirementMap = toMap(reporter.getTopLevelRequirements());
        assertThat(requirementMap.size(),is(equalTo(3)));
        Requirement capability_1 = requirementMap.get("capability_1");
        assertThat(capability_1.getDisplayName(),is(equalTo("Capability 1")));
        assertThat(capability_1.getNarrative().getText(),is(equalTo("As a business I want capability 1 so that I can make more money")));
        assertThat(capability_1.getType(),is(equalTo("Capability")));
        assertThat(capability_1.getChildren().size(),is(equalTo(3)));
        Map<String,Requirement> featureMap=toMap(capability_1.getChildren());
        Requirement feature_1_1 = featureMap.get("feature_1_1");
        assertThat(feature_1_1.getDisplayName(),is(equalTo("Feature 1.1")));
        assertThat(feature_1_1.getNarrative().getText(),is(equalTo("As a user I want feature 1.1 so that I get more value")));
        assertThat(feature_1_1.getType(),is(equalTo("HighLevelFeature")));
        assertThat(feature_1_1.getChildren().size(),is(equalTo(0)));
        Requirement capability_2 = requirementMap.get("capability_2");
        assertThat(capability_2.getChildren().size(),is(equalTo(4)));
        assertSame(feature_1_1,toMap(capability_2.getChildren()).get("feature_1_1"));
        Requirement capability_3 = requirementMap.get("capability_3");
        assertThat(capability_3.getChildren().size(),is(equalTo(4)));
        assertSame(feature_1_1,toMap(capability_3.getChildren()).get("feature_1_1"));
        Requirement feature_1_3 = featureMap.get("feature_1_3");
        assertThat(feature_1_3.getCardNumber(), is(equalTo("#PW-123123")));
    }

    private Map<String, Requirement> toMap(List<Requirement> requirements) {
        Map<String, Requirement> result=new HashMap<String ,Requirement>();
        for (Requirement requirement : requirements) {
            result.put(requirement.getName(),requirement);
        }
        return result;
    }
}
