/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright 2017 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.ipc.s3;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.aws.TestContext;
import com.nextdoor.bender.ipc.TransportException;


public class S3TransporterTest {

  private AmazonS3Client getMockClient() {
    AmazonS3Client mockClient = spy(AmazonS3Client.class);
    UploadPartResult uploadResult = new UploadPartResult();
    uploadResult.setETag("foo");
    doReturn(uploadResult).when(mockClient).uploadPart(any(UploadPartRequest.class));

    InitiateMultipartUploadResult initUploadResult = new InitiateMultipartUploadResult();
    initUploadResult.setUploadId("123");
    doReturn(initUploadResult).when(mockClient)
        .initiateMultipartUpload(any(InitiateMultipartUploadRequest.class));

    return mockClient;
  }

  @Test
  public void testUnpartitioned() throws TransportException, IllegalStateException, IOException {
    /*
     * Create mock client, requets, and replies
     */
    AmazonS3Client mockClient = getMockClient();

    /*
     * Fill buffer with mock data
     */
    S3TransportBuffer buffer = new S3TransportBuffer(1000, false, new S3TransportSerializer());
    InternalEvent mockIevent = mock(InternalEvent.class);
    doReturn("foo").when(mockIevent).getSerialized();

    /*
     * Create transport
     */
    Map<String, MultiPartUpload> multiPartUploads = new HashMap<String, MultiPartUpload>(0);
    S3Transport transport =
        new S3Transport(mockClient, "bucket", "basepath", false, multiPartUploads);

    /*
     * Do actual test
     */
    buffer.add(mockIevent);
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>();
    partitions.put(S3Transport.FILENAME_KEY, "a_filename");

    ArgumentCaptor<UploadPartRequest> argument = ArgumentCaptor.forClass(UploadPartRequest.class);
    transport.sendBatch(buffer, partitions, new TestContext());
    verify(mockClient).uploadPart(argument.capture());

    /*
     * Check results
     */
    assertEquals("bucket", argument.getValue().getBucketName());
    assertEquals("basepath/a_filename", argument.getValue().getKey());
    assertEquals(1, argument.getValue().getPartNumber());
    assertEquals(4, argument.getValue().getPartSize()); // foo\n
    assertEquals("123", argument.getValue().getUploadId());
  }

  @Test
  public void testPartitioned() throws TransportException, IllegalStateException, IOException {
    /*
     * Create mock client, requets, and replies
     */
    AmazonS3Client mockClient = getMockClient();

    /*
     * Fill buffer with mock data
     */
    S3TransportBuffer buffer = new S3TransportBuffer(1000, false, new S3TransportSerializer());
    InternalEvent mockIevent = mock(InternalEvent.class);
    doReturn("foo").when(mockIevent).getSerialized();

    /*
     * Create transport
     */
    Map<String, MultiPartUpload> multiPartUploads = new HashMap<String, MultiPartUpload>(0);
    S3Transport transport =
        new S3Transport(mockClient, "bucket", "basepath", false, multiPartUploads);

    /*
     * Do actual test
     */
    buffer.add(mockIevent);
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>();
    partitions.put(S3Transport.FILENAME_KEY, "a_filename");
    partitions.put("day", "01");
    partitions.put("hour", "23");

    ArgumentCaptor<UploadPartRequest> argument = ArgumentCaptor.forClass(UploadPartRequest.class);
    transport.sendBatch(buffer, partitions, new TestContext());
    verify(mockClient).uploadPart(argument.capture());

    /*
     * Check results
     */
    assertEquals("bucket", argument.getValue().getBucketName());
    assertEquals("basepath/day=01/hour=23/a_filename", argument.getValue().getKey());
    assertEquals(1, argument.getValue().getPartNumber());
    assertEquals(4, argument.getValue().getPartSize()); // foo\n
    assertEquals("123", argument.getValue().getUploadId());
  }

  @Test
  public void testCompressedPartitoned()
      throws TransportException, IllegalStateException, IOException {
    /*
     * Create mock client, requets, and replies
     */
    AmazonS3Client mockClient = getMockClient();

    /*
     * Fill buffer with mock data
     */
    S3TransportBuffer buffer = new S3TransportBuffer(1000, true, new S3TransportSerializer());
    InternalEvent mockIevent = mock(InternalEvent.class);
    doReturn("foo").when(mockIevent).getSerialized();

    /*
     * Create transport
     */
    Map<String, MultiPartUpload> multiPartUploads = new HashMap<String, MultiPartUpload>(0);
    S3Transport transport =
        new S3Transport(mockClient, "bucket", "basepath", true, multiPartUploads);

    /*
     * Do actual test
     */
    buffer.add(mockIevent);
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>();
    partitions.put(S3Transport.FILENAME_KEY, "a_filename");
    partitions.put("day", "01");
    partitions.put("hour", "23");

    ArgumentCaptor<UploadPartRequest> argument = ArgumentCaptor.forClass(UploadPartRequest.class);
    transport.sendBatch(buffer, partitions, new TestContext());
    verify(mockClient).uploadPart(argument.capture());

    /*
     * Check results
     */
    assertEquals("bucket", argument.getValue().getBucketName());
    assertEquals("basepath/day=01/hour=23/a_filename.bz2", argument.getValue().getKey());
    assertEquals(1, argument.getValue().getPartNumber());
    assertEquals(3, argument.getValue().getPartSize());
    assertEquals("123", argument.getValue().getUploadId());
  }

  @Test
  public void testCompressedBuffer() throws TransportException, IllegalStateException, IOException {
    /*
     * Create mock client, requets, and replies
     */
    AmazonS3Client mockClient = getMockClient();

    /*
     * Capture the InputStream into a ByteArrayOutputStream before the Transport thread closes the
     * InputStream and makes it unavailable for reading.
     */
    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    Answer answer = new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        UploadPartRequest req = invocation.getArgumentAt(0, UploadPartRequest.class);
        captured.write(req.getInputStream());
        return new UploadPartResult();
      }
    };

    Mockito.doAnswer(answer).when(mockClient).uploadPart(any(UploadPartRequest.class));

    /*
     * Fill buffer with mock data
     */
    S3TransportBuffer buffer = new S3TransportBuffer(1000, true, new S3TransportSerializer());
    InternalEvent mockIevent = mock(InternalEvent.class);
    doReturn("foo").when(mockIevent).getSerialized();

    /*
     * Create transport
     */
    Map<String, MultiPartUpload> multiPartUploads = new HashMap<String, MultiPartUpload>(0);
    S3Transport transport =
        new S3Transport(mockClient, "bucket", "basepath", true, multiPartUploads);

    /*
     * Do actual test
     */
    buffer.add(mockIevent);
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>();
    partitions.put(S3Transport.FILENAME_KEY, "a_filename");
    ArgumentCaptor<UploadPartRequest> argument = ArgumentCaptor.forClass(UploadPartRequest.class);

    buffer.close();
    transport.sendBatch(buffer, partitions, new TestContext());
    verify(mockClient).uploadPart(argument.capture());

    /*
     * Check results
     */
    assertEquals("bucket", argument.getValue().getBucketName());
    assertEquals("basepath/a_filename.bz2", argument.getValue().getKey());
    assertEquals(1, argument.getValue().getPartNumber());
    assertEquals(40, argument.getValue().getPartSize());
    assertEquals("123", argument.getValue().getUploadId());

    /*
     * Convert the actual InputStream from the client into a ByteArrayOutputStream which can be read
     * and verified.
     */
    byte[] actualBytes = captured.toByteArray();
    byte[] expectedBytes =
        {66, 90, 104, 57, 49, 65, 89, 38, 83, 89, 118, -10, -77, -27, 0, 0, 0, -63, 0, 0, 16, 1, 0,
            -96, 0, 48, -52, 12, -62, 12, 46, -28, -118, 112, -95, 32, -19, -19, 103, -54};

    assertArrayEquals(expectedBytes, actualBytes);
  }

  @Test
  public void testCompressed() throws TransportException, IllegalStateException, IOException {
    /*
     * Create mock client, requets, and replies
     */
    AmazonS3Client mockClient = getMockClient();

    /*
     * Capture the InputStream into a ByteArrayOutputStream before the Transport thread closes the
     * InputStream and makes it unavailable for reading.
     */
    ByteArrayOutputStream captured = new ByteArrayOutputStream();
    Answer answer = new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        UploadPartRequest req = invocation.getArgumentAt(0, UploadPartRequest.class);
        captured.write(req.getInputStream());
        return new UploadPartResult();
      }
    };

    Mockito.doAnswer(answer).when(mockClient).uploadPart(any(UploadPartRequest.class));

    /*
     * Fill buffer with mock data
     */
    S3TransportBuffer buffer = new S3TransportBuffer(1000, false, new S3TransportSerializer());
    InternalEvent mockIevent = mock(InternalEvent.class);
    doReturn("foo").when(mockIevent).getSerialized();

    /*
     * Create transport
     */
    Map<String, MultiPartUpload> multiPartUploads = new HashMap<String, MultiPartUpload>(0);
    S3Transport transport =
        new S3Transport(mockClient, "bucket", "basepath", true, multiPartUploads);

    /*
     * Do actual test
     */
    buffer.add(mockIevent);
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>();
    partitions.put(S3Transport.FILENAME_KEY, "a_filename");
    ArgumentCaptor<UploadPartRequest> argument = ArgumentCaptor.forClass(UploadPartRequest.class);

    buffer.close();
    transport.sendBatch(buffer, partitions, new TestContext());
    verify(mockClient).uploadPart(argument.capture());

    /*
     * Check results
     */
    assertEquals("bucket", argument.getValue().getBucketName());
    assertEquals("basepath/a_filename.bz2", argument.getValue().getKey());
    assertEquals(1, argument.getValue().getPartNumber());
    assertEquals(40, argument.getValue().getPartSize());
    assertEquals("123", argument.getValue().getUploadId());

    /*
     * Convert the actual InputStream from the client into a ByteArrayOutputStream which can be read
     * and verified.
     */
    byte[] actualBytes = captured.toByteArray();
    byte[] expectedBytes =
        {66, 90, 104, 57, 49, 65, 89, 38, 83, 89, 118, -10, -77, -27, 0, 0, 0, -63, 0, 0, 16, 1, 0,
            -96, 0, 48, -52, 12, -62, 12, 46, -28, -118, 112, -95, 32, -19, -19, 103, -54};

    assertArrayEquals(expectedBytes, actualBytes);
  }

  @Test
  public void testGzFilename() throws TransportException, IllegalStateException, IOException {
    /*
     * Create mock client, requests, and replies
     */
    AmazonS3Client mockClient = getMockClient();

    /*
     * Fill buffer with mock data
     */
    S3TransportBuffer buffer = new S3TransportBuffer(1000, true, new S3TransportSerializer());
    InternalEvent mockIevent = mock(InternalEvent.class);
    doReturn("foo").when(mockIevent).getSerialized();

    /*
     * Create transport
     */
    Map<String, MultiPartUpload> multiPartUploads = new HashMap<String, MultiPartUpload>(0);
    S3Transport transport =
        new S3Transport(mockClient, "bucket", "basepath/", true, multiPartUploads);

    /*
     * Do actual test
     */
    buffer.add(mockIevent);
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>();
    partitions.put(S3Transport.FILENAME_KEY, "a_filename.gz");
    ArgumentCaptor<UploadPartRequest> argument = ArgumentCaptor.forClass(UploadPartRequest.class);
    transport.sendBatch(buffer, partitions, new TestContext());
    verify(mockClient).uploadPart(argument.capture());

    /*
     * Check results
     */
    assertEquals("basepath/a_filename.bz2", argument.getValue().getKey());
  }

  @Test
  public void testContextBasedFilename()
      throws TransportException, IllegalStateException, IOException {
    /*
     * Create mock client, requests, and replies
     */
    AmazonS3Client mockClient = getMockClient();

    /*
     * Fill buffer with mock data
     */
    S3TransportBuffer buffer = new S3TransportBuffer(1000, true, new S3TransportSerializer());
    InternalEvent mockIevent = mock(InternalEvent.class);
    doReturn("foo").when(mockIevent).getSerialized();

    /*
     * Create transport
     */
    Map<String, MultiPartUpload> multiPartUploads = new HashMap<String, MultiPartUpload>(0);
    S3Transport transport =
        new S3Transport(mockClient, "bucket", "basepath/", true, multiPartUploads);

    /*
     * Do actual test
     */
    buffer.add(mockIevent);
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>();
    ArgumentCaptor<UploadPartRequest> argument = ArgumentCaptor.forClass(UploadPartRequest.class);
    TestContext context = new TestContext();
    context.setAwsRequestId("request_id");
    transport.sendBatch(buffer, partitions, context);
    verify(mockClient).uploadPart(argument.capture());

    /*
     * Check results
     */
    assertEquals("basepath/request_id.bz2", argument.getValue().getKey());
  }

  @Test
  public void testMultipleUploads() throws TransportException, IllegalStateException, IOException {
    /*
     * Create mock client, requets, and replies
     */
    AmazonS3Client mockClient = getMockClient();

    /*
     * Fill buffer with mock data
     */
    S3TransportBuffer buffer1 = new S3TransportBuffer(1000, false, new S3TransportSerializer());
    S3TransportBuffer buffer2 = new S3TransportBuffer(1000, false, new S3TransportSerializer());
    InternalEvent mockIevent = mock(InternalEvent.class);
    doReturn("foo").doReturn("bar1").when(mockIevent).getSerialized();
    buffer1.add(mockIevent);
    buffer2.add(mockIevent);

    /*
     * Create transport
     */
    Map<String, MultiPartUpload> multiPartUploads = new HashMap<String, MultiPartUpload>(0);
    S3Transport transport =
        new S3Transport(mockClient, "bucket", "basepath", false, multiPartUploads);

    /*
     * Do actual test
     */
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>();
    partitions.put(S3Transport.FILENAME_KEY, "a_filename");
    ArgumentCaptor<UploadPartRequest> argument = ArgumentCaptor.forClass(UploadPartRequest.class);

    transport.sendBatch(buffer1, partitions, new TestContext());
    transport.sendBatch(buffer2, partitions, new TestContext());

    verify(mockClient, times(2)).uploadPart(argument.capture());

    List<UploadPartRequest> arguments = argument.getAllValues();

    assertEquals(1, arguments.get(0).getPartNumber());
    assertEquals(4, arguments.get(0).getPartSize()); // foo\n
    assertEquals("123", arguments.get(0).getUploadId());

    assertEquals(2, arguments.get(1).getPartNumber());
    assertEquals(5, arguments.get(1).getPartSize()); // bar1\n
    assertEquals("123", arguments.get(1).getUploadId());
  }

  @Test(expected = TransportException.class)
  public void testAmazonClientException()
      throws TransportException, IllegalStateException, IOException {
    /*
     * Create mock client, requets, and replies
     */
    AmazonS3Client mockClient = mock(AmazonS3Client.class);
    UploadPartResult uploadResult = new UploadPartResult();
    uploadResult.setETag("foo");
    doThrow(new AmazonClientException("expected")).when(mockClient)
        .uploadPart(any(UploadPartRequest.class));

    InitiateMultipartUploadResult initUploadResult = new InitiateMultipartUploadResult();
    initUploadResult.setUploadId("123");
    doReturn(initUploadResult).when(mockClient)
        .initiateMultipartUpload(any(InitiateMultipartUploadRequest.class));

    /*
     * Fill buffer with mock data
     */
    S3TransportBuffer buffer = new S3TransportBuffer(1000, false, new S3TransportSerializer());
    InternalEvent mockIevent = mock(InternalEvent.class);
    doReturn("foo").when(mockIevent).getSerialized();

    /*
     * Create transport
     */
    Map<String, MultiPartUpload> multiPartUploads = new HashMap<String, MultiPartUpload>(0);
    S3Transport transport =
        new S3Transport(mockClient, "bucket", "basepath", false, multiPartUploads);

    /*
     * Do actual test
     */
    buffer.add(mockIevent);
    LinkedHashMap<String, String> partitions = new LinkedHashMap<String, String>();
    partitions.put(S3Transport.FILENAME_KEY, "a_filename");

    ArgumentCaptor<UploadPartRequest> argument = ArgumentCaptor.forClass(UploadPartRequest.class);
    try {
      transport.sendBatch(buffer, partitions, new TestContext());
    } catch (Exception e) {
      assertEquals(e.getCause().getClass(), AmazonClientException.class);
      throw e;
    }
  }
}
