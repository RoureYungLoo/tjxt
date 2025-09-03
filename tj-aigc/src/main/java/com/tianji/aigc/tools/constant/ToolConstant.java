package com.tianji.aigc.tools.constant;

/**
 * 常量类
 *
 * @Author mr.wu
 * @Date 2025-8-19 9:56
 */
public interface ToolConstant {

  String USER_ID = "user_id";

  interface Tools {
    /**
     * 函数描述
     */
    String QUERY_COURSE_BY_ID = "根据课程ID查询课程详情";
    String PRE_PLACE_ORDER_BY_COURSE_ID = "根据课程ID下单";
  }

  interface Tool_Param {
    /**
     * 函数参数描述
     */
    String COURSE_ID = "课程ID";
  }

  /**
   * 请求ID的KEY
   */
  String REQUEST_ID = "request_id";
}