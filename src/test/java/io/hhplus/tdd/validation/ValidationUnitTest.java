package io.hhplus.tdd.validation;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import io.hhplus.tdd.CustomErrorCode;
import io.hhplus.tdd.CustomException;
import io.hhplus.tdd.point.ValidatorImpl;

public class ValidationUnitTest {

	@Mock
	private final ValidatorImpl validator = new ValidatorImpl();

	@ValueSource(longs = {0L, -100L})
	@ParameterizedTest
	void 입력받은_id_값이_0_이거나_음수라면_예외를_발생시킨다(long id) {
		//act & assert
		assertThatThrownBy(() -> validator.invalidId(id)).isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_ID.getMessage());
	}

	@ValueSource(longs = {0L, -100L})
	@ParameterizedTest
	void 입력받은_amount_값이_0_이거나_음수라면_예외를_발생시킨다(long amount) {
		// act & assert
		assertThatThrownBy(() -> validator.invalidAmount(amount)).isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_AMOUNT.getMessage());
	}
}
