package babybeb.usersusedbookstore.api;

import babybeb.usersusedbookstore.api.dto.item.ChangeDealStatusRequest;
import babybeb.usersusedbookstore.api.dto.item.CreateItemDto;
import babybeb.usersusedbookstore.api.dto.item.ItemResponse;
import babybeb.usersusedbookstore.api.dto.item.SaveItemRequest;
import babybeb.usersusedbookstore.api.dto.item.UpdateItemRequest;
import babybeb.usersusedbookstore.domain.Book;
import babybeb.usersusedbookstore.domain.Category;
import babybeb.usersusedbookstore.domain.DealStatus;
import babybeb.usersusedbookstore.domain.Item;
import babybeb.usersusedbookstore.domain.ItemCondition;
import babybeb.usersusedbookstore.domain.dealarea.DealArea;
import babybeb.usersusedbookstore.domain.dealarea.First;
import babybeb.usersusedbookstore.domain.dealarea.Second;
import babybeb.usersusedbookstore.service.ItemService;
import babybeb.usersusedbookstore.service.MemberService;
import babybeb.usersusedbookstore.service.PurchaseService;
import babybeb.usersusedbookstore.service.SaleService;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/items")
public class ItemController {
    
    private final ItemService itemService;
    private final SaleService saleService;
    private final PurchaseService purchaseService;
    private final MemberService memberService;
    
    /**
     * Item ??????
     *
     * @param memberId
     * @param request
     * @return itemId
     */
    @ApiOperation(value = "memberId??? SaveItemRequest??? ?????? Item ??????", notes = "json ??????: int price, "
        + "String itemCondition, String isbn, String title, int bookPrice, String publisher, "
        + "String author, int page, String kdc, String category, String first, String second")
    @PostMapping("/{member_id}")
    public ResponseEntity<Long> saveItem(
        @PathVariable("member_id") Long memberId,
        @RequestBody @Valid SaveItemRequest request) {
        
        Book book = new Book(request.getIsbn(), request.getTitle(), request.getBookPrice(),
                             request.getPublisher(), request.getAuthor(), request.getPage(),
                             request.getKdc(),
                             Category.valueOf(request.getCategory()));
        CreateItemDto createItemDto;
        if (request.getSecond() == "") {
            createItemDto = new CreateItemDto(book, request.getPrice(),
                                              ItemCondition.valueOf(
                                                  request.getItemCondition()),
                                              new DealArea(
                                                  First.valueOf(request.getFirst())));
        } else {
            createItemDto = new CreateItemDto(book, request.getPrice(),
                                              ItemCondition.valueOf(
                                                  request.getItemCondition()),
                                              new DealArea(
                                                  First.valueOf(request.getFirst()),
                                                  Second.valueOf(
                                                      request.getSecond())));
        }
        Long itemId = itemService.saveItem(createItemDto);
        
        saleService.addSale(memberId, itemService.findById(itemId));
        
        //????????? ????????? ?????? itemId ??? ??????
        return ResponseEntity.ok(itemId);
    }
    
    /**
     * Item ??????
     *
     * @param itemId
     * @return return itemId
     */
    @ApiOperation(value = "itemId??? ?????? Item ??????")
    @DeleteMapping("/{item_id}")
    public ResponseEntity<Long> deleteItem(
        @PathVariable("item_id") Long itemId) {
        
        Item findItem = itemService.findById(itemId);
        if (findItem.getDealStatus().equals(DealStatus.COMP)) {
            return new ResponseEntity("Cannot delete products that have already been traded.",
                                      HttpStatus.OK);
        }
        
        itemService.deleteItem(itemId);
        saleService.cancelSale(itemId);
        
        return ResponseEntity.ok(itemId);
    }
    
    /**
     * DealStatus ??????
     *
     * @param itemId
     * @param request
     * @return "???????????? ?????? ???????????????." ?????? ItemResponse
     */
    @ApiOperation(value = "itemId??? ChangeDealStatusRequest??? ?????? Item??? DealStatus ??????")
    @PatchMapping("/{item_id}")
    public ResponseEntity<ItemResponse> changeDealStatus(
        @PathVariable("item_id") Long itemId,
        @RequestBody @Valid ChangeDealStatusRequest request) {
        
        Item findItem = itemService.findById(itemId);
        
        if (request.getDealStatus().equals("COMP")) {
            // ???????????? ???????????? ??????
            Long buyerId = 0l;
            String option = memberService.isAlreadyExists(request.getBuyerInfo());
            if (option.equals("email")) { // option ?????? enum?????? int??? ????????? ???????
                buyerId = memberService.findByEmail(request.getBuyerInfo()).getId();
            } else if (option.equals("phone")) {
                buyerId = memberService.findByPhoneNumber(request.getBuyerInfo()).getId();
            } else {
                return new ResponseEntity("Member does not exist.", HttpStatus.OK);
            }
            purchaseService.savePurchase(buyerId, findItem);
        } else if (findItem.getDealStatus().equals(DealStatus.COMP)) {
            return new ResponseEntity(
                "Transaction status cannot be changed for products that have already been traded.",
                HttpStatus.OK);
        }
        itemService.changeDealStatus(findItem, request.getDealStatus());
        
        return ResponseEntity.ok(ItemResponse.toItemResponse(itemService.findById(itemId)));
    }
    
    /**
     * Item ??????
     *
     * @param itemId
     * @param request
     * @return ItemResponse
     */
    @ApiOperation(value = "itemId??? UpdateItemRequest??? ?????? Item Update")
    @PutMapping("/{item_id}")
    public ResponseEntity<ItemResponse> updateItem(
        @PathVariable("item_id") Long itemId,
        @RequestBody UpdateItemRequest request) {
        
        Item findItem = itemService.findById(itemId);
        itemService.updateItem(itemId, findItem.getBook(), request.getItemPrice(),
                               request.getItemCondition(), request.getDealArea());
        
        if (findItem.getDealStatus().equals(DealStatus.COMP)) {
            return new ResponseEntity(
                "Cannot change the product status for products that have already been traded.",
                HttpStatus.OK);
        }
        
        return ResponseEntity.ok(ItemResponse.toItemResponse(itemService.findById(itemId)));
    }
    
    /**
     * Item ??????
     *
     * @param itemId
     * @return ItemResponse
     */
    @ApiOperation(value = "itemId??? ?????? Item ?????? ??????")
    @GetMapping("/{item_id}")
    public ResponseEntity<ItemResponse> findItem(@PathVariable("item_id") Long itemId) {
        
        return ResponseEntity.ok(ItemResponse.toItemResponse(itemService.findById(itemId)));
    }
}