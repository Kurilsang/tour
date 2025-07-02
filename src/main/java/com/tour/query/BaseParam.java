package com.tour.query;


public class BaseParam {

	private Integer pageNo;
	private Integer pageSize;
	private String orderBy;
	private Integer totalNo;


	public Integer getTotalNo() {
return totalNo;
	}
	public void setTotalNo(Integer totalNo) {
		this.totalNo = totalNo;
	}
    public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public void setOrderBy(String orderBy){
		this.orderBy = orderBy;
	}

	public String getOrderBy(){
		return this.orderBy;
	}
}
