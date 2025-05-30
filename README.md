# 🐳 항해 99 1주차 과제
# Java 동시성 제어 방식들과 각 방식에 대한 장단점 비교


## 서론

동시성 이슈는 **여러 스레드가 동시에 공유 자원에 접근할 때 발생하는 문제** 입니다.

간단한 예시를 들어보자면 계좌에서 동시에 출금 요청이 들어왔을대 잔액이 음수가 되는 문제를 말할 수 있습니다.

이러한 문제를 해결하기 위해서 자바에는 다양한 동시성 제어 기법을 사용하여 원자성(atomicity), 가시성(visibility), 상호 배제(mutual exclusion) 를 보장할 수 있습니다.

대표적인 제어 기법인  Sycronzied, Atomic , Reentranlock 에 대해서 비교해보겠습니다.

## 본론



## 1. synchronized

### 개념 및 특징

- synchronized 키워드는 **<u>메서드나 블록 단위로 상호 배제를 보장하는 JVM 내장 락입니다</u>**.

- 한 번에 한 스레드만 접근 가능하며, 임계 구역의 코드가 완료될 때까지 다른 스레드는 대기 상태가 됩니다.

  - synchronized 키워드는 **동기화가 필요한 메소드나 코드 블럭 앞에 사용하여 동기화 할 수 있습니다.**
  - synchronized로 지정된 **임계영역은 한 스레드가 이 영역에 접근하여 사용할때 lock이 걸림으로써 다른 스레드가 접근할 수 없게 됩니다.**

- **이후 해당 **스레드가 이 임계영역의 코드를 다 실행 후 벗어나게 되면 unlock 상태가 되어 그때서야 대기하고 있던 다른 스레드가 이 임계영역에 접근하여 다시 lock을 걸고 사용할 수 있게 됩니다.

  - **synchronized로 설정된 임계영역은 lock 권한을 얻은 하나의 객체만이 독점적으로 사용하게됩니다.**

  - 즉 공유자원에 대한 작업은 하나의 스레드가 작업을 완료할때까지 다른 스레드들은 해당 공유자원에 접근이 불가하다고 생각하면 됩니다.

    

- 예시 코드

``` java
public class Counter {
	// 여러 스레드가 공유하는 자원
	private int count = 0;

	// 메소드 전체를 synchronized로 선언하는 방법
	public synchronized void increment() {
		count++;
		System.out.println("스레드 " + Thread.currentThread().getName() +
			"에 의해 증가됨: " + count);
	}

	public void decrement() {
		System.out.println("감소 작업이 진행됩니다~~");
		// 이 블록만 동기화됨
		synchronized(this) {
			count--;
			System.out.println("스레드 " + Thread.currentThread().getName() +
				"에 의해 감소됨: " + count);
		}
	}

	// 현재 카운트 값을 반환
	public synchronized int getCount() {
		return count;
	}

	public static void main(String[] args) {
		Counter counter = new Counter();

		// 증가 스레드 실행
		Thread incrementThread1 = new Thread(() -> {
		for (int i = 0; i < 5; i++) {
			counter.increment();
			try{
				Thread.sleep(100);
			}catch (InterruptedException e){
				e.printStackTrace();
			}
		}}
		);

		// 증가 스레드 실행
		Thread decrementThread1 = new Thread(() -> {
			for (int i = 0; i < 3; i++) {
				counter.decrement();
				try{
					Thread.sleep(300);
				}catch (InterruptedException e){
					e.printStackTrace();
				}
			}}
		);

		incrementThread1.start();
		decrementThread1.start();

		try {
			incrementThread1.join();
			decrementThread1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 최종 카운트 출력
		System.out.println("최종 카운트: " + counter.getCount());

	}
}
```

### 장점

- 명확한 원자성과 가시성을 제공합니다.
- 예외 발생 시 자동으로 락 해제되어 안전합니다.

### 단점

- sychronized는 **blocking 방식으로 동작**하기에 `synchronized` 블록에 진입한 스레드는 해당 블록이 끝날 때까지 락을 유지하므로, 다른 스레드들은 대기해야 합니다.
  - 락은 특정 자원을 보호하기 위해 스레드가 해당 자원에 대한 접근하는 것을 제한합니다.
  - 락이 걸려 있는 동안 다른 스레드들은 해당 자원에 접근할 수 없고, 락이 해제될 때까지 대기해야 합니다.
- **락 기반 접근에서는 락을 획득하고 해제하는 데 시간이 소요됩니다**.
  - 이로 인해 병목 현상이 발생하여 성능이 저하된다는 단점이 있습니다.
  - 여러 스레드가 서로 다른 자원을 기다며 무한 대기 상태에 빠지는 데드(deadlock) 상황이 발생할 수가 있습니다.
- 실무에서 sychronized는 위의 단점 때문에 거의 사용하지 않습니다.



## Atomic Class

### 개념 및 특징

- Atomic 클래스는 동시성 프로그래밍에서 **원자적(atomic)**인 연산을 제공하는 클래스들을 말합니다.
- atomic 클래스 들은 내부적으로 Compare-And-Swap(CAS) 알고리즘을 사용하여 동기화 없이도 스레드 안전한 연산을 수행할 수 있습니다
- 이러한 특성 덕분에 <u>Atomic 클래스는 **스레드 안전성**을 보장하면서도 **락(lock)**을 사용하지 않고 효율적으로 동기화를 구현할 수 있습니다</u>



예시 코드

```java
public class AtomicCounter {
	// AtomicInteger를 사용하여 스레드 안전한 카운터 구현
	private AtomicInteger count = new AtomicInteger(0);


	public void increment() {
		// getAndIncrement(): 현재 값을 반환한 후 증가
		// incrementAndGet(): 증가 후 새 값 반환
		int newValue = count.incrementAndGet();
		System.out.println("스레드 " + Thread.currentThread().getName() +
			"에 의해 증가됨: " + newValue);
	}


	public void decrement() {

		// getAndDecrement(): 현재 값을 반환한 후 감소
		// decrementAndGet(): 감소 후 새 값 반환
		int newValue = count.decrementAndGet();
		System.out.println("스레드 " + Thread.currentThread().getName() +
			"에 의해 감소됨: " + newValue);
	}

	// 현재 카운트 값을 반환
	public int getCount() {
		return count.get();
	}

	// 메인 메소드로 테스트
	public static void main(String[] args) {
		AtomicCounter counter = new AtomicCounter();

		// 증가 스레드 생성
		Thread incrementThread = new Thread(() -> {
			for (int i = 0; i < 5; i++) {
				counter.increment();
				try {
					Thread.sleep(100); // 스레드 실행 간격을 두기 위한 지연
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "증가 스레드");


		// 감소 스레드 생성
		Thread decrementThread = new Thread(() -> {
			for (int i = 0; i < 3; i++) {
				counter.decrement();
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "감소 스레드");

		// 스레드 시작
		incrementThread.start();
		decrementThread.start();

		// 모든 스레드가 완료될 때까지 대기
		try {
			incrementThread.join();
			decrementThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 최종 카운트 출력
		System.out.println("최종 카운트: " + counter.getCount());

	}
}

```



장점

- 높은 성능과 효율성을 보장합니다.
- 데드락이 없습니다.

단점

- **복잡한 연산의 한계**: 단순한 변수 업데이트에는 적합하지만, 복잡한 연산이나 여러 변수 간의 동기화가 필요한 경우에는 한계가 있습니다.
- **ABA 문제**: CAS는 값이 A에서 B로, 다시 A로 변경되는 경우를 감지하지 못합니다. 이는 특정 상황에서 문제를 일으킬 수 있습니다.



## 3. ReentrantLock

### 개념 및 특징 

- 자바의 동시성 제어를 위한 Lock 인터페이스의 구현체 중 하나로, 동일한 스레드가 여러 번 잠금을 획득할 수 있는 **재진입(reentrant)** 기능을 제공합니다. 
- 이는 기본적으로 synchronized 블록과 유사한 역할을 하지만, 추가적인 제어 및 확장 기능을 제공합니다

- **명시적 락 제어**: 코드에서 볼 수 있듯이 `lock()` 메소드로 락을 획득하고 `unlock()` 메소드로 락을 해제합니다. 
  - 이 명시적인 제어는 더 세밀한 락 관리를 가능하게 합니다.
- **try-finally 패턴**: 코드에서는 항상 락 해제를 보장하기 위해 try-finally 블록을 사용합니다. 이는 예외가 발생하더라도 락이 해제되도록 하는 중요한 패턴입니다.

- **공정성(Fairness) 옵션:** ReentrantLock은 공정 모드를 설정할 수 있어, 락을 획득하기 위한 대기 순서를 FIFO 방식으로 관리할 수 있습니다. 
  - 공정 모드를 사용하면 락의 획득 순서가 보장되지만, 성능에는 다소 영향이 있을 수 있습니다.
  - **<u>가장 오래 기다린 스레드에게 우선권을 주어 스레드 기아 현상을 방지할 수 있습니다.</u>**

- **타임아웃 기능**: `tryLock(long time, TimeUnit unit)` 메소드를 사용하여 지정된 시간 동안만 락 획득을 시도할 수 있습니다. 
  - 이는 데드락 방지에 유용합니다.



### 코드 예시

``` java
public class ReentrantLockCounter {
    // 카운터 값
    private int count = 0;
    
    // ReentrantLock 인스턴스 생성
    private final Lock lock = new ReentrantLock(true);
    
    // 증가 메소드
    public void increment() {
        // 락 획득 시도
        lock.lock();
        try {
            // 임계 영역 - 락을 획득한 스레드만 실행 가능
            count++;
            System.out.println("스레드 " + Thread.currentThread().getName() + 
                              "에 의해 증가됨: " + count);
        } finally {
            // 항상 락을 해제하도록 finally 블록에 작성
            lock.unlock();
        }
    }
    
    // 감소 메소드
    public void decrement() {
        // 락 획득을 시도하기 전 메시지 출력
        System.out.println("스레드 " + Thread.currentThread().getName() + 
                          "가 감소 시작");
        
        // 락 획득 시도
        lock.lock();
        try {
            // 임계 영역
            count--;
            System.out.println("스레드 " + Thread.currentThread().getName() + 
                              "에 의해 감소됨: " + count);
        } finally {
            // 항상 락을 해제
            lock.unlock();
        }
    }

    
    // 현재 카운트 값을 반환
    public int getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
    

    // 메인 메소드로 테스트
    public static void main(String[] args) {
        // 기본 ReentrantLock 사용 예시
        System.out.println("=== ReentrantLock 예제 ===");
        basicLockExample();

    }
    
    private static void basicLockExample() {
        ReentrantLockCounter counter = new ReentrantLockCounter();
        
        // 증가 스레드 생성
        Thread incrementThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                counter.increment();
                try {
                    Thread.sleep(150); // 스레드 실행 간격을 두기 위한 지연
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "증가 스레드");

        
        // 감소 스레드 생성
        Thread decrementThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                counter.decrement();
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "감소 스레드");
        
        // 스레드 시작
        incrementThread.start();
        decrementThread.start();
        
        // 모든 스레드가 완료될 때까지 대기
        try {
            incrementThread.join();
            decrementThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 최종 카운트 출력
        System.out.println("최종 카운트: " + counter.getCount());
    }

}
```



**장점**

- **재진입(Reentrancy):** 동일 스레드가 여러 번 락을 획득할 수 있어, 복잡한 재귀 호출이나 중첩된 메서드 호출 시 안전하게 사용할 수 있습니다.
- **명시적 제어:** lock()과 unlock()을 명시적으로 호출할 수 있어, 락 획득과 해제 시점을 세밀하게 관리할 수 있습니다.
- **Interruptible 락 획득:** lockInterruptibly()를 통해 대기 중인 스레드가 인터럽트를 받을 수 있어, 시스템의 응답성을 높일 수 있습니다.
- **TryLock 메서드:** tryLock()을 사용하면 락 획득 실패 시 즉시 대체 로직을 실행할 수 있어, 불필요한 대기를 줄일 수 있습니다.
- **Condition 지원:** newCondition()을 통해 wait/notify 패턴보다 세밀한 스레드 협업을 구현할 수 있습니다.

**단점**

- **명시적 해제 필요:** 락을 명시적으로 해제해야 하므로, try-finally 구문을 사용하지 않으면 데드락 위험이 있습니다.
- **복잡성 증가:** synchronized에 비해 코드 복잡도가 높아, 잘못 사용할 경우 예상치 못한 동시성 문제가 발생할 수 있습니다.
- **오버헤드:** 내부 구현에 따른 오버헤드가 존재할 수 있으며, 상황에 따라 성능 저하 요인이 될 수 있습니다.
