package com.kmbeast.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/*
* 宠物种类信息表，与数据库petType对应
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetType implements Serializable {
    private static final long serialVersionUID = 1L;
    /*
    宠物种类信息表主键ID
    */
    private Integer id;
    /*
    * 类别名
    * */
    private String name;
}
