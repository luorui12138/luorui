package com.ks.eshop.eshop_basic_sever.Service;

import com.ks.eshop.eshop_basic_sever.Repository.CompanyRepository;
import com.ks.eshop.eshop_common.MongoFetch.ReactiveService;
import com.ks.eshop.eshop_model.Models.Brand;
import com.ks.eshop.eshop_model.Models.Company;
import com.ks.eshop.eshop_model.Service.CompanyTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CompanyService extends ReactiveService<Company, CompanyRepository> {

    private final AddressService addressService;
    private final CompanyTypeService companyTypeService;

    @Autowired
    public CompanyService(AddressService addressService, CompanyTypeService companyTypeService) {
        this.addressService = addressService;
        this.companyTypeService = companyTypeService;
    }

    @Override
    public Flux<Company> insert(Flux<Company> flux) {
        return flux
                .flatMap(this::setCompanyType)
                .flatMap(addressService::saveFromWhen)
                .compose(Source()::insert);
    }

    private Mono<Company> setCompanyType(Company company) {
        return Mono.justOrEmpty(company.getCompanyTypeId())
                .flatMap(companyTypeService::isExist)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("请设置公司经营模式")))
                .thenReturn(company)
                .flatMap(this::saveCompanyType);
    }

    private Mono<Company> saveCompanyType(Company company) {
        return Mono.justOrEmpty(company.getCompanyTypeId())
                .flatMap(companyTypeService::findById)
                .doOnNext(company::setCompanyType)
                .thenReturn(company);
    }

    public Mono<Company> saveCompanyLogo(Flux<FilePart> filePartFlux, String id) {
        return Source()
                .findById(id)
                .flatMap(company -> this.setCompanyLogo(company, filePartFlux))
                .flatMap(this::update);
    }

    private Mono<Company> setCompanyLogo(Company company, Flux<FilePart> filePartFlux) {
        return filePartFlux
                .compose(f -> saveImage(f, company.getId()))
                .doOnNext(company::setLogo)
                .then(Mono.justOrEmpty(company));
    }

    @Override
    public Flux<Company> delete(Flux<Company> flux) {
        return flux
                .map(Company::getId)
                .flatMap(this::findById)
                .doOnNext(company -> company.setIsDelete(true))
                .flatMap(this::update);
    }
}
