package test;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreemarkerTest {
	@Test
	public void testOutput() throws Exception {
		// 1、创建Configuration，生成模板实例
		Configuration configuration = new Configuration(Configuration.VERSION_2_3_0);
		configuration.setDirectoryForTemplateLoading(new File("src/main/webapp/WEB-INF/templates"));
		// 2、指定使用模板文件，生成Template实例
		Template template = configuration.getTemplate("hello.ftl");
		// 3、填充数据模型，数据模型就是一个Map
		Map<String, String> map = new HashMap<String, String>();
		map.put("title", "黑马程序员");
		map.put("msg", "你好,这是你的第一个freemarker案例");
		// 4、调用Template实例process完成数据合并
		template.process(map, new PrintWriter(System.out));
	}
}
