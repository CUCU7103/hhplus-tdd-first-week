package io.hhplus.tdd.point;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.lock.UserLockManager;
import io.hhplus.tdd.validator.ValidatorImpl;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

	private final PointHistoryTable pointHistoryTable;
	private final UserPointTable userPointTable;
	private final ValidatorImpl validator;
	private final UserLockManager userLockManager;

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

	/**
	 * 	동시성 제어를 하려면?<p>
	 * 	동일한 사용자에 대해서 여러 스레드에서 충전 작업이 이루어질 경우 이 동시 접근에 대한 제어가 필요하다. <p>
	 * 	각 사용자에 대한 개별적인 ReetrantLock을 사용하면 동일한 사용자에 대해서 여러 스레드에서 충전 작업이 이루어질 경우 동시 접근에 대한 제어가 가능하다. <p>
	 * 	각기 다른 사용자에 대한 충전작업이 동시에 이루어질 경우 각 사용자에 대해 개별적 ReetrantLock을 사용하기에 병렬적으로 작업이 이루어진다.
	 */

	public UserPoint charge(long id, long amount) {
		// 입력받은 값들을 검증하는 로직
		validator.invalidId(id);
		validator.invalidAmount(amount);
		ReentrantLock userLock = userLockManager.getUserLock(id);
		userLock.lock();
		try {
			UserPoint userPoint = userPointTable.selectById(id);
			// 유저 정보를 가져오고 이를 검증하고 새로운 객체를 반환하는 역할을 밖으로 뺀다.
			UserPoint afterChargedUserPoint = userPoint.charge(amount);
			// 유저 포인트 저장하기
			UserPoint resultUserPoint = userPointTable.insertOrUpdate(afterChargedUserPoint.id(),
				afterChargedUserPoint.point());
			// 히스토리에 저장하는 기능
			pointHistoryTable.insert(resultUserPoint.id(), resultUserPoint.point(), TransactionType.CHARGE,
				System.currentTimeMillis());
			return resultUserPoint;
		} finally {
			userLock.unlock();
		}
	}

	public UserPoint use(long id, long amount) {
		validator.invalidId(id);
		validator.invalidAmount(amount);
		// UserPoint 객체를 가져온다.
		ReentrantLock userLock = userLockManager.getUserLock(id);
		userLock.lock();
		try {
			UserPoint userPoint = userPointTable.selectById(id);
			// UserPoint에서 처리
			UserPoint afterUsedUserPoint = userPoint.use(amount);
			// 유저 포인트 저장하기
			UserPoint resultUserPoint = userPointTable.insertOrUpdate(afterUsedUserPoint.id(),
				afterUsedUserPoint.point());
			pointHistoryTable.insert(resultUserPoint.id(), resultUserPoint.point(), TransactionType.USE,
				System.currentTimeMillis());
			return resultUserPoint;
		} finally {
			userLock.unlock();
		}
	}
}
