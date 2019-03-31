package com.xuecheng.auth.test;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {
    //生成一个jwt令牌
    @Test
    public void testCreateJwt() {
//证书文件
        String key_location = "xc.keystore";
//密钥库密码
        String keystore_password = "xuechengkeystore";
//访问证书路径
        ClassPathResource resource = new ClassPathResource(key_location);
//密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,
                keystore_password.toCharArray());
//密钥的密码，此密码和别名要匹配
        String keypassword = "xuecheng";
//密钥别名
        String alias = "xckey";
//密钥对（密钥和公钥）
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypassword.toCharArray());
//私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
//定义payload信息
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", "123");
        tokenMap.put("name", "mrt");
        tokenMap.put("roles", "r01,r02");
        tokenMap.put("ext", "1");
//生成jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(aPrivate));
//取出jwt令牌
        String token = jwt.getEncoded();
        System.out.println("token=" + token);
        //token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHQiOiIxIiwicm9sZXMiOiJyMDEscjAyIiwibmFtZSI6Im1ydCIsImlkIjoiMTIzIn0.a3pPI4RIGiI-4gYmC5aO_5iqsVaigKb_vSmjTxx-AjtujjBTENxqt6OI4K9ppx0IO3WEebBeOKQr6po-PnQvmMl7aNHPd9KWfKD_he_0Im88nXWU2JBX0ARc85rFiIB190r7kjFqlZrj7YkhwUsccC7PqfiyKg7Y6B7Ca_l98Dx3Zv6VJwYxVU159XlB6G2NGMIRmDbJhnxLUG0zZdOKBs0BsQobU-IRRCI2bEu5zTImimfogNsGciTCS-7CMN8kO4T_rSMwEe66vPMA-IjchsubfseIptYNVu9_QHbc8RNGf3wWrBcZOmTxaCIH-zAo02u1TQXyeeafLQQVzRTt8g
    }

    //资源服务使用公钥验证jwt的合法性，并对jwt解码
    @Test
    public void testVerify(){
//jwt令牌
        String token ="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJ0ZXN0MDIiLCJ1dHlwZSI6IjEwMTAwMiIsImlkIjoiNDkiLCJleHAiOjE1NDg4MTgwMjQsImF1dGhvcml0aWVzIjpbInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYmFzZSIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfZGVsIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9saXN0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wbGFuIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZSIsImNvdXJzZV9maW5kX2xpc3QiLCJ4Y190ZWFjaG1hbmFnZXIiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX21hcmtldCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfcHVibGlzaCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYWRkIl0sImp0aSI6IjdjNjdiMTZkLWMyMDUtNDM0My1iOTdhLTBlMThjMDYwOTc4YiIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.KwiDj1xvGBASUJJYJC-DtnoG3xOnmBrbNHgv94j2Sl7y2yGAfjgtf-Xr3v0_dN0SgB4ahVyFC0jPjqkiGgLR7qYlutBjQ7KKvSwRg-4ZAUYMAHpcwhvTkY84LcJmYPRdrLUXHpVuQwjV6EulwI-2dhdMduv7p7vYUp_F-gNAYt_M8QSQTec1_FmtPUApQYRNd9ZzhmI2zo5c6kgiyKvTXS_y0D0aL_wmOomfhqKcap6iMN4prLNtvXyO_EFYfkv9I4sWAw_eaNoDf7wi8XXRVYdNJ0u600VBACaOUtIzAYUAdy8efCWJf-QY64NM5yGRB_mi2n9NvaBnVKCFkxZwMg";
//公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
//校验jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));
//获取jwt原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
//jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

    @Test
    public void testPasswrodEncoder(){
        String password = "111111";
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        for(int i=0;i<10;i++) {
//每个计算出的Hash值都不一样
            String hashPass = passwordEncoder.encode(password);
            System.out.println(hashPass);
//虽然每次计算的密码Hash值不一样但是校验是通过的
            boolean f = passwordEncoder.matches(password, hashPass);
            System.out.println(f);
        }
    }
}
