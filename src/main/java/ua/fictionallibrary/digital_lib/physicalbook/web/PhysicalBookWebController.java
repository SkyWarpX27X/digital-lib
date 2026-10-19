package ua.fictionallibrary.digital_lib.physicalbook.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.fictionallibrary.digital_lib.exception.IllegalResourceTypeException;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBookService;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookResponse;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/physical-books")
public class PhysicalBookWebController {

    private final PhysicalBookService service;

    public PhysicalBookWebController(PhysicalBookService service) {
        this.service = service;
    }

    @GetMapping
    public String listBooks(Model model) {
        List<PhysicalBookResponse> books = service.getAllPhysicalBooks();
        model.addAttribute("books", books);
        return "physical-books/list";
    }

    @GetMapping("/new")
    public String showCreateForm (Model model) {
        model.addAttribute("form", new AddPhysicalBookForm("", "", "", "", null, "", ""));
        return "physical-books/add";
    }

    @PostMapping
    public String handleCreateForm (@Valid @ModelAttribute("form") AddPhysicalBookForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "physical-books/add";
        }
        List<String> authors = Arrays.stream(form.authors().split(",")).map(String::trim).filter(a -> !a.isEmpty()).toList();
        try {
            service.addPhysicalBook(new PhysicalBookRequest(form.name(), authors, form.description(), form.topic(), form.publishingYear(), form.language(), form.resourceType()));
        } catch (IllegalResourceTypeException e) {
            bindingResult.rejectValue("resourceType", "resourceType.invalid", e.getMessage());
            return "physical-books/add";
        }
        redirectAttributes.addFlashAttribute("message", "Книгу успішно додано");
        return "redirect:/physical-books";
    }
}