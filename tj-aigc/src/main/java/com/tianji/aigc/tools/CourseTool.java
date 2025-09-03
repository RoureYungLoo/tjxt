package com.tianji.aigc.tools;

import com.tianji.aigc.tools.constant.Constant;
import com.tianji.aigc.tools.result.CourseInfo;
import com.tianji.aigc.tools.result.ToolResultHolder;
import com.tianji.api.client.course.CourseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 课程工具类
 *
 * @Author mr.wu
 * @Date 2025-8-19 9:58
 */
@Component
@Slf4j
public class CourseTool {

  @Autowired
  private CourseClient courseClient;

  @Tool(description = Constant.Tools.QUERY_COURSE_BY_ID)
  public CourseInfo queryCourseById(@ToolParam(description = Constant.Tool_Param.COURSE_ID) Long id,
                                    ToolContext toolContext) {
    log.info("[工具调用]根据课程ID查询课程详情，课程ID：{}", id);

    //封装返回结果对象
    CourseInfo courseInfo = CourseInfo.of(courseClient.baseInfo(id, true));

    // 从toolContext中获取请求ID
    String requestId = (String) toolContext.getContext().get(Constant.REQUEST_ID);

    // 封装field
    String field = "courseInfo_" + requestId;

    // 保存 courseinfo数据到 toolContext
    ToolResultHolder.put(requestId, field, courseInfo);

    return courseInfo;
  }
}