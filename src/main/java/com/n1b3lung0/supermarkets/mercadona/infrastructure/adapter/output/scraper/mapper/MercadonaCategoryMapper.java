package com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.mapper;

import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaLeafGroupDto;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaLevel1CategoryDto;
import com.n1b3lung0.supermarkets.mercadona.infrastructure.adapter.output.scraper.dto.MercadonaTopCategoryDto;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;
import java.util.UUID;

/** Maps Mercadona category DTOs → {@link RegisterCategoryCommand}. */
public class MercadonaCategoryMapper {

  public RegisterCategoryCommand toTopCommand(
      MercadonaTopCategoryDto dto, SupermarketId supermarketId) {
    return new RegisterCategoryCommand(
        dto.name(), String.valueOf(dto.id()), supermarketId.value(), "TOP", null, dto.order());
  }

  public RegisterCategoryCommand toSubCommand(
      MercadonaLevel1CategoryDto dto, SupermarketId supermarketId, String parentExternalId) {
    return new RegisterCategoryCommand(
        dto.name(),
        String.valueOf(dto.id()),
        supermarketId.value(),
        "SUB",
        null, // parentId UUID resolved by the sync handler after TOP categories are persisted
        dto.order());
  }

  public RegisterCategoryCommand toLeafCommand(
      MercadonaLeafGroupDto dto, SupermarketId supermarketId, UUID parentCategoryId) {
    return new RegisterCategoryCommand(
        dto.name(),
        String.valueOf(dto.id()),
        supermarketId.value(),
        "LEAF",
        parentCategoryId,
        dto.order());
  }
}
