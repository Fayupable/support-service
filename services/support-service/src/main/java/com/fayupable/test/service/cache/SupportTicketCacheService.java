package com.fayupable.test.service.cache;

import com.fayupable.test.dto.SupportTicketDto;
import com.fayupable.test.entity.SupportTicket;
import com.fayupable.test.mapper.SupportTicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketCacheService {

    private static final String SUPPORT_TICKET_KEY = "support:ticket:";
    private static final String ALL_TICKETS_KEY = "support:tickets:all";

    private final RedisTemplate<String, Object> redisTemplate;
    private final SupportTicketMapper supportTicketMapper;

    private long determineTTL(SupportTicket ticket) {
        switch (ticket.getStatus()) {
            case PENDING:
            case IN_PROGRESS:
                return TimeUnit.HOURS.toSeconds(6);
            case CLOSED:
            case RESOLVED:
                return TimeUnit.MINUTES.toSeconds(30);
            default:
                return TimeUnit.HOURS.toSeconds(1);
        }
    }

    private long determineTTLDto(SupportTicketDto ticket) {
        switch (ticket.getStatus()) {
            case PENDING:
            case IN_PROGRESS:
                return TimeUnit.HOURS.toSeconds(6);
            case CLOSED:
            case RESOLVED:
                return TimeUnit.MINUTES.toSeconds(30);
            default:
                return TimeUnit.HOURS.toSeconds(1);
        }
    }


    public void cacheTicketDto(SupportTicketDto ticket) {
        try {
            String key = SUPPORT_TICKET_KEY + ticket.getSupportTicketId();
            long ttl = determineTTLDto(ticket);

            redisTemplate.opsForValue().set(key, ticket, ttl, TimeUnit.SECONDS);
            log.info("Ticket cached: {} (TTL: {} seconds)", key, ttl);
        } catch (Exception e) {
            log.error("Failed to cache ticket: {}", ticket.getSupportTicketId(), e);
        }
    }


    public void cacheTicket(SupportTicket ticket) {
        try {
            SupportTicketDto dto = supportTicketMapper.fromSupportTicket(ticket);
            String key = SUPPORT_TICKET_KEY + ticket.getSupportTicketId();
            long ttl = determineTTL(ticket);

            redisTemplate.opsForValue().set(key, dto, ttl, TimeUnit.SECONDS);
            log.info("Ticket cached: {} (TTL: {} seconds)", key, ttl);
        } catch (Exception e) {
            log.error("Failed to cache ticket: {}", ticket.getSupportTicketId(), e);
        }
    }


    public Optional<SupportTicketDto> getCachedTicket(UUID ticketId) {
        try {
            String key = SUPPORT_TICKET_KEY + ticketId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("Ticket found in cache: {}", key);
                return Optional.of((SupportTicketDto) cached);
            }
        } catch (Exception e) {
            log.error("Failed to fetch ticket from cache: {}", ticketId, e);
        }
        return Optional.empty();
    }

    public void cacheAllTickets(List<SupportTicketDto> tickets) {
        try {
            redisTemplate.opsForValue().set(ALL_TICKETS_KEY, tickets, 3, TimeUnit.HOURS);
            log.info("All support tickets cached (TTL: 3 hours)");
        } catch (Exception e) {
            log.error("Failed to cache all tickets", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Optional<List<SupportTicketDto>> getCachedAllTickets() {
        try {
            Object cached = redisTemplate.opsForValue().get(ALL_TICKETS_KEY);
            if (cached != null) {
                log.info("All tickets found in cache.");
                return Optional.of((List<SupportTicketDto>) cached);
            }
        } catch (Exception e) {
            log.error("Failed to fetch all tickets from cache", e);
        }
        return Optional.empty();
    }

    public void evictTicket(UUID ticketId) {
        try {
            String key = SUPPORT_TICKET_KEY + ticketId;
            redisTemplate.delete(key);
            log.info("Ticket evicted from cache: {}", key);
        } catch (Exception e) {
            log.error("Failed to evict ticket from cache: {}", ticketId, e);
        }
    }

    public void evictAllTickets() {
        try {
            redisTemplate.delete(ALL_TICKETS_KEY);
            log.info("All tickets list evicted from cache");
        } catch (Exception e) {
            log.error("Failed to evict all tickets from cache", e);
        }
    }
}