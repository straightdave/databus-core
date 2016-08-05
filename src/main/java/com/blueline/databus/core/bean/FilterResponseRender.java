package com.blueline.databus.core.bean;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletResponse;

/**
 * 帮助在filter中渲染ServletResponse对象
 * 用以在错误时返回http消息
 */
public class FilterResponseRender {
    public static void render(ServletResponse resp, RestResult result) {
        try (PrintWriter writer = resp.getWriter()) {
            resp.reset();
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json");
            writer.print(result.toString());
        } catch (IOException ex) {
            System.err.print(ex.getMessage());
        }
    }
}
