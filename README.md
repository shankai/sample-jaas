# JAAS Samples
```shell
$ java -version 
openjdk version "11.0.2" 2019-01-15
OpenJDK Runtime Environment 18.9 (build 11.0.2+9)
OpenJDK 64-Bit Server VM 18.9 (build 11.0.2+9, mixed mode)
```

## Authentication
> package `me.kvn.codes.authn`

### 源码

#### SampleAuthn
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
```shell
mvn clean compile
cd target/classes
java -Djava.security.auth.login.config==sample_jaas.config me.kvn.codes.authn.SampleAuthn
```
系统将提示您输入用户名和密码，登录配置文件中指定的 SampleLoginModule 将进行检查，以确保这些都是正确的。SampleLoginModule 需要 `testUser` 作为用户名，`testPassword` 作为密码。

### 运行（安全管理器）
当 Java 程序在安装了安全管理器的情况下运行时，不允许程序访问资源或执行对安全性敏感的操作，除非现行安全策略明确授予程序这样做的权限。
大多数浏览器都安装了安全管理器，因此 applet 通常在安全管理器的监督下运行。另一方面，应用程序则不会，因为在应用程序运行时不会自动安装安全管理器

要使用安全管理器运行应用程序，只需使用命令行中包含的 `-Djava.security.manager` 参数调用解释器。

```shell
java -Djava.security.manager -Djava.security.auth.login.config==sample_jaas.config me.kvn.codes.authn.SampleAuthn
```
执行以上命令后，出现错误：
```shell
Cannot create LoginContext. access denied ("javax.security.auth.AuthPermission" "createLoginContext.Sample")
```
按照下列步骤解决以上问题：

0. 构建
```shell
mvn clean compile
cd target/classes
```

1. 创建 `SampleAuthn.jar`

```shell
jar -cvf SampleAuthn.jar me/kvn/codes/authn/SampleAuthn4SecurityMgr.class me/kvn/codes/authn/SampleCallbackHandler4SecurityMgr.class
```

**备注：SampleAuthn 与 SampleAuthn4SecurityMgr 的区别在于分别引用了 SampleCallbackHandler 和 SampleCallbackHandler4SecurityMgr，后两者的区别是 `System.console().readPassword()` 会引发安全异常。该异常暂时未能解决，考虑到本例仅是样例程序，故改用明文输入密码的方式展示最终效果。**

2. 创建 `SampleLM.jar` (SampleLoginModule)

```shell
jar -cvf SampleLM.jar me/kvn/codes/authn/modules/SampleLoginModule.class me/kvn/codes/authn/principals/SamplePrincipal.class
```

3. 创建授权策略文件

`resources/sample_authn.policy`，其中包含两部分授权：`SampleAuthn.jar` 拥有 `createLoginContext.Sample` 的权限，`SampleLM.jar` 拥有 `modifyPrincipals` 的权限。

4. 执行
```shell
java -classpath SampleAuthn.jar:SampleLM.jar -Djava.security.manager -Djava.security.policy==sample_authn.policy -Djava.security.auth.login.config==sample_jaas.config me.kvn.codes.authn.SampleAuthn4SecurityMgr
```

## Authorization
> package `me.kvn.codes.authz`

### 源码

#### SampleAuthz
1. 实例化 `LoginContext` , LoginModule Name: `Sample`
2. 调用 `LoginContext` 的 `login` 方法
3. 成功认证后获得 `Subject`，并尝试进行操作。

#### CallbackHandler
SampleCallbackHandler 处理三种类型的 Callbacks:
- NameCallback 提示用户输入用户名
- PasswordCallback 提示输入密码
- TextOutputCallback 报告任何错误、警告或 SampleLoginModule 希望发送给用户的其他消息。

#### SampleLoginModule(复用)
SampleLoginModule 的用户身份验证包括简单地验证用户指定的名称和密码是否具有特定的值。

#### SamplePrincipal(复用)
如果身份验证成功，SampleLoginModule 将用表示用户的 SamplePrincipal 填充 Subject。

#### SampleAction

SampleAction 是对 PrivilegedAction 接口的实现，用于演示授权操作。具体操作：
1. 读取系统属性 `java.home`
2. 读取系统属性 `user.home`
3. 访问系统文件 `foo.txt`

### 构建执行

0. 构建
```shell
mvn clean compile
cd target/classes
```

1. 创建 `SampleAuthz.jar`

```shell
jar -cvf SampleAuthz.jar me/kvn/codes/authz/SampleAuthz.class me/kvn/codes/authz/SampleCallbackHandler.class
```

2. 创建 `SampleLM.jar` (SampleLoginModule)

> 复用与 SampleAuthn 相同的 SampleLoginModule 

```shell
jar -cvf SampleLM.jar me/kvn/codes/authn/modules/SampleLoginModule.class me/kvn/codes/authn/principals/SamplePrincipal.class
```

3. 创建 `SampleAction.jar`

```shell
jar -cvf SampleAction.jar me/kvn/codes/authz/SampleAction.class
```

3. 创建授权策略文件

本例 `resources/sample_authz.policy` 的完整配置：
```shell
grant codebase "file:./SampleAction.jar", Principal me.kvn.codes.authn.principals.SamplePrincipal "testUser" {
    permission java.util.PropertyPermission "java.home", "read";
    permission java.util.PropertyPermission "user.home", "read";
    permission java.io.FilePermission "foo.txt", "read";
};
grant codebase "file:./SampleAuthz.jar" {
    permission javax.security.auth.AuthPermission "createLoginContext.Sample";
    permission javax.security.auth.AuthPermission "doAsPrivileged";
};
grant codebase "file:./SampleLM.jar" {
    permission javax.security.auth.AuthPermission "modifyPrincipals";
};
```
4. 执行
```shell
java -classpath SampleAuthz.jar:SampleAction.jar:SampleLM.jar  -Djava.security.manager  -Djava.security.policy==sample_authz.policy  -Djava.security.auth.login.config==sample_jaas.config me.kvn.codes.authz.SampleAuthz
```

当 `resources/sample_authz.policy` 仅设置认证权限时，例如：
```shell
grant codebase "file:./SampleAuthz.jar" {
    permission javax.security.auth.AuthPermission "createLoginContext.Sample";
    permission javax.security.auth.AuthPermission "doAsPrivileged";
};
grant codebase "file:./SampleLM.jar" {
    permission javax.security.auth.AuthPermission "modifyPrincipals";
};
```
执行后出现错误：
```shell
...Omitted
Exception in thread "main" java.security.AccessControlException: access denied ("java.util.PropertyPermission" "java.home" "read")
...Omitted
```

