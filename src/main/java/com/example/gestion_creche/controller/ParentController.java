package com.example.gestion_creche.controller;
import com.example.gestion_creche.model.Child;
import com.example.gestion_creche.model.ModificationRequest;
import com.example.gestion_creche.model.User;
import com.example.gestion_creche.service.ChildService;
import com.example.gestion_creche.service.ModificationRequestService;
import com.example.gestion_creche.service.UserService;
import com.example.gestion_creche.service.AgeValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

@Controller
@RequestMapping("/parent")
public class ParentController {
    private final ChildService childService;
    private final UserService userService;
    private final ModificationRequestService modificationRequestService;
    private final ObjectMapper objectMapper;
    private final AgeValidationService ageValidationService;

    public ParentController(ChildService childService, UserService userService, 
                          ModificationRequestService modificationRequestService,
                          ObjectMapper objectMapper,
                          AgeValidationService ageValidationService) {
        this.childService = childService;
        this.userService = userService;
        this.modificationRequestService = modificationRequestService;
        this.objectMapper = objectMapper;
        this.ageValidationService = ageValidationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User parent = userService.findByEmail(userDetails.getUsername());
        List<Child> children = childService.getChildrenByParent(parent);
        model.addAttribute("children", children);
        return "parent/dashboard";
    }

    @GetMapping("/add-child")
    public String showAddChildForm(Model model) {
        model.addAttribute("child", new Child());
        return "parent/add-child";
    }

    @PostMapping("/add-child")
    public String addChild(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam String birthDate,
                          @RequestParam(required = false) String allergies,
                          @RequestParam String address,
                          @RequestParam String phoneNumber,
                          @RequestParam(required = false) String additionalInfo,
                          RedirectAttributes redirectAttributes) {
        try {
            LocalDate parsedBirthDate = LocalDate.parse(birthDate);
            
            // Validate age
            if (!ageValidationService.isAgeValid(parsedBirthDate)) {
                redirectAttributes.addFlashAttribute("error", ageValidationService.getValidationMessage());
                return "redirect:/parent/add-child";
            }
            
            User parent = userService.findByEmail(userDetails.getUsername());
            
            Child child = new Child();
            child.setFirstName(firstName);
            child.setLastName(lastName);
            child.setBirthDate(parsedBirthDate);
            child.setAllergies(allergies);
            child.setAddress(address);
            child.setPhoneNumber(phoneNumber);
            child.setAdditionalInfo(additionalInfo);
            child.setParent(parent);
            child.setActive(true);

            childService.saveChild(child);
            redirectAttributes.addFlashAttribute("success", "Child added successfully!");
            return "redirect:/parent/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add child. Please try again.");
            return "redirect:/parent/add-child";
        }
    }

    @PostMapping("/edit-child/{id}")
    public String submitModificationRequest(@PathVariable Long id, 
                                         @ModelAttribute Child updatedChild,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         RedirectAttributes redirectAttributes) {
        try {
            // Validate age
            if (!ageValidationService.isAgeValid(updatedChild.getBirthDate())) {
                redirectAttributes.addFlashAttribute("error", ageValidationService.getValidationMessage());
                return "redirect:/parent/edit-child/" + id;
            }

            User currentUser = userService.findByEmail(userDetails.getUsername());
            Child existingChild = childService.getChildById(id);
            
            // Security check - ensure the child belongs to the current parent
            if (!existingChild.getParent().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to edit this child");
            }
            
            // Check if there's already a pending request
            if (modificationRequestService.hasActiveRequest(existingChild)) {
                redirectAttributes.addFlashAttribute("error", "There is already a pending modification request for this child");
                return "redirect:/parent/edit-child/" + id;
            }
            
            // Create modification request
            ModificationRequest request = new ModificationRequest();
            request.setChild(existingChild);
            request.setParent(currentUser);
            request.setProposedChanges(objectMapper.writeValueAsString(updatedChild));
            
            modificationRequestService.createRequest(request);
            
            redirectAttributes.addFlashAttribute("success", 
                "Your modification request has been submitted and is pending admin approval");
            return "redirect:/parent/dashboard";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit modification request: " + e.getMessage());
            return "redirect:/parent/edit-child/" + id;
        }
    }

    @GetMapping("/view-child/{id}")
    public String viewChild(@PathVariable Long id, 
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            User parent = userService.findByEmail(userDetails.getUsername());
            Child child = childService.getChildById(id);
            
            // Security check - ensure the child belongs to the current parent
            if (!child.getParent().getId().equals(parent.getId())) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to view this child's information");
                return "redirect:/parent/dashboard";
            }
            
            model.addAttribute("child", child);
            return "parent/view-child";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Child not found");
            return "redirect:/parent/dashboard";
        }
    }

    @GetMapping("/edit-child/{id}")
    public String editChild(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            User parent = userService.findByEmail(userDetails.getUsername());
            Child child = childService.getChildById(id);
            
            // Security check - ensure the child belongs to the current parent
            if (!child.getParent().getId().equals(parent.getId())) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this child's information");
                return "redirect:/parent/dashboard";
            }
            
            // Check if there's already a pending request
            if (modificationRequestService.hasActiveRequest(child)) {
                redirectAttributes.addFlashAttribute("warning", 
                    "There is already a pending modification request for this child");
            }
            
            model.addAttribute("child", child);
            return "parent/edit-child";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Child not found");
        return "redirect:/parent/dashboard";
        }
    }
}

