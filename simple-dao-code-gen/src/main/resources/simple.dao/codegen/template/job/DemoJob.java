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
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 分布式定时任务
 * 通过Redis锁支持分布式，允许在多个节点同时。
 *
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 */
@Service(PLUGIN_PREFIX + "DemoJob")
@Slf4j
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "DemoJob", matchIfMissing = true)
public class DemoJob
        extends AbstractDistributionJob<String> {

    @Autowired
    SimpleDao simpleDao;

    //@Resource(name = "xxTaskExecutor")
    @Resource(name = TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    TaskExecutor executor;

    /**
     * @return
     */
    @Override
    protected String getName() {
        return "Demo-定时任务";
    }

    /**
     * 定时任务入口
     * <p>
     * 在同一个JVM中，如果定时时间到，但上次的任务还在执行中，则会跳过本次的执行
     */
    @Scheduled(fixedRate = 2 * 60 * 1000, initialDelay = 45 * 1000)
    protected void doTask() {
        try {
            super.doTask(10 * 60 * 1000, false, 200);
        } catch (Exception e) {
            log.error("分布式定时任务[" + getClass().getSimpleName() + "]执行失败", e);
        }
    }

    /**
     * 整个定时任务的锁
     * <p>
     * 通常同一个定时任务，不允许并发。
     * <p>
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
     * <p>
     * 同一个数据ID如果有其它节点的定时任务在处理，本定时任务将跳过这条数据
     *
     * @param dataId
     * @return
     */
    @Override
    protected String getDataLockKey(String dataId) {
        //同一个数据ID如果有其它节点的定时任务在处理，本定时任务将跳过这条数据
        return getClass().getName() + dataId;
    }

    /**
     * 需要覆盖这个方法，返回任务执行器
     * 返回 null 则使用调度器的线程执行
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
    protected List<String> getBatchData(long startTime, int pageIndex, int pageSize) {
        //@todo 查询出要批处理的记录ID
        return simpleDao.selectFrom(TestEntity.class)
                .select(E_TestEntity.id)
                .gt(E_TestEntity.createTime, new Date())
                .page(pageIndex, pageSize)
                .find();
    }

    /**
     * 处理单条记录
     *
     * @param dataId
     * @return
     */
    @Override
    protected boolean processData(String dataId) {

        //@todo 调用服务，加载当条记录进行处理

        return true;
    }
}