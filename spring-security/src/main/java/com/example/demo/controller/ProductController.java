package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

// 접근 권한은 SecurityConfig에서 검사하고, Controller는 상품 CRUD만 처리한다.
@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;

    @GetMapping("/products")
    public String list(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "product-list";
    }

    @GetMapping("/products/new")
    public String addForm(@ModelAttribute("product") Product product) {
        return "product-add";
    }

    @PostMapping("/products")
    public String add(@ModelAttribute("product") Product product) {
        product.setId(null);
        productRepository.save(product);
        return "redirect:/products";
    }

    @GetMapping("/products/edit")
    public String editForm(@RequestParam("id") Long id, Model model) {
        model.addAttribute("product", findProduct(id));
        return "product-edit";
    }

    @PostMapping("/products/edit")
    public String edit(@ModelAttribute("product") Product product) {
        Product found = findProduct(product.getId());
        found.setName(product.getName());
        found.setPrice(product.getPrice());
        productRepository.save(found);
        return "redirect:/products";
    }

    @GetMapping("/products/delete")
    public String delete(@RequestParam("id") Long id) {
        // 실습에서는 삭제 링크의 GET 요청으로 상품을 삭제한다.
        productRepository.delete(findProduct(id));
        return "redirect:/products";
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }
}
