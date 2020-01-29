package org.example;



import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

public class AppTest {

    private ZooKeeper zooKeeper;

    /**
     * 1.创建Zookeeper连接
     * @throws IOException
     */
    @Before
    public void connectZooKeeperServer() throws IOException {
        zooKeeper = new ZooKeeper("192.168.0.110:2181", 200000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("path: " + event.getPath());
                System.out.println("state: " + event.getState());
                System.out.println("type: " + event.getType());
                System.out.println("wrapper: " + event.getWrapper());
            }
        }, false);
    }

    /**
     * 2.创建父节点
     */
    @Test
    public void createNode() throws KeeperException, InterruptedException {
        String spring = zooKeeper.create("/Spring", "change the java".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        //返回节点的路径
        System.out.println(spring);
    }

    /**
     * 3.创建子节点
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void createChildrenNode() throws KeeperException, InterruptedException {
        String spring = zooKeeper.create("/Spring/SpringMVC", "change the controller".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        //返回节点的路径
        System.out.println(spring);
    }

    /**
     * 4.获取节点的值
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void getNodeValue() throws KeeperException, InterruptedException {
        //用于接收节点的信息(创建时间、数据版本等)对象
        Stat stat = new Stat();
        byte[] fatherData = zooKeeper.getData("/Spring", false, stat);
        byte[] sonData = zooKeeper.getData("/Spring/SpringMVC", false, null);
        System.out.println(new String(fatherData));
        System.out.println(new String(sonData));
    }

    /**
     * 5.设置节点的值
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void setNodeValue() throws KeeperException, InterruptedException {
        zooKeeper.setData("/Spring", "Spring".getBytes(), -1);
    }

    /**
     * 6.判断某个节点是否存在
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void isExists() throws KeeperException, InterruptedException {
        Stat spring = zooKeeper.exists("/Spring", false);
        System.out.println(spring);
    }

    /**
     * 7.删除节点(节点必须为空)
     */
    @Test
    public void deleteNode() throws KeeperException, InterruptedException {
        zooKeeper.delete("/a", -1);
    }

    /**
     * 8.查询子节点
     * @throws KeeperException
     * @throws InterruptedException
     */
    @Test
    public void getChildrenNode() throws KeeperException, InterruptedException {
        List<String> children = zooKeeper.getChildren("/", false);
        children.forEach(System.out::println);
    }

    @Test
    public void treeSet() {
        Stream<String> stream = Stream.of("Java", "Python", "C#", "GoLand", "JavaScript", "ErLand", "Shell");
        ArrayList<String> langs = stream.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        TreeSet<String> set = langs.stream().collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        System.out.println(set.first());
        System.out.println(set.last());
        System.out.println(set.lower("C#"));
        System.out.println(set.lower("JavaScript"));
    }
}
