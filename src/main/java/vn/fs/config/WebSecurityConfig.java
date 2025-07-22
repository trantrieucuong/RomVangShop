package vn.fs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import vn.fs.service.UserDetailService;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	private UserDetailService userDetailService;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
		auth.setUserDetailsService(userDetailService);
		auth.setPasswordEncoder(passwordEncoder());
		return auth;
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

		http.authorizeRequests()
				// ⚠️ Quyền cụ thể nên đặt trước quyền tổng quát
				.antMatchers("/admin/users").hasRole("ADMIN")

				// Role SALE chỉ được vào /sale/**
				.antMatchers("/sale/**").hasRole("SALE")

				// ADMIN và EMPLOYEE được vào /admin/**
				.antMatchers("/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")

				// ROLE_USER được vào checkout
				.antMatchers("/checkout").hasRole("USER")

				// Các request còn lại cho phép tất cả (nên đặt cuối cùng)
				.antMatchers("/**").permitAll()

				// Các request chưa khớp thì yêu cầu phải login
				.anyRequest().authenticated()

				.and()
				.formLogin()
				.loginProcessingUrl("/doLogin")
				.loginPage("/login")
				.defaultSuccessUrl("/?login_success")
				.successHandler(new SuccessHandler()) // xử lý custom nếu có
				.failureUrl("/login?error=true")
				.permitAll()
				.and()
				.logout()
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.logoutSuccessUrl("/?logout_success")
				.permitAll()
				.and()
				.rememberMe()
				.rememberMeParameter("remember")
				.and()
				.exceptionHandling()
				.accessDeniedPage("/web/notFound");
	}

}