package com.tianji.aigc.constant;

/**
 * @author luruoyang
 */

import lombok.Getter;

@Getter
public enum TimeStrConstant {
  TODAY("当天"),
  THIS_MONTH("最近30天"),
  THIS_YEAR("最近1年"),
  OVER_YEAR("1年以上");

  private String value;

  TimeStrConstant(String s) {
    this.value = s;
  }
}
