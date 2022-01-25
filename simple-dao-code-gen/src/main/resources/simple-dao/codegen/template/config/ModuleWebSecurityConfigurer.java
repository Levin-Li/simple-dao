package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.*;
import org.springframework.security.web.firewall.*;

//参考文章： https://blog.csdn.net/u012702547/article/details/106800446/

//默认不开启
//@Configuration(PLUGIN_PREFIX + "ModuleWebSecurityConfigurer")
//@Order(${moduleNameHashCode})
@Slf4j
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//@EnableGlobalAuthentication
@ConditionalOnClass({WebSecurityConfigurer.class})
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "${className}", matchIfMissing = true)
public class ModuleWebSecurityConfigurer implements WebSecurityConfigurer<WebSecurity>  {


//    @Bean
//    @ConditionalOnMissingBean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http
//                .antMatcher("/**")
//                .authorizeRequests(authorize -> authorize
//                        .anyRequest().authenticated()
//                )
//                .build();
//    }

    @Bean
    @ConditionalOnMissingBean
    HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedPeriod(true);
        return firewall;
    }

//    @Override
    protected void configure(HttpSecurity http) throws Exception {

        log.debug("config HttpSecurity");

    }

    @Override
    public void init(WebSecurity webSecurity) throws Exception {

    }

    @Override
    public void configure(WebSecurity web) throws Exception {

        web.ignoring().antMatchers(
                "/error/**",
                "/*/api-docs",
                "/swagger-ui/**/*",
                "/springfox-swagger-ui/**/*",
                "/swagger-resources/**",
                ADMIN_UI_PATH + "**",
                H5_UI_PATH + "**",
                API_PATH + "auth/**",
                API_PATH + "weixin/**"
        );

        log.debug("config WebSecurity");

    }
}