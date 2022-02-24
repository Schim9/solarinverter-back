package lu.kaminski.inverter.controler;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShutdownController implements ApplicationContextAware {
    
    private ApplicationContext context;
    
    @PostMapping("/shutdownContext")
    public ResponseEntity<?> shutdownContext() {
        ((ConfigurableApplicationContext) context).close();
        return ResponseEntity.ok("Solar-back will shut done gracefully");
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;

    }
}