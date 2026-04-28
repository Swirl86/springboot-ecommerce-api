package com.swirl.ecomengine.common.etag;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EtagService {

    public String generate(LocalDateTime lastUpdated) {
        if (lastUpdated == null) {
            return "\"empty\"";
        }
        return "\"" + lastUpdated.toString() + "\"";
    }

    public boolean matches(String ifNoneMatch, String currentEtag) {
        return ifNoneMatch != null && ifNoneMatch.equals(currentEtag);
    }
}
