package com.classroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("classroom")
public class Classroom {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String building;
    private Integer floorNum;
    private Integer capacity;
    private String roomType;
    private Integer hasProjector;
    private Integer hasAc;
    private Integer roomStatus;
    private String description;
    private String createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public Integer getFloorNum() { return floorNum; }
    public void setFloorNum(Integer floorNum) { this.floorNum = floorNum; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public Integer getHasProjector() { return hasProjector; }
    public void setHasProjector(Integer hasProjector) { this.hasProjector = hasProjector; }
    public Integer getHasAc() { return hasAc; }
    public void setHasAc(Integer hasAc) { this.hasAc = hasAc; }
    public Integer getRoomStatus() { return roomStatus; }
    public void setRoomStatus(Integer roomStatus) { this.roomStatus = roomStatus; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
