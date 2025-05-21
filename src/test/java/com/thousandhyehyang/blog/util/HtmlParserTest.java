package com.thousandhyehyang.blog.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HtmlParserTest {

    @Test
    void extractText_shouldRemoveHtmlTags() {
        // Given
        String html = "<p>This is a <strong>test</strong> paragraph.</p>";
        
        // When
        String result = HtmlParser.extractText(html);
        
        // Then
        assertEquals("This is a test paragraph.", result);
    }
    
    @Test
    void extractText_shouldLimitTo200Characters() {
        // Given
        StringBuilder longHtml = new StringBuilder("<p>");
        for (int i = 0; i < 50; i++) {
            longHtml.append("This is a very long text. ");
        }
        longHtml.append("</p>");
        
        // When
        String result = HtmlParser.extractText(longHtml.toString());
        
        // Then
        assertEquals(200, result.length());
    }
    
    @Test
    void extractText_shouldHandleEmptyInput() {
        // Given
        String html = "";
        
        // When
        String result = HtmlParser.extractText(html);
        
        // Then
        assertEquals("", result);
    }
    
    @Test
    void extractText_shouldHandleNullInput() {
        // Given
        String html = null;
        
        // When
        String result = HtmlParser.extractText(html);
        
        // Then
        assertEquals("", result);
    }
    
    @Test
    void extractText_shouldHandleComplexHtml() {
        // Given
        String html = "<div><h1>Title</h1><p>This is a paragraph with <a href='#'>link</a> and <img src='image.jpg' alt='image'/>.</p><ul><li>Item 1</li><li>Item 2</li></ul></div>";
        
        // When
        String result = HtmlParser.extractText(html);
        
        // Then
        assertEquals("Title This is a paragraph with link and . Item 1 Item 2", result);
    }
}