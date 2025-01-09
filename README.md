# ReverseGeocode_China
中国地区逆地理编码，通过Java实现精确度到村镇程度的本地逆地理方式，避免高德、百度API限量问题，ReverseGeocode in China！

该代码大部分基于2位github上大佬的文章结合使用：

1、[AReallyGoodName](https://github.com/AReallyGoodName) 的文章[OfflineReverseGeocode](https://github.com/AReallyGoodName/OfflineReverseGeocode) （解决了村镇精度的问题）

2、[linG5821](https://github.com/linG5821)的文章[jetgeo](https://github.com/linG5821/jetgeo) （解决了省市区部分的问题）

## 项目启动准备
 - 1、克隆项目
 - 2、解压resource文件夹下的 CN.RAR 与 geodata.part01...05
 - 3、修改yaml文件下的  geo-data-parent-path （本地CN.RAR解压后文件地址） 与 geo-data-country-path （本地geodata.part解压后文件夹位置）
> 具体如下：
```yaml
  geo-data-parent-path: D:\JetGeo\jetgeo-main\data\geodata  #geodata文件夹所在位置，resource/data下面geodata.7z解压后文件夹位置
  geo-data-country-path: C:\Users\YLXT01\Desktop\CN.txt   #CN.txt村镇所在文件位置,见resource/data位置
 ```
## 项目运行效果

### ①接口地址：

http://localhost:5555/localGeo/getGeo

### ②请求方式：

**POST**

### ③请求参数：

<i>application/json</i>

```json
{
    "lng":"116.56183",
    "lat":"35.320279"
}
```

### ④返回结果：

```json
{
    "formatAddress": "山东省济宁市任城区刘街村",
    "province": "山东省",
    "city": "济宁市",
    "district": "任城区",
    "street": "刘街村",
    "adcode": "370811",
    "level": null
}
```



