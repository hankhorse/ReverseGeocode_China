package com.jetgeo.net.controller;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.jetgeo.net.core.ReverseGeoCode;
import com.jetgeo.net.model.Point;
import com.ling5821.jetgeo.JetGeo;
import com.ling5821.jetgeo.model.GeoInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/localGeo")
public class GeoApiController {


    @Autowired
    private JetGeo jetGeo;
    @Resource
    private ReverseGeoCode reverseGeoCode;

    @PostMapping("/getGeo")
    public GeoInfo getGeo(@RequestBody Point point) throws ExecutionException, InterruptedException {
        // 创建 CompletableFuture 用来异步处理 geoInfo 和 street 的获取
        CompletableFuture<GeoInfo> geoInfoFuture = CompletableFuture.supplyAsync(() -> jetGeo.getGeoInfo(point.getLat(), point.getLng()));
        CompletableFuture<String> streetFuture = CompletableFuture.supplyAsync(() -> reverseGeoCode.nearestPlace(point.getLat(), point.getLng()).allNames);

        // 等待两个异步任务完成并组合结果
        CompletableFuture.allOf(geoInfoFuture, streetFuture).join(); // 等待所有任务完成
        GeoInfo g = geoInfoFuture.get();
        String street = streetFuture.get();

        return dealResult(g, street);
    }

    /**
    *  处理合并2个异步的结果
    **/
    private GeoInfo dealResult(GeoInfo geoInfo, String street){
        //正常的 street结果是：hou ting,hou ting cun,厚庭,厚庭村   需要截取出最后的中文名/英文名
        String streetName = "";
        if(!StringUtils.isEmpty(street)){
            String[] nameArray = street.split(",");
            // 获取最后一个名字
            streetName = nameArray[nameArray.length - 1].trim();
            //如果是中文
            if(isChinese(streetName)){
                geoInfo.setStreet(streetName);
                geoInfo.setFormatAddress(geoInfo.getFormatAddress()+ streetName);
            }
        }
        return geoInfo;
    }

    public static boolean isChinese(String str) {
        // 正则表达式匹配中文字符
        String regex = "^[\\u4e00-\\u9fa5]+$";
        return str.matches(regex);
    }
}
