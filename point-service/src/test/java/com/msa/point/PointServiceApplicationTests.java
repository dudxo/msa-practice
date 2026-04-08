package com.msa.point;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Kafka 연결 필요 — 통합 테스트 시에만 실행")
class PointServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
