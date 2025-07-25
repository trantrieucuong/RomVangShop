package vn.fs.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import vn.fs.entities.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

public class SuccessHandler implements AuthenticationSuccessHandler {

	private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) throws IOException, ServletException {

		// === Lưu user vào session ===
		Object principal = authentication.getPrincipal();
		if (principal instanceof User) {
			User user = (User) principal;
			HttpSession session = request.getSession();
			session.setAttribute("userId", user.getUserId());
			session.setAttribute("currentUser", user);

			// ✅ In ra console
			System.out.println(">>>>> Đăng nhập thành công. userId trong session: " + user.getUserId());
		}

		// === Lấy URL trước khi login (nếu có) ===
		HttpSession session = request.getSession(false);
		String redirectUrl = null;
		if (session != null) {
			redirectUrl = (String) session.getAttribute("redirectUrl");
			session.removeAttribute("redirectUrl");
		}

		if (redirectUrl != null && !redirectUrl.isEmpty()) {
			redirectStrategy.sendRedirect(request, response, redirectUrl);
			return;
		}

		// === Điều hướng theo vai trò ===
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		for (GrantedAuthority authority : authorities) {
			String role = authority.getAuthority();
			switch (role) {
				case "ROLE_USER":
					redirectStrategy.sendRedirect(request, response, "/checkout");
					return;
				case "ROLE_ADMIN":
				case "ROLE_EMPLOYEE":
					redirectStrategy.sendRedirect(request, response, "/admin/home");
					return;
				case "ROLE_SALE":
					redirectStrategy.sendRedirect(request, response, "/sale/saleHome");
					return;
			}
		}

		throw new IllegalStateException("Không xác định được vai trò người dùng.");
	}

}
