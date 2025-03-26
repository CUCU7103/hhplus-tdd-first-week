package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.CustomErrorCode;
import io.hhplus.tdd.CustomException;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

@ExtendWith(MockitoExtension.class)
class PointServiceUnitTest {

	@Mock
	private PointHistoryTable pointHistoryTable;

	@Mock
	private UserPointTable userPointTable;

	@InjectMocks
	private PointService pointService;

	@Test
	void 포인트_조회시_유저_아이디가_양수라면_정상적으로_조회된다() {
		// arrange 테스트 조건
		long id = 1L;
		long point = 100L;
		UserPoint userPoint = new UserPoint(id, point, System.currentTimeMillis());
		given(userPointTable.selectById(id)).willReturn(userPoint); // stubbing 객체의 상태를 반환
		// act 테스트 실행
		UserPoint result = pointService.searchUserPoint(id);
		// assert 검증
		assertThat(result).isEqualTo(userPoint);
		assertThat(userPoint.id()).isEqualTo(id);
		assertThat(userPoint.point()).isEqualTo(point);
	}

	@ValueSource(longs = {0L, -100L})
	@ParameterizedTest
	void 포인트_조회시_유저_아이디가_0이거나_0보다_작다면_예외가_발생한다(long id) {
		// assert
		assertThatThrownBy(() -> pointService.searchUserPoint(id))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void 포인트_충전_이용내역_조회시_유저_아이디가_양수라면_정상적으로_조회된다() {
		//arrange
		long userId = 1L;
		PointHistory firstHistory = new PointHistory(1, userId, 100L, TransactionType.CHARGE,
			System.currentTimeMillis());
		PointHistory secondHistory = new PointHistory(2, userId, 200L, TransactionType.CHARGE,
			System.currentTimeMillis());
		List<PointHistory> histories = Arrays.asList(firstHistory, secondHistory);
		given(pointHistoryTable.selectAllByUserId(userId)).willReturn(histories);

		// act 테스트 실행
		List<PointHistory> results = pointService.searchPointHistory(userId);
		// assert 검증
		assertThat(results).isEqualTo(histories);
		assertThat(results.size()).isEqualTo(2);
	}

	@ValueSource(longs = {0L, -100L})
	@ParameterizedTest
	void 포인트_충전_이용내역_조회시_유저_아이디가_0이거나_0보다_작다면_예외가_발생한다(long id) {
		// act & assert
		assertThatThrownBy(() -> pointService.searchPointHistory(id))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_ID.getMessage());
	}

	@Test
	void 포인트_충전_이용내역_조회시_유저의_포인트가_0보다_작은_기록이_있다면_예외가_발생한다() {
		long userId = 1L;
		PointHistory firstHistory = new PointHistory(1, userId, 100L, TransactionType.CHARGE,
			System.currentTimeMillis());
		PointHistory secondHistory = new PointHistory(2, userId, -200L, TransactionType.CHARGE,
			System.currentTimeMillis());
		List<PointHistory> histories = Arrays.asList(firstHistory, secondHistory);
		given(pointHistoryTable.selectAllByUserId(userId)).willReturn(histories);

		// act & assert 검증
		assertThatThrownBy(() -> pointService.searchPointHistory(userId))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_POINT.getMessage());
	}
}
