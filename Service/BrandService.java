package com.ks.eshop.eshop_basic_sever.Service;

import com.ks.eshop.eshop_basic_sever.Repository.BrandRepository;
import com.ks.eshop.eshop_common.MongoFetch.ReactiveService;
import com.ks.eshop.eshop_model.Models.Advertisement;
import com.ks.eshop.eshop_model.Models.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BrandService extends ReactiveService<Brand, BrandRepository> {
    private final CompanyService companyService;
    @Autowired
    public BrandService(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Override
    public Flux<Brand> insert(Flux<Brand> flux) {
        return flux.flatMap(this::saveCompanyBrand);
    }
    private Mono<Brand> saveCompanyBrand(Brand brand){
        return Mono.justOrEmpty(brand.getCompanyId())
                .flatMap(companyService::isExist)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new RuntimeException("无法找到相应公司")))
                .then(super.insert(brand))
                .flatMap(this::setCompanyBrand);
    }
    private Mono<Brand> setCompanyBrand(Brand brand){
        return Mono.justOrEmpty(brand.getCompanyId())
                .flatMap(companyService::findById)
                .doOnNext(company -> company.setCertainBrand(brand))
                .flatMap(companyService::update)
                .thenReturn(brand);
    }

    public Mono<Brand> saveBrandLogo(Flux<FilePart> filePartFlux, String id){
        return Source()
                .findById(id)
                .flatMap(brand->this.setBrandLogo(brand,filePartFlux))
                .flatMap(this::update);
    }

    private Mono<Brand> setBrandLogo(Brand brand, Flux<FilePart> filePartFlux){
        return filePartFlux
                .compose(f-> saveImage(f,brand.getId()))
                .doOnNext(brand::setLogo)
                .then(Mono.justOrEmpty(brand));
    }

    @Override
    public Flux<Brand> delete(Flux<Brand> flux) {
        return flux
                .map(Brand::getId)
                .flatMap(this::findById)
                .doOnNext(brand -> brand.setIsDelete(true))
                .flatMap(this::update);
    }
}
