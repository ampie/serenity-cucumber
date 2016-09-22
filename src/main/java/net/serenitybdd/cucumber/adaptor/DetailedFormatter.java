package net.serenitybdd.cucumber.adaptor;

import gherkin.formatter.Formatter;

public interface DetailedFormatter extends Formatter {
    void childStep(String name);
}
