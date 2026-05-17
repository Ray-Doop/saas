package com.skynet.product.workshop.controller;

import com.skynet.common.core.result.PageResult;
import com.skynet.common.core.result.Result;
import com.skynet.product.workshop.dto.ProductSPUDTO;
import com.skynet.product.workshop.qo.ProductSPUQO;
import com.skynet.product.workshop.service.ProductService;
import com.skynet.product.workshop.vo.ProductSPUVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/spu")
    public Result<ProductSPUVO> create(@Valid @RequestBody ProductSPUDTO dto) {
        return Result.ok(productService.create(dto));
    }

    @PutMapping("/spu/{id}")
    public Result<ProductSPUVO> update(@PathVariable Long id, @Valid @RequestBody ProductSPUDTO dto) {
        return Result.ok(productService.update(id, dto));
    }

    @GetMapping("/spu/{id}")
    public Result<ProductSPUVO> getById(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }

    @GetMapping("/spu/page")
    public Result<PageResult<ProductSPUVO>> page(ProductSPUQO qo) {
        return Result.ok(productService.page(qo));
    }

    @DeleteMapping("/spu/{id}")
    public Result<?> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }

    @PostMapping("/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.ok(productService.uploadImage(file));
    }
}
