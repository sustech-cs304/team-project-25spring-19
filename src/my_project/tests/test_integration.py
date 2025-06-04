"""
Integration tests for Web IDE PDF functionality
"""

import pytest
from fastapi import status
from unittest.mock import patch, Mock
import io
import uuid

class TestIntegration:
    """Integration test cases"""
    
    def test_complete_pdf_workflow(self, client, sample_pdf_file, mock_ai_client, db_session):
        """Test complete PDF processing workflow"""
        
        # Step 1: Upload PDF and ask question
        with patch('app.routers.ai_assistant.extract_pdf_text') as mock_extract:
            mock_extract.return_value = "This is a test document about machine learning."
            
            response = client.post(
                "/ai/ask_with_pdf",
                data={"question": "What is this document about?"},
                files={"pdf_file": ("test.pdf", sample_pdf_file, "application/pdf")}
            )
            
            assert response.status_code == status.HTTP_200_OK
            data = response.json()
            assert "answer" in data
            assert "pdf_summary" in data
        
        # Step 2: Generate mindmap from same PDF
        sample_pdf_file.seek(0)  # Reset file pointer
        with patch('app.routers.ai_assistant.extract_pdf_text') as mock_extract:
            mock_extract.return_value = "This is a test document about machine learning."
            
            response = client.post(
                "/ai/generate_mindmap",
                data={"map_type": "mindmap"},
                files={"pdf_file": ("test.pdf", sample_pdf_file, "application/pdf")}
            )
            
            assert response.status_code == status.HTTP_200_OK
            data = response.json()
            assert "diagram" in data
            assert data["diagram_type"] == "思维导图"
        
        # Step 3: Generate true/false question
        sample_pdf_file.seek(0)
        mock_ai_client.chat.completions.create.return_value.choices[0].message.content = """
        题目：机器学习是人工智能的一个分支
        
        答案：true
        
        解释：机器学习确实是人工智能的一个重要分支
        """
        
        with patch('app.routers.ai_assistant.extract_pdf_text') as mock_extract:
            mock_extract.return_value = "This is a test document about machine learning."
            
            response = client.post(
                "/ai/generate_truefalse",
                files={"pdf_file": ("test.pdf", sample_pdf_file, "application/pdf")}
            )
            
            assert response.status_code == status.HTTP_200_OK
            data = response.json()
            assert "question_id" in data
            assert "question" in data
            question_id = data["question_id"]
        
        # Step 4: Get answer for the true/false question
        response = client.get(f"/ai/truefalse/{question_id}/answer")
        assert response.status_code == status.HTTP_200_OK
        data = response.json()
        assert "answer" in data
        assert isinstance(data["answer"], bool)
        
        # Step 5: Get explanation for the true/false question
        response = client.get(f"/ai/truefalse/{question_id}/explanation")
        assert response.status_code == status.HTTP_200_OK
        data = response.json()
        assert "explanation" in data
    
    def test_error_handling_workflow(self, client, db_session):
        """Test error handling in various scenarios"""
        
        # Test invalid file format
        fake_file = io.BytesIO(b"not a pdf")
        response = client.post(
            "/ai/ask_with_pdf",
            data={"question": "Test"},
            files={"pdf_file": ("test.txt", fake_file, "text/plain")}
        )
        assert response.status_code == status.HTTP_400_BAD_REQUEST
        
        # Test empty question
        response = client.post("/ai/ask", json={"question": ""})
        assert response.status_code == status.HTTP_400_BAD_REQUEST
        
        # Test invalid true/false question ID
        invalid_id = str(uuid.uuid4())
        response = client.get(f"/ai/truefalse/{invalid_id}/answer")
        assert response.status_code == status.HTTP_404_NOT_FOUND
    
    def test_api_documentation_access(self, client):
        """Test that API documentation is accessible"""
        response = client.get("/docs")
        assert response.status_code == status.HTTP_200_OK
        
        response = client.get("/redoc")
        assert response.status_code == status.HTTP_200_OK
        
        response = client.get("/openapi.json")
        assert response.status_code == status.HTTP_200_OK
    
    def test_database_persistence(self, client, mock_ai_client, db_session):
        """Test that data is properly persisted in database"""
        from app import models
        
        # Make a request that should create a database record
        response = client.post("/ai/ask", json={"question": "Test persistence"})
        assert response.status_code == status.HTTP_200_OK
        
        # Check that record was created in database
        records = db_session.query(models.AIRequestRecord).all()
        assert len(records) > 0
        
        # Verify the record content
        record = records[0]
        assert record.question == "Test persistence"
        assert record.answer == "This is a test AI response."
    
    def test_concurrent_requests(self, client, mock_ai_client, db_session):
        """Test handling of concurrent requests"""
        import threading
        import time
        
        results = []
        
        def make_request():
            try:
                response = client.post("/ai/ask", json={"question": "Concurrent test"})
                results.append(response.status_code)
            except Exception as e:
                results.append(str(e))
        
        # Create multiple threads
        threads = []
        for i in range(5):
            thread = threading.Thread(target=make_request)
            threads.append(thread)
        
        # Start all threads
        for thread in threads:
            thread.start()
        
        # Wait for all threads to complete
        for thread in threads:
            thread.join()
        
        # Check results
        assert len(results) == 5
        success_count = sum(1 for result in results if result == 200)
        assert success_count >= 3  # Allow for some failures in concurrent testing

class TestPerformance:
    """Performance-related tests"""
    
    def test_response_time_text_question(self, client, mock_ai_client, db_session):
        """Test response time for text questions"""
        import time
        
        start_time = time.time()
        response = client.post("/ai/ask", json={"question": "Performance test"})
        end_time = time.time()
        
        assert response.status_code == status.HTTP_200_OK
        response_time = end_time - start_time
        assert response_time < 5.0  # Should respond within 5 seconds
    
    def test_large_pdf_handling(self, client, mock_ai_client, db_session):
        """Test handling of large PDF files"""
        # Create a larger mock PDF content
        large_content = b"Mock PDF content " * 1000
        large_pdf = io.BytesIO(large_content)
        
        with patch('app.routers.ai_assistant.extract_pdf_text') as mock_extract:
            mock_extract.return_value = "Large PDF content " * 500
            
            response = client.post(
                "/ai/ask_with_pdf",
                data={"question": "Summarize this large PDF"},
                files={"pdf_file": ("large.pdf", large_pdf, "application/pdf")}
            )
            
            assert response.status_code == status.HTTP_200_OK 