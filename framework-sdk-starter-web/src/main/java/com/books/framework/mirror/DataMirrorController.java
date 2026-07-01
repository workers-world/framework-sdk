package com.books.framework.mirror;

import com.books.framework.annotation.ApiName;
import com.books.framework.message.BaseMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-mirror")
public class DataMirrorController {

    private final DataMirrorQueryService queryService;

    public DataMirrorController(DataMirrorQueryService queryService) {
        this.queryService = queryService;
    }

    @ApiName(name = "listDataMirror",
            input = {DataMirrorListX1.class},
            output = {DataMirrorListZ1.class},
            description = "数据镜像审计列表")
    @PostMapping("/list")
    public BaseMessage list(@RequestBody(required = false) BaseMessage request) {
        DataMirrorListX1 x1 = request.getDto(DataMirrorListX1.class);
        DataMirrorListZ1 data = queryService.list(x1);
        BaseMessage response = request.newResponse();
        response.setMessage("查询成功");
        response.addDto(data);
        return response;
    }

    @ApiName(name = "dataMirrorDetail",
            input = {DataMirrorIdX1.class},
            output = {DataMirrorDetailZ1.class},
            description = "数据镜像审计详情")
    @PostMapping("/detail")
    public BaseMessage detail(@RequestBody(required = false) BaseMessage request) {
        DataMirrorIdX1 x1 = request.getDto(DataMirrorIdX1.class);
        DataMirrorDetailZ1 data = queryService.getDetail(x1.getId());
        BaseMessage response = request.newResponse();
        response.setMessage("查询成功");
        response.addDto(data);
        return response;
    }

    @ApiName(name = "dataMirrorBatch",
            input = {DataMirrorBatchX1.class},
            output = {DataMirrorBatchZ1.class},
            description = "数据镜像审计批次")
    @PostMapping("/batch")
    public BaseMessage batch(@RequestBody(required = false) BaseMessage request) {
        DataMirrorBatchX1 x1 = request.getDto(DataMirrorBatchX1.class);
        DataMirrorBatchZ1 data = queryService.getBatch(x1.getBatchId());
        BaseMessage response = request.newResponse();
        response.setMessage("查询成功");
        response.addDto(data);
        return response;
    }
}
