package com.example.gestion_creche.controller;

import com.example.gestion_creche.model.Child;
import com.example.gestion_creche.model.User;
import com.example.gestion_creche.service.ChildService;
import com.example.gestion_creche.service.UserService;
import com.example.gestion_creche.service.ModificationRequestService;
import com.example.gestion_creche.service.AgeValidationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final ChildService childService;
    private final UserService userService;
    private final ModificationRequestService modificationRequestService;
    private final AgeValidationService ageValidationService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public AdminController(ChildService childService, 
                         UserService userService,
                         ModificationRequestService modificationRequestService,
                         AgeValidationService ageValidationService) {
        this.childService = childService;
        this.userService = userService;
        this.modificationRequestService = modificationRequestService;
        this.ageValidationService = ageValidationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Boolean showArchived, Model model) {
        List<Child> children;
        if (Boolean.TRUE.equals(showArchived)) {
            children = childService.getArchivedChildren();
        } else {
            children = childService.getActiveChildren();
        }

        // Calculate ages for all children
        Map<Long, String> childAges = new HashMap<>();
        Map<Long, String> formattedBirthDates = new HashMap<>();
        
        for (Child child : children) {
            try {
                childAges.put(child.getId(), calculateAgeString(child.getBirthDate()));
                formattedBirthDates.put(child.getId(), formatBirthDate(child.getBirthDate()));
            } catch (Exception e) {
                // Handle any potential errors in age calculation
                childAges.put(child.getId(), "Age not available");
                formattedBirthDates.put(child.getId(), "Date not available");
            }
        }

        model.addAttribute("children", children);
        model.addAttribute("childAges", childAges);
        model.addAttribute("formattedBirthDates", formattedBirthDates);
        model.addAttribute("showArchived", Boolean.TRUE.equals(showArchived));
        model.addAttribute("totalChildren", childService.getTotalChildrenCount());
        model.addAttribute("activeChildren", childService.getActiveChildrenCount());
        model.addAttribute("archivedChildren", childService.getArchivedChildrenCount());

        return "admin/dashboard";
    }

    @GetMapping("/view-child/{id}")
    public String viewChild(@PathVariable Long id, Model model) {
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/archive-child/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String archiveChild(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Child child = childService.getChildById(id);
            if (child == null) {
                throw new RuntimeException("Child not found");
            }
            
            if (!child.isActive()) {
                throw new RuntimeException("Child is already archived");
            }
            
            childService.archiveChild(id);
            redirectAttributes.addFlashAttribute("success", 
                "Child " + child.getFirstName() + " " + child.getLastName() + " has been archived successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to archive child: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/restore-child/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String restoreChild(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Child child = childService.getChildById(id);
            if (child == null) {
                throw new RuntimeException("Child not found");
            }
            
            if (child.isActive()) {
                throw new RuntimeException("Child is already active");
            }
            
            childService.restoreChild(id);
            redirectAttributes.addFlashAttribute("success", 
                "Child " + child.getFirstName() + " " + child.getLastName() + " has been restored successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to restore child: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?showArchived=true";
    }

    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable Long id) {
        modificationRequestService.approveRequest(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable Long id) {
        modificationRequestService.rejectRequest(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/edit-child/{id}")
    public String showEditChildForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Child child = childService.getChildById(id);
            if (child == null) {
                throw new RuntimeException("Child not found");
            }
            
            List<User> parents = userService.getAllParents();
            model.addAttribute("child", child);
            model.addAttribute("parents", parents);
            return "admin/edit-child";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Child not found");
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/edit-child/{id}")
    public String updateChild(@PathVariable Long id,
                            @ModelAttribute Child updatedChild,
                            @RequestParam Long parentId,
                            RedirectAttributes redirectAttributes) {
        try {
            Child existingChild = childService.getChildById(id);
            if (existingChild == null) {
                throw new RuntimeException("Child not found");
            }

            // Validate age
            if (!ageValidationService.isAgeValid(updatedChild.getBirthDate())) {
                redirectAttributes.addFlashAttribute("error", ageValidationService.getValidationMessage());
                return "redirect:/admin/edit-child/" + id;
            }

            // Set the parent
            User parent = userService.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));
            updatedChild.setParent(parent);

            // Update the child
            childService.updateChild(id, updatedChild);
            redirectAttributes.addFlashAttribute("success", 
                "Child " + updatedChild.getFirstName() + " " + updatedChild.getLastName() + " has been updated successfully");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update child: " + e.getMessage());
            return "redirect:/admin/edit-child/" + id;
        }
    }

    // Utility method to calculate age string including years and months
    private String calculateAgeString(LocalDate birthDate) {
        if (birthDate == null) {
            return "Unknown";
        }

        try {
            LocalDate now = LocalDate.now();
            Period period = Period.between(birthDate, now);
            
            int years = period.getYears();
            int months = period.getMonths();
            
            if (years > 0) {
                if (months > 0) {
                    return String.format("%d year%s, %d month%s", 
                        years, years != 1 ? "s" : "",
                        months, months != 1 ? "s" : "");
                } else {
                    return String.format("%d year%s", years, years != 1 ? "s" : "");
                }
            } else {
                return String.format("%d month%s", months, months != 1 ? "s" : "");
            }
        } catch (Exception e) {
            return "Age calculation error";
        }
    }

    // Utility method to format birth date
    private String formatBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            return "Not specified";
        }
        try {
            return birthDate.format(dateFormatter);
        } catch (Exception e) {
            return "Invalid date";
        }
    }
}