package com.example.mensajeria_aws;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final ColaAdminService admin;
    private final MensajeriaProperties props;

    public AdminController(ColaAdminService admin, MensajeriaProperties props) {
        this.admin = admin;
        this.props = props;
    }

    @GetMapping("/colas/{cola}")
    public Map<String, Long> estado(@PathVariable String cola) {
        return admin.estado(cola);
    }

    @PostMapping("/colas/{cola}")
    public Map<String, String> crear(@PathVariable String cola) {
        return Map.of("cola", cola, "url", admin.crear(cola));
    }

    @DeleteMapping("/colas/{cola}")
    public String eliminar(@PathVariable String cola) {
        admin.eliminar(cola);
        return "Cola eliminada";
    }

    @PostMapping("/listener/pausar")
    public String pausar() {
        admin.pausar();
        return "Listener pausado";
    }

    @PostMapping("/listener/reanudar")
    public String reanudar() {
        admin.reanudar();
        return "Listener reanudado";
    }

    @PostMapping("/dlq/reprocesar")
    public Map<String, Integer> reprocesar() {
        return Map.of("movidos", admin.reprocesar(props.dlqOrdenes(), props.colaOrdenes()));
    }
}