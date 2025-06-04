"""
Tests for AI Assistant functionality
"""

import pytest
from fastapi import status
from unittest.mock import patch, Mock
import io
from app.routers.ai_assistant import extract_pdf_text, convert_mermaid_to_text

class TestAIAssistant:
    """Test cases for AI Assistant endpoints"""
    
    def test_ask_text_question_success(self, client, sample_text_question, mock_ai_client, db_session):
        """Test successful text question handling"""
        response = client.post("/ai/ask", json=sample_text_question)
        assert response.status_code == status.HTTP_200_OK
        data = response.json()
        assert "answer" in data
        assert data["answer"] == "This is a test AI response."
    
    def test_ask_text_question_empty(self, client, db_session):
        """Test text question with empty content"""
        response = client.post("/ai/ask", json={"question": ""})
        assert response.status_code == status.HTTP_400_BAD_REQUEST
        data = response.json()
        assert "问题内容不能为空" in data["detail"]
    
    def test_ask_with_pdf_success(self, client, sample_pdf_file, mock_ai_client, db_session):
        """Test successful PDF question handling"""
        with patch('app.routers.ai_assistant.extract_pdf_text') as mock_extract:
            mock_extract.return_value = "Sample PDF content for testing"
            
            response = client.post(
                "/ai/ask_with_pdf",
                data={"question": "What is this PDF about?"},
                files={"pdf_file": ("test.pdf", sample_pdf_file, "application/pdf")}
            )
            
            assert response.status_code == status.HTTP_200_OK
            data = response.json()
            assert "answer" in data
            assert "pdf_summary" in data
            assert "已分析" in data["pdf_summary"]
    
    def test_ask_with_pdf_invalid_format(self, client, db_session):
        """Test PDF upload with invalid file format"""
        fake_file = io.BytesIO(b"not a pdf file")
        response = client.post(
            "/ai/ask_with_pdf",
            data={"question": "Test question"},
            files={"pdf_file": ("test.txt", fake_file, "text/plain")}
        )
        
        assert response.status_code == status.HTTP_400_BAD_REQUEST
        data = response.json()
        assert "仅支持PDF格式文件" in data["detail"]
    
    def test_generate_mindmap_success(self, client, sample_pdf_file, mock_ai_client, db_session):
        """Test successful mindmap generation from PDF"""
        with patch('app.routers.ai_assistant.extract_pdf_text') as mock_extract:
            mock_extract.return_value = "Sample PDF content for mindmap"
            
            response = client.post(
                "/ai/generate_mindmap",
                data={"map_type": "mindmap"},
                files={"pdf_file": ("test.pdf", sample_pdf_file, "application/pdf")}
            )
            
            assert response.status_code == status.HTTP_200_OK
            data = response.json()
            assert "diagram" in data
            assert "diagram_type" in data
            assert data["diagram_type"] == "思维导图"
    
    def test_generate_mindmap_flowchart(self, client, sample_pdf_file, mock_ai_client, db_session):
        """Test flowchart generation from PDF"""
        with patch('app.routers.ai_assistant.extract_pdf_text') as mock_extract:
            mock_extract.return_value = "Sample PDF content for flowchart"
            
            response = client.post(
                "/ai/generate_mindmap",
                data={"map_type": "flowchart"},
                files={"pdf_file": ("test.pdf", sample_pdf_file, "application/pdf")}
            )
            
            assert response.status_code == status.HTTP_200_OK
            data = response.json()
            assert data["diagram_type"] == "流程图"
    
    def test_generate_mindmap_invalid_type(self, client, sample_pdf_file, db_session):
        """Test mindmap generation with invalid type"""
        response = client.post(
            "/ai/generate_mindmap",
            data={"map_type": "invalid_type"},
            files={"pdf_file": ("test.pdf", sample_pdf_file, "application/pdf")}
        )
        
        assert response.status_code == status.HTTP_400_BAD_REQUEST
        data = response.json()
        assert "图表类型仅支持" in data["detail"]
    
    def test_generate_truefalse_question(self, client, sample_pdf_file, mock_ai_client, db_session):
        """Test true/false question generation"""
        # Mock the AI response to return properly formatted content
        mock_ai_client.chat.completions.create.return_value.choices[0].message.content = """
        题目：这是一个测试判断题
        
        答案：true
        
        解释：这是测试解释内容
        """
        
        with patch('app.routers.ai_assistant.extract_pdf_text') as mock_extract:
            mock_extract.return_value = "Sample PDF content for true/false question"
            
            response = client.post(
                "/ai/generate_truefalse",
                files={"pdf_file": ("test.pdf", sample_pdf_file, "application/pdf")}
            )
            
            assert response.status_code == status.HTTP_200_OK
            data = response.json()
            assert "question_id" in data
            assert "question" in data
    
    def test_convert_mermaid_to_text(self, client, db_session):
        """Test mermaid diagram conversion to text"""
        mermaid_content = """
        ```mermaid
        mindmap
        root((Main Topic))
            A(Topic A)
            B(Topic B)
        ```
        """
        
        response = client.post(
            "/ai/convert_mermaid_to_text",
            json={"mermaid_content": mermaid_content}
        )
        
        assert response.status_code == status.HTTP_200_OK
        data = response.json()
        assert "text_diagram" in data
    
    def test_convert_mermaid_empty_content(self, client, db_session):
        """Test mermaid conversion with empty content"""
        response = client.post(
            "/ai/convert_mermaid_to_text",
            json={"mermaid_content": ""}
        )
        
        assert response.status_code == status.HTTP_400_BAD_REQUEST
        data = response.json()
        assert "内容不能为空" in data["detail"]

class TestAIHelperFunctions:
    """Test helper functions in AI assistant module"""
    
    def test_extract_pdf_text(self, sample_pdf_file):
        """Test PDF text extraction function"""
        from unittest.mock import Mock
        
        # Create a mock UploadFile
        mock_file = Mock()
        mock_file.file.read.return_value = sample_pdf_file.read()
        
        with patch('app.routers.ai_assistant.PyPDF2.PdfReader') as mock_reader:
            mock_page = Mock()
            mock_page.extract_text.return_value = "Test PDF content"
            mock_reader.return_value.pages = [mock_page]
            
            result = extract_pdf_text(mock_file)
            assert result == "Test PDF content"
    
    def test_convert_mermaid_to_text_function(self):
        """Test mermaid to text conversion function"""
        mermaid_input = """
        ```mermaid
        mindmap
        root((Main Topic))
            A(Topic A)
            B(Topic B)
        ```
        """
        
        result = convert_mermaid_to_text(mermaid_input)
        assert isinstance(result, str)
        assert len(result) > 0 