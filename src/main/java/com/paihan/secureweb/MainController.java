package com.paihan.secureweb;

import com.paihan.entities.WorkItem;
import com.paihan.services.DynamoDBService;
import com.paihan.services.ShareEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMethod;
import com.paihan.services.WriteExcel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class MainController {

    @Autowired
    DynamoDBService dbService;

    @Autowired
    ShareEvent sendMsg;

    @Autowired
    WriteExcel excel;

    @GetMapping("/")
    public String root() {
        return "items";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/add")
    public String designer() {
        return "add";
    }

    @GetMapping("/items")
    public String items() {
        return "items";
    }

    // Adds a new item to the DynamoDB database
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    String addItems(HttpServletRequest request, HttpServletResponse response) {

        // Get the logged-in user
        String name = getLoggedUser();

        String event = request.getParameter("event");
        String eventDate = request.getParameter("event_date");
        String description = request.getParameter("description");

        // Create a Work Item object to pass to the injectNewSubmission method
        WorkItem myWork = new WorkItem();
        myWork.setName(name);
        myWork.setEvent(event);
        myWork.setEventDate(eventDate);
        myWork.setDescription(description);


        dbService.setItem(myWork);
        return "Item added";
    }

    // Builds and emails a report with all items
    @RequestMapping(value = "/report", method = RequestMethod.POST)
    @ResponseBody
    String getReport(HttpServletRequest request, HttpServletResponse response) {

        String email = request.getParameter("email");
        System.out.println(email);
        List<WorkItem> theList = dbService.getListItems();
        java.io.InputStream is = excel.exportExcel(theList);
        System.out.println("Excel ok");

        try {
            sendMsg.sendReport(is, email);

        } catch (IOException e) {
            e.getStackTrace();
        }
        return "Report is created";
    }


    // Modifies the value of a work item
    @RequestMapping(value = "/changewi", method = RequestMethod.POST)
    @ResponseBody
    String changeWorkItem(HttpServletRequest request, HttpServletResponse response) {

        String id = request.getParameter("id");
        String event = request.getParameter("event");
        String eventDate = request.getParameter("event_date");
        String description = request.getParameter("description");
        dbService.UpdateItem(id, event, eventDate, description);
        return id;
    }

    // Retrieve items
    @RequestMapping(value = "/retrieve", method = RequestMethod.POST)
    @ResponseBody
    String retrieveItems(HttpServletRequest request, HttpServletResponse response) {

        // Pass back items from the DynamoDB table
        String xml = "";

        xml = dbService.getAllItems();

        return xml;
    }

    // Returns a work item to modify
    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    @ResponseBody
    String modifyWork(HttpServletRequest request, HttpServletResponse response) {

        String id = request.getParameter("id");
        String xmlRes = dbService.getItem(id);
        return xmlRes;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    String deleteWork(HttpServletRequest request, HttpServletResponse response) {

        String id = request.getParameter("id");
        String xmlRes = dbService.deleteItemWithId(id);
        return xmlRes;
    }


    private String getLoggedUser() {

        // Get the logged-in user
        org.springframework.security.core.userdetails.User user2 = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = user2.getUsername();
        return name;
    }
}