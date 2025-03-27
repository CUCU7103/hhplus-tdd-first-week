package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.hhplus.tdd.CustomErrorCode;
import io.hhplus.tdd.CustomException;

public class UserPointUnitTest {

	@ValueSource(longs = {-500L, -3700L})
	@ParameterizedTest
	void 포인트가_0미만의_유저는_예외를_발생시킨다(long amount) {
		// arrange
		long id = 1L;

		// act & assert
		assertThatThrownBy(() -> new UserPoint(id, amount, System.currentTimeMillis()))
			.isInstanceOf(CustomException.class)
			.hasMessage(CustomErrorCode.INVALID_POINT.getMessage());

	}

	@Test
	void 유저_포인트_충전시_충전금액이_최대값_이상이면_예외를_발생시킨다() {
		// arrange
		long id = 1L;
		long currentPoint = 20000L;
		long amount = 1500000L;
		long resultAmount = currentPoint + amount;
		UserPoint userPoint = new UserPoint(id, currentPoint, System.currentTimeMillis());

		assertThatThrownBy(() -> userPoint.charge(amount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(String.format(CustomErrorCode.EXCEEDED_CHARGE_POINT.getMessage(), resultAmount));

	}

	@ValueSource(longs = {50000L, 60000L, 800L})
	@ParameterizedTest
	void 유저_포인트_충전시_최대금액을_초과하지_않으면_정상적으로_충전이_이루어진다(long chargePoint) {
		// arrange
		long id = 1L;
		long currentPoint = 20000L;
		long expectPoint = currentPoint + chargePoint;

		UserPoint userPoint = new UserPoint(id, currentPoint, System.currentTimeMillis());
		UserPoint updatedUserPoint = new UserPoint(id, expectPoint, System.currentTimeMillis());
		//act
		UserPoint result = userPoint.charge(chargePoint);
		//assert
		assertThat(result.point()).isEqualTo(updatedUserPoint.point());
		assertThat(result.id()).isEqualTo(updatedUserPoint.id());
	}

	@Test
	void 유저_포인트_사용시_보유한_포인트를_초과하여_사용하면_예외가_발생한다() {
		// arrange
		long id = 1L;
		long currentPoint = 20000L;
		long amount = 20001L;
		UserPoint userPoint = new UserPoint(id, currentPoint, System.currentTimeMillis());

		assertThatThrownBy(() -> userPoint.use(amount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(String.format(CustomErrorCode.OVER_USED_POINT.getMessage(), currentPoint));
	}

	@ValueSource(longs = {99L, 1L, 30L})
	@ParameterizedTest
	void 유저_포인트_사용시_최소_포인트_이상_사용하지_않으면_예외가_발생한다(long amount) {
		// arrange
		long id = 1L;
		long currentPoint = 20000L;

		UserPoint userPoint = new UserPoint(id, currentPoint, System.currentTimeMillis());

		assertThatThrownBy(() -> userPoint.use(amount))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_USE_AMOUNT.getMessage());
	}

	@ValueSource(longs = {1000L, 6600L, 900L})
	@ParameterizedTest
	void 포인트_사용시_최소_포인트_사용_및_음수_포인트_사용이_아니라면_정상적으로_사용가능하다(long amount) {
		long id = 1L;
		long currentPoint = 40000L;

		long usedPoint = currentPoint - amount;
		UserPoint userPoint = new UserPoint(id, currentPoint, System.currentTimeMillis());
		UserPoint usedUserPoint = new UserPoint(id, usedPoint, System.currentTimeMillis());
		//act
		UserPoint result = userPoint.use(amount);
		//assert
		assertThat(result.point()).isEqualTo(usedUserPoint.point());
		assertThat(result.id()).isEqualTo(usedUserPoint.id());

	}

}
