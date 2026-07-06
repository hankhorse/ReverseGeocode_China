package com.jetgeo.net.core;

import java.util.Locale;

/**
 * 坐标系类型。
 *
 * @author yf
 */
public enum CoordinateSystem {
    WGS84,
    GCJ02,
    BD09;

    /**
     * 将配置中的坐标系文本转换为枚举值。
     *
     * @param value 配置文本，支持 wgs84/gcj02/bd09
     * @return 坐标系枚举
     */
    public static CoordinateSystem fromConfig(String value) {
        if (value == null || value.trim().length() == 0) {
            return WGS84;
        }
        String normalized = value.trim().replace("-", "").replace("_", "").toUpperCase(Locale.ROOT);
        for (CoordinateSystem coordinateSystem : values()) {
            if (coordinateSystem.name().equals(normalized)) {
                return coordinateSystem;
            }
        }
        throw new IllegalArgumentException("不支持的坐标系：" + value + "，可选值：wgs84/gcj02/bd09");
    }
}
