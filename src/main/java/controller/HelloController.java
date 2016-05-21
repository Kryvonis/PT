package controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Kryvonis on 4/17/16.
 */

@Controller
public class HelloController {

    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public String login() {
        return "hello";
    }

    @RequestMapping(value = "/lab", method = RequestMethod.GET)
    public String lab() {
        return "output";
    }

    @RequestMapping(value = "/tree", method = RequestMethod.GET)
    public String tree() {
        return "tree";
    }

    @RequestMapping(value = "treep", method = RequestMethod.GET)
    public String getTree() {
        return "tree";
    }
}
