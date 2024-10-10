package main;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    void save_shouldCreateDocumentWithGeneratedId() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Test Document")
                .content("This is a test document")
                .author(new DocumentManager.Author(UUID.randomUUID().toString(), "Author"))
                .created(Instant.now())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        Assert.assertNotNull(savedDocument.getId());
        Assert.assertEquals("Test Document", document.getTitle());
    }

    @Test
    void save_shouldUpsertDocument() {
        String documentId = UUID.randomUUID().toString();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id(documentId)
                .title("Test Title")
                .content("Test Content")
                .author(new DocumentManager.Author(UUID.randomUUID().toString(), "Author"))
                .created(Instant.now())
                .build();

        documentManager.save(document);

        DocumentManager.Document updatedDocument = DocumentManager.Document.builder()
                .id(documentId)
                .title("Updated Title")
                .content("Updated Content")
                .author(document.getAuthor())
                .created(document.getCreated())
                .build();

        documentManager.save(updatedDocument);


        Optional<DocumentManager.Document> result = documentManager.findById(documentId);
        Assert.assertNotNull(result);
        Assert.assertEquals("Updated Title", result.get().getTitle());
        Assert.assertEquals("Updated Content", result.get().getContent());
    }

    @Test
    void findById_shouldReturnDocumentIfFound() {
        String documentId = UUID.randomUUID().toString();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id(documentId)
                .title("Test Document")
                .content("Content")
                .author(new DocumentManager.Author(UUID.randomUUID().toString(), "Author"))
                .created(Instant.now())
                .build();

        documentManager.save(document);

        Optional<DocumentManager.Document> foundDocument = documentManager.findById(documentId);
        Assert.assertNotNull(foundDocument);
        Assert.assertEquals("Test Document", foundDocument.get().getTitle());
    }

    @Test
    void findById_shouldReturnEmptyIfNotFound() {
        Optional<DocumentManager.Document> foundDocument = documentManager.findById("non-existent-id");
        Assert.assertFalse(foundDocument.isPresent());
    }

    @Test
    void search_shouldReturnDocumentsByTitlePrefix() {
        DocumentManager.Document doc1 = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Title A")
                .content("Content 1")
                .author(new DocumentManager.Author(UUID.randomUUID().toString(), "Author A"))
                .created(Instant.now())
                .build();

        DocumentManager.Document doc2 = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Title B")
                .content("Content 2")
                .author(new DocumentManager.Author(UUID.randomUUID().toString(), "Author B"))
                .created(Instant.now())
                .build();

        documentManager.save(doc1);
        documentManager.save(doc2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Title A"))
                .build();

        List<DocumentManager.Document> result = documentManager.search(request);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("Title A", result.get(0).getTitle());
    }

    @Test
    void search_shouldReturnDocumentsByAuthorId() {
        String authorId = UUID.randomUUID().toString();
        DocumentManager.Document doc1 = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Title A")
                .content("Content 1")
                .author(new DocumentManager.Author(authorId, "Author A"))
                .created(Instant.now())
                .build();

        DocumentManager.Document doc2 = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Title B")
                .content("Content 2")
                .author(new DocumentManager.Author(UUID.randomUUID().toString(), "Author B"))
                .created(Instant.now())
                .build();

        documentManager.save(doc1);
        documentManager.save(doc2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .authorIds(List.of(authorId))
                .build();

        List<DocumentManager.Document> result = documentManager.search(request);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(authorId, result.get(0).getAuthor().getId());
    }

    @Test
    void search_shouldReturnDocumentsByCreatedRange() {
        Instant now = Instant.now();

        DocumentManager.Document doc1 = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Title A")
                .content("Content 1")
                .author(new DocumentManager.Author(UUID.randomUUID().toString(), "Author A"))
                .created(now.minusSeconds(3600))
                .build();

        DocumentManager.Document doc2 = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Title B")
                .content("Content 2")
                .author(new DocumentManager.Author(UUID.randomUUID().toString(), "Author B"))
                .created(now.plusSeconds(3600))
                .build();

        documentManager.save(doc1);
        documentManager.save(doc2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdTo(now.plusSeconds(3600))
                .build();

        List<DocumentManager.Document> result = documentManager.search(request);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("Title A", result.get(0).getTitle());
    }

    @Test
    void search_shouldReturnEmptyListIfNoDocumentsMatch() {
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Non-existent"))
                .build();

        List<DocumentManager.Document> result = documentManager.search(request);

        Assert.assertEquals(0 , result.size());
    }
}
