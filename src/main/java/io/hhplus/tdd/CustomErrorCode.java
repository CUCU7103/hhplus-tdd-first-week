package io.hhplus.tdd;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum CustomErrorCode {

	INVALID_ID(HttpStatus.BAD_REQUEST, "400", "유효하지 않은 아이디 입니다."),
	INVALID_POINT(HttpStatus.INTERNAL_SERVER_ERROR, "500", "해당 유저는 유효하지 않은 포인트를 보유하고 있습니다."),
	INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "400", "입력한 포인트 값은 유효하지 않습니다."),
	EXCEEDED_CHARGE_POINT(HttpStatus.BAD_REQUEST, "400", "보유할 수 있는 포인트는 10만 포인트까지 입니다. 해당 충전 후 예상 포인트: %d"),
	OVER_USED_POINT(HttpStatus.BAD_REQUEST, "400", "보유한 포인트 이상의 사용 요청입니다. 현재 보유한 포인트 %d"),
	INVALID_USE_AMOUNT(HttpStatus.BAD_REQUEST, "400", "포인트 사용시 100 포인트 이상을 사용해야 합니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	CustomErrorCode(HttpStatus httpStatus, String code, String message) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.message = message;
	}
}
