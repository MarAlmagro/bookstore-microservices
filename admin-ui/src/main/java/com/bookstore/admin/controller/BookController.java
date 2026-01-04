package com.bookstore.admin.controller;

import com.bookstore.admin.client.CatalogClient;
import com.bookstore.admin.dto.BookDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final CatalogClient catalogClient;

    @GetMapping
    public String listBooks(
            @RequestParam(value = "query", required = false) String query,
            Model model) {
        try {
            List<BookDTO> books;
            if (query != null && !query.isEmpty()) {
                books = catalogClient.searchBooks(query);
            } else {
                books = catalogClient.getAllBooks();
            }
            model.addAttribute("books", books);
            model.addAttribute("query", query);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load books: " + e.getMessage());
        }
        return "books/list";
    }

    @GetMapping("/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new BookDTO());
        return "books/form";
    }

    @GetMapping("/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        try {
            BookDTO book = catalogClient.getBookById(id);
            model.addAttribute("book", book);
        } catch (Exception e) {
            model.addAttribute("error", "books.error.notFound");
            return "redirect:/admin/books";
        }
        return "books/form";
    }

    @PostMapping
    public String createBook(@ModelAttribute BookDTO book, RedirectAttributes redirectAttributes) {
        try {
            catalogClient.createBook(book);
            redirectAttributes.addFlashAttribute("success", "books.success.created");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create book: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/{id}")
    public String updateBook(@PathVariable Long id, @ModelAttribute BookDTO book, RedirectAttributes redirectAttributes) {
        try {
            catalogClient.updateBook(id, book);
            redirectAttributes.addFlashAttribute("success", "books.success.updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update book: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            catalogClient.deleteBook(id);
            redirectAttributes.addFlashAttribute("success", "books.success.deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete book: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }
}
