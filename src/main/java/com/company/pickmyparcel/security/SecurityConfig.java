package com.company.pickmyparcel.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/v2/api-docs",
                        "/configuration/ui",
                        "/swagger-resources",
                        "/configuration/security",
                        "/swagger-ui.html",
                        "/webjars/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js")
                .permitAll()
                .antMatchers("/user/login/**")// this is permitting all request
                .permitAll()
                .antMatchers("/user/signup/**")// this is permitting all request
                .permitAll()
                .antMatchers("/user/sendRegisterOTP/**")// this is permitting all request
                .permitAll()
                .antMatchers("/user/sendPasswordResetOTP/**")// this is permitting all request
                .permitAll()
                .antMatchers("/user/verifyPasswordResetOTP/**")// this is permitting all request
                .permitAll()
                .antMatchers("/user/submitNewPassword/**")// this is permitting all request
                .permitAll()
                .antMatchers("/contactUs/sendEnquiry/**")// this is permitting all request
                .permitAll()
                .antMatchers("/priceMaintenance/**") // this is open for temporary, later add role as a ADMIN
                .permitAll()
                .antMatchers("/futureTrip/addTrip/**")
                .permitAll()
                .antMatchers("/futureTrip/getFutureTrips/**")
                .permitAll()
                .antMatchers("/futureTrip/getFutureTripById/**")
                .permitAll()
                .antMatchers("/futureTrip/getFutureTripByPinCodes/**")
                .permitAll()
                .antMatchers("/futureTrip/searchFutureDeals/**")
                .permitAll()
                .antMatchers("/internalDashboard/getDealsAndPaymentInfoByContactNumber/**")
                .permitAll()
                .antMatchers("/internalDashboard/byDate/**")
                .permitAll()
                .antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**")
                .permitAll()
                .antMatchers("/payment/**")
                .permitAll()
                .antMatchers("/phonePay/**")
                .permitAll()
                .antMatchers("/invoice/**")
                .permitAll()
                .antMatchers("/paytm/**")
                .permitAll()
                .and()
                .authorizeRequests()
                .anyRequest()
                .authenticated();

        // Add our custom JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    }

}
