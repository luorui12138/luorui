package com.ks.eshop.eshop_basic_sever.Handler;

import com.ks.eshop.eshop_basic_sever.Service.BrandService;
import com.ks.eshop.eshop_common.MongoFetch.ReactiveHandler;
import com.ks.eshop.eshop_model.Models.Brand;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/BrandHandler")
public class BrandHandler extends ReactiveHandler<Brand, BrandService> {

    @RequestMapping(value = "/uploadAvatar", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Mono<Brand> addAttach(@RequestPart("avatar") Flux<FilePart> filePart,//文件参数
                                           @RequestPart("id") String brandId) {
        return Source().saveBrandLogo(filePart,brandId);
    }
}
