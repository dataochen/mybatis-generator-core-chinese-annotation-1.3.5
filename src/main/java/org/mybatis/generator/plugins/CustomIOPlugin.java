package org.mybatis.generator.plugins;

import org.mybatis.generator.api.PluginAdapter;

import java.util.List;

/**
 * @author dataochen
 * @Description 简单的文件io
 * @date: 2017/11/27 22:30
 */
public class CustomIOPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        //IO指定文件a写到指定文件b
        return true;
    }
}
