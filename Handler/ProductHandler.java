package com.ks.eshop.eshop_basic_sever.Handler;

import com.ks.eshop.eshop_basic_sever.Service.ProductService;
import com.ks.eshop.eshop_common.MongoFetch.ReactiveHandler;
import com.ks.eshop.eshop_model.Models.Product;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/ProductHandler")
public class ProductHandler extends ReactiveHandler<Product, ProductService> {

    @RequestMapping(value = "/findByBrandId/{brandId}", method = RequestMethod.POST)
    @ResponseBody
    public Flux<Product> findByBrandId(@PathVariable("brandId") String brandId) {
        return Source().findByBrandId(brandId);
    }

    @RequestMapping(value = "/publishProduct/{productId}", method = RequestMethod.POST)
    @ResponseBody
    public Mono<Product> publishProduct(@PathVariable("productId") String productId) {
        return Source().publish(productId);
    }

    @RequestMapping(value = "/unPublishProduct/{productId}", method = RequestMethod.POST)
    @ResponseBody
    public Mono<Product> unPublishProduct(@PathVariable("productId") String productId) {
        return Source().unPublish(productId);
    }
}
