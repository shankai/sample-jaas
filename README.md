# JAAS Samples

## Authentication

### 源码

#### LoginContext
SampleAuthn 类的主方法执行身份验证，然后报告身份验证是否成功。
1. 实例化 `LoginContext` , LoginModule Name: `Sample`
2. 调用 `LoginContext` 的 `login` 方法

#### CallbackHandler
SampleCallbackHandler 处理三种类型的 Callbacks: 
- NameCallback 提示用户输入用户名
- PasswordCallback 提示输入密码
- TextOutputCallback 报告任何错误、警告或 SampleLoginModule 希望发送给用户的其他消息。

#### SampleLoginModule
SampleLoginModule 的用户身份验证包括简单地验证用户指定的名称和密码是否具有特定的值。

#### SamplePrincipal
如果身份验证成功，SampleLoginModule 将用表示用户的 SamplePrincipal 填充 Subject。

### 配置

`resources/sample_jaas.config`

```properties
Sample {
  me.kvn.codes.authn.modules.SampleLoginModule required debug=true;
};
```
其中 `Sample` 为 LoginModule 配置索引使用的名称， 实例化 `LoginContext` 引用，如：
```java
new LoginContext("Sample", new SampleCallbackHandler());
```

### 运行
```
mvn clean compile
cd target/classes
java -Djava.security.auth.login.config==sample_jaas.config me.kvn.codes.authn.SampleAuthn
```
系统将提示您输入用户名和密码，登录配置文件中指定的 SampleLoginModule 将进行检查，以确保这些都是正确的。SampleLoginModule 需要 `testUser` 作为用户名，`testPassword` 作为密码。

## Authorization