package com.hotelbooking.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/error")
    public String throwError() {
        throw new RuntimeException("Simulated server error for Telegram test!");
    }

    @GetMapping("/log")
    public String logError() {
        logger.error("Test ERROR log from /log endpoint");
        return "Logged error!";
    }

    @GetMapping("/info")
    public String logInfo() {
        logger.info("Test INFO log from /info endpoint");
        return "Logged info!";
    }
}
