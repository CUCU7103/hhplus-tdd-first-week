package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final PointHistoryTable pointHistoryTable;
	private final UserPointTable userPointTable;
	private final ValidatorImpl validator;

	public UserPoint searchUserPoint(long id) {
		validator.invalidId(id);
		return userPointTable.selectById(id);
	}

	// 해당하는 유저의 포인트 충전/이용내역을 조회한다.
	public List<PointHistory> searchPointHistory(long id) {
		validator.invalidId(id);
		List<PointHistory> histories = pointHistoryTable.selectAllByUserId(id);
		for (PointHistory history : histories) {
			validator.invalidAmount(history.amount());
		}
		return histories;
	}

	public UserPoint charge(long id, long amount) {
		// 입력받은 값들을 검증하는 로직
		validator.invalidId(id);
		validator.invalidAmount(amount);
		// 유저 정보를 가져오고 이를 검증하고 새로운 객체를 반환하는 역할을 밖으로 뺀다.
		UserPoint userPoint = userPointTable.selectById(id);
		UserPoint afterChargedUserPoint = userPoint.charge(amount);
		// 유저 포인트 저장하기
		UserPoint resultUserPoint = userPointTable.insertOrUpdate(afterChargedUserPoint.id(),
			afterChargedUserPoint.point());
		// 히스토리에 저장하는 기능
		pointHistoryTable.insert(resultUserPoint.id(), resultUserPoint.point(), TransactionType.CHARGE,
			System.currentTimeMillis());
		return resultUserPoint;
	}

	public UserPoint use(long id, long amount) {
		validator.invalidId(id);
		validator.invalidAmount(amount);
		// UserPoint 객체를 가져온다.
		UserPoint userPoint = userPointTable.selectById(id);
		// UserPoint에서 처리
		UserPoint afterUsedUserPoint = userPoint.use(amount);
		// 유저 포인트 저장하기
		UserPoint resultUserPoint = userPointTable.insertOrUpdate(afterUsedUserPoint.id(), afterUsedUserPoint.point());
		pointHistoryTable.insert(resultUserPoint.id(), resultUserPoint.point(), TransactionType.CHARGE,
			System.currentTimeMillis());
		return resultUserPoint;
	}
}
