package net.serenitybdd.cucumber.adapter

import spock.lang.Specification
/**
 * Uses a cucumber json file generated by
 * @see net.serenitybdd.cucumber.integration.ScenariosWithTableInBackgroundSteps
 */
class WhenImportingCucumberJsonFilesWithBackgrounds extends Specification {
    def "It should still follow the ScenarioLifcycle as if run from a feature file"() {
        given:
        def adapter = new CucumberJsonAdapter();
        when:
        def outcomes = adapter.loadOutcomesFrom(new File("src/test/resources/cucumber-json-files/ScenariosWithTableInBackgroundSteps.json"))
        then:
        outcomes.size() == 1
        outcomes[0].testSteps.size() == 4
        //2 from background
        outcomes[0].testSteps[0].description.startsWith("Given the following customers exist:") == true
        outcomes[0].testSteps[1].description == "And I am logged into the OneView app"
        //2 from scenario
        outcomes[0].testSteps[2].description == "When I locate a customer with a Reg Number of 80862061"
        outcomes[0].testSteps[3].description == "Then I should see the customer profile for TONY SMITH"
    }

}
