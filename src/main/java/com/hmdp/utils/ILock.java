package com.hmdp.utils;

public interface ILock {

/**
 * 尝试获取锁，最多等待指定的时间
 *
 * @param timeoutSec 等待锁的最大时间（秒）
 * @return 如果成功获取锁则返回true，如果在指定时间内未获取到锁则返回false
 */
    boolean tryLock(long timeoutSec);

/**
 * 解锁方法
 * 该方法用于执行解锁操作，具体实现依赖于调用它的类
 * 通常用于释放资源、解除锁定状态或允许后续操作继续执行
 */
    void unLock();
}
