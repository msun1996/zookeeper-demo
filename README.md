# Zookeeper
* Zookeeper API实例
* Zookeeper Curator实例
* [Zookeeper Dubbo整合及Zookeeper分布式锁(基于SpringBoot订单与商品服务实例)][0]

# Zookeepr基础
> ZooKeeper是一种为分布式应用所设计的高可用、高性能且一致的开源 协调服务，它提供了一项基本服务：**分布式锁服务**。由于ZooKeeper的开源特性，后来我们的开发者在分布式锁的基础上，摸索了出了其他的使用方法：**配置维护、组服务、分布式消息队列**、**分布式通知/协调**等。

---

![](doc/zk%E5%BA%94%E7%94%A8%E5%9C%BA%E6%99%AF.jpg)

## zk的作用体现
*  首脑选举模式
*  统一配置文件管理
*  发布与订阅
*  提供分布式锁
*  集群管理，保证数据强一致性

## zk特性
*  **一致性**：数据一致性，数据按照顺序分批入库。
*  **原子性**：事务要么成功要么失败，不会局部化。
*  **单一视图**： 客户端连接集群中的任一zk节点，数据都是一致的
*  **可靠性**：每次对zk操作状态都保存在服务端(类似日志)
*  **实时性**： 客户端可以读取zk服务的最新数据
----
*  **session**
  *  客户端与服务端之间的连接存在会话
  *  每个会话会设置一个超时时间
  *  心跳结束，session则过期
  *  Session过期，则临时节点znode会被抛弃
  *  心跳机制:客户端向服务器的ping包请求
*  **watcher机制**
  *  针对每个节点的操作，都会有一个监督者->watcher
  *  当监控的某个对象(znode)发生变化，则触发watcher事件
  *  zk中的watcher是一次性的，触发后立即销毁
  *  父节点，子节点增删改都能触发器watcher
  *  针对不同类型的操作，触发的watcher事件也不同
    *  创建触发事件 ` NodeCreated`(父节点) `NodeChildrenChanged`(子节点)
    *  修改触发事件 `NodeDataChanged` `NodeChildrenDataChanged`
    *  删除触发事件 `NodeDeleted` `NodeChildrenDataChanged`

## Zookeeper安装

```shell
wget http://mirrors.hust.edu.cn/apache/zookeeper/zookeeper-3.4.13/zookeeper-3.4.13.tar.gz
tar -zxvf zookeeper-3.4.13.tar.gz
mv zookeeper-3.4.13 /usr/local/zookeeper
# 环境变量配置
vim /etc/profile
export ZOOKEEPER_HOME=/usr/local/zookeeper
export PATH=$PATH:$ZOOKEEPER_HOME/bin

source /etc/profile
# 运行
# 配置添加
cd /usr/local/zookeeper/conf/
cp zoo_sample.cfg zoo.cfg
# 启动zk
zkServer.sh start
# 连接zk，默认直接连接本地
zkCli.sh
```
### Zookeeper主要目录
*  `bin`: 主要存放一些允许命令
*  `conf`: 存放配置文件，包括日志文件
*  `contrib`： 附加功能
*  `dist-maven`: 编译打包文件
*  `lib`: 依赖jar包

### Zookeeper配置文件
***zoo_sample.cfg***
```shell
# 用于计算的时间单元
tickTime=2000
# 用于集群，允许 从节点 连接并同步到 master节点 的初始化连接时间，N*tickTime
initLimit=10
# 用于集群，master主节点 与 从节点 之间发送消息，请求和应答时间长度(心跳机制)
syncLimit=5
# zookeeper的事务等信息存放目录
dataDir=/tmp/zookeeper
# dataLogDor=/tmp/zookeeper
# 连接服务器端口
clientPort=2181
# the maximum number of client connections.
# increase this if you need to handle more clients
#maxClientCnxns=60
#
# Be sure to read the maintenance section of the 
# administrator guide before turning on autopurge.
#
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
#autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1

# 启动4字命令
4lw.commands.whitelist=*
```

## Zookeeper数据模型
### zk数据模型Znode
zk拥有一个层次命名空间，与标准文件系统的树结构类似

#### 1) 引用方式
Znode通过**路径引用**，路径必须绝对，即以"/"开头。

#### 2）Znode结构
zk命名空间Znode兼具文件和目录两种特点。即像文件一样维护着数据、元信息、ACL、时间戳等数据结构，又想目录可以作路径标识。
*  `stat`：状态信息，描述Znode版本权限信息。
*  `data`:与Znode关联的数据
*  `children`: 该Znode下子节点

#### 3）数据访问
zk每个节点存储的数据要被**原子性操作**

#### 4）节点类型
*  **临时节点：**该节点的生命周期依赖于创建它们的会话。一旦会话(Session)结束，临时节点将被自动删除，当然可以也可以手动删除。虽然每个临时的Znode都会绑定到一个客户端会话，但他们对所有的客户端还是可见的。另外，ZooKeeper的临时节点不允许拥有子节点。

*  **永久节点：**该节点的生命周期不依赖于会话，并且只有在客户端显示执行删除操作的时候，他们才能被删除。

#### 5)顺序节点
当创建Znode的时候，用户可以请求在zk的路径结尾添加一个**递增的计数**。这个计数**对于此节点的父节点来说**是唯一的。

#### 6)观察
客户端可以在节点上设置watch，我们称之为**[监视器]**。当节点状态发生改变时(Znode的增、删、改)将会触发watch所对应的操作。当watch被触发时，zk将会向客户端发送且仅发送一条通知

## ZK客户端命令
### 基础命令
```
显示根目录下、文件： ls / 使用 ls 命令来查看当前 ZooKeeper 中所包含的内容
显示根目录下、文件： ls2 / 查看当前节点数据并能看到更新次数等数据
创建文件，并设置初始内容： create /zk "test" 创建一个新的 znode节点“ zk ”以及与它关联的字符串
获取文件内容： get /zk 确认 znode 是否包含我们所创建的字符串
修改文件内容： set /zk "zkbak" 对 zk 所关联的字符串进行设置
删除文件： delete /zk 将刚才创建的 znode 删除
```

### 4字命令
```
echo stat|nc 127.0.0.1 2181 来查看哪个节点被选择作为follower或者leader
echo ruok|nc 127.0.0.1 2181 测试是否启动了该Server，若回复imok表示已经启动。
echo dump| nc 127.0.0.1 2181 ,列出未经处理的会话和临时节点。
echo kill | nc 127.0.0.1 2181 ,关掉server
echo conf | nc 127.0.0.1 2181 ,输出相关服务配置的详细信息。
echo cons | nc 127.0.0.1 2181 ,列出所有连接到服务器的客户端的完全的连接 / 会话的详细信息。
echo envi |nc 127.0.0.1 2181 ,输出关于服务环境的详细信息（区别于 conf 命令）。
echo reqs | nc 127.0.0.1 2181 ,列出未经处理的请求。
echo wchs | nc 127.0.0.1 2181 ,列出服务器 watch 的详细信息。
echo wchc | nc 127.0.0.1 2181 ,通过 session 列出服务器 watch 的详细信息，它的输出是一个与 watch 相关的会话的列表。
echo wchp | nc 127.0.0.1 2181 ,通过路径列出服务器 watch 的详细信息。它输出一个与 session 相关的路径。
```

#### ACL权限
在Zookeeper中，znode的ACL是没有继承关系的，是独立控制的。

**zookeeper支持的权限**
*  CREATE(c): 创建权限，可以在在当前node下创建child node
*  DELETE(d): 删除权限，可以删除当前的node
*  READ(r): 读权限，可以获取当前node的数据，可以list当前node所有的child nodes
*  WRITE(w): 写权限，可以向当前node写数据
*  ADMIN(a): 管理权限，可以设置当前node的permission

**zookeeper ACL 的组成**
`Scheme:id:permission`
*  `Scheme`: cheme对应于采用哪种方案来进行权限管理
*  `Id`: 权限被赋予的对象，比如ip或者某个用户
*  `Permission`： 权限，上面的crdwa，表示五个权限组合

**ZooKeeper ACL权限控制方法**
*  world
  *  `world:anyone:crdwa`表示任何用户都具有crdwa权限
```
setAcl /zk-acl world:anyone:ca
getAcl /zk-acl
```

*  auth
  *  `auth:username:password:crdwa`表示给认证通过的所有用户设置acl权限
```
addauth digest test1:123
create  /authpath 123 auth:test1:123:crdwa
get /authpath
getAcl /authpath
```

*  digest
  *  `digest:username:password:crdwa`指定某个用户及它的`BASE64(SHA1(username:password))`密码可以访问
```
create /digpath ddd
setAcl /digpath digest:test1:ubmgtiw94At8IplZnri3fHnZhsA=:cdrwa
getAcl /digpath
```

*  ip
  *  `ip:127.0.0.1:crdwa`指定某个ip地址可以访问

*  super
  *  在这种scheme情况下，对应的id拥有超级权限，可以做任何事情(cdrwa)
> 启动时，在命令参数zkCli.sh中配置: `-Dzookeeper.DigestAuthenticationProvider.superDigest = admin:015uTByzA4zSglcmseJsxTo7n3c=` 密码需要通过sha1和base64编码

## ZK集群

### ZK集群安装

#### 环境准备
|name|版本|内网IP|
|------|------|------|
|zk1|CentOS7|192.168.0.42|
|zk2|CentOS7|192.168.0.40|
|zk3|CentOS7|192.168.0.38|

#### java安装
```shell
yum install java-1.8.0-openjdk -y
```
#### zk安装
```shell
wget http://mirrors.hust.edu.cn/apache/zookeeper/zookeeper-3.4.13/zookeeper-3.4.13.tar.gz
tar -zxvf zookeeper-3.4.13.tar.gz
mv zookeeper-3.4.13 /usr/local/zookeeper
# 环境变量配置
vim /etc/profile
export ZOOKEEPER_HOME=/usr/local/zookeeper
export PATH=$PATH:$ZOOKEEPER_HOME/bin

source /etc/profile
cd /usr/local/zookeeper/
# 创建数据目录和日志保存目录
mkdir dataDir logDir
# 创建zk id (分别1，2，3)
echo "1">dataDir/myid
# 创建配置文件并编辑
cp conf/zoo_sample.cfg conf/zoo.cfg
vim conf/zoo.cfg

tickTime=2000
initLimit=10
syncLimit=5
dataDir=/usr/local/zookeeper/dataDir
dataLogDir=/usr/local/zookeeper/dataLogDir
clientPort=2181
server.1=192.168.0.42:2888:3888
server.2=192.168.0.40:2888:3888
server.3=192.168.0.38:2888:3888

# 运行
zkServer.sh start
# 打印日志
# zkServer.sh start-foreground
# 查看状态(所有节点启动完成后)
zkServer.sh status
```

### ZK测试
```shell
# 登录任意及节点
zkCli.sh -server 192.168.0.42:2181

create /real-cluster xyz
ls /
get /real-cluster

# 登录其他节点可执行(测试同步正常)
ls /
get /real-cluster
```

## 原生API 简单说明
|操作|说明|
|---|---|
|`String create(String path, byte[] data, List<ACL> acl,CreateMode createMode)`|创建一个给定的目录节点 path, 并给它设置数据CreateMode 标识有四种形式的目录节点，分别是 **PERSISTENT：持久化目录节点**，这个目录节点存储的数据不会丢失；PERSISTENT_SEQUENTIAL：顺序自动编号的目录节点，这种目录节点会根据当前已近存在的节点数自动加 1，然后返回给客户端已经成功创建的目录节点名；**EPHEMERAL：临时目录节点**，一旦创建这个节点的客户端与服务器端口也就是 session 超时，这种节点会被自动删除；EPHEMERAL_SEQUENTIAL：临时自动编号节点|
|`Stat exists(String path, boolean watch)`|判断某个 path 是否存在，并设置是否监控这个目录节点，这里的 watcher 是在创建 ZooKeeper 实例时指定的 watcher，exists方法还有一个重载方法，可以指定特定的watcher|
|`Stat exists(String path,Watcher watcher)`|重载方法，这里给某个目录节点设置特定的 watcher，Watcher 在 ZooKeeper 是一个核心功能，Watcher 可以监控目录节点的数据变化以及子目录的变化，一旦这些状态发生变化，服务器就会通知所有设置在这个目录节点上的 Watcher，从而每个客户端都很快知道它所关注的目录节点的状态发生变化，而做出相应的反应|
|`void delete(String path, int version)`|删除 path 对应的目录节点，version 为 -1 可以匹配任何版本，也就删除了这个目录节点所有数据|
|`List<String>getChildren(String path, boolean watch)`|获取指定 path 下的所有子目录节点，同样 getChildren方法也有一个重载方法可以设置特定的 watcher 监控子节点的状态|
|`Stat setData(String path, byte[] data, int version)`|给 path 设置数据，可以指定这个数据的版本号，如果 version 为 -1 怎可以匹配任何版本|
|`byte[] getData(String path, boolean watch, Stat stat)`|获取这个 path 对应的目录节点存储的数据，数据的版本等信息可以通过 stat 来指定，同时还可以设置是否监控这个目录节点数据的状态|
|`voidaddAuthInfo(String scheme, byte[] auth)`|客户端将自己的授权信息提交给服务器，服务器将根据这个授权信息验证客户端的访问权限。|
|`Stat setACL(String path,List<ACL> acl, int version)`|给某个目录节点重新设置访问权限，需要注意的是 Zookeeper 中的目录节点权限不具有传递性，父目录节点的权限不能传递给子目录节点。目录节点 ACL 由两部分组成：perms 和 id。Perms 有 ALL、READ、WRITE、CREATE、DELETE、ADMIN 几种 而 id 标识了访问目录节点的身份列表，默认情况下有以下两种：ANYONE_ID_UNSAFE = new Id("world", "anyone") 和 AUTH_IDS = new Id("auth", "") 分别表示任何人都可以访问和创建者拥有访问权限。|
|`List<ACL>getACL(String path,Stat stat)`|获取某个目录节点的访问权限列表|

  [0]: https://github.com/msun1996/zookeeper-demo/tree/master/zk-dubbo-demo
