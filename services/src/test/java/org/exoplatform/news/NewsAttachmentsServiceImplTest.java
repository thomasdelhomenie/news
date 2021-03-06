package org.exoplatform.news;

import org.exoplatform.news.model.NewsAttachment;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionManager;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionMode;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionType;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class NewsAttachmentsServiceImplTest {

  @Mock
  RepositoryService       repositoryService;

  @Mock
  SessionProviderService  sessionProviderService;

  @Mock
  ManageableRepository repository;

  @Mock
  RepositoryEntry repositoryEntry;

  @Mock
  SessionProvider sessionProvider;

  @Mock
  Session session;

  @Mock
  NodeHierarchyCreator    nodeHierarchyCreator;

  @Mock
  DataDistributionManager dataDistributionManager;

  @Mock
  SpaceService            spaceService;

  @Mock
  UploadService           uploadService;

  @Mock
  DocumentService         documentService;

  @Test
  public void shouldGetAttachmentWhenItExists() throws Exception {
    // Given
    NewsAttachmentsService newsAttachmentsService = new NewsAttachmentsServiceImpl(sessionProviderService,
                                                                                   repositoryService,
                                                                                   nodeHierarchyCreator,
                                                                                   dataDistributionManager,
                                                                                   spaceService,
                                                                                   uploadService,
                                                                                   documentService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    when(node.getUUID()).thenReturn("id123");
    when(node.getName()).thenReturn("name123");
    when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Node resourceNode = mock(Node.class);
    Property mimetypeProperty = mock(Property.class);
    when(mimetypeProperty.getString()).thenReturn("image/png");
    when(resourceNode.hasProperty(eq("jcr:mimeType"))).thenReturn(true);
    when(resourceNode.getProperty(eq("jcr:mimeType"))).thenReturn(mimetypeProperty);
    Property dataProperty = mock(Property.class);
    when(dataProperty.getStream()).thenReturn(new ByteArrayInputStream("image".getBytes()));
    when(resourceNode.getProperty(eq("jcr:data"))).thenReturn(dataProperty);
    when(node.getNode(eq("jcr:content"))).thenReturn(resourceNode);


    // When
    NewsAttachment newsAttachment = newsAttachmentsService.getNewsAttachment("id123");

    // Then
    assertNotNull(newsAttachment);
    assertEquals("id123", newsAttachment.getId());
    assertEquals("name123", newsAttachment.getName());
    assertEquals("image/png", newsAttachment.getMimetype());
    assertEquals("image".getBytes().length, newsAttachment.getSize());
  }

  @Test
  public void shouldNotGetAttachmentWhenItDoesNotExist() throws Exception {
    // Given
    NewsAttachmentsService newsAttachmentsService = new NewsAttachmentsServiceImpl(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(eq("id123"))).thenReturn(null);


    // When
    NewsAttachment newsAttachment = newsAttachmentsService.getNewsAttachment("id123");

    // Then
    assertNull(newsAttachment);
  }

  @Test
  public void shouldNotGetAttachmentWhenItHasNoResourceNode() throws Exception {
    // Given
    NewsAttachmentsService newsAttachmentsService = new NewsAttachmentsServiceImpl(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    when(node.getUUID()).thenReturn("id123");
    when(node.getName()).thenReturn("name123");
    when(node.getNode(eq("jcr:content"))).thenReturn(null);
    when(session.getNodeByUUID(eq("id123"))).thenReturn(node);


    // When
    NewsAttachment newsAttachment = newsAttachmentsService.getNewsAttachment("id123");

    // Then
    assertNotNull(newsAttachment);
    assertEquals("id123", newsAttachment.getId());
    assertEquals("name123", newsAttachment.getName());
    assertEquals(0, newsAttachment.getSize());
    assertEquals("", newsAttachment.getMimetype());
  }

  @Test
  public void shouldGetAttachmentsOfANewsWhenThereAre() throws Exception {
    // Given
    NewsAttachmentsService newsAttachmentsService = new NewsAttachmentsServiceImpl(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    when(node.getUUID()).thenReturn("id123");
    when(node.getName()).thenReturn("name123");
    when(node.getSession()).thenReturn(session);
    Property attachmentIdsProperty = mock(Property.class);
    when(attachmentIdsProperty.getValues()).thenReturn(new Value[] { new StringValue("idAttach1"), new StringValue("idAttach2"), new StringValue("idAttach3") });
    when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Node attachmentNode = mock(Node.class);
    when(session.getNodeByUUID(eq("idAttach1"))).thenReturn(attachmentNode);
    when(session.getNodeByUUID(eq("idAttach2"))).thenReturn(attachmentNode);
    when(session.getNodeByUUID(eq("idAttach3"))).thenReturn(attachmentNode);


    // When
    List<NewsAttachment> newsAttachments = newsAttachmentsService.getNewsAttachments(node);

    // Then
    assertNotNull(newsAttachments);
    assertEquals(3, newsAttachments.size());
  }

  @Test
  public void shouldGetNoAttachmentsOfANewsWhenThereAreNot() throws Exception {
    // Given
    NewsAttachmentsService newsAttachmentsService = new NewsAttachmentsServiceImpl(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    when(node.getUUID()).thenReturn("id123");
    when(node.getName()).thenReturn("name123");
    when(node.getSession()).thenReturn(session);
    Property attachmentIdsProperty = mock(Property.class);
    when(attachmentIdsProperty.getValues()).thenReturn(new Value[] {});
    when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentIdsProperty);
    when(session.getNodeByUUID(eq("id123"))).thenReturn(node);


    // When
    List<NewsAttachment> newsAttachments = newsAttachmentsService.getNewsAttachments(node);

    // Then
    assertNotNull(newsAttachments);
    assertEquals(0, newsAttachments.size());
  }

  @Test
  public void shouldGetAttachmentStreamWhenAttachmentExists() throws Exception {
    // Given
    NewsAttachmentsService newsAttachmentsService = new NewsAttachmentsServiceImpl(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    when(node.getUUID()).thenReturn("id123");
    when(node.getName()).thenReturn("name123");
    when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    Node resourceNode = mock(Node.class);
    Property dataProperty = mock(Property.class);
    when(dataProperty.getStream()).thenReturn(new ByteArrayInputStream("image".getBytes()));
    when(resourceNode.getProperty(eq("jcr:data"))).thenReturn(dataProperty);
    when(node.getNode(eq("jcr:content"))).thenReturn(resourceNode);


    // When
    InputStream stream = newsAttachmentsService.getNewsAttachmentStream("id123");

    // Then
    assertNotNull(stream);
    assertEquals("image".getBytes().length, stream.available());
  }

  @Test
  public void shouldNotGetAttachmentStreamWhenAttachmentDoesNotExist() throws Exception {
    // Given
    NewsAttachmentsService newsAttachmentsService = new NewsAttachmentsServiceImpl(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(eq("id123"))).thenReturn(null);


    // When
    InputStream stream = newsAttachmentsService.getNewsAttachmentStream("id123");

    // Then
    assertNull(stream);
  }

  @Test
  public void shouldAddAttachmentFromUploadResource() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(eq(DataDistributionMode.NONE))).thenReturn(dataDistributionType);
    NewsAttachmentsService newsAttachmentsService = new NewsAttachmentsServiceImpl(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    when(node.getUUID()).thenReturn("id123");
    when(node.getName()).thenReturn("name123");
    Property spacesIdProperty = mock(Property.class);
    when(spacesIdProperty.getString()).thenReturn("1");
    when(node.hasProperty(eq("exo:spaceId"))).thenReturn(true);
    when(node.getProperty(eq("exo:spaceId"))).thenReturn(spacesIdProperty);
    when(node.getSession()).thenReturn(session);
    Node spaceDocumentsFolderNode = mock(Node.class);
    when(session.getItem(anyString())).thenReturn(spaceDocumentsFolderNode);
    when(spaceDocumentsFolderNode.hasNode(eq(NewsAttachmentsServiceImpl.NEWS_ATTACHMENTS_NODES_FOLDER))).thenReturn(true);
    Node spaceNewsRootNode = mock(Node.class);
    when(spaceDocumentsFolderNode.getNode(eq(NewsAttachmentsServiceImpl.NEWS_ATTACHMENTS_NODES_FOLDER))).thenReturn(spaceNewsRootNode);
    Node newsAttachmentsFolderNode = mock(Node.class);
    when(dataDistributionType.getOrCreateDataNode(any(), anyString())).thenReturn(newsAttachmentsFolderNode);
    Node newsAttachmentsNode = mock(Node.class);
    when(newsAttachmentsNode.getUUID()).thenReturn("attachId1");
    when(newsAttachmentsFolderNode.addNode(anyString(), anyString())).thenReturn(newsAttachmentsNode);
    Node newsAttachmentsResourceNode = mock(Node.class);
    when(newsAttachmentsNode.addNode(anyString(), anyString())).thenReturn(newsAttachmentsResourceNode);
    when(session.getNodeByUUID(eq("id123"))).thenReturn(node);
    when(spaceService.getSpaceById(eq("1"))).thenReturn(new Space());
    String uploadId = "uploadId1";
    when(uploadService.getUploadResource(eq(uploadId))).thenReturn(new UploadResource(uploadId, "uploaded-file.png"));


    // When
    String attachmentId = newsAttachmentsService.addAttachmentFromUploadedResource(node, uploadId);

    // Then
    assertEquals("attachId1", attachmentId);
  }

  @Test
  public void shouldAddAttachmentFromExistingResourceToNewsHavingAttachments() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(eq(DataDistributionMode.NONE))).thenReturn(dataDistributionType);
    NewsAttachmentsService newsAttachmentsService = new NewsAttachmentsServiceImpl(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    Value[] oldValues = new Value[]{new StringValue("1"),new StringValue("2"),new StringValue("3")};
    Value[] newValues = new Value[]{new StringValue("1"),new StringValue("2"),new StringValue("3"),new StringValue("4")};
    Property attachmentsIdsProperty = mock(Property.class);
    when(attachmentsIdsProperty.getValues()).thenReturn(oldValues);
    when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(true);
    when(node.getProperty(eq("exo:attachmentsIds"))).thenReturn(attachmentsIdsProperty);
    when(node.getSession()).thenReturn(session);
    String uploadId = "4";

    // When
    newsAttachmentsService.addAttachmentFromExistingResource(node, uploadId);

    // Then
    verify(node, times(1)).setProperty(eq("exo:attachmentsIds"), eq(newValues));
  }

  @Test
  public void shouldAddAttachmentFromExistingResourceToNewsNotHavingAttachments() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(eq(DataDistributionMode.NONE))).thenReturn(dataDistributionType);
    NewsAttachmentsService newsAttachmentsService = new NewsAttachmentsServiceImpl(sessionProviderService,
            repositoryService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            uploadService,
            documentService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    when(node.hasProperty(eq("exo:attachmentsIds"))).thenReturn(false);
    Value[] newValues = new Value[]{new StringValue("1")};
    when(node.getSession()).thenReturn(session);
    String uploadId = "1";

    // When
    newsAttachmentsService.addAttachmentFromExistingResource(node, uploadId);

    // Then
    verify(node, times(1)).setProperty(eq("exo:attachmentsIds"), eq(newValues));
  }


}
