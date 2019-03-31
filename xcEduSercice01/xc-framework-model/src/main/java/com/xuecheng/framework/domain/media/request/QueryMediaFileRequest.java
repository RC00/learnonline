package com.xuecheng.framework.domain.media.request;

import com.xuecheng.framework.model.request.RequestData;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class QueryMediaFileRequest extends RequestData {

    private String fileOriginalName;
    private String processStatus;
    private String tag;
}
