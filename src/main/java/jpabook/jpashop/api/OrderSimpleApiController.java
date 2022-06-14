package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// x to one 관계에 대해 조회해본다.
// order
// order -> member : order 와 멤버는 many to one
// order -> delivery : one to one 관계

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() { // 엔티티 직접 노출하면 발생 하는 문제를 알아보게 될 것
        List<Order> all = orderRepository.findAllByString(new OrderSearch()); // order에 member, delivery가 다 연관되어있는 상태에서 반환하고있다.

        for (Order order : all) {
            order.getMember().getName(); // 이렇게 get으로 조회를 해버리면 Lazy가 강제 초기화됨. 영속성 컨텍스트로 관리.
            order.getDelivery().getAddress();//
        }
        return all;
    }

    // 원하는 api 스펙에 맞추어 딱 필요한 응답만 개발
    // order / member / delivery 조회 쿼리가 나간
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2(){
        // Order 2개가 조회됨
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        // entity를 dto로 변환. 스트림으로 하나하나 처리
        // 루프를 돌때 첫 번째 주문서에 대한 order를 찾음
        // simple order 타고 들어갈때 멤버 / 딜리버리호출. 총 5개 쿼리가 조회됨
        return orders.stream().map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    // fetch join을 써서 쿼리 하나만 날라가도록 변환
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

//     엔티티를 dto로 변환하지 않고, dto로 그대로 쿼리 뽑기.
//     단, 화면에는 최적화 되어있지만 재사용성이 떨어진다.
//     v3와 v4에 trade off가 있다.
//     dto로 조회한 데이터는 비즈니스로직 수정 등이어렵다.
//     new 키워드로 일반 db에서 sql 짜듯 짜서 가져오는 것
//     생각보다 성능이 그렇게 좋지는 않다.
//     어쩌면 레포지토리 계층이 화면을 의존하고 있는것이나 마찬가지
//     그 레포지토리 밖에 못씀.
//     레퍼지토리는 엔티티 조회용임. dto를 조회하는게 적합하지 않음.
//     성능 최적화 쿼리용 별도 패키지를 뽑는 방법도 있음.


//    @GetMapping("/api/v4/simple-orders")
//    public List<SimpleOrderDto> orderV4(){
//       orderRepository.findOrderDtos()
//    }


    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private OrderStatus orderStatus;
        private LocalDateTime orderDate;
        private Address address; // 배송지 정보.


        // dto로 바꿔주는 과정에서 get 호출시 lazy가 초기화
        // 영속성 컨텍스트에 있으면 조회, 없으면 db에서 긁어옴
        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
