package com.example.mensajeria_aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;




@Component
public class OrdenListener {

    private static final Logger log = LoggerFactory.getLogger(OrdenListener.class);

    private final ProcesadorOrden procesador;
    private final SqsTemplate sqsTemplate;
    private final MensajeriaProperties props;
    private final Set<String> procesadas = ConcurrentHashMap.newKeySet();

    public OrdenListener(ProcesadorOrden procesador, SqsTemplate sqsTemplate, MensajeriaProperties props) {
        this.procesador = procesador;
        this.sqsTemplate = sqsTemplate;
        this.props = props;
    }

    @SqsListener(id = "procesador-ordenes", value = "${mensajeria.cola-ordenes}")
    public void escuchar(Orden orden) throws InterruptedException {
        log.info("Recibida orden {} de {}", orden.id(), orden.cliente());

        if (procesadas.contains(orden.id())) {
            log.warn("Orden {} duplicada, se ignora", orden.id());
            return;
        }
        try {
            procesador.procesar(orden);
            procesadas.add(orden.id());   // solo si terminó sin errores
        } catch (ErrorDefinitivo e) {
            log.error("Orden {} descartada: {}", orden.id(), e.getMessage());
            sqsTemplate.send(props.dlqOrdenes(), orden);
        }
        // ErrorTransitorio no se captura: el mensaje no se confirma y se reintenta
    }
}