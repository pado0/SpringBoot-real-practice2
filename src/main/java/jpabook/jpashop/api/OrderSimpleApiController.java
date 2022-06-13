package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
