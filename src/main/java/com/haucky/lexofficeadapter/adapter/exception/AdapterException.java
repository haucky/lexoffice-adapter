package com.haucky.lexofficeadapter.adapter.exception;

import lombok.Builder;
import lombok.Getter;

/**
 * General-purpose exception for exceptions thrown within adapter-specific code
 */
@Getter
@Builder
public class AdapterException extends RuntimeException {
    private final String title;
    private final String detail;
    private final String upstreamRequestId;

    public AdapterException(String title, String detail, String upstreamRequestId) {
        super(detail);
        this.title = title;
        this.detail = detail;
        this.upstreamRequestId = upstreamRequestId;
    }
}