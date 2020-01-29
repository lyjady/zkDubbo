package org.lock;

import com.sun.source.tree.Tree;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author LinYongJin
 * @date 2020/1/29 15:39
 */
public class ZkLock implements Lock {

    //zookeeper客户端
    private ZooKeeper zooKeeper;

    private final String root = "/lock";

    private String lockName;

    //存放执行线程的数据, 用于存放指定线程的节点名称, 当指定的线程要删除节点的时候从里面取出来, 就是之前的这个线程存放的那个节点
    private ThreadLocal<String> nodeId = new ThreadLocal<>();

    //用于线程挂起
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private final static int sessionTimeout = 3000;

    private final static byte[] data = new byte[0];

    public ZkLock(String config, String lockName) {
        this.lockName = lockName;
        try {
            zooKeeper = new ZooKeeper(config, sessionTimeout, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            Stat exists = zooKeeper.exists(root, false);
            if (exists == null) {
                zooKeeper.create(root, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

        } catch (IOException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }

    }

    class LcokWatch implements Watcher {

        private CountDownLatch countDownLatch = null;

        public LcokWatch(CountDownLatch latch) {
            this.countDownLatch = latch;
        }

        @Override
        public void process(WatchedEvent event) {
            //如果是节点删除那么计数-1
            if (event.getType() == Event.EventType.NodeDeleted) {
                countDownLatch.countDown();
            }
        }
    }

    @Override
    public void lock() {
        try {
            //创建零时顺序节点
            String temporaryNode = zooKeeper.create(root + "/" + lockName, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(Thread.currentThread().getName() + "-" + temporaryNode + " created");
            //获得节点并进行排序
            List<String> children = zooKeeper.getChildren(root, false);
            TreeSet<String> sortedNodes = new TreeSet<>();
            children.forEach(child -> {
                sortedNodes.add(root + "/" + child);
            });
            //取到第一个节点并判断自己是不是第一个节点
            String smallNode = sortedNodes.first();
            if (temporaryNode.equals(smallNode)) {
                System.out.println(Thread.currentThread().getName() + " get lock");
                this.nodeId.set(temporaryNode);
                return;
            }
            //如果不是第一个节点, 就对前一个节点进行监听, 并挂起。前一个节点删除之后将节点的信息放入到ThreadLocal中
            String preNode = sortedNodes.lower(temporaryNode);
            CountDownLatch latch = new CountDownLatch(1);
            Stat exists = zooKeeper.exists(preNode, new LcokWatch(latch));
            if (exists != null) {
                System.out.println(Thread.currentThread().getName() + " waiting for " + root + "/" + preNode + " release lock");
                latch.await();
                nodeId.set(temporaryNode);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        try {
            System.out.println(Thread.currentThread().getName() + " release lock");
            if (nodeId != null) {
                //取出当前线程之前存放的节点信息, 删除
                zooKeeper.delete(nodeId.get(), -1);
            }
            //ThreadLocal清空
            nodeId.remove();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
