package com.trainning.ordersystem.specification;

import com.trainning.ordersystem.entity.Product;
import com.trainning.ordersystem.entity.enums.ProductStatus;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProductSpecification {

    private final ProductRepository productRepository;

    public void validateSkuNotExists(String sku) {
        if (productRepository.existsBySku(sku)) {
            throw new AppException(
                    ErrorCode.SKU_ALREADY_EXISTS,
                    "Mã SKU '" + sku + "' đã tồn tại"
            );
        }
    }

    public void checkProductActive(Long productId){
        Product product =  productRepository.findById(productId).orElseThrow(
                () -> new AppException(ErrorCode.PRODUCT_NOT_FOUND, "Không tìm thấy sản phẩm với id = " + productId)
        );
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new AppException(ErrorCode.PRODUCT_INACTIVE);
        }
    }

    public void validateSkuNotExistsForUpdate(Long productId, String newSku) {
        productRepository.findBySku(newSku).ifPresent(existingProduct -> {
            if (!existingProduct.getId().equals(productId)) {
                throw new AppException(
                        ErrorCode.SKU_ALREADY_EXISTS,
                        "Mã SKU '" + newSku + "' đã được sử dụng bởi sản phẩm khác"
                );
            }
        });
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "Không tìm thấy sản phẩm với id = " + productId
                ));
    }

    public void validateStatus(ProductStatus status) {
        if (status == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Trạng thái sản phẩm không được để trống"
            );
        }
    }

    public boolean isAvailable(Product product, int quantity) {
        if (product == null || product.getStatus() != ProductStatus.ACTIVE) {
            return false;
        }
        return product.getStockQuantity() != null && product.getStockQuantity() >= quantity;
    }

    public void validateAvailability(Product product, int quantity) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new AppException(
                    ErrorCode.PRODUCT_INACTIVE,
                    "Sản phẩm '" + product.getName() + "' hiện đang ngừng kinh doanh"
            );
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
            throw new AppException(
                    ErrorCode.INSUFFICIENT_STOCK,
                    String.format("Sản phẩm '%s' không đủ số lượng tồn kho (Yêu cầu: %d, Hiện có: %d)",
                            product.getName(), quantity, product.getStockQuantity() == null ? 0 : product.getStockQuantity())
            );
        }
    }
}
