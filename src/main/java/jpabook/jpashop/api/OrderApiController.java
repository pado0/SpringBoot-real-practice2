package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Mod11Check;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    // [총정리]
    // orderItems와 item 정보를 출력하고싶어
    // hibernate5module 기본설정이 레이지라서 프록시인애를 데이터를 안뿌려서
    // 강제초기화를 해서 아이템 정보를 넣어주고 뿌릴 수 있도록 함
    // 양방향 관계는 jsonIgnore를 꼭 걸어야함

    // 엔티티 직접노출이므로 사용하지 않기
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // 강제 lazy 초기화
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();

            //orderItems 강제 초기화. orderItem, 내부에 item도 이름을 얻어와 초기화
            orderItems.stream().forEach(o -> o.getItem().getName());

        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;


        // Dto 안에 entity가 있으면 안됨
        // 현재는 orderItems 엔티티가 그대로 있음
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            //orderItems = order.getOrderItems(); // 엔티티라 출력되지 않고있음.
            //order.getOrderItems().stream().forEach(o -> o.getItem().getName());
            //프록시 초기화해서 다시 호출
            //orderItems = order.getOrderItems(); // orderItems 엔티티 그대로 반환중. 다 dto로 바꾸기

            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }

        // orderItem에도 노출하고자 하는 파라미터만 설정한 dto 만들어주기
        // orderItem에서 상품 명만 필요
        @Getter
        public static class OrderItemDto{

            private String itemName;
            private int orderPrice;
            private int count;

            public OrderItemDto(OrderItem orderItem) {
                itemName = orderItem.getItem().getName();
                orderPrice = orderItem.getOrderPrice();
                count = orderItem.getCount();
            }
        }
    }
}

