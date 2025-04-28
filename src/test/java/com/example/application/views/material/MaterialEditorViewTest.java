package com.example.application.views.material;

import com.example.application.data.LearningMaterial;
import com.example.application.data.User;
import com.example.application.security.SecurityService;
import com.example.application.services.LearningMaterialService;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.FileData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MaterialEditorViewTest {

    @Mock private LearningMaterialService materialService;
    @Mock private SecurityService securityService;
    @Mock private UserDetails userDetails;
    @Mock private MemoryBuffer buffer; // Mock MemoryBuffer
    @Mock private FileData fileData; // Mock FileData

    private MaterialEditorView view;
    
    // Argument captors for verification
    @Captor ArgumentCaptor<LearningMaterial> materialCaptor;
    @Captor ArgumentCaptor<InputStream> inputStreamCaptor;
    @Captor ArgumentCaptor<String> fileNameCaptor;
    @Captor ArgumentCaptor<String> mimeTypeCaptor;
    @Captor ArgumentCaptor<Long> fileSizeCaptor;
    @Captor ArgumentCaptor<User> userCaptor;

    @Test
    void testFormValidation_ValidInput() {
        // Setup
        view.getTitleField().setValue("Test Title");
        view.getDescriptionField().setValue("Test Description");
        
        // Create a mock file upload
        try (InputStream inputStream = new ByteArrayInputStream("test content".getBytes())) {
            view.getFileUpload().getElement().setProperty("files", "test.txt");
            
            // Test
            assertTrue(view.isFormValid());
        } catch (IOException e) {
            fail("IOException occurred: " + e.getMessage());
        }
    }

    @Test
    void testFormValidation_InvalidInput() {
        // Test empty title
        view.getTitleField().setValue("");
        view.getDescriptionField().setValue("Test Description");
        assertFalse(view.isFormValid());

        // Test empty description
        view.getTitleField().setValue("Test Title");
        view.getDescriptionField().setValue("");
        assertFalse(view.isFormValid());

        // Test no file uploaded
        view.getTitleField().setValue("Test Title");
        view.getDescriptionField().setValue("Test Description");
        assertFalse(view.isFormValid());
    }

    @Test
    void testSaveMaterial() throws Exception { // Add throws Exception
        // Setup User
        when(securityService.getAuthenticatedUser()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        User mockUser = new User(); // Assuming findUserByUsername returns a User
        mockUser.setUsername("testuser");
        // Mock UserService if needed within MaterialEditorView's constructor/methods 
        // (Currently MaterialEditorView only uses SecurityService to get username)
        
        // Setup Buffer Mocks for file upload scenario
        String testContent = "test file content";
        byte[] fileBytes = testContent.getBytes();
        String fileName = "test.txt";
        String mimeType = "text/plain";
        InputStream testInputStream = new ByteArrayInputStream(fileBytes);

        when(buffer.getInputStream()).thenReturn(new ByteArrayInputStream(fileBytes)); // Return new stream each time
        when(buffer.getFileData()).thenReturn(fileData);
        when(buffer.getFileName()).thenReturn(fileName);
        when(fileData.getMimeType()).thenReturn(mimeType);
        // No direct getLength in FileData mock, we calculate from bytes

        // Set form fields
        view.getTitleField().setValue("Test Title");
        view.getDescriptionField().setValue("Test Description");
        
        // Trigger save
        view.getSaveButton().click(); // Assuming getSaveButton() exists or trigger saveMaterial directly
        
        // Verify that the material service is called with correct arguments
        verify(materialService).saveLearningMaterial(
            materialCaptor.capture(), 
            inputStreamCaptor.capture(), 
            fileNameCaptor.capture(), 
            mimeTypeCaptor.capture(), 
            fileSizeCaptor.capture(), 
            userCaptor.capture()
        );

        // Assert captured values
        assertEquals("Test Title", materialCaptor.getValue().getTitle());
        assertEquals("Test Description", materialCaptor.getValue().getDescription());
        assertEquals(fileName, fileNameCaptor.getValue());
        assertEquals(mimeType, mimeTypeCaptor.getValue());
        assertEquals(fileBytes.length, fileSizeCaptor.getValue());
        // Assert InputStream content if needed (read bytes and compare)
        assertArrayEquals(fileBytes, inputStreamCaptor.getValue().readAllBytes());
        assertEquals(mockUser.getUsername(), userCaptor.getValue().getUsername());

    }

    @Test
    void testSaveMaterial_NoFile() throws Exception {
        // Setup User
        when(securityService.getAuthenticatedUser()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        User mockUser = new User(); 
        mockUser.setUsername("testuser");
        
        // Setup Buffer Mocks for NO file upload scenario
        when(buffer.getInputStream()).thenReturn(null);
        when(buffer.getFileData()).thenReturn(null);
        when(buffer.getFileName()).thenReturn(null);

        // Set form fields
        view.getTitleField().setValue("No File Title");
        view.getDescriptionField().setValue("No File Description");
        
        // Trigger save
        view.getSaveButton().click();
        
        // Verify service call with null/0 for file details
        verify(materialService).saveLearningMaterial(
            materialCaptor.capture(), 
            isNull(), // Expect null InputStream
            isNull(), // Expect null filename
            isNull(), // Expect null mimeType
            eq(0L),   // Expect 0 fileSize
            userCaptor.capture()
        );

        assertEquals("No File Title", materialCaptor.getValue().getTitle());
        assertEquals("No File Description", materialCaptor.getValue().getDescription());
        assertEquals(mockUser.getUsername(), userCaptor.getValue().getUsername());
    }

    @Test
    void testClearForm() {
        // Setup
        view.getTitleField().setValue("Test Title");
        view.getDescriptionField().setValue("Test Description");
        
        // Test
        view.getTitleField().clear();
        view.getDescriptionField().clear();
        view.getFileUpload().getElement().setProperty("files", "");
        
        // Verify
        assertTrue(view.getTitleField().isEmpty());
        assertTrue(view.getDescriptionField().isEmpty());
        assertTrue(view.getFileUpload().getElement().getProperty("files").isEmpty());
    }

    // Helper methods in MaterialEditorView needed for testing:
    // public void setBufferForTesting(MemoryBuffer buffer) { this.buffer = buffer; }
    // public Button getSaveButton() { /* return reference to save button */ }
} 