package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
	void 포인트_조회시_유저_아이디가_0보다_작다면_예외가_발생한다(long id) {
		// arrange 테스트 조건
		// act & assert
		assertThatThrownBy(() -> pointService.searchUserPoint(id)).isInstanceOf(RuntimeException.class);
		
	}

	@Test
	void 포인트_조회시_유저의_포인트가_음수라면_예외가_발생한다() {
		// arrange
		long id = 1L;
		long point = -100L;
		UserPoint userPoint = new UserPoint(id, point, System.currentTimeMillis());
		given(userPointTable.selectById(id)).willReturn(userPoint);
		// act & assert
		assertThatException().isThrownBy(() -> pointService.searchUserPoint(id)).isInstanceOf(RuntimeException.class);
	}

}
