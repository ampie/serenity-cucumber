package net.serenitybdd.cucumber.adaptor;


import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestTag;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TaggingAdaptorTest {
    @Test
    public void testLoad() throws Exception {
        ContextTaggingAdaptor adapter = new ContextTaggingAdaptor();
        adapter.setSourceContext("DomainService");
        List<TestOutcome> testOutcomes = adapter.loadOutcomesFrom(new File("src/test/resources/sample-importing-pom/service-reports"));
        assertThat(testOutcomes.size(), is(equalTo(1)));
        Set<TestTag> tags = testOutcomes.get(0).getTags();
        boolean found = false;
        for (TestTag tag : tags) {
            found |= tag.getName().equals("DomainService");
        }
        assertTrue(found);
    }
}
