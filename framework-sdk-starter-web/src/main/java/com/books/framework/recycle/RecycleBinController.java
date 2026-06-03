package com.books.framework.recycle;

import com.books.framework.annotation.ApiName;
import com.books.framework.message.BaseMessage;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recycle-bin")
public class RecycleBinController {

    private final RecycleBinService service;

    public RecycleBinController(RecycleBinService service) {
        this.service = service;
    }

    @ApiName(name = "listRecycleBin",
            input = {RecycleBinListX1.class},
            output = {RecycleBinListZ1.class},
            description = "回收站列表")
    @PostMapping("/list")
    public BaseMessage list(@RequestBody(required = false) BaseMessage request) {
        RecycleBinListX1 x1 = request.getDto(RecycleBinListX1.class);
        RecycleBinListZ1 data = service.list(x1);
        BaseMessage response = request.newResponse();
        response.setMessage("查询成功");
        response.addDto(data);
        return response;
    }

    @ApiName(name = "recycleBinDetail",
            input = {RecycleBinIdX1.class},
            output = {RecycleBinEntry.class},
            description = "回收站记录详情")
    @PostMapping("/detail")
    public BaseMessage detail(@RequestBody(required = false) BaseMessage request) {
        RecycleBinIdX1 x1 = request.getDto(RecycleBinIdX1.class);
        RecycleBinEntry data = service.getById(x1.getId());
        BaseMessage response = request.newResponse();
        response.setMessage("查询成功");
        response.addDto(data);
        return response;
    }

    @ApiName(name = "restoreRecycleBin",
            input = {RecycleBinIdX1.class},
            description = "恢复单条")
    @PostMapping("/restore")
    public BaseMessage restoreSingle(@RequestBody(required = false) BaseMessage request) {
        RecycleBinIdX1 x1 = request.getDto(RecycleBinIdX1.class);
        Long newId = service.restoreSingle(x1.getId());
        BaseMessage response = request.newResponse();
        response.setMessage("恢复成功");
        response.put("newId", newId);
        return response;
    }

    @ApiName(name = "restoreRecycleBinBatch",
            input = {RecycleBinBatchX1.class},
            description = "恢复批次")
    @PostMapping("/restore-batch")
    public BaseMessage restoreBatch(@RequestBody(required = false) BaseMessage request) {
        RecycleBinBatchX1 x1 = request.getDto(RecycleBinBatchX1.class);
        int count = service.restoreBatch(x1.getBatchId());
        BaseMessage response = request.newResponse();
        response.setMessage("批次恢复成功");
        response.put("count", count);
        return response;
    }

    @ApiName(name = "deleteRecycleBin",
            input = {RecycleBinIdX1.class},
            description = "彻底删除")
    @PostMapping("/delete")
    public BaseMessage permanentlyDelete(@RequestBody(required = false) BaseMessage request) {
        RecycleBinIdX1 x1 = request.getDto(RecycleBinIdX1.class);
        service.permanentlyDelete(x1.getId());
        BaseMessage response = request.newResponse();
        response.setMessage("已彻底删除");
        return response;
    }

    @ApiName(name = "deleteRecycleBinBatch",
            input = {RecycleBinBatchX1.class},
            description = "彻底删除批次")
    @PostMapping("/delete-batch")
    public BaseMessage permanentlyDeleteBatch(@RequestBody(required = false) BaseMessage request) {
        RecycleBinBatchX1 x1 = request.getDto(RecycleBinBatchX1.class);
        int count = service.permanentlyDeleteBatch(x1.getBatchId());
        BaseMessage response = request.newResponse();
        response.setMessage("已彻底删除批次");
        response.put("count", count);
        return response;
    }
}
