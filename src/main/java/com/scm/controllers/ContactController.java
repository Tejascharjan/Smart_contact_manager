package com.scm.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.forms.ContactForm;
import com.scm.forms.ContactSearchForm;
import com.scm.helper.AppCostants;
import com.scm.helper.Helper;
import com.scm.helper.Message;
import com.scm.helper.MessageType;
import com.scm.services.ContactService;
import com.scm.services.ImageService;
import com.scm.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user/contacts")
public class ContactController {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @RequestMapping("/add")
    // add contact page : handler
    public String addContactView(Model model) {
        ContactForm contactForm = new ContactForm();

        contactForm.setFavorite(true);
        model.addAttribute("contactForm", contactForm);
        return "user/add_contact";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String saveContact(@Valid @ModelAttribute ContactForm contactForm, BindingResult result,
            Authentication authentication, HttpSession session) {

        // process the form data

        // validate form
        if (result.hasErrors()) {

            result.getAllErrors().forEach(error -> logger.info(error.toString()));
            session.setAttribute("message", Message.builder()
                    .content("Please correct the following errors")
                    .type(MessageType.red)
                    .build());
            return "user/add_contact";
        }

        String userName = Helper.getEmailOfLoggedInUser(authentication);

        User user = userService.getUserByEmail(userName);

        // process the contact picture

        Contact contact = new Contact();
        contact.setName(contactForm.getName());
        contact.setFavorite(contactForm.isFavorite());
        contact.setEmail(contactForm.getEmail());
        contact.setPhoneNumber(contactForm.getPhoneNumber());
        contact.setAddress(contactForm.getAddress());
        contact.setDescription(contactForm.getDescription());
        contact.setUser(user);
        contact.setLinkedInLink(contactForm.getLinkedInLink());
        contact.setWebsiteLink(contactForm.getWebsiteLink());

        if (contactForm.getContactImage() != null && !contactForm.getContactImage().isEmpty()) {
            String filename = UUID.randomUUID().toString();
            // image processing
            String fileURL = imageService.uploadImage(contactForm.getContactImage(), filename);
            contact.setPicture(fileURL);
            contact.setCloudinaryImagePublicId(filename);
        }

        // process the form data
        contactService.save(contact);

        // set the contact picture url

        // set the message on the view
        session.setAttribute("message", Message.builder()
                .content("You have successfully added a new contact")
                .type(MessageType.green)
                .build());

        return "redirect:/user/contacts/add";
    }

    // view contacts

    @RequestMapping
    public String viewContacts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = AppCostants.PAGE_SIZE + "") int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction, Model model,
            Authentication authentication) {

        // load all the user contacts
        String username = Helper.getEmailOfLoggedInUser(authentication);

        User user = userService.getUserByEmail(username);

        Page<Contact> pageContact = contactService.getByUser(user, page, size, sortBy, direction);

        model.addAttribute("pageContact", pageContact);
        model.addAttribute("pageSize", AppCostants.PAGE_SIZE);
        model.addAttribute("contactSearchForm", new ContactSearchForm());

        return "user/contacts";
    }

    // search handler

    @RequestMapping("/search")
    public String searchHandler(
            @ModelAttribute ContactSearchForm contactSearchForm,
            @RequestParam(value = "size", defaultValue = AppCostants.PAGE_SIZE + "") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction, Model model,
            Authentication authentication) {

        User user = userService.getUserByEmail(Helper.getEmailOfLoggedInUser(authentication));

        Page<Contact> pageContact = null;
        if (contactSearchForm.getField().equalsIgnoreCase("name")) {
            pageContact = contactService.searchByName(contactSearchForm.getValue(), size, page, sortBy, direction,
                    user);
        } else if (contactSearchForm.getField().equalsIgnoreCase("email")) {
            pageContact = contactService.searchByEmail(contactSearchForm.getValue(), size, page, sortBy, direction,
                    user);
        } else if (contactSearchForm.getField().equalsIgnoreCase("phone")) {
            pageContact = contactService.searchByPhone(contactSearchForm.getValue(), size, page, sortBy, direction,
                    user);
        }

        model.addAttribute("contactSearchFomr", contactSearchForm);

        model.addAttribute("pageContact", pageContact);

        model.addAttribute("pageSize", AppCostants.PAGE_SIZE);

        return "user/search";
    }

    // delete contact

    @RequestMapping("/delete/{contactId}")
    public String deleteContact(@PathVariable("contactId") String contactId, HttpSession session) {
        contactService.delete(contactId);
        session.setAttribute("message",
                Message.builder()
                        .content("Contact Deleted Successfully !!")
                        .type(MessageType.green)
                        .build());

        return "redirect:/user/contacts";
    }

    // update contact form view

    @RequestMapping("/view/{contactId}")
    public String updateContactFormView(@PathVariable String contactId, Model model) {
        var contact = contactService.getById(contactId);
        ContactForm contactForm = new ContactForm();
        contactForm.setName(contact.getName());
        contactForm.setEmail(contact.getEmail());
        contactForm.setPhoneNumber(contact.getPhoneNumber());
        contactForm.setAddress(contact.getAddress());
        contactForm.setDescription(contact.getDescription());
        contactForm.setFavorite(contact.isFavorite());
        contactForm.setLinkedInLink(contact.getLinkedInLink());
        contactForm.setWebsiteLink(contact.getWebsiteLink());
        contactForm.setPicture(contact.getPicture());

        model.addAttribute("contactForm", contactForm);
        model.addAttribute("contactId", contactId);
        return "user/update_contact_view";
    }

    @RequestMapping(value = "/update/{contactId}", method = RequestMethod.POST)
    public String updateContact(@PathVariable String contactId, @Valid @ModelAttribute ContactForm contactForm,
            BindingResult bindingResult, Model model) {

        // update the contact
        if (bindingResult.hasErrors()) {
            return "user/update_contact_view";
        }

        var cont = contactService.getById(contactId);

        cont.setId(contactId);
        cont.setName(contactForm.getName());
        cont.setEmail(contactForm.getEmail());
        cont.setPhoneNumber(contactForm.getPhoneNumber());
        cont.setAddress(contactForm.getAddress());
        cont.setDescription(contactForm.getDescription());
        cont.setFavorite(contactForm.isFavorite());
        cont.setLinkedInLink(contactForm.getLinkedInLink());
        cont.setWebsiteLink(contactForm.getWebsiteLink());

        // process image
        if (contactForm.getContactImage() != null && !contactForm.getContactImage().isEmpty()) {
            System.out.println("file is not empty");
            String filename = UUID.randomUUID().toString();
            String imageUrl = imageService.uploadImage(contactForm.getContactImage(), filename);
            cont.setCloudinaryImagePublicId(filename);
            cont.setPicture(imageUrl);
            contactForm.setPicture(imageUrl);
        } else {
            System.out.println("file is empty");
        }

        var updatedCon = contactService.update(cont);

        model.addAttribute("message", Message.builder().content("Contact Updated !!").type(MessageType.green).build());
        return "redirect:/user/contacts/view/" + contactId;
    }
}
