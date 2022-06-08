package babybeb.usersusedbookstore.api;

import babybeb.usersusedbookstore.api.dto.CreateItemRequest;
import babybeb.usersusedbookstore.api.dto.CreateItemResponse;
import babybeb.usersusedbookstore.api.dto.ItemDto;
import babybeb.usersusedbookstore.api.dto.UpdateItemRequest;
import babybeb.usersusedbookstore.api.dto.UpdateItemResponse;
import babybeb.usersusedbookstore.domain.Item;
import babybeb.usersusedbookstore.api.dto.ItemResponse;
import babybeb.usersusedbookstore.service.ItemService;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
//
//    @PostMapping("/items")
//    public CreateItemResponse saveItem(@RequestBody CreateItemRequest request) {
//
//        Item item = new Item(request.getBook(),request.getItemPrice(), request.getItemCondition(), request.getCreateDate());
//
//        Long id = itemService.saveItem(item);
//
//        return new CreateItemResponse(id);
//    }
    
//    @PutMapping("/items/{item_id}")
//    public UpdateItemResponse updateMemberV2(
//        @PathVariable("item_id") Long itemId,
//        @RequestBody @Valid UpdateItemRequest request) {
//
//        itemService.updateItem(itemId, request.getName());
//        Item findItem = itemService.findById(itemId);
//        return new UpdateItemResponse(findMember.getId(), findMember.getName());
//    }
    
    @GetMapping("/items")
    public Result findItems() {
        
        List<Item> findItems = itemService.findItems();
        List<ItemDto> collect = findItems.stream()
            .map(i -> new ItemDto(i.getBook(), i.getItemPrice(), i.getItemCondition(), i.getCreateDate(), i.getDealStatus(), i.getHit()))
            .collect(Collectors.toList());
        
        return new Result(collect.size(), collect);
    }
    
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }
    
    @GetMapping("items/{item_id}")
    public ResponseEntity<ItemResponse> findItem(@PathVariable("item_id") Long itemId) {
        
        return ResponseEntity.ok(ItemResponse.toItemResponse(itemService.findById(itemId)));
    }
}
