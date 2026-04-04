package com.mindgarden.customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Zentraler Exception-Handler für alle REST-Controller.
 *
 * Ohne diesen Handler würde Spring bei einer unbehandelten Exception entweder: - eine generische 500-HTML-Seite
 * zurückgeben - interne Stack Traces an den Client leaken
 *
 * Alle Fehlerantworten folgen RFC 7807 (ProblemDetail): { "type":   "about:blank", "title":  "Customer not found",
 * "status": 404, "detail": "Customer with id '...' not found" }
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody Gilt automatisch für alle @RestController im Projekt.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------
    // 404 Not Found
    // -------------------------------------------------------

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleCustomerNotFound(CustomerNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Customer not found");
        return problem;
    }

    // -------------------------------------------------------
    // 409 Conflict – Eindeutigkeitsverletzungen
    // -------------------------------------------------------

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Email already exists");
        return problem;
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ProblemDetail handleCustomerAlreadyExists(CustomerAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Customer already exists");
        return problem;
    }

    // -------------------------------------------------------
    // 409 Conflict – Ungültige Statusübergänge
    // -------------------------------------------------------

    @ExceptionHandler(CustomerAlreadyActiveException.class)
    public ProblemDetail handleCustomerAlreadyActive(CustomerAlreadyActiveException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Customer already active");
        return problem;
    }

    @ExceptionHandler(CustomerAlreadyInactiveException.class)
    public ProblemDetail handleCustomerAlreadyInactive(CustomerAlreadyInactiveException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Customer already inactive");
        return problem;
    }

    @ExceptionHandler(CustomerAlreadyBlockedException.class)
    public ProblemDetail handleCustomerAlreadyBlocked(CustomerAlreadyBlockedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Customer already blocked");
        return problem;
    }

    // -------------------------------------------------------
    // 400 Bad Request – Validierungsfehler
    // -------------------------------------------------------

    /**
     * Wird ausgelöst wenn @Valid einen oder mehrere Fehler findet.
     *
     * Antwortformat: { "status": 400, "title":  "Validation failed", "detail": "One or more fields are invalid",
     * "errors": { "email":     "must be a well-formed email address", "firstname": "must not be blank" } }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                                       .getFieldErrors()
                                       .stream()
                                       .collect(Collectors.toMap(FieldError::getField,
                                                                 f -> f.getDefaultMessage() != null ?
                                                                      f.getDefaultMessage() : "invalid"));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                                                                 "One or more fields are invalid");
        problem.setTitle("Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }
}
