package jpabook.jpashop.repository.simplequery;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
@Data
// 레퍼지토리용 DTO를 하나 만든다
public class OrderSimpleQueryDto {
        private Long orderId;
        private String name;
        private OrderStatus orderStatus;
        private LocalDateTime orderDate;
        private Address address; // 배송지 정보.


        // dto로 바꿔주는 과정에서 get 호출시 lazy가 초기화
        // 영속성 컨텍스트에 있으면 조회, 없으면 db에서 긁어옴
        public OrderSimpleQueryDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

    }
}
