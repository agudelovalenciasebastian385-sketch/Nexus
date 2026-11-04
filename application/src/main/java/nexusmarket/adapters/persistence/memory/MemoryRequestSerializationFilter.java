package nexusmarket.adapters.persistence.memory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Attends the API requests of the memory profile one at a time. All the requests share the same mutable
 * objects, and @Version cannot protect them because there are no stale copies to detect. Serializing the
 * whole request also covers the response mappers, which read those objects after the transaction.
 */
@Component
@Profile("memory")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MemoryRequestSerializationFilter extends OncePerRequestFilter {

    private final ReentrantLock lock = new ReentrantLock(true);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        lock.lock();
        try {
            filterChain.doFilter(request, response);
        } finally {
            lock.unlock();
        }
    }
}
