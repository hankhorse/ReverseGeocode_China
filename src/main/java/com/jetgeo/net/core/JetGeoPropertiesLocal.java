package com.jetgeo.net.core;

import com.ling5821.jetgeo.enums.LevelEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
* @Author : YangFeng
* @Desc :  实体类
* @Date : 2024/12/27
**/
@Component
public class JetGeoPropertiesLocal {

    /**
     * 区域地理边界坐标数据所在的父目录
     */
    @Value("${jetgeo.geo-data-parent-path}")
    private String geoDataParentPath;

    /**
     * 逆地理转换到的最低级别
     */
    @Value("${jetgeo.level}")
    private LevelEnum level;

    /**
     * S2 最小/最大单元格级别 调整前请务必先了解S2算法
     */
    private int s2MinLevel = 11;
    private int s2MaxLevel = 16;

    /**
     * S2 最大单元格个数 调整前请务必先了解S2算法
     */
    private int s2MaxCells = 100;

    /**
     * 只对市级/县级缓存有效, 省级缓存在初始化时已经全部加载到内存
     */
    private int loadCacheInitialCapacity = 20;

    private int loadCacheMaximumSize = 100;

    /**
     * 在指定的过期时间内没有读写，缓存数据即失效 建议与 loadCacheRefreshAfterWrite 设置为 5:1 / 3:1 的关系
     */
    private Duration loadCacheExpireAfterAccess = Duration.of(5, ChronoUnit.MINUTES);

    /**
     * 在指定的过期时间之后访问时，刷新缓存数据，在刷新任务未完成之前，其他线程返回旧值 建议与 loadCacheRefreshAfterWrite 设置为 1:5 / 1:3 的关系
     */
    private Duration loadCacheRefreshAfterWrite = Duration.of(1, ChronoUnit.MINUTES);


    public String getGeoDataParentPath() {
        return geoDataParentPath;
    }

    public void setGeoDataParentPath(String geoDataParentPath) {
        this.geoDataParentPath = geoDataParentPath;
    }

    public LevelEnum getLevel() {
        return level;
    }

    public void setLevel(LevelEnum level) {
        this.level = level;
    }

    public int getS2MinLevel() {
        return s2MinLevel;
    }

    public void setS2MinLevel(int s2MinLevel) {
        this.s2MinLevel = s2MinLevel;
    }

    public int getS2MaxLevel() {
        return s2MaxLevel;
    }

    public void setS2MaxLevel(int s2MaxLevel) {
        this.s2MaxLevel = s2MaxLevel;
    }

    public int getS2MaxCells() {
        return s2MaxCells;
    }

    public void setS2MaxCells(int s2MaxCells) {
        this.s2MaxCells = s2MaxCells;
    }

    public int getLoadCacheInitialCapacity() {
        return loadCacheInitialCapacity;
    }

    public void setLoadCacheInitialCapacity(int loadCacheInitialCapacity) {
        this.loadCacheInitialCapacity = loadCacheInitialCapacity;
    }

    public int getLoadCacheMaximumSize() {
        return loadCacheMaximumSize;
    }

    public void setLoadCacheMaximumSize(int loadCacheMaximumSize) {
        this.loadCacheMaximumSize = loadCacheMaximumSize;
    }

    public Duration getLoadCacheExpireAfterAccess() {
        return loadCacheExpireAfterAccess;
    }

    public void setLoadCacheExpireAfterAccess(Duration loadCacheExpireAfterAccess) {
        this.loadCacheExpireAfterAccess = loadCacheExpireAfterAccess;
    }

    public Duration getLoadCacheRefreshAfterWrite() {
        return loadCacheRefreshAfterWrite;
    }

    public void setLoadCacheRefreshAfterWrite(Duration loadCacheRefreshAfterWrite) {
        this.loadCacheRefreshAfterWrite = loadCacheRefreshAfterWrite;
    }
}
