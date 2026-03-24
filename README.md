项目实战系列<br>
领养社区平台：从 0 到 1 构建一个 B2C 宠物领养平台
需求分析 & 技术选型
数据库设计 & 索引优化
缓存架构：布隆过滤器防缓存穿透
幂等设计：基于 Redis 的接口幂等实现
宠物交易平台：高并发场景下的技术实践
分布式锁解决超卖问题（基于 Redis SETNX + Lua）
缓存一致性：Cache Aside 模式 + 消息队列异步通知
点赞功能的设计与优化（简化版：Redis Set + 定时同步）
全栈协作：Vue + ElementUI 开发后台管理系统的经验
技术专题系列
Java 集合源码分析（ArrayList、HashMap、ConcurrentHashMap）
JVM 内存模型与垃圾回收
Spring Boot 自动配置原理
MySQL 索引优化实战（结合 EXPLAIN）
Redis 数据结构与应用场景
踩坑与解决方案
缓存穿透、雪崩、击穿的应对
事务失效的几种场景
分布式锁的常见问题（锁超时、误删、可重入）
分页查询性能优化
读书 & 学习笔记
《Java 并发编程实战》关键概念整理
《Redis 深度历险》读书笔记
面试经验与总结
实习面试常见问题与回答思路
技术博客的意义与收获
二、第一篇示例博客：布隆过滤器在领养社区中的应用
这篇博客聚焦于你真正实现过的功能——布隆过滤器防止缓存穿透。你可以把它写得详细、具体，让读者（和面试官）看到你对技术的理解深度。
布隆过滤器在领养社区平台中的应用：防止缓存穿透
1. 背景
在开发领养社区平台时，我们遇到了一个典型的缓存穿透问题。平台提供宠物详情页，用户可以通过宠物 ID 查看详细信息。正常情况下，热点数据会缓存在 Redis 中，以减轻数据库压力。
但恶意用户或爬虫可能不断请求大量不存在的宠物 ID（例如 ID 为负数或超大数值），导致这些请求绕过 Redis 直接打到数据库上。如果并发量高，数据库可能瞬间压力激增，甚至崩溃。
2. 缓存穿透的常见解决方案
业界常用的方案有三种：
缓存空对象：对不存在的 key 也缓存一个空值，设置较短的过期时间。缺点是会占用内存，且短时间内大量不存在 key 仍会打到数据库（缓存未命中时）。
布隆过滤器：在缓存前加一层布隆过滤器，存储所有存在的宠物 ID。请求到达时先经过布隆过滤器判断，如果过滤器认为不存在，直接返回，不再查询缓存或数据库。
请求限流 & 参数校验：对 ID 做基础校验（如必须为正整数），但无法防止大量存在的但非热门的 ID 穿透。
考虑到内存占用和准确性，我们选择了布隆过滤器。
3. 布隆过滤器原理
布隆过滤器是一种空间效率极高的概率型数据结构，用于判断一个元素是否可能存在于一个集合中。它的特点是：
如果布隆过滤器说一个元素不存在，那它一定不存在。
如果布隆过滤器说一个元素存在，那它可能存在（存在一定的误判率）。
内部实现：一个位数组和多个哈希函数。添加元素时，用哈希函数计算多个位置，并将这些位置设为 1；判断元素是否存在时，同样计算哈希值，如果所有位都是 1，则判断为存在；如果任意一位为 0，则判断为不存在。
4. 实现方案
我们使用 Redisson 提供的 RedissonBloomFilter，它基于 Redis 的 Bitmap 实现，使用方便，支持自定义误判率。
4.1 引入依赖
   <dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.23.2</version>
</dependency>
4.2 配置布隆过滤器
   /**
     * 初始化宠物ID布隆过滤器
     */
    @Bean
    public RBloomFilter<Integer> petIdBloomFilter() {
        // 前置校验：RedissonClient不能为空
        if (redissonClient == null) {
            throw new RuntimeException("RedissonClient 未初始化，无法创建布隆过滤器");
        }

        RBloomFilter<Integer> bloomFilter = redissonClient.getBloomFilter("petIdBloomFilter");
        // 初始化：预期插入数量 100000，误判率 0.01
        boolean initSuccess = bloomFilter.tryInit(100000L, 0.01);
        if (initSuccess) {
            log.info("宠物ID布隆过滤器初始化成功");
        } else {
            log.info("宠物ID布隆过滤器已存在，无需重复初始化");
        }
        return bloomFilter;
    }
    // 补充：添加日志（如果需要在配置类中打印日志）
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BloomFilterConfig.class);
4.3 在业务代码中使用
  /**
     * 带布隆过滤器和缓存的宠物查询
     */
    public Pet getCachedPetById(Integer id) {
        // 1. 布隆过滤器拦截
        if (!petIdBloomFilter.contains(id)) {
            return null; // 肯定不存在
        }

        // 2. 查缓存
        String cacheKey = "pet:" + id;
        Pet pet = (Pet) redisTemplate.opsForValue().get(cacheKey);
        if (pet != null) {
            return pet;
        }

        // 3. 分布式锁回源查数据库
        String lockKey = "lock:pet:" + id;
        RLock lock = redissonClient.getLock((lockKey));
        try {
            if (lock.tryLock(2, 30, TimeUnit.SECONDS)) {
                // 双重检查缓存
                pet = (Pet) redisTemplate.opsForValue().get(cacheKey);
                if (pet != null) {
                    return pet;
                }

                // 查询数据库
                pet = petMapper.selectById(id);

                // 4. 写入缓存（空值缓存5分钟，正常缓存30分钟）
                if (pet == null) {
                    redisTemplate.opsForValue().set(cacheKey, new Pet(), 5, TimeUnit.MINUTES);
                } else {
                    redisTemplate.opsForValue().set(cacheKey, pet, 30, TimeUnit.MINUTES);
                }
                return pet;
            } else {
                Thread.sleep(100);
                return getCachedPetById(id); // 递归重试
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    //用于将 Pet 转换为 PetVO
    private PetVO convertToVO(Pet pet) {
        if (pet == null) {
            return null;
        }
        PetVO vo = new PetVO();
        // 使用 Spring BeanUtils 复制相同属性
        org.springframework.beans.BeanUtils.copyProperties(pet, vo);
        // 如果有特殊字段需要处理，在这里补充
        return vo;
    }
}
5. 效果与思考
效果：通过布隆过滤器，我们拦截了约 90% 的无效 ID 请求（测试环境下），数据库压力显著降低。
误判率：我们设置的误判率为 1%，意味着最多有 1% 的不存在 ID 会绕过过滤器打到数据库，但这部分我们可以通过短时间缓存空对象进一步兜底。
更新问题：当新增宠物时，需要同步更新布隆过滤器。我们通过消息队列（RabbitMQ）异步添加新 ID 到过滤器中，保证最终一致性。


幂等接口设计实战：基于 Redis 防止领养申请重复提交
1. 问题背景
在领养社区平台中，用户提交领养申请是一个核心操作。前端可能由于网络延迟、用户双击等原因，导致同一个申请被多次提交。如果不对后端接口做幂等处理，
可能会造成数据库中产生多条相同的领养申请记录，甚至影响后续的业务逻辑（如重复扣减库存、重复发送通知等）。
幂等的定义：同一个操作，无论执行多少次，产生的结果与执行一次相同。对于领养申请接口，我们希望：即使用户短时间内连续提交多次，最终也只生成一条有效的申请记录。
2. 常见幂等方案
方案	            原理	                                              适用场景	                    优缺点
数据库唯一约束	在关键字段上建立唯一索引（如 user_id + pet_id + date）	防重复插入	                简单可靠，但需提前设计好唯一键
乐观锁	        使用版本号或状态字段，更新时校验	                      更新操作	                  适合更新场景，不适合纯插入
防重 Token	    服务端下发 Token，提交时校验并删除	                    表单提交	                  可靠但需要前后端配合
Redis 分布式锁	基于业务唯一标识生成锁 Key，加锁成功后执行	            通用场景	                  性能好，适合高并发
我们选择 Redis SETNX + 业务唯一标识 的方式，因为它实现简单、性能高，并且我们已在项目中使用了 Redis。
3. 设计思路
3.1 幂等 Key 的构成
对于领养申请，一个用户对同一个宠物只能提交一次申请（业务规则），因此我们可以用 user_id + pet_id 作为幂等 Key。
但考虑到业务可能允许同一用户对同一宠物提交多次申请（比如被拒绝后再次申请），我们改为更通用的方案：使用请求的唯一标识（如前端生成的 UUID） 作为幂等 Key。
这样设计的好处是：
不依赖业务规则，灵活性高
每次请求都有自己的唯一 ID，适用于任何需要幂等的接口
前端可以生成 UUID 并传递给后端，后端将其作为幂等 Key
3.2 幂等处理流程
前端在请求头或请求体中传入一个 idempotentId（UUID）。
后端接收请求后，使用 Redis 的 SETNX 命令尝试设置 Key：idempotent:{接口名}:{idempotentId}。
如果 SETNX 返回 1（设置成功），说明本次请求是第一次，正常执行业务逻辑。
如果 SETNX 返回 0（Key 已存在），说明该请求已经被处理过，直接返回“重复提交”或已成功的结果。
业务处理完成后，不立即删除 Key，而是让它自然过期，以避免在业务执行期间再次提交。
设置合理的过期时间（如 30 秒~2 分钟），保证在正常业务处理时间内 Key 有效。
3.3 为什么不用锁过期后删除？
如果业务执行完就删除 Key，在高并发下可能出现：
请求 A 获得锁，执行业务，删除锁
请求 B 在请求 A 删除锁后的瞬间获取到锁，而请求 A 可能还没完全提交结果，导致重复处理
因此，我们采用 “锁持有至业务完成，但不过早删除” 的策略，依靠过期时间来清理。
4. 代码实现
4.1 自定义注解
   
4.2 幂等拦截器（AOP）
private static final String LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // 获取令牌
        String token = null;
        if("header".equalsIgnoreCase(idempotent.location())) {
            token = request.getHeader(idempotent.location());
        } else if ("parameter".equalsIgnoreCase(idempotent.location())) {
            token = request.getParameter(idempotent.key());}

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("幂等性令牌不能为空");
        }

        String redisKey = "idempotent:token:" + token;
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(redisKey), token);

        if (result == null || result == 0) {
            throw new RuntimeException("请勿重复提交");
        }

        return joinPoint.proceed();
    }
4.4 前端配合
const idempotentId = uuidv4();
axios.post('/api/adoption/apply', data, {
    headers: { 'Idempotent-Token': idempotentId }
});
5. 总结
幂等接口设计是高并发系统的基石之一。
通过 Redis SETNX 实现幂等，既保证了高性能，又解决了重复提交的问题。
在实际项目中，我们还需要结合业务特点选择合适的幂等 Key，并考虑异常场景下的处理策略（如删除 Key 允许重试）。



分布式锁防超卖实战：基于 Redis 解决高并发下的库存扣减问题
1. 问题背景
在宠物交易与互动平台中，我们推出了一个“宠物标签”活动：某个热门宠物会有一个限量名额，用户可以抢购或领养。这类场景最典型的问题就是超卖——库存只有 1 个，却有多个用户同时下单，最终导致实际售卖数量超过库存。
超卖的本质：在并发环境下，多个线程同时读取到库存还有余量，然后各自执行扣减操作，导致库存被扣成负数。
要解决超卖，核心是保证库存扣减操作的原子性。在单体应用中，可以通过数据库乐观锁或 synchronized 实现；但在分布式架构下，我们需要一种跨进程的互斥机制——分布式锁。
2. 技术选型我们选择 Redis 作为分布式锁的实现载体，原因如下：
Redis 单线程模型，操作原子性强
SETNX 命令天然支持互斥
高性能，适合高并发场景
团队已有 Redis 基础设施
我们采用 SETNX + Lua 脚本 的方式，将“加锁 + 设置过期时间”合并为一个原子操作，避免死锁问题。
3. 设计方案
3.1 核心思路
对于每个宠物的库存扣减，我们以 pet_id 为锁的 Key。在扣减库存之前，先尝试获取锁：
获取成功：执行库存扣减业务，然后释放锁。
获取失败：说明其他线程正在处理，当前请求等待或直接返回“抢购失败”。
3.2 锁的要点
原子性：加锁和设置过期时间必须原子执行，否则可能加锁后程序崩溃，导致锁永远不释放（死锁）。
锁的持有者标识：为了防止误删别人的锁，释放锁时需要验证当前线程是否持有锁。
锁的过期时间：防止业务执行时间过长，导致锁自动释放，其他线程进入，引起并发问题。但过期时间也不能太短，以免业务未完成锁就失效。
重试机制：获取锁失败时，可以根据业务需求进行短暂等待后重试。
3.3 为什么用 Lua 脚本
Redis 执行 Lua 脚本时，整个脚本会作为一个原子操作执行，中间不会被其他命令打断。因此，我们可以将“判断锁是否存在 + 删除锁”放在一个 Lua 脚本中，确保释放锁时是原子的，避免误删。
4. 代码实现
4.1 加锁与释放锁的工具类
java
@Component
public class RedisDistributedLock {

    private final StringRedisTemplate redisTemplate;

    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取锁
     * @param lockKey 锁的Key
     * @param requestId 请求标识，用于区分不同线程，可以是 UUID
     * @param expireSeconds 锁自动释放时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String requestId, long expireSeconds) {
        // SET key value NX EX seconds 原子性设置
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, requestId, Duration.ofSeconds(expireSeconds));
        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放锁（Lua脚本保证原子性）
     * @param lockKey 锁的Key
     * @param requestId 请求标识，用于验证是否是自己的锁
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String requestId) {
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), requestId);
        return result != null && result == 1;
    }
}
4.2 业务中使用分布式锁
以宠物领养（抢购）接口为例：

java
@Service
public class PetOrderService {

    @Autowired
    private RedisDistributedLock redisLock;

    @Autowired
    private PetStockMapper petStockMapper;

    public boolean adoptPet(Long petId, Long userId) {
        String lockKey = "lock:pet:adopt:" + petId;
        String requestId = UUID.randomUUID().toString();
        // 尝试获取锁，超时时间5秒
        if (!redisLock.tryLock(lockKey, requestId, 5)) {
            return false; // 获取锁失败，说明有其他用户正在抢购
        }

        try {
            // 查询库存
            PetStock stock = petStockMapper.selectByPetId(petId);
            if (stock == null || stock.getRemaining() <= 0) {
                return false; // 无库存
            }

            // 扣减库存
            int affectedRows = petStockMapper.decreaseStock(petId, 1);
            if (affectedRows > 0) {
                // 创建订单、记录领养申请等...
                createOrder(petId, userId);
                return true;
            }
            return false;
        } finally {
            // 无论成功与否，释放锁
            redisLock.unlock(lockKey, requestId);
        }
    }
}
4.3 数据库层乐观锁兜底
虽然分布式锁已经防止了并发扣减，但为了进一步提高数据安全性，我们还可以在数据库层面使用乐观锁：
UPDATE pet_stock 
SET remaining = remaining - 1, version = version + 1 
WHERE pet_id = #{petId} AND remaining > 0 AND version = #{version}
这样即使 Redis 锁出现极少数异常，数据库乐观锁也能保证库存不被扣超。
5. 常见问题与优化
5.1 锁的过期时间如何设置
过期时间要大于业务执行的平均时间，但也不能过大，否则一旦业务异常导致锁未释放，其他线程会长时间等待。通常设置 5~10 秒，如果业务耗时不确定，可以使用 Redisson 的看门狗机制，它会自动为锁续期。
5.2 锁的粒度
我们这里使用 petId 作为锁 Key，粒度较细，不同宠物之间的抢购互不影响，并发度高。如果锁的粒度太大（如全局锁），会严重影响性能。
5.3 锁的重试策略
在高并发场景下，获取锁失败后如果直接返回失败，用户体验较差。可以适当让前端稍后重试，或在服务端实现有限重试（如循环尝试 3 次，每次间隔 50ms）。但要注意，重试会增加等待时间，可能造成请求堆积。
5.4 Redis 单点故障
如果 Redis 宕机，所有分布式锁都会失效。生产环境建议使用 Redis 主从或集群，并考虑使用 Redisson 的 RedLock 算法（但 RedLock 有争议，简单场景主从+哨兵已足够）。
6. 扩展思考
6.1 分布式锁的替代方案
除了 Redis，还可以使用 ZooKeeper 或 etcd 实现分布式锁，它们提供更强的可靠性和顺序保证，但性能略低于 Redis。对于抢购场景，Redis 通常更合适。
6.2 无锁方案
对于库存扣减，也可以完全依赖数据库乐观锁，结合 update ... where remaining > 0 的原子性。但数据库行锁在高并发下性能可能成为瓶颈，且需要处理重试逻辑。分布式锁+数据库乐观锁的组合是比较稳妥的方案。
7. 总结
通过 Redis 分布式锁，我们成功解决了宠物抢购场景下的超卖问题。关键点在于：
使用 SET NX EX 原子加锁，避免死锁
使用 Lua 脚本原子释放锁，避免误删
锁的粒度要精细（按业务资源）
结合数据库乐观锁，形成双重保障
分布式锁不是银弹，它引入了额外的依赖和复杂性。但在需要严格互斥的高并发场景下，它仍然是可靠的选择。通过这个实践，我深入理解了分布式锁的原理和实现细节，也体会到了在设计高并发系统时，需要在性能、可靠性、复杂度之间做出权衡。
