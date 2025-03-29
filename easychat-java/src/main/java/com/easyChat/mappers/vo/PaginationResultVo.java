package com.easyChat.mappers.vo;

import java.util.ArrayList;
import java.util.List;

public class PaginationResultVo<T>{
    private Integer totalCount;
    private Integer pageSize;
    private Integer pageNo;
    private Integer pageTotal;
    private List<T> list = new ArrayList<T>();

    public PaginationResultVo(Integer totalCount, Integer pageSize, Integer pageNo, List<T> list) {
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.list = list;
    }
    public PaginationResultVo(Integer totalCount, Integer pageSize, Integer pageNo, Integer pageTotal, List<T> list) {
        if(pageNo == 0){
            pageNo = 1;
        }
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.pageTotal = pageTotal;
        this.list = list;
    }
    public PaginationResultVo(List<T> list) {
        this.list = list;
    }
    public PaginationResultVo() {

    }
    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void pageTotal(Integer pageTotal) {
        this.pageTotal = pageTotal;
    }

    public Integer getPageTotal(){
        return pageTotal;
    }

}
