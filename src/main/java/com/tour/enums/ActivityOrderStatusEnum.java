package com.tour.enums;


public enum ActivityOrderStatusEnum {
//    1-待支付 2-已支付 3-已取消 4-已过期 5-已完成（签到完成并活动结束） 6-已免单
    NONPAYMENT(1),PAID(2),CANCELED(3),TIMEOUT(4),COMPLETED(5),FEE_EXEMPTION(6);

    int status;

    private ActivityOrderStatusEnum(int size) {
        this.status = size;
    }

    public int getStatus() {
        return this.status;
    }
}
