package iuh.fit.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "userservice")
public interface UserClient {
    // Gọi API theo username
    @GetMapping("/api/user/{username}/exists")
    boolean checkUserExists(@PathVariable("username") String username);

    @GetMapping("/api/user/bank-account/check")
    boolean hasLinkedBankAccount(@RequestParam String username);
}
