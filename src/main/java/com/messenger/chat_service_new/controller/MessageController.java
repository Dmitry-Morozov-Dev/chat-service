package com.messenger.chat_service_new.controller;

import com.messenger.chat_service_new.modelHelper.DTO.MessageDTO;
import com.messenger.chat_service_new.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/chats")
public class MessageController {

    private final MessageService messageService;

    @Value("${app.pagination.default-limit}")
    private int defaultLimit;

    @Value("${app.pagination.max-limit}")
    private long maxLimit;

    @GetMapping("/{chat_id}/messages")
    public Mono<ResponseEntity<PagedModel<EntityModel<MessageDTO>>>> getMessages(
            @PathVariable("chat_id") UUID chatId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(defaultValue = "", required = false) String bucketMonth,
            @RequestParam(defaultValue = "", required = false) UUID after,
            @RequestParam(defaultValue = "", required = false) UUID before,
            @RequestParam(defaultValue = "", required = false) UUID around) {

        int finalLimit = limit != null ? limit : defaultLimit;

        return messageService.getMessages(chatId, finalLimit, bucketMonth, after, before, around)
                .flatMap(messages -> {
                    if (messages.isEmpty()) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }

                    PagedModel<EntityModel<MessageDTO>> pagedModel = PagedModel.of(
                            messages.stream().map(EntityModel::of).toList(),
                            new PagedModel.PageMetadata(finalLimit, 0, messages.size())
                    );

                    String base = "/chats/" + chatId + "/messages?limit=" + finalLimit +
                            (bucketMonth.isBlank() ? "" : "&bucketMonth=" + bucketMonth);

                    String selfParams =
                            after != null ? "&after=" + after :
                                    before != null ? "&before=" + before :
                                            around != null ? "&around=" + around : "";

                    pagedModel.add(Link.of(base + selfParams, IanaLinkRelations.SELF));

                    if (!messages.isEmpty()) {
                        UUID lastId = messages.getLast().getMessageId();
                        pagedModel.add(Link.of(base + "&after=" + lastId, IanaLinkRelations.NEXT));
                    }

                    if (!messages.isEmpty()) {
                        UUID firstId = messages.getFirst().getMessageId();
                        pagedModel.add(Link.of(base + "&before=" + firstId, IanaLinkRelations.PREV));
                    }

                    return Mono.just(ResponseEntity.ok(pagedModel))
                            .doOnSuccess(e -> log.debug("Success"))
                            .doOnError(e -> log.error(e.getMessage()));
                });
    }

    @GetMapping("/{chat_id}/messages/search")
    public Mono<ResponseEntity<PagedModel<EntityModel<MessageDTO>>>> searchMessages(
            @PathVariable("chat_id") UUID chatId,
            @RequestParam String query,
            @RequestParam(required = false) Long offset,
            @RequestParam(required = false) Long limit) {

        long finalLimit = limit != null ? limit : defaultLimit;
        long finalOffset = offset != null ? offset : 0;

        return messageService.searchMessages(chatId, query.trim(), finalOffset, finalLimit)
                .flatMap(messages -> {
                    if (messages.isEmpty()) {
                        return Mono.just(ResponseEntity.ok(PagedModel.empty()));
                    }

                    PagedModel<EntityModel<MessageDTO>> pagedModel = PagedModel.of(
                            messages.stream().map(EntityModel::of).toList(),
                            new PagedModel.PageMetadata(finalLimit, 0, messages.size())
                    );

                    pagedModel.add(Link.of(
                            "/chats/" + chatId + "/messages/search?query=" + query +
                                    "&limit=" + finalLimit + "&offset=" + finalOffset,
                            IanaLinkRelations.SELF
                    ));

                    pagedModel.add(Link.of(
                            "/chats/" + chatId + "/messages/search?query=" + query +
                                    "&limit=" + finalLimit + "&offset=" + (finalOffset + finalLimit),
                            IanaLinkRelations.NEXT
                    ));

                    return Mono.just(ResponseEntity.ok(pagedModel));
                });
    }
}
