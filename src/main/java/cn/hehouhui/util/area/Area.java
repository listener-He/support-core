package cn.hehouhui.util.area;

import java.util.Collections;
import java.util.List;

/**
 * 行政区域对象
 *
 * @author HeHui
 * @date 2024-11-28 13:22
 */
public class Area {

    /**
     * 默认区域代码，当没有父地区的时候使用该代码作为父区域代码
     */
    public static final String DEFAULT = "000000";

    /**
     * 当前地区代码
     */
    private String code;

    /**
     * 父地区代码
     */
    private String parent;

    /**
     * 地区名
     */
    private String name;

    /**
     * 子地区
     */
    private List<Area> childList = Collections.emptyList();


    /**
     * 自己复制自己，deep copy
     *
     * @return 复制结果
     */
    public Area copy() {
        Area area = new Area();
        area.code = code;
        area.parent = parent;
        area.name = name;
        area.childList = childList.stream().map(Area::copy).toList();
        return area;
    }


    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(final String parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Area> getChildList() {
        return childList;
    }

    public void setChildList(final List<Area> childList) {
        this.childList = childList;
    }
}
