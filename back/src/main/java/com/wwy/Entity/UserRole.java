package com.wwy.Entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserRole implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Integer userId;
    private Integer roleId;
    private Long createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
} 