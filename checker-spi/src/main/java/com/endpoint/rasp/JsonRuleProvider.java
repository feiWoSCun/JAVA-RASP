package com.endpoint.rasp;

import com.endpoint.rasp.common.ErrorType;
import com.endpoint.rasp.common.LogTool;
import com.endpoint.rasp.common.exception.ConfigLoadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.List;
import java.util.function.Function;

/**
 * @author: feiwoscun
 * @date: 2024/7/31
 * @email: 2825097536@qq.com
 * @description:
 */
public class JsonRuleProvider implements RuleProvider {
    private List<Rule> rules;
    private static final String SPI_URL;

    static {
        try {
            CodeSource codeSource = JsonRuleProvider.class.getProtectionDomain().getCodeSource();
            File f = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
            SPI_URL = f.getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadRules() {
        loadRules(null);
    }

    @Override
    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public <R> void loadRules(Function<List<Rule>, R> function) {
        try {
            // 确保每次读取最新的文件内容
            byte[] jsonData = Files.readAllBytes(Paths.get(SPI_URL, "rule.json"));
            ObjectMapper mapper = new ObjectMapper();
            this.rules = mapper.readValue(jsonData, new TypeReference<List<Rule>>() {
            });
            if (function != null) {
                function.apply(rules);
            }
        } catch (IOException e) {
            LogTool.error(ErrorType.CONFIG_ERROR, "spi加载json失败", e);
            throw new ConfigLoadException(e);
        }
    }
}
