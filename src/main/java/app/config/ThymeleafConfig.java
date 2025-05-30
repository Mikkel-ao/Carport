package app.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class ThymeleafConfig
{
    public static TemplateEngine templateEngine()
    {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        //Assuming templates are in resources/templates/
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }
}
