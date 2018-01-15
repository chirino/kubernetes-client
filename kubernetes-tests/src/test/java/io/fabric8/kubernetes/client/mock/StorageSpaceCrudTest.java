/**
 * Copyright (C) 2018 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fabric8.kubernetes.client.mock;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.StorageClass;
import io.fabric8.kubernetes.api.model.StorageClassBuilder;
import io.fabric8.kubernetes.api.model.StorageClassList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class StorageSpaceCrudTest {

  private static final Logger logger = LoggerFactory.getLogger(StorageSpaceCrudTest.class);

  @Rule
  public KubernetesServer server = new KubernetesServer(true, true);

  @Test
  public void testCrud() {
    KubernetesClient client = server.getClient();

    final String name = "test";
    ObjectMeta metadata = new ObjectMeta();
    metadata.setName(name);

    Map<String, String> parameters = new HashMap<>();
    parameters.put("key", "value");

    StorageClass storageClass = new StorageClassBuilder().withApiVersion("storage.k8s.io/v1")
      .withKind("StorageClass")
      .withMetadata(metadata)
      .withParameters(parameters)
      .withProvisioner("kubernetes.io/aws-ebs")
      .build();

    //test create
    storageClass = client.storageClasses().create(storageClass);
    assertNotNull(storageClass);
    assertEquals(name, storageClass.getMetadata().getName());

    //test get
    StorageClassList storageClassList = client.storageClasses().list();
    logger.info(storageClassList.toString());
    assertNotNull(storageClassList);
    assertEquals(1, storageClassList.getItems().size());
    assertEquals("kubernetes.io/aws-ebs", storageClassList.getItems().get(0).getProvisioner());
    assertEquals("value", storageClassList.getItems().get(0).getParameters().get("key"));
    assertEquals(0, storageClassList.getItems().get(0).getMetadata().getLabels().size());

    //test update
    storageClass = client.storageClasses().withName(name).edit().editOrNewMetadata().addToLabels("key1", "value1").endMetadata().done();
    logger.info("Updated Storage Class: {} ", storageClass.toString());
    assertNotNull(storageClass);
    assertEquals(1, storageClass.getMetadata().getLabels().size());

    //test delete
    boolean result = client.storageClasses().delete(storageClass);
    assertEquals(true, result);
    StorageClassList storageClassList2 = client.storageClasses().list();
    assertEquals(0, storageClassList2.getItems().size());

  }
}
