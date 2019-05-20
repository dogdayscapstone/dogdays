package com.codeup.dogdays.controllers;

import com.codeup.dogdays.models.Comment;
import com.codeup.dogdays.models.Dog;
import com.codeup.dogdays.models.Event;

import com.codeup.dogdays.repositories.CommentRepository;

import com.codeup.dogdays.models.User;
import com.codeup.dogdays.repositories.EventRepository;
import com.codeup.dogdays.repositories.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.expression.Lists;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;


@Controller
public class EventController {

    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final CommentRepository commentRepo;
    private CommentController CC;




    public EventController(EventRepository eventRepo, UserRepository userRepo, CommentRepository commentRepo, CommentController CC) {
        this.eventRepo = eventRepo;
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
        this.CC = CC;
    }

    public List<Dog> dogsByUser(List<Dog> dogs, User user){
        List<Dog> filteredDogs = new ArrayList<>();

        for(int i = 0; i < dogs.size(); i++){
            if(dogs.get(i).getDogs().getId() == user.getId()){
                filteredDogs.add(dogs.get(i));
            }
        }

        return filteredDogs;
    }

    @GetMapping("/events")
    public String allEvents (Model model){
        model.addAttribute("events", eventRepo.findAll());

        return "events/events";
    }


    @GetMapping("/events/create")
    public String showPostForm (Model model, HttpServletRequest request){

        if(request.getSession().getAttribute("user") == null){
            return "redirect:/login";
        }

        model.addAttribute("event", new Event());
        return "events/create";
    }

    @PostMapping("/events/create")
    public String createPost (@ModelAttribute Event event,HttpServletRequest request) {

        User user = (User)request.getSession().getAttribute("user");
        User dbUser = userRepo.findOne(user.getId());
        event.setUser(dbUser);

        eventRepo.save(event);
        return "redirect:/events";

    }


    @GetMapping("/events/{id}")
    public String getOneBook (Model model, @PathVariable Long id, HttpServletRequest request){
        Event event = eventRepo.findOne(id);

        User user = event.getUser();
        request.getSession().setAttribute("user", user);

        model.addAttribute("event", event);
        model.addAttribute("commentA", new Comment());

       model.addAttribute("countAttending", event.getDogAttendees().size());
       model.addAttribute("location", event.getLocation());

        model.addAttribute("comments", CC.commentsByEvent((List<Comment>)commentRepo.findAll(), eventRepo.findById(id)));
        return "events/show";
    }


    @GetMapping("/events/{id}/edit")
    public String editForm ( @PathVariable int id, Model model){


        Event event = eventRepo.findOne((long) id);
        model.addAttribute("event", event);
        return "events/edit";

    }

    @PostMapping("/events/{id}/edit")
    public String editPost (@ModelAttribute Event event,HttpServletRequest request){
        User user = (User)request.getSession().getAttribute("user");
        User dbUser = userRepo.findOne(user.getId());
        event.setUser(dbUser);
        eventRepo.save(event);
        return "redirect:/events";
    }


    @GetMapping("/events/{id}/delete")
    public String deletePost (@PathVariable Long id, Model model){
        Event event = eventRepo.findOne(id);
        eventRepo.delete(event);
        return "redirect:/events";
    }


    @GetMapping("/search")
    public String search(@RequestParam String  q, Model viewModel) {
        viewModel.addAttribute("searchedContent", eventRepo.search("%" + q + "%"));
        viewModel.addAttribute("searchTerm", q);

        return "events/search";
    }


    @PostMapping("/events/{id}/attend")
    public String attendEvent (HttpServletRequest request, @PathVariable Long id,  @ModelAttribute Event event){
        User user = (User)request.getSession().getAttribute("user");

        List<Dog> dogs = user.getDogs();

        for(int i = 0; i < dogs.size(); i++){

            Dog dog = user.getDogs().get(i);
            event.setDogAttendees(event.getDogAttendees(), dog);
            dog.setEvents(dog.getEvents(), event);

        }
        return "redirect:/events/" + id;
    }



}