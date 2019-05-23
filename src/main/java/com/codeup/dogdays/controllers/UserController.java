package com.codeup.dogdays.controllers;

import com.codeup.dogdays.models.User;
import com.codeup.dogdays.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
public class UserController {
    private final UserRepository userRepo;

    private PasswordEncoder passwordEncoder;


    public UserController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }



    @GetMapping("/register")
    public String showRegisterForm (Model viewModel){

        viewModel.addAttribute("user", new User());
        return "users/register";
    }



    @PostMapping("/register")
    public String saveUser (@ModelAttribute User user, Model vmodel){
        for(User registeredUser : userRepo.findAll()){
            if(user.getUsername().equals(registeredUser.getUsername()) || user.getEmail().equals(registeredUser.getEmail())){
                if((user.getUsername().equals(registeredUser.getUsername()))){
                    vmodel.addAttribute("invalidUserName", "Username already taken");
                    return "users/signup-form";
                } else if(user.getEmail().equals(registeredUser.getEmail())){
                    vmodel.addAttribute("duplicateEmail", "Email already in use");
                    return "users/signup-form";
                }
            }
        }

        String hash = passwordEncoder.encode(user.getPassword());
        user.setPassword(hash);
        user.setPicture("/images/person_default.png");
        userRepo.save(user);

        return "redirect:/login";
    }


            @PostMapping("/login")
            public String loginUser (HttpServletRequest request, @RequestParam("username") String username, @RequestParam("password") String password) {
//                User sessionUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                 User user = userRepo.findByUsername(username);

                if (password.equals(user.getPassword())) {
                    request.getSession().setAttribute("user", user);
                    return "redirect:/";
                } else {
                    return "redirect:/login";
                }
            }

            @GetMapping("/logout")
            public String logoutUser(HttpServletRequest request){
                request.getSession().removeAttribute("user");
                request.getSession().invalidate();
                return "redirect:/login";
            }

            @GetMapping("/profile")
            public String showProfilePage (HttpServletRequest request, Model vmodel) {
                User sessionUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if(sessionUser == null){
                    return "redirect:/login";
                }

                User currentUser = userRepo.findOne(sessionUser.getId());
                User currentUsername = userRepo.findByUsername(sessionUser.getUsername());
                System.out.println(currentUsername);
                vmodel.addAttribute("user", currentUser);

                return "users/profile";
            }


            @GetMapping("/users/{id}/edit")
            public String getEditUserForm ( @PathVariable int id){

                return "users/editUser";
            }

            @PostMapping("/users/{id}/edit")
            public String EditUser ( @PathVariable int id){

                return "users/editUser";
            }


        }

