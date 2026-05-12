package com.smartcloud.product.controller;

import com.smartcloud.common.result.PageResult;
import com.smartcloud.common.result.Result;
import com.smartcloud.product.dto.ProductSPUDTO;
import com.smartcloud.product.qo.ProductSPUQO;
import com.smartcloud.product.service.ProductService;
import com.smartcloud.product.vo.ProductSPUVO;
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
