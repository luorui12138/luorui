package com.ks.eshop.eshop_basic_sever.Service;

import com.ks.eshop.eshop_basic_sever.Repository.ProductCategoryRepository;
import com.ks.eshop.eshop_common.MongoFetch.ReactiveService;
import com.ks.eshop.eshop_model.Models.ProductCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductCategoryService extends ReactiveService<ProductCategory, ProductCategoryRepository> {
    private final StoreService storeService;
    private final CompanyService companyService;

    @Autowired
    public ProductCategoryService(StoreService storeService, CompanyService companyService) {
        this.storeService = storeService;
        this.companyService = companyService;
    }

    @Override
    public Flux<ProductCategory> insert(Flux<ProductCategory> flux) {
        return flux.flatMap(this::check);
    }

    private Mono<ProductCategory> check(ProductCategory productCategory) {
        return Mono.justOrEmpty(productCategory.getCompanyId())
                .flatMap(companyService::isExist)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new RuntimeException("无法找到对应公司")))
                .then(this.insert(productCategory))
                .flatMap(this::saveCompanyProductCategory);
    }

    private Mono<ProductCategory> saveCompanyProductCategory(ProductCategory productCategory) {
        return companyService.findById(productCategory.getCompanyId())
                .doOnNext(company -> company.setCertainProductCategories(productCategory))
                .flatMap(companyService::update)
                .thenReturn(productCategory);
    }

    public Mono<ProductCategory> saveProductCategoryIcon(Flux<FilePart> filePartFlux, String id){
        return this.findById(id)
                .flatMap(productCategory->this.setProductCategoryIcon(productCategory,filePartFlux))
                .flatMap(this::update);
    }

    private Mono<ProductCategory> setProductCategoryIcon(ProductCategory productCategory, Flux<FilePart> filePartFlux){
        return filePartFlux
                .compose(f-> saveImage(f,productCategory.getId()))
                .doOnNext(productCategory::setImage)
                .then(Mono.justOrEmpty(productCategory));
    }

    public Mono<ProductCategory> saveChildrenProductCategory(ProductCategory productCategory){
        return Mono.justOrEmpty(productCategory.getParentCategoryId())
                .flatMap(this::isExist)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("无法找到相应父级分类")))
                .then(this.insert(productCategory))
                .flatMap(this::setChildrenProductCategory);
    }

    private Mono<ProductCategory> setChildrenProductCategory(ProductCategory childrenProductCategory){
        return Mono.justOrEmpty(childrenProductCategory.getParentCategoryId())
                .flatMap(this::findById)
                .doOnNext(parent -> parent.setChildrenCategories(childrenProductCategory))
                .flatMap(this::update)
                .thenReturn(childrenProductCategory);
    }

    @Override
    public Flux<ProductCategory> delete(Flux<ProductCategory> flux) {
        return flux
                .map(ProductCategory::getId)
                .flatMap(this::findById)
                .doOnNext(category -> category.setIsDelete(true))
                .flatMap(this::update);
    }
}
