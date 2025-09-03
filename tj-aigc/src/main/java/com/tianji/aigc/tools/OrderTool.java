package com.tianji.aigc.tools;

import com.tianji.aigc.entity.PrePlaceOrder;
import com.tianji.aigc.tools.constant.ToolConstant;
import com.tianji.aigc.tools.result.ToolResultHolder;
import com.tianji.api.client.trade.TradeClient;
import com.tianji.api.dto.trade.OrderConfirmVO;
import com.tianji.common.utils.UserContext;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 预下单 tool
 *
 * @author luruoyang
 */
@Component
public class OrderTool {

  @Autowired
  private TradeClient tradeClient;

  @Tool(description = ToolConstant.Tools.PRE_PLACE_ORDER_BY_COURSE_ID)
  public PrePlaceOrder prePlaceOrder(
      @ToolParam(description = ToolConstant.Tool_Param.COURSE_ID) Long courseId,
      ToolContext toolContext) {

    // 设置UserId
    Long userId = (Long) toolContext.getContext().get("user_id");
    UserContext.setUser(userId);

    // key field value
    // requestId  prePlaceOrder prePlaceOrder
    OrderConfirmVO orderConfirmVO = tradeClient.prePlaceOrder(Arrays.asList(courseId));
    PrePlaceOrder prePlaceOrder = PrePlaceOrder.of(orderConfirmVO);

    String requestId = (String) toolContext.getContext().get("request_id");
    ToolResultHolder.put(requestId, "prePlaceOrder", prePlaceOrder);
    return prePlaceOrder;
  }
}
