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

import org.skyscreamer.jsonassert.JSONAssert;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.MetadataStreamInputParameter;
import com.adaptris.core.common.PayloadStreamInputParameter;
import com.adaptris.core.common.PayloadStreamOutputParameter;
import com.flipkart.zjsonpatch.DiffFlags;

public class GeneratePatchDiffTest extends ServiceCase {

  private static final String DIFF_SOURCE = "{\"a\": 0,\"b\": [1,2]}";
  private static final String DIFF_TARGET = " {\"b\": [1,2,0]}";
  private static final String PATCH_TRANSFORM =
      "[{\"op\":\"move\",\"from\":\"/a\",\"path\":\"/b/2\"}]";
  public void testFlags() throws Exception {
    GeneratePatchDiffService service =
        new GeneratePatchDiffService();
    assertNull(service.getFlags());
    assertNotNull(service.flags());
    assertEquals(DiffFlags.defaults(), service.flags());
    service = new GeneratePatchDiffService().withFlags(DiffFlags.OMIT_COPY_OPERATION,
        DiffFlags.OMIT_COPY_OPERATION);
    assertNotNull(service.getFlags());
    assertNotNull(service.flags());
    assertTrue(service.flags().contains(DiffFlags.OMIT_COPY_OPERATION));
    assertFalse(service.flags().contains(DiffFlags.EMIT_TEST_OPERATIONS));
  }

  public void testService() throws Exception {
    MetadataStreamInputParameter diffSource = new MetadataStreamInputParameter("diffSource");
    MetadataStreamInputParameter diffTarget = new MetadataStreamInputParameter("diffTarget");
    GeneratePatchDiffService service =
        new GeneratePatchDiffService()
            .withDiffSource(diffSource)
            .withDiffTarget(diffTarget)
            .withOutput(new PayloadStreamOutputParameter());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMessageHeader("diffSource", DIFF_SOURCE);
    msg.addMessageHeader("diffTarget", DIFF_TARGET);
    execute(service, msg);
    JSONAssert.assertEquals(PATCH_TRANSFORM, msg.getContent(), false);
  }

  public void testService_BrokenSource() throws Exception {
    MetadataStreamInputParameter diffSource = new MetadataStreamInputParameter("diffSource");
    BrokenWrapper diffTarget = new BrokenWrapper();
    GeneratePatchDiffService service = new GeneratePatchDiffService()
        .withDiffSource(diffSource)
        .withDiffTarget(diffTarget)
        .withOutput(new PayloadStreamOutputParameter());
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
        .withDiffSource(new PayloadStreamInputParameter())
        .withDiffTarget(new MetadataStreamInputParameter("metadata key"))
        .withOutput(new PayloadStreamOutputParameter());
    
    return service;
  }
}
