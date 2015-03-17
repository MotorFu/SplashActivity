package com.zxq.vo;

import java.util.List;

/**
 * Created by zxq on 2014/9/14.
 */
public class DepartmentInfo extends OrgInfo {
    private List<OrgInfo> childInfo;
    private String deptName;
    private int deptId;

    public DepartmentInfo() {
    }

    public DepartmentInfo(List<OrgInfo> childInfo, String deptName, int deptId) {

        this.childInfo = childInfo;
        this.deptName = deptName;
        this.deptId = deptId;
    }

    public void setChildInfo(List<OrgInfo> childInfo) {

        this.childInfo = childInfo;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {

        return deptName;
    }

    public int getDeptId() {
        return deptId;
    }

    public List<OrgInfo> getChildInfo() {

        return childInfo;
    }

    @Override
    public boolean addChildInfo(OrgInfo orgInfo){
        childInfo.add(orgInfo);
        return true;
    }


    @Override
    public boolean removeChildInfo(OrgInfo orgInfo){
        childInfo.remove(orgInfo);
        return true;
    }

    @Override
    public boolean isDepartmentInfo() {
        return true;
    }

}
