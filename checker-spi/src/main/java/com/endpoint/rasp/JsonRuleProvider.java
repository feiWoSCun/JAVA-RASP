package com.endpoint.rasp;

import com.endpoint.rasp.common.ErrorType;
import com.endpoint.rasp.common.LogTool;
import com.endpoint.rasp.common.exception.ConfigLoadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.List;
import java.util.function.Function;

/**
 * @author feiwoscun
 */
public class JsonRuleProvider implements RuleProvider {
    private List<Rule> rules;
    private static final String SPI_URL;

    static {
        try {
            CodeSource codeSource = JsonRuleProvider.class.getProtectionDomain().getCodeSource();
            File f=new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
            SPI_URL  = f.getParent();
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
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.rules = mapper.readValue(
                    new File(SPI_URL,"rule.json"), new TypeReference<List<Rule>>() {
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
