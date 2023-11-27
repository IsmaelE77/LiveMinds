package io.github.ismaele77.LiveMinds.DTO;

public class SecurityResponse {

    private String error;

    public SecurityResponse() {

    }

    public SecurityResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
