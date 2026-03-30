package com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.mapper;

import com.n1b3lung0.supermarkets.basket.domain.model.Basket;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketId;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItem;
import com.n1b3lung0.supermarkets.basket.domain.model.BasketItemId;
import com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.entity.BasketEntity;
import com.n1b3lung0.supermarkets.basket.infrastructure.adapter.output.persistence.entity.BasketItemEntity;
import java.util.ArrayList;

/** Maps between Basket aggregate and BasketEntity. */
public class BasketPersistenceMapper {

  public BasketEntity toEntity(Basket basket) {
    var entity = new BasketEntity();
    entity.setId(basket.getId().value());
    entity.setName(basket.getName());
    entity.setCreatedAt(basket.getCreatedAt());
    entity.setUpdatedAt(basket.getUpdatedAt());

    var itemEntities = new ArrayList<BasketItemEntity>();
    for (var item : basket.getItems()) {
      var itemEntity = new BasketItemEntity();
      itemEntity.setId(item.getId().value());
      itemEntity.setBasket(entity);
      itemEntity.setProductName(item.getProductName());
      itemEntity.setQuantity(item.getQuantity());
      itemEntities.add(itemEntity);
    }
    entity.setItems(itemEntities);
    return entity;
  }

  public Basket toDomain(BasketEntity entity) {
    var items =
        entity.getItems().stream()
            .map(
                i ->
                    BasketItem.reconstitute(
                        BasketItemId.of(i.getId()), i.getProductName(), i.getQuantity()))
            .toList();
    return Basket.reconstitute(
        BasketId.of(entity.getId()),
        entity.getName(),
        items,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
