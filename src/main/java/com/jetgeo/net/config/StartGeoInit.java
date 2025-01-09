package com.jetgeo.net.config;

import com.jetgeo.net.core.JetGeoPropertiesLocal;
import com.jetgeo.net.core.ReverseGeoCode;
import com.ling5821.jetgeo.JetGeo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
* @Author : YangFeng
* @Desc : 启动初始化
* @Date : 2024/12/27
**/
@Configuration
public class StartGeoInit {

    @Resource
    private JetGeoPropertiesLocal properties;

    @Value("${jetgeo.geo-data-country-path}")
    private String countryFilePath;
    @Bean
    public JetGeo jetGeo() {
        com.ling5821.jetgeo.config.JetGeoProperties config = new com.ling5821.jetgeo.config.JetGeoProperties();
        config.setGeoDataParentPath(properties.getGeoDataParentPath());
        config.setLevel(properties.getLevel());
        config.setS2MinLevel(properties.getS2MinLevel());
        config.setS2MaxLevel(properties.getS2MaxLevel());
        config.setS2MaxCells(properties.getS2MaxCells());
        config.setLoadCacheInitialCapacity(properties.getLoadCacheInitialCapacity());
        config.setLoadCacheMaximumSize(properties.getLoadCacheMaximumSize());
        config.setLoadCacheExpireAfterAccess(properties.getLoadCacheExpireAfterAccess());
        config.setLoadCacheRefreshAfterWrite(properties.getLoadCacheRefreshAfterWrite());
        return new JetGeo(config);
    }

    @Bean
    public ReverseGeoCode reverseGeoCode() throws IOException {
        return new ReverseGeoCode(new FileInputStream(countryFilePath), true);
    }

}
