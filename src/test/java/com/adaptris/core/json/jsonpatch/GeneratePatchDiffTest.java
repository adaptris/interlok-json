/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
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
 *******************************************************************************/
package com.adaptris.core.json.jsonpatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.MetadataInputStreamWrapper;
import com.adaptris.core.common.PayloadInputStreamWrapper;
import com.adaptris.core.common.PayloadOutputStreamWrapper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;
import com.flipkart.zjsonpatch.DiffFlags;

public class GeneratePatchDiffTest extends ExampleServiceCase {

  private static final String DIFF_SOURCE = "{\"a\": 0,\"b\": [1,2]}";
  private static final String DIFF_TARGET = " {\"b\": [1,2,0]}";
  private static final String PATCH_TRANSFORM =
      "[{\"op\":\"move\",\"from\":\"/a\",\"path\":\"/b/2\"}]";


  @Test
  public void testFlags() throws Exception {
    GeneratePatchDiffService service =
        new GeneratePatchDiffService();
    assertNotNull(service.getFlags());
    assertNotNull(service.flags());
    assertEquals(DiffFlags.defaults(), service.flags());
    service = new GeneratePatchDiffService().withFlags(PatchDiffFlag.OMIT_COPY_OPERATION,
        PatchDiffFlag.OMIT_MOVE_OPERATION, PatchDiffFlag.ADD_ORIGINAL_VALUE_ON_REPLACE,
        PatchDiffFlag.EMIT_TEST_OPERATIONS, PatchDiffFlag.OMIT_VALUE_ON_REMOVE);
    assertNotNull(service.getFlags());
    assertNotNull(service.flags());
    assertTrue(service.flags().contains(DiffFlags.OMIT_COPY_OPERATION));
    assertTrue(service.flags().contains(DiffFlags.OMIT_MOVE_OPERATION));
  }

  @Test
  public void testService() throws Exception {
    MetadataInputStreamWrapper diffSource = new MetadataInputStreamWrapper("diffSource");
    MetadataInputStreamWrapper diffTarget = new MetadataInputStreamWrapper("diffTarget");
    GeneratePatchDiffService service =
        new GeneratePatchDiffService()
            .withDiffSource(diffSource)
            .withDiffTarget(diffTarget)
            .withOutput(new PayloadOutputStreamWrapper());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMessageHeader("diffSource", DIFF_SOURCE);
    msg.addMessageHeader("diffTarget", DIFF_TARGET);
    execute(service, msg);
    JSONAssert.assertEquals(PATCH_TRANSFORM, msg.getContent(), false);
  }

  @Test
  public void testService_BrokenSource() throws Exception {
    MetadataInputStreamWrapper diffSource = new MetadataInputStreamWrapper("diffSource");
    BrokenWrapper diffTarget = new BrokenWrapper();
    GeneratePatchDiffService service = new GeneratePatchDiffService()
        .withDiffSource(diffSource)
        .withDiffTarget(diffTarget)
        .withOutput(new PayloadOutputStreamWrapper());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMessageHeader("diffSource", DIFF_SOURCE);
    msg.addMessageHeader("diffTarget", DIFF_TARGET);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {
      ;
    }
  }

  @Override
  protected GeneratePatchDiffService retrieveObjectForSampleConfig() {
    GeneratePatchDiffService service = new GeneratePatchDiffService()
        .withDiffSource(new PayloadInputStreamWrapper())
        .withDiffTarget(new MetadataInputStreamWrapper("metadata key containing another json object"))
        .withFlags(PatchDiffFlag.OMIT_MOVE_OPERATION, PatchDiffFlag.OMIT_COPY_OPERATION)
        .withOutput(new PayloadOutputStreamWrapper());

    return service;
  }
}
