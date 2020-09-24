# 基于`Gradle`的大型框架封装模板

# **简介**

参考 `Spring Boot` 2.3 版本以后的 `Gradle` 编译脚本，开发的项目模板。

特性:
*  支持构建 `Maven` 的 `POM`
    + 支持生成标准 `POM`
    + 支持自定义修改 `BOM`
        + 可以实现 `parent` 的继承关系
        + 支持`build`的插件依赖传递和配置
    + 支持 `Maven` 的 `可选依赖` 
*  简化全局插件配置

# **项目结构**

```
+--- Project 'buildSrc' - 构建脚本
+--- Project ':tenon-boot-project' - Spring Boot 扩展模块
|    +--- Project ':tenon-boot-project:tenon-boot-parent' - Maven项目依赖模块
|    \--- Project ':tenon-boot-project:tenon-boot-starters' - Spring Boot 模块
|         +--- Project ':tenon-boot-project:tenon-boot-starters:tenon-boot-starter'
|         +--- Project ':tenon-boot-project:tenon-boot-starters:tenon-boot-starter-actuator'
|         +--- Project ':tenon-boot-project:tenon-boot-starters:tenon-boot-starter-web'
|         \--- Project ':tenon-boot-project:tenon-boot-starters:tenon-boot-starter-webflux'
+--- Project ':tenon-common-project' - 工具类子模块
|    \--- Project ':tenon-common-project:tenon-common-basic' - 工具类测试模块
+--- Project ':tenon-container-project' - 容器化模块
|    +--- Project ':tenon-container-project:tenon-container-chart' - helm chart 模块
|    \--- Project ':tenon-container-project:tenon-container-image' - 容器镜像模块
\--- Project ':tenon-dependency-project' - 所有 `Java` 项目的全局依赖
```



# **插件使用方式**

## `cloud.tenon.gradle.bom`

用于构建 `BOM` 文件

**常规用法**

```groovy
bom {

    library("Spring Boot", "${springBootVersion}") {
        group("org.springframework.boot") {
            imports = [
                    "spring-boot-dependencies"
            ]
            plugins = [
                    "spring-boot-maven-plugin"
            ]
        }
    }
    
    library("Tenon", "${project.version}") {
        group("cloud.tenon.common") {
            modules = [
                    "tenon-common-basic"
            ]
        }
    }
}
```

``` xml
  <properties>
    <spring-boot.version>2.3.4.RELEASE</spring-boot.version>
    <tenon.version>1.0.0-SNAPSHOT</tenon.version>
  </properties>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>cloud.tenon.common</groupId>
        <artifactId>tenon-common-basic</artifactId>
        <version>${tenon.version}</version>
      </dependency>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-maven-plugin</artifactId>
          <version>${spring-boot.version}</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
```

**自定义配置**

```groovy


publishing.publications.withType(MavenPublication) {
    pom.withXml { xml ->
        def root = xml.asNode()
        // 实现parent
        root.groupId.replaceNode {
            parent {
                delegate.groupId("${project.group}")
                delegate.artifactId("tenon-dependency-project")
                delegate.version("${project.version}")
            }
        }
        root.remove(root.version)
        
        // 添加自定义参数配置
		root.description.plus {
			properties {
				delegate."project.build.sourceEncoding"('UTF-8')
				delegate."project.reporting.outputEncoding"('UTF-8')
			}
		}
        
        // 扩展配置
		root.scm.plus {
			build {
				resources {
					resource {
						delegate.directory('${basedir}/src/main/resources')
						delegate.filtering('true')
						includes {
							delegate.include('**/application*.yml')
							delegate.include('**/application*.yaml')
							delegate.include('**/application*.properties')
						}
					}
					resource {
						delegate.directory('${basedir}/src/main/resources')
						excludes {
							delegate.exclude('**/application*.yml')
							delegate.exclude('**/application*.yaml')
							delegate.exclude('**/application*.properties')
						}
					}
				}
				pluginManagement {
					plugins {
						plugin {
							delegate.groupId('org.jetbrains.kotlin')
							delegate.artifactId('kotlin-maven-plugin')
							delegate.version('${kotlin.version}')
							configuration {
								delegate.jvmTarget('${java.version}')
								delegate.javaParameters('true')
							}
							executions {
								execution {
									delegate.id('compile')
									delegate.phase('compile')
									goals {
										delegate.goal('compile')
									}
								}
								execution {
									delegate.id('test-compile')
									delegate.phase('test-compile')
									goals {
										delegate.goal('test-compile')
									}
								}
							}
						}
					}
				}
			}
		}

    }
}
```


## `cloud.tenon.gradle.project`

配置默认`Java`工程，方便生成 `jar`, `sources.jar`, `javadoc`， `pom`。

## `cloud.tenon.gradle.convention`

`Java` 项目默认配置

## `cloud.tenon.gradle.maven.option`

`Maven` 可选依赖

```groovy
dependencies {
    optional("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("org.junit.jupiter:junit-jupiter")
}
```

## `cloud.tenon.gradle.maven.deployed`

`Maven` 发布插件


## `cloud.tenon.gradle.maven.repository`

`Maven` 发布源管理

## 
# **感谢**

感谢 `Spring Boot` 的构建脚本和对应的开发人员
