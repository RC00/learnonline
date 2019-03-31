package com.xuecheng.auth.client;


import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(XcServiceList.XC_SERVICE_UCENTER)
public interface UserClient {
    @GetMapping("/ucenter/getuserext")
    public XcUserExt getUserext(@RequestParam("username") String username);
}
