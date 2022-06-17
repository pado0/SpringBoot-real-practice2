package jpabook.jpashop.repository.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;
    public List<OrderQueryDto> findOrderQueryDtos(){
        List<OrderQueryDto> result = findOrders(); // 1) 쿼리 1번, orders 2개 찾음 (1)

        // findOrders에서 orderQueryDto의 값을 못채움
        result.forEach(o ->{ // 2) 루프를 두 번 돎

            // 루프를 돌면서 orderitems를 찾아서 채워줌
            List<OrderItemsQueryDto> orderItems = findOrderItems(o.getOrderId()); // 쿼리가 두번 나감 (N)
            o.setOrderItems(orderItems); // 가져온 orderitem 셋
        });
        return result;
    }

    private List<OrderItemsQueryDto> findOrderItems(Long orderId) {
        return em.createQuery("select " +
                        " new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemsQueryDto.class)
                .setParameter("orderId", orderId).getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    // 이전 코드의 단점은 루프를 돌았다는 것
    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders(); // to one 먼저조회

        // to one에서 얻은 식별자 아이디로 to Many 관계인 OrderItem을 한꺼번에 조회
        List<Long> orderIds = result.stream().map(o -> o.getOrderId()).collect(Collectors.toList());

        List<OrderItemsQueryDto> orderItems =
                     em.createQuery("select " +
                        " new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds", OrderItemsQueryDto.class)
                            .setParameter("orderIds", orderIds)
                             .getResultList();

        // 메모리에서 맵을 가져와 매칭해서 세팅함. 쿼리가 덜나온다. 총 쿼리가 2 번으로 줄어든다
        // 메모리 (그냥 map 컬렉션) 에다가 꽂아놓는 것
        Map<Long, List<OrderItemsQueryDto>> orderItemMap =
            orderItems.stream().collect(Collectors.groupingBy(orderItemsQueryDto -> orderItemsQueryDto.getOrderId()));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return result;
    }
}
