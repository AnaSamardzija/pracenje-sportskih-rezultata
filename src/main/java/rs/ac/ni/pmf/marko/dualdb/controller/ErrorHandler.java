package rs.ac.ni.pmf.marko.dualdb.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rs.ac.ni.pmf.marko.dualdb.dto.common.ErrorDto;
import rs.ac.ni.pmf.marko.dualdb.exception.DuplicateResourceException;
import rs.ac.ni.pmf.marko.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.marko.dualdb.exception.ResourceNotFoundException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler
{
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorDto handleResourceNotFoundException(final ResourceNotFoundException ex)
	{
		return ErrorDto.builder()
				.timestamp(OffsetDateTime.now())
				.message(ex.getMessage())
				.build();
	}

	@ExceptionHandler(DuplicateResourceException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorDto handleDuplicateResourceException(final DuplicateResourceException ex)
	{
		return ErrorDto.builder()
				.timestamp(OffsetDateTime.now())
				.message(ex.getMessage())
				.build();
	}

	@ExceptionHandler(InvalidOperationException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	public ErrorDto handleInvalidOperationException(final InvalidOperationException ex)
	{
		return ErrorDto.builder()
				.timestamp(OffsetDateTime.now())
				.message(ex.getMessage())
				.build();
	}

	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ErrorDto handleAccessDeniedException(final AccessDeniedException ex)
	{
		return ErrorDto.builder()
				.timestamp(OffsetDateTime.now())
				.message(ex.getMessage())
				.build();
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorDto handleDataIntegrityViolationException()
	{
		return ErrorDto.builder()
				.timestamp(OffsetDateTime.now())
				.message("Podatak narušava ograničenje baze (npr. duplikat).")
				.build();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorDto handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex)
	{
		final String message = ex.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.collect(Collectors.joining("; "));

		return ErrorDto.builder()
				.timestamp(OffsetDateTime.now())
				.message(message)
				.build();
	}
}
