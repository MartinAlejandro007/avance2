package ec.intercampus.intercampus.web;

import ec.intercampus.intercampus.domain.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@ControllerAdvice
public class WebAdvice {
    @ModelAttribute("section")
    public String section(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/") ? "dashboard" : path.split("/")[1];
    }
    @ExceptionHandler(DomainException.class)
    public ModelAndView domain(DomainException error) {
        HttpStatus status = switch (error.getCode()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case DUPLICATE, IN_USE -> HttpStatus.CONFLICT;
        };
        return error(status, error.getMessage());
    }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ModelAndView invalid(Exception error) {
        return error(HttpStatus.BAD_REQUEST, "Revisa los campos: nombres, coordenadas, fechas e identificadores deben ser válidos.");
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView conflict(DataIntegrityViolationException error) {
        return error(HttpStatus.CONFLICT, "No se pudo guardar el cambio: comprueba duplicados y registros relacionados.");
    }
    private ModelAndView error(HttpStatus status, String message) {
        var view = new ModelAndView("error"); view.setStatus(status);
        view.addObject("errorCode", status.value()); view.addObject("message", message); return view;
    }
}
