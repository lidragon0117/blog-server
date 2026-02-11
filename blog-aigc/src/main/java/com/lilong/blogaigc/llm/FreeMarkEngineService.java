package com.lilong.blogaigc.llm;

import com.lilong.blog.enums.agent.PromptTemplateEnum;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author : lilong
 * @date : 2026-02-11 20:07
 * @description : 模版渲染
 */
@Slf4j
@Service
public class FreeMarkEngineService {

    /**
     * 默认编码
     */
    private static final String UTF8 = "UTF-8";

    /**
     * 默认模板位置
     */
    private static final String DEFAULT_PROMPT_TEMPLATE_PATH = "/templates";

    /**
     * 获取模版内容
     *
     * @param vars
     * @param promptTemplate
     * @return
     */
    public String getContentByTemplate(Map<String, Object> vars, PromptTemplateEnum promptTemplate) {

        return this.getContentByTemplate(vars, promptTemplate.getPath(), DEFAULT_PROMPT_TEMPLATE_PATH);
    }

    /**
     * 获取模版内容
     *
     * @param vars
     * @param templateName
     * @param defaultPath
     * @return
     */
    public String getContentByTemplate(Map<String, Object> vars, String templateName, String defaultPath) {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
            ClassTemplateLoader templateLoader = new ClassTemplateLoader(
                    new FreeMarkEngineService().getClass().getClassLoader(), defaultPath);
            cfg.setTemplateLoader(templateLoader);
            cfg.setDefaultEncoding(UTF8);
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            return this.processBuildTemplate(cfg.getTemplate(templateName), vars);
        } catch (IOException e) {
            log.info("获取模版内容失败:", e);
        } catch (TemplateException e) {
            log.info("模版渲染失败:", e);
        }
        return null;
    }

    /**
     * 渲染
     *
     * @param template
     * @param vars
     * @return
     * @throws TemplateException
     * @throws IOException
     */
    private String processBuildTemplate(Template template, Map<String, Object> vars)
            throws TemplateException, IOException {

        StringWriter writer = new StringWriter();
        template.process(vars, writer);
        writer.flush();
        writer.close();
        return writer.getBuffer().toString();
    }
}
