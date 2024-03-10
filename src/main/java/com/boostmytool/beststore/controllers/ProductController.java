package com.boostmytool.beststore.controllers;

import com.boostmytool.beststore.models.Product;
import com.boostmytool.beststore.models.ProductDto;
import com.boostmytool.beststore.services.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Binding;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    ProductRepository repo;
    @GetMapping({"", "/"})
    public String showProductList(Model model){
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }
    @GetMapping( "/create")
    public String showCreatePage(Model model){
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/createProduct";
    }
    @PostMapping( "/create")
    public String showCreatePage(
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result){
        if(productDto.getImageFile().isEmpty()){
            result.addError(new FieldError("productDto","imageFile","The imageFile is required"));
        }
        if(result.hasErrors()){
            return "products/createProduct";
        }
        //save imgfile
        MultipartFile image = productDto.getImageFile();
        Date createAt = new Date();
        String storageFileName = createAt.getTime()+ "_" + image.getOriginalFilename();

        try{
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);
            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }
            try(InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
        }
        Product product =new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreateAt(createAt);
        product.setImageFileName(storageFileName);
        repo.save(product);

        return "redirect:/products";
    }
    @GetMapping("/edit")
    public String showEditPage(
            Model model,
            @RequestParam int id
    ){
        try{
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());

            model.addAttribute("productDto", productDto);
        }
        catch (Exception e){
            System.out.println("Exception: "+e.getMessage());
            return  "redirect:/products";
        }
        return "products/editProduct";
    }
    @PostMapping("/edit")
    public String showEditPage(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result
    ){
        try{
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);
            if(result.hasErrors()){
                return "products/editProduct";
            }
            if(!productDto.getImageFile().isEmpty()){
                String uploadDir= "public/images/";
                Path oldImagePath = Paths.get(uploadDir+product.getImageFileName());
                try{
                    Files.delete(oldImagePath);
                }catch (Exception e){
                    System.out.println("ExceptionL"+e.getMessage());
                }
                //save new image file
                MultipartFile image = productDto.getImageFile();
                Date createAt = new Date();
                String storageFileName = createAt.getTime()+ "_" + image.getOriginalFilename();

                try(InputStream inputStream = image.getInputStream()){
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFileName);
            }
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());

            repo.save(product);
        }
        catch (Exception e){
            System.out.println("Exception: "+e.getMessage());
        }
        return "redirect:/products";
    }
    @GetMapping("/delete")
    public String deleteProduct(
            Model model,
            @RequestParam int id
    ){
        try{
            Product product = repo.findById(id).get();

            //delete product image
            Path imagePath = Paths.get("public/images/"+ product.getImageFileName());

            try{
                Files.delete(imagePath);
            }
            catch (Exception e){
                System.out.println("Exception:"+ e.getMessage());
            }

            //delete the product
            repo.delete(product);
        }
        catch (Exception e){
            System.out.println("Exception: "+e.getMessage());
        }
        return "redirect:/products";
    }
}
