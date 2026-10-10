package com.example.mensajeria_aws;

import java.util.Map;
import org.springframework.stereotype.Service;
import io.awspring.cloud.sqs.listener.MessageListenerContainerRegistry;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

@Service
public class ColaAdminService {

    private static final String LISTENER_ORDENES = "procesador-ordenes";

    private final SqsAsyncClient sqs;
    private final MessageListenerContainerRegistry registry;

    public ColaAdminService(SqsAsyncClient sqs, MessageListenerContainerRegistry registry) {
        this.sqs = sqs;
        this.registry = registry;
    }

    private String url(String cola) {
        return sqs.getQueueUrl(r -> r.queueName(cola)).join().queueUrl();
    }

    public Map<String, Long> estado(String cola) {
        String url = url(cola);
        var attrs = sqs.getQueueAttributes(r -> r.queueUrl(url).attributeNames(
                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE)).join().attributes();
        return Map.of(
            "disponibles", Long.parseLong(attrs.get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)),
            "enProceso",   Long.parseLong(attrs.get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE)));
    }

    public String crear(String cola) {
        return sqs.createQueue(r -> r.queueName(cola)).join().queueUrl();
    }

    public void eliminar(String cola) {
        String url = url(cola);
        sqs.deleteQueue(r -> r.queueUrl(url)).join();
    }

    public void pausar() {
        var c = registry.getContainerById(LISTENER_ORDENES);
        if (c != null) c.stop();
    }

    public void reanudar() {
        var c = registry.getContainerById(LISTENER_ORDENES);
        if (c != null) c.start();
    }

    /** Mueve los mensajes de la DLQ a la cola de destino. Devuelve cuántos movió. */
    public int reprocesar(String dlq, String destino) {
        String dlqUrl = url(dlq);
        String destinoUrl = url(destino);
        int movidos = 0;
        while (true) {
            var mensajes = sqs.receiveMessage(r -> r.queueUrl(dlqUrl)
                    .maxNumberOfMessages(10).waitTimeSeconds(1)).join().messages();
            if (mensajes.isEmpty()) break;
            for (Message m : mensajes) {
                sqs.sendMessage(r -> r.queueUrl(destinoUrl).messageBody(m.body())).join();
                sqs.deleteMessage(r -> r.queueUrl(dlqUrl).receiptHandle(m.receiptHandle())).join();
                movidos++;
            }
        }
        return movidos;
    }
}