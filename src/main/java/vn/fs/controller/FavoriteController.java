package vn.fs.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;
import vn.fs.commom.CommomDataService;
import vn.fs.entities.Favorite;
import vn.fs.entities.Product;
import vn.fs.entities.User;
import vn.fs.repository.FavoriteRepository;
import vn.fs.repository.ProductRepository;


@Controller
public class FavoriteController extends CommomController {

	@Autowired
	FavoriteRepository favoriteRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	CommomDataService commomDataService;

	@GetMapping(value = "/favorite")
	public String favorite(Model model, User user) {
		List<Favorite> favorites = favoriteRepository.selectAllSaves(user.getUserId());
		commomDataService.commonData(model, user);
		model.addAttribute("favorites", favorites);
		return "web/favorite";
	}

//	@GetMapping(value = "/doFavorite")
//	public String doFavorite(Model model, Favorite favorite, User user, @RequestParam("id") Long id) {
//		Product product = productRepository.findById(id).orElse(null);
//		favorite.setProduct(product);
//		favorite.setUser(user);
//		product.setFavorite(true);
//		favoriteRepository.save(favorite);
//		commomDataService.commonData(model, user);
//		return "redirect:/products";
//	}
// Thêm sản phẩm vào yêu thích
@GetMapping("/doFavorite")
public String doFavorite(Model model, User user, @RequestParam("id") Long productId,
                         @RequestHeader(value = "Referer", required = false) String referer) {
    if (user == null) {
        return "redirect:/login";
    }

    boolean exists = favoriteRepository.existsByProduct_ProductIdAndUser_UserId(productId, user.getUserId());
    if (!exists) {
        productRepository.findById(productId).ifPresent(product -> {
            Favorite favorite = new Favorite();
            favorite.setProduct(product);
            favorite.setUser(user);
            favoriteRepository.save(favorite);
        });
    }

    return "redirect:" + (referer != null ? referer : "/products");
}


    @GetMapping("/doUnFavorite")
    public String doUnFavorite(Model model, User user, @RequestParam("id") Long id,
                               @RequestHeader(value = "Referer", required = false) String referer) {
        Favorite favorite = favoriteRepository.selectSaves(id, user.getUserId());
        productRepository.findById(id).ifPresent(product -> product.setFavorite(false));
        if (favorite != null) {
            favoriteRepository.delete(favorite);
        }
        commomDataService.commonData(model, user);
        return "redirect:" + (referer != null ? referer : "/products");
    }

    @GetMapping("/toggleFavorite")
    @ResponseBody
    public Map<String, Object> toggleFavorite(@RequestParam("id") Long productId, Principal principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("status", "unauthorized");
            return response;
        }

        User currentUser = userRepository.findByEmail(principal.getName());

        boolean exists = favoriteRepository.existsByUser_UserIdAndProduct_ProductId(
                currentUser.getUserId(), productId
        );

        if (exists) {
            Favorite favorite = favoriteRepository.findByUser_UserIdAndProduct_ProductId(
                    currentUser.getUserId(), productId
            );
            favoriteRepository.delete(favorite);
            response.put("favorite", false);
        } else {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                Favorite newFav = new Favorite();
                newFav.setUser(currentUser);
                newFav.setProduct(product);
                favoriteRepository.save(newFav);
            }
            response.put("favorite", true);
        }

        response.put("status", "success");
        return response;
    }


}
