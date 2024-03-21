package org.dataart.sales.service;

import io.quarkus.runtime.util.StringUtil;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Slf4j
@Builder
public class QueryResult<T> {

    private T result;
    private String alternateHost;
    private String alternatePort;
    private String alternateEndpoint;

    public Optional<T> foundResult() {
        return Optional.ofNullable(result);
    }

    public Optional<URI> alternateSearchUri() throws URISyntaxException {
        if (StringUtil.isNullOrEmpty(alternateHost)) {
            return Optional.empty();
        }

        log.info("Building URI from {}:{}{}", alternateHost, alternatePort, alternateEndpoint);
        return Optional.of(new URI("http://%s:%s%s".formatted(alternateHost, alternatePort, alternateEndpoint)));
    }
}
