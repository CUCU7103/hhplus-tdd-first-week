package io.hhplus.tdd;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.hhplus.tdd.error.CustomException;
import io.hhplus.tdd.error.ErrorResponse;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
	}

	@ExceptionHandler(value = CustomException.class)
	public ResponseEntity<ErrorResponse> handleException(CustomException e) {
		return ResponseEntity.status(e.getCustomErrorCode().getHttpStatus())
			.body(new ErrorResponse(e.getCustomErrorCode().getCode(), e.getMessage()));

	}
}
