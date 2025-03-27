package io.hhplus.tdd.point;

import io.hhplus.tdd.CustomErrorCode;
import io.hhplus.tdd.CustomException;

public record UserPoint(long id, long point, long updateMillis) {

	public UserPoint {
		if (point < 0) {
			throw new CustomException(CustomErrorCode.INVALID_POINT);
		}
	}

	public static UserPoint empty(long id) {
		return new UserPoint(id, 0, System.currentTimeMillis());
	}

	UserPoint charge(long point) {
		final long MAX_POINT = 100000L;
		long result = this.point + point;
		if (result > MAX_POINT) {
			throw new CustomException(CustomErrorCode.EXCEEDED_CHARGE_POINT, result);
		}
		return new UserPoint(id, result, System.currentTimeMillis());
	}

	UserPoint use(long amount) {
		final long MIN_POINT = 100L;
		if (amount < MIN_POINT) {
			throw new CustomException(CustomErrorCode.INVALID_USE_AMOUNT);
		}
		long result = this.point - amount;
		if (result < 0) {
			throw new CustomException(CustomErrorCode.OVER_USED_POINT, this.point);
		}
		return new UserPoint(id, result, System.currentTimeMillis());
	}
}
