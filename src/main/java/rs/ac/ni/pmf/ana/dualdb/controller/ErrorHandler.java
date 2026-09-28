package rs.ac.ni.pmf.ana.dualdb.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rs.ac.ni.pmf.ana.dualdb.dto.common.ErrorDto;
import rs.ac.ni.pmf.ana.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.ana.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.dualdb.exception.ResourceNotFoundException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler
{
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorDto handleResourceNotFoundException(final ResourceNotFoundException ex, final HttpServletRequest request)
	{
		return clientError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	@ExceptionHandler(DuplicateResourceException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorDto handleDuplicateResourceException(final DuplicateResourceException ex, final HttpServletRequest request)
	{
		return clientError(HttpStatus.CONFLICT, ex.getMessage(), request);
	}

	@ExceptionHandler(InvalidOperationException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ErrorDto handleInvalidOperationException(final InvalidOperationException ex, final HttpServletRequest request)
	{
		return clientError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorDto handleAccessDeniedException(final AccessDeniedException ex, final HttpServletRequest request)
	{
		return clientError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorDto handleDataIntegrityViolationException(final DataIntegrityViolationException ex, final HttpServletRequest request)
	{
		log.error("409 {} {}: database constraint violated: {}",
				request.getMethod(), request.getRequestURI(), ex.getMostSpecificCause().getMessage());

		return errorDto("The data violates a database constraint (e.g. a duplicate value)", request);
	}

	@ExceptionHandler(NumberFormatException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorDto handleNumberFormatException(final HttpServletRequest request)
	{
		return clientError(HttpStatus.BAD_REQUEST, "Invalid id: a numeric value is required", request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorDto handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex, final HttpServletRequest request)
	{
		final String message = ex.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.collect(Collectors.joining("; "));

		return clientError(HttpStatus.BAD_REQUEST, message, request);
	}

	private ErrorDto clientError(final HttpStatus status, final String message, final HttpServletRequest request)
	{
		log.warn("{} {} {}: {}", status.value(), request.getMethod(), request.getRequestURI(), message);
		return errorDto(message, request);
	}

	private ErrorDto errorDto(final String message, final HttpServletRequest request)
	{
		return ErrorDto.builder()
				.timestamp(OffsetDateTime.now())
				.message(message)
				.path(request.getRequestURI())
				.build();
	}
}
