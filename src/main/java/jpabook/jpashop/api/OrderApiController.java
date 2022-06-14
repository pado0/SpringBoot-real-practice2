package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Mod11Check;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public List<Order> ordersV1(){
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
}
