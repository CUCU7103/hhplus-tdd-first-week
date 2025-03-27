package io.hhplus.tdd.point;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.error.CustomErrorCode;
import io.hhplus.tdd.error.CustomException;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PointServiceIntegrationTest {

	@Autowired
	private PointService pointService;
	@Autowired
	private UserPointTable userPointTable;
	@Autowired
	private PointHistoryTable pointHistoryTable;

	@ValueSource(longs = {0L, -100L})
	@ParameterizedTest
	void 유효하지_않은_아이디를_통해_유저_포인트를_조회시_예외가_발생한다(long id) {

		assertThatThrownBy(() -> pointService.searchPointHistory(id))
			.isInstanceOf(CustomException.class).hasMessageContaining(
				CustomErrorCode.INVALID_ID.getMessage());

	}

	@ValueSource(longs = {0L, -100L})
	@ParameterizedTest
	void 유효하지_않은_아이디를_통해_유저의_포인트_충전_사용내역_조회시_예외가_발생한다(long id) {

		assertThatThrownBy(() -> pointService.searchPointHistory(id))
			.isInstanceOf(CustomException.class).hasMessageContaining(
				CustomErrorCode.INVALID_ID.getMessage());

	}

	@Test
	void 포인트_충전_이용내역_조회시_유저의_포인트가_0보다_작은_기록이_있다면_예외가_발생한다() {

		long id = 1L;
		pointHistoryTable.insert(id, -100L, TransactionType.CHARGE,
			System.currentTimeMillis());
		pointHistoryTable.insert(id, -100L, TransactionType.CHARGE,
			System.currentTimeMillis());

		// act & assert 검증
		assertThatThrownBy(() -> pointService.searchPointHistory(id))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(CustomErrorCode.INVALID_AMOUNT.getMessage());
	}

	@ValueSource(longs = {1000L, 500L, 3700L})
	@ParameterizedTest
	void 유효한_아이디와_포인트일때_충전에_성공한다(long chargedAmount) {
		// 유저 포인트 객체 생성
		long id = 1L;
		long amount = 200L;
		long expectAmount = chargedAmount + amount;
		userPointTable.insertOrUpdate(id, amount);
		// 포인트 충전
		UserPoint chargedUser = pointService.charge(id, chargedAmount);
		// 충전 후 검증
		assertThat(pointService.searchPointHistory(id)).hasSize(1);
		assertThat(chargedUser.point()).isEqualTo(expectAmount);
		assertThat(chargedUser.id()).isEqualTo(userPointTable.selectById(id).id());
		assertThat(pointHistoryTable.selectAllByUserId(id)).hasSize(1);
		assertThat(pointHistoryTable.selectAllByUserId(id).get(0).amount()).isEqualTo(expectAmount);
		assertThat(pointHistoryTable.selectAllByUserId(id).get(0).userId()).isEqualTo(chargedUser.id());
		assertThat(pointHistoryTable.selectAllByUserId(id).get(0).type()).isEqualTo(TransactionType.CHARGE);

	}

	@ValueSource(longs = {0L, -100L})
	@ParameterizedTest
	void 유효한_아이디가_아닐때_충전에_실패한다(long id) {
		long amount = 200L;
		long chargedAmount = 1000L;
		userPointTable.insertOrUpdate(id, amount);

		assertThatThrownBy(() -> pointService.charge(id, chargedAmount))
			.hasMessageContaining(CustomErrorCode.INVALID_ID.getMessage());
	}

	@ValueSource(longs = {0L, -100L})
	@ParameterizedTest
	void 유효한_포인트가_아닐때_충전에_실패한다(long amount) {
		long id = 1L;
		long currentAmount = 1000L;
		userPointTable.insertOrUpdate(id, currentAmount);

		assertThatThrownBy(() -> pointService.charge(id, amount))
			.hasMessageContaining(CustomErrorCode.INVALID_AMOUNT.getMessage());
	}

	@ValueSource(longs = {1000L, 500L})
	@ParameterizedTest
	void 유효한_포인트와_사용자일_경우_포인트_사용에_성공한다(long usedAmount) {
		long id = 1L;
		long amount = 2000L;
		long expectedAmount = amount - usedAmount;
		userPointTable.insertOrUpdate(id, amount);
		// 포인트 사용
		UserPoint usedPoint = pointService.use(id, usedAmount);
		// 사용 후 검증
		assertThat(pointService.searchPointHistory(id)).hasSize(1);
		assertThat(usedPoint.point()).isEqualTo(expectedAmount);
		assertThat(usedPoint.id()).isEqualTo(userPointTable.selectById(id).id());
		assertThat(pointHistoryTable.selectAllByUserId(id)).hasSize(1);
		assertThat(pointHistoryTable.selectAllByUserId(id).get(0).amount()).isEqualTo(expectedAmount);
		assertThat(pointHistoryTable.selectAllByUserId(id).get(0).userId()).isEqualTo(usedPoint.id());
		assertThat(pointHistoryTable.selectAllByUserId(id).get(0).type()).isEqualTo(TransactionType.USE);
	}

	@Test
	void 동시에_같은_사용자가_충전시_순차적으로_충전되어진다() throws InterruptedException {
		long id = 1L;
		long currentAmount = 1000L;
		long updatedAmount = 4500L;
		int threadAmount = 10;
		userPointTable.insertOrUpdate(id, currentAmount);

		CountDownLatch latch = new CountDownLatch(threadAmount);
		ExecutorService executorService = Executors.newFixedThreadPool(threadAmount);

		AtomicInteger successCount = new AtomicInteger();

		for (int i = 0; i < threadAmount; i++) {
			executorService.submit(() -> {
				try {
					pointService.charge(id, updatedAmount);
					successCount.incrementAndGet();
				} catch (CustomException e) {
					e.getCustomErrorCode();
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executorService.shutdown();

		assertThat(successCount.get()).isEqualTo(10);
		assertThat(pointHistoryTable.selectAllByUserId(id)).hasSize(10);
		assertThat(userPointTable.selectById(id).point()).isEqualTo(currentAmount + (updatedAmount * threadAmount));
		assertThat(pointHistoryTable.selectAllByUserId(id).get(9).amount()).isEqualTo(
			currentAmount + (updatedAmount * threadAmount));

	}

	@Test
	void 동시에_들어온_포인트_사용요청이_보유한_포인트_보다_많을_경우_나중에_들어온_요청들은_예외를_던진다() throws InterruptedException {
		long id = 1L;
		long currentAmount = 5000L;
		long usedAmount = 2500L;
		int threadAmount = 10;
		userPointTable.insertOrUpdate(id, currentAmount);

		CountDownLatch latch = new CountDownLatch(threadAmount);
		ExecutorService executorService = Executors.newFixedThreadPool(threadAmount);

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		for (int i = 0; i < threadAmount; i++) {
			executorService.submit(() -> {
				try {
					pointService.use(id, usedAmount);
					successCount.incrementAndGet();
				} catch (CustomException e) {
					failCount.incrementAndGet();
					throw new CustomException(e.getCustomErrorCode(), e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executorService.shutdown();

		assertThat(successCount.get()).isEqualTo(2);
		assertThat(failCount.get()).isEqualTo(8);
		assertThat(pointHistoryTable.selectAllByUserId(id)).hasSize(successCount.intValue());
		assertThat(userPointTable.selectById(id).point()).isEqualTo(0);
	}

	@Test
	void 동일한_사용자의_충전_사용_동시_요청을_순차적으로_처리한다() throws InterruptedException {
		long id = 1L;
		long currentAmount = 1000L;
		long updatedAmount = 6000L;
		long usedAmount = 500L;
		int threadAmount = 10;
		userPointTable.insertOrUpdate(id, currentAmount);
		
		CountDownLatch latch = new CountDownLatch(threadAmount * 2);
		ExecutorService executorService = Executors.newFixedThreadPool(threadAmount);

		AtomicInteger chargePointSuccessCount = new AtomicInteger();
		AtomicInteger usedPointSuccessCount = new AtomicInteger();

		// 최종 60000포인트 예상
		for (int i = 0; i < threadAmount; i++) {
			executorService.submit(() -> {
				try {
					pointService.charge(id, updatedAmount);
					chargePointSuccessCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		// 동시에 포인트 500씩 10번 사용 (5000 사용예상)
		for (int i = 0; i < threadAmount; i++) {
			executorService.submit(() -> {
				try {
					pointService.use(id, usedAmount);
					usedPointSuccessCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executorService.shutdown();

		assertThat(chargePointSuccessCount.get()).isEqualTo(10);
		assertThat(usedPointSuccessCount.get()).isEqualTo(10);
		assertThat(userPointTable.selectById(id).point()).isEqualTo(
			currentAmount + ((updatedAmount * threadAmount) - (usedAmount * threadAmount)));
		assertThat(pointHistoryTable.selectAllByUserId(id)).hasSize(threadAmount * 2);
	}

	@Test
	void 서로_다른_사용자의_요청을_병렬적으로_처리한다() throws InterruptedException {
		long id = 1L;
		long currentAmount = 1000L;
		long updatedAmount = 6000L;
		long usedAmount = 500L;
		int userCount = 10;
		// 여러 유저의 정보를 초기 셋팅
		for (int i = 1; i <= userCount; i++) {
			userPointTable.insertOrUpdate(id, currentAmount);
		}

		CountDownLatch latch = new CountDownLatch(userCount);
		ExecutorService executorService = Executors.newFixedThreadPool(userCount);

		AtomicInteger successCount = new AtomicInteger();

		for (int i = 1; i <= userCount; i++) {
			final long userId = i;
			executorService.submit(() -> {
				try {
					pointService.charge(userId, updatedAmount);
					pointService.use(userId, usedAmount);
					successCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executorService.shutdown();

		assertThat(successCount.get()).isEqualTo(userCount);
		for (int i = 1; i <= userCount; i++) {
			assertThat(userPointTable.selectById(id).point()).isEqualTo(currentAmount + (updatedAmount - usedAmount));
			assertThat(pointHistoryTable.selectAllByUserId(id)).hasSize(2);
		}
	}

}
