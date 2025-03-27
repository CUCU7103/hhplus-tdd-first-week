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

	/**
	 *
	 * 생성한 검증 로직이 실제로 검증이 되는지에 대해서 테스트 코드를 작성해야 된다고 판단하여
	 * <p>해당 테스트 코드를 작성하였습니다.
	 * <p>각각 id 값, amount 값의 유효성을 판단합니다.
	 */

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
