package com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.mapper;

import com.n1b3lung0.supermarkets.carrefour.infrastructure.adapter.output.scraper.dto.CarrefourCategoryNodeDto;
import com.n1b3lung0.supermarkets.category.application.dto.RegisterCategoryCommand;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketId;

/** Maps Carrefour category node DTOs to {@link RegisterCategoryCommand}. */
public class CarrefourCategoryMapper {

  public RegisterCategoryCommand toTopCommand(
      CarrefourCategoryNodeDto dto, SupermarketId supermarketId) {
    return new RegisterCategoryCommand(
        dto.localizedTitle(), dto.uid(), supermarketId.value(), "TOP", null, 0);
  }

  public RegisterCategoryCommand toSubCommand(
      CarrefourCategoryNodeDto dto, SupermarketId supermarketId, String parentExternalId) {
    return new RegisterCategoryCommand(
        dto.localizedTitle(), dto.uid(), supermarketId.value(), "SUB", parentExternalId, 0);
  }
}
