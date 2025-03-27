package io.hhplus.tdd.error;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
	CustomErrorCode customErrorCode;

	public CustomException(CustomErrorCode customErrorCode) {
		super(customErrorCode.getMessage());
		this.customErrorCode = customErrorCode;
	}

	public CustomException(CustomErrorCode customErrorCode, Object... args) {
		super(String.format(customErrorCode.getMessage(), args));
		this.customErrorCode = customErrorCode;
	}
}
