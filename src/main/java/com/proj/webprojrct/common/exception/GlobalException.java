package com.proj.webprojrct.common.exception;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

import com.proj.webprojrct.auth.dto.response.RestResponse;
// import com.proj.webprojrct.storage.exception.InvalidFolder;
import com.proj.webprojrct.user.exception.InvalidEmailFormatException;
import com.proj.webprojrct.user.exception.UnauthenticatedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityExistsException;

@RestControllerAdvice
public class GlobalException {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        EntityExistsException.class,
        EmailAlreadyExistsException.class, VerificationException.class
    })
    public ResponseEntity<RestResponse<Object>> badRequestException(RuntimeException exception) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(exception.getMessage());
        res.setMessage("Exception occurs...");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> validationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Bad request");

        List<String> errors
                = fieldErrors.stream().map(f -> f.getDefaultMessage()).collect(Collectors.toList());
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(
            value = {
                UsernameNotFoundException.class,
                BadCredentialsException.class
            })
    public ResponseEntity<RestResponse<Object>> handleAuthenticationException(Exception ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("Exception occurs...");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RestResponse<Object>> handleUnauthorizedAccessException(AccessDeniedException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError(ex.getMessage());
        res.setMessage("Unauthorized access. You do not have permission to access this resource.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    @ExceptionHandler(PermissionDeny.class)
    public ResponseEntity<RestResponse<Object>> handlePermissionDenyException(PermissionDeny ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError(ex.getMessage());
        res.setMessage("Permission denied. You are not allowed to perform this action.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    // @ExceptionHandler(InvalidFolder.class)
    // public ResponseEntity<RestResponse<Object>> handleInvalidFolderException(InvalidFolder ex) {
    //     RestResponse<Object> res = new RestResponse<>();
    //     res.setStatusCode(HttpStatus.BAD_REQUEST.value());
    //     res.setError(ex.getMessage());
    //     res.setMessage("Invalid folder action");
    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    // }
    @ExceptionHandler(InValidTokenExeption.class)
    public ResponseEntity<RestResponse<Object>> handleInvalidTokenException(InValidTokenExeption ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("Invalid token");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<RestResponse<Object>> handleUnauthenticatedException(UnauthenticatedException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError("Unauthorized");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<RestResponse<Object>> handleInvalidEmailFormatException(InvalidEmailFormatException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Invalid Email Format");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

}
