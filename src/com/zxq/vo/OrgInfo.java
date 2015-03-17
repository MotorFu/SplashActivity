package com.zxq.vo;

import java.util.List;

/**
 * Created by zxq on 2014/9/14.
 */
public abstract class OrgInfo {



    public abstract boolean addChildInfo(OrgInfo orgInfo);

    public abstract boolean removeChildInfo(OrgInfo orgInfo);

    public abstract boolean isDepartmentInfo();

}
