package io.eddvance.production.monitoring.model;

public class ServiceEndpoint {

    private final String name;
    private final String url;

    public ServiceEndpoint(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
