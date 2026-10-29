package com.acme.salary.common;

import com.acme.salary.employee.EmployeeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns exceptions into one consistent JSON error shape (RFC 7807 ProblemDetail),
 * so the UI can show field-level messages and never sees a stack trace.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(EmployeeNotFoundException.class)
    ProblemDetail notFound(EmployeeNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validationFailed(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail unreadableBody(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed request body",
                "Check the JSON format, dates (yyyy-MM-dd) and allowed values such as country");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail badParameter(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request parameter",
                "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'");
    }

    @ExceptionHandler(PropertyReferenceException.class)
    ProblemDetail badSort(PropertyReferenceException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid sort field", "Cannot sort by '" + ex.getPropertyName() + "'");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        if (ex instanceof ErrorResponse springError) {
            // Spring's own errors (unknown URL, wrong HTTP method, ...) already carry the right status
            return springError.getBody();
        }
        log.error("Unhandled error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", "Something went wrong. Please try again.");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
