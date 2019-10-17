package com.ks.eshop.eshop_basic_sever.Service;

import com.ks.eshop.eshop_basic_sever.Repository.ProductRepository;
import com.ks.eshop.eshop_common.MongoFetch.ReactiveService;
import com.ks.eshop.eshop_model.Models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class ProductService extends ReactiveService<Product, ProductRepository> {
    private final ProductCategoryService productCategoryService;
    private final ProductSKUService productSKUService;
    private final BrandService brandService;

    @Autowired
    public ProductService(ProductCategoryService productCategoryService, ProductSKUService productSKUService, BrandService brandService) {
        this.productCategoryService = productCategoryService;
        this.productSKUService = productSKUService;
        this.brandService = brandService;
    }

    @Override
    public Flux<Product> insert(Flux<Product> flux) {
        return flux.flatMap(this::saveProduct);
    }

    private Mono<Product> saveProduct(Product product) {
        return Mono.justOrEmpty(product.getProductCategoryId())
                .flatMap(productCategoryService::isExist)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("无法找到相应产品分类")))
                .thenReturn(product.getBrandId())
                .flatMap(brandService::isExist)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("无法找到相应品牌")))
                .thenReturn(product)
                .doOnNext(Product::initProduct)
                .flatMap(this::saveProductSKU)
                .flatMap(this::saveProductCategory)
                .flatMap(this::saveProductBrand);

    }

    private Mono<Product> saveProductCategory(Product product) {
        return Mono.justOrEmpty(product.getProductCategoryId())
                .flatMap(productCategoryService::findById)
                .doOnNext(c -> c.setCertainProducts(product))
                .flatMap(productCategoryService::update)
                .thenReturn(product);
    }

    private Mono<Product> saveProductBrand(Product product) {
        return Mono.justOrEmpty(product.getBrandId())
                .flatMap(brandService::findById)
                .doOnNext(b -> b.setProducts(product))
                .flatMap(brandService::update)
                .thenReturn(product);
    }

    private Mono<Product> saveProductSKU(Product product) {
        return Flux.fromIterable(product.getProductSKU())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("请设置规格")))
                .doOnNext(productSKUService::check)
                .flatMap(productSKUService::insert)
                .collect(Collectors.toList())
                .doOnNext(product::setCertainProductSKU)
                .thenReturn(product)
                .flatMap(this::insert);
    }

    @Override
    public Flux<Product> delete(Flux<Product> flux) {
        return flux
                .map(Product::getId)
                .flatMap(this::findById)
                .doOnNext(product -> product.setIsDelete(true))
                .flatMap(this::update);
    }

    public Flux<Product> findByBrandId(String brandId) {
        return Source()
                .findByBrandId(brandId)
                .filter(Product::getIsDelete);
    }

    public Mono<Product> publish(String productId) {
        return this.findById(productId)
                .doOnNext(product -> product.setProduct_status(Product.PRODUCT_STATUS.PUBLISH))
                .flatMap(this::update)
                .switchIfEmpty(Mono.error(new RuntimeException("商品不存在")));
    }

    public Mono<Product> unPublish(String productId) {
        return this.findById(productId)
                .doOnNext(product -> product.setProduct_status(Product.PRODUCT_STATUS.UNPUBLISH))
                .flatMap(this::update)
                .switchIfEmpty(Mono.error(new RuntimeException("商品不存在")));
    }
}
