package com.easyChat.aspect;

import com.easyChat.anotation.GlobalInterceptor;
import com.easyChat.constants.Constants;
import com.easyChat.controller.AGlobalExceptionHandlerController;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.enums.ResponseCodeEnum;
import com.easyChat.exception.BusinessException;
import com.easyChat.redis.RedisUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class GlobalOperationAspect {
    @Resource
    private RedisUtils redisUtils;

    private static final Logger logger = LoggerFactory.getLogger(AGlobalExceptionHandlerController.class);

    @Before("@annotation(com.easyChat.anotation.GlobalInterceptor)")
    public void interceptorDo(JoinPoint joinPoint) {
       try {
           Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
           GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
           if (interceptor == null) {
               return;
           }
           if (interceptor.checkLogin() ||interceptor.checkAdmin()){
               checkLogin(interceptor.checkAdmin());
           }
       }catch (BusinessException e){
           logger.error("全局拦截异常",e);
           throw e;
       }catch (Exception e){
           logger.error("全局拦截异常",e);
           throw new BusinessException(ResponseCodeEnum.CODE_500);
       }catch (Throwable e){
           logger.error("全局拦截异常",e);
           throw new BusinessException(ResponseCodeEnum.CODE_500);
       }
    }
    private void checkLogin(Boolean checkAdmin) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("token");

        if(token == null){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }

        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (checkAdmin && !tokenUserInfoDto.getAdmin()) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }

    }
}

/**一、代码详解
 这几步代码是AOP（面向切面编程）中的一部分，用于在方法执行前进行拦截和处理。具体来说，它们的作用如下：

 ### 1. **获取当前执行的方法**

 Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
 ```
 - `joinPoint.getSignature()`：获取当前连接点（即被拦截的方法）的签名。
 - `(MethodSignature) joinPoint.getSignature()`：将签名转换为`MethodSignature`类型，因为`getSignature()`返回的是`Signature`接口，而`MethodSignature`提供了更多方法相关的信息。
 - `getMethod()`：通过`MethodSignature`获取当前执行的具体方法（`Method`对象）。

 ### 2. **获取方法上的注解**

 GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
 ```
 - `method.getAnnotation(GlobalInterceptor.class)`：从当前方法上获取`@GlobalInterceptor`注解的实例。
 - 如果方法上没有该注解，`interceptor`将为`null`。

 ### 3. **判断注解是否存在**

 if (interceptor == null) {
 return;
 }
 ```
 - 如果`interceptor`为`null`，说明当前方法上没有`@GlobalInterceptor`注解，直接返回，不进行后续处理。

 ### 4. **检查注解中的属性**

 if (interceptor.checkLogin() || interceptor.checkAdmin()) {
 // 这里可以添加具体的逻辑
 }
 ```
 - `interceptor.checkLogin()`：获取`@GlobalInterceptor`注解中`checkLogin`属性的值。
 - `interceptor.checkAdmin()`：获取`@GlobalInterceptor`注解中`checkAdmin`属性的值。
 - 如果`checkLogin`为`true`或`checkAdmin`为`true`，则进入条件块，执行相应的逻辑（例如检查用户是否登录或是否是管理员）。

 ---

 ### 总结
 这段代码的作用是：
 1. 获取当前执行的方法。
 2. 检查该方法是否被`@GlobalInterceptor`注解标记。
 3. 如果标记了，则根据注解中的属性（`checkLogin`和`checkAdmin`）决定是否执行某些逻辑（例如权限校验）。

 在实际应用中，你可以在`if (interceptor.checkLogin() || interceptor.checkAdmin())`块中添加具体的逻辑，例如：
 - 检查用户是否登录（通过`RedisUtils`或其他方式）。
 - 检查用户是否是管理员。
 - 如果校验失败，可以抛出异常或返回错误信息。
 */

//二、checkAdmin怎么更改它的值呢？
/*
`checkAdmin` 是 `@GlobalInterceptor` 注解的一个属性，它的值是在 **注解使用时** 指定的，而不是在运行时动态修改的。如果你想更改 `checkAdmin` 的值，需要在代码中修改注解的使用方式。

以下是具体的方法：

---

### 1. **在方法上直接修改注解的值**
`@GlobalInterceptor` 是一个注解，它的属性值是在方法上使用时指定的。例如：

```java
@GlobalInterceptor(checkLogin = true, checkAdmin = true) // 这里设置 checkAdmin 为 true
public void someMethod() {
    // 方法逻辑
}
```

如果你想将 `checkAdmin` 的值改为 `false`，只需修改注解的属性值：

```java
@GlobalInterceptor(checkLogin = true, checkAdmin = false) // 这里设置 checkAdmin 为 false
public void someMethod() {
    // 方法逻辑
}
```

---

### 2. **动态修改注解的值（不推荐）**
注解的属性值通常是在编译时确定的，无法在运行时直接修改。如果你确实需要在运行时动态改变行为，可以通过以下方式间接实现：

#### 方法 1：通过配置文件或外部条件
- 将 `checkAdmin` 的逻辑改为从配置文件或外部条件中读取，而不是直接依赖注解的值。
- 例如，使用 `@Value` 注解从配置文件中注入值：

```java
@Value("${security.checkAdmin}") // 从配置文件中读取
private boolean checkAdmin;
```

然后在 AOP 中根据这个值决定是否执行逻辑：

```java
if (checkAdmin) {
    // 执行管理员检查逻辑
}
```

#### 方法 2：通过反射修改注解的值（不推荐）
虽然不推荐，但可以通过反射修改注解的值。以下是一个示例：

```java
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class AnnotationUtil {
    public static void changeAnnotationValue(Method method, String annotationName, String attributeName, Object newValue) {
        try {
            // 获取注解的处理器
            Object handler = Proxy.getInvocationHandler(method.getAnnotation(GlobalInterceptor.class));
            // 获取注解的成员值
            Field field = handler.getClass().getDeclaredField("memberValues");
            field.setAccessible(true);
            // 获取注解的属性值
            Map<String, Object> memberValues = (Map<String, Object>) field.get(handler);
            // 修改属性值
            memberValues.put(attributeName, newValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

使用时：

```java
Method method = MyClass.class.getMethod("someMethod");
AnnotationUtil.changeAnnotationValue(method, "GlobalInterceptor", "checkAdmin", false);
```

**注意：**
- 这种方式依赖于 JDK 内部实现，可能会在不同版本的 JDK 中失效。
- 修改注解的值可能会破坏代码的可读性和可维护性，因此不推荐使用。

---

### 3. **推荐做法**
如果你需要动态控制 `checkAdmin` 的行为，建议将逻辑从注解中抽离出来，改为通过代码实现。例如：

- 在 AOP 中根据业务逻辑动态决定是否检查管理员权限。
- 使用配置文件或数据库存储权限规则。

例如：

```java
@Before("@annotation(com.easyChat.anotation.GlobalInterceptor)")
public void interceptorDo(JoinPoint joinPoint) {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
    GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
    if (interceptor == null) {
        return;
    }

    boolean shouldCheckAdmin = someService.shouldCheckAdmin(); // 从服务中动态获取是否需要检查管理员
    if (interceptor.checkLogin() || shouldCheckAdmin) {
        // 执行逻辑
    }
}
```

---

### 总结
- 如果你只是想修改 `checkAdmin` 的值，直接在注解使用时修改即可。
- 如果需要动态控制，建议将逻辑从注解中抽离，改为通过代码或配置文件实现。
- 反射修改注解的值是一种不推荐的做法，可能会带来维护性和兼容性问题。
 */