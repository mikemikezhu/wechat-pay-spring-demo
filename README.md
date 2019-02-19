## 微信支付 Demo Project / Demo Project for WeChat Pay

### 前言 / Introduction

本项目主要基于Spring框架来实现微信小程序的支付功能。

This is a demo project based on Spring framework, which demonstrates how to implement WeChat pay feature.

### 用法 / Usage

(1) 下载本项目到本地后，打开"config->application.yml"文件，并修改以下几个配置：
- wechat.id: 微信小程序的AppId
- wechat.secret: 微信小程序的AppSecret
- wechat.pay.notify: 微信支付的结果通知地址
- wechat.pay.mch-id: 微信支付的商户号
- wechat.pay.key: 微信支付的API密钥

Download the project, and then open the file "config->application.yml". The following fields need to be updated:
- wechat.id: The AppId of the mini program
- wechat.secret: The AppSecret of the mini program
- wechat.pay.notify: The notify URL of WeChat pay result
- wechat.pay.mch-id: The merchant id of WeChat pay
- wechat.pay.key: The API key of WeChat pay

(2) 在本项目的根目录运行:
```
mvn spring-boot:run
```

Run the following command:
```
mvn spring-boot:run
```

(3) 打开微信小程序，为了演示用途，我们在onLaunch这里加入以下代码:
```javascript
onLaunch: function() {

        wx.login({
            success: response => {
                const code = response.code
                if (code) {
                    const data = {
                        totalPrice: 6, // Product price
                        productName: 'product_name', // Produce name
                        code: code
                    }

                    wx.request({
                        url: 'http://localhost:8080/request_wechat_pay',
                        data: data,
                        method: 'POST',
                        success: function(res) {
                            
                            const result = res.data.result

                            wx.requestPayment({
                                timeStamp: result.timeStamp,
                                nonceStr: result.nonce_str,
                                package: result.package,
                                signType: result.signType,
                                paySign: result.sign,
                                success: function(response) {
                                    // Request payment success
                                },
                                fail: function (response) {
                                    // Request payment fail
                                }
                            })
                        },
                        fail: function(res) {
                            // Request payment fail 
                        }
                    })
                }
            }
        })
    }
```

For the demonstration purpose, we simply add the following code in the "onLaunch" method of WeChat mini program:
```javascript
onLaunch: function() {

        wx.login({
            success: response => {
                const code = response.code
                if (code) {
                    const data = {
                        totalPrice: 6, // Product price
                        productName: 'product_name', // Produce name
                        code: code
                    }

                    wx.request({
                        url: 'http://localhost:8080/request_wechat_pay',
                        data: data,
                        method: 'POST',
                        success: function(res) {
                            
                            const result = res.data.result

                            wx.requestPayment({
                                timeStamp: result.timeStamp,
                                nonceStr: result.nonce_str,
                                package: result.package,
                                signType: result.signType,
                                paySign: result.sign,
                                success: function(response) {
                                    // Request payment success
                                },
                                fail: function (response) {
                                    // Request payment fail
                                }
                            })
                        },
                        fail: function(res) {
                            // Request payment fail 
                        }
                    })
                }
            }
        })
    }
```
### 优化 / Improvements

本项目以演示目的为主，实际过程中我们的优化方法如下：

为了改进，我们可以让小程序onLaunch启动的时候，只调用一次“wx.login”的API。然后，我们的后台换取openid。拿到openid之后，我们可以生成一个session_id, 并将其存入Redis数据库当中。
为了安全起见，我们还可以将session_id设置一定的时效性。最后，我们再把这个session_id返回给小程序。
那么这样，每次微信支付的时候，小程序只需要通过session_id与我们后台沟通即可。

This project is primarily for demonstration purpose. As a matter of fact, we may adopt the following improvements:

For the purpose of improvements, we may call the API "wx.login" only once, when the mini program launches. Then, the mini program may send such code to the backend. And our backend will further send such code to WeChat server, in order to get the unique openid.

When the backend receives the openid from WeChat server, we may create a session_id, and save both session_id and openid into Redis database, with a certain expiration date.

Then, the backend may send the session_id back to the mini program. Later, the mini program only needs to communicate with our backend with such session_id, as long as the session is still valid.