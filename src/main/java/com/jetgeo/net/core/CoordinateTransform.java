package com.jetgeo.net.core;

import com.jetgeo.net.model.Point;

/**
 * 坐标系转换工具。
 *
 * @author yf
 */
public final class CoordinateTransform {

    private static final double X_PI = Math.PI * 3000.0 / 180.0;
    private static final double A = 6378245.0;
    private static final double EE = 0.00669342162296594323;

    private CoordinateTransform() {
    }

    /**
     * 按配置将请求坐标转换为本地数据使用的坐标系。
     *
     * @param point 原始请求坐标
     * @param source 请求坐标系
     * @param target 本地数据坐标系
     * @return 转换后的坐标
     */
    public static Point convert(Point point, CoordinateSystem source, CoordinateSystem target) {
        if (point == null) {
            throw new IllegalArgumentException("坐标不能为空");
        }
        CoordinateSystem sourceSystem = source == null ? CoordinateSystem.WGS84 : source;
        CoordinateSystem targetSystem = target == null ? CoordinateSystem.WGS84 : target;
        if (sourceSystem == targetSystem) {
            return point(point.getLat(), point.getLng());
        }
        if (sourceSystem == CoordinateSystem.WGS84 && targetSystem == CoordinateSystem.GCJ02) {
            return wgs84ToGcj02(point);
        }
        if (sourceSystem == CoordinateSystem.WGS84 && targetSystem == CoordinateSystem.BD09) {
            return gcj02ToBd09(wgs84ToGcj02(point));
        }
        if (sourceSystem == CoordinateSystem.GCJ02 && targetSystem == CoordinateSystem.WGS84) {
            return gcj02ToWgs84(point);
        }
        if (sourceSystem == CoordinateSystem.GCJ02 && targetSystem == CoordinateSystem.BD09) {
            return gcj02ToBd09(point);
        }
        if (sourceSystem == CoordinateSystem.BD09 && targetSystem == CoordinateSystem.GCJ02) {
            return bd09ToGcj02(point);
        }
        if (sourceSystem == CoordinateSystem.BD09 && targetSystem == CoordinateSystem.WGS84) {
            return gcj02ToWgs84(bd09ToGcj02(point));
        }
        return point(point.getLat(), point.getLng());
    }

    /**
     * 将 WGS84 坐标转换为 GCJ02 坐标。
     *
     * @param point WGS84 坐标
     * @return GCJ02 坐标
     */
    public static Point wgs84ToGcj02(Point point) {
        double lat = point.getLat();
        double lng = point.getLng();
        if (outOfChina(lat, lng)) {
            return point(lat, lng);
        }
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * Math.PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * Math.PI);
        return point(lat + dLat, lng + dLng);
    }

    /**
     * 将 GCJ02 坐标近似转换为 WGS84 坐标。
     *
     * @param point GCJ02 坐标
     * @return WGS84 坐标
     */
    public static Point gcj02ToWgs84(Point point) {
        double lat = point.getLat();
        double lng = point.getLng();
        if (outOfChina(lat, lng)) {
            return point(lat, lng);
        }
        Point gcjPoint = wgs84ToGcj02(point);
        return point(lat * 2 - gcjPoint.getLat(), lng * 2 - gcjPoint.getLng());
    }

    /**
     * 将 GCJ02 坐标转换为 BD09 坐标。
     *
     * @param point GCJ02 坐标
     * @return BD09 坐标
     */
    public static Point gcj02ToBd09(Point point) {
        double lat = point.getLat();
        double lng = point.getLng();
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI);
        return point(z * Math.sin(theta) + 0.006, z * Math.cos(theta) + 0.0065);
    }

    /**
     * 将 BD09 坐标转换为 GCJ02 坐标。
     *
     * @param point BD09 坐标
     * @return GCJ02 坐标
     */
    public static Point bd09ToGcj02(Point point) {
        double x = point.getLng() - 0.0065;
        double y = point.getLat() - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        return point(z * Math.sin(theta), z * Math.cos(theta));
    }

    /**
     * 判断坐标是否在中国常用偏移范围之外。
     *
     * @param lat 纬度
     * @param lng 经度
     * @return true 表示不需要做 GCJ02 偏移
     */
    private static boolean outOfChina(double lat, double lng) {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }

    /**
     * 计算纬度偏移量。
     *
     * @param x 经度差值
     * @param y 纬度差值
     * @return 纬度偏移量
     */
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 计算经度偏移量。
     *
     * @param x 经度差值
     * @param y 纬度差值
     * @return 经度偏移量
     */
    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y
                + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 创建新的坐标对象，避免改动请求对象本身。
     *
     * @param lat 纬度
     * @param lng 经度
     * @return 坐标对象
     */
    private static Point point(double lat, double lng) {
        Point point = new Point();
        point.setLat(lat);
        point.setLng(lng);
        return point;
    }
}
