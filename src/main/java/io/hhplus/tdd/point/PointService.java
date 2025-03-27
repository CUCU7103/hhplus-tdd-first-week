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

}
