package com.haucky.lexofficeadapter.lexoffice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LexofficeLegacyError {
    @JsonProperty("IssueList")
    private List<Issue> issueList = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issue {
        private String i18nKey;
        private String source;
        private String type;
        private Object additionalData;
        private Object args;
    }
}