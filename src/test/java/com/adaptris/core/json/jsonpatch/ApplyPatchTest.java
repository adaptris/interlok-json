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
import com.flipkart.zjsonpatch.CompatibilityFlags;

public class ApplyPatchTest extends ServiceCase {

  private static final String SOURCE = "{\"a\": 0,\"b\": [1,2]}";
  private static final String TARGET = " {\"b\": [1,2,0]}";
  private static final String PATCH_TRANSFORM =
      "[{\"op\":\"move\",\"from\":\"/a\",\"path\":\"/b/2\"}]";

  public void testFlags() throws Exception {
    ApplyPatchService service = new ApplyPatchService();
    assertNull(service.getFlags());
    assertNotNull(service.flags());
    assertEquals(CompatibilityFlags.defaults(), service.flags());
    service = new ApplyPatchService().withFlags(CompatibilityFlags.MISSING_VALUES_AS_NULLS,
        CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT);
    assertNotNull(service.getFlags());
    assertNotNull(service.flags());
    assertTrue(service.flags().contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS));
  }

  public void testService() throws Exception {
    MetadataStreamInputParameter patchSource = new MetadataStreamInputParameter("patchSource");
    ApplyPatchService service = new ApplyPatchService().withPatchSource(patchSource)
        .withSource(new PayloadStreamInputParameter())
        .withOutput(new PayloadStreamOutputParameter());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SOURCE);
    msg.addMessageHeader("patchSource", PATCH_TRANSFORM);
    execute(service, msg);
    JSONAssert.assertEquals(TARGET, msg.getContent(), false);
  }

  public void testService_BrokenSource() throws Exception {
    BrokenWrapper patchSource = new BrokenWrapper();
    ApplyPatchService service = new ApplyPatchService().withPatchSource(patchSource)
        .withSource(new PayloadStreamInputParameter())
        .withOutput(new PayloadStreamOutputParameter());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SOURCE);
    msg.addMessageHeader("patchSource", PATCH_TRANSFORM);
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {
      ;
    }
  }


  @Override
  protected ApplyPatchService retrieveObjectForSampleConfig() {
    ApplyPatchService service =
        new ApplyPatchService().withSource(new PayloadStreamInputParameter())
            .withPatchSource(new MetadataStreamInputParameter("metadata key"))
            .withOutput(new PayloadStreamOutputParameter());
    return service;
  }
}
