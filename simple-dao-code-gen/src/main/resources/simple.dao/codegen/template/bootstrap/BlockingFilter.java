package ${modulePackageName};

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.*;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *
 * 支持的必要的时候，阻断新的请求
 * @author Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。
 * 
 */
@Slf4j
public class BlockingFilter extends OncePerRequestFilter {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    private boolean isBlocked = false;

    @Autowired
    Environment environment;
    private int nohupOutFileMaxSize = 5;

    @Override
    public void afterPropertiesSet() throws ServletException {

        log.info("阻断过滤器已经启用，可以在本机执行[curl 127.0.0.1/local/console/stop]阻断新的请求");

        nohupOutFileMaxSize = environment.getProperty("nohupOutFileMaxSize", Integer.class, 5);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        File nohupOutFile = new File("./nohup.out");
        //如果日志文件大于5G
        if (nohupOutFile.exists() && nohupOutFileMaxSize > 0 && nohupOutFile.length() > nohupOutFileMaxSize * 1_000_000_000L) {
            FileUtil.writeUtf8String("---日志文件超过5G，自动清除---\n", nohupOutFile);
        }

        final String serverName = request.getServerName();

        final boolean isLocalhost = serverName.equals("127.0.0.1") || serverName.equals("localhost");

        //如果是本机
        if (isLocalhost) {

            final String path = request.getServletPath();

            boolean isConsoleCmd = false;

            if ("/local/console/stop".equals(path)) {
                isBlocked = true;
                isConsoleCmd = true;
            } else if ("/local/console/start".equals(path)) {
                isBlocked = false;
                isConsoleCmd = true;
            }

            //如果是控制台命令
            if (isConsoleCmd) {
                response.setStatus(HttpStatus.OK.value());
                response.getWriter().write("online:" + atomicInteger.get());
                response.getWriter().flush();
                return;
            }
        }

        if (isBlocked) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.getWriter().write("server is blocked!");
            response.getWriter().flush();
        } else {
            atomicInteger.getAndIncrement();
            try {
                filterChain.doFilter(request, response);
            } finally {
                atomicInteger.getAndDecrement();
            }
        }

    }

}
