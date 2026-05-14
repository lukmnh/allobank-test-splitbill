package com.allobank.test.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_SPLIT_DATA("ES_400", "Invalid split data", HttpStatus.BAD_REQUEST),
    NOT_FOUND("GEN_404", "", HttpStatus.NOT_FOUND),
    FIELD_REQUIRED("GEN_400", "", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
