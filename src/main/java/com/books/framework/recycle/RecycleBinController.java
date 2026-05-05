package com.books.framework.recycle;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.books.framework.message.BaseMessage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/recycle-bin")
public class RecycleBinController {

    private final RecycleBinService service;

    public RecycleBinController(RecycleBinService service) {
        this.service = service;
    }

    @PostMapping("/list")
    public BaseMessage list(@RequestBody(required = false) BaseMessage request) {
        if (request == null) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "请求报文不能为空");
        }
        RecycleBinListRequest req = request.getDto(RecycleBinListRequest.class);
        String tableName = req != null ? req.getTableName() : null;
        Integer page = req != null ? req.getPage() : null;
        Integer limit = req != null ? req.getLimit() : null;

        Map<String, Object> result = service.list(tableName, page, limit);

        BaseMessage response = request.newResponse();
        response.setMessage("查询成功");
        response.put("list", result.get("list"));
        response.put("total", result.get("total"));
        response.put("page", result.get("page"));
        response.put("limit", result.get("limit"));
        return response;
    }

    @GetMapping("/{id}")
    public BaseMessage detail(@PathVariable Long id) {
        RecycleBinEntry entry = service.getById(id);
        if (entry == null) {
            throw new AppException(ErrorCodeEnum.DATA_NOT_FOUND, "回收站记录不存在");
        }
        BaseMessage response = new BaseMessage();
        response.setCode("SUCCESS");
        response.setMessage("查询成功");
        response.addDto(entry);
        return response;
    }

    @PostMapping("/{id}/restore")
    public BaseMessage restoreSingle(@PathVariable Long id) {
        Long newId = service.restoreSingle(id);
        BaseMessage response = new BaseMessage();
        response.setCode("SUCCESS");
        response.setMessage("恢复成功");
        response.put("newId", newId);
        return response;
    }

    @PostMapping("/batch/{batchId}/restore")
    public BaseMessage restoreBatch(@PathVariable String batchId) {
        int count = service.restoreBatch(batchId);
        BaseMessage response = new BaseMessage();
        response.setCode("SUCCESS");
        response.setMessage("批次恢复成功");
        response.put("count", count);
        return response;
    }

    @PostMapping("/{id}/delete")
    public BaseMessage permanentlyDelete(@PathVariable Long id) {
        service.permanentlyDelete(id);
        BaseMessage response = new BaseMessage();
        response.setCode("SUCCESS");
        response.setMessage("已彻底删除");
        return response;
    }

    @PostMapping("/batch/{batchId}/delete")
    public BaseMessage permanentlyDeleteBatch(@PathVariable String batchId) {
        int count = service.permanentlyDeleteBatch(batchId);
        BaseMessage response = new BaseMessage();
        response.setCode("SUCCESS");
        response.setMessage("已彻底删除批次");
        response.put("count", count);
        return response;
    }
}
