package pl.edu.pw.ee.zai.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pl.edu.pw.ee.zai.dto.ProblemResponse;
import pl.edu.pw.ee.zai.dto.ValidationErrorItem;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType PROBLEM_JSON = MediaType.parseMediaType("application/problem+json");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationErrorItem> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ValidationErrorItem(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        ProblemResponse problem = new ProblemResponse(
                "Unprocessable Entity",
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Validation failed for one or more fields",
                request.getRequestURI(),
                errors
        );
        return problemResponse(HttpStatus.UNPROCESSABLE_ENTITY, problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Bad Request",
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON request body or incompatible data types",
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Bad Request",
                HttpStatus.BAD_REQUEST.value(),
                "Invalid parameter value for: " + ex.getName(),
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Bad Request",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.BAD_REQUEST, problem);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Not Found",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.NOT_FOUND, problem);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Conflict",
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.CONFLICT, problem);
    }

    @ExceptionHandler(UnprocessableException.class)
    public ResponseEntity<ProblemResponse> handleUnprocessable(UnprocessableException ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Unprocessable Entity",
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.UNPROCESSABLE_ENTITY, problem);
    }

    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public ResponseEntity<ProblemResponse> handleUnauthorized(Exception ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Unauthorized",
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.UNAUTHORIZED, problem);
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ProblemResponse> handleForbidden(Exception ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Forbidden",
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.FORBIDDEN, problem);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Unsupported Media Type",
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Content-Type not supported: " + ex.getContentType(),
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, problem);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Void> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Method Not Allowed",
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "HTTP method " + ex.getMethod() + " is not supported for this resource",
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.METHOD_NOT_ALLOWED, problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        ProblemResponse problem = new ProblemResponse(
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected server error occurred",
                request.getRequestURI()
        );
        return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, problem);
    }

    private ResponseEntity<ProblemResponse> problemResponse(HttpStatus status, ProblemResponse body) {
        return ResponseEntity.status(status)
                .contentType(PROBLEM_JSON)
                .header(HttpHeaders.CONTENT_TYPE, "application/problem+json")
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }
}
