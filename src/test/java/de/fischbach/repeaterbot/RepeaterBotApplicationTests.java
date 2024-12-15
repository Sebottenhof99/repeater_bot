package de.fischbach.repeaterbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
class RepeaterBotApplicationTests {
    @Test
    void contextLoads() throws InterruptedException, ExecutionException {
        List<Long> groupIds = List.of(1L, 2L, 3L, 4L, 5L);

        List<Long> unsuccessed = new ArrayList<>();
        var executorService = Executors.newSingleThreadScheduledExecutor();

        for (int i = 0; i < groupIds.size(); i++) {
            long groupId = groupIds.get(i);
            executorService.schedule(() -> {
                System.out.println(groupId);

            }, 2, TimeUnit.SECONDS).get();
        }



    }




}
