package ${modulePackageName};

import com.levin.commons.dao.SimpleDao;
import com.levin.commons.plugin.PluginManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executor;

<#list entityClassList as clazz>
import ${clazz.name};
</#list>

<#list serviceClassList as className>
import ${className};
</#list>

<#list controllerClassList as className>
import ${className};
</#list>


/**
 *  数据初始化
 *  @author Auto gen by simple-dao-codegen ${now}
 */
@Component
@Slf4j
public class DataInitializer implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    SimpleDao dao;

    @Autowired
    PluginManager pluginManager;

    @Autowired
    Executor executor;

    @Autowired
    ServerProperties serverProperties;


    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        log.info("on applicationContext ...");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (event.getApplicationContext() == applicationContext) {
            initData();
        }

    }

    void initData() {

        log.info("on init ...");

        Random random = new Random(this.hashCode());

        if (dao.selectFrom(User.class).count() > 0) {
            return;
        }


        //先删除旧数据
        dao.deleteFrom(Task.class)
                .disableSafeMode()
                .delete();

        dao.deleteFrom(User.class)
                .disableSafeMode()
                .delete();

        dao.deleteFrom(Group.class)
                .disableSafeMode()
                .delete();

        int gCount = 15;

        String[] states = {"正常", "已取消", "审请中", "已删除", "已冻结"};

        String[] types = {"虚拟组织", "部门", "小组", "协会"};

        String[] categories = {"临时", "常设", "月度", "年度"};

        String[] areas = {"福州", "厦门", "深圳", "上海"};


        Object one = dao.selectFrom(Group.class).select("max(id)").findOne();

        long n = (one == null) ? 1 : (long) one;

        Long parentId = null;


        while (gCount-- > 0) {

            //  n++;

            Group group = new Group("Group-" + n++, parentId);

//            group.setId((long) n);

            group.setState(states[Math.abs(random.nextInt()) % states.length]);
            group.setCategory(categories[Math.abs(random.nextInt()) % categories.length]);
            group.setType(types[Math.abs(random.nextInt()) % categories.length]);

            dao.create(group);

            long uCount = 3 * gCount;


            while (uCount-- > 0) {

                User user = new User();
                user.setName("User-" + group.getId() + "-" + uCount);

//                  user.setId((long) uCount);

                user.setState(states[Math.abs(random.nextInt()) % states.length]);
                user.setScore(Math.abs(random.nextInt(100)));
                user.setGroup(group)
                        .setArea(areas[Math.abs(random.nextInt()) % areas.length]);
                dao.create(user);


                long taskCount = 3 * uCount;

                //创建任务

                while (taskCount-- > 0) {

                    Task task = new Task();
                    task.setName("Task-" + taskCount);

                    dao.create(task
                            .setScore(random.nextInt(100))
                            .setUser(user)
                            .setState(states[Math.abs(random.nextInt()) % states.length])
                            .setArea(areas[Math.abs(random.nextInt()) % areas.length])
                    );
                }
            }

            if (parentId == null || (gCount % 5) == 0) {
                parentId = group.getId();
            }

        }


        log.info("***** 示例数据初始化完成 ******");


        Integer port = Optional.ofNullable(serverProperties.getPort()).orElse(8080);

        log.info("***** 插件首页： http://127.0.0.1:" + port + "/" + ModuleOption.ADMIN_PATH + "index.html");
        log.info("***** 查询组织： http://127.0.0.1:" + port + "/" + ModuleOption.API_PATH + "group/query");
        log.info("***** 查询用户&总数： http://127.0.0.1:" + port + "/" + ModuleOption.API_PATH + "user/query?pageSize=100&pageIndex=1&requireTotals=true");
        log.info("***** 查询任务： http://127.0.0.1:" + port + "/" + ModuleOption.API_PATH + "task/query");


        log.info("***** 查询插件： http://127.0.0.1:" + port + "/system/plugin/list");

    }

}
