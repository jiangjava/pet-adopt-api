package com.kmbeast.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/*
* 宠物领养单据信息表，与数据库pet_adopt_order对应
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetAdoptOrder {/*
    宠物领养单据信息表主键ID
*/
    private Integer id;
    /*
    * 用户ID，外键，关联的用户表
    * */
    private Integer userId;
    /*
    * 宠物ID，外键，关联的宠物表
    * */
    private Integer petId;
    /*
    * 收货地址ID，外键，关联收货地址表
    * */
    private Integer addressId;
    /*
    * 领养描述
    * */
    private String detail;
    /*
    * 单据状态（1：申请中;2:已审核 ;3:审核未通过;4已完成)
    * */
    private Integer status;
    /*
    * 审核不通过的原因描述
    * */
    private String auditErrorDetail;
    /*
    * 是否再次提交（0：初次提交；1：再次提交）
    * */
    private Boolean isAgainPost;
    /*
    * 提交次数
    * */
    private Integer postNumber;
    /*
    * 创建时间
    * */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
