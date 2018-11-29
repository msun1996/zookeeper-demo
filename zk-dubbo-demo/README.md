
## ZK Dubbo整合实例

***pom.xml***
```
<dependency>
    <groupId>com.alibaba.boot</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
    <version>0.2.0</version>
</dependency>
<!--zk-->
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>4.0.1</version>
</dependency>
```

### Provider API
```java
public interface ProviderService {
    public void way();
}
```

### Provider Server
***com.provider.service.impl***
```java
@Service(
        version = "${provider.service.version}",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}"
)
public class ProviderServiceImpl implements ProviderService {

    @Override
    public void way() {

    }
}
```

***application.yml***
```yaml
dubbo:
  scan:
    basePackages: com.provider.service.impl
  application:
    id: provider
    name: provider
  protocol:
    id: dubbo
    name: dubbo
    port: 12345
  registry:
    id: my-provider
    address: 127.0.0.1:2181
    protocol: zookeeper
    client: curator
    timeout: 3000
provider:
  service:
    version: 1.0.0
```

### Consumer Server
```java
// zk 有registry时，不能再写url属性，会导致不注册
@Reference(
    version= "${product.service.version}",
    application = "${dubbo.application.id}",
    registry = "${dubbo.registry.id}",
    timeout = 10000
)
private ProductService productService;
```

***application.yml***
```java
dubbo:
  application:
    id: consumer
    name: consumer
  protocol:
    id: dubbo
    name: dubbo
    port: 123456
  registry:
    id: my-order
    address: 127.0.0.1:2181
    protocol: zookeeper
    client: curator
    timeout: 3000
provider:
  service:
    version: 1.0.0
```

## zk分布式锁

### 锁概念
*  `悲观锁`: 悲观锁假定其他用户企图访问或改变你正在访问更改的对象很高。在你**开始改变此对象之前就将对象锁住，并且直到你提交完成后才释放锁**。【加锁时间长，并发性不好】

*  `乐观锁`:其他用户企图改变你正在访问对象的概率很小，在你**准备提交所做更改时才将对象锁住，当你读取以及改变该对象时并不加锁**。【并发性好】【读锁是共享锁，写锁是排他锁】

---

*  `死锁`：当二或多个工作各自具有某个资源的锁定，但其它工作尝试要锁定此资源，而造成工作永久封锁彼此时，会发生死锁。
> 例如：
1)事务A 取得数据列 1 的共享锁定。
2)事务B 取得数据列 2 的共享锁定。
3)事务A 现在要求数据列 2 的独占锁定，但会被封锁直到事务B 完成并释出对数据列 2 的共享锁定为止。
4)事务B 现在要求数据列 1 的独占锁定，但会被封锁直到事务A 完成并释出对数据列 1 的共享锁定为止。
等到事务B 完成后，事务A 才能完成，但事务B 被事务A 封锁了。这个状况也称为「循环相依性」(Cyclic Dependency)。事务A 相依于事务B，并且事务B 也因为相依于事务A 而封闭了这个循环。

#### 分布式锁
*  **线程锁**：主要用来给方法、代码块加锁。当某个方法或代码使用锁，在同一时刻仅有一个线程执行该方法或该代码段。线程锁只在同一JVM中有效果，因为线程锁的实现在根本上是依靠线程之间共享内存实现的，比如synchronized是共享对象头，显示锁Lock是共享某个变量（state）。
*  **进程锁**：为了控制同一操作系统中多个进程访问某个共享资源，因为进程具有独立性，各个进程无法访问其他进程的资源，因此无法通过synchronized等线程锁实现进程锁。
*  **分布式锁**：当多个进程不在同一个系统中，用分布式锁控制多个进程对资源的访问

### 常用的分布式锁
*  基于数据库mysql
*  基于缓存redis
*  基于zookeeper
(基于ETCD)
---

实现复杂度：zk>=缓存>数据库  
性能： 缓存>zk>=数据库  
可靠性: zk>缓存>数据库  

---

**数据库锁**

优点：直接使用数据库，使用简单。
缺点：分布式系统大多数瓶颈都在数据库，使用数据库锁会增加数据库负担。

**缓存锁**

优点：性能高，实现起来较为方便，在允许偶发的锁失效情况，不影响系统正常使用，建议采用缓存锁。
缺点：通过锁超时机制不是十分可靠，当线程获得锁后，处理时间过长导致锁超时，就失效了锁的作用。

**zookeeper锁**

优点：不依靠超时时间释放锁；可靠性高；系统要求高可靠性时，建议采用zookeeper锁。
缺点：性能比不上缓存锁，因为要频繁的创建节点删除节点。

### SpringBoot+Curator分布式锁
***pom.xml***
```
<!--zk-->
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>4.0.1</version>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>4.0.1</version>
</dependency>
```

***CuratorConfiguration***
```java
/**
 * Curator配置注入 * @author: mSun
 * @date: 2018/11/29
 */
@Configuration
public class CuratorConfiguration {

    @Value("${curator.retryCount}")
    private int retryCount;

    @Value("${curator.elapsedTimeMs}")
    private int elapsedTimeMs;

    @Value("${curator.connectString}")
    private String connectString;

    @Value("${curator.sessionTimeoutMs}")
    private int sessionTimeoutMs;

    @Value("${curator.connectionTimeoutMs}")
    private int connectionTimeoutMs;

    @Bean(initMethod = "start")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(
                connectString,
                sessionTimeoutMs,
                connectionTimeoutMs,
                new RetryNTimes(retryCount, elapsedTimeMs)
        );
    }
}
```

***application.yml***
```yaml
#重试次数
curator:
  retryCount: 5
  #重试间隔时间
  elapsedTimeMs: 5000
  # zookeeper 地址
  connectString: 127.0.0.1:2181
  # session超时时间
  sessionTimeoutMs: 60000
  # 连接超时时间
  connectionTimeoutMs: 5000
```

***DistributedLockByCurator***
```java
/**
 * 锁操作 * @author: mSun
 * @date: 2018/11/29
 */
@Slf4j
@Service
public class DistributedLockByCurator implements InitializingBean {

    /**
     * 一级目录名
     */
    private static final String ROOT_PATH_LOCK = "product";

    /**
     * 用于挂起当前请求，等待上个请求释放锁
     */
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 注入curator客户端
     */
    @Autowired
    private CuratorFramework curatorFramework;

    /**
     * 创建父节点，并创建永久节点，初始化
      */
    @Override
    public void afterPropertiesSet() {
        curatorFramework = curatorFramework.usingNamespace("lock-namespace");
        String path = "/" + ROOT_PATH_LOCK;
        try {
            if (curatorFramework.checkExists().forPath(path) == null) {
                curatorFramework.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(path);
            }
            addWatcher(ROOT_PATH_LOCK);
            log.info("root path 的 watcher 事件创建成功");
        } catch (Exception e) {
            log.error("connect zookeeper fail，please check the log >> {}", e.getMessage(), e);
        }
    }

    /**
     * 获取分布式锁
     */
    public void acquireDistributedLock(String path) {
        String keyPath = "/" + ROOT_PATH_LOCK + "/" + path;

        // 使用死循环，当且仅当上一个锁释放并且当前请求获得所之后才可跳出
        while (true) {
            try {
                // 创建锁，异常则说明锁被占用
                curatorFramework
                        .create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(keyPath);
                log.info("success to acquire lock for path:{}", keyPath);
                break;
            } catch (Exception e) {
                // 等待锁释放
                log.info("failed to acquire lock for path:{}", keyPath);
                log.info("while try again .......");
                try {
                    if (countDownLatch.getCount() <= 0) {
                        countDownLatch = new CountDownLatch(1);
                    }
                    countDownLatch.await();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 释放分布式锁
     */
    public boolean releaseDistributedLock(String path) {
        try {
            String keyPath = "/" + ROOT_PATH_LOCK + "/" + path;
            if (curatorFramework.checkExists().forPath(keyPath) != null) {
                curatorFramework.delete().forPath(keyPath);
            }
        } catch (Exception e) {
            log.error("failed to release lock");
            return false;
        }
        return true;
    }

    /**
     * 创建 watcher 事件， 监听删除及节点从而释放锁
     */
    private void addWatcher(String path) throws Exception {
        String keyPath;
        if (path.equals(ROOT_PATH_LOCK)) {
            keyPath = "/" + path;
        } else {
            keyPath = "/" + ROOT_PATH_LOCK + "/" + path;
        }
        final PathChildrenCache cache = new PathChildrenCache(curatorFramework, keyPath, false);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener((client, event) -> {
            if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                String oldPath = event.getData().getPath();
                log.info("success to release lock for path:{}", oldPath);
                if (oldPath.contains(path)) {
                    //释放计数器，让当前的请求获取锁
                    countDownLatch.countDown();
                }
            }
        });
    }
}
```
##### 具体使用
```java
// 注入curator锁操作
@Autowired
private DistributedLockByCurator distributedLockByCurator;

// 添加锁
distributedLockByCurator.acquireDistributedLock("decreaseStock");

// 异常或操作完成释放锁
distributedLockByCurator.releaseDistributedLock("decreaseStock");
```
