package com.trainning.ordersystem;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.service.RedisService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootTest
public class RedisLockConcurrencyTest {

    @Autowired
    private RedisService redisService;

    @Test
    void testRaceCondition_UserA_HoldsLock_UserB_WaitsOrTimesOut() throws InterruptedException {
        Long productId = 999L; // Giả lập productId test
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch finishSignal = new CountDownLatch(2);

        AtomicBoolean userAGotLock = new AtomicBoolean(false);
        AtomicBoolean userBGotLock = new AtomicBoolean(false);
        AtomicBoolean userBTimedOut = new AtomicBoolean(false);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // THREAD 1: Người A vào trước, giữ lock 1.5 giây rồi nhả
        executor.submit(() -> {
            try {
                startSignal.await(); // Chờ xuất phát
                redisService.executeWithProductLocks(List.of(productId), () -> {
                    userAGotLock.set(true);
                    System.out.println("===> [User A] Đã cầm key, đang xử lý...");
                    try {
                        Thread.sleep(1500); // A giữ key 1.5s (< waitTime 3s)
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("===> [User A] Hoàn tất, nhả key!");
                    return null;
                });
            } catch (Exception e) {
                System.out.println("User A lỗi: " + e.getMessage());
            } finally {
                finishSignal.countDown();
            }
        });

        // THREAD 2: Người B vào tranh key
        executor.submit(() -> {
            try {
                startSignal.await();
                Thread.sleep(100); // Cho A chạy trước 0.1s để chắc chắn A cầm lock trước
                System.out.println("===> [User B] Bắt đầu vào tranh key...");
                redisService.executeWithProductLocks(List.of(productId), () -> {
                    userBGotLock.set(true);
                    System.out.println("===> [User B] ĐÃ CẦM ĐƯỢC KEY sau khi A nhả!");
                    return null;
                });
            } catch (AppException e) {
                System.out.println("===> [User B] Bị Timeout do chờ quá lâu: " + e.getMessage());
                userBTimedOut.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                finishSignal.countDown();
            }
        });

        // Bắt đầu cả 2 thread chạy cùng lúc
        startSignal.countDown();
        finishSignal.await(10, TimeUnit.SECONDS);

        // Assert kết quả:
        Assertions.assertTrue(userAGotLock.get(), "User A phải lấy được lock");
        Assertions.assertTrue(userBGotLock.get(), "User B phải đợi và lấy được lock sau khi A nhả");
        Assertions.assertFalse(userBTimedOut.get(), "User B không bị timeout vì A xử lý < 3s");
    }
}
