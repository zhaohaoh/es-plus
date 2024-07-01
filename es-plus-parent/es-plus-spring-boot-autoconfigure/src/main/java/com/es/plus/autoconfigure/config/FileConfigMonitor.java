package com.es.plus.autoconfigure.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;


/**
 * 配置监听器
 *
 * @author hzh
 * @date 2023/04/11
 */
public class FileConfigMonitor extends AbstractConfigManager {
    
    private static final Logger logger = LoggerFactory.getLogger(FileConfigMonitor.class);
    
    private FileAlterationMonitor fileMonitor;
    
    @PostConstruct
    public void init() throws FileNotFoundException {
        URL url = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX);
        if (!ResourceUtils.isFileURL(url)) {
            return;
        }
        
        FileAlterationObserver observer = new FileAlterationObserver(url.getPath(), FileFilterUtils
                .and(FileFilterUtils.fileFileFilter(), FileFilterUtils
                        .and(FileFilterUtils.suffixFileFilter(dataId))));
        
        FileListener listener = new FileListener();
        observer.addListener(listener);
        fileMonitor = new FileAlterationMonitor(5000, observer);
        try {
            fileMonitor.start();
        } catch (Exception e) {
            logger.error("fileMonitor ", e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        try {
            fileMonitor.stop();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    
    private class FileListener extends FileAlterationListenerAdaptor {
        @Override
        public void onFileChange(File file) {
            logger.info("onFileChange");
            super.onFileChange(file);
            try {
                // 解析文件内容
                logger.info("读取映射文件 :{}", file);
                String name = file.getName();
                String input = FileUtils.readFileToString(file);
                listenerChange(name, input);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    
}
