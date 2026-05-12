package com.smartcloud.product.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smartcloud.common.exception.BizException;
import com.smartcloud.common.result.ResultCode;
import com.smartcloud.product.dto.ProductSPUDTO;
import com.smartcloud.product.entity.ProductSKUPO;
import com.smartcloud.product.entity.ProductSPUPO;
import com.smartcloud.product.entity.QProductSPUPO;
import com.smartcloud.product.entity.QProductSKUPO;
import com.smartcloud.product.qo.ProductSPUQO;
import com.smartcloud.product.repository.ProductSKURepository;
import com.smartcloud.product.repository.ProductSPURepository;
import com.smartcloud.product.vo.ProductSKUVO;
import com.smartcloud.product.vo.ProductSPUVO;
import com.smartcloud.common.result.PageResult;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductSPURepository spuRepository;
    private final ProductSKURepository skuRepository;
    private final JPAQueryFactory queryFactory;
    private final MinioClient minioClient;
    private static final String BUCKET = "products";
    private static final String MINIO_ENDPOINT = "http://127.0.0.1:9000";

    @Transactional
    public ProductSPUVO create(ProductSPUDTO dto) {
        if (spuRepository.existsBySpuCode(dto.getSpuCode())) {
            throw new BizException("SPU编码已存在");
        }

        ProductSPUPO spu = new ProductSPUPO();
        BeanUtils.copyProperties(dto, spu);
        if (dto.getImageList() != null) {
            spu.setImageList(String.join(",", dto.getImageList()));
        }
        spu.setStatus(1);
        spuRepository.save(spu);

        if (dto.getSkuList() != null) {
            for (ProductSPUDTO.SKUItem item : dto.getSkuList()) {
                ProductSKUPO sku = new ProductSKUPO();
                sku.setSpuId(spu.getId());
                sku.setSkuCode(item.getSkuCode());
                sku.setSkuName(item.getSkuName());
                sku.setSpecs(item.getSpecs());
                sku.setPrice(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                sku.setCostPrice(item.getCostPrice());
                sku.setStock(item.getStock() != null ? item.getStock() : 0);
                sku.setImage(item.getImage());
                sku.setStatus(1);
                skuRepository.save(sku);
            }
        }

        return getById(spu.getId());
    }

    @Transactional
    public ProductSPUVO update(Long id, ProductSPUDTO dto) {
        ProductSPUPO spu = spuRepository.findById(id)
                .orElseThrow(() -> new BizException(ResultCode.PRODUCT_NOT_EXIST));

        BeanUtils.copyProperties(dto, spu, "id", "spuCode", "createTime", "updateTime", "isDeleted");
        if (dto.getImageList() != null) {
            spu.setImageList(String.join(",", dto.getImageList()));
        }
        spuRepository.save(spu);

        if (dto.getSkuList() != null) {
            skuRepository.deleteBySpuId(id);
            for (ProductSPUDTO.SKUItem item : dto.getSkuList()) {
                ProductSKUPO sku = new ProductSKUPO();
                sku.setSpuId(spu.getId());
                sku.setSkuCode(item.getSkuCode());
                sku.setSkuName(item.getSkuName());
                sku.setSpecs(item.getSpecs());
                sku.setPrice(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                sku.setCostPrice(item.getCostPrice());
                sku.setStock(item.getStock() != null ? item.getStock() : 0);
                sku.setImage(item.getImage());
                sku.setStatus(1);
                skuRepository.save(sku);
            }
        }

        return getById(id);
    }

    public ProductSPUVO getById(Long id) {
        ProductSPUPO spu = spuRepository.findById(id)
                .orElseThrow(() -> new BizException(ResultCode.PRODUCT_NOT_EXIST));
        return toVO(spu);
    }

    public PageResult<ProductSPUVO> page(ProductSPUQO qo) {
        QProductSPUPO q = QProductSPUPO.productSPUPO;
        var query = queryFactory.selectFrom(q).where(q.isDeleted.eq(0));

        if (qo.getSpuName() != null) {
            query.where(q.spuName.like("%" + qo.getSpuName() + "%"));
        }
        if (qo.getSpuCode() != null) {
            query.where(q.spuCode.like("%" + qo.getSpuCode() + "%"));
        }
        if (qo.getCategoryId() != null) {
            query.where(q.categoryId.eq(qo.getCategoryId()));
        }
        if (qo.getStatus() != null) {
            query.where(q.status.eq(qo.getStatus()));
        }

        long total = query.fetchCount();
        long offset = (long) (qo.getPage() - 1) * qo.getSize();
        List<ProductSPUPO> pos = query.offset(offset).limit(qo.getSize()).fetch();
        List<ProductSPUVO> vos = pos.stream().map(this::toVO).collect(Collectors.toList());

        return PageResult.of(total, qo.getPage(), qo.getSize(), vos);
    }

    @Transactional
    public void delete(Long id) {
        ProductSPUPO spu = spuRepository.findById(id)
                .orElseThrow(() -> new BizException(ResultCode.PRODUCT_NOT_EXIST));
        skuRepository.deleteBySpuId(id);
        spuRepository.delete(spu);
    }

    public String uploadImage(MultipartFile file) {
        try {
            String objectName = "product/" + IdUtil.fastSimpleUUID() + "_" + file.getOriginalFilename();
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(objectName)
                        .stream(is, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }
            return MINIO_ENDPOINT + "/" + BUCKET + "/" + objectName;
        } catch (Exception e) {
            log.error("上传图片失败", e);
            throw new BizException(ResultCode.FILE_UPLOAD_FAILED);
        }
    }

    private ProductSPUVO toVO(ProductSPUPO po) {
        ProductSPUVO vo = new ProductSPUVO();
        BeanUtils.copyProperties(po, vo);

        QProductSKUPO qSku = QProductSKUPO.productSKUPO;
        List<ProductSKUPO> skus = queryFactory.selectFrom(qSku)
                .where(qSku.spuId.eq(po.getId()).and(qSku.isDeleted.eq(0)))
                .fetch();

        List<ProductSKUVO> skuVOs = new ArrayList<>();
        for (ProductSKUPO sku : skus) {
            ProductSKUVO skuVO = new ProductSKUVO();
            BeanUtils.copyProperties(sku, skuVO);
            skuVOs.add(skuVO);
        }
        vo.setSkuList(skuVOs);
        return vo;
    }
}
