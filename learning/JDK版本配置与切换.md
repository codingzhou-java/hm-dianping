# JDK 版本配置与切换

## 一、IDE 运行项目时的 JDK 版本选择机制

### 1.1 版本优先级（由高到低）

| 层级 | 优先级 | 说明 | 配置位置 |
|------|--------|------|----------|
| 项目级 SDK | 最高 | IDEA 实际用来执行编译和运行的 JDK | `File → Project Structure → Project → SDK` |
| Maven Runner JRE | 中等 | Maven 插件运行时使用的 JDK | `File → Settings → Build Tools → Maven → Runner → JRE` |
| maven-compiler-plugin | 低 | 控制编译目标字节码版本 | `pom.xml` 中的 `<source>/<target>` |
| 环境变量 JAVA_HOME | 最低 | 命令行 `mvn` 命令使用的默认 JDK | 系统环境变量 |

### 1.2 各配置项的作用

```xml
<!-- pom.xml 中的 java.version -->
<properties>
    <java.version>1.8</java.version>
</properties>
```
- **作用**：Spring Boot 的配置属性，仅影响某些插件的默认值
- **注意**：**不会强制切换实际使用的 JDK**，只是一个提示性配置

```xml
<!-- pom.xml 中的 maven-compiler-plugin -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <source>1.8</source>  <!-- 源代码兼容的 Java 版本 -->
        <target>1.8</target>  <!-- 生成的字节码目标版本 -->
    </configuration>
</plugin>
```
- **作用**：真正控制**编译目标版本**（生成的 .class 字节码版本）
- **注意**：编译过程仍用项目级 JDK 执行，但可以向下兼容

---

## 二、常见问题：Lombok 与 JDK 版本不兼容

### 2.1 问题现象

```
java: java.lang.NoSuchFieldError: Class com.sun.tools.javac.tree.JCTree$JCImport
does not have member field 'com.sun.tools.javac.tree.JCTree qualid'
```

### 2.2 问题原因

Lombok 通过注解处理器在编译时修改 AST（抽象语法树），但 JDK 9+ 对编译器内部 API 做了重大修改：

| Lombok 版本 | 支持的 JDK 版本 |
|-------------|-----------------|
| 1.18.12 及以下 | JDK 8 |
| 1.18.22+ | JDK 16 以下 |
| 1.18.30+ | JDK 21 以下 |

**常见场景**：
- 项目用 Spring Boot 2.3.12（自带 Lombok 1.18.12）
- 但 IDEA 配置了 JDK 21
- 编译时报错

### 2.3 解决方案

#### 方案一：降级 JDK（推荐）

保持 Lombok 版本不变，将项目 JDK 改为 Java 8 或 11：

```
File → Project Structure → Project → SDK
选择 JDK 1.8 或 JDK 11
```

#### 方案二：升级 Lombok

保持用 JDK 21，升级 Lombok 到支持版本：

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <optional>true</optional>
</dependency>
```

#### 方案三：明确指定编译版本

在 `pom.xml` 中配置 `maven-compiler-plugin`：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <encoding>UTF-8</encoding>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 三、快速切换 JDK 版本的方法

### 3.1 在 IDEA 中切换

#### 修改项目级 SDK（最常用）
```
1. File → Project Structure
2. Project → SDK
3. 选择需要的 JDK 版本
4. 点击 Apply
```

#### 修改 Maven Runner JRE
```
1. File → Settings
2. Build, Execution, Deployment → Build Tools → Maven
3. Runner → JRE
4. 选择对应的 JDK
```

### 3.2 命令行切换

```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_301
mvn clean install

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
mvn clean install
```

### 3.3 Maven 多版本支持

如果需要在同一台机器上切换不同 JDK，可以配置 Maven Toolchains：

**~/.m2/toolchains.xml**
```xml
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>1.8</version>
      <vendor>oracle</vendor>
    </provides>
    <configuration>
      <jdkHome>C:/Program Files/Java/jdk1.8.0_301</jdkHome>
    </configuration>
  </toolchain>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>11</version>
      <vendor>oracle</vendor>
    </provides>
    <configuration>
      <jdkHome>C:/Program Files/Java/jdk-11</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

**pom.xml 中指定**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-toolchains-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <goals>
                <goal>toolchain</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <toolchains>
            <jdk>
                <version>1.8</version>
            </jdk>
        </toolchains>
    </configuration>
</plugin>
```

---

## 四、总结

### 4.1 关键要点

1. **IDEA 项目 SDK** 是实际运行用的 JDK，版本不匹配会导致编译错误
2. `pom.xml` 中的 `java.version` 只是配置属性，不能强制切换 JDK
3. `maven-compiler-plugin` 控制编译目标版本，但执行仍依赖项目 SDK
4. Lombok 与 JDK 版本需要匹配，否则会报内部 API 不兼容错误

### 4.2 排查步骤

遇到版本相关问题时，按以下顺序检查：

```
1. 查看 IDEA 项目设置的 SDK 版本
   ↓
2. 检查 Maven Runner 的 JRE 设置
   ↓
3. 确认 pom.xml 中 maven-compiler-plugin 配置
   ↓
4. 检查依赖库（如 Lombok）与 JDK 版本是否兼容
```

### 4.3 推荐实践

- **新项目**：统一使用 JDK 11 或 17（LTS 版本），配置好 maven-compiler-plugin
- **老项目**：保持原有 JDK 版本，不要随意升级
- **多 JDK 环境**：使用 Maven Toolchains 管理，避免手动切换 JAVA_HOME
