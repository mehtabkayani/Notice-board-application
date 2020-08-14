package com.example.noticesession.Controllers;

import com.example.noticesession.Models.Comment;
import com.example.noticesession.Models.Notice;
import com.example.noticesession.Models.User;
import com.example.noticesession.Repositories.CommentRepository;
import com.example.noticesession.Repositories.NoticeRepository;
import com.example.noticesession.Repositories.UserRepository;
import com.example.noticesession.Utils.SessionKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private CommentRepository commentRepository;

    private User userSession = new User();

    @GetMapping("/")
    public String index(Model model, HttpSession session,HttpServletResponse response) throws IOException {

        List<User> users = userRepository.findAll();
        List<Notice> notices = noticeRepository.findAll();
        List<Comment> comments = commentRepository.findAll();

        Collections.reverse(notices);

        model.addAttribute("loginStatus", SessionKeeper.getInstance().CheckSession(session.getId()));
        model.addAttribute("userSession", userSession);
        model.addAttribute("users", users);
        model.addAttribute("notices", notices);
        model.addAttribute("comments", comments);

        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session,HttpServletResponse response, RedirectAttributes redirectAttributes) throws IOException {
        model.addAttribute("User", new User());
        if (checkUserSession(session.getId())) {
            response.sendRedirect("/");
        }
        return "login";
    }

    @PostMapping("/saveSession")
    public String login(@ModelAttribute("User") User user, HttpServletRequest request, HttpServletResponse response, HttpSession session,
                        Model model, RedirectAttributes redirectAttributes) throws IOException {
        List<User> users = userRepository.findAll();
        for (User u : users) {
            if (u.getPassword().equals(user.getPassword()) && u.getName().equalsIgnoreCase(user.getName())) {
                SessionKeeper.getInstance().AddSession(session.getId());

                session.setMaxInactiveInterval(10*60);
                userSession = u;
                System.out.println("password correct");
                return "redirect:/";
            }
        }

        System.out.println("login failed");

        return "login";
    }

    ////ADD NOTICE

    @GetMapping("/addNotice/{id}")
    public String add(@ModelAttribute Notice notice, @PathVariable Integer id, Model model,HttpSession session,HttpServletResponse response) throws IOException {
        if (!checkUserSession(session.getId())) {
            response.sendRedirect("/");
        }
        User user = userRepository.getOne(id);
        model.addAttribute("user", user);

        return "addNotice";
    }


    @PostMapping("/addNotice/{id}")
    public String add(@PathVariable Integer id, Notice notice, RedirectAttributes redirectAttributes) {

        User userNotice = userRepository.getOne(id);

        notice.setUser(userNotice);

        userNotice.add(notice);

        noticeRepository.save(notice);
        userRepository.save(userNotice);

        return "redirect:/";
    }

    @GetMapping("/addComment/{id}")
    public String add(@ModelAttribute Comment comment, @PathVariable Integer id, Model model, HttpSession session,HttpServletResponse response) throws IOException {
        if (!checkUserSession(session.getId())) {
            response.sendRedirect("/");
        }
        Notice notice = noticeRepository.getOne(id);
        comment.setNotice(notice);
        comment.setUser(userSession);
        model.addAttribute("commentObj", comment);
        model.addAttribute("user", userSession);

        return "addComment";
    }


    @PostMapping("/addComment")
    public String add(@ModelAttribute("commentObj") Comment comment1, RedirectAttributes redirectAttributes) {

        commentRepository.save(comment1);

        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id,HttpSession session,HttpServletResponse response) throws IOException {

        Notice notice = noticeRepository.getOne(id);
        User user = userRepository.getOne(notice.getUser().getUser_id());


        noticeRepository.deleteById(id);

        return "redirect:/";

    }

    @GetMapping("/edit/{id}")
    public String editNotice(@PathVariable("id") Integer id, Model model,HttpSession session,HttpServletResponse response) {

        Notice notice = noticeRepository.getOne(id);
        model.addAttribute("notice", notice);

        return "edit";
    }

    @PostMapping("/edit")
    public String editNotice(Notice notice1, RedirectAttributes redirectAttributes) {

        User userNotice = userRepository.getOne(userSession.getUser_id());
        System.out.println("Notice1 ID " + notice1.getNotice_id());
        System.out.println("User ID " + userNotice.getUser_id());

        Notice notice = noticeRepository.getOne(notice1.getNotice_id());

        notice.setNotice_id(notice1.getNotice_id());
        notice.setText(notice1.getText());
        notice.setUser(userNotice);

        noticeRepository.save(notice);

        return "redirect:/";
    }




    @GetMapping("/logout")
    public String logout(Model model,HttpSession session) {
        model.addAttribute("User", new User());
        session.invalidate();
        return "login";
    }

    private boolean checkUserSession(String sessionId) {
        return SessionKeeper.getInstance().CheckSession(sessionId);
    }


}
