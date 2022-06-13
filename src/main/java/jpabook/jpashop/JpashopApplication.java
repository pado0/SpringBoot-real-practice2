package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import jpabook.jpashop.domain.Order;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	// bytebuddy lazy loading 이슈떄문에 조치하는 것이긴 하지만,
	// 사실 근본적으로 이방법은 맞지 않다.
	@Bean
	Hibernate5Module hibernate5Module(){
		Hibernate5Module hibernate5Module = new Hibernate5Module();
		// hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true); // json 생성 시점에 lazy loading을 해버림

		return hibernate5Module;
	}
}
