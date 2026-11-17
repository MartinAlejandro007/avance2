package ec.intercampus.intercampus.web;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profiles")
public class ProfileController {
    private final UserService users;
    private final FavoriteService favorites;
    private final RouteService routes;
    public ProfileController(UserService users, FavoriteService favorites, RouteService routes) {
        this.users = users; this.favorites = favorites; this.routes = routes;
    }
    @GetMapping
    public String list(@RequestParam(required = false) Long edit, Model model) {
        model.addAttribute("users", users.findAll()); model.addAttribute("types", UserType.values());
        if (edit != null) model.addAttribute("editing", users.findById(edit));
        return "profiles";
    }
    @GetMapping("/{id}")
    public String profile(@PathVariable Long id, Model model) {
        var profile = favorites.profile(id);
        model.addAttribute("profile", profile); model.addAttribute("routes", routes.findAll());
        model.addAttribute("favoriteRouteIds", profile.favoriteRoutes().stream().map(r -> r.id()).toList());
        model.addAttribute("favoriteStopIds", profile.favoriteStops().stream().map(s -> s.id()).toList());
        return "profile";
    }
    @PostMapping
    public String create(@RequestParam String name, @RequestParam String email, @RequestParam UserType type, RedirectAttributes flash) {
        var user = users.create(name, email, type); flash.addFlashAttribute("success", "Perfil creado"); return "redirect:/profiles/" + user.id();
    }
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam String name, @RequestParam String email,
                         @RequestParam UserType type, RedirectAttributes flash) {
        users.update(id, name, email, type); flash.addFlashAttribute("success", "Perfil actualizado"); return "redirect:/profiles";
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        users.delete(id); flash.addFlashAttribute("success", "Perfil eliminado"); return "redirect:/profiles";
    }
    @PostMapping("/{id}/routes/{routeId}")
    public String route(@PathVariable Long id, @PathVariable Long routeId, @RequestParam boolean favorite) {
        favorites.favoriteRoute(id, routeId, favorite); return "redirect:/profiles/" + id;
    }
    @PostMapping("/{id}/stops/{stopId}")
    public String stop(@PathVariable Long id, @PathVariable Long stopId, @RequestParam boolean favorite) {
        favorites.favoriteStop(id, stopId, favorite); return "redirect:/profiles/" + id;
    }
    @PostMapping("/{id}/theme")
    public String theme(@PathVariable Long id, @RequestParam VisualTheme theme, RedirectAttributes flash) {
        favorites.changeTheme(id, theme); flash.addFlashAttribute("success", "Preferencia visual guardada"); return "redirect:/profiles/" + id;
    }
}
