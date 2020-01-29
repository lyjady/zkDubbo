package org.thread;


import org.example.Stock;
import org.lock.ZkLock;

import java.util.concurrent.locks.Lock;

/**
 * @author LinYongJin
 * @date 2020/1/26 17:47
 */
public class StockThead implements Runnable{


    Stock stock = new Stock();

    private static ZkLock lock;

    static {
        lock = new ZkLock("192.168.0.110:2182", "lock_stock");
    }

    @Override
    public void run() {
        lock.lock();
        if (stock.reduce()) {
            System.out.println(Thread.currentThread().getName() + " 扣减库存成功");
        } else {
            System.out.println(Thread.currentThread().getName() + " 扣减库存失败");
        }
        lock.unlock();
    }

    public static void main(String[] args) {
        new Thread(new StockThead()).start();
        new Thread(new StockThead()).start();
    }

}
