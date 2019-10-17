package com.ks.eshop.eshop_basic_sever.Handler;

import com.alibaba.fastjson.JSONObject;
import com.ks.eshop.eshop_basic_sever.Service.ProductCategoryService;
import com.ks.eshop.eshop_common.MongoFetch.ReactiveHandler;
import com.ks.eshop.eshop_model.Models.ProductCategory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/ProductCategoryHandler")
public class ProductCategoryHandler extends ReactiveHandler<ProductCategory, ProductCategoryService> {

    @RequestMapping(value = "/uploadAvatar", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Mono<ProductCategory> addAttach(@RequestPart("categoryIcon") Flux<FilePart> filePart,//文件参数
                                           @RequestPart("id") String productCategoryId) {
        return Source().saveProductCategoryIcon(filePart, productCategoryId);
    }

    @RequestMapping(value = "/saveChildrenProductCategory", method = RequestMethod.POST)
    @ResponseBody
    public Mono<ProductCategory> saveChildrenProductCategory(@RequestBody JSONObject jsonObject) {
        return Source().saveChildrenProductCategory(jsonObject.toJavaObject(ProductCategory.class));
    }

}
