package com.example.spring_crud.controllers;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import com.example.spring_crud.models.Product;
import com.example.spring_crud.models.ProductDto;
import com.example.spring_crud.services.ProductRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductRepository repo;

    private final String UPLOAD_DIR = "public/images/";

    // ================= LIST =================
    @GetMapping({ "", "/" })
    public String showProductList(Model model) {
        List<Product> products = repo.findAll();
        model.addAttribute("products", products);
        return "products/index";
    }

    // ================= CREATE =================
    @GetMapping("/create")
    public String showCreatePage(Model model) {
        model.addAttribute("productDto", new ProductDto());
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result) {

        if (productDto.getImageFile().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFile", "Image is required"));
        }

        if (result.hasErrors()) {
            return "products/CreateProduct";
        }

        String storageFileName = "";

        try {
            MultipartFile image = productDto.getImageFile();
            Date createdAt = new Date();

            storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream,
                        uploadPath.resolve(storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            Product product = new Product();
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());
            product.setCreatedAt(createdAt);
            product.setImageFileName(storageFileName);

            repo.save(product);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "redirect:/products";
    }

    // ================= EDIT =================
    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {

        Optional<Product> optionalProduct = repo.findById(id);

        if (optionalProduct.isEmpty()) {
            return "redirect:/products";
        }

        Product product = optionalProduct.get();
        model.addAttribute("product", product);

        ProductDto dto = new ProductDto();
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());

        model.addAttribute("productDto", dto);

        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result) {

        Optional<Product> optionalProduct = repo.findById(id);

        if (optionalProduct.isEmpty()) {
            return "redirect:/products";
        }

        Product product = optionalProduct.get();
        model.addAttribute("product", product);

        if (result.hasErrors()) {
            return "products/EditProduct";
        }

        try {
            // IMAGE UPDATE
            if (!productDto.getImageFile().isEmpty()) {

                // delete old image
                Path oldImagePath = Paths.get(UPLOAD_DIR + product.getImageFileName());
                Files.deleteIfExists(oldImagePath);

                MultipartFile image = productDto.getImageFile();
                String newFileName = new Date().getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream,
                            Paths.get(UPLOAD_DIR).resolve(newFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImageFileName(newFileName);
            }

            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());

            repo.save(product);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "redirect:/products";
    }

    // ================= DELETE =================
    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {

        Optional<Product> optionalProduct = repo.findById(id);

        if (optionalProduct.isEmpty()) {
            return "redirect:/products";
        }

        Product product = optionalProduct.get();

        try {
            Path imagePath = Paths.get(UPLOAD_DIR + product.getImageFileName());
            Files.deleteIfExists(imagePath);

            repo.delete(product);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "redirect:/products";
    }
}