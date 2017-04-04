package ph.gov.naga.controller.internal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
    
    @RequestMapping("/")
    public ModelAndView index (ModelMap model) {
        //model is unused
        return new ModelAndView("redirect:/index.html", model);
    }

}
