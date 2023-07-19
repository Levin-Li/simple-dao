package ${modulePackageName}.job;

import static ${modulePackageName}.ModuleOption.*;

import com.levin.commons.dao.SimpleDao;
import com.levin.commons.dao.domain.support.E_TestEntity;
import com.levin.commons.dao.domain.support.TestEntity;
import com.levin.commons.service.support.AbstractDistributionJob;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 分布式定时任务
 * 通过Redis锁支持分布式，允许在多个节点同时。
 *
 *
 *
 */
@Service(PLUGIN_PREFIX + "DemoJob")
@Slf4j
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "DemoJob", matchIfMissing = true)
public class DemoJob
        extends AbstractDistributionJob<TestEntity> {

    @Autowired
    SimpleDao simpleDao;

    //    @Autowired
//    @Resource(name = "xxTaskExecutor")
    @Resource(name = TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    TaskExecutor executor;

    /**
     * 定时任务入口
     *
     * 在同一个JVM中，如果定时时间到，但上次的任务还在执行中，则会跳过本次的执行
     *
     *
     */
    //@Scheduled(fixedRate = 2 * 60 * 1000, initialDelay = 45 * 1000)
    protected void doTask() {
        try {
            // log.info("分布式定时任务[" + getClass().getSimpleName() + "] 开始刷新店铺token...");
            super.doTask(10 * 60 * 1000, false, 200);
        } catch (Exception e) {
            log.error("分布式定时任务[" + getClass().getSimpleName() + "]执行失败", e);
        }
    }

    /**
     * 整个定时任务的锁
     *
     * 通常同一个定时任务，不允许并发。
     *
     *
     *
     * 返回null 则表示不锁定
     *
     * @return
     */
    @Override
    protected String getJobLockKey() {
        return getClass().getName();
    }

    /**
     * 获取单条记录锁
     * 返回null 则表示不锁定
     *
     * @param testEntity
     * @return
     */
    @Override
    protected String getDataLockKey(TestEntity testEntity) {
        //同一个店铺，当店铺被锁定时，快速返回，不做等待
        return getClass().getName() + testEntity.getId();
    }

    /**
     * 需要覆盖这个方法，放回任务执行器
     *
     * @return
     */
    @Override
    protected Executor getExecutor() {
        return executor;
    }


    /**
     * 返回一个批次要处理的数据
     *
     * @param startTime
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @Override
    protected List<TestEntity> getBatchData(long startTime, int pageIndex, int pageSize) {

        return simpleDao.selectFrom(TestEntity.class)
                .gt(E_TestEntity.createTime, ((int) (startTime / 1000L)))
                .page(pageIndex, pageSize)
                .find();
    }

    /**
     * 处理单条记录
     *
     * @param testEntity
     * @return
     */
    @Override
    protected boolean processData(TestEntity testEntity) {
        return true;
    }

}
