package com.example.wangjie.systemblueteeth.util;

/**
 * Created by wangjie on 2017/6/23.
 * 小车信息交换工具类
 */

public class MsgUtils {
  public static String getDirectionValue(Float ang){
      String res = "'";
      if(0<ang && ang < 30){
          res = "0X02";
      }else if(30<ang && ang < 60){
          res = "0X01";
      } else if(60<ang &&ang < 90){
          res = "0X0C";
      }else if(90<ang && ang < 120){
          res = "0X0B";
      }else if(120<ang && ang < 150){
          res = "0X0A";
      } else if(150<ang && ang < 180){
          res = "0X09";
      }else if(180<ang && ang < 210){
          res = "0X08";
      }else if(210<ang && ang < 240){
          res = "0X07";
      }else if(240<ang && ang < 270){
          res = "0X06";
      }else if(270<ang && ang < 300){
          res = "0X05";
      }else if(300<ang && ang < 330){
          res = "0X04";
      }if(330 < ang){
          res = "0X03";
      }
      return res;
  }

  public static String sendMsg(String type,String value){
          int v1 = 0xBC;
          int v2 = 0x85;
          int v3 = 0x35;
          Integer v4 = Integer.parseInt(type.substring(2),16);
          Integer v5 = Integer.parseInt(value);
          int v6 = (v1+v2+v3+v4+v5) % 100;
          int v7 = 0x0D;
          int v8 = 0x0A;
          return (format(v1)+""+format(v2)+""+format(v3)+""+format(v4)+""+format(v5)+""+format(v6)+""+format(v7)+""+format(v8)).toUpperCase();
  }

    public static String format(int n){
        String str=Integer.toHexString(n);
        int l=str.length();
        if(l==1)return "0"+str;
        return str.substring(l-2,l);
    }
}
