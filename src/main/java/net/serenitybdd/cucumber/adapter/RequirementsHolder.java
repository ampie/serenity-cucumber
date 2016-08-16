package net.serenitybdd.cucumber.adapter;

import net.thucydides.core.requirements.model.Requirement;

import java.util.List;

public class RequirementsHolder {
    private List<Requirement> requirements;

    public RequirementsHolder() {
    }

    public RequirementsHolder(List<Requirement> requirements) {
        this.requirements = requirements;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }
}
