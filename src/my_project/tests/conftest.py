"""
Pytest configuration and fixtures for Web IDE PDF tests
"""

import pytest
import tempfile
import os
from pathlib import Path
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from fastapi.testclient import TestClient
from unittest.mock import Mock, patch
import io

# Import our app modules
import sys
sys.path.insert(0, str(Path(__file__).parent.parent))

from app.main import app
from app.database import Base
from app import models

# Test database setup
SQLALCHEMY_DATABASE_URL = "sqlite:///./test.db"
engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def override_get_db():
    """Override database dependency for testing"""
    try:
        db = TestingSessionLocal()
        yield db
    finally:
        db.close()

# Check if get_db exists in auth module and override it
try:
    from app.auth import get_db
    app.dependency_overrides[get_db] = override_get_db
except ImportError:
    # get_db doesn't exist in auth, that's ok for now
    pass

@pytest.fixture(scope="function")
def db_session():
    """Create a fresh database session for each test"""
    Base.metadata.create_all(bind=engine)
    session = TestingSessionLocal()
    try:
        yield session
    finally:
        session.close()
        Base.metadata.drop_all(bind=engine)

@pytest.fixture(scope="function")
def client():
    """Create a test client for each test"""
    with TestClient(app) as test_client:
        yield test_client

@pytest.fixture
def sample_pdf_file():
    """Create a sample PDF file for testing"""
    # Create a simple PDF-like content for testing
    pdf_content = b"""%PDF-1.4
1 0 obj
<<
/Type /Catalog
/Pages 2 0 R
>>
endobj

2 0 obj
<<
/Type /Pages
/Kids [3 0 R]
/Count 1
>>
endobj

3 0 obj
<<
/Type /Page
/Parent 2 0 R
/MediaBox [0 0 612 792]
/Contents 4 0 R
>>
endobj

4 0 obj
<<
/Length 44
>>
stream
BT
/F1 12 Tf
100 700 Td
(Hello World Test) Tj
ET
endstream
endobj

xref
0 5
0000000000 65535 f 
0000000009 00000 n 
0000000058 00000 n 
0000000115 00000 n 
0000000206 00000 n 
trailer
<<
/Size 5
/Root 1 0 R
>>
startxref
300
%%EOF"""
    
    return io.BytesIO(pdf_content)

@pytest.fixture
def mock_openai_response():
    """Mock OpenAI API response"""
    mock_response = Mock()
    mock_response.choices = [Mock()]
    mock_response.choices[0].message = Mock()
    mock_response.choices[0].message.content = "This is a test AI response."
    return mock_response

@pytest.fixture
def mock_ai_client(mock_openai_response):
    """Mock AI client for testing"""
    with patch('app.routers.ai_assistant.get_ai_client') as mock_client:
        mock_instance = Mock()
        mock_instance.chat.completions.create.return_value = mock_openai_response
        mock_client.return_value = mock_instance
        yield mock_instance

@pytest.fixture
def sample_text_question():
    """Sample text question for testing"""
    return {"question": "What is the capital of France?"}

@pytest.fixture
def sample_user_data():
    """Sample user data for testing"""
    return {
        "username": "testuser",
        "email": "test@example.com",
        "password": "testpassword123"
    } 