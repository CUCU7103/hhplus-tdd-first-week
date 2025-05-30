package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.validator.ValidatorImpl;

@ExtendWith(MockitoExtension.class)
class PointServiceUnitTest {

	@Mock
	private PointHistoryTable pointHistoryTable;

	@Mock
	private UserPointTable userPointTable;

	@Mock
	private ValidatorImpl validator;

	@InjectMocks
	private PointService pointService;

	@Test
	void 포인트_조회시_유저_아이디가_양수라면_정상적으로_조회된다() {
		// arrange 테스트 조건
		long id = 1L;
		long point = 100L;
		UserPoint userPoint = new UserPoint(id, point, System.currentTimeMillis());
		doNothing().when(validator).invalidId(id);
		given(userPointTable.selectById(id)).willReturn(userPoint); // stubbing 객체의 상태를 반환
		// act 테스트 실행
		UserPoint result = pointService.searchUserPoint(id);
		// assert 검증
		assertThat(result).isEqualTo(userPoint);
		assertThat(userPoint.id()).isEqualTo(id);
		assertThat(userPoint.point()).isEqualTo(point);
	}

	@Test
	void 포인트_충전_이용내역_조회시_유저_아이디가_양수라면_정상적으로_조회된다() {
		//arrange
		long id = 1L;
		long firstAmount = 100L;
		long secondAmount = 100L;
		PointHistory firstHistory = new PointHistory(1, id, firstAmount, TransactionType.CHARGE,
			System.currentTimeMillis());
		PointHistory secondHistory = new PointHistory(2, id, secondAmount, TransactionType.CHARGE,
			System.currentTimeMillis());
		doNothing().when(validator).invalidId(id);
		List<PointHistory> histories = Arrays.asList(firstHistory, secondHistory);
		given(pointHistoryTable.selectAllByUserId(id)).willReturn(histories);

		// act
		List<PointHistory> resultList = pointService.searchPointHistory(id);

		// assert 검증
		assertThat(resultList).hasSize(2);
		assertThat(resultList.get(0).userId()).isEqualTo(id);
		assertThat(resultList.get(1).userId()).isEqualTo(id);
		assertThat(resultList.get(0).amount()).isEqualTo(firstAmount);
		assertThat(resultList.get(1).amount()).isEqualTo(secondAmount);
	}

}
