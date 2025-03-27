package io.hhplus.tdd.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class UserLockManager {

	private final Map<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

	/**
	 * 사용자 별 ReentrantLock을 반환합니다.
	 * 각 사용자별로 고유한 락을 생성하여 반환하며, 공정성을 보장합니다.
	 */
	public ReentrantLock getUserLock(long userId) {
		return userLocks.computeIfAbsent(userId, id -> new ReentrantLock(true));
	}
}
