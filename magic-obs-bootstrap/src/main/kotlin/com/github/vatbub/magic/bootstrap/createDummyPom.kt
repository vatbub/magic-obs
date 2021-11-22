/*-
 * #%L
 * magic-obs-bootstrap
 * %%
 * Copyright (C) 2019 - 2021 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.magic.bootstrap

import java.net.URL

fun createDummyPom(
    groupId: String,
    artifactId: String,
    initialVersion: String,
    mainClass: String,
    allowSnapshots: Boolean,
    customReleaseRepoUrl: URL?,
    customSnapshotRepoUrl: URL?
): String = """
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
    
        <groupId>com.github.vatbub</groupId>
        <artifactId>placeholderPom</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <packaging>jar</packaging>
    
        <repositories>
        """.trimIndent() +
        (if (customReleaseRepoUrl != null)
            """
                <repository>
                    <id>custom-release-repo</id>
                    <url>${customReleaseRepoUrl.toExternalForm()}</url>
                </repository>
            """.trimIndent()
        else "") +
        (if (customSnapshotRepoUrl != null)
            """
            <repository>
                <id>custom-snapshot-repo</id>
                <url>${customSnapshotRepoUrl.toExternalForm()}</url>
                <snapshots>
                    <enabled>true</enabled>
                </snapshots>
            </repository>
            """
        else "") +
        """
                </repositories>
           
                <dependencies>
                    <dependency>
                        <groupId>${groupId}</groupId>
                        <artifactId>${artifactId}</artifactId>
                        <version>${initialVersion}</version>
                    </dependency>
                </dependencies>
           
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.codehaus.mojo</groupId>
                        <artifactId>exec-maven-plugin</artifactId>
                        <version>3.0.0</version>
                        <configuration>
                            <mainClass>${mainClass}</mainClass>
                        </configuration>
                    </plugin>
                    <plugin>
                        <groupId>org.codehaus.mojo</groupId>
                        <artifactId>versions-maven-plugin</artifactId>
                        <version>2.8.1</version>
                        <configuration>
                            <allowSnapshots>$allowSnapshots</allowSnapshots>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </project>
        """.trimIndent()
