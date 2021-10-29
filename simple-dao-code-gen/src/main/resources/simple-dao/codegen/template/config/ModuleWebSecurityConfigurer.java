package $

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

{modulePackageName}.config;
        {modulePackageName}.ModuleOption.*;
        {modulePackageName}.*;

//参考文章： https://blog.csdn.net/u012702547/article/details/106800446/

//默认不开启
//@Configuration(PLUGIN_PREFIX + "ModuleWebSecurityConfigurer")
//@Order(${moduleNameHashCode})
@Slf4j
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//@EnableGlobalAuthentication
@ConditionalOnProperty(value = PLUGIN_PREFIX + "ModuleWebSecurityConfigurer", havingValue = "false", matchIfMissing = true)
public class ModuleWebSecurityConfigurer implements WebSecurityConfigurer<WebSecurity> {


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