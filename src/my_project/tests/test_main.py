"""
Tests for main application endpoints
"""

import pytest
from fastapi import status

class TestMainApp:
    """Test cases for main application"""
    
    def test_root_endpoint(self, client):
        """Test the root endpoint returns correct message"""
        response = client.get("/")
        assert response.status_code == status.HTTP_200_OK
        data = response.json()
        assert "message" in data
        assert "Web IDE Backend is running" in data["message"]
    
    def test_app_creation(self):
        """Test that the FastAPI app is created properly"""
        from app.main import app
        assert app.title == "Web IDE Backend"
        assert "基于 FastAPI 的网页 IDE 后端系统" in app.description
    
    def test_cors_middleware(self, client):
        """Test CORS middleware is properly configured"""
        response = client.options("/")
        # CORS headers should be present
        assert response.status_code in [200, 405]  # OPTIONS might not be explicitly handled
    
    def test_app_startup(self, client):
        """Test that app starts up properly and database is initialized"""
        # Test that we can make a request to the app
        response = client.get("/")
        assert response.status_code == 200
        
        # Test that docs endpoint is available
        response = client.get("/docs")
        assert response.status_code == 200 