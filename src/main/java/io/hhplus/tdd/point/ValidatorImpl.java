package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

import io.hhplus.tdd.CustomErrorCode;
import io.hhplus.tdd.CustomException;

@Component
public class ValidatorImpl implements Validator {
	@Override
	public void invalidId(long id) {
		if (id <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_ID);
		}
	}

	@Override
	public void invalidAmount(long amount) {
		if (amount <= 0) {
			throw new CustomException(CustomErrorCode.INVALID_AMOUNT);
		}
	}
}
