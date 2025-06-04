"""
Tests for database models
"""

import pytest
from sqlalchemy.exc import IntegrityError
from app import models
import datetime

class TestModels:
    """Test cases for database models"""
    
    def test_ai_request_record_creation(self, db_session):
        """Test creating an AI request record"""
        record = models.AIRequestRecord(
            question="Test question",
            answer="Test answer"
        )
        
        db_session.add(record)
        db_session.commit()
        
        # Verify the record was created
        assert record.id is not None
        assert record.question == "Test question"
        assert record.answer == "Test answer"
    
    def test_ai_request_record_required_fields(self, db_session):
        """Test that required fields are enforced"""
        # Test without question (should fail since question is NOT NULL)
        with pytest.raises(IntegrityError):
            record = models.AIRequestRecord(answer="Test answer")
            db_session.add(record)
            db_session.commit()
        
        # Rollback the failed transaction
        db_session.rollback()
        
        # Test without answer (should fail since answer is NOT NULL)  
        with pytest.raises(IntegrityError):
            record = models.AIRequestRecord(question="Test question")
            db_session.add(record)
            db_session.commit()
        
        # Rollback the failed transaction
        db_session.rollback()
    
    def test_ai_request_record_long_text(self, db_session):
        """Test AI request record with long text"""
        long_question = "A" * 1000
        long_answer = "B" * 2000
        
        record = models.AIRequestRecord(
            question=long_question,
            answer=long_answer
        )
        
        db_session.add(record)
        db_session.commit()
        
        assert record.question == long_question
        assert record.answer == long_answer
    
    def test_ai_request_record_query(self, db_session):
        """Test querying AI request records"""
        # Create multiple records
        records = [
            models.AIRequestRecord(question=f"Question {i}", answer=f"Answer {i}")
            for i in range(3)
        ]
        
        for record in records:
            db_session.add(record)
        db_session.commit()
        
        # Query all records
        all_records = db_session.query(models.AIRequestRecord).all()
        assert len(all_records) == 3
        
        # Query specific record
        specific_record = db_session.query(models.AIRequestRecord).filter_by(
            question="Question 1"
        ).first()
        assert specific_record is not None
        assert specific_record.answer == "Answer 1"
    
    def test_ai_request_record_update(self, db_session):
        """Test updating an AI request record"""
        record = models.AIRequestRecord(
            question="Original question",
            answer="Original answer"
        )
        
        db_session.add(record)
        db_session.commit()
        
        # Update the record
        record.question = "Updated question"
        record.answer = "Updated answer"
        db_session.commit()
        
        # Verify the update
        updated_record = db_session.query(models.AIRequestRecord).filter_by(
            id=record.id
        ).first()
        assert updated_record.question == "Updated question"
        assert updated_record.answer == "Updated answer"
    
    def test_ai_request_record_deletion(self, db_session):
        """Test deleting an AI request record"""
        record = models.AIRequestRecord(
            question="To be deleted",
            answer="Will be removed"
        )
        
        db_session.add(record)
        db_session.commit()
        record_id = record.id
        
        # Delete the record
        db_session.delete(record)
        db_session.commit()
        
        # Verify deletion
        deleted_record = db_session.query(models.AIRequestRecord).filter_by(
            id=record_id
        ).first()
        assert deleted_record is None
    
    def test_model_relationships(self, db_session):
        """Test model relationships if any exist"""
        # This test can be expanded when more models are added
        record = models.AIRequestRecord(
            question="Relationship test",
            answer="Testing relationships"
        )
        
        db_session.add(record)
        db_session.commit()
        
        # Test basic functionality
        assert record.id is not None
    
    def test_model_string_representation(self, db_session):
        """Test string representation of models"""
        record = models.AIRequestRecord(
            question="String test",
            answer="Testing string representation"
        )
        
        db_session.add(record)
        db_session.commit()
        
        # Test that string representation works (doesn't crash)
        str_repr = str(record)
        assert isinstance(str_repr, str)
        assert len(str_repr) > 0
    
    def test_user_model_basic(self, db_session):
        """Test User model basic functionality"""
        user = models.User(
            username="testuser",
            hashed_password="hashed_password_here",
            role="student"
        )
        
        db_session.add(user)
        db_session.commit()
        
        assert user.id is not None
        assert user.username == "testuser"
        assert user.role == "student"
    
    def test_codeblock_model_basic(self, db_session):
        """Test CodeBlock model basic functionality""" 
        # First create a user and PPT for foreign key relations
        user = models.User(
            username="testteacher",
            hashed_password="hashed_password",
            role="teacher"
        )
        db_session.add(user)
        db_session.commit()
        
        ppt = models.PPT(
            name="Test PPT",
            file_path="/path/to/test.ppt",
            uploaded_by=user.id
        )
        db_session.add(ppt)
        db_session.commit()
        
        # Now create a codeblock
        codeblock = models.CodeBlock(
            ppt_id=ppt.id,
            page_number=1,
            language="python",
            content="print('hello world')"
        )
        
        db_session.add(codeblock)
        db_session.commit()
        
        assert codeblock.id is not None
        assert codeblock.language == "python"
        assert codeblock.page_number == 1