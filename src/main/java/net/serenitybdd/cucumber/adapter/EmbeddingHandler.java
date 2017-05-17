package net.serenitybdd.cucumber.adapter;

public interface EmbeddingHandler {
    boolean attemptHandling(String mimeType, byte[] data);
}
