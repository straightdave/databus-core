package com.blueline.databus.core.helper;

import com.blueline.databus.core.datatype.RestResult;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletResponse;

/**
 * 帮助在filter中渲染ServletResponse对象
 * 用以在错误时返回http消息
 */
public class FilterResponseRender {
    public static void render(ServletResponse resp, RestResult result) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(resp.getOutputStream());
            resp.setCharacterEncoding("utf-8");
            resp.setContentType("application/json");
            writer.print(result.toString());
            writer.flush();
        }
        catch (IOException ex) {
            System.err.print(ex.getMessage());
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
