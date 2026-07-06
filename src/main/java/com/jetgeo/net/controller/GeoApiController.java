package com.jetgeo.net.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.jetgeo.net.core.CoordinateTransform;
import com.jetgeo.net.core.JetGeoPropertiesLocal;
import com.jetgeo.net.core.ReverseGeoCode;
import com.jetgeo.net.model.GeoName;
import com.jetgeo.net.model.Point;
import com.ling5821.jetgeo.JetGeo;
import com.ling5821.jetgeo.model.GeoInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/localGeo")
public class GeoApiController {


    @Autowired
    private JetGeo jetGeo;
    @Resource
    private ReverseGeoCode reverseGeoCode;
    @Resource
    private JetGeoPropertiesLocal properties;

    /**
     * 逆地理编码入口，兼容单点请求和批量请求。
     *
     * @param requestBody 请求坐标，支持对象、数组或 points 包装对象
     * @return 单点返回 GeoInfo，批量返回 GeoInfo 列表
     * @throws ExecutionException 异步查询执行异常
     * @throws InterruptedException 异步查询中断异常
     */
    @PostMapping("/getGeo")
    public Object getGeo(@RequestBody JsonNode requestBody) throws ExecutionException, InterruptedException {
        if (requestBody == null || requestBody.isNull()) {
            throw badRequest("请求体不能为空");
        }
        if (requestBody.isArray()) {
            return getGeoList(requestBody);
        }
        JsonNode pointsNode = requestBody.get("points");
        if (pointsNode != null) {
            if (!pointsNode.isArray()) {
                throw badRequest("points 必须是坐标数组");
            }
            return getGeoList(pointsNode);
        }
        return getGeoOne(parsePoint(requestBody));
    }

    /**
     * 查询单个坐标点的逆地理结果。
     *
     * @param point 请求坐标
     * @return 逆地理结果
     * @throws ExecutionException 异步查询执行异常
     * @throws InterruptedException 异步查询中断异常
     */
    private GeoInfo getGeoOne(Point point) throws ExecutionException, InterruptedException {
        Point queryPoint = CoordinateTransform.convert(point, properties.getInputCoordinateSystem(), properties.getDataCoordinateSystem());
        CompletableFuture<GeoInfo> geoInfoFuture = CompletableFuture.supplyAsync(() -> jetGeo.getGeoInfo(queryPoint.getLat(), queryPoint.getLng()));
        CompletableFuture<String> streetFuture = CompletableFuture.supplyAsync(() -> {
            GeoName geoName = reverseGeoCode.nearestPlace(queryPoint.getLat(), queryPoint.getLng());
            return geoName == null ? null : geoName.allNames;
        });

        CompletableFuture.allOf(geoInfoFuture, streetFuture).join();
        GeoInfo geoInfo = geoInfoFuture.get();
        String street = streetFuture.get();

        return dealResult(geoInfo, street);
    }

    /**
     * 批量查询多个坐标点的逆地理结果。
     *
     * @param pointsNode 坐标数组节点
     * @return 逆地理结果列表
     * @throws ExecutionException 异步查询执行异常
     * @throws InterruptedException 异步查询中断异常
     */
    private List<GeoInfo> getGeoList(JsonNode pointsNode) throws ExecutionException, InterruptedException {
        List<GeoInfo> resultList = new ArrayList<>();
        for (JsonNode pointNode : pointsNode) {
            resultList.add(getGeoOne(parsePoint(pointNode)));
        }
        return resultList;
    }

    /**
     * 从请求节点中解析坐标点。
     *
     * @param pointNode 坐标 JSON 节点
     * @return 坐标点
     */
    private Point parsePoint(JsonNode pointNode) {
        if (pointNode == null || !pointNode.isObject()) {
            throw badRequest("坐标必须是包含 lat 和 lng 的对象");
        }
        Point point = new Point();
        point.setLat(readDouble(pointNode.get("lat"), "lat"));
        point.setLng(readDouble(pointNode.get("lng"), "lng"));
        return point;
    }

    /**
     * 读取数字字段，兼容数字和字符串两种 JSON 表达。
     *
     * @param node 字段节点
     * @param fieldName 字段名称
     * @return double 数值
     */
    private double readDouble(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            throw badRequest(fieldName + " 不能为空");
        }
        if (node.isNumber()) {
            return node.asDouble();
        }
        if (node.isTextual()) {
            String text = node.asText().trim();
            if (text.length() == 0) {
                throw badRequest(fieldName + " 不能为空");
            }
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                throw badRequest(fieldName + " 必须是数字");
            }
        }
        throw badRequest(fieldName + " 必须是数字");
    }

    /**
     * 处理合并2个异步的结果。
     *
     * @param geoInfo 行政区划查询结果
     * @param street 村镇最近点别名
     * @return 合并后的逆地理结果
     */
    private GeoInfo dealResult(GeoInfo geoInfo, String street) {
        if (geoInfo == null) {
            return null;
        }
        String streetName = "";
        if (StringUtils.hasText(street)) {
            String[] nameArray = street.split(",");
            streetName = nameArray[nameArray.length - 1].trim();
            if (isChinese(streetName)) {
                geoInfo.setStreet(streetName);
                String formatAddress = geoInfo.getFormatAddress() == null ? "" : geoInfo.getFormatAddress();
                geoInfo.setFormatAddress(formatAddress + streetName);
            }
        }
        return geoInfo;
    }

    /**
     * 判断文本是否全部为中文字符。
     *
     * @param str 待判断文本
     * @return true 表示文本全部为中文
     */
    public static boolean isChinese(String str) {
        String regex = "^[\\u4e00-\\u9fa5]+$";
        return str != null && str.matches(regex);
    }

    /**
     * 创建 400 请求异常。
     *
     * @param message 错误信息
     * @return 请求异常
     */
    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
