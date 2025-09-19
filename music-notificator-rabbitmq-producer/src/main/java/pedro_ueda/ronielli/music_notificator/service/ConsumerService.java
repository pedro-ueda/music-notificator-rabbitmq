package pedro_ueda.ronielli.music_notificator.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import pedro_ueda.ronielli.music_notificator.model.Music;

@Service
public class ConsumerService {

    @RabbitListener(queues = "${broker.queue.name}")
    public void receive(Music music) {
        System.out.println("Received message: " + music.getName());
    }
}
