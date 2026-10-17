/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.hudi;

import io.trino.plugin.hive.containers.Hive3FlociDataLake;
import io.trino.plugin.hudi.testing.TpchHudiTablesInitializer;
import io.trino.testing.QueryRunner;
import org.testcontainers.DockerClientFactory;

import static io.trino.plugin.hive.containers.HiveHadoop.HIVE3_IMAGE;
import static io.trino.plugin.hudi.testing.HudiTestUtils.COLUMNS_TO_HIDE;
import static io.trino.testing.TestingNames.randomNameSuffix;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TestHudiFlociConnectorSmokeTest
        extends BaseHudiConnectorSmokeTest
{
    @Override
    protected QueryRunner createQueryRunner()
            throws Exception
    {
        // The Floci S3 endpoint and the Hive 3 metastore are Linux containers. Hosts without a
        // container runtime (e.g. GitHub's Apple Silicon macOS runners, which lack nested
        // virtualization) cannot start them; the connector itself is still covered there by
        // TestHudiSmokeTest and the other local-filesystem suites.
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker environment is not available");
        String bucketName = "test-hudi-connector-" + randomNameSuffix();
        Hive3FlociDataLake hiveFlociDataLake = closeAfterClass(new Hive3FlociDataLake(bucketName, HIVE3_IMAGE));
        hiveFlociDataLake.start();

        return HudiQueryRunner.builder(hiveFlociDataLake)
                .addConnectorProperty("hudi.columns-to-hide", COLUMNS_TO_HIDE)
                .setDataLoader(new TpchHudiTablesInitializer(REQUIRED_TPCH_TABLES))
                .build();
    }
}
