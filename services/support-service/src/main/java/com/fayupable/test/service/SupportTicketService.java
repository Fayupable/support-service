package com.fayupable.test.service;

import com.fayupable.test.client.image.ImageDto;
import com.fayupable.test.client.image.ImageResponse;
import com.fayupable.test.client.image.ImageServiceClient;
import com.fayupable.test.client.user.UserClient;
import com.fayupable.test.dto.SupportTicketDto;
import com.fayupable.test.dto.TicketResolutionDto;
import com.fayupable.test.entity.SupportTicket;
import com.fayupable.test.entity.TicketDetails;
import com.fayupable.test.entity.TicketResolution;
import com.fayupable.test.enums.SupportStatus;
import com.fayupable.test.exception.InvalidTicketDetailsException;
import com.fayupable.test.exception.UnauthorizedException;
import com.fayupable.test.kafka.SupportTicketConfirmation;
import com.fayupable.test.kafka.SupportTicketProducer;
import com.fayupable.test.kafka.SupportTicketResolutionConfirmation;
import com.fayupable.test.mapper.SupportTicketMapper;
import com.fayupable.test.repository.ISupportTicketRepository;
import com.fayupable.test.request.*;
import com.fayupable.test.service.cache.SupportTicketCacheService;
import com.fayupable.test.util.RoleUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketService implements ISupportTicketService {
    private final ISupportTicketRepository supportTicketRepository;
    private final SupportTicketMapper supportTicketMapper;
    private final UserClient userClient;
    private final SupportTicketProducer supportTicketProducer;
    private final HttpServletRequest httpServletRequest;
    private final ImageServiceClient imageServiceClient;
    private final RoleUtil roleUtil;
    private final SupportTicketCacheService supportTicketCacheService;


    @Override
    public List<SupportTicketDto> getAllSupportTickets() {
        validateAndGetStaffUserIdTest();
        List<SupportTicket> tickets = supportTicketRepository.findAll();
        return tickets.stream()
                .map(supportTicketMapper::fromSupportTicket)
                .toList();
    }


    @Override
    public List<SupportTicketDto> getAllSupportTicketsWithRedis() {
        validateAndGetStaffUserIdTest();
        return supportTicketCacheService.getCachedAllTickets()
                .orElseGet(() -> {
                    List<SupportTicket> tickets = supportTicketRepository.findAll();
                    List<SupportTicketDto> ticketDtos = tickets.stream()
                            .map(supportTicketMapper::fromSupportTicket)
                            .toList();
                    supportTicketCacheService.cacheAllTickets(ticketDtos);
                    return ticketDtos;
                });
    }


    @Override
    @Transactional
    public SupportTicketDto addSupportTicket(AddSupportTicketRequest addSupportTicketRequest, MultipartFile attachment) {
        validateUserAccess(addSupportTicketRequest.getUserId());
        UUID attachmentId = processAttachment(attachment, addSupportTicketRequest.getUserId());
        addSupportTicketRequest.setAttachmentId(attachmentId);

        return Optional.of(addSupportTicketRequest)
                .map(this::createSupportTicket)
                .map(supportTicketRepository::save)
                .map(supportTicketMapper::fromSupportTicket)
                .map(dto -> {
                    sendSupportConfirmation(dto);
                    supportTicketCacheService.cacheTicketDto(dto);
                    return dto;
                })
                .orElseThrow(() -> new InvalidTicketDetailsException("Invalid ticket details"));
    }

    private SupportTicket createSupportTicket(AddSupportTicketRequest addSupportTicketRequest) {
        SupportTicket supportTicket = new SupportTicket();
        supportTicket.setUserId(addSupportTicketRequest.getUserId());
        supportTicket.setStatus(addSupportTicketRequest.getStatus());
        supportTicket.setPriority(addSupportTicketRequest.getPriority());
        Set<TicketDetails> ticketDetails = createTicketDetailsList(addSupportTicketRequest.getTicketDetails(), supportTicket);
        Set<TicketResolution> ticketResolutions = createTicketResolutionList(addSupportTicketRequest.getResolutions(), supportTicket);

        supportTicket.setTicketDetails(ticketDetails);
        supportTicket.setResolutions(ticketResolutions);
        return supportTicket;
    }

    private Set<TicketDetails> createTicketDetailsList(List<AddTicketDetailsRequest> addTicketDetailsRequests, SupportTicket supportTicket) {
        return addTicketDetailsRequests.stream()
                .map(request -> {
                    TicketDetails ticketDetails = new TicketDetails();
                    ticketDetails.setDescription(request.getDescription());
                    ticketDetails.setTicket(supportTicket);
                    return ticketDetails;
                })
                .collect(Collectors.toSet());
    }

    private Set<TicketResolution> createTicketResolutionList(List<AddTicketResolutionRequest> addTicketResolutionRequests, SupportTicket supportTicket) {
        return addTicketResolutionRequests.stream()
                .map(request -> {
                    TicketResolution ticketResolution = new TicketResolution();
                    ticketResolution.setResolutionMessage(request.getResolutionMessage());
                    ticketResolution.setTicket(supportTicket);
                    return ticketResolution;
                })
                .collect(Collectors.toSet());
    }


    private void sendSupportConfirmation(SupportTicketDto supportTicketDto) {
        String email = userClient.getEmailByUserId(supportTicketDto.getUserId());
        SupportTicketConfirmation supportTicketConfirmation = new SupportTicketConfirmation();
        supportTicketConfirmation.setEmail(email);
        supportTicketConfirmation.setSupportTicketId(supportTicketDto.getSupportTicketId());
        supportTicketConfirmation.setStatus(SupportStatus.PENDING);
        supportTicketProducer.sendConfirmation(supportTicketConfirmation);
    }


    @Transactional
    @Override
    public SupportTicketDto updateSupportTicketForUser(UUID supportTicketId, UpdateSupportTicketRequest updateRequest) {
        validateUserAccess(updateRequest.getUserId());
        return Optional.of(validateTicketOwnership(supportTicketId, updateRequest.getUserId()))
                .map(ticket -> updateSupportTicketHelper(ticket, updateRequest))
                .map(supportTicketRepository::save)
                .map(supportTicketMapper::fromSupportTicket)
                .map(dto -> {
                    supportTicketCacheService.cacheTicketDto(dto);
                    return dto;
                })
                .orElseThrow(() -> new InvalidTicketDetailsException("Failed to update ticket"));
    }

    private SupportTicket updateSupportTicketHelper(SupportTicket ticket, UpdateSupportTicketRequest updateRequest) {
        ticket.setPriority(updateRequest.getPriority());
        ticket.getTicketDetails().clear();
        Set<TicketDetails> newDetails = createTicketDetailsListHelperForUpdate(updateRequest.getTicketDetails(), ticket);
        ticket.getTicketDetails().addAll(newDetails);

        return ticket;
    }

    private Set<TicketDetails> createTicketDetailsListHelperForUpdate(List<UpdateTicketDetailsRequest> updateRequests,
                                                                      SupportTicket supportTicket) {
        return updateRequests.stream()
                .map(request -> {
                    TicketDetails details = new TicketDetails();
                    details.setDescription(request.getDescription());
                    details.setTicket(supportTicket);
                    return details;
                })
                .collect(Collectors.toSet());
    }

    private SupportTicket validateTicketOwnership(UUID supportTicketId, UUID userId) {
        SupportTicket existingTicket = supportTicketRepository.findById(supportTicketId)
                .orElseThrow(() -> new InvalidTicketDetailsException("Ticket not found"));
        if (!existingTicket.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own tickets");
        }
        return existingTicket;
    }


    @Override
    @Transactional
    public SupportTicketDto updateSupportTicketForStaff(UUID supportTicketId, UpdateSupportTicketRequest updateRequest) {
        validateAndGetStaffUserIdTest();
        return processTicketUpdate(supportTicketId, updateRequest);
    }

    private SupportTicketDto processTicketUpdate(UUID supportTicketId, UpdateSupportTicketRequest updateRequest) {
        return Optional.of(findTicketById(supportTicketId))
                .map(ticket -> updateSupportTicketHelperForStaff(ticket, updateRequest))
                .map(supportTicketRepository::save)
                .map(supportTicketMapper::fromSupportTicket)
                .map(dto -> {
                    supportTicketCacheService.cacheTicketDto(dto);
                    return handleResolutionIfResolved(dto);
                })
                .orElseThrow(() -> new InvalidTicketDetailsException("Failed to update ticket"));
    }

    private SupportTicket findTicketById(UUID supportTicketId) {
        return supportTicketRepository.findById(supportTicketId)
                .orElseThrow(() -> new InvalidTicketDetailsException("Ticket not found"));
    }

    private SupportTicket updateSupportTicketHelperForStaff(SupportTicket ticket, UpdateSupportTicketRequest updateRequest) {
        updateTicketBasicInfo(ticket, updateRequest);
//        updateTicketDetails(ticket, updateRequest.getTicketDetails());
        updateTicketResolutions(ticket, updateRequest.getResolutions());
        return ticket;
    }

    private void updateTicketBasicInfo(SupportTicket ticket, UpdateSupportTicketRequest updateRequest) {
        ticket.setStatus(updateRequest.getStatus());
        ticket.setPriority(updateRequest.getPriority());
    }

    private void updateTicketDetails(SupportTicket ticket, List<UpdateTicketDetailsRequest> detailsRequests) {
        ticket.getTicketDetails().clear();
        Set<TicketDetails> newDetails = createTicketDetailsListHelperForUpdate(detailsRequests, ticket);
        ticket.getTicketDetails().addAll(newDetails);
    }

    private void updateTicketResolutions(SupportTicket ticket, List<UpdateTicketResolutionRequest> resolutions) {
        if (resolutions != null && !resolutions.isEmpty()) {
            Set<TicketResolution> newResolutions = createTicketResolutionListHelperForUpdate(resolutions, ticket);
            ticket.getResolutions().addAll(newResolutions);
        }
    }

    private SupportTicketDto handleResolutionIfResolved(SupportTicketDto dto) {
        if (SupportStatus.RESOLVED.equals(dto.getStatus())) {
            sendSupportResolution(dto);
        }
        return dto;
    }

    private void sendSupportResolution(SupportTicketDto supportTicketDto) {
        SupportTicketResolutionConfirmation confirmation = createResolutionConfirmation(supportTicketDto);
        supportTicketProducer.sendResolutionConfirmation(confirmation);
    }

    private SupportTicketResolutionConfirmation createResolutionConfirmation(SupportTicketDto supportTicketDto) {
        String email = userClient.getEmailByUserId(supportTicketDto.getUserId());
        String resolutionMessage = extractResolutionMessage(supportTicketDto);

        return SupportTicketResolutionConfirmation.builder()
                .email(email)
                .supportTicketId(supportTicketDto.getSupportTicketId())
                .status(SupportStatus.RESOLVED)
                .resolutionMessage(resolutionMessage)
                .resolvedAt(LocalDateTime.now())
                .build();
    }

    private String extractResolutionMessage(SupportTicketDto supportTicketDto) {
        return supportTicketDto.getResolutions()
                .stream()
                .map(TicketResolutionDto::getResolutionMessage)
                .findFirst()
                .orElse("");
    }

    private Set<TicketResolution> createTicketResolutionListHelperForUpdate(List<UpdateTicketResolutionRequest> updateRequests,
                                                                            SupportTicket supportTicket) {
        return updateRequests.stream()
                .map(request -> {
                    TicketResolution resolution = new TicketResolution();
                    resolution.setResolutionMessage(request.getResolutionMessage());
                    resolution.setTicket(supportTicket);
                    resolution.setClosedAt(LocalDateTime.now());
                    return resolution;
                })
                .collect(Collectors.toSet());
    }


    @Transactional
    @Override
    public TicketResolutionDto updateTicketResolution(UUID supportTicketId, UpdateTicketResolutionRequest updateRequest) {
        validateAndGetStaffUserIdTest();
        return Optional.of(findTicketById(supportTicketId))
                .map(ticket -> updateTicketResolutionHelper(ticket, updateRequest))
                .map(supportTicketRepository::save)
                .map(ticket -> findUpdatedResolution(ticket, updateRequest.getTicketResolutionId()))
                .map(this::mapToTicketResolutionDto)
                .map(dto -> {
                    supportTicketCacheService.cacheTicketDto(supportTicketMapper.fromSupportTicket(findTicketById(supportTicketId)));
                    return dto;
                })
                .orElseThrow(() -> new InvalidTicketDetailsException("Failed to update ticket resolution"));
    }

    private SupportTicket updateTicketResolutionHelper(SupportTicket ticket, UpdateTicketResolutionRequest updateRequest) {
        Set<TicketResolution> resolutions = ticket.getResolutions();
        TicketResolution resolutionToUpdate = resolutions.stream()
                .filter(resolution -> resolution.getResolutionId().equals(updateRequest.getTicketResolutionId()))
                .findFirst()
                .orElseThrow(() -> new InvalidTicketDetailsException("Resolution not found"));

        resolutionToUpdate.setResolutionMessage(updateRequest.getResolutionMessage());
        resolutionToUpdate.setClosedAt(LocalDateTime.now());

        return ticket;
    }

    private TicketResolution findUpdatedResolution(SupportTicket ticket, UUID resolutionId) {
        return ticket.getResolutions().stream()
                .filter(resolution -> resolution.getResolutionId().equals(resolutionId))
                .findFirst()
                .orElseThrow(() -> new InvalidTicketDetailsException("Updated resolution not found"));
    }

    private TicketResolutionDto mapToTicketResolutionDto(TicketResolution resolution) {
        TicketResolutionDto dto = new TicketResolutionDto();
        dto.setResolutionMessage(resolution.getResolutionMessage());
        dto.setClosedAt(resolution.getClosedAt());
        return dto;
    }


    private UUID processAttachment(MultipartFile attachment, UUID userId) {
        if (attachment != null && !attachment.isEmpty()) {
            try {
                log.info("Uploading attachment for user: {}", userId);
                List<MultipartFile> files = List.of(attachment);

                ImageResponse imageResponse = imageServiceClient.addImages(files, userId);
                List<ImageDto> uploadedImages = imageResponse.getData();


                if (!uploadedImages.isEmpty()) {
                    UUID imageId = uploadedImages.get(0).getImageId();
                    log.info("Attachment uploaded successfully with ID: {}", imageId);
                    return imageId;
                }
            } catch (Exception e) {
                log.error("Failed to upload attachment for user: {}", userId, e);
                throw new RuntimeException("Failed to upload attachment", e);
            }
        }
        return null;
    }

    private void validateUserAccess(UUID userId) {
        if (userId == null) {
            throw new UnauthorizedException("User ID is missing in request");
        }

        if (!roleUtil.hasRoleOrHigher(userId, "ROLE_USER")) {
            throw new UnauthorizedException("Only users or higher roles can create tickets");
        }
    }

    private void validateAndGetStaffUserId() {
        String userId = httpServletRequest.getHeader("userId");
        if (userId == null) {
            throw new UnauthorizedException("User ID not found in header");
        }

        UUID userUUID = UUID.fromString(userId);
        if (!roleUtil.hasRoleOrHigher(userUUID, "ROLE_SUPPORT_STAFF")) {
            throw new UnauthorizedException("Only staff members can perform this action");
        }
    }

    private void validateAndGetStaffUserIdTest() {
        UUID userUUID = getUserIdFromHeader();
        if (!roleUtil.hasRoleOrHigherTest(userUUID, "ROLE_SUPPORT_STAFF")) {
            throw new UnauthorizedException("Only staff members can perform this action");
        }
    }

    private UUID getUserIdFromHeader() {
        String userId = httpServletRequest.getHeader("userId");
        if (userId == null) {
            throw new UnauthorizedException("User ID not found in header");
        }
        return UUID.fromString(userId);
    }

    private void validateAndGetUserUserId() {
        String userId = httpServletRequest.getHeader("userId");
        if (userId == null) {
            throw new UnauthorizedException("User ID not found in header");
        }

        UUID userUUID = UUID.fromString(userId);
        if (!roleUtil.hasRoleOrHigher(userUUID, "ROLE_USER")) {
            throw new UnauthorizedException("Only users or higher roles can perform this action");
        }
    }

    private boolean isStaffRole(String role) {
        return role.equals("ROLE_ADMIN") ||
                role.equals("ROLE_MODERATOR") ||
                role.equals("ROLE_SUPPORT_STAFF");
    }

    private boolean isStaffMember(UUID userId) {
        try {
            String userRole = userClient.getRoleByUserId(userId);
            return userRole != null && (
                    userRole.equals("ROLE_ADMIN") || userRole.equals("ROLE_MODERATOR") || userRole.equals("ROLE_SUPPORT_STAFF")
            );
        } catch (Exception e) {
            log.error("Error checking user role for userId: {}", userId, e);
            return false;
        }
    }

    private boolean isUserStaff(UUID userId) {
        try {
            String userRole = userClient.getRoleByUserId(userId);
            return "ROLE_STAFF".equals(userRole);
        } catch (Exception e) {
            log.error("Error checking user role for userId: {}", userId, e);
            return false;
        }
    }

    private boolean isUser(UUID userId) {
        try {
            String userRole = userClient.getRoleByUserId(userId);
            return "ROLE_USER".equals(userRole);
        } catch (Exception e) {
            log.error("Error checking user role for userId: {}", userId, e);
            return false;
        }
    }

}
