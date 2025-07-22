package vn.fs.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

public class SuccessHandler implements AuthenticationSuccessHandler {
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Override
	public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
										Authentication authentication) throws IOException, ServletException {
		boolean hasRoleUser = false;
		boolean hasAdmin = false;
		boolean hasEmployee = false;
		boolean hasSale = false;
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		for (GrantedAuthority grantedAuthority : authorities) {
			if (grantedAuthority.getAuthority().equals("ROLE_USER")) {
				hasRoleUser = true;
				break;
			} else if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
				hasAdmin = true;
				break;
			}
			else if (grantedAuthority.getAuthority().equals("ROLE_EMPLOYEE")) {
				hasEmployee = true;
				break;
			}
			else if (grantedAuthority.getAuthority().equals("ROLE_SALE")) {
				hasSale = true;
				break;
			}
		}
		if (hasRoleUser) {
			redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, "/checkout");
		} else if (hasAdmin || hasEmployee) {
			redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, "/admin/home");
		}else if (hasSale ) {
			redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, "/sale/saleHome");
		}
		else {
			throw new IllegalStateException();
		}
	}

}
