package com.example.mensajeria_aws;

import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController; 
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class EstadoController {

    private final SqsAsyncClient sqs;
    private final MensajeriaProperties props;

    public EstadoController(SqsAsyncClient sqs, MensajeriaProperties props) {
        this.sqs = sqs;
        this.props = props;
    }

    @GetMapping("/estado")
    public Map<String, Long> estado() {
        return Map.of(
            "principal", atributo(props.colaOrdenes(), QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES),
            "enVuelo",   atributo(props.colaOrdenes(), QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE),
            "dlq",       atributo(props.colaOrdenes(), QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
        );
    }

    private long atributo(String cola, QueueAttributeName nombre) {
        String url = sqs.getQueueUrl(r -> r.queueName(cola)).join().queueUrl();
        String valor = sqs.getQueueAttributes(r -> r.queueUrl(url).attributeNames(nombre))
                .join().attributes().get(nombre);
        return Long.parseLong(valor);
    }
}