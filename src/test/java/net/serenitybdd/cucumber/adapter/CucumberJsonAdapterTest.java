package net.serenitybdd.cucumber.adapter;


import net.thucydides.core.model.TestOutcome;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class CucumberJsonAdapterTest {
    @Test
    public void testLoad() throws Exception{
        List<TestOutcome> testOutcomes = new CucumberJsonAdapter().loadOutcomesFrom(new File("src/test/resources/json/cucumber1.json"));
        testOutcomes = new CucumberJsonAdapter().loadOutcomesFrom(new File("src/test/resources/json/cucumber2.json"));
        System.out.println();
    }
}
