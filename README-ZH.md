# json-repair : 大语言模型生成异常 JSON 应用层解决方案

[![Release](https://img.shields.io/github/release/HAibiiin/json-repair.svg?sort=semver)](https://github.com/HAibiiin/json-repair/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.haibiiin/json-repair.svg)](https://search.maven.org/artifact/io.github.haibiiin/json-repair)
[![Apache 2.0 license](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)
[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](https://github.com/HAibiiin/json-repair/blob/main/README.md)

![Logo](https://haibiiin.github.io/resources/json-repair-logo.png)

## json-repair 是什么？

**json-repair** 是一个在应用层为 LLMs（大语言模型）生成异常 JSON 提供修复的 Java 库。基于 **json-repair** 可以简单高效并准确的实现异常 JSON 的修复。

## 如何使用

要想使用 **json-repair**，只需将其作为依赖添加到你的 Java 项目中即可。 Maven 配置如下:

```xml
<dependency>
    <groupId>io.github.haibiiin</groupId>
    <artifactId>json-repair</artifactId>
    <version>0.4.0</version>
</dependency>
```

Gradle 配置如下：

```
implementation 'io.github.haibiiin:json-repair:0.4.0'
```

接下来你只需要实例化 `JSONRepair` 并调用 `handle()` 方法便可以完成异常 JSON 的修复。

```java
JSONRepair repair = new JSONRepair();
String correctJSON = repair.handle(mistakeJSON);
```

如果需要从文本字符串中提取出符合 JSON 格式的内容，需要通过 JSONRepairConfig 启用提取功能。

```java
JSONRepairConfig config = new JSONRepairConfig();
config.enableExtractJSON();
JSONRepair repair = new JSONRepair(config);
String correctJSON = repair.handle(mistakeJSON);
```

## 功能特性

你可以通过查看 [测试用例数据集](https://github.com/HAibiiin/json-repair/blob/main/src/test/resources/case/simple.xml) 或 [测试报告](https://haibiiin.github.io/json-repair/reports/testcase/) 了解当前 0.4.0 版本 **json-repair** 支持修补的所有 JSON 异常。

基于当前 0.4.0 版本的功能如下：

* 实现对 JSON 字符串基本修补功能：
  * 修补缺少的右括号；
  * 修补缺少的右中括号；
  * 值为数组情况下多余逗号清理；
  * 值缺失以 null 填补；
  * 修补缺少的左括号； 
  * 修补缺少的最外层括号； 
  * 修补个别场景下字符串缺少引号； 
  * 提供自定义修补尝试次数。
* 实现对基于文本字符串的 JSON 提取功能：
  * 提取符合 JSON 格式的字符串；
  * 并支持对提取字符串的有限修复； 

## 性能指标

你可以通过运行 [BenchmarkTests](https://github.com/HAibiiin/json-repair/blob/main/src/test/java/io/github/haibiiin/json/repair/BenchmarkTests.java) 进行更多场景的性能测试

基于当前 0.4.0 版本的性能测试如下：

```
--AverageTime --NANOSECONDS --Warmup-5-1-SECONDS
Benchmark                                                          (anomalyJSON)    Mode     Cnt          Score         Error   Units
BenchmarkTests.testSimpleRepairStrategy                      {"f":"v", "f2":"v2"    avgt       5     114767.597 ±  246035.480   ns/op
BenchmarkTests.testSimpleRepairStrategy                         {"f":"v", "a":[1    avgt       5      42658.921 ±   66863.970   ns/op
BenchmarkTests.testSimpleRepairStrategy  {"f":"v", "a":[1,2], "o1":{"f1":"v1"},     avgt       5     410883.872 ±  703643.753   ns/op
BenchmarkTests.testSimpleRepairStrategy     "f":"v", "a":[1,2], "o1":{"f1":"v1"}    avgt       5     180096.819 ±  535679.217   ns/op
BenchmarkTests.testSimpleRepairStrategy                                      f:v    avgt       5     252954.401 ±  908076.158   ns/op

--AverageTime --MILLISECONDS --Warmup-5-1-SECONDS
Benchmark                                                          (anomalyJSON)    Mode     Cnt          Score         Error   Units
BenchmarkTests.testSimpleRepairStrategy                      {"f":"v", "f2":"v2"    avgt       5          0.179 ±       0.256   ms/op
BenchmarkTests.testSimpleRepairStrategy                         {"f":"v", "a":[1    avgt       5          0.215 ±       0.373   ms/op
BenchmarkTests.testSimpleRepairStrategy  {"f":"v", "a":[1,2], "o1":{"f1":"v1"},     avgt       5          0.070 ±       0.152   ms/op
BenchmarkTests.testSimpleRepairStrategy     "f":"v", "a":[1,2], "o1":{"f1":"v1"}    avgt       5          0.306 ±       0.121   ms/op
BenchmarkTests.testSimpleRepairStrategy                                      f:v    avgt       5          0.125 ±       0.243   ms/op
```

## 测试覆盖

你可以通过查看 [报告](https://haibiiin.github.io/json-repair/reports/coverage/) 了解项目测试覆盖率详情。

## 未来规划

 - [ ] 通过提供参考 JSON 样式，对异常 JSON 进行更准确的修正；

## 开发指南

将代码克隆到本地后，在项目根目录下，编译并安装所有模块到 Maven 本地仓库缓存，同时会生成 ANTLR `.g4` 语法文件对应的解析器 Java 类，这样在 IDE 就不会有相关的编译错误了。

```shell
mvnw install
```

## 开源许可

[Apache-2.0 license](https://github.com/HAibiiin/json-repair/blob/master/LICENSE).
